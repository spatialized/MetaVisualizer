/********************************************************************************
* GeoSynth v1.0.0
* @author davidgordon
* 
* A program for displaying large multimedia collections as 3D immersive 
* environments. Photos, panoramas and videos become virtual scenes that change
* over time.
* 
* Built using GeoMultimediaViewer, a Java library for creative visualization of
* media using temporal, spatial and orientation metadata
*********************************************************************************/

/*****************************
* GeoSynth 
* @author davidgordon
* 
* Main GMViewer app class
*/
package gmViewer;
import toxi.math.*;
import java.util.*;
import java.io.*;
import processing.core.*;
import processing.video.Movie;

public class GeoSynth extends PApplet 				// GMViewer extends PApplet class
{
	/* System Status */
	public boolean running = false;				// Is simulation running?
	public boolean startup = true;					// Startup frame

	/* World */
	GMV_World world;				// The 3D world

	/** 
	 * Load the PApplet, either in a window of specified size or in fullscreen
	 */
	static public void main(String[] args) 
	{
		PApplet.main("gmViewer.GeoSynth");									// Open in window
//		PApplet.main(new String[] { "--present", "gmViewer.GeoSynth" });	// Open in fullscreen mode
	}
	
	/** 
	 * Setup function called at startup
	 */
	public void setup()
	{
		world = new GMV_World(this);
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
	}
	
	/**
	 * Called when library folder has been selected
	 * @param selection File object for selected folder
	 */
	public void libraryFolderSelected(File selection) 
	{
		world.openLibraryFolder(selection);
	}

	/**
	 * Called when image output folder has been selected
	 * @param selection
	 */
	public void outputFolderSelected(File selection) 
	{
		if (selection == null) 
		{
			if (world.debug.main)
				println("Window was closed or the user hit cancel.");
		} 
		else 
		{
			String input = selection.getPath();

			if (world.debug.main)
				println("----> User selected output folder: " + input);

			world.outputFolder = input;
			world.outputFolderSelected = true;
		}
	}
	
	/**
	 * Called when a key is pressed
	 */
	public void keyPressed() {
		world.input.handleKeyPressed(key);
	}

	/**
	 * Called when a key is released
	 */
	public void keyReleased() {
		world.input.handleKeyReleased(key);
	}

	/**
	 * @param m Movie the event pertains to
	 */
	public void movieEvent(Movie m) 	// Called every time a new frame is available to read
	{
		try{
			if(m != null)				// Testing skipping 30th frame to avoid NullPointerException
				if(m.available())		// If a frame is available,
					m.read();			// read from disk
		}
		catch(NullPointerException npe)
		{
			if(world.debug.video)
				println("movieEvent() NullPointerException:"+npe);
		}
		catch(Throwable t)
		{
			if(world.debug.video)
				println("movieEvent() Throwable:"+t);
		}
	}
	
	public void mouseDragged() {
		if(world.display.inDisplayView())
		{
			PApplet.println("pmouseX:"+pmouseX+" pmouseY:"+pmouseY);
			PApplet.println("mouseX:"+mouseX+" mouseY:"+mouseY);
			world.input.handleMouseDragged(pmouseX, pmouseY);
		}
	}

	//	public void mousePressed()
//	{
//		if(viewer.mouseNavigation)
//			world.input.handleMousePressed(mouseX, mouseY);
//	}

//	public void mouseClicked() {
//		if(navigation.mouseNavigation)
//			world.input.handleMouseClicked(mouseX, mouseY);
//	}


//	public void mouseReleased() {
//		if(viewer.mouseNavigation)
//			world.input.handleMouseReleased(mouseX, mouseY);
//	}
}