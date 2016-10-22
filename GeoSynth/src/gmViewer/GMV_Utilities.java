package gmViewer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/******************
 * GMV_Utilities
 * @author davidgordon
 * Utility methods 
 */

public class GMV_Utilities 
{
	GMV_World p;
	
	GMV_Utilities(GMV_World parent)
	{
		p = parent;
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
			theta1 += PApplet.PI * 2.f;
		if (theta2 < 0)
			theta2 += PApplet.PI * 2.f;
		if (theta1 > PApplet.PI * 2.f)
			theta1 -= PApplet.PI * 2.f;
		if (theta2 > PApplet.PI * 2.f)
			theta2 -= PApplet.PI * 2.f;

		float dist1 = PApplet.abs(theta1-theta2);
		float dist2;

		if (theta1 > theta2)
			dist2   = PApplet.abs(theta1 - PApplet.PI*2.f-theta2);
		else
			dist2   = PApplet.abs(theta2 - PApplet.PI*2.f-theta1);

		if (dist1 > dist2)
			return dist2;
		else
			return dist1;
	}

	/** 
	 * Get (approximate) distance between two GPS points in meters 
	 * @param startLatitude Latitude of first point
	 * @param startLongitude Longitude of first point
	 * @param endLatitude Latitude of second point
	 * @param endLongitude Longitude of second point 
	 * @return Distance in meters
	 */
	public float gpsToMeters(double startLatitude, double startLongitude, double endLatitude, double endLongitude)
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
	 * utcToPacificTime
	 * @param hour UTC hour
	 * @return Corresponding hour in Pacific Time
	 */
	public int utcToPacificTime(int hour)
	{
		hour -= 8;
		if(hour < 0)
			hour+= 24;
		return hour;
	}

	/**
	 * Shrink images to 3D view format (640 pixels max width)
	 * @param largeImages Images to shrink
	 * @param destination Destination folder
	 * @return Whether successful
	 */
	public boolean shrinkImages(String largeImages, String destination)
	{
		PApplet.println("Shrinking images:"+largeImages+" to:"+destination+"...");
		GMV_Command commandExecutor;
		ArrayList<String> command = new ArrayList<String>();
		ArrayList<String> files = new ArrayList<String>();

		/* Get files in directory */
		command = new ArrayList<String>();
		command.add("ls");
		commandExecutor = new GMV_Command(largeImages, command);
		try {
			int result = commandExecutor.executeCommand();

			// get the output from the command
			StringBuilder stdout = commandExecutor.getStandardOutputFromCommand();
//			StringBuilder stderr = commandExecutor.getStandardErrorFromCommand();

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
			PApplet.println("Throwable t while getting largeImage file list:"+t);
			return false;
		}
		
		/* Copy files to new directory */
		command = new ArrayList<String>();
		command.add("cp");
		command.add("-a");
		command.add(largeImages + ".");
		command.add(destination);
//		PApplet.println("Copying command:"+command.toString());
		
		commandExecutor = new GMV_Command("", command);
		try {
			int result = commandExecutor.executeCommand();

//			StringBuilder stdout = commandExecutor.getStandardOutputFromCommand();
//			StringBuilder stderr = commandExecutor.getStandardErrorFromCommand();

//			PApplet.println("... copying result ..."+result);
		}
		catch(Throwable t)
		{
			PApplet.println("Throwable t while copying files:"+t);
			return false;
		}
		
		files = new ArrayList<String>();
		command = new ArrayList<String>();
		command.add("ls");
		commandExecutor = new GMV_Command(destination, command);
		try {
			int result = commandExecutor.executeCommand();

			StringBuilder stdout = commandExecutor.getStandardOutputFromCommand();
			StringBuilder stderr = commandExecutor.getStandardErrorFromCommand();

			String out = stdout.toString();
			String[] parts = out.split("\n");
			for (int i=0; i<parts.length; i++)
				files.add(parts[i]);
		}
		catch(Throwable t)
		{
			PApplet.println("Throwable t while getting small_images file list for shrinking:"+t);
			return false;
		}

//		PApplet.println("files.size():"+files.size());

		/* Shrink files in new directory */
		for (String fileName : files)					// -- Do in one command??
		{
			boolean isJpeg = false;
//			PApplet.println("fileName:"+fileName);

			if(fileName != null && !fileName.equals(""))
			{
				String[] parts = fileName.split("\\.");

//				PApplet.println("parts.length:"+parts.length);
				if(parts.length > 0)
				{
//					PApplet.println("parts[0]:"+parts[0]);
//					PApplet.println("parts[length-1]:"+parts[parts.length-1]);
					if(parts[parts.length-1].equals("jpg") || parts[parts.length-1].equals("JPG"))
						isJpeg = true;
				}
				
				if(isJpeg)
				{
					//Command: sips -Z 640 *.jpg
					command = new ArrayList<String>();
					command.add("sips");
					command.add("-Z");
					command.add("640");
					command.add(fileName);
//					PApplet.println("destination:"+destination +" command:"+command);
					commandExecutor = new GMV_Command(destination, command);

					try {
						int result = commandExecutor.executeCommand();

						// get the output from the command
						StringBuilder stdout = commandExecutor.getStandardOutputFromCommand();
						StringBuilder stderr = commandExecutor.getStandardErrorFromCommand();
					}
					catch(Throwable t)
					{
						PApplet.println("Throwable t:"+t);
						return false;
					}
				}
			}
		}
		
		return true;
	}

