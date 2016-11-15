package org.citopt.websensor.web;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.bson.types.ObjectId;
import org.citopt.websensor.dao.DeviceDao;
import org.citopt.websensor.dao.ScriptDao;
import org.citopt.websensor.dao.SensorDao;
import org.citopt.websensor.domain.Device;
import org.citopt.websensor.domain.Script;
import org.citopt.websensor.domain.Sensor;
import org.citopt.websensor.service.Heartbeat;
import org.citopt.websensor.service.HeartbeatResult;
import org.citopt.websensor.service.SSHDeployer;
import org.citopt.websensor.dao.InsertFailureException;
import org.citopt.websensor.web.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value = "/sensor")
public class SensorController {

    @Autowired
    private SensorDao sensorDao;

    @Autowired
    private DeviceDao deviceDao;

    @Autowired
    private ScriptDao scriptDao;

    @Autowired
    private Heartbeat heartbeat;

    @Autowired
    private SSHDeployer sshDeployer;

    @Autowired
    private ServletContext servletContext;

    public static String URI_DEPLOY = "/deploy";
    private static String MQTTURL = "192.168.43.124";

    public static String getUriSensor(ServletContext servletContext) {
        return servletContext.getContextPath() + "/sensor";
    }

    public String getUriSensorId(ServletContext servletContext, ObjectId id) {
        return getUriSensor(servletContext)
                + "/" + id.toString();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String getSensors(Map<String, Object> model) {
        Sensor sensorForm = new Sensor();
        model.put("sensorForm", sensorForm);

        model.put("sensors", sensorDao.findAll());
        model.put("devices", deviceDao.findAll());
        model.put("scripts", scriptDao.findAll());

        model.put("uriSensor", getUriSensor(servletContext));
        model.put("uriDevice", DeviceController.getUriDevice(servletContext));
        model.put("uriScript", ScriptController.getUriScript(servletContext));

        return "sensor";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String postSensor(
            @ModelAttribute("sensorForm") Sensor sensor,
            RedirectAttributes redirectAttrs) {
        try {
            sensor = sensorDao.insert(sensor);
            redirectAttrs.addAttribute("id", sensor.getId())
                    .addFlashAttribute("msgSuccess", "Sensor registered!");
            return "redirect:/sensor/{id}";
        } catch (InsertFailureException ex) {
            redirectAttrs.addAttribute("id", sensor.getId())
                    .addFlashAttribute("msgError", "Failed to register!");
            return "redirect:/sensor";
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String getSensorID(
            @PathVariable("id") ObjectId id,
            Map<String, Object> model)
            throws ParseException, NotFoundException, IOException {
        Sensor sensor = sensorDao.find(id);
        model.put("sensor", sensor);
        model.put("sensorForm", sensor);
        model.put("deployForm", new Object());
        model.put("devices", deviceDao.findAll());
        model.put("scripts", scriptDao.findAll());

        String uriId = getUriSensorId(servletContext, id);
        model.put("uriId", uriId);
        model.put("uriDeploy", uriId + URI_DEPLOY);
        model.put("uriDevice", DeviceController.getUriDevice(servletContext));
        model.put("uriScript", ScriptController.getUriScript(servletContext));

        boolean hasHb
                = heartbeat.isRegistered(sensor.getDevice().getId().toString());
        model.put("hasHeartbeat", hasHb);
        if (hasHb) {
            HeartbeatResult hb
                    = heartbeat.getResult(sensor.getDevice().getId().toString());
            model.put("heartbeatResult", hb);
            if (HeartbeatResult.Status.REACHABLE.equals(hb.getStatus())) {
                boolean running = sshDeployer.isRunning(sensor, hb.getIp(), 22, "pi",
                        SSHDeployer.key);
                model.put("isRunning", running);
                System.out.println(running);
            }
        }

        return "sensor/id";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public String putSensorID(
            @ModelAttribute("sensorForm") Sensor sensor,
            RedirectAttributes redirectAttrs) {
        sensorDao.save(sensor);

        redirectAttrs.addAttribute("id", sensor.getId())
                .addFlashAttribute("msgSuccess", "Saved succesfully!");
        return "redirect:/sensor/{id}";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String deleteSensorID(
            @PathVariable("id") ObjectId id,
            RedirectAttributes redirectAttrs) {
        sensorDao.delete(id);

        redirectAttrs.addFlashAttribute("msgSuccess", "Sensor deleted!");
        return "redirect:/sensor";
    }

    @RequestMapping(value = "/{id}/deploy", method = RequestMethod.POST)
    public String postSensorIDDeploy(
            @PathVariable("id") ObjectId id,
            HttpServletRequest request,
            RedirectAttributes redirectAttrs)
            throws ParseException, IOException, NotFoundException {
        String pinset = request.getParameter("pinset");
        Sensor sensor = sensorDao.find(id);
        Device device = deviceDao.find(sensor.getDevice().getId());
        Script script = scriptDao.find(sensor.getScript().getId());
        HeartbeatResult hb = heartbeat.getResult(device.getId().toString());

        if (HeartbeatResult.Status.REACHABLE.equals(hb.getStatus())) {
            try {
                sshDeployer.deploy(
                        id.toString(), hb.getIp(), 22, "pi", SSHDeployer.key,
                        MQTTURL, script, pinset);
                redirectAttrs.addFlashAttribute("msgSuccess", "Deployed succesfully!");
            } catch (Exception e) {
                redirectAttrs.addFlashAttribute("msgError", "Failed to deploy!");
            }
        } else {
            System.out.println(hb.getStatus());
        }

        redirectAttrs.addAttribute("id", sensor.getId());
        return "redirect:/sensor/{id}";
    }

    @RequestMapping(value = "/{id}/deploy", method = RequestMethod.DELETE)
    public String deleteSensorIDDeploy(
            @PathVariable("id") ObjectId id,
            HttpServletRequest request,
            RedirectAttributes redirectAttrs)
            throws ParseException, IOException, NotFoundException {
        String pinset = request.getParameter("pinset");
        Sensor sensor = sensorDao.find(id);
        HeartbeatResult hb = heartbeat.getResult(sensor.getDevice().getId().toString());

        if (HeartbeatResult.Status.REACHABLE.equals(hb.getStatus())) {
            try {
                sshDeployer.undeploy(
                        sensor, hb.getIp(), 22, "pi", SSHDeployer.key);
                redirectAttrs.addFlashAttribute("msgSuccess", "Undeployed succesfully!");
            } catch (Exception e) {
                redirectAttrs.addFlashAttribute("msgError", "Failed to undeploy!");
            }
        } else {
            System.out.println(hb.getStatus());
        }

        redirectAttrs.addAttribute("id", sensor.getId());
        return "redirect:/sensor/{id}";
    }

}
