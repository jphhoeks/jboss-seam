//$Id: IteratorEnumeration.java 5629 2007-06-29 00:41:37Z gavin $
package org.jboss.seam.util;

import java.util.Enumeration;
import java.util.Iterator;

public class IteratorEnumeration<T> implements Enumeration<T> {

	private Iterator<? extends T> iterator;

	public IteratorEnumeration(Iterator<? extends T> iterator) {
		this.iterator = iterator;
	}

	public boolean hasMoreElements() {
		return iterator.hasNext();
	}

	public T nextElement() {
		return iterator.next();
	}

}
