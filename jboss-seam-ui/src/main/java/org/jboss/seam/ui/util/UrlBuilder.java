package org.jboss.seam.ui.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.faces.component.UIParameter;

public class UrlBuilder {
	private static final Charset URL_ENCODING = StandardCharsets.UTF_8;

	private String url;

	private String fragment;
	private String characterEncoding;

	private boolean urlEncodeParameters = true;

	private Map<String, String> parameters;

	protected UrlBuilder(String fragment, String characterEncoding) {
		this.fragment = fragment;
		parameters = new LinkedHashMap<String, String>();
		this.characterEncoding = characterEncoding;
	}

	public UrlBuilder(String fragment, String characterEncoding, boolean urlEncodeParameters) {
		this(fragment, characterEncoding);
		this.urlEncodeParameters = urlEncodeParameters;
	}

	public UrlBuilder(String url, String fragment, String characterEncoding) {
		this(fragment, characterEncoding);
		setUrl(url);
	}

	public UrlBuilder(String url, String fragment, String characterEncoding, boolean urlEncodeParameters) {
		this(url, fragment, characterEncoding);
		this.urlEncodeParameters = urlEncodeParameters;
	}

	protected void setUrl(String url) {
		if (url == null) {
			throw new NullPointerException("url must not be null");
		}
		this.url = url;
	}

	protected String urlEncode(String value) throws UnsupportedEncodingException {
		return characterEncoding == null ? URLEncoder.encode(value, URL_ENCODING.name()) : URLEncoder.encode(value, characterEncoding);
	}

	protected String getFragment() {
		if (fragment != null && !"".equals(fragment)) {
			return "#" + fragment;
		} else {
			return "";
		}
	}

	public String getEncodedUrl() {
		String params = getParametersAsString();
		if (!"".equals(params) && !url.contains("?")) {
			params = "?" + params.substring(1);
		}
		return url + params + getFragment();
	}

	protected String getParametersAsString() {
		StringBuilder params = new StringBuilder();
		
		for (Map.Entry<String, String> entry: parameters.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			params.append('&').append(key).append('=').append(value);
		}
		return params.toString();
	}

	protected Map<String, String> getParameters() {
		return parameters;
	}

	public void addParameter(String name, String value) throws UnsupportedEncodingException {
		parameters.put(name, urlEncodeParameters ? urlEncode(value) : value);
	}

	public void addParameter(UIParameter parameter) throws UnsupportedEncodingException {
		String value = parameter.getValue() == null ? "" : parameter.getValue().toString();
		String name = parameter.getName();
		addParameter(name, value);
	}

}
