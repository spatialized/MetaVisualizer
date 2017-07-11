package main.java.com.entoptic.multimediaLocator;

//import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Movie;

import org.homeunix.thecave.moss.util.image.ExifToolWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
//import com.sun.tools.javac.util.Paths;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
//import java.util.prefs.BackingStoreException;
//import java.util.prefs.Preferences;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**************
 * Class for extracting metadata and adding media to field 
 * @author davidgordon
 */
class WMV_Metadata
{
	/* Classes */
	MultimediaLocator ml; 											// Parent app
	WMV_Utilities u;												// Utility class
	ML_DebugSettings debug;									// Debug settings
	
	public File exiftoolFile;										// File for ExifTool executable

	/* File System */
	public String library = "";
	public String largeImageFolder = "", smallImageFolder = "";
	public String panoramaFolder = "";
	public String largeVideoFolder = "", smallVideoFolder = "";				// File path for media folders
	public String soundFolder = "";
	public String gpsTrackFolder = "";
	public String dataFolder = "";
	
	public File largeImageFolderFile = null, smallImageFolderFile = null, panoramaFolderFile = null, // Folders containing the media 
				largeVideoFolderFile = null, smallVideoFolderFile = null, soundFolderFile = null;
	public File gpsTrackFolderFile = null, dataFolderFile = null;
	
	public boolean largeImageFolderFound = false, smallImageFolderFound = false;
	public boolean panoramaFolderFound = false;
	public boolean largeVideoFolderFound = false, smallVideoFolderFound = false; 	
	public boolean soundFolderFound = false;
	
	public boolean gpsTrackFolderFound = false;
	public boolean dataFolderFound = false; 	
	
	public File[] smallImageFiles = null, imageFiles = null, panoramaFiles = null, // Temp arrays to store media files
				  smallVideoFiles = null, largeVideoFiles = null, soundFiles = null, gpsTrackFiles = null, dataFiles = null;								

	public boolean smallImageFilesFound = false;
	public boolean largeImageFilesFound = false;
	public boolean panoramaFilesFound = false;
	public boolean largeVideoFilesFound = false, smallVideoFilesFound = false;
	public boolean soundFilesFound = false;
	public boolean gpsTrackFilesFound = false;
	private boolean dataFilesValidFormat = false;

	private int iCount = 0, pCount = 0, vCount = 0, sCount = 0;							// Media count by type 

	/**
	 * Constructor for metadata loader
	 * @param parent Parent App
	 * @param newDebugSettings Debug settings
	 */
	public WMV_Metadata( MultimediaLocator parent, ML_DebugSettings newDebugSettings )
	{
		ml = parent;
		u = new WMV_Utilities();
		debug = newDebugSettings;
		
//		loadExiftoolPath();
	}
	
	/**
	 * Reset metadata loader to original state
	 */
	public void reset()
	{
		library = "";
		
		// Media folder file paths
		largeImageFolder = ""; smallImageFolder = "";
		panoramaFolder = "";
		largeVideoFolder = ""; smallVideoFolder = "";	
		soundFolder = "";
		gpsTrackFolder = "";
		dataFolder = "";
		
		smallImageFolderFile = null; largeImageFolderFile = null; 
		panoramaFolderFile = null;		
		
		// Media folders 
		smallVideoFolderFile = null; largeVideoFolderFile = null; 
		soundFolderFile = null;
		gpsTrackFolderFile = null;
		dataFolderFile = null;
		
		
		smallImageFolderFound = false; largeImageFolderFound = false; 
		panoramaFolderFound = false;
		smallVideoFolderFound = false; largeVideoFolderFound = false; 
		soundFolderFound = false;
		
		gpsTrackFolderFound = false;
		dataFolderFound = false; 	
		
		smallImageFiles = null;
		imageFiles = null;
		panoramaFiles = null;
		
		// Temp arrays to store media files
		smallVideoFiles = null; largeVideoFiles = null;
		soundFiles = null;
		gpsTrackFiles = null;
		dataFiles = null;								

		smallImageFilesFound = false; largeImageFilesFound = false;
		panoramaFilesFound = false;
		smallVideoFilesFound = false; largeVideoFilesFound = false;
		soundFilesFound = false;
		gpsTrackFilesFound = false;
		dataFilesValidFormat = false;

		// Media counts by type
		iCount = 0;
		pCount = 0;
		vCount = 0;
		sCount = 0;							 
	}

	/**
	 * Load metadata from a library for a field 
	 * @param f Field to load metadata for
	 * @param libraryFolder Library folder (correctly formatted, e.g. folders small_images, small_videos, data)
	 * @return Simulation state if one was saved, otherwise null
	 */
	public boolean load(WMV_Field f, String libraryFolder)
	{
		reset();						// Added 6-22-17
		
		library = libraryFolder;
		String fieldPath = f.getName();

		if(debug.metadata) ml.systemMessage("Metadata.load()... Will load files from:"+library+" at fieldPath:"+fieldPath);

		loadImageFolders(fieldPath); 	// Load image + panorama folder(s)
		if(panoramaFolderFound) loadPanoramas(fieldPath);
		if(largeImageFolderFound || smallImageFolderFound) loadImageFiles(fieldPath);			// Load image + panorama file names
		
		loadVideoFolders(fieldPath); 	// Load video folder
		if(largeVideoFolderFound || smallVideoFolderFound) loadVideoFiles(fieldPath);		// Load video file names

		loadSoundFolder(fieldPath); 	// Load sound folder
		loadSoundFiles(fieldPath);		// Load sound file names

		loadGPSTrackFolder(fieldPath); 	// Load GPS track folder
		loadGPSTrackFiles(fieldPath); 	// Load GPS track files

		loadDataFolder(fieldPath);		// Load media data from disk

		if(dataFilesValidFormat)
		{
			return true;
		}
		else
		{
			iCount = 0; 
			pCount = 0;
			vCount = 0;

			if(smallImageFilesFound) loadImages(f, smallImageFiles);						// Load image metadata 
			if(panoramaFilesFound) loadImages(f, panoramaFiles); 						    // Load panorama metadata
			if(smallVideoFilesFound) loadVideos(f, smallVideoFiles);	 	 				// Load video metadata 
			if(soundFilesFound) loadSounds(f, soundFiles);				 					// Load sound file metadata
		}
		
		return false;
	}

	/**
	 * Load and analyze GPS track file in response to user selection
	 * @param newTrackFile GPS track file
	 */
	public ArrayList<WMV_Waypoint> loadGPSTrack(WMV_Field f, File newTrackFile, int gpsTrackID, WMV_WorldSettings worldSettings) 
	{
		File gpsTrackFile = null;
		String gpsTrackName = "";
		boolean valid = false;
		
		if (newTrackFile == null) 
		{
			ml.systemMessage("Metadata.loadGPSTrack()... newTrackFile is null!");
		} 
		else 
		{
			String input = newTrackFile.getPath();
			gpsTrackName = input;

			if(debug.metadata || debug.gps || debug.viewer)
				ml.systemMessage("Metadata.loadGPSTrack()... Adding GPS Track: " + input);
			
			try
			{
				String[] parts = gpsTrackName.split("/");
				String fileName = parts[parts.length-1];
				
				parts = fileName.split("\\.");

				if(parts[parts.length-1].equals("gpx"))				// Check that it's a GPX file
				{
					gpsTrackFile = new File(gpsTrackName);
					valid = true;
				}
				else
				{
					valid = false;
					ml.systemMessage("Metadata.loadGPSTrack()... Bad GPS Track.. doesn't end in .GPX!:"+input);
				}
			}
			catch (Throwable t)
			{
				ml.systemMessage("Metadata.loadGPSTrack()... ERROR... Throwable: "+t);
			}
		}
		
		if(valid)
			return getGPSTrackFromFile(ml, f, gpsTrackFile, gpsTrackID);
		else 
			return null;
	}
	
