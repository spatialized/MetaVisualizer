package gmViewer;

/******************************
 * GMV_Debug 
 * @author David Gordon
 * Debug settings
 */

public class GMV_Debug 
{
	/* General */
	public boolean memory = false;				// Debug memory
	public boolean detailed = false;			// Use verbose debugging messages
	public boolean print = true;				// Print all user messages
	
	/* Classes */
	public boolean main = true;					// Debug main GMViewer class
	public boolean viewer = false;				// Debug GMV_Viewer class
	public boolean field = false;				// Debug GMV_Field class
	public boolean time = false;
	public boolean model = false;				// Debug GMV_Model class
	public boolean cluster = false;				// Debug GMV_Cluster class
	public boolean display = false;				// Debug GMV_Display class
	
	public boolean viewable = false;			// Debug GMV_Viewable class
	public boolean image = false;				// Debug GMV_Image class
	public boolean panorama = true;				// Debug GMV_Panorama class
	public boolean video = false;				// Debug GMV_Video class
	public boolean metadata = true;				// Debug GMV_Metadata class
	
	/* Interactive Debugging */
	public boolean hideImages = false;
	public boolean hidePanoramas = false;
	public boolean hideVideos = false;

	/* Memory */
	int availableProcessors;
	long freeMemory;
	long maxMemory;
	long totalMemory;
	long allocatedMemory;
	long approxUsableFreeMemory;
	
	boolean lowMemory = false;
	boolean performanceSlow = false;
	
	GeoSynth p;
	
	GMV_Debug (GeoSynth parent)
	{
		p = parent;
	}
	
	public void checkFrameRate()
	{
		if(p.frameRate < p.minFrameRate)
		{
			if(!performanceSlow)
				performanceSlow = true;
			
			if(performanceSlow && memory)
			{
				p.display.message("Performance slow...");
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
		  
		  if(approxUsableFreeMemory < p.minAvailableMemory && !lowMemory)
			  lowMemory = true;
		  if(approxUsableFreeMemory > p.minAvailableMemory && lowMemory)
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
