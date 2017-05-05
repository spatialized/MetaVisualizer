package multimediaLocator;

import java.util.ArrayList;

/**
 * List of cluster states
 * @author davidgordon
 */
public class WMV_ClusterStateList 
{
	public ArrayList<WMV_ClusterState> clusters;			// Clusters for exporting/importing

	WMV_ClusterStateList(){}
	
	public void setClusters(ArrayList<WMV_ClusterState> newClusters)
	{
		clusters = newClusters;
	}

	public ArrayList<WMV_ClusterState> getClusters()
	{
		return clusters;
	}
}
