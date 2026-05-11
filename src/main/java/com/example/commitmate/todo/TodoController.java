package com.example.commitmate.todo;

import com.example.commitmate.user.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TodoController {

    @GetMapping("/todo-list")
    public String todoListPage(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if(sessionUser == null) {
            return "redirect:/login-form";
        }

        model.addAttribute("sessionUser",sessionUser);

        return "todo/todo-list";
    }

}
