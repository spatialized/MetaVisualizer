package multimediaLocator;

/******************************
 * MultimediaLocator debugging settings
 * @author davidgordon
 */

public class ML_DebugSettings 
{
	/* General */
	public boolean memory = false;				// Debug memory
	public boolean detailed = true;				// Verbose debugging messages
	public boolean print = true;				// Print all user messages
	
	/* Data */
	public boolean ml = true;					// Debug MultimediaLocator class
	public boolean world = true;				// Debug WMV_Field class

	/* Model */
	public boolean metadata = true;			// Debug WMV_Metadata class
	public boolean cluster = false;				// Debug WMV_Cluster class
	public boolean time = false;				// Debug WMV_Time class

	/* Viewer */
	public boolean viewer = true;				// Debug WMV_Viewer class
	public boolean path = true;					// Debug WMV_Viewer class
	public boolean gps = true;					// Debug WMV_Viewer class
	public boolean display = false;				// Debug WMV_Display class
	
	/* Media */
	public boolean media = true;				// Debug WMV_Viewable class
	public boolean image = false;				// Debug WMV_Image class
	public boolean panorama = false;			// Debug WMV_Panorama class
	public boolean video = true;				// Debug WMV_Video class
	public boolean sound = false;				// Debug WMV_Sound class

	/* Other */
	public boolean map = false;					// Debug WMV_Map class
	public boolean stitching = false;			// Debug WMV_Stitcher class

	ML_DebugSettings (){}
}