	/**
	 * Analyze current GPS track
	 */
	public ArrayList<WMV_Waypoint> getGPSTrackFromFile(MultimediaLocator ml, WMV_Field f, File gpsTrackFile, int gpsTrackID)
	{
		ArrayList<WMV_Waypoint> gpsTrack = new ArrayList<WMV_Waypoint>();			
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(gpsTrackFile);

			doc.getDocumentElement().normalize();

//			System.out.println("\ngetGPSTrackFromFile()... Analyzing GPS Track:"+gpsTrackFile.getName());
//			System.out.println("Root Node:" + doc.getDocumentElement().getNodeName());
//			System.out.println("----");

			NodeList allNodes = doc.getElementsByTagName("*");
			
			int len;
			int count = 0;
			
			len = allNodes.getLength();
			
//			for (int h=0; h < 5; h++)												// Iterate through each item in .gpx XML file
//			{
//				Element e;
//				e = (Element)allNodes.item(h);										// Location
////				System.out.println("Node "+h+" is "+e.getTagName() + ":");
//			}
			for (int h=4; h < len; h+=3)												// Iterate through each item in .gpx XML file
			{
				NamedNodeMap locationNodeMap;
				Element locationVal, elevationVal, timeVal;

				locationVal = (Element)allNodes.item(h);							// Location
				elevationVal = (Element)allNodes.item(h+1);							// Elevation
				timeVal = (Element)allNodes.item(h+2);								// Time
//				System.out.println("Node "+h+" Start ---> "+locationVal.getTagName() + ":");

				/* Parse Location */
				locationNodeMap = locationVal.getAttributes();

				Node latitudeVal, longitudeVal;
				latitudeVal = locationNodeMap.item(0);
				longitudeVal = locationNodeMap.item(1);

				float fLat = Float.parseFloat(latitudeVal.getNodeValue());
				float fLong = Float.parseFloat(longitudeVal.getNodeValue());
				float latitude = fLat;
				float longitude = fLong;
				
				float altitude = Float.parseFloat(elevationVal.getTextContent());
				
				String latitudeRef = ml.world.utilities.getLatitudeRefFromDecimal( fLat );
				String longitudeRef = ml.world.utilities.getLongitudeRefFromDecimal( fLong );

				/* Parse Node Date */
				String dateTimeStr = timeVal.getTextContent(); 			// Ex. string: 2016-05-01T23:55:33Z   <time>2017-02-05T23:31:23Z</time>
				
				ZonedDateTime zoned = ml.world.utilities.parseUTCDateTimeString(dateTimeStr, f.getTimeZoneID());	// Parse node time in UTC 
				WMV_Time zonedTime = new WMV_Time();
				zonedTime.initialize( zoned, "", count, 0, -1, f.getTimeZoneID() );

				PVector gpsLocation = new PVector(longitude, latitude);
				PVector captureLocation = ml.world.utilities.getCaptureLocationFromGPSAndAltitude(gpsLocation, altitude, longitudeRef, latitudeRef, f.getModel());
				
//				System.out.println("Metadata.getGPSTrackFromFile()... captureLocation x:"+captureLocation.x+" captureLocation.y:"+captureLocation.y+" captureLocation.z:"+captureLocation.z);
				
				WMV_Waypoint wp = new WMV_Waypoint(count, -1, gpsTrackID, captureLocation, gpsLocation, altitude, zonedTime);		// Convert GPS track node to Waypoint
				gpsTrack.add(wp);																					// Add Waypoint to GPS track
				
				count++;
			}

//			System.out.println("Added "+count+" nodes to gpsTrack...");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return gpsTrack;
	}

	/**
	 * Set image folder paths, check if folders exist
	 * @param fieldPath Field folder path
	 */
	public void loadImageFolders(String fieldPath) 		
	{
		smallImageFolder = library + "/" + fieldPath + "/small_images/";	/* Check for small_images folder */
		largeImageFolder = library + "/" + fieldPath + "/large_images/";				/* Check for images folder */
		panoramaFolder = library + "/" + fieldPath + "/panoramas/";			/* Check for panoramas folder */

		smallImageFolderFile = new File(smallImageFolder);					// Max size 640 x 480 px
		largeImageFolderFile = new File(largeImageFolder);							// No max. size
		panoramaFolderFile = new File(panoramaFolder);						// 2:1 aspect ratio only

		smallImageFolderFound = (smallImageFolderFile.exists() && smallImageFolderFile.isDirectory());			
		largeImageFolderFound = (largeImageFolderFile.exists() && largeImageFolderFile.isDirectory());	
		panoramaFolderFound = (panoramaFolderFile.exists() && panoramaFolderFile.isDirectory());
		
		if(debug.metadata)
		{
			ml.systemMessage("Metadata.loadImageFolders()... smallImageFolder: "+smallImageFolder+" found? "+smallImageFolderFound);
			ml.systemMessage("               				 largeImageFolder: "+largeImageFolder+" found? "+largeImageFolderFound);
			ml.systemMessage("                          	 panoramaFolder: "+panoramaFolder+" found? "+panoramaFolderFound);
		}
	}
	
	/**
	 * Load metadata for folder of videos
	 */
	public void loadVideoFolders(String fieldPath) // Load photos up to limit to load at once, save those over limit to load later
	{
		smallVideoFolder = library + "/" + fieldPath + "/small_videos/";		// Max size 480p
		largeVideoFolder = library + "/" + fieldPath + "/large_videos/";			// Max size 4K
		
		smallVideoFolderFile = new File(smallVideoFolder);
		largeVideoFolderFile = new File(largeVideoFolder);
		
		smallVideoFolderFound = (smallVideoFolderFile.exists() && smallVideoFolderFile.isDirectory());	
		largeVideoFolderFound = (largeVideoFolderFile.exists() && largeVideoFolderFile.isDirectory());

		if(debug.metadata)
		{
			if(smallVideoFolderFound)
				ml.systemMessage("Metadata.loadVideoFolders()... smallVideoFolder: "+smallVideoFolder);
			if(largeVideoFolderFound)
				ml.systemMessage("Metadata.loadVideoFolders()... videoFolder: "+largeVideoFolder);
		}
	}
	
	/**
	 * Load metadata for folder of videos
	 */
	public void loadSoundFolder(String fieldPath) // Load photos up to limit to load at once, save those over limit to load later
	{
		soundFolder = library + "/" + fieldPath + "/sounds/";		// Max width 720 pixels  -- Check this!
		soundFolderFile = new File(soundFolder);
		soundFolderFound = (soundFolderFile.exists() && soundFolderFile.isDirectory());	
		
		if(debug.metadata)
		{
			if(soundFolderFound)
				ml.systemMessage("Metadata.loadSoundFolder()... soundFolder: "+soundFolder);
		}
	}
	
	/**
	 * Load metadata for folder of videos
	 */
	public void loadGPSTrackFolder(String fieldPath) // Load photos up to limit to load at once, save those over limit to load later
	{
		gpsTrackFolder = library  + "/" + fieldPath + "/gps_tracks/";		// Max width 720 pixels  -- Check this!
	}
	
	/**
	 * Load metadata for folder of videos
	 */
	public void loadSoundFiles(String fieldPath) // Load photos up to limit to load at once, save those over limit to load later
	{
		soundFiles = null;

		if(soundFolderFound)				// Check for sound files
		{
			soundFiles = soundFolderFile.listFiles();
			if(soundFiles != null && soundFiles.length > 0)
				soundFilesFound = true;
		}
		
		if (debug.sound)
			ml.systemMessage("Sound Folder:" + soundFolder);
	}
	
	/**
	 * Load metadata for folder of videos
	 * @param fieldPath Field path
	 */
	public void loadGPSTrackFiles(String fieldPath) // Load photos up to limit to load at once, save those over limit to load later
	{
		gpsTrackFolderFile = new File(gpsTrackFolder);
		gpsTrackFolderFound = (gpsTrackFolderFile.exists() && gpsTrackFolderFile.isDirectory());	
		gpsTrackFiles = null;

		if(gpsTrackFolderFound)				// Check for sound files
		{
			gpsTrackFiles = gpsTrackFolderFile.listFiles();
			if(gpsTrackFiles != null && gpsTrackFiles.length > 0)
				gpsTrackFilesFound = true;
		}
		
		if (debug.metadata) ml.systemMessage("GPS Track Folder:" + gpsTrackFolder);
	}
	
	/**
	 * Load GPS tracks from disk and determine locations for sounds in field
	 * @param f Field
	 * @param files Array of GPS track files
	 */
	public ArrayList<ArrayList<WMV_Waypoint>> loadGPSTracks(WMV_Field f)
	{
		File[] files = gpsTrackFiles;
		ArrayList<ArrayList<WMV_Waypoint>> tracks = new ArrayList<ArrayList<WMV_Waypoint>>();
		
		int count = 0;
		if(files != null)
		{
			for(File file : files)
			{
				ArrayList<WMV_Waypoint> gpsTrack = loadGPSTrack(f, file, count, ml.world.settings); 
				tracks.add(gpsTrack);
				count++;
			}
		}
		return tracks;
	}
	
	/**
	 * Set field sound locations from loaded GPS tracks 
	 * @param f Field to load sound locations for
	 * @param soundList Sound list
	 */
	public void setSoundLocationsFromGPSTracks(WMV_Field f, ArrayList<WMV_Sound> soundList)
	{
		if(f.getState().gpsTracks != null)
		{
			for(ArrayList<WMV_Waypoint> track : f.getState().gpsTracks)
			{
				if(debug.metadata && debug.sound)
					ml.systemMessage("Metadata.setSoundLocationsFromGPSTracks()...  Setting sound locations from track...");
				ml.world.utilities.setSoundGPSLocationsFromGPSTrack( f.getSounds(), track, ml.world.getSettings().soundGPSTimeThreshold );
			}
		}
		else
		{
			if(debug.metadata && debug.sound)
				ml.systemMessage("Metadata.setSoundLocationsFromGPSTracks()... No GPS tracks in field #"+f.getID());
		}
	}
	
	/**
	 * Load data folder for field
	 * @param fieldPath Path to field folder
	 */
	public void loadDataFolder(String fieldPath) // Load photos up to limit to load at once, save those over limit to load later
	{
		dataFolder = library + "/" + fieldPath + "/data/";		// Max width 720 pixels  -- Check this!

		dataFolderFile = new File(dataFolder);
		dataFolderFound = (dataFolderFile.exists() && dataFolderFile.isDirectory());	
		dataFiles = null;

		if(dataFolderFound)				// Check for sound files
		{
			dataFiles = dataFolderFile.listFiles();
			if(dataFiles != null && dataFiles.length > 0)
			{
				if(dataFiles[0].getName().equals(".DS_Store"))
				{
					File[] newDataFiles = new File[dataFiles.length-1];
					for(int i=1; i<dataFiles.length; i++)
						newDataFiles[i-1]=dataFiles[i];
					dataFiles = newDataFiles;
				}
				int idx = 0;
				if( dataFiles[idx].getName().equals("ml_library_clusterStates") &&
					dataFiles[++idx].getName().equals("ml_library_fieldState.json") &&
					dataFiles[++idx].getName().equals("ml_library_imageStates") &&
					dataFiles[++idx].getName().equals("ml_library_panoramaStates") &&
					dataFiles[++idx].getName().equals("ml_library_soundStates") &&
					dataFiles[++idx].getName().equals("ml_library_videoStates") &&
					dataFiles[++idx].getName().equals("ml_library_viewerSettings.json") &&
					dataFiles[++idx].getName().equals("ml_library_viewerState.json") &&
					dataFiles[++idx].getName().equals("ml_library_worldSettings.json") &&
					dataFiles[++idx].getName().equals("ml_library_worldState.json")    )
				dataFilesValidFormat = true;
			}
		}
		
		if (debug.ml)
			ml.systemMessage("Data Folder: " + dataFolder + " Valid Format? "+dataFilesValidFormat);
	}
	
