/********************************************************************************
* WorldMediaViewer v1.0.0
* @author davidgordon
* 
* A program for displaying large multimedia collections as 3D immersive 
* environments based on spatial, temporal and orientation metadata. 
*********************************************************************************/

/************************************
* @author davidgordon 
* WMViewer main app class
*/

package wmViewer;
import java.io.*;
import processing.core.*;
import processing.video.Movie;

public class WorldMediaViewer extends PApplet 	// WMViewer extends PApplet class
{
	/* System Status */
	public boolean running = false;				// Is simulation running?
	public boolean startup = true;				// Startup frame
	public boolean exit = false;				// System message to exit the program
	public boolean selectedLibrary = false;		// Has user selected a library folder?
	
	/* Library */
	WMV_Metadata metadata;						// Metadata handler class
	WMV_Library library;						// WMViewer Media Library

	/* World */
	WMV_World world;							// The 3D World

	/* Utilities */
	WMV_Utilities utilities;					// Utility methods

	/* Debugging */
	WMV_Debug debug;							// Handles debugging functions

	/** 
	 * Load the PApplet either in a window of specified size or in fullscreen
	 */
	static public void main(String[] args) 
	{
		PApplet.main("wmViewer.WorldMediaViewer");									// Open in window
//		PApplet.main(new String[] { "--present", "wmViewer.WorldMediaViewer" });	// Open in fullscreen mode
	}
	
	/** 
	 * Setup function called at startup
	 */
	public void setup()
	{
		world = new WMV_World(this);
		metadata = new WMV_Metadata(this);
		utilities = new WMV_Utilities(world);
		debug = new WMV_Debug(this);
		
		world.initialize();
	}

	/** 
	 * Main program loop called every frame
	 */
	public void draw() 
	{		
		if (startup)
		{ 
			world.display.showStartup();			/* Startup screen */
			if (startup) startup = false;	
		}
		else if(!running) world.runSetup();		/* Run setup */
		else world.run();							/* Run program */
	}

	/**
	 * Set window resolution and graphics mode
	 */
	public void settings() 
	{
//		size(4000, 3000, processing.core.PConstants.P3D);		// Large
//		size(1980, 1080, processing.core.PConstants.P3D);
		size(1600, 900, processing.core.PConstants.P3D);		// MacBook Pro
//		size(960, 540, processing.core.PConstants.P3D);			// Web Video Large
	}
	
	/**
	 * Open library folder when folder has been selected
	 * @param selection File object for selected folder
	 */
	public void libraryFolderSelected(File selection) 
	{
		openLibraryFolder(selection);
	}

	/**
	 * Stop the program
	 */
	void stopWorldMediaViewer() 
	{
		PApplet.println("Exiting WorldMediaViewer 1.0.0...");
		exit();
	}

	/**
	 * Restart the program
	 */
	void restartWorldMediaViewer()
	{
		camera();
		setup();
		
		startup = true;
		running = false;				
		exit = false;					
		selectedLibrary = false;	
		
		world.reset();
	}
	
	/**
	 * Called when image output folder has been selected
	 * @param selection
	 */
	public void outputFolderSelected(File selection) 
	{
		if (selection == null) 
		{
			if (debug.main)
				println("Window was closed or the user hit cancel.");
		} 
		else 
		{
			String input = selection.getPath();

			if (debug.main)
				println("----> User selected output folder: " + input);

			world.outputFolder = input;
			world.outputFolderSelected = true;
		}
	}
	
	/**
	 * Analyze and load media folders given user selection
	 * @param selection Selected folder
	 */
	public void openLibraryFolder(File selection) 
	{
		boolean selectedFolder = false;
		
		if (selection == null) {
			PApplet.println("Window was closed or the user hit cancel.");
		} 
		else 
		{
			String input = selection.getPath();

			if (debug.metadata)
				PApplet.println("User selected library folder: " + input);

			library = new WMV_Library(input);

			String[] parts = input.split("/");
			
			String[] nameParts = parts[parts.length-1].split("_");		// Check if single field library 
			boolean singleField = !nameParts[0].equals("Environments");
			String parentFilePath = "";
			
			if(singleField)
			{
				String libFilePath = "";
				for(int i=0; i<parts.length-1; i++)
				{
					libFilePath = libFilePath + parts[i] + "/";
				}

				library = new WMV_Library(libFilePath);
				library.addFolder(parts[parts.length-1]);
				
				selectedFolder = true;

				for(int i=0; i<parts.length-2; i++)
					parentFilePath = parentFilePath + parts[i] + "/";
			}
			else
			{
				File libFile = new File(library.getLibraryFolder());
				String[] mediaFolderList = libFile.list();
				for(String mediaFolder : mediaFolderList)
					if(!mediaFolder.equals(".DS_Store"))
						library.addFolder(mediaFolder);

				selectedFolder = true;

				for(int i=0; i<parts.length-1; i++)
					parentFilePath = parentFilePath + parts[i] + "/";
			}

			world.loadImageMasks(parentFilePath);
			selectedFolder = true;
		}
		
		if(selectedFolder)
			selectedLibrary = true;	// Library folder has been selected
	}
	
	/**
	 * Called when a key is pressed
	 */
	public void keyPressed() 
	{
		world.input.handleKeyPressed(key);
	}

	/**
	 * Called when a key is released
	 */
	public void keyReleased() 
	{
		world.input.handleKeyReleased(key);
	}

	/**
	 * Called every time a new frame is available to read
	 * @param m Movie the event pertains to
	 */
	public void movieEvent(Movie m) 	
	{
		try{
			if(m != null)				// Testing skipping 30th frame to avoid NullPointerException
				if(m.available())		// If a frame is available,
					m.read();			// read from disk
		}
		catch(NullPointerException npe)
		{
			if(debug.video)
				println("movieEvent() NullPointerException:"+npe);
		}
		catch(Throwable t)
		{
			if(debug.video)
				println("movieEvent() Throwable:"+t);
		}
	}
	
	public void mouseDragged() {
//		if(world.mouseNavigation)
//		{
//			if(world.display.inDisplayView())
//			{
//				PApplet.println("pmouseX:"+pmouseX+" pmouseY:"+pmouseY);
//				PApplet.println("mouseX:"+mouseX+" mouseY:"+mouseY);
//				world.input.handleMouseDragged(pmouseX, pmouseY);
//			}
//		}
	}

	public void mousePressed()
	{
		if(world.viewer.mouseNavigation)
			world.input.handleMousePressed(mouseX, mouseY);
	}


	public void mouseReleased() {
		if(world.viewer.mouseNavigation)
			world.input.handleMouseReleased(mouseX, mouseY);
	}
	
	public void mouseClicked() {
		if(world.viewer.mouseNavigation)
			world.input.handleMouseClicked(mouseX, mouseY);
	}
	
	
	/**
	 * gpsTrackSelected()
	 * Called when user selects a GPS track file
	 * @param selection Selected GPS Track file
	 */
	public void gpsTrackSelected(File selection)
	{
		world.viewer.loadGPSTrack(selection);
	}
}