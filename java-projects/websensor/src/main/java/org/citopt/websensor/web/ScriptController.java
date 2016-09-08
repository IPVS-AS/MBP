package org.citopt.websensor.web;

import java.util.Map;
import javax.servlet.ServletContext;
import org.bson.types.ObjectId;
import org.citopt.websensor.domain.Script;
import org.citopt.websensor.repository.ScriptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(value = "/script")
public class ScriptController {

    @Autowired
    private ScriptRepository scriptRepository;

    @Autowired
    private ServletContext servletContext;

    @RequestMapping(method = RequestMethod.GET)
    public String viewScript(Map<String, Object> model) {
        Script scriptForm = new Script();
        model.put("scriptForm", scriptForm);

        model.put("scripts", scriptRepository.findAll());
        model.put("uriScript", servletContext.getContextPath() + "/script");

        return "script";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String processRegistration(
            final @RequestPart(value = "service", required = false) MultipartFile service,
            final @RequestPart(value = "routine", required = false) MultipartFile routine,
            @ModelAttribute("scriptForm") Script script,
            Map<String, Object> model) {
        System.out.println(script);
        script = scriptRepository.insert(script);

        return "redirect:" + "/script" + "/" + script.getId();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String viewScriptById(
            @PathVariable("id") ObjectId id,
            Map<String, Object> model) {
        Script script = scriptRepository.findOne(id.toString());
        model.put("script", script);
        model.put("scriptForm", script);

        String uriScript = servletContext.getContextPath() + "/script"
                + "/" + id;
        model.put("uriEdit", uriScript + "/edit");
        model.put("uriDelete", uriScript + "/delete");
        model.put("uriCancel", uriScript);

        return "script/id";
    }

}
