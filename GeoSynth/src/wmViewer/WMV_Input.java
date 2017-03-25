package wmViewer;
import java.awt.Toolkit;

import g4p_controls.GButton;
import g4p_controls.GCheckbox;
import g4p_controls.GEvent;
import g4p_controls.GToggleControl;
import g4p_controls.GValueControl;
import processing.core.*;
import processing.data.IntList;

/**************************************
 * WMV_Input
 * @author davidgordon
 * Methods for responding to user input from keyboard or mouse
 */

public class WMV_Input
{
	public boolean wasTimeFading;
	public boolean shiftKey = false;
	public boolean optionKey = false;
	public boolean commandKey = false;
	final public int COMMAND_KEY = 157;
	
	private int mouseClickedX = 0, mouseClickedY = 0;
//	private int mouseOffsetX = 0, mouseOffsetY = 0;
	
	private boolean mouseClickedRecently = false;
	private boolean mouseReleased = false;
	private int clickedRecentlyFrame = 1000000;
	private int doubleClickSpeed = 10;

	WMV_World p;

	WMV_Input(WMV_World parent) {
		p = parent;
		wasTimeFading = p.timeFading;
	}

	public void handleSliderEvent(GValueControl slider, GEvent event)
	{
		if(p.display.window.setupGraphicsWindow)
		{
			if (slider.tag == "Alpha") 
			{
				p.alpha = slider.getValueF();
			}

			if (slider.tag == "Brightness") 
			{
				p.viewer.settings.userBrightness = slider.getValueF();
			}
		}
		
		if(p.display.window.setupTimeWindow)
		{
			if (slider.tag == "MediaLength") 
			{
				p.settings.defaultMediaLength = slider.getValueI();
			}
			
			if (slider.tag == "TimeCycleLength") 
			{
				p.settings.timeCycleLength = slider.getValueI();
			}
		}
		
		if (slider.tag == "AltitudeScaling") 
		{
			if(p.display.window.setupModelWindow)
			{
//				PApplet.println("altitudeScalingFactor before:"+p.altitudeScalingFactor);
				p.settings.altitudeScalingFactor = PApplet.round(slider.getValueF() * 1000.f) * 0.001f;
//				PApplet.println("altitudeScalingFactor after:"+p.altitudeScalingFactor);
				p.getCurrentField().calculateMediaLocations();		// Recalculate media locations
				p.getCurrentField().recalculateGeometries();		// Recalculate media geometries at new locations
				p.getCurrentField().createClusters();				// Recalculate cluster locations
			}
		}
	}

	public void handleButtonEvent(GButton button, GEvent event) 
	{ 
		switch(button.tag) 
		{
			case "Restart":
				p.p.restart();
				break;
	
			/* Navigation */
			case "OpenNavigationWindow":
				p.display.window.openNavigationWindow();
				break;

			case "CloseNavigationWindow":
				p.display.window.hideNavigationWindow();
				break;
	
			case "NearestCluster":
				p.viewer.moveToNearestCluster(p.viewer.movementTeleport);
				break;
			case "RandomCluster":
				p.viewer.moveToRandomCluster(p.viewer.movementTeleport);
				break;
			case "LastCluster":
				p.viewer.moveToLastCluster(p.viewer.movementTeleport);
				break;
			case "NextField":
				if(p.display.displayView == 1)
					p.viewer.teleportToField(1, false);
				else
					p.viewer.teleportToField(1, true);
				break;
			case "PreviousField":
				if(p.display.displayView == 1)
					p.viewer.teleportToField(-1, false);
				else
					p.viewer.teleportToField(-1, true);
				break;
			case "ImportGPSTrack":
				p.viewer.importGPSTrack();						// Select a GPS tracking file from disk to load and navigate 
				break;
	
			case "FollowStart":
				if(!p.viewer.isFollowing())
				{
					switch(p.viewer.followMode)
					{
					case 0:
						p.viewer.followTimeline(true, false);
						break;
					case 1:
						p.viewer.followGPSTrack();
						break;
					case 2:
						p.viewer.followMemory();
						break;
					}
				}
				break;
			case "FollowStop":
				p.viewer.stopFollowing();
				break;
				/* Model */
			case "SubjectDistanceDown":
				p.getCurrentField().fadeObjectDistances(0.85f);
				break;
	
			case "SubjectDistanceUp":
				p.getCurrentField().fadeObjectDistances(1.176f);
				break;
				
				/* Help */
			case "OpenHelpWindow":
				p.display.window.openHelpWindow();
				break;
				
			case "CloseHelpWindow":
				p.display.window.hideHelpWindow();
//				p.display.window.helpWindow.setVisible(false);
				break;
				
				/* Memory */
			case "OpenMemoryWindow":
				p.display.window.openMemoryWindow();
				break;
				
			case "CloseMemoryWindow":
				p.display.window.hideMemoryWindow();
//				p.display.window.memoryWindow.setVisible(false);
				break;
				
				/* Statistics */
			case "OpenStatisticsWindow":
				p.display.window.openStatisticsWindow();
				break;
				
			case "CloseStatisticsWindow":
				p.display.window.hideStatisticsWindow();
//				p.display.window.statisticsWindow.setVisible(false);
				break;

				/* Time */
			case "OpenTimeWindow":
				p.display.window.openTimeWindow();
				break;
				
			case "CloseTimeWindow":
				p.display.window.hideTimeWindow();
//				p.display.window.graphicsWindow.setVisible(false);
				break;
				
				/* Graphics */
			case "OpenGraphicsWindow":
				p.display.window.openGraphicsWindow();
				break;
				
			case "CloseGraphicsWindow":
				p.display.window.hideGraphicsWindow();
//				p.display.window.graphicsWindow.setVisible(false);
				break;

			case "ZoomIn":
				p.viewer.startZoomTransition(-1);
				break;
			case "ZoomOut":
				p.viewer.startZoomTransition(1);
				break;
	
				/* Model */
			case "OpenModelWindow":
				p.display.window.openModelWindow();
				break;
				
			case "CloseModelWindow":
				p.display.window.modelWindow.setVisible(false);
				break;
				
			/* Time */
			case "NextTime":
				p.viewer.moveToNextTimeSegment(true, p.viewer.movementTeleport, true);
				break;
			case "PreviousTime":
				p.viewer.moveToPreviousTimeSegment(true, p.viewer.movementTeleport, true);
				break;
	
			/* Selection */
			case "OpenSelectionWindow":
				p.display.window.openSelectionWindow();
				p.viewer.settings.selection = true;
				p.display.window.chkbxSelectionMode.setSelected(true);
				break;

			case "CloseSelectionWindow":
				p.display.window.selectionWindow.setVisible(false);
				break;

			case "SelectFront":
				p.viewer.chooseMediaInFront(true);
				break;
				
			case "DeselectFront":
				p.viewer.chooseMediaInFront(false);	
				break;
					
			case "DeselectAll":
				p.getCurrentField().deselectAllMedia(false);
				break;
			
			case "StitchPanorama":
				p.getCurrentCluster().stitchImages();    			
				break;
				
				/* Memory */
			case "SaveLocation":
				p.viewer.addPlaceToMemory();
				break;
			case "ClearMemory":
				p.viewer.clearMemory();
				break;
	
				/* Output */
			case "ExportImage":
				if(!p.outputFolderSelected) p.p.selectFolder("Select an output folder:", "outputFolderSelected");
				p.saveToDisk();
				break;
			case "OutputFolder":
				p.p.selectFolder("Select an output folder:", "outputFolderSelected");
				break;
		}
	}

