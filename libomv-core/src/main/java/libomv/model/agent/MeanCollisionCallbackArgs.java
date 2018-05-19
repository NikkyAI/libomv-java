package libomv.model.agent;

import java.util.Date;

import libomv.types.UUID;
import libomv.utils.CallbackArgs;

// Data sent from a simulator indicating a collision with your agent
public class MeanCollisionCallbackArgs implements CallbackArgs {
	private final MeanCollisionType m_Type;
	private final UUID m_Aggressor;
	private final UUID m_Victim;
	private final float m_Magnitude;
	private final Date m_Time;

	// Get the Type of collision
	public MeanCollisionType getType() {
		return m_Type;
	}

	// Get the ID of the agent or object that collided with your agent
	public UUID getAggressor() {
		return m_Aggressor;
	}

	// Get the ID of the agent that was attacked
	public UUID getVictim() {
		return m_Victim;
	}

	// A value indicating the strength of the collision
	public float getMagnitude() {
		return m_Magnitude;
	}

	// Get the time the collision occurred
	public Date getTime() {
		return m_Time;
	}

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
		this.m_Type = type;
		this.m_Aggressor = perp;
		this.m_Victim = victim;
		this.m_Magnitude = magnitude;
		this.m_Time = time;
	}
}