	/**
	 * Load image files
	 * @param fieldPath Field folder path
	 */
	public void loadImageFiles(String fieldPath)
	{
		if(debug.metadata) 
			ml.systemMessage("Metadata.loadImageFiles()...");
		
		smallImageFiles = null;
		imageFiles = null;
		
		if(largeImageFolderFound)		// Look for original images and panoramas
		{
			imageFiles = largeImageFolderFile.listFiles();
			if(imageFiles != null && imageFiles.length > 0)
				largeImageFilesFound = true;
		}
		
		if(smallImageFolderFound)								// Found small_images folder
		{
			smallImageFiles = smallImageFolderFile.listFiles();	// Check for files

			if(smallImageFiles != null && smallImageFiles.length > 0)
				smallImageFilesFound = true;
			
			if(smallImageFilesFound)
			{
				if(smallImageFiles.length == 1)
				{
					File f = smallImageFiles[0];
					String check = f.getName();
					if(check.equals(".DS_Store"))
						smallImageFilesFound = false;			/* Only found .DS_Store, ignore it */
				}
				
				if(debug.metadata) 
				{
					if(smallImageFilesFound)
						ml.systemMessage("Metadata.loadImageFiles()... Files found in small_images folder, will use instead of shrinking large images...");
					else
						ml.systemMessage("Metadata.loadImageFiles()... No files found in small_images folder, will shrink large images...");
				}
			}
		}
		
		// If images exist but no small images are found
		if(largeImageFilesFound && !smallImageFilesFound)	// Copy original images to small_images directory and resize
		{
			if(!smallImageFolderFile.exists())
				smallImageFolderFile.mkdir();

			boolean success = u.shrinkImageFolder(largeImageFolder, smallImageFolder);		
			if(success)
			{
				if(debug.metadata) ml.systemMessage("Shrink images successful...");
			}
			else
				if(debug.metadata)  ml.systemMessage("Shrink images failed...");
		}
	}

	/**
	 * Load panorama files
	 * @param fieldPath Field folder path
	 */
	public void loadPanoramas(String fieldPath)
	{
		panoramaFiles = panoramaFolderFile.listFiles();
		if(panoramaFiles != null) 
			if(panoramaFiles.length > 0)
			panoramaFilesFound = true;	
	}
	
	/**
	 * Load video files
	 * @param fieldPath Field folder path
	 */
	public void loadVideoFiles(String fieldPath)
	{
		largeVideoFiles = null;
		if(largeVideoFolderFound)				// Check for large video files if folder found
		{
			largeVideoFiles = largeVideoFolderFile.listFiles();
			if(largeVideoFiles != null) 
				if(largeVideoFiles.length > 0)
					largeVideoFilesFound = true;
		}
		
		smallVideoFiles = null;
		if(smallVideoFolderFound)				// Check for small video files if folder found
		{
			smallVideoFiles = smallVideoFolderFile.listFiles();
			if(smallVideoFiles != null)
				if(smallVideoFiles.length > 0)
					smallVideoFilesFound = true;
		}
		
		if(largeVideoFilesFound && !smallVideoFilesFound)	// If original images exist but no small images were found
		{
			if(!smallVideoFolderFile.exists())
				smallVideoFolderFile.mkdir();
			
			String inputPath = largeVideoFolder;
			String outputPath = smallVideoFolder;
			Process conversionProcess = ml.world.utilities.convertVideos(ml, inputPath, outputPath);
			
			try{								// Copy original videos to small_videos directory and resize	-- Move to ML class
				conversionProcess.waitFor();
			}
			catch(Throwable t)
			{
				ml.systemMessage("Metadata.loadVideoFiles()... ERROR in process.waitFor()... t:"+t);
			}
			
			boolean success = true;		
			if(success)
			{
				if(debug.metadata) ml.systemMessage("Shrink videos successful...");
				smallVideoFiles = smallVideoFolderFile.listFiles();
				if(smallVideoFiles != null) 
					if(smallVideoFiles.length > 0)
						smallVideoFilesFound = true;
			}
			else
				if(debug.metadata)  ml.systemMessage("Shrink videos failed...");
		}
	}
	
	/** 
	 * Read metadata tags from image and panorama files and add 3D media objects to field
	 * @param f Field to load images and panoramas into
	 * @param files File array
	 */
	public boolean loadImages(WMV_Field f, File[] files) 			// Load metadata for a folder of images and add to imgs ArrayList
	{
		int fileCount;
		if(files != null) 
			fileCount = files.length;	 		// File count
		else 
			return false;
		
		for (int currentMedia = 0; currentMedia < fileCount; currentMedia++) 
		{
			File file = files[currentMedia];
			boolean panorama = fileIsPanorama(file);	// Check whether image is a panorama
			if(panorama)
				addPanoramaToField(f, file);
			else
				addImageToField(f, file);
		}
		
		if(largeImageFilesFound && smallImageFilesFound)
			associateOriginalImages(f);

		return true;
	}
	
	/** 
	 * Read metadata tags from video files and add 3D video objects to field
	 * @param f Field to load videos into
	 * @param files File array
	 */
	public boolean loadVideos(WMV_Field f, File[] files) 
	{
		int fileCount;
		if(files != null)
			fileCount = files.length;	 		// File count
		else
			return false;
		
		for (int currentMedia = 0; currentMedia < fileCount; currentMedia++) 
			addVideoToField(f, files[currentMedia]);
		
		if(smallVideoFilesFound && largeVideoFilesFound)
			associateOriginalVideos(f);

		return true;
	}
	
	/**
	 * Associate low-resolution videos with originals
	 * @param f Field containing images
	 */
	private void associateOriginalImages(WMV_Field f)
	{
		if(imageFiles != null)
		{
			for(WMV_Image img : f.getImages())
				for(int i = 0; i<imageFiles.length; i++)
					if(imageFiles[i].getName().equals(img.getName()))
						img.setOriginalPath(imageFiles[i].getAbsolutePath());
		}
		else
		{
			if(ml.debug.metadata || ml.debug.image)
				ml.systemMessage("Metadata.associateOriginalImages()... No original images found in field:"+f.getName());
		}
	}
	
	/**
	 * Associate low-resolution videos with originals
	 * @param f Field containing videos
	 */
	private void associateOriginalVideos(WMV_Field f)
	{
		if(largeVideoFiles != null)
		{
			for(WMV_Video vid : f.getVideos())
				for(int i = 0; i<largeVideoFiles.length; i++)
					if(largeVideoFiles[i].getName().equals(vid.getName()))
						vid.setOriginalPath(largeVideoFiles[i].getAbsolutePath());
		}
		else
		{
			if(ml.debug.metadata || ml.debug.video)
				ml.systemMessage("Metadata.associateOriginalVideos()... No original videos found in field:"+f.getName());
		}
	}
	
	/** 
	 * Read tags from array of video files and create 3D video objects
	 * @param files File array
	 */
	public boolean loadSounds(WMV_Field f, File[] files) 			// Load metadata for a folder of images and add to imgs ArrayList
	{
		int fileCount;
		if(files != null) fileCount = files.length;	 		// File count
		else return false;

		for (int currentMedia = 0; currentMedia < fileCount; currentMedia++) 
			addSoundToField(f, files[currentMedia]);
		
		return true;
	}

	/**
	 * Load metadata and add image to field 
	 * @param f Field to add image to
	 * @param file Image file
	 */
	public void addImageToField(WMV_Field f, File file)
	{
		try 
		{
			WMV_ImageMetadata iMetadata = loadImageMetadata(file, f.getTimeZoneID());
			if(iMetadata.isValid())
			{
				PImage pImg = ml.createImage(0, 0, processing.core.PConstants.RGB);
				f.addImage( new WMV_Image(iCount, pImg, 0, iMetadata ) );
				iCount++;
			}
			else
				ml.systemMessage("Invalid image metadata!  Image:"+iMetadata.name);

		}
		catch (RuntimeException ex) {
			if (debug.metadata) ml.systemMessage("Could not add image! Error: "+ex);
		}
	}
	
	/**
	 * Load metadata and add panorama to field
	 * @param f Field to add panorama to
	 * @param file Panorama file
	 */
	public void addPanoramaToField(WMV_Field f, File file)
	{
		try 
		{
			WMV_PanoramaMetadata pMetadata = loadPanoramaMetadata(file, f.getTimeZoneID());
			if(pMetadata.isValid())
			{
				PImage pImg = ml.createImage(0,0,processing.core.PConstants.RGB);
				f.addPanorama( new WMV_Panorama(pCount, 1, 0.f, null, pImg, pMetadata) );
				pCount++;
			}
			else
				ml.systemMessage("Invalid panorama metadata!  Panorama:"+pMetadata.name);
		}
		catch (RuntimeException ex) {
			if (debug.metadata) ml.systemMessage("Could not add panorama! Error: "+ex);
		}
	}

