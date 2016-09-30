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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            RedirectAttributes redirectAttrs) {
        device = deviceRepository.insert(device);
        
        redirectAttrs.addAttribute("id", device.getId())
                .addFlashAttribute("msgSuccess", "Device registered!");
        return "redirect:/device/{id}";
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
            RedirectAttributes redirectAttrs) {
        deviceRepository.save(device);
        
        redirectAttrs.addAttribute("id", device.getId())
                .addFlashAttribute("msgSuccess", "Saved succesfully!");
        return "redirect:/device/{id}";
    }

    @RequestMapping(value = "/{id}" + "/delete", method = RequestMethod.GET)
    public String processDeleteDevice(
            @PathVariable("id") ObjectId id,
            RedirectAttributes redirectAttrs) {
        deviceRepository.delete(id.toString());
        heartbeat.removeMac(id.toString());

        redirectAttrs.addFlashAttribute("msgSuccess", "Device deleted!");
        return "redirect:/device";
    }

    @RequestMapping(value = "/{id}" + "/heartbeat", method = RequestMethod.GET)
    public String registerHeartbeat(
            @PathVariable("id") ObjectId id,
            RedirectAttributes redirectAttrs) {
        Device d = deviceRepository.findOne(id.toString());
        heartbeat.registerMac(d.getRawMacAddress(), d.getId().toString());
        
        redirectAttrs.addAttribute("id", id)
                .addFlashAttribute("msgSuccess", "Heartbeat registered!");
        return "redirect:/device/{id}";        
    }

}
