package com.example.commitmate.todo;

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

    @GetMapping("/groups/{groupId}")
    public String listTodos(@PathVariable Integer groupId,
                            @RequestParam(name = "filter", required = false) String filter,
                            Model model, HttpSession session) {

        // 1. 세션에서 로그인한 유저 정보 가져오기
        User sessionUser = (User) session.getAttribute("sessionUser");

        List<Todo> todoList;

        // 2. filter 파라미터가 "my"인지 확인
        if ("my".equals(filter)) {
            // 내 미션만 조회하는 서비스 로직 실행
            todoList = ts.findMyTodos(groupId, sessionUser.getId());
            model.addAttribute("todoList", todoList);
            model.addAttribute("isFiltered", true); // Mustache에서 버튼 상태 바꿀 때 사용
        } else {
            // 전체 미션 조회
            todoList = ts.showTodo(groupId);
            model.addAttribute("todoList", todoList);
            model.addAttribute("isFiltered", false);
        }

        model.addAttribute("todoList", todoList);
        return "group/todo-list"; // 해당 Mustache 페이지 렌더링
    }
}