	/**
	 * Load metadata and add 3D video object to field
	 * @param f Field to add video to
	 * @param file Video file
	 */
	public void addVideoToField(WMV_Field f, File file)
	{
		try 
		{
			WMV_VideoMetadata vMetadata = loadVideoMetadata(file, f.getTimeZoneID());
			if(vMetadata.isValid())
			{
				Movie pMov = new Movie(ml, vMetadata.filePath);
				f.addVideo( new WMV_Video(vCount, pMov, 2, vMetadata) );
				vCount++;
			}
			else
				ml.systemMessage("Invalid video metadata!  Video:"+vMetadata.name);
		}
		catch (Throwable t) {
			if (debug.metadata)
			{
				ml.systemMessage("Could not add video! Error: "+t);
				t.printStackTrace();
				StackTraceElement[] stes = t.getStackTrace();
				if (debug.video)
					for(StackTraceElement ste : stes)
						ml.systemMessage("     : "+ste.toString());
			}
		}	
	}

	/**
	 * Load metadata and add 3D video object to field
	 * @param f Field to add video to
	 * @param file Video file
	 */
	public void addSoundToField(WMV_Field f, File file)
	{
		try 
		{
			WMV_SoundMetadata sMetadata = loadSoundMetadata(f, file, f.getTimeZoneID());
			if(sMetadata.isValid())
			{
				f.addSound( new WMV_Sound (sCount, 3, sMetadata) );
				sCount++;
			}
			else
			{
				ml.systemMessage("Invalid sound metadata!  Sound:"+sMetadata.name);
			}
		}
		catch (Throwable t) {
			if (debug.metadata)
			{
				ml.systemMessage("Could not add sound! Error:: "+t);
			}
		}	
	}

	/**
	 * Load image metadata from disk
	 * @param file Image file
	 * @param timeZoneID Image time zone
	 * @return Image metadata
	 */
	public WMV_ImageMetadata loadImageMetadata(File file, String timeZoneID)
	{
		String sName = file.getName();
		boolean dataMissing = false;
//		boolean brightnessMissing = false, descriptionMissing = false;

		ZonedDateTime zonedDateTime = null;
		
		int iWidth = -1, iHeight = -1;
		float fDirection = 0, fElevation = 0, fRotation = 0, fFocalLength = 0, fOrientation = 0, fSensorSize = 0;
		float fFocusDistance = -1.f;										
		float fBrightness = -1.f;
		int iCameraModel = 0;

		String[] sKeywords = new String[0];	
		
		String sDateTime = null;										// Time
		
		String sLatitude = null, sLongitude = null, sAltitude = null;	// GPS Location
		String sLatitudeRef = null, sLongitudeRef = null;

		String sOrientation = null, sDirection = null;
		String sFocalLength = null, sFocalLength35mm = null;
		String sSoftware = null;
		String sCameraModel = null, sDescription = null;

		PVector gpsLoc = new PVector(0, 0, 0);
		String sFilePath = "";

		Metadata imageMetadata = null;				// For images

//		if(debugSettings.metadata && debugSettings.detailed)
//			ml.systemMessage("Metadata.loadImageMetadata()...  "+sName);

		try {
			imageMetadata = JpegMetadataReader.readMetadata(file);		/* Read metadata with JpegMetadataReader */
		}
		catch (Throwable t) 
		{
			if(debug.metadata && debug.detailed)
				ml.systemMessage("Metadata.loadImageMetadata()...  Throwable:" + t +" name:"+sName+"  file.getAbsolutePath():"+file.getAbsolutePath());
			if(!dataMissing) dataMissing = true;
		}

		/* Set image variables from metadata */
		if (imageMetadata != null) 
		{
			if (debug.metadata && debug.image && debug.detailed) 
			{
				ml.systemMessage("*******************************************************");
				ml.systemMessage("*  Loading Image Metadata: "+sName);
			}

			for (Directory directory : imageMetadata.getDirectories()) 
			{
				for (Tag tag : directory.getTags()) 
				{
					String tagString = tag.toString();		
					String tagName = tag.getTagName();

					if (tag.getTagName().equals("Software")) // Software
					{
						sSoftware = parseSoftware(tagString);
						if (debug.metadata && debug.detailed) 
							ml.systemMessage("Found Software..." + sSoftware);
					}

					if (tagName.equals("Model")) // Model
					{
						sCameraModel = tagString;
						if (debug.metadata && debug.detailed) ml.systemMessage("Found Camera Model..." + sCameraModel);

						try
						{
							iCameraModel = parseCameraModel(sCameraModel);
						}
						catch (Throwable t) // If not, must be only one keyword
						{
							if (debug.metadata) ml.systemMessage("Metadata.loadImageMetadata()... Throwable in camera model / focal length..." + t);
							if(!dataMissing) dataMissing = true;
						}
					}

					if (tagName.equals("Orientation")) 			// Orientation
					{
						sOrientation = tagString;
						if (debug.metadata && debug.detailed && debug.image) 
							ml.systemMessage("Found Orientation..." + sOrientation);
					}
					if (tagName.equals("Date/Time Original")) 	// Original Date/Time
					{
						sDateTime = tagString;
						if (debug.metadata && debug.detailed && debug.image) 
							ml.systemMessage("Found DateTimeOriginal..." + sDateTime);
						String[] parts = sDateTime.split(" - ");
						sDateTime = parts[1];
					}
					if (tagName.equals("GPS Latitude Ref")) 		// Latitude
					{
						sLatitudeRef = tagString;
						if ((debug.metadata && debug.detailed && debug.image) || (debug.gps && debug.detailed) ) 
							ml.systemMessage("Found Latitude Ref..." + sLatitudeRef);
					}
					if (tagName.equals("GPS Longitude Ref"))		// Longitude
					{
						sLongitudeRef = tagString;
						if ((debug.metadata && debug.detailed && debug.image) || (debug.gps && debug.detailed) ) 
							ml.systemMessage("Found Longitude Ref..." + sLongitudeRef);
					}
					if (tagName.equals("GPS Latitude")) 			// Latitude
					{
						sLatitude = tagString;
						if ((debug.metadata && debug.detailed && debug.image) || (debug.gps && debug.detailed) ) 
							ml.systemMessage("Found Latitude..." + sLatitude);
					}
					if (tagName.equals("GPS Longitude")) 		// Longitude
					{
						sLongitude = tagString;
						if ((debug.metadata && debug.detailed && debug.image) || (debug.gps && debug.detailed) ) 
							ml.systemMessage("Found Longitude..." + sLongitude);
					}
					if (tagName.equals("GPS Altitude")) 			// Altitude
					{
						sAltitude = tagString;
						if (debug.metadata && debug.detailed && debug.image) 
							ml.systemMessage("Found Altitude..." + sAltitude);
					}
					if (tagName.equals("Focal Length"))			// Focal Length
					{
						sFocalLength = tagString;
						if (debug.metadata && debug.detailed && debug.image) 
							ml.systemMessage("Found Focal Length..." + sFocalLength);
					}

					if (tagName.equals("Focal Length 35")) 		// Focal Length (35 mm. equivalent)
					{
						sFocalLength35mm = tagString;
						if (debug.metadata && debug.detailed && debug.image) 
							ml.systemMessage("Found Focal Length 35mm Equivalent..." + sFocalLength);
					}
					if (tagName.equals("GPS Img Direction")) 	// Image Direction
					{
						sDirection = tagString;
						if ((debug.metadata && debug.detailed && debug.image) || (debug.gps && debug.detailed) )
							ml.systemMessage("Found GPS Img Direction..." + sDirection);
					}
					if (tagName.equals("Image Description")) 	// Description (for Theodolite app vertical / elevation angles)
					{
						sDescription = tagString;
						String[] parts = tagString.split("Image Description -");
						String input = parts[1];
						parts = input.split("vert_angle_deg=");
						if(parts.length > 1)
						{
							input = parts[1];
							parts = input.split("/");
							fElevation = Float.valueOf(parts[0]);
							input = parts[1];
							parts = input.split("=");
							fRotation = Float.valueOf(parts[1]);
						}
						else
						{
//							descriptionMissing = true;
							if(debug.metadata) ml.systemMessage("Metadata.loadImageMetadata()...  Not a Theodolite image...");
						}

						if (debug.metadata && debug.detailed && debug.image)
							ml.systemMessage("Found Description..." + sDescription);
					}
					
					if (tagName.equals("AFPointsUsed")) // Orientation
					{
//						String afPointsStr = tagString;
//						if (afPointsStr.equals("-"))
//							afPoints[0] = 0;
//						else
//							afPoints = ParseAFPointsArray(afPointsStr);
					}

					if (tagName.equals("Aperture Value")) // Aperture
					{
//						fAperture = 
//						ml.systemMessage("Aperture Value (not recorded)..."+tagString);
					}

					if (tagName.equals("Keywords"))
					{
						if (debug.metadata)
							ml.systemMessage("Keywords: "+tagString);
						
						sKeywords = ParseKeywordArray(tagString);
					}

					if (tagName.equals("Brightness Value")) // Brightness
					{
						fBrightness = parseBrightness(tagString);
//						if(fBrightness == -1.f)
//							brightnessMissing = true;
					}

					if (tagName.equals("Image Width")) // Orientation
						iWidth = ParseWidthOrHeight(tagString);

					if (tagName.equals("Image Height")) // Orientation
						iHeight = ParseWidthOrHeight(tagString);
				}

				sFilePath = file.getPath();
				if(sFilePath == null)
					dataMissing = true;
				else if(sFilePath.equals(""))
					dataMissing = true;

				if (directory.hasErrors()) {
					for (String error : directory.getErrors()) {
						ml.systemMessage("ERROR: " + error);
					}
				}
			}

			try {
				zonedDateTime = parseDateTime(sDateTime, timeZoneID);
			} 
			catch (RuntimeException ex) 
			{
				if (debug.metadata) ml.systemMessage("Error in getting zoned date / time... " + ex);
				if(!dataMissing) dataMissing = true;
			}

			try {
				fFocalLength = parseFocalLength(sFocalLength);
				fSensorSize = parseSensorSize(sFocalLength35mm);		// 29 mm for iPhone 6S+
			} 
			catch (Throwable t) // If not, must be only one keyword
			{
				if (debug.metadata) ml.systemMessage("Throwable in camera model / focal length..." + t);
				if(!dataMissing) dataMissing = true;
			}
			

			try {
				float xCoord, yCoord, zCoord;
				sLongitudeRef = parseLongitudeRef(sLongitudeRef);
				sLatitudeRef = parseLatitudeRef(sLatitudeRef);
				
				if(ml.debug.gps && ml.debug.detailed)
				{
					ml.systemMessage(" Parsed image longitude Ref:"+sLongitudeRef);
					ml.systemMessage(" Parsed image latitude Ref:"+sLatitudeRef);
				}
				
				xCoord = parseLongitude(sLongitude, sLongitudeRef);
				yCoord = parseAltitude(sAltitude);
				zCoord = parseLatitude(sLatitude, sLatitudeRef);

				if (u.isNaN(xCoord) || u.isNaN(yCoord) || u.isNaN(zCoord)) 
				{
					gpsLoc = new PVector(0, 0, 0);
					if(!dataMissing) dataMissing = true;
				}
				else
					gpsLoc = new PVector(xCoord, yCoord, zCoord);
			} 
			
			catch (RuntimeException ex) 
			{
				if (debug.metadata) ml.systemMessage("Error reading image location:" + sName + "  " + ex);

				if(!dataMissing) dataMissing = true;
			}

			try {
				if(sOrientation != null)
					fOrientation = ParseOrientation(sOrientation);
				else
					fOrientation = -1;
				
				if(sDirection != null)
					fDirection = ParseDirection(sDirection);		
				else
				{
					if (debug.metadata) ml.systemMessage("Image fDirection is null! "+sName);
					fDirection = -100000.f;
				}
			} 
			catch (RuntimeException ex) {
				if (debug.metadata)
					ml.systemMessage("Metadata.loadImageMetadata()... Error reading image orientation / direction:" + fOrientation + "  " + fDirection + "  " + ex);
				if(!dataMissing) dataMissing = true;
			}
		}
		
		/* Add this media object to field */
		try 
		{
			if(!dataMissing)
			{
				return new WMV_ImageMetadata( sName, sFilePath, gpsLoc, zonedDateTime, sDateTime, timeZoneID, fDirection, fFocalLength, fOrientation, 
						fElevation, fRotation, fFocusDistance, fSensorSize, iCameraModel, iWidth, iHeight, fBrightness, sKeywords, sSoftware,
						sLongitudeRef, sLatitudeRef );
			}
			else
				if(debug.metadata) ml.systemMessage("Metadata.loadImageMetadata()... Data missing! Excluded image:"+sName);
		}
		catch (RuntimeException ex) {
			if (debug.metadata) ml.systemMessage("Metadata.loadImageMetadata()... Could not add image! Error: "+ex);
		}

		return null;
	}