	/**
	 * Handles checkboxes
	 * @param option 
	 * @param event
	 */
	public void handleToggleControlEvent(GToggleControl option, GEvent event) 
	{
		switch (option.tag)
		{
			/* Views */
			case "SceneView":
				p.display.setDisplayView(0);
				break;
			case "MapView":
				p.display.setDisplayView(1);
				break;
			case "ClusterView":
				p.display.setDisplayView(2);
				break;
			case "TimelineView":
				p.display.setDisplayView(3);
				break;
				
			/* Navigation */
			case "FollowTimeline":
				if(option.isSelected())
				{
					p.viewer.followMode = 0;
					p.display.window.optGPSTrack.setSelected(false);
					p.display.window.optMemory.setSelected(false);
				}
				break;
	  		case "FollowGPSTrack":
				if(option.isSelected())
				{
					p.viewer.followMode = 1;
//					PApplet.println("1 p.display.window.optTimeline.isSelected():"+p.display.window.optTimeline.isSelected());
//					PApplet.println("1 p.display.window.optMemory.isSelected():"+p.display.window.optMemory.isSelected());
					p.display.window.optTimeline.setSelected(false);
					p.display.window.optMemory.setSelected(false);
//					PApplet.println("2 p.display.window.optTimeline.isSelected():"+p.display.window.optTimeline.isSelected());
//					PApplet.println("2 p.display.window.optMemory.isSelected():"+p.display.window.optMemory.isSelected());
				}
				break;
	  		case "FollowMemory":
				if(option.isSelected())
				{
					p.viewer.followMode = 2;
					p.display.window.optTimeline.setSelected(false);
					p.display.window.optGPSTrack.setSelected(false);
				}
				break;
			case "MovementTeleport":
				p.viewer.movementTeleport = option.isSelected();
				if(!p.viewer.movementTeleport)
					p.viewer.stopFollowing();
				break;
				
			case "FollowTeleport":
				//--need to implement
				break;
				
			/* Time */
			case "ClusterTimeMode":
				p.setTimeMode(0);
				break;
			case "FieldTimeMode":
				p.setTimeMode(1);
				break;
			case "MediaTimeMode":
				p.setTimeMode(2);
				break;
			/* Graphics */
			case "TimeFading":
				p.timeFading = option.isSelected();
				break;
			case "FadeEdges":
				p.fadeEdges = option.isSelected();
				break;
			case "HideImages":
				if(!option.isSelected() && p.getCurrentField().hideImages)
					p.getCurrentField().showImages();
				else if(option.isSelected() && !p.getCurrentField().hideImages)
					p.getCurrentField().hideImages();
				break;
			case "HideVideos":
				if(!option.isSelected() && p.getCurrentField().hideVideos)
					p.getCurrentField().showVideos();
				else if(option.isSelected() && !p.getCurrentField().hideVideos)
					p.getCurrentField().hideVideos();
				break;
			case "HidePanoramas":
				if(!option.isSelected() && p.getCurrentField().hidePanoramas)
					p.getCurrentField().showPanoramas();
				else if(option.isSelected() && !p.getCurrentField().hidePanoramas)
					p.getCurrentField().hidePanoramas();
				break;
			case "AlphaMode":
				p.alphaMode = option.isSelected();
				break;
			case "OrientationMode":
				p.viewer.settings.orientationMode = !p.viewer.settings.orientationMode;
				break;
			case "AngleFading":
				p.viewer.settings.angleFading = option.isSelected();
				break;
			case "AngleThinning":
				p.viewer.settings.angleThinning = option.isSelected();
				break;
				
			/* Model */
			case "ShowModel":
				p.showModel = option.isSelected();
				break;
			case "MediaToCluster":
				p.showMediaToCluster = option.isSelected();
				break;
			case "CaptureToMedia":
				p.showCaptureToMedia = option.isSelected();
				break;
			case "CaptureToCluster":
				p.showCaptureToCluster = option.isSelected();
				break;
				
			/* Selection */
			case "SelectionMode":
				p.viewer.settings.selection = option.isSelected();
				break;
				
			case "MultiSelection":
				p.viewer.settings.multiSelection = option.isSelected();
				break;
					
			case "SegmentSelection":
				p.viewer.settings.segmentSelection = option.isSelected();
				break;
				
			case "ViewMetadata":
				p.showMetadata = option.isSelected();
				break;
		}
	}

