package wmViewer;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import toxi.math.ScaleMap;
import shapes3d.*;

/***********************************
 * @author davidgordon
 * Class for displaying interactive 2D map of media-based virtual environment
 */

public class WMV_Map 
{
	/* Interaction */
	private int selectedCluster = -1;
	ArrayList<Ellipsoid> selectableClusters;
	
	/* Graphics */
	PGraphics simpleClusters;
	private boolean simpleClustersCreated = false;

	public float mapDistance = 1.f;
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

	private float fieldRatio;
	
	float largeMapWidth, largeMapHeight, largeMapMaxWidth, largeMapMaxHeight;
	float largeMapXOffset, largeMapYOffset;
	float largeMapZoomLevel = 1.f;

	float zoomMapLeftEdge = 0.f, zoomMapTopEdge = 0.f;
	float zoomMapWidth, zoomMapHeight, zoomMapMaxWidth, zoomMapMaxHeight;
	float zoomMapXOffset, zoomMapYOffset;

	float smallMapWidth, smallMapHeight, smallMapMaxWidth, smallMapMaxHeight;
	float smallMapXOffset, smallMapYOffset;

	float smallPointSize;
	float mediumPointSize;
	float largePointSize;
	float hugePointSize;
	float cameraPointSize;

	public boolean beginZoomTransition = false, zoomTransition = false;
	private int zoomTransitionStartFrame = 0, zoomTransitionEndFrame = 0, zoomTransitionLength = 14;	
	private float zoomTransitionStart, zoomTransitionTarget;

	public boolean beginScrollTransition = false, scrollTransition = false;
	private int scrollTransitionStartFrame = 0, scrollTransitionEndFrame = 0, scrollTransitionLength = 16;	
	private float mapXTransitionStart, mapXTransitionTarget, mapYTransitionStart, mapYTransitionTarget;

	PVector mapVectorOrigin, mapVectorVector;

	WMV_Display p;

	WMV_Map(WMV_Display parent)
	{
		p = parent;

		largeMapXOffset = -p.p.p.width * 0.5f;
		largeMapYOffset = -p.p.p.height * 0.5f;
		largeMapMaxWidth = p.p.p.height * 0.95f;
//		largeMapMaxWidth = p.p.p.width * 0.95f;
		largeMapMaxHeight = p.p.p.height * 0.95f;

		smallMapMaxWidth = p.p.p.width / 4.f;
		smallMapMaxHeight = p.p.p.height / 4.f;

		smallPointSize = 0.0000022f * p.p.p.width;
		mediumPointSize = 0.0000028f * p.p.p.width;
		largePointSize = 0.0000032f * p.p.p.width;
		hugePointSize = 0.0000039f * p.p.p.width;
		cameraPointSize = 0.005f * p.p.p.width;
	}

	void initializeLargeMap()
	{
		fieldRatio = p.p.getCurrentField().model.fieldAspectRatio;			//	Field ratio == fieldWidth / fieldLength;

		largeMapWidth = largeMapMaxWidth;
		largeMapHeight = largeMapMaxHeight;

		zoomToRectangle(0, 0, largeMapMaxWidth, largeMapMaxHeight);
	}

	void initializeMaps()
	{
		initializeLargeMap();
		p.initializedMaps = true;
//		initializeItemSelector = true;
	}
	
	void reset()
	{
		
	}
	