	/**
	 * Load panorama metadata from disk
	 * @param file File to read metadata from
	 * @param timeZoneID Panorama time zone ID
	 * @return Panorama metadata
	 */
	public WMV_PanoramaMetadata loadPanoramaMetadata(File file, String timeZoneID)
	{
		String sName = file.getName();
		boolean dataMissing = false;
		boolean brightnessMissing = false, descriptionMissing = false;

		ZonedDateTime zonedDateTime = null;
		
		int iWidth = -1, iHeight = -1;
		int iCameraModel = 0;
		float fDirection = 0, fOrientation = 0, fBrightness = -1.f;
//		float fDirection = 0, fElevation = 0, fRotation = 0, fFocalLength = 0, fOrientation = 0, fSensorSize = 0, fFocusDistance = -1.f;										

		String[] sKeywords = new String[0];	
		
		String sDateTime = null;										// Time
		
		String sLatitude = null, sLongitude = null, sAltitude = null;	// GPS Location
		String sLatitudeRef = null, sLongitudeRef = null;
		
		String sOrientation = null, sDirection = null;					// Orientation / Direction
		String sFocalLength = null; // sFocalLength35mm = null;
		String sSoftware = null;
		String sCameraModel = null, sDescription = null;

		PVector gpsLoc = new PVector(0, 0, 0);
		String sFilePath = "";

		Metadata panoramaMetadata = null;				// For images

		if (debug.metadata && debug.panorama && debug.detailed) 
		{
			ml.systemMessage("*******************************************************");
			ml.systemMessage("*  Loading Panorama Metadata: "+sName);
		}

		try {
			panoramaMetadata = JpegMetadataReader.readMetadata(file);		/* Read metadata with JpegMetadataReader */
		}
		catch (Throwable t) 
		{
			if(debug.metadata || debug.panorama) ml.systemMessage("Metadata.loadPanoramaMetadata()... Throwable:" + t);
			if(!dataMissing) dataMissing = true;
		}

		/* Set panorama variables from metadata */
		if (panoramaMetadata != null) 
		{
			for (Directory directory : panoramaMetadata.getDirectories()) 
			{
				for (Tag tag : directory.getTags()) 
				{
					String tagString = tag.toString();		
					String tagName = tag.getTagName();

					if (tag.getTagName().equals("Software")) // Software
					{
						sSoftware = tagString;
						if (debug.metadata && debug.detailed && debug.panorama) 
							ml.systemMessage("Found Software..." + sSoftware);
					}

					if (tagName.equals("Model")) // Model
					{
						sCameraModel = tagString;
						if (debug.metadata && debug.detailed && debug.panorama)
							ml.systemMessage("Found Camera Model..." + sCameraModel);

						try
						{
							iCameraModel = parseCameraModel(sCameraModel);
							if (debug.metadata && debug.detailed && debug.panorama) 
								ml.systemMessage("Set iCameraModel:" + iCameraModel);
						}
						catch (Throwable t) // If not, must be only one keyword
						{
							if (debug.metadata || debug.panorama) ml.systemMessage("Metadata.loadPanoramaMetadata()... Throwable in camera model..." + t);
							if(!dataMissing) dataMissing = true;
						}
					}

					if (tagName.equals("Orientation")) 			// Orientation		-- Not needed for panorama?
					{
						sOrientation = tagString;
						if (debug.metadata && debug.detailed && debug.panorama) 
							ml.systemMessage("Found Orientation..." + sOrientation);
					}
					if (tagName.equals("Date/Time Original")) 	// Date/Time
					{
						sDateTime = tagString;
						if (debug.metadata && debug.detailed && debug.panorama) 
							ml.systemMessage("Found DateTimeOriginal..." + sDateTime);
						String[] parts = sDateTime.split(" - ");
						sDateTime = parts[1];
					}
					if (tagName.equals("GPS Latitude Ref")) // Latitude
					{
						sLatitudeRef = tagString;
						if ((debug.metadata && debug.detailed && debug.panorama) || (debug.gps && debug.detailed) ) 
							ml.systemMessage("Found Latitude Ref..." + sLatitudeRef);
					}
					if (tagName.equals("GPS Longitude Ref")) // Longitude
					{
						sLongitudeRef = tagString;
						if ((debug.metadata && debug.detailed && debug.panorama) || (debug.gps && debug.detailed) ) 
							ml.systemMessage("Found Longitude Ref..." + sLongitudeRef);
					}
					if (tagName.equals("GPS Latitude")) // Latitude
					{
						sLatitude = tagString;
						if ((debug.metadata && debug.detailed && debug.panorama) || (debug.gps && debug.detailed) ) 
							ml.systemMessage("Found Latitude..." + sLatitude);
					}
					if (tagName.equals("GPS Longitude")) // Longitude
					{
						sLongitude = tagString;
						if ((debug.metadata && debug.detailed && debug.panorama) || (debug.gps && debug.detailed) ) 
							ml.systemMessage("Found Longitude..." + sLongitude);
					}
					if (tagName.equals("GPS Altitude")) // Altitude
					{
						sAltitude = tagString;
						if ((debug.metadata && debug.detailed && debug.panorama) || (debug.gps && debug.detailed) ) 
							ml.systemMessage("Found Altitude..." + sAltitude);
					}
					if (tagName.equals("Focal Length")) // Focal Length
					{
						sFocalLength = tagString;
						if ((debug.metadata && debug.detailed && debug.panorama) || (debug.gps && debug.detailed) )
							ml.systemMessage("Found Focal Length..." + sFocalLength);
					}

//					if (tagName.equals("Focal Length 35")) // Focal Length (35 mm. equivalent)
//					{
//						sFocalLength35mm = tagString;
//						if (debugSettings.metadata && debugSettings.detailed && debugSettings.panorama) 
//							ml.systemMessage("Found Focal Length 35mm Equivalent..." + sFocalLength);
//					}
					if (tagName.equals("GPS Img Direction")) // Image Direction
					{
						sDirection = tagString;
						
						if (debug.metadata && debug.detailed && debug.panorama)
							ml.systemMessage("Found Panorama Direction..." + sDirection);
					}
					if (tagName.equals("Image Description")) 	// Description	 -- Unused for panorama?
					{
						sDescription = tagString;
						if (debug.metadata && debug.detailed && debug.panorama)
							ml.systemMessage("Found Description..." + sDescription);
					}
					
					if (tagName.equals("Keywords"))
					{
						if (debug.metadata && debug.detailed && debug.panorama)
							ml.systemMessage("Keywords: "+tagString);
						
						sKeywords = ParseKeywordArray(tagString);
					}

					if (tagName.equals("Aperture Value")) // Aperture
					{
//						ml.systemMessage("Aperture Value (not recorded)..."+tagString);
					}

					if (tagName.equals("Brightness Value")) // Brightness
					{
						fBrightness = parseBrightness(tagString);
						if(fBrightness == -1.f) brightnessMissing = true;
					}

					if (tagName.equals("Image Width")) // Orientation
						iWidth = ParseWidthOrHeight(tagString);

					if (tagName.equals("Image Height")) // Orientation
						iHeight = ParseWidthOrHeight(tagString);
				}

				sFilePath = file.getPath();
				if(sFilePath == null)
				{
					if(!dataMissing) dataMissing = true;
				}
				else if(sFilePath.equals(""))
				{
					if(!dataMissing) dataMissing = true;
				}

				if (directory.hasErrors()) {
					for (String error : directory.getErrors()) {
						if (debug.metadata || debug.panorama) 
							ml.systemMessage("Metadata.loadPanoramaMetadata()... ERROR: " + error);
					}
				}
			}

			try {
				zonedDateTime = parseDateTime(sDateTime, timeZoneID);
			} 
			catch (RuntimeException ex) 
			{
				if (debug.metadata || debug.panorama) 
					ml.systemMessage("Metadata.loadPanoramaMetadata()... Error in date / time... " + ex);
				if(!dataMissing) dataMissing = true;
			}

//			iCameraModel = -1;
//			fFocalLength = -1.f;

			try {
				float xCoord, yCoord, zCoord;
				sLongitudeRef = parseLongitudeRef(sLongitudeRef);
				sLatitudeRef = parseLatitudeRef(sLatitudeRef);
				xCoord = parseLongitude(sLongitude, sLongitudeRef);
				yCoord = parseAltitude(sAltitude);
				zCoord = parseLatitude(sLatitude, sLatitudeRef);

				if(ml.debug.gps && ml.debug.detailed)
				{
					ml.systemMessage("  Parsed longitude Ref:"+sLongitudeRef);
					ml.systemMessage("  Parsed latitude Ref:"+sLatitudeRef);
				}
				
				if (u.isNaN(xCoord) || u.isNaN(yCoord) || u.isNaN(zCoord)) 
				{
					gpsLoc = new PVector(0, 0, 0);
					if(!dataMissing) dataMissing = true;
				}
				else
					gpsLoc = new PVector(xCoord, yCoord, zCoord);
			} 
			
			catch (RuntimeException ex) 
			{
				if (debug.metadata) ml.systemMessage("Metadata.loadPanoramaMetadata()... Error reading image location:" + sName + "  " + ex);
				if(!dataMissing) dataMissing = true;
			}

			try {
				if(sDirection == null)
				{
					if (debug.metadata) ml.systemMessage("Metadata.loadPanoramaMetadata()... Panorama fDirection is null! "+sName);
					fDirection = -100000.f;
				}
				else
					fDirection = ParseDirection(sDirection);		
			}
			catch (RuntimeException ex) {
				if (debug.ml || debug.metadata) 
					ml.systemMessage("Metadata.loadPanoramaMetadata()... Error reading panorama orientation / direction... sDirection:" + sDirection+" fOrientation:"+fOrientation + "  fDirection:" + fDirection + "  " + ex);
				if(!dataMissing) dataMissing = true;
			}
		}
		
		/* Add this media object to field */
		try 
		{
			if(!dataMissing)
			{
				return new WMV_PanoramaMetadata( sName, sFilePath, gpsLoc, zonedDateTime, sDateTime, timeZoneID, fDirection, iCameraModel, 
												 iWidth, iHeight, fBrightness, sKeywords, sSoftware, sLongitudeRef, sLatitudeRef  );
			}
			else
				ml.systemMessage("Metadata.loadPanoramaMetadata()... Data missing!  Could not get panorama metadata:"+sName);
		}
		catch (RuntimeException ex) {
			if (debug.metadata)
				ml.systemMessage("Metadata.loadPanoramaMetadata()... Could not get panorama metadata! Error: "+ex);
		}
		
		return null;
	}

