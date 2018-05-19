package libomv.model.object;

import libomv.model.Simulator;
import libomv.packets.ObjectUpdatePacket;
import libomv.primitives.Primitive;
import libomv.types.NameValue;
import libomv.utils.CallbackArgs;

public class ObjectDataBlockUpdateCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final Primitive prim;
	private final Primitive.ConstructionData constructionData;
	private final ObjectUpdatePacket.ObjectDataBlock block;
	private final ObjectMovementUpdate update;
	private final NameValue[] nameValues;

	public ObjectDataBlockUpdateCallbackArgs(Simulator simulator, Primitive prim,
			Primitive.ConstructionData constructionData, ObjectUpdatePacket.ObjectDataBlock block,
			ObjectMovementUpdate objectupdate, NameValue[] nameValues) {
		this.simulator = simulator;
		this.prim = prim;
		this.constructionData = constructionData;
		this.block = block;
		this.update = objectupdate;
		this.nameValues = nameValues;
	}

	// Get the simulator the object is located
	public final Simulator getSimulator() {
		return simulator;
	}

	// Get the primitive details
	public final Primitive getPrim() {
		return prim;
	}

	//
	public final Primitive.ConstructionData getConstructionData() {
		return constructionData;
	}

	//
	public final ObjectUpdatePacket.ObjectDataBlock getBlock() {
		return block;
	}

	public final ObjectMovementUpdate getUpdate() {
		return update;
	}

	public final NameValue[] getNameValues() {
		return nameValues;
	}

}