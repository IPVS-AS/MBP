package org.citopt.connde.web.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;

/**
 *
 * @author rafaelkperes
 */
@Controller
@RequestMapping("/templates")
@ApiIgnore("Only serves HTML templates")
public class TemplateController {
    
    @RequestMapping(value="/{template}")
    public String getHomeTemplate(@PathVariable(value="template") String template) {
        return "templates/" + template;   
    }
    
}
