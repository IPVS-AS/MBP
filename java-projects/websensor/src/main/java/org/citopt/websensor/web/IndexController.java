package org.citopt.websensor.web;

import com.mongodb.Mongo;
import java.util.Map;
import javax.servlet.ServletContext;
import org.bson.types.ObjectId;
import org.citopt.websensor.MongoConfiguration;
import org.citopt.websensor.service.ARPReader;
import org.citopt.websensor.service.MQTTLoggerReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class IndexController {

    @Autowired
    private ARPReader arpReader;

    @Autowired
    MQTTLoggerReader mqttLoggerReader;

    @Autowired
    private ServletContext servletContext;
    
    @Autowired
    private Mongo mongo;
    
    @RequestMapping(value = "/factoryreset", method = RequestMethod.GET)
    public String resetMongo(RedirectAttributes redirectAttrs) {
        mongo.dropDatabase(MongoConfiguration.DB_NAME);
        
        redirectAttrs.addFlashAttribute("msgSuccess", "FACTORY RESETED!");
        return "redirect:/";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String viewIndex(Map<String, Object> model) {
        model.put("arpTable", arpReader.getTable());
        
        model.put("uriReset", servletContext.getContextPath() + "/arp/reset");
        model.put("uriMessage", servletContext.getContextPath() + "/message");
        model.put("uriSensor", servletContext.getContextPath() + "/sensor");
        model.put("uriMqtt", servletContext.getContextPath() + "/mqtt");
        return "index";
    }

    @RequestMapping(value = "/arp/reset", method = RequestMethod.GET)
    public String resetArp(RedirectAttributes redirectAttrs) {
        arpReader.resetTable();
        
        redirectAttrs.addFlashAttribute("msgSuccess", "ARP Table cleaned!");
        return "redirect:/";
    }

    @RequestMapping(value = "/message/{id}", method = RequestMethod.GET)
    public String getLogMessage(
            @PathVariable("id") ObjectId id,
            Map<String, Object> model) {
        model.put("title", mqttLoggerReader.loadEntry(id).getId());
        model.put("content", mqttLoggerReader.loadEntry(id).getMessage());

        return "message";
    }

}
