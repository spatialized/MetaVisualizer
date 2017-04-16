package multimediaLocator;

import java.util.ArrayList;
//import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;
import processing.data.IntList;
import toxi.math.ScaleMap;
//import shapes3d.*;

import de.fhpotsdam.unfolding.UnfoldingMap;
//import de.fhpotsdam.unfolding.data.MarkerFactory;
import de.fhpotsdam.unfolding.events.EventDispatcher;
//import de.fhpotsdam.unfolding.events.PanMapEvent;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.interactions.MouseHandler;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Microsoft;
//import de.fhpotsdam.unfolding.providers.AbstractMapProvider;
//import de.fhpotsdam.unfolding.utils.DebugDisplay;
//import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;

/***********************************
 * Methods for displaying interactive 2D maps of 3D environments
 * @author davidgordon
 */
public class ML_Map 
{
	/* Map */
	private UnfoldingMap map;
	private Location mapCenter;
	private EventDispatcher eventDispatcher;
	private MarkerManager<Marker> markerManager;
	private SimplePointMarker viewerMarker;
	private int clusterZoomLevel = 18;
	
	/* Graphics */
	private float hudDistance;			// Distance of the Heads-Up Display from the virtual camera -- Change with zoom level??
	private int screenWidth = -1;
	private int screenHeight = -1;
	
	/* Interaction */
	private int selectedCluster = -1;
	private IntList selectableClusterIDs;
	private ArrayList<SelectableClusterLocation> selectableClusterLocations;
	int mousePressedFrame = -1;
	int mouseDraggedFrame = -1;

	public float mapDistance = 1.f;										// Obsolete soon
	private final float mapDistanceMin = 0.04f, mapDistanceMax = 1.2f;	// Obsolete soon
	public float mapLeftEdge = 0.f, mapTopEdge = 0.f;					// Obsolete soon

	private float curMapWidth, curMapHeight;							// Obsolete soon
	private float largeMapXOffset, largeMapYOffset;						// Obsolete soon

	/* Clusters */
	private boolean selectableClustersCreated = false;
	public float mapClusterHue = 112.f;
	private float mapAttractorClusterHue = 222.f;
	
	/* Media */
	float smallPointSize, mediumPointSize, largePointSize, hugePointSize;	// Obsolete soon
	float cameraPointSize;

	public boolean mapImages = true, mapPanoramas = true, mapVideos = true, mapMedia = true;
	private float imageHue = 150.f, imageCaptureHue = 90.f;
	private float panoramaHue = 190.f, panoramaCaptureHue = 220.f;
	private float videoHue = 40.f, videoCaptureHue = 70.f;
	private float soundHue = 40.f;
	private float cameraHue = 140.f;

	private float mediaTransparency = 120.f;
	private float maxSaturation = 210.f, lowSaturation = 80.f;
	private float fieldAspectRatio;
	
	private float zoomMapLeftEdge = 0.f, zoomMapTopEdge = 0.f;
	private float zoomMapWidth, zoomMapHeight, zoomMapMaxWidth, zoomMapMaxHeight;
	private float zoomMapXOffset, zoomMapYOffset;
	private float zoomMapDefaultWidth, zoomMapDefaultHeight;

	public boolean scrollTransition = false;
	private int scrollTransitionStartFrame = 0, scrollTransitionEndFrame = 0, scrollTransitionLength = 16;	
	private float mapXTransitionStart, mapXTransitionTarget, mapYTransitionStart, mapYTransitionTarget;

	public boolean zoomToRectangleTransition = false;   
	private int zoomToRectangleTransitionStartFrame = 0, zoomToRectangleTransitionEndFrame = 0, zoomToRectangleTransitionLength = 16;

	private float zoomMapXOffsetTransitionStart, zoomMapXOffsetTransitionTarget;
	private float zoomMapYOffsetTransitionStart, zoomMapYOffsetTransitionTarget;
	private float zoomMapWidthTransitionStart, zoomMapWidthTransitionTarget;
	private float zoomMapHeightTransitionStart, zoomMapHeightTransitionTarget;
	
	private float mapDistanceTransitionStart, mapDistanceTransitionTarget;
	private float mapLeftEdgeTransitionStart, mapLeftEdgeTransitionTarget;
	private float mapTopEdgeTransitionStart, mapTopEdgeTransitionTarget;
	
	PVector mapVectorOrigin, mapVectorVector;

	WMV_Utilities utilities;
	ML_Display p;

	ML_Map(ML_Display parent, int newScreenWidth, int newScreenHeight, float newHUDDistance)
	{
		p = parent;
		screenWidth = newScreenWidth;
		screenHeight = newScreenHeight;
		hudDistance = newHUDDistance;
		
		utilities = new WMV_Utilities();
		
		largeMapXOffset = -screenWidth * 0.5f;
		largeMapYOffset = -screenHeight * 0.5f;

		smallPointSize = 0.0000022f * screenWidth;
		mediumPointSize = 0.0000028f * screenWidth;
		largePointSize = 0.0000032f * screenWidth;
		hugePointSize = 0.0000039f * screenWidth;
		cameraPointSize = 0.005f * screenWidth;
	}

	/**
	 * Initialize maps
	 */
	public void initializeMaps(WMV_World world)
	{
		WMV_Model m = world.getCurrentModel();
		fieldAspectRatio = m.getState().fieldAspectRatio;						//	Field ratio == fieldWidth / fieldLength;
		zoomMapDefaultWidth = (float)Math.log10(m.getState().fieldWidth) * 33.3f;										// Was 240.f
		zoomMapDefaultHeight = (float)Math.log10(m.getState().fieldWidth) * 33.3f * screenHeight / screenWidth;		// Was 180.f

		if(fieldAspectRatio >= 1.f)									
		{
			curMapWidth = screenWidth * 0.95f;
			curMapHeight = screenWidth / fieldAspectRatio;
		}
		else
		{
			curMapWidth = screenHeight * fieldAspectRatio;
			curMapHeight = screenHeight * 0.95f;
		}

		zoomToRectangle(0, 0, curMapWidth, curMapHeight);			// Start zoomed out on whole map

		initializeSatelliteMap(world);
		p.initializedMaps = true;
	}
	
