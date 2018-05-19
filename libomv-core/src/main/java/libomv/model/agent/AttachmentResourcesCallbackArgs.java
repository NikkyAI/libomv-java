package libomv.model.agent;

import libomv.capabilities.CapsMessage.AttachmentResourcesMessage;

public class AttachmentResourcesCallbackArgs {
	private final AttachmentResourcesMessage info;
	private final boolean success;

	public AttachmentResourcesCallbackArgs(boolean success, AttachmentResourcesMessage info) {
		this.info = info;
		this.success = success;
	}

	public AttachmentResourcesMessage getInfo() {
		return info;
	}

	public boolean getSuccess() {
		return success;
	}

}