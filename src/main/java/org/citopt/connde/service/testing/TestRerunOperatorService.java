package org.citopt.connde.service.testing;

import org.apache.commons.io.IOUtils;
import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.Code;
import org.citopt.connde.domain.adapter.parameters.Parameter;
import org.citopt.connde.domain.adapter.parameters.ParameterType;
import org.citopt.connde.repository.AdapterRepository;
import org.citopt.connde.repository.TestDetailsRepository;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

/**
 * This service provides means for the management of default operators that may be added to the operator repository
 * on user request.
 */
@Service
public class TestRerunOperatorService {

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private List<String> rerunOperatorWhitelist;

    @Autowired
    private AdapterRepository adapterRepository;

    private static final String DESCRIPTOR_FILE = "operator.json";

    /**
     * Loads default operators from the resources directory and adds them to the operator repository so that they
     * can be used in actuators and sensors by all users.
     *
     * @return An action response containing the result of this operation
     */
    public ActionResponse addDefaultOperators() {



        //Remembers if an operator was inserted
        boolean inserted = false;

        //Iterate over all default operator paths
        for (String operatorPath : rerunOperatorWhitelist) {
            //Create new operator object to add it later to the repository
            Adapter newOperator = new Adapter();

            //New operator is not owned by anyone
            newOperator.setOwner(null);

            //Get content of the operator directory
            Set<String> operatorContent = servletContext.getResourcePaths(operatorPath);

            //Build path of descriptor
            String descriptorPath = operatorPath + "/" + DESCRIPTOR_FILE;

            //Check if there is a descriptor file, otherwise skip the operator
            if (!operatorContent.contains(descriptorPath)) {
                continue;
            }

            try {
                //Read descriptor file
                InputStream stream = servletContext.getResourceAsStream(descriptorPath);
                String descriptorContent = IOUtils.toString(stream, StandardCharsets.UTF_8);
                JSONObject descriptorJSON = new JSONObject(descriptorContent);

                //Set operator properties from the descriptor
                newOperator.setName(descriptorJSON.optString("name"));
                newOperator.setDescription(descriptorJSON.optString("description"));
                newOperator.setUnit(descriptorJSON.optString("unit"));

                //Get parameters
                JSONArray parameterArray = descriptorJSON.optJSONArray("parameters");

                //Check if there are parameters
                if (parameterArray != null) {

                    //Create new list for parameters
                    List<Parameter> parameterList = new ArrayList<>();

                    //Iterate over all parameters
                    for (int i = 0; i < parameterArray.length(); i++) {
                        //Get parameter JSON object
                        JSONObject parameterObject = parameterArray.getJSONObject(i);

                        //Create new parameter object
                        Parameter newParameter = new Parameter();
                        newParameter.setName(parameterObject.optString("name"));
                        newParameter.setType(ParameterType.create(parameterObject.optString("type")));
                        newParameter.setUnit(parameterObject.optString("unit"));
                        newParameter.setMandatory(parameterObject.optBoolean("mandatory", false));

                        //Add parameter to list
                        parameterList.add(newParameter);
                    }

                    //Add parameter list to operator
                    newOperator.setParameters(parameterList);
                }

                //Get files
                JSONArray fileArray = descriptorJSON.optJSONArray("files");

                //Skip operator if no files are associated with it
                if ((fileArray == null) || (fileArray.length() < 1)) {
                    continue;
                }

                //Iterate over all files
                for (int i = 0; i < fileArray.length(); i++) {
                    //Get current file path and create a file object
                    String operatorFilePath = operatorPath + "/" + fileArray.getString(i);
                    File operatorFile = new File(servletContext.getRealPath(operatorFilePath));

                    //Determine mime type of the file
                    String operatorFileMime = servletContext.getMimeType(operatorFilePath);

                    if((operatorFileMime == null) || (operatorFileMime.isEmpty())){
                        operatorFileMime = "application/octet-stream";
                    }

                    //Try to read the file
                    InputStream operatorFileStream = servletContext.getResourceAsStream(operatorFilePath);
                    byte[] operatorFileBytes = IOUtils.toByteArray(operatorFileStream);

                    //Convert file content to base64 with mime type prefix
                    String base64String = Base64.getEncoder().encodeToString(operatorFileBytes);
                    base64String = "data:" + operatorFileMime + ";base64," + base64String;
                    //data:text/x-sh;base64,

                    //Create new code object for this file
                    Code newCode = new Code();
                    newCode.setName(operatorFile.getName());
                    newCode.setContent(base64String);

                    //Add code to operator
                    newOperator.addRoutine(newCode);
                }

                //Insert new operator into repository
                adapterRepository.insert(newOperator);
                inserted = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new ActionResponse(inserted);
    }
}
