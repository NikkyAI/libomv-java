package libomv.model.avatar;

import libomv.types.UUID;

/**
 * Contains an animation currently being played by an agent
 */
public class Animation {
	// The ID of the animation asset
	public UUID animationID;
	// A number to indicate start order of currently playing animations
	// On Linden Grids this number is unique per region, with OpenSim it is
	// per client
	public int animationSequence;
	//
	public UUID animationSourceObjectID;
}