package libomv.model.parcel;

import libomv.types.UUID;

// Parcel Media Information
public final class ParcelMedia {
	// A byte, if 0x1 viewer should auto scale media to fit object
	public boolean mediaAutoScale;
	// A boolean, if true the viewer should loop the media
	public boolean mediaLoop;
	// The Asset UUID of the Texture which when applied to a primitive will
	// display the media
	public UUID mediaID;
	// A URL which points to any Quicktime supported media type
	public String mediaURL;
	// A description of the media
	public String mediaDesc;
	// An Integer which represents the height of the media
	public int mediaHeight;
	// An integer which represents the width of the media
	public int mediaWidth;
	// A string which contains the mime type of the media
	public String mediaType;
}