	/**
	 * Load video metadata from disk
	 * @param file Video file
	 * @param timeZoneID Video time zone
	 * @return Video metadata
	 */
	public WMV_VideoMetadata loadVideoMetadata(File file, String timeZoneID)
	{
		String sName = file.getName();

		boolean dataMissing = false;
		ZonedDateTime zonedDateTime = null;			

		int iWidth = -1, iHeight = -1;
		float fBrightness = -1.f;
		PVector gpsLoc = new PVector(0, 0, 0);
		String sFilePath = "";
		
		String sWidth = null, sHeight = null;

		String sDateTime = null;										// Time
		
		String sLatitude = null, sLongitude = null, altitude = null;	// GPS Location
		String sLatitudeRef = null, sLongitudeRef = null;
		
		String duration = null;										
		String sKeywords = null;
		String[] keywords = new String[0];	

		if(debug.metadata && debug.video && debug.detailed) 
			ml.systemMessage("Metadata.loadVideoMetadata()... Loading video metadata for file: "+sName);

		Map<String, String> videoMetadata = null;

		if (debug.metadata && debug.video && debug.detailed) 
		{
			ml.systemMessage("*******************************************************");
			ml.systemMessage("*  Loading Video Metadata: "+sName);
		}

		/* Read video metadata from file using ExifToolWrapper */
		try {
			videoMetadata = readVideoMetadata(file, exiftoolFile);		
		}
		catch(Throwable t)
		{
			if(debug.metadata) 
				ml.systemMessage("Metadata.loadVideoMetadata()... Throwable while reading video metadata: " + t);
			dataMissing = true;
		}

		/* Set video variables from metadata */
		try {
//			sLongitudeRef = videoMetadata.get("GPSLongitudeRef");
//			sLatitudeRef = videoMetadata.get("GPSLatitudeRef");
			sLongitude = videoMetadata.get("GPSLongitude");
			sLatitude = videoMetadata.get("GPSLatitude");
			altitude = videoMetadata.get("GPSAltitude");
			duration = videoMetadata.get("MediaDuration");
			sDateTime = videoMetadata.get("CreationDate");
			sWidth = videoMetadata.get("ImageWidth");
			sHeight = videoMetadata.get("ImageHeight");
			sKeywords = videoMetadata.get("Keywords");
			
			String[] parts = sDateTime.split("-");
			sDateTime = parts[0];

			if(debug.metadata && debug.video && debug.detailed)
			{
				ml.systemMessage("  Latitude:"+sLatitude+"  Longitude:"+sLongitude+"  Altitude:"+altitude);
				ml.systemMessage("  Date:"+sDateTime+"  duration:"+duration+"  width:"+sWidth+"  height:"+sHeight);
				ml.systemMessage("  Longitude Ref:"+sLongitudeRef);
				ml.systemMessage("  Latitude Ref:"+sLatitudeRef);
				ml.systemMessage("  keywords:"+sKeywords);
			}

			try {
				zonedDateTime = parseDateTime(sDateTime, timeZoneID);
			} 
			catch (Throwable t) {
				ml.systemMessage("Metadata.loadVideoMetadata()... Throwable while parsing date / time... " + t);
				dataMissing = true;
			}

			/* Parse video GPS coordinates */
			try {
				float xCoord, yCoord, zCoord;
				
				xCoord = parseFloatLongitude(sLongitude);			// Get longitude decimal value without sign
				yCoord = Float.valueOf(altitude);					// Get altitude in m. (Altitude ref. assumed to be sea level)
				zCoord = parseFloatLatitude(sLatitude);				// Get longitude decimal value without sign
				
				if (u.isNaN(xCoord) || u.isNaN(yCoord) || u.isNaN(zCoord)) 
				{
					gpsLoc = new PVector(0, 0, 0);
					if(!dataMissing) dataMissing = true;
				}
				else 
					gpsLoc = new PVector(xCoord, yCoord, zCoord);
				
				sLongitudeRef = getFloatLongitudeRef(sLongitude);	// Get reference direction of longitude sign of longitude
				sLatitudeRef = getFloatLatitudeRef(sLatitude);		// Get reference direction of latitude sign of longitude
			} 
			catch (RuntimeException ex) 
			{
				if (debug.metadata)
					ml.systemMessage("Metadata.loadVideoMetadata()... Error reading video location:" + sName + "  " + ex);
				dataMissing = true;
			}

			iWidth = Integer.valueOf(sWidth);
			iHeight = Integer.valueOf(sHeight);

			sFilePath = file.getPath();
			if(sFilePath == null)
			{
				if(!dataMissing) dataMissing = true;
			}
			else if(sFilePath.equals(""))
			{
				if(!dataMissing) dataMissing = true;
			}
		}
		catch (Throwable t) {
			ml.systemMessage("Metadata.loadVideoMetadata()... Throwable while extracting video EXIF data:" + t);
			if(!dataMissing) dataMissing = true;
		}

		try 
		{
			if(!dataMissing)
			{
				WMV_VideoMetadata vMetadata = new WMV_VideoMetadata( sName, sFilePath, gpsLoc, zonedDateTime, sDateTime, timeZoneID, 
						-1, -1, -1, -1, -1, -1, iWidth, iHeight, fBrightness, keywords, "", sLongitudeRef, sLatitudeRef );
				return vMetadata;
			}
			else if(debug.metadata || debug.video)
				ml.systemMessage("Metadata.loadVideoMetadata()... Data missing!  Could not get video metadata:"+sName);

		}
		catch (Throwable t) {
			if (debug.metadata)
			{
				ml.systemMessage("Metadata.loadVideoMetadata()... Throwable while getting video metadata: "+t);
				ml.systemMessage("   pFilePath:" + sFilePath+"  pLoc.x:" + gpsLoc.x+"    pLoc.y:" + gpsLoc.y+"    pLoc.z:" + gpsLoc.z);
			}
		}

		return null;
	}
	
