package multimediaLocator;

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
		if (key == 'R')
			ml.restart();

		if (keyCode == PApplet.SHIFT) {
			input.shiftKey = true;
		}

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

//		if (key == '4')									// Library View -- Disabled 
//			ml.display.setDisplayView(ml.world, 3);

		if (key == 'H')
		{
			ml.world.settings.screenMessagesOn = !ml.world.settings.screenMessagesOn;
			if(ml.display.window.setupMLWindow)
				ml.display.window.chkbxScreenMessagesOn.setSelected(ml.world.settings.screenMessagesOn);
//			ml.world.settings.showStitchedPanoramas = !ml.world.settings.showStitchedPanoramas;
		}

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

//		if (key == '0')
//			ml.state.sphericalView = !ml.state.sphericalView;
		
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
		
		/* Graphics */
		if (input.optionKey && key == '[')
		{
			if(ml.world.viewer.getThinningAngle() > PApplet.PI / 64.f)
				ml.world.viewer.setThinningAngle( ml.world.viewer.getThinningAngle() - PApplet.PI / 128.f );
			ml.world.getCurrentField().findClusterMediaDirections();
		}

		if (input.optionKey && key == ']')
		{
			if(ml.world.viewer.getThinningAngle() < ml.world.viewer.getVisibleAngle() - PApplet.PI / 128.f)
				ml.world.viewer.setThinningAngle(ml.world.viewer.getThinningAngle() + PApplet.PI / 128.f);
			ml.world.getCurrentField().findClusterMediaDirections();
		}

