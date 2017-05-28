package multimediaLocator;

import g4p_controls.GButton;
import g4p_controls.GEvent;
import g4p_controls.GToggleControl;
import g4p_controls.GValueControl;
import processing.core.PApplet;

/**************************************
 * Methods for responding to user input
 * @author davidgordon
 */
public class ML_Input
{
	/* Classes */
	ML_KeyboardControls keyboardInput;					/* Keyboard input class */
	
	public boolean shiftKey = false;
	public boolean optionKey = false;
	
	private boolean mouseClickedRecently = false;
	private boolean mouseReleased = false;
	private int clickedRecentlyFrame = 1000000;
	private int doubleClickSpeed = 10;

	private int screenWidth, screenHeight;

	/**
	 * Constructor for input class
	 * @param newScreenWidth
	 * @param newScreenHeight
	 */
	public ML_Input(int newScreenWidth, int newScreenHeight) 
	{
		screenWidth = newScreenWidth;
		screenHeight = newScreenHeight;
		
		keyboardInput = new ML_KeyboardControls(this);
	}

	/**
	 * Respond to key pressed
	 * @param ml Parent app
	 * @param key Key that was pressed
	 * @param keyCode Key code
	 */
	void handleKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		if (ml.state.running && ml.state.selectedLibrary)
		{
			/* General */
			keyboardInput.handleUniversalKeyPressed(ml, key, keyCode);

			if(ml.display.displayView == 1)						 /* 2D Map View */
				keyboardInput.handleMapViewKeyPressed(ml, key, keyCode);
			else if(ml.display.displayView == 2)				 /* Time View */
				keyboardInput.handleTimelineViewKeyPressed(ml, key, keyCode);
			else if(ml.display.displayView == 3)				 /* Library View */
				keyboardInput.handleLibraryViewKeyPressed(ml, key, keyCode);

			if (ml.state.interactive)							 /* World View Controls */
				keyboardInput.handleInteractiveClusteringKeyPressed(ml, key, keyCode);
			else 						// Interactive Clustering Mode
			{
				keyboardInput.handleGeneralKeyPressed(ml, key, keyCode);	 	 /* Controls for both 3D + HUD Views */

				if(!ml.display.inDisplayView())							
					keyboardInput.handleWorldViewKeyPressed(ml, key, keyCode); /* Controls only for World View */
			}
		}
	}
	
	/**
	 * Respond to user key releases
	 */
	void handleKeyReleased(WMV_Viewer viewer, ML_Display display, char key, int keyCode)
	{
		keyboardInput.handleKeyReleased(viewer, display, key, keyCode);
	}
	
	/**
	 * Handle input from all sliders
	 * @param world Current world
	 * @param display 2D display object
	 * @param slider Slider that triggered the event
	 * @param event The slider event
	 */
	public void handleSliderEvent(WMV_World world, ML_Display display, GValueControl slider, GEvent event)
	{
		if(display.window.setupNavigationWindow)
		{
			if (slider.tag == "TeleportLength")
				world.viewer.setTeleportLength( slider.getValueI() );
			if (slider.tag == "PathWaitLength")
				world.viewer.setPathWaitLength( slider.getValueI() );
		}
		if(display.window.setupTimeWindow)
		{
			if (slider.tag == "MediaLength") 
				world.settings.defaultMediaLength = slider.getValueI();
			
			if (slider.tag == "TimeCycleLength") 
			{
				switch(world.state.timeMode)
				{
					case 0:												// Cluster
						world.setAllClustersTimeCycleLength(slider.getValueI());
						break;
					case 1:												// Field
						world.settings.timeCycleLength = slider.getValueI();
						world.settings.timeInc = world.settings.timeCycleLength / 30.f;			
						break;
					case 2:												// Media
						break;
					default:
						break;
				}
			}

			if (slider.tag == "CurrentTime") 
				world.setCurrentTime(slider.getValueF());
			
			if (slider.tag == "VisibleTimeInterval") 
				world.settings.timeVisibleInterval = slider.getValueF();
		}
		
		if(display.window.setupGraphicsWindow)
		{
			if (slider.tag == "VisibleAngle") 
				world.viewer.setVisibleAngle( slider.getValueF() );
			
			if (slider.tag == "Alpha") 
				world.state.alpha = slider.getValueF();
			
			if (slider.tag == "Brightness") 
				world.viewer.setUserBrightness( slider.getValueF() );
		}
		
		if (slider.tag == "AltitudeScaling") 
		{
			if(display.window.setupModelWindow)
			{
				world.settings.altitudeScalingFactor = PApplet.round(slider.getValueF() * 1000.f) * 0.001f;
				world.getCurrentField().calculateMediaLocations(true);		// Recalculate media locations
				world.getCurrentField().recalculateGeometries();		// Recalculate media geometries at new locations
				world.getCurrentField().createClusters();				// Recalculate cluster locations
			}
		}
	}

	/**
	 * Handle button input
	 * @param ml Parent App
	 * @param display Display object
	 * @param button GButton that was pressed
	 * @param event GEvent that occurred
	 */
	public void handleButtonEvent(MultimediaLocator ml, ML_Display display, GButton button, GEvent event) 
	{ 
		if(event == GEvent.CLICKED)
		{
			switch(button.tag) 
			{
			/* General */
			case "Restart":
				ml.restart();
				break;

				/* Library */
			case "CreateLibrary":
				ml.createNewLibrary = true;
				ml.state.chooseMediaFolders = true;
				ml.state.librarySetup = true;
				display.window.hideLibraryWindow();
				break;

			case "OpenLibrary":
				if(ml.createNewLibrary) ml.createNewLibrary = false;
				ml.state.librarySetup = true;
				display.window.hideLibraryWindow();
				break;

			case "AddMediaFolder":
				ml.mediaFolderDialog();
				break;

			case "MakeLibrary":
				ml.state.selectedMediaFolders = true;			// Media folder has been selected
				ml.state.chooseMediaFolders = false;			// No longer choose a media folder
				ml.state.chooseLibraryDestination = true;		// Choose library destination folder
				break;

				/* Navigation */
			case "OpenNavigationWindow":
				display.window.openNavigationWindow();
				break;

			case "CloseNavigationWindow":
				display.window.hideNavigationWindow();
				break;

			case "NearestCluster":
				ml.world.viewer.moveToNearestCluster(ml.world.viewer.getMovementTeleport());
				break;
			case "RandomCluster":
				ml.world.viewer.moveToRandomCluster(ml.world.viewer.getMovementTeleport(), true);
				break;
			case "LastCluster":
				ml.world.viewer.moveToLastCluster(ml.world.viewer.getMovementTeleport());
				break;
			case "NextField":
				if(display.displayView == 1)
					ml.world.viewer.teleportToFieldOffset(1, true, false);
				else
					ml.world.viewer.teleportToFieldOffset(1, true, true);
				break;
			case "PreviousField":
				if(display.displayView == 1)
					ml.world.viewer.teleportToFieldOffset(-1, true, false);
				else
					ml.world.viewer.teleportToFieldOffset(-1, true, true);
				break;
			case "ImportGPSTrack":
				ml.world.viewer.importGPSTrack();						// Select a GPS tracking file from disk to load and navigate 
				break;

			case "FollowStart":
				if(!ml.world.viewer.isFollowing())
				{
					switch(ml.world.viewer.getFollowMode())
					{
					case 0:
						ml.world.viewer.followTimeline(true, false);
						break;
					case 1:
						ml.world.viewer.followGPSTrack();
						break;
					case 2:
						ml.world.viewer.followMemory();
						break;
					}
				}
				break;

			case "FollowStop":
				ml.world.viewer.stopFollowing();
				break;

				/* Model */
			case "SubjectDistanceDown":
				ml.world.getCurrentField().fadeObjectDistances(0.85f);
				break;

			case "SubjectDistanceUp":
				ml.world.getCurrentField().fadeObjectDistances(1.176f);
				break;

				/* Help */
			case "OpenHelpWindow":
				display.window.openHelpWindow();
				break;

			case "CloseHelpWindow":
				display.window.hideHelpWindow();
				break;

				/* Memory */
			case "OpenMemoryWindow":
				display.window.openMemoryWindow();
				break;

			case "CloseMemoryWindow":
				display.window.hideMemoryWindow();
				break;

				/* Statistics */
			case "OpenStatisticsWindow":
				display.window.openStatisticsWindow();
				break;

			case "CloseStatisticsWindow":
				display.window.hideStatisticsWindow();
				break;

				/* Time */
			case "OpenTimeWindow":
				display.window.openTimeWindow();
				break;

			case "CloseTimeWindow":
				display.window.hideTimeWindow();
				break;

				/* Graphics */
			case "OpenGraphicsWindow":
				display.window.openGraphicsWindow();
				break;

			case "CloseGraphicsWindow":
				display.window.hideGraphicsWindow();
				break;

			case "MoveForward":
				ml.world.viewer.walkForward();
				break;
			case "MoveBackward":
				ml.world.viewer.walkBackward();
				break;
			case "MoveLeft":
				ml.world.viewer.startMoveXTransition(-1);
				break;
			case "MoveRight":
				ml.world.viewer.startMoveXTransition(1);
				break;

			case "ZoomIn":
				ml.world.viewer.zoomIn();
				break;
			case "ZoomOut":
				ml.world.viewer.zoomOut();
				break;

				/* Model */
			case "OpenModelWindow":
				display.window.openModelWindow();
				break;

			case "CloseModelWindow":
				display.window.modelWindow.setVisible(false);
				break;

				/* Time */
			case "NextTime":
				ml.world.viewer.moveToNextTimeSegment(true, ml.world.viewer.getMovementTeleport(), true);
				break;
			case "PreviousTime":
				ml.world.viewer.moveToPreviousTimeSegment(true, ml.world.viewer.getMovementTeleport(), true);
				break;

				/* Selection */
			case "OpenSelectionWindow":
				display.window.openSelectionWindow();
				ml.world.viewer.setSelection( true );
				display.window.chkbxSelectionMode.setSelected(true);
				break;

			case "CloseSelectionWindow":
				display.window.selectionWindow.setVisible(false);
				break;

			case "SelectFront":
				ml.world.viewer.chooseMediaInFront(true);
				break;

			case "DeselectFront":
				ml.world.viewer.chooseMediaInFront(false);	
				break;

			case "DeselectAll":
				ml.world.getCurrentField().deselectAllMedia(false);
				break;

			case "StitchPanorama":
				ml.world.getCurrentCluster().stitchImages(ml.stitcher, ml.library.getLibraryFolder(), ml.world.getCurrentField().getSelectedImages());    			
				break;

				/* Memory */
			case "SaveLocation":
				ml.world.viewer.addPlaceToMemory();
				break;
			case "ClearMemory":
				ml.world.viewer.clearMemory();
				break;

				/* Output */
			case "ExportImage":
				if(!ml.world.outputFolderSelected) ml.selectFolder("Select an output folder:", "outputFolderSelected");
				ml.world.exportCurrentView();
				break;
			case "OutputFolder":
				ml.selectFolder("Select an output folder:", "outputFolderSelected");
				break;
			}
		}
		
		if(event == GEvent.RELEASED)
		{
			System.out.println("RELEASED EVENT: tag:"+button.tag);
			
			switch(button.tag)
			{
				case "MoveForward":
					ml.world.viewer.stopMoveZTransition();
					break;
				case "MoveBackward":
					ml.world.viewer.stopMoveZTransition();
					break;
				case "MoveLeft":
					ml.world.viewer.stopMoveXTransition();
					break;
				case "MoveRight":
					ml.world.viewer.stopMoveXTransition();
					break;
				case "ZoomIn":
					ml.world.viewer.stopZooming();
					break;
				case "ZoomOut":
					ml.world.viewer.stopZooming();
					break;
			}
		}
	}

	/**
	 * Handle toggle control (radio button) events 
	 * @param world Current world
	 * @param display 2D display object
	 * @param option Toggle control object involved
	 * @param event Radio button event
	 */
	public void handleToggleControlEvent(WMV_World world, ML_Display display, GToggleControl option, GEvent event) 
	{
		switch (option.tag)
		{
			/* Views */
			case "SceneView":
				display.setDisplayView(world, 0);
				break;
			case "MapView":
				display.setDisplayView(world, 1);
				break;
			case "TimelineView":
				display.setDisplayView(world, 2);
				break;
//			case "LibraryView":
//				display.setDisplayView(world, 3);
//				break;
				
			/* Navigation */
			case "FollowTimeline":
				if(option.isSelected())
				{
					world.viewer.setFollowMode( 0 );
					display.window.optGPSTrack.setSelected(false);
					display.window.optMemory.setSelected(false);
				}
				break;
	  		case "FollowGPSTrack":
				if(option.isSelected())
				{
					world.viewer.setFollowMode( 1 );
					display.window.optTimeline.setSelected(false);
					display.window.optMemory.setSelected(false);
				}
				break;
	  		case "FollowMemory":
				if(option.isSelected())
				{
					world.viewer.setFollowMode( 2 );
					display.window.optTimeline.setSelected(false);
					display.window.optGPSTrack.setSelected(false);
				}
				break;
			case "MovementTeleport":
				world.viewer.setMovementTeleport( option.isSelected() );
				if(!world.viewer.getMovementTeleport())
					world.viewer.stopFollowing();
				break;
				
			case "FollowTeleport":
				world.viewer.setFollowTeleport( option.isSelected() );
				break;
				
			/* Time */
			case "ClusterTimeMode":
				world.setTimeMode(0);
				break;
			case "FieldTimeMode":
				world.setTimeMode(1);
				break;
//			case "MediaTimeMode":			// -- Disabled
//				world.setTimeMode(2);
//				break;
				
			case "TimeFading":
				world.getState().timeFading = option.isSelected();
				break;
			case "Paused":
				world.getState().paused = option.isSelected();
				break;
			/* Graphics */
			case "FadeEdges":
				world.getState().useBlurMasks = option.isSelected();
				break;
			case "HideImages":
				if(!option.isSelected() && world.viewer.getSettings().hideImages)
					world.viewer.showImages();
				else if(option.isSelected() && !world.viewer.getSettings().hideImages)
					world.viewer.hideImages();
				break;
			case "HideVideos":
				if(!option.isSelected() && world.viewer.getSettings().hideVideos)
					world.viewer.showVideos();
				else if(option.isSelected() && !world.viewer.getSettings().hideVideos)
					world.viewer.hideVideos();
				break;
			case "HidePanoramas":
				if(!option.isSelected() && world.viewer.getSettings().hidePanoramas)
					world.viewer.showPanoramas();
				else if(option.isSelected() && !world.viewer.getSettings().hidePanoramas)
					world.viewer.hidePanoramas();
				break;
			case "AlphaMode":
				world.state.alphaMode = option.isSelected();
				break;
			case "OrientationMode":
				world.viewer.setOrientationMode( !world.viewer.getSettings().orientationMode );
				break;
			case "AngleFading":
				world.viewer.setAngleFading( option.isSelected() );
				break;
			case "AngleThinning":
				world.viewer.setAngleThinning( option.isSelected() );
				break;
				
			/* Model */
			case "ShowModel":
				world.state.showModel = option.isSelected();
				break;
			case "MediaToCluster":
				world.state.showMediaToCluster = option.isSelected();
				break;
			case "CaptureToMedia":
				world.state.showCaptureToMedia = option.isSelected();
				break;
			case "CaptureToCluster":
				world.state.showCaptureToCluster = option.isSelected();
				break;
				
			/* Selection */
			case "SelectionMode":
				world.viewer.setSelection( option.isSelected() );
				break;
				
			case "MultiSelection":
				world.viewer.setMultiSelection( option.isSelected() );
				break;
					
			case "SegmentSelection":
				world.viewer.setSegmentSelection( option.isSelected() );
				break;
				
			case "ViewMetadata":
				world.state.showMetadata = option.isSelected();
				break;
		}
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
	 * Update mouse navigation									//-- Disabled
	 * @param viewer
	 * @param mouseX
	 * @param mouseY
	 * @param frameCount
	 */
	void updateMouseNavigation(WMV_Viewer viewer, int mouseX, int mouseY, int frameCount)
	{			
		if(frameCount - clickedRecentlyFrame > doubleClickSpeed && mouseClickedRecently)
		{
			mouseClickedRecently = false;
//			mouseReleasedRecently = false;
		}
		
		if(frameCount - clickedRecentlyFrame > 20 && !mouseReleased)
			viewer.addPlaceToMemory();				// Held mouse
		
		if (mouseX < screenWidth * 0.25 && mouseX > -1) 
		{
			if(!viewer.turningX())
				viewer.turnXToAngle(PApplet.radians(5.f), -1);
		}
		else if (mouseX > screenWidth * 0.75 && mouseX < screenWidth + 1) 
		{
			if(!viewer.turningX())
				viewer.turnXToAngle(PApplet.radians(5.f), 1);
		}
		else if (mouseY < screenHeight * 0.25 && mouseY > -1) 
		{
			if(!viewer.turningY())
				viewer.turnYToAngle(PApplet.radians(5.f), -1);
		}
		else if (mouseY > screenHeight * 0.75 && mouseY < screenHeight + 1) 
		{
			if(!viewer.turningY())
				viewer.turnYToAngle(PApplet.radians(5.f), 1);
		}
		else
		{
			if(viewer.turningX()) viewer.setTurningX( false );
			if(viewer.turningY()) viewer.setTurningY( false );
		}
	}

	void handleMousePressed(WMV_Viewer viewer, int mouseX, int mouseY, int frameCount)
	{
//		boolean doubleClick = false, switchedViews = false;
		if(!viewer.getSettings().orientationMode && viewer.getState().lastMovementFrame > 5)
		{
			if(mouseX > screenWidth * 0.25 && mouseX < screenWidth * 0.75 && mouseY < screenHeight * 0.75 && mouseY > screenHeight * 0.25)
				viewer.walkForward();
			viewer.getState().lastMovementFrame = frameCount;
		}
		else viewer.moveToNextCluster(false, -1);

//		mouseOffsetX = 0;
//		mouseOffsetY = 0;
	}

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
				world.viewer.moveToNearestCluster(world.viewer.getMovementTeleport());
		}
		
		if(display.displayView == 1 || (display.displayView == 3 && display.libraryViewMode == 0))
			display.map2D.handleMouseReleased(world, mouseX, mouseY);
		else if(display.displayView == 2)
			display.handleMouseReleased(world, mouseX, mouseY);
//		else if(display.displayView == 3)
//			display.handleMouseReleased(mouseX, mouseY);
	}
	
	void handleMouseClicked(int mouseX, int mouseY, int frameCount)
	{
		mouseClickedRecently = true;
		clickedRecentlyFrame = frameCount;
		mouseReleased = false;
	}

	void handleMouseDragged(int mouseX, int mouseY)
	{
//		mouseOffsetX = mouseClickedX - mouseX;
//		mouseOffsetY = mouseClickedY - mouseY;
//		viewer.lastMovementFrame = frameCount;			// Turn faster if larger offset X or Y?
	}
}