	/**
	 * @param key Key that was pressed
	 * Respond to user key presses
	 */
	void handleKeyPressed(char key, int keyCode)
	{
//		PApplet.println("handleKeyPressed keyCode: "+keyCode + " on frame:"+p.p.frameCount);
		if (!p.p.state.running && !p.p.state.selectedLibrary)
		{
			p.p.state.openLibraryDialog = true;
		}
		else
		{
			/* General */
			if (key == ' ') 
			{
				if(p.interactive)
				{
					p.finishInteractiveClustering();			// Restart simulation after interative clustering
				}
			}

			if (key == ' ') 
			{
				if(!p.p.basic)
				{
					if(p.display.window.showWMVWindow)
						p.display.window.hideWMVWindow();
					else
						p.display.window.showWMVWindow();
				}
			}

			/* Display Modes */
			if (!optionKey && !commandKey && key == '1') 
			{
				if(!p.display.initializedMaps)
					p.display.map2D.initializeMaps();

				p.display.setDisplayView(0);
			}

			if (!optionKey && !commandKey && key == '2') 
				p.display.setDisplayView(1);

			if (!optionKey && !commandKey && key == '3') 
				p.display.setDisplayView(2);

			if (!optionKey && !commandKey  && key == '4')
				p.display.setDisplayView(3);

			if (!optionKey && !commandKey  && key == '5')
			{
				boolean state = !p.showModel;
				p.showModel = state;
				if(p.display.window.setupModelWindow)
					p.display.window.chkbxShowModel.setSelected(state);
			}

			if (!optionKey && !commandKey  && key == '6') 
				p.showMediaToCluster = !p.showMediaToCluster;			// Draw line from each media point to cluster

			if (!optionKey && !commandKey  && key == '7') 
				p.showCaptureToMedia = !p.showCaptureToMedia;			// Draw line from each media point to its capture location

			if (!optionKey && !commandKey  && key == '8') 
				p.showCaptureToCluster = !p.showCaptureToCluster;		// Draw line from each media capture location to associated cluster

//			if (!optionKey && !commandKey  && key == '8') 
//			{
//				if(p.viewer.settings.maxVisibleClusters > 1)
//					p.viewer.settings.maxVisibleClusters--;		
//			}
//
//			if (!optionKey && !commandKey  && key == '9') 
//			{
//				if(p.viewer.settings.maxVisibleClusters < 9)
//					p.viewer.settings.maxVisibleClusters++;		
//			}

			if (!optionKey && commandKey && key == '1') 
			{
				if(!p.display.window.showNavigationWindow)
					p.display.window.openNavigationWindow();
				else
					p.display.window.hideNavigationWindow();
				commandKey = false;
			}

			if (!optionKey && commandKey && key == '2') 
			{
				if(!p.display.window.showTimeWindow)
					p.display.window.openTimeWindow();
				else
					p.display.window.hideTimeWindow();
				commandKey = false;
			}

			if (!optionKey && commandKey && key == '3') 
			{
				if(!p.display.window.showGraphicsWindow)
					p.display.window.openGraphicsWindow();
				else
					p.display.window.hideGraphicsWindow();
				commandKey = false;
			}

			if (!optionKey && commandKey && key == '4') 
			{
				if(!p.display.window.showModelWindow)
					p.display.window.openModelWindow();
				else
					p.display.window.hideModelWindow();
				commandKey = false;
			}

			if (!optionKey && commandKey && key == '5') 
			{
				if(!p.display.window.showMemoryWindow)
					p.display.window.openMemoryWindow();
				else
					p.display.window.hideMemoryWindow();
				commandKey = false;
			}

			if (!optionKey && commandKey && key == '6') 
			{
				if(!p.display.window.showSelectionWindow)
					p.display.window.openSelectionWindow();
				else
					p.display.window.hideSelectionWindow();
				commandKey = false;
			}

			if (!optionKey && commandKey && key == '7') 
			{
				if(!p.display.window.showStatisticsWindow)
					p.display.window.openStatisticsWindow();
				else
					p.display.window.hideStatisticsWindow();
				commandKey = false;
			}
			
			if (!optionKey && commandKey && key == '8') 
			{
				if(!p.display.window.showHelpWindow)
					p.display.window.openHelpWindow();
				else
					p.display.window.hideHelpWindow();
				commandKey = false;
			}

			if (key == 'R')
				p.p.restart();

			if(p.display.displayView == 1)	/* 2D Map View */
			{
				if( key == '+' )
					p.display.satelliteMap = !p.display.satelliteMap;

				if (key == '{')
					p.viewer.teleportToField(-1, false);

				if (key == '}') 
					p.viewer.teleportToField(1, false);

				/* Clustering */
				if (key == 'r')
				{
					p.display.map2D.resetMapZoom(true);
				}

				if (key == 'c')
				{
					p.display.map2D.resetMapZoom(true);
					p.startInteractiveClustering();
				}

				if (key == 'z')
					p.display.map2D.zoomToCluster(p.getCurrentCluster());

				if (shiftKey && key == 'c')
					p.startInitialClustering();				// Re-run clustering on all fields

				if (key == ']') {
//					p.display.map2D.mapZoomTransition(0.85f);
					p.display.map2D.zoomIn();
				}

				if (key == '[') {
					p.display.map2D.zoomOut();
//					p.display.map2D.mapZoomTransition(1.176f);
				}

				if (key == PApplet.CODED) 					
				{
					if (keyCode == PApplet.LEFT)
						p.viewer.rotateX(-1);

					if (keyCode == PApplet.RIGHT) 
						p.viewer.rotateX(1);

					if (optionKey && shiftKey && keyCode == PApplet.LEFT) 
						p.display.map2D.mapScrollTransition( 150.f * p.display.map2D.mapDistance, 0.f );

					if (optionKey && shiftKey && keyCode == PApplet.RIGHT) 
						p.display.map2D.mapScrollTransition( -150.f * p.display.map2D.mapDistance, 0.f );

					if (optionKey && shiftKey && keyCode == PApplet.DOWN) 
						p.display.map2D.mapScrollTransition( 0.f, -150.f * p.display.map2D.mapDistance );

					if (optionKey && shiftKey && keyCode == PApplet.UP) 
						p.display.map2D.mapScrollTransition( 0.f, 150.f * p.display.map2D.mapDistance );

					if (!optionKey && shiftKey && keyCode == PApplet.LEFT) 
						p.display.map2D.zoomRectangleScrollTransition( -400.f * p.display.map2D.mapDistance, 0.f );

					if (!optionKey && shiftKey && keyCode == PApplet.RIGHT) 
						p.display.map2D.zoomRectangleScrollTransition( 400.f * p.display.map2D.mapDistance, 0.f );

					if (!optionKey && shiftKey && keyCode == PApplet.DOWN) 
						p.display.map2D.zoomRectangleScrollTransition( 0.f, 400.f * p.display.map2D.mapDistance );

					if (!optionKey && shiftKey && keyCode == PApplet.UP) 
						p.display.map2D.zoomRectangleScrollTransition( 0.f, -400.f * p.display.map2D.mapDistance );
				}

				if(key == PApplet.ENTER)
				{
					if(shiftKey)
					{
						p.viewer.teleportToCluster(p.display.map2D.getSelectedClusterID(), false, -1);
					}
					else
					{
						p.viewer.teleportToCluster(p.display.map2D.getSelectedClusterID(), true, -1);
						p.display.displayView = 0;
					}
				}
			}
			else if(p.display.displayView == 2)		/* Cluster View */
			{
				if (key == PApplet.CODED) 					
				{
					if (keyCode == PApplet.LEFT) 
					{
						p.display.displayCluster--;
						if(p.display.displayCluster < 0)
							p.display.displayCluster = p.getFieldClusters().size() - 1;

						int count = 0;
						while(p.getCluster(p.display.displayCluster).isEmpty())
						{
							p.display.displayCluster--;
							count++;
							if(p.display.displayCluster < 0)
								p.display.displayCluster = p.getFieldClusters().size() - 1;

							if(count > p.getFieldClusters().size())
								break;
						}
					}

					if (keyCode == PApplet.RIGHT) 
					{
						p.display.displayCluster++;
						if( p.display.displayCluster >= p.getFieldClusters().size())
							p.display.displayCluster = 0;

						int count = 0;
						while(p.getCluster(p.display.displayCluster).isEmpty())
						{
							p.display.displayCluster++;
							count++;
							if( p.display.displayCluster >= p.getFieldClusters().size())
								p.display.displayCluster = 0;

							if(count > p.getFieldClusters().size())
								break;
						}
					}
				}
			}
			else if(p.display.displayView == 3)					/* Time View */
			{
				if (key == 'r')									// Zoom out to whole timeline
				{
					p.display.resetZoom();
				}
				if (key == 'z')									// Timeline zoom to fit
				{
					p.display.zoomTimelineToFit();
				}
				if (key == 'a')									// Timeline zoom to fit
				{
					p.display.showAllDates();
				}
				if (key == PApplet.CODED) 					
				{
					if (keyCode == PApplet.UP) 					// Timeline zoom in 
						p.display.zoom(0.95f);
					
					if (keyCode == PApplet.DOWN) 				// Timeline zoom out
					{
						p.display.zoom(1.052f);
					}

					if (keyCode == PApplet.LEFT)  				// Timeline scroll left
					{
						p.display.scroll(-600.f);
					}

					if (keyCode == PApplet.RIGHT)  				// Timeline scroll right
					{
						p.display.scroll(600.f);
					}
				}
			}

			if (!p.interactive)		
			{
				/* 3D View Controls */
				if (key == '|')
					p.getCurrentCluster().stitchImages();    			

				if (optionKey && key == '[')
				{
					if(p.viewer.settings.thinningAngle > PApplet.PI / 64.f)
						p.viewer.settings.thinningAngle -= PApplet.PI / 128.f;
					p.getCurrentField().model.analyzeClusterMediaDirections();
				}

				if (optionKey && key == ']')
				{
					if(p.viewer.settings.thinningAngle < p.viewer.settings.visibleAngle - PApplet.PI / 128.f)
						p.viewer.settings.thinningAngle += PApplet.PI / 128.f;
					p.getCurrentField().model.analyzeClusterMediaDirections();
				}

				if (optionKey && key == '\\')
					p.getCurrentField().stitchAllClusters();		// Teleport to cluster with > 1 times

				if (!optionKey && key == 'a') 
					p.viewer.startMoveXTransition(-1);

				if (!optionKey && key == 'd') 
					p.viewer.startMoveXTransition(1);

				if( key == 'l' )
					p.viewer.moveToLastCluster(p.viewer.movementTeleport);

				if( key == 'L' )
					p.viewer.lookAtNearestMedia();

				if( key == 't' )
				{
					boolean state = !p.viewer.movementTeleport;
					p.viewer.movementTeleport = state;
					if(p.display.window.setupNavigationWindow)
						p.display.window.chkbxMovementTeleport.setSelected(state);
				}

				if (key == 'T') 
				{
					boolean state = !p.timeFading;
					if(p.timeFading != state)
					{
						p.timeFading = state;
						if(p.display.window.setupTimeWindow)
							p.display.window.chkbxTimeFading.setSelected(state);
					}
				}

				if (!optionKey && key == 's') 
					p.viewer.startMoveZTransition(1);

				if (!optionKey && key == 'w') 
					p.viewer.walkForward();

				if (optionKey && key == 'm') 
				{
					boolean state = !p.showMetadata;
					p.showMetadata = state;
					if(p.display.window.setupSelectionWindow)
						p.display.window.chkbxShowMetadata.setSelected(state);
				}

				if (key == 'Q')
					p.viewer.moveToNextCluster(false, -1);

				if (!optionKey && key == 'e')									// Move UP
					p.viewer.startMoveYTransition(-1);

				if (key == 'c') 									// Move DOWN
					p.viewer.startMoveYTransition(1);

				if (key == 'A') 								
				{
					p.paused = !p.paused;
					//				p.display.setFullScreen(!p.display.fullscreen);
				}
				//			if (key == 'J') 
				//				p.viewer.moveToRandomCluster(p.viewer.movementTeleport);					// Move to random cluster

				if (key == 'j') 
					p.viewer.moveToRandomCluster(p.viewer.movementTeleport);				// Jump (teleport) to random cluster

				if (key == 'I')
				{
					boolean state = !p.viewer.settings.orientationMode;
					p.viewer.settings.orientationMode = state;
					if(p.display.window.setupGraphicsWindow)
						p.display.window.chkbxOrientationMode.setSelected(state);
				}

				if (key == 'W') 
					p.viewer.moveToNearestClusterAhead(false);

				if (!optionKey && key == ']') {
					float value = p.settings.altitudeScalingFactor * 1.052f;
					p.settings.altitudeScalingFactor = PApplet.constrain(value, 0.f, 1.f);
					p.getCurrentField().calculateMediaLocations();		// Recalculate media locations
					p.getCurrentField().createClusters();				// Recalculate cluster locations
					p.getCurrentField().recalculateGeometries();				// Recalculate cluster locations
				}

				if (!optionKey && key == '[') {
					float value = p.settings.altitudeScalingFactor *= 0.95f;
					p.settings.altitudeScalingFactor = PApplet.constrain(value, 0.f, 1.f);
					p.getCurrentField().calculateMediaLocations();		// Recalculate media locations
					p.getCurrentField().createClusters();				// Recalculate cluster locations
					p.getCurrentField().recalculateGeometries();				// Recalculate cluster locations
				}

				if (key == 'n')						// Teleport to next time segment on same date
				{
					if(p.display.displayView == 0)
						p.viewer.moveToNextTimeSegment(true, p.viewer.movementTeleport, true);
					else
						p.viewer.moveToNextTimeSegment(true, true, false);
				}

				if (key == 'b')						// Teleport to previous time segment on same date
				{
					if(p.display.displayView == 0)
						p.viewer.moveToPreviousTimeSegment(true, p.viewer.movementTeleport, true);
					else
						p.viewer.moveToPreviousTimeSegment(true, true, false);
				}

				if (key == 'N')						// Teleport to next time segment on any date
				{
					if(p.display.displayView == 0)
						p.viewer.moveToNextTimeSegment(false, p.viewer.movementTeleport, true);
					else
						p.viewer.moveToNextTimeSegment(false, true, false);
				}

				if (key == 'B')						// Teleport to previous time segment on any date
				{
					if(p.display.displayView == 0)
						p.viewer.moveToPreviousTimeSegment(false, p.viewer.movementTeleport, true);
					else
						p.viewer.moveToPreviousTimeSegment(false, true, false);
				}

				if (key == '~')
					if(!p.viewer.isFollowing())
					{
						p.viewer.followMemory();
						if(p.display.window.setupNavigationWindow)
						{
							p.display.window.optTimeline.setSelected(true);
							p.display.window.optGPSTrack.setSelected(false);
							p.display.window.optMemory.setSelected(false);
						}
					}

				if (optionKey && key == 'g')
					if(!p.viewer.isFollowing())
					{
						p.viewer.followGPSTrack();
						if(p.display.window.setupNavigationWindow)
						{
							p.display.window.optTimeline.setSelected(false);
							p.display.window.optGPSTrack.setSelected(true);
							p.display.window.optMemory.setSelected(false);
						}
					}

				if (!optionKey && key == '>')
				{
					if(!p.viewer.isFollowing())
					{
						p.viewer.followTimeline(true, false);
						if(p.display.window.setupNavigationWindow)
						{
							p.display.window.optTimeline.setSelected(false);
							p.display.window.optGPSTrack.setSelected(false);
							p.display.window.optMemory.setSelected(true);
						}
					}
				}

				if (key == 'u') 		// Teleport to nearest cluster with video
					p.viewer.moveToNextCluster(true, 2);

				if (key == 'U') 		// Go to nearest cluster with video
					p.viewer.moveToNextCluster(false, 2);

//				if (key == 'm') 		// Teleport to nearest cluster with panorama
//					p.viewer.moveToNextCluster(true, 1);
//
//				if (key == 'M') 		// Go to nearest cluster with panorama
//					p.viewer.moveToNextCluster(false, 1);

				if (key == 'm') 
					p.viewer.moveToNearestCluster(true);

				if (key == 'M') 
					p.viewer.moveToNearestCluster(false);

				if (key == '-') 
					p.getCurrentField().fadeObjectDistances(0.85f);

				if (key == '=')
					p.getCurrentField().fadeObjectDistances(1.176f);

//				if (key == 'Z')
//					p.display.map2D.zoomToRectangle(100, 50, p.display.map2D.largeMapWidth * 0.5f, p.display.map2D.largeMapHeight * 0.5f);

				/* 3D Controls Disabled in HUD View */
				if(!p.display.inDisplayView())							
				{
					if (key == '{')
						p.viewer.teleportToField(-1, true);

					if (key == '}') 
						p.viewer.teleportToField(1, true);

					if (key == 'q') 
						p.viewer.startZoomTransition(-1);

					if (key == 'z') 
						p.viewer.startZoomTransition(1);

					if (optionKey && key == 'e')
					{
						boolean state = !p.fadeEdges;
						p.fadeEdges = state;
						if(p.display.window.setupGraphicsWindow)
						{
							p.display.window.chkbxFadeEdges.setSelected(state);
						}
					}

					if (key == 'i')	
					{
						if(p.getCurrentField().hideImages)
							p.getCurrentField().showImages();
						else
							p.getCurrentField().hideImages();
					}

					if (key == 'h')	
					{
						if(p.getCurrentField().hidePanoramas)
							p.getCurrentField().showPanoramas();
						else
							p.getCurrentField().hidePanoramas();
					}

					if (key == 'v')	
					{
						if(p.getCurrentField().hideVideos)
							p.getCurrentField().showVideos();
						else
							p.getCurrentField().hideVideos();
					}

					if (key == 'P')
					{
						boolean state = !p.alphaMode;
						p.alphaMode = state;
						if(p.display.window.setupGraphicsWindow)
							p.display.window.chkbxAlphaMode.setSelected(state);
					}

					if (!shiftKey && optionKey && key == ' ') 
					{
						boolean state = !p.timeFading;
						p.timeFading = state;
						if(p.display.window.setupGraphicsWindow)
						{
							p.display.window.chkbxTimeFading.setSelected(state);
						}
					}

					if (key == ')') {
						float newAlpha = PApplet.constrain(p.alpha+15.f, 0.f, 255.f);
						p.fadeAlpha(newAlpha);
					}

					if (key == '(') {
						float newAlpha = PApplet.constrain(p.alpha-15.f, 0.f, 255.f);
						p.fadeAlpha(newAlpha);
					}

					if (key == ':')
					{
						p.settings.showUserPanoramas = !p.settings.showUserPanoramas;
					}

					if (key == ';')
					{
						p.settings.showStitchedPanoramas = !p.settings.showStitchedPanoramas;
					}

					if (key == 'O') 
					{
						p.viewer.settings.selection = !p.viewer.settings.selection;
						if(p.display.window.setupSelectionWindow)
							p.display.window.chkbxSelectionMode.setSelected(p.viewer.settings.selection);

						if(p.viewer.settings.selection && p.viewer.settings.multiSelection)
						{
							p.viewer.settings.multiSelection = false;
							if(p.display.window.setupSelectionWindow)
								p.display.window.chkbxMultiSelection.setSelected(false);
						}
						if(p.viewer.settings.selection && p.viewer.settings.segmentSelection) 
						{
							p.viewer.settings.segmentSelection = false;
							if(p.display.window.setupSelectionWindow)
								p.display.window.chkbxSegmentSelection.setSelected(false);
						}
					}

					if (optionKey && key == 'x')
						p.getCurrentField().deselectAllMedia(false);

					if (optionKey && key == '-')
						p.viewer.settings.visibleAngle -= 3.1415f / 128.f; 

					if (optionKey && key == '=')
						p.viewer.settings.visibleAngle += 3.1415f / 128.f; 

					/* Selection */
					if (!optionKey && key == 'x') 
						p.viewer.chooseMediaInFront(true);

					if (!optionKey && key == 'X')
						p.viewer.chooseMediaInFront(false);

					if (key == 'S')
					{
						p.viewer.settings.multiSelection = !p.viewer.settings.multiSelection;
						if(p.viewer.settings.multiSelection && !p.viewer.settings.selection)
							p.viewer.settings.selection = true;
					}

					if (optionKey && key == 's')
					{
						p.viewer.settings.segmentSelection = !p.viewer.settings.segmentSelection;
						if(p.viewer.settings.segmentSelection && !p.viewer.settings.selection)
							p.viewer.settings.selection = true;
					}

					/* GPS */
					if (!optionKey && key == 'g') 
						p.viewer.importGPSTrack();				// Select a GPS tracking file from disk to load and navigate 

					/* Memory */
					if (key == '`') 
						p.viewer.addPlaceToMemory();

					if (key == 'Y') 
						p.viewer.clearMemory();

					/* Graphics */
					if (key == 'G')
					{
						boolean state = !p.viewer.settings.angleFading;
						p.viewer.settings.angleFading = state;
						if(p.display.window.setupGraphicsWindow)
						{
							p.display.window.chkbxAngleFading.setSelected(state);
						}
					}

					if (key == 'H')
					{
						boolean state = !p.viewer.settings.angleThinning;
						p.viewer.settings.angleThinning = state;
						if(p.display.window.setupGraphicsWindow)
						{
							p.display.window.chkbxAngleThinning.setSelected(state);
						}
					}

					/* Output */
					if (key == 'o') 
						p.p.selectFolder("Select an output folder:", "outputFolderSelected");

					if (key == 'p') 	// Save image to disk
					{	
						if(!p.outputFolderSelected) p.p.selectFolder("Select an output folder:", "outputFolderSelected");
						p.saveToDisk();
					}

					if (key == '&') 
					{
						if(p.p.world.settings.defaultMediaLength > 10)
							p.p.world.settings.defaultMediaLength -= 10;
					}

					if (key == '*') 			// Look for images when none are visible
					{
						if(p.p.world.settings.defaultMediaLength < 990)
							p.p.world.settings.defaultMediaLength += 10;
					}
				}
			}
			else 						// Interactive Clustering Mode
			{
				if (!optionKey && key == 'h')
				{
					if(!p.hierarchical)
					{
						p.hierarchical = true;
						if(!p.getCurrentField().model.dendrogramCreated)
							p.getCurrentField().model.runHierarchicalClustering();
						p.getCurrentField().model.setDendrogramDepth(p.getCurrentField().model.clusterDepth);				// Initialize clusters 
						p.getCurrentField().createTimeline();					// Create field timeline
					}

				}

				if (!optionKey && key == 'k')
				{
					if(p.hierarchical)
					{
						p.hierarchical = false;
						WMV_Model m = p.getCurrentField().model;
						m.runKMeansClustering(p.settings.kMeansClusteringEpsilon, m.clusterRefinement, m.clusterPopulationFactor);
						p.getCurrentField().createTimeline();					// Create field timeline
					}
				}

				if (key == '[') 	
				{
					if(!p.autoClusterDistances && p.settings.minClusterDistance > 0.25f)
					{
						p.settings.minClusterDistance -= 0.25f;
						for(WMV_Field f : p.getFields())
						{
							f.model.setMinClusterDistance(p.settings.minClusterDistance);	
							p.getCurrentField().model.runKMeansClustering( p.settings.kMeansClusteringEpsilon, p.getCurrentField().model.clusterRefinement, p.getCurrentField().model.clusterPopulationFactor );
							p.getCurrentField().initializeClusters();			
							p.display.map2D.initializeMaps();
						}
					}
				}

				if (key == ']') 	
				{
					if(!p.autoClusterDistances && p.settings.minClusterDistance < p.settings.maxClusterDistance - 2.f)
					{
						p.settings.minClusterDistance += 0.25f;
						//					PApplet.println("p.minClusterDistance:"+p.minClusterDistance);
						for(WMV_Field f : p.getFields())
						{
							f.model.setMinClusterDistance(p.settings.minClusterDistance);
							p.getCurrentField().model.runKMeansClustering( p.settings.kMeansClusteringEpsilon, p.getCurrentField().model.clusterRefinement, p.getCurrentField().model.clusterPopulationFactor );
							p.getCurrentField().initializeClusters();			
							p.display.map2D.initializeMaps();
						}
					}
				}
			}

			/* Arrow and Shift Keys */
			if (key == PApplet.CODED) 					
			{
				if(p.display.inDisplayView())
				{
					if(p.interactive)			/* Interactive Clustering */
					{
						if(p.hierarchical)
						{
							if (keyCode == PApplet.UP) 
							{
								int clusterDepth = p.getCurrentField().model.clusterDepth + 1;
								if(clusterDepth <= p.getCurrentField().model.deepestLevel)
									p.getCurrentField().model.setDendrogramDepth( clusterDepth );
							}

							if (keyCode == PApplet.DOWN) 
							{
								int clusterDepth = p.getCurrentField().model.clusterDepth - 1;
								if(clusterDepth >= p.getCurrentField().model.minClusterDepth)
									p.getCurrentField().model.setDendrogramDepth( clusterDepth );
							}
						}
						else
						{
							if (keyCode == PApplet.LEFT) 		
							{
								p.getCurrentField().model.clusterRefinement -= 10;
								float populationFactor = p.getCurrentField().model.clusterPopulationFactor;

								if(p.getCurrentField().model.clusterRefinement >= p.getCurrentField().model.minClusterRefinement)
								{
									p.getCurrentField().model.runKMeansClustering( p.settings.kMeansClusteringEpsilon, p.getCurrentField().model.clusterRefinement, populationFactor );
									p.getCurrentField().initializeClusters();			
									p.display.map2D.initializeMaps();
								}
								else p.getCurrentField().model.clusterRefinement += 10;
							}

							if (keyCode == PApplet.RIGHT) 	
							{
								p.getCurrentField().model.clusterRefinement += 10;
								float populationFactor = p.getCurrentField().model.clusterPopulationFactor;

								if(p.getCurrentField().model.clusterRefinement <= p.getCurrentField().model.maxClusterRefinement)
								{
									p.getCurrentField().model.runKMeansClustering( p.settings.kMeansClusteringEpsilon, p.getCurrentField().model.clusterRefinement, populationFactor );
									p.getCurrentField().initializeClusters();			
									p.display.map2D.initializeMaps();
								}
								else p.getCurrentField().model.clusterRefinement -= 10;
							}

							if (keyCode == PApplet.DOWN) 		
							{
								int refinementAmount = p.getCurrentField().model.clusterRefinement;
								p.getCurrentField().model.clusterPopulationFactor -= 1.f;

								if(p.getCurrentField().model.clusterPopulationFactor >= p.getCurrentField().model.minPopulationFactor)
								{
									p.getCurrentField().model.runKMeansClustering( p.settings.kMeansClusteringEpsilon, refinementAmount, p.getCurrentField().model.clusterPopulationFactor );
									p.getCurrentField().initializeClusters();			
									p.display.map2D.initializeMaps();
								}
								else p.getCurrentField().model.clusterPopulationFactor += 1.f;
							}

							if (keyCode == PApplet.UP) 	
							{
								int refinementAmount = p.getCurrentField().model.clusterRefinement;
								p.getCurrentField().model.clusterPopulationFactor += 1.f;

								if(p.getCurrentField().model.clusterPopulationFactor <= p.getCurrentField().model.maxPopulationFactor)
								{
									p.getCurrentField().model.runKMeansClustering( p.settings.kMeansClusteringEpsilon, refinementAmount, p.getCurrentField().model.clusterPopulationFactor );
									p.getCurrentField().initializeClusters();			
									p.display.map2D.initializeMaps();
								}
								else p.getCurrentField().model.clusterPopulationFactor -= 1.f;
							}
						}
					}
				}
				else
				{
					/* Navigation */
					if (keyCode == PApplet.LEFT) 
						p.viewer.rotateX(-1);

					if (keyCode == PApplet.RIGHT) 
						p.viewer.rotateX(1);

					if (keyCode == PApplet.UP) 
						p.viewer.rotateY(-1);

					if (keyCode == PApplet.DOWN) 
						p.viewer.rotateY(1);

					/* Time */
					if (shiftKey && keyCode == PApplet.UP) 
						p.incrementTime();

					if (shiftKey && keyCode == PApplet.DOWN) 
						p.decrementTime();

					if (shiftKey && keyCode == PApplet.LEFT) 
						p.decrementCycleLength();

					if (shiftKey && keyCode == PApplet.RIGHT) 
						p.incrementCycleLength();
				}

				if (keyCode == PApplet.SHIFT) {
					shiftKey = true;
				}

				if (keyCode == PApplet.ALT) 
					optionKey = true;
				
				if (keyCode == COMMAND_KEY) 
				{
					commandKey = true;
					PApplet.println("+ commandKey set to "+commandKey + " on frame:"+p.p.frameCount);
				}
			}
		}
	}
	
