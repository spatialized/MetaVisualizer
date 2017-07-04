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

//	private int screenWidth, screenHeight;

	/**
	 * Constructor for input class
	 * @param newScreenWidth
	 * @param newScreenHeight
	 */
	public ML_Input() 
	{
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
		if (ml.state.running && ml.state.selectedLibrary && ml.state.fieldsInitialized )
		{
			/* General */
			keyboardInput.handleUniversalKeyPressed(ml, key, keyCode);

			if (ml.state.interactive)					
				keyboardInput.handleInteractiveClusteringKeyPressed(ml, key, keyCode);
			else 						// Interactive Clustering Mode
			{
				keyboardInput.handleAllViewsKeyPressed(ml, key, keyCode);	 	 	/* Controls for both 3D + HUD Views */
				
				if(ml.display.inDisplayView())							
				{
					if(ml.display.getDisplayView() == 1)											 /* Map View */
						keyboardInput.handleMapViewKeyPressed(ml, key, keyCode);
					else if(ml.display.getDisplayView() == 2)									 /* Time View */
						keyboardInput.handleTimeViewKeyPressed(ml, key, keyCode);
					else if(ml.display.getDisplayView() == 3)									 /* Library View */
						keyboardInput.handleLibraryViewKeyPressed(ml, key, keyCode);
					else if(ml.display.getDisplayView() == 4)							 		/* Media View */
						keyboardInput.handleMediaViewKeyPressed(ml, key, keyCode);
				}
				else
					keyboardInput.handleWorldViewKeyPressed(ml, key, keyCode); 		/* Controls for World View only */
			}
		}
	}

	/**
	 * Handle key pressed in Library View
	 * @param ml
	 * @param key
	 * @param keyCode
	 */
	void handleLibraryViewKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		keyboardInput.handleLibraryViewKeyPressed(ml, key, keyCode);	 	 	/* Controls for both 3D + HUD Views */
	}

	/**
	 * Handle key pressed in List Item View
	 * @param ml
	 * @param key
	 * @param keyCode
	 */
	void handleListItemWindowKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		keyboardInput.handleListItemWindowKeyPressed(ml, key, keyCode);
	}
	
	/**
	 * Handle key pressed in Library Window
	 * @param ml
	 * @param key
	 * @param keyCode
	 */
	public void handleLibraryWindowKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		keyboardInput.handleLibraryWindowKeyPressed(ml, key, keyCode);
	}
	
	/**
	 * Respond to user key releases
	 */
	void handleKeyReleased(MultimediaLocator ml, ML_Display display, char key, int keyCode)
	{
		if (ml.state.running && ml.state.selectedLibrary && ml.state.fieldsInitialized )
		{
			keyboardInput.handleKeyReleased(ml, display, key, keyCode);
		}
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
			if (slider.tag == "MediaLength") 
				world.settings.defaultMediaLength = slider.getValueI();
		}
		
		if(display.window.setupMediaWindow)
		{
			if (slider.tag == "VisibleAngle") 
				world.viewer.setVisibleAngle( slider.getValueF() );
			
			if (slider.tag == "FarClipping")
			{
				world.viewer.setFarViewingDistance( slider.getValueF() );
			}
			
			if (slider.tag == "Alpha") 
				world.state.alpha = slider.getValueF();
			
			if (slider.tag == "Brightness") 
				world.viewer.setUserBrightness( slider.getValueF() );
			
			if (slider.tag == "AltitudeFactor") 
			{
				world.settings.altitudeScalingFactor = PApplet.round(slider.getValueF() * 1000.f) * 0.001f;
				world.getCurrentField().calculateMediaLocations(true);		// Recalculate media locations
				world.getCurrentField().recalculateGeometries();			// Recalculate media geometries at new locations
				world.getCurrentField().createClusters();					// Recalculate cluster locations
			}
		}
		
		if(display.window.setupTimeWindow)
		{
			if (slider.tag == "TimeCycleLength") 
			{
				switch(world.state.timeMode)
				{
					case 0:														// Cluster
						world.setAllClustersTimeCycleLength(slider.getValueI());
						break;
					case 1:														// Field
						world.settings.timeCycleLength = slider.getValueI();
						world.settings.timeInc = world.settings.timeCycleLength / 30.f;			
						break;
					case 2:														// Media
						break;
					default:
						break;
				}
			}

			if (slider.tag == "CurrentTime") 
				world.setCurrentTime(slider.getValueF());
			
			if (slider.tag == "ClusterLength") 
				world.settings.clusterLength = slider.getValueF();
		}
	}

	/**
	 * Handle button input
	 * @param ml Parent app
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
				case "ChooseField":
					ml.world.viewer.chooseFieldDialog();
					break;
				case "SaveWorld":
					if(ml.world.getFields().size() > 1) ml.world.saveLibrary();
					else ml.world.saveCurrentFieldState();
					break;
				case "SaveField":
					ml.world.saveCurrentFieldState();
					break;
				
				/* Library */
				case "CreateLibrary":
					ml.startCreatingNewLibrary();
					break;
					
				case "OpenLibrary":
					ml.state.inLibrarySetup = true;
					if(ml.createNewLibrary) ml.createNewLibrary = false;
					ml.display.window.btnCreateLibrary.setVisible(false);
					ml.display.window.btnOpenLibrary.setVisible(false);
					ml.display.window.chkbxRebuildLibrary.setVisible(false);
					ml.display.window.btnLibraryHelp.setVisible(false);
					ml.display.window.lblStartup.setVisible(false);
					break;
				
				case "CloseLibrary":
					ml.restart();
					break;
					
				case "Quit":
					ml.exitProgram();
					break;
					
				case "LibraryHelp":
					if(!ml.display.window.setupHelpWindow) ml.display.window.openHelpWindow();
					else if(!ml.display.window.showHelpWindow) ml.display.window.showHelpWindow();
					break;
					
				case "AboutHelp":
					if(!ml.display.window.setupHelpWindow) ml.display.window.openHelpWindow();
					else if(!ml.display.window.showHelpWindow) ml.display.window.showHelpWindow();
					ml.display.window.helpAboutText = 0;
					break;
					
				case "ImportHelp":
					if(!ml.display.window.setupHelpWindow) ml.display.window.openHelpWindow();
					else if(!ml.display.window.showHelpWindow) ml.display.window.showHelpWindow();
					ml.display.window.helpAboutText = 1;
					break;
					
