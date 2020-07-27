package org.citopt.connde.domain.access_control;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.GeneratedValue;

import org.citopt.connde.domain.user_entity.UserEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TestObj extends UserEntity {
	
	@Id
    @GeneratedValue
    public String id;

	public String s;
	
	public int i;
	
	public List<ACPolicy<?>> policies = new ArrayList<>();
	
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}
}