	/**
	 * Create selectable circle representing each cluster
	 * @param mapWidth
	 * @param mapHeight
	 */
	void createSelectableClusters(float mapWidth, float mapHeight)
	{
		selectableClusters = new ArrayList<Ellipsoid>();
		
		for( WMV_Cluster c : p.p.getCurrentField().clusters )	
		{
			if(!c.isEmpty() && c.mediaPoints != 0)
			{
				Ellipsoid ellipsoid = new Ellipsoid(p.p.p, 5, 5);
				float radius = PApplet.sqrt(c.mediaPoints) * 0.7f;
				radius *= mapDistance;
				ellipsoid.setRadius(radius);
				ellipsoid.drawMode(S3D.SOLID);
				ellipsoid.fill(p.p.p.color(105.f, 225.f, 200.f, mapMediaTransparency));
				ellipsoid.fill(p.p.p.color(105.f, 225.f, 200.f, 255.f));
				ellipsoid.strokeWeight(0.f);

				WMV_Model m = p.p.getCurrentField().model;
				float mapLocX, mapLocY, mapLocZ;
				PVector point = c.getLocation();
				if(fieldRatio >= 1.f)					//	Field ratio == fieldWidth / fieldLength;
				{
					mapLocX = PApplet.map( point.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapWidth );		
					mapLocY = PApplet.map( point.z, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapHeight );
				}
				else
				{
					mapLocX = PApplet.map( point.x, -0.5f * m.fieldLength, 0.5f*m.fieldLength, 0, mapWidth );		
					mapLocY = PApplet.map( point.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, 0, mapHeight );
				}
				mapLocZ = 0.f;
				
				PVector mapLoc = new PVector(mapLocX, mapLocY, mapLocZ);
				
				if( mapLoc.x > 0 && mapLoc.x < zoomMapWidth && mapLoc.y > 0 && mapLoc.y < zoomMapHeight )	//-- Makes clusters unselectable!??
				{
					mapLoc.add(new PVector(mapLeftEdge, mapTopEdge, 0));
					mapLoc.add(new PVector(largeMapXOffset, largeMapYOffset, p.hudDistance * mapDistance));
					ellipsoid.moveTo(mapLoc.x, mapLoc.y, mapLoc.z);
					ellipsoid.tagNo = c.getID();
					selectableClusters.add(ellipsoid);
				}
//				else
//				{
//					PApplet.println("hidden:"+mapLoc.x+" "+mapLoc.y+" "+mapLoc.z+" zoomMapWidth:"+zoomMapWidth+" zoomMapHeight:"+zoomMapHeight);
//				}
			}
		}
	}
	
	void drawSelectableClusters()
	{
		p.p.p.pushMatrix();
		for(Ellipsoid e : selectableClusters)
			e.draw();
		p.p.p.popMatrix();
	}
	
	/**
	 * Draw large map
	 */
	void drawLargeMap()
	{
		p.p.p.pushMatrix();
		p.beginHUD();									// Begin 2D drawing
		
		p.p.p.fill(55, 0, 255, 255);
		p.p.p.textSize(p.largeTextSize);
		float textXPos = p.centerTextXOffset;
		float textYPos = p.topTextYOffset;

		if(p.p.interactive)
			p.p.p.text("Interactive "+(p.p.hierarchical ? "Hierarchical" : "K-Means")+" Clustering", textXPos, textYPos, p.hudDistance);
		else
			p.p.p.text(p.p.getCurrentField().name, textXPos, textYPos, p.hudDistance);

		p.p.p.popMatrix();
		
		drawMap(largeMapWidth, largeMapHeight, largeMapXOffset, largeMapYOffset);
	}
	
