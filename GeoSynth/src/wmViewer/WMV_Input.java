package wmViewer;
import g4p_controls.GButton;
import g4p_controls.GEvent;
import g4p_controls.GToggleControl;
import g4p_controls.GValueControl;
import processing.core.*;

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
	int mouseClickedX = 0, mouseClickedY = 0;
	int mouseOffsetX = 0, mouseOffsetY = 0;
	
	boolean mouseClickedRecently = false;
	boolean mouseReleased = false;
	int clickedRecentlyFrame = 1000000;
	int releasedRecentlyFrame = 1000000;
	int doubleClickSpeed = 10;

	WMV_World p;

	WMV_Input(WMV_World parent) {
		p = parent;
		wasTimeFading = p.timeFading;
	}


	public void handleSliderEvent(GValueControl slider, GEvent event)
	{
		if (slider.tag == "Alpha") 
		{
			p.alpha = slider.getValueF();
		}

		if (slider.tag == "MediaLength") 
		{
			p.defaultMediaLength = slider.getValueI();
		}
		
//		  if (slider == sdr)  // The slider being configured?
//	    println(sdr.getValueS() + "    " + event);    
//	  if (slider == sdrEasing)
//	    sdr.setEasing(slider.getValueF());    
//	  else if (slider == sdrNbrTicks)
//	    sdr.setNbrTicks(slider.getValueI());    
//	  else if (slider == sdrBack)
//	    bgcol = slider.getValueI();

	}

	public void handleButtonEvent(GButton button, GEvent event) 
	{ 
		boolean state;

		//		  PApplet.println("button.tagNo:"+button.tagNo);
		//		  PApplet.println("button.tag:"+button.tag);

		switch(button.tag) 
		{
			/* Views + General */
			case "Scene":
				p.display.resetDisplayModes();
				break;
			case "Map":
				p.display.resetDisplayModes();
				p.display.map = true;
				break;
			case "Info":
				p.display.resetDisplayModes();
				p.display.info = true;
				break;
			case "Cluster":
				p.display.resetDisplayModes();
				p.display.cluster = true;
				break;
			case "Control":
				p.display.resetDisplayModes();
				p.display.control = true;
				break;
	
			case "Restart":
				p.p.restartWorldMediaViewer();
				break;
				
			
	
			/* Navigation */
			case "OpenNavigationWindow":
				p.display.sidebarView = 1;
				p.display.window.navigationWindow.setVisible(true);
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
				p.viewer.teleportToField(1);
				break;
			case "PreviousField":
				p.viewer.teleportToField(-1);
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
				
			case "CloseNavigationWindow":
				p.display.sidebarView = 0;
				p.display.window.navigationWindow.setVisible(false);
				break;
	
			case "OpenHelpWindow":
				p.display.sidebarView = 2;
				p.display.window.helpWindow.setVisible(true);
				break;
				
			case "CloseHelpWindow":
				p.display.sidebarView = 0;
				p.display.window.helpWindow.setVisible(false);
				break;
				
			case "OpenStatisticsWindow":
				p.display.sidebarView = 1;
				p.display.window.statisticsWindow.setVisible(true);
				break;
				
			case "CloseStatisticsWindow":
				p.display.sidebarView = 0;
				p.display.window.statisticsWindow.setVisible(false);
				break;

				/* Graphics */
			case "OpenGraphicsWindow":
				p.display.sidebarView = 1;
				p.display.window.graphicsWindow.setVisible(true);
				break;
	
			case "CloseGraphicsWindow":
				p.display.sidebarView = 0;
				p.display.window.graphicsWindow.setVisible(false);
				break;

			case "ZoomIn":
				p.viewer.startZoomTransition(-1);
				break;
			case "ZoomOut":
				p.viewer.startZoomTransition(1);
				break;
	
			/* Time */
			case "NextTime":
				p.viewer.moveToNextTimeSegment(true, p.viewer.movementTeleport);
				break;
			case "PreviousTime":
				p.viewer.moveToPreviousTimeSegment(true, p.viewer.movementTeleport);
				break;
	
			/* Selection */
			case "SelectionMode":
				p.viewer.selection = true;
				p.display.sidebarView = 3;
				p.display.window.selectionWindow.setVisible(true);
				break;
				
			case "ExitSelectionMode":
				p.display.sidebarView = 0;
				p.viewer.selection = false;
				p.display.window.selectionWindow.setVisible(false);
				break;
				
			case "SelectFront":
				p.viewer.selectFrontMedia(true);
				break;
				
			case "DeselectFront":
				p.viewer.selectFrontMedia(false);	
				break;
					
			case "DeselectAll":
				p.getCurrentField().deselectAllMedia(false);
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
				p.saveImage();
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
			/* Navigation */
			case "FollowTimeline":
				if(option.isSelected())
					p.viewer.followMode = 0;
				break;
	  		case "FollowGPSTrack":
				if(option.isSelected())
					p.viewer.followMode = 1;
				break;
	  		case "FollowMemory":
				if(option.isSelected())
					p.viewer.followMode = 2;
				break;
			case "MovementTeleport":
				p.viewer.movementTeleport = option.isSelected();
				break;
			case "FollowTeleport":
//				p.timeFading = option.isSelected();
				break;
				
			/* Graphics */
			case "TimeFading":
				p.timeFading = option.isSelected();
				break;
			case "FadeEdges":
				p.blurEdges = option.isSelected();
				break;
			case "HideImages":
				PApplet.println("option.isSelected():"+option.isSelected());
				PApplet.println("p.getCurrentField().hideImages::"+p.getCurrentField().hideImages);
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
				p.orientationMode = !p.orientationMode;
				break;
			case "AngleFading":
				p.angleFading = option.isSelected();
				break;
			case "AngleThinning":
				p.angleThinning = option.isSelected();
				break;
				
			/* Selection */
			case "MultiSelection":
				p.viewer.multiSelection = option.isSelected();
				break;
					
			case "SegmentSelection":
				p.viewer.segmentSelection = option.isSelected();
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
// 			p.p.world.paused = !p.p.world.paused;
			if(p.display.window.showWMVWindow)
				p.display.window.hideWMVWindow();
			else
				p.display.window.showWMVWindow();
		}
		
		/* Display Modes */
		if (!optionKey && key == '1') 
		{
			boolean state = p.display.map;
			p.display.resetDisplayModes();
			p.display.map = !state;
		}
		
		if (!optionKey && key == '!') 
		{
			boolean state = p.display.mapOverlay;
			p.display.resetDisplayModes();
			p.display.mapOverlay = !state;
		}
		
		if (!optionKey && key == '2') 
		{
			boolean state = p.display.info;
			p.display.resetDisplayModes();
			p.display.info = !state;
		}
		
		if (!optionKey && key == '@') 
		{
			boolean state = p.display.infoOverlay;
			p.display.resetDisplayModes();
			p.display.infoOverlay = !state;
		}

		if (!optionKey && key == '3') 
		{
			boolean state = p.display.cluster;
			p.display.resetDisplayModes();
			p.display.cluster = !state;
		}
		
		if (!optionKey && key == '#') 
		{
			boolean state = p.display.clusterOverlay;
			p.display.resetDisplayModes();
			p.display.clusterOverlay = !state;
		}
		
		if (!optionKey && key == '4') 
		{
			boolean state = p.display.control;
			p.display.resetDisplayModes();
			p.display.control = !state;
		}
		
		if (!optionKey && key == '$') 
		{
			boolean state = p.display.controlOverlay;
			p.display.resetDisplayModes();
			p.display.controlOverlay = !state;
		}
		
		if (!optionKey && key == '5') 
			p.showMediaToCluster = !p.showMediaToCluster;			// Draw line from each media point to cluster
		
		if (!optionKey && key == '6') 
			p.showCaptureToMedia = !p.showCaptureToMedia;			// Draw line from each media point to its capture location
		
		if (!optionKey && key == '7') 
			p.showCaptureToCluster = !p.showCaptureToCluster;		// Draw line from each media capture location to associated cluster

		if (!optionKey && key == '8') 
		{
			if(p.viewer.maxVisibleClusters > 1)
				p.viewer.maxVisibleClusters--;		// Draw line from each media capture location to associated cluster
		}

		if (!optionKey && key == '9') 
		{
			if(p.viewer.maxVisibleClusters < 9)
				p.viewer.maxVisibleClusters++;		// Draw line from each media capture location to associated cluster
		}
		
		if (key == '_')
			p.showModel = !p.showModel;
		
		if (key == '+')
			p.viewer.dateNavigation = !p.viewer.dateNavigation;
		
		/* Clustering */
		if (key == 'r')
			p.startInteractiveClustering();

		if (key == 'C')
			p.startInitialClustering();				// Re-run clustering on all fields
		
		if (key == 'R')
			p.p.restartWorldMediaViewer();
		
		if(p.display.map || p.display.mapOverlay)	/* 2D Map View */
		{
			// Option Key
			if (optionKey && key == '1') 
				p.display.map2D.mapMode = 1;

			if (optionKey && key == '2') 
				p.display.map2D.mapMode = 2;

			if (optionKey && key == '3') 
				p.display.map2D.mapMode = 3;

			if (optionKey && key == '4') 
				p.display.map2D.mapMode = 4;

			if (optionKey && key == '5') 
				p.display.map2D.mapMode = 5;

			if (optionKey && key == '6') 
				p.display.map2D.mapMode = 6;

			if (optionKey && key == '7') 
				p.display.map2D.mapMode = 7;

			// Option + Shift Keys
			if (optionKey && key == '!') 
				p.display.map2D.mapImages = !p.display.map2D.mapImages;

			if (optionKey && key == '@') 
				p.display.map2D.mapPanoramas = !p.display.map2D.mapPanoramas;

			if (optionKey && key == '#') 
				p.display.map2D.mapVideos = !p.display.map2D.mapVideos;

			if (key == ']') {
//				p.display.map2D.mapZoom *= 1.02f;
				p.display.map2D.mapZoomTransition(p.display.map2D.mapZoom * 1.176f);
			}

			if (key == '[') {
//				p.display.map2D.mapZoom *= 0.985f;
				p.display.map2D.mapZoomTransition(p.display.map2D.mapZoom * 0.85f);
			}

			if (key == PApplet.CODED) 					
			{
				if (keyCode == PApplet.LEFT) 
					p.viewer.rotateX(-1);

				if (keyCode == PApplet.RIGHT) 
					p.viewer.rotateX(1);

				if (shiftKey && keyCode == PApplet.LEFT) 
					p.display.map2D.mapLeftEdge -= 10.f;

				if (shiftKey && keyCode == PApplet.RIGHT) 
					p.display.map2D.mapLeftEdge += 10.f;

				if (shiftKey && keyCode == PApplet.DOWN) 
					p.display.map2D.mapTopEdge += 10.f;

				if (shiftKey && keyCode == PApplet.UP) 
					p.display.map2D.mapTopEdge -= 10.f;
			}
		}
		else if(p.display.info || p.display.infoOverlay)		/* Info View */
		{
		
		}
		else if(p.display.cluster || p.display.clusterOverlay)		/* Cluster View */
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
		else if(p.display.control || p.display.controlOverlay)		/* Controls View */
		{

		}
		
		if (!p.interactive)		
		{
			/* 3D View Controls */
			if (key == '|')
				p.getCurrentCluster().stitchImages();    			
			
			if (optionKey && key == '[')
			{
				if(p.thinningAngle > PApplet.PI / 64.f)
					p.thinningAngle -= PApplet.PI / 128.f;
//				p.display.message("Set thinningAngle:"+p.thinningAngle);
				p.getCurrentField().model.analyzeClusterMediaDirections();
			}

			if (optionKey && key == ']')
			{
				if(p.thinningAngle < p.visibleAngle - PApplet.PI / 128.f)
					p.thinningAngle += PApplet.PI / 128.f;
//				p.display.message("Set thinningAngle:"+p.thinningAngle);
				p.getCurrentField().model.analyzeClusterMediaDirections();
			}

			if (optionKey && key == '\\')
				p.getCurrentField().stitchAllClusters();		// Teleport to cluster with > 1 times
			
			if (!optionKey && key == 'a') 
				p.viewer.startMoveXTransition(-1);

			if (!optionKey && key == 'd') 
				p.viewer.startMoveXTransition(1);

			if (key == 'T') 
				p.timeFading = !p.timeFading;

			if (!optionKey && key == 's') 
				p.viewer.startMoveZTransition(1);

			if (!optionKey && key == 'w') 
				p.viewer.walkForward();

			if (optionKey && key == 'm') 
				p.showMetadata = !p.showMetadata;

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
			if (key == 'J') 
				p.viewer.moveToRandomCluster(true);					// Move to random cluster

			if (key == 'j') 
				p.viewer.moveToRandomCluster(false);				// Jump (teleport) to random cluster

			if (key == 'I')
				p.orientationMode = !p.orientationMode;

			if (key == 'W') 
				p.viewer.moveToNearestClusterAhead(false);
			
			if (!optionKey && key == ']') {
				float value = p.altitudeScalingFactor * 1.052f;
				p.altitudeScalingFactor = PApplet.constrain(value, 0.f, 1.f);
				p.getCurrentField().calculateMediaLocations();		// Recalculate media locations
				p.getCurrentField().createClusters();				// Recalculate cluster locations
			}

			if (!optionKey && key == '[') {
				float value = p.altitudeScalingFactor *= 0.95f;
				p.altitudeScalingFactor = PApplet.constrain(value, 0.f, 1.f);
				p.getCurrentField().calculateMediaLocations();		// Recalculate media locations
				p.getCurrentField().createClusters();				// Recalculate cluster locations
			}
			
			if(optionKey)
			{
//				if (key == 't') 
//					p.viewer.moveToTimeInField(p.getCurrentField().fieldID, 0, true);
//
//				if (key == 'd') 
//					p.viewer.moveToFirstTimeOnDate(p.getCurrentField().fieldID, 0, true);
//
//				if (key == 'T')
//					p.viewer.moveToTimeInField(p.getCurrentField().fieldID, 0, false);
//
//				if (key == 'D') 
//					p.viewer.moveToFirstTimeOnDate(p.getCurrentField().fieldID, 0, false);

				if (key == 'n')						// Teleport to next time segment
					p.viewer.moveToNextTimeSegment(true, true);

				if (key == 'b')						// Teleport to previous time segment
					p.viewer.moveToPreviousTimeSegment(true, true);

				if (key == 'N')						// Teleport to next cluster time segment
					p.viewer.moveToNextTimeSegment(false, true);

				if (key == 'B')						// Teleport to previous cluster time segment
					p.viewer.moveToPreviousTimeSegment(false, true);
			}
			else
			{
				if (key == 'n')						// Move to current time segment
					p.viewer.moveToNextTimeSegment(true, false);

				if (key == 'b')						// Move to current time segment
					p.viewer.moveToPreviousTimeSegment(true, false);

				if (key == 'N')						// Move to next cluster time segment
					p.viewer.moveToNextTimeSegment(false, false);

				if (key == 'B')						// Move to previous cluster time segment
					p.viewer.moveToPreviousTimeSegment(false, false);
			}
			
			if (key == '~')
				if(!p.viewer.isFollowing())
					p.viewer.followMemory();

			if (optionKey && key == 'g')
				if(!p.viewer.isFollowing())
					p.viewer.followGPSTrack();
			
			if (!optionKey && key == '>')
			{
				if(!p.viewer.isFollowing())
					p.viewer.followTimeline(true, false);
			}

			if (key == 'u') 		// Teleport to nearest cluster with video
				p.viewer.moveToNextCluster(true, 2);

			if (key == 'U') 		// Go to nearest cluster with video
				p.viewer.moveToNextCluster(false, 2);

			if (key == 'm') 		// Teleport to nearest cluster with panorama
				p.viewer.moveToNextCluster(true, 1);

			if (key == 'M') 		// Go to nearest cluster with panorama
				p.viewer.moveToNextCluster(false, 1);

			if (key == '{')
				p.viewer.teleportToField(-1);

			if (key == '}') 
				p.viewer.teleportToField(1);

			if (key == 'E') 
				p.viewer.moveToNearestCluster(false);

			if (key == '-') 
				p.getCurrentField().fadeObjectDistances(0.85f);
			
			if (key == '=')
				p.getCurrentField().fadeObjectDistances(1.176f);

			if (key == 'q') 
				p.viewer.startZoomTransition(-1);

			if (key == 'z') 
				p.viewer.startZoomTransition(1);

			/* 3D Controls Disabled in HUD View */
			if(!p.display.inDisplayView())							
			{
				if (optionKey && key == 'e')
					p.blurEdges = !p.blurEdges;

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
					p.alphaMode = !p.alphaMode;

				if (!shiftKey && optionKey && key == ' ') 
				{
					p.timeFading = !p.timeFading;
				}

				if (key == ')') {
					float newAlpha = PApplet.constrain(p.alpha+15.f, 0.f, 255.f);
					p.fadeAlpha(newAlpha);
					PApplet.println("p.alpha goal:"+newAlpha);
				}

				if (key == '(') {
					float newAlpha = PApplet.constrain(p.alpha-15.f, 0.f, 255.f);
					PApplet.println("p.alpha goal:"+newAlpha);
					p.fadeAlpha(newAlpha);
				}

				if (key == ':')
				{
					p.showUserPanoramas = !p.showUserPanoramas;
				}

				if (key == ';')
				{
					p.showStitchedPanoramas = !p.showStitchedPanoramas;
				}

				if (key == 'O') 
				{
					p.viewer.selection = !p.viewer.selection;
					if(p.viewer.selection && p.viewer.multiSelection) p.viewer.multiSelection = false;
					if(p.viewer.selection && p.viewer.segmentSelection) p.viewer.segmentSelection = false;
				}
				
				if (optionKey && key == 'x')
					p.getCurrentField().deselectAllMedia(false);

				if (optionKey && key == '-')
					p.visibleAngle -= 3.1415f / 128.f; 

				if (optionKey && key == '=')
					p.visibleAngle += 3.1415f / 128.f; 

				/* Selection */
				if (!optionKey && key == 'x') 
					p.viewer.selectFrontMedia(true);

				if (!optionKey && key == 'X')
					p.viewer.selectFrontMedia(false);
				
				if (key == 'S')
				{
					p.viewer.multiSelection = !p.viewer.multiSelection;
					if(p.viewer.multiSelection && !p.viewer.selection)
						p.viewer.selection = true;
				}

				if (optionKey && key == 's')
				{
					p.viewer.segmentSelection = !p.viewer.segmentSelection;
					if(p.viewer.segmentSelection && !p.viewer.selection)
						p.viewer.selection = true;
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
					p.angleFading = !p.angleFading;
				}
				
				if (key == 'H')
				{
					p.angleThinning = !p.angleThinning;
				}

				/* Output */
				if (key == 'o') 
					p.p.selectFolder("Select an output folder:", "outputFolderSelected");

				if (key == 'p') 	// Save image to disk
				{	
					if(!p.outputFolderSelected) p.p.selectFolder("Select an output folder:", "outputFolderSelected");
					p.saveImage();
				}

				if (key == '&') 
				{
					if(p.p.world.defaultMediaLength > 10)
						p.p.world.defaultMediaLength -= 10;
				}
					
				if (key == '*') 			// Look for images when none are visible
				{
					if(p.p.world.defaultMediaLength < 990)
						p.p.world.defaultMediaLength += 10;
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
					m.runKMeansClustering(p.kMeansClusteringEpsilon, m.clusterRefinement, m.clusterPopulationFactor);
					p.getCurrentField().createTimeline();					// Create field timeline
				}
			}
			
			if (key == '[') 	
			{
				if(!p.autoClusterDistances && p.minClusterDistance > 0.25f)
				{
					p.minClusterDistance -= 0.25f;
//					PApplet.println("p.minClusterDistance:"+p.minClusterDistance);
					for(WMV_Field f : p.getFields())
					{
						f.model.setMinClusterDistance(p.minClusterDistance);	
//						f.model.clustersNeedUpdate = true;
						p.getCurrentField().model.runKMeansClustering( p.kMeansClusteringEpsilon, p.getCurrentField().model.clusterRefinement, p.getCurrentField().model.clusterPopulationFactor );
						p.getCurrentField().initializeClusters();			
						p.display.map2D.initializeMaps();
					}
				}
			}
			
			if (key == ']') 	
			{
				if(!p.autoClusterDistances && p.minClusterDistance < p.maxClusterDistance - 2.f)
				{
					p.minClusterDistance += 0.25f;
//					PApplet.println("p.minClusterDistance:"+p.minClusterDistance);
					for(WMV_Field f : p.getFields())
					{
						f.model.setMinClusterDistance(p.minClusterDistance);
//						f.model.clustersNeedUpdate = true;					// -- Temporary: will make minClusterDistance field specific...
						p.getCurrentField().model.runKMeansClustering( p.kMeansClusteringEpsilon, p.getCurrentField().model.clusterRefinement, p.getCurrentField().model.clusterPopulationFactor );
						p.getCurrentField().initializeClusters();			
						p.display.map2D.initializeMaps();
					}
				}
			}
		}
		
		/* Arrow and Shift Keys */
		if (key == PApplet.CODED) 					
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
								p.getCurrentField().model.runKMeansClustering( p.kMeansClusteringEpsilon, p.getCurrentField().model.clusterRefinement, populationFactor );
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
								p.getCurrentField().model.runKMeansClustering( p.kMeansClusteringEpsilon, p.getCurrentField().model.clusterRefinement, populationFactor );
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
								p.getCurrentField().model.runKMeansClustering( p.kMeansClusteringEpsilon, refinementAmount, p.getCurrentField().model.clusterPopulationFactor );
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
								p.getCurrentField().model.runKMeansClustering( p.kMeansClusteringEpsilon, refinementAmount, p.getCurrentField().model.clusterPopulationFactor );
								p.getCurrentField().initializeClusters();			
								p.display.map2D.initializeMaps();
							}
							else p.getCurrentField().model.clusterPopulationFactor -= 1.f;
						}
					}
				}
			}
			
			if (keyCode == PApplet.SHIFT) {
				shiftKey = true;
			}
			
			if (keyCode == PApplet.ALT) 
				optionKey = true;
		}
	}
	
	/**
	 * handleKeyReleased()
	 * Respond to user key releases
	 */
	void handleKeyReleased(char key, int keyCode)
	{
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

		/* Arrow and Shift Keys */
		if (key == PApplet.CODED) {
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
		}
	}

	/* Mouse */
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
			PApplet.println("Held mouse...");
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
		boolean doubleClick = false, switchedViews = false;

