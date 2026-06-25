package com.example.commitmate.todo;

import com.example.commitmate.core.errors.Exception403;
import com.example.commitmate.group.GroupService;
import com.example.commitmate.user.User;
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
                            @RequestParam(name = "status", required = false) String status,
                            HttpSession session, Model model) {

        User sessionUser = (User) session.getAttribute("sessionUser");

        model.addAttribute("group", gs.findGroupById(groupId));
        model.addAttribute("sessionUser", sessionUser);
        model.addAttribute("isAdmin", gs.isAdmin(groupId, sessionUser.getId()));

        List<Todo> todoList = "my".equals(filter)
                ? ts.findMyTodos(groupId, sessionUser.getId())
                : ts.showTodo(groupId);

        // status 필터링
        if (status != null) {
            todoList = todoList.stream()
                    .filter(todo -> {
                        if ("ready".equals(status)) return todo.isReady();
                        if ("complete".equals(status)) return todo.isComplete();
                        if ("expired".equals(status)) return todo.isExpired();
                        return true;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        model.addAttribute("todoList", todoList);
        model.addAttribute("isFiltered", "my".equals(filter));
        model.addAttribute("currentStatus", status);
        model.addAttribute("isStatusReady", "ready".equals(status));   // 추가
        model.addAttribute("isStatusComplete", "complete".equals(status)); // 추가
        model.addAttribute("isStatusExpired", "expired".equals(status));   // 추가
        model.addAttribute("isStatusNone", status == null);                // 추가

        return "todo/todo-list";
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
