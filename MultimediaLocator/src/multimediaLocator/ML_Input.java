package multimediaLocator;

import java.awt.Font;

import g4p_controls.G4P;
import g4p_controls.GButton;
import g4p_controls.GEvent;
import g4p_controls.GLabel;
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
//		System.out.println("Input.handleKeyPressed()... key:"+key);
//		if (ml.state.running && ml.state.selectedLibrary )
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
					if(ml.display.displayView == 1)											 /* Map View */
						keyboardInput.handleMapViewKeyPressed(ml, key, keyCode);
					else if(ml.display.displayView == 2)									 /* Time View */
						keyboardInput.handleTimelineViewKeyPressed(ml, key, keyCode);
					else if(ml.display.displayView == 3)									 /* Library View */
						keyboardInput.handleLibraryViewKeyPressed(ml, key, keyCode);
					else if(ml.display.displayView == 4)							 		/* Media View */
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

	void handleListItemWindowKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		keyboardInput.handleListItemWindowKeyPressed(ml, key, keyCode);
	}
	
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
		
		if(display.window.setupMediaWindow)
		{
			if (slider.tag == "VisibleAngle") 
				world.viewer.setVisibleAngle( slider.getValueF() );
			
			if (slider.tag == "Alpha") 
				world.state.alpha = slider.getValueF();
			
			if (slider.tag == "Brightness") 
				world.viewer.setUserBrightness( slider.getValueF() );
			
			if (slider.tag == "AltitudeScaling") 
			{
				world.settings.altitudeScalingFactor = PApplet.round(slider.getValueF() * 1000.f) * 0.001f;
				world.getCurrentField().calculateMediaLocations(true);		// Recalculate media locations
				world.getCurrentField().recalculateGeometries();			// Recalculate media geometries at new locations
				world.getCurrentField().createClusters();					// Recalculate cluster locations
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
				case "ChooseField":
					ml.world.viewer.chooseFieldDialog();
					break;
				case "SaveWorld":
					if(ml.world.getFields().size() > 1) ml.world.saveAllSimulationStates();
					else ml.world.saveCurrentSimulationState();
					break;
				case "SaveField":
					ml.world.saveCurrentSimulationState();
					break;
				
				/* Library */
				case "CreateLibrary":
					ml.state.librarySetup = true;
					ml.createNewLibrary = true;
					ml.state.chooseMediaFolders = true;
//					ml.display.window.btnImportMediaFolder.setVisible(false);
//					ml.display.window.btnMakeLibrary.setVisible(false);
//					ml.display.window.lblImport.setVisible(false);

//					display.window.closeLibraryWindow();
					break;
	
				case "OpenLibrary":
					ml.state.librarySetup = true;
					if(ml.createNewLibrary) ml.createNewLibrary = false;
					ml.display.window.btnCreateLibrary.setVisible(false);
					ml.display.window.btnOpenLibrary.setVisible(false);
					ml.display.window.btnLibraryHelp.setVisible(false);
					ml.display.window.lblLibrary.setVisible(false);
//					ml.display.window.lblLibraryWait.setVisible(true);
//					display.window.hideLibraryWindow();
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
					
				case "CloseHelp":
					if(ml.display.window.setupHelpWindow && ml.display.window.showHelpWindow) 
						ml.display.window.hideHelpWindow();
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
					
				case "NearestImage":
					ml.world.viewer.moveToNearestClusterWithType(ml.world.viewer.getMovementTeleport(), 0, false);
					break;
				case "NearestPanorama":
					ml.world.viewer.moveToNearestClusterWithType(ml.world.viewer.getMovementTeleport(), 1, false);
					break;
				case "NearestVideo":
					ml.world.viewer.moveToNearestClusterWithType(ml.world.viewer.getMovementTeleport(), 2, false);
					break;
				case "NearestSound":
					ml.world.viewer.moveToNearestClusterWithType(ml.world.viewer.getMovementTeleport(), 3, false);
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
	
				/* Time */
				case "NextTime":
					ml.world.viewer.moveToNextTimeSegment(true, true, ml.world.viewer.getMovementTeleport(), true);
					break;
				case "PreviousTime":
					ml.world.viewer.moveToPreviousTimeSegment(true, true, ml.world.viewer.getMovementTeleport(), true);
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

				/* Media */
				case "OpenMediaWindow":
					display.window.openMediaWindow();
					break;
				case "CloseGraphicsWindow":
					display.window.hideMediaWindow();
					break;
	
//				case "ZoomIn":
//					ml.world.viewer.zoomIn();
//					break;
//				case "ZoomOut":
//					ml.world.viewer.zoomOut();
//					break;

				case "SelectFront":
					ml.world.viewer.chooseMediaInFront(true);
					break;
				case "SelectPanorama":
					ml.world.viewer.choosePanoramaNearby(true);
					break;
					
				case "DeselectFront":
					ml.world.viewer.chooseMediaInFront(false);
					if(ml.display.displayView == 4)
						ml.display.setDisplayView(ml.world, 0);			// Set current view to Media Display View
					break;
				case "DeselectPanorama":
					ml.world.viewer.choosePanoramaNearby(false);
					break;

				case "DeselectAll":
					ml.world.getCurrentField().deselectAllMedia(false);
					if(ml.display.displayView == 4)
						ml.display.setDisplayView(ml.world, 0);			// Set current view to Media Display View
					break;
	
				case "ViewSelected":
					if(ml.display.displayView == 0)
						ml.world.viewer.startViewingSelectedMedia();
					else if(ml.display.displayView == 4)
						ml.world.viewer.stopViewingSelectedMedia();
					break;
				case "StitchPanorama":
					ml.world.getCurrentCluster().stitchImages(ml.stitcher, ml.library.getLibraryFolder(), ml.world.getCurrentField().getSelectedImages());    			
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
				
				/* Statistics */
				case "OpenStatisticsWindow":
					display.window.openStatisticsWindow();
					break;
				case "CloseStatisticsWindow":
					display.window.hideStatisticsWindow();
					break;
	
				/* Map */
				case "SetMapView":
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
					if(ml.world.getFields().size() > 1)
						ml.display.map2D.zoomToField(ml.world, ml.world.getCurrentField(), true);
					else
						ml.display.map2D.zoomToWorld(true);
					break;
				case "ZoomToWorld":
					ml.display.map2D.zoomToWorld(true);
					break;
//				case "ZoomToSelected":
//					break;
				
				/* Time */
				case "SetTimeView":
					display.setDisplayView(ml.world, 2);
					break;
				case "TimelineReverse":
						ml.display.scroll(ml.world, -1);
					break;
				case "TimelineForward":
					ml.display.scroll(ml.world, 1);
					break;
				case "TimelineZoomIn":
					ml.display.zoom(ml.world, -1, true);
					break;
				case "TimelineZoomOut":
					ml.display.zoom(ml.world, 1, true);
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
					
//					if (key == 'j') 
//						ml.world.viewer.moveToRandomCluster(true, false);				// Jump (teleport) to random cluster
//
//					if (key == 'r')									// Zoom out to whole timeline
//						ml.display.resetZoom(ml.world, true);
//
//					if (key == 'z')									// Zoom to fit timeline
//						ml.display.zoomToTimeline(ml.world, true);
//
//					if (key == 't')									// Zoom to fit current time segment
//						ml.display.zoomToCurrentSelectableTimeSegment(ml.world, true);
//
//					if (key == 'd')									// Zoom to fit current date
//						ml.display.zoomToCurrentSelectableDate(ml.world, true);

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
			}
		}
		
		if(event == GEvent.RELEASED)
		{
//			System.out.println("RELEASED EVENT: tag:"+button.tag);
			
			switch(button.tag)
			{
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
			/* Main Window */
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
			
			case "Following":
				if(option.isSelected())
				{
					if(!world.viewer.isFollowing())
					{
						switch(world.viewer.getFollowMode())
						{
						case 0:
							world.viewer.followTimeline(true, false);
							break;
						case 1:
							world.viewer.startFollowingGPSTrack();
							break;
						case 2:
							world.viewer.followMemory();
							break;
						}
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
				display.window.chkbxDomeView.setEnabled(world.viewer.getSettings().orientationMode);
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
//				if(world.viewer.inSelectionMode())
//				{
//					world.ml.display.window.btnSelectFront.setEnabled(true);
//					world.ml.display.window.btnViewSelected.setEnabled(true);
//					world.ml.display.window.btnDeselectFront.setEnabled(true);
//					world.ml.display.window.btnDeselectAll.setEnabled(true);
//					world.ml.display.window.btnExportMedia.setEnabled(true);
//					world.ml.display.window.chkbxMultiSelection.setEnabled(true);
//					world.ml.display.window.chkbxSegmentSelection.setEnabled(true);
//					world.ml.display.window.chkbxShowMetadata.setEnabled(true);
//				}
//				else
//				{
//					world.getCurrentField().deselectAllMedia(false);		// Deselect media if left Selection Mode
//					if(world.ml.display.displayView == 4)
//					{
//						world.ml.display.setMediaViewObject(-1, -1);		// Reset current Media View object
//						world.ml.display.setDisplayView(world, 0);			// Set Display View to World
//					}
//					world.ml.display.window.btnSelectFront.setEnabled(false);
//					world.ml.display.window.btnViewSelected.setEnabled(false);
//					world.ml.display.window.btnDeselectFront.setEnabled(false);
//					world.ml.display.window.btnDeselectAll.setEnabled(false);
//					world.ml.display.window.btnExportMedia.setEnabled(false);
//					world.ml.display.window.chkbxMultiSelection.setEnabled(false);
//					world.ml.display.window.chkbxSegmentSelection.setEnabled(false);
//					world.ml.display.window.chkbxShowMetadata.setEnabled(false);
//				}
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
		
//		if(display.displayView == 1 || (display.displayView == 3 && display.libraryViewMode == 0))
		if(display.displayView == 1)
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