package main.java.com.entoptic.multimediaLocator;

import java.util.ArrayList;

public class WMV_ImageStateList {
	public ArrayList<WMV_ImageState> images;			// Clusters for exporting/importing

	WMV_ImageStateList(){}
	
	public void setImages(ArrayList<WMV_ImageState> newImages)
	{
		images = newImages;
	}

	public ArrayList<WMV_ImageState> getImages()
	{
		return images;
	}
}