//		if (input.optionKey && key == '\\')
//			ml.world.getCurrentField().stitchAllClusters(ml.stitcher, ml.library.getLibraryFolder());		// Teleport to cluster with > 1 times

		if (!input.optionKey && key == 'a') 
			ml.world.viewer.startMoveXTransition(-1);

		if (!input.optionKey && key == 'd') 
			ml.world.viewer.startMoveXTransition(1);

		if( key == 'l' )
			ml.world.viewer.moveToLastCluster(ml.world.viewer.getMovementTeleport());

		if( key == 'L' )
			ml.world.viewer.lookAtNearestMedia();

		if( key == '/' )
			ml.world.saveCurrentSimulationState();

		if( key == 't' && !input.optionKey )
		{
			boolean state = !ml.world.viewer.getMovementTeleport();
			ml.world.viewer.setMovementTeleport( state );
//			if(ml.display.window.setupNavigationWindow)
//				ml.display.window.chkbxMovementTeleport.setSelected(state);
		}

		if( key == 't' && input.optionKey )
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

		if (key == 'T') 
		{
			boolean state = !ml.world.getState().timeFading;
			ml.world.setTimeFading(state);
			if(ml.display.window.setupNavigationWindow)
				ml.display.window.chkbxTimeFading.setSelected(state);
		}

		if (!input.optionKey && key == 's') 
			ml.world.viewer.walkBackward();

		if (!input.optionKey && key == 'w') 
			ml.world.viewer.walkForward();

		if (key == '9')														// -- Disabled
			ml.world.viewer.setOrientationMode( !ml.world.viewer.getSettings().orientationMode );

		if (key == 'Q')
			ml.exitProgram();

		if (!input.optionKey && key == 'e')									// Move UP
			ml.world.viewer.walkUp();

		if (key == 'c') 													// Move DOWN
			ml.world.viewer.walkDown();

		if (key == 'C') 													// Choose field from list
			ml.world.viewer.chooseFieldDialog();

		if (key == '-') 								
			ml.world.state.paused = !ml.world.getState().paused;

		if (key == 'W') 
			ml.world.viewer.moveToNearestClusterAhead(false);

		if (key == 'M') 
		{
			boolean state = !ml.world.getState().showMetadata;
			ml.world.setShowMetadata(state);
			if(ml.display.window.setupMediaWindow)
				ml.display.window.chkbxShowMetadata.setSelected(state);
		}

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

		if (key == 'n')						// Teleport to next time segment on same date
		{
			if(ml.display.getDisplayView() == 0)
				ml.world.viewer.moveToNextTimeSegment(true, true, ml.world.viewer.getMovementTeleport(), true);
			else
				ml.world.viewer.moveToNextTimeSegment(true, true, true, false);
		}

		if (key == 'b')						// Teleport to previous time segment on same date
		{
			if(ml.display.getDisplayView() == 0)
				ml.world.viewer.moveToPreviousTimeSegment(true, true, ml.world.viewer.getMovementTeleport(), true);
			else
				ml.world.viewer.moveToPreviousTimeSegment(true, true, true, false);
		}

		if (key == 'N')						// Teleport to next time segment on any date
		{
			if(ml.display.getDisplayView() == 0)
				ml.world.viewer.moveToNextTimeSegment(false, true, ml.world.viewer.getMovementTeleport(), true);
			else
				ml.world.viewer.moveToNextTimeSegment(false, true, true, false);
		}

		if (key == 'B')						// Teleport to previous time segment on any date
		{
			if(ml.display.getDisplayView() == 0)
				ml.world.viewer.moveToPreviousTimeSegment(false, true, ml.world.viewer.getMovementTeleport(), true);
			else
				ml.world.viewer.moveToPreviousTimeSegment(false, true, true, false);
		}

		if (key == '~')
		{
			if(!ml.world.viewer.isFollowing())
			{
				int followMode = ml.world.viewer.getPathNavigationMode();
				if(followMode >= 2) followMode = 0;
				else followMode++;

				ml.world.viewer.setPathNavigationMode(followMode);

				if(ml.display.window.setupNavigationWindow)
				{

					switch(ml.world.viewer.getPathNavigationMode())		// 0: Timeline 1: GPS Track 2: Memory
					{
						case 0:					// Timeline
							ml.display.window.optTimeline.setSelected(true);
							ml.display.window.optGPSTrack.setSelected(false);
							ml.display.window.optMemory.setSelected(false);
							break;
						case 1:					// GPS Track
							ml.display.window.optTimeline.setSelected(false);
							ml.display.window.optGPSTrack.setSelected(true);
							ml.display.window.optMemory.setSelected(false);
							break;
						case 2:					// Memory
							ml.display.window.optTimeline.setSelected(false);
							ml.display.window.optGPSTrack.setSelected(false);
							ml.display.window.optMemory.setSelected(true);
							break;
					}
				}
			}
		}
		
		if (input.optionKey && key == 'g')
		{
			if(!ml.world.viewer.isFollowing())
			{
				ml.world.viewer.startFollowingGPSTrack();
				if(ml.display.window.setupNavigationWindow)
				{
					ml.display.window.optTimeline.setSelected(false);
					ml.display.window.optGPSTrack.setSelected(true);
					ml.display.window.optMemory.setSelected(false);
				}
			}
		}

		if (!input.optionKey && key == '>')
		{
			if(!ml.world.viewer.isFollowing())
			{
				switch(ml.world.viewer.getPathNavigationMode())
				{
					case 0:
						ml.world.viewer.followTimeline(true, false);
						break;
					case 1:
						ml.world.viewer.startFollowingGPSTrack();
						break;
					case 2:
						ml.world.viewer.followMemory();
						break;
				}

				if(ml.display.window.setupNavigationWindow)
					ml.display.window.chkbxPathFollowing.setSelected(true);
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
		
		if (key == 'i') 		// Go to nearest cluster ID with image
			ml.world.viewer.moveToNearestClusterWithType(ml.world.viewer.getMovementTeleport(), 0, false);

		if (key == 'p') 		// Go to nearest cluster ID with panorama
			ml.world.viewer.moveToNearestClusterWithType(ml.world.viewer.getMovementTeleport(), 1, false);

		if (key == 'v') 		// Go to nearest cluster ID with video
			ml.world.viewer.moveToNearestClusterWithType(ml.world.viewer.getMovementTeleport(), 2, false);

		if (key == 'u') 		// Go to nearest cluster ID with sound
			ml.world.viewer.moveToNearestClusterWithType(ml.world.viewer.getMovementTeleport(), 3, false);

		if (key == 'm') 
			ml.world.viewer.moveToNearestCluster(ml.world.viewer.getMovementTeleport());

		if (key == '_') 
			ml.world.getCurrentField().fadeObjectDistances(0.85f);

		if (key == '+')
			ml.world.getCurrentField().fadeObjectDistances(1.176f);

		if (key == 'Z')
			ml.world.setTimeMode(0);
		
		if (key == 'Ω')		// OPT + z
			ml.world.setTimeMode(1);
	}
	
	/**
	 * Handle key pressed in World View
	 * @param ml
	 * @param key
	 * @param keyCode
	 */
	public void handleWorldViewKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		if (key == 'j') 
			ml.world.viewer.moveToRandomCluster(ml.world.viewer.getMovementTeleport(), true);				// Jump (teleport) to random cluster

		if (key == '|')
			ml.world.getCurrentCluster().stitchImages(ml.stitcher, ml.library.getLibraryFolder(), ml.world.getCurrentField().getSelectedImages());    			

		if (key == '{')
			ml.world.viewer.teleportToFieldOffset(-1, true, true);

		if (key == '}') 
			ml.world.viewer.teleportToFieldOffset(1, true, true);

		if (key == 'q') 
			ml.world.viewer.zoomIn();

		if (key == 'z') 
			ml.world.viewer.zoomOut();

		if (key == '.')
			ml.world.viewer.stop(true);
		
		if (key == PApplet.ENTER)
		{
			if(ml.display.getDisplayView() == 0)
				ml.world.viewer.startViewingSelectedMedia();
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

		if (input.optionKey && key == 'p')
		{
			boolean state = !ml.world.getState().alphaMode;
			ml.world.state.alphaMode = state;
			if(ml.display.window.setupMediaWindow)
				ml.display.window.chkbxAlphaMode.setSelected(state);
		}

		if (key == ')') {		
			float newAlpha = PApplet.constrain(ml.world.getState().alpha+15.f, 0.f, 255.f);
			ml.world.fadeAlpha(newAlpha);
		}

		if (key == '(') {
			float newAlpha = PApplet.constrain(ml.world.getState().alpha-15.f, 0.f, 255.f);
			ml.world.fadeAlpha(newAlpha);
		}

//		if (key == ':')
//			ml.world.settings.showUserPanoramas = !ml.world.settings.showUserPanoramas;

		if (key == 'A') 
			ml.world.viewer.setSelection( !ml.world.viewer.inSelectionMode() );

		if (input.optionKey && key == 'x')
			ml.world.getCurrentField().deselectAllMedia(false);

		if (input.optionKey && key == '-')
		{
			ml.world.viewer.setVisibleAngle( ml.world.viewer.getVisibleAngle() - 3.1415f / 128.f ); 
		}

		if (input.optionKey && key == '=')
		{
			ml.world.viewer.setVisibleAngle( ml.world.viewer.getVisibleAngle() + 3.1415f / 128.f ); 
		}

		/* Selection */
		if (!input.optionKey && key == 'x') 
			ml.world.viewer.chooseMediaInFront(true);

		if (!input.optionKey && key == 'X')
			ml.world.viewer.chooseMediaInFront(false);

//		if (key == '≈') 		
		if (!input.optionKey && key == 'k') 		
			ml.world.viewer.choosePanoramaNearby(true);

//		if (key == '˛') 		
		if (key == 'K') 		
			ml.world.viewer.choosePanoramaNearby(false);

		if (key == 'S')						// Save all fields
		{
			if(ml.world.getFields().size() > 1)
				ml.world.saveAllSimulationStates();
			else
				ml.world.saveCurrentSimulationState();
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
	}
	
	/**
	 * Handle key pressed in Map View
	 * @param ml Parent app
	 * @param key Key pressed
	 * @param keyCode Key code
	 */
	public void handleMapViewKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		if(key == 'L')
		{
			ml.display.setMapViewMode(0);
			if(ml.display.window.setupMapWindow) 
			{
				ml.display.window.optMapViewWorldMode.setEnabled(true);
				ml.display.window.optMapViewFieldMode.setEnabled(true);
				ml.display.window.optMapViewWorldMode.setSelected(true);	
				ml.display.window.optMapViewFieldMode.setSelected(false);	
				ml.display.window.optMapViewWorldMode.setEnabled(ml.display.getDisplayView() == 1);
				ml.display.window.optMapViewFieldMode.setEnabled(ml.display.getDisplayView() == 1);
			}
		}
	
		if(key == 'F')
		{
			ml.display.setMapViewMode(1);
			if(ml.display.window.setupMapWindow) 
			{
				ml.display.window.optMapViewWorldMode.setEnabled(true);
				ml.display.window.optMapViewFieldMode.setEnabled(true);
				ml.display.window.optMapViewWorldMode.setSelected(false);	
				ml.display.window.optMapViewFieldMode.setSelected(true);	
				ml.display.window.optMapViewWorldMode.setEnabled(ml.display.getDisplayView() == 1);
				ml.display.window.optMapViewFieldMode.setEnabled(ml.display.getDisplayView() == 1);
			}
		}

		if(ml.display.mapViewMode == 0)		// World Mode Commands
		{
			if(key == PApplet.ENTER)
			{
				int selectedField = ml.display.map2D.getSelectedFieldID();
				if(input.shiftKey)
				{
					ml.world.viewer.teleportToField(selectedField, true, false);
				}
				else
				{
					ml.world.viewer.teleportToField(selectedField, true, true);
					ml.display.setDisplayView( ml.world, 0 );
				}
			}
			
//			if (input.shiftKey && key == 'c')
//				ml.startInitialClustering();				// Re-run clustering on all fields
		}
		else									// Field Mode Commands
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
		
		// Commands in both World and Field Modes
		if (key == 'A') 
			ml.display.map2D.panLeft();
		if (key == 'D') 
			ml.display.map2D.panRight();
		if (key == 'S') 
			ml.display.map2D.panDown();
		if (key == 'W') 
			ml.display.map2D.panUp();

		if (key == 'j') 
			ml.world.viewer.moveToRandomCluster(true, false);				// Teleport to random cluster
		
		if (key == '{')
			ml.world.viewer.teleportToFieldOffset(-1, true, false);

		if (key == '}') 
			ml.world.viewer.teleportToFieldOffset(1, true, false);

		if (key == 'z')
		{
			WMV_Cluster current = ml.world.getCurrentCluster();
			if(current != null) ml.display.map2D.zoomToCluster(ml.world, current, true);	// Zoom to current cluster
			else System.out.println("z Cannot move to cluster: ml.world.getCurrentCluster() is NULL!");
		}

		if (key == 'Z')
			ml.display.map2D.zoomToField(ml.world, ml.world.getCurrentField(), true);	// Zoom to current field

		if (key == PApplet.CODED) 					
		{
			if (keyCode == PApplet.LEFT)
				ml.world.viewer.rotateX(-1);

			if (keyCode == PApplet.RIGHT) 
				ml.world.viewer.rotateX(1);

			if (keyCode == PApplet.UP) 
				ml.display.map2D.zoomOut(ml.world);
				
			if (keyCode == PApplet.DOWN) 
				ml.display.map2D.zoomIn(ml.world);
		}
	}
	
	public void handleLibraryViewKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		if (key == 'j') 
			ml.world.viewer.moveToRandomCluster(true, false);				/* Teleport to random cluster */

		if (key == 'c')
			ml.display.currentDisplayCluster = ml.world.viewer.getState().getCurrentClusterID();

		if (key == '[') 
		{
			ml.display.libraryViewMode--;
			if(ml.display.libraryViewMode < 0)
				ml.display.libraryViewMode = ml.world.getCurrentFieldClusters().size() - 1;
		}

		if (key == ']') 
		{
			ml.display.libraryViewMode++;
			if( ml.display.libraryViewMode >= ml.world.getCurrentFieldClusters().size())
				ml.display.libraryViewMode = 0;
		}

		if (key == PApplet.CODED) 					
		{
//			if(ml.display.libraryViewMode == 0)					// Library World View
//			{
//			
//			}
			if(ml.display.libraryViewMode == 1)					// Library Field View
			{
				if (keyCode == PApplet.LEFT)
					ml.world.viewer.rotateX(-1);

				if (keyCode == PApplet.RIGHT) 
					ml.world.viewer.rotateX(1);
				
				if (keyCode == PApplet.UP) 
					ml.display.map2D.zoomOut(ml.world);
					
				if (keyCode == PApplet.DOWN) 
					ml.display.map2D.zoomIn(ml.world);
			}
			else if(ml.display.libraryViewMode == 2)			// Library Cluster View
			{
				if (keyCode == PApplet.DOWN) 
				{
					ml.display.currentDisplayCluster--;
					if(ml.display.currentDisplayCluster < 0)
						ml.display.currentDisplayCluster = ml.world.getCurrentFieldClusters().size() - 1;

					int count = 0;
					while(ml.world.getCurrentField().getCluster(ml.display.currentDisplayCluster).isEmpty())
					{
						ml.display.currentDisplayCluster--;
						count++;
						if(ml.display.currentDisplayCluster < 0)
							ml.display.currentDisplayCluster = ml.world.getCurrentFieldClusters().size() - 1;

						if(count > ml.world.getCurrentFieldClusters().size())
							break;
					}
				}

				if (keyCode == PApplet.UP) 
				{
					ml.display.currentDisplayCluster++;
					if( ml.display.currentDisplayCluster >= ml.world.getCurrentFieldClusters().size())
						ml.display.currentDisplayCluster = 0;

					int count = 0;
					while(ml.world.getCurrentField().getCluster(ml.display.currentDisplayCluster).isEmpty())
					{
						ml.display.currentDisplayCluster++;
						count++;
						if( ml.display.currentDisplayCluster >= ml.world.getCurrentFieldClusters().size())
							ml.display.currentDisplayCluster = 0;

						if(count > ml.world.getCurrentFieldClusters().size())
							break;
					}
				}
			}
		}
	}
	
	public void handleTimelineViewKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		if (key == 'j') 
			ml.world.viewer.moveToRandomCluster(true, false);				// Jump (teleport) to random cluster

		if (key == 'r')									// Zoom out to whole timeline
			ml.display.resetZoom(ml.world, true);

		if (key == 'z')									// Zoom to fit timeline
			ml.display.zoomToTimeline(ml.world, true);

		if (key == 't')									// Zoom to fit current time segment
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
			ml.world.viewer.stopViewingSelectedMedia();
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
			ml.display.window.btnLibraryHelp.setVisible(false);
			ml.display.window.lblStartup.setVisible(false);
		}

		if(key == 'c' || key == 'C')
		{
			ml.state.inLibrarySetup = true;
			ml.createNewLibrary = true;
			ml.state.chooseMediaFolders = true;

			ml.display.window.closeLibraryWindow();
		}
	}
	
	/**
	 * Handle key pressed in List Item Window
	 * @param ml Parent app
	 * @param key Key pressed
	 * @param keyCode Key code
	 */
	public void handleListItemWindowKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
