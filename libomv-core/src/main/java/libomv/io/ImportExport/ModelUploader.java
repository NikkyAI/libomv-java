/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv.io.ImportExport;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.log4j.Logger;

import libomv.ImportExport.ModelFace;
import libomv.ImportExport.ModelPrim;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.inventory.InventoryException;
import libomv.inventory.InventoryFolder.FolderType;
import libomv.io.GridClient;
import libomv.io.capabilities.CapsClient;
import libomv.types.Permissions.PermissionMask;
import libomv.utils.Callback;
import libomv.utils.Helpers;

/* Implements mesh upload communications with the simulator */
public class ModelUploader {
	private static final Logger logger = Logger.getLogger(ModelUploader.class);

	/* Inlcude stub convex hull physics, required for uploading to Second Life */
	public boolean includePhysicsStub;

	/* Use the same mesh used for geometry as the physical mesh upload */
	public boolean useModelAsPhysics;

	private GridClient client;
	private List<ModelPrim> prims;

	String invName;
	String invDescription;
	List<byte[]> images;
	Map<String, Integer> imgIndex;

	/**
	 * Creates instance of the mesh uploader
	 *
	 * @param client
	 *            GridClient instance to communicate with the simulator
	 * @param prims
	 *            List of ModelPrimitive objects to upload as a linkset
	 * @param newInvName
	 *            Inventory name for newly uploaded object
	 * @param newInvDesc
	 *            Inventory description for newly upload object
	 */
	public ModelUploader(GridClient client, List<ModelPrim> prims, String newInvName, String newInvDesc) {
		this.client = client;
		this.prims = prims;
		this.invName = newInvName;
		this.invDescription = newInvDesc;
	}

	OSD assetResources(boolean upload) {
		OSDArray instanceList = new OSDArray();
		List<byte[]> meshes = new ArrayList<>();

		for (ModelPrim prim : prims) {
			OSDMap primMap = new OSDMap();

			OSDArray faceList = new OSDArray();

			for (ModelFace face : prim.faces) {
				OSDMap faceMap = new OSDMap();

				faceMap.put("diffuse_color", OSD.fromColor4(face.material.diffuseColor));
				faceMap.put("fullbright", OSD.fromBoolean(false));

				if (face.material.textureData != null) {
					int index;
					if (imgIndex.containsKey(face.material.texture)) {
						index = imgIndex.get(face.material.texture);
					} else {
						index = images.size();
						images.add(face.material.textureData);
						imgIndex.put(face.material.texture, index);
					}
					faceMap.put("image", OSD.fromInteger(index));
					faceMap.put("scales", OSD.fromReal(1.0f));
					faceMap.put("scalet", OSD.fromReal(1.0f));
					faceMap.put("offsets", OSD.fromReal(0.0f));
					faceMap.put("offsett", OSD.fromReal(0.0f));
					faceMap.put("imagerot", OSD.fromReal(0.0f));
				}

				faceList.add(faceMap);
			}

			primMap.put("face_list", faceList);

			primMap.put("position", OSD.fromVector3(prim.position));
			primMap.put("rotation", OSD.fromQuaternion(prim.rotation));
			primMap.put("scale", OSD.fromVector3(prim.scale));

			primMap.put("material", OSD.fromInteger(3)); // always sent as "wood" material
			primMap.put("physics_shape_type", OSD.fromInteger(2)); // always sent as "convex hull";
			primMap.put("mesh", OSD.fromInteger(meshes.size()));
			meshes.add(prim.asset);

			instanceList.add(primMap);
		}

		OSDMap resources = new OSDMap();
		resources.put("instance_list", instanceList);

		OSDArray meshList = new OSDArray();
		for (byte[] mesh : meshes) {
			meshList.add(OSD.fromBinary(mesh));
		}
		resources.put("mesh_list", meshList);

		OSDArray textureList = new OSDArray();
		for (int i = 0; i < images.size(); i++) {
			if (upload) {
				textureList.add(OSD.fromBinary(images.get(i)));
			} else {
				textureList.add(OSD.fromBinary(Helpers.EmptyBytes));
			}
		}

		resources.put("texture_list", textureList);

		resources.put("metric", OSD.fromString("MUT_Unspecified"));

		return resources;
	}

	/**
	 * Performs model upload in one go, without first checking for the price
	 */
	public void upload() throws IOReactorException, InventoryException {
		upload(null);
	}

