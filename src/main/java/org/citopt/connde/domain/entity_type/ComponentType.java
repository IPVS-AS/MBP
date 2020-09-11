package org.citopt.connde.domain.entity_type;

import javax.persistence.GeneratedValue;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Type entity.
 * @author Imeri Amil
 */
@Document
public class ComponentType {
	
    @Id
    @GeneratedValue
    private String id;
    
    @Indexed
    private String name;
    
    @Indexed
    private String component;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}
}
