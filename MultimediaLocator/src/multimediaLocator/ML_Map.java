package multimediaLocator;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.IntList;
import toxi.math.ScaleMap;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.core.Coordinate;
import de.fhpotsdam.unfolding.events.EventDispatcher;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.interactions.MouseHandler;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.marker.SimplePolygonMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.providers.OpenStreetMap;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;

/***********************************
 * Methods for displaying interactive 2D maps of 3D environments
 * @author davidgordon
 */
public class ML_Map 
{
	/* Map */
	private UnfoldingMap satellite, osm, large, small;
	private List<SimplePolygonMarker> fieldMarkers;		// Markers for fields in library
	private List<Location> fieldMarkerCenters, allClusterLocations;
	private Location satelliteMapCenter, fieldsMapCenter, plainMapCenter;
	private EventDispatcher eventDispatcher, plainMapEventDispatcher;
	private MarkerManager<Marker> satelliteMarkerManager, osmMarkerManager, smallMarkerManager, largeMarkerManager;
	private MultiMarker allClustersMarker;
	private SimplePointMarker viewerMarker, plainMapViewerMarker;
	private int clusterZoomLevel = 18, fieldZoomLevel = 14;
	
	/* Graphics */
	private float hudDistance;			// Distance of the Heads-Up Display from the virtual camera -- Change with zoom level??
	private int screenWidth = -1;
	private int screenHeight = -1;
	
	/* Interaction */
	public int mousePressedFrame = -1;
	public int mouseDraggedFrame = -1;
	private int selectedCluster = -1, selectedField = -1;
//	private IntList selectableClusterIDs;
//	private ArrayList<SelectableClusterLocation> selectableClusterLocations;

	private float mapDistance = 1.f;										// Obsolete soon
//	private final float mapDistanceMin = 0.04f, mapDistanceMax = 1.2f;	// Obsolete soon
	private float mapLeftEdge = 0.f, mapTopEdge = 0.f;					// Obsolete soon

	private float curMapWidth, curMapHeight;							// Obsolete soon
	private float largeMapXOffset, largeMapYOffset;						// Obsolete soon

	/* Media */
	float smallPointSize, mediumPointSize, largePointSize, hugePointSize;	// Obsolete soon
	float cameraPointSize;

	public final boolean mapMedia = true;
	public boolean mapImages = true, mapPanoramas = true, mapVideos = true;
	private float imageHue = 150.f, imageCaptureHue = 90.f;
	private float panoramaHue = 190.f, panoramaCaptureHue = 220.f;
	private float videoHue = 40.f, videoCaptureHue = 70.f;
	private float soundHue = 40.f;
	private float cameraHue = 140.f;
	private float mediaTransparency = 120.f;
	private float maxSaturation = 210.f, lowSaturation = 80.f;

	private float fieldAspectRatio;
	
	private final float zoomMapWidth = 500.f, zoomMapHeight = 500.f;
	private float zoomMapLeftEdge = 0.f, zoomMapTopEdge = 0.f;
	private float zoomMapXOffset, zoomMapYOffset;
//	private float zoomMapDefaultWidth, zoomMapDefaultHeight;

	public boolean scrollTransition = false;
//	private int scrollTransitionStartFrame = 0, scrollTransitionEndFrame = 0, scrollTransitionLength = 16;	
//	private float mapXTransitionStart, mapXTransitionTarget, mapYTransitionStart, mapYTransitionTarget;

//	public boolean zoomToRectangleTransition = false;   
//	private int zoomToRectangleTransitionStartFrame = 0, zoomToRectangleTransitionEndFrame = 0, zoomToRectangleTransitionLength = 16;
//
//	private float zoomMapXOffsetTransitionStart, zoomMapXOffsetTransitionTarget;
//	private float zoomMapYOffsetTransitionStart, zoomMapYOffsetTransitionTarget;
//	private float zoomMapWidthTransitionStart, zoomMapWidthTransitionTarget;
//	private float zoomMapHeightTransitionStart, zoomMapHeightTransitionTarget;
//	
//	private float mapDistanceTransitionStart, mapDistanceTransitionTarget;
//	private float mapLeftEdgeTransitionStart, mapLeftEdgeTransitionTarget;
//	private float mapTopEdgeTransitionStart, mapTopEdgeTransitionTarget;
	