	/**
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param mapXOffset Map X offset
	 * @param mapYOffset Map Y offset
	 */
	void drawMap(float mapWidth, float mapHeight, float mapXOffset, float mapYOffset)
	{
		if(!zoomTransition && !scrollTransition)
			createSelectableClusters(mapWidth, mapHeight);
		
		p.beginHUD();

		/* Media */
		if(!zoomTransition && !scrollTransition)
		{
			if((mapImages && !p.p.getCurrentField().hideImages))
				for ( WMV_Image i : p.p.getCurrentField().images )			// Draw image capture locations on 2D Map
					drawImageOnMap(i, false, mapWidth, mapHeight, false);

			if((mapPanoramas && !p.p.getCurrentField().hidePanoramas))
				for ( WMV_Panorama n : p.p.getCurrentField().panoramas )	// Draw panorama capture locations on 2D Map
					drawPanoramaOnMap(n, false, mapWidth, mapHeight, false);

			if((mapVideos && !p.p.getCurrentField().hideVideos))
				for (WMV_Video v : p.p.getCurrentField().videos)			// Draw video capture locations on 2D Map
					drawVideoOnMap(v, false, mapWidth, mapHeight, false);
		}

		/* Clusters */
		if(mapClusters)
		{
			if(!zoomTransition && !scrollTransition)
			{
				drawSelectableClusters();
				
				if(selectedCluster >= 0)
				{
					WMV_Cluster c = p.p.getCluster(selectedCluster);
					if(!c.isEmpty() && c.mediaPoints != 0)
						highlightCluster( c.getLocation(), PApplet.sqrt(c.mediaPoints)*0.5f, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mapMediaTransparency );
					
					if((mapImages && !p.p.getCurrentField().hideImages))
						for ( int i : c.images )									// Draw images on Map
						{
							WMV_Image img = p.p.getCurrentField().images.get(i);
							drawImageOnMap(img, true, mapWidth, mapHeight, false);
						}

					if((mapPanoramas && !p.p.getCurrentField().hidePanoramas))
						for ( int n : c.panoramas )									// Draw panoramas on Map
						{
							WMV_Panorama pano = p.p.getCurrentField().panoramas.get(n);
							drawPanoramaOnMap(pano, true, mapWidth, mapHeight, false);
						}

					if((mapVideos && !p.p.getCurrentField().hideVideos))
						for (int v : c.videos)										// Draw videos on Map
						{
							WMV_Video vid = p.p.getCurrentField().videos.get(v);
							drawVideoOnMap(vid, true, mapWidth, mapHeight, false);
						}
				}
			}
			else
			{
				if(simpleClustersCreated)
				{
					drawSimpleClusters(mapWidth, mapHeight);
				}
				else
				{
					createSimpleClusters(mapWidth, mapHeight);
					drawSimpleClusters(mapWidth, mapHeight);					
				}
			}
		}
		
		if(!zoomTransition && !scrollTransition)
		{
			if(!p.p.interactive)										// While not in Clustering Mode
			{
				if(p.p.viewer.getAttractorCluster() != -1 && p.p.viewer.getAttractorCluster() < p.p.getFieldClusters().size())
					drawPoint( p.p.getAttractorCluster().getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mapMediaTransparency );

				if(p.p.viewer.getCurrentCluster() != -1 && p.p.viewer.getCurrentCluster() < p.p.getFieldClusters().size())
					drawPoint( p.p.getCurrentCluster().getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mapMediaTransparency );

				drawCameraOnMap(mapWidth, mapHeight);
			}

			if(p.p.viewer.getGPSTrack().size() > 0)
				drawPathOnMap(p.p.viewer.getGPSTrack(), mapWidth, mapHeight);
			
			if(p.p.p.debug.map)
				drawMapBorder(mapWidth, mapHeight);
			
//			drawOriginOnMap(mapWidth, mapHeight);
		}
	}
	
	void createSimpleClusters(float mapWidth, float mapHeight)
	{
		int pgWidth = PApplet.round(mapWidth);
		int pgHeight = PApplet.round(mapHeight);
		simpleClusters = p.p.p.createGraphics(pgWidth, pgHeight, PApplet.P3D);
		for( WMV_Cluster c : p.p.getCurrentField().clusters )	
			if(!c.isEmpty() && c.mediaPoints > 4)
			{
				float mapLocX, mapLocY;
				WMV_Model m = p.p.getCurrentField().model;
				PVector point = c.getLocation();
				
				if(!p.p.p.utilities.isNaN(point.x) && !p.p.p.utilities.isNaN(point.y) && !p.p.p.utilities.isNaN(point.z))
				{
					/* Find map coordinates for point */
					if(fieldRatio >= 1.f)					//	Field ratio == fieldWidth / fieldLength;
					{
						mapLocX = PApplet.map( point.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapWidth );		
						mapLocY = PApplet.map( point.z, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapHeight );
					}
					else
					{
						mapLocX = PApplet.map( point.x, -0.5f * m.fieldLength, 0.5f*m.fieldLength, 0, mapWidth );		
						mapLocY = PApplet.map( point.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, 0, mapHeight );
					}
//					mapLocX = PApplet.map( point.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapWidth );		
//					mapLocY = PApplet.map( point.z, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapHeight );

					if(mapLocX < mapWidth && mapLocX > 0 && mapLocY < mapHeight && mapLocY > 0)
					{
						simpleClusters.stroke(mapClusterHue, 255.f, 255.f, 255.f);
//						simpleClusters.strokeWeight(PApplet.sqrt(c.mediaPoints) * 0.85f);
//						float sw = PApplet.sqrt(c.mediaPoints) * 0.7f;
						float sw = PApplet.sqrt(c.mediaPoints) * 0.5f;
						sw *= mapDistance;
						simpleClusters.strokeWeight(sw);

						simpleClusters.beginDraw();
						simpleClusters.pushMatrix();
						simpleClusters.point(mapLocX, mapLocY, 0.f);
						simpleClusters.popMatrix();
						simpleClusters.endDraw();
					}
				}
			}
		
		simpleClustersCreated = true;
	}
	
