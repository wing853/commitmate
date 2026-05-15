package com.example.commitmate.fine;

import com.example.commitmate.todo.Todo;
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


        FineResponse.GroupFineInfo fineInfo = fs.getFineInfo(id);

        model.addAttribute("fineInfo",fineInfo);
        return "fine/fine-list";
    }

}
