package com.example.commitmate.todo;

import com.example.commitmate.user.User;
import com.example.commitmate.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class TodoController {

    private final TodoService ts;


    @GetMapping("/groups/{groupId}/todo-form")
    public String saveTodoFormPage(@PathVariable("groupId") Integer groupId,
                                   Model model,
                                   HttpSession session) {

        User sessionUser = (User) session.getAttribute("sessionUser");
        model.addAttribute("groupId", groupId);
        model.addAttribute("sessionUser", sessionUser);


        return "todo/save-form";
    }


    @PostMapping("/groups/{groupId}/todo/save")
    public String saveTodoProc(@PathVariable("groupId") Integer groupId,
                               @ModelAttribute TodoRequest.AddDTO addDTO,
                               HttpSession session

                              ) {
        User sessionUser = (User) session.getAttribute("sessionUser");

        addDTO.setGroupId(groupId);
        addDTO.setUserId(sessionUser.getId());


        ts.addTodo(addDTO);

        return "redirect:/groups/" + groupId;

    }

    @PostMapping("/todo/{id}/delete")
    public String deleteTodoProc(@PathVariable("id") Integer id,
                                 Integer groupId,
                                 HttpSession session) {

        User sessionUser = (User) session.getAttribute("sessionUser");

        ts.deleteTodo(id,sessionUser.getId());

        return "redirect:/groups/" + groupId;
    }

    @GetMapping("/todo/{id}/toggle")
    public String todoToggle(@PathVariable("id")Integer id,
                             Integer groupId,
                             HttpSession session) {

        User sissionUser = (User) session.getAttribute("sessionUser");

        ts.toggleDone(id,sissionUser.getId());

        return "redirect:/groups/" + groupId;
    }
}
