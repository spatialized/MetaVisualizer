package main.java.com.entoptic.metaVisualizer.misc;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.IntList;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Duration;
import java.time.LocalDateTime;
//import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import main.java.com.entoptic.metaVisualizer.MetaVisualizer;
import main.java.com.entoptic.metaVisualizer.media.WMV_Sound;
import main.java.com.entoptic.metaVisualizer.model.WMV_Date;
import main.java.com.entoptic.metaVisualizer.model.WMV_Model;
import main.java.com.entoptic.metaVisualizer.model.WMV_Time;
import main.java.com.entoptic.metaVisualizer.model.WMV_TimeSegment;
import main.java.com.entoptic.metaVisualizer.model.WMV_Waypoint;
import main.java.com.entoptic.metaVisualizer.system.MV_Command;
import main.java.com.entoptic.metaVisualizer.world.WMV_Field;

/******************
 * Utility methods 
 * @author davidgordon
 */
public class WMV_Utilities 
{
	/**
	 * Constructor for utilities class
	 */
	public WMV_Utilities(){}

	/**
	 * Get path of specified program
	 * @param programName
	 */
	public String getProgramPath(String programName)
	{
		MV_Command commandExecutor;
		ArrayList<String> command = new ArrayList<String>();

		command = new ArrayList<String>();				/* Create small_images directory */
		command.add("which");
		command.add(programName);
		commandExecutor = new MV_Command("", command);
		
		try {
			int result = commandExecutor.execute();
			StringBuilder stderr = commandExecutor.getStandardError();
			StringBuilder stdout = commandExecutor.getStandardOutput();

			if (stderr.length() > 0 || result != 0)
				System.out.println("Utilities.getProgramPath()... getting program name "+ programName+ "... result:"+result+" stderr:"+stderr.toString());
			
			System.out.println("Utilities.getProgramPath()... stdout: "+stdout.toString());
			
			if(result == 0) 
			{
				return stdout.toString();
			}
		}
		catch(Throwable t)
		{
			System.out.println("Throwable t while creating small_images directory:"+t);
			return null;
		}
		return null;
	}
	
	/**
	 * Perform linear interpolation between two 3D points
	 * @param point1 First point
	 * @param point2 Second point 
	 * @param step Step size {Between 0.f and 1.f}
	 * @return Interpolated point
	 */
	public PVector lerp3D(PVector point1, PVector point2, float step)
	{
//		System.out.println("Utilities.lerp3D()... point1.x:"+point1.x+" point2.x:"+point2.x+" step:"+step);
//		System.out.println("                      result will be:"+( lerp(point1.x, point2.x, step) ));
		PVector result = new PVector(0,0,0);
		result.x = lerp(point1.x, point2.x, step);
		result.y = lerp(point1.y, point2.y, step);
		result.z = lerp(point1.z, point2.z, step);
		return result;
	}
	
	/**
	 * Purge all files and sub-folders in a directory
	 * @param directory Directory to purge
	 */
	public void purgeDirectory(File directory) {
	    for (File file: directory.listFiles()) {
	        if (file.isDirectory()) purgeDirectory(file);
	        file.delete();
	    }
	}
	
	/**
	 * Rename folder
	 * @param oldFolderPath Folder to rename
	 * @param newFolderPath New folder name
	 * @param ignoreDirectoryStatus Whether to ignore whether paths are directories or not
	 * @return Whether successful
	 */
	public boolean renameFolder(String oldFolderPath, String newFolderPath, boolean ignoreDirectoryStatus)
	{
		if(oldFolderPath.equals(newFolderPath))			// No need to rename
			return true;
		
		File oldFolderFile = new File(oldFolderPath);
		File newNameFile = new File(newFolderPath);
		
		boolean success = false;
		
		if(!newNameFile.exists())
		{
			if(ignoreDirectoryStatus)
			{
				success = oldFolderFile.renameTo(newNameFile);
			}
			else
			{
				if(oldFolderFile.isDirectory())
				{
					success = oldFolderFile.renameTo(newNameFile);
				}
				else
				{
					System.out.println("Utilities.renameFolder()... ERROR: File "+oldFolderFile.getName()+" is not a directory!");
					success = false;
				}
			}
		}
		
		if(!success)
		{
			System.out.println("Failed at renaming to :"+newNameFile+"... will try appending numbers...");
			boolean found = false;
			int count = 2;
			while(!found)				// Append count to file name until non-duplicate name found
			{
				String path = newFolderPath + "_" + String.valueOf(count);
				newNameFile = new File(path);
				if(!newNameFile.exists()) found = true;
				count++;
			}
			success = oldFolderFile.renameTo(newNameFile);
		}
		return success;
	}
	

	/**
	 * Perform linear interpolation between two values
	 * @param val1 First value
	 * @param val2 Second value
	 * @param step Interpolation step size {Between 0.f and 1.f}
	 * @return Interpolated value
	 */
	public float lerp(float val1, float val2, float step)
	{
	    return val1 + step * (val2 - val1);
	}
	
	/**
	 * Create a directory at destination
	 * @param folderName Folder name
	 * @param destination Destination for folder
	 */
	public void makeDirectory(String folderName, String destination)
	{
		MV_Command commandExecutor;
		ArrayList<String> command = new ArrayList<String>();
		//	ArrayList<String> files = new ArrayList<String>();

		command = new ArrayList<String>();				/* Create small_images directory */
		command.add("mkdir");
		command.add(folderName);
		commandExecutor = new MV_Command(destination, command);
		try {
			int result = commandExecutor.execute();
			StringBuilder stderr = commandExecutor.getStandardError();

			if (stderr.length() > 0 || result != 0)
				System.out.println("Error creating small_images directory:" + stderr + " result:"+result);
		}
		catch(Throwable t)
		{
			System.out.println("Throwable t while creating small_images directory:"+t);
		}
	}
	
	public ArrayList<PVector> findBorderPoints(ArrayList<PVector> points)
	{
		ArrayList<PVector> borderPts = new ArrayList<PVector>();
//		PVector centerOfHull = new PVector(100000,100000);
		
		// There must be at least 3 points
		if (points.size() < 3) return null;

		JarvisPoints pts = new JarvisPoints(points);
		JarvisMarch jm = new JarvisMarch(pts);
//		double start = System.currentTimeMillis();
		int n = jm.calculateHull();
//		double end = System.currentTimeMillis();
//		System.out.printf("%d points found %d vertices %f seconds\n", points.size(), n, (end-start)/1000.);

		int length = jm.getHullPoints().pts.length;
		borderPts = new ArrayList<PVector>();
		
		for(int i=0; i<length; i++)
			borderPts.add( jm.getHullPoints().pts[i] );
		
		// Print Result
		//	  for (int i = 0; i < hull.size(); i++)
		//	    System.out.println( "(" + hull.get(i).x + ", "
		//	      + hull.get(i).y + ")\n");

//		if(debugSettings.field)
//			System.out.println("Found media points border of size:"+border.size());

//		borderPts.sort(new PointComp(centerOfHull));
		
		IntList removeList = new IntList();
		ArrayList<PVector> removePoints = new ArrayList<PVector>();
		
		int count = 0;
		for(PVector pv : borderPts)
		{
			int ct = 0;
			for(PVector comp : borderPts)
			{
				if(ct != count)		// Don't compare same indices
				{
					if(pv.equals(comp))
					{
						if(!removePoints.contains(pv))
						{
							removeList.append(count);
							removePoints.add(pv);
						}
						break;
					}
				}
				ct++;
			}
			count++;
		}
		
//		if(debugSettings.field) System.out.println("Will remove "+removeList.size()+" points from field #"+getID()+" border of size "+borderPts.size()+"...");

		ArrayList<PVector> newPoints = new ArrayList<PVector>();
		count = 0;
		for(PVector pv : borderPts)
		{
			if(!removeList.hasValue(count))
				newPoints.add(pv);
			count++;
		}
		borderPts = newPoints;
		
//		for(int i=removeList.size()-1; i>=0; i--)
//		{
//			borderPts.remove(i);
//			i--;
//		}

//		System.out.println("Points remaining: "+borderPts.size()+"...");
//		System.out.println("Will sort remaining "+borderPts.size()+" points in border...");
//		borderPts.sort(c);
		
		return borderPts;
	}
	
