package wmViewer;

import processing.core.*;

import org.homeunix.thecave.moss.util.image.ExifToolWrapper;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
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

	public File imageFolderFile = null, smallImageFolderFile = null, panoramaFolderFile = null, // Folders containing the media 
				videoFolderFile = null;	
	public boolean imageFolderFound = false, smallImageFolderFound = false;
	public boolean panoramaFolderFound = false;
	public boolean videoFolderFound = false; 	
	
	public File[] smallImageFiles = null, imageFiles = null, panoramaFiles = null, videoFiles = null;		// Arrays for images and videos

	public boolean smallImageFilesFound = false;
	public boolean imageFilesFound = false;
	public boolean panoramaFilesFound = false;
	public boolean videoFilesFound = false;
	
	int iCount = 0, pCount = 0, vCount = 0;							// Media count by type 
	public File exifToolFile;										// File for ExifTool executable

	WMV_Utilities u;												// Utility class
	WMV_Field f;													// Parent field
	WorldMediaViewer p;
	
	WMV_Metadata(WorldMediaViewer parent)
	{
		p = parent;
		exifToolFile = new File("/usr/local/bin/exiftool");						// Initialize metadata extraction class	
	}

	/**
	 * load()
	 * Load metadata for the field
	 */
	public void load(WMV_Field field)
	{
		library = p.library.getLibraryFolder();

		f = field;
		String fieldPath = f.name;
		
		loadImageFolders(library, fieldPath); 	// Load image file names
		loadVideoFolder(library, fieldPath); 	// Load video file names

		iCount = 0; 
		pCount = 0;
		vCount = 0;

//		boolean result;
		
		if(imageFilesFound || smallImageFilesFound)				// Load image metadata 
		{
			loadImagesFromTags(imageFiles);
		}
		if(panoramaFilesFound)									// Load panorama metadata  -- Fix bug in panoramaFiles.length == 0
		{
			if(p.debug.metadata)
				PApplet.println("loadImagesFromTags panoramas:"+panoramaFiles.length);
			loadImagesFromTags(panoramaFiles);
		}
		if(videoFilesFound)										// Load video metadata 
			loadVideosFromTags(videoFiles);	
	}
	
	/**
	 * loadImageFolders()
	 * Load metadata for folders of images, small images (640px wide) and panoramas
	 */
	public void loadImageFolders(String library, String fieldPath) 		
	{
		smallImageFolder = library + "/" + fieldPath + "/small_images/";	/* Check for small_images folder */
		imageFolder = library + "/" + fieldPath + "/images/";				/* Check for images folder */
		panoramaFolder = library + "/" + fieldPath + "/panoramas/";			/* Check for panoramas folder */

		smallImageFolderFile = new File(smallImageFolder);
		imageFolderFile = new File(imageFolder);
		panoramaFolderFile = new File(panoramaFolder);

		smallImageFolderFound = (smallImageFolderFile.exists() && smallImageFolderFile.isDirectory());			
		imageFolderFound = (imageFolderFile.exists() && imageFolderFile.isDirectory());	
		panoramaFolderFound = (panoramaFolderFile.exists() && panoramaFolderFile.isDirectory());
		
//		PApplet.println("smallImageFolderFound:"+smallImageFolderFound);
//		PApplet.println("imageFolderFound:"+imageFolderFound);
//		PApplet.println("panoramaFolderFound:"+panoramaFolderFound);

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
					PApplet.println("DSStore?"+check);
					if(check.equals(".DS_Store"))
						smallImageFilesFound = false;			/* Only found .DS_Store, ignore it */
				}
				
				if(smallImageFilesFound && f.p.p.debug.metadata) 
					PApplet.println("Files found in small_images folder, will use instead of shrinking large images...");

				imageFolder = smallImageFolder;					// Set imageFolder to small_images
				imageFolderFile = new File(imageFolder);
				imageFiles = imageFolderFile.listFiles();
			}
		}
		else if(imageFolderFound || panoramaFolderFound)		// If no small images, look for original images and panoramas
		{
			if(f.p.p.debug.metadata) 	
			{
				PApplet.println("No small_images folder... ");
			}

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
				ArrayList<String> files = new ArrayList<String>();

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
					PApplet.println("Throwable t while creating small_images directory:"+t);
				}
			}
			