	/* Fields Map */
	private final float fieldSelectedHue = 20.f, fieldSelectedSaturation = 255.f, fieldSelectedBrightness = 255.f;
	private final float clusterSaturation = 160.f, clusterBrightness = 185.f;
	private final float fieldTransparency = 80.f;
	private final float fieldHueRangeLow = 50.f, fieldHueRangeHigh = 160.f;
	PVector mapVectorOrigin, mapVectorVector;

	PImage blankTile;
	
	WMV_Utilities utilities;
	ML_Display p;

	/**
	 * Constructor for 2D map
	 * @param parent Parent display object
	 * @param newScreenWidth Screen width
	 * @param newScreenHeight Screen height
	 * @param newHUDDistance Heads-Up Display distance
	 */
	public ML_Map(ML_Display parent, int newScreenWidth, int newScreenHeight, float newHUDDistance)
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
		System.out.println("initializeMaps()..."+" frameCount:"+world.getState().frameCount);
		blankTile = world.p.loadImage("res/blank.jpg");

		initializeSatelliteMaps(world);
		initializeBasicMaps(world);
		
		eventDispatcher = MapUtils.createDefaultEventDispatcher(world.p, satellite, osm);
//		eventDispatcher = MapUtils.createDefaultEventDispatcher(world.p, satellite, osm, small, large);
		world.p.delay(200);
		
