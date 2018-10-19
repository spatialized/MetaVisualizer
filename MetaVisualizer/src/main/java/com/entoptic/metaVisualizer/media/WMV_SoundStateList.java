package main.java.com.entoptic.metaVisualizer.media;

import java.util.ArrayList;

public class WMV_SoundStateList {
	public ArrayList<WMV_SoundState> sounds;			// Clusters for exporting/importing

	public WMV_SoundStateList(){}
	
	public void setSounds(ArrayList<WMV_SoundState> newSounds)
	{
		sounds = newSounds;
	}

	public ArrayList<WMV_SoundState> getSounds()
	{
		return sounds;
	}
}
