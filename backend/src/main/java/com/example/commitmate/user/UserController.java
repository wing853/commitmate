package com.example.commitmate.user;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;


@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService us;
    private final PasswordResetService passwordResetService;
    private final PhoneVerificationService phoneVerificationService;
    private final MobileOAuthService mobileOAuthService;

    @GetMapping("/mobile/oauth2/google")
    public String mobileGoogleLogin(HttpSession session) {
        session.setAttribute("mobileOAuth", true);
        return "redirect:/oauth2/authorization/google";
    }

    @PostMapping("/api/phone-verifications/send")
    @ResponseBody
    public ResponseEntity<Map<String, String>> sendPhoneVerification(@RequestBody Map<String, String> body) {
        try {
            phoneVerificationService.sendCode(body.get("phoneNumber"));
            return ResponseEntity.ok(Map.of("message", "인증번호를 발송했습니다."));
        } catch (com.example.commitmate.core.errors.ExceptionInput e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/phone-verifications/verify")
    @ResponseBody
    public ResponseEntity<Map<String, String>> verifyPhoneNumber(@RequestBody Map<String, String> body) {
        try {
            String token = phoneVerificationService.verifyCode(body.get("phoneNumber"), body.get("code"));
            return ResponseEntity.ok(Map.of("message", "휴대폰 인증이 완료되었습니다.", "verificationToken", token));
        } catch (com.example.commitmate.core.errors.ExceptionInput e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/login-form")
    public String loginFormPage(@RequestParam(value = "reset", required = false) String reset,
                                Model model) {
        model.addAttribute("resetSuccess", "success".equals(reset));
        return "user/login-form";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "user/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestPasswordReset(@RequestParam String email,
                                       jakarta.servlet.http.HttpServletRequest request,
                                       Model model) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .replaceQuery(null)
                .build()
                .toUriString();
        passwordResetService.requestReset(email, baseUrl);
        model.addAttribute("submitted", true);
        return "user/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam String token, Model model) {
        model.addAttribute("validToken", passwordResetService.isValid(token));
        model.addAttribute("token", token);
        return "user/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String password,
                                @RequestParam String passwordConfirm) {
        passwordResetService.resetPassword(token, password, passwordConfirm);
        return "redirect:/login-form?reset=success";
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

        if (Boolean.TRUE.equals(session.getAttribute("mobileOAuth"))) {
            session.removeAttribute("mobileOAuth");
            return "redirect:commitmate://oauth/google?code=" + mobileOAuthService.issue(user);
        }

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
