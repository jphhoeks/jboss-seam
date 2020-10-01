package org.jboss.seam.util;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XML {
	public static Element getRootElement(InputStream stream) throws DocumentException {
		try {
			SAXReader saxReader = new SAXReader();
			saxReader.setEntityResolver(new DTDEntityResolver());
			saxReader.setMergeAdjacentText(true);
			return saxReader.read(stream).getRootElement();
		} catch (DocumentException e) {
			Throwable nested = e.getCause();
			if (nested != null) {
				if (nested instanceof FileNotFoundException) {
					throw new RuntimeException("Can't find schema/DTD reference: " + nested.getMessage(), e);
				} else if (nested instanceof UnknownHostException) {
					throw new RuntimeException("Cannot connect to host from schema/DTD reference: " + nested.getMessage()
							+ " - check that your schema/DTD reference is current", e);
				}
			}
			throw e;
		}
	}

	/**
	* Parses an XML document safely, as to not resolve any external DTDs
	*/
	public static Element getRootElementSafely(InputStream stream) throws DocumentException {
		SAXReader saxReader = new SAXReader();
		saxReader.setEntityResolver(new NullEntityResolver());
		saxReader.setMergeAdjacentText(true);
		return saxReader.read(stream).getRootElement();
	}

	public static class NullEntityResolver implements EntityResolver {
		private static final byte[] empty = new byte[0];

		@Override
		public InputSource resolveEntity(String systemId, String publicId) throws SAXException, IOException {
			return new InputSource(new ByteArrayInputStream(empty));
		}

	}

	/**
	* Get safe SaxReader with doctype feature disabled 
	* <a href="http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl">http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl</a> 
	* @return safe SaxReader with doctype feature disabled 
	* @throws Exception
	*/
	public static SAXReader getSafeSaxReader() throws Exception {
		SAXReader xmlReader = new SAXReader();
		xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		return xmlReader;
	}

	/**
	 * Get escaped &lt;, ", ' characters from input
	 * 
	 * @param input
	 * @return escaped string
	 */
	public static String escapeXMLChars(String input) {
		String escapedStr = input;
		if (input != null && input.length() > 0) {
			escapedStr = input.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;");
		}
		return escapedStr;
	}
}
