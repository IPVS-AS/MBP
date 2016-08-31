package org.citopt.websensor.web;

import java.util.Map;
import org.citopt.websensor.domain.Location;
import org.citopt.websensor.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LocationController {

    @Autowired
    private LocationRepository repository;

    @RequestMapping(value = "/location", method = RequestMethod.GET)
    public String viewLocation(Map<String, Object> model) {
        Location locationForm = new Location();
        model.put("locationForm", locationForm);
        
        model.put("locationList", repository.findAll());
        
        return "location";
    }

    @RequestMapping(value = "/location", method = RequestMethod.POST)
    public String processRegistration(@ModelAttribute("locationForm") Location location,
            Map<String, Object> model) {
        repository.save(location);

        model.put("success", true);
        
        model.put("result", location);
        
        model.put("view", "/location");

        return "result";
    }
}
