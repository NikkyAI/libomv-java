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
package libomv.ImportExport;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.nio.concurrent.FutureCallback;
import org.apache.http.nio.reactor.IOReactorException;

import libomv.GridClient;
import libomv.ImportExport.Model.ModelFace;
import libomv.ImportExport.Model.ModelPrim;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.StructuredData.OSDParser;
import libomv.capabilities.CapsClient;
import libomv.inventory.InventoryException;
import libomv.inventory.InventoryFolder.FolderType;
import libomv.types.Permissions.PermissionMask;
import libomv.utils.Callback;
import libomv.utils.Helpers;
import libomv.utils.Logger;

/* Implements mesh upload communications with the simulator */
public class ModelUploader
{
    /* Inlcude stub convex hull physics, required for uploading to Second Life */
    public boolean IncludePhysicsStub;

    /* Use the same mesh used for geometry as the physical mesh upload */
    public boolean UseModelAsPhysics;

    private GridClient _Client;
    private List<ModelPrim> _Prims;

    String InvName, InvDescription;

    /**
     * Creates instance of the mesh uploader
     *
     * @param client GridClient instance to communicate with the simulator
     * @param prims List of ModelPrimitive objects to upload as a linkset
     * @param newInvName Inventory name for newly uploaded object
     * @param newInvDesc Inventory description for newly upload object
     */
    public ModelUploader(GridClient client, List<ModelPrim> prims, String newInvName, String newInvDesc)
    {
        this._Client = client;
        this._Prims = prims;
        this.InvName = newInvName;
        this.InvDescription = newInvDesc;
    }

    List<byte[]> Images;
    Hashtable<String, Integer> ImgIndex;

    OSD AssetResources(boolean upload)
    {
        OSDArray instanceList = new OSDArray();
        List<byte[]> meshes = new ArrayList<byte[]>();

        for (ModelPrim prim : _Prims)
        {
            OSDMap primMap = new OSDMap();

            OSDArray faceList = new OSDArray();

            for (ModelFace face : prim.Faces)
            {
                OSDMap faceMap = new OSDMap();

                faceMap.put("diffuse_color", OSD.FromColor4(face.Material.DiffuseColor));
                faceMap.put("fullbright", OSD.FromBoolean(false));

                if (face.Material.TextureData != null)
                {
                    int index;
                    if (ImgIndex.containsKey(face.Material.Texture))
                    {
                        index = ImgIndex.get(face.Material.Texture);
                    }
                    else
                    {
                        index = Images.size();
                        Images.add(face.Material.TextureData);
                        ImgIndex.put(face.Material.Texture, index);
                    }
                    faceMap.put("image", OSD.FromInteger(index));
                    faceMap.put("scales", OSD.FromReal(1.0f));
                    faceMap.put("scalet", OSD.FromReal(1.0f));
                    faceMap.put("offsets", OSD.FromReal(0.0f));
                    faceMap.put("offsett", OSD.FromReal(0.0f));
                    faceMap.put("imagerot", OSD.FromReal(0.0f));
                }

                faceList.add(faceMap);
            }

            primMap.put("face_list", faceList);

            primMap.put("position", OSD.FromVector3(prim.Position));
            primMap.put("rotation", OSD.FromQuaternion(prim.Rotation));
            primMap.put("scale", OSD.FromVector3(prim.Scale));

            primMap.put("material", OSD.FromInteger(3)); // always sent as "wood" material
            primMap.put("physics_shape_type", OSD.FromInteger(2)); // always sent as "convex hull";
            primMap.put("mesh", OSD.FromInteger(meshes.size()));
            meshes.add(prim.Asset);

            instanceList.add(primMap);
        }

        OSDMap resources = new OSDMap();
        resources.put("instance_list", instanceList);

        OSDArray meshList = new OSDArray();
        for (byte[] mesh : meshes)
        {
            meshList.add(OSD.FromBinary(mesh));
        }
        resources.put("mesh_list", meshList);

        OSDArray textureList = new OSDArray();
        for (int i = 0; i < Images.size(); i++)
        {
            if (upload)
            {
                textureList.add(OSD.FromBinary(Images.get(i)));
            }
            else
            {
                textureList.add(OSD.FromBinary(Helpers.EmptyBytes));
            }
        }

        resources.put("texture_list", textureList);

        resources.put("metric", OSD.FromString("MUT_Unspecified"));

        return resources;
    }

    /**
     * Performs model upload in one go, without first checking for the price
     */
    public void Upload() throws IOReactorException, InventoryException
    {
        Upload(null);
    }