	/**
	 * Initialize maps
	 */
	public void initializeSatelliteMap(WMV_World p)
	{
		map = new UnfoldingMap(p.p, "Satellite", 0, 0, screenWidth, screenHeight, true, false, new Microsoft.AerialProvider());

		PVector gpsLoc = utilities.getGPSLocation(p.getCurrentField(), new PVector(0,0,0));
		mapCenter = new Location(gpsLoc.y, gpsLoc.x);
		
//		imageMarkers = new IntList();
//		panoramaMarkers = new IntList();
//		videoMarkers = new IntList();
		
		map.zoomAndPanTo(16, mapCenter);
		map.setTweening(true);
		map.setZoomRange(2, 19);
//		map.setZoomRange(2, 21);
		map.setTweening(true);

		eventDispatcher = new EventDispatcher();

		// Add mouse interaction to map
		MouseHandler mouseHandler = new MouseHandler(p.p, map);
		eventDispatcher.addBroadcaster(mouseHandler);

		eventDispatcher.register(map, "pan", map.getId());
		eventDispatcher.register(map, "zoom", map.getId());

		createPointMarkers(p);
		
//		System.out.println("viewerMarker getLocation():"+p.p.viewer.getLocation()+" p.p.viewer.getGPSLocation():"+p.p.viewer.getGPSLocation());
		PVector vLoc = p.viewer.getGPSLocation();
		viewerMarker = new SimplePointMarker(new Location(vLoc.y, vLoc.x));
		viewerMarker.setId("viewer");
		viewerMarker.setDiameter(20.f);
		viewerMarker.setColor(p.p.color(0, 0, 255, 255));
	}
	
	/**
	 * Create simple point markers for each cluster
	 */
	public void createPointMarkers(WMV_World world)
	{
		markerManager = new MarkerManager<Marker>();
		for( WMV_Cluster c : world.getCurrentField().getClusters() )	
		{
			if(!c.isEmpty() && c.getState().mediaCount != 0)
			{
				PVector mapLoc = c.getLocation();
				PVector gpsLoc = utilities.getGPSLocation(world.getCurrentField(), mapLoc);
				SimplePointMarker marker = new SimplePointMarker(new Location(gpsLoc.y, gpsLoc.x));
				marker.setId(String.valueOf(c.getID()));
//				marker.setColor(p.p.p.color(90, 225, 225, 155));
				marker.setColor(world.p.color(100.f, 165.f, 215.f, 225.f));			// Same color as time segments in Time View
				marker.setHighlightColor(world.p.color(170, 255, 255, 255));
				marker.setStrokeWeight(0);
				marker.setDiameter((float)Math.sqrt(c.getState().mediaCount) * 3.f);
				markerManager.addMarker(marker);
			}
		}

		map.addMarkerManager(markerManager);
		markerManager.enableDrawing();
	}
	
	/**
	 * Reset the map to initial state
	 */
	public void reset(WMV_World world)
	{
		resetMapZoom(world, false);
		initializeMaps(world);
		selectedCluster = -1;
	}
	
	/**
	 * Create selectable circle representing each cluster		// -- BROKEN
	 * @param mapWidth
	 * @param mapHeight
	 */
	public void createSelectableClusters(WMV_World world, float mapWidth, float mapHeight)
	{
//		selectableClusters = new ArrayList<Ellipsoid>();
		selectableClusterIDs = new IntList();
		selectableClusterLocations = new ArrayList<SelectableClusterLocation>();
		
		for( WMV_Cluster c : world.getCurrentField().getClusters() )	
		{
			if(!c.isEmpty() && c.getState().mediaCount != 0)
			{
				PVector mapLoc = getMapLocation(world, c.getLocation(), mapWidth, mapHeight);
				if( pointIsVisible(world, mapLoc, true) )
				{
//					Ellipsoid ellipsoid = new Ellipsoid(p.p.p, 4, 4);
//					float radius = (float)Math.sqrt(c.mediaCount) * 0.7f * mapDistance / PApplet.sqrt(PApplet.sqrt(mapDistance));

					mapLoc.add(new PVector(largeMapXOffset, largeMapYOffset, hudDistance * mapDistance));
					mapLoc.add(new PVector(mapLeftEdge, mapTopEdge, 0));
//					ellipsoid.moveTo(mapLoc.x, mapLoc.y, mapLoc.z);
					
					SelectableClusterLocation scl = new SelectableClusterLocation(c.getID(), mapLoc);
					
//					ellipsoid.tagNo = c.getID();
//					selectableClusters.add(ellipsoid);
					selectableClusterIDs.append(c.getID());
					selectableClusterLocations.add(scl);
				}
			}
		}
		
		selectableClustersCreated = true;
	}
	
	/**
	 * Get map location for a given point
	 * @param world Parent world
	 * @param point Given point
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @return
	 */
	PVector getMapLocation(WMV_World world, PVector point, float mapWidth, float mapHeight)
	{
		WMV_Model m = world.getCurrentField().getModel();
		float mapLocX, mapLocY;

		if(fieldAspectRatio >= 1.f)					
		{
			mapLocX = PApplet.map( point.x, -0.5f * m.getState().fieldWidth, 0.5f*m.getState().fieldWidth, 0, mapWidth );		
			mapLocY = PApplet.map( point.z, -0.5f * m.getState().fieldLength, 0.5f*m.getState().fieldLength, 0, mapWidth / fieldAspectRatio );
		}
		else
		{
			mapLocX = PApplet.map( point.x, -0.5f * m.getState().fieldWidth, 0.5f*m.getState().fieldWidth, 0, mapHeight * fieldAspectRatio );		
			mapLocY = PApplet.map( point.z, -0.5f * m.getState().fieldLength, 0.5f*m.getState().fieldLength, 0, mapHeight );
		}

		return new PVector(mapLocX, mapLocY, 0.f);
	}

	/**
	 * Draw map filling the main window
	 */
	void drawMainMap(WMV_World world, boolean mediaOnly)
	{
		if(mediaOnly)							// Draw media map markers only (offline)
		{
			world.p.pushMatrix();
			p.startHUD(world);											

			world.p.fill(55, 0, 255, 255);
			world.p.textSize(p.veryLargeTextSize);
			float textXPos = p.centerTextXOffset;
			float textYPos = p.topTextYOffset;

			if(world.p.state.interactive)
				world.p.text("Interactive "+(world.getState().hierarchical ? "Hierarchical" : "K-Means")+" Clustering", textXPos, textYPos, hudDistance);
			else
				world.p.text(world.getCurrentField().getName(), textXPos, textYPos, hudDistance);

			world.p.popMatrix();

			drawMap(world, curMapWidth, curMapHeight, largeMapXOffset, largeMapYOffset);
		}
		else									// Draw satellite map 
		{
			PVector vLoc = world.viewer.getGPSLocation();
			Location gpsLoc = new Location(vLoc.y, vLoc.x);
			if(gpsLoc != null)
			{
				if(viewerMarker != null)
				{
					viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker
					markerManager.addMarker(viewerMarker);
				}
				else System.out.println("viewerMarker == null!");
			}

			world.p.perspective();
			world.p.camera();												// Reset to default camera setting
			world.p.tint(255.f, 255.f);
			map.draw();

			if(mapMedia)
				displayClusters(world);
		}
	}
	
