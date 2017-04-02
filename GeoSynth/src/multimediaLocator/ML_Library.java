package multimediaLocator;

import java.io.File;
import java.util.ArrayList;

import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;

import static org.boon.Boon.puts;

import processing.core.PApplet;

/**************
 * @author davidgordon
 * The multimedia media library
 */
public class ML_Library 
{
	private String libraryFolder;								// Filepath for library folder 
	private ArrayList<String> folders;							// Directories for each field in library

	ML_Library(String newLibraryFolder)
	{
		folders = new ArrayList<String>();
		libraryFolder = newLibraryFolder;
	}
	
	public String getLibraryFolder()
	{
		return libraryFolder;
	}
	
	public void addFolder(String newFolder)
	{
		folders.add(newFolder);
		System.out.println("Added media folder "+newFolder+" to library");
	}
	
	public ArrayList<String> getFolders()
	{
		return folders;
	}
	
	public void saveFieldData(WMV_Field f)
	{
//		/* Data */
//		WMV_Model model;										// Dimensions and properties of current virtual space
//		public ArrayList<WMV_Image> images; 					// All images in this field
//		public ArrayList<WMV_Panorama> panoramas; 				// All panoramas in this field
//		public ArrayList<WMV_Video> videos; 					// All videos in this field
//		public ArrayList<WMV_Sound> sounds; 					// All videos in this field
//		public ArrayList<WMV_Cluster> clusters;					// Spatial groupings of media in the Image3D and Video3D arrays
//
//		private int imageErrors = 0, videoErrors = 0, panoramaErrors = 0;			// Metadata loading errors per media type
//
//		/* Time */
//		ArrayList<WMV_TimeSegment> timeline;						// List of time segments in this field ordered by time from 0:00 to 24:00 as a single day
//		ArrayList<ArrayList<WMV_TimeSegment>> timelines;			// Lists of time segments in field ordered by date
//		ArrayList<WMV_Date> dateline;								// List of dates in this field, whose indices correspond with timelines in timelines list
//		String timeZoneID = "America/Los_Angeles";					// Current time zone
		
		  String filePath = "fieldOutputTest.json";

		  final ObjectMapper mapper = JsonFactory.create();
		  final File file;
		  try {
		    file = File.createTempFile("json", "temp.json");    // Use temp file
//		    file = new File(filePath);
		    mapper.writeValue(file, f);    // Write staff object to file

//		    Staff newStaff = mapper.readValue(file, Staff.class);
		    WMV_Field newField = mapper.readValue(file, WMV_Field.class);
		    puts("They are equal", newField.equals(f));      // Not working ??
		    //println("They are equal", newStaff.equals(staff));
		  }
		  catch (Throwable t)
		  {
		    System.out.println("Throwable t:"+t);
		  }
		  //catch (IOException e)    // thrown by createTempFile
		  //{
		  //  println("IOException e:"+e);
		  //}

	}
	
	public void saveTestImageData(WMV_Image image, String newFilePath)		// Testing
	{
		  String filePath = newFilePath;

		  final ObjectMapper mapper = JsonFactory.create();
		  final File file;
		  try {
		    file = File.createTempFile("json", "temp.json");    // Use temp file
//		    file = new File(filePath);
		    mapper.writeValue(file, image);    // Write staff object to file

//		    Staff newStaff = mapper.readValue(file, Staff.class);
		    WMV_Image newImage = mapper.readValue(file, WMV_Image.class);
		    puts("They are equal", newImage.equals(image));      // Not working ??
		    //println("They are equal", newStaff.equals(staff));
		  }
		  catch (Throwable t)
		  {
		    System.out.println("Throwable t:"+t);
		  }
	}
	
	public void saveWorldSettings(WMV_WorldSettings settings, String newFilePath)		// Testing
	{
		  String filePath = newFilePath;

		  final ObjectMapper mapper = JsonFactory.create();
		  final File file;
		  try {
//		    file = File.createTempFile("json", "temp.json");    // Use temp file
		    file = new File(filePath);
		    mapper.writeValue(file, settings);    // Write staff object to file

//		    Staff newStaff = mapper.readValue(file, Staff.class);
		    WMV_WorldSettings newSettings = mapper.readValue(file, WMV_WorldSettings.class);
		    System.out.println("Equal"+ newSettings.equals(settings));      // Not working ??
		  }
		  catch (Throwable t)
		  {
		    System.out.println("Throwable t:"+t);
		  }
	}
	
	public void saveWorldState(WMV_WorldState state, String newFilePath)		// Testing
	{
		  String filePath = newFilePath;

		  final ObjectMapper mapper = JsonFactory.create();
		  final File file;
		  try {
//		    file = File.createTempFile("json", "temp.json");    // Use temp file
		    file = new File(filePath);
		    mapper.writeValue(file, state);    // Write staff object to file

		    WMV_WorldState newState = mapper.readValue(file, WMV_WorldState.class);
		    System.out.println("Equal"+ newState.equals(state));      // Not working ??
		  }
		  catch (Throwable t)
		  {
		    System.out.println("Throwable t:"+t);
		  }
	}
	
	public void saveViewerSettings(WMV_ViewerSettings settings, String newFilePath)		// Testing
	{
		  String filePath = newFilePath;

		  final ObjectMapper mapper = JsonFactory.create();
		  final File file;
		  try {
//		    file = File.createTempFile("json", "temp.json");    // Use temp file
		    file = new File(filePath);
		    mapper.writeValue(file, settings);    // Write staff object to file

		    WMV_ViewerSettings newSettings = mapper.readValue(file, WMV_ViewerSettings.class);
		    System.out.println("Equal"+ newSettings.equals(settings));      // Not working ??
		  }
		  catch (Throwable t)
		  {
		    System.out.println("Throwable t:"+t);
		  }
	}
	
	public void saveViewerState(WMV_ViewerState state, String newFilePath)		// Testing
	{
		  String filePath = newFilePath;

		  final ObjectMapper mapper = JsonFactory.create();
		  final File file;
		  try {
//		    file = File.createTempFile("json", "temp.json");    // Use temp file
		    file = new File(filePath);
		    mapper.writeValue(file, state);    // Write staff object to file

		    WMV_ViewerState newState = mapper.readValue(file, WMV_ViewerState.class);
		    System.out.println("Equal"+ newState.equals(state));
//		    puts("They are equal", newState.equals(state));      // Not working ??
		  }
		  catch (Throwable t)
		  {
		    System.out.println("Throwable t:"+t);
		  }
	}

	public void saveFieldState(WMV_FieldState state, String newFilePath)		// Testing
	{
		  String filePath = newFilePath;

		  final ObjectMapper mapper = JsonFactory.create();
		  final File file;
		  try {
//		    file = File.createTempFile("json", "temp.json");    // Use temp file
		    file = new File(filePath);
		    mapper.writeValue(file, state);    // Write staff object to file

		    WMV_FieldState newState = mapper.readValue(file, WMV_FieldState.class);
		    System.out.println("Equal"+ newState.equals(state));
//		    puts("They are equal", newState.equals(state));      // Not working ??
		  }
		  catch (Throwable t)
		  {
		    System.out.println("Throwable t:"+t);
		  }
	}

}
