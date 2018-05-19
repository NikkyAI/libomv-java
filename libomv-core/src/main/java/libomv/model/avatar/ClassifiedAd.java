package libomv.model.avatar;

import libomv.types.UUID;
import libomv.types.Vector3d;

public class ClassifiedAd {
	public UUID ClassifiedID;
	public int Catagory;
	public UUID ParcelID;
	public int ParentEstate;
	public UUID SnapShotID;
	public Vector3d Position;
	public byte ClassifiedFlags;
	public int Price;
	public String Name;
	public String Desc;
}