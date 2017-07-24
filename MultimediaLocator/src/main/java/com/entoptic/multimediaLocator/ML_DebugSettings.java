package main.java.com.entoptic.multimediaLocator;

/******************************
 * Program debug settings
 * @author davidgordon
 */
public class ML_DebugSettings 
{
	/* Debug Mode */
	public boolean detailed = true;				// Verbose Debug Mode On/Off
	
	/* Output Methods */
	public boolean print = true;					// Print all user messages
	public boolean messages = false;				// On screen debug messages
	public boolean file = true;					// Output debug messages to file
	
	/* Main */
	public boolean ml = true;					// Debug MultimediaLocator class
	public boolean world = false;				// Debug WMV_World and WMV_Field classes
	public boolean input = false;				// Debug MultimediaLocator class
	public boolean library = false;				// Debug library and file handling
	public boolean viewer = true;				// Debug WMV_Viewer class
	public boolean time = true;					// Debug time behavior
	public boolean gps = false;					// Debug WMV_Viewer class
	
	/* Memory */
	public boolean memory = false;				// Debug memory

	/* Input */
	public boolean mouse = false;				// Debug mouse interaction
	
	/* Model */
	public boolean metadata = false;				// Debug WMV_Metadata class
	public boolean cluster = false;				// Debug WMV_Cluster class
	
	/* Media */
	public boolean media = false;				// Debug WMV_Viewable class
	public boolean image = false;				// Debug WMV_Image class
	public boolean panorama = false;				// Debug WMV_Panorama class
	public boolean video = false;				// Debug WMV_Video class
	public boolean sound = false;				// Debug WMV_Sound class
	
	/* Viewer */
	public boolean path = false;					// Debug WMV_Viewer class

	/* Viewer */
	public boolean display = false;				// Debug WMV_Display class
	public boolean map = false;					// Debug WMV_Map class

	/* Other */
	public boolean stitching = false;			// Debug WMV_Stitcher class			-- Disabled
	
	ML_DebugSettings (){}
}
