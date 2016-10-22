package gmViewer;

import java.io.*;
import java.util.List;

/**
* @author davidgordon 
* Based on package com.devdaily.system;  
* Runs given system command as a List of Strings
* @param command The command you want to run.
*/
public class GMV_Command
{
  private List<String> command;
  private String adminPassword;
  private GMV_Thread inputStreamHandler;
  private GMV_Thread errorStreamHandler;
  private String directory;
  
  /**
   */
  public GMV_Command(final String newDirectory, final List<String> command)
  {
    directory = newDirectory;
    if (command==null) throw new NullPointerException("The command is required.");
    this.command = command;
    this.adminPassword = null;
  }

  public int executeCommand() throws IOException, InterruptedException
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

      inputStreamHandler = new GMV_Thread(inputStream);
      errorStreamHandler = new GMV_Thread(errorStream);

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
  public StringBuilder getStandardOutputFromCommand()
  {
    return inputStreamHandler.getOutputBuffer();
  }

  /**
   * Get standard error from the command 
   */
  public StringBuilder getStandardErrorFromCommand()
  {
    return errorStreamHandler.getOutputBuffer();
  }


}