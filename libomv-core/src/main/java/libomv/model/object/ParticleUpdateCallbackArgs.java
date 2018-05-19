package libomv.model.object;

import libomv.model.Simulator;
import libomv.primitives.ParticleSystem;
import libomv.primitives.Primitive;
import libomv.utils.CallbackArgs;

public class ParticleUpdateCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final ParticleSystem particleSystem;
	private final Primitive source;

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
		this.simulator = simulator;
		this.particleSystem = particlesystem;
		this.source = source;
	}

	// Get the simulator the object originated from
	public Simulator getSimulator() {
		return simulator;
	}

	// Get the <see cref="ParticleSystem"/> data
	public ParticleSystem getParticleSystem() {
		return particleSystem;
	}

	// Get <see cref="Primitive"/> source
	public Primitive getSource() {
		return source;
	}

}