	/**
	 * isNaN()
	 * @param x Float to check
	 * @return Whether the variable is NaN
	 */
	boolean isNaN(float x) {
		return x != x;
	}

	boolean isNaN(double x) {
		return x != x;
	}

	/**
	 * isInteger
	 * @param s String to check
	 * @param radix Maximum number of digits
	 * @return If the string is an integer
	 */
	boolean isInteger(String s, int radix) {
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
	 * getDaysInMonth()
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

	/**
	 * calculateDateTime()
	 * @param c Calendar date
	 * @return PVector containing (date, time, dayLength)
	 * Calculate date, time and dayLength for given Calendar date
	 */
	public PVector getSimulationTime(Calendar c) 	
	{		
		Location location = new Location("39.9522222", "-75.1641667");
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

		float cTime = cHour * 60 + cMin + cSec/60.f;
		float time = PApplet.map(cTime, 0.f, 1439.f, 0.f, 1.f); // Time of day when photo was taken		

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
		date = PApplet.constrain(PApplet.map(date, 0.f, 365, 0.f, 1.f), 0.f, 1.f);					//	 NOTE:	need to account for leap years!		

		return new PVector(date, time);				// Date between 0.f and 1.f, time between 0. and 1., dayLength in minutes
	}

	/**
	 * angleToCompass
	 * @param radians (Yaw) angle in radians
	 * @return Corresponding cmpass orientation
	 */
	String angleToCompass(float radians)
	{
		float angle = PApplet.degrees(radians);
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

	/**
	 * getSunsetTime()
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

		PApplet.println("---> ssHour:"+ssHour);

		if (cHour > ssHour) 
		{
			float ssDiff = (cHour * 60 + cMin + cSec/60.f) - (ssHour * 60 + ssMin + ssSec/60.f);
			PApplet.println("ssDiff:"+ssDiff);

			//			if (ssDiff > p.minutesPastSunset) {
			//				if (p.p.debug.debugExif)
			//					PApplet.print("Adjusted Sunset Length from:" + p.minutesPastSunset);
			//
			//				p.minutesPastSunset = ssDiff;
			//
			//				if (p.p.debug.debugExif)
			//					PApplet.println("  to:" + p.minutesPastSunset);
			//			}
		}

		if (cHour < srHour) 
		{
			float srDiff = (srHour * 60 + srMin + srSec/60.f) - (cHour * 60 + cMin + cSec/60.f);
			PApplet.println("srDiff:"+srDiff);
			//			if (srDiff > p.minutesBeforeSunrise) 
			//			{
			//				PApplet.print("Adjusted Sunrise Length from:" + p.sunriseLength);
			//				p.minutesBeforeSunrise = srDiff;
			//				PApplet.println("  to:" + p.sunriseLength);
			//			}
		}

		if (cHour > ssHour) 
		{
			PApplet.println("Difference in Sunset Time (min.): " + ((cHour * 60 + cMin + cSec/60.f) - (ssHour * 60 + ssMin + ssSec/60.f)));
			PApplet.print("Hour:" + cHour);
			PApplet.println(" Min:" + cMin);
			PApplet.print("Sunset Hour:" + ssHour);
			PApplet.println(" Sunset Min:" + ssMin);
		}
		//
		//		if (cHour < srHour && p.p.debug.debugExif && p.p.debug.debugDetail) {
		//			PApplet.println("Difference in Sunrise Time (min.): " + ((srHour * 60 + srMin + srSec/60.f) - (cHour * 60 + cMin + cSec/60.f)));
		//			PApplet.print("Hour:" + cHour);
		//			PApplet.println(" Min:" + cMin);
		//			PApplet.print("Sunrise Hour:" + srHour);
		//			PApplet.println(" Sunrise Min:" + srMin);
		//		}

		float sunriseTime = srHour * 60 + srMin + srSec/60.f;		
		float sunsetTime = ssHour * 60 + ssMin + ssSec/60.f;			
		float dayLength = sunsetTime - sunriseTime;

		float cTime = cHour * 60 + cMin + cSec/60.f;
		//		float time = PApplet.constrain(PApplet.map(cTime, sunriseTime, sunsetTime, 0.f, 1.f), 0.f, 1.f); // Time of day when photo was taken		
		float time = PApplet.map(cTime, sunriseTime, sunsetTime, 0.f, 1.f); // Time of day when photo was taken		

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
		date = PApplet.constrain(PApplet.map(date, 0.f, 365, 0.f, 1.f), 0.f, 1.f);					//	 NOTE:	need to account for leap years!		

		time = PApplet.map(sunsetTime, 0.f, 1439.f, 0.f, 1.f); // Time of day when photo was taken		

		return time;				// Date between 0.f and 1.f, time between 0. and 1., dayLength in minutes
	}

	/**
	 * getSunsetTime()
	 * @param c Calendar date
	 * @return Sunset time between 0. and 1.
	 * Get sunset time for given calendar date
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

		PApplet.println("---> ssHour:"+ssHour);

		if (cHour > ssHour) 
		{
			float ssDiff = (cHour * 60 + cMin + cSec/60.f) - (ssHour * 60 + ssMin + ssSec/60.f);
			//			PApplet.println("ssDiff:"+ssDiff);

			//			if (ssDiff > p.minutesPastSunset) {
			//				if (p.p.debug.debugExif)
			//					PApplet.print("Adjusted Sunset Length from:" + p.minutesPastSunset);
			//
			//				p.minutesPastSunset = ssDiff;
			//
			//				if (p.p.debug.debugExif)
			//					PApplet.println("  to:" + p.minutesPastSunset);
			//			}
		}

		if (cHour < srHour) 
		{
			float srDiff = (srHour * 60 + srMin + srSec/60.f) - (cHour * 60 + cMin + cSec/60.f);
			PApplet.println("srDiff:"+srDiff);
			//			if (srDiff > p.minutesBeforeSunrise) 
			//			{
			//				PApplet.print("Adjusted Sunrise Length from:" + p.sunriseLength);
			//				p.minutesBeforeSunrise = srDiff;
			//				PApplet.println("  to:" + p.sunriseLength);
			//			}
		}

		if (cHour < srHour) {
			PApplet.println("Difference in Sunrise Time (min.): " + ((srHour * 60 + srMin + srSec/60.f) - (cHour * 60 + cMin + cSec/60.f)));
			PApplet.print("Hour:" + cHour);
			PApplet.println(" Min:" + cMin);
			PApplet.print("Sunrise Hour:" + srHour);
			PApplet.println(" Sunrise Min:" + srMin);
		}

		float sunriseTime = srHour * 60 + srMin + srSec/60.f;		
		float sunsetTime = ssHour * 60 + ssMin + ssSec/60.f;			
		float dayLength = sunsetTime - sunriseTime;

		float cTime = cHour * 60 + cMin + cSec/60.f;
		//		float time = PApplet.constrain(PApplet.map(cTime, sunriseTime, sunsetTime, 0.f, 1.f), 0.f, 1.f); // Time of day when photo was taken		
		float time = PApplet.map(cTime, sunriseTime, sunsetTime, 0.f, 1.f); // Time of day when photo was taken		

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
		date = PApplet.constrain(PApplet.map(date, 0.f, 365, 0.f, 1.f), 0.f, 1.f);					//	 NOTE:	need to account for leap years!		

		time = PApplet.map(sunriseTime, 0.f, 1439.f, 0.f, 1.f); // Time of day when photo was taken		

		return time;				// Date between 0.f and 1.f, time between 0. and 1., dayLength in minutes
	}

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
}
