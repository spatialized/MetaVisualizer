package wmViewer;

/******************************
 * @author David Gordon
 * Debug settings
 */

public class WMV_Debug 
{
	/* General */
	public boolean memory = false;				// Debug memory
	public boolean detailed = false;			// Verbose debugging messages
	public boolean print = true;				// Print all user messages
	
	/* Classes */
	public boolean main = false;				// Debug main WorldMediaViewer class
	public boolean viewer = false;				// Debug WMV_Viewer class
	public boolean field = false;				// Debug WMV_Field class
	public boolean time = false;
	public boolean model = false;				// Debug WMV_Model class
	public boolean cluster = false;				// Debug WMV_Cluster class
	public boolean display = false;				// Debug WMV_Display class
	public boolean viewable = false;			// Debug WMV_Viewable class
	public boolean image = false;				// Debug WMV_Image class
	public boolean panorama = false;			// Debug WMV_Panorama class
	public boolean video = false;				// Debug WMV_Video class
	public boolean sound = false;				// Debug WMV_Sound class
	public boolean metadata = false;			// Debug WMV_Metadata class
	public boolean stitching = false;			// Debug WMV_Stitcher class
	public boolean map = true;
	
	/* Memory */
	boolean lowMemory = false;
	boolean performanceSlow = false;
	int availableProcessors;
	long freeMemory;
	long maxMemory;
	long totalMemory;
	long allocatedMemory;
	long approxUsableFreeMemory;
	
	WorldMediaViewer p;
	
	WMV_Debug (WorldMediaViewer parent)
	{
		p = parent;
	}
	
	public void checkFrameRate()
	{
		if(p.frameRate < p.world.minFrameRate)
		{
			if(!performanceSlow)
				performanceSlow = true;
			
			if(performanceSlow && memory)
			{
				p.world.display.message("Performance slow...");
			}
		}
		else
		{
			if(performanceSlow)
				performanceSlow = false;
		}
			
	}
	
	public void checkMemory()
	{
		  availableProcessors = Runtime.getRuntime().availableProcessors();		/* Total number of processors or cores available to the JVM */
		  freeMemory = Runtime.getRuntime().freeMemory();		  /* Total amount of free memory available to the JVM */
		  maxMemory = Runtime.getRuntime().maxMemory();		  /* Maximum amount of memory the JVM will attempt to use */
		  totalMemory = Runtime.getRuntime().totalMemory();		  /* Total memory currently in use by the JVM */
		  allocatedMemory = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
		  approxUsableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;

		  if(memory)
		  {
			  if(detailed)
			  {
				  System.out.println("Total memory (bytes): " + totalMemory);
				  System.out.println("Available processors (cores): "+availableProcessors);
				  System.out.println("Maximum memory (bytes): " +  (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory)); 
				  System.out.println("Total memory (bytes): " + totalMemory);
				  System.out.println("Allocated memory (bytes): " + allocatedMemory);
			  }
			  System.out.println("Free memory (bytes): "+freeMemory);
			  System.out.println("Approx. usable free memory (bytes): " + approxUsableFreeMemory);
		  }
		  
		  if(approxUsableFreeMemory < p.world.minAvailableMemory && !lowMemory)
			  lowMemory = true;
		  if(approxUsableFreeMemory > p.world.minAvailableMemory && lowMemory)
			  lowMemory = false;
		  
		  /* Other possible memory tests: */
//		  MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
//		  MemoryUsage heap = memBean.getHeapMemoryUsage();
//		  MemoryUsage nonheap = memBean.getNonHeapMemoryUsage();
		  
		  /* Get a list of all filesystem roots on this system */
//		  File[] roots = File.listRoots();

		  /* For each filesystem root, print some info */
//		  for (File root : roots) {
//		    System.out.println("File system root: " + root.getAbsolutePath());
//		    System.out.println("Total space (bytes): " + root.getTotalSpace());
//		    System.out.println("Free space (bytes): " + root.getFreeSpace());
//		    System.out.println("Usable space (bytes): " + root.getUsableSpace());
//		  }
	}
}