	private void displayClusters(WMV_World world)
	{
		float mapWidth = curMapWidth;
		float mapHeight = curMapHeight;

		if(selectedCluster >= 0)
		{
			if(selectedCluster < world.getCurrentField().getClusters().size())
			{
			WMV_Cluster c = world.getCurrentField().getCluster(selectedCluster);

			if(selectedCluster == world.viewer.getState().getCurrentClusterID())
				drawClusterMedia(world, c, mapWidth, mapHeight, false);
			else
				drawClusterMedia(world, c, mapWidth, mapHeight, true);
			}
			else
				selectedCluster = -1;
		}

		if(selectedCluster != world.viewer.getState().getCurrentClusterID() && world.viewer.getState().getCurrentClusterID() >= 0)
		{
			WMV_Cluster c = world.getCurrentCluster();
			if(c != null)
				drawClusterMedia(world, c, mapWidth, mapHeight, false);
		}
	}

	/**
	 * Draw map of media and viewer without UnfoldingMaps library
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param mapXOffset Map X offset
	 * @param mapYOffset Map Y offset
	 */
	private void drawMap(WMV_World world, float mapWidth, float mapHeight, float mapXOffset, float mapYOffset)
	{
		if(!scrollTransition && !zoomToRectangleTransition && !world.p.state.interactive)
		{
			if(!selectableClustersCreated)
				createSelectableClusters(world, mapWidth, mapHeight);
		}
		
		p.startHUD(world);

		/* Media */
//		if(!scrollTransition && !zoomToRectangleTransition && !world.interactive)
//		{
//			if((mapImages && !p.p.getCurrentField().hideImages))
//				for ( WMV_Image i : p.p.getCurrentField().images )					// Draw image capture locations on 2D Map
//					drawImageOnMap(i, false, mapWidth, mapHeight, false);
//
//			if((mapPanoramas && !p.p.getCurrentField().hidePanoramas))
//				for ( WMV_Panorama n : p.p.getCurrentField().panoramas )			// Draw panorama capture locations on 2D Map
//					drawPanoramaOnMap(n, false, mapWidth, mapHeight, false);
//
//			if((mapVideos && !p.p.getCurrentField().hideVideos))
//				for (WMV_Video v : p.p.getCurrentField().videos)					// Draw video capture locations on 2D Map
//					drawVideoOnMap(v, false, mapWidth, mapHeight, false);
//		}

		/* Clusters */
		if(mapMedia)
		{
			if(!scrollTransition && !zoomToRectangleTransition && !world.p.state.interactive)
			{
//				drawSelectableClusters();

				if(selectedCluster >= 0)
				{
					WMV_Cluster c = world.getCurrentField().getCluster(selectedCluster);
//					if(!c.isEmpty() && c.mediaCount != 0)
//						highlightCluster( c.getLocation(), PApplet.sqrt(c.mediaCount)*0.5f, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mediaTransparency );
					
					if(selectedCluster == world.viewer.getState().getCurrentClusterID())
						drawClusterMedia(world, c, mapWidth, mapHeight, false);
					else
						drawClusterMedia(world, c, mapWidth, mapHeight, true);
				}

				if(selectedCluster != world.viewer.getState().getCurrentClusterID() && world.viewer.getState().getCurrentClusterID() >= 0)
				{
					WMV_Cluster c = world.getCurrentCluster();
					if(c != null)
					{
//						if(!c.isEmpty() && c.mediaCount != 0)
//							highlightCluster( c.getLocation(), PApplet.sqrt(c.mediaCount)*0.5f, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mediaTransparency );
						drawClusterMedia(world, c, mapWidth, mapHeight, false);
					}
				}
			}
			else
			{
				drawSimpleClusters(world, mapWidth, mapHeight);
			}
		}
		
		if(!scrollTransition && !zoomToRectangleTransition)
		{
			if(!world.p.state.interactive)													// While not in Clustering Mode
			{
				if(world.viewer.getAttractorClusterID() != -1 && world.viewer.getAttractorClusterID() < world.getFieldClusters().size())
					drawPoint( world, world.viewer.getAttractorCluster().getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mediaTransparency );

				WMV_Cluster c = world.getCurrentCluster();
				if(c != null)
				{
					if(world.viewer.getState().getCurrentClusterID() != -1 && world.viewer.getState().getCurrentClusterID() < world.getFieldClusters().size())
						drawPoint( world, c.getLocation(), hugePointSize * mapWidth, mapWidth, mapHeight, mapAttractorClusterHue, 255.f, 255.f, mediaTransparency );
				}
				
				drawViewer(world, mapWidth, mapHeight);
			}

			if(world.viewer.getGPSTrack().size() > 0)
				drawPathOnMap(world, world.viewer.getGPSTrack(), mapWidth, mapHeight);
			
//			drawOriginOnMap(mapWidth, mapHeight);
		}

		if(world.p.debugSettings.map)
			drawMapBorder(world, mapWidth, mapHeight);
	}
	
	/**
	 * Draw (nonselectable) points representing clusters
	 * @param mapWidth
	 * @param mapHeight
	 */
	void drawSimpleClusters(WMV_World world, float mapWidth, float mapHeight)
	{
		for( WMV_Cluster c : world.getCurrentField().getClusters() )	
		{
			if(!c.isEmpty() && c.getState().mediaCount > 5)
			{
				PVector point = c.getLocation();

				if( pointIsVisible(world, point, false) )
				{
					float radius = PApplet.sqrt(c.getState().mediaCount) * 0.7f;
//					float radius = PApplet.sqrt(c.mediaPoints) * 0.7f  / PApplet.sqrt(PApplet.sqrt(mapDistance));
					drawPoint(world, point, radius, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, mediaTransparency );
				}
			}
		}
	}

