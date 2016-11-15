package org.citopt.websensor.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.citopt.websensor.web.file.FileBucket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.validation.Valid;
import org.bson.types.ObjectId;
import org.citopt.websensor.dao.ScriptDao;
import org.citopt.websensor.domain.Script;
import org.citopt.websensor.domain.ScriptFile;
import org.citopt.websensor.web.exception.NotFoundException;
import org.citopt.websensor.dao.InsertFailureException;
import org.citopt.websensor.web.file.FileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value = "/script")
public class ScriptController {

    @Autowired
    private ScriptDao scriptDao;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    FileValidator fileValidator;

    public static String URI_SERVICE = "/service";
    public static String URI_ROUTINE = "/routine";

    public static String getUriScript(ServletContext servletContext) {
        return servletContext.getContextPath() + "/script";
    }

    public String getUriScriptId(ServletContext servletContext, ObjectId id) {
        return getUriScript(servletContext)
                + "/" + id.toString();
    }

    @InitBinder
    public void initBinder(final DataBinder binder) {
        binder.setValidator(new FileValidator());
    }

    @RequestMapping(method = RequestMethod.GET)
    public String getScripts(Map<String, Object> model) {
        Script scriptForm = new Script();
        model.put("scriptForm", scriptForm);

        model.put("scripts", scriptDao.findAll());

        model.put("uriScript", getUriScript(servletContext));

        return "script";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String postScript(
            @ModelAttribute("scriptForm") Script script,
            RedirectAttributes redirectAttrs) {
        script = scriptDao.insert(script);

        redirectAttrs.addAttribute("id", script.getId())
                .addFlashAttribute("msgSuccess", "Script registered!");
        return "redirect:/script/{id}";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String getScriptID(
            @PathVariable("id") ObjectId id,
            Map<String, Object> model)
            throws NotFoundException {
        Script script = scriptDao.find(id);
        model.put("script", script);
        model.put("scriptForm", script);

        FileBucket fileBucket = new FileBucket();
        model.put("fileBucket", fileBucket);

        String uriId = getUriScriptId(servletContext, id);

        model.put("uriId", uriId);
        model.put("uriService", uriId + URI_SERVICE);
        model.put("uriRoutine", uriId + URI_ROUTINE);

        return "script/id";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public String putScriptID(
            @ModelAttribute("scriptForm") Script script,
            RedirectAttributes redirectAttrs) {
        scriptDao.save(script);

        redirectAttrs.addAttribute("id", script.getId())
                .addFlashAttribute("msgSuccess", "Saved succesfully!");
        return "redirect:/script/{id}";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String deleteScriptID(
            @PathVariable("id") ObjectId id,
            RedirectAttributes redirectAttrs) {
        scriptDao.delete(id);

        redirectAttrs.addFlashAttribute("msgSuccess", "Script deleted!");
        return "redirect:/script";
    }

    // SERVICE PART    
    @RequestMapping(value = "/{id}/service", method = RequestMethod.GET)
    public String getService(
            @PathVariable("id") ObjectId id,
            ModelMap model)
            throws NotFoundException {
        Script script = scriptDao.find(id);

        model.put("uriId", getUriScriptId(servletContext, id));
        model.put("title", script.getService().getName());
        try {
            model.put("content", new String(script.getService().getContent(), "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            model.put("content", script.getService().getContent().toString());
        }

        return "script/id/file/raw";
    }

    @RequestMapping(value = "/{id}/service", method = RequestMethod.POST)
    public String postService(
            @PathVariable("id") ObjectId id,
            @Valid FileBucket fileBucket,
            BindingResult result,
            RedirectAttributes redirectAttrs)
            throws NotFoundException {
        if (result.hasErrors()) {
            redirectAttrs.addFlashAttribute("msgError", "Failed to save service!");
        } else {
            MultipartFile file = fileBucket.getFile();
            Script script = scriptDao.find(id);

            try {
                ScriptFile service = new ScriptFile(file.getOriginalFilename(), file.getBytes());
                script.setService(service);
                scriptDao.save(script);
                redirectAttrs
                        .addFlashAttribute("msgSuccess", "Saved service succesfully!");
            } catch (IOException ex) {
                Logger.getLogger(ScriptController.class.getName()).log(Level.SEVERE, null, ex);
                redirectAttrs.addFlashAttribute("msgError", "Failed to save service!");
            }
        }

        redirectAttrs.addAttribute("id", id);
        return "redirect:/script/{id}";
    }

    @RequestMapping(value = "/{id}/service", method = RequestMethod.DELETE)
    public String deleteService(
            @PathVariable("id") ObjectId id,
            RedirectAttributes redirectAttrs)
            throws NotFoundException {
        Script script = scriptDao.find(id);

        script.setService(null);
        scriptDao.save(script);

        redirectAttrs.addAttribute("id", id)
                .addFlashAttribute("msgSuccess", "Removed service succesfully!");
        return "redirect:/script/{id}";
    }

    // ROUTINE PART
    @RequestMapping(value = "/{id}/routine/{filename:.+}", method = RequestMethod.GET)
    public String getRoutine(
            @PathVariable("id") ObjectId id,
            @PathVariable("filename") String filename,
            ModelMap model)
            throws NotFoundException {
        Script script = scriptDao.find(id);
        ScriptFile file = script.getRoutine(filename);

        model.put("uriId", getUriScriptId(servletContext, id));
        model.put("title", file.getName());
        try {
            model.put("content", new String(file.getContent(), "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            model.put("content", file.getContent().toString());
        }

        return "script/id/file/raw";
    }

    @RequestMapping(value = "/{id}/routine", method = RequestMethod.POST)
    public String postRoutine(
            @PathVariable("id") ObjectId id,
            @Valid FileBucket fileBucket,
            BindingResult result,
            RedirectAttributes redirectAttrs)
            throws NotFoundException, InsertFailureException {
        if (result.hasErrors()) {
            redirectAttrs.addFlashAttribute("msgError", "Failed to save routine!");
        } else {

            MultipartFile file = fileBucket.getFile();
            Script script = scriptDao.find(id);

            try {
                ScriptFile routine = new ScriptFile(file.getOriginalFilename(), file.getBytes());
                script.addRoutine(routine);
                scriptDao.save(script);
                redirectAttrs
                        .addFlashAttribute("msgSuccess", "Saved routine succesfully!");
            } catch (IOException ex) {
                Logger.getLogger(ScriptController.class.getName()).log(Level.SEVERE, null, ex);
                redirectAttrs.addFlashAttribute("msgError", "Failed to save routine!");
            }
        }

        redirectAttrs.addAttribute("id", id);
        return "redirect:/script/{id}";
    }

    @RequestMapping(value = "/{id}/routine/{filename:.+}", method = RequestMethod.DELETE)
    public String deleteRoutine(
            @PathVariable("id") ObjectId id,
            @PathVariable("filename") String filename,
            RedirectAttributes redirectAttrs)
            throws NotFoundException {
        Script script = scriptDao.find(id);

        script.deleteRoutine(filename);
        scriptDao.save(script);

        redirectAttrs.addAttribute("id", id)
                .addFlashAttribute("msgSuccess", "Removed routine succesfully!");
        return "redirect:/script/{id}";
    }
}
