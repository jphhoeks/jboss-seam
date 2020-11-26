package org.jboss.seam.deployment;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

public class ScanResultsCache {

	public static final String KEY_CACHE_SCAN_RESULTS = "org.jboss.seam.deployment.CACHE_SCAN_RESULTS";
	private static ScanResultsCache instance = new ScanResultsCache();

	private Set<String> hits = Collections.synchronizedSet(new HashSet<String>());
	private Set<String> misses = Collections.synchronizedSet(new HashSet<String>());
	
	public static ScanResultsCache getInstance() {
		return instance;
	}

	

	private ScanResultsCache() {
		super();
	}
	
	public static ScanResultsCache getInstance(ServletContext context) {

		if (context.getAttribute(KEY_CACHE_SCAN_RESULTS) == null) {
			Boolean useCache = Boolean.valueOf(context.getInitParameter(KEY_CACHE_SCAN_RESULTS));
			ScanResultsCache cache = null;
			if (useCache) {
				cache = instance;
			} else {
				cache = new ScanResultsCache() {
					@Override
					public void addHit(String hit) {
						//
					}

					@Override
					public void addMiss(String hit) {
						//
					}
				};
			}
			context.setAttribute(KEY_CACHE_SCAN_RESULTS, cache);

			return cache;

		}

		return (ScanResultsCache) context.getAttribute(KEY_CACHE_SCAN_RESULTS);
	}

	public void addHit(String hit) {
		this.hits.add(hit);
	}

	public void addMiss(String miss) {
		this.misses.add(miss);
	}

	public boolean isHit(String test) {
		return this.hits.contains(test);
	}

	public boolean isMiss(String test) {
		return this.misses.contains(test);
	}

	public boolean isIndeterminate(String test) {
		return !isHit(test) && !isMiss(test);
	}

	public void reset() {
		this.hits.clear();
		this.misses.clear();
	}

	/**
	 * Only for debug purposes
	 */
	public Set<String> getHits() {
		Set<String> results = new HashSet<String>();
		synchronized (this.hits) {
			results.addAll(this.hits);
		}
		return results;
	}

	/**
	 * Only for debug purposes
	 */
	public Set<String> getMisses() {
		Set<String> results = new HashSet<String>();
		synchronized (this.misses) {
			results.addAll(this.misses);
		}
		return results;
	}

}