package org.jboss.seam.test.unit.component;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

@Name("postConstructComponent")
public class PostConstructComponent {

	private int postConstructCalled = 0;
	private int countNullElements = 0;

	@Logger
	private Log log;

	@In(create = true)
	private MyEntityHome myEntityHome;

	public PostConstructComponent() {
		super();
	}

	@javax.annotation.PostConstruct
	//	@org.jboss.seam.annotations.Create
	public void postConstructFunction() {
		this.postConstructCalled++;
		if (log == null) {
			this.countNullElements++;
		}
		if (myEntityHome == null) {
			this.countNullElements++;
		}
	}

	public int getPostConstructCalled() {
		return postConstructCalled;
	}

	public int getCountNullElements() {
		return countNullElements;
	}

}
