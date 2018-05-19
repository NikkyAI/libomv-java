package libomv.model.avatar;

import libomv.types.UUID;
import libomv.types.Vector3d;

public class ClassifiedAd {
	public UUID classifiedID;
	public int catagory;
	public UUID parcelID;
	public int parentEstate;
	public UUID snapShotID;
	public Vector3d position;
	public byte classifiedFlags;
	public int price;
	public String name;
	public String desc;
}