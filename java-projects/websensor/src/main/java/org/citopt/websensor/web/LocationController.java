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
            Map<String, Object> model) {
        location = locationRepository.insert(location);

        return "redirect:" + "/location" + "/" + location.getId();
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

        return "location/id";
    }

    @RequestMapping(value = "/{id}" + "/edit", method = RequestMethod.POST)
    public String processEditLocation(
            @ModelAttribute("locationForm") Location location,
            Map<String, Object> model) {
        locationRepository.save(location);

        return "redirect:" + "/location" + "/" + location.getId();
    }

    @RequestMapping(value = "/{id}" + "/delete", method = RequestMethod.GET)
    public String processDeleteLocation(
            @PathVariable("id") ObjectId id,
            Map<String, Object> model) {
        locationRepository.delete(id.toString());

        return "redirect:" + "/location";
    }

}
