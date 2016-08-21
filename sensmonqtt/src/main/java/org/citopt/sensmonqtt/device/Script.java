/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;

/**
 *
 * @author rafaelkperes
 */
@Entity("scripts")
@Indexes({
    @Index(fields = {
        @Field("name")},
            options = @IndexOptions(unique = true))
})
public class Script {

    @Id
    private ObjectId id;

    private String name;
    private String description;
    private String service;
    private List<Map<ScriptIndex, String>> script;

    public enum ScriptIndex {
        NAME(0),
        CONTENT(1)
        ;
        
        private int val;
        
        private ScriptIndex(int val) {
            this.val = val;
        }
        
        public int get() {
            return this.val;
        }
    }
    
    public Script() {
    }

    public Script(String name, String description) {
        this.name = name;
        this.description = description;
        this.script = null;
        
        this.script = new ArrayList<>();
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getScriptName(int index) {
        return script.get(index).get(0);
    }

    public String getScript(int index) {
        return script.get(index).get(1);
    }

    public List<Map<ScriptIndex, String>> getScript() {
        return script;
    }

    public void setScript(List<Map<ScriptIndex, String>> script) {
        this.script = script;
    }
    
    public void addScript(String name, String script) {
        Map<ScriptIndex, String> m = new HashMap<>();
        m.put(ScriptIndex.NAME, name);
        m.put(ScriptIndex.CONTENT, script);
        this.script.add(m);
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Script other = (Script) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

}
