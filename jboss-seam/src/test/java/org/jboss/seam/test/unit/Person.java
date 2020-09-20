package org.jboss.seam.test.unit;

import java.io.Serializable;

import org.jboss.seam.annotations.Name;

@Name("person")
public class Person implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name;

	public Person(String name) {
		super();
		this.name = name;
	}

	public Person() {
		super();
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Person other = (Person) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
	
	
	

}
