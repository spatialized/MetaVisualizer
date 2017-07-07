package main.java.com.entoptic.multimediaLocator;

//import java.awt.Font;

//import g4p_controls.G4P;
//import g4p_controls.GButton;
import processing.core.PApplet;

/**
 * Keyboard input handler
 * @author davidgordon
 */
public class ML_KeyboardControls 
{
	private ML_Input input;
	
	/**
	 * Constructor for keyboard input handler
	 * @param parent
	 */
	ML_KeyboardControls(ML_Input parent)
	{
		input = parent;
	}
	
	/**
	 * Handle key commands for any view in any program state
	 * @param ml Parent app
	 * @param key Key pressed
	 * @param keyCode Key code
	 */
	public void handleUniversalKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		if (key == ' ') 
		{
			if(ml.display.window.showMainMenu)
				ml.display.window.hideMainMenu();
			else
				ml.display.window.openMainMenu();
		}

		if (key == '!') 
		{
			if(!ml.display.window.showNavigationWindow)
				ml.display.window.openNavigationWindow();
			else
				ml.display.window.hideNavigationWindow();
		}

		if (key == '@')
		{
			if(!ml.display.window.showMediaWindow)
				ml.display.window.openMediaWindow();
			else
				ml.display.window.hideMediaWindow();
		}

		if (key == '#') 
		{
			if(!ml.display.window.showTimeWindow)
				ml.display.window.openTimeWindow();
			else
				ml.display.window.hideTimeWindow();
		}

		if (key == '$') 
		{
			if(!ml.display.window.showLibraryViewWindow)
				ml.display.window.openLibraryViewWindow();
			else
				ml.display.window.hideLibraryViewWindow();
		}

		if (key == '^') 
		{
			if(!ml.display.window.showHelpWindow)
				ml.display.window.openHelpWindow();
			else
				ml.display.window.hideHelpWindow();
		}
		
		if (key == 'Q')
			ml.exitProgram();

		if (key == 'R')
			ml.restart();

		if (keyCode == PApplet.SHIFT) 
			input.shiftKey = true;

		if (keyCode == PApplet.ALT) 
			input.optionKey = true;
	}
	
	/**
	 * Handle key pressed that affects any View Mode
	 * @param ml
	 * @param key
	 * @param keyCode
	 */
	public void handleAllViewsKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		/* Display Modes */
		if (key == '1') 
			ml.display.setDisplayView(ml.world, 0);		// World View

		if (key == '2') 
			ml.display.setDisplayView(ml.world, 1);		// Map View

		if (key == '3') 
			ml.display.setDisplayView(ml.world, 2);		// Time View

		if (key == '4')									
			ml.display.setDisplayView(ml.world, 3);		// Library View

//		if (key == 'h')
//			ml.world.getCurrentField().setHome(ml.world.viewer.getLocationAsWaypoint());
			
		if (key == 'H')
		{
			ml.world.settings.screenMessagesOn = !ml.world.settings.screenMessagesOn;
			if(ml.display.window.setupMainMenu)
				ml.display.window.chkbxScreenMessagesOn.setSelected(ml.world.settings.screenMessagesOn);
			
//			ml.world.settings.showStitchedPanoramas = !ml.world.settings.showStitchedPanoramas;
		}

		if( key == '/' )
			ml.world.saveCurrentFieldState();

		if( key == 't' && !input.optionKey )
		{
			boolean state = !ml.world.viewer.getNavigationTeleport();
			ml.world.viewer.setNavigationTeleport( state );
			if(ml.display.window.setupNavigationWindow)
			{
				ml.display.window.optMove.setSelected(!state);
				ml.display.window.optTeleport.setSelected(state);
			}
		}

		if( key == 'T' && !input.optionKey )
		{
			boolean state = ml.world.state.displayTerrain;
			if(state)
				ml.world.fadeOutTerrain(true, true);
			else
			{
				ml.world.state.terrainAlpha = 0.f;
				ml.world.state.displayTerrain = true;
				ml.world.fadeInTerrain(true);
			}
		}

		if (key == 'f') 
		{
			boolean state = !ml.world.getState().timeFading;
			ml.world.setTimeFading(state);
			if(ml.display.window.setupTimeWindow)
				ml.display.window.chkbxTimeFading.setSelected(state);
		}

		if (key == '9')														
		{
			ml.world.viewer.setOrientationMode( !ml.world.viewer.getSettings().orientationMode );
			if(ml.display.window.setupMediaWindow)
				ml.display.window.chkbxOrientationMode.setSelected(ml.world.viewer.getSettings().orientationMode);
		}

		if (key == 'C') 													// Choose field from list
			ml.world.viewer.chooseFieldDialog();

		if (key == '-')
		{
			ml.world.state.paused = !ml.world.getState().paused;
			if(ml.display.window.setupTimeWindow)
				ml.display.window.chkbxPaused.setSelected(ml.world.getState().paused);
		}

		if (key == '=')
		{
			if(ml.world.state.timeMode == 0)
				ml.world.setTimeMode(1);
			else				
				ml.world.setTimeMode(0);
		}
	}
	
	/**
	 * Handle key pressed in World View
	 * @param ml
	 * @param key
	 * @param keyCode
	 */
	public void handleWorldViewKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		/* Settings for Show Model Option */
		if (key == '5')
			ml.world.setShowModel(!ml.world.getState().showModel);

		if (key == '6')
		{
			if(ml.world.getState().showModel)
			{
				ml.world.state.showMediaToCluster = !ml.world.getState().showMediaToCluster;			// Draw line from each media point to cluster
				if(ml.display.window.showMediaWindow)
					ml.display.window.chkbxMediaToCluster.setSelected( ml.world.state.showMediaToCluster );
			}			
		}

		if (key == '7')
		{
			if(ml.world.getState().showModel)
			{
				ml.world.state.showCaptureToMedia = !ml.world.getState().showCaptureToMedia;			// Draw line from each media point to its capture location
				if(ml.display.window.showMediaWindow)
					ml.display.window.chkbxCaptureToMedia.setSelected( ml.world.state.showCaptureToMedia );
			}
		}

		if (key == '8')
		{
			if(ml.world.getState().showModel)
			{
				ml.world.state.showCaptureToCluster = !ml.world.getState().showCaptureToCluster;		// Draw line from each media capture location to associated cluster
				if(ml.display.window.showMediaWindow)
					ml.display.window.chkbxCaptureToCluster.setSelected( ml.world.state.showCaptureToCluster );
			}
		}

		if (key == 'M') 
		{
			boolean state = !ml.world.getState().showMetadata;
			ml.world.setShowMetadata(state);
			if(ml.display.window.setupMediaWindow)
				ml.display.window.chkbxShowMetadata.setSelected(state);
		}

		if (key == 'n')						// Teleport to next time segment on same date
			ml.world.viewer.moveToNextTimeSegment(true, true, ml.world.viewer.getNavigationTeleport(), true);

		if (key == 'b')						// Teleport to previous time segment on same date
			ml.world.viewer.moveToPreviousTimeSegment(true, true, ml.world.viewer.getNavigationTeleport(), true);

		if (key == 'N')						// Teleport to next time segment on any date
			ml.world.viewer.moveToNextTimeSegment(false, true, ml.world.viewer.getNavigationTeleport(), true);

		if (key == 'B')						// Teleport to previous time segment on any date
			ml.world.viewer.moveToPreviousTimeSegment(false, true, ml.world.viewer.getNavigationTeleport(), true);