	/**
	 * Round value to nearest <n> decimal places
	 * @param val Value to round
	 * @param n Decimal places to round to
	 * @return Rounded value
	 */
	public float round(float val, int n)
	{
		val *= Math.pow(10.f, n);
		val = Math.round(val);
		val /= Math.pow(10.f, n);
		return val;
	}
	
	/**
	 * Map a value from given range to new range
	 * @param val Value to map
	 * @param min Initial range minimum
	 * @param max Initial range maximum
	 * @param min2 New range minimum
	 * @param max2 New range maximum
	 * @return
	 */
	public float mapValue(float val, float min, float max, float min2, float max2)
	{
	  float res;
	  res = (((max2-min2)*(val-min))/(max-min)) + min2;
	  return res;
	}
	
	/**
	 * Constrain float value between <min> and <max> by wrapping values around
	 * @param value Value to constrain by wrapping
	 * @param min Minimum value
	 * @param max Maximum value
	 */
	public float constrainWrap(float value, float min, float max)
	{
		if(value < min)
			value += max;

		else if(value > max)
			value -= max;

		return value;
	}
	
	public boolean hasDuplicateInteger(List<Integer> intList)
	{
		boolean duplicate = false;
		for(int i:intList)
		{
			for(int m:intList)
			{
				boolean found = false;
				if(i == m)
				{
					if(found) duplicate = true;
					else found = true;
				}
			}
		}

		return duplicate;
	}
	
	/**
	 * Get value in seconds of time PVector
	 * @param time PVector of the format (hour, minute, second)
	 * @return Number of seconds
	 */
	public float getTimePVectorSeconds(PVector time)
	{
		float result = time.z;
		result += time.y * 60.f;
		result += time.x * 60.f * 60.f;
		return result;
	}

	public int roundSecondsToHour(float value)
	{
		return Math.round(value / 3600.f) * 3600;
	}

	/**
	 * Round given value in seconds to nearest value given by interval parameter
	 * @param value Value to round
	 * @param interval Number of seconds to round to
	 * @return Rounded value
	 */
	public int roundSecondsToInterval(float value, float interval)
	{
		return Math.round(Math.round(value / interval) * interval);
	}
	
	/**
	 * Get date as formatted string
	 * @param date Given date
	 * @return String in format: "April 12, 1982"
	 */
	public String getDateAsString(WMV_Date date)
	{
		int year = date.getYear();
		int month = date.getMonth();
		int day = date.getDay();
		String monthStr = "";
		
		switch(month)
		{
			case 1:
				monthStr = "January";
				break;
			case 2:
				monthStr = "February";
				break;
			case 3:
				monthStr = "March";
				break;
			case 4:
				monthStr = "April";
				break;
			case 5:
				monthStr = "May";
				break;
			case 6:
				monthStr = "June";
				break;
			case 7:
				monthStr = "July";
				break;
			case 8:
				monthStr = "August";
				break;
			case 9:
				monthStr = "September";
				break;
			case 10:
				monthStr = "October";
				break;
			case 11:
				monthStr = "November";
				break;
			case 12:
				monthStr = "December";
				break;
		}
		
		String result = monthStr+" "+String.valueOf(day)+", "+String.valueOf(year);
		return result;
	}

	public String secondsToTimeAsString( float seconds, boolean showSeconds, boolean military )
	{
		int hour = Math.round(seconds) / 3600;
		int minute = (Math.round(seconds) % 3600) / 60;
		int second = (Math.round(seconds) % 3600) % 60;
		return getTimeAsString(hour, minute, second, showSeconds, military);
	}
	
	public String getTimeAsString( int hour, int minute, int second, boolean showSeconds, boolean military )
	{
		boolean pm = false;
		
		if(!military) 
		{
			if(hour == 0 && minute == 0) 
			{
				hour = 12;
				pm = false;
			}
			else
			{
				if(hour == 0) hour = 12;

				if(hour > 12)
				{
					hour -= 12;
					if(hour < 12) pm = true;
				}
				else if(hour == 12) pm = true;
			}
		}

		String strHour = String.valueOf(hour);
		
		String strMinute = String.valueOf(minute);
		if(minute < 10) strMinute = "0"+strMinute;
		
		if(showSeconds)
		{
			String strSecond = String.valueOf(second);
			if(second < 10) strSecond = "0"+strSecond;
			
			if(military)
				return strHour + ":" + strMinute + ":" + strSecond;
			else
				return strHour + ":" + strMinute + ":" + strSecond + (pm ? " pm" : " am");
		}
		else				
		{
			if(military)
				return strHour + ":" + strMinute;
			else
				return  strHour + ":" + strMinute + (pm ? " pm" : " am");
		}
	}
	
	public String getMonthAsString(int month)
	{
		String monthStr = "";
		switch(month)
		{
			case 1:
				monthStr = "January";
				break;
			case 2:
				monthStr = "February";
				break;
			case 3:
				monthStr = "March";
				break;
			case 4:
				monthStr = "April";
				break;
			case 5:
				monthStr = "May";
				break;
			case 6:
				monthStr = "June";
				break;
			case 7:
				monthStr = "July";
				break;
			case 8:
				monthStr = "August";
				break;
			case 9:
				monthStr = "September";
				break;
			case 10:
				monthStr = "October";
				break;
			case 11:
				monthStr = "November";
				break;
			case 12:
				monthStr = "December";
				break;
		}
		return monthStr;
	}
	
