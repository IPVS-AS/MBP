package org.citopt.connde.domain.access_control;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.GeneratedValue;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Document
@Relation(collectionRelation = "testObj")
public class TestObj extends RepresentationModel<TestObj> {
	
	@Id
    @GeneratedValue
    public String id;

	public String s;
	
	public int i;
	
	public List<ACPolicy<?>> policies = new ArrayList<>();
	
	public TestObj() {
		// TODO Auto-generated constructor stub
	}
	
	public TestObj(String id, String s, int i, List<ACPolicy<?>> policies) {
		this.id = id;
		this.i = i;
		this.s = s;
		this.policies = policies;
	}
	
	public String getId() {
		return id;
	}
	
	public TestObj setId(String id) {
		this.id = id;
		return this;
	}
	
	public int getI() {
		return i;
	}
	
	public TestObj setI(int i) {
		this.i = i;
		return this;
	}
	
	public String getS() {
		return s;
	}
	
	public TestObj setS(String s) {
		this.s = s;
		return this;
	}
	
	public List<ACPolicy<?>> getPolicies() {
		return policies;
	}
	
	public TestObj setPolicies(List<ACPolicy<?>> policies) {
		this.policies = policies;
		return this;
	}
	
}