	/**
	 * Respond to user key releases
	 */
	void handleKeyReleased(char key, int keyCode)
	{
//		PApplet.println("handleKeyReleased keyCode: "+keyCode + " on frame:"+p.p.frameCount);

		/* Navigation */
		if (key == 'a') 
			p.viewer.stopMoveXTransition();
		if (key == 'd') 
			p.viewer.stopMoveXTransition();
		if (key == 's') 
			p.viewer.stopMoveZTransition();
		if (key == 'w') 
			p.viewer.stopMoveZTransition();
		if (key == 'e') 
			p.viewer.stopMoveYTransition();
		if (key == 'c') 
			p.viewer.stopMoveYTransition();

		/* Coded Keys */
		if (key == PApplet.CODED) 
		{
			if (keyCode == PApplet.LEFT) 
				p.viewer.stopRotateXTransition();
			if (keyCode == PApplet.RIGHT) 
				p.viewer.stopRotateXTransition();
			if (keyCode == PApplet.UP) 
				p.viewer.stopRotateYTransition();
			if (keyCode == PApplet.DOWN) 
				p.viewer.stopRotateYTransition();
			if (keyCode == PApplet.SHIFT) 
				shiftKey = false;
			if (keyCode == PApplet.ALT) 
				optionKey = false;
			if (keyCode == COMMAND_KEY)
			{
				commandKey = false;
//				PApplet.println("commandKey set to "+commandKey + " on frame:"+p.p.frameCount);
			}
		}
	}

