package libomv.model.agent;

import java.util.Date;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Data sent from a simulator indicating a collision with your agent
public class MeanCollisionCallbackArgs implements CallbackArgs {
	private final MeanCollisionType type;
	private final UUID aggressor;
	private final UUID victim;
	private final float magnitude;
	private final Date time;

	/**
	 * Construct a new instance of the MeanCollisionEventArgs class
	 *
	 * @param type
	 *            The type of collision that occurred
	 * @param perp
	 *            The ID of the agent or object that perpetrated the agression
	 * @param victim
	 *            The ID of the Victim
	 * @param magnitude
	 *            The strength of the collision
	 * @param time
	 *            The Time the collision occurred
	 */
	public MeanCollisionCallbackArgs(MeanCollisionType type, UUID perp, UUID victim, float magnitude, Date time) {
		this.type = type;
		this.aggressor = perp;
		this.victim = victim;
		this.magnitude = magnitude;
		this.time = time;
	}

	// Get the Type of collision
	public MeanCollisionType getType() {
		return type;
	}

	// Get the ID of the agent or object that collided with your agent
	public UUID getAggressor() {
		return aggressor;
	}

	// Get the ID of the agent that was attacked
	public UUID getVictim() {
		return victim;
	}

	// A value indicating the strength of the collision
	public float getMagnitude() {
		return magnitude;
	}

	// Get the time the collision occurred
	public Date getTime() {
		return time;
	}

}