	/**
	 * Load sound metadata for specified sound file 
	 * @param f Field containing sound
	 * @param file Sound file 
	 * @param timeZoneID Time zone ID
	 * @return Sound metadata
	 */
	public WMV_SoundMetadata loadSoundMetadata(WMV_Field f, File file, String timeZoneID)
	{
		String sName = file.getName();
		String sFilePath = file.getPath();
		Path path = FileSystems.getDefault().getPath(sFilePath);
		boolean dataMissing = false;

		if(sFilePath == null)
		{
			if(!dataMissing) dataMissing = true;
		}
		else if(sFilePath.equals(""))
		{
			if(!dataMissing) dataMissing = true;
		}

		if(!file.getName().equals(".DS_Store"))
		{
			try
			{
				if(!dataMissing)
				{
					if(debug.sound || debug.metadata) ml.systemMessage("Metadata.loadSoundMetadata()... Loading sound file:"+sFilePath);
					BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
					FileTime creationTime = attr.creationTime();
					
					if(ml.debug.sound && ml.debug.metadata)
						ml.systemMessage("Metadata.loadSoundMetadata()... Sound file: "+file.getName()+" Creation Time: "+creationTime);
					
					ZonedDateTime soundDateTime = getZonedTimeFromTimeStamp(creationTime, timeZoneID);
					String soundDateTimeString = ml.world.utilities.getDateTimeAsString(soundDateTime);		// 2016:04:10 17:52:39

					if(debug.metadata && debug.sound)
						ml.systemMessage("Metadata.loadSoundMetadata()... soundDateTimeString:"+soundDateTimeString+" timeZoneID:"+timeZoneID+" Zoned Day:"+soundDateTime.getDayOfMonth()+" Hour:"+soundDateTime.getHour()+" Min:"+soundDateTime.getMinute());

					return new WMV_SoundMetadata( sName, sFilePath, new PVector(0,0,0), 0.f, -1, -1.f, soundDateTime, soundDateTimeString, timeZoneID, 
							null, "", "", "" );
				}
				else
					if(debug.metadata) ml.systemMessage("Metadata.loadSoundMetadata()... Data missing! Excluded sound:"+sName);
			}
			catch(Throwable t)
			{
				ml.systemMessage("Metadata.loadSoundMetadata()... ERROR... Throwable :"+t);
			}
		}
		
		return null;
	}
	
	/**
	 * Read video metadata using ExifToolWrapper
	 * @param file File location to read metadata from
	 * @param exifToolFile File object for ExifTool executable
	 * @return Map containing metadata values
	 */
	private Map<String, String> readVideoMetadata(File file, File exifToolFile)
	{
		Map<String, String> result;
		try
		{
			Set<String> tags = new HashSet();
			tags.add("GPSLongitude");
			tags.add("GPSLatitude");
			tags.add("GPSAltitude");
			tags.add("MediaDuration");
			tags.add("CreationDate");
			tags.add("ImageWidth");
			tags.add("ImageHeight");
			tags.add("Keywords");
			
			ExifToolWrapper exifToolWrapper = new ExifToolWrapper(exifToolFile);
			
			result = exifToolWrapper.getTagsFromFile(file, tags);
			return result;
		}
		catch(Throwable t)
		{
			ml.systemMessage("Throwable while getting video tags from file:" + t);			
		}
		
		return null;
	}

	/**
	 * Check metadata for image file to tell whether it is a regular image or panorama
	 * @param file File to check
	 * @return Whether the file is a panorama
	 */
	public boolean fileIsPanorama(File file)
	{
		Metadata imageMetadata = null;				// For images
		
		try {
			imageMetadata = JpegMetadataReader.readMetadata(file);		/* Read metadata with JpegMetadataReader */
		}
		catch (Throwable t) 
		{
			if(debug.metadata && debug.detailed)
				ml.systemMessage("fileIsPanorama()... Throwable:" + t);
		}

		if (imageMetadata != null) 
		{
			for (Directory directory : imageMetadata.getDirectories()) 
			{
				for (Tag tag : directory.getTags()) 
				{
					String tagString = tag.toString();		
					String tagName = tag.getTagName();

					if (tag.getTagName().equals("Software")) // Software
					{
						String sSoftware = tagString;
//						if (debugSettings.metadata && debugSettings.detailed) ml.systemMessage("Metadata.fileIsPanorama()... Found Software..." + sSoftware);

						if(sSoftware.equals("[Exif IFD0] Software - Occipital 360 Panorama"))
							return true;		// Image is a panorama
					}

					if (tagName.equals("Model")) // Model
					{
						String sCameraModel = tagString;
						int iCameraModel;
						
//						if (debugSettings.metadata && debugSettings.detailed) ml.systemMessage("Metadata.fileIsPanorama()... Found Camera Model..." + sCameraModel);
						
						try
						{
							iCameraModel = parseCameraModel(sCameraModel);
							if(iCameraModel == 2)				// {0: iPhone, 1: Nikon, 2: Ricoh Theta S}
								return true;		// Image is a panorama
						}
						catch (Throwable t) // If not, must be only one keyword
						{
							if (debug.metadata) ml.systemMessage("Metadata.fileIsPanorama()... Throwable in parsing camera model..." + t);
						}
					}
				}
			}
		}
		
		return false;
	}

	/**
	 * Parse longitude reference
	 * @param inputLongitudeRef
	 * @return Longitude reference string {"N" or "S"}
	 */
	public String parseLongitudeRef(String inputLongitudeRef)
	{
		if(inputLongitudeRef.equals("[GPS] GPS Longitude Ref - W"))
			return "W";
		else if(inputLongitudeRef.equals("[GPS] GPS Longitude Ref - E"))
			return "E";
		
		if(ml.debug.metadata)
			ml.systemMessage("Metadata.parseLongitudeRef()... ERROR: >>>"+inputLongitudeRef+"<<< not recognized...");
		return "";
	}

	/**
	 * Parse latitude reference metadata string
	 * @param inputLatitudeRef
	 * @return Latitude reference string {"N" or "S"}
	 */
	public String parseLatitudeRef(String inputLatitudeRef)
	{
		if(inputLatitudeRef.equals("[GPS] GPS Latitude Ref - N"))
			return "N";
		else if(inputLatitudeRef.equals("[GPS] GPS Latitude Ref - S"))
			return "S";
		if(ml.debug.metadata)
			ml.systemMessage("Metadata.parseLatitudeRef()... ERROR: >>>"+inputLatitudeRef+"<<< not recognized...");
		return "";
	}
	
	/**
	 * Parse metadata input for GPS longitude in D/M/S format
	 * @param input String input
	 * @return GPS decimal longitude
	 */
	public float parseLongitude(String input, String direction) {
		String[] parts = input.split("Longitude -");
		input = parts[1];
		parts = input.split("°");
		float degrees = Float.valueOf(parts[0]);
		input = parts[1];
		parts = input.split("'");
		float minutes = Float.valueOf(parts[0]);
		input = parts[1];
		parts = input.split("\"");
		float seconds = Float.valueOf(parts[0]);

		float decimal = convertDMSCoordsToDecimal(degrees, minutes, seconds, direction);
		return decimal;
	}

	/**
	 * Parse metadata input for GPS latitude in D/M/S format
	 * @param input String input
	 * @return GPS decimal latitude
	 */
	public float parseLatitude(String input, String direction) {
		String[] parts = input.split("Latitude -");
		input = parts[1];
		parts = input.split("°");
		float degrees = Float.valueOf(parts[0]);
		input = parts[1];
		parts = input.split("'");
		float minutes = Float.valueOf(parts[0]);
		input = parts[1];
		parts = input.split("\"");
		float seconds = Float.valueOf(parts[0]);

		float decimal = convertDMSCoordsToDecimal(degrees, minutes, seconds, direction);
		return decimal;
	}

	/**
	 * Convert GPS coordinates in DMS format to decimal
	 * @param degrees Degrees
	 * @param minutes Minutes
	 * @param seconds Seconds
	 * @return Decimal value 
	 */
	public float convertDMSCoordsToDecimal(float degrees, float minutes, float seconds, String direction) 
	{
	    float decimal = Math.signum(degrees) * (Math.abs(degrees) + (minutes / 60.0f) + (seconds / 3600.0f));

		if (direction == "S" || direction == "W") 			// -- Added 6/30/17
		{
			if(Math.signum(decimal) == 1)					// Make decimal negative if necessary
				decimal *= -1; 
		}

		return decimal;
		
		/* Old Method */
//		float dd = degrees + minutes / 60 + seconds / (60 * 60);
//		return dd;
	}
	
	/**
	 * Parse metadata input string for GPS longitude in decimal format
	 * @param input Input string
	 * @return GPS decimal longitude (without sign)
	 */
	private float parseFloatLongitude( String input ) 
	{	
		float decimal = Float.valueOf(input);
		return decimal;
	}

	/**
	 * Parse metadata input string for GPS latitude in decimal format
	 * @param input Input string
	 * @return GPS decimal latitude (without sign)
	 */
	private float parseFloatLatitude( String input ) 
	{	
		float decimal = Float.valueOf(input);
		return decimal;
	}

