package multimediaLocator;

/**
 * Represents the current system state 
 * @author davidgordon
 *
 */
public class ML_SystemState 
{
	public boolean running = false;				// Whether simulation is running
	public boolean startup = true;				// Startup frame
	public boolean reset = false;				// Whether program was recently reset
	public boolean exit = false;				// System message to exit the program
	public boolean selectedLibrary = false;		// Whether user has selected a library folder
	public boolean openLibraryDialog = false;	// Whether library dialog should open

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
