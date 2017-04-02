package multimediaLocator;

public class WMV_PanoramaState 
{
	public WMV_ViewableState vState;

	public final float initFocusDistanceFactor = 1.1f;	// Scaling from defaultFocusDistance to panorama radius
	
	/* Metadata */
	public int imageWidth, imageHeight;		// Width and height

	/* Derived Metadata */
	public float phi = 0.f;
	
	/* Graphics */
	public float radius;
	public float origRadius;
	public int resolution = 50;  										// Sphere detail setting
	public float defaultFocusDistance = 9.0f;			// Default focus distance for images and videos (m.)

	WMV_PanoramaState()
	{
		vState = new WMV_ViewableState();
	}
	
	void setViewableState(WMV_ViewableState newState)
	{
		vState = newState;
	}
}
