package wmViewer;
import java.util.ArrayList;

import com.jogamp.newt.event.KeyEvent;

import g4p_controls.GButton;
import g4p_controls.GCheckbox;
import g4p_controls.GEvent;
import g4p_controls.GOption;
import g4p_controls.GSlider;
import g4p_controls.GToggleControl;
import g4p_controls.GValueControl;
import g4p_controls.GWinData;
//import damkjer.ocd.Camera;
import processing.core.*;
//import processing.data.FloatList;
import toxi.math.ScaleMap;

/***********************************
 * @author davidgordon
 * Class for displaying 2D text and graphics
 */

class WMV_Display
{
	/* Classes */
	public WMV_Window window;					// Main interaction window
	
	/* Window Modes */
	public boolean fullscreen = true;
	
	/* Sidebar Modes */
	public boolean sidebarStatistics = false;			// Sidebar statistics view 

	/* Display Modes */
	public boolean map = false;					// Display map only
	public boolean info = false;				// Display simulation info 
	public boolean cluster = false;				// Display cluster statistics
	public boolean control = false;				// Display controls only
	public boolean about = false;				// Display about screen  -- need to implement
	
	public boolean mapOverlay = false;			// Overlay map on 3D view
	public boolean infoOverlay = false;			// Overlay simulation info on 3D view
	public boolean clusterOverlay = false;		// Display cluster statistics over 3D view
	public boolean controlOverlay = false;		// Display controls over 3D view
	
	/* Debug */
	public boolean drawForceVector = false;
	PVector mapVectorOrigin, mapVectorVector;
	
	/* Status */
	public boolean initialSetup = true;
	
	/* Graphics */
	float hudDistance;							// Distance of the Heads-Up Display from the virtual camera -- Change with zoom level??
	public boolean drawGrid = false; 			// Draw 3D grid   			-- Unused

	public int blendMode = 0;							// Alpha blending mode
	public int numBlendModes = 10;						// Number of blending modes

	/* Clusters */
	public int displayCluster = 0;

	/* Map Modes */
	int mapMode = 1;							// 	1:  All   2: Clusters + Media   3: Clusters + Capture Locations  4: Capture Locations + Media
												//	5:  Clusters Only   6: Media Only   7: Capture Locations Only
	/* Map */
	public float mapZoom = 1.f;
	public float mapLeftEdge = 0.f, mapTopEdge = 0.f;
	
	public boolean mapImages = true, mapPanoramas = true, mapVideos = true, mapClusters = true;
	private float mapImageHue = 150.f, mapImageCaptureHue = 180.f;
	private float mapPanoramaHue = 190.f, mapPanoramaCaptureHue = 220.f;
	private float mapVideoHue = 40.f, mapVideoCaptureHue = 70.f;
	
	public float mapClusterHue = 112.f;
	private float mapAttractorClusterHue = 222.f;
	
	private float mapCameraHue = 140.f;
	private float mapMediaTransparency = 120.f;
	private float minSaturation = 120.f, maxSaturation = 210.f;

	float largeMapWidth, largeMapHeight, largeMapMaxWidth, largeMapMaxHeight;
	float largeMapXOffset, largeMapYOffset;
	float largeMapZoomLevel = 1.f;
	
	float smallMapWidth, smallMapHeight, smallMapMaxWidth, smallMapMaxHeight;
	float smallMapXOffset, smallMapYOffset;
	float logoXOffset, logoYOffset;

	float smallPointSize;
	float mediumPointSize;
	float largePointSize;
	float hugePointSize;
	float cameraPointSize;
	
	/* Messages */
	ArrayList<String> messages;						// Messages to display on screen
	ArrayList<String> metadata;						// Messages to display on screen
	ArrayList<String> startupMessages;						// Messages to display on screen
	int messageStartFrame = -1;
	int metadataStartFrame = -1;
	int startupMessageStartFrame = -1;
	int messageDuration = 60;
	
	/* Text */
	float centerTextXOffset, topTextYOffset;
	float userMessageXOffset, userMessageYOffset, startupMessageXOffset;
	float leftTextXOffset, rightTextXOffset, metadataYOffset, startupMessageYOffset;
	float midLeftTextXOffset, midRightTextXOffset;
	float clusterImageXOffset, clusterImageYOffset;
	
	float largeTextSize = 28.f;
	float mediumTextSize = 22.f;
	float smallTextSize = 18.f;
	float linePadding = 10.f;
	float lineWidth = smallTextSize + linePadding;			
	float lineWidthWide = largeTextSize + linePadding;			
	float lineWidthVeryWide = largeTextSize * 2.f;			

	WMV_World p;

	WMV_Display(WMV_World parent)
	{
		p = parent;
		
		hudDistance = p.hudDistance;
		
		messages = new ArrayList<String>();
		metadata = new ArrayList<String>();
		startupMessages = new ArrayList<String>();

		largeMapXOffset = -p.p.width * 0.5f;
		largeMapYOffset = -p.p.height * 0.5f;
		largeMapMaxWidth = p.p.width * 0.95f;
		largeMapMaxHeight = p.p.height * 0.95f;

		smallMapMaxWidth = p.p.width / 4.f;
		smallMapMaxHeight = p.p.height / 4.f;

		logoXOffset = p.p.width - p.p.width / 3.f;
		logoYOffset = p.p.height / 2.5f;
		
		centerTextXOffset = 0;
		leftTextXOffset = -p.p.width / 2.f;
		midLeftTextXOffset = -p.p.width / 3.f;
		rightTextXOffset = p.p.width / 2.f;
		midRightTextXOffset = p.p.width / 3.f;

		topTextYOffset = -p.p.height / 1.5f;
		clusterImageXOffset = -p.p.width/ 1.66f;
		clusterImageYOffset = p.p.height / 3.75f;

		userMessageXOffset = -p.p.width / 2.f;
		userMessageYOffset = 0;

		metadataYOffset = -p.p.height / 2.f;

		startupMessageXOffset = p.p.width / 2;
		startupMessageYOffset = -p.p.width / 3.f;

		smallPointSize = 0.0000022f * p.p.width;
		mediumPointSize = 0.0000028f * p.p.width;
		largePointSize = 0.0000032f * p.p.width;
		hugePointSize = 0.0000039f * p.p.width;
		cameraPointSize = 0.004f * p.p.width;
	}

	void setupSidebar()
	{
		window = new WMV_Window(this);				// Setup and display interaction window
	}

	/**
	 * Draw Heads-Up Display elements: messages, interactive map, field statistics, metadata.
	 */
	void draw()
	{
		if(initialSetup)
		{
			p.p.hint(PApplet.DISABLE_DEPTH_TEST);												// Disable depth testing for drawing HUD
			p.p.background(0);																// Hide 3D view
			displayStartupMessages();														// Draw startup messages
			progressBar();
		}
		else
		{
			if(window.mainSidebar.isVisible())
			{
				if(p.timeFading)
				{
					if(p.timeMode == 0)			// Need to fix cluster date / time structures first!
					{
//						WMV_Cluster curCluster = p.getCurrentCluster();
//						int firstTimeID = curCluster.getFirstTimeSegment();
//						int dateCount = 1;
//						
//						if(curCluster.dateline != null)
//							dateCount = curCluster.dateline.size();
//						
//						PApplet.println("curCluster.lowDate:"+curCluster.lowDate+" curCluster.highDate:"+curCluster.highDate);
//						PApplet.println("curCluster.lowDate:"+curCluster.lowDate+" curCluster.highDate:"+curCluster.highDate);
//						float fTime = (float) p.getCurrentCluster().currentTime / (float) p.getCurrentCluster().timeCycleLength;
//						PApplet.println("p.getCurrentCluster().currentTime:"+p.getCurrentCluster().currentTime+" p.getCurrentCluster().timeCycleLength: "+p.getCurrentCluster().timeCycleLength);
//						float fHour = fTime * 24.f;
//						int hour = (int)(fHour);
//						int min = PApplet.round((fHour - hour) * 60);
//						window.lblCurrentTime.setText(hour+":"+min);
//						PApplet.println("fHour:"+fHour+"  fHour - hour:"+(fHour - hour));
//						PApplet.println("fTime:"+fTime+" Time = "+hour+":"+min);
					}
					else
					{
						float fTime = (float) p.currentTime / (float) p.timeCycleLength;
//						PApplet.println("p.getCurrentCluster().currentTime:"+p.getCurrentCluster().currentTime+" p.getCurrentCluster().timeCycleLength: "+p.getCurrentCluster().timeCycleLength);
						float fHour = fTime * 24.f;
						int hour = (int)(fHour);
						int min = PApplet.round((fHour - hour) * 60);
						window.lblCurrentTime.setText((hour==0?"00":hour)+":"+(min==0?"00":min));
//						PApplet.println("fHour:"+fHour+"  fHour - hour:"+(fHour - hour));
//						PApplet.println("fTime:"+fTime+" Time = "+hour+":"+min);
					}
				}
			}
			
			if( map || control || info || about || cluster || p.interactive )
			{
				p.p.hint(PApplet.DISABLE_DEPTH_TEST);												// Disable depth testing for drawing HUD
				p.p.background(0.f);																// Hide 3D view

				if(map)
				{
					drawLargeMap();
					drawTimelines();
					drawDatelines();
				}

				if(info)
					displayInfo();

				if(cluster)
					displayClusterInfo();

				if(control)
					displayControls();

				if(p.interactive)
				{
					displayInteractiveClustering();
				}
			}
			else if( mapOverlay || controlOverlay || infoOverlay || clusterOverlay || messages.size() > 0 || metadata.size() > 0 )
			{
				p.p.hint(PApplet.DISABLE_DEPTH_TEST);												// Disable depth testing for drawing HUD

				if(mapOverlay)
				{
					drawLargeMap();
					drawTimelines();
				}

				if(infoOverlay)
					displayInfo();

				if(clusterOverlay)
					displayClusterInfo();				

				if(controlOverlay)
					displayControls();

				if(messages.size() > 0)
					displayMessages();

				if(p.showMetadata && metadata.size() > 0 && p.viewer.selection)	
					displayMetadata();

				//			if(aboutOverlay)

				if((map || mapOverlay) && drawForceVector)						// Draw force vector
				{
					drawForceVector();
				}
			}
		}
	}

