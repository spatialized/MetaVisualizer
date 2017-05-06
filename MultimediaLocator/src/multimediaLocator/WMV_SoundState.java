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
	public float radius = 10.f;					// Audible distance			-- Set globally
	
	public boolean loaded = false;
	public boolean playing = false;

	public boolean fadingVolume = false;
	public int volumeFadingStartFrame = 0, volumeFadingEndFrame = 0;
	public float volumeFadingStartVal = 0.f, volumeFadingTarget = 0.f;
	public final int volumeFadingLength = 60;	// Fade volume over 30 frames
	
	public boolean soundFadedIn = false, soundFadedOut = false;

	WMV_SoundState(){}
	
	public void initialize(WMV_SoundMetadata newMetadata)
	{
		mState = new WMV_MediaState();
		if(newMetadata != null)
			metadata = newMetadata;
	}
	
	void setMediaState(WMV_MediaState newState, WMV_SoundMetadata newMetadata)
	{
		mState = newState;
		metadata = newMetadata;
	}
	
	public WMV_MediaState getMediaState()
	{
		return mState;
	}
	
	public WMV_SoundMetadata getMetadata()
	{
		return metadata;
	}

	public void resetStatusModes()
	{
		mState.resetState();
	}
}
