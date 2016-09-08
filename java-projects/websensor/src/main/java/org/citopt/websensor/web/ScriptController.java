package org.citopt.websensor.web;

import org.citopt.websensor.web.file.FileBucket;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.validation.Valid;
import org.bson.types.ObjectId;
import org.citopt.websensor.domain.Script;
import org.citopt.websensor.repository.ScriptRepository;
import org.citopt.websensor.web.file.FileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/script")
public class ScriptController {

    @Autowired
    private ScriptRepository scriptRepository;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    FileValidator fileValidator;

    private static String UPLOAD_LOCATION = "./temp/file/";

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

        FileBucket fileBucket = new FileBucket();
        model.put("fileBucket", fileBucket);

        String uriScript = servletContext.getContextPath() + "/script"
                + "/" + id;
        model.put("uriEdit", uriScript + "/edit");
        model.put("uriEditService", uriScript + "/edit/service");
        model.put("uriEditRoutine", uriScript + "/edit/routine");
        model.put("uriDelete", uriScript + "/delete");
        model.put("uriCancel", uriScript);

        return "script/id";
    }

    @RequestMapping(value = "/{id}/edit/service", method = RequestMethod.POST)
    public String editService(
            @PathVariable("id") ObjectId id,
            @Valid FileBucket fileBucket,
            BindingResult result,
            ModelMap model) {
        String uriScript = servletContext.getContextPath() + "/script"
                + "/" + id;

        if (result.hasErrors()) {
            System.out.println("validation errors");
            System.out.println(result.getAllErrors().get(0).toString());     
            System.out.println(fileBucket.toString());
        } else {
            System.out.println(id);
        }

        return "redirect:" + "/script" + "/" + id;
    }

    @RequestMapping(value = "/{id}/edit/routine", method = RequestMethod.POST)
    public String editRoutine(
            @PathVariable("id") ObjectId id,
            @ModelAttribute("fileBucket") FileBucket fileBucket) {
        String uriScript = servletContext.getContextPath() + "/script"
                + "/" + id;

        return "redirect:" + "/script" + "/" + id;
    }

}
