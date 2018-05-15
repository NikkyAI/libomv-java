package libomv.model;

import libomv.types.UUID;

public interface Avatar {
	/**
	 * Contains an animation currently being played by an agent
	 */
	public class Animation
	{
		// The ID of the animation asset
		public UUID AnimationID;
		// A number to indicate start order of currently playing animations
		// On Linden Grids this number is unique per region, with OpenSim it is
		// per client
		public int AnimationSequence;
		//
		public UUID AnimationSourceObjectID;
	}

}
