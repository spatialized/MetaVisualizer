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
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
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
//	private Location satelliteMapCenter, worldMapCenter, plainMapCenter;
	
	private EventDispatcher eventDispatcher, plainMapEventDispatcher;
	public MarkerManager<Marker> satelliteMarkerManager, osmMarkerManager, smallMarkerManager, largeMarkerManager;
	private MultiMarker allClustersMarker;
	private SimplePointMarker viewerMarker, plainMapViewerMarker;
	private SimpleLinesMarker gpsTrackMarker;
	
	private int clusterZoomLevel = 18, fieldZoomLevel = 14;
	
	/* Graphics */
	private int screenWidth = -1;
	private int screenHeight = -1;
	private final float viewerDiameter = 15.f;
	private final int viewerArrowPoints = 9;
	private float viewerArrowPointSpacingFactor;
			
	/* Interaction */
	public int mousePressedFrame = -1;
	public int mouseDraggedFrame = -1;
	private int selectedCluster = -1, selectedField = -1;
	private boolean zoomingIn = false, zoomingOut = false;
	public boolean panningLeft = false, panningRight = false, panningUp = false, panningDown = false;

	/* Media */
	float smallPointSize, mediumPointSize, largePointSize, hugePointSize;	// Obsolete soon

	public final boolean mapMedia = true;
	public boolean mapImages = true, mapPanoramas = true, mapVideos = true, mapSounds = true;
	private float maxSaturation = 210.f, lowSaturation = 80.f;
	
	private final float imageHue = 150.f, imageCaptureHue = 90.f;
	private final float panoramaHue = 190.f;
	private final float videoHue = 40.f, videoCaptureHue = 70.f;
	private final float soundHue = 40.f;
	private final float cameraHue = 140.f;
	private final float mediaTransparency = 120.f;
	
	private final float zoomMapWidth = 500.f, zoomMapHeight = 500.f;

	/* Fields Map */
	PVector mapVectorOrigin, mapVectorVector;
	private final float fieldSelectedHue = 20.f, fieldSelectedSaturation = 255.f, fieldSelectedBrightness = 255.f;
	private final float clusterSaturation = 160.f, clusterBrightness = 185.f;
	private final float fieldTransparency = 80.f;
	private final float fieldHueRangeLow = 50.f, fieldHueRangeHigh = 160.f;
	
	WMV_Utilities utilities;
	ML_Display p;
	
	PImage blankTile;

	/**
	 * Constructor for 2D map
	 * @param parent Parent display object
	 * @param newScreenWidth Screen width
	 * @param newScreenHeight Screen height
	 * @param newHUDDistance Heads-Up Display distance
	 */
	public ML_Map(ML_Display parent)
	{
		p = parent;
		screenWidth = p.ml.appWidth;
		screenHeight = p.ml.appHeight;
		
		utilities = new WMV_Utilities();
		
		smallPointSize = 0.0000022f * screenWidth;
		mediumPointSize = 0.0000028f * screenWidth;
		largePointSize = 0.0000032f * screenWidth;
		hugePointSize = 0.0000039f * screenWidth;
		viewerArrowPointSpacingFactor = 0.0033f * screenWidth;
	}
	
	/**
	 * Initialize maps
	 */
	public void initialize(WMV_World world)
	{
		blankTile = world.ml.getImageResource("blank.jpg");

		initializeSatelliteMap(world);
		
		initializeBasicMaps(world);
		createViewerMarker(world);

		if(world.getCurrentField().getGPSTracks() != null)
		{
			if(world.getCurrentField().getGPSTracks().size() > 0)
			{
				if(world.viewer.getSelectedGPSTrackID() != -1)
				{
					createGPSTrackMarker(world, world.viewer.getGPSTrack());
				}
			}
		}
		
		zoomToField(world, world.getCurrentField(), false);			// Start zoomed out on whole field

//		eventDispatcher = MapUtils.createDefaultEventDispatcher(world.p, satellite, osm);
		eventDispatcher = new EventDispatcher();
		MouseHandler mouseHandler = new MouseHandler(world.ml, satellite);
		eventDispatcher.addBroadcaster(mouseHandler);
		eventDispatcher.register(satellite, "pan", satellite.getId());
		eventDispatcher.register(satellite, "zoom", satellite.getId());
		
		world.ml.delay(150);
		
		setSelectedCluster( -1 );
		p.initializedMaps = true;
	}
	

	/**
	 * Reset to initial state
	 */
	public void reset()
	{
		mousePressedFrame = -1;
		mouseDraggedFrame = -1;
		setSelectedCluster( -1 );
		selectedField = -1;
		zoomingIn = false; 
		zoomingOut = false;
		panningLeft = false;
		panningRight = false;
		panningUp = false;
		panningDown = false;
	}

	/**
	 * Initialize maps
	 * @param p Parent world
	 */
	private void initializeSatelliteMap(WMV_World p)
	{
//		System.out.println("initializeSatelliteMap()...");
		satellite = new UnfoldingMap(p.ml, "Satellite", 0, 0, screenWidth, screenHeight, true, false, new Microsoft.AerialProvider());
		osm = new UnfoldingMap(p.ml, "Map", 0, 0, screenWidth, screenHeight, true, false, new OpenStreetMap.OpenStreetMapProvider());

//		PVector gpsLoc = utilities.getGPSLocation(p.getCurrentField(), new PVector(0,0,0));
		
		satellite.setBackgroundColor(0);
		osm.setBackgroundColor(0);
		p.ml.delay(120);
	
		satellite.setTweening(true);
		satellite.setZoomRange(2, 19);
		osm.setTweening(true);
		osm.setZoomRange(2, 19);

		createFieldClusterMarkers(p);
		p.ml.delay(120);
		
		PVector vLoc = p.viewer.getGPSLocation();
		viewerMarker = new SimplePointMarker(new Location(vLoc.y, vLoc.x));
		viewerMarker.setId("viewer");
		viewerMarker.setDiameter(viewerDiameter);
		viewerMarker.setColor(p.ml.color(0, 0, 255, 255));
		p.ml.delay(120);
	}

	/**
	 * Initialize basic maps
	 * @param p Parent world
	 */
	private void initializeBasicMaps(WMV_World p)
	{
		large = new UnfoldingMap(p.ml, "Map", 0, 0, screenWidth, screenHeight, true, false, new BlankMapProvider());
//		large = new UnfoldingMap(p.ml, "Map", 0, 0, screenWidth, screenHeight, true, false, new Microsoft.AerialProvider());
		small = new UnfoldingMap(p.ml, "Map", 0, 0, zoomMapWidth, zoomMapHeight, true, false, new BlankMapProvider());
		
		PVector gpsLoc = utilities.getGPSLocation(p.getCurrentField(), new PVector(0,0,0));

		large.setBackgroundColor(0);
		small.setBackgroundColor(0);
		p.ml.delay(100);
		
		large.setTweening(true);
		large.setZoomRange(2, 21);
		small.setTweening(true);
		small.setZoomRange(2, 21);

		/* Add mouse interaction */
		eventDispatcher = new EventDispatcher();
		MouseHandler mouseHandler = new MouseHandler(p.ml, large);
		eventDispatcher.addBroadcaster(mouseHandler);
		eventDispatcher.register(large, "pan", large.getId());
		eventDispatcher.register(large, "zoom", large.getId());

		createBasicMapsClusterMarkers(p);
		p.ml.delay(100);
		
		PVector vLoc = p.viewer.getGPSLocation();
		plainMapViewerMarker = new SimplePointMarker(new Location(vLoc.y, vLoc.x));
		plainMapViewerMarker.setId("viewer");
		plainMapViewerMarker.setDiameter(viewerDiameter);
		plainMapViewerMarker.setColor(p.ml.color(0, 0, 255, 255));
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
	 * Draw satellite map of current field
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
//				satelliteMarkerManager.addMarker(viewerMarker);					// -- Needed??
//				osmMarkerManager.addMarker(viewerMarker);					// -- Needed??
			}
			else System.out.println("viewerMarker == null!"+" frameCount:"+world.getState().frameCount);
		}

