package org.citopt.connde.domain.model;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
* Model entity.
* @author Imeri Amil
*/
@Document
public class Model implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue
    private String id;
    
    @NotNull
    private String name;
    
    @NotNull
    private String value;
    
    @NotNull
    private String username;

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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