	/* Mouse */
	void updateMouseSelection(int mouseX, int mouseY)
	{
//		mediaSelector
		
		if(p.p.frameCount - clickedRecentlyFrame > doubleClickSpeed && mouseClickedRecently)
		{
			mouseClickedRecently = false;
		}
		
		if(p.p.frameCount - clickedRecentlyFrame > 20 && !mouseReleased)
		{
//			PApplet.println("Held mouse...");
		}
	}
	
	void updateMouseNavigation(int mouseX, int mouseY)
	{			
		if(p.p.frameCount - clickedRecentlyFrame > doubleClickSpeed && mouseClickedRecently)
		{
			mouseClickedRecently = false;
//			mouseReleasedRecently = false;
	//			PApplet.println("SET CLICKED RECENTLY TO FALSE");
		}
		
		if(p.p.frameCount - clickedRecentlyFrame > 20 && !mouseReleased)
		{
//			PApplet.println("Held mouse...");
			p.viewer.addPlaceToMemory();
		}
		
//		PApplet.println("mouseX:"+mouseX+" mouseY:"+mouseY+" p.p.width * 0.25:"+(p.p.width * 0.25));
			
		if (mouseX < p.p.width * 0.25 && mouseX > -1) 
		{
//			PApplet.println("LEFT p.viewer.getXOrientation():"+p.viewer.getXOrientation());
			if(!p.viewer.turningX)
			{
				p.viewer.turnXToAngle(PApplet.radians(5.f), -1);
//				p.viewer.turnXStartFrame = p.p.frameCount;
//				p.viewer.turnXDirection = -1;
//				p.viewer.turnXTarget = p.viewer.getXOrientation() - PApplet.radians(15.f);
//				p.viewer.turnXTargetFrame = p.viewer.turnXStartFrame + 30;
//				p.viewer.turningX = true;
//				p.viewer.lastMovementFrame = p.p.frameCount;
			}
		}
		else if (mouseX > p.p.width * 0.75 && mouseX < p.p.width + 1) 
		{
			if(!p.viewer.turningX)
			{
				p.viewer.turnXToAngle(PApplet.radians(5.f), 1);

//				p.viewer.turnXStartFrame = p.p.frameCount;
//				p.viewer.turnXDirection = 1;
//				p.viewer.turnXTarget = p.viewer.getXOrientation() + PApplet.radians(15.f);
//				p.viewer.turnXTargetFrame = p.viewer.turnXStartFrame + 30;
//				p.viewer.turningX = true;
//				p.viewer.lastMovementFrame = p.p.frameCount;
			}
		}
		else if (mouseY < p.p.height * 0.25 && mouseY > -1) 
		{
			if(!p.viewer.turningY)
			{
				p.viewer.turnYToAngle(PApplet.radians(5.f), -1);

//				p.viewer.turnYStartFrame = p.p.frameCount;
//				p.viewer.turnYDirection = -1;
//				p.viewer.turnYTarget = p.viewer.getYOrientation() - PApplet.radians(15.f);
//				p.viewer.turnYTargetFrame = p.viewer.turnYStartFrame + 30;
//				p.viewer.turningY = true;
//				p.viewer.lastMovementFrame = p.p.frameCount;
			}
		}
		else if (mouseY > p.p.height * 0.75 && mouseY < p.p.height + 1) 
		{
			if(!p.viewer.turningY)
			{
				p.viewer.turnYToAngle(PApplet.radians(5.f), 1);

//				p.viewer.turnYStartFrame = p.p.frameCount;
//				p.viewer.turnYDirection = 1;
//				p.viewer.turnYTarget = p.viewer.getYOrientation() + PApplet.radians(15.f);
//				p.viewer.turnYTargetFrame = p.viewer.turnYStartFrame + 30;
//				p.viewer.turningY = true;
//				p.viewer.lastMovementFrame = p.p.frameCount;
			}
		}
		else
		{
				if(p.viewer.turningX) p.viewer.turningX = false;
				if(p.viewer.turningY) p.viewer.turningY = false;
		}
	}

