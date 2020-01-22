package org.citopt.connde.domain.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
* Authority entity.
* @author Imeri Amil
*/
@Document
public class Authority implements Serializable {
	
		private static final long serialVersionUID = 1L;

	    @NotNull
	    @Size(min = 0, max = 50)
	    @Id
	    private String name;

	    public Authority(){

		}

		public Authority(String name){
	    	this.name = name;
		}

	    public String getName() {
	        return name;
	    }

	    public void setName(String name) {
	        this.name = name;
	    }

	    @Override
	    public boolean equals(Object o) {
	        if (this == o) {
	            return true;
	        }
	        if (o == null || getClass() != o.getClass()) {
	            return false;
	        }

	        Authority authority = (Authority) o;

            return name != null ? name.equals(authority.name) : authority.name == null;
        }

	    @Override
	    public int hashCode() {
	        return name != null ? name.hashCode() : 0;
	    }

	    @Override
	    public String toString() {
	        return "Authority{" +
	            "name='" + name + '\'' +
	            "}";
	    }
}
