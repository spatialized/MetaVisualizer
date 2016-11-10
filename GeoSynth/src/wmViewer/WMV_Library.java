package wmViewer;

import java.util.ArrayList;

import processing.core.PApplet;

/**************
 * @author davidgordon
 * GeoSynth media library 
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
//		PApplet.println("Added media folder:"+newFolder);
	}
	
	public ArrayList<String> getFolders()
	{
		return folders;
	}
}
