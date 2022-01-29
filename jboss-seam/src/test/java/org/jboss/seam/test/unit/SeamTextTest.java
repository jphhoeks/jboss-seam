package org.jboss.seam.test.unit;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.jboss.seam.text.SeamTextLexer;
import org.jboss.seam.text.SeamTextParser;
import org.junit.Test;
import org.junit.Assert;

public class SeamTextTest {
	
	public SeamTextTest () {
		super();
	}
	
	
	@Test
	public void smoke() throws Exception {
		try (Reader reader = new InputStreamReader(SeamTextTest.class.getResourceAsStream("SeamTextTest.txt"))) {
			transform(reader);
		}
	}
	
	@Test
	public void empty() throws Exception {
		String seamText = "";
		String result = "";
		Assert.assertEquals(result, transform(seamText));
	}
	
	@Test
	public void example1() throws Exception {
		String seamText = "It's easy to make *emphasis*, |monospace|,\n"
				+ "~deleted text~, super^scripts^ or _underlines_.";
		String result = "<p class=\"seamTextPara\">\n"
				+ "It's easy to make <i class=\"seamTextEmphasis\">emphasis</i>, <tt>monospace</tt>,"
				+ "\n"
				+ "<del>deleted text</del>, super<sup>scripts</sup> or <u>underlines</u>."
				+ "</p>\n";
		Assert.assertEquals(result, transform(seamText));
	}
	

	
	@Test
	public void example2() throws Exception {
		String seamText = "+This is a big heading\n"
				+ "You *must* have some text following a heading!\n"
				+ "\n"
				+ "++This is a smaller heading\n"
				+ "This is the first paragraph. We can split it across multiple \n"
				+ "lines, but we must end it with a blank line.\n"
				+ "\n"
				+ "This is the second paragraph.\n";
		String result = "<h1 class=\"seamTextHeadline1\">This is a big heading</h1>\n"
				+ "<p class=\"seamTextPara\">\n"
				+ "You <i class=\"seamTextEmphasis\">must</i> have some text following a heading!\n"
				+ "</p>\n"
				+ "\n"
				+ "<h2 class=\"seamTextHeadline2\">This is a smaller heading</h2>\n"
				+ "<p class=\"seamTextPara\">\n"
				+ "This is the first paragraph. We can split it across multiple \n"
				+ "lines, but we must end it with a blank line.\n"
				+ "</p>\n"
				+ "\n"
				+ "<p class=\"seamTextPara\">\n"
				+ "This is the second paragraph.\n"
				+ "</p>\n";
		String generated = transform(seamText);
		Assert.assertEquals(result, generated);
	}
	
	@Test
	public void example3() throws Exception {
		String seamText = "An ordered list:\n"
				+ "\n"
				+ "#first item\n"
				+ "#second item\n"
				+ "#and even the /third/ item\n"
				+ "\n"
				+ "An unordered list:\n"
				+ "\n"
				+ "=an item\n"
				+ "=another item";
		String result = "<p class=\"seamTextPara\">\n"
				+ "An ordered list:\n"
				+ "</p>\n"
				+ "\n"
				+ "<ol class=\"seamTextOrderedList\">\n"
				+ "<li class=\"seamTextOrderedListItem\">first item</li>\n"
				+ "<li class=\"seamTextOrderedListItem\">second item</li>\n"
				+ "<li class=\"seamTextOrderedListItem\">and even the /third/ item</li>\n"
				+ "</ol>\n"
				+ "\n"
				+ "<p class=\"seamTextPara\">\n"
				+ "An unordered list:\n"
				+ "</p>\n"
				+ "\n"
				+ "<ul class=\"seamTextUnorderedList\">\n"
				+ "<li class=\"seamTextUnorderedListItem\">an item</li>\n"
				+ "<li class=\"seamTextUnorderedListItem\">another item</li></ul>\n";
		String generated = transform(seamText);
		Assert.assertEquals(result, generated);
	}

	
	@Test
	public void example4() throws Exception {
		String seamText = "The other guy said:\n"
				+ "\n"
				+ "\"Nyeah nyeah-nee \n"
				+ "/nyeah/ nyeah!\"\n"
				+ "\n"
				+ "But what do you think he means by \"nyeah-nee\"?";
		String result = "<p class=\"seamTextPara\">\n"
				+ "The other guy said:\n"
				+ "</p>\n"
				+ "\n"
				+ "<blockquote class=\"seamTextBlockquote\">\n"
				+ "Nyeah nyeah-nee \n"
				+ "/nyeah/ nyeah!\n"
				+ "</blockquote>\n"
				+ "\n"
				+ "<p class=\"seamTextPara\">\n"
				+ "But what do you think he means by <q>nyeah-nee</q>?</p>\n"
				+ "";
		String generated = transform(seamText);
		Assert.assertEquals(result, generated);
	}
	

	
	@Test
	public void example5() throws Exception {
		String seamText = "You can write down equations like 2\\*3\\=6 and HTML tags\n"
				+ "like \\<body\\> using the escape character: \\\\.";
		String result = "<p class=\"seamTextPara\">"
				+ "\n"
				+ "You can write down equations like 2*3=6 and HTML tags"
				+ "\n"
				+ "like &lt;body&gt; using the escape character: \\."
				+ "</p>"
				+ "\n"
				;
		Assert.assertEquals(result, transform(seamText));
	}
	
