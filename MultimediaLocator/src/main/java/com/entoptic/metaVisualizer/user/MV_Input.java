package main.java.com.entoptic.metaVisualizer.user;

import g4p_controls.GButton;
import g4p_controls.GEvent;
import g4p_controls.GToggleControl;
import g4p_controls.GValueControl;
import main.java.com.entoptic.metaVisualizer.MetaVisualizer;
import main.java.com.entoptic.metaVisualizer.world.WMV_World;
import processing.core.PApplet;

/**************************************
 * Methods for responding to user input
 * @author davidgordon
 */
public class MV_Input
{
	/* Classes */
	MV_KeyboardControls keyboard;					/* Keyboard input class */
	private MV_MouseControls mouse;
	
	/**
	 * Constructor for input class
	 * @param newScreenWidth
	 * @param newScreenHeight
	 */
	public MV_Input() 
	{
		keyboard = new MV_KeyboardControls(this);
		mouse = new MV_MouseControls();
//		mouse = new ML_MouseControls(this);
	}

	/**
	 * Respond to key pressed
	 * @param ml Parent app
	 * @param key Key that was pressed
	 * @param keyCode Key code
	 */
	public void handleKeyPressed(MetaVisualizer ml, char key, int keyCode)
	{
		if (ml.state.running && ml.state.selectedLibrary && ml.state.fieldsInitialized )
		{
			/* General */
			keyboard.handleUniversalKeyPressed(ml, key, keyCode);

			if (ml.state.interactive)
			{
				keyboard.handleInteractiveClusteringKeyPressed(ml, key, keyCode);
			}
			else 						// Interactive Clustering Mode
			{
				keyboard.handleAllViewsKeyPressed(ml, key, keyCode);	 	 	/* Controls for both 3D + HUD Views */
				
				if(ml.display.getDisplayView() == 0)											 /* World View */
					keyboard.handleWorldViewKeyPressed(ml, key, keyCode); 		
				if(ml.display.getDisplayView() == 1)											 /* Map View */
					keyboard.handleMapViewKeyPressed(ml, key, keyCode);
				else if(ml.display.getDisplayView() == 2)									 /* Time View */
					keyboard.handleTimeViewKeyPressed(ml, key, keyCode);
//				else if(ml.display.getDisplayView() == 3)									 /* Library View */
//					keyboard.handleLibraryViewKeyPressed(ml, key, keyCode);
//				else if(ml.display.getDisplayView() == 4)							 		/* Media View */
//					keyboard.handleMediaViewKeyPressed(ml, key, keyCode);
			}
		}
	}

	/**
	 * Handle key pressed in Library View
	 * @param ml
	 * @param key
	 * @param keyCode
	 */
//	public void handleLibraryViewKeyPressed(MultimediaLocator ml, char key, int keyCode)
//	{
//		keyboard.handleLibraryViewKeyPressed(ml, key, keyCode);	 	 	/* Controls for both 3D + HUD Views */
//	}

	/**
	 * Handle key pressed in List Item View
	 * @param ml
	 * @param key
	 * @param keyCode
	 */
	public void handleListItemWindowKeyPressed(MetaVisualizer ml, char key, int keyCode)
	{
		keyboard.handleListItemWindowKeyPressed(ml, key, keyCode);
	}
	
	/**
	 * Handle key pressed in Library Window
	 * @param ml
	 * @param key
	 * @param keyCode
	 */
	public void handleLibraryWindowKeyPressed(MetaVisualizer ml, char key, int keyCode)
	{
		keyboard.handleLibraryWindowKeyPressed(ml, key, keyCode);
	}
	
	/**
	 * Respond to user key releases
	 */
	public void handleKeyReleased(MetaVisualizer ml, MV_Display display, char key, int keyCode)
	{
		if (ml.state.running && ml.state.selectedLibrary && ml.state.fieldsInitialized )
		{
			if( !ml.state.interactive )
			{
				keyboard.handleAllViewsKeyReleased(ml, display, key, keyCode);
				
				if(ml.display.getDisplayView() == 0)											 /* World View */
					keyboard.handleWorldViewKeyReleased(ml, key, keyCode);
				else if(ml.display.getDisplayView() == 1)									 /* Map View */
					keyboard.handleMapViewKeyReleased(ml, key, keyCode);
				else if(ml.display.getDisplayView() == 2)									 /* Time View */
					keyboard.handleTimeViewKeyReleased(ml, key, keyCode);
				else if(ml.display.getDisplayView() == 3)									 /* Library View */
					keyboard.handleLibraryViewKeyReleased(ml, key, keyCode);
				else if(ml.display.getDisplayView() == 4)							 		 /* Media View */
					keyboard.handleMediaViewKeyReleased(ml, key, keyCode);
			}
		}
	}
	
