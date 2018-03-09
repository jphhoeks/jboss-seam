package org.jboss.seam.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;

import javax.faces.context.FacesContext;

import org.jboss.seam.core.Manager;
import org.jboss.seam.document.DocumentData;
import org.jboss.seam.document.DocumentData.DocumentType;
import org.jboss.seam.document.DocumentStore;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.ui.component.UIResource;
import org.jboss.seam.util.Resources;

/**
 * Helper class for DocumentStore 
 * 
 * @author mnovotny
 *
 */
public class DocumentStoreUtils
{
    private static final int BUFFER_SIZE = 32768;

    public static String addResourceToDataStore(FacesContext ctx, UIResource resource) {
        String baseName = Pages.getCurrentBaseName();
        String viewId = Pages.getViewId(ctx);

        DocumentStore store = DocumentStore.instance();
        String id = store.newId();

        DocumentType type = new DocumentType("", resource.getContentType());

        DocumentData documentData = new DownloadableDocumentData(baseName, type, resource.getData());
        documentData.setFilename(resource.getFileName());
        documentData.setDisposition(resource.getDisposition());

        String url = store.preferredUrlForContent(resource.getFileName(), type.getExtension(), id);
        url = Manager.instance().encodeConversationId(url, viewId);
        store.saveData(id, documentData);
        return url;
    }

    static class DownloadableDocumentData 
        extends DocumentData 
    {
        private static final long serialVersionUID = 1L;
		private Object data;

        public DownloadableDocumentData(String baseName, DocumentType type, Object data) {
            super(baseName, type);
            this.data = data;
        }

        @Override
        public void writeDataToStream(OutputStream os) 
            throws IOException 
        {
            if (data instanceof byte[]) {
                os.write((byte[]) data);
            } else if (data instanceof File) {
            	Files.copy(((File) data).toPath(), os);
            } else if (data instanceof InputStream) {
                writeStream(os, (InputStream) data);
            }
        }

        private void writeStream(OutputStream os, InputStream is)
            throws IOException 
                       
        {   
            ReadableByteChannel in =  null;
            WritableByteChannel out = null;
            
            try {
            	in = Channels.newChannel(is);
            	out = Channels.newChannel(os);
                copyChannel(in, out);
            } finally {
            	Resources.close(in, out);
            }
        }
        
        private void copyChannel(ReadableByteChannel in, WritableByteChannel out) 
            throws IOException 
        {
            ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
            
            while (in.read(buffer) != -1 || buffer.position() > 0) {
                buffer.flip();
                out.write(buffer);
                buffer.compact();
            }
        }               
    }

}
