package de.ipvs.as.mbp.service.rules.execution.ifttt_webhook;

import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.rules.RuleAction;
import de.ipvs.as.mbp.service.rules.execution.RuleActionExecutor;
import de.ipvs.as.mbp.service.cep.engine.core.output.CEPOutput;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Executor for IFTTT webhook (https://ifttt.com/maker_webhooks) actions.
 */
@Component
public class IFTTTWebhookExecutor implements RuleActionExecutor {
    //URL to use for triggering IFTTT webhooks
    private static final String IFTTT_WEBHOOK_URL = "https://maker.ifttt.com/trigger/%s/with/key/%s";

    //Parameter keys
    private static final String PARAM_KEY_IFTTT_KEY = "ifttt_key";
    private static final String PARAM_KEY_EVENT_NAME = "ifttt_name";

    //Regular expression describing permissible personal IFTTT keys
    private static final String REGEX_IFTTT_KEY = "[A-z0-9_\\-]{10,}";
    //Regular expression describing permissible event names
    private static final String REGEX_EVENT_NAME = "[A-z0-9_\\-]+";

    /**
     * Validates a parameters map for the corresponding rule action type and updates
     * an errors object accordingly.
     *
     * @param errors     The errors object to update
     * @param parameters The parameters map (parameter name -> value) to validate
     */
    @Override
    public void validateParameters(Errors errors, Map<String, String> parameters) {
        //Check key parameter
        if (parameters.containsKey(PARAM_KEY_IFTTT_KEY)) {
            //Get key
            String key = parameters.get(PARAM_KEY_IFTTT_KEY);

            //Check if key is valid
            if ((key == null) || key.isEmpty() || (!key.matches(REGEX_IFTTT_KEY))) {
                errors.rejectValue("parameters", "ruleAction.parameters.invalid",
                        "The provided key seems to be invalid.");
            }
        } else {
            //No key parameter available
            errors.rejectValue("parameters", "ruleAction.parameters.missing",
                    "A personal IFTTT key needs to be provided.");
        }

        //Check event name parameter
        if (parameters.containsKey(PARAM_KEY_EVENT_NAME)) {
            //Get event name
            String eventName = parameters.get(PARAM_KEY_EVENT_NAME);

            //Check if event name is valid
            if ((eventName == null) || eventName.isEmpty() || (!eventName.matches(REGEX_EVENT_NAME))) {
                errors.rejectValue("parameters", "ruleAction.parameters.invalid",
                        "The provided event name seems to be invalid.");
            }
        } else {
            //No event name parameter available
            errors.rejectValue("parameters", "ruleAction.parameters.missing",
                    "A IFTTT event name needs to be provided.");
        }
    }

    /**
     * Executes an given action of a given rule that is of the corresponding rule action type. In addition, the output
     * of a CEP engine that triggered the execution may be passed. The return value of this method indicates whether
     * the execution of the rule action was successful.
     *
     * @param action The rule action to execute
     * @param rule   The rule that holds the action that is supposed to be executed
     * @param output The output of a CEP engine that triggered the execution of this rule action (may be null)
     * @return True, if the execution of the rule action was successful; false otherwise
     */
    @Override
    public boolean execute(RuleAction action, Rule rule, CEPOutput output) {
        //Get action parameters
        Map<String, String> parameters = action.getParameters();
        String key = parameters.get(PARAM_KEY_IFTTT_KEY);
        String eventName = parameters.get(PARAM_KEY_EVENT_NAME);

        //Generate webhook URL
        String webhookURL = generateWebhookURL(key, eventName);

        try {
            //Open connection and send request
            URL url = new URL(webhookURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);

            //Establish reader for the result of the request
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            //Read response line by line
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();

            //Check if request was successful
            return result.toString().startsWith("Congratulations!");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generates the callable IFTTT webhook URL from an event name and a personal key.
     *
     * @param eventName The event name to use
     * @param key       The IFTTT key to use
     * @return The generated webhook URL
     */
    private static String generateWebhookURL(String key, String eventName) {
        //Sanity check
        if ((eventName == null) || eventName.isEmpty()) {
            throw new IllegalArgumentException("Event name must not be null or empty.");
        } else if ((key == null) || key.isEmpty()) {
            throw new IllegalArgumentException("Key must not be null or empty.");
        }

        //Format URL
        return String.format(IFTTT_WEBHOOK_URL, eventName, key);
    }
}
