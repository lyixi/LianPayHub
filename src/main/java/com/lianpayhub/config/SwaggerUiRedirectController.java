package com.lianpayhub.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerUiRedirectController {

    @GetMapping({"/swagger-ui/", "/swagger", "/docs"})
    public String redirectSwaggerUiRoot() {
        return "redirect:/swagger-ui/index.html";
    }
}
