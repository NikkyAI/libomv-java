package libomv.model.asset;

import libomv.types.UUID;

public class ImageRequest {
	public UUID imageID;
	public ImageType type;
	public float priority;
	public int discardLevel;

	public ImageRequest(UUID imageid, ImageType type, float priority, int discardLevel) {
		this.imageID = imageid;
		this.type = type;
		this.priority = priority;
		this.discardLevel = discardLevel;
	}

}