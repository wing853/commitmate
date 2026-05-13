package com.example.commitmate.group;

import com.example.commitmate.todo.Todo;
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

    @GetMapping("/groups")
    public String groupListPage(Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");

        List<GroupResponse.MyGroupDTO> groups = gs.getMyGroups(sessionUser.getId());
        model.addAttribute("groups", groups);
        model.addAttribute("sessionUser", sessionUser);

        return "group/group-list";
    }

    @GetMapping("/groups/save-form")
    public String groupCreateFormPage() {

        return "group/save-form";
    }

    @PostMapping("/groups/save")
    public String groupCreateProc(Model model, HttpSession session, GroupRequest.CreateDTO createDTO) {

        User userEntity = (User) session.getAttribute("sessionUser");
        createDTO.validate();
        gs.createRoom(userEntity, createDTO);

        return "redirect:/groups";
    }

    @GetMapping("/groups/{id}/update-form")
    public String groupUpdatePageForm(@PathVariable("id") Integer id,
                                      Model model) {

        GroupResponse.detailDTO group = gs.findGroupById(id);
        model.addAttribute("groups",group);

        return "group/update-form";
    }

    @PostMapping("/groups/{id}/update")
    public String groupUpdateProc(@PathVariable("id") Integer id,
                                  GroupRequest.UpdateDTO updateDTO) {

        updateDTO.validate();
        gs.updateGroup(id,updateDTO);
        return "redirect:/groups";
    }

    @PostMapping("/groups/{id}/delete")
    public String groupDeleteProc(@PathVariable("id") Integer id,
                                  HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        gs.deleteGroup(id,sessionUser.getId());
        return "redirect:/groups";
    }

    @GetMapping("/groups/{id}")
    public String todoList(@PathVariable("id") Integer id,
                           Model model) {
        List<Todo> todoList = ts.showTodo(id);
        model.addAttribute("todoList",todoList);

        return "todo/todo-list";
    }
}
