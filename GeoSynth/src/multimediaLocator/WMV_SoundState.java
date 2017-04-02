package multimediaLocator;

/**
 * Media type-specific parameters of a sound in a field
 * @author davidgordon
 *
 */
public class WMV_SoundState {
	private WMV_MediaState vState;
	WMV_SoundState()
	{
		vState = new WMV_MediaState();
	}
	
	void setViewableState(WMV_MediaState newState)
	{
		vState = newState;
	}
}