	/**
	 * Get current date in days since Jan 1, 1980
	 * @return Days since Jan 1, 1980
	 */
	public int getCurrentDateInDaysSince1980(String timeZoneID)
	{
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timeZoneID));
		int year = now.getYear();
		int month = now.getMonthValue();
		int day = now.getDayOfMonth();
		return getDaysSince1980(timeZoneID, day, month, year);
	}
	
	/**
	 * Get specified date as number of days from Jan 1, 1980
	 * @param day Day
	 * @param month Month
	 * @param year Year
	 * @return Number of days 
	 */
	public int getDaysSince1980(String timeZoneID, int day, int month, int year)
	{
		ZonedDateTime date1980 = ZonedDateTime.parse("1980-01-01T00:00:00+00:00[America/Los_Angeles]");
		ZonedDateTime date = ZonedDateTime.of(year, month, day, 0, 0, 0, 0, ZoneId.of(timeZoneID));
		Duration duration = Duration.between(date1980, date);

		return (int)duration.toDays();
	}
	
	/**
	 * Get distance in radians between two angles
	 * @param theta1 First angle
	 * @param theta2 Second angle
	 * @return Angular distance in radians
	 */
	public float getAngularDistance(float theta1, float theta2)
	{
		if (theta1 < 0)
			theta1 += Math.PI * 2.f;
		if (theta2 < 0)
			theta2 += Math.PI * 2.f;
		if (theta1 > Math.PI * 2.f)
			theta1 -= Math.PI * 2.f;
		if (theta2 > Math.PI * 2.f)
			theta2 -= Math.PI * 2.f;

		float dist1 = Math.abs(theta1-theta2);
		float dist2;

		if (theta1 > theta2)
			dist2   = Math.abs(theta1 - (float)Math.PI*2.f-theta2);
		else
			dist2   = Math.abs(theta2 - (float)Math.PI*2.f-theta1);

		if (dist1 > dist2)
			return dist2;
		else
			return dist1;
	}

	/**
	 * Get sound locations from GPS track
	 */
	public void setSoundGPSLocationsFromGPSTrack(ArrayList<WMV_Sound> soundList, ArrayList<WMV_Waypoint> gpsTrack, float soundGPSTimeThreshold)
	{
		for(WMV_Sound s : soundList)
		{
			s.calculateLocationFromGPSTrack(gpsTrack, soundGPSTimeThreshold);
		}
	}

	/**
	 * Get GPS track as formatted string
	 * @param loc {longitude, latitude}
	 */
	public String formatGPSLocation(PVector loc, boolean labels)
	{
		if(labels)
			return "Lat:"+round(loc.y, 4)+", Lon:"+round(loc.x, 4);
		else
			return ""+round(loc.y, 4)+", "+round(loc.x, 4);
	}

	/**
	 * Shrink images to optimized size (640 pixels max width) for 3D environment
	 * @param largeImages Images to shrink
	 * @param destination Destination folder
	 * @return Whether successful
	 */
	public boolean shrinkImageFolder(String largeImages, String destination)
	{
		System.out.println("shrinkImageFolder()... Shrinking images:"+largeImages+" dest: "+destination+"...");

		ArrayList<String> files = new ArrayList<String>();

		/* Get files in directory */
		files = getFilesInDirectory(largeImages);
		
		copyFiles(largeImages, destination);
		
		files = getFilesInDirectory(destination);
		
		/* Shrink files in new directory */
		shrinkImageFileList(files, destination);
		
		return true;
	}
	
	/**
	 * Shrink list of image files
	 * @param files Image file list
	 * @param directory Directory containing image files
	 */
	public void shrinkImageFileList(ArrayList<String> files, String directory)
	{
		for (String fileName : files)					// -- Do in one command??
		{
			boolean isJpeg = false;
			if(fileName != null && !fileName.equals(""))
			{
				String[] parts = fileName.split("\\.");

				if(parts.length > 0)
				{
					if(parts[parts.length-1].equals("jpg") || parts[parts.length-1].equals("JPG"))
						isJpeg = true;
				}
				
				if(isJpeg)
					shrinkImageInPlace(fileName, directory);
			}
		}
	}
	
	/**
	 * Get files in directory as list of Strings
	 * @param sourceFolder Directory to list files from
	 * @return File list
	 */
	private ArrayList<String> getFilesInDirectory(String sourceFolder)
	{
		ArrayList<String> files = new ArrayList<String>();
		MV_Command commandExecutor;
		ArrayList<String> command = new ArrayList<String>();
		command.add("ls");
		commandExecutor = new MV_Command(sourceFolder, command);
		try {
			int result = commandExecutor.execute();

			// Get the output from the command
			StringBuilder stdout = commandExecutor.getStandardOutput();
//			StringBuilder stderr = commandExecutor.getStandardError();

			String out = stdout.toString();
			String[] parts = out.split("\n");
			for (int i=0; i<parts.length; i++)
			{
				files.add(parts[i]);
				//println("parts["+i+"]:"+parts[i]);
			}
		}
		catch(Throwable t)
		{
			System.out.println("createNewLibrary()... Throwable t while getting folderString file list:"+t);
//			return false;
		}
		
		return files;
	}

	/**
	 * Copy file to destination
	 * @param filePath File to copy 
	 * @param destination Destination path
	 * @return Whether successful
	 */
	private boolean copyFile(String filePath, String destination)
	{
		MV_Command commandExecutor;
		ArrayList<String> command = new ArrayList<String>();
		command.add("cp");
		command.add("-a");		// Improved recursive option that preserves all file attributes, and also preserve symlinks.
		command.add(filePath);
		command.add(destination);
//		System.out.println("Utilities.copyFile()... Copying command:"+command.toString());

		commandExecutor = new MV_Command("", command);
		try {
			int result = commandExecutor.execute();

//			System.out.println("Utilities.copyFile()... Copying result ..."+result);
			return true;
		}
		catch(Throwable t)
		{
//			System.out.println("Utilities.copyFile()... Throwable t while copying video files:"+t);
			return false;
		}	
	}
	
	/**
	 * Copy files from source folder to destination
	 * @param sourceFolder Source folder
	 * @param destination Destination path
	 * @return Whether successful
	 */
	public boolean copyFiles(String sourceFolder, String destination)
	{
		MV_Command commandExecutor;
		ArrayList<String> command = new ArrayList<String>();

		/* Copy files to new directory */
		command = new ArrayList<String>();
		command.add("cp");
		command.add("-a");
		command.add(sourceFolder + ".");
		command.add(destination);
		
		commandExecutor = new MV_Command("", command);
		try {
			int result = commandExecutor.execute();

//			StringBuilder stdout = commandExecutor.getStandardOutputFromCommand();
//			StringBuilder stderr = commandExecutor.getStandardErrorFromCommand();

//			System.out.println("... copying result ..."+result);
			
			return true;
		}
		catch(Throwable t)
		{
			System.out.println("Utilities.copyFiles()... Throwable t while copying files:"+t);
			return false;
		}
	}

	/**
	 * Shrink image in place at specified directory
	 * @param fileName File path of image to shrink
	 * @param directory Image file directory 
	 * @return Whether successful
	 */
	public boolean shrinkImageInPlace(String fileName, String directory)
	{
		MV_Command commandExecutor;
		ArrayList<String> command = new ArrayList<String>();

		//Command: sips -Z 640 *.jpg
		command = new ArrayList<String>();
		command.add("sips");
		command.add("-Z");
		command.add("640");
		command.add(fileName);

//		System.out.println("Utilities.shrinkImageInPlace()... directory:"+directory +" command:"+command);
		commandExecutor = new MV_Command(directory, command);

		try {
			int result = commandExecutor.execute();

//			StringBuilder stdout = commandExecutor.getStandardOutput();			// get the output from the command
//			StringBuilder stderr = commandExecutor.getStandardError();
			
			return true;
		}
		catch(Throwable t)
		{
			System.out.println("Throwable t:"+t);
			return false;
		}
	}
	
	public String getFileNameFromPath(String filePath)
	{
		String[] parts = filePath.split("/");
		String filename = "";
		if(parts.length>0)
			filename= parts[parts.length-1]; 
		return filename;
	}
	/**
	 * Shrink image in place at specified directory
	 * @param fileName File path of image to shrink
	 * @param inputDirectory Image file directory 
	 * @param inputDirectory Image output directory 
	 * @return Whether successful
	 */
	public boolean shrinkImage(String filePath, String outputDirectory)
	{
		String fileName = getFileNameFromPath(filePath);
		
		MV_Command commandExecutor;
		ArrayList<String> command = new ArrayList<String>();

		command = new ArrayList<String>();		// Ex. Command: sips -Z 640 *.jpg
		command.add("sips");
		command.add("-Z");
		command.add("640");
		command.add(filePath);
		command.add("--out");
		command.add(outputDirectory + "/" + fileName);

//		System.out.println("Utilities.shrinkImage()... no directory... command:"+command);
		commandExecutor = new MV_Command("", command);

		try {
			int result = commandExecutor.execute();

//			StringBuilder stdout = commandExecutor.getStandardOutput();			// get the output from the command
//			StringBuilder stderr = commandExecutor.getStandardError();
			
			return true;
		}
		catch(Throwable t)
		{
			System.out.println("Throwable t:"+t);
			return false;
		}
	}