//		System.out.println("handleListItemWindowKeyPressed:"+key);
		if(key == PApplet.ENTER)
		{
			switch(ml.display.window.listItemWindowResultCode)
			{
				case 0:						// 0: Field
					ml.world.enterFieldByIndex( ml.display.window.listItemWindowSelectedItem );								/* Enter first field */
					break;
				case 1:						// 1: GPS Track
					ml.world.viewer.selectGPSTrack( ml.display.window.listItemWindowSelectedItem );
					break;
			}
			
			ml.display.window.closeChooseItemDialog();
		}
		
		if (key == PApplet.CODED) 
		{
			if(ml.display.getDisplayView() == 0)
			{
				if (keyCode == PApplet.DOWN) 
				{
					ml.display.window.listItemWindowSelectedItem++;
					switch(ml.display.window.listItemWindowResultCode)
					{
						case 0:						// 0: Field
							if(ml.display.window.listItemWindowSelectedItem >= ml.world.getFieldCount())
								ml.display.window.listItemWindowSelectedItem = 0;
							break;
						case 1:						// 1: GPS Track
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
						case 0:						// 0: Field
							if(ml.display.window.listItemWindowSelectedItem < 0)
								ml.display.window.listItemWindowSelectedItem = ml.world.getFieldCount() - 1;
							break;
						case 1:						// 1: GPS Track
							if(ml.display.window.listItemWindowSelectedItem < 0)
								ml.display.window.listItemWindowSelectedItem = ml.world.getCurrentField().getGPSTracks().size() - 1;
							break;
					}
				}
			}
		}
	}
	
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
//				System.out.println("ml.world.minClusterDistance:"+ml.world.minClusterDistance);
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
		if (key == ' ') 
		{
			if(ml.display.window.showMainMenu)
				ml.display.window.hideMainMenu();
			else
				ml.display.window.openMLWindow();
		}

		if (key == '!') 
		{
			if(!ml.display.window.showNavigationWindow)
				ml.display.window.openNavigationWindow();
			else
				ml.display.window.closeNavigationWindow();
		}

		if (key == '@')
		{
			if(!ml.display.window.showMediaWindow)
				ml.display.window.openMediaWindow();
			else
				ml.display.window.closeMediaWindow();
		}

		if (key == '#') 
		{
			if(!ml.display.window.showStatisticsWindow)
				ml.display.window.openStatisticsWindow();
			else
				ml.display.window.closeStatisticsWindow();
		}

		if (key == '$') 
		{
			if(!ml.display.window.showMapWindow)
				ml.display.window.openMapWindow();
			else
				ml.display.window.closeMapWindow();
		}

		if (key == '%') 
		{
			if(!ml.display.window.showTimeWindow)
				ml.display.window.openTimelineWindow();
			else
				ml.display.window.closeTimeWindow();
		}
		
		if (key == '^') 
		{
			if(!ml.display.window.showHelpWindow)
				ml.display.window.openHelpWindow();
			else
				ml.display.window.closeHelpWindow();
		}

		if(display.getDisplayView() < 2)				/* World and Map View Controls */
		{
			if (key == 'a') 
				ml.world.viewer.stopMoveXTransition();
			if (key == 'd') 
				ml.world.viewer.stopMoveXTransition();
			if (key == 's')
				ml.world.viewer.stopMoveZTransition();
			if (key == 'w') 
				ml.world.viewer.stopMoveZTransition();
		}
		
		if(display.getDisplayView() == 0)			/* World View Controls */
		{
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
				display.map2D.stopPanning();
			if (key == 'D') 
				display.map2D.stopPanning();
			if (key == 'S') 
				display.map2D.stopPanning();
			if (key == 'W') 
				display.map2D.stopPanning();
			if(display.map2D.isPanning())
			{
				if (key == 'a') 
					display.map2D.stopPanning();
				if (key == 'd') 
					display.map2D.stopPanning();
				if (key == 's') 
					display.map2D.stopPanning();
				if (key == 'w') 
					display.map2D.stopPanning();
			}
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
				if (keyCode == PApplet.LEFT) 
					ml.world.viewer.stopRotateXTransition();
				if (keyCode == PApplet.RIGHT) 
					ml.world.viewer.stopRotateXTransition();
				if (keyCode == PApplet.UP) 
					display.map2D.stopZooming();
				if (keyCode == PApplet.DOWN) 
					display.map2D.stopZooming();
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
