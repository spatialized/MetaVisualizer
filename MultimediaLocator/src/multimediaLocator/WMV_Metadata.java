package multimediaLocator;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Movie;

import org.homeunix.thecave.moss.util.image.ExifToolWrapper;

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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**************
 * @author davidgordon
 * Class for extracting metadata and add 3D media to field 
 */
class WMV_Metadata
{
	public String library = "";
	public String imageFolder = "", smallImageFolder = "";
	public String panoramaFolder = "";
	public String videoFolder = "", smallVideoFolder = "";				// File path for media folders
	public String soundFolder = "";
	public String dataFolder = "";
	
	public File imageFolderFile = null, smallImageFolderFile = null, panoramaFolderFile = null, // Folders containing the media 
				videoFolderFile = null, soundFolderFile = null, dataFolderFile = null;
	
	public boolean imageFolderFound = false, smallImageFolderFound = false;
	public boolean panoramaFolderFound = false;
	public boolean videoFolderFound = false; 	
	public boolean soundFolderFound = false; 	
	public boolean dataFolderFound = false; 	
	
	public File[] smallImageFiles = null, imageFiles = null, panoramaFiles = null, // Temp arrays to store media files
				  videoFiles = null, soundFiles = null, dataFiles = null;								

	public boolean smallImageFilesFound = false;
	public boolean imageFilesFound = false;
	public boolean panoramaFilesFound = false;
	public boolean videoFilesFound = false;
	public boolean soundFilesFound = false;
	private boolean dataFilesValidFormat = false, dataFolderValid = false;

//	private final float defaultFocusDistance = 9.0f;			// Default focus distance for images and videos (m.)

	int iCount = 0, pCount = 0, vCount = 0;							// Media count by type 
	public File exifToolFile;										// File for ExifTool executable

	MultimediaLocator p;
	WMV_Field f;													// Field to load metadata into
	WMV_Utilities u;												// Utility class
	ML_DebugSettings debugSettings;
	
	WMV_Metadata( MultimediaLocator parent, ML_DebugSettings newDebugSettings )
	{
		p = parent;
		u = new WMV_Utilities();
		debugSettings = newDebugSettings;
		exifToolFile = new File("/usr/local/bin/exiftool");						// Initialize metadata extraction class	
	}

	/**
	 * Load metadata from a folder into a field 
	 */
	public WMV_SimulationState load(WMV_Field field, String mediaFolder, boolean formatted)
	{
		if(formatted)				// mediaFolder is correctly formatted library
		{
			library = mediaFolder;

			f = field;
			String fieldPath = f.getName();

			loadImageFolders(library, fieldPath); 	// Load image and panorama file names
			loadVideoFolder(library, fieldPath); 	// Load video file names
			loadSoundFolder(library, fieldPath); 	// Load sound file names
			loadDataFolder(library, fieldPath);		// Load media data from disk
			
			if(dataFilesValidFormat)
			{
				WMV_FieldState newFieldState = p.library.loadFieldState(dataFiles[0].getAbsolutePath());
				WMV_ViewerSettings newViewerSettings = p.library.loadViewerSettings(dataFiles[1].getAbsolutePath());
				WMV_ViewerState newViewerState = p.library.loadViewerState(dataFiles[2].getAbsolutePath());
				WMV_WorldSettings newWorldSettings = p.library.loadWorldSettings(dataFiles[3].getAbsolutePath());
				WMV_WorldState newWorldState = p.library.loadWorldState(dataFiles[4].getAbsolutePath());
				
				WMV_SimulationState newSimulationState = new WMV_SimulationState( newFieldState, newViewerSettings,
									newViewerState, newWorldSettings, newWorldState );
				return newSimulationState;
			}
			else
			{
				iCount = 0; 
				pCount = 0;
				vCount = 0;

				if(smallImageFilesFound) loadImageMetadata(smallImageFiles);						// Load image metadata 
				if(panoramaFilesFound) loadImageMetadata(panoramaFiles);  // Load panorama metadata  -- Fix bug in panoramaFiles.length == 0
				if(videoFilesFound) loadVideoMetadata(videoFiles);	 	  // Load video metadata 
				if(soundFilesFound) loadSounds(soundFiles);				  // Load sound file names 
			}
		}
		else						// mediaFolder is an ordinary folder of media
		{
			f = field;
			
			// -- GO THROUGH SUBFOLDERS, CHECK FILE EXTENSIONS AND CREATE ARRAYS HERE

			iCount = 0; 
			pCount = 0;
			vCount = 0;

			if(imageFiles != null) loadImageMetadata(imageFiles);							// Load image metadata 
			if(panoramaFiles != null) loadImageMetadata(panoramaFiles); // Load panorama metadata  -- Fix bug in panoramaFiles.length == 0
			if(videoFiles != null) loadVideoMetadata(videoFiles);			// Load video metadata
			if(soundFiles != null) loadSounds(soundFiles);			// Load sounds (no metadata)
		}
		
		return null;
	}
	