//	/**
//	 * Convert list of videos to 480p (using QuickTime Player) and export to output folder
//	 * @param inputPath Input folder path
//	 * @param outputPath Output folder path
//	 * @return Whether successful
//	 */
//	private Process shrinkVideos(MultimediaLocator ml, String inputPath, String outputPath)
//	{
//		Process process;
//		String scriptPath = ml.getScriptResource("Convert_File_to_480p.txt");
//		ml.delay(200);
//
//		if(ml.debug.video)
//		{
//			System.out.println("Utilities.convertVideos()... scriptPath:"+scriptPath);
//			System.out.println(" ... inputPath:"+inputPath);
//			System.out.println(" ... outputPath:"+outputPath);
//		}
//
//		Runtime runtime = Runtime.getRuntime();
//
//		String[] args = { "osascript", scriptPath, inputPath, outputPath };
//
//		try
//		{
//			process = runtime.exec(args);
//
//			InputStream input = process.getInputStream();
//			for (int i = 0; i < input.available(); i++) {
//				System.out.println("" + input.read());
//			}
//
//			InputStream error = process.getErrorStream();
//			for (int i = 0; i < error.available(); i++) {
//				System.out.println("" + error.read());
//			}
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//			return null;
//		}
//		
//		return process;
//	}

	/**
	 * Convert list of videos to 480p (using QuickTime Player) and export to output folder
	 * @param inputPath Input folder path
	 * @param outputPath Output folder path
	 * @return Whether successful
	 */
	public void shrinkVideos(MetaVisualizer ml, String folderPath, String outputPath)
	{
		File folderFile = new File(folderPath);
		File[] files = folderFile.listFiles();
		
		for(int i=0; i<files.length; i++)
		{
			File videoFile = files[i];
			String videoPath = videoFile.getAbsolutePath();
			
			Process conversionProcess;
			
			if(i == files.length - 1)
				conversionProcess = shrinkVideo(ml, videoPath, outputPath, true);				// -- Pass argument for delay time too?
			else
				conversionProcess = shrinkVideo(ml, videoPath, outputPath, false);			// -- Pass argument for delay time too?

			try{									// Copy original videos to small_videos directory and resize	-- Move to ML class
				conversionProcess.waitFor();
			}
			catch(Throwable t)
			{
				ml.systemMessage("Metadata.shrinkVideos()... ERROR in process.waitFor()... t:"+t);
				t.printStackTrace();
			}
		}
	}

	/**
	 * Convert list of videos to 480p (using QuickTime Player) and export to output folder
	 * @param inputPath Input folder path
	 * @param outputPath Output folder path
	 * @param exitAfter Whether to quit QuickTime Player after finished
	 * @return Whether successful
	 */
	public Process shrinkVideo(MetaVisualizer ml, String filePath, String outputPath, boolean exitAfter) // -- Pass argument for delay time?
	{
		String fileName = getFileNameFromPath(filePath);
		
		Process process;
		String scriptPath = ml.getScriptResource("Convert_File_to_480p.txt");
		ml.delay(200);

		if(ml.debug.video)
		{
			System.out.println("Utilities.convertVideo()... scriptPath:"+scriptPath);
			System.out.println(" ... filePath:"+filePath);
			System.out.println(" ... fileName:"+fileName);
			System.out.println(" ... outputPath:"+outputPath);
		}

		Runtime runtime = Runtime.getRuntime();

//		String[] args = { "osascript", scriptPath, filePath, fileName, outputPath };
		String[] args;

		if(exitAfter)
		{
			String[] arguments = { "osascript", scriptPath, filePath, fileName, outputPath, "true" };
			args = arguments;
		}
		else
		{
			String[] arguments = { "osascript", scriptPath, filePath, fileName, outputPath, "false" };
			args = arguments;
		}

		try
		{
			process = runtime.exec(args);

			InputStream input = process.getInputStream();
			for (int i = 0; i < input.available(); i++) {
				System.out.println("" + input.read());
			}

			InputStream error = process.getErrorStream();
			for (int i = 0; i < error.available(); i++) {
				System.out.println("" + error.read());
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		
		return process;
	}

//	/**
//	 * Convert videos in input folder to 480p (using QuickTime Player) and export to output folder
//	 * @param inputPath Input folder path
//	 * @param outputPath Output folder path
//	 * @return Whether successful
//	 */
//	public Process convertVideos(MultimediaLocator ml, String inputPath, String outputPath)
//	{
//		Process process;
//		String scriptPath = ml.getScriptResource("Convert_to_480p.txt");
//		ml.delay(200);
//
//		if(ml.debugSettings.video )
//		{
//			System.out.println("Utilities.convertVideos()... scriptPath:"+scriptPath);
//			System.out.println(" ... inputPath:"+inputPath);
//			System.out.println(" ... outputPath:"+outputPath);
//		}
//
//		Runtime runtime = Runtime.getRuntime();
//
//		String[] args = { "osascript", scriptPath, inputPath, outputPath };
//
//		try
//		{
//			process = runtime.exec(args);
//
//			InputStream input = process.getInputStream();
//			for (int i = 0; i < input.available(); i++) {
//				System.out.println("" + input.read());
//			}
//
//			InputStream error = process.getErrorStream();
//			for (int i = 0; i < error.available(); i++) {
//				System.out.println("" + error.read());
//			}
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//			return null;
//		}
//		
//		return process;
//	}

	public PImage bufferedImageToPImage(BufferedImage bimg)
	{         
		try {
			PImage img=new PImage(bimg.getWidth(), bimg.getHeight(), PConstants.ARGB);
			bimg.getRGB(0, 0, img.width, img.height, img.pixels, 0, img.width);
			img.updatePixels();
			return img;
		}
		catch(Exception e) {
			System.err.println("Can't create image from buffer");
			e.printStackTrace();
		}
		return null;
	}

	
	/**
	 * @param x Float to check
	 * @return Whether the variable is NaN
	 */
	public boolean isNaN(float x) {
		return x != x;
	}

	public boolean isNaN(double x) {
		return x != x;
	}

	/**
	 * @param s String to check
	 * @param radix Maximum number of digits
	 * @return If the string is an integer
	 */
	public boolean isInteger(String s, int radix) {
		Scanner sc = new Scanner(s.trim());
		if(!sc.hasNextInt(radix))
		{
			sc.close();
			return false;
		}
		sc.nextInt(radix);
		boolean result = !sc.hasNext();
		sc.close();
		return result;
	}

	/** 
	 * @param month Month
	 * @param year Year
	 * @return Number of days in given month of given year
	 */
	public int getDaysInMonth(int month, int year) {
		boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
		int days = 0;

		switch (month) {
		case 1:
			days = 31;
			break;
		case 2:
			if (isLeapYear)
				days = 29;
			else
				days = 28;
			break;
		case 3:
			days = 31;
			break;
		case 4:
			days = 30;
			break;
		case 5:
			days = 31;
			break;
		case 6:
			days = 30;
			break;
		case 7:
			days = 31;
			break;
		case 8:
			days = 31;
			break;
		case 9:
			days = 30;
			break;
		case 10:
			days = 31;
			break;
		case 11:
			days = 30;
			break;
		case 12:
			days = 31;
			break;
		default:
			days = 0;
		}
		return days;
	}

	public void checkTimeSegment(WMV_TimeSegment ts)
	{
		float upper = ts.getUpper().getAbsoluteTime();
		float lower = ts.getLower().getAbsoluteTime();
		
//		System.out.println("checkTimeSegment()...");
		for(WMV_Time t : ts.timeline)
		{
//			System.out.println(" checkTimeSegment()... t.getTime():"+t.getTime());
			if(t.getAbsoluteTime() < lower)
			{
				System.out.println("  t.getTime() < lower... t.getTime():"+t.getAbsoluteTime()+" lower:"+lower);
			}
			if(t.getAbsoluteTime() > upper)
			{
				System.out.println("  t.getTime() < lower... t.getTime():"+t.getAbsoluteTime()+" upper:"+upper);
			}
		}
	}

	public float getTimelineLength(ArrayList<WMV_TimeSegment> timeline)
	{
		float start = timeline.get(0).getLower().getAbsoluteTime();
		float end = timeline.get(timeline.size()-1).getUpper().getAbsoluteTime();
		float length = (end - start) * getTimePVectorSeconds(new PVector(24,0,0));
		return length;
	}
	
	/**
	 * @param radians (Yaw) angle in radians
	 * @return Corresponding cmpass orientation
	 */
	String angleToCompass(float radians)
	{
		float angle = (float)Math.toDegrees(radians);
		float sweep = 360.f/8.f;

		angle += sweep*0.5f;

		if(angle < 0.f)								// Adjust angle to between 0 and 360
			angle = 360.f + angle % 360.f;
		if(angle > 360.f)
			angle %= 360.f;

		for(int i=0; i<8; i++)
		{
			if(angle > 0 && angle < sweep)
			{
				switch(i)
				{
				case 0: return "N";
				case 7: return "NE";
				case 6: return "E";
				case 5: return "SE";
				case 4: return "S";
				case 3: return "SW";
				case 2: return "W";
				case 1: return "NW";
				}
			}

			angle -= sweep;
		}

		return null;
	}

	public String getCurrentTimeZoneID(float latitude, float longitude)
	{
		JSONObject json;
		String start = "https://maps.googleapis.com/maps/api/timezone/json?location=";
		String end = "&timestamp=1331161200&key=AIzaSyBXrzfHmo4t8hhrTX1lVgXwfbuThSokjNY";
		String url = start+String.valueOf(latitude)+","+String.valueOf(longitude)+end;
//		String url = "https://maps.googleapis.com/maps/api/timezone/json?location=37.77492950,-122.41941550&timestamp=1331161200&key=AIzaSyBXrzfHmo4t8hhrTX1lVgXwfbuThSokjNY";
		try
		{
			json = readJsonFromUrl(url);
		}
		catch(Throwable t)
		{
			System.out.println("Error reading JSON from Google Time Zone API: "+t);
			return null;
		}

		if (json!=null)
		{
			String timeZoneID= ((String)json.get("timeZoneId"));
			return timeZoneID;
		} 
		else
			return null;
	}
	
	/**
	 * Calculate virtual capture location based on GPS location in format {longitude, latitude} and GPS altitude
	 * @param gpsLocation GPS location in format {longitude, altitude, latitude}
	 * @param altitude Altitude
	 * @param longitudeRef Longitude reference
	 * @param latitudeRef Latitude reference
	 * @param model Field model
	 * @return Capture location associated with GPS location
	 */
	public PVector getCaptureLocationFromGPSAndAltitude( PVector gpsLocation, float altitude, String longitudeRef, String latitudeRef, WMV_Model model )                                  
	{
		PVector gpsWithAltitude = new PVector(gpsLocation.x, altitude, gpsLocation.y);	// {longitude, altitude, latitude}
		return getCaptureLocationFromGPSLocation(gpsWithAltitude, longitudeRef, latitudeRef, model);
	}
	
	/**
	 * Calculate virtual capture location based on GPS location in format {longitude, altitude, latitude}
	 * @param gpsLocation GPS location in format {longitude, altitude, latitude}
	 * @param longitudeRef Longitude reference
	 * @param latitudeRef Latitude reference
	 * @param model Field model
	 * @return Capture location associated with GPS location
	 */
	public PVector getCaptureLocationFromGPSLocation( PVector gpsLocation, String longitudeRef, String latitudeRef, WMV_Model model )                                  
	{
		PVector newCaptureLocation = new PVector(0,0,0);
		
		float highLongitude = model.getState().lowLongitude;
		float lowLongitude = model.getState().highLongitude;
		float highLatitude = model.getState().highLatitude;
		float lowLatitude = model.getState().lowLatitude;
		float lowAltitude = model.getState().lowAltitude;
		float highAltitude = model.getState().highAltitude;

		 if(lowAltitude == 1000000.f && highAltitude != -1000000.f) 			// Adjust for fields with no altitude variation
			 lowAltitude = highAltitude;
		 else if(highAltitude == -1000000.f && lowAltitude != 1000000.f) 
			 highAltitude = lowAltitude;
		 
		 if(lowAltitude == 1000000.f || highAltitude == -1000000.f) 			// Adjust for fields with no altitude variation
			 System.out.println("Utilities.getCaptureLocationFromGPSAndAltitude()... ERROR: highAltitude:"+model.getState().highAltitude+" lowAltitude:"+model.getState().lowAltitude);
		 
//		if(model.getState().highLongitude != -1000000 && model.getState().lowLongitude != 1000000 && model.getState().highLatitude != -1000000 && 
//				model.getState().lowLatitude != 1000000 && model.getState().highAltitude != -1000000 && model.getState().lowAltitude != 1000000)
		if( highLongitude != -1000000.f && lowLongitude != 1000000.f && highLatitude != -1000000.f && 
			lowLatitude != 1000000.f && highAltitude != -1000000.f && lowAltitude != 1000000.f )
		{
			if(model.getState().highLongitude != model.getState().lowLongitude && model.getState().highLatitude != model.getState().lowLatitude)
			{
				newCaptureLocation.x = mapValue( gpsLocation.x, model.getState().lowLongitude, 	// GPS longitude decreases from left to right
						model.getState().highLongitude, -0.5f * model.getState().fieldWidth, 0.5f 
						* model.getState().fieldWidth); 					

				newCaptureLocation.y = -mapValue( gpsLocation.y, model.getState().lowAltitude,  // Convert altitude feet to meters, negative to match P3D coordinate space
						model.getState().highAltitude, 0.f, model.getState().fieldHeight); 	

				newCaptureLocation.z = mapValue( gpsLocation.z, model.getState().lowLatitude,   // GPS latitude increases from bottom to top, reversed to match P3D coordinate space
						model.getState().highLatitude, 0.5f * model.getState().fieldLength, 
						-0.5f * model.getState().fieldLength); 
				
				if(model.worldSettings.altitudeScaling)	
					newCaptureLocation.y *= model.worldSettings.altitudeScalingFactor;
				else
					newCaptureLocation.y *= model.worldSettings.defaultAltitudeScalingFactor;
				
				if(model.debug.gps && model.debug.detailed)
				{
					System.out.println("Utilities.getCaptureLocationFromGPSLocation()... gpsLocation x:"+gpsLocation.x+" y:"+gpsLocation.y+" z:"+gpsLocation.z);
					System.out.println("		High longitude:"+model.getState().highLongitude+"  Low longitude:"+model.getState().lowLongitude);
					System.out.println("		High latitude:"+model.getState().highLatitude+"  Low latitude:"+model.getState().lowLatitude);
					System.out.println("			newX:"+newCaptureLocation.x+" newY"+newCaptureLocation.y+" newZ"+newCaptureLocation.z);
				}
			}
			else
			{
				System.out.println("Utilities.getCaptureLocationFromGPSAndAltitude()... ERROR 1: high longitude:"+model.getState().highLongitude+" lowLongitude:"+model.getState().lowLongitude);
				System.out.println("    High latitude:"+model.getState().highLatitude+" Low latitude:"+model.getState().lowLatitude);
				System.out.println("    High altitude:"+model.getState().highAltitude+" Low altitude:"+model.getState().lowAltitude);
			}
		}
		else
		{
			System.out.println("Utilities.getCaptureLocationFromGPSAndAltitude()... ERROR 2: high longitude:"+model.getState().highLongitude+" lowLongitude:"+model.getState().lowLongitude);
			System.out.println("    High latitude:"+model.getState().highLatitude+" Low latitude:"+model.getState().lowLatitude);
			System.out.println("    High altitude:"+model.getState().highAltitude+" Low altitude:"+model.getState().lowAltitude);
		}

		return newCaptureLocation;
	}
	
	/**
	 * Get GPS location for a given point in a field						-- Need to account for Longitude Ref?
	 * @param f Given field 
	 * @param loc Given point
	 * @return GPS location for given point in format {longitude, latitude}
	 */
	public PVector getGPSLocationFromCaptureLocation(WMV_Field f, PVector loc)			
	{
		if(loc != null)
		{
			WMV_Model m = f.getModel();
			
//			newX = PApplet.map(mState.gpsLocation.x, model.getState().lowLongitude, model.getState().highLongitude, -0.5f * model.getState().fieldWidth, 0.5f*model.getState().fieldWidth); 			// GPS longitude decreases from left to right
//			newZ = PApplet.map(mState.gpsLocation.z, model.getState().lowLatitude, model.getState().highLatitude, 0.5f*model.getState().fieldLength, -0.5f * model.getState().fieldLength); 			// GPS latitude increases from bottom to top, reversed to match P3D coordinate space

			float gpsX = mapValue( loc.x, -0.5f * m.getState().fieldWidth, 0.5f*m.getState().fieldWidth, m.getState().lowLongitude, m.getState().highLongitude ); 			// GPS longitude decreases from left to right
			float gpsY = mapValue( loc.z, -0.5f * m.getState().fieldLength, 0.5f*m.getState().fieldLength, m.getState().highLatitude, m.getState().lowLatitude ); 			// GPS latitude increases from bottom to top

			PVector gpsLoc = new PVector(gpsX, gpsY);
			return gpsLoc;
		}
		else 
		{
			return null;
		}
	}
	
	/**
	 * Get longitude reference from decimal longitude value
	 * @param decimal Decimal longitude input
	 * @return Longitude ref
	 */
	public String getLongitudeRefFromDecimal( float decimal )
	{
		String gpsLongitudeRef = "E";
		if( (int)Math.signum(decimal) == -1 )
			gpsLongitudeRef = "W";
		return gpsLongitudeRef;
	}
	
	/**
	 * Get latitude reference from decimal latitude value
	 * @param decimal Decimal latitude input
	 * @return Latitude ref
	 */
	public String getLatitudeRefFromDecimal( float decimal )
	{
		String gpsLatitudeRef = "N";
		if( (int)Math.signum(decimal) == -1 )
			gpsLatitudeRef = "S";
		return gpsLatitudeRef;
	}

	/**
	 * Parse date time string in UTC format (Ex. 2016-05-01T23:55:33Z)
	 * @param dateTimeStr Given date/time string
	 * @param zoneIDStr Time zone ID string
	 * @return ZonedDateTime object from date/time string
	 */
	public ZonedDateTime parseUTCDateTimeString(String dateTimeStr, String zoneIDStr)
	{
		String[] parts = dateTimeStr.split("T");
		String dateStr = parts[0];			
		String timeStr = parts[1];

		parts = dateStr.split("-");
		String yearStr, monthStr, dayStr;
		yearStr = parts[0];
		monthStr = parts[1];
		dayStr = parts[2];
		int year = Integer.parseInt(yearStr);
		int month = Integer.parseInt(monthStr);
		int day = Integer.parseInt(dayStr);

		parts = timeStr.split("Z");					/* Parse Time */
		timeStr = parts[0];

		parts = timeStr.split(":");
		String hourStr, minuteStr, secondStr;

		hourStr = parts[0];
		minuteStr = parts[1];
		secondStr = parts[2];
		int hour = Integer.parseInt(hourStr);
		int minute = Integer.parseInt(minuteStr);
		int second = Integer.parseInt(secondStr);

        LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, minute, second);
        ZonedDateTime utc = ldt.atZone(ZoneId.of("UTC"));
        ZonedDateTime zoned = utc.withZoneSameInstant(ZoneId.of(zoneIDStr));

		return zoned;
	}

	/** 
	 * Get approximate distance between two GPS points in meters 
	 * @param startLatitude Latitude of first point
	 * @param startLongitude Longitude of first point
	 * @param endLatitude Latitude of second point
	 * @param endLongitude Longitude of second point 
	 * @return Distance in meters
	 */
	public float getGPSDistanceInMeters(double startLatitude, double startLongitude, double endLatitude, double endLongitude)
	{
		double R = 6371000;												// Radius of Earth (m.)

		double φ1 = Math.toRadians(startLatitude);
		double φ2 = Math.toRadians(endLatitude);
		double Δφ = Math.toRadians(endLatitude-startLatitude);
		double Δλ = Math.toRadians(endLongitude-startLongitude);

		double a = Math.sin(Δφ/2) * Math.sin(Δφ/2) + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ/2) * Math.sin(Δλ/2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double d = R * c;

		return (float)d;
	}

	/**
	 * Convert ZonedDateTime object to EXIF metadata-style string
	 * @param dateTime ZonedDateTime object
	 * @return Date/time string
	 */
	public String getDateTimeAsString(ZonedDateTime dateTime)
	{
		String result = "";				// Format: 2016:05:28 17:13:39
		String yearStr = String.valueOf(dateTime.getYear());
		String monthStr = String.valueOf(dateTime.getMonthValue());
		String dayStr = String.valueOf(dateTime.getDayOfMonth());
		String hourStr = String.valueOf(dateTime.getHour());
		String minuteStr = String.valueOf(dateTime.getMinute());
		String secondStr = String.valueOf(dateTime.getSecond());
		result = yearStr + ":" + monthStr + ":" + dayStr  + " " + hourStr + ":" + minuteStr + ":" + secondStr;
//		System.out.println("getTimeStringFromDateTime()... result:"+result);
		return result;
	}
	
	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException 
	{

		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} 
		finally {
			is.close();
		}
	}
	
	/**
	 * Read all lines in input buffer
	 * @param rd Reader object
	 * @return Single string output
	 * @throws IOException 
	 */
	private static String readAll(Reader rd) throws IOException 
	{
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
	 
	/**
	 * Get altitude from a GPS location in format {longitude, altitude, latitude}
	 * @param loc GPS location
	 * @return Altitude
	 */
	public float getAltitude(PVector loc)
	{
		return loc.y;
	}
	
	/**
	 * Class for finding convex hull using Jarvis March Algorithm
	 * Based on JarvisMarch class by UncleBob
	 * @author davidgordon
	 */
	public class JarvisMarch 
	{
		JarvisPoints pts;
		private JarvisPoints hullPoints = null;
		private List<Float> hy;
		private List<Float> hx;
		private int startingPoint;
		private double currentAngle;
		private static final double MAX_ANGLE = 4;

		/**
		 * Constructor for JarvisMarch class
		 * @param pts JarvisPoints for which to find border 
		 */
		JarvisMarch(JarvisPoints pts) {
			this.pts = pts;
		}

		/**
		 * The Jarvis March, i.e. Gift Wrap Algorithm. The next point is the point with the next largest angle. 
		 * Imagine wrapping a string around a set of nails in a board.  Tie the string to the leftmost nail
		 * and hold the string vertical.  Now move the string clockwise until you hit the next, then the next, then
		 * the next.  When the string is vertical again, you will have found the hull.
		 */
		public int calculateHull() 
		{
			initializeHull();

			startingPoint = getStartingPoint();
			currentAngle = 0;

			addToHull(startingPoint);
			for (int p = getNextPoint(startingPoint); p != startingPoint; p = getNextPoint(p))
				addToHull(p);

			buildHullPoints();
			return hullPoints.pts.length;
		}

		public int getStartingPoint() {
			return pts.startingPoint();
		}

		private int getNextPoint(int p) {
			double minAngle = MAX_ANGLE;
			int minP = startingPoint;
			for (int i = 0; i < pts.pts.length; i++) {
				if (i != p) {
					double thisAngle = relativeAngle(i, p);
					if (thisAngle >= currentAngle && thisAngle <= minAngle) {
						minP = i;
						minAngle = thisAngle;
					}
				}
			}
			currentAngle = minAngle;
			return minP;
		}

		/**
		 * Find relative angle between two poins
		 * @param i First point
		 * @param p Second point
		 * @return Relative angle
		 */
		private double relativeAngle(int i, int p) {
			return pseudoAngle(pts.pts[i].x - pts.pts[p].x, pts.pts[i].y - pts.pts[p].y);
		}

		/**
		 * Initialize points in hx and hy
		 */
		private void initializeHull() {
			hx = new LinkedList<Float>();
			hy = new LinkedList<Float>();
		}

		/**
		 * Build points in convex hull
		 */
		private void buildHullPoints() {
			float[] ax = new float[hx.size()];
			float[] ay = new float[hy.size()];
			int n = 0;
			for (Iterator<Float> ix = hx.iterator(); ix.hasNext(); )
				ax[n++] = ix.next();

			n = 0;
			for (Iterator<Float> iy = hy.iterator(); iy.hasNext(); )
				ay[n++] = iy.next();

			ArrayList<PVector> newPts = new ArrayList<PVector>();
			for(int i=0; i<ax.length; i++)
			{
				newPts.add(new PVector(ax[i], ay[i]));
			}
			hullPoints = new JarvisPoints(newPts);
		}

		/**
		 * Add point to hull
		 * @param p Index of point to add
		 */
		private void addToHull(int p) {
			hx.add(pts.pts[p].x);
			hy.add(pts.pts[p].y);
		}

		/**
		 * The PseudoAngle increases as the angle from vertical increases. Current implementation has the maximum pseudo angle < 4.  
		 * The pseudo angle for each quadrant is 1. The algorithm finds where the angle intersects a square and measures the
		 * perimeter of the square at that point
		 */
		double pseudoAngle(double dx, double dy) {
			if (dx >= 0 && dy >= 0)
				return quadrantOnePseudoAngle(dx, dy);
			if (dx >= 0 && dy < 0)
				return 1 + quadrantOnePseudoAngle(Math.abs(dy), dx);
			if (dx < 0 && dy < 0)
				return 2 + quadrantOnePseudoAngle(Math.abs(dx), Math.abs(dy));
			if (dx < 0 && dy >= 0)
				return 3 + quadrantOnePseudoAngle(dy, Math.abs(dx));
			throw new Error("pseudoAngle()... Impossible");
		}

		double quadrantOnePseudoAngle(double dx, double dy) {
			return dx / (dy + dx);
		}

		public JarvisPoints getHullPoints() {
			return hullPoints;
		}
	}
	
	/**
	 * Array of points for Jarvis March algorithm
	 * Based on JarvisPoints class by UncleBob
	 * @author davidgordon
	 */
	class JarvisPoints 
	{
		public PVector[] pts;
		
		public JarvisPoints(ArrayList<PVector> newPts) 
		{
			pts = new PVector[newPts.size()];
			for(int i=0; i<newPts.size(); i++)
				pts[i] = newPts.get(i);
		}

		// Find starting point, i.e. point with the lowest X with ties going to the lowest Y.  This guarantees next point over is clockwise.
		int startingPoint() 
		{
			double minX = pts[0].x;
			double minY = pts[0].y;
			
			int iMin = 0;
			for (int i = 1; i < pts.length; i++) 
			{
				if (pts[i].x < minX) 
				{
					minX = pts[i].x;
					iMin = i;
				} 
				else if (minX == pts[i].x && pts[i].y < minY) 
				{
					minY = pts[i].y;
					iMin = i;
				}
			}
			return iMin;
		}
	}

	/**
	 * Check Unix path	-- Debugging
	 */
	public void checkPath()
	{
		MV_Command commandExecutor;
		ArrayList<String> command = new ArrayList<String>();

		command = new ArrayList<String>();				/* Create small_images directory */
		command.add("env");
//		command.add(programName);
		commandExecutor = new MV_Command("", command);
		
		try {
			int result = commandExecutor.execute();
			StringBuilder stderr = commandExecutor.getStandardError();
			StringBuilder stdout = commandExecutor.getStandardOutput();

			if (stderr.length() > 0 || result != 0)
				System.out.println("Utilities.checkUnixPath() ... result:"+result+" stderr:"+stderr.toString());
			
			System.out.println("Utilities.checkUnixPath()... stdout: "+stdout.toString());
		}
		catch(Throwable t)
		{
			System.out.println("Throwable t while checking Unix PATH:"+t);
		}
	}
	
	/**
	 * @param c Calendar date
	 * @return Sunset time between 0. and 1.
	 * Get sunset time for given calendar date
	 */
	public float getSunsetTime(Calendar c, Location location) 	
	{		
		//		Location location = new Location("39.9522222", "-75.1641667");				// ??????
		SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, "America/Los_Angeles");

		Calendar sr = calculator.getOfficialSunriseCalendarForDate(c);		// Get sunrise time
		Calendar ss = calculator.getOfficialSunsetCalendarForDate(c);		// Get sunset time

		/* Adjust for sunset time */
		int cHour, cMin, cSec, srHour, srMin, srSec, ssHour, ssMin, ssSec;
		int cDay, cMonth, cYear;

		cYear = c.get(Calendar.YEAR);
		cMonth = c.get(Calendar.MONTH);
		cDay = c.get(Calendar.DAY_OF_MONTH);
		cHour = c.get(Calendar.HOUR_OF_DAY); // Adjust for New York time
		cMin = c.get(Calendar.MINUTE);
		cSec = c.get(Calendar.SECOND);
		srHour = sr.get(Calendar.HOUR_OF_DAY);
		srMin = sr.get(Calendar.MINUTE); 
		srSec = sr.get(Calendar.SECOND); 
		ssHour = ss.get(Calendar.HOUR_OF_DAY);
		ssMin = ss.get(Calendar.MINUTE); 
		ssSec = ss.get(Calendar.SECOND); 

		System.out.println("---> ssHour:"+ssHour);

		if (cHour > ssHour) 
		{
			float ssDiff = (cHour * 60 + cMin + cSec/60.f) - (ssHour * 60 + ssMin + ssSec/60.f);
			System.out.println("ssDiff:"+ssDiff);

			//			if (ssDiff > p.minutesPastSunset) {
			//				if (p.p.debug.debugExif)
			//					PApplet.print("Adjusted Sunset Length from:" + p.minutesPastSunset);
			//
			//				p.minutesPastSunset = ssDiff;
			//
			//				if (p.p.debug.debugExif)
			//					System.out.println("  to:" + p.minutesPastSunset);
			//			}
		}

		if (cHour < srHour) 
		{
			float srDiff = (srHour * 60 + srMin + srSec/60.f) - (cHour * 60 + cMin + cSec/60.f);
			System.out.println("srDiff:"+srDiff);
			//			if (srDiff > p.minutesBeforeSunrise) 
			//			{
			//				PApplet.print("Adjusted Sunrise Length from:" + p.sunriseLength);
			//				p.minutesBeforeSunrise = srDiff;
			//				System.out.println("  to:" + p.sunriseLength);
			//			}
		}

		if (cHour > ssHour) 
		{
			System.out.println("Difference in Sunset Time (min.): " + ((cHour * 60 + cMin + cSec/60.f) - (ssHour * 60 + ssMin + ssSec/60.f)));
			System.out.print("Hour:" + cHour);
			System.out.println(" Min:" + cMin);
			System.out.print("Sunset Hour:" + ssHour);
			System.out.println(" Sunset Min:" + ssMin);
		}
		//
		//		if (cHour < srHour && p.p.debug.debugExif && p.p.debug.debugDetail) {
		//			System.out.println("Difference in Sunrise Time (min.): " + ((srHour * 60 + srMin + srSec/60.f) - (cHour * 60 + cMin + cSec/60.f)));
		//			PApplet.print("Hour:" + cHour);
		//			System.out.println(" Min:" + cMin);
		//			PApplet.print("Sunrise Hour:" + srHour);
		//			System.out.println(" Sunrise Min:" + srMin);
		//		}

		float sunriseTime = srHour * 60 + srMin + srSec/60.f;		
		float sunsetTime = ssHour * 60 + ssMin + ssSec/60.f;			
		float dayLength = sunsetTime - sunriseTime;

		float cTime = cHour * 60 + cMin + cSec/60.f;
		//		float time = PApplet.constrain(mapValue(cTime, sunriseTime, sunsetTime, 0.f, 1.f), 0.f, 1.f); // Time of day when photo was taken		
		float time = mapValue(cTime, sunriseTime, sunsetTime, 0.f, 1.f); // Time of day when photo was taken		

		//		if (sunsetTime > p.lastSunset) {
		//			p.lastSunset = sunsetTime;
		//		}

		int daysInMonth = 0, daysCount = 0;

		for (int i = 1; i < cMonth; i++) 				// Find number of days in prior months
		{
			daysInMonth = getDaysInMonth(i, cYear);		// Get days in month
			daysCount += daysInMonth;
		}

		//		int startYear = 2013;							
		//		int date = (year - startYear) * 365 + daysCount + day; 		
		float date = daysCount + cDay; 						// Days since Jan. 1st							//	 NOTE:	need to account for leap years!		

		//		int endDate = 5000;																					
		date = PApplet.constrain(mapValue(date, 0.f, 365, 0.f, 1.f), 0.f, 1.f);					//	 NOTE:	need to account for leap years!		
		time = mapValue(sunsetTime, 0.f, 1439.f, 0.f, 1.f); // Time of day when photo was taken		

		return time;				// Date between 0.f and 1.f, time between 0. and 1., dayLength in minutes
	}

	/**
	 * @param c Calendar date
	 * @return Sunrise time between 0. and 1.
	 * Get sunrise time for given calendar date
	 */
	public float getSunriseTime(Calendar c, Location location) 	
	{		
		//		Location location = new Location("39.9522222", "-75.1641667");				// ??????
		SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, "America/Los_Angeles");

		Calendar sr = calculator.getOfficialSunriseCalendarForDate(c);		// Get sunrise time
		Calendar ss = calculator.getOfficialSunsetCalendarForDate(c);		// Get sunset time

		/* Adjust for sunset time */
		int cHour, cMin, cSec, srHour, srMin, srSec, ssHour, ssMin, ssSec;
		int cDay, cMonth, cYear;

		cYear = c.get(Calendar.YEAR);
		cMonth = c.get(Calendar.MONTH);
		cDay = c.get(Calendar.DAY_OF_MONTH);
		cHour = c.get(Calendar.HOUR_OF_DAY); // Adjust for New York time
		cMin = c.get(Calendar.MINUTE);
		cSec = c.get(Calendar.SECOND);
		srHour = sr.get(Calendar.HOUR_OF_DAY);
		srMin = sr.get(Calendar.MINUTE); 
		srSec = sr.get(Calendar.SECOND); 
		ssHour = ss.get(Calendar.HOUR_OF_DAY);
		ssMin = ss.get(Calendar.MINUTE); 
		ssSec = ss.get(Calendar.SECOND); 

		System.out.println("---> ssHour:"+ssHour);

