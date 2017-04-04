package multimediaLocator;

/**
 * Current simulation state
 * @author davidgordon
 * 
 */
public class WMV_SimulationState 
{
	public WMV_FieldState fieldState;
	public WMV_ViewerSettings viewerSettings;
	public WMV_ViewerState viewerState;
	public WMV_WorldSettings worldSettings;
	public WMV_WorldState worldState;

	WMV_SimulationState(WMV_FieldState newFieldState, WMV_ViewerSettings newViewerSettings, WMV_ViewerState newViewerState,
						WMV_WorldSettings newWorldSettings, WMV_WorldState newWorldState)
	{
		fieldState = newFieldState;
		viewerSettings = newViewerSettings;
		viewerState = newViewerState;
		worldSettings = newWorldSettings;
		worldState = newWorldState;
	}
}