	/**
	 * Draw (on 2D map) a point given in 3D world coordinates 
	 * @param point Point in 3D world coordinates
	 * @param pointSize Point size
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param hue Point hue
	 * @param saturation Point saturation
	 * @param brightness Point brightness
	 * @param transparency Point transparency
	 */
	public void drawMapPoint( PVector point, float pointSize, float mapWidth, float mapHeight, float hue, float saturation, float brightness, float transparency )
	{		
		float mapLocX, mapLocY;
		WMV_Model m = p.getCurrentField().model;

		if(!p.p.utilities.isNaN(point.x) && !p.p.utilities.isNaN(point.y) && !p.p.utilities.isNaN(point.z))
		{
			/* Find 2D map coordinates for this image */
			mapLocX = PApplet.map( point.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapWidth * mapZoom );		
			mapLocY = PApplet.map( point.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, 0, mapHeight * mapZoom );

			if(mapLocX < mapWidth && mapLocX > 0 && mapLocY < mapHeight && mapLocY > 0)
			{
				p.p.stroke(hue, saturation, brightness, transparency);
				p.p.pushMatrix();
				beginHUD();

				p.p.strokeWeight(pointSize);
				p.p.translate(mapLeftEdge, mapTopEdge);
				p.p.point(largeMapXOffset + mapLocX, largeMapYOffset + mapLocY, hudDistance);

				p.p.popMatrix();
			}
		}
		else message("Map point is NaN!:"+point+" hue:"+hue);
	}
	
	/**
	 * Draw (on 2D map) a point given in 3D world coordinates 
	 * @param point Point in 3D world coordinates
	 * @param pointSize Point size
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param hue Point hue
	 * @param saturation Point saturation
	 * @param brightness Point brightness
	 * @param transparency Point transparency
	 */
	public void drawFuzzyMapPoint( PVector point, float pointSize, float mapWidth, float mapHeight, float hue, float saturation, float brightness, float transparency )
	{
		float size = pointSize;
		int iterations = PApplet.round(size);
		int sizeDiff = PApplet.round(size/iterations);
		float alpha = transparency;
		float alphaDiff = transparency / iterations;
		
		for(int i=0; i<iterations; i++)
		{
			drawMapPoint( point, size * smallPointSize * mapWidth, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, alpha * 0.33f );
			size-=sizeDiff;
			alpha-=alphaDiff;
		}
	}
	
	/**
	 * Draw Interactive Clustering screen
	 */
	void displayInteractiveClustering()
	{
		drawLargeMap();
		if(messages.size() > 0) displayMessages();
	}

	/**
	 * Draw Interactive Clustering footer text
	 */
	void displayClusteringInfo()
	{
//		message("Interactive Clustering Mode: "+(p.hierarchical ?"Hierarchical Clustering":"K-Means Clustering"));
//		message(" ");
		
		if(p.hierarchical)
		{
//			message("Hierarchical Clustering");
			message(" ");
			message("Use arrow keys UP and DOWN to change clustering depth... ");
			message("Use [ and ] to change Minimum Cluster Distance... ");
		}
		else
		{
//			message("K-Means Clustering");
			message(" ");
			message("Use arrow keys LEFT and RIGHT to change Iterations... ");
			message("Use arrow keys UP and DOWN to change Population Factor... ");
			message("Use [ and ] to change Minimum Cluster Distance... ");
		}
		
		message(" ");
		message("Press <spacebar> to restart 3D viewer...");
	}
	
	/**
	 * Draw large 2D map
	 */
	void drawLargeMap()
	{
		p.p.pushMatrix();
		beginHUD();									// Begin 2D drawing
		
		p.p.fill(55, 0, 255, 255);
		p.p.textSize(largeTextSize);
		float textXPos = centerTextXOffset;
		float textYPos = topTextYOffset;

		if(p.interactive)
		{
			p.p.text("Interactive "+(p.hierarchical ? "Hierarchical" : "K-Means")+" Clustering", textXPos, textYPos, hudDistance);
		}
		else
			p.p.text(p.getCurrentField().name, textXPos, textYPos, hudDistance);

		p.p.popMatrix();
		drawMap(largeMapWidth, largeMapHeight, largeMapXOffset, largeMapYOffset);
	}
	
	/**
	 * Draw timelines
	 */
	void drawTimelines()
	{
		float y = logoYOffset * 0.75f;			// Starting vertical position
		float x = logoXOffset * 0.525f;

		p.p.fill(55, 0, 255, 255);
		p.p.stroke(55, 0, 255, 255);
		p.p.strokeWeight(1.f);

		WMV_Cluster c = p.getCurrentCluster();

		if(c != null && c.timeline.size() > 0)
		{
			p.p.pushMatrix();
			beginHUD();						
			p.p.textSize(mediumTextSize);
			p.p.text("Timeline", x, y, hudDistance);
			p.p.popMatrix();

			y += 100.f;			
			x = logoXOffset / 4.f;

			p.p.pushMatrix();
			beginHUD();						
			p.p.textSize(smallTextSize);
			p.p.text("Cluster", x, y, hudDistance);
			p.p.popMatrix();

//			float inc = p.p.width * 0.3f / c.clusterTimesHistogram.length;
//			int currentTime;
//			
//			if(p.timeMode == 0)
//				currentTime = (int)PApplet.map(c.currentTime, 0, p.timeCycleLength, 0, c.clusterTimesHistogram.length);
//			else
//				currentTime = (int)PApplet.map(p.currentTime, 0, p.timeCycleLength, 0, c.fieldTimesHistogram.length);
//
//			for(int i=0; i<c.clusterTimesHistogram.length; i++)
//			{
//				float val = inc * PApplet.sqrt( c.clusterTimesHistogram[i] ) * 50.f / PApplet.sqrt( (p.getCurrentCluster().images.size() + 
//												p.getCurrentCluster().panoramas.size() + p.getCurrentCluster().videos.size() ) );
//				x = logoXOffset / 3.f + i * inc;
//
//				p.p.pushMatrix();
//				beginHUD();
//				p.p.translate(x, y, hudDistance);
//				p.p.box(inc, val, 0);
//				p.p.popMatrix();
//				
//				if(i == currentTime)					// Draw current time
//				{
//					p.p.fill(145, 255, 255, 255);
//					p.p.stroke(145, 255, 255, 255);
//					
//					p.p.pushMatrix();
//					beginHUD();
//					p.p.translate(x, y, hudDistance);
//					p.p.box(inc, 25.f, 0);
//					p.p.popMatrix();
//					
//					p.p.fill(55, 0, 255, 255);
//					p.p.stroke(55, 0, 255, 255);
//				}
//			}

			if(p.p.debug.time)
			{
//				p.p.pushMatrix();
//				beginHUD();

//				FloatList clusterTimes = p.getCurrentCluster().getClusterTimes();
//				x = logoXOffset / 3.f + c.clusterTimesHistogram.length * inc + 30.f;
//				
//				if(clusterTimes.size() == 1)
//					p.p.text(clusterTimes.get(0), x, y, hudDistance);
//				if(clusterTimes.size() == 2)
//					p.p.text(clusterTimes.get(0)+" "+clusterTimes.get(1), x, y, hudDistance);
//				if(clusterTimes.size() >= 3)
//					p.p.text(clusterTimes.get(0)+" "+clusterTimes.get(1)+" "+clusterTimes.get(2), x, y, hudDistance);
				
//				p.p.popMatrix();
			}

			y += 100.f;			
			x = logoXOffset / 4.f;

			p.p.pushMatrix();
			beginHUD();						
			p.p.textSize(smallTextSize);
			p.p.text("Field", x, y, hudDistance);
			p.p.popMatrix();

//			inc = p.p.width * 0.3f / c.fieldTimesHistogram.length;
//			currentTime = (int)PApplet.map(p.currentTime, 0, p.timeCycleLength, 0, c.fieldTimesHistogram.length);
//
//			for(int i=0; i<c.fieldTimesHistogram.length; i++)
//			{
//				float val = inc * PApplet.sqrt( c.fieldTimesHistogram[i] ) * 500.f / PApplet.sqrt( (p.getCurrentField().images.size() + 
//												p.getCurrentField().panoramas.size() + p.getCurrentField().videos.size() ) );
//				x = logoXOffset / 3.f + i * inc;
//
//				p.p.pushMatrix();
//				beginHUD();
//				p.p.translate(x, y, hudDistance);
//				p.p.box(inc, val, 0);
//				p.p.popMatrix();
//				
//				if(i == currentTime)					// Draw current time
//				{
//					p.p.fill(145, 255, 255, 255);
//					p.p.stroke(145, 255, 255, 255);
//					
//					p.p.pushMatrix();
//					beginHUD();
//					p.p.translate(x, y, hudDistance);
//					p.p.box(inc, 25.f, 0);
//					p.p.popMatrix();
//					
//					p.p.fill(55, 0, 255, 255);
//					p.p.stroke(55, 0, 255, 255);
//				}
//			}
		}
	}
	
