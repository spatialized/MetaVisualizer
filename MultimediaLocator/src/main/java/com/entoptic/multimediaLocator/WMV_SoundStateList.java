package main.java.com.entoptic.multimediaLocator;

import java.util.ArrayList;

public class WMV_SoundStateList {
	public ArrayList<WMV_SoundState> sounds;			// Clusters for exporting/importing

	WMV_SoundStateList(){}
	
	public void setSounds(ArrayList<WMV_SoundState> newSounds)
	{
		sounds = newSounds;
	}

	public ArrayList<WMV_SoundState> getSounds()
	{
		return sounds;
	}
}
