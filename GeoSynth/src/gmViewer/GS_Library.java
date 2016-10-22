package gmViewer;

import java.util.ArrayList;

import processing.core.PApplet;

/**************
 * GS_Library
 * @author davidgordon
 * GeoSynth media library 
 */
public class GS_Library 
{
	private String libraryFolder;								// Filepath for library folder 
	private ArrayList<String> folders;							// Directories for each field in library

	GS_Library(String newLibraryFolder)
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