//		if (cHour > ssHour) 
//		{
//			float ssDiff = (cHour * 60 + cMin + cSec/60.f) - (ssHour * 60 + ssMin + ssSec/60.f);
			//			System.out.println("ssDiff:"+ssDiff);

//			if (ssDiff > p.minutesPastSunset) {
//				if (p.p.debug.debugExif)
//					PApplet.print("Adjusted Sunset Length from:" + p.minutesPastSunset);
//
//				p.minutesPastSunset = ssDiff;
//
//				if (p.p.debug.debugExif)
//					System.out.println("  to:" + p.minutesPastSunset);
//			}
//		}

		if (cHour < srHour) 
		{
			float srDiff = (srHour * 60 + srMin + srSec/60.f) - (cHour * 60 + cMin + cSec/60.f);
			System.out.println("srDiff:"+srDiff);
//			if (srDiff > p.minutesBeforeSunrise) 
//			{
//				PApplet.print("Adjusted Sunrise Length from:" + p.sunriseLength);
//				p.minutesBeforeSunrise = srDiff;
//				System.out.println("  to:" + p.sunriseLength);
//			}
		}

		if (cHour < srHour) {
			System.out.println("Difference in Sunrise Time (min.): " + ((srHour * 60 + srMin + srSec/60.f) - (cHour * 60 + cMin + cSec/60.f)));
			System.out.print("Hour:" + cHour);
			System.out.println(" Min:" + cMin);
			System.out.print("Sunrise Hour:" + srHour);
			System.out.println(" Sunrise Min:" + srMin);
		}

		float sunriseTime = srHour * 60 + srMin + srSec/60.f;		
		float sunsetTime = ssHour * 60 + ssMin + ssSec/60.f;			