	void handleMousePressed(int mouseX, int mouseY)
	{
//		boolean doubleClick = false, switchedViews = false;

//			PApplet.println("MousePressed!");
		if(!p.viewer.settings.orientationMode && p.viewer.lastMovementFrame > 5)
		{
			if(mouseX > p.p.width * 0.25 && mouseX < p.p.width * 0.75 && mouseY < p.p.height * 0.75 && mouseY > p.p.height * 0.25)
			{
				p.viewer.walkForward();
			}
			else
			{
				mouseClickedX = mouseX;
				mouseClickedY = mouseY;
			}
			p.viewer.lastMovementFrame = p.p.frameCount;
		}
		else p.viewer.moveToNextCluster(false, -1);

//		mouseOffsetX = 0;
//		mouseOffsetY = 0;
	}

	void handleMouseReleased(int mouseX, int mouseY)
	{
		mouseReleased = true;
//		releasedRecentlyFrame = p.p.frameCount;
		
		boolean doubleClick = false;

		if(mouseClickedRecently)							// Double click
		{
			doubleClick = true;
		}

		if(p.viewer.settings.mouseNavigation)
		{
			p.viewer.walkSlower();
			p.viewer.lastMovementFrame = p.p.frameCount;
			if(doubleClick)									
				p.viewer.moveToNearestCluster(p.viewer.movementTeleport);
		}
		
		if(p.display.displayView == 1)
		{
			p.display.map2D.handleMouseReleased(mouseX, mouseY);
		}
		else if(p.display.displayView == 3)
		{
			p.display.handleMouseReleased(mouseX, mouseY);
		}
	}
	