	/**
	 * Draw datelines
	 */
	void drawDatelines()
	{
		float y = logoYOffset * 0.75f - 250.f;			// Starting vertical position
		float x = logoXOffset * 0.525f;

		p.p.fill(55, 0, 255, 255);
		p.p.stroke(55, 0, 255, 255);
		p.p.strokeWeight(1.f);

		WMV_Cluster c = p.getCurrentCluster();

		if(c != null && c.dateline.size() > 0)
		{
			p.p.pushMatrix();
			beginHUD();						
			p.p.textSize(mediumTextSize);
			p.p.text("Dateline", x, y, hudDistance);
			p.p.popMatrix();

			y += 100.f;			
			x = logoXOffset / 4.f;

			p.p.pushMatrix();
			beginHUD();						
			p.p.textSize(smallTextSize);
			p.p.text("Cluster", x, y, hudDistance);
			p.p.popMatrix();

//			float inc = p.p.width * 0.3f / c.clusterDatesHistogram.length;
//			int currentDate = (int)PApplet.map(p.currentDate, 0, p.dateCycleLength, 0, c.clusterDatesHistogram.length);
//			
//			for(int i=0; i<c.clusterDatesHistogram.length; i++)
//			{
//				float val = inc * PApplet.sqrt( c.clusterDatesHistogram[i] ) * 50.f / PApplet.sqrt( (p.getCurrentCluster().images.size() + 
//												p.getCurrentCluster().panoramas.size() + p.getCurrentCluster().videos.size() ) );
//				x = logoXOffset / 3.f + i * inc;
//
//				p.p.pushMatrix();
//				beginHUD();
//				p.p.translate(x, y, hudDistance);
//				p.p.box(inc, val, 0);
//				p.p.popMatrix();
//				
//				if(i == currentDate)					// Draw current date
//				{
//					p.p.fill(145, 255, 255, 255);
//					p.p.stroke(145, 255, 255, 255);
//					
//					p.p.pushMatrix();
//					beginHUD();
//					p.p.translate(x, y, hudDistance);
//					p.p.box(inc, 25.f, 0);
//					p.p.popMatrix();
//					
//					p.p.fill(55, 0, 255, 255);
//					p.p.stroke(55, 0, 255, 255);
//				}
//			}

			if(p.p.debug.time)
			{
//				p.p.pushMatrix();
//				beginHUD();

//				FloatList clusterDates = p.getCurrentCluster().getClusterDates();
//				x = logoXOffset / 3.f + c.clusterDatesHistogram.length * inc + 30.f;
//				
//				if(clusterDates.size() == 1)
//					p.p.text(clusterDates.get(0), x, y, hudDistance);
//				if(clusterDates.size() == 2)
//					p.p.text(clusterDates.get(0)+" "+clusterDates.get(1), x, y, hudDistance);
//				if(clusterDates.size() >= 3)
//					p.p.text(clusterDates.get(0)+" "+clusterDates.get(1)+" "+clusterDates.get(2), x, y, hudDistance);
				
//				p.p.popMatrix();
			}

			y += 100.f;			
			x = logoXOffset / 4.f;

			p.p.pushMatrix();
			beginHUD();						
			p.p.textSize(smallTextSize);
			p.p.text("Field", x, y, hudDistance);
			p.p.popMatrix();

//			inc = p.p.width * 0.3f / c.fieldDatesHistogram.length;
//			currentDate = (int)PApplet.map(p.currentDate, 0, p.dateCycleLength, 0, c.fieldDatesHistogram.length);
//
//			for(int i=0; i<c.fieldDatesHistogram.length; i++)
//			{
//				float val = inc * PApplet.sqrt( c.fieldDatesHistogram[i] ) * 500.f / PApplet.sqrt( (p.getCurrentField().images.size() + 
//												p.getCurrentField().panoramas.size() + p.getCurrentField().videos.size() ) );
//				x = logoXOffset / 3.f + i * inc;
//
//				p.p.pushMatrix();
//				beginHUD();
//				p.p.translate(x, y, hudDistance);
//				p.p.box(inc, val, 0);
//				p.p.popMatrix();
//				
//				if(i == currentDate)					// Draw current date
//				{
//					p.p.fill(145, 255, 255, 255);
//					p.p.stroke(145, 255, 255, 255);
//					
//					p.p.pushMatrix();
//					beginHUD();
//					p.p.translate(x, y, hudDistance);
//					p.p.box(inc, 25.f, 0);
//					p.p.popMatrix();
//					
//					p.p.fill(55, 0, 255, 255);
//					p.p.stroke(55, 0, 255, 255);
//				}
//			}
		}
	}

	/**
	 * Draw progress bar
	 */
	void progressBar()
	{
		int length = 100;	// total length
		int pos = p.setupProgress;	//current position

		for(int i=0; i<pos; i++)
		{
			p.p.pushMatrix();
			beginHUD();
			
			p.p.fill(140, 100, 255);
			float xPos = PApplet.map(i, 0, length, 0, p.p.width * 1.f);
			float inc = PApplet.map(2, 0, length, 0, p.p.width * 1.f) - PApplet.map(1, 0, length, 0, p.p.width*1.f);
			int x = -p.p.width/2 + (int)xPos;
			int y = -p.p.height/2+p.p.height/2;

			p.p.translate(x, y, hudDistance);
			p.p.box(inc, inc*10.f, 1);    // Display 
			p.p.popMatrix();
		}
	}

	/**
	 * Initialize 2D drawing 
	 */
	void beginHUD()
	{
		float camInitFov = p.viewer.getInitFieldOfView();
		p.p.perspective(camInitFov, (float)p.p.width/(float)p.p.height, p.viewer.getNearClippingDistance(), 10000);
		
		PVector t = new PVector(p.viewer.camera.position()[0], p.viewer.camera.position()[1], p.viewer.camera.position()[2]);
		p.p.translate(t.x, t.y, t.z);
		p.p.rotateY(p.viewer.camera.attitude()[0]);
		p.p.rotateX(-p.viewer.camera.attitude()[1]);
		p.p.rotateZ(p.viewer.camera.attitude()[2]);
	}

