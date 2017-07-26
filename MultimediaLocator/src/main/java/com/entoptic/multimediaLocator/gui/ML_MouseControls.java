package main.java.com.entoptic.multimediaLocator.gui;

import main.java.com.entoptic.multimediaLocator.world.WMV_World;

/******************************
 * Mouse handler
 * @author davidgordon
 *
 */
public class ML_MouseControls
{
//	private ML_Input input;	// Input class
	
	public boolean mouseClickedRecently = false;
	public boolean mouseReleased = false;
	public int clickedRecentlyFrame = 1000000;
	public int doubleClickSpeed = 10;

	/**
	 * Constructor for mouse handler
	 * @param parent
	 */
//	ML_MouseControls(ML_Input parent)
	ML_MouseControls()
	{
//		input = parent;
	}
	
	/* Mouse */
	public void updateMouseSelection(int mouseX, int mouseY, int frameCount)
	{
		if(frameCount - clickedRecentlyFrame > doubleClickSpeed && mouseClickedRecently)
			mouseClickedRecently = false;
		
		if(frameCount - clickedRecentlyFrame > 20 && !mouseReleased)
			System.out.println("Held mouse...");
	}

	/**
	 * Handle mouse released event
	 * @param world Parent world
	 * @param display Display object
	 * @param mouseX Mouse x location
	 * @param mouseY Mouse y location
	 * @param frameCount Current frame 
	 */
	void handleMouseReleased(WMV_World world, ML_Display display, int mouseX, int mouseY, int frameCount)
	{
		mouseReleased = true;
//		releasedRecentlyFrame = frameCount;
		
		boolean doubleClick = false;
		if(mouseClickedRecently)							// Double click
			doubleClick = true;

		if(world.viewer.getSettings().mouseNavigation)
		{
			world.viewer.walkSlower();
			world.viewer.getState().lastMovementFrame = frameCount;
			if(doubleClick)									
				world.viewer.moveToNearestCluster(world.viewer.getNavigationTeleport());
		}
		
		if(display.getDisplayView() == 1)
		{
			display.handleMapViewMouseReleased(world, mouseX, mouseY);
//			display.map2D.handleMouseReleased(world, mouseX, mouseY);
		}
		else if(display.getDisplayView() == 2)
			display.handleTimeViewMouseReleased(world, mouseX, mouseY);
		else if(display.getDisplayView() == 3)
			display.handleLibraryViewMouseReleased(world, mouseX, mouseY);
		else if(display.getDisplayView() == 4)
			display.handleMediaViewMouseReleased(world, mouseX, mouseY);
	}
	
	/**
	 * Respond to mouse clicked event
	 * @param mouseX Mouse x location
	 * @param mouseY Mouse y location
	 * @param frameCount Current frame
	 */
	void handleMouseClicked(int mouseX, int mouseY, int frameCount)
	{
		mouseClickedRecently = true;
		clickedRecentlyFrame = frameCount;
		mouseReleased = false;
	}

	void handleMousePressed(WMV_World world, ML_Display display, int mouseX, int mouseY, int frameCount)
	{
		if(display.getDisplayView() == 1)
			display.handleMapViewMousePressed(world, mouseX, mouseY);
		if(display.getDisplayView() == 2)
			display.handleTimeViewMousePressed(world, mouseX, mouseY);
//		else if(display.getDisplayView() == 3)
//			display.handleLibraryViewMousePressed(world, mouseX, mouseY);
//		else if(display.getDisplayView() == 4)
//			display.handleMediaViewMousePressed(world, mouseX, mouseY);

		
		
		
//		if(!viewer.getSettings().orientationMode && viewer.getState().lastMovementFrame > 5)
//		{
//			if(mouseX > screenWidth * 0.25 && mouseX < screenWidth * 0.75 && mouseY < screenHeight * 0.75 && mouseY > screenHeight * 0.25)
//				viewer.walkForward();
//			viewer.getState().lastMovementFrame = frameCount;
//		}
//		else viewer.moveToNextCluster(false, -1);
	}

	void handleMouseDragged(int mouseX, int mouseY)
	{
//		mouseOffsetX = mouseClickedX - mouseX;
//		mouseOffsetY = mouseClickedY - mouseY;
//		viewer.lastMovementFrame = frameCount;			// Turn faster if larger offset X or Y?
	}
	
	/**
	 * Update mouse navigation									//-- Disabled
	 * @param viewer
	 * @param mouseX
	 * @param mouseY
	 * @param frameCount
	 */
//	void updateMouseNavigation(WMV_Viewer viewer, int mouseX, int mouseY, int frameCount)
//	{			
//		if(frameCount - clickedRecentlyFrame > doubleClickSpeed && mouseClickedRecently)
//		{
//			mouseClickedRecently = false;
////			mouseReleasedRecently = false;
//		}
//		
//		if(frameCount - clickedRecentlyFrame > 20 && !mouseReleased)
//			viewer.addPlaceToMemory();				// Held mouse
//		
//		if (mouseX < screenWidth * 0.25 && mouseX > -1) 
//		{
//			if(!viewer.turningX())
//				viewer.turnXToAngle(PApplet.radians(5.f), -1);
//		}
//		else if (mouseX > screenWidth * 0.75 && mouseX < screenWidth + 1) 
//		{
//			if(!viewer.turningX())
//				viewer.turnXToAngle(PApplet.radians(5.f), 1);
//		}
//		else if (mouseY < screenHeight * 0.25 && mouseY > -1) 
//		{
//			if(!viewer.turningY())
//				viewer.turnYToAngle(PApplet.radians(5.f), -1);
//		}
//		else if (mouseY > screenHeight * 0.75 && mouseY < screenHeight + 1) 
//		{
//			if(!viewer.turningY())
//				viewer.turnYToAngle(PApplet.radians(5.f), 1);
//		}
//		else
//		{
//			if(viewer.turningX()) viewer.setTurningX( false );
//			if(viewer.turningY()) viewer.setTurningY( false );
//		}
//	}
}
