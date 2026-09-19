package com.lianpayhub.web.api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller public class OfficialWebsitePageController {
 @GetMapping({"/","/product","/product/{slug}","/news","/news/{slug}","/about","/contact"}) public String official(){return "forward:/official-ui/index.html";}
 @GetMapping({"/admin","/admin/"}) public String websiteAdmin(){return "forward:/website-admin/index.html";}
}
