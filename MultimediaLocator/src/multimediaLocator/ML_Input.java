package multimediaLocator;

import g4p_controls.GButton;
//import g4p_controls.GCheckbox;
import g4p_controls.GEvent;
import g4p_controls.GToggleControl;
import g4p_controls.GValueControl;
import processing.core.PApplet;

/**************************************
 * Class for responding to user input from keyboard or mouse
 * @author davidgordon
 */

public class ML_Input
{
	public boolean shiftKey = false;
	public boolean optionKey = false;
	public boolean commandKey = false;
	final public int COMMAND_KEY = 157;
	
	private boolean mouseClickedRecently = false;
	private boolean mouseReleased = false;
	private int clickedRecentlyFrame = 1000000;
	private int doubleClickSpeed = 10;

//	private int mouseClickedX = 0, mouseClickedY = 0;
//	private int mouseOffsetX = 0, mouseOffsetY = 0;

	private int screenWidth, screenHeight;

	ML_Input(int newScreenWidth, int newScreenHeight) 
	{
		screenWidth = newScreenWidth;
		screenHeight = newScreenHeight;
	}

	public void handleSliderEvent(WMV_World world, ML_Display display, GValueControl slider, GEvent event)
	{
		if(display.window.setupNavigationWindow)
		{
			if (slider.tag == "TeleportLength")
			{
				world.viewer.setTeleportLength( slider.getValueI() );
			}
			if (slider.tag == "PathWaitLength")
			{
				world.viewer.setPathWaitLength( slider.getValueI() );
			}
		}
		if(display.window.setupTimeWindow)
		{
			if (slider.tag == "MediaLength") 
			{
				world.settings.defaultMediaLength = slider.getValueI();
			}
			
			if (slider.tag == "TimeCycleLength") 
			{
				world.settings.timeCycleLength = slider.getValueI();
			}
		}
		
		if(display.window.setupGraphicsWindow)
		{
			if (slider.tag == "Alpha") 
			{
				world.getState().alpha = slider.getValueF();
			}
			
			if (slider.tag == "Brightness") 
			{
//				viewer.getSettings().userBrightness = slider.getValueF();
				world.viewer.setUserBrightness( slider.getValueF() );
			}
		}
		
		if (slider.tag == "AltitudeScaling") 
		{
			if(display.window.setupModelWindow)
			{
				world.settings.altitudeScalingFactor = PApplet.round(slider.getValueF() * 1000.f) * 0.001f;
				world.getCurrentField().calculateMediaLocations();		// Recalculate media locations
				world.getCurrentField().recalculateGeometries();		// Recalculate media geometries at new locations
				world.getCurrentField().createClusters();				// Recalculate cluster locations
			}
		}
	}

	/**
	 * Handle button input
	 * @param button GButton that was pressed
	 * @param event GEvent that occurred
	 */
	public void handleButtonEvent(WMV_World world, ML_Display display, GButton button, GEvent event) 
	{ 
		switch(button.tag) 
		{
			case "Restart":
				world.p.restart();
				break;
	
			/* Navigation */
			case "OpenNavigationWindow":
				display.window.openNavigationWindow();
				break;

			case "CloseNavigationWindow":
				display.window.hideNavigationWindow();
				break;
	
			case "NearestCluster":
				world.viewer.moveToNearestCluster(world.viewer.getMovementTeleport());
				break;
			case "RandomCluster":
				world.viewer.moveToRandomCluster(world.viewer.getMovementTeleport());
				break;
			case "LastCluster":
				world.viewer.moveToLastCluster(world.viewer.getMovementTeleport());
				break;
			case "NextField":
				if(display.displayView == 1)
					world.viewer.teleportToField(1, false);
				else
					world.viewer.teleportToField(1, true);
				break;
			case "PreviousField":
				if(display.displayView == 1)
					world.viewer.teleportToField(-1, false);
				else
					world.viewer.teleportToField(-1, true);
				break;
			case "ImportGPSTrack":
				world.viewer.importGPSTrack();						// Select a GPS tracking file from disk to load and navigate 
				break;
	
			case "FollowStart":
				if(!world.viewer.isFollowing())
				{
					switch(world.viewer.getFollowMode())
					{
					case 0:
						world.viewer.followTimeline(true, false);
						break;
					case 1:
						world.viewer.followGPSTrack();
						break;
					case 2:
						world.viewer.followMemory();
						break;
					}
				}
				break;
			case "FollowStop":
				world.viewer.stopFollowing();
				break;
				/* Model */
			case "SubjectDistanceDown":
				world.getCurrentField().fadeObjectDistances(0.85f);
				break;
	
			case "SubjectDistanceUp":
				world.getCurrentField().fadeObjectDistances(1.176f);
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

			case "ZoomIn":
				world.viewer.startZoomTransition(-1);
				break;
			case "ZoomOut":
				world.viewer.startZoomTransition(1);
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
				world.viewer.moveToNextTimeSegment(true, world.viewer.getMovementTeleport(), true);
				break;
			case "PreviousTime":
				world.viewer.moveToPreviousTimeSegment(true, world.viewer.getMovementTeleport(), true);
				break;
	
			/* Selection */
			case "OpenSelectionWindow":
				display.window.openSelectionWindow();
				world.viewer.setSelection( true );
				display.window.chkbxSelectionMode.setSelected(true);
				break;

			case "CloseSelectionWindow":
				display.window.selectionWindow.setVisible(false);
				break;

			case "SelectFront":
				world.viewer.chooseMediaInFront(true);
				break;
				
			case "DeselectFront":
				world.viewer.chooseMediaInFront(false);	
				break;
					
			case "DeselectAll":
				world.getCurrentField().deselectAllMedia(false);
				break;
			
			case "StitchPanorama":
				world.getCurrentCluster().stitchImages(world.p.stitcher, world.p.library.getLibraryFolder(), world.getCurrentField().getSelectedImages());    			
				break;
				
				/* Memory */
			case "SaveLocation":
				world.viewer.addPlaceToMemory();
				break;
			case "ClearMemory":
				world.viewer.clearMemory();
				break;
	
				/* Output */
			case "ExportImage":
				if(!world.outputFolderSelected) world.p.selectFolder("Select an output folder:", "outputFolderSelected");
				world.saveToDisk();
				break;
			case "OutputFolder":
				world.p.selectFolder("Select an output folder:", "outputFolderSelected");
				break;
		}
	}