		p.initializedMaps = true;
	}
	
	/**
	 * Reset the map to initial state
	 */
	public void reset(WMV_World world)
	{
//		resetMapZoom(world, false);
		initializeMaps(world);
		setSelectedCluster( -1 );
	}

	/**
	 * Initialize maps
	 * @param p Parent world
	 */
	private void initializeSatelliteMaps(WMV_World p)
	{
		satellite = new UnfoldingMap(p.p, "Satellite", 0, 0, screenWidth, screenHeight, true, false, new Microsoft.AerialProvider());
		osm = new UnfoldingMap(p.p, "Map", 0, 0, screenWidth, screenHeight, true, false, new OpenStreetMap.OpenStreetMapProvider());
//		terrain = new UnfoldingMap(p.p, "Map", 0, 0, screenWidth, screenHeight, true, false, new Google.GoogleTerrainProvider());

		PVector gpsLoc = utilities.getGPSLocation(p.getCurrentField(), new PVector(0,0,0));
		satelliteMapCenter = new Location(gpsLoc.y, gpsLoc.x);
		
		if(p.p.display.satelliteMap)
			zoomToField(p, p.getCurrentField(), false);			// Start zoomed out on whole field

		satellite.setBackgroundColor(0);
		osm.setBackgroundColor(0);
		p.p.delay(100);
	
		satellite.setTweening(true);
		satellite.setZoomRange(2, 19);
		osm.setTweening(true);
		osm.setZoomRange(2, 19);

		createSatelliteMapsClusterMarkers(p);
		p.p.delay(100);
		
		PVector vLoc = p.viewer.getGPSLocation();
		viewerMarker = new SimplePointMarker(new Location(vLoc.y, vLoc.x));
		viewerMarker.setId("viewer");
		viewerMarker.setDiameter(20.f);
		viewerMarker.setColor(p.p.color(0, 0, 255, 255));
	}

	/**
	 * Initialize maps
	 * @param p Parent world
	 */
	private void initializeBasicMaps(WMV_World p)
	{
		large = new UnfoldingMap(p.p, "Map", 0, 0, screenWidth, screenHeight, true, false, new BlankMapProvider());
//		large = new UnfoldingMap(p.p, "Map", 0, 0, screenWidth, screenHeight, true, false, new Microsoft.AerialProvider());
		small = new UnfoldingMap(p.p, "Map", 0, 0, zoomMapWidth, zoomMapHeight, true, false, new BlankMapProvider());
		
		PVector gpsLoc = utilities.getGPSLocation(p.getCurrentField(), new PVector(0,0,0));
		plainMapCenter = new Location(gpsLoc.y, gpsLoc.x);

		large.setBackgroundColor(0);
		small.setBackgroundColor(0);
		p.p.delay(100);
		
		if(!p.p.display.satelliteMap)
			zoomToField(p, p.getCurrentField(), false);			// Start zoomed out on whole field

		large.setTweening(true);
		large.setZoomRange(2, 21);
		small.setTweening(true);
		small.setZoomRange(2, 21);

		// Add mouse interaction to map
		eventDispatcher = new EventDispatcher();
		MouseHandler mouseHandler = new MouseHandler(p.p, large);
		eventDispatcher.addBroadcaster(mouseHandler);
		eventDispatcher.register(large, "pan", large.getId());
		eventDispatcher.register(large, "zoom", large.getId());

		createBasicMapsClusterMarkers(p);
		p.p.delay(100);
		
		PVector vLoc = p.viewer.getGPSLocation();
		plainMapViewerMarker = new SimplePointMarker(new Location(vLoc.y, vLoc.x));
		plainMapViewerMarker.setId("viewer");
		plainMapViewerMarker.setDiameter(20.f);
		plainMapViewerMarker.setColor(p.p.color(0, 0, 255, 255));
	}
	
	/**
	 * Blank map tile provider
	 * @author davidgordon
	 */
	public class BlankMapProvider extends MBTilesMapProvider 
	{
		public PImage getTile(Coordinate coord)
		{
			return blankTile;
		}
	}
	
	/**
	 * Draw map filling the main window
	 */
	void displaySatelliteMap(WMV_World world)
	{
		PVector vLoc = world.viewer.getGPSLocation();
		Location gpsLoc = new Location(vLoc.y, vLoc.x);
		if(gpsLoc != null)
		{
			if(viewerMarker != null)
			{
				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker
				satelliteMarkerManager.addMarker(viewerMarker);					// -- Needed??
				osmMarkerManager.addMarker(viewerMarker);					// -- Needed??
			}
			else System.out.println("viewerMarker == null!"+" frameCount:"+world.getState().frameCount);
		}

		world.p.perspective();
		world.p.camera();												// Reset to default camera setting
		world.p.tint(255.f, 255.f);
		satellite.draw();														// Draw the Unfolding Map
	}
	
	/**
	 * Display map of all fields in library
	 */
	void displayWorldMap(WMV_World world)
	{
		PVector vLoc = world.viewer.getGPSLocation();
		Location gpsLoc = new Location(vLoc.y, vLoc.x);
		if(gpsLoc != null)
		{
			if(viewerMarker != null)
			{
				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker
				satelliteMarkerManager.addMarker(viewerMarker);					// -- Needed??
			}
			else System.out.println("viewerMarker == null!");
		}

		world.p.perspective();
		world.p.camera();												// Reset to default camera setting
		world.p.tint(255.f, 255.f);
		satellite.draw();
	}
	
	/**
	 * Display OSM map  
	 * @param world Parent world
	 */
	public void displayOSMcMap(WMV_World world)				// -- Need to finish
	{
		PVector vLoc = world.viewer.getGPSLocation();
		Location gpsLoc = new Location(vLoc.y, vLoc.x);
		if(gpsLoc != null)
		{
			if(viewerMarker != null)
			{
				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker
				smallMarkerManager.addMarker(viewerMarker);					// -- Needed??
				largeMarkerManager.addMarker(viewerMarker);					// -- Needed??
			}
			else System.out.println("viewerMarker == null!"+" frameCount:"+world.getState().frameCount);
		}

		world.p.perspective();
		world.p.camera();												// Reset to default camera setting
		world.p.tint(255.f, 255.f);
		osm.draw();														// Draw the Unfolding Map
	}
	
	/**
	 * Draw large map without satellite overlay 
	 * @param world Parent world
	 */
	public void displayLargeBasicMap(WMV_World world)				// -- Need to finish
	{
		PVector vLoc = world.viewer.getGPSLocation();
		Location gpsLoc = new Location(vLoc.y, vLoc.x);
		if(gpsLoc != null)
		{
			if(viewerMarker != null)
			{
				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker
				smallMarkerManager.addMarker(viewerMarker);					// -- Needed??
				largeMarkerManager.addMarker(viewerMarker);					// -- Needed??
			}
			else System.out.println("viewerMarker == null!"+" frameCount:"+world.getState().frameCount);
		}

		world.p.perspective();
		world.p.camera();												// Reset to default camera setting
		world.p.tint(255.f, 255.f);
		large.draw();														// Draw the Unfolding Map
	}

	/**
	 * Draw small map without satellite overlay 
	 * @param world Parent world
	 */
	public void displaySmallBasicMap(WMV_World world)				// -- Need to finish
	{
		PVector vLoc = world.viewer.getGPSLocation();
		Location gpsLoc = new Location(vLoc.y, vLoc.x);
		if(gpsLoc != null)
		{
			if(viewerMarker != null)
			{
				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker
				smallMarkerManager.addMarker(viewerMarker);					// -- Needed??
				largeMarkerManager.addMarker(viewerMarker);					// -- Needed??
			}
			else System.out.println("viewerMarker == null!"+" frameCount:"+world.getState().frameCount);
		}

		world.p.perspective();
		world.p.camera();												// Reset to default camera setting
		world.p.tint(255.f, 255.f);
		small.draw();														// Draw the Unfolding Map
	}

	/**
	 * Zoom in on cluster
	 * @param c Cluster to zoom in on
	 */
	void zoomToCluster(WMV_World world, WMV_Cluster c, boolean fade)
	{
		if(!c.isEmpty() && c.getState().mediaCount > 0)
		{
			PVector mapLoc = c.getLocation();
			PVector gpsLoc = utilities.getGPSLocation(world.getCurrentField(), mapLoc);

			if(p.displayView == 1)
			{
				if(p.satelliteMap)
				{
					zoomAndPanMapTo(satellite, clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);
					zoomAndPanMapTo(osm, clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);

//					if(fade)
//						satellite.zoomAndPanTo(clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
//					else
//					{
//						satellite.setTweening(false);
//						satellite.zoomAndPanTo(clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
//						satellite.setTweening(true);
//					}
				}
				else
				{
					zoomAndPanMapTo(large, clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);
					zoomAndPanMapTo(small, clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);

//					if(fade)
//						large.zoomAndPanTo(clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
//					else
//					{
//						large.setTweening(false);
//						large.zoomAndPanTo(clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
//						large.setTweening(true);
//					}
				}
			}
			else
			{
				zoomAndPanMapTo(satellite, clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);
				zoomAndPanMapTo(osm, clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);

//				if(fade)
//					satellite.zoomAndPanTo(clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
//				else
//				{
//					satellite.setTweening(false);
//					satellite.zoomAndPanTo(clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
//					satellite.setTweening(true);
//				}
			}
		}
	}

	/**
	 * Zoom in on cluster
	 * @param c Cluster to zoom in on
	 */
	void zoomToField(WMV_World world, WMV_Field f, boolean fade)
	{
		PVector gpsLoc = new PVector(f.getModel().getState().centerLongitude, f.getModel().getState().centerLatitude);
		if(p.displayView == 1)
		{
			if(p.satelliteMap)
			{
				zoomAndPanMapTo(satellite, fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);
				zoomAndPanMapTo(osm, fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);
//				if(fade)
//					satellite.zoomAndPanTo(fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
//				{
//					satellite.setTweening(false);
//					satellite.zoomAndPanTo(fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
//					satellite.setTweening(true);
//				}
			}
			else
			{
				zoomAndPanMapTo(small, fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);
				zoomAndPanMapTo(large, fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);
//				if(fade)
//					large.zoomAndPanTo(fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
//				else
//				{
//					large.setTweening(false);
//					large.zoomAndPanTo(fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
//					large.setTweening(true);
//				}
			}
		}
		else
		{
			zoomAndPanMapTo(satellite, fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);
			zoomAndPanMapTo(osm, fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);
//			if(fade)
//				satellite.zoomAndPanTo(fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
//			else
//			{
//				satellite.setTweening(false);
//				satellite.zoomAndPanTo(fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x));
//				satellite.setTweening(true);
//			}
		}
	}
	
	private void zoomAndPanMapTo(UnfoldingMap zoomMap, int fieldZoomLevel, Location location, boolean fade)
	{
		if(fade)
		{
			zoomMap.setTweening(true);
			zoomMap.zoomAndPanTo(fieldZoomLevel, location);
		}
		else
		{
			zoomMap.setTweening(false);
			zoomMap.zoomAndPanTo(fieldZoomLevel, location);
			zoomMap.setTweening(true);
		}
	}

	/**
	 * Zoom in map	-- Obsolete?
	 */
	public void zoomIn(WMV_World world)
	{
		if(p.displayView == 1)
		{
			if (p.satelliteMap) satellite.zoomIn();
			else large.zoomIn();
		}
		else
			satellite.zoomIn();
	}
	
	/**
	 * Zoom out map	-- Obsolete?
	 */
	public void zoomOut(WMV_World world)
	{
		if(p.displayView == 1)
		{
			if (p.satelliteMap) satellite.zoomOut();
			else large.zoomOut();
		}
		else
			satellite.zoomOut();
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
			point = getMapLocation(world, world.getCurrentField(), loc, curMapWidth, curMapHeight);

		if( point.x > xOff && point.x < zoomMapWidth + xOff && point.y > yOff && point.y < zoomMapHeight + yOff )
			return true;
		else
			return false;
	}
	
	/**
	 * Draw viewer as arrow on map
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw current viewer location and orientation on map of specified size
	 */
	private void drawViewer(WMV_World world, WMV_Field f, float mapWidth, float mapHeight)
	{
		PVector camLoc = world.viewer.getLocation();
		if(pointIsVisible(world, camLoc, false))
		{
			float camYaw = -world.viewer.getXOrientation() - 0.5f * PApplet.PI;

//			drawPoint( world, f, camLoc, cameraPointSize, mapWidth, mapHeight, cameraHue, 255.f, 255.f, mediaTransparency );
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
//				drawPoint( world, f, arrowPoint, ptSize, mapWidth, mapHeight, cameraHue, 120.f, 255.f, 255.f );

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
	void drawPathOnMap(WMV_World world, WMV_Field f, ArrayList<WMV_Waypoint> path, float mapWidth, float mapHeight)
	{
//		System.out.println("drawPathOnMap..."+path.size());
		float pointSize = smallPointSize * mapWidth;
		
		float saturation = maxSaturation;                                              

		for(WMV_Waypoint w : path)
		{
//			drawPoint( world, f, w.getLocation(), pointSize * 4.f, mapWidth, mapHeight, 30, saturation, 255.f, mediaTransparency );
//			System.out.println("Path ---> location.x:"+w.getLocation().x+" y:"+w.getLocation().y);
		}
	}

	/**
	 * Create satellite map markers for each cluster in current field
	 */
	private void createSatelliteMapsClusterMarkers(WMV_World world)
	{
		satelliteMarkerManager = new MarkerManager<Marker>();
		osmMarkerManager = new MarkerManager<Marker>();
		for( WMV_Cluster c : world.getCurrentField().getClusters() )	
		{
			if(!c.isEmpty() && c.getState().mediaCount != 0)
			{
				PVector mapLoc = c.getLocation();
				PVector gpsLoc = utilities.getGPSLocation(world.getCurrentField(), mapLoc);
				SimplePointMarker marker = new SimplePointMarker(new Location(gpsLoc.y, gpsLoc.x));
				marker.setId("Cluster_"+String.valueOf(c.getID()));
				marker.setColor(world.p.color(100.f, 165.f, 215.f, 225.f));			// Same color as time segments in Time View
				marker.setHighlightColor(world.p.color(170, 255, 255, 255.f));
				marker.setStrokeWeight(0);
				marker.setDiameter((float)Math.sqrt(c.getState().mediaCount) * 3.f);
				satelliteMarkerManager.addMarker(marker);
				osmMarkerManager.addMarker(marker);
			}
		}

		satellite.addMarkerManager(satelliteMarkerManager);
		osm.addMarkerManager(osmMarkerManager);
		satelliteMarkerManager.enableDrawing();
		osmMarkerManager.enableDrawing();
	}
	
	/**
	 * Create simple point markers for each cluster
	 * @param Parent world
	 */
	private void createBasicMapsClusterMarkers(WMV_World world)
	{
		smallMarkerManager = new MarkerManager<Marker>();
		largeMarkerManager = new MarkerManager<Marker>();
		for( WMV_Cluster c : world.getCurrentField().getClusters() )	
		{
			if(!c.isEmpty() && c.getState().mediaCount != 0)
			{
				PVector mapLoc = c.getLocation();
				PVector gpsLoc = utilities.getGPSLocation(world.getCurrentField(), mapLoc);
				SimplePointMarker marker = new SimplePointMarker(new Location(gpsLoc.y, gpsLoc.x));
				marker.setId("Cluster_"+String.valueOf(c.getID()));
				marker.setColor(world.p.color(100.f, 165.f, 215.f, 225.f));			// Same color as time segments in Time View
				marker.setHighlightColor(world.p.color(170, 255, 255, 255.f));
				marker.setStrokeWeight(0);
				marker.setDiameter((float)Math.sqrt(c.getState().mediaCount) * 3.f);
				smallMarkerManager.addMarker(marker);
				largeMarkerManager.addMarker(marker);
			}
		}
		
		large.addMarkerManager(largeMarkerManager);
		small.addMarkerManager(smallMarkerManager);
		largeMarkerManager.enableDrawing();
		smallMarkerManager.enableDrawing();
	}
	
	/**
	 * Recreate all map markers
	 * @param world Parent world
	 */
	public void recreateMarkers(WMV_World world)
	{
		createFieldMarkers(world);
		createSatelliteMapsClusterMarkers(world);
		createBasicMapsClusterMarkers(world);
		createWorldClusterMarkers(world);
	}
	
	/**
	 * Get map location for a given point
	 * @param world Parent world
	 * @param point Given point
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * @return
	 */
	PVector getMapLocation(WMV_World world, WMV_Field f, PVector point, float mapWidth, float mapHeight)
	{
		PVector gpsPoint = utilities.getGPSLocation(f, point);
		ScreenPosition screenPos = large.getScreenPosition(new Location(gpsPoint.y, gpsPoint.x));
		return new PVector(screenPos.x, screenPos.y);
	}

	/**
	 * Update map settings based on current mouse position
	 * @param world Parent world
	 */
	public void updateMouse(WMV_World world)
	{
		if(p.displayView == 1)						// Main map visible
		{
			List<Marker> hitMarkers;
			if(p.satelliteMap)
			{
				for (Marker m : satellite.getMarkers()) 
					if(m.isSelected()) m.setSelected(false);

				hitMarkers = satellite.getHitMarkers(world.p.mouseX, world.p.mouseY);
			}
			else
			{
				for (Marker m : large.getMarkers()) 
					if(m.isSelected()) m.setSelected(false);

				hitMarkers = large.getHitMarkers(world.p.mouseX, world.p.mouseY);
			}

			for(Marker marker : hitMarkers)
			{
				if(marker != null)
				{
					String mID = marker.getId();
					if(mID != null)
					{
//						System.out.println("mID:"+mID);
						String[] parts = mID.split("_");
						if(parts.length == 2)
						{
//							System.out.println("parts[0]:"+parts[0]);
							if(parts[0].equals("Cluster"))
							{
								marker.setSelected(true);
								setSelectedCluster( Integer.parseInt(parts[1]) );
							}
						}
					}
					else
					{
						setSelectedCluster( world.viewer.getState().getCurrentClusterID() );
					}
				}
				else
				{
					setSelectedCluster( world.viewer.getState().getCurrentClusterID() );
				}
			}
		}
		else if(p.displayView == 2 && p.libraryViewMode == 0)			// World map visible
		{
			for (Marker m : satellite.getMarkers()) 
				m.setSelected(false);
			
			List<Marker> hitMarkers = satellite.getHitMarkers(world.p.mouseX, world.p.mouseY);
			for(Marker marker : hitMarkers)
			{
				if(marker != null)
				{
					String mID = marker.getId();
					if(mID != null)
					{
						if (!marker.getId().equals("allClusters") && !mID.equals("viewer") && !mID.contains("Cluster_")) 
						{
							if(selectedField != Integer.parseInt(mID))
							{
//								System.out.println("Selected Field ID:"+Integer.parseInt(mID)+" mID:"+mID);
								marker.setSelected(true);
								setSelectedField( world, Integer.parseInt(mID) );
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Handle map mouse released event
	 * @param world Parent world
	 * @param mouseX Mouse x screen position
	 * @param mouseY Mouse y screen position
	 */
	public void handleMouseReleased(WMV_World world, int mouseX, int mouseY)
	{
		if(mousePressedFrame > mouseDraggedFrame)
		{
			if(p.displayView == 1)
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
				else 							// If mouse was most recently pressed, rather than dragged
				{
					if(selectedCluster != -1)
						zoomToCluster(world, world.getCurrentField().getCluster(selectedCluster), true);
				}
			}
			else if(p.displayView == 2 && p.libraryViewMode == 0)	// World map visible
			{
				if(selectedField >= 0 && selectedField < world.getFields().size())
				{
					p.currentDisplayCluster = 0;
					zoomToField(world, world.getField(selectedField), true);
				}
			}
		}		
	}
	
	/**
	 * @return Selected field ID
	 */
	public int getSelectedFieldID()
	{
		return selectedField;
	}
	
	/**
	 * Set selected field
	 * @param newField New selected field
	 */
	public void setSelectedField( WMV_World world, int newField )
	{
		selectedField = newField;
		recreateMarkers( world );
	}
	
	/**
	 * @return Selected cluster ID
	 */
	public int getSelectedClusterID()
	{
		return selectedCluster;
	}
	
	/**
	 * Set selected cluster
	 * @param newCluster New selected cluster
	 */
	public void setSelectedCluster( int newCluster )
	{
		selectedCluster = newCluster;
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
			PVector point = getMapLocation(world, world.getCurrentField(), c.getLocation(), curMapWidth, curMapHeight);
			world.p.translate(mapLeftEdge, mapTopEdge);
			world.p.translate(largeMapXOffset, largeMapYOffset);
			world.p.point(point.x, point.y, hudDistance * mapDistance);
			world.p.popMatrix();

			world.p.pushMatrix();
			world.p.stroke(100,255,255,255);
			world.p.strokeWeight(35);
			c = world.getCurrentField().getEdgeClusterOnZAxis(true);
			point = getMapLocation(world, world.getCurrentField(), c.getLocation(), curMapWidth, curMapHeight);
			world.p.translate(mapLeftEdge, mapTopEdge);
			world.p.translate(largeMapXOffset, largeMapYOffset);
			world.p.point(point.x, point.y, hudDistance * mapDistance);
			world.p.popMatrix();
		}
	}

	/**
	 * Initialize map for all fields in world
	 * @param world Parent world
	 */
	public void initializeFieldsMap(WMV_World world, boolean fade)
	{
		createFieldMarkers(world);
		createWorldClusterMarkers(world);
		
		float highLongitude = -100000, lowLongitude = 100000;
		float highLatitude = -100000, lowLatitude = 100000;
		
		for(WMV_Field f : world.getFields())				// -- Precalculate
		{
			if(f.getModel().getState().lowLongitude < lowLongitude)
				lowLongitude = f.getModel().getState().lowLongitude;
			if(f.getModel().getState().lowLatitude < lowLatitude)
				lowLatitude = f.getModel().getState().lowLatitude;
			if(f.getModel().getState().highLongitude > highLongitude)
				highLongitude = f.getModel().getState().highLongitude;
			if(f.getModel().getState().highLatitude > highLatitude)
				highLatitude = f.getModel().getState().highLatitude;
		}

		fieldsMapCenter = new Location(highLatitude, highLongitude);				// -- Obsolete
		setSelectedField( world, world.getCurrentField().getID() );

		if(fade)
		{
			satellite.zoomAndPanToFit(allClusterLocations);
		}
		else
		{
			satellite.setTweening(false);
			satellite.zoomAndPanToFit(allClusterLocations);
			satellite.zoomAndPanToFit(allClusterLocations);		// Not a typo, not reliable on first call
			satellite.setTweening(true);
		}
		
		p.initializedFieldMap = true;
	}
	
	/**
	 * Create simple point markers for each cluster
	 * @param world Parent world
	 */
	private void createFieldMarkers(WMV_World world)
	{
		fieldMarkers = new ArrayList<SimplePolygonMarker>();
		fieldMarkerCenters = new ArrayList<Location>();
		
		int count = 0;
		for( WMV_Field f : world.getFields() )	
		{
			ArrayList<Location> locations = new ArrayList<Location>();
			
			if(f.getBorder() == null)
				f.calculateBorderPoints();
			
			for(PVector pv : f.getBorder())			
			{
				Location loc = new Location(pv.y, pv.x);
				locations.add(loc);
			}
			
			float hue = utilities.mapValue(count, 0, world.getFields().size(), fieldHueRangeLow, fieldHueRangeHigh);
			
			SimplePolygonMarker fieldMarker = new SimplePolygonMarker(locations);
			fieldMarker.setId(String.valueOf(f.getID()));
			
			fieldMarker.setStrokeWeight(0);
//			fieldMarker.setHidden(true);

			if(f.getID() == getSelectedFieldID())
			{
				fieldMarker.setColor(world.p.color(fieldSelectedHue, fieldSelectedSaturation, fieldSelectedBrightness, 0.f));		
//				fieldMarker.setColor(world.p.color(fieldSelectedHue, fieldSelectedSaturation, fieldSelectedBrightness, fieldTransparency));		
			}
			else
			{
				fieldMarker.setColor(world.p.color(hue, 255.f, 255.f, 0.f));		
//				fieldMarker.setHighlightColor(world.p.color(fieldSelectedHue, 0.f, 0.f, 0.f));
			}
			
			fieldMarker.setHighlightColor(world.p.color(fieldSelectedHue, fieldSelectedSaturation, fieldSelectedBrightness, 0.f));
			fieldMarkers.add(fieldMarker);
			
			Location fieldCenterPoint = new Location(f.getModel().getState().centerLatitude, f.getModel().getState().centerLongitude);
			fieldMarkerCenters.add(fieldCenterPoint);
			
			// Draw field center
//			WMV_ModelState m = f.getModel().getState();
//			PVector centerPoint = new PVector(m.centerLongitude, m.centerLatitude);
//			Location loc = new Location(centerPoint.y, centerPoint.x);
//			SimplePointMarker centerMarker = new SimplePointMarker(loc);
//			centerMarker.setDiameter(10.f);
//			centerMarker.setColor(world.p.color(15.f, 15.f, 255.f, 255.f));			// Same color as time segments in Time View
//			multiMarker.addMarkers(centerMarker);
			
			count++;
		}

		for(SimplePolygonMarker marker : fieldMarkers)
			satellite.addMarkers(marker);
	}
	
	private void createWorldClusterMarkers(WMV_World world)
	{
		allClusterLocations = new ArrayList<Location>();
		allClustersMarker = new MultiMarker();
		
		int fCount = 0;
		for( WMV_Field f : world.getFields() )	
		{
			for(WMV_Cluster c : f.getClusters())
			{
				PVector cLoc = c.getLocation();
				PVector gpsLoc = utilities.getGPSLocation(f, cLoc);
				Location loc = new Location(gpsLoc.y, gpsLoc.x);
				
				float hue = utilities.mapValue(fCount, 0, world.getFields().size(), fieldHueRangeLow, fieldHueRangeHigh);
				
				SimplePointMarker clusterMarker = new SimplePointMarker(loc);
				clusterMarker.setId("Cluster_"+String.valueOf(c.getID()));
				clusterMarker.setDiameter((float)Math.sqrt(c.getState().mediaCount) * 3.f);

				if(selectedField == f.getID())
					clusterMarker.setColor(world.p.color(hue, clusterSaturation, clusterBrightness, fieldTransparency));			
				else
					clusterMarker.setColor(world.p.color(hue, fieldSelectedSaturation, fieldSelectedBrightness, fieldTransparency));			

				clusterMarker.setStrokeWeight(0);

				if(mapMedia) allClustersMarker.addMarkers(clusterMarker);
				allClusterLocations.add(loc);
			}
			fCount++;
		}

		allClustersMarker.setId("allClusters");
		satellite.addMarkers(allClustersMarker);
		osm.addMarkers(allClustersMarker);
	}

	public float getMapDistance()		// -- Obsolete
	{
		return mapDistance;
	}
}

