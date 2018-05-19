package libomv.model.agent;

import libomv.capabilities.CapsMessage.AttachmentResourcesMessage;

public class AttachmentResourcesCallbackArgs {
	AttachmentResourcesMessage info;
	boolean success;

	public AttachmentResourcesCallbackArgs(boolean success, AttachmentResourcesMessage info) {
		this.info = info;
		this.success = success;
	}
}