//			PApplet.println("MousePressed!");
		if(!p.orientationMode && p.viewer.lastMovementFrame > 5)
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

		mouseOffsetX = 0;
		mouseOffsetY = 0;
	}

	void handleMouseReleased(int mouseX, int mouseY)
	{
		mouseReleased = true;
		releasedRecentlyFrame = p.p.frameCount;
		
		p.viewer.walkSlower();
		p.viewer.lastMovementFrame = p.p.frameCount;

		boolean doubleClick = false, switchedViews = false;

		if(mouseClickedRecently)		// Double click
		{
			doubleClick = true;
		}

		if(doubleClick)			// Double clicked
		{
			if(p.p.debug.viewer)
				PApplet.println("Double click...");
			p.viewer.moveToNearestCluster(true);
//			p.viewer.moveToNextCluster(false, -1);
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
		mouseOffsetX = mouseClickedX - mouseX;
		mouseOffsetY = mouseClickedY - mouseY;

		p.viewer.lastMovementFrame = p.p.frameCount;			// Turn faster if larger offset X or Y?
	}
	
	void updateMapMouse()
	{
//		p.display.map2D.largeMapWidth
//		p.display.map2D.largeMapHeight
//		p.display.map2D.largeMapXOffset
//		p.display.map2D.largeMapYOffset
		
		PApplet.print("pmouseX:"+p.p.pmouseX);
		PApplet.println(" pmouseY:"+p.p.pmouseY);
//		PApplet.print("mouseX:"+p.p.mouseX);
//		PApplet.println(" mouseY:"+p.p.mouseY);
		p.display.map2D.drawMousePointOnMap(new PVector(p.p.pmouseX, p.p.pmouseY, 0), 3, 
				p.display.map2D.largeMapWidth, p.display.map2D.largeMapHeight, 255, 255, 255, 255);
		p.display.map2D.drawMousePointOnMap(new PVector(p.p.mouseX, p.p.mouseY, 0), 6, 
				p.display.map2D.largeMapWidth, p.display.map2D.largeMapHeight, 111, 255, 255, 255);
	}

}