package wmViewer;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.data.IntList;
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
	IntList selectedClusterIDs;
	
	/* Graphics */
	PGraphics simpleClusters;
//	private boolean simpleClustersCreated = false;

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
	private float maxSaturation = 210.f;

	private float fieldRatio;
	
	float largeMapWidth, largeMapHeight;
	float largeMapXOffset, largeMapYOffset;
	float largeMapZoomLevel = 1.f;

	float zoomMapLeftEdge = 0.f, zoomMapTopEdge = 0.f;
	float zoomMapWidth, zoomMapHeight, zoomMapMaxWidth, zoomMapMaxHeight;
	float zoomMapXOffset, zoomMapYOffset;
	float zoomMapDefaultWidth, zoomMapDefaultHeight;

//	float smallMapWidth, smallMapHeight, smallMapMaxWidth, smallMapMaxHeight;
//	float smallMapXOffset, smallMapYOffset;
	
	float smallPointSize;
	float mediumPointSize;
	float largePointSize;
	float hugePointSize;
	float cameraPointSize;

	public boolean scrollTransition = false;
	private int scrollTransitionStartFrame = 0, scrollTransitionEndFrame = 0, scrollTransitionLength = 16;	
	private float mapXTransitionStart, mapXTransitionTarget, mapYTransitionStart, mapYTransitionTarget;

	public boolean zoomToRectangleTransition = false;   
	private int zoomToRectangleTransitionStartFrame = 0, zoomToRectangleTransitionEndFrame = 0, zoomToRectangleTransitionLength = 16;

