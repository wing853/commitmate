package com.example.commitmate.fine;

import com.example.commitmate.groupmember.GroupRole;
import com.example.commitmate.todo.Todo;
import com.example.commitmate.user.User;
import com.example.commitmate.user.UserService;
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
    private final UserService us;

    @GetMapping("/groups/{groupId}/fines")
    public String showFines(@PathVariable("groupId") Integer groupId,
                            @RequestParam(name = "filter", required = false) String filter,
                            @RequestParam(name = "status", required = false) String status,
                            Model model,
                            HttpSession session) {

        User sessionUser = (User) session.getAttribute("sessionUser");
        model.addAttribute("sessionUser", sessionUser);
        FineResponse.GroupFineInfo fineInfo = fs.getFineInfo(groupId);

        // 내 벌금 필터
        List<Fine> fineList = "my".equals(filter)
                ? fineInfo.getExpiredFines().stream()
                .filter(f -> f.getGroupMember() != null &&
                        f.getGroupMember().getUser().getId().equals(sessionUser.getId()))
                .collect(java.util.stream.Collectors.toList())
                : fineInfo.getExpiredFines();

        // 납부 상태 필터
        if (status != null) {
            fineList = fineList.stream()
                    .filter(f -> {
                        if ("unpaid".equals(status)) return f.isUnpaid();
                        if ("paid".equals(status)) return f.isPaid();
                        return true;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        fineInfo.setExpiredFines(fineList);

        model.addAttribute("fineInfo", fineInfo);
        model.addAttribute("isAdmin", fs.isAdmin(groupId, sessionUser.getId()));
        model.addAttribute("group", fs.getGroup(groupId));
        model.addAttribute("isFiltered", "my".equals(filter));
        model.addAttribute("currentStatus", status);
        model.addAttribute("isStatusUnpaid", "unpaid".equals(status));
        model.addAttribute("isStatusPaid", "paid".equals(status));
        model.addAttribute("isStatusNone", status == null);

        return "fine/fine-list";
    }

    @PostMapping("/fines/{fineId}/pay")
    public String payFine(@PathVariable("fineId") Integer fineId,
                          @RequestParam(value = "memo", required = false) String memo,
                          @RequestParam("groupId") Integer groupId,
                          HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        fs.payFine(fineId, sessionUser.getId(), memo);

        User updatedUser = us.findById(sessionUser.getId());
        session.setAttribute("sessionUser",updatedUser);

        return "redirect:/groups/" + groupId + "/fines";
    }

}
