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
	public boolean selectedMediaFolders = false;			// Whether user has selected a media folder
	public boolean librarySetup = false;					// Whether library dialog should open
	public boolean createdLibrary = false;					// Whether library has been created
	public boolean chooseLibraryDestination = false;		// Whether library destination dialog should open
	public boolean chooseMediaFolders = false;				// Whether media folder dialog should open
	
	public boolean initialClustering = false;			// Performing initial setup 
	public boolean initializingFields = false;			// Initializing fields
	public boolean fieldsInitialized = false;			// Initialized fields
	public int initializationField = 0;					// Field to be initialized this frame

	/* Graphics */
	public boolean sphericalView = false;				// 360-degree fulldome view

	/* Clustering Modes */
	public boolean interactive = false;					// In user clustering mode?
	public boolean startInteractive = false;			// Start user clustering

	public boolean export = false;
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
		selectedMediaFolders = false;				// Whether user has selected a media folder
		librarySetup = false;
		createdLibrary = false;
		chooseLibraryDestination = false;
		chooseMediaFolders = false;
		
		initialClustering = false;
		initializingFields = false;
		fieldsInitialized = false;
		initializationField = 0;
		
		sphericalView = false;
		
		export = false;
		exportCubeMap = false;
		
		reset = true;								// Set program reset flag
	}
}
