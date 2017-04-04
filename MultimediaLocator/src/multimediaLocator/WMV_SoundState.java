package multimediaLocator;

/**
 * State of a sound in a field
 * @author davidgordon
 *
 */
public class WMV_SoundState
{
	public WMV_MediaState mState;
	public WMV_SoundMetadata metadata;
	public int id;
	public float length;
	public float volume = 0.f;					// Sound volume between 0. and 1.
	public boolean fadingVolume = false;
	public int volumeFadingStartFrame = 0, volumeFadingEndFrame = 0;
	public float volumeFadingStartVal = 0.f, volumeFadingTarget = 0.f;
	public final int volumeFadingLength = 60;	// Fade volume over 30 frames

	WMV_SoundState()
	{
		mState = new WMV_MediaState();
	}
	
	void setMediaState(WMV_MediaState newState, WMV_SoundMetadata newMetadata)
	{
		mState = newState;
		metadata = newMetadata;
	}
}
