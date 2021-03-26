package de.ipvs.as.mbp.domain.operator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.domain.operator.parameters.Parameter;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import org.apache.commons.io.FilenameUtils;
import org.springframework.data.annotation.Id;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import javax.persistence.GeneratedValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Document class for operators.
 */
@MBPEntity(createValidator = OperatorCreateValidator.class)
public class Operator extends UserEntity {
    //Whitelist of file extensions indicating files to which line break fixes are applied
    private static final List<String> LBs_FIX_EXTENSION_WHITELIST = Arrays.asList("sh", "py", "md", "txt", "json",
            "xml", "yaml", "js");

    @Id
    @GeneratedValue
    private String id;

    private String name;

    private String description;

    private String unit;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Code> routines;

    private List<Parameter> parameters;

    public Operator() {
        this.routines = new ArrayList<>();
        this.parameters = new ArrayList<>();
    }

    public List<Code> getRoutines() {
        return routines;
    }

    public Operator setRoutines(List<Code> routines) {
        this.routines = routines;
        return this;
    }

    public Operator addRoutine(Code routine) {
        this.routines.add(routine);
        return this;
    }

    public boolean hasRoutines() {
        return !this.routines.isEmpty();
    }

    public String getId() {
        return id;
    }

    public Operator setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Operator setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Operator setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public Operator setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    @JsonIgnore
    public Unit<? extends Quantity> getUnitObject() {
        try {
            return Unit.valueOf(this.unit);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public Operator setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * Replaces all line breaks of certain routine files by LF line breaks in order to avoid incompatibilities when
     * using files that were created on non-unix operating systems (Windows etc.). The replacement is only applied to
     * files whose MIME type of the base64 encoding starts with "text" r files having a file extension that
     * is part of the whitelist.
     */
    public void replaceLineBreaks() {
        //Iterate over all routine files
        for (Code file : routines) {
            //Get MIME prefix for current file
            String mimePrefix = file.getBase64MimePrefix();

            //Get extension of current file
            String fileExtension = FilenameUtils.getExtension(file.getName());

            //Check if replacements may be applied to the routine file
            if (!(mimePrefix.startsWith("data:text") || LBs_FIX_EXTENSION_WHITELIST.contains(fileExtension))) {
                continue;
            }

            //Decode file content from base64
            String decodedContent = new String(Base64.getDecoder().decode(file.getContent()));

            //Replace all line breaks with LF line breaks
            String fixedContent = decodedContent.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");

            //Encode file content to base64 again and prepend prefix
            String encodedContent = mimePrefix + Base64.getEncoder().encodeToString(fixedContent.getBytes());

            //Set fixed and encoded file content
            file.setContent(encodedContent);
        }
    }
}