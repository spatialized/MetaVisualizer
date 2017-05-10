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
//	final public int COMMAND_KEY = 157;			// -- Not reliable, change this!
	
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

	/**
	 * Handle user input from all sliders
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
				switch(world.state.timeMode)
				{
					case 0:												// Cluster
						world.setAllClustersTimeCycleLength(slider.getValueI());
//						world.getCurrentCluster().setTimeCycleLength( slider.getValueI() );
						break;
					case 1:												// Field
						world.settings.timeCycleLength = slider.getValueI();
						break;
					case 2:												// Media
						break;
					default:
						break;
				}
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
				world.viewer.setUserBrightness( slider.getValueF() );
			}
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
	 * @param button GButton that was pressed
	 * @param event GEvent that occurred
	 */
	public void handleButtonEvent(MultimediaLocator ml, ML_Display display, GButton button, GEvent event) 
	{ 
		switch(button.tag) 
		{
			case "Restart":
				ml.restart();
				break;
			
			/* Library */
			case "CreateLibrary":
				ml.createNewLibrary = true;
				ml.state.chooseLibrary = true;
				display.window.hideLibraryWindow();
				break;
	
			case "OpenLibrary":
				if(ml.createNewLibrary) ml.createNewLibrary = false;
				ml.state.chooseLibrary = true;
				display.window.hideLibraryWindow();
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

			case "ZoomIn":
				ml.world.viewer.startZoomTransition(-1);
				break;
			case "ZoomOut":
				ml.world.viewer.startZoomTransition(1);
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
				ml.world.saveToDisk();
				break;
			case "OutputFolder":
				ml.selectFolder("Select an output folder:", "outputFolderSelected");
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
			case "LibraryView":
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
	void handleKeyPressed(MultimediaLocator ml, char key, int keyCode)
	{
		if (!ml.state.running && !ml.state.selectedLibrary)
		{
//			ml.state.chooseLibrary = true;			// -- Now handled by buttonEvent
		}
		else
		{
			/* General */
			if (key == ' ') 
			{
				if(ml.state.interactive)
					ml.finishInteractiveClustering();			// Restart simulation after interactive clustering
			}

			if (key == ' ') 
			{
				if(ml.display.window.showMLWindow)
					ml.display.window.hideMLWindow();
				else
					ml.display.window.showWMVWindow();
			}

			/* Display Modes */
			if (key == '1') 
				ml.display.setDisplayView(ml.world, 0);

			if (key == '2') 
				ml.display.setDisplayView(ml.world, 1);

			if (key == '3') 
				ml.display.setDisplayView(ml.world, 2);

			if (key == '4')
				ml.display.setDisplayView(ml.world, 3);

			if (key == '5')
			{
				boolean state = !ml.world.getState().showModel;
				ml.world.state.showModel = state;
				if(ml.display.window.setupModelWindow)
					ml.display.window.chkbxShowModel.setSelected(state);
			}

			if (key == '6') 
				ml.world.state.showMediaToCluster = !ml.world.getState().showMediaToCluster;			// Draw line from each media point to cluster

			if (key == '7') 
				ml.world.state.showCaptureToMedia = !ml.world.getState().showCaptureToMedia;			// Draw line from each media point to its capture location

			if (key == '8') 
				ml.world.state.showCaptureToCluster = !ml.world.getState().showCaptureToCluster;		// Draw line from each media capture location to associated cluster

			if (key == '!') 
			{
				if(!ml.display.window.showNavigationWindow)
					ml.display.window.openNavigationWindow();
				else
					ml.display.window.hideNavigationWindow();
//				commandKey = false;
			}

			if (key == '@') 
			{
				if(!ml.display.window.showTimeWindow)
					ml.display.window.openTimeWindow();
				else
					ml.display.window.hideTimeWindow();
			}

			if (key == '#') 
			{
				if(!ml.display.window.showGraphicsWindow)
					ml.display.window.openGraphicsWindow();
				else
					ml.display.window.hideGraphicsWindow();
			}

			if (key == '$') 
			{
				if(!ml.display.window.showModelWindow)
					ml.display.window.openModelWindow();
				else
					ml.display.window.hideModelWindow();
			}

			if (key == '%') 
			{
				if(!ml.display.window.showMemoryWindow)
					ml.display.window.openMemoryWindow();
				else
					ml.display.window.hideMemoryWindow();
			}

			if (key == '^') 
			{
				if(!ml.display.window.showSelectionWindow)
					ml.display.window.openSelectionWindow();
				else
					ml.display.window.hideSelectionWindow();
			}

			if (key == '&') 
			{
				if(!ml.display.window.showStatisticsWindow)
					ml.display.window.openStatisticsWindow();
				else
					ml.display.window.hideStatisticsWindow();
			}
			
			if (key == '*') 
			{
				if(!ml.display.window.showHelpWindow)
					ml.display.window.openHelpWindow();
				else
					ml.display.window.hideHelpWindow();
			}

			if (key == 'R')
				ml.restart();

			if(ml.display.displayView == 1)		/* 2D Map View */
			{
				if (key == 'a') 
					ml.display.map2D.panLeft();
				if (key == 'd') 
					ml.display.map2D.panRight();
				if (key == 's') 
					ml.display.map2D.panDown();
				if (key == 'w') 
					ml.display.map2D.panUp();

				if (key == 'j') 
					ml.world.viewer.moveToRandomCluster(true, false);				// Teleport to random cluster
				
				if (key == '{')
					ml.world.viewer.teleportToFieldOffset(-1, true, false);

				if (key == '}') 
					ml.world.viewer.teleportToFieldOffset(1, true, false);

				/* Clustering */
				if (key == 'r')
				{
//					ml.display.map2D.resetMapZoom(ml.world, true);
				}

				if (key == 'c')
				{
//					ml.display.map2D.resetMapZoom(ml.world, true);
					ml.startInteractiveClustering();
				}

				if (key == 'z')
					ml.display.map2D.zoomToCluster(ml.world, ml.world.getCurrentCluster(), true);	// Zoom to current cluster

				if (key == 'Z')
					ml.display.map2D.zoomToField(ml.world, ml.world.getCurrentField(), true);	// Zoom to current field

				if (shiftKey && key == 'c')
					ml.startInitialClustering();				// Re-run clustering on all fields

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

				if(key == PApplet.ENTER)
				{
					ml.world.viewer.moveToClusterOnMap(ml.display.map2D.getSelectedClusterID(), shiftKey);
				}
			}
			else if(ml.display.displayView == 2)									/* Library View */
			{
				if (key == 'j') 
					ml.world.viewer.moveToRandomCluster(true, false);				/* Teleport to random cluster */

				if (key == 'c')
				{
					ml.display.currentDisplayCluster = ml.world.viewer.getState().getCurrentClusterID();
				}

				if (key == 'z')
					ml.display.map2D.zoomToField(ml.world, ml.world.getCurrentField(), true);

				
				if (ml.display.libraryViewMode == 0)
				{
					if (key == 'a') 
						ml.display.map2D.panLeft();
					if (key == 'd') 
						ml.display.map2D.panRight();
					if (key == 's') 
						ml.display.map2D.panDown();
					if (key == 'w') 
						ml.display.map2D.panUp();
					
					if(key == PApplet.ENTER)
					{
						int selectedField = ml.display.map2D.getSelectedFieldID();
						if(shiftKey)
						{
//							if(ml.world.getField(selectedField).hasBeenVisited())
							ml.world.viewer.teleportToField(selectedField, true, false);
						}
						else
						{
							ml.world.viewer.teleportToField(selectedField, true, true);
							ml.display.displayView = 0;
						}
					}
				}

				if (key == '[') 
				{
					ml.display.libraryViewMode--;
					if(ml.display.libraryViewMode < 0)
						ml.display.libraryViewMode = ml.world.getFieldClusters().size() - 1;
				}

				if (key == ']') 
				{
					ml.display.libraryViewMode++;
					if( ml.display.libraryViewMode >= ml.world.getFieldClusters().size())
						ml.display.libraryViewMode = 0;
					
					if(ml.display.libraryViewMode == 2)
					{
						if(!ml.display.initializedWorldMap)
							ml.display.map2D.initializeWorldMap(ml.world, false);
					}
				}

				if (key == PApplet.CODED) 					
				{
					if(ml.display.libraryViewMode == 0)					// Library World View
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
								ml.display.currentDisplayCluster = ml.world.getFieldClusters().size() - 1;

							int count = 0;
							while(ml.world.getCurrentField().getCluster(ml.display.currentDisplayCluster).isEmpty())
							{
								ml.display.currentDisplayCluster--;
								count++;
								if(ml.display.currentDisplayCluster < 0)
									ml.display.currentDisplayCluster = ml.world.getFieldClusters().size() - 1;

								if(count > ml.world.getFieldClusters().size())
									break;
							}
						}

						if (keyCode == PApplet.UP) 
						{
							ml.display.currentDisplayCluster++;
							if( ml.display.currentDisplayCluster >= ml.world.getFieldClusters().size())
								ml.display.currentDisplayCluster = 0;

							int count = 0;
							while(ml.world.getCurrentField().getCluster(ml.display.currentDisplayCluster).isEmpty())
							{
								ml.display.currentDisplayCluster++;
								count++;
								if( ml.display.currentDisplayCluster >= ml.world.getFieldClusters().size())
									ml.display.currentDisplayCluster = 0;

								if(count > ml.world.getFieldClusters().size())
									break;
							}
						}
					}
				}
			}
			else if(ml.display.displayView == 3)					/* Time View */
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

			if (!ml.state.interactive)						/* World View Controls */
			{
				if (key == '0')
				{
					ml.state.sphericalView = !ml.state.sphericalView;
				}
				
				if (optionKey && key == '[')
				{
					if(ml.world.viewer.getThinningAngle() > PApplet.PI / 64.f)
						ml.world.viewer.setThinningAngle( ml.world.viewer.getThinningAngle() - PApplet.PI / 128.f );
					ml.world.getCurrentField().findClusterMediaDirections();
				}

				if (optionKey && key == ']')
				{
					if(ml.world.viewer.getThinningAngle() < ml.world.viewer.getVisibleAngle() - PApplet.PI / 128.f)
						ml.world.viewer.setThinningAngle(ml.world.viewer.getThinningAngle() + PApplet.PI / 128.f);
					ml.world.getCurrentField().findClusterMediaDirections();
				}

				if (optionKey && key == '\\')
					ml.world.getCurrentField().stitchAllClusters(ml.stitcher, ml.library.getLibraryFolder());		// Teleport to cluster with > 1 times

				if (!optionKey && key == 'a') 
					ml.world.viewer.startMoveXTransition(-1);

				if (!optionKey && key == 'd') 
					ml.world.viewer.startMoveXTransition(1);

				if( key == 'l' )
					ml.world.viewer.moveToLastCluster(ml.world.viewer.getMovementTeleport());

				if( key == 'L' )
					ml.world.viewer.lookAtNearestMedia();

				if( key == '/' )
					ml.world.saveSimulationState();

				if( key == '?' )
				{
					if(ml.world.getFields().size() > 1)
						ml.world.saveAllSimulationStates();
					else
						ml.world.saveSimulationState();
				}

				if( key == 't' && !optionKey )
				{
					boolean state = !ml.world.viewer.getMovementTeleport();
					ml.world.viewer.setMovementTeleport( state );
					if(ml.display.window.setupNavigationWindow)
						ml.display.window.chkbxMovementTeleport.setSelected(state);
				}

				if( key == 't' && optionKey )
				{
					boolean state = ml.world.state.displayTerrain;
					if(state)
					{
						ml.world.fadeOutTerrain(true);
					}
					else
					{
						ml.world.state.terrainAlpha = 0.f;
						ml.world.state.displayTerrain = true;
						ml.world.fadeInTerrain();
					}
				}

				if (key == 'T') 
				{
					boolean state = !ml.world.getState().timeFading;
					if(ml.world.getState().timeFading != state)
					{
						ml.world.state.timeFading = state;
						if(ml.display.window.setupTimeWindow)
							ml.display.window.chkbxTimeFading.setSelected(state);
					}
				}

				if (!optionKey && key == 's') 
					ml.world.viewer.startMoveZTransition(1);

				if (!optionKey && key == 'w') 
					ml.world.viewer.walkForward();

				if (optionKey && key == 'M') 
				{
					boolean state = !ml.world.getState().showMetadata;
					ml.world.state.showMetadata = state;
					if(ml.display.window.setupSelectionWindow)
						ml.display.window.chkbxShowMetadata.setSelected(state);
				}

				if (key == 'Q')
					ml.world.viewer.moveToNextCluster(false, -1);

				if (!optionKey && key == 'e')									// Move UP
					ml.world.viewer.startMoveYTransition(-1);

				if (key == 'c') 									// Move DOWN
					ml.world.viewer.startMoveYTransition(1);

				if (key == '-') 								
					ml.world.state.paused = !ml.world.getState().paused;

				if (key == '9')							// -- Disabled
					ml.world.viewer.setOrientationMode( !ml.world.viewer.getSettings().orientationMode );

				if (key == 'W') 
					ml.world.viewer.moveToNearestClusterAhead(false);

				if (!optionKey && key == ']') {
					float value = ml.world.settings.altitudeScalingFactor * 1.052f;
					ml.world.settings.altitudeScalingFactor = PApplet.constrain(value, 0.f, 1.f);
					ml.world.getCurrentField().calculateMediaLocations(true);		// Recalculate media locations
					ml.world.getCurrentField().createClusters();				// Recalculate cluster locations
					ml.world.getCurrentField().recalculateGeometries();				// Recalculate cluster locations
				}

				if (!optionKey && key == '[') {
					float value = ml.world.settings.altitudeScalingFactor *= 0.95f;
					ml.world.settings.altitudeScalingFactor = PApplet.constrain(value, 0.f, 1.f);
					ml.world.getCurrentField().calculateMediaLocations(true);		// Recalculate media locations
					ml.world.getCurrentField().createClusters();				// Recalculate cluster locations
					ml.world.getCurrentField().recalculateGeometries();				// Recalculate cluster locations
				}

				if (key == 'n')						// Teleport to next time segment on same date
				{
					if(ml.display.displayView == 0)
						ml.world.viewer.moveToNextTimeSegment(true, ml.world.viewer.getMovementTeleport(), true);
					else
						ml.world.viewer.moveToNextTimeSegment(true, true, false);
				}

				if (key == 'b')						// Teleport to previous time segment on same date
				{
					if(ml.display.displayView == 0)
						ml.world.viewer.moveToPreviousTimeSegment(true, ml.world.viewer.getMovementTeleport(), true);
					else
						ml.world.viewer.moveToPreviousTimeSegment(true, true, false);
				}

				if (key == 'N')						// Teleport to next time segment on any date
				{
					if(ml.display.displayView == 0)
						ml.world.viewer.moveToNextTimeSegment(false, ml.world.viewer.getMovementTeleport(), true);
					else
						ml.world.viewer.moveToNextTimeSegment(false, true, false);
				}

				if (key == 'B')						// Teleport to previous time segment on any date
				{
					if(ml.display.displayView == 0)
						ml.world.viewer.moveToPreviousTimeSegment(false, ml.world.viewer.getMovementTeleport(), true);
					else
						ml.world.viewer.moveToPreviousTimeSegment(false, true, false);
				}

				if (key == '~')
					if(!ml.world.viewer.isFollowing())
					{
						ml.world.viewer.followMemory();
						if(ml.display.window.setupNavigationWindow)
						{
							ml.display.window.optTimeline.setSelected(true);
							ml.display.window.optGPSTrack.setSelected(false);
							ml.display.window.optMemory.setSelected(false);
						}
					}

				if (optionKey && key == 'g')
					if(!ml.world.viewer.isFollowing())
					{
						ml.world.viewer.followGPSTrack();
						if(ml.display.window.setupNavigationWindow)
						{
							ml.display.window.optTimeline.setSelected(false);
							ml.display.window.optGPSTrack.setSelected(true);
							ml.display.window.optMemory.setSelected(false);
						}
					}

				if (!optionKey && key == '>')
				{
					if(!ml.world.viewer.isFollowing())
					{
						ml.world.viewer.followTimeline(true, false);
						if(ml.display.window.setupNavigationWindow)
						{
							ml.display.window.optTimeline.setSelected(false);
							ml.display.window.optGPSTrack.setSelected(false);
							ml.display.window.optMemory.setSelected(true);
						}
					}
				}
				
				if (key == 'i') 		// Go to next cluster ID with image
					ml.world.viewer.moveToNextCluster(ml.world.viewer.getMovementTeleport(), 0);

				if (key == 'p') 		// Go to next cluster ID with panorama
					ml.world.viewer.moveToNextCluster(ml.world.viewer.getMovementTeleport(), 1);

				if (key == 'v') 		// Go to to next cluster ID with video
					ml.world.viewer.moveToNextCluster(ml.world.viewer.getMovementTeleport(), 2);

				if (key == 'u') 		// Go to to next cluster ID with sound
					ml.world.viewer.moveToNextCluster(ml.world.viewer.getMovementTeleport(), 3);

				if (key == 'm') 
					ml.world.viewer.moveToNearestCluster(ml.world.viewer.getMovementTeleport());

				if (key == '_') 
					ml.world.getCurrentField().fadeObjectDistances(0.85f);

				if (key == '+')
					ml.world.getCurrentField().fadeObjectDistances(1.176f);

//				if (key == 'Z')
//					ml.display.map2D.zoomToRectangle(100, 50, ml.display.map2D.largeMapWidth * 0.5f, ml.display.map2D.largeMapHeight * 0.5f);

				/* 3D Controls Disabled in HUD View */
				if(!ml.display.inDisplayView())							
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
						ml.world.viewer.startZoomTransition(-1);

					if (key == 'z') 
						ml.world.viewer.startZoomTransition(1);

					if (optionKey && key == 'e')
					{
						boolean state = !ml.world.getState().useBlurMasks;
						ml.world.getState().useBlurMasks = state;
						if(ml.display.window.setupGraphicsWindow)
						{
							ml.display.window.chkbxFadeEdges.setSelected(state);
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

					if (optionKey && key == 'p')
					{
						boolean state = !ml.world.getState().alphaMode;
						ml.world.state.alphaMode = state;
						if(ml.display.window.setupGraphicsWindow)
							ml.display.window.chkbxAlphaMode.setSelected(state);
					}

					if (!shiftKey && optionKey && key == ' ') 
					{
						boolean state = !ml.world.getState().timeFading;
						ml.world.state.timeFading = state;
						if(ml.display.window.setupGraphicsWindow)
						{
							ml.display.window.chkbxTimeFading.setSelected(state);
						}
					}

					if (key == ')') {		// -- Obsolete?
						float newAlpha = PApplet.constrain(ml.world.getState().alpha+15.f, 0.f, 255.f);
						ml.world.fadeAlpha(newAlpha);
					}

					if (key == '(') {
						float newAlpha = PApplet.constrain(ml.world.getState().alpha-15.f, 0.f, 255.f);
						ml.world.fadeAlpha(newAlpha);
					}

					if (key == ':')
					{
						ml.world.settings.showUserPanoramas = !ml.world.settings.showUserPanoramas;
					}

					if (key == ';')
					{
						ml.world.settings.showStitchedPanoramas = !ml.world.settings.showStitchedPanoramas;
					}

					if (key == 'A') 
					{
						ml.world.viewer.setSelection( !ml.world.viewer.getSelection() );
						if(ml.display.window.setupSelectionWindow)
							ml.display.window.chkbxSelectionMode.setSelected(ml.world.viewer.getSettings().selection);

						if(ml.world.viewer.getSelection() && ml.world.viewer.getMultiSelection())
						{
							ml.world.viewer.setMultiSelection( false );
							if(ml.display.window.setupSelectionWindow)
								ml.display.window.chkbxMultiSelection.setSelected( false );
						}
						if(ml.world.viewer.getSelection() && ml.world.viewer.getSegmentSelection()) 
						{
							ml.world.viewer.setSegmentSelection( false );
							if(ml.display.window.setupSelectionWindow)
								ml.display.window.chkbxSegmentSelection.setSelected( false );
						}
					}

					if (optionKey && key == 'x')
						ml.world.getCurrentField().deselectAllMedia(false);

					if (optionKey && key == '-')
						ml.world.viewer.setVisibleAngle( ml.world.viewer.getVisibleAngle() - 3.1415f / 128.f ); 

					if (optionKey && key == '=')
						ml.world.viewer.setVisibleAngle( ml.world.viewer.getVisibleAngle() + 3.1415f / 128.f ); 

					/* Selection */
					if (!optionKey && key == 'x') 
						ml.world.viewer.chooseMediaInFront(true);

					if (!optionKey && key == 'X')
						ml.world.viewer.chooseMediaInFront(false);

					if (key == 'S')
					{
						ml.world.viewer.setMultiSelection( !ml.world.viewer.getMultiSelection() );
						if(ml.world.viewer.getMultiSelection() && !ml.world.viewer.getSelection())
							ml.world.viewer.setSelection( true );
					}

					if (optionKey && key == 's')
					{
						ml.world.viewer.setSegmentSelection( !ml.world.viewer.getSegmentSelection() );
						if(ml.world.viewer.getSegmentSelection() && !ml.world.viewer.getSelection())
							ml.world.viewer.setSelection( true );
					}

					/* GPS */
					if (!optionKey && key == 'g') 
						ml.world.viewer.importGPSTrack();				// Select a GPS tracking file from disk to load and navigate 

					/* Memory */
					if (key == '`') 
						ml.world.viewer.addPlaceToMemory();

					if (key == 'Y') 
						ml.world.viewer.clearMemory();

					/* Graphics */
					if (key == 'G')
					{
						boolean state = !ml.world.viewer.getAngleFading();
						ml.world.viewer.setAngleFading( state );
						if(ml.display.window.setupGraphicsWindow)
							ml.display.window.chkbxAngleFading.setSelected(state);
					}

//					if (key == 'H')
//					{
//						boolean state = !ml.world.viewer.getAngleThinning();
//						ml.world.viewer.setAngleThinning( state );
//						if(ml.display.window.setupGraphicsWindow)
//							ml.display.window.chkbxAngleThinning.setSelected(state);
//					}

					/* Output */
					if (key == 'O') 
						ml.selectFolder("Select an output folder:", "outputFolderSelected");

					if (key == 'o') 	// Save image to disk
					{	
						if(!ml.world.outputFolderSelected) ml.selectFolder("Select an output folder:", "outputFolderSelected");
//						if(ml.state.sphericalView)
//							ml.world.saveCubeMapToDisk();
//						else
						ml.world.saveToDisk();
					}

//					if (key == '&') 
//					{
//						if(ml.world.settings.defaultMediaLength > 10)
//							ml.world.settings.defaultMediaLength -= 10;
//					}

//					if (key == '*') 			// Look for images when none are visible
//					{
//						if(ml.world.settings.defaultMediaLength < 990)
//							ml.world.settings.defaultMediaLength += 10;
//					}
				}
			}
			else 						// Interactive Clustering Mode
			{
				if (!optionKey && key == 'h')
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

				if (!optionKey && key == 'k')
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
							ml.world.getCurrentField().mergeAllAdjacentClusters();							/* Mark clusters with no media as empty */
							ml.display.map2D.initializeMaps(ml.world);
						}
					}
				}

				if (key == ']') 	
				{
					if(ml.world.settings.minClusterDistance < ml.world.settings.maxClusterDistance - 2.f)
					{
						ml.world.settings.minClusterDistance += 0.25f;
						//					System.out.println("ml.world.minClusterDistance:"+ml.world.minClusterDistance);
						for(WMV_Field f : ml.world.getFields())
						{
							f.getModel().setMinClusterDistance(ml.world.settings.minClusterDistance);
							ml.world.getCurrentField().runKMeansClustering( ml.world.settings.kMeansClusteringEpsilon, ml.world.getCurrentField().getModel().getState().clusterRefinement, ml.world.getCurrentField().getModel().getState().clusterPopulationFactor );
							ml.world.getCurrentField().mergeAllAdjacentClusters();							/* Mark clusters with no media as empty */
							ml.world.getCurrentField().finishClusterSetup();			
							ml.display.map2D.initializeMaps(ml.world);
						}
					}
				}
			}

			/* Arrow and Shift Keys */
			if (key == PApplet.CODED) 					
			{
				if(ml.display.inDisplayView())
				{
					if(ml.state.interactive)			/* Interactive Clustering */
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
									ml.world.getCurrentField().mergeAllAdjacentClusters();						
									ml.world.getCurrentField().finishClusterSetup();			
									ml.display.map2D.initializeMaps(ml.world);
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
									ml.world.getCurrentField().mergeAllAdjacentClusters();						
									ml.world.getCurrentField().finishClusterSetup();			
									ml.display.map2D.initializeMaps(ml.world);
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
									ml.world.getCurrentField().mergeAllAdjacentClusters();						
									ml.world.getCurrentField().finishClusterSetup();			
									ml.display.map2D.initializeMaps(ml.world);
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
									ml.world.getCurrentField().mergeAllAdjacentClusters();						
									ml.world.getCurrentField().finishClusterSetup();			
									ml.display.map2D.initializeMaps(ml.world);
								}
								else ml.world.getCurrentField().getModel().state.clusterPopulationFactor -= 1.f;
							}
						}
					}
				}
				else
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
					if (shiftKey && keyCode == PApplet.UP) 
						ml.world.incrementTime();

					if (shiftKey && keyCode == PApplet.DOWN) 
						ml.world.decrementTime();

					if (shiftKey && keyCode == PApplet.LEFT) 
						ml.world.decrementCycleLength();

					if (shiftKey && keyCode == PApplet.RIGHT) 
						ml.world.incrementCycleLength();
				}

				if (keyCode == PApplet.SHIFT) {
					shiftKey = true;
				}

				if (keyCode == PApplet.ALT) 
					optionKey = true;
				
//				if (keyCode == COMMAND_KEY) 
//				{
//					commandKey = true;
//					System.out.println("+ commandKey set to "+commandKey + " on frame:"+frameCount);
//				}
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
		if(display.displayView == 0)
		{
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
		}
		else if( display.displayView == 1 || (display.displayView == 2 && display.libraryViewMode != 2) )
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
			else if( display.displayView == 1 || (display.displayView == 2 && display.libraryViewMode != 2) )
			{
				if (keyCode == PApplet.LEFT) 
					viewer.stopRotateXTransition();
				if (keyCode == PApplet.RIGHT) 
					viewer.stopRotateXTransition();
				if (keyCode == PApplet.UP) 
					display.map2D.stopZooming();
				if (keyCode == PApplet.DOWN) 
					display.map2D.stopZooming();
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
			}
			
			if (keyCode == PApplet.SHIFT) 
				shiftKey = false;
			if (keyCode == PApplet.ALT) 
				optionKey = false;
//			if (keyCode == COMMAND_KEY)						// -- Obsolete
//				commandKey = false;
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
//		System.out.println("mouseReleased");
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
		
		if(display.displayView == 1 || (display.displayView == 2 && display.libraryViewMode == 0))
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