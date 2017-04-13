package multimediaLocator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.lang.reflect.*;

import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
//import static org.boon.Boon.puts;
import com.google.gson.reflect.TypeToken;

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
			file = new File(filePath);
			mapper.writeValue(file, settings);    // Write object to file
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
			mapper.writeValue(file, state);    // Write object to file

//			WMV_WorldState newState = mapper.readValue(file, WMV_WorldState.class);
//			System.out.println("WorldStates are equal:"+ newState.equals(state));      
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
			mapper.writeValue(file, settings);    // Write object to file
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
			mapper.writeValue(file, state);    // Write object to file
		}
		catch (Throwable t)
		{
			System.out.println("saveViewerState Throwable t:"+t);
		}
	}

//	public void saveFieldState(WMV_FieldState fState, String newFilePath)
//	{
////		Gson gson = new Gson();
////		String jsonString = gson.toJson(fState);
////		System.out.println(jsonString);
//
//		Type type = new TypeToken<WMV_FieldState>(){}.getType();
////		Type type = WMV_FieldState.class;
//		saveJson(fState, type, newFilePath);
//	}

	public void saveFieldState(WMV_FieldState fState, String newFilePath)
	{
		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
			file = new File(newFilePath);
			mapper.writeValue(file, fState);    // Write object to file
		}
		catch (Throwable t)
		{
			System.out.println("saveFieldData Throwable t:"+t);
		}
	}

	public void saveClusterStateList(WMV_ClusterStateList csl, String newFilePath)
	{
		if(csl.clusters.size() <= 50)
		{
			final ObjectMapper mapper = JsonFactory.create();
			final File file;
			try {
				file = new File(newFilePath);
				mapper.writeValue(file, csl);    // Write object to file
			}
			catch (Throwable t)
			{
				System.out.println("saveClusterStateList Throwable t:"+t);
			}
		}
		else
		{
			for(int i=0; i<csl.clusters.size(); i+=50)
			{
				ArrayList<WMV_ClusterState> temp = new ArrayList<WMV_ClusterState>();
				for(int idx = i; idx < i+50; idx++)
				{
					if(idx < csl.clusters.size() - 1)
						temp.add(csl.clusters.get(idx));
					else
						break;
				}
				
				WMV_ClusterStateList tempCsl = new WMV_ClusterStateList();
				tempCsl.setClusters(temp);
				
				final ObjectMapper mapper = JsonFactory.create();
				final File file;
				try {
					file = new File(newFilePath.replaceAll(".json", "_"+((Math.round(i/50)+1)+".json")));
					mapper.writeValue(file, tempCsl);    // Write object to file
				}
				catch (Throwable t)
				{
					System.out.println("saveClusterStateList Throwable t:"+t+" i:"+i+"...");
				}
			}
		}
	}

	public void saveImageStateList(WMV_ImageStateList isl, String newFilePath)
	{
		if(isl.images.size() <= 100)
		{
			final ObjectMapper mapper = JsonFactory.create();
			final File file;
			try {
				file = new File(newFilePath);
				mapper.writeValue(file, isl);    // Write object to file
			}
			catch (Throwable t)
			{
				System.out.println("saveImageStateList Throwable t:"+t);
			}
		}
		else
		{
			for(int i=0; i<isl.images.size(); i+=1000)
			{
				ArrayList<WMV_ImageState> temp = new ArrayList<WMV_ImageState>();
				for(int idx = i; idx < i+1000; idx++)
				{
					if(idx < isl.images.size() - 1)
						temp.add(isl.images.get(idx));
					else
						break;
				}

				WMV_ImageStateList tempIsl = new WMV_ImageStateList();
				tempIsl.setImages(temp);
//				System.out.println("i:"+i+" saved:"+tempIsl.images.size());

				final ObjectMapper mapper = JsonFactory.create();
				final File file;
				try {
					file = new File(newFilePath.replaceAll(".json", "_"+((Math.round(i/1000)+1)+".json")));
					mapper.writeValue(file, tempIsl);    // Write object to file
				}
				catch (Throwable t)
				{
					System.out.println("saveImageStateList Throwable t:"+t+" i:"+i+"...");
				}
			}
		}
	}