//		world.ml.perspective();
		p.startHUD();
		world.ml.tint(255.f, 255.f);
		satellite.draw();												// Draw the Unfolding Map
		displayViewerOrientation(world, world.getCurrentField(), satellite);						// Draw the viewer arrow
	}
	
	/**
	 * Display satellite map of all fields in library
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
//				satelliteMarkerManager.addMarker(viewerMarker);					// -- Needed??
			}
			else System.out.println("viewerMarker == null!");
		}

//		world.ml.perspective();
		p.startHUD();
		world.ml.tint(255.f, 255.f);
		satellite.draw();
		displayViewerOrientation(world, world.getCurrentField(), satellite);		// Draw the viewer arrow
	}
	
	/**
	 * Display OSM map  
	 * @param world Parent world
	 */
	public void displayOSMMap(WMV_World world)				// -- Need to finish
	{
		PVector vLoc = world.viewer.getGPSLocation();
		Location gpsLoc = new Location(vLoc.y, vLoc.x);
		if(gpsLoc != null)
		{
			if(viewerMarker != null)
			{
				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker
//				osmMarkerManager.addMarker(viewerMarker);					// -- Needed??
			}
			else System.out.println("viewerMarker == null!"+" frameCount:"+world.getState().frameCount);
		}

		world.ml.perspective();
		world.ml.camera();												// Reset to default camera setting
		world.ml.tint(255.f, 255.f);
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
				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker
			else 
				System.out.println("viewerMarker == null!"+" frameCount:"+world.getState().frameCount);
		}

		world.ml.perspective();
		world.ml.camera();												// Reset to default camera setting
		world.ml.tint(255.f, 255.f);
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
				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker
			else 
				System.out.println("viewerMarker == null!"+" frameCount:"+world.getState().frameCount);
		}

		world.ml.perspective();
		world.ml.camera();												// Reset to default camera setting
		world.ml.tint(255.f, 255.f);
		small.draw();														// Draw the Unfolding Map
	}

	/**
	 * Zoom in on cluster
	 * @param world Parent world
	 * @param c Cluster to zoom in on
	 * @param fade Whether to fade smoothly or jump
	 */
	void zoomToCluster(WMV_World world, WMV_Cluster c, boolean fade)
	{
		if(!c.isEmpty() && c.getState().mediaCount > 0)
		{
			System.out.println("Zooming to cluster:"+c.getID());
			PVector mapLoc = c.getLocation();
			PVector gpsLoc = utilities.getGPSLocation(world.getCurrentField(), mapLoc);
			zoomAndPanMapTo(satellite, clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);
			zoomAndPanMapTo(osm, clusterZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);
		}
	}
	
	public void zoomToWorld(boolean fade)
	{
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
	}

	/**
	 * Zoom in on field
	 * @param world Parent world
	 * @param f Field to zoom in on
	 * @param fade Whether to fade smoothly or jump
	 */
	void zoomToField(WMV_World world, WMV_Field f, boolean fade)
	{
		PVector gpsLoc = new PVector(f.getModel().getState().centerLongitude, f.getModel().getState().centerLatitude);
		zoomAndPanMapTo(satellite, fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);
		zoomAndPanMapTo(osm, fieldZoomLevel, new Location(gpsLoc.y, gpsLoc.x), fade);
	}
	
	/**
	 * Zoom and pan map to a location and zoom level
	 * @param zoomMap Map to zoom and pan
	 * @param fieldZoomLevel New zoom level
	 * @param location New map center
	 * @param fade Whether to fade smoothly or jump
	 */
	private void zoomAndPanMapTo(UnfoldingMap zoomMap, int fieldZoomLevel, Location location, boolean fade)
	{
		if(fade)
		{
			if(!zoomMap.isTweening()) zoomMap.setTweening(true);
			zoomMap.zoomAndPanTo(fieldZoomLevel, location);
		}
		else
		{
			if(zoomMap.isTweening()) zoomMap.setTweening(false);
			zoomMap.zoomAndPanTo(fieldZoomLevel, location);
			if(!zoomMap.isTweening()) zoomMap.setTweening(true);
		}
	}

	/**
	 * Start zooming in map
	 */
	public void zoomIn(WMV_World world)
	{
		zoomingIn = true;
	}
	
	/**
	 * Start zooming out map
	 */
	public void zoomOut(WMV_World world)
	{
		zoomingOut = true;
	}
	
	/**
	 * Stop zooming the map
	 */
	public void stopZooming()
	{
		if(zoomingIn) zoomingIn = false;
		if(zoomingOut) zoomingOut = false;
	}
	
	/**
	 * Start panning map to left
	 */
	public void panLeft()
	{
		panningLeft = true;
	}
	
	/**
	 * Start panning map to right
	 */
	public void panRight()
	{
		panningRight = true;
	}
	
	/**
	 * Start panning map up
	 */
	public void panUp()
	{
		panningUp = true;
	}
	
	/**
	 * Start panning map down
	 */
	public void panDown()
	{
		panningDown = true;
	}
	
	/**
	 * Stop panning the map
	 */
	public void stopPanning()
	{
		if(panningLeft) panningLeft = false;
		if(panningRight) panningRight = false;
		if(panningUp) panningUp = false;
		if(panningDown) panningDown = false;
	}

	/**
	 * Stop panning the map
	 */
	public boolean isPanning()
	{
		return panningLeft || panningRight || panningUp || panningDown;
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
//		float pointSize = smallPointSize * mapWidth;
//		float saturation = maxSaturation;                                              

		for(WMV_Waypoint w : path)
		{
//			drawPoint( world, f, w.getLocation(), pointSize * 4.f, mapWidth, mapHeight, 30, saturation, 255.f, mediaTransparency );
//			System.out.println("Path ---> location.x:"+w.getLocation().x+" y:"+w.getLocation().y);
		}
	}

	/**
	 * Create satellite map markers for clusters in current field
	 * @param world Parent world
	 */
	private void createFieldClusterMarkers(WMV_World world)
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
				marker.setColor(world.ml.color(100.f, 165.f, 215.f, 225.f));			// Same color as time segments in Time View
				marker.setHighlightColor(world.ml.color(170, 255, 255, 255.f));
				marker.setStrokeWeight(0);
