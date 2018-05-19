package libomv.model.appearance;

import libomv.VisualParams.VisualColorParam;
import libomv.VisualParams.VisualParam;
import libomv.assets.AssetWearable.WearableType;

// Data collected from visual params for each wearable needed for the
// calculation of the color
public class ColorParamInfo {
	public VisualParam visualParam;
	public VisualColorParam visualColorParam;
	public float value;
	public WearableType wearableType;
}