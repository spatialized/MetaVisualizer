package multimediaLocator;

/******************************
 * MultimediaLocator debugging settings
 * @author davidgordon
 */
public class ML_DebugSettings 
{
	/* Debug Modes */
	public boolean detailed = false;				// Verbose debugging messages
	public boolean print = true;				// Print all user messages
	public boolean messages = false;			// On screen debug messages
	
	/* Main */
	public boolean ml = true;					// Debug MultimediaLocator class
	public boolean world = true;				// Debug WMV_World and WMV_Field class
	public boolean library = false;
	public boolean memory = false;				// Debug memory
	
	/* Model */
	public boolean metadata = false;				// Debug WMV_Metadata class
	public boolean cluster = false;				// Debug WMV_Cluster class
	public boolean time = false;				// Debug WMV_Time class
	
	/* Media */
	public boolean media = false;				// Debug WMV_Viewable class
	public boolean image = false;				// Debug WMV_Image class
	public boolean panorama = false;				// Debug WMV_Panorama class
	public boolean video = true;				// Debug WMV_Video class
	public boolean sound = false;				// Debug WMV_Sound class
	
	/* Viewer */
	public boolean viewer = false;				// Debug WMV_Viewer class
	public boolean path = false;					// Debug WMV_Viewer class

	/* Viewer */
	public boolean display = false;				// Debug WMV_Display class
	public boolean map = false;					// Debug WMV_Map class

	/* Other */
	public boolean stitching = false;			// Debug WMV_Stitcher class
	public boolean mouse = false;
	public boolean gps = true;					// Debug WMV_Viewer class
	
	ML_DebugSettings (){}
}
