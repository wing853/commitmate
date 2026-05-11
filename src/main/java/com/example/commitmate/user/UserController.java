package com.example.commitmate.user;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


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

        return "redirect:/todo-list";
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

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if(session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }
}