	void handleMouseClicked(int mouseX, int mouseY)
	{
		mouseClickedRecently = true;
		clickedRecentlyFrame = p.p.frameCount;
		mouseReleased = false;
	}

	void handleMouseDragged(int mouseX, int mouseY)
	{
//		mouseOffsetX = mouseClickedX - mouseX;
//		mouseOffsetY = mouseClickedY - mouseY;

		p.viewer.lastMovementFrame = p.p.frameCount;			// Turn faster if larger offset X or Y?
	}
	
//	void updateMapMouse()
//	{
////		p.display.map2D.largeMapWidth
////		p.display.map2D.largeMapHeight
////		p.display.map2D.largeMapXOffset
////		p.display.map2D.largeMapYOffset
//		
////		PApplet.print("pmouseX:"+p.p.pmouseX);
////		PApplet.println(" pmouseY:"+p.p.pmouseY);
////		PApplet.print("mouseX:"+p.p.mouseX);
////		PApplet.println(" mouseY:"+p.p.mouseY);
//		
////		p.display.map2D.drawMousePointOnMap(new PVector(p.p.pmouseX, p.p.pmouseY, 0), 3, 
////				p.display.map2D.largeMapWidth, p.display.map2D.largeMapHeight, 255, 255, 255, 255);
//
////		PVector mousePoint = new PVector(p.p.mouseX, p.p.mouseY, 0);
////		p.display.map2D.drawMousePointOnMap(mousePoint, 6, 
////				p.display.map2D.largeMapWidth, p.display.map2D.largeMapHeight, 111, 255, 255, 255);
//		p.display.map2D.selectMouseClusterOnMap();
//	}
}