	@Test
	public void example6() throws Exception {
		String seamText = "My code doesn't work:\n"
				+ "\n"
				+ "`for (int i=0; i<100; i--)\n"
				+ "{\n"
				+ "    doSomething();\n"
				+ "}`\n"
				+ "\n"
				+ "Any ideas?";
		String result = "<p class=\"seamTextPara\">\n"
				+ "My code doesn't work:\n"
				+ "</p>\n"
				+ "\n"
				+ "<pre class=\"seamTextPreformatted\">\n"
				+ "for (int i=0; i&lt;100; i--)\n"
				+ "{\n"
				+ "    doSomething();\n"
				+ "}</pre>\n"
				+ "\n"
				+ "\n"
				+ "<p class=\"seamTextPara\">\n"
				+ "Any ideas?</p>\n";
		String generated = transform(seamText);
		Assert.assertEquals(result, generated);
	}
	
	@Test
	public void example7() throws Exception {
		String seamText = "This is a |<tag attribute=\"value\"/>| example.";
		String result = "<p class=\"seamTextPara\">\n" 
				+ "This is a <tt>&lt;tag attribute=&quot;value&quot;/&gt;</tt> example."
				+ "</p>\n";
		Assert.assertEquals(result, transform(seamText));
	}
	
	@Test
	public void example8() throws Exception {
		String seamText = "Go to the Seam website at [=>http://jboss.org/schema/seam].";
		String result = "<p class=\"seamTextPara\">\n" 
				+ "Go to the Seam website at <a href=\"http://jboss.org/schema/seam\" class=\"seamTextLink\"></a>." 
				+ "</p>\n";
		Assert.assertEquals(result, transform(seamText));
	}
	@Test
	public void example9() throws Exception {
		String seamText = "Go to [the Seam website=>http://jboss.org/schema/seam].";
		String result = "<p class=\"seamTextPara\">\n" 
		+ "Go to <a href=\"http://jboss.org/schema/seam\" class=\"seamTextLink\">the Seam website</a>." 
		+ "</p>\n";
		Assert.assertEquals(result, transform(seamText));
	}

	
	@Test
	public void examplehtml1() throws Exception {
		String seamText = "You might want to link to <a href=\"http://jboss.org/schema/seam\">something "
				+ "cool</a>, or even include an image: <img src=\"/logo.jpg\"/>";
		String result = "<p class=\"seamTextPara\">\n" + seamText + "</p>\n";
		Assert.assertEquals(result, transform(seamText));
	}
	
	
	@Test
	public void examplehtml2() throws Exception {
		String seamText = "<table>\n"
				+ "\n"
				+ "    <tr><td>First name:</td><td>Gavin</td></tr>\n"
				+ "\n"
				+ "    <tr><td>Last name:</td><td>King</td></tr>\n"
				+ "\n"
				+ "</table>";
		String result = seamText;
		Assert.assertEquals(result, transform(seamText));
	}
	
	

	private String transform(String text) throws Exception {
		try (Reader reader = new StringReader(text)) {
			return transform(reader);
		}
	}
	private String transform(Reader reader) throws Exception {
		SeamTextLexer lexer = new SeamTextLexer(reader);
		SeamTextParser parser = new SeamTextParser(lexer);
		parser.startRule();
		return parser.toString();
	}
}
