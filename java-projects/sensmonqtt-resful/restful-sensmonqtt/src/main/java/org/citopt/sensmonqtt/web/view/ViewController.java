package org.citopt.sensmonqtt.web.view;

import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author rafaelkperes
 */
@Controller
public class ViewController {
    
    @RequestMapping("/")
    public String viewIndex(Map<String, Object> model) {
        System.out.println("GET /");
        return "index";
    }
    
}