	/**
	 * Load metadata for folders of images, small images (640px wide) and panoramas
	 */
	public void loadImageFolders(String library, String fieldPath) 		
	{
		smallImageFolder = library + fieldPath + "/small_images/";	/* Check for small_images folder */
		imageFolder = library + fieldPath + "/images/";				/* Check for images folder */
		panoramaFolder = library + fieldPath + "/panoramas/";			/* Check for panoramas folder */

		smallImageFolderFile = new File(smallImageFolder);
		imageFolderFile = new File(imageFolder);
		panoramaFolderFile = new File(panoramaFolder);

		smallImageFolderFound = (smallImageFolderFile.exists() && smallImageFolderFile.isDirectory());			
		imageFolderFound = (imageFolderFile.exists() && imageFolderFile.isDirectory());	
		panoramaFolderFound = (panoramaFolderFile.exists() && panoramaFolderFile.isDirectory());
		
		smallImageFiles = null;
		imageFiles = null;
		panoramaFiles = null;
		
		if(panoramaFolderFound)
		{
			panoramaFiles = panoramaFolderFile.listFiles();
			if(panoramaFiles != null && panoramaFiles.length > 0)
				panoramaFilesFound = true;	
		}
		
		if(smallImageFolderFound)		// Found small_images folder. Check for files
		{
			smallImageFiles = smallImageFolderFile.listFiles();

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
				
				if(smallImageFilesFound && debugSettings.metadata) 
					System.out.println("Files found in small_images folder, will use instead of shrinking large images...");

//				imageFolder = smallImageFolder;					// Set imageFolder to small_images
//				imageFolderFile = new File(imageFolder);
//				imageFiles = imageFolderFile.listFiles();
			}
		}
		else if(imageFolderFound || panoramaFolderFound)		// If no small images, look for original images and panoramas
		{
			if(debugSettings.metadata) 	
				System.out.println("No small_images folder... ");

			if(imageFolderFound)			// Check for image files
			{
				imageFiles = imageFolderFile.listFiles();
				if(imageFiles != null && imageFiles.length > 0)
					imageFilesFound = true;
			}
			
			if(imageFilesFound)				/* If no small images, but there are images */
			{
				WMV_Command commandExecutor;
				ArrayList<String> command = new ArrayList<String>();
//				ArrayList<String> files = new ArrayList<String>();

				command = new ArrayList<String>();				/* Create small_images directory */
				command.add("mkdir");
				command.add("small_images");
				commandExecutor = new WMV_Command(library, command);
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
		}
			
		// If images exist but no small images are found
		if(imageFilesFound && !smallImageFilesFound)	// Copy original images to small_images directory and resize
		{
			imageFolder = library + "/" + fieldPath + "/images/";					// Original size
			boolean success = u.shrinkImages(imageFolder, smallImageFolder);		
			if(success)
			{
//				if(debugSettings.metadata) 
					System.out.println("Shrink images successful...");
			}
			else
			{
//				if(debugSettings.metadata) 
					System.out.println("Shrink images failed...");
			}

			imageFolder = smallImageFolder;					// Set imageFolder to small_images
			imageFolderFile = new File(imageFolder);
			imageFiles = imageFolderFile.listFiles();
		}

		if(debugSettings.metadata) 	
		{
			if(smallImageFilesFound)
				System.out.println("Small Image Folder Location:" + smallImageFolderFile + " smallImageFiles.length:"+smallImageFiles.length);
			if(imageFilesFound)
				System.out.println("Image Folder Location:" + imageFolderFile + " imageFiles.length:"+imageFiles.length);
			if(panoramaFilesFound)
				System.out.println("Panorama Folder Location:" + panoramaFolderFile + " panoramaFiles.length:"+panoramaFiles.length);
		}
	}

	/**
	 * Load metadata for folder of videos
	 */
	public void loadVideoFolder(String library, String fieldPath) // Load photos up to limit to load at once, save those over limit to load later
	{
//		File smallVideoFolderFile;
//		File[] smallVideoFiles;				
		
//		videoFolder = library  + "/" + fieldPath + "/videos/";					// Original size
		videoFolder = library  + "/" + fieldPath + "/small_videos/";		// Max width 720 pixels  -- Check this!

		videoFolderFile = new File(videoFolder);
		videoFolderFound = (videoFolderFile.exists() && videoFolderFile.isDirectory());	
		videoFiles = null;

//		smallVideoFolderFile = new File(smallImageFolder);
//		smallVideoFiles = new File[1];

		if(videoFolderFound)				// Check for video files
		{
			videoFiles = videoFolderFile.listFiles();
			if(videoFiles != null && videoFiles.length > 0)
				videoFilesFound = true;
		}
	}

	/**
	 * Load metadata for folder of videos
	 */
	public void loadSoundFolder(String library, String fieldPath) // Load photos up to limit to load at once, save those over limit to load later
	{
		soundFolder = library  + "/" + fieldPath + "/sounds/";		// Max width 720 pixels  -- Check this!

		soundFolderFile = new File(soundFolder);
		soundFolderFound = (soundFolderFile.exists() && soundFolderFile.isDirectory());	
		soundFiles = null;

		if(soundFolderFound)				// Check for sound files
		{
			soundFiles = soundFolderFile.listFiles();
			if(soundFiles != null && soundFiles.length > 0)
				soundFilesFound = true;
		}
		
		if (debugSettings.sound)
			System.out.println("Sound Folder:" + soundFolder);
	}
	
