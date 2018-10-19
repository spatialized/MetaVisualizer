package main.java.com.entoptic.metaVisualizer.system;

/**
 * Current system state 
 * @author davidgordon
 *
 */
public class MV_SystemState 
{
	/* Advanced Mode */
	public boolean advanced = true;				// Advanced Mode
	
	/* General */
	public boolean startup = true;				// Startup frame
	public boolean running = false;				// Whether simulation is running
	public boolean startedRunning = false;		// Program just started running
	public int framesSinceStart = 0;				// Frames since simulation start
	public boolean singleField = false;							// Loaded single field of a library

	public boolean reset = false;				// Whether program was recently reset
	public boolean exit = false;					// System message to exit the program
	public boolean hints = true;					// Whether to show hints		-- Used?

	/* Setup */
	public boolean selectedLibrary = false;					// Whether user has selected a library folder
	public boolean selectedNewLibraryDestination = false;	// Whether user has selected a library destination folder
	public boolean selectedNewLibraryMedia = false;			// Whether user has selected a media folder
	public boolean inLibrarySetup = false;					// Whether library dialog should open
	public boolean rebuildLibrary = false;					// Whether to rebuild library before opening
	
	public boolean createdLibrary = false;					// Whether library has been created
	public boolean chooseLibraryDestination = false;		// Whether library destination dialog should open
	public boolean chooseMediaFolders = false;				// Whether media folder dialog should open
	
	public boolean inFieldInitialization = false;			// Performing initial setup 
	public boolean initializingFields = false;				// Initializing fields
	public boolean fieldsInitialized = false;				// Initialized fields
	
	public boolean gettingExiftoolPath = false;				// Whether getting path to Exiftool from user
	public boolean libraryNamed = false;						// Whether new library has been named
	public boolean fieldsNamed = true; 						// Whether new library fields have been named
	public boolean inFieldNaming = false;					// Whether currently naming fields
	public String oldFieldName = "";
	
	public int initializationField = 0;						// Field to be initialized 
	public int namingField = 0;								// Field to be named 

	/* Graphics */
	public boolean sphericalView = false;				// 360-degree fulldome view	-- In progress

	/* Clustering Modes */
	public boolean startInteractive = false;			// Whether started Interactive Clustering Mode
	public boolean interactive = false;					// Whether in Interactive Clustering Mode 	-- Disabled

	public boolean export = false;						// Whether to export screenshot
	public boolean exportMedia = false;					// Whether to export selected media
	public boolean exportCubeMap = false;				// Whethe to export cubemap -- In progress

	public MV_SystemState(){}
	
	/**
	 * Reset system state
	 */
	public void reset()
	{
		advanced = true;				// Advanced Mode

		startup = true;
		running = false;				
		startedRunning = false;
		framesSinceStart = 0;						
		singleField = false;							// Loaded single field of a library

		reset = true;
		exit = false;
		hints = true;
		
		selectedLibrary = false;	
		selectedNewLibraryDestination = false;		
		selectedNewLibraryMedia = false;			
		inLibrarySetup = false;
		rebuildLibrary = false;
		
		createdLibrary = false;
		chooseLibraryDestination = false;
		chooseMediaFolders = false;
		
		inFieldInitialization = false;
		initializingFields = false;
		fieldsInitialized = false;
		
		gettingExiftoolPath = false;			
		libraryNamed = false;					
		fieldsNamed = true; 					
		inFieldNaming = false;					
		oldFieldName = "";

		initializationField = 0;
		namingField = 0;								

		sphericalView = false;
		
		startInteractive = false;			// Whether started Interactive Clustering Mode
		interactive = false;					// Whether in Interactive Clustering Mode 	-- Disabled

		export = false;
		exportMedia = false;					// Whether to export selected media
		exportCubeMap = false;
	}
}
