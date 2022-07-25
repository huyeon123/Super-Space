package com.huyeon.apiserver.controller;

import com.huyeon.apiserver.model.Authority;
import com.huyeon.apiserver.model.UserDetailsImpl;
import com.huyeon.apiserver.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Role;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Locale;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    @GetMapping
    public String mainPage(Locale locale, Model model) {
        model.addAttribute("response", locale.getDisplayCountry() + "에서 접속중입니다.");
        return "main";
    }

    @GetMapping("/signup")
    public String signUpPage() {
        return "signup";
    }

    @GetMapping("/error")
    public String accessDeniedPage() {
        return "AccessDenied.html";
    }

}