	void drawSimpleClusters(float mapWidth, float mapHeight)
	{
//		for( WMV_Cluster c : p.p.getCurrentField().clusters )	
//			if(!c.isEmpty() && c.mediaPoints > 4)
//				drawPoint( c.getLocation(), PApplet.sqrt(c.mediaPoints) * 0.85f, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mapMediaTransparency );
		
		p.p.p.pushMatrix();
		p.p.p.translate(mapLeftEdge, mapTopEdge, 0.f);
		p.p.p.translate(largeMapXOffset, largeMapYOffset, p.hudDistance * mapDistance);
		p.p.p.tint(255.f, 255.f);
		p.p.p.image(simpleClusters, 0.f, 0.f);
		p.p.p.popMatrix();
	}

	/**
	 * @param image GMV_Image to draw
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param capture Draw capture location (true) or viewing location (false)
	 * Draw image location on map of specified size
	 */
	void drawImageOnMap(WMV_Image image, boolean forceDisplay, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = smallPointSize * mapWidth;
		float saturation = maxSaturation;
		float imageDistance = image.getViewingDistance();   // Get photo distance from current camera position
		boolean visible = forceDisplay;
		
		if (imageDistance < p.p.viewer.getFarViewingDistance() && imageDistance > p.p.viewer.getNearClippingDistance())    // If image is in visible range
			visible = true;                                              

		if(visible && image.location != null && !image.disabled && !image.hidden)
		{
			float alpha = 255.f;
			if(!forceDisplay)
				alpha = image.getTimeBrightness() * mapMediaTransparency;

			if(alpha > 0.f)
			{
				if(capture)
					drawPoint( image.getCaptureLocation(), pointSize, mapWidth, mapHeight, mapImageCaptureHue, saturation, 255.f, mapMediaTransparency );
				else
					drawPoint( image.getLocation(),  pointSize, mapWidth, mapHeight, mapImageHue, saturation, 255.f, mapMediaTransparency );
			}
		}
		
	}

	/**
	 * @param panorama GMV_Panorama to draw
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw image location on map of specified size
	 */
	void drawPanoramaOnMap(WMV_Panorama panorama, boolean forceDisplay, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = mediumPointSize * mapWidth;
		float saturation = maxSaturation;
		float panoramaDistance = panorama.getViewingDistance();   // Get photo distance from current camera position
		boolean visible = forceDisplay;

		if (panoramaDistance < p.p.viewer.getFarViewingDistance() && panoramaDistance > p.p.viewer.getNearClippingDistance())    // If panorama is in visible range
			visible = true;                                              

		if(visible && panorama.location != null && !panorama.disabled && !panorama.hidden)
		{
			float alpha = 255.f;
			if(!forceDisplay)
				alpha = panorama.getTimeBrightness() * mapMediaTransparency;

			if(alpha > 0.f)
			{

				if(capture)
					drawPoint( panorama.getCaptureLocation(),  pointSize, mapWidth, mapHeight, mapPanoramaCaptureHue, saturation, 255.f, mapMediaTransparency );
				else
					drawPoint( panorama.getLocation(),  pointSize, mapWidth, mapHeight, mapPanoramaHue, saturation, 255.f, mapMediaTransparency );
			}
		}
	}

	/**
	 * @param video GMV_Video to draw
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw image location on map of specified size
	 */
	void drawVideoOnMap(WMV_Video video, boolean forceDisplay, float mapWidth, float mapHeight, boolean capture)
	{
		float pointSize = mediumPointSize * mapWidth;
		float saturation = maxSaturation;
		float videoDistance = video.getViewingDistance();   // Get photo distance from current camera position
		boolean visible = forceDisplay;
		
		if (videoDistance < p.p.viewer.getFarViewingDistance() && videoDistance > p.p.viewer.getNearClippingDistance())    // If video is in visible range
			visible = true;                                              

		if(visible && video.location != null && !video.disabled && !video.hidden)
		{
			float alpha = 255.f;
			if(!forceDisplay)
				alpha = video.getTimeBrightness() * mapMediaTransparency;

			if(alpha > 0.f)
			{
				if(capture)
					drawPoint( video.getCaptureLocation(), pointSize, mapWidth, mapHeight, mapVideoCaptureHue, saturation, 255.f, mapMediaTransparency );
				else
					drawPoint( video.getLocation(), pointSize, mapWidth, mapHeight, mapVideoHue, saturation, 255.f, mapMediaTransparency );
			}
		}
	}