//		float dayLength = sunsetTime - sunriseTime;

		float cTime = cHour * 60 + cMin + cSec/60.f;
		//		float time = PApplet.constrain(mapValue(cTime, sunriseTime, sunsetTime, 0.f, 1.f), 0.f, 1.f); // Time of day when photo was taken		
		float time = mapValue(cTime, sunriseTime, sunsetTime, 0.f, 1.f); // Time of day when photo was taken		

//		if (sunsetTime > p.lastSunset) {
//			p.lastSunset = sunsetTime;
//		}

		int daysInMonth = 0, daysCount = 0;

		for (int i = 1; i < cMonth; i++) 				// Find number of days in prior months
		{
			daysInMonth = getDaysInMonth(i, cYear);		// Get days in month
			daysCount += daysInMonth;
		}

//		int startYear = 2013;							
//		int date = (year - startYear) * 365 + daysCount + day; 		
		float date = daysCount + cDay; 						// Days since Jan. 1st							//	 NOTE:	need to account for leap years!		

//		int endDate = 5000;																					
		date = PApplet.constrain(mapValue(date, 0.f, 365, 0.f, 1.f), 0.f, 1.f);					//	 NOTE:	need to account for leap years!		

		time = mapValue(sunriseTime, 0.f, 1439.f, 0.f, 1.f); // Time of day when photo was taken		

		return time;				// Date between 0.f and 1.f, time between 0. and 1., dayLength in minutes
	}


	
