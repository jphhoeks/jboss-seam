package org.jboss.seam.document;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.jboss.seam.util.Resources;

/**
 * Stores DocumentData in a file, delete at end when DocumentStore is destroyed.
 */
public class FileDocumentData extends DocumentData {

	private static final long serialVersionUID = 1L;
	private File data;

	public FileDocumentData(String baseName, DocumentType documentType, File data) {
		super(baseName, documentType);
		this.data = data;
	}

	@Override
	public void writeDataToStream(OutputStream stream) throws IOException {
		Files.copy(data.toPath(), stream);
	}

	public File getData() {
		return this.data;
	}

}
