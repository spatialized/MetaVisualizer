package main.java.com.entoptic.metaVisualizer.media;

import java.util.ArrayList;

public class WMV_PanoramaStateList {
	public ArrayList<WMV_PanoramaState> panoramas;			// Clusters for exporting/importing

	public WMV_PanoramaStateList(){}
	
	public void setPanoramas(ArrayList<WMV_PanoramaState> newPanoramas)
	{
		panoramas = newPanoramas;
	}

	public ArrayList<WMV_PanoramaState> getPanoramas()
	{
		return panoramas;
	}
}
