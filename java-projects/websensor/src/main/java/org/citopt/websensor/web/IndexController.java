package org.citopt.websensor.web;

import java.util.Map;
import javax.servlet.ServletContext;
import org.citopt.websensor.service.ARPReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class IndexController {

    @Autowired
    private ARPReader arpReader;
    
    @Autowired
    private ServletContext servletContext;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String viewIndex(Map<String, Object> model) {
        System.out.println(arpReader.getTable());
        model.put("arpTable", arpReader.getTable());
        model.put("uriReset", servletContext.getContextPath() + "/arp/reset");
        return "index";
    }
    
    @RequestMapping(value = "/arp/reset", method = RequestMethod.GET)
    public String resetArp(Map<String, Object> model) {
        arpReader.resetTable();
        return "redirect:" + "/";
    }
}