//	zoomMapLeftEdgeTransitionStart = zoomMapLeftEdge;
//	zoomMapLeftEdgeTransitionTarget = zoomMapLeftEdge + boxXOffset;
//	zoomMapTopEdgeTransitionStart = zoomMapTopEdge;
//	zoomMapTopEdgeTransitionTarget = zoomMapTopEdge + boxYOffset;

	private float zoomMapXOffsetTransitionStart, zoomMapXOffsetTransitionTarget;
	private float zoomMapYOffsetTransitionStart, zoomMapYOffsetTransitionTarget;
	
	private float zoomMapWidthTransitionStart, zoomMapWidthTransitionTarget;
	private float zoomMapHeightTransitionStart, zoomMapHeightTransitionTarget;
	
	private float mapDistanceTransitionStart, mapDistanceTransitionTarget;
	private float mapLeftEdgeTransitionStart, mapLeftEdgeTransitionTarget;
	private float mapTopEdgeTransitionStart, mapTopEdgeTransitionTarget;
	
	PVector mapVectorOrigin, mapVectorVector;

	WMV_Display p;

	WMV_Map(WMV_Display parent)
	{
		p = parent;

		largeMapXOffset = -p.p.p.width * 0.5f;
		largeMapYOffset = -p.p.p.height * 0.5f;
//		largeMapWidth = p.p.p.height * 0.95f;
//		largeMapHeight = p.p.p.height * 0.95f;

		smallPointSize = 0.0000022f * p.p.p.width;
		mediumPointSize = 0.0000028f * p.p.p.width;
		largePointSize = 0.0000032f * p.p.p.width;
		hugePointSize = 0.0000039f * p.p.p.width;
		cameraPointSize = 0.005f * p.p.p.width;
	}
	
	void initializeLargeMap()
	{
		WMV_Model m = p.p.getCurrentModel();
		fieldRatio = m.fieldAspectRatio;			//	Field ratio == fieldWidth / fieldLength;
//		zoomMapDefaultWidth = m.fieldWidth / 10.f;										// Was 240.f
//		zoomMapDefaultHeight = m.fieldWidth / 10.f * p.p.p.height / p.p.p.width;		// Was 180.f
		zoomMapDefaultWidth = (float)Math.log10(m.fieldWidth) * 33.3f;										// Was 240.f
		zoomMapDefaultHeight = (float)Math.log10(m.fieldWidth) * 33.3f * p.p.p.height / p.p.p.width;		// Was 180.f
		PApplet.println("zoomMapDefaultWidth:"+zoomMapDefaultWidth);
		PApplet.println("zoomMapDefaultHeight:"+zoomMapDefaultHeight);
		if(fieldRatio >= 1.f)									
		{
			largeMapWidth = p.p.p.width * 0.98f;
//			largeMapWidth = p.p.p.width * 0.95f;
			largeMapHeight = p.p.p.width / fieldRatio;
		}
		else
		{
			largeMapWidth = p.p.p.height * fieldRatio;
			largeMapHeight = p.p.p.height * 0.98f;
//			largeMapHeight = p.p.p.height * 0.95f;
		}

		zoomToRectangle(0, 0, largeMapWidth, largeMapHeight);			// Start zoomed out on whole map
	}

	void initializeMaps()
	{
		initializeLargeMap();
		p.initializedMaps = true;
	}
	
	/**
	 * Reset the map to initial state
	 */
	void reset()
	{
		resetMapZoom(false);
		initializeMaps();
	}
	
	/**
	 * Create selectable circle representing each cluster
	 * @param mapWidth
	 * @param mapHeight
	 */
	void createSelectableClusters(float mapWidth, float mapHeight)
	{
		selectableClusters = new ArrayList<Ellipsoid>();
		selectedClusterIDs = new IntList();
		
		for( WMV_Cluster c : p.p.getCurrentField().clusters )	
		{
			if(!c.isEmpty() && c.mediaPoints != 0)
			{
				PVector mapLoc = getMapLocation(c.getLocation(), mapWidth, mapHeight);
				if( pointIsVisible(mapLoc, true) )
				{
					Ellipsoid ellipsoid = new Ellipsoid(p.p.p, 4, 4);
					float radius = PApplet.sqrt(c.mediaPoints) * 0.7f * mapDistance;

					ellipsoid.setRadius(radius);
					ellipsoid.drawMode(S3D.SOLID);
					ellipsoid.fill(p.p.p.color(105.f, 225.f, 200.f, mapMediaTransparency));
					ellipsoid.fill(p.p.p.color(105.f, 225.f, 200.f, 255.f));
					ellipsoid.strokeWeight(0.f);

					mapLoc.add(new PVector(largeMapXOffset, largeMapYOffset, p.hudDistance * mapDistance));
					mapLoc.add(new PVector(mapLeftEdge, mapTopEdge, 0));
					ellipsoid.moveTo(mapLoc.x, mapLoc.y, mapLoc.z);
					ellipsoid.tagNo = c.getID();
					selectableClusters.add(ellipsoid);
					selectedClusterIDs.append(c.getID());
				}
			}
		}
	}
	
	PVector getMapLocation(PVector point, float mapWidth, float mapHeight)
	{
		WMV_Model m = p.p.getCurrentField().model;
		float mapLocX, mapLocY;

		if(fieldRatio >= 1.f)					
		{
			mapLocX = PApplet.map( point.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapWidth );		
			mapLocY = PApplet.map( point.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, 0, mapWidth / fieldRatio );
		}
		else
		{
			mapLocX = PApplet.map( point.x, -0.5f * m.fieldWidth, 0.5f*m.fieldWidth, 0, mapHeight * fieldRatio );		
			mapLocY = PApplet.map( point.z, -0.5f * m.fieldLength, 0.5f*m.fieldLength, 0, mapHeight );
		}

		return new PVector(mapLocX, mapLocY, 0.f);
	}
	
	/**
	 * Check if a location is visible on map
	 * @param point The location to check
	 * @param mapCoords Whether the point is in map or in virtual world coordinates (m.)
	 * @return Whether the location is visible on map
	 */
	boolean pointIsVisible(PVector loc, boolean mapCoords)
	{
		PVector point = new PVector(loc.x, loc.y, loc.z);
		
		float xOff = zoomMapXOffset - largeMapXOffset;
		float yOff = zoomMapYOffset - largeMapYOffset;
		
		if(!mapCoords)														// Convert given world coords to map coords
			point = getMapLocation(loc, largeMapWidth, largeMapHeight);

		if( point.x > xOff && point.x < zoomMapWidth + xOff && point.y > yOff && point.y < zoomMapHeight + yOff )
			return true;
		else
			return false;
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
		p.beginHUD();											
		
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
		if(!scrollTransition && !zoomToRectangleTransition && !p.p.interactive)
			createSelectableClusters(mapWidth, mapHeight);
		
		p.beginHUD();

		/* Media */
		if(!scrollTransition && !zoomToRectangleTransition && !p.p.interactive)
		{
			if((mapImages && !p.p.getCurrentField().hideImages))
				for ( WMV_Image i : p.p.getCurrentField().images )					// Draw image capture locations on 2D Map
					drawImageOnMap(i, false, mapWidth, mapHeight, false);

			if((mapPanoramas && !p.p.getCurrentField().hidePanoramas))
				for ( WMV_Panorama n : p.p.getCurrentField().panoramas )			// Draw panorama capture locations on 2D Map
					drawPanoramaOnMap(n, false, mapWidth, mapHeight, false);

			if((mapVideos && !p.p.getCurrentField().hideVideos))
				for (WMV_Video v : p.p.getCurrentField().videos)					// Draw video capture locations on 2D Map
					drawVideoOnMap(v, false, mapWidth, mapHeight, false);
		}

		/* Clusters */
		if(mapClusters)
		{
			if(!scrollTransition && !zoomToRectangleTransition && !p.p.interactive)
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
				drawSimpleClusters(mapWidth, mapHeight);
			}
		}
		
		if(!scrollTransition && !zoomToRectangleTransition)
		{
			if(!p.p.interactive)													// While not in Clustering Mode
			{
				if(p.p.viewer.getAttractorCluster() != -1 && p.p.viewer.getAttractorCluster() < p.p.getFieldClusters().size())
					drawPoint( p.p.getAttractorCluster().getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mapMediaTransparency );

				if(p.p.viewer.getCurrentCluster() != -1 && p.p.viewer.getCurrentCluster() < p.p.getFieldClusters().size())
					drawPoint( p.p.getCurrentCluster().getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mapMediaTransparency );

				drawCameraOnMap(mapWidth, mapHeight);
			}

			if(p.p.viewer.getGPSTrack().size() > 0)
				drawPathOnMap(p.p.viewer.getGPSTrack(), mapWidth, mapHeight);
			
//			drawOriginOnMap(mapWidth, mapHeight);
		}

		if(p.p.p.debug.map)
			drawMapBorder(mapWidth, mapHeight);
	}
	
	void drawSimpleClusters(float mapWidth, float mapHeight)
	{
		for( WMV_Cluster c : p.p.getCurrentField().clusters )	
		{
			if(!c.isEmpty() && c.mediaPoints > 5)
			{
				PVector point = c.getLocation();

				if( pointIsVisible(point, false) )
				{
					float radius = PApplet.sqrt(c.mediaPoints) * 0.7f;
					drawPoint( point, radius, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mapMediaTransparency );
				}
			}
		}
	}

	void zoomInOnCluster(WMV_Cluster c)
	{
		if(!c.isEmpty() && c.mediaPoints > 0)
		{
			PVector point = getMapLocation(c.getLocation(), largeMapWidth, largeMapHeight);
			zoomMapLeftEdge = 0.f;
			zoomMapTopEdge = 0.f;
			zoomToRectangleTransition(point.x - zoomMapDefaultWidth/2, point.y - zoomMapDefaultHeight/2.f, zoomMapDefaultWidth, 
									  zoomMapDefaultHeight);	
			// -- Make sure not to zoom in too much on small fields!
		}
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
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw current viewer location and orientation on map of specified size
	 */
	void drawCameraOnMap(float mapWidth, float mapHeight)
	{
		PVector camLoc = p.p.viewer.getLocation();
		if(pointIsVisible(camLoc, false))
		{
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
		if(!p.p.p.utilities.isNaN(point.x) && !p.p.p.utilities.isNaN(point.y) && !p.p.p.utilities.isNaN(point.z))
		{
			PVector mapLoc = getMapLocation(point, mapWidth, mapHeight);

			if(mapLoc.x < mapWidth && mapLoc.x > 0 && mapLoc.y < mapHeight && mapLoc.y > 0)
			{
				p.p.p.stroke(hue, saturation, brightness, transparency);
				p.p.p.strokeWeight(pointSize);

				p.p.p.pushMatrix();
				p.p.p.translate(mapLeftEdge, mapTopEdge);
				p.p.p.point(largeMapXOffset + mapLoc.x, largeMapYOffset + mapLoc.y, p.hudDistance * mapDistance);
				p.p.p.popMatrix();
			}
		}
		else if(p.p.p.debug.map) 
			p.message("Map point is NaN!:"+point+" hue:"+hue);
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
			
			if(selectedClusterIDs.hasValue(clusterID))
			{
				if(clusterID >= 0 && clusterID < p.p.getCurrentField().clusters.size())
				{
					if(clusterID != selectedCluster)
					{
						selectedCluster = clusterID;
						if(p.p.p.debug.map) 
							PApplet.println("Selected new cluster:"+selectedCluster);
					}
				}
			}
		}
	}
	
	public void handleMouseReleased(int mouseX, int mouseY)
	{
		if(selectedCluster != -1)
			zoomInOnCluster(p.p.getCluster(selectedCluster));
	}
	
	public int getSelectedClusterID()
	{
		return selectedCluster;
	}
	
	public void setSelectedCluster( int newCluster )
	{
		selectedCluster = newCluster;
	}
	
	/**
	 * Transition map zoom from current to given value
	 */
	void mapZoomTransition(float zoomFactor)
	{
		zoomToRectangleTransition(0.f, 0.f, zoomMapWidth * zoomFactor, zoomMapHeight * zoomFactor);
	}
	
	/**
	 * Draw map border
	 * @param mapWidth
	 * @param mapHeight
	 */
	void drawMapBorder(float mapWidth, float mapHeight)
	{
		// Zoom map border
		p.p.p.stroke(133,255,255,255);	
		p.p.p.strokeWeight(1);
		
		p.p.p.pushMatrix();
		p.p.p.translate(mapLeftEdge, mapTopEdge);
		p.p.p.translate(zoomMapXOffset, zoomMapYOffset);
		p.p.p.line(0.f, 0.f, p.hudDistance * mapDistance, zoomMapWidth, 0.f, p.hudDistance * mapDistance );
		p.p.p.line(zoomMapWidth, 0, p.hudDistance * mapDistance, zoomMapWidth, zoomMapHeight, p.hudDistance * mapDistance );
		p.p.p.line(zoomMapWidth, zoomMapHeight, p.hudDistance * mapDistance, 0.f, zoomMapHeight, p.hudDistance * mapDistance );
		p.p.p.line(0.f, zoomMapHeight, p.hudDistance * mapDistance,  0.f, 0.f, p.hudDistance * mapDistance );
		p.p.p.popMatrix();
		
		if(p.p.p.debug.map)		// Large map border
		{
			p.p.p.stroke(255,255,255,255);
			p.p.p.strokeWeight(2);
			p.p.p.pushMatrix();
			p.p.p.translate(mapLeftEdge, mapTopEdge);
			p.p.p.translate(largeMapXOffset, largeMapYOffset);
			p.p.p.line(0.f, 0.f, p.hudDistance * mapDistance, mapWidth, 0.f, p.hudDistance * mapDistance );
			p.p.p.line(mapWidth, 0, p.hudDistance * mapDistance,  mapWidth, mapHeight, p.hudDistance * mapDistance );
			p.p.p.line(mapWidth, mapHeight, p.hudDistance * mapDistance,  0.f, mapHeight, p.hudDistance * mapDistance );
			p.p.p.line(0.f, mapHeight, p.hudDistance * mapDistance,  0.f, 0.f, p.hudDistance * mapDistance );
			p.p.p.popMatrix();
		}
		
		if(p.p.p.debug.map && p.p.p.debug.detailed)
		{
			p.p.p.pushMatrix();
			p.p.p.stroke(0,255,255,255);
			p.p.p.strokeWeight(25);
			WMV_Cluster c = p.p.getCurrentField().getEdgeClusterOnXAxis(true);
			PVector point = getMapLocation(c.getLocation(), largeMapWidth, largeMapHeight);
			p.p.p.translate(mapLeftEdge, mapTopEdge);
			p.p.p.translate(largeMapXOffset, largeMapYOffset);
			p.p.p.point(point.x, point.y, p.hudDistance * mapDistance);
			p.p.p.popMatrix();

			p.p.p.pushMatrix();
			p.p.p.stroke(100,255,255,255);
			p.p.p.strokeWeight(35);
			c = p.p.getCurrentField().getEdgeClusterOnZAxis(true);
			point = getMapLocation(c.getLocation(), largeMapWidth, largeMapHeight);
			p.p.p.translate(mapLeftEdge, mapTopEdge);
			p.p.p.translate(largeMapXOffset, largeMapYOffset);
			p.p.p.point(point.x, point.y, p.hudDistance * mapDistance);
			p.p.p.popMatrix();
		}
	}

	/**
	 * Zoom to rectangle in large map
	 * @param rectXOffset X offset in large map of rectangle left edge (Range: 0 to mapWidth)
	 * @param rectYOffset Y offset in large map of rectangle top edge (Range: 0 to mapHeight)
	 * @param rectWidth Width of rectangle
	 * @param rectHeight Height of rectangle
	 */
	void zoomToRectangle(float rectXOffset, float rectYOffset, float rectWidth, float rectHeight)	// -- Use clusters instead of absolute?
	{
		zoomMapLeftEdge += rectXOffset;
		zoomMapTopEdge += rectYOffset;
		zoomMapXOffset = largeMapXOffset + zoomMapLeftEdge;
		zoomMapYOffset = largeMapYOffset + zoomMapTopEdge;
		zoomMapWidth = rectWidth; 
		zoomMapHeight = rectHeight;

		mapDistance = zoomMapWidth / largeMapWidth;
		mapLeftEdge = (p.p.p.width - zoomMapWidth)/2 - zoomMapLeftEdge;
		mapTopEdge = (p.p.p.height - zoomMapHeight)/2 - zoomMapTopEdge;
		
		if(p.p.p.debug.map)
		{
			PApplet.println("---> zoomToRectangle()...");
			PApplet.println("Set mapLeftEdge:" + mapLeftEdge);
			PApplet.println("Set mapTopEdge:" + mapTopEdge);
			PApplet.println("Set mapDistance:" + mapDistance);
			PApplet.println("Set zoomMapXOffset:" + zoomMapXOffset);
			PApplet.println("Set zoomMapYOffset:" + zoomMapYOffset);
			PApplet.println("Set zoomMapLeftEdge:" + zoomMapLeftEdge);
			PApplet.println("Set zoomMapTopEdge:" + zoomMapTopEdge);
			PApplet.println("Set zoomMapWidth:" + zoomMapWidth);
			PApplet.println("Set zoomMapHeight:" + zoomMapHeight);
		}
//		mapZoomTransition();
	}

	/**
	 * Zoom in smoothly on a rectangular portion of the map
	 * @param rectXOffset
	 * @param rectYOffset
	 * @param rectWidth
	 * @param rectHeight
	 */
	void zoomToRectangleTransition(float rectXOffset, float rectYOffset, float rectWidth, float rectHeight)
	{
		if(!zoomToRectangleTransition)
		{
			zoomMapLeftEdge += rectXOffset;
			zoomMapTopEdge += rectYOffset;

			zoomToRectangleTransition = true;   
			zoomToRectangleTransitionStartFrame = p.p.p.frameCount;
			zoomToRectangleTransitionEndFrame = zoomToRectangleTransitionStartFrame + zoomToRectangleTransitionLength;

			zoomMapXOffsetTransitionStart = zoomMapXOffset;
			zoomMapXOffsetTransitionTarget = largeMapXOffset + zoomMapLeftEdge;
			zoomMapYOffsetTransitionStart = zoomMapYOffset;
			zoomMapYOffsetTransitionTarget = largeMapYOffset + zoomMapTopEdge;
			
			zoomMapWidthTransitionStart = zoomMapWidth;
			zoomMapWidthTransitionTarget = rectWidth;
			zoomMapHeightTransitionStart = zoomMapHeight;
			zoomMapHeightTransitionTarget = rectHeight;
			
			mapDistanceTransitionStart = mapDistance;
			mapDistanceTransitionTarget = zoomMapWidthTransitionTarget / largeMapWidth;
			mapLeftEdgeTransitionStart = mapLeftEdge;
			mapLeftEdgeTransitionTarget = (p.p.p.width - zoomMapWidthTransitionTarget)/2 - zoomMapLeftEdge;
			mapTopEdgeTransitionStart = mapTopEdge;
			mapTopEdgeTransitionTarget = (p.p.p.height - zoomMapHeightTransitionTarget)/2 - zoomMapTopEdge;

			if(p.p.p.debug.map)
				PApplet.println("Started zoomToRectangleTransition transition...");
		}
	}
	
	void updateZoomToRectangleTransition()
	{
		float newZoomMapXOffset = zoomMapXOffset;
		float newZoomMapYOffset = zoomMapYOffset;
		float newZoomMapWidth = zoomMapWidth;
		float newZoomMapHeight = zoomMapHeight;
		float newMapDistance = mapDistance;
		float newMapLeftEdge = mapLeftEdge;
		float newMapTopEdge = mapTopEdge;

		if (p.p.p.frameCount >= zoomToRectangleTransitionEndFrame)		// Reached end of transition
		{
			zoomToRectangleTransition = false;
			newZoomMapXOffset = zoomMapXOffsetTransitionTarget;
			newZoomMapYOffset = zoomMapYOffsetTransitionTarget;
			newZoomMapWidth = zoomMapWidthTransitionTarget;
			newZoomMapHeight = zoomMapHeightTransitionTarget;
			newMapDistance = mapDistanceTransitionTarget;
			newMapLeftEdge = mapLeftEdgeTransitionTarget;
			newMapTopEdge = mapTopEdgeTransitionTarget;
		} 
		else
		{
			if(zoomMapXOffset != zoomMapXOffsetTransitionTarget)
			{
				newZoomMapXOffset = PApplet.map(p.p.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  		 zoomMapXOffsetTransitionStart, zoomMapXOffsetTransitionTarget); 
			}
			if(zoomMapYOffset != zoomMapYOffsetTransitionTarget)
			{
				newZoomMapYOffset = PApplet.map(p.p.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  		 zoomMapYOffsetTransitionStart, zoomMapYOffsetTransitionTarget);     			
			}
			if(zoomMapWidth != zoomMapWidthTransitionTarget)
			{
				newZoomMapWidth = PApplet.map(p.p.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
								  	  	   zoomMapWidthTransitionStart, zoomMapWidthTransitionTarget); 
			}
			if(zoomMapHeight != zoomMapHeightTransitionTarget)
			{
				newZoomMapHeight = PApplet.map(p.p.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  	    zoomMapHeightTransitionStart, zoomMapHeightTransitionTarget);     			
			}
			if(mapDistance != mapDistanceTransitionTarget)
			{
				newMapDistance = PApplet.map(p.p.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  	  mapDistanceTransitionStart, mapDistanceTransitionTarget); 
			}
			if(mapLeftEdge != mapLeftEdgeTransitionTarget)
			{
				newMapLeftEdge = PApplet.map(p.p.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  	  mapLeftEdgeTransitionStart, mapLeftEdgeTransitionTarget);     			
			}
			if(mapTopEdge != mapTopEdgeTransitionTarget)
			{
				newMapTopEdge = PApplet.map(p.p.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  	 mapTopEdgeTransitionStart, mapTopEdgeTransitionTarget); 
			}
		}

		if(zoomMapXOffset != newZoomMapXOffset)
			zoomMapXOffset = newZoomMapXOffset;
		if(zoomMapYOffset != newZoomMapYOffset)
			zoomMapYOffset = newZoomMapYOffset;
		if(zoomMapWidth != newZoomMapWidth)
			zoomMapWidth = newZoomMapWidth;
		
		if(zoomMapHeight != newZoomMapHeight)
			zoomMapHeight = newZoomMapHeight;
		if(mapDistance != newMapDistance)
			mapDistance = newMapDistance;
		if(mapLeftEdge != newMapLeftEdge)
			mapLeftEdge = newMapLeftEdge;
		if(mapTopEdge != newMapTopEdge)
			mapTopEdge = newMapTopEdge;
	}
	
	/**
	 * Transition map zoom from current to given value
	 */
	void zoomRectangleScroll(float mapXOffset, float mapYOffset)
	{
		if(mapXOffset != 0 || mapYOffset != 0)					// Check if already at target
		{
			if(mapXOffset != 0 && mapYOffset != 0)
			{
				zoomToRectangle((1.f-mapDistance)*mapXOffset, (1.f-mapDistance)*mapYOffset, zoomMapWidth, zoomMapHeight);
			}
			else if(mapXOffset != 0 && mapYOffset == 0)
			{
				zoomToRectangle((1.f-mapDistance)*mapXOffset, 0, zoomMapWidth, zoomMapHeight);

			}
			else if(mapXOffset == 0 && mapYOffset != 0)
			{
				zoomToRectangle(0, (1.f-mapDistance)*mapYOffset, zoomMapWidth, zoomMapHeight);
			}
		}
	}
	
	/**
	 * Transition map zoom from current to given value
	 */
	void zoomRectangleScrollTransition(float mapXOffset, float mapYOffset)
	{
		if(mapXOffset != 0 || mapYOffset != 0)					// Check if already at target
		{
			if(mapXOffset != 0 && mapYOffset != 0)
			{
				zoomToRectangleTransition((1.f-mapDistance)*mapXOffset, (1.f-mapDistance)*mapYOffset, zoomMapWidth, zoomMapHeight);
			}
			else if(mapXOffset != 0 && mapYOffset == 0)
			{
				zoomToRectangleTransition((1.f-mapDistance)*mapXOffset, 0, zoomMapWidth, zoomMapHeight);
			}
			else if(mapXOffset == 0 && mapYOffset != 0)
			{
				zoomToRectangleTransition(0, (1.f-mapDistance)*mapYOffset, zoomMapWidth, zoomMapHeight);
			}
		}
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
//				beginScrollTransition = true;
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
		
		if(p.p.p.debug.map)
		{
			PApplet.println("Updated mapLeftEdge:"+mapLeftEdge);
			PApplet.println("Updated mapTopEdge:"+mapTopEdge);
		}
	}

	/**
	 * Zoom out on whole map
	 */
	void resetMapZoom(boolean transition)
	{
//		mapLeftEdge = 0.f;
//		mapTopEdge = 0.f;
		zoomMapLeftEdge = 0.f;
		zoomMapTopEdge = 0.f;

		if(transition)
			zoomToRectangleTransition(0, 0, largeMapWidth, largeMapHeight);	
		else
			zoomToRectangle(0, 0, largeMapWidth, largeMapHeight);	
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
	
//	/**
//	 * Draw camera attraction force vector (for debugging)
//	 */
//	public void drawForceVector()
//	{
//		mapVectorOrigin = p.p.viewer.getLocation();
//		
//		float ptSize = cameraPointSize * 2.f;
//		drawPoint( mapVectorOrigin, ptSize, largeMapWidth, largeMapHeight, mapCameraHue, 255.f, 255.f, mapMediaTransparency );
//		
//		int arrowLength = 30;
//		
//		ScaleMap logMap;
//		logMap = new ScaleMap(6., 60., 6., 60.);			/* Time fading interpolation */
//		logMap.setMapFunction(p.p.circularEaseOut);
//		
//		logMap = new ScaleMap(0.f, 0.25f, 0.f, 0.25f);		/* Time fading interpolation */
//		logMap.setMapFunction(p.p.circularEaseOut);
//		
//		float shrinkFactor = PApplet.map(arrowLength, 6.f, 60.f, 0.f, 0.25f);
//		shrinkFactor = (float)logMap.getMappedValueFor(shrinkFactor);
//		shrinkFactor = 0.95f - (0.25f - shrinkFactor);		// Reverse mapping high to low
//		PVector current = mapVectorVector;
//		
//		for(int i=1; i<arrowLength; i++)
//		{
//			p.p.p.textSize(ptSize);
//			float mult = (i+6) * 0.025f;
//			current = mapVectorVector.mult( mult );
//			
//			PVector arrowPoint = new PVector(mapVectorOrigin.x + current.x, 0, mapVectorOrigin.z + current.z);
//			drawPoint( arrowPoint, ptSize, largeMapWidth, largeMapHeight, 255.f-mapCameraHue, 170.f, 255.f, 255.f );
//
//			ptSize *= shrinkFactor;
//		}
//	}
//	
//	/**
//	 * Interesting effect
//	 * @param mapWidth
//	 * @param mapHeight
//	 */
//	void drawMandalaOnMap(float mapWidth, float mapHeight)
//	{
//		int size = (int)(mapWidth / 20.f);
//		for(int i=-size/2; i<size/2; i+=size/20)
//			drawPoint( new PVector(i, 0.f, 0.f), hugePointSize * mapWidth * 20.f / (i+size/2), mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
//		for(int i=-size/2; i<size/2; i+=size/20)
//			drawPoint( new PVector(0.f, 0.f, i), hugePointSize * mapWidth * 20.f / (i+size/2), mapWidth, mapHeight, 180.f, 30.f, 255.f, mapMediaTransparency / 2.f );
//	}
	
//	void createSimpleClusters(float mapWidth, float mapHeight)
//	{
//		int pgWidth = PApplet.round(mapWidth);
//		int pgHeight = PApplet.round(mapHeight);
//
//		simpleClusters = p.p.p.createGraphics(pgWidth, pgHeight, PApplet.P3D);
//		
//		for( WMV_Cluster c : p.p.getCurrentField().clusters )	
//			if(!c.isEmpty() && c.mediaPoints > 4)
//			{
//				PVector point = c.getLocation();
//				
//				if(!p.p.p.utilities.isNaN(point.x) && !p.p.p.utilities.isNaN(point.y) && !p.p.p.utilities.isNaN(point.z))
//				{
//					PVector mapLoc = getMapLocation(point, mapWidth, mapHeight);
//
//					if(mapLoc.x < mapWidth && mapLoc.x > 0 && mapLoc.y < mapHeight && mapLoc.y > 0)
//					{
//						simpleClusters.stroke(mapClusterHue, 255.f, 255.f, 255.f);
//						float sw = PApplet.sqrt(c.mediaPoints) * 0.5f;
//						sw *= mapDistance;
//						simpleClusters.strokeWeight(sw);
//
//						simpleClusters.beginDraw();
//						simpleClusters.pushMatrix();
//						simpleClusters.point(mapLoc.x, mapLoc.y, 0.f);
//						simpleClusters.popMatrix();
//						simpleClusters.endDraw();
//					}
//				}
//			}
//		
//		simpleClustersCreated = true;
//	}
	
}
