package multimediaLocator;

/******************************
 * MultimediaLocator debugging settings
 * @author davidgordon
 */

public class ML_DebugSettings 
{
	/* General */
	public boolean memory = false;				// Debug memory
	public boolean detailed = false;			// Verbose debugging messages
	public boolean print = true;				// Print all user messages
	
	/* Data */
	public boolean main = true;				// Debug WorldMediaViewer class
	public boolean metadata = false;			// Debug WMV_Metadata class
	public boolean data = false;				// Debug WorldMediaViewer class

	/* Model */
	public boolean field = true;				// Debug WMV_Field class
	public boolean cluster = false;				// Debug WMV_Cluster class
	public boolean time = false;				// Debug WMV_Time class

	/* Viewer */
	public boolean viewer = false;				// Debug WMV_Viewer class
	public boolean path = false;				// Debug WMV_Viewer class
	public boolean display = false;				// Debug WMV_Display class
	
	/* Media */
	public boolean media = false;				// Debug WMV_Viewable class
	public boolean image = false;				// Debug WMV_Image class
	public boolean panorama = false;			// Debug WMV_Panorama class
	public boolean video = false;				// Debug WMV_Video class
	public boolean sound = false;				// Debug WMV_Sound class

	/* Other */
	public boolean map = false;					// Debug WMV_Map class
	public boolean stitching = false;			// Debug WMV_Stitcher class

	ML_DebugSettings (){}
}
