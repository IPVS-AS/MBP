package org.citopt.connde.service.settings;

import org.apache.commons.io.IOUtils;
import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.Code;
import org.citopt.connde.exception.InsertFailureException;
import org.citopt.connde.repository.AdapterRepository;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Set;

/**
 * This service provides means for the management of default operators that may be added to the operator repository
 * on user request.
 */
@Service
public class DefaultOperatorService {

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private List<String> defaultOperatorWhitelist;

    @Autowired
    private AdapterRepository adapterRepository;

    /**
     * Loads default operators from the resources directory and adds them to the operator repository so that they
     * can be used in actuators and sensors by all users.
     *
     * @return An action response containing the result of this operation
     */
    public ActionResponse addDefaultOperators() {
        //Iterate over all default operator paths
        for (String operatorPath : defaultOperatorWhitelist) {

            //Create new adapter object
            Adapter newAdapter = new Adapter();
            newAdapter.setName("Imported");
            newAdapter.setDescription("imported adapter...");
            newAdapter.setUnit("°C");

            try {
                //Get content of the operator directory
                Set<String> operatorContent = servletContext.getResourcePaths(operatorPath);

                for (String content : operatorContent) {
                    File file = new File(servletContext.getRealPath(content));
                    String mime = servletContext.getMimeType(content);
                    InputStream stream = servletContext.getResourceAsStream(content);
                    byte[] fileBytes = IOUtils.toByteArray(stream);
                    String base64String = Base64.getEncoder().encodeToString(fileBytes);

                    Code code = new Code();
                    code.setName(file.getName());
                    code.setContent(base64String);

                    newAdapter.addRoutine(code);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InsertFailureException ignore) {
            }

            adapterRepository.insert(newAdapter);
        }

        return new ActionResponse(true);
    }
}
