package multimediaLocator;

import java.io.File;
import java.util.ArrayList;

import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;

/**************
 * Represents a multimedia library
 * @author davidgordon
 */
public class ML_Library 
{
	private String libraryFolder;								// Filepath for library folder 
	private ArrayList<String> folders;							// Directory paths for each field in library
	public ArrayList<String> mediaFolders;						// Directory paths for media folders added to library

	/**
	 * Constructor for media library
	 * @param newLibraryFolder Library folder
	 */
	ML_Library(String newLibraryFolder)
	{
		folders = new ArrayList<String>();
		mediaFolders = new ArrayList<String>();
		setLibraryFolder( newLibraryFolder );
	}
	
	/**
	 * Reset to default settings
	 */
	public void reset()
	{
		folders = new ArrayList<String>();
		mediaFolders = new ArrayList<String>();
	}

	/**
	 * Create new library in destination folder from given media folders
	 * @param ml Parent app
	 * @param mediaFolders List of media folder(s)
	 * @param libraryFolder Destination folder for library
	 */
	public boolean createNewLibrary(MultimediaLocator ml, ArrayList<String> mediaFolders)
	{
		System.out.println("createNewLibrary()... mediaFolders.size():"+mediaFolders.size());
		
		String destination = getLibraryFolder();
		File destinationFile = new File(destination);
		if(!destinationFile.exists()) destinationFile.mkdir();
		
		String fieldFolder = destination + "/field";
		File fieldFolderFile = new File(fieldFolder);
		if(!fieldFolderFile.exists()) fieldFolderFile.mkdir();

		System.out.println(">>> getLibraryFolder():"+getLibraryFolder()+" fieldFolder:"+fieldFolder);
		if(mediaFolders.size() == 0)
		{
			System.out.println("Error mediaFolders.size() == 0!");
		}
		else
		{
			for(String mediaFolder : mediaFolders)
			{
				boolean success = sortAndCopyMedia(mediaFolder, fieldFolder);
				
				if(success)
				{
					String smallImagesFolder = fieldFolder + "/small_images";
					String largeImagesFolder = fieldFolder + "/large_images";
					ArrayList<String> movedFiles = getFilesInDirectory(smallImagesFolder);
					
					for(String fs : movedFiles)
					{
						String filePath = smallImagesFolder + "/"+ fs;
						File file = new File(filePath);
						File largeImagesFolderFile = new File(largeImagesFolder);
						
						WMV_ImageMetadata iMetadata = ml.metadata.loadImageMetadata(file, "America/Los_Angeles");
						if(iMetadata == null)
						{
							System.out.println("Library.createNewLibrary()... iMetadata is NULL for image "+fs+"!");
						}
						else
						{
							if(iMetadata.cameraModel != 2)		// Check that image isn't a Theta S panorama
							{
								if(!iMetadata.software.equals("Occipital 360 Panorama"))		// -- Check this!!
								{
									if(iMetadata.imageWidth > 640)
									{
										System.out.println("Library.createNewLibrary()... Image larger than 640 px: "+fs);
										if(!largeImagesFolderFile.exists())
											largeImagesFolderFile.mkdir();
										copyFile(filePath, largeImagesFolder);						// Import full size image to large_images
										ml.world.utilities.shrinkImage(filePath, fieldFolder);		// Shrink existing image in small_images
									}
									else if(iMetadata.imageWidth < 640)
									{
										System.out.println("ERROR in createNewLibrary()... Image smaller than 640 px: "+fs);
									}
								}
							}
//							else
//							{
//								System.out.println("Library.createNewLibrary()... Verified image width for image:"+fs);
//							}
						}
					}
				}
				else
				{
					System.out.println("ERROR creating new library! Failed to copy media folder: "+mediaFolder);
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Rename field folder
	 * @param field Field to rename
	 * @param newName New name to attempt
	 * @return Whether operation succeeded
	 */
	public boolean renameFieldFolder(WMV_Field field, String newName)
	{
		field.setName(newName);
		String fieldFolderPath = getLibraryFolder() + "/" + field.getName();
		File fieldFolderFile = new File(fieldFolderPath);
		String newFilePath = getLibraryFolder() + "/" + newName;
		File newNameFile = new File(newFilePath);
		boolean success = false;

		if(!newNameFile.exists())
			success = fieldFolderFile.renameTo(newNameFile);

		if(!success)
		{
			System.out.println("Failed at renaming to :"+newFilePath+"... will try appending numbers...");
			boolean found = false;
			int count = 2;
			while(!found)				// Append count to file name until non-duplicate name found
			{
				String path = getLibraryFolder() + "/" + newName + "_" + String.valueOf(count);
				newNameFile = new File(path);
				if(!newNameFile.exists()) found = true;
				count++;
			}
			success = fieldFolderFile.renameTo(newNameFile);
		}
		
		return success;
	}
	
	/**
	 * Move matching media in source field to target
	 * @param source Source field
	 * @param target Target field
	 */
	public void moveFieldMediaFiles(WMV_Field source, WMV_Field target)								// Move media into new field folders
	{
		String name = target.getName();
		if(folders.contains(name))				// Check for duplicate folder name
		{
			System.out.println("folders contains name:"+name);
			System.out.println("--> folders:");
			for(String s : folders)
				System.out.println("> "+s);
				
			boolean success = false;
			int count = 2;
			while(!success)
			{
				name = target.getName() + "_" + String.valueOf(count);
				if(!folders.contains(name))
				{
					folders.add(name);
					success = true;
				}
			}
		}
		else folders.add(name);
		
		System.out.println("Library.moveFieldMedia()... Added new library folder: "+name +" image count:"+target.getImages().size());

		String fieldFolder = name;
		String fieldFolderPath = getLibraryFolder() + "/" + fieldFolder;
		File fieldFolderFile = new File(fieldFolderPath);
		if(!fieldFolderFile.exists()) fieldFolderFile.mkdir();

		System.out.println(">>> Copying / deleting media... fieldFolderPath:"+fieldFolderPath+" getLibraryFolder():"+getLibraryFolder());

		for(WMV_Image img : target.getImages())
		{
			String filePath = img.getFilePath();
			String destination = fieldFolderPath + "/small_images/";
			File imagesFolder = new File(destination);
			if(!imagesFolder.exists())
				imagesFolder.mkdir();

			moveFile(filePath, destination);
			
			String imgPath = img.getFilePath();
			String[] parts = imgPath.split("/");
			parts[parts.length-3] = target.getName();
			imgPath = parts[0];
			for(int i=1; i<parts.length; i++)
				imgPath = imgPath + "/" + parts[i];
			System.out.println("Library.moveFieldMediaFiles()... Will set image path to:"+imgPath);
			img.setFilePath(imgPath);
//			copyFile(filePath, destination);
//			deleteFile(filePath);
		}
		for(WMV_Panorama pano : target.getPanoramas())
		{
			String filePath = pano.getFilePath();
			String destination = fieldFolderPath + "/panoramas/";
			File panoramasFolder = new File(destination);
			if(!panoramasFolder.exists())
				panoramasFolder.mkdir();

			moveFile(filePath, destination);
			
			String panoPath = pano.getFilePath();
			String[] parts = panoPath.split("/");
			parts[parts.length-3] = target.getName();
			panoPath = parts[0];
			for(int i=1; i<parts.length; i++)
				panoPath = panoPath + "/" + parts[i];
			System.out.println("Library.moveFieldMediaFiles()... Will set panorama path to:"+panoPath);
			pano.setFilePath(panoPath);
		}
		for(WMV_Video vid : target.getVideos())
		{
			String filePath = vid.getFilePath();
			String destination = fieldFolderPath + "/small_videos/";
			File videosFolder = new File(destination);
			if(!videosFolder.exists())
				videosFolder.mkdir();

			moveFile(filePath, destination);
			
			String vidPath = vid.getFilePath();
			String[] parts = vidPath.split("/");
			parts[parts.length-3] = target.getName();
			vidPath = parts[0];
			for(int i=1; i<parts.length; i++)
				vidPath = vidPath + "/" + parts[i];
			System.out.println("Library.moveFieldMediaFiles()... Will set video path to:"+vidPath);
			vid.setFilePath(vidPath);
		}
		for(WMV_Sound snd : target.getSounds())
		{
			String filePath = snd.getFilePath();
			String destination = fieldFolderPath + "/sounds/";
			File soundsFolder = new File(destination);
			if(!soundsFolder.exists())
				soundsFolder.mkdir();

			moveFile(filePath, destination);
			
			String sndPath = snd.getFilePath();
			String[] parts = sndPath.split("/");
			parts[parts.length-3] = target.getName();
			sndPath = parts[0];
			for(int i=1; i<parts.length; i++)
				sndPath = sndPath + "/" + parts[i];
			System.out.println("Library.moveFieldMediaFiles()... Will set sound path to:"+sndPath);
			snd.setFilePath(sndPath);
		}
	}
	
	/**
	 * Copy media files in source folder to appropriate folders in given destination folder
	 * @param sourceFolder Source folder
	 * @param destFolder Destination folder
	 * @return
	 */
	public boolean sortAndCopyMedia(String sourceFolder, String destFolder)
	{
		System.out.println("Library.sortAndCopyMedia()... sourceFolder:"+sourceFolder+" destFolder:"+destFolder);
		
		ArrayList<String> imagePaths = new ArrayList<String>();
		ArrayList<String> videoPaths = new ArrayList<String>();
		ArrayList<String> soundPaths = new ArrayList<String>();
		
		ArrayList<String> files = getFilesInDirectory(sourceFolder);
		
		for(String fs : files)							/* Split file list into lists based on media type */
		{
			File f = new File(sourceFolder + "/" + fs);
			String[] parts = f.getName().split("\\.");
			String end = parts[parts.length-1];
			if(end.equals("jpg") || end.equals("JPG"))
				imagePaths.add(f.getAbsolutePath());
			else if(end.equals("mov") || end.equals("MOV"))
				videoPaths.add(f.getAbsolutePath());
			else if(end.equals("wav") || end.equals("WAV") || end.equals("aiff") || end.equals("AIFF"))
				soundPaths.add(f.getAbsolutePath());
		}
		
		if(imagePaths.size() > 0)
		{
			String destination = destFolder + "/small_images/";
			File imagesFolder = new File(destination);
			if(!imagesFolder.exists())
				imagesFolder.mkdir();

			for(String fs : imagePaths)
				copyFile(fs, destination);
		}

		if(videoPaths.size() > 0)
		{
			String destination = destFolder + "/small_videos/";
			File videosFolder = new File(destination);
			if(!videosFolder.exists())
				videosFolder.mkdir();

			for(String fs : videoPaths)
				copyFile(fs, destination);
		}
		
		if(soundPaths.size() > 0)
		{
			String destination = destFolder + "/sounds/";
			File soundsFolder = new File(destination);
			if(!soundsFolder.exists())
				soundsFolder.mkdir();

			for(String fs : soundPaths)
				copyFile(fs, destination);
		}
		
		return true;
	}

	/**
	 * Copy file to destination
	 * @param filePath Absolute path to file 
	 * @param destination Destination path
	 * @return Whether successful
	 */
	public boolean moveFile(String filePath, String destination)
	{
		WMV_Command commandExecutor;
		ArrayList<String> command = new ArrayList<String>();
		command.add("mv");
		command.add(filePath);
		command.add(destination);
		
//		System.out.println("copyFile()... command.toString():"+command.toString());
//		cp -a /source/. /dest/
//		cp /home/usr/dir/{file1,file2,file3,file4} /home/usr/destination/

		commandExecutor = new WMV_Command("", command);
		try {
			int result = commandExecutor.execute();

//			System.out.println("... Copying result ..."+result);
			return true;
		}
		catch(Throwable t)
		{
			System.out.println("Throwable t while copying video files:"+t);
			return false;
		}	
	}
	
	/**
	 * Copy file to destination
	 * @param filePath Absolute path to file 
	 * @param destination Destination path
	 * @return Whether successful
	 */
	public boolean copyFile(String filePath, String destination)
	{
		WMV_Command commandExecutor;
		ArrayList<String> command = new ArrayList<String>();
		command.add("cp");
		command.add("-a");		// Improved recursive option that preserves all file attributes, and also preserve symlinks.
		command.add(filePath);
		command.add(destination);
		
//		System.out.println("copyFile()... command.toString():"+command.toString());
//		cp -a /source/. /dest/
//		cp /home/usr/dir/{file1,file2,file3,file4} /home/usr/destination/

		commandExecutor = new WMV_Command("", command);
		try {
			int result = commandExecutor.execute();

//			System.out.println("... Copying result ..."+result);
			return true;
		}
		catch(Throwable t)
		{
			System.out.println("Throwable t while copying video files:"+t);
			return false;
		}	
	}
	
	/**
	 * Delete file 
	 * @param filePath Absolute path to file 
	 * @return Whether successful
	 */
	public boolean deleteFile(String filePath)
	{
		File file = new File(filePath);
		if(file.exists())
		{
//			System.out.println("deleteFile()... Would delete:"+filePath);
//			file.delete();
			return true;
		}
		
		return false;
	}
	
	private ArrayList<String> getFilesInDirectory(String sourceFolder)
	{
		System.out.println("getFilesInDirectory(): sourceFolder:"+sourceFolder);
		ArrayList<String> files = new ArrayList<String>();
		WMV_Command commandExecutor;
		ArrayList<String> command = new ArrayList<String>();
		command.add("ls");
		commandExecutor = new WMV_Command(sourceFolder, command);
		try {
			int result = commandExecutor.execute();

			// Get the output from the command
			StringBuilder stdout = commandExecutor.getStandardOutput();
//			StringBuilder stderr = commandExecutor.getStandardError();

			String out = stdout.toString();
			String[] parts = out.split("\n");
			for (int i=0; i<parts.length; i++)
				files.add(parts[i]);
		}
		catch(Throwable t)
		{
			System.out.println("createNewLibrary()... Throwable t while getting folderString file list:"+t);
//			return false;
		}
		
		return files;
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

	public String getFolder(int folderIdx)
	{
		return folders.get(folderIdx);
	}

	public String getDataFolder(int fieldID)
	{
		return getLibraryFolder() + "/" + folders.get(fieldID) + "/data/";
	}

	public void saveWorldSettings(WMV_WorldSettings settings, String filePath)		
	{
//		String filePath = filePath;

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

	public void saveWorldState(WMV_WorldState state, String filePath)		
	{
//		String filePath = filePath;

		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
//			file = File.createTempFile("json", "temp.json");    // Use temp file
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

	public void saveViewerSettings(WMV_ViewerSettings settings, String filePath)		
	{
//		String filePath = filePath;

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

	public void saveViewerState(WMV_ViewerState state, String filePath)		// Testing
	{
//		String filePath = filePath;

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

	/**
	 * Save current field state
	 * @param fState Field state 
	 * @param filePath File path
	 */
	public void saveFieldState(WMV_FieldState fState, String filePath)
	{
		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
			file = new File(filePath);
			mapper.writeValue(file, fState);    // Write object to file
		}
		catch (Throwable t)
		{
			System.out.println("saveFieldData Throwable t:"+t);
		}
	}

	public void saveClusterStateList(WMV_ClusterStateList csl, String filePath)
	{
		if(csl.clusters.size() <= 50)
		{
			final ObjectMapper mapper = JsonFactory.create();
			final File file;
			try {
				file = new File(filePath);
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
					if(idx < csl.clusters.size())
						temp.add(csl.clusters.get(idx));
					else
						break;
				}

				WMV_ClusterStateList tempCsl = new WMV_ClusterStateList();
				tempCsl.setClusters(temp);

				final ObjectMapper mapper = JsonFactory.create();
				final File file;
				try {
					file = new File(filePath.replaceAll(".json", "_"+((Math.round(i/50)+1)+".json")));
					mapper.writeValue(file, tempCsl);    // Write object to file
				}
				catch (Throwable t)
				{
					System.out.println("saveClusterStateList Throwable t:"+t+" i:"+i+"...");
				}
			}
		}
	}

	public void saveImageStateList(WMV_ImageStateList isl, String filePath)
	{
		if(isl.images.size() <= 100)
		{
			final ObjectMapper mapper = JsonFactory.create();
			final File file;
			try {
				file = new File(filePath);
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
					if(idx < isl.images.size())
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
					file = new File(filePath.replaceAll(".json", "_"+((Math.round(i/1000)+1)+".json")));
					mapper.writeValue(file, tempIsl);    // Write object to file
				}
				catch (Throwable t)
				{
					System.out.println("saveImageStateList Throwable t:"+t+" i:"+i+"...");
				}
			}
		}
	}

	public void savePanoramaStateList(WMV_PanoramaStateList psl, String filePath)
	{
		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
			file = new File(filePath);
			mapper.writeValue(file, psl);    // Write object to file
		}
		catch (Throwable t)
		{
			System.out.println("saveClusterStateList Throwable t:"+t);
		}
	}

	public void saveVideoStateList(WMV_VideoStateList vsl, String filePath)
	{
		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
			file = new File(filePath);
			mapper.writeValue(file, vsl);    // Write object to file
		}
		catch (Throwable t)
		{
			System.out.println("saveClusterStateList Throwable t:"+t);
		}
	}

	public void saveSoundStateList(WMV_SoundStateList ssl, String filePath)
	{
		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
			file = new File(filePath);
			mapper.writeValue(file, ssl);    // Write object to file
		}
		catch (Throwable t)
		{
			System.out.println("saveSoundStateList Throwable t:"+t);
		}
	}

	public WMV_WorldSettings loadWorldSettings(String filePath)		// Testing
	{
//		String filePath = filePath;

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

	public WMV_WorldState loadWorldState(String filePath)		// Testing
	{
//		String filePath = filePath;

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

	public WMV_ViewerSettings loadViewerSettings(String filePath)		// Testing
	{
//		String filePath = filePath;

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

	public WMV_ViewerState loadViewerState(String filePath)		// Testing
	{
//		String filePath = filePath;

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
	 * @param filePath File path
	 * @return Field state
	 */
	public WMV_FieldState loadFieldState(String filePath)		// Testing
	{
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
	 * @param filePath File path
	 * @return Field state
	 */
	public WMV_ClusterStateList loadClusterStateLists(String directoryPath)		// Testing
	{
		File dir = new File(directoryPath);
		WMV_ClusterStateList csl = new WMV_ClusterStateList();
		csl.clusters = new ArrayList<WMV_ClusterState>();

//		System.out.println("loadClusterStateLists... directoryPath:"+directoryPath);
		if(dir.exists())
		{
			File[] files = dir.listFiles();
			for(int i = 0; i<files.length; i++)
			{
				String filePath = files[i].getAbsolutePath();

				final ObjectMapper mapper = JsonFactory.create();
				final File file;
				try {
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

			return csl;
		}
		return null;
	}

	/**
	 * Load image state list from given file path
	 * @param filePath File path
	 * @return Image state list
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

			return csl;
		}
		return null;
	}

	/**
	 * Load panorama state list from given file path
	 * @param filePath File path
	 * @return Field state
	 */
	public WMV_PanoramaStateList loadPanoramaStateList(String filePath)		// Testing
	{
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
	 * Load video state list from given file path
	 * @param filePath File path
	 * @return Field state
	 */
	public WMV_VideoStateList loadVideoStateList(String filePath)		// Testing
	{
		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
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
	 * Load sound state list from given file path
	 * @param filePath File path
	 * @return Sound state list
	 */
	public WMV_SoundStateList loadSoundStateList(String filePath)		// Testing
	{
		final ObjectMapper mapper = JsonFactory.create();
		final File file;
		try {
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

	/**
	 * Set library folder
	 * @param newLibraryFolder New library folder path
	 */
	public void setLibraryFolder(String newLibraryFolder)
	{
		libraryFolder = newLibraryFolder;
	}
}