	/**
	 * Handles checkboxes
	 * @param option 
	 * @param event
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
			case "ClusterView":
				display.setDisplayView(world, 2);
				break;
			case "TimelineView":
				display.setDisplayView(world, 3);
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
			case "MediaTimeMode":
				world.setTimeMode(2);
				break;
			/* Graphics */
			case "TimeFading":
				world.getState().timeFading = option.isSelected();
				break;
			case "FadeEdges":
				world.getState().fadeEdges = option.isSelected();
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
				world.getState().alphaMode = option.isSelected();
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
				world.getState().showModel = option.isSelected();
				break;
			case "MediaToCluster":
				world.getState().showMediaToCluster = option.isSelected();
				break;
			case "CaptureToMedia":
				world.getState().showCaptureToMedia = option.isSelected();
				break;
			case "CaptureToCluster":
				world.getState().showCaptureToCluster = option.isSelected();
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
				world.getState().showMetadata = option.isSelected();
				break;
		}
	}

	/**
	 * @param key Key that was pressed
	 * Respond to user key presses
	 */
	void handleKeyPressed(WMV_World world, char key, int keyCode)
	{
		if (!world.p.state.running && !world.p.state.selectedLibrary)
		{
			world.p.state.openLibraryDialog = true;
		}
		else
		{
			/* General */
			if (key == ' ') 
			{
				if(world.getState().interactive)
				{
					world.finishInteractiveClustering();			// Restart simulation after interative clustering
				}
			}

			if (key == ' ') 
			{
				if(!world.p.basic)
				{
					if(world.p.display.window.showWMVWindow)
						world.p.display.window.hideWMVWindow();
					else
						world.p.display.window.showWMVWindow();
				}
			}

			/* Display Modes */
			if (!optionKey && !commandKey && key == '1') 
				world.p.display.setDisplayView(world, 0);

			if (!optionKey && !commandKey && key == '2') 
				world.p.display.setDisplayView(world, 1);

			if (!optionKey && !commandKey && key == '3') 
				world.p.display.setDisplayView(world, 2);

			if (!optionKey && !commandKey  && key == '4')
				world.p.display.setDisplayView(world, 3);

			if (!optionKey && !commandKey  && key == '5')
			{
				boolean state = !world.getState().showModel;
				world.getState().showModel = state;
				if(world.p.display.window.setupModelWindow)
					world.p.display.window.chkbxShowModel.setSelected(state);
			}

			if (!optionKey && !commandKey  && key == '6') 
				world.getState().showMediaToCluster = !world.getState().showMediaToCluster;			// Draw line from each media point to cluster

			if (!optionKey && !commandKey  && key == '7') 
				world.getState().showCaptureToMedia = !world.getState().showCaptureToMedia;			// Draw line from each media point to its capture location

			if (!optionKey && !commandKey  && key == '8') 
				world.getState().showCaptureToCluster = !world.getState().showCaptureToCluster;		// Draw line from each media capture location to associated cluster

			if (!optionKey && commandKey && key == '1') 
			{
				if(!world.p.display.window.showNavigationWindow)
					world.p.display.window.openNavigationWindow();
				else
					world.p.display.window.hideNavigationWindow();
				commandKey = false;
			}

			if (!optionKey && commandKey && key == '2') 
			{
				if(!world.p.display.window.showTimeWindow)
					world.p.display.window.openTimeWindow();
				else
					world.p.display.window.hideTimeWindow();
				commandKey = false;
			}

			if (!optionKey && commandKey && key == '3') 
			{
				if(!world.p.display.window.showGraphicsWindow)
					world.p.display.window.openGraphicsWindow();
				else
					world.p.display.window.hideGraphicsWindow();
				commandKey = false;
			}

			if (!optionKey && commandKey && key == '4') 
			{
				if(!world.p.display.window.showModelWindow)
					world.p.display.window.openModelWindow();
				else
					world.p.display.window.hideModelWindow();
				commandKey = false;
			}

			if (!optionKey && commandKey && key == '5') 
			{
				if(!world.p.display.window.showMemoryWindow)
					world.p.display.window.openMemoryWindow();
				else
					world.p.display.window.hideMemoryWindow();
				commandKey = false;
			}

			if (!optionKey && commandKey && key == '6') 
			{
				if(!world.p.display.window.showSelectionWindow)
					world.p.display.window.openSelectionWindow();
				else
					world.p.display.window.hideSelectionWindow();
				commandKey = false;
			}

			if (!optionKey && commandKey && key == '7') 
			{
				if(!world.p.display.window.showStatisticsWindow)
					world.p.display.window.openStatisticsWindow();
				else
					world.p.display.window.hideStatisticsWindow();
				commandKey = false;
			}
			
			if (!optionKey && commandKey && key == '8') 
			{
				if(!world.p.display.window.showHelpWindow)
					world.p.display.window.openHelpWindow();
				else
					world.p.display.window.hideHelpWindow();
				commandKey = false;
			}

			if (key == 'R')
				world.p.restart();

			if(world.p.display.displayView == 1)	/* 2D Map View */
			{
				if( key == '+' )
					world.p.display.satelliteMap = !world.p.display.satelliteMap;

				if (key == '{')
					world.viewer.teleportToField(-1, false);

				if (key == '}') 
					world.viewer.teleportToField(1, false);

				/* Clustering */
				if (key == 'r')
				{
					world.p.display.map2D.resetMapZoom(world, true);
				}

				if (key == 'c')
				{
					world.p.display.map2D.resetMapZoom(world, true);
					world.startInteractiveClustering();
				}

				if (key == 'z')
					world.p.display.map2D.zoomToCluster(world, world.getCurrentCluster());

				if (shiftKey && key == 'c')
					world.startInitialClustering();				// Re-run clustering on all fields

				if (key == ']') 
					world.p.display.map2D.zoomIn(world);

				if (key == '[') 
					world.p.display.map2D.zoomOut(world);

				if (key == PApplet.CODED) 					
				{
					if (keyCode == PApplet.LEFT)
						world.viewer.rotateX(-1);

					if (keyCode == PApplet.RIGHT) 
						world.viewer.rotateX(1);

					if (optionKey && shiftKey && keyCode == PApplet.LEFT) 
						world.p.display.map2D.mapScrollTransition( world, 150.f * world.p.display.map2D.mapDistance, 0.f );

					if (optionKey && shiftKey && keyCode == PApplet.RIGHT) 
						world.p.display.map2D.mapScrollTransition( world, -150.f * world.p.display.map2D.mapDistance, 0.f );

					if (optionKey && shiftKey && keyCode == PApplet.DOWN) 
						world.p.display.map2D.mapScrollTransition( world, 0.f, -150.f * world.p.display.map2D.mapDistance );

					if (optionKey && shiftKey && keyCode == PApplet.UP) 
						world.p.display.map2D.mapScrollTransition( world, 0.f, 150.f * world.p.display.map2D.mapDistance );

					if (!optionKey && shiftKey && keyCode == PApplet.LEFT) 
						world.p.display.map2D.zoomRectangleScrollTransition( world, -400.f * world.p.display.map2D.mapDistance, 0.f );

					if (!optionKey && shiftKey && keyCode == PApplet.RIGHT) 
						world.p.display.map2D.zoomRectangleScrollTransition( world, 400.f * world.p.display.map2D.mapDistance, 0.f );

					if (!optionKey && shiftKey && keyCode == PApplet.DOWN) 
						world.p.display.map2D.zoomRectangleScrollTransition( world, 0.f, 400.f * world.p.display.map2D.mapDistance );

					if (!optionKey && shiftKey && keyCode == PApplet.UP) 
						world.p.display.map2D.zoomRectangleScrollTransition( world, 0.f, -400.f * world.p.display.map2D.mapDistance );
				}

				if(key == PApplet.ENTER)
				{
					if(shiftKey)
					{
						world.viewer.teleportToCluster(world.p.display.map2D.getSelectedClusterID(), false, -1);
					}
					else
					{
						world.viewer.teleportToCluster(world.p.display.map2D.getSelectedClusterID(), true, -1);
						world.p.display.displayView = 0;
					}
				}
			}
			else if(world.p.display.displayView == 2)		/* Cluster View */
			{
				if (key == 'c')
				{
					world.p.display.displayCluster = world.viewer.getState().getCurrentClusterID();
				}
				
				if (key == PApplet.CODED) 					
				{
					if (keyCode == PApplet.LEFT) 
					{
						world.p.display.displayCluster--;
						if(world.p.display.displayCluster < 0)
							world.p.display.displayCluster = world.getFieldClusters().size() - 1;

						int count = 0;
						while(world.getCurrentField().getCluster(world.p.display.displayCluster).isEmpty())
						{
							world.p.display.displayCluster--;
							count++;
							if(world.p.display.displayCluster < 0)
								world.p.display.displayCluster = world.getFieldClusters().size() - 1;

							if(count > world.getFieldClusters().size())
								break;
						}
					}

					if (keyCode == PApplet.RIGHT) 
					{
						world.p.display.displayCluster++;
						if( world.p.display.displayCluster >= world.getFieldClusters().size())
							world.p.display.displayCluster = 0;

						int count = 0;
						while(world.getCurrentField().getCluster(world.p.display.displayCluster).isEmpty())
						{
							world.p.display.displayCluster++;
							count++;
							if( world.p.display.displayCluster >= world.getFieldClusters().size())
								world.p.display.displayCluster = 0;

							if(count > world.getFieldClusters().size())
								break;
						}
					}
				}
			}
			else if(world.p.display.displayView == 3)					/* Time View */
			{
				if (key == 'r')									// Zoom out to whole timeline
					world.p.display.resetZoom(world, true);

				if (key == 'z')									// Zoom to fit timeline
					world.p.display.zoomToTimeline(world, true);

				if (key == 't')									// Zoom to fit current time segment
					world.p.display.zoomToCurrentTimeSegment(world, true);

				if (key == 'd')									// Zoom to fit current time segment
					world.p.display.zoomToCurrentDate(world, true);

				if (key == 'a')									// Timeline zoom to fit
					world.p.display.showAllDates();

				if (key == PApplet.ENTER)
				{
					if(world.p.display.getCurrentSelectableTime() >= 0)
					{
						world.viewer.teleportToCluster(world.p.display.getSelectedCluster(), true, -1); 
						world.p.display.setDisplayView(world, 0);
					}
				}
				if (key == PApplet.CODED) 					
				{
					if (keyCode == PApplet.UP) 					// Timeline zoom in 
						world.p.display.zoom(world, -1, true);
					
					if (keyCode == PApplet.DOWN) 				// Timeline zoom out
						world.p.display.zoom(world, 1, true);

					if (keyCode == PApplet.LEFT)  				// Start timeline scrolling left
						world.p.display.scroll(world, -1);
					
					if (keyCode == PApplet.RIGHT)  				// Start timeline scrolling right
						world.p.display.scroll(world, 1);
				}
			}

			if (!world.getState().interactive)		
			{
				/* 3D View Controls */
				if (key == '|')
					world.getCurrentCluster().stitchImages(world.p.stitcher, world.p.library.getLibraryFolder(), world.getCurrentField().getSelectedImages());    			
//					world.getCurrentCluster().stitchImages();    			

				if (optionKey && key == '[')
				{
					if(world.viewer.getThinningAngle() > PApplet.PI / 64.f)
						world.viewer.setThinningAngle( world.viewer.getThinningAngle() - PApplet.PI / 128.f );
//						world.viewer.getSettings().thinningAngle -= PApplet.PI / 128.f;
					world.getCurrentField().analyzeClusterMediaDirections();
				}

				if (optionKey && key == ']')
				{
					if(world.viewer.getThinningAngle() < world.viewer.getVisibleAngle() - PApplet.PI / 128.f)
						world.viewer.setThinningAngle(world.viewer.getThinningAngle() + PApplet.PI / 128.f);
					world.getCurrentField().analyzeClusterMediaDirections();
				}

				if (optionKey && key == '\\')
					world.getCurrentField().stitchAllClusters(world.p.stitcher, world.p.library.getLibraryFolder());		// Teleport to cluster with > 1 times
//					world.getCurrentField().stitchAllClusters();		// Teleport to cluster with > 1 times

				if (!optionKey && key == 'a') 
					world.viewer.startMoveXTransition(-1);

				if (!optionKey && key == 'd') 
					world.viewer.startMoveXTransition(1);

				if( key == 'l' )
					world.viewer.moveToLastCluster(world.viewer.getMovementTeleport());

				if( key == 'L' )
					world.viewer.lookAtNearestMedia();

				if( key == '/' )
					world.saveSimulationState();

				if( key == '?' )
					world.loadViewerState();

//				if( key == '?' )
//					world.loadViewerSettings();

				if( key == 't' )
				{
					boolean state = !world.viewer.getMovementTeleport();
					world.viewer.setMovementTeleport( state );
					if(world.p.display.window.setupNavigationWindow)
						world.p.display.window.chkbxMovementTeleport.setSelected(state);
				}

				if (key == 'T') 
				{
					boolean state = !world.getState().timeFading;
					if(world.getState().timeFading != state)
					{
						world.getState().timeFading = state;
						if(world.p.display.window.setupTimeWindow)
							world.p.display.window.chkbxTimeFading.setSelected(state);
					}
				}

				if (!optionKey && key == 's') 
					world.viewer.startMoveZTransition(1);

				if (!optionKey && key == 'w') 
					world.viewer.walkForward();

				if (optionKey && key == 'm') 
				{
					boolean state = !world.getState().showMetadata;
					world.getState().showMetadata = state;
					if(world.p.display.window.setupSelectionWindow)
						world.p.display.window.chkbxShowMetadata.setSelected(state);
				}

				if (key == 'Q')
					world.viewer.moveToNextCluster(false, -1);

				if (!optionKey && key == 'e')									// Move UP
					world.viewer.startMoveYTransition(-1);

				if (key == 'c') 									// Move DOWN
					world.viewer.startMoveYTransition(1);

				if (key == 'p') 								
					world.getState().paused = !world.getState().paused;

				if (key == 'j') 
					world.viewer.moveToRandomCluster(world.viewer.getMovementTeleport());				// Jump (teleport) to random cluster

				if (key == 'I')
					world.viewer.setOrientationMode( !world.viewer.getSettings().orientationMode );

				if (key == 'W') 
					world.viewer.moveToNearestClusterAhead(false);

				if (!optionKey && key == ']') {
					float value = world.settings.altitudeScalingFactor * 1.052f;
					world.settings.altitudeScalingFactor = PApplet.constrain(value, 0.f, 1.f);
					world.getCurrentField().calculateMediaLocations();		// Recalculate media locations
					world.getCurrentField().createClusters();				// Recalculate cluster locations
					world.getCurrentField().recalculateGeometries();				// Recalculate cluster locations
				}

				if (!optionKey && key == '[') {
					float value = world.settings.altitudeScalingFactor *= 0.95f;
					world.settings.altitudeScalingFactor = PApplet.constrain(value, 0.f, 1.f);
					world.getCurrentField().calculateMediaLocations();		// Recalculate media locations
					world.getCurrentField().createClusters();				// Recalculate cluster locations
					world.getCurrentField().recalculateGeometries();				// Recalculate cluster locations
				}

				if (key == 'n')						// Teleport to next time segment on same date
				{
					if(world.p.display.displayView == 0)
						world.viewer.moveToNextTimeSegment(true, world.viewer.getMovementTeleport(), true);
					else
						world.viewer.moveToNextTimeSegment(true, true, false);
				}

				if (key == 'b')						// Teleport to previous time segment on same date
				{
					if(world.p.display.displayView == 0)
						world.viewer.moveToPreviousTimeSegment(true, world.viewer.getMovementTeleport(), true);
					else
						world.viewer.moveToPreviousTimeSegment(true, true, false);
				}

				if (key == 'N')						// Teleport to next time segment on any date
				{
					if(world.p.display.displayView == 0)
						world.viewer.moveToNextTimeSegment(false, world.viewer.getMovementTeleport(), true);
					else
						world.viewer.moveToNextTimeSegment(false, true, false);
				}

				if (key == 'B')						// Teleport to previous time segment on any date
				{
					if(world.p.display.displayView == 0)
						world.viewer.moveToPreviousTimeSegment(false, world.viewer.getMovementTeleport(), true);
					else
						world.viewer.moveToPreviousTimeSegment(false, true, false);
				}

				if (key == '~')
					if(!world.viewer.isFollowing())
					{
						world.viewer.followMemory();
						if(world.p.display.window.setupNavigationWindow)
						{
							world.p.display.window.optTimeline.setSelected(true);
							world.p.display.window.optGPSTrack.setSelected(false);
							world.p.display.window.optMemory.setSelected(false);
						}
					}

				if (optionKey && key == 'g')
					if(!world.viewer.isFollowing())
					{
						world.viewer.followGPSTrack();
						if(world.p.display.window.setupNavigationWindow)
						{
							world.p.display.window.optTimeline.setSelected(false);
							world.p.display.window.optGPSTrack.setSelected(true);
							world.p.display.window.optMemory.setSelected(false);
						}
					}

				if (!optionKey && key == '>')
				{
					if(!world.viewer.isFollowing())
					{
						world.viewer.followTimeline(true, false);
						if(world.p.display.window.setupNavigationWindow)
						{
							world.p.display.window.optTimeline.setSelected(false);
							world.p.display.window.optGPSTrack.setSelected(false);
							world.p.display.window.optMemory.setSelected(true);
						}
					}
				}

				if (key == 'u') 		// Teleport to nearest cluster with video
					world.viewer.moveToNextCluster(true, 2);

				if (key == 'U') 		// Go to nearest cluster with video
					world.viewer.moveToNextCluster(false, 2);

				if (shiftKey && key == 'u') 		// Teleport to nearest cluster with panorama
					world.viewer.moveToNextCluster(true, 1);

				if (shiftKey && key == 'U') 		// Go to nearest cluster with panorama
					world.viewer.moveToNextCluster(false, 1);

				if (key == 'm') 
					world.viewer.moveToNearestCluster(true);

				if (key == 'M') 
					world.viewer.moveToNearestCluster(false);

				if (key == '-') 
					world.getCurrentField().fadeObjectDistances(0.85f);

				if (key == '=')
					world.getCurrentField().fadeObjectDistances(1.176f);

//				if (key == 'Z')
//					world.p.display.map2D.zoomToRectangle(100, 50, world.p.display.map2D.largeMapWidth * 0.5f, world.p.display.map2D.largeMapHeight * 0.5f);

				/* 3D Controls Disabled in HUD View */
				if(!world.p.display.inDisplayView())							
				{
					if (key == '{')
						world.viewer.teleportToField(-1, true);

					if (key == '}') 
						world.viewer.teleportToField(1, true);

					if (key == 'q') 
						world.viewer.startZoomTransition(-1);

					if (key == 'z') 
						world.viewer.startZoomTransition(1);

					if (optionKey && key == 'e')
					{
						boolean state = !world.getState().fadeEdges;
						world.getState().fadeEdges = state;
						if(world.p.display.window.setupGraphicsWindow)
						{
							world.p.display.window.chkbxFadeEdges.setSelected(state);
						}
					}

					if (key == 'i')	
					{
						if(world.viewer.getSettings().hideImages)
							world.viewer.showImages();
						else
							world.viewer.hideImages();
					}

					if (key == 'h')	
					{
						if(world.viewer.getSettings().hidePanoramas)
							world.viewer.showPanoramas();
						else
							world.viewer.hidePanoramas();
					}

					if (key == 'v')	
					{
						if(world.viewer.getSettings().hideVideos)
							world.viewer.showVideos();
						else
							world.viewer.hideVideos();
					}

					if (key == 'P')
					{
						boolean state = !world.getState().alphaMode;
						world.getState().alphaMode = state;
						if(world.p.display.window.setupGraphicsWindow)
							world.p.display.window.chkbxAlphaMode.setSelected(state);
					}

					if (!shiftKey && optionKey && key == ' ') 
					{
						boolean state = !world.getState().timeFading;
						world.getState().timeFading = state;
						if(world.p.display.window.setupGraphicsWindow)
						{
							world.p.display.window.chkbxTimeFading.setSelected(state);
						}
					}

					if (key == ')') {
						float newAlpha = PApplet.constrain(world.getState().alpha+15.f, 0.f, 255.f);
						world.fadeAlpha(newAlpha);
					}

					if (key == '(') {
						float newAlpha = PApplet.constrain(world.getState().alpha-15.f, 0.f, 255.f);
						world.fadeAlpha(newAlpha);
					}

					if (key == ':')
					{
						world.settings.showUserPanoramas = !world.settings.showUserPanoramas;
					}

					if (key == ';')
					{
						world.settings.showStitchedPanoramas = !world.settings.showStitchedPanoramas;
					}

					if (key == 'A') 
					{
						world.viewer.setSelection( !world.viewer.getSelection() );
						if(world.p.display.window.setupSelectionWindow)
							world.p.display.window.chkbxSelectionMode.setSelected(world.viewer.getSettings().selection);

						if(world.viewer.getSelection() && world.viewer.getMultiSelection())
						{
							world.viewer.setMultiSelection( false );
							if(world.p.display.window.setupSelectionWindow)
								world.p.display.window.chkbxMultiSelection.setSelected( false );
						}
						if(world.viewer.getSelection() && world.viewer.getSegmentSelection()) 
						{
							world.viewer.setSegmentSelection( false );
							if(world.p.display.window.setupSelectionWindow)
								world.p.display.window.chkbxSegmentSelection.setSelected( false );
						}
					}

					if (optionKey && key == 'x')
						world.getCurrentField().deselectAllMedia(false);

					if (optionKey && key == '-')
						world.viewer.setVisibleAngle( world.viewer.getVisibleAngle() - 3.1415f / 128.f ); 

					if (optionKey && key == '=')
						world.viewer.setVisibleAngle( world.viewer.getVisibleAngle() + 3.1415f / 128.f ); 

					/* Selection */
					if (!optionKey && key == 'x') 
						world.viewer.chooseMediaInFront(true);

					if (!optionKey && key == 'X')
						world.viewer.chooseMediaInFront(false);

					if (key == 'S')
					{
						world.viewer.setMultiSelection( !world.viewer.getMultiSelection() );
						if(world.viewer.getMultiSelection() && !world.viewer.getSelection())
							world.viewer.setSelection( true );
					}

					if (optionKey && key == 's')
					{
						world.viewer.setSegmentSelection( !world.viewer.getSegmentSelection() );
						if(world.viewer.getSegmentSelection() && !world.viewer.getSelection())
							world.viewer.setSelection( true );
					}

					/* GPS */
					if (!optionKey && key == 'g') 
						world.viewer.importGPSTrack();				// Select a GPS tracking file from disk to load and navigate 

					/* Memory */
					if (key == '`') 
						world.viewer.addPlaceToMemory();

					if (key == 'Y') 
						world.viewer.clearMemory();

					/* Graphics */
					if (key == 'G')
					{
						boolean state = !world.viewer.getAngleFading();
						world.viewer.setAngleFading( state );
						if(world.p.display.window.setupGraphicsWindow)
							world.p.display.window.chkbxAngleFading.setSelected(state);
					}

//					if (key == 'H')
//					{
//						boolean state = !world.viewer.getAngleThinning();
//						world.viewer.setAngleThinning( state );
//						if(world.p.display.window.setupGraphicsWindow)
//							world.p.display.window.chkbxAngleThinning.setSelected(state);
//					}

					/* Output */
					if (key == 'O') 
						world.p.selectFolder("Select an output folder:", "outputFolderSelected");

					if (key == 'o') 	// Save image to disk
					{	
						if(!world.outputFolderSelected) world.p.selectFolder("Select an output folder:", "outputFolderSelected");
						world.saveToDisk();
					}

					if (key == '&') 
					{
						if(world.settings.defaultMediaLength > 10)
							world.settings.defaultMediaLength -= 10;
					}

					if (key == '*') 			// Look for images when none are visible
					{
						if(world.settings.defaultMediaLength < 990)
							world.settings.defaultMediaLength += 10;
					}
				}
			}
			else 						// Interactive Clustering Mode
			{
				if (!optionKey && key == 'h')
				{
					if(!world.getState().hierarchical)
					{
						world.getState().hierarchical = true;
						if(!world.getCurrentField().getState().dendrogramCreated)
							world.getCurrentField().runHierarchicalClustering();
						world.getCurrentField().setDendrogramDepth(world.getCurrentField().getState().clusterDepth);				// Initialize clusters 
						world.getCurrentField().createTimeline();					// Create field timeline
					}

				}

				if (!optionKey && key == 'k')
				{
					if(world.getState().hierarchical)
					{
						world.getState().hierarchical = false;
						WMV_Field f = world.getCurrentField();
						f.runKMeansClustering(world.settings.kMeansClusteringEpsilon, f.getModel().getState().clusterRefinement, f.getModel().getState().clusterPopulationFactor);
						world.getCurrentField().createTimeline();					// Create field timeline
					}
				}

				if (key == '[') 	
				{
					if(!world.getState().autoClusterDistances && world.settings.minClusterDistance > 0.25f)
					{
						world.settings.minClusterDistance -= 0.25f;
						for(WMV_Field f : world.getFields())
						{
							f.getModel().setMinClusterDistance(world.settings.minClusterDistance);	
							world.getCurrentField().runKMeansClustering( world.settings.kMeansClusteringEpsilon, world.getCurrentField().getModel().getState().clusterRefinement, world.getCurrentField().getModel().getState().clusterPopulationFactor );
							world.getCurrentField().initializeClusters(world.getState().mergeClusters);			
							world.p.display.map2D.initializeMaps(world);
						}
					}
				}

				if (key == ']') 	
				{
					if(!world.getState().autoClusterDistances && world.settings.minClusterDistance < world.settings.maxClusterDistance - 2.f)
					{
						world.settings.minClusterDistance += 0.25f;
						//					System.out.println("world.minClusterDistance:"+world.minClusterDistance);
						for(WMV_Field f : world.getFields())
						{
							f.getModel().setMinClusterDistance(world.settings.minClusterDistance);
							world.getCurrentField().runKMeansClustering( world.settings.kMeansClusteringEpsilon, world.getCurrentField().getModel().getState().clusterRefinement, world.getCurrentField().getModel().getState().clusterPopulationFactor );
							world.getCurrentField().initializeClusters(world.getState().mergeClusters);			
							world.p.display.map2D.initializeMaps(world);
						}
					}
				}
			}

			/* Arrow and Shift Keys */
			if (key == PApplet.CODED) 					
			{
				if(world.p.display.inDisplayView())
				{
					if(world.getState().interactive)			/* Interactive Clustering */
					{
						if(world.getState().hierarchical)
						{
							if (keyCode == PApplet.UP) 
							{
								int clusterDepth = world.getCurrentField().getState().clusterDepth + 1;
								if(clusterDepth <= world.getCurrentField().getState().deepestLevel)
									world.getCurrentField().setDendrogramDepth( clusterDepth );
							}

							if (keyCode == PApplet.DOWN) 
							{
								int clusterDepth = world.getCurrentField().getState().clusterDepth - 1;
								if(clusterDepth >= world.getCurrentField().getState().minClusterDepth)
									world.getCurrentField().setDendrogramDepth( clusterDepth );
							}
						}
						else
						{
							if (keyCode == PApplet.LEFT) 		
							{
								world.getCurrentField().getModel().state.clusterRefinement -= 10;
								float populationFactor = world.getCurrentField().getModel().state.clusterPopulationFactor;

								if(world.getCurrentField().getModel().state.clusterRefinement >= world.getCurrentField().getModel().state.minClusterRefinement)
								{
									world.getCurrentField().runKMeansClustering( world.settings.kMeansClusteringEpsilon, world.getCurrentField().getModel().state.clusterRefinement, populationFactor );
									world.getCurrentField().initializeClusters(world.getState().mergeClusters);			
									world.p.display.map2D.initializeMaps(world);
								}
								else world.getCurrentField().getModel().state.clusterRefinement += 10;
							}

							if (keyCode == PApplet.RIGHT) 	
							{
								world.getCurrentField().getModel().state.clusterRefinement += 10;
								float populationFactor = world.getCurrentField().getModel().state.clusterPopulationFactor;

								if(world.getCurrentField().getModel().state.clusterRefinement <= world.getCurrentField().getModel().state.maxClusterRefinement)
								{
									world.getCurrentField().runKMeansClustering( world.settings.kMeansClusteringEpsilon, world.getCurrentField().getModel().state.clusterRefinement, populationFactor );
									world.getCurrentField().initializeClusters(world.getState().mergeClusters);			
									world.p.display.map2D.initializeMaps(world);
								}
								else world.getCurrentField().getModel().state.clusterRefinement -= 10;
							}

							if (keyCode == PApplet.DOWN) 		
							{
								int refinementAmount = world.getCurrentField().getModel().state.clusterRefinement;
								world.getCurrentField().getModel().state.clusterPopulationFactor -= 1.f;

								if(world.getCurrentField().getModel().state.clusterPopulationFactor >= world.getCurrentField().getModel().state.minPopulationFactor)
								{
									world.getCurrentField().runKMeansClustering( world.settings.kMeansClusteringEpsilon, refinementAmount, world.getCurrentField().getModel().state.clusterPopulationFactor );
									world.getCurrentField().initializeClusters(world.getState().mergeClusters);			
									world.p.display.map2D.initializeMaps(world);
								}
								else world.getCurrentField().getModel().state.clusterPopulationFactor += 1.f;
							}

							if (keyCode == PApplet.UP) 	
							{
								int refinementAmount = world.getCurrentField().getModel().state.clusterRefinement;
								world.getCurrentField().getModel().state.clusterPopulationFactor += 1.f;

								if(world.getCurrentField().getModel().state.clusterPopulationFactor <= world.getCurrentField().getModel().state.maxPopulationFactor)
								{
									world.getCurrentField().runKMeansClustering( world.settings.kMeansClusteringEpsilon, refinementAmount, world.getCurrentField().getModel().state.clusterPopulationFactor );
									world.getCurrentField().initializeClusters(world.getState().mergeClusters);			
									world.p.display.map2D.initializeMaps(world);
								}
								else world.getCurrentField().getModel().state.clusterPopulationFactor -= 1.f;
							}
						}
					}
				}
				else
				{
					/* Navigation */
					if (keyCode == PApplet.LEFT) 
						world.viewer.rotateX(-1);

					if (keyCode == PApplet.RIGHT) 
						world.viewer.rotateX(1);

					if (keyCode == PApplet.UP) 
						world.viewer.rotateY(-1);

					if (keyCode == PApplet.DOWN) 
						world.viewer.rotateY(1);

					/* Time */
					if (shiftKey && keyCode == PApplet.UP) 
						world.incrementTime();

					if (shiftKey && keyCode == PApplet.DOWN) 
						world.decrementTime();

					if (shiftKey && keyCode == PApplet.LEFT) 
						world.decrementCycleLength();

					if (shiftKey && keyCode == PApplet.RIGHT) 
						world.incrementCycleLength();
				}

				if (keyCode == PApplet.SHIFT) {
					shiftKey = true;
				}

				if (keyCode == PApplet.ALT) 
					optionKey = true;
				
				if (keyCode == COMMAND_KEY) 
				{
					commandKey = true;
//					System.out.println("+ commandKey set to "+commandKey + " on frame:"+frameCount);
				}
			}
		}
	}
	
	/**
	 * Respond to user key releases
	 */
	void handleKeyReleased(WMV_Viewer viewer, ML_Display display, char key, int keyCode)
	{
//		System.out.println("handleKeyReleased keyCode: "+keyCode + " on frame:"+frameCount);

		/* Navigation */
		if (key == 'a') 
			viewer.stopMoveXTransition();
		if (key == 'd') 
			viewer.stopMoveXTransition();
		if (key == 's') 
			viewer.stopMoveZTransition();
		if (key == 'w') 
			viewer.stopMoveZTransition();
		if (key == 'e') 
			viewer.stopMoveYTransition();
		if (key == 'c') 
			viewer.stopMoveYTransition();

		/* Coded Keys */
		if (key == PApplet.CODED) 
		{
			if(display.displayView == 0)
			{
				if (keyCode == PApplet.LEFT) 
					viewer.stopRotateXTransition();
				if (keyCode == PApplet.RIGHT) 
					viewer.stopRotateXTransition();
				if (keyCode == PApplet.UP) 
					viewer.stopRotateYTransition();
				if (keyCode == PApplet.DOWN) 
					viewer.stopRotateYTransition();
			}
			else if(display.displayView == 3)
			{
				if (keyCode == PApplet.UP)  				// Timeline scroll left
					display.stopZooming();
				if (keyCode == PApplet.DOWN)  				// Timeline scroll right
					display.stopZooming();
				if (keyCode == PApplet.LEFT)  				// Timeline scroll left
					display.stopScrolling();
				if (keyCode == PApplet.RIGHT)  				// Timeline scroll right
					display.stopScrolling();

//				if (keyCode == PApplet.LEFT)  				// Timeline scroll left
//					display.scroll(5.f, -1);
//				if (keyCode == PApplet.RIGHT)  				// Timeline scroll right
//					display.scroll(5.f, 1);
			}
			
			if (keyCode == PApplet.SHIFT) 
				shiftKey = false;
			if (keyCode == PApplet.ALT) 
				optionKey = false;
			if (keyCode == COMMAND_KEY)
			{
				commandKey = false;
//				System.out.println("commandKey set to "+commandKey + " on frame:"+frameCount);
			}
		}
	}

	/* Mouse */
	public void updateMouseSelection(int mouseX, int mouseY, int frameCount)
	{
//		mediaSelector
		
		if(frameCount - clickedRecentlyFrame > doubleClickSpeed && mouseClickedRecently)
		{
			mouseClickedRecently = false;
		}
		
		if(frameCount - clickedRecentlyFrame > 20 && !mouseReleased)
		{
//			System.out.println("Held mouse...");
		}
	}
	
	void updateMouseNavigation(WMV_Viewer viewer, int mouseX, int mouseY, int frameCount)
	{			
		if(frameCount - clickedRecentlyFrame > doubleClickSpeed && mouseClickedRecently)
		{
			mouseClickedRecently = false;
//			mouseReleasedRecently = false;
	//			System.out.println("SET CLICKED RECENTLY TO FALSE");
		}
		
		if(frameCount - clickedRecentlyFrame > 20 && !mouseReleased)
		{
//			System.out.println("Held mouse...");
			viewer.addPlaceToMemory();
		}
		
//		System.out.println("mouseX:"+mouseX+" mouseY:"+mouseY+" screenWidth * 0.25:"+(screenWidth * 0.25));
			
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
//			System.out.println("MousePressed!");
		if(!viewer.getSettings().orientationMode && viewer.getState().lastMovementFrame > 5)
		{
			if(mouseX > screenWidth * 0.25 && mouseX < screenWidth * 0.75 && mouseY < screenHeight * 0.75 && mouseY > screenHeight * 0.25)
			{
				viewer.walkForward();
			}
			else
			{
//				mouseClickedX = mouseX;
//				mouseClickedY = mouseY;
			}
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
		{
			doubleClick = true;
		}

		if(world.viewer.getSettings().mouseNavigation)
		{
			world.viewer.walkSlower();
			world.viewer.getState().lastMovementFrame = frameCount;
			if(doubleClick)									
				world.viewer.moveToNearestCluster(world.viewer.getMovementTeleport());
		}
		
		if(display.displayView == 1)
		{
			display.map2D.handleMouseReleased(world, mouseX, mouseY);
		}
//		else if(display.displayView == 2)
//		{
//			display.handleMouseReleased(mouseX, mouseY);
//		}
		else if(display.displayView == 3)
		{
			display.handleMouseReleased(world, mouseX, mouseY);
		}
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