package multimediaLocator;

/**
 * Media type-specific parameters of a panorama in a field
 * @author davidgordon
 *
 */
public class WMV_PanoramaState 
{
	/* Classes */
	public WMV_MediaState vState;
	private WMV_PanoramaMetadata metadata;

	/* Model */
	public final float initFocusDistanceFactor = 1.1f;	// Initial scaling from defaultFocusDistance to panorama radius
	public float phi = 0.f;             				// Elevation (Pitch angle) for stitched panoramas 

	/* Graphics */
	public float radius;
	public float origRadius;
	public int resolution = 50;  										// Sphere detail setting
	public float defaultFocusDistance = 9.0f;			// Default focus distance for images and videos (m.)

	WMV_PanoramaState()
	{
		vState = new WMV_MediaState();
	}
	
	void setMediaState(WMV_MediaState newState, WMV_PanoramaMetadata newMetadata)
	{
		vState = newState;
		metadata = newMetadata;
	}
	
	public WMV_PanoramaMetadata getMetadata()
	{
		return metadata;
	}
}
