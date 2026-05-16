package com.example.commitmate.core.errors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception400.class)
    public Object handle400(Exception400 e, HttpServletRequest request) {
        if (e.getMessage().contains("입력") || e.getMessage().contains("선택") ||
                e.getMessage().contains("만료") || e.getMessage().contains("초대")) {
            return new ResponseEntity<>("""
           <script>
               alert("%s");
               history.back();
           </script>
           """.formatted(e.getMessage()),
                    org.springframework.http.HttpHeaders.EMPTY,
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        request.setAttribute("msg", e.getMessage());
        return new ModelAndView("err/400");
    }

    @ExceptionHandler(Exception401.class)
    @ResponseBody
    public String ex401(Exception401 e){
        return """
               <script>
                   alert("%s");
                   location.href = "/login-form"; // 로그인 페이지 주소에 맞게 수정
               </script>
               """.formatted(e.getMessage());
    }

    @ExceptionHandler(Exception403.class)
    @ResponseBody
    public String ex403(Exception403 e){
        return """
               <script>
                   alert("%s");
                   history.back();
               </script>
               """.formatted(e.getMessage());
    }

    @ExceptionHandler(Exception404.class)
    public String ex404(Exception404 e, HttpServletRequest request){
        request.setAttribute("msg", e.getMessage());
        return "err/404";
    }

    @ExceptionHandler(Exception500.class)
    public String ex500(Exception500 e, HttpServletRequest request){
        request.setAttribute("msg", e.getMessage());
        return "err/500";
    }
}
