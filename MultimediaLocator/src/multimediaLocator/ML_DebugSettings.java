package multimediaLocator;

/******************************
 * Debug settings for MultimediaLocator
 * @author davidgordon
 */

public class ML_DebugSettings 
{
	/* General */
	public boolean memory = false;				// Debug memory
	public boolean detailed = false;			// Verbose debugging messages
	public boolean print = false;				// Print all user messages
	
	/* Data */
	public boolean main = true;				// Debug WorldMediaViewer class
	public boolean data = false;				// Debug WorldMediaViewer class
	public boolean metadata = false;			// Debug WMV_Metadata class

	/* Model */
	public boolean time = false;				// Debug WMV_Time class
	public boolean field = false;				// Debug WMV_Field class
	public boolean cluster = false;				// Debug WMV_Cluster class

	/* Viewer */
	public boolean viewer = false;				// Debug WMV_Viewer class
	public boolean path = false;				// Debug WMV_Viewer class
	public boolean display = false;				// Debug WMV_Display class
	
	/* Media */
	public boolean media = false;			// Debug WMV_Viewable class
	public boolean image = false;				// Debug WMV_Image class
	public boolean panorama = false;			// Debug WMV_Panorama class
	public boolean video = false;				// Debug WMV_Video class
	public boolean sound = false;				// Debug WMV_Sound class

	/* Other */
	public boolean map = false;					// Debug WMV_Map class
	public boolean stitching = false;			// Debug WMV_Stitcher class
	
	/* Memory */
	public boolean lowMemory = false;
	public boolean performanceSlow = false;
	public int availableProcessors;
	public long freeMemory;
	public long maxMemory;
	public long totalMemory;
	public long allocatedMemory;
	public long approxUsableFreeMemory;
	
	ML_DebugSettings (){}
}
