package com.lec.packages.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        // 에러 발생 시 리다이렉트 또는 사용자 정의 에러 페이지로 이동
        return "redirect:/";
    }

}

