package org.citopt.connde.service.rules.execution.executors;

import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.rules.RuleAction;
import org.citopt.connde.service.cep.engine.core.output.CEPOutput;
import org.citopt.connde.service.rules.execution.RuleActionExecutor;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
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
    private static final String PARAM_KEY_JSON_DATA = "ifttt_json";

    //Regular expression describing permissible personal IFTTT keys
    private static final String REGEX_IFTTT_KEY = "[A-z0-9_\\-]{10,}";
    //Regular expression describing permissible event names
    private static final String REGEX_EVENT_NAME = "[A-z0-9_\\-]+";
    //Regular expression describing permissible keys in the JSON data
    private static final String REGEX_JSON_KEYS = "value[1-9][0-9]*";

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

        //Check JSON data parameter (optional)
        if (parameters.containsKey(PARAM_KEY_EVENT_NAME)) {
            //Get JSON data
            String jsonData = parameters.get(PARAM_KEY_JSON_DATA);

            //Check if JSON data was provided by user
            if ((jsonData == null) || jsonData.isEmpty()) {
                return;
            }

            //Check if JSON data is valid
            try {
                //Try to create a JSON object from the data string
                JSONObject jsonObject = new JSONObject(jsonData);

                //Check keys of the JSON object on highest level
                Iterator keyIterator = jsonObject.keys();
                while (keyIterator.hasNext()) {
                    //Get current key
                    String currentKey = (String) keyIterator.next();

                    //Check against regular expression
                    if (!currentKey.matches(REGEX_JSON_KEYS)) {
                        throw new IllegalArgumentException();
                    }
                }

            } catch (Exception e) {
                errors.rejectValue("parameters", "ruleAction.parameters.invalid",
                        "The provided JSON data seems to be invalid.");
            }
        }
    }

    /**
     * Executes the action of a given rule of the corresponding rule action type. In addition, the output
     * of a CEP engine that triggered the execution of this rule action may be passed. The return value of this method indicates whether
     * the execution of the rule action was successful.
     *
     * @param rule   The rule that holds the action that is supposed to be executed
     * @param output The output of a CEP engine that triggered the execution of this rule action (may be null)
     * @return True, if the execution of the rule action was successful; false otherwise
     */
    @Override
    public boolean execute(Rule rule, CEPOutput output) {
        //Get rule action
        RuleAction action = rule.getAction();

        //Get action parameters
        Map<String, String> parameters = action.getParameters();
        String key = parameters.get(PARAM_KEY_IFTTT_KEY);
        String eventName = parameters.get(PARAM_KEY_EVENT_NAME);
        String jsonData = parameters.get(PARAM_KEY_JSON_DATA);

        //Sanitize JSON data
        if (jsonData == null) {
            jsonData = "";
        }

        //Generate webhook URL
        String webhookURL = generateWebhookURL(key, eventName);

        try {
            URL url = new URL(webhookURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            OutputStream os = con.getOutputStream();
            byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
            os.close();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            br.close();

            System.out.println(response.toString());
        } catch (Exception e) {
            return false;
        }
        return true;
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
