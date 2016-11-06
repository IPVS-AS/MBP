package org.citopt.websensor.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.citopt.websensor.domain.Location;
import org.citopt.websensor.repository.SensorRepository;
import org.citopt.websensor.service.MQTTLoggerReader;
import org.citopt.websensor.service.MQTTLoggerResult;
import org.citopt.websensor.web.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/mqtt")
public class MQTTController {

    @Autowired
    MQTTLoggerReader mqttLoggerReader;

    @Autowired
    private SensorRepository sensorRepository;

    @RequestMapping(method = RequestMethod.GET)
    public String getTable(Map<String, Object> model) {
        List<MQTTLoggerResult> log = mqttLoggerReader.loadLog(200);
        for (MQTTLoggerResult entry : log) {
            try {
                entry.setSensorName(sensorRepository.findOne(entry.getSensorId())
                        .getName());
            } catch (Exception e) {
                entry.setSensorName(entry.getSensorId());
            }
        }
        model.put("mqttTable", log);

        return "mqtt";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String getLocationID(
            @PathVariable("id") ObjectId id,
            Map<String, Object> model)
            throws NotFoundException {
        System.out.println(id);
        List<MQTTLoggerResult> log = mqttLoggerReader.loadLog(500);
        List<MQTTLoggerResult> result = new ArrayList<>();
        
        for (MQTTLoggerResult entry : log) {
            if (id.equals(new ObjectId(entry.getSensorId()))) {
                result.add(entry);
            }
        }
        model.put("mqttTable", result);

        return "mqtt-id";
    }

}
