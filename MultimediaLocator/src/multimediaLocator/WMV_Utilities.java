package multimediaLocator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import processing.core.PApplet;
import processing.core.PVector;

/******************
 * @author davidgordon
 * Utility methods 
 */

public class WMV_Utilities 
{
	WMV_Utilities(){}
	
	/**
	 * Round to nearest <n> decimal places
	 */
	float round(float val, int n)
	{
		val *= Math.pow(10.f, n);
		val = Math.round(val);
		val /= Math.pow(10.f, n);
		return val;
	}
	
	/**
	 * Constrain float value between <min> and <max> by wrapping values around
	 */
	float constrainWrap(float value, float min, float max)
	{
		if(value < min)
			value += max;

		else if(value > max)
			value -= max;

		return value;
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
	
	/**
	 * @return Current date in days since Jan 1, 1980
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
		
//		if(p.p.debug.time)
//		{
//			System.out.println("Days: " + (int)duration.toDays());
//			System.out.println("  ISO-8601: " + duration);
//		}		

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
			theta1 += PApplet.PI * 2.f;
		if (theta2 < 0)
			theta2 += PApplet.PI * 2.f;
		if (theta1 > PApplet.PI * 2.f)
			theta1 -= PApplet.PI * 2.f;
		if (theta2 > PApplet.PI * 2.f)
			theta2 -= PApplet.PI * 2.f;

		float dist1 = Math.abs(theta1-theta2);
		float dist2;

		if (theta1 > theta2)
			dist2   = Math.abs(theta1 - PApplet.PI*2.f-theta2);
		else
			dist2   = Math.abs(theta2 - PApplet.PI*2.f-theta1);

		if (dist1 > dist2)
			return dist2;
		else
			return dist1;
	}

	/**
	 * Get GPS location for a given point in a field
	 * @param f Given field 
	 * @param loc Given point
	 * @return GPS location for given point
	 */
	public PVector getGPSLocation(WMV_Field f, PVector loc)			// -- Working??
	{
		WMV_Model m = f.getModel();
		
		float newX = PApplet.map( loc.x, -0.5f * m.getState().fieldWidth, 0.5f*m.getState().fieldWidth, m.getState().lowLongitude, m.getState().highLongitude ); 			// GPS longitude decreases from left to right
		float newY = PApplet.map( loc.z, -0.5f * m.getState().fieldLength, 0.5f*m.getState().fieldLength, m.getState().highLatitude, m.getState().lowLatitude ); 			// GPS latitude increases from bottom to top; negative to match P3D coordinate space

		PVector gpsLoc = new PVector(newX, newY);

		return gpsLoc;
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
	 * @param hour UTC hour
	 * @return Corresponding hour in Pacific Time
	 */
	public WMV_Time utcToPacificTime(WMV_Time time)
	{
		int year = time.getYear();
		int day = time.getDay();
		int month = time.getMonth();
		int hour = time.getHour();

		ZonedDateTime utcDateTime = ZonedDateTime.of(year, month, day, hour, time.getMinute(), time.getSecond(), time.getMillisecond(), ZoneId.of("UTC"));
		ZonedDateTime localDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("America/Los_Angeles"));
		
		WMV_Time result = new WMV_Time( localDateTime, time.getID(), time.getClusterID(), time.getMediaType(), "America/Los_Angeles" );
		return result;
	}

	/**
	 * Shrink images to 3D view format (640 pixels max width)
	 * @param largeImages Images to shrink
	 * @param destination Destination folder
	 * @return Whether successful
	 */
	public boolean shrinkImages(String largeImages, String destination)
	{
		System.out.println("Shrinking images:"+largeImages+" to:"+destination+"...");
		WMV_Command commandExecutor;
		ArrayList<String> command = new ArrayList<String>();
		ArrayList<String> files = new ArrayList<String>();

		/* Get files in directory */
		command = new ArrayList<String>();
		command.add("ls");
		commandExecutor = new WMV_Command(largeImages, command);
		try {
			int result = commandExecutor.execute();

			// get the output from the command
			StringBuilder stdout = commandExecutor.getStandardOutput();
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
			System.out.println("Throwable t while getting largeImage file list:"+t);
			return false;
		}
		
		/* Copy files to new directory */
		command = new ArrayList<String>();
		command.add("cp");
		command.add("-a");
		command.add(largeImages + ".");
		command.add(destination);
//		System.out.println("Copying command:"+command.toString());
		
		commandExecutor = new WMV_Command("", command);
		try {
			int result = commandExecutor.execute();

//			StringBuilder stdout = commandExecutor.getStandardOutputFromCommand();
//			StringBuilder stderr = commandExecutor.getStandardErrorFromCommand();

//			System.out.println("... copying result ..."+result);
		}
		catch(Throwable t)
		{
			System.out.println("Throwable t while copying files:"+t);
			return false;
		}
		
		files = new ArrayList<String>();
		command = new ArrayList<String>();
		command.add("ls");
		commandExecutor = new WMV_Command(destination, command);
		try {
			int result = commandExecutor.execute();

			StringBuilder stdout = commandExecutor.getStandardOutput();
			StringBuilder stderr = commandExecutor.getStandardError();

			String out = stdout.toString();
			String[] parts = out.split("\n");
			for (int i=0; i<parts.length; i++)
				files.add(parts[i]);
		}
		catch(Throwable t)
		{
			System.out.println("Throwable t while getting small_images file list for shrinking:"+t);
			return false;
		}

//		System.out.println("files.size():"+files.size());

		/* Shrink files in new directory */
		for (String fileName : files)					// -- Do in one command??
		{
			boolean isJpeg = false;
//			System.out.println("fileName:"+fileName);

			if(fileName != null && !fileName.equals(""))
			{
				String[] parts = fileName.split("\\.");

//				System.out.println("parts.length:"+parts.length);
				if(parts.length > 0)
				{
//					System.out.println("parts[0]:"+parts[0]);
//					System.out.println("parts[length-1]:"+parts[parts.length-1]);
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
//					System.out.println("destination:"+destination +" command:"+command);
					commandExecutor = new WMV_Command(destination, command);

					try {
						int result = commandExecutor.execute();

						// get the output from the command
						StringBuilder stdout = commandExecutor.getStandardOutput();
						StringBuilder stderr = commandExecutor.getStandardError();
					}
					catch(Throwable t)
					{
						System.out.println("Throwable t:"+t);
						return false;
					}
				}
			}
		}
		
		return true;
	}

	/**
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
	 * Find cluster time segments from given media's capture times
	 * @param times List of times
	 * @param timePrecision Number of histogram bins
	 * @return Time segments
	 */
	ArrayList<WMV_TimeSegment> calculateTimeSegments(ArrayList<WMV_Time> mediaTimes, float timePrecision, int clusterID)				// -- clusterTimelineMinPoints!!								
	
	{
		mediaTimes.sort(WMV_Time.WMV_SimulationTimeComparator);			// Sort media by simulation time (normalized 0. to 1.)

		if(mediaTimes.size() > 0)
		{
			ArrayList<WMV_TimeSegment> segments = new ArrayList<WMV_TimeSegment>();
			
			int count = 0, startCount = 0;
			WMV_Time curLower, curUpper, last;

			curLower = mediaTimes.get(0);
			curUpper = mediaTimes.get(0);
			last = mediaTimes.get(0);

			for(WMV_Time t : mediaTimes)									// Multiple time segments for cluster
			{
				if(t.getTime() != last.getTime())
				{
					if(t.getTime() - last.getTime() < timePrecision)		// If moved by less than precision amount since last time, extend segment 
					{
						curUpper = t;										// Move curUpper to new value
						last = t;
//						PApplet.print("Extending segment...");
					}
					else
					{
						WMV_Time center;
						if(count == startCount)
						{
							curLower = t;
							curUpper = t;
							center = t;
						}
						else
						{
							if(curUpper.getTime() == curLower.getTime())
							{
								center = curUpper;								// If upper and lower are same, set center to that value
							}
							else
							{
								int middle = (count-startCount)/2;				// Find center
								if ((count-startCount)%2 == 1) 
									center = mediaTimes.get(middle);			// Median if even #
								else
									center = mediaTimes.get(middle-1);			// Use lower of center pair if odd #
							}
						}
						
//						if(p.p.debug.time)
//						{
//							if(p.p.debug.detailed) System.out.println("Cluster #"+clusterID+"... Finishing time segment... center:"+(center.getTime())+" curUpper:"+(curUpper.getTime())+" curLower:"+(curLower.getTime()));
//
//							if(curUpper.getTime() - curLower.getTime() > 0.001f)
//							{
//								System.out.println("---> Cluster #"+clusterID+" with long time segment: center:"+(center.getTime())+" curUpper:"+(curUpper.getTime())+" curLower:"+(curLower.getTime()));
//								System.out.println("t.getTime():"+t.getTime()+" last:"+last.getTime()+" t.getTime() - last.getTime():"+(t.getTime() - last.getTime()));
//							}
//						}
						ArrayList<WMV_Time> tl = new ArrayList<WMV_Time>();			// Create timeline for segment
						for(int i=startCount; i<=count; i++)
						{
							tl.add(mediaTimes.get(i));
//							System.out.println("Added media time...");
						}
						
						segments.add(new WMV_TimeSegment(clusterID, -1, -1, -1, -1, -1, -1, center, curUpper, curLower, tl));	// Add time segment
						
//						tsID++;
						curLower = t;
						curUpper = t;
						last = t;
						startCount = count + 1;
					}
				}
//				else 
//					System.out.println("Same as last...");
				
				count++;
//				System.out.println("count:"+count);
			}
			
			if(startCount == 0)									// Single time segment for cluster
			{
				WMV_Time center;
				if(curUpper.getTime() == curLower.getTime())
					center = curUpper;							// If upper and lower are same, set center to that value
				else
				{
					int middle = (count-startCount)/2;			// Find center
					if ((count-startCount)%2 == 1) 
					    center = mediaTimes.get(middle);			// Median if even #
					else
					   center = mediaTimes.get(middle-1);			// Use lower of center pair if odd #
				}

				ArrayList<WMV_Time> tl = new ArrayList<WMV_Time>();			// Create timeline for segment
				for(int i=0; i<mediaTimes.size(); i++)
					tl.add(mediaTimes.get(i));

//				System.out.println("Finishing time segment... center:"+(center.getTime())+" curUpper:"+(curUpper.getTime())+" curLower:"+(curLower.getTime()));

				if(curUpper.getTime() - curLower.getTime() > 0.001f)
				{
					System.out.println("-> Cluster #"+clusterID+" with long time segment: center:"+(center.getTime())+" curUpper:"+(curUpper.getTime())+" curLower:"+(curLower.getTime()));
//					System.out.println("t.getTime():"+t.getTime()+" last:"+last.getTime());
//					System.out.println("t.getTime() - last.getTime():"+(t.getTime() - last.getTime()));
				}
				
				segments.add(new WMV_TimeSegment(clusterID, -1, -1, -1, -1, -1, -1, center, curUpper, curLower, tl));
			}
			
			return segments;			// Return cluster list
		}
		else
		{
//			if(p.p.p.debug.time)
//				System.out.println("cluster:"+id+" getTimeSegments() == null but has mediaPoints:"+mediaCount);
			return null;		
		}
	}
	

	public float getTimelineLength(ArrayList<WMV_TimeSegment> timeline)
	{
		float start = timeline.get(0).getLower().getTime();
		float end = timeline.get(timeline.size()-1).getUpper().getTime();
		float length = (end - start) * getTimePVectorSeconds(new PVector(24,0,0));
		return length;
	}
	
	/**
	 * angleToCompass
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
			PApplet.print("Hour:" + cHour);
			System.out.println(" Min:" + cMin);
			PApplet.print("Sunset Hour:" + ssHour);
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

		System.out.println("---> ssHour:"+ssHour);

		if (cHour > ssHour) 
		{
			float ssDiff = (cHour * 60 + cMin + cSec/60.f) - (ssHour * 60 + ssMin + ssSec/60.f);
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

		if (cHour < srHour) {
			System.out.println("Difference in Sunrise Time (min.): " + ((srHour * 60 + srMin + srSec/60.f) - (cHour * 60 + cMin + cSec/60.f)));
			PApplet.print("Hour:" + cHour);
			System.out.println(" Min:" + cMin);
			PApplet.print("Sunrise Hour:" + srHour);
			System.out.println(" Sunrise Min:" + srMin);
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
	
	private static String readAll(Reader rd) throws IOException 
	{
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
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