//				case "CloseHelp":
//					if(ml.display.window.setupHelpWindow && ml.display.window.showHelpWindow) 
//						ml.display.window.hideHelpWindow();
//					break;
	
				case "AddMediaFolder":
					ml.mediaFolderDialog();
					break;
					
				case "MakeLibrary":
					ml.state.selectedNewLibraryMedia = true;			// Media folder has been selected
					ml.state.chooseMediaFolders = false;			// No longer choose a media folder
					ml.state.chooseLibraryDestination = true;		// Choose library destination folder
					break;
				
				case "CancelCreateLibrary":
					ml.display.window.closeCreateLibraryWindow();
					ml.state.inLibrarySetup = false;
					ml.createNewLibrary = false;
					ml.state.chooseMediaFolders = false;
					display.window.lblStartupWindowText.setVisible(false);
					break;

				/* Text Entry */
				case "EnteredText":
					switch(ml.display.window.textEntryWindowResultCode)
					{
						case 0:						// 0: Field Name 
							String fieldName = ml.display.window.txfInputText.getText();
//							System.out.println("Input.buttonPressed()... Field name input text:"+fieldName);
							ml.world.getField(ml.state.namingField).setName(fieldName);
							ml.display.window.closeTextEntryWindow();
							break;
						case 1:						// 1: Library Name
							String libraryName = ml.display.window.txfInputText.getText();
//							System.out.println("Input.buttonPressed()... Library name input text:"+libraryName);
							ml.library.rename(libraryName);
							ml.world.updateMediaFilePaths();		// Update media file paths with new library name
							ml.state.libraryNamed = true;
							ml.state.fieldsNamed = false;
							ml.display.window.closeTextEntryWindow();
							break;
						case 2:						// 2: Exiftool Path
							String exiftoolPath = ml.display.window.txfInputText.getText();
							System.out.println("Input.buttonPressed()... Set exiftoolPath:"+exiftoolPath);
							ml.setExiftoolPath(exiftoolPath);
							if(!ml.state.gettingExiftoolPath)
								ml.display.window.closeTextEntryWindow();	// Close Text Entry Window if found valid Exiftool path
							break;
					}
					break;
	
					/* Navigation */
					case "OpenNavigationWindow":
						if(!ml.display.window.showNavigationWindow)
							ml.display.window.openNavigationWindow();
						else
							ml.display.window.closeNavigationWindow();
						break;
					case "CloseNavigationWindow":
						ml.display.window.closeNavigationWindow();
						break;
					case "ExitNavigationWindow":
						display.window.openMainMenu();
						break;
	
					case "NearestCluster":
						ml.world.viewer.moveToNearestCluster(ml.world.viewer.getNavigationTeleport());
						break;
					case "RandomCluster":
						ml.world.viewer.moveToRandomCluster(ml.world.viewer.getNavigationTeleport(), true);
						break;
					case "LastCluster":
						ml.world.viewer.moveToLastCluster(ml.world.viewer.getNavigationTeleport());
						break;
						
					case "NearestImage":
						ml.world.viewer.moveToNearestClusterWithType(0, false, ml.world.viewer.getNavigationTeleport(), true);
						break;
					case "NearestPanorama":
						ml.world.viewer.moveToNearestClusterWithType(1, false, ml.world.viewer.getNavigationTeleport(), true);
						break;
					case "NearestVideo":
						ml.world.viewer.moveToNearestClusterWithType(2, false, ml.world.viewer.getNavigationTeleport(), true);
						break;
					case "NearestSound":
						ml.world.viewer.moveToNearestClusterWithType(3, false, ml.world.viewer.getNavigationTeleport(), true);
						break;
						
					case "NextField":
						if(display.getDisplayView() == 1)
							ml.world.viewer.teleportToFieldOffset(1, false);
						else
							ml.world.viewer.teleportToFieldOffset(1, true);
						break;
					case "PreviousField":
						if(display.getDisplayView() == 1)
							ml.world.viewer.teleportToFieldOffset(-1, false);
						else
							ml.world.viewer.teleportToFieldOffset(-1, true);
						break;
					
					case "ZoomIn":
						ml.world.viewer.stopZooming();
						break;
					case "ZoomOut":
						ml.world.viewer.stopZooming();
						break;
	
					case "MoveForward":								// -- Disabled
						ml.world.viewer.stopMoveZTransition();
						break;
					case "MoveBackward":							// -- Disabled
						ml.world.viewer.stopMoveZTransition();
						break;
					case "MoveLeft":								// -- Disabled
						ml.world.viewer.stopMoveXTransition();
						break;
					case "MoveRight":								// -- Disabled
						ml.world.viewer.stopMoveXTransition();
						break;
						
					case "StopViewer":
						ml.world.viewer.stop(true);
						ml.world.viewer.stopZooming();
						break;
	
					/* Model */
					case "SubjectDistanceDown":
						ml.world.getCurrentField().fadeObjectDistances(1.176f);
						break;
					case "SubjectDistanceUp":
						ml.world.getCurrentField().fadeObjectDistances(0.85f);
						break;
		
					/* Help */
					case "OpenHelpWindow":
						if(!ml.display.window.showHelpWindow)
							ml.display.window.openHelpWindow();
						else
							ml.display.window.closeHelpWindow();
						break;
					case "CloseHelpWindow":
						display.window.closeHelpWindow();
						break;
					case "ExitHelpWindow":
						display.window.openMainMenu();
						break;
		
					/* Time */
					case "OpenTimeWindow":
						if(!ml.display.window.showTimeWindow)
							ml.display.window.openTimeWindow();
						else
							ml.display.window.closeTimeWindow();
						break;
					case "CloseTimeWindow":
						ml.display.window.closeTimeWindow();
						break;
					case "ExitTimeWindow":
						display.window.openMainMenu();
						break;

					case "NextTime":
						ml.world.viewer.moveToNextTimeSegment(true, true, ml.world.viewer.getNavigationTeleport(), true);
						break;
					case "PreviousTime":
						ml.world.viewer.moveToPreviousTimeSegment(true, true, ml.world.viewer.getNavigationTeleport(), true);
						break;
		
					case "ChooseGPSTrack":
						ml.world.viewer.chooseGPSTrack();
						break;
						
					/* Memory */
					case "SaveLocation":
						ml.world.viewer.addPlaceToMemory();
						break;
					case "ClearMemory":
						ml.world.viewer.clearMemory();
						break;
					case "SetHome":
						ml.world.getCurrentField().setHome(ml.world.viewer.getLocationAsWaypoint());
						break;
		
					/* Map */
					case "SetMapView":
						if(display.getDisplayView() != 1)
							display.setDisplayView(ml.world, 1);
						break;
					case "PanUp":
						if(ml.display.map2D.isPanning())
							ml.display.map2D.stopPanning();
						break;
					case "PanLeft":
						if(ml.display.map2D.isPanning())
							ml.display.map2D.stopPanning();
						break;
					case "PanDown":
						if(ml.display.map2D.isPanning())
							ml.display.map2D.stopPanning();
						break;
					case "PanRight":
						if(ml.display.map2D.isPanning())
							ml.display.map2D.stopPanning();
						break;
					case "ZoomToViewer":
						ml.display.map2D.zoomToCluster(ml.world, ml.world.getCurrentCluster(), true);	// Zoom to current cluster
						break;
					case "ZoomToField":
	//					if(ml.world.getFields().size() > 1)
							ml.display.map2D.zoomToField(ml.world.getCurrentField(), true);
	//					else
	//						ml.display.map2D.zoomToWorld(true);
						break;
					case "MapZoomIn":
						ml.display.map2D.zoomIn(ml.world);
						break;
					case "ResetMapZoom":
						ml.display.map2D.resetMapZoom(true);
	//					ml.display.map2D.zoomToWorld(true);
						break;
					case "MapZoomOut":
						ml.display.map2D.zoomOut(ml.world);
						break;
	//				case "ZoomToSelected":
	//					break;
					/* Media */
					case "OpenMediaWindow":
						if(!ml.display.window.showMediaWindow)
							ml.display.window.openMediaWindow();
						else
							ml.display.window.closeMediaWindow();
						break;
					case "CloseMediaWindow":
						ml.display.window.closeMediaWindow();
						break;
					case "ExitMediaWindow":
						display.window.openMainMenu();
						break;
		
					case "SelectFront":
						ml.world.viewer.chooseMediaInFront(true);
						break;
					case "SelectPanorama":
						ml.world.viewer.choosePanoramaNearby(true);
						break;
						
					case "DeselectFront":
						ml.world.viewer.chooseMediaInFront(false);
						if(ml.display.getDisplayView() == 4)
							ml.display.setDisplayView(ml.world, 0);			// Set current view to Media Display View
						break;
					case "DeselectPanorama":
						ml.world.viewer.choosePanoramaNearby(false);
						break;

					case "DeselectAll":
						ml.world.getCurrentField().deselectAllMedia(false);
						if(ml.display.getDisplayView() == 4)
							ml.display.setDisplayView(ml.world, 0);			// Set current view to Media Display View
						break;
		
					case "ViewSelected":
						if(ml.display.getDisplayView() == 0)
							ml.world.viewer.startViewingSelectedMedia();
						else if(ml.display.getDisplayView() == 4)
							ml.world.viewer.exitMediaView();
						break;
					case "StitchPanorama":				// -- Disabled