//	 private PImage getDesaturated(PImage in, float amt) 
//	 {
//		 PImage out = in.get();
//		 for (int i = 0; i < out.pixels.length; i++) {
//			 int c = out.pixels[i];
//			 float h = p.p.p.hue(c);
//			 float s = p.p.p.saturation(c) * amt;
//			 float b = p.p.p.brightness(c);
//			 out.pixels[i] = p.p.p.color(h, s, b);
//		 }
//		 return out;
//	 }
//
//	 private PImage getFaintImage(PImage image, float amt) 
//	 {
//		 PImage out = image.get();
//		 for (int i = 0; i < out.pixels.length; i++) {
//			 int c = out.pixels[i];
//			 float h = p.p.p.hue(c);
//			 float s = p.p.p.saturation(c) * amt;
//			 float b = p.p.p.brightness(c) * amt;
//			 out.pixels[i] = p.p.p.color(h, s, b);
//		 }
//		 return out;
//	 }

//	public float calculateAverageDistance(float[] distances) 
//	{
//		float sum = 0;
//		float result;
//
//		for (int i=0; i<distances.length; i++) 
//		{
//			float dist = distances[i];
//			sum += dist;
//		}
//
//		if (distances.length > 0) 
//			result = sum / distances.length;
//		else 
//			result = 0.f;
//
//		return result;
//	}
	
	/**
	 * @param hour UTC hour
	 * @return Corresponding hour in Pacific Time
	 */
//	public WMV_Time utcToPacificTime(WMV_Time time)
//	{
//		int year = time.getYear();
//		int day = time.getDay();
//		int month = time.getMonth();
//		int hour = time.getHour();
//
//		ZonedDateTime utcDateTime = ZonedDateTime.of(year, month, day, hour, time.getMinute(), time.getSecond(), time.getMillisecond(), ZoneId.of("UTC"));
//		ZonedDateTime localDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("America/Los_Angeles"));
//		
//		WMV_Time result = new WMV_Time( localDateTime, time.getID(), time.getClusterID(), time.getMediaType(), "America/Los_Angeles" );
//		return result;
//	}
}
