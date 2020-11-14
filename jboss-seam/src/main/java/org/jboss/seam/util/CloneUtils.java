package org.jboss.seam.util;

public final class CloneUtils {

	private CloneUtils() {
		throw new AssertionError("No instances allowed");
	}	
	
	public static <T> T[]  cloneArray(T[] array) {
		if (array == null) {
			return null;
		}		
		return array.clone();		
	}
}
