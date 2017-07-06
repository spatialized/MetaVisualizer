package main.java.com.entoptic.multimediaLocator;

import java.util.ArrayList;

public class WMV_VideoStateList {
	public ArrayList<WMV_VideoState> videos;			// Clusters for exporting/importing

	WMV_VideoStateList(){}
	
	public void setVideos(ArrayList<WMV_VideoState> newVideos)
	{
		videos = newVideos;
	}

	public ArrayList<WMV_VideoState> getVideos()
	{
		return videos;
	}
}
