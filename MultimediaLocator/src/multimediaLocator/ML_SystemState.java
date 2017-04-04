package multimediaLocator;

/**
 * Current system state 
 * @author davidgordon
 *
 */
public class ML_SystemState 
{
	/* General */
	public boolean running = false;				// Whether simulation is running
	public boolean startedRunning = false;		// Program just started running
	public boolean startup = true;				// Startup frame
	public boolean reset = false;				// Whether program was recently reset
	public boolean exit = false;				// System message to exit the program

	/* Setup */
	public boolean selectedLibrary = false;		// Whether user has selected a library folder
	public boolean chooseLibrary = false;	// Whether library dialog should open
	public boolean initialSetup = false;		// Performing initial setup 
	public boolean initializingFields = false;	// Initializing fields
	public boolean fieldsInitialized = false;	// Initialized fields
	public int initializationField = 0;			// Field to be initialized this frame
//	public int setupProgress = 0;				// Setup progress (0 to 100)

	/* Clustering Modes */
	public boolean interactive = false;					// In user clustering mode?
	public boolean startInteractive = false;			// Start user clustering

	public boolean export = false;

	ML_SystemState(){}
	
	public void reset()
	{
		running = false;				
		selectedLibrary = false;	
		reset = true;
		startup = true;
		exit = false;					
	}
}