	/**
	 * Handle input from all sliders
	 * @param world Current world
	 * @param display 2D display object
	 * @param slider Slider that triggered the event
	 * @param event The slider event
	 */
	public void handleSliderEvent(WMV_World world, MV_Display display, GValueControl slider, GEvent event)
	{
		if(display.window.setupNavigationWindow)
		{
			if(slider.tag == "GPSTrackSpeed")
				world.viewer.setGPSTrackSpeed( slider.getValueF() );
			if (slider.tag == "TeleportLength")
				world.viewer.setTeleportLength( slider.getValueI() );
			if (slider.tag == "PathWaitLength")
				world.viewer.setPathWaitLength( slider.getValueI() );
		}
		
		if(display.window.setupMediaWindow)
		{
			if (slider.tag == "VisibleAngle") 
				world.viewer.setVisibleAngle( slider.getValueF() );
			
			if (slider.tag == "FarClipping")
				world.viewer.setFarViewingDistance( slider.getValueF() );
			
			if (slider.tag == "ModelFarClipping")
				world.viewer.setModelFarViewingDistance( slider.getValueF() );
			
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
			
			if (slider.tag == "VideoVolumeMax")
			{
				world.settings.videoMaxVolume = PApplet.round(slider.getValueF() * 1000.f) * 0.001f;	// -- Should smoothly fade
			}
			
			if (slider.tag == "SoundVolumeMax")
			{
				world.settings.soundMaxVolume = PApplet.round(slider.getValueF() * 1000.f) * 0.001f;	// -- Should smoothly fade
			}
			
			if (slider.tag == "FarHearingDistance")
			{
				world.viewer.setFarHearingDistance( PApplet.round(slider.getValueF() * 1000.f) * 0.001f );	// -- Should smoothly fade
			}
		}
		
		if(display.window.setupTimeWindow)
		{
			if (slider.tag == "TimeCycleLength") 
			{
				switch(world.state.timeMode)
				{
					case 0:														// Cluster
						world.setClusterTimeCycleLength( slider.getValueI(), false );
						break;
					case 1:														// Field
						world.setTimeCycleLength( slider.getValueI(), false );
						break;
					default: 
						break;
				}
			}
			if (slider.tag == "SetMediaLength") 
			{
				world.setStaticMediaLength(slider.getValueI());
			}

			if (slider.tag == "SetCurrentTime") 
			{
				if(world.state.getTimeMode() == 0)
					world.setCurrentClusterTime(slider.getValueF(), false);		// Set current time relative to current Time Mode
				else
					world.setCurrentFieldTime(slider.getValueF(), false);		// Set current time relative to current Time Mode
			}
			
			if (slider.tag == "SetFadeLength") 
			{
				world.setFadeLength(slider.getValueI());
			}

			if (slider.tag == "SetClusterLength") 
			{
				world.setClusterLength(slider.getValueF());
			}
		}
	}

