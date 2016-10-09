package gmViewer;


import java.io.*;

/**
* Based on package com.devdaily.system;  
*/
class GMV_Thread extends Thread
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
  GMV_Thread(InputStream inputStream)
  {
    this.inputStream = inputStream;
  }
  
  public void run()
  {
    // on mac os x 10.5.x, when i run a 'sudo' command, i need to write
    // the admin password out immediately; that's why this code is
    // here.
    if (sudoIsRequested)
    {
      //doSleep(500);
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
  
  private void doSleep(long millis)
  {
    try
    {
      Thread.sleep(millis);
    }
    catch (InterruptedException e)
    {
      // ignore
    }
  }
  
  public StringBuilder getOutputBuffer()
  {
    return outputBuffer;
  }

}