package wmViewer;

import java.util.ArrayList;

import processing.core.PApplet;

/**************
 * @author davidgordon
 * A media library in the expected format:
 * 1. Subfolders containing media should be: "small_images", "small_videos", "panoramas"
 * 2. Media should be: images <= 640px wide / videos <= 720px / panoramas full size
 */
public class WMV_Library 
{
	private String libraryFolder;								// Filepath for library folder 
	private ArrayList<String> folders;							// Directories for each field in library

	WMV_Library(String newLibraryFolder)
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
//		PApplet.println("Added media folder "+newFolder+" to library");
	}
	
	public ArrayList<String> getFolders()
	{
		return folders;
	}
}