    /**
     * Performs model upload in one go, without first checking for the price
     *
     * @param callback Callback that will be invoked upon completion of the upload. Null is sent on request failure
     */
    public void Upload(Callback<OSD> callback) throws IOReactorException, InventoryException
    {
     	class InternalUploadCallback implements Callback<OSD>
    	{
     	   	private Callback<OSD> callback;
     	   	private boolean prepare;

     	   	public InternalUploadCallback(Callback<OSD> callback, boolean prepare)
     	    {
     	    	this.callback = callback;
     	    	this.prepare = prepare;
     	    }
     	    
 			@Override
			public boolean callback(OSD result)
 			{
 				if (prepare)
 				{
 					if (result instanceof OSDMap)
 					{
 						OSDMap res = (OSDMap)result;
 						URI uploader = res.get("uploader").AsUri();
 						try
 						{
							PerformUpload(uploader, new InternalUploadCallback(callback, false));
						}
 						catch (IOReactorException ex)
 						{
	                        Logger.Log("Error performing upload", Logger.LogLevel.Error, ex);
						}
 					}
 				}
 				else
 				{
 	                if (result instanceof OSDMap)
 	                {
 	                    OSDMap reply = (OSDMap)result;
 	                    if (reply.containsKey("new_inventory_item") && reply.containsKey("new_asset"))
 	                    {
 	                        // Request full update on the item in order to update the local store
 	                        try
 	                        {
								_Client.Inventory.RequestFetchInventory(reply.get("new_inventory_item").AsUUID(), _Client.Self.getAgentID());
							}
 	                        catch (Exception ex)
 	                        {
 	                           Logger.Log("Error requesting inventory item", Logger.LogLevel.Warning, ex);
							}
 	                    }
 	                }
 	                if (callback != null)
 	                	callback(result);
 	            }
 				return true;
			}
    	}
        PrepareUpload(new InternalUploadCallback(callback, true));
    }

	private class UploadCallback implements FutureCallback<OSD>
	{
		   	private Callback<OSD> callback;
	 	   	
	 	   	public UploadCallback(Callback<OSD> callback)
	 	    {
	 	    	this.callback = callback;
	 	    }

		@Override
		public void cancelled()
		{
            if (callback != null)
            	callback.callback(null);
		}

		@Override
		public void completed(OSD result)
		{
	        if (result instanceof OSDMap)
	        {
	        	OSDMap res = (OSDMap)result;
	        	try
	        	{
					Logger.Log("Response from mesh upload:\n" + OSDParser.serializeToString(result, OSD.OSDFormat.Notation), Logger.LogLevel.Debug);
		        	if (callback != null)
		        		callback.callback(res);
				}
	        	catch (IOException ex)
	        	{
	                Logger.Log("Serializing to string failed: " + res.get("message"), Logger.LogLevel.Error, ex);
	                if (callback != null)
	                	callback.callback(null);
				}
	        }
		}

		@Override
		public void failed(Exception ex)
		{
            Logger.Log("Mesh upload request failure", Logger.LogLevel.Error, ex);
            if (callback != null)
            	callback.callback(null);
		}
	}

	/**
     * Ask server for details of cost and impact of the mesh upload
     *
     * @param callback Callback that will be invoke upon completion of the upload. Null is sent on request failure
     */
    public void PrepareUpload(Callback<OSD> callback) throws IOReactorException, InventoryException
    {
        URI url = _Client.Network.getCapabilityURI("NewFileAgentInventory");
        if (url == null)
        {
            Logger.Log("Cannot upload mesh, no connection or NewFileAgentInventory not available", Logger.LogLevel.Warning);
            if (callback != null) callback.callback(null);
            return;
        }

        Images = new ArrayList<byte[]>();
        ImgIndex = new Hashtable<String, Integer>();

        OSDMap req = new OSDMap();
        req.put("name", OSD.FromString(InvName));
        req.put("description", OSD.FromString(InvDescription));

        req.put("asset_resources", AssetResources(false));
        req.put("asset_type", OSD.FromString("mesh"));
        req.put("inventory_type", OSD.FromString("object"));

        req.put("folder_id", OSD.FromUUID(_Client.Inventory.FindFolderForType(FolderType.Object).itemID));
        req.put("texture_folder_id", OSD.FromUUID(_Client.Inventory.FindFolderForType(FolderType.Texture).itemID));

        req.put("everyone_mask", OSD.FromInteger(PermissionMask.setValue(PermissionMask.All)));
        req.put("group_mask", OSD.FromInteger(PermissionMask.setValue(PermissionMask.All)));
        req.put("next_owner_mask", OSD.FromInteger(PermissionMask.setValue(PermissionMask.All)));

        CapsClient request = new CapsClient(_Client, "ModelUploader.PrepareUpload");
        request.executeHttpPost(url, req, OSDFormat.Xml, new UploadCallback(callback), 3 * 60 * 1000);
    }

    /**
     * Performas actual mesh and image upload
     *
     * @param uploader Uri received in the upload prepare stage
     * @param callback Callback that will be invoke upon completion of the upload. Null is sent on request failure
     */
    public void PerformUpload(URI uploader, Callback<OSD> callback) throws IOReactorException
    { 	   	
        CapsClient request = new CapsClient(_Client, "ModelUploader.DoUpload");
        request.executeHttpPost(uploader, AssetResources(true), OSDFormat.Xml, new UploadCallback(callback), 60 * 1000);
    }
}
