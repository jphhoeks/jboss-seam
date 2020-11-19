package org.jboss.seam.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

	public static <K> HashSet<K> newHashSet(int expectedSize) {
		return new HashSet<>(calculateInitialSizeForHashmaps(expectedSize));
	}

	public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(int expectedSize) {
		return new LinkedHashMap<>(calculateInitialSizeForHashmaps(expectedSize));
	}

	
	public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap(int expectedSize) {
		return new ConcurrentHashMap<>(calculateInitialSizeForHashmaps(expectedSize));
	}

	public static <K, V> HashMap<K, V> newHashMap(int expectedSize) {
		return new HashMap<>(calculateInitialSizeForHashmaps(expectedSize));
	}

	private static int calculateInitialSizeForHashmaps(int expectedSize) {
		if (expectedSize <= 0) {
			// Default
			return 16;
		}
		if (expectedSize < 3) {
			return expectedSize + 1;
		}
		int MAX_POWER_OF_TWO = 1 << (Integer.SIZE - 2);
		if (expectedSize < MAX_POWER_OF_TWO) {
			return (int) ((float) expectedSize / 0.75f + 1.0f);
		}
		return Integer.MAX_VALUE;

	}

}
