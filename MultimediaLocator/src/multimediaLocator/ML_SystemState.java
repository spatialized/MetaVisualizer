package multimediaLocator;

/**
 * Current system state 
 * @author davidgordon
 *
 */
public class ML_SystemState 
{
	/* General */
	public boolean startup = true;				// Startup frame
	public boolean running = false;				// Whether simulation is running
	public boolean startedRunning = false;		// Program just started running
	public int framesSinceStart = 0;			// Frames since simulation start
	
	public boolean reset = false;				// Whether program was recently reset
	public boolean exit = false;				// System message to exit the program
	
	public boolean hints = true;				// Whether to show hints

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
	public boolean libraryNamed = false;		// Whether new library has been named
	public boolean fieldsNamed = true; 			// Whether new library fields have been named
	public boolean inFieldNaming = false;		// Whether currently naming fields
	public String oldFieldName = "";
	
	public int initializationField = 0;					// Field to be initialized this frame
	public int namingField = 0;					// Field to be initialized this frame

	/* Graphics */
	public boolean sphericalView = false;				// 360-degree fulldome view

	/* Clustering Modes */
	public boolean interactive = false;					// In user clustering mode?
	public boolean startInteractive = false;			// Start user clustering

	public boolean export = false;
	public boolean exportMedia = false;
	public boolean exportCubeMap = false;

	ML_SystemState(){}
	
	public void reset()
	{
		startup = true;
		running = false;				
		startedRunning = false;				
		exit = false;		
		
		selectedLibrary = false;	
		selectedNewLibraryDestination = false;		// Whether user has selected a library destination folder
		selectedNewLibraryMedia = false;				// Whether user has selected a media folder
		inLibrarySetup = false;
		createdLibrary = false;
		chooseLibraryDestination = false;
		chooseMediaFolders = false;
		
		inFieldInitialization = false;
		initializingFields = false;
		fieldsInitialized = false;
		initializationField = 0;
		
		sphericalView = false;
		
		export = false;
		exportCubeMap = false;
		
		reset = true;								// Set program reset flag
	}
}
