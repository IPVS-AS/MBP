package org.citopt.websensor.web;

import java.text.ParseException;
import java.util.Map;
import javax.servlet.ServletContext;
import org.bson.types.ObjectId;
import org.citopt.websensor.domain.Device;
import org.citopt.websensor.repository.DeviceRepository;
import org.citopt.websensor.repository.LocationRepository;
import org.citopt.websensor.service.Heartbeat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/device")
public class DeviceController {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private Heartbeat heartbeat;

    @Autowired
    private ServletContext servletContext;

    @RequestMapping(method = RequestMethod.GET)
    public String viewDevice(Map<String, Object> model) {
        Device deviceForm = new Device();
        model.put("deviceForm", deviceForm);

        model.put("devices", deviceRepository.findAll());
        model.put("locations", locationRepository.findAll());
        model.put("uriDevice", servletContext.getContextPath() + "/device");
        model.put("uriLocation", servletContext.getContextPath() + "/location");

        return "device";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String processRegistration(
            @ModelAttribute("deviceForm") Device device,
            Map<String, Object> model) {
        System.out.println(device);

        device = deviceRepository.insert(device);

        return "redirect:" + "/device" + "/" + device.getId();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String viewDeviceById(
            @PathVariable("id") ObjectId id,
            Map<String, Object> model) throws ParseException {
        Device device = deviceRepository.findOne(id.toString());
        model.put("device", device);
        model.put("deviceForm", device);
        model.put("locations", locationRepository.findAll());

        String uriDevice = servletContext.getContextPath() + "/device"
                + "/" + id;
        model.put("uriEdit", uriDevice + "/edit");
        model.put("uriDelete", uriDevice + "/delete");
        model.put("uriCancel", uriDevice);
        model.put("uriDevice", uriDevice);
        model.put("uriLocation", servletContext.getContextPath() + "/location");
        System.out.println(id);        
        model.put("uriHeartbeat", uriDevice + "/heartbeat");
        
        boolean hasHb = heartbeat.isRegistered(id.toString());
        model.put("hasHeartbeat", hasHb);
        if(hasHb) {
            model.put("heartbeatResult", heartbeat.getResult(id.toString()));
            System.out.println(model.get("heartbeatResult"));
        }

        return "device/id";
    }

    @RequestMapping(value = "/{id}" + "/edit", method = RequestMethod.POST)
    public String processEditDevice(
            @ModelAttribute("deviceForm") Device device,
            Map<String, Object> model) {
        deviceRepository.save(device);

        return "redirect:" + "/device" + "/" + device.getId();
    }

    @RequestMapping(value = "/{id}" + "/delete", method = RequestMethod.GET)
    public String processDeleteDevice(
            @PathVariable("id") ObjectId id,
            Map<String, Object> model) {
        deviceRepository.delete(id.toString());
        heartbeat.removeMac(id.toString());

        return "redirect:" + "/device";
    }

    @RequestMapping(value = "/{id}" + "/heartbeat", method = RequestMethod.GET)
    public String registerHeartbeat(
            @PathVariable("id") ObjectId id,
            Map<String, Object> model) {
        Device d = deviceRepository.findOne(id.toString());
        heartbeat.registerMac(d.getRawMacAddress(), d.getId().toString());
        return "redirect:" + "/device/" + id;
    }

}
