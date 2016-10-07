package org.citopt.websensor.web;

import java.text.ParseException;
import java.util.Map;
import javax.servlet.ServletContext;
import org.bson.types.ObjectId;
import org.citopt.websensor.dao.DeviceDao;
import org.citopt.websensor.dao.LocationDao;
import org.citopt.websensor.domain.Device;
import org.citopt.websensor.service.Heartbeat;
import org.citopt.websensor.web.exception.IdNotFoundException;
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
    private DeviceDao deviceDao;

    @Autowired
    private LocationDao locationDao;

    @Autowired
    private Heartbeat heartbeat;

    @Autowired
    private ServletContext servletContext;
    
    public static String URI_HEARTBEAT = "/heartbeat";

    public static String getUriDevice(ServletContext servletContext) {
        return servletContext.getContextPath() + "/device";
    }
    
    public String getUriDeviceId(ServletContext servletContext, ObjectId id) {
        return getUriDevice(servletContext) 
                + "/" + id.toString();
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public String getDevices(Map<String, Object> model) {
        Device deviceForm = new Device();
        model.put("deviceForm", deviceForm);

        model.put("devices", deviceDao.findAll());
        model.put("locations", locationDao.findAll());
        
        model.put("uriDevice", getUriDevice(servletContext));
        model.put("uriLocation", LocationController.getUriLocation(servletContext));

        return "device";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String postDevice(
            @ModelAttribute("deviceForm") Device device,
            RedirectAttributes redirectAttrs) {
        device = deviceDao.insert(device);

        redirectAttrs.addAttribute("id", device.getId())
                .addFlashAttribute("msgSuccess", "Device registered!");
        return "redirect:/device/{id}";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String getDeviceID(
            @PathVariable("id") ObjectId id,
            Map<String, Object> model) 
            throws ParseException, IdNotFoundException {
        Device device = deviceDao.find(id);
        model.put("device", device);
        model.put("deviceForm", device);
        model.put("locations", locationDao.findAll());

        model.put("uriId", getUriDeviceId(servletContext, id));
        model.put("uriLocation", LocationController.getUriLocation(servletContext));        
        model.put("uriHeartbeat", getUriDeviceId(servletContext, id) + URI_HEARTBEAT);

        boolean hasHb = heartbeat.isRegistered(id.toString());
        model.put("hasHeartbeat", hasHb);
        if (hasHb) {
            model.put("heartbeatResult", heartbeat.getResult(id.toString()));
        }

        return "device/id";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public String putDeviceID(
            @ModelAttribute("deviceForm") Device device,
            RedirectAttributes redirectAttrs) {
        deviceDao.save(device);

        redirectAttrs.addAttribute("id", device.getId())
                .addFlashAttribute("msgSuccess", "Saved succesfully!");
        return "redirect:/device/{id}";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String deleteDeviceID(
            @PathVariable("id") ObjectId id,
            RedirectAttributes redirectAttrs) {
        heartbeat.removeMac(id.toString());
        deviceDao.delete(id);

        redirectAttrs.addFlashAttribute("msgSuccess", "Device deleted!");
        return "redirect:/device";
    }

    @RequestMapping(value = "/{id}/heartbeat", method = RequestMethod.PUT)
    public String registerHeartbeat(
            @PathVariable("id") ObjectId id,
            RedirectAttributes redirectAttrs)
            throws IdNotFoundException {
        Device d = deviceDao.find(id);
        heartbeat.registerMac(d.getRawMacAddress(), d.getId().toString());

        redirectAttrs.addAttribute("id", id)
                .addFlashAttribute("msgSuccess", "Heartbeat registered!");
        return "redirect:/device/{id}";
    }

}
