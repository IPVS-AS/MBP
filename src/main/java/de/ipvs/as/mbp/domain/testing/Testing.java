package de.ipvs.as.mbp.domain.testing;

import de.ipvs.as.mbp.domain.rules.RuleTrigger;
import de.ipvs.as.mbp.domain.valueLog.ValueLog;
import de.ipvs.as.mbp.service.cep.engine.core.output.CEPOutput;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import java.util.Map;
import java.util.Set;

@Document
public class Testing  {
    @Id
    @GeneratedValue
    private String id;

    @DBRef
    private RuleTrigger trigger;

    private CEPOutput output;

    // Stores all ValueLogs for each event which are part of the rule specified by the RuleTrigger.
    private Map<String, ValueLog> valueLogEventNameMap;

    private Set<String> rule;

    private static final String COMPONENT_TYPE_NAME = "testing-tool";

    public Map<String, ValueLog> getValueLogEventNameMap() {
        return valueLogEventNameMap;
    }

    public void setValueLogEventNameMap(Map<String, ValueLog> valueLogEventNameMap) {
        this.valueLogEventNameMap = valueLogEventNameMap;
    }

    /**
     * Returns the rule name the entry belongs to.
     *
     * @return rule
     */
    public Set<String> getRule() {
        return rule;
    }

    /**
     * Sets the rule name the entry belongs to.
     *
     * @param rule rule name
     */
    public void setRule(Set<String> rule) {
        this.rule = rule;
    }


    /**
     * Returns the trigger id of the entry.
     *
     * @return trigger id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the trigger id of the entry.
     *
     * @param id trigger id
     */
    public void setId(String id) {
        this.id = id;
    }


    /**
     * Returns the rule trigger  of the entry.
     *
     * @return rule trigger
     */
    public RuleTrigger getTrigger() {
        return trigger;
    }

    /**
     * Sets the rule trigger of the entry.
     *
     * @param trigger rule trigger
     */
    public void setTrigger(RuleTrigger trigger) {
        this.trigger = trigger;
    }

    /**
     * Returns the the time and value by which the rule was triggered.
     *
     * @return output of the cep engine
     */
    public CEPOutput getOutput() {
        return output;
    }

    /**
     * Sets the the time and value by which the rule was triggered.
     *
     * @param output of the cep engine
     */
    public void setOutput(CEPOutput output) {
        this.output = output;
    }




}
