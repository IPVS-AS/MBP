package de.ipvs.as.mbp.web.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

import java.util.List;

/**
 * General-purpose {@link Controller} for basic web-related requests, including the serving of HTML templates and the
 * injection of global {@link Model} properties for usage with Thymeleaf in HTML templates.
 */
@Controller
public class ViewController {
    //File name of the main template
    private static String MAIN_TEMPLATE = "index.html";

    /*
    Spring injections
    */
    //Active deployment profile
    @Value("${spring.profiles.active}")
    private List<String> activeProfiles;

    /**
     * Serves the main template and extends the passed data model.
     *
     * @param model The model to extend
     * @return The path to the main template
     */
    @GetMapping({"", "/", "/login", "/register", "/view/**"})
    String getMainTemplate(Model model) {
        //Extend the model for global properties
        exposeGlobalProperties(model);

        //Return name of main template
        return MAIN_TEMPLATE;
    }

    /**
     * Serves a requested template.
     *
     * @param template The name of the requested template
     * @return The path to the requested template
     */
    @RequestMapping(value = "/templates/{template}")
    public String getTemplate(@PathVariable(value = "template") String template) {
        return "templates/" + template;
    }

    /**
     * Extends a given {@link Model} for global properties in order to make them accessible via Thymeleaf within
     * HTML templates.
     *
     * @param model The model to extend
     */
    private void exposeGlobalProperties(Model model) {
        //Add the currently active profiles
        model.addAttribute("profiles", activeProfiles);
    }
}