//				marker.setDiameter((float)Math.sqrt(c.getState().mediaCount) * 3.f);
				marker.setDiameter((float)Math.sqrt(c.getMediaWeight()) * 3.f);
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
	 * @param world Parent world
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
				marker.setColor(world.ml.color(100.f, 165.f, 215.f, 225.f));			// Same color as time segments in Time View
				marker.setHighlightColor(world.ml.color(170, 255, 255, 255.f));
				marker.setStrokeWeight(0);
				marker.setDiameter((float)Math.sqrt(c.getState().mediaCount) * 3.f);
				smallMarkerManager.addMarker(marker);
				largeMarkerManager.addMarker(marker);
			}
		}
		
		large.addMarkerManager(largeMarkerManager);
		small.addMarkerManager(smallMarkerManager);

		if(p.displayView == 1)
		{
			largeMarkerManager.enableDrawing();
			smallMarkerManager.enableDrawing();
		}
	}
	
	/**
	 * Recreate all map markers
	 * @param world Parent world
	 */
	public void recreateMarkers(WMV_World world)
	{
		createFieldMarkers(world);
		createFieldClusterMarkers(world);
		createBasicMapsClusterMarkers(world);
		createWorldClusterMarkers(world);
		createViewerMarker(world);
	}
	
	private void createViewerMarker(WMV_World world)
	{
		PVector vLoc = world.viewer.getGPSLocation();
		Location gpsLoc = new Location(vLoc.y, vLoc.x);
		if(gpsLoc != null)
			if(viewerMarker != null)
				viewerMarker.setLocation(gpsLoc);						// Update location of viewer marker

		satelliteMarkerManager.addMarker(viewerMarker);				
		osmMarkerManager.addMarker(viewerMarker);				
		largeMarkerManager.addMarker(viewerMarker);			
		smallMarkerManager.addMarker(viewerMarker);				
	}
	
	private void createGPSTrackMarker(WMV_World world, ArrayList<WMV_Waypoint> gpsTrack)
	{
		List<Location> gpsPoints = new ArrayList<Location>();
		
		for(WMV_Waypoint w : gpsTrack)					 /* Waypoint GPS location format: {longitude, latitude} */
		{
			Location loc = new Location(w.getGPSLocation().y, w.getGPSLocation().x);	 /* Unfolding Location format: {latitude, longitude} */
			gpsPoints.add(loc);
		}
		
		if(gpsPoints.size() > 0)
		{
			gpsTrackMarker = new SimpleLinesMarker(gpsPoints);
			satelliteMarkerManager.addMarker(gpsTrackMarker);
		}
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
	 * Update map
	 */
	public void update(WMV_World world)
	{
		if(zoomingIn)
		{
			float zoomInPct = 1.f - 0.01f * satellite.getZoomLevel();
			satellite.zoom(zoomInPct);
		}
		if(zoomingOut)
		{
			float zoomInPct = 1.f - 0.01f * satellite.getZoomLevel();
			float zoomOutPct = 1.f / zoomInPct;
			satellite.zoom(zoomOutPct);
		}
		if(panningLeft)
		{
			float panAmount = satellite.getZoomLevel();
			satellite.panBy(panAmount, 0.f);
		}
		if(panningRight)
		{
			float panAmount = satellite.getZoomLevel();
			satellite.panBy(-panAmount, 0.f);
		}
		if(panningUp)
		{
			float panAmount = satellite.getZoomLevel();
			satellite.panBy(0.f, panAmount);
		}
		if(panningDown)
		{
			float panAmount = satellite.getZoomLevel();
			satellite.panBy(0.f, -panAmount);
		}
		updateMouse(world);
	}
	
	/**
	 * Update map settings based on current mouse position
	 * @param world Parent world
	 */
	public void updateMouse(WMV_World world)
	{
		if(p.displayView == 1)							// In Map View
		{
			if(p.mapViewMode == 1)						// Field Mode
			{
				if(world.ml.display.initializedMaps)
				{
					List<Marker> hitMarkers;
					for (Marker m : satellite.getMarkers()) 
						if(m.isSelected()) m.setSelected(false);

					hitMarkers = satellite.getHitMarkers(world.ml.mouseX, world.ml.mouseY);
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
			}
			else if(p.mapViewMode == 0)						// World Mode
			{
				if(world.ml.display.initializedMaps)
				{
					for (Marker m : satellite.getMarkers()) 
						m.setSelected(false);

					List<Marker> hitMarkers = satellite.getHitMarkers(world.ml.mouseX, world.ml.mouseY);
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
			if(p.displayView == 1)					// In Map View
			{
				if(p.mapViewMode == 1)				// Field Mode
				{
					if(selectedCluster != world.viewer.getState().getCurrentClusterID())
					{
						if(selectedCluster >= 0 && selectedCluster < world.getCurrentField().getClusters().size())
							zoomToCluster(world, world.getCurrentField().getCluster(selectedCluster), true);
					}
				}
				else if(p.mapViewMode == 0)			// World Mode
				{
					if(selectedField >= 0 && selectedField < world.getFields().size())
					{
						p.currentDisplayCluster = 0;
						zoomToField(world, world.getField(selectedField), true);
					}
				}
			}
		}
	}
	
	/**
	 * Initialize map for all fields in world
	 * @param world Parent world
	 */
	public void initializeWorldMap(WMV_World world, boolean fade)
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

//		worldMapCenter = new Location(highLatitude, highLongitude);				// -- Obsolete
		setSelectedField( world, world.getCurrentField().getID() );

		zoomToWorld(fade);
		
		p.initializedWorldMap = true;
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
				fieldMarker.setColor(world.ml.color(fieldSelectedHue, fieldSelectedSaturation, fieldSelectedBrightness, 0.f));		
//				fieldMarker.setColor(world.p.color(fieldSelectedHue, fieldSelectedSaturation, fieldSelectedBrightness, fieldTransparency));		
			}
			else
			{
				fieldMarker.setColor(world.ml.color(hue, 255.f, 255.f, 0.f));		
//				fieldMarker.setHighlightColor(world.p.color(fieldSelectedHue, 0.f, 0.f, 0.f));
			}
			
			fieldMarker.setHighlightColor(world.ml.color(fieldSelectedHue, fieldSelectedSaturation, fieldSelectedBrightness, 0.f));
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
	
	/**
	 * Create cluster markers for world
	 * @param world
	 */
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
				clusterMarker.setDiameter((float)Math.sqrt(c.getMediaWeight()) * 3.f);

				if(selectedField == f.getID())
					clusterMarker.setColor(world.ml.color(hue, clusterSaturation, clusterBrightness, fieldTransparency));			
				else
					clusterMarker.setColor(world.ml.color(hue, fieldSelectedSaturation, fieldSelectedBrightness, fieldTransparency));			

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
	 * Draw viewer orientation as arrow on map
	 * @param mapWidth Map width
	 * @param mapHeight Map height
	 * Draw current viewer location and orientation on map of specified size
	 */
	private void displayViewerOrientation(WMV_World world, WMV_Field f, UnfoldingMap map)
	{
		PVector vLoc = world.viewer.getGPSLocation();
		Location gpsLoc = new Location(vLoc.y, vLoc.x);
		float camYaw = -world.viewer.getXOrientation() - 0.5f * PApplet.PI;

		float shrinkFactor = 0.88f;
		float ptSize = viewerDiameter * shrinkFactor;

		world.ml.stroke(world.ml.color(0, 0, 255, 255));
		for(float i=1; i<viewerArrowPoints; i++)
		{
			ScreenPosition vScreenPos = map.getScreenPosition(gpsLoc);
			float x = i * viewerArrowPointSpacingFactor * (float)Math.cos( camYaw );
			float y = i * viewerArrowPointSpacingFactor * (float)Math.sin( camYaw );
			PVector arrowPoint = new PVector(vScreenPos.x + x, vScreenPos.y + y);

			world.ml.strokeWeight(ptSize);
			world.ml.point(arrowPoint.x, arrowPoint.y);
			
			ptSize *= shrinkFactor;
		}
	}
}

