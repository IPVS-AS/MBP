package org.citopt.connde.web.rest;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author rafaelkperes, Imeri Amil
 */
@Controller
public class IndexController {
    
    private boolean isExpert(HttpServletRequest request) {
        Boolean ret = (Boolean) request.getSession().getAttribute("userExpert");
        return ret != null && ret;
    }
   
    @RequestMapping("/")
    public String viewIndex() {
        return "index";
    }
    
    @RequestMapping("/login")
    public String login() {
        return "index";
    }
    
    @RequestMapping("/register")
    public String register() {
        return "index";
    }
    
    @RequestMapping("/goExpert")
    public String goExpert(HttpServletRequest request) {
        request.getSession().setAttribute("userExpert", true);
        return "redirect:/";
    }
    
    @RequestMapping("/leaveExpert")
    public String leaveExpert(HttpServletRequest request) {
        request.getSession().setAttribute("userExpert", false);
        return "redirect:/";
    }
    
    @RequestMapping("/view/**")
    public String views() {
        return "index";
    }
}
