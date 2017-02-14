package org.citopt.connde.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author rafaelkperes
 */
@Controller
@RequestMapping("/templates")
public class TemplateController {
    
    @RequestMapping(value="/{template}")
    public String getHomeTemplate(@PathVariable(value="template") String template) {
        return "templates/" + template;   
    }
    
}