//		if (key == '~')						// Increment Path Navigation Mode										
//		{
//			if(!ml.world.viewer.isFollowing())
//			{
//				int followMode = ml.world.viewer.getPathNavigationMode();
//				if(followMode >= 2) followMode = 0;
//				else followMode++;
//
//				ml.world.viewer.setPathNavigationMode(followMode);
//
//				if(ml.display.window.setupNavigationWindow)
//				{
//					switch(ml.world.viewer.getPathNavigationMode())		// 0: Timeline 1: GPS Track 2: Memory
//					{
//						case 0:					// Timeline
//							ml.display.window.optTimeline.setSelected(true);
//							ml.display.window.optGPSTrack.setSelected(false);
//							ml.display.window.optMemory.setSelected(false);
//							break;
//						case 1:					// GPS Track
//							ml.display.window.optTimeline.setSelected(false);
//							ml.display.window.optGPSTrack.setSelected(true);
//							ml.display.window.optMemory.setSelected(false);
//							break;
//						case 2:					// Memory
//							ml.display.window.optTimeline.setSelected(false);
//							ml.display.window.optGPSTrack.setSelected(false);
//							ml.display.window.optMemory.setSelected(true);
//							break;
//					}
//				}
//			}
//		}
		
		if (!input.optionKey && key == '>')
		{
			if(!ml.world.viewer.isFollowing())
			{
				ml.world.viewer.startFollowing();
			}
			else
			{
				ml.world.viewer.stopFollowing();
			}
		}
		
		if (key == ',')
		{
			ml.world.viewer.setFollowTeleport( !ml.world.viewer.getFollowTeleport() );
			if(ml.display.window.setupNavigationWindow)
				ml.display.window.chkbxFollowTeleport.setSelected( ml.world.viewer.getFollowTeleport() );
		}
		
		if (!input.optionKey && key == 'e')									// Move UP
			ml.world.viewer.walkUp();

		if (key == 'c') 													// Move DOWN
			ml.world.viewer.walkDown();

		if (!input.optionKey && key == 's') 
			ml.world.viewer.walkBackward();

		if (!input.optionKey && key == 'w') 
			ml.world.viewer.walkForward();

		if (!input.optionKey && key == 'a') 
			ml.world.viewer.sidestepLeft();

		if (!input.optionKey && key == 'd') 
			ml.world.viewer.sidestepRight();

		if (key == '{')
			ml.world.viewer.teleportToFieldOffset(-1, true);

		if (key == '}') 
			ml.world.viewer.teleportToFieldOffset(1, true);

		if (key == 'q') 
			ml.world.viewer.zoomIn();

		if (key == 'z') 
			ml.world.viewer.zoomOut();

		if (key == '.')
			ml.world.viewer.stop(true);

		if( key == 'l' )
			ml.world.viewer.moveToLastCluster(ml.world.viewer.getNavigationTeleport());

		if (key == 'j') 
			ml.world.viewer.moveToRandomCluster(ml.world.viewer.getNavigationTeleport(), true);				// Jump (teleport) to random cluster

		if (key == 'i') 		// Go to nearest cluster ID with image
			ml.world.viewer.moveToNearestClusterWithType(0, false, ml.world.viewer.getNavigationTeleport(), true);

		if (key == 'p') 		// Go to nearest cluster ID with panorama
			ml.world.viewer.moveToNearestClusterWithType(1, false, ml.world.viewer.getNavigationTeleport(), true);

		if (key == 'v') 		// Go to nearest cluster ID with video
			ml.world.viewer.moveToNearestClusterWithType(2, false, ml.world.viewer.getNavigationTeleport(), true);

		if (key == 'u') 		// Go to nearest cluster ID with sound
			ml.world.viewer.moveToNearestClusterWithType(3, false, ml.world.viewer.getNavigationTeleport(), true);

		if (key == 'm') 
			ml.world.viewer.moveToNearestCluster(ml.world.viewer.getNavigationTeleport());

