package org.citopt.websensor.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import org.citopt.websensor.web.file.FileBucket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.validation.Valid;
import org.bson.types.ObjectId;
import org.citopt.websensor.domain.Script;
import org.citopt.websensor.domain.ScriptFile;
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
import org.springframework.web.multipart.MultipartFile;

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
        
        model.put("uriRawService", uriScript + "/raw/service");
        model.put("uriRawRoutine", uriScript + "/raw/routine");
        model.put("uriEdit", uriScript + "/edit");
        model.put("uriEditService", uriScript + "/edit/service");
        model.put("uriEditRoutine", uriScript + "/edit/routine");
        model.put("uriDelete", uriScript + "/delete");
        model.put("uriDeleteService", uriScript + "/delete/service");
        model.put("uriDeleteRoutine", uriScript + "/delete/routine");
        model.put("uriCancel", uriScript);

        return "script/id";
    }

    @RequestMapping(value = "/{id}" + "/edit", method = RequestMethod.POST)
    public String processEditScript(
            @ModelAttribute("scriptForm") Script script,
            Map<String, Object> model) {
        scriptRepository.save(script);

        return "redirect:" + "/script" + "/" + script.getId();
    }

    @RequestMapping(value = "/{id}" + "/delete", method = RequestMethod.GET)
    public String processDeleteScript(
            @PathVariable("id") ObjectId id,
            Map<String, Object> model) {
        scriptRepository.delete(id.toString());

        return "redirect:" + "/script";
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
            System.out.println(result.getAllErrors().get(0).toString());
        } else {

            MultipartFile file = fileBucket.getFile();
            Script script = scriptRepository.findOne(id.toString());

            try {
                ScriptFile service = new ScriptFile(id, file.getBytes());
                System.out.println(service);
                script.setService(service);
                scriptRepository.save(script);
            } catch (IOException ex) {
                System.out.println("IOException");
                Logger.getLogger(ScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return "redirect:" + "/script" + "/" + id;
    }
    
    @RequestMapping(value = "/{id}/raw/service", method = RequestMethod.GET)
    public String viewRawService(
            @PathVariable("id") ObjectId id,
            ModelMap model) {
        String uriScript = servletContext.getContextPath() + "/script"
                + "/" + id;
        Script script = scriptRepository.findOne(id.toString());

        model.put("uriScript", uriScript);
        model.put("title", script.getService().getName());
        try {
            model.put("content", new String(script.getService().getContent(), "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            model.put("content", script.getService().getContent().toString());
        }

        return "script/id/file/raw";
    }

    @RequestMapping(value = "/{id}/delete/service", method = RequestMethod.GET)
    public String deleteService(
            @PathVariable("id") ObjectId id,
            ModelMap model) {
        String uriScript = servletContext.getContextPath() + "/script"
                + "/" + id;
        Script script = scriptRepository.findOne(id.toString());

        script.setService(null);
        scriptRepository.save(script);

        return "redirect:" + "/script" + "/" + id;
    }

    @RequestMapping(value = "/{id}/edit/routine", method = RequestMethod.POST)
    public String editRoutine(
            @PathVariable("id") ObjectId id,
            @Valid FileBucket fileBucket,
            BindingResult result,
            ModelMap model) {
        String uriScript = servletContext.getContextPath() + "/script"
                + "/" + id;

        if (result.hasErrors()) {
            System.out.println(result.getAllErrors().get(0).toString());
        } else {

            MultipartFile file = fileBucket.getFile();
            Script script = scriptRepository.findOne(id.toString());

            try {
                ScriptFile routine = new ScriptFile(file.getOriginalFilename(), file.getBytes());
                script.setRoutine(routine);
                scriptRepository.save(script);
            } catch (IOException ex) {
                Logger.getLogger(ScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return "redirect:" + "/script" + "/" + id;
    }
    
    @RequestMapping(value = "/{id}/delete/routine", method = RequestMethod.GET)
    public String deleteRoutine(
            @PathVariable("id") ObjectId id,
            ModelMap model) {
        String uriScript = servletContext.getContextPath() + "/script"
                + "/" + id;
        Script script = scriptRepository.findOne(id.toString());

        script.setRoutine(null);
        scriptRepository.save(script);

        return "redirect:" + "/script" + "/" + id;
    }
    
    @RequestMapping(value = "/{id}/raw/routine", method = RequestMethod.GET)
    public String viewRawRoutine(
            @PathVariable("id") ObjectId id,
            ModelMap model) {
        String uriScript = servletContext.getContextPath() + "/script"
                + "/" + id;
        Script script = scriptRepository.findOne(id.toString());

        model.put("uriScript", uriScript);
        model.put("title", script.getRoutine().getName());
        try {
            model.put("content", new String(script.getRoutine().getContent(), "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            model.put("content", script.getRoutine().getContent().toString());
        }

        return "script/id/file/raw";
    }

}
