package org.jboss.seam.document;

import java.io.IOException;
import java.io.OutputStream;

public class ByteArrayDocumentData extends DocumentData
{

   private static final long serialVersionUID = -471561347477979673L;
private byte[] data;

   public ByteArrayDocumentData(String baseName, DocumentType documentType, byte[] data)
   {
      super(baseName, documentType);
      this.data = data;
   }

   @Override
   public void writeDataToStream(OutputStream stream) throws IOException
   {
      stream.write(data);
   }

   public byte[] getData()
   {
      return data;
   }

}