//		if (key == 'W') 
//			ml.world.viewer.moveToNearestClusterAhead(false);
		
		if( key == 'L' )
			ml.world.viewer.lookAtNearestMedia();

		if (key == '_') 
			ml.world.getCurrentField().fadeObjectDistances(1.176f);

		if (key == '+')
			ml.world.getCurrentField().fadeObjectDistances(0.85f);
		
		if (!input.optionKey && key == ']') {
			float value = ml.world.settings.altitudeScalingFactor * 1.052f;
			ml.world.settings.altitudeScalingFactor = PApplet.constrain(value, 0.f, 1.f);
			ml.world.getCurrentField().calculateMediaLocations(true);		// Recalculate media locations
			ml.world.getCurrentField().createClusters();				// Recalculate cluster locations
			ml.world.getCurrentField().recalculateGeometries();				// Recalculate cluster locations
		}

		if (!input.optionKey && key == '[') {
			float value = ml.world.settings.altitudeScalingFactor *= 0.95f;
			ml.world.settings.altitudeScalingFactor = PApplet.constrain(value, 0.f, 1.f);
			ml.world.getCurrentField().calculateMediaLocations(true);		// Recalculate media locations
			ml.world.getCurrentField().createClusters();				// Recalculate cluster locations
			ml.world.getCurrentField().recalculateGeometries();				// Recalculate cluster locations
		}

		if (key == 'E')
		{
			boolean state = !ml.world.getState().useBlurMasks;
			ml.world.getState().useBlurMasks = state;
			if(ml.display.window.setupMediaWindow)
			{
				ml.display.window.chkbxBlurMasks.setSelected(state);
			}
		}

		if (key == 'I')	
		{
			if(ml.world.viewer.getSettings().hideImages)
				ml.world.viewer.showImages();
			else
				ml.world.viewer.hideImages();
		}

		if (key == 'P')	
		{
			if(ml.world.viewer.getSettings().hidePanoramas)
				ml.world.viewer.showPanoramas();
			else
				ml.world.viewer.hidePanoramas();
		}

		if (key == 'V')	
		{
			if(ml.world.viewer.getSettings().hideVideos)
				ml.world.viewer.showVideos();
			else
				ml.world.viewer.hideVideos();
		}

		if (key == 'U')			
		{
			if(ml.world.viewer.getSettings().hideSounds)
				ml.world.viewer.showSounds();
			else
				ml.world.viewer.hideSounds();
		}

		if (key == ';')
		{
			boolean state = !ml.world.getState().alphaMode;
			ml.world.state.alphaMode = state;
			if(ml.display.window.setupMediaWindow)
				ml.display.window.chkbxAlphaMode.setSelected(state);
		}

		if (key == ')') {		
			float newAlpha = PApplet.constrain(ml.world.getState().alpha+15.f, 0.f, 255.f);
			ml.world.startFadingAlpha(newAlpha);
		}

		if (key == '(') {
			float newAlpha = PApplet.constrain(ml.world.getState().alpha-15.f, 0.f, 255.f);
			ml.world.startFadingAlpha(newAlpha);
		}

//		if (key == '|')					// -- Disabled
//			ml.world.getCurrentCluster().stitchImages(ml.stitcher, ml.library.getLibraryFolder(), ml.world.getCurrentField().getSelectedImages());    			

