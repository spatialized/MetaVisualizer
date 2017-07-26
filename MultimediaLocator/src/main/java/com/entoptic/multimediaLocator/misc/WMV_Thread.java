package main.java.com.entoptic.multimediaLocator.misc;

import java.io.*;

/**
 * Thread object used by WMV_Command
 * @author davidgordon
 * Based on package com.devdaily.system;  
 */
class WMV_Thread extends Thread
{
  InputStream inputStream;
  String adminPassword;
  OutputStream outputStream;
  PrintWriter printWriter;
  StringBuilder outputBuffer = new StringBuilder();
  private boolean sudoIsRequested = false;
  
  /**
   * Execute command without running sudo 
   * @param inputStream
   * @param streamType
   */
  WMV_Thread(InputStream inputStream)
  {
    this.inputStream = inputStream;
  }
  
  public void run()
  {
    if (sudoIsRequested)
    {
      printWriter.println(adminPassword);
      printWriter.flush();
    }

    BufferedReader bufferedReader = null;
    try
    {
      bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      String line = null;
      while ((line = bufferedReader.readLine()) != null)
      {
        outputBuffer.append(line + "\n");
      }
    }
    catch (IOException ioe)
    {
      // TODO handle this better
      ioe.printStackTrace();
    }
    catch (Throwable t)
    {
      // TODO handle this better
      t.printStackTrace();
    }
    finally
    {
      try
      {
        bufferedReader.close();
      }
      catch (IOException e)
      {
        // ignore this one
      }
    }
  }
  
  
  public StringBuilder getOutputBuffer()
  {
    return outputBuffer;
  }

//  private void doSleep(long millis)
//  {
//    try
//    {
//      Thread.sleep(millis);
//    }
//    catch (InterruptedException e)
//    {
//      // ignore
//    }
//  }
}