//			if(panoramaFilesFound && !imageFilesFound)
//			{
//				imageFolder = panoramaFolder;
//				imageFolderFile = new File(panoramaFolder);
//				imageFiles = panoramaFolderFile.listFiles();
//			}
		}
			
		// If images exist but no small images are found
		if(imageFilesFound && !smallImageFilesFound)	// Copy original images to small_images directory and resize
		{
			imageFolder = library + "/" + fieldPath + "/images/";					// Original size
			boolean success = f.p.p.utilities.shrinkImages(imageFolder, smallImageFolder);		
			if(success)
			{
				if(f.p.p.debug.metadata) 	PApplet.println("Shrink images successful...");
			}
			else
			{
				if(f.p.p.debug.metadata) 	PApplet.println("Shrink images failed...");
			}

			imageFolder = smallImageFolder;					// Set imageFolder to small_images
			imageFolderFile = new File(imageFolder);
			imageFiles = imageFolderFile.listFiles();
		}

		if(f.p.p.debug.metadata) 	
		{
			if(smallImageFilesFound)
				PApplet.println("Small Image Folder Location:" + smallImageFolderFile + " smallImageFiles.length:"+smallImageFiles.length);
			if(imageFilesFound)
				PApplet.println("Image Folder Location:" + imageFolderFile + " imageFiles.length:"+imageFiles.length);
			if(panoramaFilesFound)
				PApplet.println("Panorama Folder Location:" + panoramaFolderFile + " panoramaFiles.length:"+panoramaFiles.length);
		}
	}

	/**
	 * loadVideoFolder()
	 * Load metadata for folder of videos
	 */
	public void loadVideoFolder(String library, String fieldPath) // Load photos up to limit to load at once, save those over limit to load later
	{
//		File smallVideoFolderFile;
//		File[] smallVideoFiles;				
		
		videoFolder = library  + "/" + fieldPath + "/videos/";					// Original size
//		smallVideoFolder = library  + "/" + fieldPath + "/small_videos/";		// Max width 720 pixels  -- Check this!

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
		
//		if (p.p.p.debug.metadata)
//		{
//			PApplet.println("Video Folder:" + videoFolder);
//		}
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
			
			ExifToolWrapper exifToolWrapper = new ExifToolWrapper(exifToolFile);
			
			result = exifToolWrapper.getTagsFromFile(file, tags);
			return result;
		}
		catch(Throwable t)
		{
			PApplet.println("Throwable while getting video tags from file:" + t);			
		}
		
		return null;
	}
	
	/** 
	 * Read tags from array of image/panorama files and create 3D image/panorama objects
	 * @param files File array
	 */
	public boolean loadImagesFromTags(File[] files) 			// Load metadata for a folder of images and add to imgs ArrayList
	{
		int fileCount;
		
		if(files != null)
			fileCount = files.length;	 		// File count
		else 
			return false;
		
//		PApplet.println("fileCount:"+fileCount);
		boolean imagesExist = true;

		if (imageFiles == null)					// Check if any images exist
			imagesExist = false;

		for (int currentMedia = 0; currentMedia < fileCount; currentMedia++) 
		{
			String name = "";
//			boolean hasImage = true;

//			if (!imagesExist || imageFiles.length <= currentMedia)		// Check if this image exists
//				hasImage = false;

			name = files[currentMedia].getName();

			boolean panorama = false;
			boolean dataMissing = false, brightnessMissing = false, descriptionMissing = false;

			Calendar calendarTime = null;			// Calendar date and time			

			float fDirection = 0, fElevation = 0, fRotation = 0,
					fFocalLength = 0, fOrientation = 0, fSensorSize = 0;
			float fFocusDistance = f.p.defaultFocusDistance;									// Focus distance currently NOT used
			int iWidth = -1, iHeight = -1;
			int iCameraModel = 0;
			float fBrightness = -1.f;

			String dateTime = null;
			String latitude = null, longitude = null, altitude = null;
			String focalLength = null, focalLength35 = null, software = null, orientation = null, direction = null;
			String camera_model = null, description = null;
			String keyword = null;

			PVector pLoc = new PVector(0, 0, 0);
			String pFilePath = "";

			File file = null;				
			Metadata imageMetadata = null;				// For images

			file = files[currentMedia];				// Get current file from array

			if(f.p.p.debug.metadata && f.p.p.debug.detailed)
				PApplet.println("Loading image: "+name);

			try {
				imageMetadata = JpegMetadataReader.readMetadata(file);		/* Read metadata with JpegMetadataReader */
			}
			catch (Throwable t) 
			{
				PApplet.println("Throwable:" + t);
				if(!dataMissing)
					dataMissing = true;
			}

			/* Set image variables from metadata */
			if (imageMetadata != null) 
			{
				for (Directory directory : imageMetadata.getDirectories()) {
					for (Tag tag : directory.getTags()) {
						String tagString = tag.toString();							
//						PApplet.println("--> dataMissing:"+dataMissing+" tagString:"+tagString);

						if (tag.getTagName().equals("Software")) // Software
						{
							software = tagString;
							if (f.p.p.debug.metadata && f.p.p.debug.detailed)
								PApplet.println("Found Software..." + software);

							if(software.equals("[Exif IFD0] Software - Occipital 360 Panorama"))
							{
								panorama = true;		// Image is a panorama
								if(dataMissing) f.addPanoramaError();		// Count dataMissing as panorama error
							}
							else
							{
								if(dataMissing) f.addImageError();			// Count dataMissing as image error
							}
						}

						if (tag.getTagName().equals("Model")) // Model
						{
							camera_model = tagString;
							if (f.p.p.debug.metadata && f.p.p.debug.detailed)
								PApplet.println("Found Camera Model..." + camera_model);

							try
							{
								iCameraModel = parseCameraModel(camera_model);
							}
							catch (Throwable t) // If not, must be only one keyword
							{
								PApplet.println("Throwable in camera model / focal length..." + t);
								if(!dataMissing)
								{
									f.addImageError();
									dataMissing = true;
								}						
							}

//							PApplet.println("iCameraModel:"+iCameraModel);

							if(iCameraModel == 1)
							{
								panorama = true;		// Image is a panorama
//								PApplet.println("panorama:"+panorama+" dataMissing:"+dataMissing);

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
//							else if(iCameraModel == 2)						// Unknown camera model
//							{
//								if(dataMissing) f.addImageError();			// Count dataMissing as image error
//							}			
						}

						if (tag.getTagName().equals("Orientation")) // Orientation
						{
							orientation = tagString;
							if (f.p.p.debug.metadata && f.p.p.debug.detailed)
								PApplet.println("Found Orientation..." + orientation);
						}
						if (tag.getTagName().equals("Date/Time Original")) // Orientation
						{
							dateTime = tagString;
							if (f.p.p.debug.metadata && f.p.p.debug.detailed)
								PApplet.println("Found DateTimeOriginal..." + dateTime);
						}
						if (tag.getTagName().equals("GPS Latitude")) // Latitude
						{
							latitude = tagString;
							if (f.p.p.debug.metadata && f.p.p.debug.detailed)
								PApplet.println("Found Latitude..." + latitude);
						}
						if (tag.getTagName().equals("GPS Longitude")) // Longitude
						{
							longitude = tagString;
							if (f.p.p.debug.metadata && f.p.p.debug.detailed)
								PApplet.println("Found Longitude..." + longitude);
						}
						if (tag.getTagName().equals("GPS Altitude")) // Altitude
						{
							altitude = tagString;
							if (f.p.p.debug.metadata && f.p.p.debug.detailed)
								PApplet.println("Found Altitude..." + altitude);
						}
						if (tag.getTagName().equals("Focal Length")) // Focal Length
						{
							focalLength = tagString;
							if (f.p.p.debug.metadata && f.p.p.debug.detailed)
								PApplet.println("Found Focal Length..." + focalLength);
						}

						if (tag.getTagName().equals("Focal Length 35")) // Focal Length (35 mm. equivalent)
						{
							focalLength35 = tagString;
							if (f.p.p.debug.metadata && f.p.p.debug.detailed)
								PApplet.println("Found Focal Length 35mm Equivalent..." + focalLength);
						}
						if (tag.getTagName().equals("GPS Img Direction")) // Image Direction
						{
							direction = tagString;
							if (f.p.p.debug.metadata && f.p.p.debug.detailed)
								PApplet.println("Found Direction..." + direction);
						}
						if (tag.getTagName().equals("Image Description")) 	// Description (for Theodolite app vertical / elevation angles)
						{
							description = tagString;
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

								if(f.p.p.debug.metadata)
								{
									PApplet.println("Not a Theodolite image...");
								}
							}

							if (f.p.p.debug.metadata && f.p.p.debug.detailed)
								PApplet.println("Found Description..." + description);
						}
						if (tag.getTagName().equals("AFPointsUsed")) // Orientation
						{
							// String afPointsStr = tagString;
							// if (afPointsStr.equals("-"))
							// afPoints[0] = 0;
							// else
							// afPoints = ParseAFPointsArray(afPointsStr);
						}

						if (tag.getTagName().equals("Keywords"))
						{
							// keywordArr = json.getJSONArray("Keywords"); //
							// Assume multiple keywords
						}

						if (tag.getTagName().equals("Aperture Value")) // Aperture
						{
							//								fAperture
							//								 PApplet.println("Aperture Value (not recorded)..."+tagString);
						}

						if (tag.getTagName().equals("Brightness Value")) // Brightness
						{
							fBrightness = parseBrightness(tagString);
							if(fBrightness == -1.f && !dataMissing)
							{
								brightnessMissing = true;
							}
						}

						if (tag.getTagName().equals("Image Width")) // Orientation
							iWidth = ParseWidthOrHeight(tagString);

						if (tag.getTagName().equals("Image Height")) // Orientation
							iHeight = ParseWidthOrHeight(tagString);
					}

					pFilePath = file.getPath();

					if (directory.hasErrors()) {
						for (String error : directory.getErrors()) {
							PApplet.println("ERROR: " + error);
						}
					}
				}

				try {
					calendarTime = parseDateTime(dateTime);
				} 
				catch (RuntimeException ex) 
				{
					PApplet.println("Error in date / time... " + ex);
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
						fFocalLength = parseFocalLength(focalLength);
						fSensorSize = parseSensorSize(focalLength35);		// 29 mm for iPhone 6S+
					} 
					catch (Throwable t) // If not, must be only one keyword
					{
						PApplet.println("Throwable in camera model / focal length..." + t);
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

				/* Parse image GPS coordinates */
				try {
					float xCoord, yCoord, zCoord;
					xCoord = parseLongitude(longitude);
					yCoord = parseAltitude(altitude);
					zCoord = parseLatitude(latitude);

					if (f.p.p.utilities.isNaN(xCoord) || f.p.p.utilities.isNaN(yCoord) || f.p.p.utilities.isNaN(zCoord)) 
					{
						pLoc = new PVector(0, 0, 0);
						if(!dataMissing)
						{
							if(panorama) f.addPanoramaError();
							else f.addImageError();
							dataMissing = true;
						}						
					}
					pLoc = new PVector(xCoord, yCoord, zCoord);
				} 
				catch (RuntimeException ex) 
				{
					if (f.p.p.debug.metadata)
						PApplet.println("Error reading image location:" + name + "  " + ex);

					if(!dataMissing)
					{
						if(panorama) f.addPanoramaError();
						else f.addImageError();
						dataMissing = true;
					}					
				}

				try {
					fOrientation = ParseOrientation(orientation);
					fDirection = ParseDirection(direction);				// -- What about panoramas??
				} 
				catch (RuntimeException ex) {
					if (f.p.p.debug.metadata)
						PApplet.println("Error reading image orientation / direction:" + fOrientation + "  " + fDirection + "  " + ex);
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
			
			/* Add this 3D media object to field based on given metadata */
			try 
			{
				if(!(pLoc.x == 0.f && pLoc.y == 0.f && pLoc.z == 0.f))
				{
					if(panorama && !dataMissing)
					{
//						PApplet.println("Adding panorama #"+pCount);
						f.panoramas.add( new WMV_Panorama( f, pCount, name, pFilePath, pLoc, 0.f, 0.f, iCameraModel, 	// Ignore elevation and direction
								iWidth, iHeight, fBrightness, calendarTime, null, null ) );
						pCount++;
					}
					else if(!dataMissing)
					{
						f.images.add( new WMV_Image(f, iCount, name, pFilePath, pLoc, fDirection, fFocalLength, fOrientation, fElevation, 
								fRotation, fFocusDistance, fSensorSize, iCameraModel, iWidth, iHeight, fBrightness, calendarTime ) );
						iCount++;
					}
				}
				else
					PApplet.println("Excluded "+(panorama?"panorama:":"image:")+name);
			}
			catch (RuntimeException ex) {
				if (f.p.p.debug.metadata)
					PApplet.println("Could not add image! Error: "+ex);
			}
		}
		
		return true;
	}
	
	/** 
	 * Read tags from array of video files and create 3D video objects
	 * @param files File array
	 */
	public boolean loadVideosFromTags(File[] files) 			// Load metadata for a folder of images and add to imgs ArrayList
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
			String name = "";

			boolean dataMissing = false;
			boolean hasVideo = true;
			
			if (!videosExist || videoFiles.length <= currentMedia)		// Check if this video exists
				hasVideo = false;

			name = videoFiles[currentMedia].getName();

			Calendar calendarTime = null;			// Calendar date and time

			float fFocusDistance = f.p.defaultFocusDistance;									// Focus distance currently NOT used
			int iWidth = -1, iHeight = -1;
			float fBrightness = -1.f;
			PVector pLoc = new PVector(0, 0, 0);
			String pFilePath = "";
			
			String dateTime = null, sWidth = null, sHeight = null;
			String latitude = null, longitude = null, altitude = null;
			String duration = null;								// Video specific 
			String keyword = null;

			File file = null;				

			file = files[currentMedia];				// Get current file from array

			if(f.p.p.debug.metadata && f.p.p.debug.detailed)
				PApplet.println("Loading video: "+name);


			Map<String, String> videoMetadata = null;
			/* Read video metadata from file using ExifToolWrapper */
			try {
				videoMetadata = readVideoMetadata(file, exifToolFile);		
			}
			catch(Throwable t)
			{
				PApplet.println("Throwable while reading video metadata: " + t);
				dataMissing = true;
			}

			/* Set video variables from metadata */
			try {
				longitude = videoMetadata.get("GPSLongitude");
				latitude = videoMetadata.get("GPSLatitude");
				altitude = videoMetadata.get("GPSAltitude");
				duration = videoMetadata.get("MediaDuration");
				dateTime = videoMetadata.get("CreationDate");
				sWidth = videoMetadata.get("ImageWidth");
				sHeight= videoMetadata.get("ImageHeight");

				if(f.p.p.debug.metadata && f.p.p.debug.video && f.p.p.debug.detailed)
				{
					PApplet.println("Video latitude:"+latitude);
					PApplet.println("  longitude:"+longitude);
					PApplet.println("  altitude:"+altitude);
					PApplet.println("  duration:"+duration);
					PApplet.println("  date:"+dateTime);
					PApplet.println("  width:"+sWidth);
					PApplet.println("  height:"+sHeight);
				}

				try {
					calendarTime = parseVideoDateTime(dateTime);
				} 
				catch (Throwable t) {
					PApplet.println("Throwable while parsing date / time... " + t);
					dataMissing = true;
				}

				/* Parse video GPS coordinates */
				try {
					float xCoord, yCoord, zCoord;
					xCoord = Float.valueOf(longitude);				// Flip sign of longitude?
					yCoord = Float.valueOf(altitude);
					zCoord = Float.valueOf(latitude);				// Flip sign of latitude?

					if (f.p.p.utilities.isNaN(xCoord) || f.p.p.utilities.isNaN(yCoord) || f.p.p.utilities.isNaN(zCoord)) {
						pLoc = new PVector(0, 0, 0);
						if(!dataMissing)
						{
							f.addVideoError();
							dataMissing = true;
						}
					}
					pLoc = new PVector(xCoord, yCoord, zCoord);
				} 
				catch (RuntimeException ex) 
				{
					if (f.p.p.debug.metadata)
						PApplet.println("Error reading video location:" + name + "  " + ex);
					dataMissing = true;
				}

				iWidth = Integer.valueOf(sWidth);
				iHeight = Integer.valueOf(sHeight);
				pFilePath = file.getPath();
			} 
			catch (Throwable t) {
				PApplet.println("Throwable while extracting video EXIF data:" + t);
				if(!dataMissing)
				{
					f.addVideoError();
					dataMissing = true;
				}
			}

			/* Add 3D video object to field based on given metadata */
			try 
			{
				if(!(pLoc.x == 0.f && pLoc.y == 0.f && pLoc.z == 0.f ) && hasVideo && !dataMissing)
				{
					f.videos.add( new WMV_Video(f, vCount, name, pFilePath, pLoc, -1, -1, -1, -1, -1, fFocusDistance, 
								   				-1, iWidth, iHeight, fBrightness, calendarTime) );
					vCount++;
				}
				else if(f.p.p.debug.metadata || f.p.p.debug.video)
					PApplet.println("Excluded video:"+name);

			}
			catch (Throwable t) {
				if (f.p.p.debug.metadata)
				{
					PApplet.println("Throwable while adding video to ArrayList: "+t);
					PApplet.println("   pFilePath:" + pFilePath);
					PApplet.print("    pLoc.x:" + pLoc.x);
					PApplet.print("    pLoc.y:" + pLoc.y);
					PApplet.println("    pLoc.z:" + pLoc.z);
					PApplet.println("   dataMissing:" + dataMissing);
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
//		PApplet.println("parse model:"+model);
		if (model.equals("iPhone"))
			return 0;
		else if (model.equals("RICOH THETA S") || model.equals("RICOH THETA"))
		{
//			PApplet.println("RICOH THETA S");
			return 1;
		}
		else
		{
//			PApplet.println("NOT RICOH THETA S:"+model);
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
			PApplet.println("Image has no keyword!");
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
			if (f.p.p.debug.metadata)
				PApplet.println("afPoints[i]:" + afPoints[i]);
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

		if (f.p.p.debug.metadata) {
			PApplet.println("Not a valid afPoint: " + afPoint);
		}

		return 1000;
	}

	public Calendar parseDateTime(String input) 
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

		Calendar c = Calendar.getInstance();
		c.set(year, month, day, hour, min, sec);

		return c;
	}

	public Calendar parseVideoDateTime(String input) 
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

		Calendar c = Calendar.getInstance();
		c.set(year, month, day, hour, min, sec);

		return c;
	}

//	public float parseSoundDateTime(String input) 
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
//		PVector result = p.p.p.utilities.calculateDateTime(c); // Returns float between 0. and 1. for sunrise/sunset adjusted time
//		float time = result.y;
//
//		return time;
//	}
	
//	public int convertKeywordToElevation(String keyword) {
//	if (keyword.equals("Center"))
//		return 0;
//	else if (keyword.equals("Lower 1")) {
//		return -1;
//	} else if (keyword.equals("Lower 2")) {
//		return -2;
//	} else if (keyword.equals("Lower 3")) {
//		return -3;
//	} else if (keyword.equals("Upper 1")) {
//		return 1;
//	} else if (keyword.equals("Upper 2")) {
//		return 2;
//	} else if (keyword.equals("Upper 3")) {
//		return 3;
//	}
//	if (p.p.p.debug.metadata) {
//		System.out.println("Not an elevation keyword: " + keyword);
//	}
//
//	return 1000;
//}

//	public int ParseKeywordArrayForElevation(String input) {
//	String[] parts = input.split(",");
//	String[] keywords = new String[parts.length];
//
//	for (int i = 0; i < parts.length; i++) {
//		// String[] split = input.split("\"");
//		String[] split = parts[i].split("\"");
//		keywords[i] = split[1];
//		if (p.p.p.debug.metadata)
//			PApplet.println("keywords[i]:" + keywords[i]);
//	}
//
//	for (int i = 0; i < keywords.length; i++) {
//		int result = convertKeywordToElevation(keywords[i]);
//
//		if (result != 1000)
//			return result;
//	}
//
//	if (p.p.p.debug.metadata) {
//		System.out.println("No elevation keyword found: setting to default of 0");
//	}
//
//	return 0;
//}
	

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