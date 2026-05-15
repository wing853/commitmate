package com.example.commitmate.todo;

import com.example.commitmate.core.errors.Exception403;
import com.example.commitmate.group.GroupService;
import com.example.commitmate.user.User;
import com.example.commitmate.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TodoController {

    private final TodoService ts;
    private final GroupService gs;

    @GetMapping("/groups/{groupId}/todos")
    public String listTodos(@PathVariable Integer groupId,
                            @RequestParam(name = "filter", required = false) String filter,
                            HttpSession session, Model model) {

        // 1. 그룹 정보 가져오기 (헤더용)
        model.addAttribute("group", gs.findGroupById(groupId));

        // 2. 투두 목록 필터 로직 (아까 짠 코드 그대로!)
        User sessionUser = (User) session.getAttribute("sessionUser");
        List<Todo> todoList = "my".equals(filter)
                ? ts.findMyTodos(groupId, sessionUser.getId())
                : ts.showTodo(groupId);

        model.addAttribute("todoList", todoList);
        model.addAttribute("isFiltered", "my".equals(filter));

        return "todo/todo-list"; // 실제 머스테치 페이지
    }


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

    @GetMapping("/groups/{groupId}/todos/{todoId}/update-form")
    public String updateTodoFormPage(@PathVariable("groupId") Integer groupId,
                                     @PathVariable("todoId") Integer todoId,
                                     Model model,
                                     HttpSession session) {

        User sessionUser = (User) session.getAttribute("sessionUser");
        Todo todo = ts.findTodo(todoId);

        if(!todo.getGroupMember().getUser().getId().equals(sessionUser.getId())) {
            throw new Exception403("수정 권한이 없습니다.");
        }
        model.addAttribute("groupId",groupId);
        model.addAttribute("todo", todo);

        return "todo/update-form";
    }

    @PostMapping("/groups/{groupId}/todos/{todoId}/update")
    public String updateTodoProc(@PathVariable("groupId") Integer groupId,
                                 @PathVariable("todoId") Integer todoId,
                                 @ModelAttribute TodoRequest.UpdateTodoDTO updateTodoDTO,
                                 HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        ts.updateTodo(todoId,sessionUser.getId(),updateTodoDTO);

        return "redirect:/groups/" + groupId + "/todos";
    }

    @PostMapping("/todo/{id}/delete")
    public String deleteTodoProc(@PathVariable("id") Integer id,
                                 Integer groupId,
                                 HttpSession session) {

        User sessionUser = (User) session.getAttribute("sessionUser");

        ts.deleteTodo(id, sessionUser.getId());

        return "redirect:/groups/" + groupId;
    }

    @GetMapping("/todo/{id}/toggle")
    public String todoToggle(@PathVariable("id") Integer id,
                             Integer groupId,
                             HttpSession session) {

        User sissionUser = (User) session.getAttribute("sessionUser");

        ts.toggleDone(id, sissionUser.getId());

        return "redirect:/groups/" + groupId;
    }


}
