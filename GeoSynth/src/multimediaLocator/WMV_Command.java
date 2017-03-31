package multimediaLocator;

import java.io.*;
import java.util.List;

/**
 * @author davidgordon
 * Runs a system command given as a List of Strings. Based on package com.devdaily.system;  
 *
 */
public class WMV_Command
{
  private List<String> command;
  private String adminPassword;
  private WMV_Thread inputStreamHandler;
  private WMV_Thread errorStreamHandler;
  private String directory;
  
  /**
   */
  public WMV_Command(final String newDirectory, final List<String> command)
  {
    directory = newDirectory;
    if (command==null) throw new NullPointerException("The command is required.");
    this.command = command;
    this.adminPassword = null;
  }

  public int execute() throws IOException, InterruptedException
  {
    int exitValue = -99;

    try
    {
      ProcessBuilder pb = new ProcessBuilder(command);
      if(directory != null && !directory.equals(""))
    	  pb.directory(new File(directory));
      
      Process process = pb.start();

//      OutputStream stdOutput = process.getOutputStream();
      
      InputStream inputStream = process.getInputStream();
      InputStream errorStream = process.getErrorStream();

      inputStreamHandler = new WMV_Thread(inputStream);
      errorStreamHandler = new WMV_Thread(errorStream);

      inputStreamHandler.start();
      errorStreamHandler.start();

      exitValue = process.waitFor();
 
      inputStreamHandler.interrupt();
      errorStreamHandler.interrupt();
      inputStreamHandler.join();
      errorStreamHandler.join();
    }
    catch (IOException e)
    {
      throw e;
    }
    catch (InterruptedException e)
    {
      throw e;
    }

    return exitValue;
  }

  /**
   * Get standard output from the command 
   */
  public StringBuilder getStandardOutput()
  {
    return inputStreamHandler.getOutputBuffer();
  }

  /**
   * Get standard error from the command 
   */
  public StringBuilder getStandardError()
  {
    return errorStreamHandler.getOutputBuffer();
  }


}