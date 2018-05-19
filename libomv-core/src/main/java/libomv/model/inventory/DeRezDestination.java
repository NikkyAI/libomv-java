package libomv.model.inventory;

/* Possible destinations for DeRezObject request */
public enum DeRezDestination {
	/* */
	AgentInventorySave,
	/* Copy from in-world to agent inventory */
	AgentInventoryCopy,
	/* Derez to TaskInventory */
	TaskInventory,
	/* */
	Attachment,
	/* Take Object */
	AgentInventoryTake,
	/* */
	ForceToGodInventory,
	/* Delete Object */
	TrashFolder,
	/* Put an avatar attachment into agent inventory */
	AttachmentToInventory,
	/* */
	AttachmentExists,
	/* Return an object back to the owner's inventory */
	ReturnToOwner,
	/* Return a deeded object back to the last owner's inventory */
	ReturnToLastOwner;

	public static DeRezDestination setValue(int value) {
		return values()[value];
	}

	public byte getValue() {
		return (byte) ordinal();
	}
}