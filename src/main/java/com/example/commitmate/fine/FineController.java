package com.example.commitmate.fine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class FineController {

    private final FineService fs;

    @GetMapping("/groups/{id}/fines")
    public String showFines(@PathVariable("id") Integer id,
                            Model model) {

        List<Fine> fineList = fs.findAllFine(id);
        List<Fine> expiredFines = fineList.stream()
                .filter(Fine::isExpired) // Todo의 마감 기한이 지난 것만 필터링
                .collect(Collectors.toList());

        model.addAttribute("fines", expiredFines); // 이제 {{fines.size}}는 마감된 것만 셉니다.
        model.addAttribute("expiredCount", expiredFines.size());

        return "fine/fine-list";
    }

}