//	public void saveImageStateList(WMV_ImageStateList isl, String newFilePath)
//	{
//		final ObjectMapper mapper = JsonFactory.create();
//		final File file;
//		try {
//			file = new File(newFilePath);
//			mapper.writeValue(file, isl);    // Write object to file
//		}
//		catch (Throwable t)
//		{
//			System.out.println("saveClusterStateList Throwable t:"+t);
//		}
//	}

	public void savePanoramaStateList(WMV_PanoramaStateList psl, String newFilePath)
	{
		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
			file = new File(newFilePath);
			mapper.writeValue(file, psl);    // Write object to file
		}
		catch (Throwable t)
		{
			System.out.println("saveClusterStateList Throwable t:"+t);
		}
	}

	public void saveVideoStateList(WMV_VideoStateList vsl, String newFilePath)
	{
		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
			file = new File(newFilePath);
			mapper.writeValue(file, vsl);    // Write object to file
		}
		catch (Throwable t)
		{
			System.out.println("saveClusterStateList Throwable t:"+t);
		}
	}

	public void saveSoundStateList(WMV_SoundStateList ssl, String newFilePath)
	{
		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
			file = new File(newFilePath);
			mapper.writeValue(file, ssl);    // Write object to file
		}
		catch (Throwable t)
		{
			System.out.println("saveSoundStateList Throwable t:"+t);
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

//	/**
//	 * Load field state from given file path (with GSON)
//	 * @param newFilePath File path
//	 * @return Field state
//	 */
//	public WMV_FieldState loadFieldState(String newFilePath)		// Testing
//	{
//		String filePath = newFilePath;
//		Gson gson = new Gson();
//
//		try {
//			WMV_FieldState fState;
//			fState = gson.fromJson(new FileReader(filePath), WMV_FieldState.class);
//			return fState;
//		}
//		catch(Throwable t)
//		{
//			System.out.println("loadFieldState throwable t:"+t);
//		}
//		
//		return null;
//	}
	
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
			file = new File(filePath);
			WMV_FieldState fState = mapper.readValue(file, WMV_FieldState.class);
			
			return fState;
		}
		catch (Throwable t)
		{
			System.out.println("loadFieldState Throwable t:"+t);
		}
		return null;
	}

	/**
	 * Load field state from given file path
	 * @param newFilePath File path
	 * @return Field state
	 */
	public WMV_ClusterStateList loadClusterStateLists(String directoryPath)		// Testing
	{
		File dir = new File(directoryPath);
		WMV_ClusterStateList csl = new WMV_ClusterStateList();
		csl.clusters = new ArrayList<WMV_ClusterState>();
		
		if(dir.exists())
		{
			File[] files = dir.listFiles();
			for(int i = 0; i<files.length; i++)
			{
				String filePath = files[i].getAbsolutePath();

				final ObjectMapper mapper = JsonFactory.create();
				final File file;
				try {
//					System.out.println("loadClusterStateLists()...  filePath: "+filePath);
					file = new File(filePath);
					WMV_ClusterStateList temp = mapper.readValue(file, WMV_ClusterStateList.class);
					for(WMV_ClusterState cs : temp.clusters)
						csl.clusters.add(cs);
				}
				catch (Throwable t)
				{
					System.out.println("loadClusterStateLists Throwable t:"+t);
				}
			}
			
			System.out.println("loadClusterStateLists loaded "+csl.clusters.size()+" clusters from "+files.length+" files...");
			return csl;
		}
		return null;
	}


	/**
	 * Load field state from given file path
	 * @param newFilePath File path
	 * @return Field state
	 */
//	public WMV_ClusterStateList loadClusterStateList(String newFilePath)		// Testing
//	{
//		String filePath = newFilePath;
//
//		final ObjectMapper mapper = JsonFactory.create();
//		final File file;
//		try {
////		    file = File.createTempFile("json", "temp.json");    // Use temp file
//			file = new File(filePath);
//			WMV_ClusterStateList csl = mapper.readValue(file, WMV_ClusterStateList.class);
//			
//			return csl;
//		}
//		catch (Throwable t)
//		{
//			System.out.println("loadClusterStateList Throwable t:"+t);
//		}
//		return null;
//	}

	/**
	 * Load field state from given file path
	 * @param newFilePath File path
	 * @return Field state
	 */
	public WMV_ImageStateList loadImageStateLists(String directoryPath)		// Testing
	{
		File dir = new File(directoryPath);
		WMV_ImageStateList csl = new WMV_ImageStateList();
		csl.images = new ArrayList<WMV_ImageState>();
		
		if(dir.exists())
		{
			File[] files = dir.listFiles();
			for(int i = 0; i<files.length; i++)
			{
				String filePath = files[i].getAbsolutePath();

				final ObjectMapper mapper = JsonFactory.create();
				final File file;
				try {
//					System.out.println("loadClusterStateLists()...  filePath: "+filePath);
					file = new File(filePath);
					WMV_ImageStateList temp = mapper.readValue(file, WMV_ImageStateList.class);
					for(WMV_ImageState cs : temp.images)
						csl.images.add(cs);
				}
				catch (Throwable t)
				{
					System.out.println("loadImageStateLists Throwable t:"+t);
				}
			}
			
			System.out.println("loadImageStateLists loaded "+csl.images.size()+" images from "+files.length+" files...");
			return csl;
		}
		return null;
	}