//						ml.world.getCurrentCluster().stitchImages(ml.stitcher, ml.library.getLibraryFolder(), ml.world.getCurrentField().getSelectedImages());    			
						break;
		
					/* Output */
					case "SaveScreenshot":
						if(!ml.world.outputFolderSelected) ml.selectFolder("", "outputFolderSelected");
						ml.world.exportCurrentView();
						break;
					case "ExportMedia":
						if(!ml.world.outputFolderSelected) ml.selectFolder("", "outputFolderSelected");
						ml.world.exportCurrentMedia();
						break;
					case "OutputFolder":
						ml.selectFolder("", "outputFolderSelected");
						break;
	
				/* Time */
				case "SetTimeView":
					if(display.getDisplayView() != 2)
						display.setDisplayView(ml.world, 2);
					break;
				case "TimelineReverse":
					ml.display.stopScrolling();
					break;
				case "TimelineForward":
					ml.display.stopScrolling();
					break;

				/* Timeline */
				case "TimelineZoomIn":
					ml.display.stopZooming();
					break;
				case "TimelineZoomOut":
					ml.display.stopZooming();
					break;
				case "TimelineZoomToFit":
					ml.display.zoomToTimeline(ml.world, true);
					break;
				case "TimelineZoomToSelected":
					ml.display.zoomToCurrentSelectableTimeSegment(ml.world, true);
					break;
				case "TimelineZoomToDate":
					ml.display.zoomToCurrentSelectableDate(ml.world, true);
					break;
				case "TimelineZoomToFull":
					ml.display.resetZoom(ml.world, true);
					break;
					
				/* Library View */
				case "OpenLibraryViewWindow":
					if(!ml.display.window.showLibraryViewWindow)
						ml.display.window.openLibraryViewWindow();
					else
						ml.display.window.closeLibraryViewWindow();
					break;
				case "CloseLibraryViewWindow":
					ml.display.window.closeLibraryViewWindow();
					break;
				case "ExitLibraryViewWindow":
					display.window.openMainMenu();
					break;

				case "SetLibraryView":
					if(display.getDisplayView() != 3)
						display.setDisplayView(ml.world, 3);
					break;
				
				case "PreviousCluster":
					ml.display.showPreviousItem();
					break;
				case "NextCluster":
					ml.display.showNextItem();
					break;
				case "CurrentCluster":
					if(ml.display.getLibraryViewMode() == 1)
						ml.display.setDisplayItem( ml.world.viewer.getCurrentFieldID() );
					else if(ml.display.getLibraryViewMode() == 2)
						ml.display.setDisplayItem( ml.world.viewer.getCurrentClusterID() );
					break;
			}
		}
		
		if(event == GEvent.PRESSED)
		{
			switch(button.tag)
			{
				/* Navigation */
				case "ZoomIn":
					ml.world.viewer.zoomIn();
					break;
				case "ZoomOut":
					ml.world.viewer.zoomOut();
					break;
					
				/* Map */
				case "PanUp":
					if(ml.display.map2D.isPanning())
						ml.display.map2D.stopPanning();
					else
						ml.display.map2D.panUp();
					break;
				case "PanLeft":
					if(ml.display.map2D.isPanning())
						ml.display.map2D.stopPanning();
					else
						ml.display.map2D.panLeft();
					break;
				case "PanDown":
					if(ml.display.map2D.isPanning())
						ml.display.map2D.stopPanning();
					else
						ml.display.map2D.panDown();
					break;
				case "PanRight":
					if(ml.display.map2D.isPanning())
						ml.display.map2D.stopPanning();
					else
						ml.display.map2D.panRight();
					break;
				
				/* Timeline */
				case "TimelineZoomIn":
					if(ml.display.isZooming())
						ml.display.stopZooming();
					else
						ml.display.zoom(ml.world, -1, true);
					break;
				case "TimelineZoomOut":
					if(ml.display.isZooming())
						ml.display.stopZooming();
					else
						ml.display.zoom(ml.world, 1, true);
					break;
				case "TimelineReverse":
					if(ml.display.isScrolling())
						ml.display.stopScrolling();
					else
						ml.display.scroll(ml.world, -1);
					break;
				case "TimelineForward":
					if(ml.display.isScrolling())
						ml.display.stopScrolling();
					else
						ml.display.scroll(ml.world, 1);
					break;
			}
		}
		
		if(event == GEvent.RELEASED)
		{
			switch(button.tag)
			{
				case "ZoomIn":
					ml.world.viewer.stopZooming();
					break;
				case "ZoomOut":
					ml.world.viewer.stopZooming();
					break;
					
				/* Map */
				case "PanUp":
				case "PanLeft":
				case "PanDown":
				case "PanRight":
					display.map2D.stopPanning();
//					System.out.println("Stopped panning... panningLeft:"+display.map2D.panningLeft+ " panningRight:"+display.map2D.panningRight);
					break;
					
				/* Timeline */
				case "TimelineZoomIn":
					ml.display.stopZooming();
					break;
				case "TimelineZoomOut":
					ml.display.stopZooming();
					break;
				case "TimelineReverse":
					ml.display.stopScrolling();
					break;
				case "TimelineForward":
					ml.display.stopScrolling();
					break;
				
				/* Navigation -- Disabled */
//				case "MoveForward":
//					ml.world.viewer.stopMoveZTransition();
//					break;
//				case "MoveBackward":
//					ml.world.viewer.stopMoveZTransition();
//					break;
//				case "MoveLeft":
//					ml.world.viewer.stopMoveXTransition();
//					break;
//				case "MoveRight":
//					ml.world.viewer.stopMoveXTransition();
//					break;

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
			/* Create Library Window */
			case "RebuildLibrary":
				world.ml.state.rebuildLibrary = option.isSelected();
				break;
			
			/* Main Window */
			case "SceneView":
				if(display.getDisplayView() != 0)
					display.setDisplayView(world, 0);
				break;
			case "MapView":
				if(display.getDisplayView() != 1)
					display.setDisplayView(world, 1);
				break;
			case "TimelineView":
				if(display.getDisplayView() != 2)
					display.setDisplayView(world, 2);
				break;
			case "LibraryView":							// -- Disabled
				if(display.getDisplayView() != 3)
					display.setDisplayView(world, 3);
				break;
			case "ScreenMessagesOn":
				world.settings.screenMessagesOn = option.isSelected();
//				ml.display.window.chkbxScreenMessagesOn.setSelected(ml.world.settings.screenMessagesOn);
				break;
				
			case "SetMapViewWorldMode":
				display.setMapViewMode(0);
				if(option.isSelected()) display.window.optMapViewFieldMode.setSelected(false);	
				break;
			case "SetMapViewFieldMode":
				display.setMapViewMode(1);
				if(option.isSelected()) display.window.optMapViewWorldMode.setSelected(false);	
				break;

			/* Navigation */
			case "NavigationTeleport":
				world.viewer.setNavigationTeleport( true );
				if(!world.viewer.getNavigationTeleport())
					world.viewer.stopFollowing();
				display.window.optMove.setSelected(false);
				break;
			case "NavigationMove":
				world.viewer.setNavigationTeleport( false );
				if(!world.viewer.getNavigationTeleport())
					world.viewer.stopFollowing();
				display.window.optTeleport.setSelected(false);
				break;
				
			case "FollowTimeline":
				if(option.isSelected())
				{
					world.viewer.setPathNavigationMode( 0 );
					display.window.optGPSTrack.setSelected(false);
					display.window.optMemory.setSelected(false);
				}
				break;
	  		case "FollowGPSTrack":
				if(option.isSelected())
				{
					world.viewer.setPathNavigationMode( 1 );
					display.window.optTimeline.setSelected(false);
					display.window.optMemory.setSelected(false);
				}
				break;
	  		case "FollowMemory":
				if(option.isSelected())
				{
					world.viewer.setPathNavigationMode( 2 );
					display.window.optTimeline.setSelected(false);
					display.window.optGPSTrack.setSelected(false);
				}
				break;
//			case "MovementTeleport":
//				world.viewer.setMovementTeleport( option.isSelected() );
//				if(!world.viewer.getMovementTeleport())
//					world.viewer.stopFollowing();
//				break;
			
			case "Following":
				if(option.isSelected())
				{
					if(!world.viewer.isFollowing())
					{
						world.viewer.startFollowing();
					}
				}
				else world.viewer.stopFollowing();
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
				world.setTimeFading(option.isSelected());
//				world.getState().timeFading = option.isSelected();
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
				
			case "HideSounds":
				if(!option.isSelected() && world.viewer.getSettings().hideSounds)
					world.viewer.showSounds();
				else if(option.isSelected() && !world.viewer.getSettings().hideSounds)
					world.viewer.hideSounds();
				break;
				
			case "AlphaMode":
				world.state.alphaMode = option.isSelected();
				break;
			case "OrientationMode":
				world.viewer.setOrientationMode( !world.viewer.getSettings().orientationMode );
//				display.window.chkbxDomeView.setEnabled(world.viewer.getSettings().orientationMode);
				break;
			case "DomeView":
				world.ml.state.sphericalView = option.isSelected();
				break;
			case "AngleFading":
				world.viewer.setAngleFading( option.isSelected() );
				break;
			case "AngleThinning":
				world.viewer.setAngleThinning( option.isSelected() );
				break;
				
			/* Model */
			case "ShowModel":
				world.setShowModel( option.isSelected() );
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
			case "EnableSelection":
				world.viewer.setSelection( option.isSelected() );
				break;
				
			case "MultiSelection":
				world.viewer.setMultiSelection( option.isSelected(), true );
				break;
					
			case "SegmentSelection":
				world.viewer.setGroupSelection( option.isSelected(), true );
				break;
				
			case "ViewMetadata":
				world.state.showMetadata = option.isSelected();
				break;
				
			/* Library View */
			case "LibraryViewModeLibrary":
				if(world.ml.display.getLibraryViewMode() != 0)
					world.ml.display.setLibraryViewMode(0);
				break;
			case "LibraryViewModeField":
				if(world.ml.display.getLibraryViewMode() != 1)
					world.ml.display.setLibraryViewMode(1);
				break;
			case "LibraryViewModeCluster":
				if(world.ml.display.getLibraryViewMode() != 2)
					world.ml.display.setLibraryViewMode(2);
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

	void handleMousePressed(WMV_Viewer viewer, int mouseX, int mouseY, int frameCount)
	{
//		if(!viewer.getSettings().orientationMode && viewer.getState().lastMovementFrame > 5)
//		{
//			if(mouseX > screenWidth * 0.25 && mouseX < screenWidth * 0.75 && mouseY < screenHeight * 0.75 && mouseY > screenHeight * 0.25)
//				viewer.walkForward();
//			viewer.getState().lastMovementFrame = frameCount;
//		}
//		else viewer.moveToNextCluster(false, -1);
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
				world.viewer.moveToNearestCluster(world.viewer.getNavigationTeleport());
		}
		
		if(display.getDisplayView() == 1)
			display.map2D.handleMouseReleased(world, mouseX, mouseY);
		else if(display.getDisplayView() == 2)
			display.handleTimeViewMouseReleased(world, mouseX, mouseY);
		else if(display.getDisplayView() == 3)
			display.handleLibraryViewMouseReleased(world, mouseX, mouseY);
		else if(display.getDisplayView() == 4)
			display.handleMediaViewMouseReleased(world, mouseX, mouseY);
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