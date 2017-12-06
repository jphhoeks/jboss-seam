package org.jboss.seam.tool;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class PrintTask extends Task
{
   private String file;

   public void setFile(String file)
   {
      this.file = file;
   }
   
   @Override
   public void execute() throws BuildException
   {
      try
      {
         BufferedReader reader = Files.newBufferedReader(new File(file).toPath(), StandardCharsets.UTF_8);
         while ( reader.ready() )
         {
            System.out.println( reader.readLine() );
         }
         reader.close();
      }
      catch (Exception e)
      {
         throw new BuildException(e);
      }
   }
}
