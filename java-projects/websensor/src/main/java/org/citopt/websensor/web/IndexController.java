package org.citopt.websensor.web;

import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import org.bson.types.ObjectId;
import org.citopt.websensor.repository.SensorRepository;
import org.citopt.websensor.service.ARPReader;
import org.citopt.websensor.service.MQTTLoggerReader;
import org.citopt.websensor.service.MQTTLoggerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class IndexController {

    @Autowired
    private ARPReader arpReader;

    @Autowired
    MQTTLoggerReader mqttLoggerReader;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ServletContext servletContext;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String viewIndex(Map<String, Object> model) {
        model.put("arpTable", arpReader.getTable());

        List<MQTTLoggerResult> log = mqttLoggerReader.loadLog(200);
        for (MQTTLoggerResult entry : log) {
            entry.setSensorName(sensorRepository.findOne(entry.getSensorId())
                    .getName());
        }
        model.put("logTable", log);

        model.put("uriReset", servletContext.getContextPath() + "/arp/reset");
        model.put("uriMessage", servletContext.getContextPath() + "/message");
        model.put("uriSensor", servletContext.getContextPath() + "/sensor");
        return "index";
    }

    @RequestMapping(value = "/arp/reset", method = RequestMethod.GET)
    public String resetArp(Map<String, Object> model) {
        arpReader.resetTable();
        return "redirect:" + "/";
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
