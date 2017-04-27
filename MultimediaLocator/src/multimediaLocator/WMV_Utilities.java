package multimediaLocator;

import processing.core.PApplet;
import processing.core.PVector;
import processing.data.IntList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

/******************
 * @author davidgordon
 * Utility methods 
 */

public class WMV_Utilities 
{
	WMV_Utilities(){}
	
	/**
	 * Create a directory at destination
	 * @param folderName Folder name
	 * @param destination Destination for folder
	 */
	public void makeDirectory(String folderName, String destination)
	{
		WMV_Command commandExecutor;
		ArrayList<String> command = new ArrayList<String>();
		//	ArrayList<String> files = new ArrayList<String>();

		command = new ArrayList<String>();				/* Create small_images directory */
		command.add("mkdir");
		command.add(folderName);
		commandExecutor = new WMV_Command(destination, command);
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
	    throw new Error("Impossible");
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
	 * Based on JarvisPoints by UncleBob
	 * @author davidgordon
	 *
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

		// The starting point is the point with the lowest X with ties going to the lowest Y.  This guarantees
		// that the next point over is clockwise.
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
	 * Get GPS location for a given point in a field
	 * @param f Given field 
	 * @param loc Given point
	 * @return GPS location for given point
	 */
	public PVector getGPSLocation(WMV_Field f, PVector loc)			
	{
		WMV_Model m = f.getModel();
		
		float newX = mapValue( loc.x, -0.5f * m.getState().fieldWidth, 0.5f*m.getState().fieldWidth, m.getState().lowLongitude, m.getState().highLongitude ); 			// GPS longitude decreases from left to right
		float newY = mapValue( loc.z, -0.5f * m.getState().fieldLength, 0.5f*m.getState().fieldLength, m.getState().highLatitude, m.getState().lowLatitude ); 			// GPS latitude increases from bottom to top; negative to match P3D coordinate space

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

	/**
	 * Shrink images to 3D view format (640 pixels max width)
	 * @param largeImages Images to shrink
	 * @param destination Destination folder
	 * @return Whether successful
	 */
	public boolean shrinkImages(String largeImages, String destination)
	{
//		if(world.debugSettings.main)
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

			// Get the output from the command
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
	ArrayList<WMV_TimeSegment> createTimeline(ArrayList<WMV_Time> mediaTimes, float timePrecision, int clusterID)				// -- clusterTimelineMinPoints!!								
	{
		boolean finished = false;
		mediaTimes.sort(WMV_Time.WMV_SimulationTimeComparator);			// Sort media by simulation time (normalized 0. to 1.)

		if(mediaTimes.size() > 0)
		{
			ArrayList<WMV_TimeSegment> segments = new ArrayList<WMV_TimeSegment>();
			
			int count = 0, startCount = 0;
			WMV_Time curLower, curUpper, last;

			curLower = mediaTimes.get(0);
			curUpper = mediaTimes.get(0);
			last = mediaTimes.get(0);

			for(WMV_Time t : mediaTimes)									// Find time segments for cluster
			{
				if(t.getClusterID() != clusterID)							// Set cluster ID if incorrect value
					t.setClusterID(clusterID);	
				
				if(t.getTime() != last.getTime())
				{
					if(t.getTime() - last.getTime() < timePrecision)		// Extend segment if moved by less than precision amount
					{
						curUpper = t;										// Move curUpper to new value
						last = t;

//						System.out.println("Extending segment...");
						if(count == mediaTimes.size() - 1)					// Reached end while extending segment
						{
//							System.out.println("---> Extending segment at end...");
							ArrayList<WMV_Time> tl = new ArrayList<WMV_Time>();			// Create timeline for segment
							for(int i=startCount; i<=count; i++)
								tl.add(mediaTimes.get(i));
							tl.sort(WMV_Time.WMV_SimulationTimeComparator);

//							System.out.println("(0) Finishing time segment... ");
							segments.add(createSegment(clusterID, tl));		// Add time segment
							finished = true;
						}
					}
					else
					{
						ArrayList<WMV_Time> tl = new ArrayList<WMV_Time>();			// Create timeline for segment
//						for(int i=startCount; i<=count; i++)
						for(int i=startCount; i<count; i++)
							tl.add(mediaTimes.get(i));
						tl.sort(WMV_Time.WMV_SimulationTimeComparator);
						
						if(tl.get(tl.size()-1).getTime() - tl.get(0).getTime() > 0.002f)
						{
							System.out.println("(1) Finishing time segment... startCount:"+startCount+" count:"+count);
							System.out.println("---> Very long time segment: tl upper:"+(tl.get(tl.size()-1).getTime())+" tl lower:"+(tl.get(0).getTime()));
							System.out.println("           			         curUpper:"+(curUpper.getTime())+" curLower:"+(curLower.getTime()));
						}

						segments.add(createSegment(clusterID, tl));	// Add time segment
//						segments.add(new WMV_TimeSegment(clusterID, -1, -1, -1, -1, -1, -1, tl));

						curLower = t;
						curUpper = t;
						last = t;
						startCount = count;
						
						if(count == mediaTimes.size() - 1)			// Create single segment at end
						{
							tl = new ArrayList<WMV_Time>();			// Create timeline for segment
							tl.add(mediaTimes.get(count));
//							System.out.println("Finishing single segment at end...");
//							System.out.println("(2) Finishing time segment...");
							segments.add(createSegment(clusterID, tl));		// Add time segment
						}
					}
				}
				else																// Same time as last
				{
					if(count == mediaTimes.size() - 1)								// Reached end
					{
//						System.out.println("--> Same as last at end...");

						ArrayList<WMV_Time> tl = new ArrayList<WMV_Time>();			// Create timeline for segment
						for(int i=startCount; i<=count; i++)
							tl.add(mediaTimes.get(i));

						tl.sort(WMV_Time.WMV_SimulationTimeComparator);
						if(tl.get(tl.size()-1).getTime() - tl.get(0).getTime() > 0.002f)
						{
							System.out.println("(3) Finishing time segment...");
							System.out.println("---> Very long time segment: tl upper:"+(tl.get(tl.size()-1).getTime())+" tl lower:"+(tl.get(0).getTime()));
							System.out.println("           			         curUpper:"+(curUpper.getTime())+" curLower:"+(curLower.getTime()));
						}

						segments.add(createSegment(clusterID, tl));	// Add time segment
						finished = true;
					}
				}
				
				count++;
			}
			
			if(startCount == 0 && !finished)									// Single time segment
			{
//				System.out.println("--> Single time segment...");
				ArrayList<WMV_Time> tl = new ArrayList<WMV_Time>();				// Create timeline for segment
				for(WMV_Time t : mediaTimes)
					tl.add(t);
				tl.sort(WMV_Time.WMV_SimulationTimeComparator);

//				System.out.println("(4) Finishing time segment...");
				if(tl.get(tl.size()-1).getTime() - tl.get(0).getTime() > 0.002f)
				{
					System.out.println("---> Very long time segment: tl upper:"+(tl.get(tl.size()-1).getTime())+" tl lower:"+(tl.get(0).getTime()));
					System.out.println("           			         curUpper:"+(curUpper.getTime())+" curLower:"+(curLower.getTime()));
				}

				segments.add(createSegment(clusterID, tl));	// Add time segment
			}
			
			return segments;			
		}
		else
		{
//			if(p.p.p.debug.time) System.out.println("cluster:"+id+" getTimeSegments() == null but has mediaPoints:"+mediaCount);
			return null;		
		}
	}
	
	public WMV_TimeSegment createSegment(int clusterID, ArrayList<WMV_Time> timeline)
	{
		WMV_TimeSegment ts = new WMV_TimeSegment();
		ts.initialize(clusterID, -1, -1, -1, -1, -1, -1, timeline);
		
		checkTimeSegment(ts);
		return ts;
	}

	public void checkTimeSegment(WMV_TimeSegment ts)
	{
		float upper = ts.getUpper().getTime();
		float lower = ts.getLower().getTime();
		
//		System.out.println("checkTimeSegment()...");
		for(WMV_Time t : ts.timeline)
		{
//			System.out.println(" checkTimeSegment()... t.getTime():"+t.getTime());
			if(t.getTime() < lower)
			{
				System.out.println("  t.getTime() < lower... t.getTime():"+t.getTime()+" lower:"+lower);
			}
			if(t.getTime() > upper)
			{
				System.out.println("  t.getTime() < lower... t.getTime():"+t.getTime()+" upper:"+upper);
			}
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
			System.out.print("Hour:" + cHour);
			System.out.println(" Min:" + cMin);
			System.out.print("Sunrise Hour:" + srHour);
			System.out.println(" Sunrise Min:" + srMin);
		}

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

		time = mapValue(sunriseTime, 0.f, 1439.f, 0.f, 1.f); // Time of day when photo was taken		

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
}
