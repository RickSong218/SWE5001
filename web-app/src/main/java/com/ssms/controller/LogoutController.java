package com.ssms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ssms.common.auth.Sessions;
import com.ssms.common.env.EnvConfig;

import javax.servlet.http.HttpServletResponse;

@Controller
public class LogoutController {

    @Autowired
    private EnvConfig envConfig;

    @RequestMapping(value = "/logout")
    public String logout(HttpServletResponse response) {
        Sessions.logout(envConfig.getExternalApex(), response);
        return "redirect:/";
    }
}
