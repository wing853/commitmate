package com.example.commitmate.user;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService us;

    @GetMapping("/login-form")
    public String loginFormPage() {

        return "user/login-form";
    }

    @PostMapping("/login")
    public String loginProc(UserRequest.LoginDTO loginDTO, HttpSession session, Model model) {

        loginDTO.validate();
        User user = us.login(loginDTO);
        session.setAttribute("sessionUser", user);

        return "redirect:/groups";
    }

    @GetMapping("/login/oauth2/google")
    public String googleLogin(@AuthenticationPrincipal OAuth2User oAuth2User,
                              HttpSession session) {
        String providerId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = us.findOrCreateSocialUser(
                AuthProvider.GOOGLE, providerId, email, name);
        session.setAttribute("sessionUser", user);

        return "redirect:/groups";
    }

    @GetMapping("/signup-form")
    public String signupFormPage() {
        return "user/signup-form";
    }

    @PostMapping("/signup")
    public String signupProc(UserRequest.SignupDTO signupDTO) {
        signupDTO.validate();
        us.signup(signupDTO);
        return "redirect:/login-form";

    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        if(session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    @GetMapping("/user/profile")
    public String updateFormPage(HttpSession session,Model model) {

        User sessionUser = (User) session.getAttribute("sessionUser");
        model.addAttribute("sessionUser", sessionUser);
        return "user/update-form";
    }

    @PostMapping("/user/profile/update")
    public String updateProfile(@RequestParam(value = "nickname", required = false) String nickname,
                                @RequestParam(value = "currentPassword", required = false) String currentPassword,
                                @RequestParam(value = "newPassword", required = false) String newPassword,
                                @RequestParam(value = "newPasswordConfirm", required = false) String newPasswordConfirm,
                                HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        us.updateProfile(sessionUser.getId(), nickname, currentPassword, newPassword, newPasswordConfirm);
        User updatedUser = us.findById(sessionUser.getId());
        session.setAttribute("sessionUser", updatedUser);
        return "redirect:/user/profile";
    }

    @PostMapping("/user/point/charge")
    @ResponseBody
    public ResponseEntity<Void> chargePoint(@RequestBody Map<String, Integer> body,
                                            HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        int amount = body.get("amount");
        us.chargePoint(sessionUser.getId(), amount);
        User updatedUser = us.findById(sessionUser.getId());
        session.setAttribute("sessionUser", updatedUser);
        return ResponseEntity.ok().build();
    }
}