	/**
	 * Get longitude reference of longitude metadata input
	 * @param input Longitude metadata input
	 * @return Longitude reference
	 */
	private String getFloatLongitudeRef( String input )
	{
		float decimal = Float.valueOf(input);
		String gpsLongitudeRef = "E";
		if( (int)Math.signum(decimal) == -1 )
			gpsLongitudeRef = "W";
		return gpsLongitudeRef;
	}
	
	/**
	 * Get latitude reference of latitude metadata input
	 * @param input Latitude metadata input
	 * @return Latitude reference
	 */
	private String getFloatLatitudeRef( String input )
	{
		float decimal = Float.valueOf(input);
		String gpsLatitudeRef = "N";
		if( (int)Math.signum(decimal) == -1 )
			gpsLatitudeRef = "S";
		return gpsLatitudeRef;
	}

	/**
	 * Parse metadata input for GPS bearing (i.e. compass orientation) in decimal format, given in degrees
	 * @param input String input
	 * @return GPS orientation in degrees
	 */
	public float parseFloatBearing(String input) {
		String[] parts = input.split("Magnetic North");
		float bearing = Float.valueOf(parts[0]);

		return bearing;
	}

	/**
	 * Parse metadata input for GPS altitude in decimal format, given in m.
	 * @param input String input
	 * @return Altitude in meters
	 */
	public float parseAltitude(String input) {
		String[] parts = input.split("-");
		input = parts[1];
		parts = input.split("metres");
		float altitude = Float.valueOf(parts[0]);

		return altitude;
	}

	/**
	 * Parse metadata input for lens focal length, given in mm.
	 * @param input String input
	 * @return Focal length in millimeters
	 */
	public float parseFocalLength(String input) {
		String[] parts = input.split("-");
		input = parts[1];
		parts = input.split("m");
		float focLength = Float.valueOf(parts[0]);

		if (parts[0] == "-")
			focLength = -1000.f;

		return focLength;
	}

	/**
	 * Parse metadata input for camera sensor size in mm.
	 * @param input String input
	 * @return Camera sensor size in millimeters
	 */
	public float parseSensorSize(String input)
	{
		String[] parts = input.split("-");
		input = parts[1];
		parts = input.split("m");
		float sensorSize = Float.valueOf(parts[0]);

		return sensorSize;
	}
	
	/**
	 * Parse metadata input for focus distance 
	 * @param input String input
	 * @return Focus distance in meters
	 */
	public float ParseFocusDistance(String input) {
		String[] parts = input.split("m");
		float focusDist = Float.valueOf(parts[0]);

		if (parts[0] == "-")
			focusDist = -1000.f;

		return focusDist;
	}

	public int ParseOrientation(String input) 	// Currently only handles horizontal and rotated 90 CW
	{
		if(input.contains("Horizontal") || input.contains("Normal") || input.contains("normal"))
		{
			return 0;
		}

		if(input.contains("Rotate 90 CW"))
		{
			return 90;
		}

		return 0;
		
//		String[] parts = input.split("-");
//		input = parts[1];
//		parts = input.split(",");
//		input = parts[0];
//		if (parts[0].trim().equals("Top"))
//			return 0;
//		else {
//			return Integer.valueOf(parts[1].trim());
//		}
	}

	public String parseSoftware(String input)
	{
//		Ex. "[Exif IFD0] Software - Aperture 3.6"
		
		String[] parts = input.split(" Software - ");
		String software = parts[parts.length-1];
		
		return software;
	}
	
	/**
	 * Parse metadata camera model
	 * @param input Camera Model EXIF String
	 * @return Camera model ID {0: iPhone, 1: Nikon, 2: Ricoh Theta S}
	 */
	public int parseCameraModel(String input) 
	{
//		ml.systemMessage("parseCameraModel()... input:"+input);
		String[] parts = input.split(" Model - ");
		String model = parts[parts.length-1];
		model = model.replaceAll("\\s\\s","");

//		if (model.equals("iPhone") || model.equals("iPhone 7 Plus"))
		if (model.contains("iPhone"))
		{
			return 0;
		}
		else if (model.equals("NIKON"))
		{
			return 1;
		}
		else if (model.equals("RICOH THETA S") || model.equals("RICOH THETA"))
		{
			return 2;
		}
		else
		{
			if(debug.metadata) ml.systemMessage("Unknown Camera Model:"+model);
			return 0;
		}
	}

	public float ParseDirection(String input) 
	{
		String[] parts = input.split("-");
		input = parts[1];
		parts = input.split("degrees");
		return Float.valueOf(parts[0].trim());
	}

	public int ParseKeyword(String input)
	{
		if (input.equals(null)) {
			ml.systemMessage("Image has no keyword!");
		}

		if (input.equals("Center"))
			return 0;
		else if (input.equals("Lower 1"))
			return -1;
		else if (input.equals("Lower 2"))
			return -2;
		else if (input.equals("Lower 3"))
			return -3;
		else if (input.equals("Lower 4"))
			return -4;
		else if (input.equals("Upper 1"))
			return 1;
		else if (input.equals("Upper 2"))
			return 2;
		else if (input.equals("Upper 3"))
			return 3;
		else if (input.equals("Upper 4"))
			return 4;

		return 0;
	}

	public int ParseWidthOrHeight(String input)
	{
		String[] parts = input.split("-");
		input = parts[1];
		parts = input.split("pixels");
		return Integer.valueOf(parts[0].trim());
	}

	public float parseBrightness(String input)
	{
		String[] parts = input.split("Value - ");
		input = parts[1];							// Fractional brightness
		
		parts = input.split("/");

		if(parts.length == 2)
			return Float.parseFloat(parts[0]) / Float.parseFloat(parts[1]);
		else 
			return -1.f;
	}

	public int[] parseAFPointsArray(String input) 
	{
		String[] parts = input.split(",");
		int[] afPoints = new int[parts.length];

		for (int i = 0; i < parts.length; i++) {
			if (parts[i].startsWith(" ")) {
				parts[i] = parts[i].substring(1);
			}
			afPoints[i] = parseAFPoint(parts[i]);
			if (debug.metadata)
				ml.systemMessage("afPoints[i]:" + afPoints[i]);
		}

		return afPoints;
	}

	
	public int parseAFPoint(String afPoint) 
	{
		if (afPoint.equals("Center"))
			return 0;
		else if (afPoint.equals("Top"))
			return 1;
		else if (afPoint.equals("Bottom"))
			return 2;
		else if (afPoint.equals("Mid-left"))
			return 3;
		else if (afPoint.equals("Mid-right"))
			return 4;
		else if (afPoint.equals("Upper-left"))
			return 5;
		else if (afPoint.equals("Upper-right"))
			return 6;
		else if (afPoint.equals("Lower-left"))
			return 7;
		else if (afPoint.equals("Lower-right"))
			return 8;
		else if (afPoint.equals("Far Left"))
			return 9;
		else if (afPoint.equals("Far Right"))
			return 10;

		if (debug.metadata) {
			ml.systemMessage("Not a valid afPoint: " + afPoint);
		}

		return 1000;
	}

	/**
	 * Get ZonedDateTime object from metadata "Date/Time Original" string using specified time zone
	 * @param input Date/time string, e.g. "[Exif SubIFD] Date/Time Original - 2016:08:11 16:40:10"
	 * @param timeZoneID Time zone ID string, e.g. "America/Los_Angeles"
	 * @return ZonedDateTime object for given date/time and time zone
	 */
	public ZonedDateTime parseDateTime(String input, String timeZoneID)
	{		
		String[] parts = input.split(":");

		int year = Integer.valueOf(parts[0].trim());
		int month = Integer.valueOf(parts[1]);
		int min = Integer.valueOf(parts[3]);
		int sec = Integer.valueOf(parts[4]);
		input = parts[2];
		parts = input.split(" ");
		int day = Integer.valueOf(parts[0]);
		int hour = Integer.valueOf(parts[1]);

		ZonedDateTime zoned = ZonedDateTime.of(year, month, day, hour, min, sec, 0, ZoneId.of(timeZoneID));
		return zoned;
//		ZonedDateTime pac = ZonedDateTime.of(year, month, day, hour, min, sec, 0, ZoneId.of("America/Los_Angeles"));
//		return pac;
	}
	
	/**
	 * Get creation time as ZonedDateTime object from timestamp
	 * @param creationTime FileTime object
	 * @return ZonedDateTime object
	 */
	private ZonedDateTime getZonedTimeFromTimeStamp(FileTime creationTime, String timeZoneID)
	{
		Instant creationInstant = creationTime.toInstant();
		ZonedDateTime mediaTime = creationInstant.atZone( ZoneId.of(timeZoneID) );
//		ZonedDateTime mediaTime = creationInstant.atZone( ZoneId.of(ml.world.getCurrentField().getTimeZoneID()) );
		return mediaTime;
	}
	
	/**
	 * Parse keyword array
	 * @param input Single string of keywords separated by commas
	 * @return Array of keyword strings
	 */
	public String[] ParseKeywordArray(String input) 
	{
		String[] parts = input.split(",");
		String[] keywords = new String[parts.length];

		for (int i = 0; i < parts.length; i++) 
		{
			String[] keywordArray = parts[i].split(";");
			for(int idx = 0; idx<keywordArray.length; idx++)
			{
				keywords[i] = keywordArray[idx];
			}
			
			if (ml.debug.metadata && ml.debug.detailed)
				ml.systemMessage("Image or panorama keywords[i]:" + keywords[i]);
		}

		return keywords;
	}
}