//		if (key == ':')
//			ml.world.settings.showUserPanoramas = !ml.world.settings.showUserPanoramas;

		if (key == 'A') 
			ml.world.viewer.setSelection( !ml.world.viewer.inSelectionMode() );

		if (input.optionKey && key == 'x')
			ml.world.getCurrentField().deselectAllMedia(false);

		if (input.optionKey && key == '-')
			ml.world.viewer.setVisibleAngle( ml.world.viewer.getVisibleAngle() - 3.1415f / 128.f ); 

		if (input.optionKey && key == '=')
			ml.world.viewer.setVisibleAngle( ml.world.viewer.getVisibleAngle() + 3.1415f / 128.f ); 

		/* Selection */
		if (!input.optionKey && key == 'x') 
			ml.world.viewer.chooseMediaInFront(true);

		if (!input.optionKey && key == 'X')
			ml.world.viewer.chooseMediaInFront(false);

		if (!input.optionKey && key == 'k') 		
			ml.world.viewer.choosePanoramaNearby(true);

		if (key == 'K') 		
			ml.world.viewer.choosePanoramaNearby(false);

		if (key == 'S')						// Save all fields
		{
			if(ml.world.getFields().size() > 1)
				ml.world.saveLibrary();
			else
				ml.world.saveCurrentFieldState();
		}

		if (key == 'µ')		// opt + m
		{
			ml.world.viewer.setMultiSelection( !ml.world.viewer.getMultiSelection(), true );
			if(ml.world.viewer.getMultiSelection() && !ml.world.viewer.inSelectionMode())
				ml.world.viewer.setSelection( true );
		}
		
		if (key == 'ß')		// opt + s
		{
			ml.world.viewer.setGroupSelection( !ml.world.viewer.getGroupSelection(), true );
			if(ml.world.viewer.getGroupSelection() && !ml.world.viewer.inSelectionMode())
				ml.world.viewer.setSelection( true );
		}

		/* GPS */
		if (!input.optionKey && key == 'g') 
		{
			if(ml.world.getCurrentField().getGPSTracks() != null)
				if(ml.world.getCurrentField().getGPSTracks().size() > 0)
					ml.world.viewer.chooseGPSTrack();
		}
		
		/* Memory */
		if (key == '`') 
		{
			ml.world.viewer.addPlaceToMemory();
		}

		if (key == 'y') 
		{
			ml.world.viewer.clearMemory();
		}

		/* Graphics */
		if (key == 'G')
		{
			boolean state = !ml.world.viewer.getAngleFading();
			ml.world.viewer.setAngleFading( state );
			if(ml.display.window.setupMediaWindow)
				ml.display.window.chkbxAngleFading.setSelected(state);
		}

		if (input.optionKey && key == 'H')
		{
			boolean state = !ml.world.viewer.getAngleThinning();
			ml.world.viewer.setAngleThinning( state );
			if(ml.display.window.setupMediaWindow)
				ml.display.window.chkbxAngleThinning.setSelected(state);
		}

		/* Output */
		if (key == 'ø') 
			ml.selectFolder("Select an output folder:", "outputFolderSelected");
		if (key == '\\') 
		{
			if(!ml.world.outputFolderSelected) ml.selectFolder("Select an output folder:", "outputFolderSelected");
			ml.world.exportCurrentView();
////			if(ml.state.sphericalView)
////				ml.world.saveCubeMapToDisk();
////			else
//			ml.world.exportCurrentView();
		}
		if (key == 'o') 	// Save image to disk
		{	
			if(!ml.world.outputFolderSelected) ml.selectFolder("Select an output folder:", "outputFolderSelected");
			ml.world.exportCurrentMedia();
		}

		if (key == PApplet.ENTER)
		{
			ml.world.viewer.startViewingSelectedMedia();
		}
		
		if(key == PApplet.CODED)
		{
			/* Navigation */
			if (keyCode == PApplet.LEFT) 
				ml.world.viewer.rotateX(-1);

			if (keyCode == PApplet.RIGHT) 
				ml.world.viewer.rotateX(1);

			if (keyCode == PApplet.UP) 
				ml.world.viewer.rotateY(-1);

			if (keyCode == PApplet.DOWN) 
				ml.world.viewer.rotateY(1);

			/* Time */
			if (input.shiftKey && keyCode == PApplet.UP) 
				ml.world.incrementTime();

			if (input.shiftKey && keyCode == PApplet.DOWN) 
				ml.world.decrementTime();

			if (input.shiftKey && keyCode == PApplet.LEFT) 
				ml.world.decrementTimeCycleLength();

			if (input.shiftKey && keyCode == PApplet.RIGHT) 
				ml.world.incrementTimeCycleLength();
		}
	}
	
	/**
	 * Handle key pressed in Map View
	 * @param ml Parent app
	 * @param key Key pressed
	 * @param keyCode Key code
	 */
	public void handleMapViewKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		/* General Map Controls */
		if (key == 'r')									// Zoom out to whole timeline
			ml.display.map2D.resetMapZoom(true);

		if(key == 'L')
		{
			ml.display.setMapViewMode(0);
			if(ml.display.window.setupNavigationWindow) 
			{
				ml.display.window.optMapViewWorldMode.setEnabled(true);
				ml.display.window.optMapViewFieldMode.setEnabled(true);
				ml.display.window.optMapViewWorldMode.setSelected(true);	
				ml.display.window.optMapViewFieldMode.setSelected(false);	
			}
		}
	
		if(key == 'F')
		{
			ml.display.setMapViewMode(1);
			if(ml.display.window.setupNavigationWindow) 
			{
				ml.display.window.optMapViewWorldMode.setEnabled(true);
				ml.display.window.optMapViewFieldMode.setEnabled(true);
				ml.display.window.optMapViewWorldMode.setSelected(false);	
				ml.display.window.optMapViewFieldMode.setSelected(true);	
			}
		}

		/* Map Controls Specific to View Mode */
		if(ml.display.mapViewMode == 0)			// World Map Commands
		{
			if(key == PApplet.ENTER)
			{
				int selectedField = ml.display.map2D.getSelectedFieldID();
				if(input.shiftKey)
				{
					ml.world.viewer.teleportToField(selectedField, false);
				}
				else
				{
					ml.world.viewer.teleportToField(selectedField, true);
					ml.display.setDisplayView( ml.world, 0 );
				}
			}
			
//			if (input.shiftKey && key == 'c')
//				ml.startInitialClustering();				// Re-run clustering on all fields
		}
		else									// Field Map Commands
		{
//			if (key == 'c')
//			{
////			ml.display.map2D.resetMapZoom(ml.world, true);
//				ml.startInteractiveClustering();
//			}

			if(key == PApplet.ENTER)
			{
				ml.world.viewer.moveToClusterOnMap(ml.display.map2D.getSelectedClusterID(), input.shiftKey);
			}	
		}
		
		/* Navigation Controls for Both Field and World Maps */
		if (key == '.')
			ml.world.viewer.stop(true);

		if (key == 's') 
			ml.display.map2D.startZoomingOut(ml.world);
		
		if (key == 'w') 
			ml.display.map2D.startZoomingIn(ml.world);
		
		if (key == 'j') 
			ml.world.viewer.moveToRandomCluster(true, false);				// Teleport to random cluster

		if (key == 'i') 		// Go to nearest cluster ID with image
			ml.world.viewer.moveToNearestClusterWithType(0, false, true, false);

		if (key == 'p') 		// Go to nearest cluster ID with panorama
			ml.world.viewer.moveToNearestClusterWithType(1, false, true, false);

		if (key == 'v') 		// Go to nearest cluster ID with video
			ml.world.viewer.moveToNearestClusterWithType(2, false, true, false);

		if (key == 'u') 		// Go to nearest cluster ID with sound
			ml.world.viewer.moveToNearestClusterWithType(3, false, true, false);

		if (key == 'm') 
			ml.world.viewer.moveToNearestCluster(false);

		if (key == '{')
			ml.world.viewer.teleportToFieldOffset(-1, false);

		if (key == '}') 
			ml.world.viewer.teleportToFieldOffset(1, false);

		if (!input.optionKey && key == 'S') 
			ml.world.viewer.walkBackward();

		if (!input.optionKey && key == 'W') 
			ml.world.viewer.walkForward();

		if (!input.optionKey && key == 'A') 
			ml.world.viewer.sidestepLeft();

		if (!input.optionKey && key == 'D') 
			ml.world.viewer.sidestepRight();

		if( key == 'l' )
			ml.world.viewer.moveToLastCluster(false);

		if (key == 'n')						// Teleport to next time segment on same date
			ml.world.viewer.moveToNextTimeSegment(true, true, true, false);

		if (key == 'b')						// Teleport to previous time segment on same date
			ml.world.viewer.moveToPreviousTimeSegment(true, true, true, false);

		if (key == 'N')						// Teleport to next time segment on any date
			ml.world.viewer.moveToNextTimeSegment(false, true, true, false);

		if (key == 'B')						// Teleport to previous time segment on any date
			ml.world.viewer.moveToPreviousTimeSegment(false, true, true, false);

		if (!input.optionKey && key == '>')
		{
			if(!ml.world.viewer.isFollowing())
			{
				ml.world.viewer.startFollowing();
			}
			else
			{
				ml.world.viewer.stopFollowing();
			}
		}

		/* Map Controls for Both Field and World Maps */
		if (key == 'z')
		{
			WMV_Cluster current = ml.world.getCurrentCluster();
			if(current != null) ml.display.map2D.zoomToCluster(ml.world, current, true);	// Zoom to current cluster
		}

		if (key == 'Z')
			ml.display.map2D.zoomToField(ml.world.getCurrentField(), true);	// Zoom to current field

		if (key == PApplet.CODED) 					
		{
			if (!input.shiftKey && keyCode == PApplet.LEFT)
				ml.display.map2D.panLeft();

			if (!input.shiftKey && keyCode == PApplet.RIGHT) 
				ml.display.map2D.panRight();

			if (keyCode == PApplet.UP) 
				ml.display.map2D.panUp();
				
			if (keyCode == PApplet.DOWN) 
				ml.display.map2D.panDown();

			if (input.shiftKey && keyCode == PApplet.LEFT)
				ml.world.viewer.rotateX(-1);

			if (input.shiftKey && keyCode == PApplet.RIGHT) 
				ml.world.viewer.rotateX(1);
		}
	}
	
	/**
	 * 
	 * @param ml
	 * @param key
	 * @param keyCode
	 */
	public void handleLibraryViewKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		if (key == 'j') 
			ml.world.viewer.moveToRandomCluster(true, false);				/* Teleport to random cluster */

		if (key == 'c')
		{
			if( ml.display.getLibraryViewMode() == 1 )
				ml.display.setDisplayItem( ml.world.viewer.getCurrentFieldID() );
			else if( ml.display.getLibraryViewMode() == 2 )
				ml.display.setDisplayItem( ml.world.viewer.getCurrentClusterID() );
		}

		if (key == 'L')
		{
			if( ml.display.getLibraryViewMode() != 0 )
				ml.display.setLibraryViewMode( 0 );
		}

		if (key == 'F')
		{
			if( ml.display.getLibraryViewMode() != 1 )
				ml.display.setLibraryViewMode( 1 );
		}

		if (key == 'C')
		{
			if( ml.display.getLibraryViewMode() != 2 )
				ml.display.setLibraryViewMode( 2 );
		}

		if (key == PApplet.ENTER)
		{
			if( ml.display.currentDisplayCluster >= 0 && ml.display.currentDisplayCluster < ml.world.getCurrentFieldClusters().size() )
			{
				ml.world.viewer.teleportToCluster(ml.display.currentDisplayCluster, true, -1); 
				ml.display.setDisplayView(ml.world, 0);
			}
		}

		if (key == '-' || key == '_') 
		{
			int newLibraryViewMode = ml.display.getLibraryViewMode()+1;
			ml.display.setLibraryViewMode(newLibraryViewMode);
		}

		if (key == '=' || key == '+') 
		{
			int newLibraryViewMode = ml.display.getLibraryViewMode()-1;
			ml.display.setLibraryViewMode(newLibraryViewMode);
		}

		if (key == PApplet.CODED) 					
		{
			if(ml.display.getLibraryViewMode() == 0)					// Library World View
			{

			}
			if(ml.display.getLibraryViewMode() == 1)					// Library Field View
			{
				if (keyCode == PApplet.LEFT) 
					ml.display.showPreviousItem();

				if (keyCode == PApplet.RIGHT) 
					ml.display.showNextItem();

			}
			else if(ml.display.getLibraryViewMode() == 2)			// Library Cluster View
			{
				if (keyCode == PApplet.LEFT) 
					ml.display.showPreviousItem();

				if (keyCode == PApplet.RIGHT) 
					ml.display.showNextItem();
			}
		}
	}
	
	/**
	 * 
	 * @param ml
	 * @param key
	 * @param keyCode
	 */
	public void handleTimeViewKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		/* Navigation */
		if (key == 'j') 
			ml.world.viewer.moveToRandomCluster(true, false);				// Jump to random cluster

		if (key == 'i') 		// Go to nearest cluster ID with image
			ml.world.viewer.moveToNearestClusterWithType(0, false, true, false);

		if (key == 'p') 		// Go to nearest cluster ID with panorama
			ml.world.viewer.moveToNearestClusterWithType(1, false, true, false);

		if (key == 'v') 		// Go to nearest cluster ID with video
			ml.world.viewer.moveToNearestClusterWithType(2, false, true, false);

		if (key == 'u') 		// Go to nearest cluster ID with sound
			ml.world.viewer.moveToNearestClusterWithType(3, false, true, false);

		if (key == 'm') 
			ml.world.viewer.moveToNearestCluster(false);

		if (key == '{')
			ml.world.viewer.teleportToFieldOffset(-1, false);

		if (key == '}') 
			ml.world.viewer.teleportToFieldOffset(1, false);

		if( key == 'l' )
			ml.world.viewer.moveToLastCluster(false);

		if (key == 'n')						// Teleport to next time segment on same date
			ml.world.viewer.moveToNextTimeSegment(true, true, true, false);

		if (key == 'b')						// Teleport to previous time segment on same date
			ml.world.viewer.moveToPreviousTimeSegment(true, true, true, false);

		if (key == 'N')						// Teleport to next time segment on any date
			ml.world.viewer.moveToNextTimeSegment(false, true, true, false);

		if (key == 'B')						// Teleport to previous time segment on any date
			ml.world.viewer.moveToPreviousTimeSegment(false, true, true, false);

		if (!input.optionKey && key == '>')
		{
			if(!ml.world.viewer.isFollowing())
			{
				ml.world.viewer.startFollowing();
			}
			else
			{
				ml.world.viewer.stopFollowing();
			}
		}

		/* Timeline */
		if (key == 'r')									// Zoom out to whole timeline
			ml.display.resetZoom(ml.world, true);

		if (key == 'z')									// Zoom to fit timeline
			ml.display.zoomToTimeline(ml.world, true);

		if (key == 'c')									// Zoom to fit current time segment
			ml.display.zoomToCurrentSelectableTimeSegment(ml.world, true);

		if (key == 'd')									// Zoom to fit current time segment
			ml.display.zoomToCurrentSelectableDate(ml.world, true);

		if (key == 'a')									// Timeline zoom to fit
			ml.display.showAllDates();

		if (key == PApplet.ENTER)
		{
			if(ml.display.getCurrentSelectableTimeSegment() >= 0)
			{
				ml.world.viewer.teleportToCluster(ml.display.getSelectedCluster(), true, -1); 
				ml.display.setDisplayView(ml.world, 0);
			}
		}
		if (key == PApplet.CODED) 					
		{
			if (keyCode == PApplet.UP) 					// Timeline zoom in 
				ml.display.zoom(ml.world, -1, true);
			
			if (keyCode == PApplet.DOWN) 				// Timeline zoom out
				ml.display.zoom(ml.world, 1, true);

			if (keyCode == PApplet.LEFT)  				// Start timeline scrolling left
				ml.display.scroll(ml.world, -1);
			
			if (keyCode == PApplet.RIGHT)  				// Start timeline scrolling right
				ml.display.scroll(ml.world, 1);
		}
	}
	
	/**
	 * Handle key pressed in Media View
	 * @param ml Parent app
	 * @param key Key pressed
	 * @param keyCode Key code
	 */
	public void handleMediaViewKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		if(key == PApplet.ENTER)
			ml.world.viewer.exitMediaView();
	}
	
	/**
	 * Handle key pressed in List Item Window
	 * @param ml Parent app
	 * @param key Key pressed
	 * @param keyCode Key code
	 */
	public void handleLibraryWindowKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		if(key == 'o' || key == 'O')
		{
			ml.state.inLibrarySetup = true;
			if(ml.createNewLibrary) ml.createNewLibrary = false;
			ml.display.window.btnCreateLibrary.setVisible(false);
			ml.display.window.btnOpenLibrary.setVisible(false);
			ml.display.window.chkbxRebuildLibrary.setVisible(false);
//			ml.display.window.btnLibraryHelp.setVisible(false);
			ml.display.window.lblStartup.setVisible(false);
		}

		if(key == 'c' || key == 'C')
		{
			ml.state.inLibrarySetup = true;
			ml.createNewLibrary = true;
			ml.state.chooseMediaFolders = true;
			ml.display.window.closeLibraryWindow();
		}

		if(key == 'r')
		{
			ml.display.window.chkbxRebuildLibrary.setSelected(!ml.display.window.chkbxRebuildLibrary.isSelected());
		}
		
		if(key == 'Q')
			ml.exitProgram();
	}
	
	/**
	 * Handle key pressed in List Item Window
	 * @param ml Parent app
	 * @param key Key pressed
	 * @param keyCode Key code
	 */
	public void handleListItemWindowKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		if(key == PApplet.ENTER)
		{
			WMV_Field f;
			switch(ml.display.window.listItemWindowResultCode)
			{
				case 0:						// 0: Field
					f = ml.world.getField(ml.display.window.listItemWindowSelectedItem);
					boolean loaded = f.getDataFolderLoaded();
					if(!loaded)
					{
						if(ml.debug.ml) ml.systemMessage("Keyboard.handleListItemWindowKeyPressed()... No state loaded, will enter field at beginning...");
						ml.world.enterFieldAtBeginning( ml.display.window.listItemWindowSelectedItem );			/* Enter first field */
					}
					else
					{
						if(ml.debug.ml) ml.systemMessage("Keyboard.handleListItemWindowKeyPressed()... State loaded, will enter field at saved location? "+f.hasBeenVisited());
						if( f.hasBeenVisited() )	
							ml.world.viewer.enterField( ml.display.window.listItemWindowSelectedItem, true );		/* Enter field at saved location */
						else
							ml.world.enterFieldAtBeginning( ml.display.window.listItemWindowSelectedItem );		/* Enter field at saved location */
//						ml.world.viewer.enterField( ml.display.window.listItemWindowSelectedItem, f.hasBeenVisited() );		/* Enter field at saved location */
					}
					break;
				case 1:						// 1: GPS Track
					ml.world.viewer.selectGPSTrackID( ml.display.window.listItemWindowSelectedItem );
					break;
			}
			
			ml.display.window.closeChooseItemDialog();
		}
		
		if (key == PApplet.CODED) 
		{
			if (keyCode == PApplet.DOWN) 
			{
				ml.display.window.listItemWindowSelectedItem++;
				switch(ml.display.window.listItemWindowResultCode)
				{
				case 0:							// 0: Field
					if(ml.display.window.listItemWindowSelectedItem >= ml.world.getFieldCount())
						ml.display.window.listItemWindowSelectedItem = 0;
					break;
				case 1:							// 1: GPS Track
					if(ml.display.window.listItemWindowSelectedItem >= ml.world.getCurrentField().getGPSTracks().size())
						ml.display.window.listItemWindowSelectedItem = 0;
					break;
				}
			}
			if (keyCode == PApplet.UP) 
			{
				ml.display.window.listItemWindowSelectedItem--;
				switch(ml.display.window.listItemWindowResultCode)
				{
				case 0:							// 0: Field
					if(ml.display.window.listItemWindowSelectedItem < 0)
						ml.display.window.listItemWindowSelectedItem = ml.world.getFieldCount() - 1;
					break;
				case 1:							// 1: GPS Track
					if(ml.display.window.listItemWindowSelectedItem < 0)
						ml.display.window.listItemWindowSelectedItem = ml.world.getCurrentField().getGPSTracks().size() - 1;
					break;
				}
			}
		}
	}
	
	/**
	 * Handle key pressed in Interactive Clustering Mode -- Disabled
	 * @param ml Parent app
	 * @param key Key pressed
	 * @param keyCode Key code
	 */
	public void handleInteractiveClusteringKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		if (!input.optionKey && key == 'h')
		{
			if(!ml.world.getState().hierarchical)
			{
				ml.world.getState().hierarchical = true;
				if(!ml.world.getCurrentField().getState().dendrogramCreated)
					ml.world.getCurrentField().runHierarchicalClustering();
				ml.world.getCurrentField().setDendrogramDepth(ml.world.getCurrentField().getState().clusterDepth);				// Initialize clusters 
				ml.world.getCurrentField().createTimeline();					// Create field timeline
				// FINISH TIMELINE HERE
			}

		}

		if (!input.optionKey && key == 'k')
		{
			if(ml.world.getState().hierarchical)
			{
				ml.world.getState().hierarchical = false;
				WMV_Field f = ml.world.getCurrentField();
				f.runKMeansClustering(ml.world.settings.kMeansClusteringEpsilon, f.getModel().getState().clusterRefinement, f.getModel().getState().clusterPopulationFactor);
				ml.world.getCurrentField().createTimeline();					// Create field timeline
				// FINISH TIMELINE HERE
			}
		}

		if (key == '[') 	
		{
			if(ml.world.settings.minClusterDistance > 0.25f)
			{
				ml.world.settings.minClusterDistance -= 0.25f;
				for(WMV_Field f : ml.world.getFields())
				{
					f.getModel().setMinClusterDistance(ml.world.settings.minClusterDistance);	
					ml.world.getCurrentField().runKMeansClustering( ml.world.settings.kMeansClusteringEpsilon, ml.world.getCurrentField().getModel().getState().clusterRefinement, ml.world.getCurrentField().getModel().getState().clusterPopulationFactor );
					ml.world.getCurrentField().mergeAdjacentClusters();							/* Mark clusters with no media as empty */
					ml.display.map2D.initialize(ml.world);
				}
			}
		}

		if (key == ']') 	
		{
			if(ml.world.settings.minClusterDistance < ml.world.settings.maxClusterDistance - 2.f)
			{
				ml.world.settings.minClusterDistance += 0.25f;
				for(WMV_Field f : ml.world.getFields())
				{
					f.getModel().setMinClusterDistance(ml.world.settings.minClusterDistance);
					ml.world.getCurrentField().runKMeansClustering( ml.world.settings.kMeansClusteringEpsilon, ml.world.getCurrentField().getModel().getState().clusterRefinement, ml.world.getCurrentField().getModel().getState().clusterPopulationFactor );
					ml.world.getCurrentField().mergeAdjacentClusters();							/* Mark clusters with no media as empty */
					ml.world.getCurrentField().finishClusterSetup();			
					ml.display.map2D.initialize(ml.world);
				}
			}
		}	
		
		if (key == ' ') 
			if(ml.state.interactive)
				ml.finishInteractiveClustering();			// Restart simulation after interactive clustering

		/* Arrow and Shift Keys */
		if (key == PApplet.CODED) 					
		{
			if(ml.world.getState().hierarchical)
			{
				if (keyCode == PApplet.UP) 
				{
					int clusterDepth = ml.world.getCurrentField().getState().clusterDepth + 1;
					if(clusterDepth <= ml.world.getCurrentField().getState().deepestLevel)
						ml.world.getCurrentField().setDendrogramDepth( clusterDepth );
				}

				if (keyCode == PApplet.DOWN) 
				{
					int clusterDepth = ml.world.getCurrentField().getState().clusterDepth - 1;
					if(clusterDepth >= ml.world.getCurrentField().getState().minClusterDepth)
						ml.world.getCurrentField().setDendrogramDepth( clusterDepth );
				}
			}
			else
			{
				if (keyCode == PApplet.LEFT) 		
				{
					ml.world.getCurrentField().getModel().state.clusterRefinement -= 10;
					float populationFactor = ml.world.getCurrentField().getModel().state.clusterPopulationFactor;

					if(ml.world.getCurrentField().getModel().state.clusterRefinement >= ml.world.getCurrentField().getModel().state.minClusterRefinement)
					{
						ml.world.getCurrentField().runKMeansClustering( ml.world.settings.kMeansClusteringEpsilon, ml.world.getCurrentField().getModel().state.clusterRefinement, populationFactor );
						ml.world.getCurrentField().mergeAdjacentClusters();						
						ml.world.getCurrentField().finishClusterSetup();			
						ml.display.map2D.initialize(ml.world);
					}
					else ml.world.getCurrentField().getModel().state.clusterRefinement += 10;
				}

				if (keyCode == PApplet.RIGHT) 	
				{
					ml.world.getCurrentField().getModel().state.clusterRefinement += 10;
					float populationFactor = ml.world.getCurrentField().getModel().state.clusterPopulationFactor;

					if(ml.world.getCurrentField().getModel().state.clusterRefinement <= ml.world.getCurrentField().getModel().state.maxClusterRefinement)
					{
						ml.world.getCurrentField().runKMeansClustering( ml.world.settings.kMeansClusteringEpsilon, ml.world.getCurrentField().getModel().state.clusterRefinement, populationFactor );
						ml.world.getCurrentField().mergeAdjacentClusters();						
						ml.world.getCurrentField().finishClusterSetup();			
						ml.display.map2D.initialize(ml.world);
					}
					else ml.world.getCurrentField().getModel().state.clusterRefinement -= 10;
				}

				if (keyCode == PApplet.DOWN) 		
				{
					int refinementAmount = ml.world.getCurrentField().getModel().state.clusterRefinement;
					ml.world.getCurrentField().getModel().state.clusterPopulationFactor -= 1.f;

					if(ml.world.getCurrentField().getModel().state.clusterPopulationFactor >= ml.world.getCurrentField().getModel().state.minPopulationFactor)
					{
						ml.world.getCurrentField().runKMeansClustering( ml.world.settings.kMeansClusteringEpsilon, refinementAmount, ml.world.getCurrentField().getModel().state.clusterPopulationFactor );
						ml.world.getCurrentField().mergeAdjacentClusters();						
						ml.world.getCurrentField().finishClusterSetup();			
						ml.display.map2D.initialize(ml.world);
					}
					else ml.world.getCurrentField().getModel().state.clusterPopulationFactor += 1.f;
				}

				if (keyCode == PApplet.UP) 	
				{
					int refinementAmount = ml.world.getCurrentField().getModel().state.clusterRefinement;
					ml.world.getCurrentField().getModel().state.clusterPopulationFactor += 1.f;

					if(ml.world.getCurrentField().getModel().state.clusterPopulationFactor <= ml.world.getCurrentField().getModel().state.maxPopulationFactor)
					{
						ml.world.getCurrentField().runKMeansClustering( ml.world.settings.kMeansClusteringEpsilon, refinementAmount, ml.world.getCurrentField().getModel().state.clusterPopulationFactor );
						ml.world.getCurrentField().mergeAdjacentClusters();						
						ml.world.getCurrentField().finishClusterSetup();			
						ml.display.map2D.initialize(ml.world);
					}
					else ml.world.getCurrentField().getModel().state.clusterPopulationFactor -= 1.f;
				}
			}
		}
	}
	
	public void handleKeyReleased(MultimediaLocator ml, ML_Display display, char key, int keyCode)
	{
//		if (key == ' ') 
//		{
//			if(ml.display.window.showMainMenu)
//				ml.display.window.hideMainMenu();
//			else
//				ml.display.window.openMainMenu();
//		}
//
//		if (key == '!') 
//		{
//			if(!ml.display.window.showNavigationWindow)
//				ml.display.window.openNavigationWindow();
//			else
//				ml.display.window.hideNavigationWindow();
//		}
//
//		if (key == '@')
//		{
//			if(!ml.display.window.showMediaWindow)
//				ml.display.window.openMediaWindow();
//			else
//				ml.display.window.hideMediaWindow();
//		}
//
//		if (key == '#') 
//		{
//			if(!ml.display.window.showTimeWindow)
//				ml.display.window.openTimeWindow();
//			else
//				ml.display.window.hideTimeWindow();
//		}
//
//		if (key == '$') 
//		{
//			if(!ml.display.window.showLibraryViewWindow)
//				ml.display.window.openLibraryViewWindow();
//			else
//				ml.display.window.hideLibraryViewWindow();
//		}
//
//		if (key == '^') 
//		{
//			if(!ml.display.window.showHelpWindow)
//				ml.display.window.openHelpWindow();
//			else
//				ml.display.window.hideHelpWindow();
//		}

		if(display.getDisplayView() == 0)				/* World View Controls */
		{
			if (key == 'a') 
				ml.world.viewer.stopMoveXTransition();
			if (key == 'd') 
				ml.world.viewer.stopMoveXTransition();
			if (key == 's')
				ml.world.viewer.stopMoveZTransition();
			if (key == 'w') 
				ml.world.viewer.stopMoveZTransition();
			if (key == 'e') 
				ml.world.viewer.stopMoveYTransition();
			if (key == 'c') 
				ml.world.viewer.stopMoveYTransition();
			if (key == 'q')
				ml.world.viewer.stopZooming();
			if (key == 'z')
				ml.world.viewer.stopZooming();
		}
		else if( display.getDisplayView() == 1 )		/* Map View Controls */
		{
			if (key == 'A') 
				ml.world.viewer.stopMoveXTransition();
			if (key == 'D') 
				ml.world.viewer.stopMoveXTransition();
			if (key == 'S')
				ml.world.viewer.stopMoveZTransition();
			if (key == 'W') 
				ml.world.viewer.stopMoveZTransition();

			if (key == 'w') 
				display.map2D.stopZooming();
			if (key == 's') 
				display.map2D.stopZooming();
		}
		
		/* Coded Keys */
		if (key == PApplet.CODED) 
		{
			if(display.getDisplayView() == 0)
			{
				if (keyCode == PApplet.LEFT) 
					ml.world.viewer.stopRotateXTransition();
				if (keyCode == PApplet.RIGHT) 
					ml.world.viewer.stopRotateXTransition();
				if (keyCode == PApplet.UP) 
					ml.world.viewer.stopRotateYTransition();
				if (keyCode == PApplet.DOWN) 
					ml.world.viewer.stopRotateYTransition();
			}
			else if( display.getDisplayView() == 1 )
			{
				if(display.map2D.isPanning())
				{
					if (keyCode == PApplet.LEFT) 
						display.map2D.stopPanning();
					if (keyCode == PApplet.RIGHT) 
						display.map2D.stopPanning();
					if (keyCode == PApplet.UP) 
						display.map2D.stopPanning();
					if (keyCode == PApplet.DOWN) 
						display.map2D.stopPanning();
				}

				if (keyCode == PApplet.LEFT) 
					ml.world.viewer.stopRotateXTransition();
				if (keyCode == PApplet.RIGHT) 
					ml.world.viewer.stopRotateXTransition();
			}
			else if(display.getDisplayView() == 2)
			{
				if (keyCode == PApplet.UP)  				// Timeline scroll left
					display.stopZooming();
				if (keyCode == PApplet.DOWN)  				// Timeline scroll right
					display.stopZooming();
				if (keyCode == PApplet.LEFT)  				// Timeline scroll left
					display.stopScrolling();
				if (keyCode == PApplet.RIGHT)  				// Timeline scroll right
					display.stopScrolling();
			}
			
			if (keyCode == PApplet.SHIFT) 
				input.shiftKey = false;
			if (keyCode == PApplet.ALT) 
				input.optionKey = false;
		}
	}
}