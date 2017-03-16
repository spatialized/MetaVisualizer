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

import g4p_controls.GButton;
import g4p_controls.GEvent;
import g4p_controls.GToggleControl;
import g4p_controls.GValueControl;
import g4p_controls.GWinData;
import processing.core.*;
import processing.video.Movie;

public class WorldMediaViewer extends PApplet 	// WMViewer extends PApplet class
{
	/* System Status */
	public WMV_SystemState state = new WMV_SystemState();
	
	/* System Modes */
	public boolean basic = false;				// Minimal mode with no windows
	
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
		
		if(debug.main)
			PApplet.println("Finished setup...");
	}

	/** 
	 * Main program loop called every frame
	 */
	public void draw() 
	{		
		if (state.startup)
		{
			if(state.reset) reset();
			else world.display.showStartup();	/* Startup screen */
			state.startup = false;	
		}
		else if(!state.running)
		{
//			noLoop();
			world.setup();						/* Run setup */
//			loop();
		}
		else
		{
			world.run();						/* Run program */
		}
	}
	
	/**
	 * Set window resolution and graphics mode
	 */
	public void settings() 
	{
//		size(1980, 1080, processing.core.PConstants.P3D);		// 
		size(1680, 960, processing.core.PConstants.P3D);		// MacBook Pro
//		size(1360, 940, processing.core.PConstants.P3D);		// MacBook Pro
//		size(960, 540, processing.core.PConstants.P3D);			// Web Video Large
	}
	
	public void reset()
	{
		background(0.f);
		world.display.window.hideWindows();
		world.reset();
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
	void restart()
	{
		state.reset();
		world.viewer.initialize(0,0,0);
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

			world.stitchingPath = parentFilePath + "stitched/";					// -- Move this to library!
//			boolean success = world.loadImageMasks(parentFilePath);
			world.loadImageMasks();					
			
			selectedFolder = true;
		}
		
		if(selectedFolder)
			state.selectedLibrary = true;	// Library folder has been selected
		else
		{
			state.selectedLibrary = false;				// Library in improper format if masks are missing
			selectFolderPrompt();					// Retry folder prompt
		}
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
	
	/* Input Handling */
	public void mouseMoved()
	{
//		if(world.display.displayView == 1)
//			if(world.display.satelliteMap)
//				world.display.map2D.handleMouseMoved(mouseX, mouseY);
	}
	
	/**
	 * Called when mouse is pressed
	 */
	public void mousePressed()
	{
//		if(world.viewer.mouseNavigation)
//			world.input.handleMousePressed(mouseX, mouseY);
	}

	/**
	 * Called when mouse is released
	 */
	public void mouseReleased() {
//		if(world.viewer.mouseNavigation)
//			world.input.handleMouseReleased(mouseX, mouseY);
		if(world.display.displayView == 1)
			world.input.handleMouseReleased(mouseX, mouseY);
	}
	
	/**
	 * Called when mouse is clicked
	 */
	public void mouseClicked() {
//		if(world.viewer.mouseNavigation)
//			world.input.handleMouseClicked(mouseX, mouseY);
	}
	
	/**
	 * Called when mouse is dragged
	 */
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

	public void handleButtonEvents(GButton button, GEvent event) { 
		world.input.handleButtonEvent(button, event);
	}
	
	public void handleToggleControlEvents(GToggleControl option, GEvent event) {
		world.input.handleToggleControlEvent(option, event);
	}
	
	public void handleSliderEvents(GValueControl slider, GEvent event) 
	{ 
		world.input.handleSliderEvent(slider, event);
	}

	/**
	 * Processing method called when a key is pressed
	 */
	public void keyPressed() 
	{
		world.input.handleKeyPressed(key, keyCode);
	}

	/**
	 * Processing method called when a key is released
	 */
	public void keyReleased() 
	{
		world.input.handleKeyReleased(key, keyCode);
	}
	
	public void wmvWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			world.input.handleKeyPressed(keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			world.input.handleKeyReleased(keyevent.getKey(), keyevent.getKeyCode());
	}

	public void timeWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			world.input.handleKeyPressed(keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			world.input.handleKeyReleased(keyevent.getKey(), keyevent.getKeyCode());
	}

	public void navigationWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			world.input.handleKeyPressed(keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			world.input.handleKeyReleased(keyevent.getKey(), keyevent.getKeyCode());
	}
	
	public void graphicsWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			world.input.handleKeyPressed(keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			world.input.handleKeyReleased(keyevent.getKey(), keyevent.getKeyCode());
	}
	
	public void memoryWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			world.input.handleKeyPressed(keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			world.input.handleKeyReleased(keyevent.getKey(), keyevent.getKeyCode());
	}
	
	public void modelWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			world.input.handleKeyPressed(keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			world.input.handleKeyReleased(keyevent.getKey(), keyevent.getKeyCode());
	}
	
	public void selectionWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			world.input.handleKeyPressed(keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			world.input.handleKeyReleased(keyevent.getKey(), keyevent.getKeyCode());
	}
	
	public void statisticsWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			world.input.handleKeyPressed(keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			world.input.handleKeyReleased(keyevent.getKey(), keyevent.getKeyCode());
	}
	
	public void helpWindowKey(PApplet applet, GWinData windata, processing.event.KeyEvent keyevent)
	{
		if(keyevent.getAction() == processing.event.KeyEvent.PRESS)
			world.input.handleKeyPressed(keyevent.getKey(), keyevent.getKeyCode());
		if(keyevent.getAction() == processing.event.KeyEvent.RELEASE)
			world.input.handleKeyReleased(keyevent.getKey(), keyevent.getKeyCode());
	}
	
	public void selectFolderPrompt()
	{
		selectFolder("Select library folder:", "libraryFolderSelected");		// Get filepath of PhotoSceneLibrary folder
	}
	
	/**
	 * Called when user selects a GPS track file
	 * @param selection Selected GPS Track file
	 */
	public void gpsTrackSelected(File selection)
	{
		world.viewer.loadGPSTrack(selection);
	}
	
	public void setSurfaceSize(int newWidth, int newHeight)
	{
		surface.setResizable(true);
		surface.setSize(newWidth, newHeight);
		surface.setResizable(false);
	}

//	public void setSurfaceLocation(int newX, int newY)
//	{
//		surface.setLocation(newX, newY);
//	}
	
	public void setSurfaceVisible(boolean newState)
	{
		surface.setVisible(newState);
	}
}