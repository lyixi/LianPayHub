package com.lianpayhub.web.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminUiController {

    @GetMapping("/console")
    public String consoleRoot() {
        return "redirect:/console/";
    }

    @GetMapping("/console/")
    public String console() {
        return "forward:/console/index.html";
    }

    @GetMapping({"/admin-ui", "/admin-ui/"})
    public String legacyAdminUi() {
        return "redirect:/console/";
    }
}
