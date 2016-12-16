package org.citopt.sensmonqtt.web.view;

import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author rafaelkperes
 */
@Controller
public class IndexController {
    
    @RequestMapping("/")
    public String viewIndex(Map<String, Object> model) {
        //model.put("view", "greeting");
        
        return "greeting";
        //return "redirect:/welcome";
    }
    
    @RequestMapping("/layout")
    public String viewAngular() {
        return "layout";
        //return "redirect:/welcome";
    }
    
}