//	/**
//	 * Load field state from given file path
//	 * @param newFilePath File path
//	 * @return Field state
//	 */
//	public WMV_ImageStateList loadImageStateList(String newFilePath)		// Testing
//	{
//		String filePath = newFilePath;
//
//		final ObjectMapper mapper = JsonFactory.create();
//		final File file;
//		try {
////		    file = File.createTempFile("json", "temp.json");    // Use temp file
//			file = new File(filePath);
//			WMV_ImageStateList isl = mapper.readValue(file, WMV_ImageStateList.class);
//			
//			return isl;
//		}
//		catch (Throwable t)
//		{
//			System.out.println("loadImageStateList Throwable t:"+t);
//		}
//		return null;
//	}

	/**
	 * Load field state from given file path
	 * @param newFilePath File path
	 * @return Field state
	 */
	public WMV_PanoramaStateList loadPanoramaStateList(String newFilePath)		// Testing
	{
		String filePath = newFilePath;

		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
//		    file = File.createTempFile("json", "temp.json");    // Use temp file
			file = new File(filePath);
			WMV_PanoramaStateList psl = mapper.readValue(file, WMV_PanoramaStateList.class);
			
			return psl;
		}
		catch (Throwable t)
		{
			System.out.println("loadPanoramaStateList Throwable t:"+t);
		}
		return null;
	}

	/**
	 * Load field state from given file path
	 * @param newFilePath File path
	 * @return Field state
	 */
	public WMV_VideoStateList loadVideoStateList(String newFilePath)		// Testing
	{
		String filePath = newFilePath;

		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
//		    file = File.createTempFile("json", "temp.json");    // Use temp file
			file = new File(filePath);
			WMV_VideoStateList vsl = mapper.readValue(file, WMV_VideoStateList.class);
			
			return vsl;
		}
		catch (Throwable t)
		{
			System.out.println("loadVideoStateList Throwable t:"+t);
		}
		return null;
	}

	/**
	 * Load field state from given file path
	 * @param newFilePath File path
	 * @return Field state
	 */
	public WMV_SoundStateList loadSoundStateList(String newFilePath)		// Testing
	{
		String filePath = newFilePath;

		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
//		    file = File.createTempFile("json", "temp.json");    // Use temp file
			file = new File(filePath);
			WMV_SoundStateList ssl = mapper.readValue(file, WMV_SoundStateList.class);
			
			return ssl;
		}
		catch (Throwable t)
		{
			System.out.println("loadSoundStateList Throwable t:"+t);
		}
		return null;
	}

	private void saveJson(Object object, Type type, String fileName) 
	{
	  File file = new File(fileName);

	  OutputStream outputStream = null;
	  GsonBuilder gsonBuilder = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting();
//	  gsonBuilder.registerTypeAdapter(object.getClass(), new Gson());
	  Gson gson = gsonBuilder.create();
//	  Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting()
//			    .create();
	  
	  try {
	    outputStream = new FileOutputStream(file);
	    BufferedWriter bufferedWriter;
	    bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

	    gson.toJson(object, type, bufferedWriter);
	    bufferedWriter.close();
	  } 
	  catch (FileNotFoundException e) {
	    e.printStackTrace();
	    System.out.println("FileNotFoundException e:"+e);
	  } 
	  catch (IOException e) {
	    e.printStackTrace();
	    System.out.println("IOException e:"+e);
	  } 
	  finally {
	    if (outputStream != null) {
	      try {
	        outputStream.flush();
	        outputStream.close();
	      } 
	      catch (IOException e) {
	    	  System.out.println("IOException e:"+e);
	      }
	    }
	  }
	}
}
