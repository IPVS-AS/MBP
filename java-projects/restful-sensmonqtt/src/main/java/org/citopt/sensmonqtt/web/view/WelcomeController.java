package org.citopt.sensmonqtt.web.view;

import java.util.Map;
import org.citopt.sensmonqtt.domain.Location;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author rafaelkperes
 */
@Controller
public class WelcomeController {
    
    @RequestMapping("/welcome")
    public String viewIndex(Map<String, Object> model) {
        Location l = new Location();
        l.setName("You are here");
        l.setDescription("In nowhere to be found");
        model.put("location", l);
        
        return "index";
    }
    
}
