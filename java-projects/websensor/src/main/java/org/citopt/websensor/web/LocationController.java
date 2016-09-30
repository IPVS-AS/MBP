package org.citopt.websensor.web;

import java.util.Map;
import javax.servlet.ServletContext;
import org.bson.types.ObjectId;
import org.citopt.websensor.domain.Location;
import org.citopt.websensor.repository.LocationRepository;
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
    private LocationRepository locationRepository;

    @Autowired
    private ServletContext servletContext;

    @RequestMapping(method = RequestMethod.GET)
    public String viewLocation(Map<String, Object> model) {
        Location locationForm = new Location();
        model.put("locationForm", locationForm);

        model.put("locations", locationRepository.findAll());
        model.put("uriLocation", servletContext.getContextPath() + "/location");

        return "location";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String processRegistration(
            @ModelAttribute("locationForm") Location location,
            RedirectAttributes redirectAttrs) {
        location = locationRepository.insert(location);

        redirectAttrs.addAttribute("id", location.getId())
                .addFlashAttribute("msgSuccess", "Location registered!");
        return "redirect:/location/{id}";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String viewLocationById(
            @PathVariable("id") ObjectId id,
            Map<String, Object> model) {
        Location location = locationRepository.findOne(id.toString());
        model.put("location", location);
        model.put("locationForm", location);

        String uriLocation = servletContext.getContextPath() + "/location"
                + "/" + id;
        model.put("uriEdit", uriLocation + "/edit");
        model.put("uriDelete", uriLocation + "/delete");
        model.put("uriCancel", uriLocation);
        model.put("uriLocation", uriLocation);

        return "location/id";
    }

    @RequestMapping(value = "/{id}" + "/edit", method = RequestMethod.POST)
    public String processEditLocation(
            @ModelAttribute("locationForm") Location location,
            RedirectAttributes redirectAttrs) {
        locationRepository.save(location);

        redirectAttrs.addAttribute("id", location.getId())
                .addFlashAttribute("msgSuccess", "Saved succesfully!");
        return "redirect:/location/{id}";
    }

    @RequestMapping(value = "/{id}" + "/delete", method = RequestMethod.GET)
    public String processDeleteLocation(
            @PathVariable("id") ObjectId id,
            RedirectAttributes redirectAttrs) {
        locationRepository.delete(id.toString());

        redirectAttrs.addFlashAttribute("msgSuccess", "Location deleted!");
        return "redirect:" + "/location";
    }

}
