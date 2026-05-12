package com.example.commitmate.group;

import com.example.commitmate.todo.TodoService;
import com.example.commitmate.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GroupController {

    private final GroupService gs;
    private final TodoService ts;

    @GetMapping("/group-list")
    public String groupListPage(Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/login-form";
        }

        List<GroupResponse.MyGroupDTO> groups = gs.getMyGroups(sessionUser.getId());
        model.addAttribute("groups", groups);
        model.addAttribute("sessionUser", sessionUser);

        return "group/group-list";
    }

    @GetMapping("/group-create")
    public String groupCreateFormPage(HttpSession session) {

        User sessionUser = (User) session.getAttribute("sessionUser");

        if (sessionUser == null) {
            return "redirect:/login-form";
        }
        return "group/create-form";
    }

    @PostMapping("/create")
    public String groupCreateProc(Model model, HttpSession session, GroupRequest.CreateDTO createDTO) {

        User userEntity = (User) session.getAttribute("sessionUser");

        if (userEntity == null) {
            return "redirect:/login-form";
        }

        createDTO.validate();
        gs.createRoom(userEntity, createDTO);

        return "redirect:/group-list";
    }

    @GetMapping("/group/{id}/edit-form")
    public String groupUpdatePageForm(@PathVariable("id") Integer id,
                                      HttpSession session,
                                      Model model) {
        User sessionUser = (User) session.getAttribute("sessionUser");

        if (sessionUser == null) {
            return "redirect:/login-form";
        }

        GroupResponse.detailDTO group = gs.findGroupById(id);
        model.addAttribute("group",group);

        return "group/update-form";
    }

    @PostMapping("/group/{id}/edit")
    public String groupUpdateProc(@PathVariable("id") Integer id, HttpSession session, GroupRequest.UpdateDTO updateDTO) {
        User sessionUser = (User) session.getAttribute("sessionUser");

        if (sessionUser == null) {
            return "redirect:/login-form";
        }
        updateDTO.validate();
        gs.updateGroup(id,updateDTO);
        return "redirect:/group-list";
    }

    @PostMapping("/group/{id}/delete")
    public String groupDeleteProc(@PathVariable("id") Integer id,
                                  HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");

        if (sessionUser == null) {
            return "redirect:/login-form";
        }

        gs.deleteGroup(id,sessionUser.getId());

        return "redirect:/group-list";
    }

    @GetMapping("/groups/{id}")
    public String todoList(@PathVariable("id") Integer id,
                           HttpSession session,
                           Model model) {
        User sessionUser = (User) session.getAttribute("sessionUser");

        if (sessionUser == null) {
            return "redirect:/login-form";
        }
        ts.showTodo();

        return "todo/todo-list";
    }
}
