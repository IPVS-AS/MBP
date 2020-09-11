package org.citopt.connde.domain.testing;

import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.rules.RuleTrigger;
import org.citopt.connde.service.cep.engine.core.output.CEPOutput;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import java.util.Set;

@Document
public class Testing  {
    @Id
    @GeneratedValue
    private String id;

    @DBRef
    private RuleTrigger trigger;

    private CEPOutput output;

    private Set<String> rule;

    private static final String COMPONENT_TYPE_NAME = "testing-tool";
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
