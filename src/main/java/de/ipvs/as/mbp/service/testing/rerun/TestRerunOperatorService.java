package de.ipvs.as.mbp.service.testing.rerun;

import de.ipvs.as.mbp.domain.operator.Code;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.operator.parameters.Parameter;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterType;
import de.ipvs.as.mbp.repository.OperatorRepository;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private List<String> defaultRerunOperatorWhitelist;

    @Autowired
    private OperatorRepository operatorRepository;

    private static final String DESCRIPTOR_FILE = "operator.json";


}