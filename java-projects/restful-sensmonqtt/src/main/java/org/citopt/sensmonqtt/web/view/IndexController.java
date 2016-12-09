package org.citopt.sensmonqtt.web.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author rafaelkperes
 */
@Controller
public class IndexController {
    
    @RequestMapping("/")
    public String viewIndex() {
        return "index";
        //return "redirect:/welcome";
    }
    
}
