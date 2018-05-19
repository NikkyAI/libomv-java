package libomv.model.object;

import libomv.model.Simulator;
import libomv.primitives.ParticleSystem;
import libomv.primitives.Primitive;
import libomv.utils.CallbackArgs;

public class ParticleUpdateCallbackArgs implements CallbackArgs {
	private final Simulator m_Simulator;
	private final ParticleSystem m_ParticleSystem;
	private final Primitive m_Source;

	// Get the simulator the object originated from
	public Simulator getSimulator() {
		return m_Simulator;
	}

	// Get the <see cref="ParticleSystem"/> data
	public ParticleSystem getParticleSystem() {
		return m_ParticleSystem;
	}

	// Get <see cref="Primitive"/> source
	public Primitive getSource() {
		return m_Source;
	}

	/**
	 * Construct a new instance of the ParticleUpdateEventArgs class
	 *
	 * @param "simulator"
	 *            The simulator the packet originated from
	 * @param "particlesystem"
	 *            The ParticleSystem data
	 * @param "source"
	 *            The Primitive source
	 */
	public ParticleUpdateCallbackArgs(Simulator simulator, ParticleSystem particlesystem, Primitive source) {
		m_Simulator = simulator;
		m_ParticleSystem = particlesystem;
		m_Source = source;
	}
}