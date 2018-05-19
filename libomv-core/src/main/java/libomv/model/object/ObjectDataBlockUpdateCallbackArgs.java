package libomv.model.object;

import libomv.model.Simulator;
import libomv.packets.ObjectUpdatePacket;
import libomv.primitives.Primitive;
import libomv.types.NameValue;
import libomv.utils.CallbackArgs;

public class ObjectDataBlockUpdateCallbackArgs implements CallbackArgs {
	private final Simulator m_Simulator;
	private final Primitive m_Prim;
	private final Primitive.ConstructionData m_ConstructionData;
	private final ObjectUpdatePacket.ObjectDataBlock m_Block;
	private final ObjectMovementUpdate m_Update;
	private final NameValue[] m_NameValues;

	// Get the simulator the object is located
	public final Simulator getSimulator() {
		return m_Simulator;
	}

	// Get the primitive details
	public final Primitive getPrim() {
		return m_Prim;
	}

	//
	public final Primitive.ConstructionData getConstructionData() {
		return m_ConstructionData;
	}

	//
	public final ObjectUpdatePacket.ObjectDataBlock getBlock() {
		return m_Block;
	}

	public final ObjectMovementUpdate getUpdate() {
		return m_Update;
	}

	public final NameValue[] getNameValues() {
		return m_NameValues;
	}

	public ObjectDataBlockUpdateCallbackArgs(Simulator simulator, Primitive prim,
			Primitive.ConstructionData constructionData, ObjectUpdatePacket.ObjectDataBlock block,
			ObjectMovementUpdate objectupdate, NameValue[] nameValues) {
		this.m_Simulator = simulator;
		this.m_Prim = prim;
		this.m_ConstructionData = constructionData;
		this.m_Block = block;
		this.m_Update = objectupdate;
		this.m_NameValues = nameValues;
	}
}