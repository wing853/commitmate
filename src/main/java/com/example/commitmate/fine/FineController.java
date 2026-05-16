package com.example.commitmate.fine;

import com.example.commitmate.groupmember.GroupRole;
import com.example.commitmate.todo.Todo;
import com.example.commitmate.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class FineController {

    private final FineService fs;

    @GetMapping("/groups/{groupId}/fines")
    public String showFines(@PathVariable("groupId") Integer groupId,
                            Model model,
                            HttpSession session) {

        User sessionUser = (User) session.getAttribute("sessionUser");

        FineResponse.GroupFineInfo fineInfo = fs.getFineInfo(groupId);
        model.addAttribute("fineInfo",fineInfo);
        model.addAttribute("isAdmin", fs.isAdmin(groupId, sessionUser.getId()));
        return "fine/fine-list";
    }

    @PostMapping("/fines/{fineId}/pay")
    public String payFine(@PathVariable("fineId") Integer fineId,
                          @RequestParam(value = "memo", required = false) String memo,
                          @RequestParam("groupId") Integer groupId,
                          HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        fs.payFine(fineId, sessionUser.getId(), memo);
        return "redirect:/groups/" + groupId + "/fines";
    }

    // 납부 승인 (PENDING → PAID)
    @PostMapping("/fines/{fineId}/approve")
    public String approveFine(@PathVariable("fineId") Integer fineId,
                              @RequestParam("groupId") Integer groupId,
                              HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        fs.approveFine(fineId, sessionUser.getId(), groupId);
        return "redirect:/groups/" + groupId + "/fines";
    }
}