	/**
	 * Performs model upload in one go, without first checking for the price
	 *
	 * @param callback
	 *            Callback that will be invoked upon completion of the upload. Null
	 *            is sent on request failure
	 */
	public void upload(Callback<OSD> callback) throws IOReactorException, InventoryException {
		class InternalUploadCallback implements Callback<OSD> {
			private Callback<OSD> callback;
			private boolean prepare;

			public InternalUploadCallback(Callback<OSD> callback, boolean prepare) {
				this.callback = callback;
				this.prepare = prepare;
			}

			@Override
			public boolean callback(OSD result) {
				if (prepare) {
					if (result instanceof OSDMap) {
						OSDMap res = (OSDMap) result;
						URI uploader = res.get("uploader").asUri();
						try {
							performUpload(uploader, new InternalUploadCallback(callback, false));
						} catch (IOReactorException ex) {
							logger.error("Error performing upload", ex);
						}
					}
				} else {
					if (result instanceof OSDMap) {
						OSDMap reply = (OSDMap) result;
						if (reply.containsKey("new_inventory_item") && reply.containsKey("new_asset")) {
							// Request full update on the item in order to update the local store
							try {
								client.inventory.requestFetchInventory(reply.get("new_inventory_item").asUUID(),
										client.agent.getAgentID());
							} catch (Exception ex) {
								logger.warn("Error requesting inventory item", ex);
							}
						}
					}
					if (callback != null)
						callback(result);
				}
				return true;
			}
		}
		prepareUpload(new InternalUploadCallback(callback, true));
	}

	private class UploadCallback implements FutureCallback<OSD> {
		private Callback<OSD> callback;

		public UploadCallback(Callback<OSD> callback) {
			this.callback = callback;
		}

		@Override
		public void cancelled() {
			if (callback != null)
				callback.callback(null);
		}

		@Override
		public void completed(OSD result) {
			if (result instanceof OSDMap) {
				OSDMap res = (OSDMap) result;
				try {
					logger.debug("Response from mesh upload:\n"
							+ OSDParser.serializeToString(result, OSD.OSDFormat.Notation));
					if (callback != null)
						callback.callback(res);
				} catch (IOException ex) {
					logger.error("Serializing to string failed: " + res.get("message"), ex);
					if (callback != null)
						callback.callback(null);
				}
			}
		}

		@Override
		public void failed(Exception ex) {
			logger.error("Mesh upload request failure", ex);
			if (callback != null)
				callback.callback(null);
		}
	}

	/**
	 * Ask server for details of cost and impact of the mesh upload
	 *
	 * @param callback
	 *            Callback that will be invoke upon completion of the upload. Null
	 *            is sent on request failure
	 */
	public void prepareUpload(Callback<OSD> callback) throws IOReactorException, InventoryException {
		URI url = client.network.getCapabilityURI("NewFileAgentInventory");
		if (url == null) {
			logger.warn("Cannot upload mesh, no connection or NewFileAgentInventory not available");
			if (callback != null)
				callback.callback(null);
			return;
		}

		images = new ArrayList<>();
		imgIndex = new Hashtable<>();

		OSDMap req = new OSDMap();
		req.put("name", OSD.fromString(invName));
		req.put("description", OSD.fromString(invDescription));

		req.put("asset_resources", assetResources(false));
		req.put("asset_type", OSD.fromString("mesh"));
		req.put("inventory_type", OSD.fromString("object"));

		req.put("folder_id", OSD.fromUUID(client.inventory.findFolderForType(FolderType.Object).itemID));
		req.put("texture_folder_id", OSD.fromUUID(client.inventory.findFolderForType(FolderType.Texture).itemID));

		req.put("everyone_mask", OSD.fromInteger(PermissionMask.setValue(PermissionMask.All)));
		req.put("group_mask", OSD.fromInteger(PermissionMask.setValue(PermissionMask.All)));
		req.put("next_owner_mask", OSD.fromInteger(PermissionMask.setValue(PermissionMask.All)));

		CapsClient request = new CapsClient(client, "ModelUploader.PrepareUpload");
		request.executeHttpPost(url, req, OSDFormat.Xml, new UploadCallback(callback), 3 * 60 * 1000);
	}

	/**
	 * Performas actual mesh and image upload
	 *
	 * @param uploader
	 *            Uri received in the upload prepare stage
	 * @param callback
	 *            Callback that will be invoke upon completion of the upload. Null
	 *            is sent on request failure
	 */
	public void performUpload(URI uploader, Callback<OSD> callback) throws IOReactorException {
		CapsClient request = new CapsClient(client, "ModelUploader.DoUpload");
		request.executeHttpPost(uploader, assetResources(true), OSDFormat.Xml, new UploadCallback(callback), 60 * 1000);
	}
}