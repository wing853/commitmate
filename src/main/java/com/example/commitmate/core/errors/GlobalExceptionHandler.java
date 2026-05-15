package com.example.commitmate.core.errors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception400.class)
    public Object handle400(Exception400 e, HttpServletRequest request) {
        // 1. 만약 메시지에 "입력"이라는 단어가 포함되어 있다면 Alert (UX 배려)
        if (e.getMessage().contains("입력") || e.getMessage().contains("선택")) {
            return """
               <script>
                   alert("%s");
                   history.back();
               </script>
               """.formatted(e.getMessage());
        }

        // 2. 그 외 (이미지 요청 오류, 잘못된 파라미터 등)는 에러 페이지
        request.setAttribute("msg", e.getMessage());
        return "error/400";
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
