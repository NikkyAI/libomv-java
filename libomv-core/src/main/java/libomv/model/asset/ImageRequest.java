package libomv.model.asset;

import libomv.types.UUID;

public class ImageRequest {
	public UUID ImageID;
	public ImageType Type;
	public float Priority;
	public int DiscardLevel;

	public ImageRequest(UUID imageid, ImageType type, float priority, int discardLevel) {
		ImageID = imageid;
		Type = type;
		Priority = priority;
		DiscardLevel = discardLevel;
	}

}