	/**
	 * Handle button input
	 * @param ml Parent app
	 * @param display Display object
	 * @param button GButton that was pressed
	 * @param event GEvent that occurred
	 */
	public void handleButtonEvent(MetaVisualizer ml, MV_Display display, GButton button, GEvent event) 
	{
		if(event == GEvent.CLICKED)				// Button clicked
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
//					ml.display.window.btnLibraryHelp.setVisible(false);
					ml.display.window.lblStartup.setVisible(false);
					break;
				
				case "CloseLibrary":
					ml.restart();
					break;
					
				case "Quit":
					ml.exitProgram();
					break;
					
//				case "LibraryHelp":
//					if(!ml.display.window.setupHelpWindow) ml.display.window.openHelpWindow();
//					else if(!ml.display.window.showHelpWindow) ml.display.window.showHelpWindow();
//					break;
//					
//				case "AboutHelp":
//					if(!ml.display.window.setupHelpWindow) ml.display.window.openHelpWindow();
//					else if(!ml.display.window.showHelpWindow) ml.display.window.showHelpWindow();
//					ml.display.window.helpAboutText = 0;
//					break;
//					
//				case "ImportHelp":
//					if(!ml.display.window.setupHelpWindow) ml.display.window.openHelpWindow();
//					else if(!ml.display.window.showHelpWindow) ml.display.window.showHelpWindow();
//					ml.display.window.helpAboutText = 1;
//					break;
//					
////			case "CloseHelp":
////				if(ml.display.window.setupHelpWindow && ml.display.window.showHelpWindow) 
////					ml.display.window.hideHelpWindow();
////				break;
	
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
					case "SetWorldView":
						if(display.getDisplayView() != 0)
							display.setDisplayView(ml.world, 0);
						break;
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
					case "MoveUp":
						ml.world.viewer.stopMoveYTransition();
						break;
					case "MoveDown":
						ml.world.viewer.stopMoveYTransition();
						break;

					case "LookLeft":								
						ml.world.viewer.stopRotateXTransition();
						break;
					case "LookRight":								
						ml.world.viewer.stopRotateXTransition();
						break;
					case "LookUp":
						ml.world.viewer.stopRotateYTransition();
						break;
					case "LookDown":
						ml.world.viewer.stopRotateYTransition();
						break;
					case "LookAround":
						ml.world.viewer.lookAtNearestMedia( ml.world.getVisibleClusters(), !ml.world.getState().timeFading );
						break;

					case "StopViewer":
						ml.world.viewer.stop(true);
						break;

					/* Model */
					case "SubjectDistanceUp":
//						System.out.println("" + ml.frameCount + " SubjectDistanceDown CLICKED...");
						ml.display.window.subjectDistanceUpBtnDown = false;
						ml.world.getCurrentField().stopFadingFocusDistances();
//						ml.world.getCurrentField().transitionFocusDistances(0.85f);
						break;
					case "SubjectDistanceDown":
//						System.out.println("" + ml.frameCount + " SubjectDistanceUp CLICKED...");
						ml.display.window.subjectDistanceDownBtnDown = false;
						ml.world.getCurrentField().stopFadingFocusDistances();
//						ml.world.getCurrentField().transitionFocusDistances(1.176f);
						break;
					case "SubjectDistanceReset":
						ml.world.getCurrentField().resetFocusDistances(ml.world);
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
						ml.world.viewer.openChooseGPSTrackWindow();
						break;
						
					/* Memory */
					case "SaveLocation":
						ml.world.viewer.addPlaceToMemory();
						break;
					case "ClearMemory":
						ml.world.viewer.clearMemory();
						break;
//					case "SetHome":
//						ml.world.getCurrentField().setHome(ml.world.viewer.getLocationAsWaypoint());
//						break;
		
					/* Map */
					case "SetMapView":
						if(display.getDisplayView() != 1)
							display.setDisplayView(ml.world, 1);
						break;
						
//					case "PanUp":
//						if(ml.display.map2D.isPanning())
//							ml.display.map2D.stopPanning();
//						break;
//					case "PanLeft":
//						if(ml.display.map2D.isPanning())
//							ml.display.map2D.stopPanning();
//						break;
//					case "PanDown":
//						if(ml.display.map2D.isPanning())
//							ml.display.map2D.stopPanning();
//						break;
//					case "PanRight":
//						if(ml.display.map2D.isPanning())
//							ml.display.map2D.stopPanning();
//						break;
//					case "ZoomToViewer":
//						ml.display.map2D.zoomToCluster(ml.world, ml.world.getCurrentCluster(), true);	// Zoom to current cluster
//						break;
//					case "ZoomToField":
//							ml.display.map2D.zoomToField(ml.world.getCurrentField(), true);
//						break;
//					case "MapZoomIn":
//						if(ml.display.map2D.isZooming())
//							ml.display.map2D.stopZooming();
//						break;
//					case "ResetMapZoom":
//						ml.display.map2D.resetMapZoom(true);
//	//					ml.display.map2D.zoomToWorld(true);
//						break;
//					case "MapZoomOut":
//						if(ml.display.map2D.isZooming())
//							ml.display.map2D.stopZooming();
//						break;
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
//				case "TimelineZoomIn":
//					ml.display.stopZooming();
//					break;
//				case "TimelineZoomOut":
//					ml.display.stopZooming();
//					break;
//				case "TimelineZoomToFit":
//					ml.display.zoomToTimeline(ml.world, true);
//					break;
//				case "TimelineZoomToSelected":
//					ml.display.zoomToCurrentSelectableTimeSegment(ml.world, true);
//					break;
//				case "TimelineZoomToDate":
//					ml.display.zoomToCurrentSelectableDate(ml.world, true);
//					break;
//				case "TimelineZoomToFull":
//					ml.display.resetZoom(ml.world, true);
//					break;
					
				/* Library View */
				case "OpenPreferencesWindow":
					if(!ml.display.window.showPreferencesWindow)
						ml.display.window.openPreferencesWindow();
					else
						ml.display.window.closePreferencesWindow();
					break;
				case "SetLibraryView":
					if(display.getDisplayView() != 3)
						display.setDisplayView(ml.world, 3);
					break;
				case "ClosePreferencesWindow":
					ml.display.window.closePreferencesWindow();
					break;
				case "ExitPreferencesWindow":
					display.window.openMainMenu();
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
		
		if(event == GEvent.PRESSED)				// Button pressed
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

				case "MoveForward":
					if(ml.world.viewer.getState().movingZ)
						ml.world.viewer.stopMoveZTransition();
					else
						ml.world.viewer.walkForward();
					break;
				case "MoveLeft":
					if(ml.world.viewer.getState().movingX)
						ml.world.viewer.stopMoveXTransition();
					else
						ml.world.viewer.sidestepLeft();
					break;
				case "MoveRight":
					if(ml.world.viewer.getState().movingX)
						ml.world.viewer.stopMoveXTransition();
					else
						ml.world.viewer.sidestepRight();
					break;
				case "MoveBackward":
					if(ml.world.viewer.getState().movingZ)
						ml.world.viewer.stopMoveZTransition();
					else
						ml.world.viewer.walkBackward();
					break;
				case "MoveUp":
					if(ml.world.viewer.getState().movingY)
						ml.world.viewer.stopMoveYTransition();
					else
						ml.world.viewer.walkUp();
					break;
				case "MoveDown":
					if(ml.world.viewer.getState().movingY)
						ml.world.viewer.stopMoveYTransition();
					else
						ml.world.viewer.walkDown();
					break;
					
				case "LookUp":
					if(ml.world.viewer.getState().rotatingY)
						ml.world.viewer.stopRotateYTransition();
					else
						ml.world.viewer.rotateY(-1);
					break;
				case "LookDown":
					if(ml.world.viewer.getState().rotatingY)
						ml.world.viewer.stopRotateYTransition();
					else
						ml.world.viewer.rotateY(1);
					break;
				case "LookLeft":
					if(ml.world.viewer.getState().rotatingX)
						ml.world.viewer.stopRotateXTransition();
					else
						ml.world.viewer.rotateX(-1);
					break;
				case "LookRight":
					if(ml.world.viewer.getState().rotatingX)
						ml.world.viewer.stopRotateXTransition();
					else
						ml.world.viewer.rotateX(1);
					break;
					
				/* Model */
				case "SubjectDistanceUp":
//					System.out.println("" + ml.frameCount + " SubjectDistanceDown PRESSED...");
					ml.display.window.subjectDistanceUpBtnDown = true;
					ml.world.getCurrentField().fadeFocusDistances(ml.world, 0.985f);
//					ml.world.getCurrentField().startFadingFocusDistances(0.98f);
					break;
				case "SubjectDistanceDown":
//					System.out.println("" + ml.frameCount + " SubjectDistanceUp PRESSED...");
					ml.display.window.subjectDistanceDownBtnDown = true;
					ml.world.getCurrentField().fadeFocusDistances(ml.world, 1.015228f);
//					ml.world.getCurrentField().startFadingFocusDistances(1.0204f);
					break;

//				case "SubjectDistanceReset":
//					ml.world.getCurrentField().resetObjectDistances();
//					break;
					
				/* Map */
//				case "PanUp":
//					if(ml.display.map2D.isPanning())
//						ml.display.map2D.stopPanning();
//					else
//						ml.display.map2D.panUp();
//					break;
//				case "PanLeft":
//					if(ml.display.map2D.isPanning())
//						ml.display.map2D.stopPanning();
//					else
//						ml.display.map2D.panLeft();
//					break;
//				case "PanDown":
//					if(ml.display.map2D.isPanning())
//						ml.display.map2D.stopPanning();
//					else
//						ml.display.map2D.panDown();
//					break;
//				case "PanRight":
//					if(ml.display.map2D.isPanning())
//						ml.display.map2D.stopPanning();
//					else
//						ml.display.map2D.panRight();
//					break;
//				case "MapZoomIn":
//					if(ml.display.map2D.isZooming())
//						ml.display.map2D.stopZooming();
//					else
//						ml.display.map2D.startZoomingIn(ml.world);
//					break;
//				case "MapZoomOut":
//					if(ml.display.map2D.isZooming())
//						ml.display.map2D.stopZooming();
//					else
//						ml.display.map2D.startZoomingOut(ml.world);
//					break;
				
				/* Timeline */
//				case "TimelineZoomIn":
//					if(ml.display.isZooming())
//						ml.display.stopZooming();
//					else
//						ml.display.zoom(ml.world, -1, true);
//					break;
//				case "TimelineZoomOut":
//					if(ml.display.isZooming())
//						ml.display.stopZooming();
//					else
//						ml.display.zoom(ml.world, 1, true);
//					break;
//				case "TimelineReverse":
//					if(ml.display.isScrolling())
//						ml.display.stopScrolling();
//					else
//						ml.display.scroll(ml.world, -1);
//					break;
//				case "TimelineForward":
//					if(ml.display.isScrolling())
//						ml.display.stopScrolling();
//					else
//						ml.display.scroll(ml.world, 1);
//					break;
			}
		}
		
		if(event == GEvent.RELEASED)			// Button released 
		{
			switch(button.tag)
			{
				/* Navigation */
				case "ZoomIn":
					ml.world.viewer.stopZooming();
					break;
				case "ZoomOut":
					ml.world.viewer.stopZooming();
					break;
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
				case "MoveUp":
					ml.world.viewer.stopMoveYTransition();
					break;
				case "MoveDown":
					ml.world.viewer.stopMoveYTransition();
					break;
					
				case "LookLeft":								
					ml.world.viewer.stopRotateXTransition();
					break;
				case "LookRight":								
					ml.world.viewer.stopRotateXTransition();
					break;
				case "LookUp":
					ml.world.viewer.stopRotateYTransition();
					break;
				case "LookDown":
					ml.world.viewer.stopRotateYTransition();
					break;
					
				/* Model */
				case "SubjectDistanceUp":
//					System.out.println("" + ml.frameCount + " SubjectDistanceDown RELEASED...");
					ml.display.window.subjectDistanceUpBtnDown = false;
					ml.world.getCurrentField().stopFadingFocusDistances();
					break;
				case "SubjectDistanceDown":
//					System.out.println("" + ml.frameCount + " SubjectDistanceUp RELEASED...");
					ml.display.window.subjectDistanceDownBtnDown = false;
					ml.world.getCurrentField().stopFadingFocusDistances();
					break;
				case "SubjectDistanceReset":
					ml.world.getCurrentField().resetFocusDistances(ml.world);
					break;

//					/* Model */
//				case "SubjectDistanceDown":
//					ml.world.getCurrentField().startFadingFocusDistances(0.98f);
//					ml.world.getCurrentField().transitionFocusDistances(0.85f);
//					break;
//				case "SubjectDistanceUp":
//					ml.world.getCurrentField().startFadingFocusDistances(1.0204f);
//					ml.world.getCurrentField().transitionFocusDistances(1.176f);
//					break;

				/* Map */
//				case "PanUp":
//				case "PanLeft":
//				case "PanDown":
//				case "PanRight":
//					display.map2D.stopPanning();
//					break;
//				case "MapZoomIn":
//				case "MapZoomOut":
//					display.map2D.stopZooming();
//					break;
					
				/* Timeline */
//				case "TimelineZoomIn":
//					ml.display.stopZooming();
//					break;
//				case "TimelineZoomOut":
//					ml.display.stopZooming();
//					break;
//				case "TimelineReverse":
//					ml.display.stopScrolling();
//					break;
//				case "TimelineForward":
//					ml.display.stopScrolling();
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
	public void handleToggleControlEvent(WMV_World world, MV_Display display, GToggleControl option, GEvent event) 
	{
		switch (option.tag)
		{
			/* Create Library Window */
			case "RebuildLibrary":
				world.mv.state.rebuildLibrary = option.isSelected();
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
			case "LibraryView":							
				if(display.getDisplayView() != 3)
					display.setDisplayView(world, 3);
				break;
			case "ScreenMessagesOn":
				world.settings.screenMessagesOn = option.isSelected();
//				ml.display.window.chkbxScreenMessagesOn.setSelected(ml.world.settings.screenMessagesOn);
				break;
				
//			case "SetMapViewWorldMode":
//				display.setMapViewMode(0);
////				if(option.isSelected()) display.window.optMapViewFieldMode.setSelected(false);	
//				break;
//			case "SetMapViewFieldMode":
//				display.setMapViewMode(1);
////				if(option.isSelected()) display.window.optMapViewWorldMode.setSelected(false);	
//				break;

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
//				System.out.println("FollowTimeline");
				if(option.isSelected())
				{
					world.viewer.setPathNavigationMode( 0 );
					display.window.optGPSTrack.setSelected(false);
					display.window.optMemory.setSelected(false);
				}
				break;
	  		case "FollowGPSTrack":
//				System.out.println("FollowGPSTrack");
				if(option.isSelected())
				{
					world.viewer.setPathNavigationMode( 1 );
					display.window.optTimeline.setSelected(false);
					display.window.optMemory.setSelected(false);
				}
				break;
	  		case "FollowMemory":
//				System.out.println("FollowMemory");
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
			
			case "PathFollowing":
				if(option.isSelected())
				{
					if(!world.viewer.isFollowing())
						world.viewer.startFollowing();
				}
				else 
				{
					if(world.viewer.isFollowing())
						world.viewer.stopFollowing();
					if(world.viewer.isFollowingGPSTrack())
						world.viewer.stopFollowingGPSTrack();
				}
				break;
				
			case "FollowTeleport":
				world.viewer.setFollowTeleport( option.isSelected() );
				break;
				
			/* Time */
			case "ClusterTimeMode":
				world.setTimeMode(0, false);
				break;
			case "FieldTimeMode":
				world.setTimeMode(1, false);
				break;
//			case "MediaTimeMode":			// -- Disabled
//				world.setTimeMode(2);
//				break;
				
			case "TimeFading":
				if(option.isSelected())
					world.turnTimeFadingOn();
				else
					world.turnTimeFadingOff();
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
			case "DisplayTerrain":
				boolean state = world.state.displayTerrain;
				if(state)
					world.fadeOutTerrain(true, true);
				else
				{
					world.state.terrainAlpha = 0.f;
					world.state.displayTerrain = true;
					world.fadeInTerrain(true);
				}
				break;
			
			case "OrientationMode":
				world.viewer.setOrientationMode( !world.viewer.getSettings().orientationMode );
//				display.window.chkbxDomeView.setEnabled(world.viewer.getSettings().orientationMode);
				break;
			case "DomeView":
				world.mv.state.sphericalView = option.isSelected();
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
				if(world.mv.display.getLibraryViewMode() != 0)
					world.mv.display.setLibraryViewMode(0);
				break;
			case "LibraryViewModeField":
				if(world.mv.display.getLibraryViewMode() != 1)
					world.mv.display.setLibraryViewMode(1);
				break;
			case "LibraryViewModeCluster":
				if(world.mv.display.getLibraryViewMode() != 2)
					world.mv.display.setLibraryViewMode(2);
				break;
		}
	}

	/* Mouse */
	public void updateMouseSelection(int mouseX, int mouseY, int frameCount)
	{
		mouse.updateMouseSelection(mouseX, mouseY, frameCount);
	}
	
	public void handleMousePressed(WMV_World world, MV_Display display, int mouseX, int mouseY, int frameCount)
	{
		mouse.handleMousePressed(world, display, mouseX, mouseY, frameCount);
	}
	
	public void handleMouseReleased(WMV_World world, MV_Display display, int mouseX, int mouseY, int frameCount)
	{
		mouse.handleMouseReleased(world, display, mouseX, mouseY, frameCount);
	}
	
	public void handleMouseClicked(int mouseX, int mouseY, int frameCount)
	{
		mouse.handleMouseClicked(mouseX, mouseY, frameCount);
	}
	
	public void handleMouseDragged(int mouseX, int mouseY)
	{
		mouse.handleMouseDragged(mouseX, mouseY);
	}

	/**
	 * Update mouse navigation									//-- Disabled
	 * @param viewer
	 * @param mouseX
	 * @param mouseY
	 * @param frameCount
	 */
//	public void updateMouseNavigation(WMV_Viewer viewer, int mouseX, int mouseY, int frameCount)
//	{			
//		mouse.updateMouseNavigation(viewer, mouseX, mouseY, frameCount);
//	}
}