	/**
	 * Draw the map origin
	 * @param mapWidth
	 * @param mapHeight
	 */
	void drawOriginOnMap(float mapWidth, float mapHeight)
	{
		int size = (int)(mapWidth / 40.f);
		for(int i=-size/2; i<size/2; i+=size/10)
			drawPoint( new PVector(i, 0.f, 0.f), hugePointSize * mapWidth, mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
		for(int i=-size/2; i<size/2; i+=size/10)
			drawPoint( new PVector(0.f, 0.f, i), hugePointSize * mapWidth, mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
	}
	
	/**
	 * @param mapWidth
	 * @param mapHeight
	 */
	void drawMapBorder(float mapWidth, float mapHeight)
	{
		p.p.p.stroke(255,255,255,255);
		p.p.p.strokeWeight(3);
		p.p.p.pushMatrix();
		
		//p.p.p.line(x1, y1, z1, x2, y2, z2);
		p.p.p.translate(largeMapXOffset, largeMapYOffset);
		p.p.p.translate(mapLeftEdge, mapTopEdge);
		p.p.p.line(0.f, 0.f, p.hudDistance * mapDistance, mapWidth, 0.f, p.hudDistance * mapDistance );
		p.p.p.line(mapWidth, 0, p.hudDistance * mapDistance,  mapWidth, mapHeight, p.hudDistance * mapDistance );
		p.p.p.line(mapWidth, mapHeight, p.hudDistance * mapDistance,  0.f, mapHeight, p.hudDistance * mapDistance );
		p.p.p.line(0.f, mapHeight, p.hudDistance * mapDistance,  0.f, 0.f, p.hudDistance * mapDistance );

		p.p.p.popMatrix();
	}
	
	/**
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw current viewer location and orientation on map of specified size
	 */
	void drawCameraOnMap(float mapWidth, float mapHeight)
	{
		PVector camLoc = p.p.viewer.getLocation();
		float camYaw = -p.p.viewer.getXOrientation() - 0.5f * PApplet.PI;

		drawPoint( camLoc, cameraPointSize, mapWidth, mapHeight, mapCameraHue, 255.f, 255.f, mapMediaTransparency );
		float ptSize = cameraPointSize;
		
//		float arrowSize = 60.f;
		float arrowSize = fieldRatio >= 1 ? p.p.getCurrentModel().fieldWidth : p.p.getCurrentModel().fieldLength;

		ScaleMap logMap;
		logMap = new ScaleMap(6., arrowSize, 6., 60.);		/* Time fading interpolation */
		logMap.setMapFunction(p.p.circularEaseOut);

		/* Change viewer arrow based on fieldWidth -- should be fieldLength??  -- should depend on zoom level too! */
		int arrowLength = 20;
		
		logMap = new ScaleMap(0.f, 0.25f, 0.f, 0.25f);		/* Time fading interpolation */
		logMap.setMapFunction(p.p.circularEaseOut);
		
		float shrinkFactor = PApplet.map(arrowLength, 6.f, arrowSize, 0.f, 0.25f);
		shrinkFactor = (float)logMap.getMappedValueFor(shrinkFactor);
		shrinkFactor = 0.95f - (0.25f - shrinkFactor);		// Reverse mapping high to low
				
		for(int i=1; i<arrowLength; i++)
		{
			p.p.p.textSize(ptSize);
			float x = i * cameraPointSize * 0.25f * (float)Math.cos( camYaw );
			float y = i * cameraPointSize * 0.25f * (float)Math.sin( camYaw );
			
			PVector arrowPoint = new PVector(camLoc.x + x, 0, camLoc.z + y);
			drawPoint( arrowPoint, ptSize, mapWidth, mapHeight, mapCameraHue, 120.f, 255.f, 255.f );

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
//		PApplet.println("drawPathOnMap..."+path.size());
		float pointSize = smallPointSize * mapWidth;
		
		float saturation = maxSaturation;                                              

		for(WMV_Waypoint w : path)
		{
			drawPoint( w.getLocation(), pointSize * 4.f, mapWidth, mapHeight, 30, saturation, 255.f, mapMediaTransparency );
//			PApplet.println("Path ---> location.x:"+w.getLocation().x+" y:"+w.getLocation().y);
		}
	}

	
	/**
	 * @param origin Vector starting point
	 * @param vector Direction vector
	 * Draw current viewer location and orientation on map of specified size
	 */
	void drawForceVector(PVector vector)
	{
		if(!p.drawForceVector)
			p.drawForceVector = true;

//		mapVectorOrigin = origin;
		mapVectorVector = vector;
	}

	/**
	 * Draw the clusters at given depth
	 */
	void drawGMVClusters()
	{		 
		if(  !p.initialSetup && !p.mapOverlay && !p.controlOverlay && !p.info 
				&& p.messages.size() < 0 && p.metadata.size() < 0	 )
		{
			p.p.p.hint(PApplet.DISABLE_DEPTH_TEST);						// Disable depth testing for drawing HUD
		}

		for( WMV_Cluster c : p.p.getCurrentField().clusters )								// For all clusters at current depth
		{
			drawPoint( c.getLocation(), 5.f, largeMapWidth, largeMapHeight, mapClusterHue, 255.f, 255.f, mapMediaTransparency );
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
	public void highlightCluster( PVector point, float pointSize, float mapWidth, float mapHeight, float hue, float saturation, float brightness, float transparency )
	{
		float size = pointSize;
		int iterations = PApplet.round(size);
		int sizeDiff = PApplet.round(size/iterations);
		float alpha = transparency;
		float alphaDiff = transparency / iterations;
		
		for(int i=0; i<iterations; i++)
		{
			drawPoint( point, size * smallPointSize * mapWidth, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, alpha * 0.33f );

			size-=sizeDiff;
			alpha-=alphaDiff;
		}
	}
	
	/**
	 * Draw (on map) a point given in 3D world coordinates 
	 * @param point Point in 3D world coordinates
	 * @param pointSize Point size
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param hue Point hue
	 * @param saturation Point saturation
	 * @param brightness Point brightness
	 * @param transparency Point transparency
	 */
	public void drawPoint( PVector point, float pointSize, float mapWidth, float mapHeight, float hue, float saturation, float brightness, float transparency )
	{		
		float mapLocX, mapLocY;
		WMV_Model m = p.p.getCurrentField().model;

		if(!p.p.p.utilities.isNaN(point.x) && !p.p.p.utilities.isNaN(point.y) && !p.p.p.utilities.isNaN(point.z))
		{
			/* Find map coordinates for point */
			if(fieldRatio >= 1.f)					//	Field ratio == fieldWidth / fieldLength;
			{
				mapLocX = PApplet.map( point.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapWidth );		
				mapLocY = PApplet.map( point.z, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapHeight );
			}
			else
			{
				mapLocX = PApplet.map( point.x, -0.5f * m.fieldLength, 0.5f*m.fieldLength, 0, mapWidth );		
				mapLocY = PApplet.map( point.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, 0, mapHeight );
			}
//			mapLocX = PApplet.map( point.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapWidth );		
//			mapLocY = PApplet.map( point.z, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapHeight );

			if(mapLocX < mapWidth && mapLocX > 0 && mapLocY < mapHeight && mapLocY > 0)
			{
				p.p.p.stroke(hue, saturation, brightness, transparency);
				p.p.p.strokeWeight(pointSize);

				p.p.p.pushMatrix();
				p.p.p.translate(mapLeftEdge, mapTopEdge);
				p.p.p.point(largeMapXOffset + mapLocX, largeMapYOffset + mapLocY, p.hudDistance * mapDistance);
				p.p.p.popMatrix();
			}
		}
		else p.message("Map point is NaN!:"+point+" hue:"+hue);
	}
	
	/**
	 * Draw cursor location (on map) 
	 * @param point Point in 3D world coordinates
	 * @param pointSize Point size
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param hue Point hue
	 * @param saturation Point saturation
	 * @param brightness Point brightness
	 * @param transparency Point transparency
	 */
	public void drawMousePointOnMap( PVector point, float pointSize, float mapWidth, float mapHeight, float hue, float saturation, float brightness, float transparency )
	{		
		if(!p.p.p.utilities.isNaN(point.x) && !p.p.p.utilities.isNaN(point.y) && !p.p.p.utilities.isNaN(point.z))
		{
			if(point.x < mapWidth && point.x > 0 && point.y < mapHeight && point.y > 0)
			{
				p.p.p.stroke(hue, saturation, brightness, transparency);
				p.p.p.pushMatrix();

				p.p.p.strokeWeight(pointSize);
				p.p.p.translate(mapLeftEdge, mapTopEdge);
				p.p.p.point(largeMapXOffset + point.x, largeMapYOffset + point.y, p.hudDistance);
//				p.p.p.point(largeMapXOffset + point.x, largeMapYOffset + point.y, p.hudDistance * mapZoom);
				
				p.p.p.popMatrix();
			}
		}
		else p.message("Map point is NaN!:"+point+" hue:"+hue);
	}
	
	public void updateMapMouse()
	{
		Shape3D itemSelected = Shape3D.pickShape(p.p.p, p.p.p.mouseX, p.p.p.mouseY);

		int clusterID = -1;
		
		if(itemSelected == null)
		{
			selectedCluster = -1;
		}
		else
		{
			clusterID = itemSelected.tagNo;
			
			if(clusterID >= 0 && clusterID < p.p.getCurrentField().clusters.size())
			{
				selectedCluster = clusterID;
				if(p.p.p.debug.map)
					PApplet.println("selectedCluster:"+selectedCluster);
			}
		}
		
//		PApplet.println("itemSelected clusterID:"+clusterID);
		
	}
	
	public void handleMouseReleased(int mouseX, int mouseY)
	{
		if(selectedCluster != -1)
		{
//			PApplet.println("handleMouseReleased:"+selectedCluster);
			p.p.viewer.teleportToCluster(selectedCluster, true);
			p.p.display.map = false;
		}
	}
	
	public void setSelectedCluster( int newCluster )
	{
		selectedCluster = newCluster;
	}
	
	/**
	 * Draw camera attraction force vector
	 */
	public void drawForceVector()
	{
		mapVectorOrigin = p.p.viewer.getLocation();
		
		float ptSize = cameraPointSize * 2.f;
		drawPoint( mapVectorOrigin, ptSize, largeMapWidth, largeMapHeight, mapCameraHue, 255.f, 255.f, mapMediaTransparency );
		
		int arrowLength = 30;
		
		ScaleMap logMap;
		logMap = new ScaleMap(6., 60., 6., 60.);		/* Time fading interpolation */
		logMap.setMapFunction(p.p.circularEaseOut);

//		arrowLength = (int)logMap.p.getMappedValueFor(arrowLength);
		
		logMap = new ScaleMap(0.f, 0.25f, 0.f, 0.25f);		/* Time fading interpolation */
		logMap.setMapFunction(p.p.circularEaseOut);
		
		float shrinkFactor = PApplet.map(arrowLength, 6.f, 60.f, 0.f, 0.25f);
		shrinkFactor = (float)logMap.getMappedValueFor(shrinkFactor);
		shrinkFactor = 0.95f - (0.25f - shrinkFactor);		// Reverse mapping high to low
		PVector current = mapVectorVector;
		
		for(int i=1; i<arrowLength; i++)
		{
			p.p.p.textSize(ptSize);
			float mult = (i+6) * 0.025f;
			current = mapVectorVector.mult( mult );
			
			PVector arrowPoint = new PVector(mapVectorOrigin.x + current.x, 0, mapVectorOrigin.z + current.z);
			drawPoint( arrowPoint, ptSize, largeMapWidth, largeMapHeight, 255.f-mapCameraHue, 170.f, 255.f, 255.f );

			ptSize *= shrinkFactor;
		}
	}
	
	/**
	 * Transition map zoom from current to given value
	 */
	void mapZoomTransition(float target)
	{
		if(target != mapDistance)			// Check if already at target
		{
			beginZoomTransition = true;
			zoomTransition = true;   
			zoomTransitionStart = mapDistance;
			zoomTransitionTarget = target;
			zoomTransitionStartFrame = p.p.p.frameCount;
			zoomTransitionEndFrame = zoomTransitionStartFrame + zoomTransitionLength;
		}
		else
		{
			zoomTransition = false;
		}
	}
	
	/**
	 * Update map zoom level each frame
	 */
	void updateMapZoomTransition()
	{
		float newZoomLevel = 0.f;

		if(beginZoomTransition)
		{
			zoomTransitionStartFrame = p.p.p.frameCount;					
			zoomTransitionEndFrame = p.p.p.frameCount + zoomTransitionLength;	
			beginZoomTransition = false;
		}

		if (p.p.p.frameCount >= zoomTransitionEndFrame)
		{
			zoomTransition = false;
			newZoomLevel = zoomTransitionTarget;
		} 
		else
		{
			newZoomLevel = PApplet.map(p.p.p.frameCount, zoomTransitionStartFrame, zoomTransitionEndFrame, 
									   zoomTransitionStart, zoomTransitionTarget);  
		}

		mapDistance = newZoomLevel;
	}


	/**
	 * Zoom to rectangle in large map
	 * @param boxXOffset X offset in large map of rectangle
	 * @param boxYOffset Y offset in large map of rectangle
	 * @param boxWidth Width of rectangle
	 * @param boxHeight Height of rectangle
	 */
	void zoomToRectangle(float boxXOffset, float boxYOffset, float boxWidth, float boxHeight)	// -- Use clusters instead of absolute?
	{
		zoomMapXOffset = boxXOffset;
		zoomMapYOffset = boxYOffset;
		zoomMapWidth = boxWidth; 
		zoomMapHeight = boxHeight;

//		mapDistance = newZoomLevel;
	}

	/**
	 * Transition map zoom from current to given value
	 */
	void mapScrollTransition(float mapXOffset, float mapYOffset)
	{
		if(!scrollTransition)
		{
			if(mapXOffset != 0 || mapYOffset != 0)					// Check if already at target
			{
				beginScrollTransition = true;
				scrollTransition = true;   
				scrollTransitionStartFrame = p.p.p.frameCount;
				scrollTransitionEndFrame = scrollTransitionStartFrame + scrollTransitionLength;
				
				if(mapXOffset != 0 && mapYOffset != 0)
				{
					mapXTransitionStart = mapLeftEdge;
					mapXTransitionTarget = mapLeftEdge + mapXOffset;
					mapYTransitionStart = mapTopEdge;
					mapYTransitionTarget = mapTopEdge + mapYOffset;
				}
				else if(mapXOffset != 0 && mapYOffset == 0)
				{
					mapXTransitionStart = mapLeftEdge;
					mapXTransitionTarget = mapLeftEdge + mapXOffset;
					mapYTransitionTarget = mapTopEdge;
				}
				else if(mapXOffset == 0 && mapYOffset != 0)
				{
					mapXTransitionTarget = mapLeftEdge;
					mapYTransitionStart = mapTopEdge;
					mapYTransitionTarget = mapTopEdge + mapYOffset;
				}
			}
			else
			{
				scrollTransition = false;
			}
		}
	}
	
	/**
	 * Update map zoom level each frame
	 */
	void updateMapScrollTransition()
	{
		float newMapX = mapLeftEdge;
		float newMapY = mapTopEdge;

		if(beginScrollTransition)
		{
			scrollTransitionStartFrame = p.p.p.frameCount;					
			scrollTransitionEndFrame = p.p.p.frameCount + scrollTransitionLength;	
			beginScrollTransition = false;
		}

		if (p.p.p.frameCount >= scrollTransitionEndFrame)
		{
			scrollTransition = false;
			newMapX = mapXTransitionTarget;
			newMapY = mapYTransitionTarget;
		} 
		else
		{
			if(mapLeftEdge != mapXTransitionTarget)
			{
				newMapX = PApplet.map(p.p.p.frameCount, scrollTransitionStartFrame, scrollTransitionEndFrame,
									  mapXTransitionStart, mapXTransitionTarget); 
			}
			if(mapTopEdge != mapYTransitionTarget)
			{
				newMapY = PApplet.map(p.p.p.frameCount, scrollTransitionStartFrame, scrollTransitionEndFrame,
									  mapYTransitionStart, mapYTransitionTarget);     			
			}
		}

		if(mapLeftEdge != newMapX)
			mapLeftEdge = newMapX;
		if(mapTopEdge != newMapY)
			mapTopEdge = newMapY;
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
			drawPoint( new PVector(i, 0.f, 0.f), hugePointSize * mapWidth * 20.f / (i+size/2), mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
		for(int i=-size/2; i<size/2; i+=size/20)
			drawPoint( new PVector(0.f, 0.f, i), hugePointSize * mapWidth * 20.f / (i+size/2), mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
	}
}
