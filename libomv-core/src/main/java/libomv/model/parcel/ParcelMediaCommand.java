package libomv.model.parcel;

/** Parcel Media Command used in ParcelMediaCommandMessage */
public enum ParcelMediaCommand {
	// Stop the media stream and go back to the first frame
	Stop,
	// Pause the media stream (stop playing but stay on current frame)
	Pause,
	// Start the current media stream playing and stop when the end is
	// reached
	Play,
	// Start the current media stream playing, loop to the beginning
	// when the end is reached and continue to play
	Loop,
	// Specifies the texture to replace with video. If passing the key of a
	// texture,
	// it must be explicitly typecast as a key, not just passed within
	// double quotes.
	Texture,
	// Specifies the movie URL (254 characters max)
	URL,
	// Specifies the time index at which to begin playing
	Time,
	// Specifies a single agent to apply the media command to
	Agent,
	// Unloads the stream. While the stop command sets the texture to the
	// first frame of the
	// movie, unload resets it to the real texture that the movie was
	// replacing.
	Unload,
	// Turn on/off the auto align feature, similar to the auto align
	// checkbox in the parcel
	// media properties.
	// (NOT to be confused with the "align" function in the textures view of
	// the editor!)
	// Takes TRUE or FALSE as parameter.
	AutoAlign,
	// Allows a Web page or image to be placed on a prim (1.19.1 RC0 and
	// later only).
	// Use "text/html" for HTML.
	Type,
	// Resizes a Web page to fit on x, y pixels (1.19.1 RC0 and later only).
	// This might still not be working
	Size,
	// Sets a description for the media being displayed (1.19.1 RC0 and
	// later only).
	Desc;

	public static ParcelMediaCommand setValue(int value) {
		return values()[value];
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}