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
		PApplet.println("Added media folder "+newFolder+" to library");
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
		    PApplet.println("Throwable t:"+t);
		  }
		  //catch (IOException e)    // thrown by createTempFile
		  //{
		  //  println("IOException e:"+e);
		  //}

	}

}