	/**
	 * Zoom in on cluster
	 * @param c Cluster to zoom in on
	 */
	void zoomToCluster(WMV_World world, WMV_Cluster c)
	{
		if(!c.isEmpty() && c.getState().mediaCount > 0)
		{
			if(p.satelliteMap)
			{
				PVector mapLoc = c.getLocation();
				PVector gpsLoc = utilities.getGPSLocation(world.getCurrentField(), mapLoc);
				
				map.zoomAndPanTo(clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
			}
			else
			{
				PVector point = getMapLocation(world, c.getLocation(), curMapWidth, curMapHeight);
				zoomMapLeftEdge = 0.f;
				zoomMapTopEdge = 0.f;
				zoomToRectangleTransition(world, point.x - zoomMapDefaultWidth/2, point.y - zoomMapDefaultHeight/2.f, zoomMapDefaultWidth, 
										  zoomMapDefaultHeight);								// -- Make sure not to zoom in too much on small fields!
			}
		}
	}
	
	/**
	 * Zoom in on map
	 */
	public void zoomIn(WMV_World world)
	{
		if (p.satelliteMap) map.zoomIn();
		else mapZoomTransition(world, 0.85f);
	}
	
	/**
	 * Zoom out on map
	 */
	public void zoomOut(WMV_World world)
	{
		if (p.satelliteMap) map.zoomOut();
		else mapZoomTransition(world, 1.176f);
	}

	/**
	 * Check if a location is visible on map
	 * @param point The location to check
	 * @param mapCoords Whether the point is in map or in virtual world coordinates (m.)
	 * @return Whether the location is visible on map
	 */
	boolean pointIsVisible(WMV_World world, PVector loc, boolean mapCoords)
	{
		PVector point = new PVector(loc.x, loc.y, loc.z);
		
		float xOff = zoomMapXOffset - largeMapXOffset;
		float yOff = zoomMapYOffset - largeMapYOffset;
		
		if(!mapCoords)														// Convert given world coords to map coords
			point = getMapLocation(world, loc, curMapWidth, curMapHeight);

		if( point.x > xOff && point.x < zoomMapWidth + xOff && point.y > yOff && point.y < zoomMapHeight + yOff )
			return true;
		else
			return false;
	}
	
	/**
	 * Draw media in given cluster on map
	 * @param c Given cluster
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param ignoreTime Whether to display independently of time (true) or not (false)
	 */
	void drawClusterMedia(WMV_World world, WMV_Cluster c, float mapWidth, float mapHeight, boolean ignoreTime)
	{
		if((mapImages && !world.viewer.getSettings().hideImages))
			for ( int i : c.getState().images )									// Draw images on Map
			{
				WMV_Image img = world.getCurrentField().getImage(i);
				drawImageOnMap(world, img, ignoreTime, mapWidth, mapHeight, false);
				if(world.getState().showModel)
				{
//					drawLine(c.getLocation(), img.getLocation(), 60.f, 160.f, 255.f, mapWidth, mapHeight);
					if(world.getState().showMediaToCluster)
						drawLine(world, c.getLocation(), img.getLocation(), 40.f, 155.f, 200.f, mapWidth, mapHeight);

					if(world.getState().showCaptureToMedia)
						drawLine(world, img.getLocation(), img.getCaptureLocation(), 160.f, 100.f, 255.f, mapWidth, mapHeight);

					if(world.getState().showCaptureToCluster)
						drawLine(world, c.getLocation(), img.getCaptureLocation(), 100.f, 55.f, 255.f, mapWidth, mapHeight);
				}
			}

		if((mapPanoramas && !world.viewer.getSettings().hidePanoramas))
			for ( int n : c.getState().panoramas )									// Draw panoramas on Map
			{
				WMV_Panorama pano = world.getCurrentField().getPanorama(n);
				drawPanoramaOnMap(world, pano, ignoreTime, mapWidth, mapHeight, false);
			}

		if((mapVideos && !world.viewer.getSettings().hideVideos))
			for (int v : c.getState().videos)										// Draw videos on Map
			{
				WMV_Video vid = world.getCurrentField().getVideo(v);
				drawVideoOnMap(world, vid, ignoreTime, mapWidth, mapHeight, false);
				if(world.getState().showModel)
				{
					if(world.getState().showMediaToCluster)
						drawLine(world, c.getLocation(), vid.getLocation(), 140.f, 155.f, 200.f, mapWidth, mapHeight);

					if(world.getState().showCaptureToMedia)
						drawLine(world, vid.getLocation(), vid.getCaptureLocation(), 50.f, 100.f, 255.f, mapWidth, mapHeight);

					if(world.getState().showCaptureToCluster)
						drawLine(world, c.getLocation(), vid.getCaptureLocation(), 190.f, 55.f, 255.f, mapWidth, mapHeight);
				}
			}
	}
	
	/**
	 * Draw line between two points on map
	 * @param point1 First point
	 * @param point2 Second Point
	 * @param hue Hue
	 * @param saturation Saturation
	 * @param brightness Brightness
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 */
	void drawLine(WMV_World world, PVector point1, PVector point2, float hue, float saturation, float brightness, float mapWidth, float mapHeight)
	{
		if(p.satelliteMap)
		{
			// -- Need to implement
		}
		else
		{
			PVector mapLoc1 = getMapLocation(world, point1, mapWidth, mapHeight);
			PVector mapLoc2 = getMapLocation(world, point2, mapWidth, mapHeight);

			if( (mapLoc1.x < mapWidth && mapLoc1.x > 0 && mapLoc1.y < mapHeight && mapLoc1.y > 0) ||
					(mapLoc2.x < mapWidth && mapLoc2.x > 0 && mapLoc2.y < mapHeight && mapLoc2.y > 0) )
			{
				float pointSize = smallPointSize * 0.1f * mapWidth / (float)Math.sqrt(Math.sqrt(mapDistance));
				world.p.strokeWeight(pointSize);
				world.p.stroke(hue, saturation, brightness, 255.f);
				world.p.pushMatrix();
				world.p.translate(mapLeftEdge, mapTopEdge);
				world.p.line( largeMapXOffset + mapLoc1.x, largeMapYOffset + mapLoc1.y, hudDistance * mapDistance,
						largeMapXOffset + mapLoc2.x, largeMapYOffset + mapLoc2.y, hudDistance * mapDistance );
				world.p.popMatrix();
			}
		}
	}
	
	/**
	 * @param image Image to draw
	 * @param ignoreTime Force image to display even when out of time
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @param capture Draw capture location (true) or viewing location (false)
	 * Draw image location on map of specified size
	 */
	void drawImageOnMap(WMV_World world, WMV_Image image, boolean ignoreTime, float mapWidth, float mapHeight, boolean capture)
	{
		if(p.satelliteMap)
		{
			/* Markers method */
//			if(!imageMarkers.hasValue(image.getID()))
//			{
//				PVector loc = image.getGPSLocation();  
//				SimplePointMarker marker = new SimplePointMarker(new Location(loc.z, loc.x));
//				marker.setId(String.valueOf(image.getID()));
//				marker.setColor(p.p.p.color(imageHue, maxSaturation, 255.f, mediaTransparency));
//				marker.setStrokeWeight(0);
//				marker.setDiameter(10.f);
//				markerManager.addMarker(marker);
//				imageMarkers.append(image.getID());
//			}
			
			float pointSize = smallPointSize * mapWidth;
			float saturation = lowSaturation;
//			float imageDistance = image.getViewingDistance();   // Get photo distance from current camera position
			PVector loc = new PVector(image.getGPSLocation().z, image.getGPSLocation().x); 
			drawPoint( world, loc, pointSize, mapWidth, mapHeight, imageCaptureHue, saturation, 255.f, mediaTransparency );
		}
		else
		{
			float pointSize = smallPointSize * mapWidth;
			float saturation = maxSaturation;
			float imageDistance = image.getViewingDistance();   // Get photo distance from current camera position
			boolean visible = ignoreTime;

			if (imageDistance < world.viewer.getSettings().getFarViewingDistance() && imageDistance > world.viewer.getSettings().getNearClippingDistance())    // If image is in visible range
				visible = true;                                              

			if(visible && image.getMediaState().location != null && !image.getMediaState().disabled && !image.getMediaState().hidden)
			{
				float alpha = 255.f;
				if(!ignoreTime && world.getState().timeFading)
					alpha = image.getTimeBrightness() * mediaTransparency;

				if(alpha > 0.f)
				{
					if(image.isSelected()) pointSize *= 5.f;
					if(capture)
						drawPoint( world, image.getCaptureLocation(), pointSize, mapWidth, mapHeight, imageCaptureHue, saturation, 255.f, mediaTransparency );
					else
						drawPoint( world, image.getLocation(),  pointSize, mapWidth, mapHeight, imageHue, saturation, 255.f, mediaTransparency );
				}
			}
		}
	}

	/**
	 * @param panorama Panorama to draw
	 * @param ignoreTime Force panorama to display even when out of time
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw image location on map of specified size
	 */
	void drawPanoramaOnMap(WMV_World world, WMV_Panorama panorama, boolean ignoreTime, float mapWidth, float mapHeight, boolean capture)
	{
		if(p.satelliteMap)
		{
//			if(!panoramaMarkers.hasValue(panorama.getID()))
//			{
//				PVector loc = panorama.getGPSLocation();
//				SimplePointMarker marker = new SimplePointMarker(new Location(loc.z, loc.x));
//				marker.setId(String.valueOf(panorama.getID()));
//				marker.setColor(p.p.p.color(imageHue, maxSaturation, 255.f, mediaTransparency));
//				marker.setStrokeWeight(0);
//				marker.setDiameter(3.f);
//				markerManager.addMarker(marker);
//				panoramaMarkers.append(panorama.getID());
//			}
		}
		else
		{
			float pointSize = mediumPointSize * mapWidth;
			float saturation = maxSaturation;
			float panoramaDistance = panorama.getViewingDistance();   // Get photo distance from current camera position
			boolean visible = ignoreTime;

			if (panoramaDistance < world.viewer.getSettings().getFarViewingDistance() && panoramaDistance > world.viewer.getSettings().getNearClippingDistance())    // If panorama is in visible range
				visible = true;                                              

			if(visible && panorama.getMediaState().location != null && !panorama.getMediaState().disabled && !panorama.getMediaState().hidden)
			{
				float alpha = 255.f;
				if(!ignoreTime && world.getState().timeFading)
					alpha = panorama.getTimeBrightness() * mediaTransparency;

				if(alpha > 0.f)
				{
					if(panorama.isSelected()) pointSize *= 5.f;
//					if(capture)
//						drawPoint( panorama.getCaptureLocation(),  pointSize, mapWidth, mapHeight, panoramaCaptureHue, saturation, 255.f, mediaTransparency );
//					else
						drawPoint( world, panorama.getLocation(),  pointSize, mapWidth, mapHeight, panoramaHue, saturation, 255.f, mediaTransparency );
				}
			}
		}
	}

	/**
	 * @param video Video to draw
	 * @param ignoreTime Force video to display even when out of time
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw image location on map of specified size
	 */
	void drawVideoOnMap(WMV_World world, WMV_Video video, boolean ignoreTime, float mapWidth, float mapHeight, boolean capture)
	{
		if(p.satelliteMap)
		{
//			if(!videoMarkers.hasValue(video.getID()))
//			{
//				PVector loc = video.getGPSLocation();
//				SimplePointMarker marker = new SimplePointMarker(new Location(loc.z, loc.x));
//				marker.setId(String.valueOf(video.getID()));
//				marker.setColor(p.p.p.color(imageHue, maxSaturation, 255.f, mediaTransparency));
//				marker.setStrokeWeight(0);
//				marker.setDiameter(3.f);
//				markerManager.addMarker(marker);
//				videoMarkers.append(video.getID());
//			}
		}
		else
		{
			float pointSize = mediumPointSize * mapWidth;
			float saturation = maxSaturation;
			float videoDistance = video.getViewingDistance();   // Get photo distance from current camera position
			boolean visible = ignoreTime;

			if (videoDistance < world.viewer.getSettings().getFarViewingDistance() && videoDistance > world.viewer.getSettings().getNearClippingDistance())    // If video is in visible range
				visible = true;                                              

			if(visible && video.getMediaState().location != null && !video.getMediaState().disabled && !video.getMediaState().hidden)
			{
				float alpha = 255.f;
				if(!ignoreTime && world.getState().timeFading)
					alpha = video.getTimeBrightness() * mediaTransparency;

				if(alpha > 0.f)
				{
					if(video.isSelected()) pointSize *= 5.f;
					if(capture)
						drawPoint( world, video.getCaptureLocation(), pointSize, mapWidth, mapHeight, videoCaptureHue, saturation, 255.f, mediaTransparency );
					else
						drawPoint( world, video.getLocation(), pointSize, mapWidth, mapHeight, videoHue, saturation, 255.f, mediaTransparency );
				}
			}
		}
	}

	/**
	 * Draw the map origin
	 * @param mapWidth
	 * @param mapHeight
	 */
	void drawOriginOnMap(WMV_World world, float mapWidth, float mapHeight)
	{
		int size = (int)(mapWidth / 40.f);
		for(int i=-size/2; i<size/2; i+=size/10)
			drawPoint( world, new PVector(i, 0.f, 0.f), hugePointSize * mapWidth, mapWidth, mapHeight, 180.f, 30.f, 255.f, mediaTransparency / 2.f );
		for(int i=-size/2; i<size/2; i+=size/10)
			drawPoint( world, new PVector(0.f, 0.f, i), hugePointSize * mapWidth, mapWidth, mapHeight, 180.f, 30.f, 255.f, mediaTransparency / 2.f );
	}
	
	/**
	 * Draw viewer as arrow on map
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw current viewer location and orientation on map of specified size
	 */
	void drawViewer(WMV_World world, float mapWidth, float mapHeight)
	{
		PVector camLoc = world.viewer.getLocation();
		if(pointIsVisible(world, camLoc, false))
		{
			float camYaw = -world.viewer.getXOrientation() - 0.5f * PApplet.PI;

			drawPoint( world, camLoc, cameraPointSize, mapWidth, mapHeight, cameraHue, 255.f, 255.f, mediaTransparency );
			float ptSize = cameraPointSize;

			float arrowSize = fieldAspectRatio >= 1 ? world.getCurrentModel().getState().fieldWidth : world.getCurrentModel().getState().fieldLength;
			arrowSize = PApplet.round(PApplet.map(arrowSize, 0.f, 2500.f, 0.f, 100.f) * PApplet.sqrt(mapDistance));

			ScaleMap logMap;
			logMap = new ScaleMap(6., arrowSize, 6., 60.);		/* Time fading interpolation */
			logMap.setMapFunction(world.circularEaseOut);

			int arrowPoints = 15;								/* Number of points in arrow */

			logMap = new ScaleMap(0.f, 0.25f, 0.f, 0.25f);		/* Time fading interpolation */
			logMap.setMapFunction(world.circularEaseOut);

			float shrinkFactor = 0.88f;
			float mapDistanceFactor = PApplet.map(mapDistance, 0.f, 1.f, 0.f, 0.25f);
			
			for(float i=1; i<arrowPoints; i++)
			{
				world.p.textSize(ptSize);
				float x = i * cameraPointSize * mapDistanceFactor * (float)Math.cos( camYaw );
				float y = i * cameraPointSize * mapDistanceFactor * (float)Math.sin( camYaw );

				PVector arrowPoint = new PVector(camLoc.x + x, 0, camLoc.z + y);
				drawPoint( world, arrowPoint, ptSize, mapWidth, mapHeight, cameraHue, 120.f, 255.f, 255.f );

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
	void drawPathOnMap(WMV_World world, ArrayList<WMV_Waypoint> path, float mapWidth, float mapHeight)
	{
//		System.out.println("drawPathOnMap..."+path.size());
		float pointSize = smallPointSize * mapWidth;
		
		float saturation = maxSaturation;                                              

		for(WMV_Waypoint w : path)
		{
			drawPoint( world, w.getLocation(), pointSize * 4.f, mapWidth, mapHeight, 30, saturation, 255.f, mediaTransparency );
//			System.out.println("Path ---> location.x:"+w.getLocation().x+" y:"+w.getLocation().y);
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
	void drawGMVClusters(WMV_World world)
	{		 
		if(  !p.initialSetup && p.messages.size() < 0 && p.metadata.size() < 0	 )
		{
			world.p.hint(PApplet.DISABLE_DEPTH_TEST);						// Disable depth testing for drawing HUD
		}

		for( WMV_Cluster c : world.getCurrentField().getClusters() )								// For all clusters at current depth
		{
			drawPoint( world, c.getLocation(), 5.f, curMapWidth, curMapHeight, mapClusterHue, 255.f, 255.f, mediaTransparency );
		}
	}

//	/**
//	 * Draw (on 2D map) a point given in 3D world coordinates 
//	 * @param point Point in 3D world coordinates
//	 * @param pointSize Point size
//	 * @param mapWidth Map width
//	 * @param mapHeight Map height
//	 * @param hue Point hue
//	 * @param saturation Point saturation
//	 * @param brightness Point brightness
//	 * @param transparency Point transparency
//	 */
//	public void highlightCluster( PVector point, float pointSize, float mapWidth, float mapHeight, float hue, float saturation, float brightness, float transparency )
//	{
//		float size = pointSize;
//		int iterations = PApplet.round(size);
//		int sizeDiff = PApplet.round(size/iterations);
//		float alpha = transparency;
//		float alphaDiff = transparency / iterations;
//		
//		for(int i=0; i<iterations; i++)
//		{
//			float ptSize = size * smallPointSize * mapWidth / PApplet.sqrt(PApplet.sqrt(mapDistance));
//			drawPoint( world, point, ptSize, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, alpha * 0.33f );
////			drawPoint( point, size * smallPointSize * mapWidth, mapWidth, mapHeight, mapClusterHue, 255.f, 255.f, alpha * 0.33f );
//
//			size-=sizeDiff;
//			alpha-=alphaDiff;
//		}
//	}
	
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
	public void drawPoint( WMV_World world, PVector point, float pointSize, float mapWidth, float mapHeight, float hue, float saturation, float brightness, float transparency )
	{
		if(!utilities.isNaN(point.x) && !utilities.isNaN(point.y) && !utilities.isNaN(point.z))
		{
			if(p.satelliteMap)
			{
				Location mapPoint = new Location(point.x, point.y);
				ScreenPosition screenPos = map.getScreenPosition(mapPoint);
				world.p.stroke(hue, saturation, brightness, transparency);
				world.p.strokeWeight(pointSize / PApplet.sqrt(PApplet.sqrt(mapDistance)));
				
				world.p.pushMatrix();
				world.p.point(screenPos.x, screenPos.y, screenPos.z);
				world.p.popMatrix();
			}
			else
			{
				PVector mapLoc = getMapLocation(world, point, mapWidth, mapHeight);

				if(mapLoc.x < mapWidth && mapLoc.x > 0 && mapLoc.y < mapHeight && mapLoc.y > 0)
				{
					world.p.stroke(hue, saturation, brightness, transparency);
					world.p.strokeWeight(pointSize / PApplet.sqrt(PApplet.sqrt(mapDistance)));
					world.p.pushMatrix();
					world.p.translate(mapLeftEdge, mapTopEdge);
					world.p.point(largeMapXOffset + mapLoc.x, largeMapYOffset + mapLoc.y, hudDistance * mapDistance);
					world.p.popMatrix();
				}
			}
		}
//		else if(world.p.debug.map) 
//			p.message(worldSettings, "Map point is NaN!:"+point+" hue:"+hue);
	}
	
	public void updateMapMouse(WMV_World world)
	{
		if(p.satelliteMap)
		{
			for (Marker m : map.getMarkers()) 
				m.setSelected(false);

			Marker marker = map.getFirstHitMarker(world.p.mouseX, world.p.mouseY);		// Select hit marker
			if (marker != null) 
			{
				marker.setSelected(true);

				String mID = marker.getId();

				if(!mID.equals("viewer"))
					selectedCluster = Integer.parseInt(mID);
			}
			else
			{
				selectedCluster = world.viewer.getState().getCurrentClusterID();
			}
		}
	}
	
	public void handleMouseReleased(WMV_World world, int mouseX, int mouseY)
	{
		if(mousePressedFrame > mouseDraggedFrame)
		{
			if(p.satelliteMap)				// If mouse was most recently pressed, rather than dragged
			{
				if(selectedCluster != world.viewer.getState().getCurrentClusterID())
				{
					if(selectedCluster >= 0 && selectedCluster < world.getCurrentField().getClusters().size())
					{
						world.viewer.teleportToCluster(selectedCluster, false, -1);
					}
				}
			}
			else 				// If mouse was most recently pressed, rather than dragged
			{
				if(selectedCluster != -1)
					zoomToCluster(world, world.getCurrentField().getCluster(selectedCluster));
			}
		}		
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
	void mapZoomTransition(WMV_World world, float zoomFactor)
	{
		zoomToRectangleTransition(world, 0.f, 0.f, zoomMapWidth * zoomFactor, zoomMapHeight * zoomFactor);
	}
	
	/**
	 * Draw map border
	 * @param mapWidth
	 * @param mapHeight
	 */
	void drawMapBorder(WMV_World world, float mapWidth, float mapHeight)
	{
		// Zoom map border
		world.p.stroke(133,255,255,255);	
		world.p.strokeWeight(1);
		
		world.p.pushMatrix();
		world.p.translate(mapLeftEdge, mapTopEdge);
		world.p.translate(zoomMapXOffset, zoomMapYOffset);
		world.p.line(0.f, 0.f, hudDistance * mapDistance, zoomMapWidth, 0.f, hudDistance * mapDistance );
		world.p.line(zoomMapWidth, 0, hudDistance * mapDistance, zoomMapWidth, zoomMapHeight, hudDistance * mapDistance );
		world.p.line(zoomMapWidth, zoomMapHeight, hudDistance * mapDistance, 0.f, zoomMapHeight, hudDistance * mapDistance );
		world.p.line(0.f, zoomMapHeight, hudDistance * mapDistance,  0.f, 0.f, hudDistance * mapDistance );
		world.p.popMatrix();
		
		if(world.p.debugSettings.map)		// Large map border
		{
			world.p.stroke(255,255,255,255);
			world.p.strokeWeight(2);
			world.p.pushMatrix();
			world.p.translate(mapLeftEdge, mapTopEdge);
			world.p.translate(largeMapXOffset, largeMapYOffset);
			world.p.line(0.f, 0.f, hudDistance * mapDistance, mapWidth, 0.f, hudDistance * mapDistance );
			world.p.line(mapWidth, 0, hudDistance * mapDistance,  mapWidth, mapHeight, hudDistance * mapDistance );
			world.p.line(mapWidth, mapHeight, hudDistance * mapDistance,  0.f, mapHeight, hudDistance * mapDistance );
			world.p.line(0.f, mapHeight, hudDistance * mapDistance,  0.f, 0.f, hudDistance * mapDistance );
			world.p.popMatrix();
		}
		
		if(world.p.debugSettings.map && world.p.debugSettings.detailed)
		{
			world.p.pushMatrix();
			world.p.stroke(0,255,255,255);
			world.p.strokeWeight(25);
			WMV_Cluster c = world.getCurrentField().getEdgeClusterOnXAxis(true);
			PVector point = getMapLocation(world, c.getLocation(), curMapWidth, curMapHeight);
			world.p.translate(mapLeftEdge, mapTopEdge);
			world.p.translate(largeMapXOffset, largeMapYOffset);
			world.p.point(point.x, point.y, hudDistance * mapDistance);
			world.p.popMatrix();

			world.p.pushMatrix();
			world.p.stroke(100,255,255,255);
			world.p.strokeWeight(35);
			c = world.getCurrentField().getEdgeClusterOnZAxis(true);
			point = getMapLocation(world, c.getLocation(), curMapWidth, curMapHeight);
			world.p.translate(mapLeftEdge, mapTopEdge);
			world.p.translate(largeMapXOffset, largeMapYOffset);
			world.p.point(point.x, point.y, hudDistance * mapDistance);
			world.p.popMatrix();
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

		mapDistance = zoomMapWidth / curMapWidth;
		mapLeftEdge = (screenWidth - zoomMapWidth)/2 - zoomMapLeftEdge;
		mapTopEdge = (screenHeight - zoomMapHeight)/2 - zoomMapTopEdge;
		
//		if(p.p.p.debug.map)
//		{
//			System.out.println("---> zoomToRectangle()...");
//			System.out.println("Set mapLeftEdge:" + mapLeftEdge);
//			System.out.println("Set mapTopEdge:" + mapTopEdge);
//			System.out.println("Set mapDistance:" + mapDistance);
//			System.out.println("Set zoomMapXOffset:" + zoomMapXOffset);
//			System.out.println("Set zoomMapYOffset:" + zoomMapYOffset);
//			System.out.println("Set zoomMapLeftEdge:" + zoomMapLeftEdge);
//			System.out.println("Set zoomMapTopEdge:" + zoomMapTopEdge);
//			System.out.println("Set zoomMapWidth:" + zoomMapWidth);
//			System.out.println("Set zoomMapHeight:" + zoomMapHeight);
//		}
//		mapZoomTransition();
		
		selectableClustersCreated = false;
	}

	/**
	 * Zoom in smoothly on a rectangular portion of the map
	 * @param rectXOffset
	 * @param rectYOffset
	 * @param rectWidth
	 * @param rectHeight
	 */
	void zoomToRectangleTransition(WMV_World world, float rectXOffset, float rectYOffset, float rectWidth, float rectHeight)
	{
		if(!zoomToRectangleTransition)
		{
			if(rectWidth / curMapWidth > mapDistanceMin && rectWidth / curMapWidth < mapDistanceMax )
			{
				zoomMapLeftEdge += rectXOffset;
				zoomMapTopEdge += rectYOffset;

				zoomToRectangleTransition = true;   
				zoomToRectangleTransitionStartFrame = world.p.frameCount;
				zoomToRectangleTransitionEndFrame = zoomToRectangleTransitionStartFrame + zoomToRectangleTransitionLength;
				selectableClustersCreated = false;

				zoomMapXOffsetTransitionStart = zoomMapXOffset;
				zoomMapXOffsetTransitionTarget = largeMapXOffset + zoomMapLeftEdge;
				zoomMapYOffsetTransitionStart = zoomMapYOffset;
				zoomMapYOffsetTransitionTarget = largeMapYOffset + zoomMapTopEdge;

				zoomMapWidthTransitionStart = zoomMapWidth;
				zoomMapWidthTransitionTarget = rectWidth;
				zoomMapHeightTransitionStart = zoomMapHeight;
				zoomMapHeightTransitionTarget = rectHeight;

				mapDistanceTransitionStart = mapDistance;
				mapDistanceTransitionTarget = zoomMapWidthTransitionTarget / curMapWidth;
				mapLeftEdgeTransitionStart = mapLeftEdge;
				mapLeftEdgeTransitionTarget = (screenWidth - zoomMapWidthTransitionTarget)/2 - zoomMapLeftEdge;
				mapTopEdgeTransitionStart = mapTopEdge;
				mapTopEdgeTransitionTarget = (screenHeight - zoomMapHeightTransitionTarget)/2 - zoomMapTopEdge;

				if(world.p.debugSettings.map)
					System.out.println("Started zoomToRectangleTransition transition...");
			}
		}
	}
	
	void updateZoomToRectangleTransition(WMV_World world)
	{
		float newZoomMapXOffset = zoomMapXOffset;
		float newZoomMapYOffset = zoomMapYOffset;
		float newZoomMapWidth = zoomMapWidth;
		float newZoomMapHeight = zoomMapHeight;
		float newMapDistance = mapDistance;
		float newMapLeftEdge = mapLeftEdge;
		float newMapTopEdge = mapTopEdge;

		if (world.p.frameCount >= zoomToRectangleTransitionEndFrame)		// Reached end of transition
		{
			zoomToRectangleTransition = false;
			newZoomMapXOffset = zoomMapXOffsetTransitionTarget;
			newZoomMapYOffset = zoomMapYOffsetTransitionTarget;
			newZoomMapWidth = zoomMapWidthTransitionTarget;
			newZoomMapHeight = zoomMapHeightTransitionTarget;
			newMapDistance = mapDistanceTransitionTarget;
			newMapLeftEdge = mapLeftEdgeTransitionTarget;
			newMapTopEdge = mapTopEdgeTransitionTarget;
			selectableClustersCreated = false;
		} 
		else
		{
			if(zoomMapXOffset != zoomMapXOffsetTransitionTarget)
			{
				newZoomMapXOffset = PApplet.map(world.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  		 zoomMapXOffsetTransitionStart, zoomMapXOffsetTransitionTarget); 
			}
			if(zoomMapYOffset != zoomMapYOffsetTransitionTarget)
			{
				newZoomMapYOffset = PApplet.map(world.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  		 zoomMapYOffsetTransitionStart, zoomMapYOffsetTransitionTarget);     			
			}
			if(zoomMapWidth != zoomMapWidthTransitionTarget)
			{
				newZoomMapWidth = PApplet.map(world.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
								  	  	   zoomMapWidthTransitionStart, zoomMapWidthTransitionTarget); 
			}
			if(zoomMapHeight != zoomMapHeightTransitionTarget)
			{
				newZoomMapHeight = PApplet.map(world.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  	    zoomMapHeightTransitionStart, zoomMapHeightTransitionTarget);     			
			}
			if(mapDistance != mapDistanceTransitionTarget)
			{
				newMapDistance = PApplet.map(world.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  	  mapDistanceTransitionStart, mapDistanceTransitionTarget); 
			}
			if(mapLeftEdge != mapLeftEdgeTransitionTarget)
			{
				newMapLeftEdge = PApplet.map(world.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
									  	  mapLeftEdgeTransitionStart, mapLeftEdgeTransitionTarget);     			
			}
			if(mapTopEdge != mapTopEdgeTransitionTarget)
			{
				newMapTopEdge = PApplet.map(world.p.frameCount, zoomToRectangleTransitionStartFrame, zoomToRectangleTransitionEndFrame,
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
	void zoomRectangleScrollTransition(WMV_World world, float mapXOffset, float mapYOffset)
	{
		if(mapXOffset != 0 || mapYOffset != 0)					// Check if already at target
		{
			if(mapXOffset != 0 && mapYOffset != 0)
			{
				zoomToRectangleTransition(world, (1.f-mapDistance)*mapXOffset, (1.f-mapDistance)*mapYOffset, zoomMapWidth, zoomMapHeight);
			}
			else if(mapXOffset != 0 && mapYOffset == 0)
			{
				zoomToRectangleTransition(world, (1.f-mapDistance)*mapXOffset, 0, zoomMapWidth, zoomMapHeight);
			}
			else if(mapXOffset == 0 && mapYOffset != 0)
			{
				zoomToRectangleTransition(world, 0, (1.f-mapDistance)*mapYOffset, zoomMapWidth, zoomMapHeight);
			}
		}
	}
	
	/**
	 * Transition map zoom from current to given value
	 */
	void mapScrollTransition(WMV_World world, float mapXOffset, float mapYOffset)
	{
		if(!scrollTransition)
		{
			if(mapXOffset != 0 || mapYOffset != 0)					// Check if already at target
			{
				scrollTransition = true;   
				scrollTransitionStartFrame = world.p.frameCount;
				scrollTransitionEndFrame = scrollTransitionStartFrame + scrollTransitionLength;
				selectableClustersCreated = false;
				
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
	public void updateMapScrollTransition(WMV_World world)
	{
		float newMapX = mapLeftEdge;
		float newMapY = mapTopEdge;

		if (world.p.frameCount >= scrollTransitionEndFrame)
		{
			scrollTransition = false;
			newMapX = mapXTransitionTarget;
			newMapY = mapYTransitionTarget;
		} 
		else
		{
			if(mapLeftEdge != mapXTransitionTarget)
			{
				newMapX = PApplet.map(world.p.frameCount, scrollTransitionStartFrame, scrollTransitionEndFrame,
									  mapXTransitionStart, mapXTransitionTarget); 
			}
			if(mapTopEdge != mapYTransitionTarget)
			{
				newMapY = PApplet.map(world.p.frameCount, scrollTransitionStartFrame, scrollTransitionEndFrame,
									  mapYTransitionStart, mapYTransitionTarget);     			
			}
		}

		if(mapLeftEdge != newMapX)
			mapLeftEdge = newMapX;
		if(mapTopEdge != newMapY)
			mapTopEdge = newMapY;
		
		if(world.p.debugSettings.map)
		{
			System.out.println("Updated mapLeftEdge:"+mapLeftEdge);
			System.out.println("Updated mapTopEdge:"+mapTopEdge);
		}
	}

	/**
	 * Zoom out on whole map
	 */
	void resetMapZoom(WMV_World world, boolean transition)
	{
//		mapLeftEdge = 0.f;
//		mapTopEdge = 0.f;
		zoomMapLeftEdge = 0.f;
		zoomMapTopEdge = 0.f;

		if(transition)
			zoomToRectangleTransition(world, 0, 0, curMapWidth, curMapHeight);	
		else
			zoomToRectangle(0, 0, curMapWidth, curMapHeight);	
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
	public void drawMousePointOnMap( WMV_World world, PVector point, float pointSize, float mapWidth, float mapHeight, float hue, float saturation, float brightness, float transparency )
	{		
		if(!utilities.isNaN(point.x) && !utilities.isNaN(point.y) && !utilities.isNaN(point.z))
		{
			if(point.x < mapWidth && point.x > 0 && point.y < mapHeight && point.y > 0)
			{
				world.p.stroke(hue, saturation, brightness, transparency);
				world.p.pushMatrix();

				world.p.strokeWeight(pointSize);
				world.p.translate(mapLeftEdge, mapTopEdge);
				world.p.point(largeMapXOffset + point.x, largeMapYOffset + point.y, hudDistance);
				
				world.p.popMatrix();
			}
		}
//		else p.message(worldSettings, "Map point is NaN!:"+point+" hue:"+hue);
	}
	
	private class SelectableClusterLocation
	{
		public int id;
		public PVector location;
		SelectableClusterLocation(int newID, PVector newLocation)
		{
			id = newID;
			location = newLocation;
		}
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
//				if(!utilities.isNaN(point.x) && !utilities.isNaN(point.y) && !utilities.isNaN(point.z))
//				{
//					PVector mapLoc = getMapLocation(point, mapWidth, mapHeight);
//
//					if(mapLoc.x < mapWidth && mapLoc.x > 0 && mapLoc.y < mapHeight && mapLoc.y > 0)
//					{
//						simpleClusters.stroke(mapClusterHue, 255.f, 255.f, 255.f);
//						float sw = (float)Math.sqrt(c.mediaPoints) * 0.5f;
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
	

//	public void handleMouseMoved(int mouseX, int mouseY)
//	{
//		for (Marker m : map.getMarkers()) 
//			m.setSelected(false);
//
//		List<Marker> markers = map.getHitMarkers(mouseX, mouseY);
////		Marker marker = map.getFirstHitMarker(mouseX, mouseY);		// Select hit marker
//		
////		for(Marker m : markers)
////		{
////			
////		}
//		if(markers.size() > 0)
//		{
//			Marker marker = markers.get(0);
//
//			if (marker != null) 
//				marker.setSelected(true);
//		}
//
//		// -- Use getHitMarkers(x, y) to allow multiple selection.
//	}
	
}

