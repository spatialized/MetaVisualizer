package multimediaLocator;

import java.io.File;
import java.util.ArrayList;

import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;

import static org.boon.Boon.puts;

/**************
 * Represents a multimedia library
 * @author davidgordon
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

	public void saveWorldSettings(WMV_WorldSettings settings, String newFilePath)		
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
			System.out.println("WorldSettings are equal:"+ newSettings.equals(settings)); 
		}
		catch (Throwable t)
		{
			System.out.println("saveWorldSettings Throwable t:"+t);
		}
	}

	public void saveWorldState(WMV_WorldState state, String newFilePath)		
	{
		String filePath = newFilePath;

		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
			//		    file = File.createTempFile("json", "temp.json");    // Use temp file
			file = new File(filePath);
			mapper.writeValue(file, state);    // Write staff object to file

			WMV_WorldState newState = mapper.readValue(file, WMV_WorldState.class);
			System.out.println("WorldStates are equal:"+ newState.equals(state));      
		}
		catch (Throwable t)
		{
			System.out.println("saveWorldState Throwable t:"+t);
		}
	}

	public void saveViewerSettings(WMV_ViewerSettings settings, String newFilePath)		
	{
		String filePath = newFilePath;

		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
			file = new File(filePath);
			mapper.writeValue(file, settings);    // Write staff object to file

			WMV_ViewerSettings newSettings = mapper.readValue(file, WMV_ViewerSettings.class);
			System.out.println("ViewerSettings are equal:"+ newSettings.equals(settings));      // Not working ??
		}
		catch (Throwable t)
		{
			System.out.println("saveViewerSettings Throwable t:"+t);
		}
	}

	public void saveViewerState(WMV_ViewerState state, String newFilePath)		// Testing
	{
		String filePath = newFilePath;

		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
			file = new File(filePath);
			mapper.writeValue(file, state);    // Write staff object to file

			WMV_ViewerState newState = mapper.readValue(file, WMV_ViewerState.class);
			System.out.println("ViewerStates are equal:"+ newState.equals(state));      // Not working ??
		}
		catch (Throwable t)
		{
			System.out.println("saveViewerState Throwable t:"+t);
		}
	}

	public void saveFieldState(WMV_FieldState fState, String newFilePath)
	{
//		System.out.println("--> library.saveFieldState()");
//		System.out.println("iState.getMediaState() == null? "+(fState.images.get(0).getMediaState() == null));
//		System.out.println("iState.getMediaState().location == null? "+(fState.images.get(0).getMediaState().location == null));
//		if(fState.images.get(0).getMediaState().location != null)
//			System.out.println("  mState.location: "+fState.images.get(0).getMediaState().location);
//		System.out.println("fState.images.get(0).getMediaState().name == null? "+(fState.images.get(0).getMediaState().name == null));
//		if(fState.images.get(0).getMediaState().name != null)
//			System.out.println("  mState.name: "+fState.images.get(0).getMediaState().name);
		
		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
//			file = File.createTempFile("json", "temp.json");    // Use temp file
			file = new File(newFilePath);
			mapper.writeValue(file, fState);    // Write staff object to file

			WMV_FieldState newFieldState = mapper.readValue(file, WMV_FieldState.class);
			puts("saveFieldData... Field states are equal", newFieldState.equals(fState));      // Not working ??
		}
		catch (Throwable t)
		{
			System.out.println("saveFieldData Throwable t:"+t);
		}
	}


	public WMV_WorldSettings loadWorldSettings(String newFilePath)		// Testing
	{
		String filePath = newFilePath;

		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
			file = new File(filePath);
			WMV_WorldSettings newState = mapper.readValue(file, WMV_WorldSettings.class);
			return newState;
		}
		catch (Throwable t)
		{
			System.out.println("loadWorldSettings Throwable t:"+t);
		}
		return null;
	}

	public WMV_WorldState loadWorldState(String newFilePath)		// Testing
	{
		String filePath = newFilePath;

		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
			file = new File(filePath);
			WMV_WorldState newState = mapper.readValue(file, WMV_WorldState.class);
			return newState;
		}
		catch (Throwable t)
		{
			System.out.println("loadWorldState Throwable t:"+t);
		}
		return null;
	}

	public WMV_ViewerSettings loadViewerSettings(String newFilePath)		// Testing
	{
		String filePath = newFilePath;

		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
			file = new File(filePath);
			WMV_ViewerSettings newState = mapper.readValue(file, WMV_ViewerSettings.class);
			return newState;
		}
		catch (Throwable t)
		{
			System.out.println("loadViewerSettings Throwable t:"+t);
		}
		return null;
	}

	public WMV_ViewerState loadViewerState(String newFilePath)		// Testing
	{
		String filePath = newFilePath;

		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
			file = new File(filePath);
			WMV_ViewerState newState = mapper.readValue(file, WMV_ViewerState.class);
			return newState;
		}
		catch (Throwable t)
		{
			System.out.println("loadViewerState Throwable t:"+t);
		}
		return null;
	}

	/**
	 * Load field state from given file path
	 * @param newFilePath File path
	 * @return Field state
	 */
	public WMV_FieldState loadFieldState(String newFilePath)		// Testing
	{
		String filePath = newFilePath;

		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
//		    file = File.createTempFile("json", "temp.json");    // Use temp file
			file = new File(filePath);
//		    mapper.writeValue(file, state);    // Write staff object to file

			WMV_FieldState fState = mapper.readValue(file, WMV_FieldState.class);
			
			return fState;
		}
		catch (Throwable t)
		{
			System.out.println("loadFieldState Throwable t:"+t);
		}
		return null;
	}
}
