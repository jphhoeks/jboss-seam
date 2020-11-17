package org.jboss.seam.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CollectionsUtils {
	
	private CollectionsUtils() {
		throw new AssertionError("NO instances");
	}
	
	@SafeVarargs
	public static <T> Set<T> set(T... elements) {
		return new HashSet<T>(list(elements));
	}
	
	@SafeVarargs
	public static <T> Set<T> unmodifiableSet(T... elements) {
		return Collections.unmodifiableSet(set(elements));
	}
	
	@SafeVarargs
	public static <T> List<T> list(T... elements) {
		return Arrays.asList(elements);
	}
	
	@SafeVarargs
	public static <T> List<T> unmodifiableList(T... elements) {
		return Collections.unmodifiableList(list(elements));
	}

}