	/**
	 * Load metadata for folder of videos
	 */
	public void loadDataFolder(String library, String fieldPath) // Load photos up to limit to load at once, save those over limit to load later
	{
		dataFolder = library + fieldPath + "/data/";		// Max width 720 pixels  -- Check this!
//		dataFolder = library + "/" + fieldPath + "/data/";		// Max width 720 pixels  -- Check this!

		dataFolderFile = new File(dataFolder);
		dataFolderFound = (dataFolderFile.exists() && dataFolderFile.isDirectory());	
		dataFiles = null;

		System.out.println("dataFolderFound? " + (dataFolderFound)); 
//		System.out.println("dataFolderFile.isDirectory()? " + (dataFolderFile.isDirectory())); 
//		System.out.println("dataFolderFile.exists()? " + (dataFolderFile.exists())); 
//		System.out.println("dataFiles != null? " + (dataFiles != null)); 
		if(dataFolderFound)				// Check for sound files
		{
			dataFiles = dataFolderFile.listFiles();
			if(dataFiles != null && dataFiles.length > 0)
			{
				System.out.println("dataFiles[0].getName(): " + (dataFiles[0].getName())); 
				System.out.println("dataFiles[1].getName(): " + (dataFiles[1].getName())); 
				System.out.println("dataFiles[2].getName(): " + (dataFiles[2].getName())); 
				System.out.println("dataFiles[3].getName(): " + (dataFiles[3].getName())); 
				System.out.println("dataFiles[4].getName(): " + (dataFiles[4].getName())); 
				if( dataFiles[0].getName().equals("ml_library_fieldState.json") &&
					dataFiles[1].getName().equals("ml_library_viewerSettings.json") &&
					dataFiles[2].getName().equals("ml_library_viewerState.json") &&
					dataFiles[3].getName().equals("ml_library_worldSettings.json") &&
					dataFiles[4].getName().equals("ml_library_worldState.json")    )
				dataFilesValidFormat = true;
			}
		}
		
		if (debugSettings.data)
		{
			System.out.println("Data Folder:" + dataFolder); 
			System.out.println("dataFilesValidFormat? "+dataFilesValidFormat);
		}
	}
	
	/** 
	 * Read tags from array of video files and create 3D video objects
	 * @param files File array
	 */
	public boolean loadSounds(File[] files) 			// Load metadata for a folder of images and add to imgs ArrayList
	{
		int count = 0;
		
		/* -- Should calculate time / date at least here */
		
		if(files != null)
		{
			for(File file : files)
			{
				String sName = "";
				sName = file.getName();

				String sFilePath = file.getPath();
				Path path = FileSystems.getDefault().getPath(sFilePath);

				if(!file.getName().equals(".DS_Store"))
				{
					try
					{
						System.out.println("Loading sound:"+sFilePath);

						BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
						FileTime creationTime = attr.creationTime();
						if(p.debugSettings.sound || p.debugSettings.metadata)
							System.out.println("file: "+file.getName()+" creationTime: "+creationTime);
						ZonedDateTime soundTime = getTimeFromTimeStamp(creationTime);

						WMV_SoundMetadata sMetadata = new WMV_SoundMetadata( sName, sFilePath, new PVector(0,0,0), 0.f, -1, -1.f, soundTime, 
								p.world.getCurrentField().getTimeZoneID() );
						f.addSound( new WMV_Sound (count, 3, sMetadata) );
					}
					catch(Throwable t)
					{
						System.out.println("Throwable in loadSounds()... "+t);
					}
				}
				count++;
			}

			return true;
		}
		else return false;
	}
	
	private ZonedDateTime getTimeFromTimeStamp(FileTime creationTime)
	{
		String tsStr = creationTime.toString();

		/* Old Method */
//		String[] parts = tsStr.split("T");
//		String strDate = parts[0];
//		String strTime = parts[1];
//
//		parts = strDate.split("-");
//		String strYear = parts[0];
//		String strMonth = parts[1];
//		String strDay = parts[2];
//	
//		parts = strTime.split("Z");
//		parts = parts[0].split(":");
//		
//		String strHour = parts[0];
//		String strMinute = parts[1];
//		String strSecond = parts[2];
//
//
//		ZonedDateTime utc = ZonedDateTime.of( Integer.parseInt(strYear), Integer.parseInt(strMonth), Integer.parseInt(strDay), 
//				 							  Integer.parseInt(strHour), Integer.parseInt(strMinute), Integer.parseInt(strSecond), 0, ZoneId.of("UTC") );
//		return utc;

		Instant creationInstant = creationTime.toInstant();
		ZonedDateTime mediaTime = creationInstant.atZone(ZoneId.of(p.world.getCurrentField().getTimeZoneID()));

		return mediaTime;
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
			System.out.println("Throwable while getting video tags from file:" + t);			
		}
		
