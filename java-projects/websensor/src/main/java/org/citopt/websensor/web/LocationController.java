package org.citopt.websensor.web;

import java.util.Map;
import javax.servlet.ServletContext;
import org.bson.types.ObjectId;
import org.citopt.websensor.dao.LocationDao;
import org.citopt.websensor.domain.Location;
import org.citopt.websensor.web.exception.IdNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value = "/location")
public class LocationController {

    @Autowired
    private LocationDao locationDao;

    @Autowired
    private ServletContext servletContext;

    private static String getLocationUri(ServletContext servletContext) {
        return servletContext.getContextPath() + "/location";
    }

    private String getLocationIdUri(ServletContext servletContext, ObjectId id) {
        return LocationController.getLocationUri(servletContext)
                + "/" + id.toString();
    }

    @RequestMapping(method = RequestMethod.GET)
    public String viewLocation(Map<String, Object> model) {
        Location locationForm = new Location();
        model.put("locationForm", locationForm);

        model.put("locations", locationDao.findAll());

        model.put("uriLocation", getLocationUri(servletContext));

        return "location";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String processRegistration(
            @ModelAttribute("locationForm") Location location,
            RedirectAttributes redirectAttrs) {
        location = locationDao.insert(location);

        redirectAttrs.addAttribute("id", location.getId())
                .addFlashAttribute("msgSuccess", "Location registered!");
        return "redirect:/location/{id}";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String viewLocationById(
            @PathVariable("id") ObjectId id,
            Map<String, Object> model)
            throws IdNotFoundException {
        Location location = locationDao.find(id);

        model.put("location", location);
        model.put("locationForm", location);

        model.put("uriEdit", getLocationIdUri(servletContext, id) + "/edit");
        model.put("uriDelete", getLocationIdUri(servletContext, id) + "/delete");
        model.put("uriCancel", getLocationIdUri(servletContext, id));
        model.put("uriLocation", getLocationIdUri(servletContext, id));

        return "location/id";
    }

    @RequestMapping(value = "/{id}/edit", method = RequestMethod.POST)
    public String processEditLocation(
            @ModelAttribute("locationForm") Location location,
            RedirectAttributes redirectAttrs) {
        locationDao.save(location);

        redirectAttrs.addAttribute("id", location.getId())
                .addFlashAttribute("msgSuccess", "Saved succesfully!");
        return "redirect:/location/{id}";
    }

    @RequestMapping(value = "/{id}/delete", method = RequestMethod.GET)
    public String processDeleteLocation(
            @PathVariable("id") ObjectId id,
            RedirectAttributes redirectAttrs) {
        locationDao.delete(id);

        redirectAttrs.addFlashAttribute("msgSuccess", "Location deleted!");
        return "redirect:/location";
    }

}