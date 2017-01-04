package org.citopt.sensmonqtt.web.view;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author rafaelkperes
 */
@Controller
public class ViewController {
    
    private boolean isExpert(HttpServletRequest request) {
        Boolean ret = (Boolean) request.getSession().getAttribute("userExpert");
        return ret != null && ret;
    }
    
    @RequestMapping("/")
    public String viewIndex() {
        return "redirect:/home";
    }
    
    @RequestMapping("/goExpert")
    public String goExpert(HttpServletRequest request) {
        request.getSession().setAttribute("userExpert", true);
        return "redirect:/home";
    }
    
    @RequestMapping("/leaveExpert")
    public String leaveExpert(HttpServletRequest request) {
        request.getSession().setAttribute("userExpert", false);
        return "redirect:/home";
    }
    
    @RequestMapping("/home")
    public String viewHome() {
        return "home";
    }
    
    @RequestMapping("/sensors")
    public String viewSensors() {
        return "sensors";
    }
    
    @RequestMapping("/actuators")
    public String viewActuators() {
        return "actuators";
    }
    
    @RequestMapping("/devices")
    public String viewDevices(HttpServletRequest request) {
        if (isExpert(request)) {
            return "devices";
        }
        
        return "redirect:/home";
    }
    
    @RequestMapping("/types")
    public String viewTypes(HttpServletRequest request) {
        if (isExpert(request)) {
            return "types";
        }
        
        return "redirect:/home";
    }
}