	/**
	 * Display the main key commands on screen
	 */
	void displayControls()
	{
		p.p.pushMatrix();
		beginHUD();
		
		float xPos = centerTextXOffset;
		float yPos = topTextYOffset;			// Starting vertical position
		
		p.p.fill(0, 0, 255, 255);                        
		p.p.textSize(largeTextSize);
		p.p.text(" Keyboard Controls ", xPos, yPos, hudDistance);

		xPos = midLeftTextXOffset;
		p.p.textSize(mediumTextSize);
		p.p.text(" Main", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" R    Restart WorldMediaViewer", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" CMD + q    Quit WorldMediaViewer", xPos, yPos += lineWidth, hudDistance);

		p.p.textSize(mediumTextSize);
		p.p.text(" Display", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" 1    Show/Hide Field Map   		  +SHIFT to Overlay", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" 2    Show/Hide Field Statistics    +SHIFT to Overlay", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" 3    Show/Hide Cluster Statistics  +SHIFT to Overlay", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" 4    Show/Hide Keyboard Controls   +SHIFT to Overlay", xPos, yPos += lineWidth, hudDistance);

		p.p.textSize(mediumTextSize);
		p.p.text(" Time", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" T    Time Fading On/Off", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" D    Date Fading On/Off", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" Z    Toggle Time Fading Mode (Field/Cluster)", textXPos, textYPos += lineWidth, hudDistance);
		p.p.text(" space Pause On/Off   ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" &/*  Default Media Length - / +", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" SHIFT + Lt/Rt   Cycle Length - / +", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" SHIFT + Up/Dn   Current Time - / +", xPos, yPos += lineWidth, hudDistance);

		p.p.textSize(mediumTextSize);
		p.p.text(" Time Navigation", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" t    Teleport to Earliest Time in Field", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" T    Move to Earliest Time in Field", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" d    Teleport to Earliest Time on Earliest Date", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" D    Move to Earliest Time on Earliest Date", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" n    Move to Next Time Segment in Field", xPos, yPos += lineWidthWide, hudDistance);
		p.p.text(" N    Move to Next Time Segment in Cluster", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" b    Move to Previous Time Segment in Field", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" B    Move to Previous Time Segment in Cluster", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" l    Move to Next Date in Field", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" L    Move to Next Date in Cluster", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" k    Move to Previous Date in Field", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" K    Move to Previous Date in Cluster", xPos, yPos += lineWidth, hudDistance);

		xPos = centerTextXOffset;
		yPos = topTextYOffset;			// Starting vertical position

		/* Model */
		p.p.textSize(mediumTextSize);
		p.p.text(" Model", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" [ ]  Altitude Scaling Adjustment  + / - ", xPos, yPos += lineWidthVeryWide, hudDistance);
//		p.p.text(" , .  Object Distance  + / - ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" - =  Object Distance  - / +      ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" OPTION + -   Visible Angle  -      ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" OPTION + =   Visible Angle  +      ", xPos, yPos += lineWidth, hudDistance);
		
		/* Graphics */
		p.p.textSize(mediumTextSize);
		p.p.text(" Graphics", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" G    Angle Fading On/Off", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" H    Angle Thinning On/Off", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" P    Transparency Mode  On / Off      ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" ( )  Blend Mode  - / +      ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" i h v  Hide images / panoramas / videos    ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" D    Video Mode On/Off ", xPos, yPos += lineWidth, hudDistance);

		/* Movement */
		p.p.textSize(mediumTextSize);
		p.p.text(" Movement", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" a d w s   Walk Left / Right / Forward / Backward ", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" Arrows    Turn Camera ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" q z  Zoom In / Out + / - ", xPos, yPos += lineWidth, hudDistance);
		
		/* Navigation */
		p.p.textSize(mediumTextSize);
		p.p.text(" Navigation", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" >    Follow Timeline Only", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" .    Follow Timeline by Date", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" OPTION + .    Follow Dateline Only", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" E    Move to Nearest Cluster", xPos, yPos += lineWidthWide, hudDistance);
		p.p.text(" W    Move to Nearest Cluster in Front", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" Q    Move to Next Cluster in Time", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" A    Move to Next Location in Memory", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" Z    Move to Random Cluster", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" U    Move to Next Video ", xPos, yPos += lineWidthWide, hudDistance);
		p.p.text(" u    Teleport to Next Video ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" M    Move to Next Panorama ", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" m    Teleport to Next Panorama ", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" C    Lock Viewer to Nearest Cluster On/Off", xPos, yPos += lineWidthWide, hudDistance);
//		p.p.text(" l    Look At Selected Media", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" L    Look for Media", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" { }  Teleport to Next / Previous Field ", xPos, yPos += lineWidth, hudDistance);

		xPos = midRightTextXOffset;
		yPos = topTextYOffset;			// Starting vertical position

		p.p.textSize(mediumTextSize);
		p.p.text(" Interaction", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" O    Selection Mode On/Off", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" S    Multi-Selection Mode On/Off", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" OPTION + s    Segment Selection Mode On/Off", xPos, yPos += lineWidthWide, hudDistance);
		p.p.text(" x    Select Media in Front", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" X    Deselect Media in Front", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" OPTION + x    Deselect All Media", xPos, yPos += lineWidth, hudDistance);

		p.p.textSize(mediumTextSize);
		p.p.text(" GPS Tracks", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" g    Load GPS Track from File", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" OPTION + g    Follow GPS Track", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" y	  Navigate Memorized Places", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" Y    Clear Memory", xPos, yPos += lineWidth, hudDistance);

		p.p.textSize(mediumTextSize);
		p.p.text(" Memory", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" `    Save Current View to Memory", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" ~    Follow Memory Path", xPos, yPos += lineWidth, hudDistance);
		p.p.text(" Y    Clear Memory", xPos, yPos += lineWidth, hudDistance);

		p.p.textSize(mediumTextSize);
		p.p.text(" Output", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" o    Set Image Output Folder", xPos, yPos += lineWidthVeryWide, hudDistance);
		p.p.text(" p    Save Screen Image to Disk", xPos, yPos += lineWidth, hudDistance);
	
//		p.p.text(" F    Select Media in Front", xPos, yPos += lineWidth, hudDistance);
//		p.p.text(" 2 1 Camera Speed + / - ", dispLocX, textYPos += lineWidth * 2, hudDistance);
//		p.p.text(" 4 3 Background Brightness + / - ", dispLocX, textYPos += lineWidth, hudDistance);
//		p.p.text(" k j  Altitude Scale Factor  + / - ", dispLocX, textYPos += lineWidth, hudDistance);
//		p.p.text(" _ + Cluster Minimum Points - / +      ", dispLocX, textYPos += lineWidth, hudDistance);

		p.p.popMatrix();
	}
	
	void initializeLargeMap()
	{
		float fr = p.getCurrentField().model.fieldAspectRatio;			//	Field ratio == fieldWidth / fieldLength;

		if(fr > 1)
		{
			largeMapWidth = largeMapMaxWidth;
			largeMapHeight = largeMapWidth / fr;
		}
		else
		{
			largeMapHeight = largeMapMaxHeight;
			largeMapWidth = largeMapHeight * fr;
		}
	}

	void initializeMaps()
	{
		initializeLargeMap();
	}
	
	/**
	 * Add message to queue
	 * @param message Message to send
	 */
	void message(String message)
	{
		if(p.interactive)
		{
			messages.add(message);
			while(messages.size() > 16)
				messages.remove(0);
		}
		else
		{
			messageStartFrame = p.p.frameCount;		
			messages.add(message);
			while(messages.size() > 16)
				messages.remove(0);
		}

		if(p.p.debug.print)
			PApplet.println(message);
	}
	
	/**
	 * Clear previous messages
	 */
	void clearMessages()
	{
		messages = new ArrayList<String>();			
	}
	
	/**
	 * Display current messages
	 */
	void displayMessages()
	{
		float yPos = userMessageYOffset - lineWidth;

		p.p.pushMatrix();
		beginHUD();
		p.p.fill(0, 0, 255, 255);            								
		p.p.textSize(smallTextSize);

		if(p.interactive)
		{
			for(String s : messages)
				p.p.text(s, userMessageXOffset, yPos += lineWidth, hudDistance);		// Use period character to draw a point
		}
		else if(p.p.frameCount - messageStartFrame < messageDuration)
		{
			for(String s : messages)
				p.p.text(s, userMessageXOffset, yPos += lineWidth, hudDistance);		// Use period character to draw a point
		}
		else
		{
			clearMessages();														// Clear messages after duration has ended
		}

		p.p.popMatrix();
	}

	/**
	 * Add a metadata message (single line) to the display queue
	 * @param message Line of metadata 
	 */
	void metadata(String message)
	{
		metadataStartFrame = p.p.frameCount;		
		metadata.add(message);
		
		while(metadata.size() > 16)
			metadata.remove(0);
	}
	
	/**
	 * Draw current metadata messages to the screen
	 */
	void displayMetadata()
	{
//		if( !(p.viewer.lastMovementFrame == p.p.frameCount) )				// As long as the user doesn't move, display metadata
		{
			float yPos = metadataYOffset - lineWidth;
			
			p.p.pushMatrix();
			beginHUD();
			
			p.p.fill(0, 0, 255, 255);                     // White text
			p.p.textSize(mediumTextSize);

			for(String s : metadata)
			{
				p.p.text(s, leftTextXOffset, yPos += lineWidth, hudDistance);				// Use period character to draw a point
			}
			p.p.popMatrix();
		}
	}
	
	/**
	 * Clear previous metadata messages
	 */
	void clearMetadata()
	{
		metadata = new ArrayList<String>();							// Reset message list
	}
	
	/**
	 * @param message Message to be sent
	 * Add startup message to display queue
	 */
	void sendSetupMessage(String message)
	{
		if(initialSetup)																
		{
			startupMessageStartFrame = p.p.frameCount;		
			startupMessages.add(message);
			while(startupMessages.size() > 16)
				startupMessages.remove(0);

			if(p.p.debug.print)
				PApplet.println(message);
		}
	}
	
	/**
	 * Display startup messages in queue
	 */
	void displayStartupMessages()
	{
		float yPos = startupMessageYOffset - lineWidth;

		p.p.pushMatrix();
		beginHUD();
		p.p.fill(0, 0, 255, 255);            								
		p.p.textSize(largeTextSize * 1.5f);
		
		if(initialSetup)																// Showing setup startup messages
		{
			for(String s : startupMessages)
				p.p.text(s, startupMessageXOffset, yPos += lineWidth * 1.5f, hudDistance);		// Use period character to draw a point
		}
		else
			displayMessages();

		p.p.popMatrix();
	}
	
	/**
	 * Clear previous setup messages
	 */
	void clearSetupMessages()
	{
		startupMessages = new ArrayList<String>();
	}
	
	/**
	 * Reset (turn off) display modes and clear messages
	 */
	void resetDisplayModes()
	{
		map = false;
		mapOverlay = false;
		control = false;
		controlOverlay = false;
		cluster = false;
		clusterOverlay = false;
		info = false;
		infoOverlay = false;
		
		clearMessages();
		clearMetadata();
	}
	
	/**
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param mapXOffset Map X offset
	 * @param mapYOffset Map Y offset
	 */
	void drawMap(float mapWidth, float mapHeight, float mapXOffset, float mapYOffset)
	{
//		Map Modes  	1:  All   2: Clusters + Media   3: Clusters + Capture Locations  4: Capture Locations + Media
//					5:  Clusters Only   6: Media Only   7: Capture Locations Only
		
		/* Media */
		if((mapMode == 1 || mapMode == 2 || mapMode == 4 || mapMode == 6) && mapImages && !p.getCurrentField().hideImages)
			for ( WMV_Image i : p.getCurrentField().images )		// Draw images on 2D Map
				drawImageOnMap(i, mapWidth, mapHeight, false);

		if((mapMode == 1 || mapMode == 2 || mapMode == 4 || mapMode == 6) && mapPanoramas && !p.getCurrentField().hidePanoramas)
			for ( WMV_Panorama n : p.getCurrentField().panoramas )	// Draw panoramas on 2D Map
				drawPanoramaOnMap(n, mapWidth, mapHeight, false);

		if((mapMode == 1 || mapMode == 2 || mapMode == 4 || mapMode == 6) && mapVideos && !p.getCurrentField().hideVideos)
			for (WMV_Video v : p.getCurrentField().videos)			// Draw videos on 2D Map
				drawVideoOnMap(v, mapWidth, mapHeight, false);

		if((mapMode == 1 || mapMode == 3 || mapMode == 4 || mapMode == 7) && mapImages && !p.getCurrentField().hideImages)
			for ( WMV_Image i : p.getCurrentField().images )		// Draw image capture locations on 2D Map
				drawImageOnMap(i, mapWidth, mapHeight, true);

		if((mapMode == 1 || mapMode == 3 || mapMode == 4 || mapMode == 7) && mapPanoramas && !p.getCurrentField().hidePanoramas)
			for ( WMV_Panorama n : p.getCurrentField().panoramas )	// Draw panorama capture locations on 2D Map
				drawPanoramaOnMap(n, mapWidth, mapHeight, true);

		if((mapMode == 1 || mapMode == 3 || mapMode == 4 || mapMode == 7) && mapVideos && !p.getCurrentField().hideVideos)
			for (WMV_Video v : p.getCurrentField().videos)			// Draw video capture locations on 2D Map
				drawVideoOnMap(v, mapWidth, mapHeight, true);

		/* Clusters */
		if((mapMode == 1 || mapMode == 2 || mapMode == 3 || mapMode == 5) && mapClusters)
			for( WMV_Cluster c : p.getCurrentField().clusters )							
				drawFuzzyMapPoint( c.getLocation(), PApplet.sqrt(c.mediaPoints) * 0.5f, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mapMediaTransparency );

		if(!p.interactive)				// While not in Clustering Mode
		{
			if(mapMode == 1 || mapMode == 2 || mapMode == 3 || mapMode == 5)
			{
				if(p.viewer.getAttractorCluster() != -1 && p.viewer.getAttractorCluster() < p.getFieldClusters().size())
					drawMapPoint( p.getAttractorCluster().getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mapMediaTransparency );

				if(p.viewer.getCurrentCluster() != -1 && p.viewer.getCurrentCluster() < p.getFieldClusters().size())
					drawMapPoint( p.getCurrentCluster().getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mapMediaTransparency );
			}
			
			drawCameraOnMap(mapWidth, mapHeight);
		}
		
//		if(p.viewer.getPath().size() > 0)
//			drawPathOnMap(p.viewer.getPath(), mapWidth, mapHeight);
		if(p.viewer.getGPSTrack().size() > 0)
			drawPathOnMap(p.viewer.getGPSTrack(), mapWidth, mapHeight);
			
//		drawOriginOnMap(mapWidth, mapHeight);
	}

	/**
	 * @param image GMV_Image to draw
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param capture Draw capture location (true) or viewing location (false)
	 * Draw image location on map of specified size
	 */
	void drawImageOnMap(WMV_Image image, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = smallPointSize * mapWidth;
		
		float saturation;

		float imageDistance = image.getViewingDistance();   // Get photo distance from current camera position

		// IMPLEMENT TIME
		// p.p.fill(mapVideoHue, 100, 255, 255);                         // If out of visible range and not at a visible time, lightest color
		// p.p.stroke(mapVideoHue, 100, 255, 255);                                        

		if (imageDistance < p.viewer.getFarViewingDistance() && imageDistance > p.viewer.getNearClippingDistance())    // If image is in visible range
			saturation = 100.f;                                              
		else      																				// If out of visible range
			saturation = maxSaturation;                                              

		if(image.location != null && !image.disabled && !image.hidden)
		{
			if(capture)
				drawMapPoint( image.getCaptureLocation(), pointSize * (image.isSelected() ? 10.f : 1.f), mapWidth, mapHeight, mapImageCaptureHue, saturation, 255.f, mapMediaTransparency );
			else
				drawMapPoint( image.getLocation(), pointSize, mapWidth, mapHeight, mapImageHue, saturation, 255.f, mapMediaTransparency );
		}
		
	}

	/**
	 * @param panorama GMV_Panorama to draw
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw image location on map of specified size
	 */
	void drawPanoramaOnMap(WMV_Panorama panorama, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = mediumPointSize * mapWidth;
		
		float saturation = 255.f;
		float panoramaDistance = panorama.getViewingDistance();   // Get photo distance from current camera position

		// IMPLEMENT TIME
		// p.p.fill(mapVideoHue, 100, 255, 255);                         // If out of visible range and not at a visible time, lightest color
		// p.p.stroke(mapVideoHue, 100, 255, 255);                                        

		if (panoramaDistance < p.viewer.getFarViewingDistance() && panoramaDistance > p.viewer.getNearClippingDistance())    // If panorama is in visible range
			saturation = 100.f;                                              
		else      																				// If out of visible range
			saturation = maxSaturation;                                              

		if(panorama.location != null && !panorama.disabled && !panorama.hidden)
		{
			if(capture)
				drawMapPoint( panorama.getCaptureLocation(), pointSize * (panorama.isSelected() ? 10.f : 1.f), mapWidth, mapHeight, mapPanoramaCaptureHue, saturation, 255.f, mapMediaTransparency );
			else
				drawMapPoint( panorama.getLocation(), pointSize, mapWidth, mapHeight, mapPanoramaHue, saturation, 255.f, mapMediaTransparency );
		}
	}

	/**
	 * @param video GMV_Video to draw
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw image location on map of specified size
	 */
	void drawVideoOnMap(WMV_Video video, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = mediumPointSize * mapWidth;
		
		float saturation;
		float videoDistance = video.getViewingDistance();   // Get photo distance from current camera position

		// IMPLEMENT TIME
		// p.p.fill(mapVideoHue, 100, 255, 255);                         // If out of visible range and not at a visible time, lightest color
		// p.p.stroke(mapVideoHue, 100, 255, 255);                                        

		if (videoDistance < p.viewer.getFarViewingDistance() && videoDistance > p.viewer.getNearClippingDistance())    // If video is in visible range
			saturation = minSaturation;                                              
		else      																				// If out of visible range
			saturation = maxSaturation;                                              

		if(video.location != null && !video.disabled && !video.hidden)
		{
			if(capture)
				drawMapPoint( video.getCaptureLocation(), pointSize * (video.isSelected() ? 10.f : 1.f), mapWidth, mapHeight, mapVideoCaptureHue, saturation, 255.f, mapMediaTransparency );
			else
				drawMapPoint( video.getLocation(), pointSize, mapWidth, mapHeight, mapVideoHue, saturation, 255.f, mapMediaTransparency );
		}
	}

	/**
	 * Draw the map origin
	 * @param mapWidth
	 * @param mapHeight
	 */
	void drawOriginOnMap(float mapWidth, float mapHeight)
	{
		int size = (int)(mapWidth / 20.f);
		for(int i=-size/2; i<size/2; i+=size/20)
			drawMapPoint( new PVector(i, 0.f, 0.f), hugePointSize * mapWidth, mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
		for(int i=-size/2; i<size/2; i+=size/20)
			drawMapPoint( new PVector(0.f, 0.f, i), hugePointSize * mapWidth, mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
	}
	
	/**
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw current viewer location and orientation on map of specified size
	 */
	void drawCameraOnMap(float mapWidth, float mapHeight)
	{
		PVector camLoc = p.viewer.getLocation();
		float camYaw = -p.viewer.getXOrientation() - 0.5f * PApplet.PI;

		drawMapPoint( camLoc, cameraPointSize, mapWidth, mapHeight, mapCameraHue, 255.f, 255.f, mapMediaTransparency );
		float ptSize = cameraPointSize;
				
		ScaleMap logMap;
		logMap = new ScaleMap(6., 60., 6., 60.);		/* Time fading interpolation */
		logMap.setMapFunction(p.circularEaseOut);

		/* Change viewer arrow based on fieldWidth -- should be fieldLength??  -- should depend on zoom level too! */
//		int arrowLength = (int)logMap.p.getMappedValueFor( PApplet.map( p.getCurrentField().model.fieldWidth, 100.f, 10000.f, 6.f, 60.f ) );
		int arrowLength = 30;
		
		logMap = new ScaleMap(0.f, 0.25f, 0.f, 0.25f);		/* Time fading interpolation */
		logMap.setMapFunction(p.circularEaseOut);
		
		float shrinkFactor = PApplet.map(arrowLength, 6.f, 60.f, 0.f, 0.25f);
		shrinkFactor = (float)logMap.getMappedValueFor(shrinkFactor);
		shrinkFactor = 0.95f - (0.25f - shrinkFactor);		// Reverse mapping high to low
				
		for(int i=1; i<arrowLength; i++)
		{
			p.p.textSize(ptSize);
			float x = i * cameraPointSize * 0.5f * (float)Math.cos( camYaw );
			float y = i * cameraPointSize * 0.5f * (float)Math.sin( camYaw );
			
			PVector arrowPoint = new PVector(camLoc.x + x, 0, camLoc.z + y);
			drawMapPoint( arrowPoint, ptSize, mapWidth, mapHeight, mapCameraHue, 120.f, 255.f, 255.f );

			ptSize *= shrinkFactor;
		}
	}
	

	/**
	 * @param path Path to draw
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param capture Draw capture location (true) or viewing location (false)
	 * Draw image location on map of specified size
	 */
	void drawPathOnMap(ArrayList<WMV_Waypoint> path, float mapWidth, float mapHeight)
	{
		PApplet.println("drawPathOnMap..."+path.size());
		float pointSize = smallPointSize * mapWidth;
		
		float saturation = maxSaturation;                                              

		for(WMV_Waypoint w : path)
		{
			drawMapPoint( w.getLocation(), pointSize * 4.f, mapWidth, mapHeight, 30, saturation, 255.f, mapMediaTransparency );
			PApplet.println("Path ---> location.x:"+w.getLocation().x+" y:"+w.getLocation().y);
		}
	}

	/**
	 * Show startup screen
	 */
	public void showStartup()
	{
		sendSetupMessage("Welcome to WorldMediaViewer!");
		sendSetupMessage(" ");
		sendSetupMessage("Please select a library folder...");
		draw();								// Draw setup display
	}
	
	/**
	 * @param origin Vector starting point
	 * @param vector Direction vector
	 * Draw current viewer location and orientation on map of specified size
	 */
	void drawForceVector(PVector vector)
	{
		if(!drawForceVector)
			drawForceVector = true;

//		mapVectorOrigin = origin;
		mapVectorVector = vector;
	}

	/**
	 * Draw the clusters at given depth
	 */
	void drawGMVClusters()
	{		 
		if(  !initialSetup && !mapOverlay && !controlOverlay && !info 
				&& messages.size() < 0 && metadata.size() < 0	 )
		{
			p.p.hint(PApplet.DISABLE_DEPTH_TEST);						// Disable depth testing for drawing HUD
		}

		for( WMV_Cluster c : p.getCurrentField().clusters )								// For all clusters at current depth
		{
			drawMapPoint( c.getLocation(), 5.f, largeMapWidth, largeMapHeight, mapClusterHue, 255.f, 255.f, mapMediaTransparency );
		}
	}
	
	/**
	 * Draw camera attraction force vector
	 */
	private void drawForceVector()
	{
		mapVectorOrigin = p.viewer.getLocation();
		
		float ptSize = cameraPointSize * 2.f;
		drawMapPoint( mapVectorOrigin, ptSize, largeMapWidth, largeMapHeight, mapCameraHue, 255.f, 255.f, mapMediaTransparency );
		
		int arrowLength = 30;
		
		ScaleMap logMap;
		logMap = new ScaleMap(6., 60., 6., 60.);		/* Time fading interpolation */
		logMap.setMapFunction(p.circularEaseOut);

//		arrowLength = (int)logMap.p.getMappedValueFor(arrowLength);
		
		logMap = new ScaleMap(0.f, 0.25f, 0.f, 0.25f);		/* Time fading interpolation */
		logMap.setMapFunction(p.circularEaseOut);
		
		float shrinkFactor = PApplet.map(arrowLength, 6.f, 60.f, 0.f, 0.25f);
		shrinkFactor = (float)logMap.getMappedValueFor(shrinkFactor);
		shrinkFactor = 0.95f - (0.25f - shrinkFactor);		// Reverse mapping high to low
		PVector current = mapVectorVector;
		
		for(int i=1; i<arrowLength; i++)
		{
			p.p.textSize(ptSize);
			float mult = (i+6) * 0.025f;
			current = mapVectorVector.mult( mult );
			
			PVector arrowPoint = new PVector(mapVectorOrigin.x + current.x, 0, mapVectorOrigin.z + current.z);
			drawMapPoint( arrowPoint, ptSize, largeMapWidth, largeMapHeight, 255.f-mapCameraHue, 170.f, 255.f, 255.f );

			ptSize *= shrinkFactor;
		}
	}
	
	 /**
	  * Increment blendMode by given amount and call setBlendMode()
	  * @param inc Increment to blendMode number
	  */
	public void changeBlendMode(int inc) 
	{
		if(inc > 0)
		{
			if (blendMode+inc < numBlendModes) 	
				blendMode += inc;
			else 
				blendMode = 0;
		}
		else if(inc < 0)
		{
			{
				if (blendMode-inc >= 0) 
					blendMode -= inc;
				else 
					blendMode = numBlendModes - 1;
			}
		}

		if(inc != 0)
			setBlendMode(blendMode);
	}

	/**
	 * Change effect of image alpha channel on blending
	 * @param blendMode
	 */
	public void setBlendMode(int blendMode) {
		switch (blendMode) {
		case 0:
			p.p.blendMode(PApplet.BLEND);
			break;

		case 1:
			p.p.blendMode(PApplet.ADD);
			break;

		case 2:
			p.p.blendMode(PApplet.SUBTRACT);
			break;

		case 3:
			p.p.blendMode(PApplet.DARKEST);
			break;

		case 4:
			p.p.blendMode(PApplet.LIGHTEST);
			break;

		case 5:
			p.p.blendMode(PApplet.DIFFERENCE);
			break;

		case 6:
			p.p.blendMode(PApplet.EXCLUSION);
			break;

		case 7:
			p.p.blendMode(PApplet.MULTIPLY);
			break;

		case 8:
			p.p.blendMode(PApplet.SCREEN);
			break;

		case 9:
			p.p.blendMode(PApplet.REPLACE);
			break;

		case 10:
			// blend(HARD_LIGHT);
			break;

		case 11:
			// blend(SOFT_LIGHT);
			break;

		case 12:
			// blend(OVERLAY);
			break;

		case 13:
			// blend(DODGE);
			break;

		case 14:
			// blend(BURN);
			break;
		}

		if (p.p.debug.field)
			PApplet.println("blendMode:" + blendMode);
	}
	
	/**
	 * Show statistics of the current simulation
	 */
	void displayInfo()
	{
		p.p.pushMatrix();
		beginHUD();
		
		float xPos = centerTextXOffset;
		float yPos = topTextYOffset;			// Starting vertical position
		
		WMV_Field f = p.getCurrentField();
		
		if(p.viewer.getCurrentCluster() >= 0)
		{
			WMV_Cluster c = p.getCurrentCluster();
			float[] camTar = p.viewer.camera.target();

			p.p.fill(0, 0, 255, 255);
			p.p.textSize(largeTextSize);
			p.p.text(" WorldMediaViewer v1.0 ", xPos, yPos, hudDistance);
			p.p.textSize(mediumTextSize);

			xPos = midLeftTextXOffset;
			
			p.p.text(" Program Modes ", xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.textSize(smallTextSize);
			p.p.text(" Orientation Mode: "+p.orientationMode, xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.text(" Alpha Mode:"+p.alphaMode, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Time Fading: "+ p.timeFading, xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Date Fading: "+ p.dateFading, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Altitude Scaling: "+p.altitudeScaling, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Lock Media to Clusters:"+p.lockMediaToClusters, xPos, yPos += lineWidth, hudDistance);
		
			p.p.textSize(mediumTextSize);
			p.p.text(" Graphics ", xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.textSize(smallTextSize);
			p.p.text(" Alpha:"+p.alpha, xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.text(" Default Media Length:"+p.defaultMediaLength, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Media Angle Fading: "+p.angleFading, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Media Angle Thinning: "+p.angleThinning, xPos, yPos += lineWidth, hudDistance);
			if(p.angleThinning)
				p.p.text(" Media Thinning Angle:"+p.thinningAngle, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Image Size Factor:"+p.subjectSizeRatio, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Subject Distance (m.):"+p.defaultFocusDistance, xPos, yPos += lineWidth, hudDistance);
//			p.p.text(" Image Size Factor:"+p.subjectSizeRatio, xPos, yPos += lineWidth, hudDistance);

			xPos = centerTextXOffset;
			yPos = topTextYOffset;			// Starting vertical position

			p.p.textSize(mediumTextSize);
			p.p.text(" Field", xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.textSize(smallTextSize);
			p.p.text(" Name: "+f.name, xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.text(" ID: "+(p.viewer.getField()+1)+" out of "+p.getFieldCount()+" Total Fields", xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Width (m.): "+f.model.fieldWidth+" Length (m.): "+f.model.fieldLength+" Height (m.): "+f.model.fieldHeight, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Total Images: "+f.getImageCount(), xPos, yPos += lineWidth, hudDistance);					// Doesn't check for dataMissing!!
			p.p.text(" Total Panoramas: "+f.getPanoramaCount(), xPos, yPos += lineWidth, hudDistance);			// Doesn't check for dataMissing!!
			p.p.text(" Total Videos: "+f.getVideoCount(), xPos, yPos += lineWidth, hudDistance);					// Doesn't check for dataMissing!!
			p.p.text(" Media Density per sq. m.: "+f.model.mediaDensity, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Images Visible: "+f.imagesVisible, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Panoramas Visible: "+f.panoramasVisible, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Videos Visible: "+f.videosVisible, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Videos Playing: "+f.videosPlaying, xPos, yPos += lineWidth, hudDistance);
			if(p.orientationMode)
				p.p.text(" Clusters Visible: "+p.viewer.clustersVisible+"  (Orientation Mode)", xPos, yPos += lineWidth, hudDistance);

			p.p.textSize(mediumTextSize);
			p.p.text(" Model ", xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.textSize(smallTextSize);
			
			p.p.text(" Clusters:"+(f.clusters.size()-f.model.mergedClusters), xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.text(" Merged: "+f.model.mergedClusters+" out of "+f.clusters.size()+" Total", xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Minimum Distance: "+p.minClusterDistance, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Maximum Distance: "+p.maxClusterDistance, xPos, yPos += lineWidth, hudDistance);
			if(p.altitudeScaling)
				p.p.text(" Altitude Scaling Factor: "+p.altitudeScalingFactor+"  (Altitude Scaling)", xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.text(" Clustering Method : "+ ( p.hierarchical ? "Hierarchical" : "K-Means" ), xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Population Factor: "+f.model.clusterPopulationFactor, xPos, yPos += lineWidth, hudDistance);
			if(p.hierarchical) p.p.text(" Current Cluster Depth: "+f.model.clusterDepth, xPos, yPos += lineWidth, hudDistance);

			p.p.textSize(mediumTextSize);
			p.p.text(" Viewer ", xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.textSize(smallTextSize);
			p.p.text(" Location, x: "+PApplet.round(p.viewer.getLocation().x)+" y:"+PApplet.round(p.viewer.getLocation().y)+" z:"+
					 PApplet.round(p.viewer.getLocation().z), xPos, yPos += lineWidthVeryWide, hudDistance);		
			p.p.text(" GPS Longitude: "+p.viewer.getGPSLocation().x+" Latitude:"+p.viewer.getGPSLocation().y, xPos, yPos += lineWidth, hudDistance);		

			p.p.text(" Current Cluster: "+p.viewer.getCurrentCluster(), xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.text("   Media Points: "+c.mediaPoints, xPos, yPos += lineWidth, hudDistance);
			p.p.text("   Media Segments: "+p.getCurrentCluster().segments.size(), xPos, yPos += lineWidth, hudDistance);
			p.p.text("   Distance: "+PApplet.round(PVector.dist(c.getLocation(), p.viewer.getLocation())), xPos, yPos += lineWidth, hudDistance);
			p.p.text("   Auto Stitched Panoramas: "+p.getCurrentCluster().stitchedPanoramas.size(), xPos, yPos += lineWidth, hudDistance);
			p.p.text("   User Stitched Panoramas: "+p.getCurrentCluster().userPanoramas.size(), xPos, yPos += lineWidth, hudDistance);
			if(p.viewer.getAttractorCluster() != -1)
			{
				p.p.text(" Destination Cluster : "+p.viewer.getAttractorCluster(), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Destination Media Points: "+p.getCluster(p.viewer.getAttractorCluster()).mediaPoints, xPos, yPos += lineWidth, hudDistance);
				p.p.text("    Destination Distance: "+PApplet.round( PVector.dist(f.clusters.get(p.viewer.getAttractorCluster()).getLocation(), p.viewer.getLocation() )), xPos, yPos += lineWidth, hudDistance);
			}

			if(p.p.debug.viewer) 
			{
				p.p.text(" Debug: Current Attraction: "+p.viewer.attraction.mag(), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Debug: Current Acceleration: "+(p.viewer.isWalking() ? p.viewer.walkingAcceleration.mag() : p.viewer.acceleration.mag()), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Debug: Current Velocity: "+ (p.viewer.isWalking() ? p.viewer.walkingVelocity.mag() : p.viewer.velocity.mag()) , xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Debug: Moving? " + p.viewer.isMoving(), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Debug: Slowing? " + p.viewer.isSlowing(), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Debug: Halting? " + p.viewer.isHalting(), xPos, yPos += lineWidth, hudDistance);
			}

			if(p.p.debug.viewer)
			{
				p.p.text(" Debug: X Orientation (Yaw):" + p.viewer.getXOrientation(), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Debug: Y Orientation (Pitch):" + p.viewer.getYOrientation(), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Debug: Target Point x:" + camTar[0] + ", y:" + camTar[1] + ", z:" + camTar[2], xPos, yPos += lineWidth, hudDistance);
			}
			else
			{
				p.p.text(" Compass Direction:" + p.p.utilities.angleToCompass(p.viewer.getXOrientation())+" Angle: "+p.viewer.getXOrientation(), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Vertical Direction:" + PApplet.degrees(p.viewer.getYOrientation()), xPos, yPos += lineWidth, hudDistance);
				p.p.text(" Zoom:"+p.viewer.camera.fov(), xPos, yPos += lineWidth, hudDistance);
			}
			p.p.text(" Field of View:"+p.viewer.camera.fov(), xPos, yPos += lineWidth, hudDistance);

			xPos = midRightTextXOffset;
			yPos = topTextYOffset;			// Starting vertical position

			p.p.textSize(mediumTextSize);
			p.p.text(" Time ", xPos, yPos += lineWidthVeryWide, hudDistance);
			p.p.textSize(smallTextSize);
			p.p.text(" Time Mode: "+ ((p.p.world.timeMode == 0) ? "Cluster" : "Field"), xPos, yPos += lineWidthVeryWide, hudDistance);
			
			if(p.p.world.timeMode == 0)
				p.p.text(" Current Field Time: "+ p.currentTime, xPos, yPos += lineWidth, hudDistance);
			if(p.p.world.timeMode == 1)
				p.p.text(" Current Cluster Time: "+ p.getCurrentCluster().currentTime, xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Current Field Timeline Segments: "+ p.getCurrentField().timeline.size(), xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Current Field Time Segment: "+ p.viewer.currentFieldTimeSegment, xPos, yPos += lineWidth, hudDistance);
			if(f.timeline.size() > 0 && p.viewer.currentFieldTimeSegment >= 0 && p.viewer.currentFieldTimeSegment < f.timeline.size())
				p.p.text(" Upper: "+f.timeline.get(p.viewer.currentFieldTimeSegment).getUpper().getTime()
						+" Center:"+f.timeline.get(p.viewer.currentFieldTimeSegment).getCenter().getTime()+
						" Lower: "+f.timeline.get(p.viewer.currentFieldTimeSegment).getLower().getTime(), xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Current Cluster Timeline Segments: "+ p.getCurrentCluster().timeline.size(), xPos, yPos += lineWidth, hudDistance);
			p.p.text(" Field Dateline Segments: "+ p.getCurrentField().dateline.size(), xPos, yPos += lineWidth, hudDistance);
			p.p.textSize(mediumTextSize);

			if(p.p.debug.memory)
			{
				if(p.p.debug.detailed)
				{
					p.p.text("Total memory (bytes): " + p.p.debug.totalMemory, xPos, yPos += lineWidth, hudDistance);
					p.p.text("Available processors (cores): "+p.p.debug.availableProcessors, xPos, yPos += lineWidth, hudDistance);
					p.p.text("Maximum memory (bytes): " +  (p.p.debug.maxMemory == Long.MAX_VALUE ? "no limit" : p.p.debug.maxMemory), xPos, yPos += lineWidth, hudDistance); 
					p.p.text("Total memory (bytes): " + p.p.debug.totalMemory, xPos, yPos += lineWidth, hudDistance);
					p.p.text("Allocated memory (bytes): " + p.p.debug.allocatedMemory, xPos, yPos += lineWidth, hudDistance);
				}
				p.p.text("Free memory (bytes): "+p.p.debug.freeMemory, xPos, yPos += lineWidth, hudDistance);
				p.p.text("Approx. usable free memory (bytes): " + p.p.debug.approxUsableFreeMemory, xPos, yPos += lineWidth, hudDistance);
			}			
		}
		else
			message("Can't display statistics: currentCluster == "+p.viewer.getCurrentCluster()+"!!!");
		
		p.p.popMatrix();
	}

	/**
	 * Draw cluster statistics display
	 */
	void displayClusterInfo()
	{
		p.p.pushMatrix();
		beginHUD();
		
		float textXPos = centerTextXOffset;
		float textYPos = topTextYOffset;			// Starting vertical position
		
		WMV_Field f = p.getCurrentField();
		WMV_Cluster c = p.getCluster(displayCluster);	// Get the cluster to display info about

		p.p.fill(0, 0, 255, 255);

		p.p.textSize(mediumTextSize);
		p.p.text(""+p.getCurrentField().name+ " Media Clusters", textXPos, textYPos, hudDistance);
		p.p.textSize(smallTextSize);
		p.p.text(" Clusters:"+(f.clusters.size()-f.model.mergedClusters), textXPos, textYPos += lineWidthVeryWide, hudDistance);
		p.p.text(" Merged: "+f.model.mergedClusters+" out of "+f.clusters.size()+" Total", textXPos, textYPos += lineWidth, hudDistance);
		if(p.hierarchical) p.p.text(" Current Cluster Depth: "+f.model.clusterDepth, textXPos, textYPos += lineWidth, hudDistance);
		p.p.text(" Minimum Distance: "+p.minClusterDistance, textXPos, textYPos += lineWidth, hudDistance);
		p.p.text(" Maximum Distance: "+p.maxClusterDistance, textXPos, textYPos += lineWidth, hudDistance);
		p.p.text(" Population Factor: "+f.model.clusterPopulationFactor, textXPos, textYPos += lineWidth, hudDistance);
		p.p.text(" ID: "+ c.getID(), textXPos, textYPos += lineWidthVeryWide, hudDistance);
		p.p.text(" Location: "+ c.getLocation(), textXPos, textYPos += lineWidth, hudDistance);
		p.p.text(" Media Points: "+ c.mediaPoints, textXPos, textYPos += lineWidth, hudDistance);
		p.p.text(" Auto Stitched Panoramas: "+p.getCurrentCluster().stitchedPanoramas.size(), textXPos, textYPos += lineWidth, hudDistance);
		p.p.text(" User Stitched Panoramas: "+p.getCurrentCluster().userPanoramas.size(), textXPos, textYPos += lineWidth, hudDistance);
		p.p.text(" Media Segments: "+ c.segments.size(), textXPos, textYPos += lineWidth, hudDistance);
		
		if(c.timeline.size() > 0)
			p.p.text(" Timeline Segments: "+ c.timeline.size(), textXPos, textYPos += lineWidthWide, hudDistance);
		if(c.dateline != null)
			if(c.dateline.size() > 0)
				p.p.text(" Dateline Segments: "+ c.dateline.size(), textXPos, textYPos += lineWidth, hudDistance);
		p.p.text(" ", textXPos, textYPos += lineWidth, hudDistance);
		p.p.text(" Active: "+ c.isActive(), textXPos, textYPos += lineWidth, hudDistance);
		p.p.text(" Single: "+ c.isSingle(), textXPos, textYPos += lineWidth, hudDistance);
		p.p.text(" Empty: "+ c.isEmpty(), textXPos, textYPos += lineWidth, hudDistance);
		p.p.text(" ", textXPos, textYPos += lineWidth, hudDistance);
		p.p.text(" Viewer Distance: "+PApplet.round(PVector.dist(c.getLocation(), p.viewer.getLocation())), textXPos, textYPos += lineWidth, hudDistance);

		WMV_Cluster cl = p.getCurrentCluster();
		p.p.text(" Current Cluster ID: "+p.viewer.getCurrentCluster(), textXPos, textYPos += lineWidthVeryWide, hudDistance);
		p.p.text("   Media Points: "+cl.mediaPoints, textXPos, textYPos += lineWidth, hudDistance);
		p.p.text("   Viewer Distance: "+PApplet.round(PVector.dist(cl.getLocation(), p.viewer.getLocation())), textXPos, textYPos += lineWidth, hudDistance);
		
		if(p.viewer.getAttractorCluster() != -1)
		{
			p.p.text(" Destination Cluster ID: "+p.viewer.getAttractorCluster(), textXPos, textYPos += lineWidth, hudDistance);
			p.p.text("    Destination Distance: "+PApplet.round( PVector.dist(f.clusters.get(p.viewer.getAttractorCluster()).getLocation(), p.viewer.getLocation() )), textXPos, textYPos += lineWidth, hudDistance);
			if(p.p.debug.viewer) 
			{
				p.p.text(" Debug: Current Attraction:"+p.viewer.attraction.mag(), textXPos, textYPos += lineWidth, hudDistance);
				p.p.text(" Debug: Current Acceleration:"+(p.viewer.isWalking() ? p.viewer.walkingAcceleration.mag() : p.viewer.acceleration.mag()), textXPos, textYPos += lineWidth, hudDistance);
				p.p.text(" Debug: Current Velocity:"+ (p.viewer.isWalking() ? p.viewer.walkingVelocity.mag() : p.viewer.velocity.mag()) , textXPos, textYPos += lineWidth, hudDistance);
			}
		}

		p.p.popMatrix();
		
		drawClusterImages(c);
	}

	private void drawClusterImages(WMV_Cluster cluster)
	{
		int count = 1;
		float imgXPos = clusterImageXOffset;
		float imgYPos = clusterImageYOffset;			// Starting vertical position

		p.p.stroke(255, 255, 255);
		p.p.strokeWeight(15);
		p.p.fill(0, 0, 255, 255);

		for(WMV_Image i : cluster.getImages())
		{
			p.p.pushMatrix();
			beginHUD();
			float origWidth = i.getWidth();
			float origHeight = i.getHeight();
			float width = 90.f;
			float height = width * origHeight / origWidth;
			
			p.p.translate(imgXPos, imgYPos, hudDistance);
			p.p.tint(255);
			
			if(count < 60)
			{
				PImage image = p.p.loadImage(i.filePath);
				p.p.image(image, 0, 0, width, height);
			}
			
			imgXPos += width * 1.5f;

			if(count % 14 == 0)
			{
				imgXPos = clusterImageXOffset;
				imgYPos += height * 1.5f;
			}
			
			count++;
			p.p.popMatrix();
		}
	}
	
	public boolean inDisplayView()
	{
		if( map || control || info || about || cluster || p.interactive || mapOverlay || controlOverlay || infoOverlay || clusterOverlay )
			return true;
		else 
			return false;
	}

	/**
	 * Draw Heads-Up Display of 2D Map and Logo
	 */
	void drawSmallMap()
	{
		
	}
	
	void setFullScreen(boolean newState)
	{
		if(newState && !fullscreen)			// Switch to Fullscreen
		{
			if(!p.viewer.selection) window.mainSidebar.setVisible(false);	
			else window.selectionSidebar.setVisible(false);
		}
		if(!newState && fullscreen)			// Switch to Window Size
		{
			if(!p.viewer.selection) window.mainSidebar.setVisible(true);	
			else window.selectionSidebar.setVisible(true);
		}
		
		fullscreen = newState;
	}
	
	/**
	 * Interesting effect
	 * @param mapWidth
	 * @param mapHeight
	 */
	void drawMandalaOnMap(float mapWidth, float mapHeight)
	{
		int size = (int)(mapWidth / 20.f);
		for(int i=-size/2; i<size/2; i+=size/20)
			drawMapPoint( new PVector(i, 0.f, 0.f), hugePointSize * mapWidth * 20.f / (i+size/2), mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
		for(int i=-size/2; i<size/2; i+=size/20)
			drawMapPoint( new PVector(0.f, 0.f, i), hugePointSize * mapWidth * 20.f / (i+size/2), mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
	}
	
	public void handleSliderEvent(GSlider slider)
	{
		if (slider.tag == "Alpha") 
		{
			p.alpha = slider.getValueF();
		}

		if (slider.tag == "MediaLength") 
		{
			p.defaultMediaLength = slider.getValueI();
		}
	}
	
	public void handleButtonEvent(GButton button, GEvent event) 
	{ 
		  boolean state;
		  
//		  PApplet.println("button.tagNo:"+button.tagNo);
//		  PApplet.println("button.tag:"+button.tag);
		  
		  switch(button.tag) 
		  {
				/* Display Modes */
		  		case "Scene":
		  			resetDisplayModes();
					break;
		  		case "Map":
					resetDisplayModes();
					map = true;
					break;
		  		case "Info":
					resetDisplayModes();
					info = true;
					break;
		  		case "Cluster":
					resetDisplayModes();
					cluster = true;
					break;
		  		case "Control":
					resetDisplayModes();
					control = true;
					break;
				
				/* General */
		  		case "Restart":
					p.p.restartWorldMediaViewer();
		  			break;

		  		case "SelectionMode":
					p.viewer.selection = true;
					window.setupSelectionSidebar();
		  			break;

		  		case "StatisticsView":
		  			sidebarStatistics = true;
					window.setupStatisticsSidebar();
		  			break;

		  		case "ExitSelectionMode":
					p.viewer.selection = false;
					window.setupMainSidebar();
					break;

		  		case "ExitStatisticsMode":
					sidebarStatistics = false;
					window.setupMainSidebar();
					break;
					
				/* Graphics */
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

				/* Navigation */
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
			case "TimeFading":
				p.timeFading = option.isSelected();
//				p.timeFading = !p.timeFading;
				break;
			case "MovementTeleport":
				p.viewer.movementTeleport = option.isSelected();
				break;
			case "FollowTeleport":
//				p.timeFading = option.isSelected();
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
		}
	}
	
	public void handleSliderEvent(GValueControl slider, GEvent event) 
	{ 
//		  if (slider == sdr)  // The slider being configured?
//		    println(sdr.getValueS() + "    " + event);    
//		  if (slider == sdrEasing)
//		    sdr.setEasing(slider.getValueF());    
//		  else if (slider == sdrNbrTicks)
//		    sdr.setNbrTicks(slider.getValueI());    
//		  else if (slider == sdrBack)
//		    bgcol = slider.getValueI();
		}

}