		return null;
	}
	
	/** 
	 * Read tags from array of image/panorama files and create 3D image/panorama objects
	 * @param files File array
	 */
	public boolean loadImageMetadata(File[] files) 			// Load metadata for a folder of images and add to imgs ArrayList
	{
		int fileCount;

		if(files != null)
			fileCount = files.length;	 		// File count
		else 
			return false;
		
		for (int currentMedia = 0; currentMedia < fileCount; currentMedia++) 
		{
			boolean panorama = false;
			boolean dataMissing = false, brightnessMissing = false, descriptionMissing = false;

			String sName = "";
			sName = files[currentMedia].getName();

			ZonedDateTime zonedDateTime = null;
			
			int iWidth = -1, iHeight = -1;
			float fDirection = 0, fElevation = 0, fRotation = 0, fFocalLength = 0, fOrientation = 0, fSensorSize = 0;
			float fFocusDistance = -1.f;										
			float fBrightness = -1.f;
			int iCameraModel = 0;

			String[] sKeywords = new String[0];	
			
			String sDateTime = null;
			String sLatitude = null, sLongitude = null, sAltitude = null;
			String sOrientation = null, sDirection = null;
			String sFocalLength = null, sFocalLength35mm = null;
			String sSoftware = null;
			String sCameraModel = null, sDescription = null;

			PVector gpsLoc = new PVector(0, 0, 0);
			String sFilePath = "";

			Metadata imageMetadata = null;				// For images

			File file = files[currentMedia];				// Get current file from array

			if(debugSettings.metadata && debugSettings.detailed)
				System.out.println("Loading image: "+sName);

			try {
				imageMetadata = JpegMetadataReader.readMetadata(file);		/* Read metadata with JpegMetadataReader */
			}
			catch (Throwable t) 
			{
				if(debugSettings.metadata && debugSettings.detailed)
					System.out.println("loadImageMetadata()... Throwable:" + t);
				if(!dataMissing) dataMissing = true;
			}

			/* Set image variables from metadata */
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
							sSoftware = tagString;
							if (debugSettings.metadata && debugSettings.detailed) System.out.println("Found Software..." + sSoftware);

							if(sSoftware.equals("[Exif IFD0] Software - Occipital 360 Panorama"))
							{
								panorama = true;		// Image is a panorama
								if(dataMissing) f.addPanoramaError();		// Count dataMissing as panorama error
							}
							else
							{
								if(dataMissing) f.addImageError();			// Count dataMissing as image error
							}
						}

						if (tagName.equals("Model")) // Model
						{
							sCameraModel = tagString;
							if (debugSettings.metadata && debugSettings.detailed) System.out.println("Found Camera Model..." + sCameraModel);

							try
							{
								iCameraModel = parseCameraModel(sCameraModel);
							}
							catch (Throwable t) // If not, must be only one keyword
							{
								if (debugSettings.metadata) System.out.println("Throwable in camera model / focal length..." + t);
								if(!dataMissing)
								{
									f.addImageError();
									dataMissing = true;
								}						
							}

							if(iCameraModel == 1)
							{
								panorama = true;		// Image is a panorama
								if(dataMissing) f.addPanoramaError();		// Count dataMissing as panorama error
							}
							else
							{
								if(brightnessMissing || descriptionMissing)
								{
									if(!dataMissing)
									{
										f.addImageError();
										dataMissing = true;
									}						
								}
							}
						}

						if (tagName.equals("Orientation")) // Orientation
						{
							sOrientation = tagString;
							if (debugSettings.metadata && debugSettings.detailed) System.out.println("Found Orientation..." + sOrientation);
						}
						if (tagName.equals("Date/Time Original")) // Orientation
						{
							sDateTime = tagString;
							if (debugSettings.metadata && debugSettings.detailed) System.out.println("Found DateTimeOriginal..." + sDateTime);
						}
						if (tagName.equals("GPS Latitude")) // Latitude
						{
							sLatitude = tagString;
							if (debugSettings.metadata && debugSettings.detailed) System.out.println("Found Latitude..." + sLatitude);
						}
						if (tagName.equals("GPS Longitude")) // Longitude
						{
							sLongitude = tagString;
							if (debugSettings.metadata && debugSettings.detailed) System.out.println("Found Longitude..." + sLongitude);
						}
						if (tagName.equals("GPS Altitude")) // Altitude
						{
							sAltitude = tagString;
							if (debugSettings.metadata && debugSettings.detailed) System.out.println("Found Altitude..." + sAltitude);
						}
						if (tagName.equals("Focal Length")) // Focal Length
						{
							sFocalLength = tagString;
							if (debugSettings.metadata && debugSettings.detailed) System.out.println("Found Focal Length..." + sFocalLength);
						}

						if (tagName.equals("Focal Length 35")) // Focal Length (35 mm. equivalent)
						{
							sFocalLength35mm = tagString;
							if (debugSettings.metadata && debugSettings.detailed) System.out.println("Found Focal Length 35mm Equivalent..." + sFocalLength);
						}
						if (tagName.equals("GPS Img Direction")) // Image Direction
						{
							sDirection = tagString;
							
							if (debugSettings.metadata && debugSettings.detailed)
								if(panorama) System.out.println("Found Panorama Direction..." + sDirection);
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
								descriptionMissing = true;
								if(debugSettings.metadata) System.out.println("Not a Theodolite image...");
							}

							if (debugSettings.metadata && debugSettings.detailed)
								System.out.println("Found Description..." + sDescription);
						}
						
						if (tagName.equals("AFPointsUsed")) // Orientation
						{
							// String afPointsStr = tagString;
							// if (afPointsStr.equals("-"))
							// 		afPoints[0] = 0;
							// else
							// 		afPoints = ParseAFPointsArray(afPointsStr);
						}

						if (tagName.equals("Keywords"))
						{
							System.out.println("-------------->  Keywords: "+tagString);
							
							sKeywords = ParseKeywordArray(tagString);
						}

						if (tagName.equals("Aperture Value")) // Aperture
						{
//							fAperture = 
//							System.out.println("Aperture Value (not recorded)..."+tagString);
						}

						if (tagName.equals("Brightness Value")) // Brightness
						{
							fBrightness = parseBrightness(tagString);
							if(fBrightness == -1.f && !dataMissing)
								brightnessMissing = true;
						}

						if (tagName.equals("Image Width")) // Orientation
							iWidth = ParseWidthOrHeight(tagString);

						if (tagName.equals("Image Height")) // Orientation
							iHeight = ParseWidthOrHeight(tagString);
					}

					sFilePath = file.getPath();

					if (directory.hasErrors()) {
						for (String error : directory.getErrors()) {
							System.out.println("ERROR: " + error);
						}
					}
				}

				try {
					zonedDateTime = parseDateTime(sDateTime);
				} 
				catch (RuntimeException ex) 
				{
					if (debugSettings.metadata) System.out.println("Error in date / time... " + ex);
					if(!dataMissing)
					{
						if(panorama) f.addPanoramaError();
						else f.addImageError();
						dataMissing = true;
					}					
				}

				if(!panorama)			// -- Update this
				{
					try {
						fFocalLength = parseFocalLength(sFocalLength);
						fSensorSize = parseSensorSize(sFocalLength35mm);		// 29 mm for iPhone 6S+
					} 
					catch (Throwable t) // If not, must be only one keyword
					{
						if (debugSettings.metadata) System.out.println("Throwable in camera model / focal length..." + t);
						if(!dataMissing)
						{
							if(panorama) f.addPanoramaError();
							else f.addImageError();
							dataMissing = true;
						}						
					}
				}
				else
				{
					iCameraModel = -1;
					fFocalLength = -1.f;
				}

				try {
					float xCoord, yCoord, zCoord;
					xCoord = parseLongitude(sLongitude);
					yCoord = parseAltitude(sAltitude);
					zCoord = parseLatitude(sLatitude);

					if (u.isNaN(xCoord) || u.isNaN(yCoord) || u.isNaN(zCoord)) 
					{
						gpsLoc = new PVector(0, 0, 0);
						if(!dataMissing)
						{
							if(panorama) f.addPanoramaError();
							else f.addImageError();
							dataMissing = true;
						}						
					}
					gpsLoc = new PVector(xCoord, yCoord, zCoord);
				} 
				
				catch (RuntimeException ex) 
				{
					if (debugSettings.metadata) System.out.println("Error reading image location:" + sName + "  " + ex);

					if(!dataMissing)
					{
						if(panorama) f.addPanoramaError();
						else f.addImageError();
						dataMissing = true;
					}					
				}

				try {
					fOrientation = ParseOrientation(sOrientation);
					fDirection = ParseDirection(sDirection);		

					if(panorama && sDirection == null)
					{
						if (debugSettings.metadata) System.out.println("Panorama fDirection is null!");
					}
				} 
				catch (RuntimeException ex) {
					if (debugSettings.metadata) System.out.println("Error reading image orientation / direction:" + fOrientation + "  " + fDirection + "  " + ex);
					if(!panorama)
					{
						if(!dataMissing)
						{
							f.addImageError();
							dataMissing = true;
						}			
					}
				}
			}
			
			/* Add this media object to field */
			try 
			{
				if(!(gpsLoc.x == 0.f && gpsLoc.y == 0.f && gpsLoc.z == 0.f))
				{
					if(panorama && !dataMissing)
					{
//						WMV_MediaMetadata mMetadata = new WMV_MediaMetadata(sName, sFilePath, gpsLoc, zonedDateTime, f.getTimeZoneID());
						WMV_PanoramaMetadata pMetadata = new WMV_PanoramaMetadata(sName, sFilePath, gpsLoc, zonedDateTime, f.getTimeZoneID(), fDirection, iCameraModel, iWidth, iHeight, fBrightness, sKeywords);
//						WMV_MediaMetadata mMetadata = new WMV_MediaMetadata(sName, sFilePath, gpsLoc, zonedDateTime, f.getTimeZoneID());
//						WMV_PanoramaMetadata pMetadata = new WMV_PanoramaMetadata(fDirection, iCameraModel, iWidth, iHeight, fBrightness, sKeywords);

						PImage pImg = p.createImage(0,0,processing.core.PConstants.RGB);

						f.addPanorama( new WMV_Panorama(pCount, 1, 0.f, null, pImg, pMetadata) );
						pCount++;
					}
					else if(!dataMissing)
					{
//						WMV_MediaMetadata mMetadata = new WMV_MediaMetadata(sName, sFilePath, gpsLoc, zonedDateTime, f.getTimeZoneID());
						WMV_ImageMetadata iMetadata = new WMV_ImageMetadata(sName, sFilePath, gpsLoc, zonedDateTime, f.getTimeZoneID(), fDirection, fFocalLength, fOrientation, fElevation, fRotation, fFocusDistance, 
								fSensorSize, iCameraModel, iWidth, iHeight, fBrightness, sKeywords);
//						WMV_MediaMetadata mMetadata = new WMV_MediaMetadata(sName, sFilePath, gpsLoc, zonedDateTime, f.getTimeZoneID());
//						WMV_ImageMetadata iMetadata = new WMV_ImageMetadata(fDirection, fFocalLength, fOrientation, fElevation, fRotation, fFocusDistance, 
//								fSensorSize, iCameraModel, iWidth, iHeight, fBrightness, sKeywords);
						
						PImage pImg = p.createImage(0, 0, processing.core.PConstants.RGB);
						f.addImage( new WMV_Image(iCount, pImg, 0, iMetadata ) );
						iCount++;
					}
				}
				else
					System.out.println("Excluded "+(panorama?"panorama:":"image:")+sName);
			}
			catch (RuntimeException ex) {
				if (debugSettings.metadata)
					System.out.println("Could not add image! Error: "+ex);
			}
		}
		
		return true;
	}
	
	/** 
	 * Read tags from array of video files and create 3D video objects
	 * @param files File array
	 */
	public boolean loadVideoMetadata(File[] files) 			// Load metadata for a folder of images and add to imgs ArrayList
	{
		int fileCount;
		if(files != null)
			fileCount = files.length;	 		// File count
		else
			return false;
		
		boolean videosExist = true;
		
		if (videoFiles == null)					// Check if any videos exist
			videosExist = false;
		
		for (int currentMedia = 0; currentMedia < fileCount; currentMedia++) 
		{
			boolean dataMissing = false;
			boolean hasVideo = true;

			if (!videosExist || videoFiles.length <= currentMedia)		// Check if this video exists
				hasVideo = false;

			String sName = "";
			sName = videoFiles[currentMedia].getName();

			ZonedDateTime zonedDateTime = null;			

			int iWidth = -1, iHeight = -1;
			float fBrightness = -1.f;
			PVector gpsLoc = new PVector(0, 0, 0);
			String sFilePath = "";
			
			String duration = null;										
			String sDateTime = null, sWidth = null, sHeight = null;
			String sLatitude = null, sLongitude = null, altitude = null;
			String sKeywords = null;
			String[] keywords = new String[0];	

			File file = files[currentMedia];				// Get current file from array

			if(debugSettings.metadata)
				System.out.println("Loading video metadata for file: "+sName);


			Map<String, String> videoMetadata = null;
			/* Read video metadata from file using ExifToolWrapper */
			try {
				videoMetadata = readVideoMetadata(file, exifToolFile);		
			}
			catch(Throwable t)
			{
				System.out.println("Throwable while reading video metadata: " + t);
				dataMissing = true;
			}

			/* Set video variables from metadata */
			try {
				sLongitude = videoMetadata.get("GPSLongitude");
				sLatitude = videoMetadata.get("GPSLatitude");
				altitude = videoMetadata.get("GPSAltitude");
				duration = videoMetadata.get("MediaDuration");
				sDateTime = videoMetadata.get("CreationDate");
				sWidth = videoMetadata.get("ImageWidth");
				sHeight = videoMetadata.get("ImageHeight");
				sKeywords = videoMetadata.get("Keywords");

				if(debugSettings.metadata && debugSettings.video && debugSettings.detailed)
				{
					System.out.println("--> Video latitude:"+sLatitude);
					System.out.println("  longitude:"+sLongitude);
					System.out.println("  altitude:"+altitude);
					System.out.println("  duration:"+duration);
					System.out.println("  date:"+sDateTime);
					System.out.println("  width:"+sWidth);
					System.out.println("  height:"+sHeight);
					System.out.println("  keywords:"+sKeywords);
				}

				try {
					zonedDateTime = parseVideoDateTime(sDateTime);
				} 
				catch (Throwable t) {
					System.out.println("Throwable while parsing date / time... " + t);
					dataMissing = true;
				}

				/* Parse video GPS coordinates */
				try {
					float xCoord, yCoord, zCoord;
					xCoord = Float.valueOf(sLongitude);				// Flip sign of longitude?
					yCoord = Float.valueOf(altitude);
					zCoord = Float.valueOf(sLatitude);				// Flip sign of latitude?

					if (u.isNaN(xCoord) || u.isNaN(yCoord) || u.isNaN(zCoord)) 
					{
						gpsLoc = new PVector(0, 0, 0);
						if(!dataMissing)
						{
							f.addVideoError();
							dataMissing = true;
						}
					}
					gpsLoc = new PVector(xCoord, yCoord, zCoord);
				} 
				catch (RuntimeException ex) 
				{
					if (debugSettings.metadata)
						System.out.println("Error reading video location:" + sName + "  " + ex);
					dataMissing = true;
				}

				iWidth = Integer.valueOf(sWidth);
				iHeight = Integer.valueOf(sHeight);
				sFilePath = file.getPath();
			} 
			catch (Throwable t) {
				System.out.println("Throwable while extracting video EXIF data:" + t);
				if(!dataMissing)
				{
					f.addVideoError();
					dataMissing = true;
				}
			}

			/* Add 3D video object to field based on given metadata */
			try 
			{
				if(!(gpsLoc.x == 0.f && gpsLoc.y == 0.f && gpsLoc.z == 0.f ) && hasVideo && !dataMissing)
				{
					Movie pMov = new Movie(p, sFilePath);

//					WMV_MediaMetadata mMetadata = new WMV_MediaMetadata(sName, sFilePath, gpsLoc, zonedDateTime, f.getTimeZoneID());
//					WMV_MediaMetadata mMetadata = new WMV_MediaMetadata(sName, sFilePath, gpsLoc, zonedDateTime, f.getTimeZoneID());
//					WMV_VideoMetadata vMetadata = new WMV_VideoMetadata(-1, -1, -1, -1, -1, -1, iWidth, iHeight, 
//							fBrightness, keywords);

					WMV_VideoMetadata vMetadata = new WMV_VideoMetadata(sName, sFilePath, gpsLoc, zonedDateTime, f.getTimeZoneID(), 
							-1, -1, -1, -1, -1, -1, iWidth, iHeight, fBrightness, keywords);
					f.addVideo( new WMV_Video(vCount, pMov, 2, vMetadata) );
					vCount++;
				}
				else if(debugSettings.metadata || debugSettings.video)
					System.out.println("Excluded video:"+sName);

			}
			catch (Throwable t) {
				if (debugSettings.metadata)
				{
					System.out.println("Throwable while adding video to ArrayList: "+t);
					System.out.println("   pFilePath:" + sFilePath);
					PApplet.print("    pLoc.x:" + gpsLoc.x);
					PApplet.print("    pLoc.y:" + gpsLoc.y);
					System.out.println("    pLoc.z:" + gpsLoc.z);
					System.out.println("   dataMissing:" + dataMissing);
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Parse metadata input for GPS longitude in D/M/S format
	 * @param input String input
	 * @return GPS decimal longitude
	 */
	public float parseLongitude(String input) {
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

		float decimal = ConvertDMSToDDLongitude(degrees, minutes, seconds);
		return decimal;
	}

	/**
	 * Parse metadata input for GPS latitude in D/M/S format
	 * @param input String input
	 * @return GPS decimal latitude
	 */
	public float parseLatitude(String input) {
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

		float decimal = ConvertDMSToDD(degrees, minutes, seconds);
		return decimal;
	}

	/**
	 * Parse metadata input for GPS longitude in decimal format
	 * @param input String input
	 * @return GPS decimal longitude
	 */
	public float parseFloatLongitude(String input) {
		String[] parts = input.split("W");
		float decimal = Float.valueOf(parts[0]);
		return decimal;
	}

	/**
	 * Parse metadata input for GPS latitude in decimal format
	 * @param input String input
	 * @return GPS decimal latitude
	 */
	public float parseFloatLatitude(String input) {
		String[] parts = input.split("N");
		float decimal = Float.valueOf(parts[0]);
		return decimal;
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
	 * Parse metadata input for camera sensor size, given in mm.
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

	public int ParseOrientation(String input) 	// Currently only handles horizontal
	{
		String[] parts = input.split("-");
		input = parts[1];
		parts = input.split(",");
		input = parts[0];
		if (parts[0].trim().equals("Top"))
			return 0;
		else {
			return Integer.valueOf(parts[1].trim());
		}
	}


	public int parseCameraModel(String input) {
		String[] parts = input.split(" Model - ");
		String model = parts[parts.length-1];
		model = model.replaceAll("\\s\\s","");
//		System.out.println("parse model:"+model);
		if (model.equals("iPhone"))
			return 0;
		else if (model.equals("RICOH THETA S") || model.equals("RICOH THETA"))
		{
//			System.out.println("RICOH THETA S");
			return 1;
		}
		else
		{
//			System.out.println("NOT RICOH THETA S:"+model);
			return 2;
		}
//		else if (model.equals("NIKON"))
//			return 2;
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
			System.out.println("Image has no keyword!");
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

	/**
	 * ConvertDMSToDD()
	 * @param degrees Degrees
	 * @param minutes Minutes
	 * @param seconds Seconds
	 * @return Decimal value of coordinates
	 */
	public float ConvertDMSToDD(float degrees, float minutes, float seconds) 
	{
		float dd = degrees + minutes / 60 + seconds / (60 * 60);

		/*
		 * if (direction == "S" || direction == "W") { dd = dd * -1; } // Don't
		 * do anything for N or E
		 */

		return dd;
	}

	public float ConvertDMSToDDLongitude(float degrees, float minutes, float seconds) {
		float dd;
		dd = degrees - minutes/60.f - seconds/(60.f*60.f);
		return dd;
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
			if (debugSettings.metadata)
				System.out.println("afPoints[i]:" + afPoints[i]);
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

		if (debugSettings.metadata) {
			System.out.println("Not a valid afPoint: " + afPoint);
		}

		return 1000;
	}

	public ZonedDateTime parseDateTime(String input) 
	{		
		String[] parts = input.split("-");
		input = parts[1];
		parts = input.split(":");

		int year = Integer.valueOf(parts[0].trim());
		int month = Integer.valueOf(parts[1]);
		int min = Integer.valueOf(parts[3]);
		int sec = Integer.valueOf(parts[4]);
		input = parts[2];
		parts = input.split(" ");
		int day = Integer.valueOf(parts[0]);
		int hour = Integer.valueOf(parts[1]);

//		ZonedDateTime utc = ZonedDateTime.of(year, month, day, hour, min, sec, 0, ZoneId.of("UTC"));
		ZonedDateTime pac = ZonedDateTime.of(year, month, day, hour, min, sec, 0, ZoneId.of("America/Los_Angeles"));
		
//		year = utc.getYear();
//		month = utc.getMonthValue();
//		day = utc.getDayOfMonth();
//		hour = utc.getHour();
//		System.out.println("IMAGE utc year:"+year+" month:"+month+" day:"+day+" hour:"+hour);
//		year = pac.getYear();
//		month = pac.getMonthValue();
//		day = pac.getDayOfMonth();
//		hour = pac.getHour();
//		System.out.println("vs. pac year:"+year+" month:"+month+" day:"+day+" hour:"+hour);

		return pac;
	}

	public ZonedDateTime parseVideoDateTime(String input) 
	{		
		String[] parts = input.split(":");

		int year = Integer.valueOf(parts[0].trim());
		int month = Integer.valueOf(parts[1]);
		int min = Integer.valueOf(parts[3]);
		String secStr = parts[4];

		input = parts[2];
		parts = input.split(" ");
		int day = Integer.valueOf(parts[0]);
		int hour = Integer.valueOf(parts[1]);

		parts = secStr.split("-");
		int sec = Integer.valueOf(parts[0]);

//		Calendar c = Calendar.getInstance();
//		c.set(year, month, day, hour, min, sec);
//		ZonedDateTime utc = ZonedDateTime.of(year, month, day, hour, min, sec, 0, ZoneId.of("UTC"));
		ZonedDateTime pac = ZonedDateTime.of(year, month, day, hour, min, sec, 0, ZoneId.of("America/Los_Angeles"));
//		year = utc.getYear();
//		month = utc.getMonthValue();
//		day = utc.getDayOfMonth();
//		hour = utc.getHour();
//		System.out.println("VIDEO utc year:"+year+" month:"+month+" day:"+day+" hour:"+hour);
//		year = pac.getYear();
//		month = pac.getMonthValue();
//		day = pac.getDayOfMonth();
//		hour = pac.getHour();
//		System.out.println("vs. pac year:"+year+" month:"+month+" day:"+day+" hour:"+hour);

		return pac;
	}

//	public ZonedDateTime parseSoundDateTime(String input) 
//	{
//		String[] parts = input.split(":");
//
//		int year = Integer.valueOf(parts[0]);
//		int month = Integer.valueOf(parts[1]);
//		int min = Integer.valueOf(parts[3]);
//		int sec = Integer.valueOf(parts[4]);
//		input = parts[2];
//		parts = input.split(" ");
//		int day = Integer.valueOf(parts[0]);
//		int hour = Integer.valueOf(parts[1]);
//
//		Calendar c = Calendar.getInstance();
//		c.set(year, month, day, hour, min, sec);
//
//	Calendar c = Calendar.getInstance();
//	c.set(year, month, day, hour, min, sec);
//	ZonedDateTime utc = ZonedDateTime.of(year, month, day, hour, min, sec, 0, ZoneId.of("UTC"));
//	return utc;
//	}
	
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
			
//			if (p.debugSettings.metadata)
				System.out.println("Image or panorama keywords[i]:" + keywords[i]);
		}

//		for (int i = 0; i < keywords.length; i++) 
//		{
//			
//		}

		return keywords;
	}
	

//	public PVector ParseTrackLocation(String input) 
//	{
//		String[] parts = input.split(",");
//
//		float latitude = Float.valueOf(parts[0]);
//		float longitude = Float.valueOf(parts[1]);
//		float altitude = Float.valueOf(parts[2]);
//
//		PVector loc = new PVector(longitude, altitude, latitude);
//		return loc;
//	}

}