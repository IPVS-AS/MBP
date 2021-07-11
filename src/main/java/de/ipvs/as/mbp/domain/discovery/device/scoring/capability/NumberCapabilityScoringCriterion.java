package de.ipvs.as.mbp.domain.discovery.device.scoring.capability;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescriptionCapability;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.discovery.processing.DeviceDescriptionScorer;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;

import java.util.Optional;

/**
 * Objects of this class represent scoring criteria for capabilities of devices that are expressed as numbers.
 */
@JsonIgnoreProperties
public class NumberCapabilityScoringCriterion extends CapabilityScoringCriterion {
    //Type name of this scoring criterion
    private static final String TYPE_NAME = "number_capability";

    //The variable name to use in transformation expressions
    private static final String VARIABLE_NAME = "x";

    //Math. expression that transforms the capability value to a score
    private String transformationExpression;

    /**
     * Creates a new scoring criterion for a capability that is expressed as number.
     */
    public NumberCapabilityScoringCriterion() {

    }

    /**
     * Creates a new scoring criterion for a capability that is expressed as number by using the name of the pertaining
     * capability and a mathematical expression that can be used to transform the capability value to a score.
     *
     * @param capabilityName           The name of the capability to which this criterion is supposed to be applied
     * @param transformationExpression The mathematical expression to use for transforming the capability value to a score
     */
    @JsonCreator
    public NumberCapabilityScoringCriterion(@JsonProperty("capabilityName") String capabilityName,
                                            @JsonProperty("transformationFunction") String transformationExpression) {
        //Set fields
        super(capabilityName);
        setTransformationExpression(transformationExpression);
    }

    /**
     * Returns the transformation expression that is used in order to transform the capability value to a score.
     *
     * @return The transformation expression
     */
    public String getTransformationExpression() {
        return transformationExpression;
    }

    /**
     * Sets the transformation expression that is used in order to transform the capability value to a score.
     *
     * @param transformationExpression The transformation expression to set
     * @return The number capability scoring criterion
     */
    public NumberCapabilityScoringCriterion setTransformationExpression(String transformationExpression) {
        this.transformationExpression = transformationExpression;
        return this;
    }

    /**
     * Returns the name of the requirement.
     *
     * @return The name
     */
    @JsonProperty("type")
    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    /**
     * Validates the device scoring criterion by extending the provided exception with information about invalid fields.
     *
     * @param exception   The exception to extend as part of the validation
     * @param fieldPrefix Prefix that is supposed to be added to the fields that are validated
     */
    @Override
    public void validate(EntityValidationException exception, String fieldPrefix) {
        //Call validation method of super class
        super.validate(exception, fieldPrefix);

        //Check transformation expression for null and empty
        if ((this.transformationExpression == null) || (this.transformationExpression.isEmpty())) {
            exception.addInvalidField(fieldPrefix + ".transformationExpression", "The transformation expression must not be empty.");
            return;
        }

        //Validate the transformation expression syntactically
        ValidationResult validationResult;
        try {
            validationResult = new ExpressionBuilder(this.transformationExpression)
                    .variable(VARIABLE_NAME).build() //Declare capability value variable
                    .setVariable(VARIABLE_NAME, 1) //Set variable to a test value
                    .validate(true); //Consider variables as well
        } catch (Exception e) {
            //Validation failed unexpectedly
            exception.addInvalidField(fieldPrefix + ".transformationExpression", "The expression is invalid: " + e.getMessage());
            return;
        }

        //Check validation result
        if (!validationResult.isValid()) {
            exception.addInvalidField(fieldPrefix + ".transformationExpression", "The expression is invalid: " + validationResult.getErrors().iterator().next());
        }
    }

    /**
     * Applies the scoring criterion to a given {@link DeviceDescription} and returns the resulting scoring increment
     * (positive number) or scoring decrement (negative number) for this description as result. In addition, a
     * reference to the {@link DeviceDescriptionScorer} that currently performs the overall score calculations is
     * provided, which may contain additional information about the collection of {@link DeviceDescription}s that are
     * currently subject to the scoring process and thus enables the calculation of relative scores.
     *
     * @param deviceDescription The device description for which the score increment of this scoring criterion is
     *                          supposed to be calculated
     * @param scorer            The {@link DeviceDescriptionScorer} that currently performs the overall score
     *                          calculations for a collection of {@link DeviceDescription}s.
     * @return The score increment/decrement that results from the application of this scoring criterion to the given
     * device description
     */
    @Override
    public double getScoreIncrement(DeviceDescription deviceDescription, DeviceDescriptionScorer scorer) {
        //Try to find a capability that matches the name and is of type number
        Optional<DeviceDescriptionCapability> capability =
                this.findCapability(deviceDescription, DeviceDescriptionCapability::isNumber);

        //Check if capability was found
        if (!capability.isPresent()) {
            return 0;
        }

        try {
            //Build the transformation expression for transforming the capability value to a score
            Expression transformationExpression = new ExpressionBuilder(this.transformationExpression)
                    .variable(VARIABLE_NAME).build();

            //Plug the capability value into the transformation expression and evaluate it
            return transformationExpression.setVariable(VARIABLE_NAME, capability.get().getValueAsDouble()).evaluate();
        } catch (Exception e) {
            //Something failed, thus do not influence the score
            return 0;
        }
    }
}
