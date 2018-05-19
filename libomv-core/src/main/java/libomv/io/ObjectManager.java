/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * Portions Copyright (c) 2006, Lateral Arts Limited
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
package libomv.io;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import libomv.capabilities.CapsMessage.CapsEventType;
import libomv.capabilities.CapsMessage.ObjectPhysicsPropertiesMessage;
import libomv.capabilities.IMessage;
import libomv.io.capabilities.CapsCallback;
import libomv.model.login.LoginProgressCallbackArgs;
import libomv.model.login.LoginStatus;
import libomv.model.network.DisconnectedCallbackArgs;
import libomv.model.object.AvatarSitChangedCallbackArgs;
import libomv.model.object.AvatarUpdateCallbackArgs;
import libomv.model.object.CompressedFlags;
import libomv.model.object.KillObjectsCallbackArgs;
import libomv.model.object.ObjectDataBlockUpdateCallbackArgs;
import libomv.model.object.ObjectMovementUpdate;
import libomv.model.object.ObjectPropertiesCallbackArgs;
import libomv.model.object.ObjectPropertiesFamilyCallbackArgs;
import libomv.model.object.ObjectPropertiesUpdatedCallbackArgs;
import libomv.model.object.ParticleUpdateCallbackArgs;
import libomv.model.object.PayPriceReplyCallbackArgs;
import libomv.model.object.PhysicsPropertiesCallbackArgs;
import libomv.model.object.PrimCallbackArgs;
import libomv.model.object.ReportType;
import libomv.model.object.SaleType;
import libomv.model.object.TerseObjectUpdateCallbackArgs;
import libomv.model.object.UpdateType;
import libomv.model.Simulator;
import libomv.packets.ImprovedTerseObjectUpdatePacket;
import libomv.packets.KillObjectPacket;
import libomv.packets.MultipleObjectUpdatePacket;
import libomv.packets.ObjectAddPacket;
import libomv.packets.ObjectAttachPacket;
import libomv.packets.ObjectBuyPacket;
import libomv.packets.ObjectDeGrabPacket;
import libomv.packets.ObjectDelinkPacket;
import libomv.packets.ObjectDescriptionPacket;
import libomv.packets.ObjectDeselectPacket;
import libomv.packets.ObjectDetachPacket;
import libomv.packets.ObjectDropPacket;
import libomv.packets.ObjectExtraParamsPacket;
import libomv.packets.ObjectGrabPacket;
import libomv.packets.ObjectGroupPacket;
import libomv.packets.ObjectImagePacket;
import libomv.packets.ObjectLinkPacket;
import libomv.packets.ObjectMaterialPacket;
import libomv.packets.ObjectNamePacket;
import libomv.packets.ObjectOwnerPacket;
import libomv.packets.ObjectPermissionsPacket;
import libomv.packets.ObjectPropertiesFamilyPacket;
import libomv.packets.ObjectPropertiesPacket;
import libomv.packets.ObjectRotationPacket;
import libomv.packets.ObjectSaleInfoPacket;
import libomv.packets.ObjectSelectPacket;
import libomv.packets.ObjectShapePacket;
import libomv.packets.ObjectUpdateCachedPacket;
import libomv.packets.ObjectUpdateCompressedPacket;
import libomv.packets.ObjectUpdatePacket;
import libomv.packets.Packet;
import libomv.packets.PacketType;
import libomv.packets.PayPriceReplyPacket;
import libomv.packets.RequestMultipleObjectsPacket;
import libomv.packets.RequestObjectPropertiesFamilyPacket;
import libomv.packets.RequestPayPricePacket;
import libomv.primitives.Avatar;
import libomv.primitives.ObjectProperties;
import libomv.primitives.ParticleSystem;
import libomv.primitives.Primitive;
import libomv.primitives.Primitive.ClickAction;
import libomv.primitives.Primitive.ConstructionData;
import libomv.primitives.Primitive.ExtraParamType;
import libomv.primitives.Primitive.FlexibleData;
import libomv.primitives.Primitive.Grass;
import libomv.primitives.Primitive.JointType;
import libomv.primitives.Primitive.LightData;
import libomv.primitives.Primitive.Material;
import libomv.primitives.Primitive.ObjectCategory;
import libomv.primitives.Primitive.PCode;
import libomv.primitives.Primitive.PathCurve;
import libomv.primitives.Primitive.PrimFlags;
import libomv.primitives.Primitive.ProfileCurve;
import libomv.primitives.Primitive.SculptData;
import libomv.primitives.Primitive.SoundFlags;
import libomv.primitives.Primitive.Tree;
import libomv.primitives.TextureEntry;
import libomv.types.Color4;
import libomv.types.NameValue;
import libomv.types.PacketCallback;
import libomv.types.Permissions;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.Vector4;
import libomv.utils.Callback;
import libomv.utils.CallbackHandler;
import libomv.utils.Helpers;
import libomv.utils.RefObject;
import libomv.utils.Settings.SettingsUpdateCallbackArgs;

// Handles all network traffic related to prims and avatar positions and
// movement.
public class ObjectManager implements PacketCallback, CapsCallback {
	private static final Logger logger = Logger.getLogger(ObjectManager.class);

	public static final float HAVOK_TIMESTEP = 1.0f / 45.0f;

	/**
	 * Raised when the simulator sends us data containing
	 *
	 * A <see cref="Primitive"/>, Foliage or Attachment {@link RequestObject}
	 * {@link RequestObjects}
	 */
	public CallbackHandler<ParticleUpdateCallbackArgs> OnParticleUpdate = new CallbackHandler<ParticleUpdateCallbackArgs>();

	/**
	 * Raised when the simulator sends us data containing
	 *
	 * A <see cref="Primitive"/>, Foliage or Attachment {@link RequestObject}
	 * {@link RequestObjects}
	 */
	public CallbackHandler<PrimCallbackArgs> OnObjectUpdate = new CallbackHandler<PrimCallbackArgs>();

	// Raised when the simulator sends us data containing
	// additional <seea cref="Primitive"/> information
	// {@link SelectObject}
	// {@link SelectObjects}
	public CallbackHandler<ObjectPropertiesCallbackArgs> OnObjectProperties = new CallbackHandler<ObjectPropertiesCallbackArgs>();

	// Raised when the simulator sends us data containing
	// Primitive.ObjectProperties for an object we are currently tracking
	public CallbackHandler<ObjectPropertiesUpdatedCallbackArgs> OnObjectPropertiesUpdated = new CallbackHandler<ObjectPropertiesUpdatedCallbackArgs>();

	// Raised when the simulator sends us data containing
	// additional <seea cref="Primitive"/> and <see cref="Avatar"/> details
	// {@link RequestObjectPropertiesFamily}
	public CallbackHandler<ObjectPropertiesFamilyCallbackArgs> OnObjectPropertiesFamily = new CallbackHandler<ObjectPropertiesFamilyCallbackArgs>();

	// Raised when the simulator sends us data containing updated information
	// for an <see cref="Avatar"/>
	public CallbackHandler<AvatarUpdateCallbackArgs> OnAvatarUpdate = new CallbackHandler<AvatarUpdateCallbackArgs>();

	// Raised when the simulator sends us data containing
	// <see cref="Primitive"/> and <see cref="Avatar"/> movement changes
	public CallbackHandler<TerseObjectUpdateCallbackArgs> OnTerseObjectUpdate = new CallbackHandler<TerseObjectUpdateCallbackArgs>();

	// Raised when the simulator sends us data containing updates to an Objects
	// DataBlock
	public CallbackHandler<ObjectDataBlockUpdateCallbackArgs> OnObjectDataBlockUpdate = new CallbackHandler<ObjectDataBlockUpdateCallbackArgs>();

	// Raised when the simulator informs us an <see cref="Primitive"/> or <see
	// cref="Avatar"/> is no longer within view
	public CallbackHandler<KillObjectsCallbackArgs> OnKillObject = new CallbackHandler<KillObjectsCallbackArgs>();

	// Raised when the simulator sends us data containing updated sit
	// information for our <see cref="Avatar"/>
	public CallbackHandler<AvatarSitChangedCallbackArgs> OnAvatarSitChanged = new CallbackHandler<AvatarSitChangedCallbackArgs>();

	// Raised when the simulator sends us data containing purchase price
	// information for a <see cref="Primitive"/>
	public CallbackHandler<PayPriceReplyCallbackArgs> OnPayPriceReply = new CallbackHandler<PayPriceReplyCallbackArgs>();

	// Set when simulator sends us infomation on primitive's physical properties
	public CallbackHandler<PhysicsPropertiesCallbackArgs> OnPhysicsProperties = new CallbackHandler<PhysicsPropertiesCallbackArgs>();

	

	// /#region Internal event handlers

	private class Network_OnDisconnected implements Callback<DisconnectedCallbackArgs> {
		@Override
		public boolean callback(DisconnectedCallbackArgs args) {
			if (_InterpolationTimer != null) {
				_InterpolationTimer.cancel();
				_InterpolationTimer = null;
			}
			return true;
		}
	}

	private class Network_OnLoginProgress implements Callback<LoginProgressCallbackArgs> {
		@Override
		public boolean callback(LoginProgressCallbackArgs args) {
			if (args.getStatus() == LoginStatus.Success
					&& _Client.Settings.getBool(LibSettings.USE_INTERPOLATION_TIMER)) {
				_InterpolationTimer = new Timer("InterpolationTimer");
				_InterpolationTimer.schedule(new InterpolationTimer_Elapsed(), LibSettings.INTERPOLATION_INTERVAL);
			}
			return false;
		}
	}

	private class InterpolationTimer_Elapsed extends TimerTask {
		@Override
		public void run() {
			long elapsed = 0;

			if (_Client.Network.getConnected()) {
				long start = System.currentTimeMillis();

				long interval = start - lastInterpolation;
				float seconds = interval / 1000f;

				ArrayList<SimulatorManager> simulators = _Client.Network.getSimulators();
				synchronized (simulators) {
					// Iterate through all of the simulators
					for (SimulatorManager sim : simulators) {
						float adjSeconds = seconds * sim.Statistics.dilation;

						// Iterate through all of this sims avatars
						synchronized (sim.getObjectsAvatars()) {
							for (Avatar avatar : sim.getObjectsAvatars().values()) {
								// #region Linear Motion
								// Only do movement interpolation (extrapolation) when there is a non-zero
								// velocity and/or acceleration
								if (!Vector3.isZeroOrNull(avatar.acceleration)) {
									// avatar.Position += (avatar.Velocity + (avatar.Acceleration * (0.5f *
									// (adjSeconds - HAVOK_TIMESTEP)))) * adjSeconds;
									// avatar.Velocity += avatar.Acceleration * adjSeconds;
									avatar.position
											.add(Vector3.multiply(
													Vector3.add(avatar.velocity,
															Vector3.multiply(avatar.acceleration,
																	(0.5f * (adjSeconds - HAVOK_TIMESTEP)))),
													adjSeconds));
									avatar.velocity.add(Vector3.multiply(avatar.acceleration, adjSeconds));
								} else if (!Vector3.isZeroOrNull(avatar.velocity)) {
									// avatar.Position += avatar.Velocity * adjSeconds;
									avatar.position.add(Vector3.multiply(avatar.velocity, adjSeconds));
								}
								// #endregion Linear Motion
							}
						}

						// Iterate through all of this sims primitives
						synchronized (sim.getObjectsPrimitives()) {
							for (Primitive prim : sim.getObjectsPrimitives().values()) {
								if (prim.joint != null) {
									if (prim.joint == JointType.Invalid) {
										// #region Angular Velocity
										if (prim.angularVelocity != null) {
											Vector3 angVel = prim.angularVelocity;
											float omega = angVel.lengthSquared();
											if (omega > 0.00001f) {
												omega = (float) Math.sqrt(omega);
												float angle = omega * adjSeconds;
												angVel.multiply(1.0f / omega);
												Quaternion dQ = Quaternion.createFromAxisAngle(angVel, angle);
												prim.rotation.multiply(dQ);
											}
										}
										// #endregion Angular Velocity

										// #region Linear Motion
										// Only do movement interpolation (extrapolation) when there is a non-zero
										// velocity and/or acceleration
										if (!Vector3.isZeroOrNull(prim.acceleration)) {
											// prim.Position += (prim.Velocity + (prim.Acceleration * (0.5f *
											// (adjSeconds - HAVOK_TIMESTEP)))) * adjSeconds;
											// prim.Velocity += prim.Acceleration * adjSeconds;
											prim.position
													.add(Vector3
															.multiply(
																	Vector3.add(prim.velocity,
																			Vector3.multiply(prim.acceleration, (0.5f
																					* (adjSeconds - HAVOK_TIMESTEP)))),
																	adjSeconds));
											prim.velocity.add(Vector3.multiply(prim.acceleration, adjSeconds));
										} else if (!Vector3.isZeroOrNull(prim.velocity)) {
											// prim.Position += prim.Velocity * adjSeconds;
											prim.position.add(Vector3.multiply(prim.velocity, adjSeconds));
										}
										// #endregion Linear Motion
									} else if (prim.joint == JointType.Hinge) {
										// FIXME: Hinge movement extrapolation
									} else if (prim.joint == JointType.Point) {
										// FIXME: Point movement extrapolation
									} else {
										logger.warn(GridClient.Log("Unhandled joint type " + prim.joint, _Client));
										break;
									}
								}
							}
						}
					}

					// Make sure the last interpolated time is always updated
					lastInterpolation = System.currentTimeMillis();

					elapsed = lastInterpolation - start;
				}
			}

			// Start the timer again. Use a minimum of a 50ms pause in between
			// calculations
			int delay = Math.max(50, (int) (LibSettings.INTERPOLATION_INTERVAL - elapsed));
			if (_InterpolationTimer != null) {
				_InterpolationTimer.schedule(new InterpolationTimer_Elapsed(), delay);
			}
		}
	}

	private GridClient _Client;
	// Does periodic dead reckoning calculation to convert
	// velocity and acceleration to new positions for objects
	private Timer _InterpolationTimer;
	private long lastInterpolation;

	private boolean objectTracking;
	private boolean alwaysDecodeObjects;
	private boolean alwaysRequestObjects;

	private class SettingsUpdate implements Callback<SettingsUpdateCallbackArgs> {
		@Override
		public boolean callback(SettingsUpdateCallbackArgs params) {
			String key = params.getName();
			if (key == null) {
				objectTracking = _Client.Settings.getBool(LibSettings.OBJECT_TRACKING);
				alwaysDecodeObjects = _Client.Settings.getBool(LibSettings.ALWAYS_DECODE_OBJECTS);
				alwaysRequestObjects = _Client.Settings.getBool(LibSettings.ALWAYS_REQUEST_OBJECTS);
			} else if (key.equals(LibSettings.OBJECT_TRACKING)) {
				objectTracking = params.getValue().AsBoolean();
			} else if (key.equals(LibSettings.ALWAYS_DECODE_OBJECTS)) {
				alwaysDecodeObjects = params.getValue().AsBoolean();
			} else if (key.equals(LibSettings.ALWAYS_REQUEST_OBJECTS)) {
				alwaysRequestObjects = params.getValue().AsBoolean();
			}
			return false;
		}
	}

	public ObjectManager(GridClient client) {
		_Client = client;

		_Client.Settings.onSettingsUpdate.add(new SettingsUpdate());
		objectTracking = _Client.Settings.getBool(LibSettings.OBJECT_TRACKING);
		alwaysDecodeObjects = _Client.Settings.getBool(LibSettings.ALWAYS_DECODE_OBJECTS);
		alwaysRequestObjects = _Client.Settings.getBool(LibSettings.ALWAYS_REQUEST_OBJECTS);

		_Client.Login.OnLoginProgress.add(new Network_OnLoginProgress());
		_Client.Network.OnDisconnected.add(new Network_OnDisconnected(), true);

		_Client.Network.RegisterCallback(PacketType.ObjectUpdate, this);
		_Client.Network.RegisterCallback(PacketType.ImprovedTerseObjectUpdate, this);
		_Client.Network.RegisterCallback(PacketType.ObjectUpdateCompressed, this);
		_Client.Network.RegisterCallback(PacketType.ObjectUpdateCached, this);
		_Client.Network.RegisterCallback(PacketType.KillObject, this);
		_Client.Network.RegisterCallback(PacketType.ObjectPropertiesFamily, this);
		_Client.Network.RegisterCallback(PacketType.ObjectProperties, this);
		_Client.Network.RegisterCallback(PacketType.PayPriceReply, this);

		_Client.Network.RegisterCallback(CapsEventType.ObjectPhysicsProperties, this);
	}

	@Override
	public void packetCallback(Packet packet, Simulator simulator) throws Exception {
		switch (packet.getType()) {
		case ObjectUpdate:
			HandleObjectUpdate(packet, simulator);
			break;
		case ImprovedTerseObjectUpdate:
			HandleTerseObjectUpdate(packet, simulator);
			break;
		case ObjectUpdateCompressed:
			HandleObjectUpdateCompressed(packet, simulator);
			break;
		case ObjectUpdateCached:
			HandleObjectUpdateCached(packet, simulator);
			break;
		case KillObject:
			HandleKillObject(packet, simulator);
			break;
		case ObjectPropertiesFamily:
			HandleObjectPropertiesFamily(packet, simulator);
			break;
		case ObjectProperties:
			HandleObjectProperties(packet, simulator);
			break;
		case PayPriceReply:
			HandlePayPriceReply(packet, simulator);
			break;
		default:
			break;
		}
	}

	@Override
	public void capsCallback(IMessage message, SimulatorManager simulator) throws Exception {
		switch (message.getType()) {
		case ObjectPhysicsProperties:
			HandleObjectPhysicsProperties(message, simulator);
			break;
		default:
			break;
		}
	}

	/**
	 * Request information for a single object from a <see cref="Simulator"/> you
	 * are currently connected to
	 *
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param localID
	 *            The Local ID of the object
	 * @throws Exception
	 */
	public void RequestObject(Simulator simulator, int localID) throws Exception {
		RequestMultipleObjectsPacket request = new RequestMultipleObjectsPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.ObjectData = new RequestMultipleObjectsPacket.ObjectDataBlock[1];
		request.ObjectData[0].ID = localID;
		request.ObjectData[0].CacheMissType = 0;

		simulator.sendPacket(request);
	}

	/**
	 * Request information for multiple objects contained in the same simulator
	 *
	 * @param simulator
	 *            The <see cref="Simulator"/> the objects are located
	 * @param localIDs
	 *            An array containing the Local IDs of the objects
	 * @throws Exception
	 */
	public final void RequestObjects(Simulator simulator, int[] localIDs) throws Exception {
		RequestMultipleObjectsPacket request = new RequestMultipleObjectsPacket();
		request.AgentData.AgentID = _Client.Self.getAgentID();
		request.AgentData.SessionID = _Client.Self.getSessionID();
		request.ObjectData = new RequestMultipleObjectsPacket.ObjectDataBlock[localIDs.length];

		for (int i = 0; i < localIDs.length; i++) {
			request.ObjectData[i] = request.new ObjectDataBlock();
			request.ObjectData[i].ID = localIDs[i];
			request.ObjectData[i].CacheMissType = 0;
		}
		simulator.sendPacket(request);
	}

	/**
	 * Attempt to purchase an original object, a copy, or the contents of an object
	 *
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param localID
	 *            The Local ID of the object
	 * @param saleType
	 *            Whether the original, a copy, or the object contents are on sale.
	 *            This is used for verification, if the this sale type is not valid
	 *            for the object the purchase will fail
	 * @param price
	 *            Price of the object. This is used for verification, if it does not
	 *            match the actual price the purchase will fail
	 * @param groupID
	 *            Group ID that will be associated with the new purchase
	 * @param categoryID
	 *            Inventory folder UUID where the object or objects purchased should
	 *            be placed <example> <code>
	 *     BuyObject(_Client.Network.CurrentSim, 500, SaleType.Copy,
	 *         100, UUID.Zero, _Client.Self.InventoryRootFolderUUID);
	 *  </code> </example>
	 * @throws Exception
	 */
	public final void BuyObject(Simulator simulator, int localID, SaleType saleType, int price, UUID groupID,
			UUID categoryID) throws Exception {
		ObjectBuyPacket buy = new ObjectBuyPacket();

		buy.AgentData.AgentID = _Client.Self.getAgentID();
		buy.AgentData.SessionID = _Client.Self.getSessionID();
		buy.AgentData.GroupID = groupID;
		buy.AgentData.CategoryID = categoryID;

		buy.ObjectData = new ObjectBuyPacket.ObjectDataBlock[1];
		buy.ObjectData[0].ObjectLocalID = localID;
		buy.ObjectData[0].SaleType = (byte) saleType.ordinal();
		buy.ObjectData[0].SalePrice = price;

		simulator.sendPacket(buy);
	}

	/**
	 * Request prices that should be displayed in pay dialog. This will trigger the
	 * simulator to send us back a PayPriceReply which can be handled by
	 * OnPayPriceReply event
	 *
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param objectID
	 *            The ID of the object
	 *
	 *            The result is raised in the <see cref="OnPayPriceReply"/> event
	 */
	public final void RequestPayPrice(Simulator simulator, UUID objectID) throws Exception {
		RequestPayPricePacket payPriceRequest = new RequestPayPricePacket();
		payPriceRequest.ObjectID = objectID;
		simulator.sendPacket(payPriceRequest);
	}

	/**
	 * Select a single object. This will cause the <see cref="Simulator"/> to send
	 * us an <see cref="ObjectPropertiesPacket"/> which will raise the
	 * <see cref="OnObjectProperties"/> event
	 *
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param localID
	 *            The Local ID of the object
	 *            {@link ObjectPropertiesFamilyCallbackArgs}
	 * @throws Exception
	 */
	public final void SelectObject(Simulator simulator, int localID) throws Exception {
		SelectObject(simulator, localID, true);
	}

	/**
	 * Select a single object. This will cause the <see cref="Simulator"/> to send
	 * us an <see cref="ObjectPropertiesPacket"/> which will raise the
	 * <see cref="OnObjectProperties"/> event
	 *
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param localID
	 *            The Local ID of the object
	 * @param automaticDeselect
	 *            if true, a call to <see cref="DeselectObject"/> is made
	 *            immediately following the request
	 *            {@link ObjectPropertiesFamilyCallbackArgs}
	 * @throws Exception
	 */
	public final void SelectObject(Simulator simulator, int localID, boolean automaticDeselect) throws Exception {
		ObjectSelectPacket select = new ObjectSelectPacket();

		select.AgentData.AgentID = _Client.Self.getAgentID();
		select.AgentData.SessionID = _Client.Self.getSessionID();

		select.ObjectLocalID = new int[1];
		select.ObjectLocalID[0] = localID;

		simulator.sendPacket(select);

		if (automaticDeselect) {
			DeselectObject(simulator, localID);
		}
	}

	/**
	 * Select multiple objects. This will cause the <see cref="Simulator"/> to send
	 * us an <see cref="ObjectPropertiesPacket"/> which will raise the
	 * <see cref="OnObjectProperties"/> event
	 *
	 * @param simulator
	 *            The <see cref="Simulator"/> the objects are located
	 * @param localIDs
	 *            An array containing the Local IDs of the objects
	 *            {@link ObjectPropertiesFamilyCallbackArgs}
	 * @throws Exception
	 */
	public final void SelectObjects(Simulator simulator, int[] localIDs) throws Exception {
		SelectObjects(simulator, localIDs, true);
	}

	/**
	 * Select multiple objects. This will cause the <see cref="Simulator"/> to send
	 * us an <see cref="ObjectPropertiesPacket"/> which will raise the
	 * <see cref="OnObjectProperties"/> event
	 *
	 * @param simulator
	 *            The <see cref="Simulator"/> the objects are located
	 * @param localIDs
	 *            An array containing the Local IDs of the objects
	 * @param automaticDeselect
	 *            Should objects be deselected immediately after selection
	 *            {@link ObjectPropertiesFamilyCallbackArgs}
	 * @throws Exception
	 */
	public final void SelectObjects(Simulator simulator, int[] localIDs, boolean automaticDeselect) throws Exception {
		ObjectSelectPacket select = new ObjectSelectPacket();

		select.AgentData.AgentID = _Client.Self.getAgentID();
		select.AgentData.SessionID = _Client.Self.getSessionID();

		select.ObjectLocalID = new int[localIDs.length];

		for (int i = 0; i < localIDs.length; i++) {
			select.ObjectLocalID[i] = localIDs[i];
		}

		simulator.sendPacket(select);

		if (automaticDeselect) {
			DeselectObjects(simulator, localIDs);
		}
	}

	/**
	 * Sets the sale properties of a single object
	 *
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param localID
	 *            The Local ID of the object
	 * @param saleType
	 *            One of the options from the <see cref="SaleType"/> enum
	 * @param price
	 *            The price of the object
	 * @throws Exception
	 */
	public final void SetSaleInfo(Simulator simulator, int localID, SaleType saleType, int price) throws Exception {
		ObjectSaleInfoPacket sale = new ObjectSaleInfoPacket();
		sale.AgentData.AgentID = _Client.Self.getAgentID();
		sale.AgentData.SessionID = _Client.Self.getSessionID();
		sale.ObjectData = new ObjectSaleInfoPacket.ObjectDataBlock[1];
		sale.ObjectData[0] = sale.new ObjectDataBlock();
		sale.ObjectData[0].LocalID = localID;
		sale.ObjectData[0].SalePrice = price;
		sale.ObjectData[0].SaleType = (byte) saleType.ordinal();

		simulator.sendPacket(sale);
	}

	/**
	 * Sets the sale properties of multiple objects
	 *
	 * @param simulator
	 *            The <see cref="Simulator"/> the objects are located
	 * @param localIDs
	 *            An array containing the Local IDs of the objects
	 * @param saleType
	 *            One of the options from the <see cref="SaleType"/> enum
	 * @param price
	 *            The price of the object
	 * @throws Exception
	 */
	public final void SetSaleInfo(Simulator simulator, int[] localIDs, SaleType saleType, int price) throws Exception {
		ObjectSaleInfoPacket sale = new ObjectSaleInfoPacket();
		sale.AgentData.AgentID = _Client.Self.getAgentID();
		sale.AgentData.SessionID = _Client.Self.getSessionID();
		sale.ObjectData = new ObjectSaleInfoPacket.ObjectDataBlock[localIDs.length];

		for (int i = 0; i < localIDs.length; i++) {
			sale.ObjectData[i] = sale.new ObjectDataBlock();
			sale.ObjectData[i].LocalID = localIDs[i];
			sale.ObjectData[i].SalePrice = price;
			sale.ObjectData[i].SaleType = (byte) saleType.ordinal();
		}

		simulator.sendPacket(sale);
	}

	/**
	 * Deselect a single object
	 *
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param localID
	 *            The Local ID of the object
	 * @throws Exception
	 */
	public final void DeselectObject(Simulator simulator, int localID) throws Exception {
		ObjectDeselectPacket deselect = new ObjectDeselectPacket();

		deselect.AgentData.AgentID = _Client.Self.getAgentID();
		deselect.AgentData.SessionID = _Client.Self.getSessionID();

		deselect.ObjectLocalID = new int[1];
		deselect.ObjectLocalID[0] = localID;

		simulator.sendPacket(deselect);
	}

	/**
	 * Deselect multiple objects.
	 *
	 * @param simulator
	 *            The <see cref="Simulator"/> the objects are located
	 * @param localIDs
	 *            An array containing the Local IDs of the objects
	 * @throws Exception
	 */
	public final void DeselectObjects(Simulator simulator, int[] localIDs) throws Exception {
		ObjectDeselectPacket deselect = new ObjectDeselectPacket();

		deselect.AgentData.AgentID = _Client.Self.getAgentID();
		deselect.AgentData.SessionID = _Client.Self.getSessionID();

		deselect.ObjectLocalID = new int[localIDs.length];

		for (int i = 0; i < localIDs.length; i++) {
			deselect.ObjectLocalID[i] = localIDs[i];
		}

		simulator.sendPacket(deselect);
	}

	/**
	 * Perform a click action on an object
	 *
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param localID
	 *            The Local ID of the object
	 * @throws Exception
	 */
	public final void ClickObject(Simulator simulator, int localID) throws Exception {
		ClickObject(simulator, localID, Vector3.Zero, Vector3.Zero, 0, Vector3.Zero, Vector3.Zero, Vector3.Zero);
	}

	/**
	 * Perform a click action (Grab) on a single object
	 *
	 * @param simulator
	 *            The <see cref="Simulator"/> the object is located
	 * @param localID
	 *            The Local ID of the object
	 * @param uvCoord
	 *            The texture coordinates to touch
	 * @param stCoord
	 *            The surface coordinates to touch
	 * @param faceIndex
	 *            The face of the position to touch
	 * @param position
	 *            The region coordinates of the position to touch
	 * @param normal
	 *            The surface normal of the position to touch (A normal is a vector
	 *            perpindicular to the surface)
	 * @param binormal
	 *            The surface binormal of the position to touch (A binormal is a
	 *            vector tangen to the surface pointing along the U direction of the
	 *            tangent space
	 * @throws Exception
	 */
	public final void ClickObject(Simulator simulator, int localID, Vector3 uvCoord, Vector3 stCoord, int faceIndex,
			Vector3 position, Vector3 normal, Vector3 binormal) throws Exception {
		ObjectGrabPacket grab = new ObjectGrabPacket();
		grab.AgentData.AgentID = _Client.Self.getAgentID();
		grab.AgentData.SessionID = _Client.Self.getSessionID();
		grab.ObjectData.GrabOffset = Vector3.Zero;
		grab.ObjectData.LocalID = localID;
		grab.SurfaceInfo = new ObjectGrabPacket.SurfaceInfoBlock[1];
		grab.SurfaceInfo[0] = grab.new SurfaceInfoBlock();
		grab.SurfaceInfo[0].UVCoord = uvCoord;
		grab.SurfaceInfo[0].STCoord = stCoord;
		grab.SurfaceInfo[0].FaceIndex = faceIndex;
		grab.SurfaceInfo[0].Position = position;
		grab.SurfaceInfo[0].Normal = normal;
		grab.SurfaceInfo[0].Binormal = binormal;

		simulator.sendPacket(grab);

		// TODO: If these hit the server out of order the click will fail
		// and we'll be grabbing the object
		Thread.sleep(50);

		ObjectDeGrabPacket degrab = new ObjectDeGrabPacket();
		degrab.AgentData.AgentID = _Client.Self.getAgentID();
		degrab.AgentData.SessionID = _Client.Self.getSessionID();
		degrab.LocalID = localID;
		degrab.SurfaceInfo = new ObjectDeGrabPacket.SurfaceInfoBlock[1];
		degrab.SurfaceInfo[0] = degrab.new SurfaceInfoBlock();
		degrab.SurfaceInfo[0].UVCoord = uvCoord;
		degrab.SurfaceInfo[0].STCoord = stCoord;
		degrab.SurfaceInfo[0].FaceIndex = faceIndex;
		degrab.SurfaceInfo[0].Position = position;
		degrab.SurfaceInfo[0].Normal = normal;
		degrab.SurfaceInfo[0].Binormal = binormal;

		simulator.sendPacket(degrab);
	}

	/**
	 * Create (rez) a new prim object in a simulator
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object to place the
	 *            object in
	 * @param prim
	 *            Data describing the prim object to rez
	 * @param groupID
	 *            Group ID that this prim will be set to, or UUID.Zero if you do not
	 *            want the object to be associated with a specific group
	 * @param position
	 *            An approximation of the position at which to rez the prim
	 * @param scale
	 *            Scale vector to size this prim
	 * @param rotation
	 *            Rotation quaternion to rotate this prim Due to the way client prim
	 *            rezzing is done on the server, the requested position for an
	 *            object is only close to where the prim actually ends up. If you
	 *            desire exact placement you'll need to follow up by moving the
	 *            object after it has been created. This function will not set
	 *            textures, light and flexible data, or other extended primitive
	 *            properties
	 * @throws Exception
	 */
	public final void AddPrim(Simulator simulator, Primitive.ConstructionData prim, UUID groupID, Vector3 position,
			Vector3 scale, Quaternion rotation) throws Exception {
		AddPrim(simulator, prim, groupID, position, scale, rotation, PrimFlags.CreateSelected);
	}

	/**
	 * Create (rez) a new prim object in a simulator
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator} object to place the object in
	 * @param prim
	 *            Data describing the prim object to rez
	 * @param groupID
	 *            Group ID that this prim will be set to, or UUID.Zero if you do not
	 *            want the object to be associated with a specific group
	 * @param position
	 *            An approximation of the position at which to rez the prim
	 * @param scale
	 *            Scale vector to size this prim
	 * @param rotation
	 *            Rotation quaternion to rotate this prim
	 * @param createFlags
	 *            Specify the {@link PrimFlags} Due to the way client prim rezzing
	 *            is done on the server, the requested position for an object is
	 *            only close to where the prim actually ends up. If you desire exact
	 *            placement you'll need to follow up by moving the object after it
	 *            has been created. This function will not set textures, light and
	 *            flexible data, or other extended primitive properties
	 * @throws Exception
	 */
	public final void AddPrim(Simulator simulator, ConstructionData prim, UUID groupID, Vector3 position, Vector3 scale,
			Quaternion rotation, int createFlags) throws Exception {
		ObjectAddPacket packet = new ObjectAddPacket();

		packet.AgentData.AgentID = _Client.Self.getAgentID();
		packet.AgentData.SessionID = _Client.Self.getSessionID();
		packet.AgentData.GroupID = groupID;

		packet.ObjectData.State = prim.state;
		packet.ObjectData.AddFlags = createFlags;
		packet.ObjectData.PCode = PCode.Prim.getValue();

		packet.ObjectData.Material = (byte) prim.material.ordinal();
		packet.ObjectData.Scale = scale;
		packet.ObjectData.Rotation = rotation;

		packet.ObjectData.PathCurve = prim.pathCurve.getValue();
		packet.ObjectData.PathBegin = Primitive.packBeginCut(prim.pathBegin);
		packet.ObjectData.PathEnd = Primitive.packEndCut(prim.pathEnd);
		packet.ObjectData.PathRadiusOffset = Primitive.packPathTwist(prim.pathRadiusOffset);
		packet.ObjectData.PathRevolutions = Primitive.packPathRevolutions(prim.pathRevolutions);
		packet.ObjectData.PathScaleX = Primitive.packPathScale(prim.pathScaleX);
		packet.ObjectData.PathScaleY = Primitive.packPathScale(prim.pathScaleY);
		packet.ObjectData.PathShearX = Primitive.packPathShear(prim.pathShearX);
		packet.ObjectData.PathShearY = Primitive.packPathShear(prim.pathShearY);
		packet.ObjectData.PathSkew = Primitive.packPathTwist(prim.pathSkew);
		packet.ObjectData.PathTaperX = Primitive.packPathTaper(prim.pathTaperX);
		packet.ObjectData.PathTaperY = Primitive.packPathTaper(prim.pathTaperY);
		packet.ObjectData.PathTwist = Primitive.packPathTwist(prim.pathTwist);
		packet.ObjectData.PathTwistBegin = Primitive.packPathTwist(prim.pathTwistBegin);

		packet.ObjectData.ProfileCurve = prim.getProfileValue();
		packet.ObjectData.ProfileBegin = Primitive.packBeginCut(prim.profileBegin);
		packet.ObjectData.ProfileEnd = Primitive.packEndCut(prim.profileEnd);
		packet.ObjectData.ProfileHollow = Primitive.packProfileHollow(prim.profileHollow);

		packet.ObjectData.RayStart = position;
		packet.ObjectData.RayEnd = position;
		packet.ObjectData.RayEndIsIntersection = 0;
		packet.ObjectData.RayTargetID = UUID.Zero;
		packet.ObjectData.BypassRaycast = 1;

		simulator.sendPacket(packet);
	}

	/**
	 * Rez a Linden tree
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param scale
	 *            The size of the tree
	 * @param rotation
	 *            The rotation of the tree
	 * @param position
	 *            The position of the tree
	 * @param treeType
	 *            The Type of tree
	 * @param groupOwner
	 *            The {@link UUID} of the group to set the tree to, or UUID.Zero if
	 *            no group is to be set
	 * @param newTree
	 *            true to use the "new" Linden trees, false to use the old
	 * @throws Exception
	 */
	public final void AddTree(Simulator simulator, Vector3 scale, Quaternion rotation, Vector3 position, Tree treeType,
			UUID groupOwner, boolean newTree) throws Exception {
		ObjectAddPacket add = new ObjectAddPacket();

		add.AgentData.AgentID = _Client.Self.getAgentID();
		add.AgentData.SessionID = _Client.Self.getSessionID();
		add.AgentData.GroupID = groupOwner;
		add.ObjectData.BypassRaycast = 1;
		add.ObjectData.Material = 3;
		add.ObjectData.PathCurve = 16;
		add.ObjectData.PCode = newTree ? (byte) PCode.NewTree.getValue() : (byte) PCode.Tree.getValue();
		add.ObjectData.RayEnd = position;
		add.ObjectData.RayStart = position;
		add.ObjectData.RayTargetID = UUID.Zero;
		add.ObjectData.Rotation = rotation;
		add.ObjectData.Scale = scale;
		add.ObjectData.State = (byte) treeType.ordinal();

		simulator.sendPacket(add);
	}

	/**
	 * Rez grass and ground cover
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param scale
	 *            The size of the grass
	 * @param rotation
	 *            The rotation of the grass
	 * @param position
	 *            The position of the grass
	 * @param grassType
	 *            The type of grass from the {@link Grass} enum
	 * @param groupOwner
	 *            The {@link UUID} of the group to set the tree to, or UUID.Zero if
	 *            no group is to be set
	 * @throws Exception
	 */
	public final void AddGrass(Simulator simulator, Vector3 scale, Quaternion rotation, Vector3 position,
			Grass grassType, UUID groupOwner) throws Exception {
		ObjectAddPacket add = new ObjectAddPacket();

		add.AgentData.AgentID = _Client.Self.getAgentID();
		add.AgentData.SessionID = _Client.Self.getSessionID();
		add.AgentData.GroupID = groupOwner;
		add.ObjectData.BypassRaycast = 1;
		add.ObjectData.Material = 3;
		add.ObjectData.PathCurve = 16;
		add.ObjectData.PCode = PCode.Grass.getValue();
		add.ObjectData.RayEnd = position;
		add.ObjectData.RayStart = position;
		add.ObjectData.RayTargetID = UUID.Zero;
		add.ObjectData.Rotation = rotation;
		add.ObjectData.Scale = scale;
		add.ObjectData.State = (byte) grassType.ordinal();

		simulator.sendPacket(add);
	}

	/**
	 * Set the textures to apply to the faces of an object
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param textures
	 *            The texture data to apply
	 * @throws Exception
	 * @throws IOException
	 */
	public final void SetTextures(Simulator simulator, int localID, TextureEntry textures)
			throws IOException, Exception {
		SetTextures(simulator, localID, textures, Helpers.EmptyString);
	}

	/**
	 * Set the textures to apply to the faces of an object
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param textures
	 *            The texture data to apply
	 * @param mediaUrl
	 *            A media URL (not used)
	 * @throws Exception
	 */
	public final void SetTextures(Simulator simulator, int localID, TextureEntry textures, String mediaUrl)
			throws Exception {
		ObjectImagePacket image = new ObjectImagePacket();

		image.AgentData.AgentID = _Client.Self.getAgentID();
		image.AgentData.SessionID = _Client.Self.getSessionID();
		image.ObjectData = new ObjectImagePacket.ObjectDataBlock[1];
		image.ObjectData[0] = image.new ObjectDataBlock();
		image.ObjectData[0].ObjectLocalID = localID;
		image.ObjectData[0].setTextureEntry(textures.getBytes());
		image.ObjectData[0].setMediaURL(Helpers.StringToBytes(mediaUrl));

		simulator.sendPacket(image);
	}

	/**
	 * Set the Light data on an object
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param light
	 *            A {@link Primitive.LightData} object containing the data to set
	 * @throws Exception
	 */
	public final void SetLight(Simulator simulator, int localID, LightData light) throws Exception {
		ObjectExtraParamsPacket extra = new ObjectExtraParamsPacket();

		extra.AgentData.AgentID = _Client.Self.getAgentID();
		extra.AgentData.SessionID = _Client.Self.getSessionID();
		extra.ObjectData = new ObjectExtraParamsPacket.ObjectDataBlock[1];
		extra.ObjectData[0] = extra.new ObjectDataBlock();
		extra.ObjectData[0].ObjectLocalID = localID;
		extra.ObjectData[0].ParamType = ExtraParamType.Light.getValue();
		if (light.intensity == 0.0f) {
			// Disables the light if intensity is 0
			extra.ObjectData[0].ParamInUse = false;
		} else {
			extra.ObjectData[0].ParamInUse = true;
		}
		extra.ObjectData[0].setParamData(light.getBytes());
		extra.ObjectData[0].ParamSize = light.getBytes().length;

		simulator.sendPacket(extra);
	}

	/**
	 * Set the flexible data on an object
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param flexible
	 *            A {@link Primitive.FlexibleData} object containing the data to set
	 * @throws Exception
	 */
	public final void SetFlexible(Simulator simulator, int localID, FlexibleData flexible) throws Exception {
		ObjectExtraParamsPacket extra = new ObjectExtraParamsPacket();

		extra.AgentData.AgentID = _Client.Self.getAgentID();
		extra.AgentData.SessionID = _Client.Self.getSessionID();
		extra.ObjectData = new ObjectExtraParamsPacket.ObjectDataBlock[1];
		extra.ObjectData[0] = extra.new ObjectDataBlock();
		extra.ObjectData[0].ObjectLocalID = localID;
		extra.ObjectData[0].ParamType = ExtraParamType.Flexible.getValue();
		extra.ObjectData[0].ParamInUse = true;
		extra.ObjectData[0].setParamData(flexible.getBytes());
		extra.ObjectData[0].ParamSize = flexible.getBytes().length;

		simulator.sendPacket(extra);
	}

	/**
	 * Set the sculptie texture and data on an object
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param sculpt
	 *            A {@link Primitive.SculptData} object containing the data to set
	 * @throws Exception
	 */
	public final void SetSculpt(Simulator simulator, int localID, SculptData sculpt) throws Exception {
		ObjectExtraParamsPacket extra = new ObjectExtraParamsPacket();

		extra.AgentData.AgentID = _Client.Self.getAgentID();
		extra.AgentData.SessionID = _Client.Self.getSessionID();

		extra.ObjectData = new ObjectExtraParamsPacket.ObjectDataBlock[1];
		extra.ObjectData[0] = extra.new ObjectDataBlock();
		extra.ObjectData[0].ObjectLocalID = localID;
		extra.ObjectData[0].ParamType = ExtraParamType.Sculpt.getValue();
		extra.ObjectData[0].ParamInUse = true;
		extra.ObjectData[0].setParamData(sculpt.getBytes());
		extra.ObjectData[0].ParamSize = sculpt.getBytes().length;

		simulator.sendPacket(extra);

		// Not sure why, but if you don't send this the sculpted prim disappears
		ObjectShapePacket shape = new ObjectShapePacket();

		shape.AgentData.AgentID = _Client.Self.getAgentID();
		shape.AgentData.SessionID = _Client.Self.getSessionID();

		shape.ObjectData = new ObjectShapePacket.ObjectDataBlock[1];
		shape.ObjectData[0] = shape.new ObjectDataBlock();
		shape.ObjectData[0].ObjectLocalID = localID;
		shape.ObjectData[0].PathScaleX = 100;
		shape.ObjectData[0].PathScaleY = (byte) 150;
		shape.ObjectData[0].PathCurve = 32;

		simulator.sendPacket(shape);
	}

	/**
	 * Unset additional primitive parameters on an object
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param type
	 *            The extra parameters to set
	 * @throws Exception
	 */
	public final void SetExtraParamOff(Simulator simulator, int localID, ExtraParamType type) throws Exception {
		ObjectExtraParamsPacket extra = new ObjectExtraParamsPacket();

		extra.AgentData.AgentID = _Client.Self.getAgentID();
		extra.AgentData.SessionID = _Client.Self.getSessionID();
		extra.ObjectData = new ObjectExtraParamsPacket.ObjectDataBlock[1];
		extra.ObjectData[0] = extra.new ObjectDataBlock();
		extra.ObjectData[0].ObjectLocalID = localID;
		extra.ObjectData[0].ParamType = type.getValue();
		extra.ObjectData[0].ParamInUse = false;
		extra.ObjectData[0].setParamData(Helpers.EmptyBytes);
		extra.ObjectData[0].ParamSize = 0;

		simulator.sendPacket(extra);
	}

	/**
	 * Link multiple prims into a linkset
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            objects reside
	 * @param localIDs
	 *            An array which contains the IDs of the objects to link The last
	 *            object in the array will be the root object of the linkset TODO:
	 *            Is this true?
	 * @throws Exception
	 */
	public final void LinkPrims(Simulator simulator, int[] localIDs) throws Exception {
		ObjectLinkPacket packet = new ObjectLinkPacket();

		packet.AgentData.AgentID = _Client.Self.getAgentID();
		packet.AgentData.SessionID = _Client.Self.getSessionID();

		packet.ObjectLocalID = new int[localIDs.length];

		for (int i = 0; i < localIDs.length; i++) {
			packet.ObjectLocalID[i] = localIDs[i];
		}

		simulator.sendPacket(packet);
	}

	/**
	 * Delink/Unlink multiple prims from a linkset
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            objects reside
	 * @param localIDs
	 *            An array which contains the IDs of the objects to delink
	 * @throws Exception
	 */
	public final void DelinkPrims(Simulator simulator, int[] localIDs) throws Exception {
		ObjectDelinkPacket packet = new ObjectDelinkPacket();

		packet.AgentData.AgentID = _Client.Self.getAgentID();
		packet.AgentData.SessionID = _Client.Self.getSessionID();

		packet.ObjectLocalID = new int[localIDs.length];

		for (int i = 0; i < localIDs.length; ++i) {
			packet.ObjectLocalID[i] = localIDs[i];
		}

		simulator.sendPacket(packet);
	}

	/**
	 * Change the rotation of an object
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param rotation
	 *            The new rotation of the object
	 * @throws Exception
	 */
	public final void SetRotation(Simulator simulator, int localID, Quaternion rotation) throws Exception {
		ObjectRotationPacket objRotPacket = new ObjectRotationPacket();
		objRotPacket.AgentData.AgentID = _Client.Self.getAgentID();
		objRotPacket.AgentData.SessionID = _Client.Self.getSessionID();

		objRotPacket.ObjectData = new ObjectRotationPacket.ObjectDataBlock[1];

		objRotPacket.ObjectData[0] = objRotPacket.new ObjectDataBlock();
		objRotPacket.ObjectData[0].ObjectLocalID = localID;
		objRotPacket.ObjectData[0].Rotation = rotation;
		simulator.sendPacket(objRotPacket);
	}

	/**
	 * Set the name of an object
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param name
	 *            A string containing the new name of the object
	 * @throws Exception
	 */
	public final void SetName(Simulator simulator, int localID, String name) throws Exception {
		SetNames(simulator, new int[] { localID }, new String[] { name });
	}

	/**
	 * Set the name of multiple objects
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            objects reside
	 * @param localIDs
	 *            An array which contains the IDs of the objects to change the name
	 *            of
	 * @param names
	 *            An array which contains the new names of the objects
	 * @throws Exception
	 */
	public final void SetNames(Simulator simulator, int[] localIDs, String[] names) throws Exception {
		ObjectNamePacket namePacket = new ObjectNamePacket();
		namePacket.AgentData.AgentID = _Client.Self.getAgentID();
		namePacket.AgentData.SessionID = _Client.Self.getSessionID();

		namePacket.ObjectData = new ObjectNamePacket.ObjectDataBlock[localIDs.length];

		for (int i = 0; i < localIDs.length; ++i) {
			namePacket.ObjectData[i] = namePacket.new ObjectDataBlock();
			namePacket.ObjectData[i].LocalID = localIDs[i];
			namePacket.ObjectData[i].setName(Helpers.StringToBytes(names[i]));
		}

		simulator.sendPacket(namePacket);
	}

	/**
	 * Set the description of an object
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param description
	 *            A string containing the new description of the object
	 * @throws Exception
	 */
	public final void SetDescription(Simulator simulator, int localID, String description) throws Exception {
		SetDescriptions(simulator, new int[] { localID }, new String[] { description });
	}

	/**
	 * Set the descriptions of multiple objects
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            objects reside
	 * @param localIDs
	 *            An array which contains the IDs of the objects to change the
	 *            description of
	 * @param descriptions
	 *            An array which contains the new descriptions of the objects
	 * @throws Exception
	 */
	public final void SetDescriptions(Simulator simulator, int[] localIDs, String[] descriptions) throws Exception {
		ObjectDescriptionPacket descPacket = new ObjectDescriptionPacket();
		descPacket.AgentData.AgentID = _Client.Self.getAgentID();
		descPacket.AgentData.SessionID = _Client.Self.getSessionID();

		descPacket.ObjectData = new ObjectDescriptionPacket.ObjectDataBlock[localIDs.length];

		for (int i = 0; i < localIDs.length; ++i) {
			descPacket.ObjectData[i] = descPacket.new ObjectDataBlock();
			descPacket.ObjectData[i].LocalID = localIDs[i];
			descPacket.ObjectData[i].setDescription(Helpers.StringToBytes(descriptions[i]));
		}

		simulator.sendPacket(descPacket);
	}

	/**
	 * Attach an object to this avatar
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param attachPoint
	 *            The point on the avatar the object will be attached
	 * @param rotation
	 *            The rotation of the attached object
	 * @throws Exception
	 */
	public final void AttachObject(Simulator simulator, int localID, Primitive.AttachmentPoint attachPoint,
			Quaternion rotation) throws Exception {
		ObjectAttachPacket attach = new ObjectAttachPacket();
		attach.AgentData.AgentID = _Client.Self.getAgentID();
		attach.AgentData.SessionID = _Client.Self.getSessionID();
		attach.AgentData.AttachmentPoint = (byte) attachPoint.ordinal();

		attach.ObjectData = new ObjectAttachPacket.ObjectDataBlock[1];
		attach.ObjectData[0] = attach.new ObjectDataBlock();
		attach.ObjectData[0].ObjectLocalID = localID;
		attach.ObjectData[0].Rotation = rotation;

		simulator.sendPacket(attach);
	}

	/**
	 * Drop an attached object from this avatar
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            objects reside. This will always be the simulator the avatar is
	 *            currently in
	 *
	 * @param localID
	 *            The object's ID which is local to the simulator the object is in
	 * @throws Exception
	 */
	public final void DropObject(Simulator simulator, int localID) throws Exception {
		ObjectDropPacket dropit = new ObjectDropPacket();
		dropit.AgentData.AgentID = _Client.Self.getAgentID();
		dropit.AgentData.SessionID = _Client.Self.getSessionID();
		dropit.ObjectLocalID = new int[1];
		dropit.ObjectLocalID[0] = localID;

		simulator.sendPacket(dropit);
	}

	/**
	 * Detach an object from yourself
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            objects reside
	 *
	 *            This will always be the simulator the avatar is currently in
	 *
	 * @param localIDs
	 *            An array which contains the IDs of the objects to detach
	 * @throws Exception
	 */
	public final void DetachObjects(Simulator simulator, int[] localIDs) throws Exception {
		ObjectDetachPacket detach = new ObjectDetachPacket();
		detach.AgentData.AgentID = _Client.Self.getAgentID();
		detach.AgentData.SessionID = _Client.Self.getSessionID();
		detach.ObjectLocalID = new int[localIDs.length];

		for (int i = 0; i < localIDs.length; i++) {
			detach.ObjectLocalID[i] = localIDs[i];
		}

		simulator.sendPacket(detach);
	}

	/**
	 * Change the position of an object, Will change position of entire linkset
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param position
	 *            The new position of the object
	 * @throws Exception
	 */
	public final void SetPosition(Simulator simulator, int localID, Vector3 position) throws Exception {
		byte type = UpdateType.Position | UpdateType.Linked;
		UpdateObject(simulator, localID, position, type);
	}

	/**
	 * Change the position of an object
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param position
	 *            The new position of the object
	 * @param childOnly
	 *            if true, will change position of (this) child prim only, not
	 *            entire linkset
	 * @throws Exception
	 */
	public final void SetPosition(Simulator simulator, int localID, Vector3 position, boolean childOnly)
			throws Exception {
		byte type = UpdateType.Position;

		if (!childOnly) {
			type |= UpdateType.Linked;
		}

		UpdateObject(simulator, localID, position, type);
	}

	/**
	 * Change the Scale (size) of an object
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param scale
	 *            The new scale of the object
	 * @param childOnly
	 *            If true, will change scale of this prim only, not entire linkset
	 * @param uniform
	 *            True to resize prims uniformly
	 * @throws Exception
	 */
	public final void SetScale(Simulator simulator, int localID, Vector3 scale, boolean childOnly, boolean uniform)
			throws Exception {
		byte type = UpdateType.Scale;

		if (!childOnly) {
			type |= UpdateType.Linked;
		}

		if (uniform) {
			type |= UpdateType.Uniform;
		}

		UpdateObject(simulator, localID, scale, type);
	}

	/**
	 * Change the Rotation of an object that is either a child or a whole linkset
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param quat
	 *            The new scale of the object
	 * @param childOnly
	 *            If true, will change rotation of this prim only, not entire
	 *            linkset
	 * @throws Exception
	 */
	public final void SetRotation(Simulator simulator, int localID, Quaternion quat, boolean childOnly)
			throws Exception {
		byte type = UpdateType.Rotation;

		if (!childOnly) {
			type |= UpdateType.Linked;
		}

		MultipleObjectUpdatePacket multiObjectUpdate = new MultipleObjectUpdatePacket();
		multiObjectUpdate.AgentData.AgentID = _Client.Self.getAgentID();
		multiObjectUpdate.AgentData.SessionID = _Client.Self.getSessionID();

		multiObjectUpdate.ObjectData = new MultipleObjectUpdatePacket.ObjectDataBlock[1];

		multiObjectUpdate.ObjectData[0] = multiObjectUpdate.new ObjectDataBlock();
		multiObjectUpdate.ObjectData[0].Type = type;
		multiObjectUpdate.ObjectData[0].ObjectLocalID = localID;
		multiObjectUpdate.ObjectData[0].setData(quat.GetBytes());

		simulator.sendPacket(multiObjectUpdate);
	}

	/**
	 * Send a Multiple Object Update packet to change the size, scale or rotation of
	 * a primitive
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param data
	 *            The new rotation, size, or position of the target object
	 * @param type
	 *            The flags from the {@link UpdateType} Enum
	 * @throws Exception
	 */
	public final void UpdateObject(Simulator simulator, int localID, Vector3 data, byte type) throws Exception {
		MultipleObjectUpdatePacket multiObjectUpdate = new MultipleObjectUpdatePacket();
		multiObjectUpdate.AgentData.AgentID = _Client.Self.getAgentID();
		multiObjectUpdate.AgentData.SessionID = _Client.Self.getSessionID();

		multiObjectUpdate.ObjectData = new MultipleObjectUpdatePacket.ObjectDataBlock[1];

		multiObjectUpdate.ObjectData[0] = multiObjectUpdate.new ObjectDataBlock();
		multiObjectUpdate.ObjectData[0].Type = type;
		multiObjectUpdate.ObjectData[0].ObjectLocalID = localID;
		multiObjectUpdate.ObjectData[0].setData(data.getBytes());

		simulator.sendPacket(multiObjectUpdate);
	}

	/**
	 * Deed an object (prim) to a group, Object must be shared with group which can
	 * be accomplished with SetPermissions()
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param groupOwner
	 *            The {@link UUID} of the group to deed the object to
	 * @throws Exception
	 */
	public final void DeedObject(Simulator simulator, int localID, UUID groupOwner) throws Exception {
		ObjectOwnerPacket objDeedPacket = new ObjectOwnerPacket();
		objDeedPacket.AgentData.AgentID = _Client.Self.getAgentID();
		objDeedPacket.AgentData.SessionID = _Client.Self.getSessionID();

		// Can only be use in God mode
		objDeedPacket.HeaderData.Override = false;
		objDeedPacket.HeaderData.OwnerID = UUID.Zero;
		objDeedPacket.HeaderData.GroupID = groupOwner;

		objDeedPacket.ObjectLocalID = new int[1];

		objDeedPacket.ObjectLocalID[0] = localID;

		simulator.sendPacket(objDeedPacket);
	}

	/**
	 * Deed multiple objects (prims) to a group, Objects must be shared with group
	 * which can be accomplished with SetPermissions()
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param localIDs
	 *            An array which contains the IDs of the objects to deed
	 * @param groupOwner
	 *            The {@link UUID} of the group to deed the object to
	 * @throws Exception
	 */
	public final void DeedObjects(Simulator simulator, int[] localIDs, UUID groupOwner) throws Exception {
		ObjectOwnerPacket packet = new ObjectOwnerPacket();
		packet.AgentData.AgentID = _Client.Self.getAgentID();
		packet.AgentData.SessionID = _Client.Self.getSessionID();

		// Can only be use in God mode
		packet.HeaderData.Override = false;
		packet.HeaderData.OwnerID = UUID.Zero;
		packet.HeaderData.GroupID = groupOwner;

		packet.ObjectLocalID = new int[localIDs.length];

		for (int i = 0; i < localIDs.length; i++) {
			packet.ObjectLocalID[i] = localIDs[i];
		}
		simulator.sendPacket(packet);
	}

	/**
	 * Set the permissions on multiple objects
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            objects reside
	 * @param localIDs
	 *            An array which contains the IDs of the objects to set the
	 *            permissions on
	 * @param who
	 *            The new Who mask to set
	 * @param permissions
	 *            The new Permissions mark to set
	 * @param set
	 *            TODO: What does this do?
	 * @throws Exception
	 */
	public final void SetPermissions(Simulator simulator, int[] localIDs, byte who, int permissions, boolean set)
			throws Exception {
		ObjectPermissionsPacket packet = new ObjectPermissionsPacket();

		packet.AgentData.AgentID = _Client.Self.getAgentID();
		packet.AgentData.SessionID = _Client.Self.getSessionID();

		// Override can only be used by gods
		packet.Override = false;

		packet.ObjectData = new ObjectPermissionsPacket.ObjectDataBlock[localIDs.length];

		for (int i = 0; i < localIDs.length; i++) {
			packet.ObjectData[i] = packet.new ObjectDataBlock();

			packet.ObjectData[i].ObjectLocalID = localIDs[i];
			packet.ObjectData[i].Field = who;
			packet.ObjectData[i].Mask = permissions;
			packet.ObjectData[i].Set = (byte) (set ? 1 : 0);
		}

		simulator.sendPacket(packet);
	}

	/**
	 * Request additional properties for an object
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param objectID
	 * @throws Exception
	 */
	public final void RequestObjectPropertiesFamily(Simulator simulator, UUID objectID) throws Exception {
		RequestObjectPropertiesFamily(simulator, objectID, true);
	}

	/**
	 * Request additional properties for an object
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            object resides
	 * @param objectID
	 *            Absolute UUID of the object
	 * @param reliable
	 *            Whether to require server acknowledgement of this request
	 * @throws Exception
	 */
	public final void RequestObjectPropertiesFamily(Simulator simulator, UUID objectID, boolean reliable)
			throws Exception {
		RequestObjectPropertiesFamilyPacket properties = new RequestObjectPropertiesFamilyPacket();
		properties.AgentData.AgentID = _Client.Self.getAgentID();
		properties.AgentData.SessionID = _Client.Self.getSessionID();
		properties.ObjectData.ObjectID = objectID;
		// TODO: RequestFlags is typically only for bug report submissions, but
		// we might be able to
		// use it to pass an arbitrary uint back to the callback
		properties.ObjectData.RequestFlags = 0;

		properties.getHeader().setReliable(reliable);

		simulator.sendPacket(properties);
	}

	/**
	 * Set the ownership of a list of objects to the specified group
	 *
	 * @param simulator
	 *            A reference to the {@link Simulator.Simulator} object where the
	 *            objects reside
	 * @param localIds
	 *            An array which contains the IDs of the objects to set the group id
	 *            on
	 * @param groupID
	 *            The Groups ID
	 * @throws Exception
	 */
	public final void SetObjectsGroup(Simulator simulator, int[] localIds, UUID groupID) throws Exception {
		ObjectGroupPacket packet = new ObjectGroupPacket();
		packet.AgentData.AgentID = _Client.Self.getAgentID();
		packet.AgentData.GroupID = groupID;
		packet.AgentData.SessionID = _Client.Self.getSessionID();

		packet.ObjectLocalID = new int[localIds.length];
		for (int i = 0; i < localIds.length; i++) {
			packet.ObjectLocalID[i] = localIds[i];
		}
		simulator.sendPacket(packet);
	}

	protected final void UpdateDilation(SimulatorManager s, int dilation) {
		s.Statistics.dilation = dilation / 65535.0f;
	}

	private static ConstructionData CreateConstructionData(Primitive enclosing, PCode pcode,
			ObjectUpdatePacket.ObjectDataBlock block) {
		ConstructionData data = enclosing.new ConstructionData();
		data.state = block.State;
		data.material = Material.setValue(block.Material);
		data.pathCurve = PathCurve.setValue(block.PathCurve);
		data.setProfileValue(block.ProfileCurve);
		data.pathBegin = Primitive.unpackBeginCut(block.PathBegin);
		data.pathEnd = Primitive.unpackEndCut(block.PathEnd);
		data.pathScaleX = Primitive.unpackPathScale(block.PathScaleX);
		data.pathScaleY = Primitive.unpackPathScale(block.PathScaleY);
		data.pathShearX = Primitive.unpackPathShear(block.PathShearX);
		data.pathShearY = Primitive.unpackPathShear(block.PathShearY);
		data.pathTwist = Primitive.unpackPathTwist(block.PathTwist);
		data.pathTwistBegin = Primitive.unpackPathTwist(block.PathTwistBegin);
		data.pathRadiusOffset = Primitive.unpackPathTwist(block.PathRadiusOffset);
		data.pathTaperX = Primitive.unpackPathTaper(block.PathTaperX);
		data.pathTaperY = Primitive.unpackPathTaper(block.PathTaperY);
		data.pathRevolutions = Primitive.unpackPathRevolutions(block.PathRevolutions);
		data.pathSkew = Primitive.unpackPathTwist(block.PathSkew);
		data.profileBegin = Primitive.unpackBeginCut(block.ProfileBegin);
		data.profileEnd = Primitive.unpackEndCut(block.ProfileEnd);
		data.profileHollow = Primitive.unpackProfileHollow(block.ProfileHollow);
		data.primCode = pcode;
		return data;
	}

	private void HandleObjectUpdate(Packet packet, Simulator sim) {
		SimulatorManager simulator = (SimulatorManager) sim;
		ObjectUpdatePacket update = (ObjectUpdatePacket) packet;

		UpdateDilation(simulator, update.RegionData.TimeDilation);

		for (ObjectUpdatePacket.ObjectDataBlock block : update.ObjectData) {
			// #region Relevance check
			// Check if we are interested in this object
			Primitive.PCode pcode = PCode.setValue(block.PCode);
			if (!alwaysDecodeObjects) {
				switch (pcode) {
				case Grass:
				case Tree:
				case NewTree:
				case Prim:
					if (OnObjectUpdate.count() == 0 && OnParticleUpdate.count() == 0) {
						continue;
					}
					break;
				case Avatar:
					// Make an exception for updates about our own agent
					if (!block.FullID.equals(_Client.Self.getAgentID()) && OnAvatarUpdate.count() == 0) {
						continue;
					}
					break;
				case ParticleSystem:
					continue; // TODO: Do something with these
				default:
					break;
				}
			}
			// #endregion Relevance check

			// #region NameValue parsing
			NameValue[] nameValues;
			boolean attachment = false;
			String nameValue = Helpers.EmptyString;
			try {
				nameValue = Helpers.BytesToString(block.getNameValue());
			} catch (UnsupportedEncodingException e) {
			}

			if (nameValue.length() > 0) {
				String[] lines = nameValue.split("\n");
				nameValues = new NameValue[lines.length];

				for (int i = 0; i < lines.length; i++) {
					if (!Helpers.isEmpty(lines[i])) {
						NameValue nv = new NameValue(lines[i]);
						if (nv.Name.equals("AttachItemID")) {
							attachment = true;
						}
						nameValues[i] = nv;
					}
				}
			} else {
				nameValues = new NameValue[0];
			}
			// #endregion NameValue parsing

			// /#region Decode Additional packed parameters in ObjectData
			ObjectMovementUpdate objectupdate = new ObjectMovementUpdate();
			int pos = 0;
			byte[] bytes = block.getObjectData();
			switch (bytes.length) {
			case 76:
				// Collision normal for avatar
				objectupdate.collisionPlane = new Vector4(bytes, pos, true);
				pos += 16;
				// fall through
			case 60:
				// Position
				objectupdate.position = new Vector3(bytes, pos, true);
				pos += 12;
				// Velocity
				objectupdate.velocity = new Vector3(bytes, pos, true);
				pos += 12;
				// Acceleration
				objectupdate.acceleration = new Vector3(bytes, pos, true);
				pos += 12;
				// Rotation (theta)
				objectupdate.rotation = new Quaternion(bytes, pos, true, true);
				pos += 12;
				// Angular velocity (omega)
				objectupdate.angularVelocity = new Vector3(bytes, pos, true);
				pos += 12;
				break;
			case 48:
				// Collision normal for avatar
				objectupdate.collisionPlane = new Vector4(bytes, pos, true);
				pos += 16;
				// fall through
			case 32:
				// The bytes is an array of unsigned shorts

				// Position
				objectupdate.position = new Vector3(Helpers.UInt16ToFloatL(bytes, pos, -0.5f * 256.0f, 1.5f * 256.0f),
						Helpers.UInt16ToFloatL(bytes, pos + 2, -0.5f * 256.0f, 1.5f * 256.0f),
						Helpers.UInt16ToFloatL(bytes, pos + 4, -256.0f, 3.0f * 256.0f));
				pos += 6;
				// Velocity
				objectupdate.velocity = new Vector3(Helpers.UInt16ToFloatL(bytes, pos, -256.0f, 256.0f),
						Helpers.UInt16ToFloatL(bytes, pos + 2, -256.0f, 256.0f),
						Helpers.UInt16ToFloatL(bytes, pos + 4, -256.0f, 256.0f));
				pos += 6;
				// Acceleration
				objectupdate.acceleration = new Vector3(Helpers.UInt16ToFloatL(bytes, pos, -256.0f, 256.0f),
						Helpers.UInt16ToFloatL(bytes, pos + 2, -256.0f, 256.0f),
						Helpers.UInt16ToFloatL(bytes, pos + 4, -256.0f, 256.0f));
				pos += 6;
				// Rotation (theta)
				objectupdate.rotation = new Quaternion(Helpers.UInt16ToFloatL(bytes, pos, -1.0f, 1.0f),
						Helpers.UInt16ToFloatL(bytes, pos + 2, -1.0f, 1.0f),
						Helpers.UInt16ToFloatL(bytes, pos + 4, -1.0f, 1.0f),
						Helpers.UInt16ToFloatL(bytes, pos + 6, -1.0f, 1.0f));
				pos += 8;
				// Angular velocity (omega)
				objectupdate.angularVelocity = new Vector3(
						Helpers.UInt16ToFloatL(block.getObjectData(), pos, -256.0f, 256.0f),
						Helpers.UInt16ToFloatL(block.getObjectData(), pos + 2, -256.0f, 256.0f),
						Helpers.UInt16ToFloatL(block.getObjectData(), pos + 4, -256.0f, 256.0f));
				pos += 6;
				break;
			case 16:
				// The bytes is an array of single bytes (8-bit numbers)

				// Position
				objectupdate.position = new Vector3(Helpers.ByteToFloat(block.getObjectData(), pos, -256.0f, 256.0f),
						Helpers.ByteToFloat(block.getObjectData(), pos + 1, -256.0f, 256.0f),
						Helpers.ByteToFloat(block.getObjectData(), pos + 2, -256.0f, 256.0f));
				pos += 3;
				// Velocity
				objectupdate.velocity = new Vector3(Helpers.ByteToFloat(block.getObjectData(), pos, -256.0f, 256.0f),
						Helpers.ByteToFloat(block.getObjectData(), pos + 1, -256.0f, 256.0f),
						Helpers.ByteToFloat(block.getObjectData(), pos + 2, -256.0f, 256.0f));
				pos += 3;
				// Accleration
				objectupdate.acceleration = new Vector3(
						Helpers.ByteToFloat(block.getObjectData(), pos, -256.0f, 256.0f),
						Helpers.ByteToFloat(block.getObjectData(), pos + 1, -256.0f, 256.0f),
						Helpers.ByteToFloat(block.getObjectData(), pos + 2, -256.0f, 256.0f));
				pos += 3;
				// Rotation
				objectupdate.rotation = new Quaternion(Helpers.ByteToFloat(block.getObjectData(), pos, -1.0f, 1.0f),
						Helpers.ByteToFloat(block.getObjectData(), pos + 1, -1.0f, 1.0f),
						Helpers.ByteToFloat(block.getObjectData(), pos + 2, -1.0f, 1.0f),
						Helpers.ByteToFloat(block.getObjectData(), pos + 3, -1.0f, 1.0f));
				pos += 4;
				// Angular Velocity
				objectupdate.angularVelocity = new Vector3(
						Helpers.ByteToFloat(block.getObjectData(), pos, -256.0f, 256.0f),
						Helpers.ByteToFloat(block.getObjectData(), pos + 1, -256.0f, 256.0f),
						Helpers.ByteToFloat(block.getObjectData(), pos + 2, -256.0f, 256.0f));
				pos += 3;
				break;
			default:
				logger.warn(GridClient.Log(
						"Got an ObjectUpdate block with ObjectUpdate field length of " + block.getObjectData().length,
						_Client));
				continue;
			}
			// #endregion

			// Determine the object type and create the appropriate class
			ConstructionData data;
			RefObject<Boolean> isNewObject = new RefObject<Boolean>(false);
			switch (pcode) {
			// #region Prim and Foliage
			case Grass:
			case Tree:
			case NewTree:
			case Prim:
				Primitive prim = getPrimitive(simulator, block.ID, block.FullID, isNewObject);
				data = CreateConstructionData(prim, pcode, block);
				// Textures
				try {
					objectupdate.textures = new TextureEntry(block.getTextureEntry(), 0,
							block.getTextureEntry().length);
				} catch (Exception ex) {
					logger.warn("Failed to create Texture for object update.", ex);
				}

				OnObjectDataBlockUpdate.dispatch(
						new ObjectDataBlockUpdateCallbackArgs(simulator, prim, data, block, objectupdate, nameValues));

				// #region Update Prim Info with decoded data
				prim.flags = PrimFlags.setValue(block.UpdateFlags);
				if ((prim.flags & PrimFlags.ZlibCompressed) != 0) {
					logger.warn(GridClient.Log("Got a ZlibCompressed ObjectUpdate, implement me!", _Client));
					continue;
				}

				// Automatically request ObjectProperties for prim if it was rezzed selected.
				if ((prim.flags & PrimFlags.CreateSelected) != 0) {
					try {
						SelectObject(simulator, prim.localID);
					} catch (Exception e) {
						logger.warn("Requesting object properties update failed.", e);
					}
				}

				prim.nameValues = nameValues;
				prim.parentID = block.ParentID;
				prim.regionHandle = update.RegionData.RegionHandle;
				prim.scale = block.Scale;
				prim.clickAction = ClickAction.setValue(block.ClickAction);
				prim.ownerID = block.OwnerID;
				try {
					prim.mediaURL = Helpers.BytesToString(block.getMediaURL());
					prim.text = Helpers.BytesToString(block.getText());
				} catch (UnsupportedEncodingException e) {
					logger.warn("Extracting MediaURL or Text for object properties update failed.", e);
				}
				prim.textColor = new Color4(block.TextColor, 0, false, true);
				prim.isAttachment = attachment;

				// Sound information
				prim.soundID = block.Sound;
				prim.soundFlags = SoundFlags.setValue(block.Flags);
				prim.soundGain = block.Gain;
				prim.soundRadius = block.Radius;

				// Joint information
				prim.joint = JointType.setValue(block.JointType);
				prim.jointPivot = block.JointPivot;
				prim.jointAxisOrAnchor = block.JointAxisOrAnchor;

				// Object parameters
				prim.primData = data;

				// Textures, texture animations, particle system, and extra
				// params
				prim.textures = objectupdate.textures;

				prim.textureAnim = prim.textures.new TextureAnimation(block.getTextureAnim(), 0);
				prim.particleSys = new ParticleSystem(block.getPSBlock(), 0);
				prim.setExtraParamsFromBytes(block.getExtraParams(), 0);

				// PCode-specific data
				switch (pcode) {
				case Grass:
				case Tree:
				case NewTree:
					if (block.getData().length == 1) {
						prim.treeSpecies = Tree.setValue(block.getData()[0]);
					} else {
						logger.warn("Got a foliage update with an invalid TreeSpecies field");
					}
					// prim.ScratchPad = Utils.EmptyBytes;
					// break;
					// default:
					// prim.ScratchPad = new byte[block.Data.Length];
					// if (block.Data.Length > 0)
					// Buffer.BlockCopy(block.Data, 0, prim.ScratchPad,
					// 0, prim.ScratchPad.Length);
					break;
				default:
					break;
				}
				prim.scratchPad = Helpers.EmptyBytes;

				// Packed parameters
				prim.collisionPlane = objectupdate.collisionPlane;
				prim.position = objectupdate.position;
				prim.velocity = objectupdate.velocity;
				prim.acceleration = objectupdate.acceleration;
				prim.rotation = objectupdate.rotation;
				prim.angularVelocity = objectupdate.angularVelocity;
				// #endregion

				OnObjectUpdate.dispatch(
						new PrimCallbackArgs(simulator, prim, update.RegionData.TimeDilation, isNewObject.argvalue));

				// OnParticleUpdate handler replacing decode particles, PCode.Particle system
				// appears to be deprecated this is a fix
				if (prim.particleSys.partMaxAge != 0) {
					OnParticleUpdate.dispatch(new ParticleUpdateCallbackArgs(simulator, prim.particleSys, prim));
				}
				break;
			// #endregion Prim and Foliage

			// #region Avatar
			case Avatar:
				// Update some internals if this is our avatar
				if (block.FullID.equals(_Client.Self.getAgentID())
						&& simulator.equals(_Client.Network.getCurrentSim())) {
					// #region Update _Client.Self

					// We need the local ID to recognize terse updates for our agent
					_Client.Self.setLocalID(block.ID);

					// Packed parameters
					_Client.Self.setCollisionPlane(objectupdate.collisionPlane);
					_Client.Self.setRelativePosition(objectupdate.position);
					_Client.Self.setVelocity(objectupdate.velocity);
					_Client.Self.setAcceleration(objectupdate.acceleration);
					_Client.Self.setRelativeRotation(objectupdate.rotation);
					_Client.Self.setAngularVelocity(objectupdate.angularVelocity);
					// #endregion
				}

				// #region Create an Avatar from the decoded data

				Avatar avatar = getAvatar(simulator, block.ID, block.FullID, isNewObject);
				data = CreateConstructionData(avatar, pcode, block);

				objectupdate.avatar = true;
				// Textures
				try {
					objectupdate.textures = new TextureEntry(block.getTextureEntry(), 0,
							block.getTextureEntry().length);
				} catch (Exception ex) {
					logger.warn("Failed to create Texture for avatar update.", ex);
				}

				OnObjectDataBlockUpdate.dispatch(new ObjectDataBlockUpdateCallbackArgs(simulator, avatar, data, block,
						objectupdate, nameValues));

				int oldSeatID = avatar.parentID;

				avatar.scale = block.Scale;
				avatar.collisionPlane = objectupdate.collisionPlane;
				avatar.position = objectupdate.position;
				avatar.velocity = objectupdate.velocity;
				avatar.acceleration = objectupdate.acceleration;
				avatar.rotation = objectupdate.rotation;
				avatar.angularVelocity = objectupdate.angularVelocity;
				avatar.nameValues = nameValues;
				avatar.primData = data;
				if (block.getData().length > 0) {
					logger.warn("Unexpected Data field for an avatar update, length " + block.getData().length);
				}
				avatar.parentID = block.ParentID;
				avatar.regionHandle = update.RegionData.RegionHandle;

				SetAvatarSittingOn(simulator, avatar, block.ParentID, oldSeatID);

				// Textures
				avatar.textures = objectupdate.textures;

				// #endregion Create an Avatar from the decoded data

				OnAvatarUpdate.dispatch(new AvatarUpdateCallbackArgs(simulator, avatar, update.RegionData.TimeDilation,
						isNewObject.argvalue));
				break;
			// #endregion Avatar
			case ParticleSystem:
				/* Obselete */
				break;
			default:
				logger.debug(GridClient.Log("Got an ObjectUpdate block with an unrecognized PCode " + pcode.toString(),
						_Client));
				break;
			}
		}
	}

	/**
	 * A terse object update, used when a transformation matrix or
	 * velocity/acceleration for an object changes but nothing else
	 * (scale/position/rotation/acceleration/velocity)
	 */
	private final void HandleTerseObjectUpdate(Packet packet, Simulator sim) {
		SimulatorManager simulator = (SimulatorManager) sim;
		ImprovedTerseObjectUpdatePacket terse = (ImprovedTerseObjectUpdatePacket) packet;
		UpdateDilation(simulator, terse.RegionData.TimeDilation);

		for (int i = 0; i < terse.ObjectData.length; i++) {
			ImprovedTerseObjectUpdatePacket.ObjectDataBlock block = terse.ObjectData[i];

			try {
				int pos = 4;
				byte[] data = block.getData();
				int localid = Helpers.BytesToInt32L(data, 0);

				// Check if we are interested in this update
				if (!alwaysDecodeObjects && localid != _Client.Self.getLocalID() && OnTerseObjectUpdate.count() > 0) {
					continue;
				}

				// #region Decode update data

				ObjectMovementUpdate update = new ObjectMovementUpdate();

				// LocalID
				update.localID = localid;
				// State
				update.state = data[pos++];
				// Avatar boolean
				update.avatar = (data[pos++] != 0);
				// Collision normal for avatar
				if (update.avatar) {
					update.collisionPlane = new Vector4(data, pos, true);
					pos += 16;
				}
				// Position
				update.position = new Vector3(data, pos, true);
				pos += 12;
				// Velocity
				update.velocity = new Vector3(Helpers.UInt16ToFloatL(data, pos, -128.0f, 128.0f),
						Helpers.UInt16ToFloatL(data, pos + 2, -128.0f, 128.0f),
						Helpers.UInt16ToFloatL(data, pos + 4, -128.0f, 128.0f));
				pos += 6;
				// Acceleration
				update.acceleration = new Vector3(Helpers.UInt16ToFloatL(data, pos, -64.0f, 64.0f),
						Helpers.UInt16ToFloatL(data, pos + 2, -64.0f, 64.0f),
						Helpers.UInt16ToFloatL(data, pos + 4, -64.0f, 64.0f));
				pos += 6;
				// Rotation (theta)
				update.rotation = new Quaternion(Helpers.UInt16ToFloatL(data, pos, -1.0f, 1.0f),
						Helpers.UInt16ToFloatL(data, pos + 2, -1.0f, 1.0f),
						Helpers.UInt16ToFloatL(data, pos + 4, -1.0f, 1.0f),
						Helpers.UInt16ToFloatL(data, pos + 6, -1.0f, 1.0f));
				pos += 8;
				// Angular velocity (omega)
				update.angularVelocity = new Vector3(Helpers.UInt16ToFloatL(data, pos, -64.0f, 64.0f),
						Helpers.UInt16ToFloatL(data, pos + 2, -64.0f, 64.0f),
						Helpers.UInt16ToFloatL(data, pos + 4, -64.0f, 64.0f));
				pos += 6;

				// Textures
				// FIXME: Why are we ignoring the first four bytes here?
				// Most likely because this is the number of bytes that the following
				// TextureEntry block has
				if (block.getTextureEntry().length > 4) {
					update.textures = new TextureEntry(block.getTextureEntry(), 4, block.getTextureEntry().length - 4);
				}
				// #endregion Decode update data

				Primitive obj = null;
				if (objectTracking) {
					if (update.avatar) {
						obj = getAvatar(simulator, update.localID, null, null);
					} else {
						obj = getPrimitive(simulator, update.localID, null, null);
					}
				}

				// Fire the pre-emptive notice (before we stomp the object)
				OnTerseObjectUpdate.dispatch(
						new TerseObjectUpdateCallbackArgs(simulator, obj, update, terse.RegionData.TimeDilation));

				// #region Update _Client.Self
				if (update.localID == _Client.Self.getLocalID()) {
					_Client.Self.setCollisionPlane(update.collisionPlane);
					_Client.Self.setRelativePosition(update.position);
					_Client.Self.setVelocity(update.velocity);
					_Client.Self.setAcceleration(update.acceleration);
					_Client.Self.setRelativeRotation(update.rotation);
					_Client.Self.setAngularVelocity(update.angularVelocity);
				}
				// #endregion Update _Client.Self

				if (obj != null && objectTracking) {
					obj.position = update.position;
					obj.rotation = update.rotation;
					obj.velocity = update.velocity;
					obj.collisionPlane = update.collisionPlane;
					obj.acceleration = update.acceleration;
					obj.angularVelocity = update.angularVelocity;
					if (obj.primData == null)
						obj.primData = obj.new ConstructionData();
					obj.primData.state = update.state;
					if (update.textures != null)
						obj.textures = update.textures;
				}
			} catch (Throwable ex) {
				logger.warn(GridClient.Log(ex.getMessage(), _Client), ex);
			}
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 *
	 *
	 */
	private final void HandleObjectUpdateCompressed(Packet packet, Simulator sim) {
		SimulatorManager simulator = (SimulatorManager) sim;
		ObjectUpdateCompressedPacket update = (ObjectUpdateCompressedPacket) packet;
		UpdateDilation(simulator, update.RegionData.TimeDilation);

		for (int b = 0; b < update.ObjectData.length; b++) {
			ObjectUpdateCompressedPacket.ObjectDataBlock block = update.ObjectData[b];
			int i = 0;
			byte[] data = block.getData();

			// UUID
			UUID fullID = new UUID(data, i);
			i += 16;
			// Local ID
			int localid = (int) Helpers.BytesToUInt32L(data, i);
			i += 4;
			// PCode
			PCode pcode = PCode.setValue(data[i++]);

			// /#region Relevance check
			if (!alwaysDecodeObjects) {
				switch (pcode) {
				case Grass:
				case Tree:
				case NewTree:
				case Prim:
					if (OnObjectUpdate.count() == 0) {
						continue;
					}
					break;
				default:
					break;
				}
			}
			// /#endregion Relevance check
			RefObject<Boolean> isNewObject = new RefObject<Boolean>(false);
			Primitive prim = getPrimitive(simulator, localid, fullID, isNewObject);

			prim.flags = PrimFlags.setValue(block.UpdateFlags);
			prim.primData = prim.new ConstructionData();
			prim.primData.primCode = pcode;

			// /#region Decode block and update Prim

			// State
			prim.primData.state = data[i++];
			// CRC
			i += 4;
			// Material
			prim.primData.material = Material.setValue(data[i++]);
			// Click action
			prim.clickAction = ClickAction.setValue(data[i++]);
			// Scale
			prim.scale = new Vector3(data, i, true);
			i += 12;
			// Position
			prim.position = new Vector3(data, i, true);
			i += 12;
			// Rotation
			prim.rotation = new Quaternion(data, i, true, true);
			i += 12;
			// Compressed flags
			int flags = (int) Helpers.BytesToUInt32L(data, i);
			i += 4;

			prim.ownerID = new UUID(data, i);
			i += 16;

			// Angular velocity
			if ((flags & CompressedFlags.HasAngularVelocity) != 0) {
				prim.angularVelocity = new Vector3(data, i, true);
				i += 12;
			}

			// Parent ID
			if ((flags & CompressedFlags.HasParent) != 0) {
				prim.parentID = (int) Helpers.BytesToUInt32L(data, i);
				i += 4;
			} else {
				prim.parentID = 0;
			}

			prim.scratchPad = Helpers.EmptyBytes;
			// Tree data
			if ((flags & CompressedFlags.Tree) != 0) {
				prim.treeSpecies = Tree.setValue(data[i++]);
			}
			// Scratch pad
			else {
				prim.treeSpecies = Tree.setValue((byte) 0);

				if ((flags & CompressedFlags.ScratchPad) != 0) {
					int size = (int) Helpers.BytesToUInt32L(data, i);
					i += 4;
					prim.scratchPad = new byte[size];
					System.arraycopy(data, i, prim.scratchPad, 0, size);
					i += size;
				}
			}

			// Floating text
			prim.text = Helpers.EmptyString;
			if ((flags & CompressedFlags.HasText) != 0) {
				int idx = i;
				while (data[i] != 0) {
					i++;
				}
				;
				try {
					prim.text = Helpers.BytesToString(data, idx, i - idx, Helpers.UTF8_ENCODING);
				} catch (UnsupportedEncodingException e) {
				}
				i++;
				// Text color
				prim.textColor = new Color4(data, i, false, true);
				i += 4;
			}

			prim.isAttachment = (((flags & CompressedFlags.HasNameValues) != 0) && prim.parentID != 0);

			// Media URL
			prim.mediaURL = Helpers.EmptyString;
			if ((flags & CompressedFlags.MediaURL) != 0) {
				int idx = i;
				while (data[i] != 0) {
					i++;
				}
				;
				try {
					prim.mediaURL = Helpers.BytesToString(data, idx, i - idx, Helpers.UTF8_ENCODING);
				} catch (UnsupportedEncodingException e) {
				}
				i++;
			}

			// Particle system
			if ((flags & CompressedFlags.HasParticles) != 0) {
				prim.particleSys = new ParticleSystem(data, i);
				i += 86;
			} else {
				prim.particleSys = null;
			}

			// Extra parameters
			i += prim.setExtraParamsFromBytes(data, i);

			// Sound data
			if ((flags & CompressedFlags.HasSound) != 0) {
				prim.soundID = new UUID(data, i);
				i += 16;

				prim.soundGain = Helpers.BytesToFloatL(data, i);
				i += 4;
				prim.soundFlags = SoundFlags.setValue(data[i++]);
				prim.soundRadius = Helpers.BytesToFloatL(data, i);
				i += 4;
			}

			// Name values
			if ((flags & CompressedFlags.HasNameValues) != 0) {
				String text = Helpers.EmptyString;
				int idx = i;
				while (data[i] != 0) {
					i++;
				}
				;
				try {
					text = Helpers.BytesToString(data, idx, i - idx, Helpers.UTF8_ENCODING);
				} catch (UnsupportedEncodingException e) {
				}
				i++;

				// Parse the name values
				if (text.length() > 0) {
					String[] lines = text.split("\n");
					prim.nameValues = new NameValue[lines.length];

					for (int j = 0; j < lines.length; j++) {
						if (!Helpers.isEmpty(lines[j])) {
							NameValue nv = new NameValue(lines[j]);
							prim.nameValues[j] = nv;
						}
					}
				}
			}

			if (data.length >= i + 23) {
				prim.primData.pathCurve = PathCurve.setValue(data[i++]);

				prim.primData.pathBegin = Primitive.unpackBeginCut((short) Helpers.BytesToUInt16L(data, i));
				i += 2;
				prim.primData.pathEnd = Primitive.unpackEndCut((short) Helpers.BytesToUInt16L(data, i));
				i += 2;
				prim.primData.pathScaleX = Primitive.unpackPathScale(data[i++]);
				prim.primData.pathScaleY = Primitive.unpackPathScale(data[i++]);
				prim.primData.pathShearX = Primitive.unpackPathShear(data[i++]);
				prim.primData.pathShearY = Primitive.unpackPathShear(data[i++]);
				prim.primData.pathTwist = Primitive.unpackPathTwist(data[i++]);
				prim.primData.pathTwistBegin = Primitive.unpackPathTwist(data[i++]);
				prim.primData.pathRadiusOffset = Primitive.unpackPathTwist(data[i++]);
				prim.primData.pathTaperX = Primitive.unpackPathTaper(data[i++]);
				prim.primData.pathTaperY = Primitive.unpackPathTaper(data[i++]);
				prim.primData.pathRevolutions = Primitive.unpackPathRevolutions(data[i++]);
				prim.primData.pathSkew = Primitive.unpackPathTwist(data[i++]);

				prim.primData.profileCurve = ProfileCurve.setValue(data[i++]);
				prim.primData.profileBegin = Primitive.unpackBeginCut((short) Helpers.BytesToUInt16L(data, i));
				i += 2;
				prim.primData.profileEnd = Primitive.unpackEndCut((short) Helpers.BytesToUInt16L(data, i));
				i += 2;
				prim.primData.profileHollow = Primitive.unpackProfileHollow((short) Helpers.BytesToUInt16L(data, i));
				i += 2;
			}

			if (data.length >= i + 4) {
				// TextureEntry
				int textureEntryLength = (int) Helpers.BytesToUInt32L(data, i);
				i += 4;
				prim.textures = new TextureEntry(data, i, textureEntryLength);
				i += textureEntryLength;
			}
			// int textureAnimLength = (int)Helpers.BytesToUInt32L(data, i);
			if (data.length >= i + 20 && (flags & CompressedFlags.TextureAnimation) != 0) {
				// Texture animation
				int textureAnimationLength = (int) Helpers.BytesToUInt32L(data, i);
				i += 4;
				prim.textureAnim = prim.textures.new TextureAnimation(data, i, textureAnimationLength);
				i += textureAnimationLength;
			}

			prim.isAttachment = (flags & CompressedFlags.HasNameValues) != 0 && prim.parentID != 0;

			if (data.length > i) {
				logger.debug("CompressedUpdate has extra data of " + (data.length - i) + " bytes.");
			}
			// #endregion

			OnObjectUpdate.dispatch(
					new PrimCallbackArgs(simulator, prim, update.RegionData.TimeDilation, isNewObject.argvalue));

			if (prim.particleSys != null && prim.particleSys.partMaxAge != 0) {
				OnParticleUpdate.dispatch(new ParticleUpdateCallbackArgs(simulator, prim.particleSys, prim));
			}
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void HandleObjectUpdateCached(Packet packet, Simulator simulator) {
		if (alwaysRequestObjects) {
			ObjectUpdateCachedPacket update = (ObjectUpdateCachedPacket) packet;
			int[] ids = new int[update.ObjectData.length];

			// No object caching implemented yet, so request updates for all of
			// these objects
			for (int i = 0; i < update.ObjectData.length; i++) {
				ids[i] = update.ObjectData[i].ID;
			}
			try {
				RequestObjects(simulator, ids);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void HandleKillObject(Packet packet, Simulator sim) {
		SimulatorManager simulator = (SimulatorManager) sim;
		KillObjectPacket kill = (KillObjectPacket) packet;

		// Notify first, so that handler has a chance to get a
		// reference from the ObjectTracker to the object being killed
		int[] killed = new int[kill.ID.length];
		for (int i = 0; i < kill.ID.length; i++) {
			killed[i] = kill.ID[i];
		}
		OnKillObject.dispatch(new KillObjectsCallbackArgs(simulator, killed));

		ArrayList<Integer> removeAvatars = new ArrayList<Integer>();
		ArrayList<Integer> removePrims = new ArrayList<Integer>();

		HashMap<Integer, Primitive> primitives = simulator.getObjectsPrimitives();
		synchronized (primitives) {
			if (objectTracking) {
				for (int localID : kill.ID) {
					if (simulator.getObjectsPrimitives().containsKey(localID)) {
						removePrims.add(localID);
					}

					for (Entry<Integer, Primitive> e : simulator.getObjectsPrimitives().entrySet()) {
						if (e.getValue().parentID == localID) {
							removePrims.add(e.getKey());
						}
					}
				}
			}

			if (_Client.Settings.getBool(LibSettings.AVATAR_TRACKING)) {
				HashMap<Integer, Avatar> avatars = simulator.getObjectsAvatars();
				synchronized (avatars) {
					for (int localID : kill.ID) {
						if (avatars.containsKey(localID)) {
							removeAvatars.add(localID);
						}

						ArrayList<Integer> rootPrims = new ArrayList<Integer>();

						for (Entry<Integer, Primitive> e : primitives.entrySet()) {
							if (e.getValue().parentID == localID) {
								removePrims.add(e.getKey());
								rootPrims.add(e.getKey());
							}
						}

						for (Entry<Integer, Primitive> e : primitives.entrySet()) {
							if (rootPrims.contains(e.getValue().parentID)) {
								removePrims.add(e.getKey());
							}
						}
					}

					// Do the actual removing outside of the loops but still inside the lock.
					// This safely prevents the collection from being modified during a loop.
					for (int removeID : removeAvatars) {
						avatars.remove(removeID);
					}
				}
			}

			int i = 0;
			killed = new int[removePrims.size()];
			for (int removeID : removePrims) {
				killed[i++] = removeID;
			}
			OnKillObject.dispatch(new KillObjectsCallbackArgs(simulator, killed));

			for (int removeID : removePrims) {
				primitives.remove(removeID);
			}
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void HandleObjectProperties(Packet packet, Simulator sim) {
		SimulatorManager simulator = (SimulatorManager) sim;
		ObjectPropertiesPacket op = (ObjectPropertiesPacket) packet;
		ObjectPropertiesPacket.ObjectDataBlock[] datablocks = op.ObjectData;

		for (int i = 0; i < datablocks.length; ++i) {
			ObjectPropertiesPacket.ObjectDataBlock objectData = datablocks[i];
			ObjectProperties props = new ObjectProperties();

			props.objectID = objectData.ObjectID;
			props.aggregatePerms = objectData.AggregatePerms;
			props.aggregatePermTextures = objectData.AggregatePermTextures;
			props.aggregatePermTexturesOwner = objectData.AggregatePermTexturesOwner;
			props.category = ObjectCategory.setValue(objectData.Category);
			props.folderID = objectData.FolderID;
			props.fromTaskID = objectData.FromTaskID;
			props.inventorySerial = objectData.InventorySerial;
			props.itemID = objectData.ItemID;
			props.ownershipCost = objectData.OwnershipCost;
			props.salePrice = objectData.SalePrice;
			props.saleType = SaleType.setValue(objectData.SaleType);
			props.permissions = new Permissions(objectData.CreatorID, objectData.OwnerID, objectData.LastOwnerID,
					objectData.GroupID, objectData.BaseMask, objectData.EveryoneMask, objectData.GroupMask,
					objectData.NextOwnerMask, objectData.OwnerMask);

			try {
				props.name = Helpers.BytesToString(objectData.getName());
				props.description = Helpers.BytesToString(objectData.getDescription());
				props.creationDate = Helpers.UnixTimeToDateTime(objectData.CreationDate);
				props.sitName = Helpers.BytesToString(objectData.getSitName());
				props.touchName = Helpers.BytesToString(objectData.getTouchName());
			} catch (UnsupportedEncodingException e) {
				logger.warn("Encoding Exception when decoding object properties reply.", e);
				return;
			}

			int numTextures = objectData.getTextureID().length / 16;
			props.textureIDs = new UUID[numTextures];
			for (int j = 0; j < numTextures; ++j) {
				props.textureIDs[j] = new UUID(objectData.getTextureID(), j * 16);
			}

			if (objectTracking) {
				synchronized (simulator.getObjectsPrimitives()) {
					for (Primitive prim : simulator.getObjectsPrimitives().values()) {
						if (prim.id.equals(props.objectID)) {
							OnObjectPropertiesUpdated
									.dispatch(new ObjectPropertiesUpdatedCallbackArgs(simulator, prim, props));

							if (simulator.getObjectsPrimitives().containsKey(prim.localID)) {
								simulator.getObjectsPrimitives().get(prim.localID).properties = props;
							}
							break;
						}
					}
				}
			}
			OnObjectProperties.dispatch(new ObjectPropertiesCallbackArgs(simulator, props));
		}
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void HandleObjectPropertiesFamily(Packet packet, Simulator sim) {
		SimulatorManager simulator = (SimulatorManager) sim;
		ObjectPropertiesFamilyPacket op = (ObjectPropertiesFamilyPacket) packet;
		ObjectProperties props = new ObjectProperties();

		ReportType requestType = ReportType.setValue(op.ObjectData.RequestFlags);

		props.objectID = op.ObjectData.ObjectID;
		props.category = ObjectCategory.setValue(op.ObjectData.Category);
		props.ownershipCost = op.ObjectData.OwnershipCost;
		props.salePrice = op.ObjectData.SalePrice;
		props.saleType = SaleType.setValue(op.ObjectData.SaleType);
		props.permissions = new Permissions(null, op.ObjectData.OwnerID, op.ObjectData.LastOwnerID,
				op.ObjectData.GroupID, op.ObjectData.BaseMask, op.ObjectData.EveryoneMask, op.ObjectData.GroupMask,
				op.ObjectData.NextOwnerMask, op.ObjectData.OwnerMask);
		try {
			props.name = Helpers.BytesToString(op.ObjectData.getName());
			props.description = Helpers.BytesToString(op.ObjectData.getDescription());
		} catch (UnsupportedEncodingException e) {
			logger.warn("Encoding Exception when decoding object properties family reply.", e);
			return;
		}

		if (objectTracking) {
			synchronized (simulator.getObjectsPrimitives()) {
				for (Primitive prim : simulator.getObjectsPrimitives().values()) {
					if (prim.id.equals(op.ObjectData.ObjectID)) {
						if (simulator.getObjectsPrimitives().containsKey(prim.localID)) {
							if (simulator.getObjectsPrimitives().get(prim.localID).properties == null) {
								simulator.getObjectsPrimitives().get(prim.localID).properties = new ObjectProperties();
							}
							simulator.getObjectsPrimitives().get(prim.localID).properties.setFamilyProperties(props);
						}
						break;
					}
				}
			}
		}
		OnObjectPropertiesFamily.dispatch(new ObjectPropertiesFamilyCallbackArgs(simulator, props, requestType));
	}

	/**
	 * Process an incoming packet and raise the appropriate events
	 */
	private final void HandlePayPriceReply(Packet packet, Simulator simulator) {
		if (OnPayPriceReply.count() > 0) {
			PayPriceReplyPacket p = (PayPriceReplyPacket) packet;
			UUID objectID = p.ObjectData.ObjectID;
			int defaultPrice = p.ObjectData.DefaultPayPrice;
			int[] buttonPrices = new int[p.PayButton.length];

			for (int i = 0; i < p.PayButton.length; i++) {
				buttonPrices[i] = p.PayButton[i];
			}

			OnPayPriceReply.dispatch(new PayPriceReplyCallbackArgs(simulator, objectID, defaultPrice, buttonPrices));
		}
	}

	private void HandleObjectPhysicsProperties(IMessage message, Simulator sim) {
		SimulatorManager simulator = (SimulatorManager) sim;
		ObjectPhysicsPropertiesMessage msg = (ObjectPhysicsPropertiesMessage) message;

		if (objectTracking) {
			for (int i = 0; i < msg.objectPhysicsProperties.length; i++) {
				synchronized (simulator.getObjectsPrimitives()) {
					if (simulator.getObjectsPrimitives().containsKey(msg.objectPhysicsProperties[i].localID)) {
						simulator.getObjectsPrimitives().get(
								msg.objectPhysicsProperties[i].localID).physicsProps = msg.objectPhysicsProperties[i];
					}
				}
			}
		}

		if (OnPhysicsProperties.count() > 0) {
			for (int i = 0; i < msg.objectPhysicsProperties.length; i++) {
				OnPhysicsProperties
						.dispatch(new PhysicsPropertiesCallbackArgs(simulator, msg.objectPhysicsProperties[i]));
			}
		}
	}
	// #endregion Packet Handlers

	// #region Utility Functions
	/**
	 *
	 *
	 * @param sim
	 * @param av
	 * @param localid
	 * @param oldSeatID
	 */
	protected final void SetAvatarSittingOn(Simulator sim, Avatar av, int localid, int oldSeatID) {
		if (_Client.Network.getCurrentSim() == sim && av.localID == _Client.Self.getLocalID()) {
			_Client.Self.setSittingOn(localid);
		}

		av.parentID = localid;

		if (OnAvatarSitChanged.count() > 0 && oldSeatID != localid) {
			OnAvatarSitChanged.dispatch(new AvatarSitChangedCallbackArgs(sim, av, localid, oldSeatID));
		}
	}

	/**
	 * Set the Shape data of an object
	 *
	 * @param simulator
	 *            A reference to the <seealso cref="OpenMetaverse.Simulator"/>
	 *            object where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param prim
	 *            Data describing the prim shape
	 * @throws Exception
	 */
	public void SetShape(Simulator simulator, int localID, Primitive.ConstructionData prim) throws Exception {
		ObjectShapePacket shape = new ObjectShapePacket();

		shape.AgentData.AgentID = _Client.Self.getAgentID();
		shape.AgentData.SessionID = _Client.Self.getSessionID();

		shape.ObjectData = new ObjectShapePacket.ObjectDataBlock[1];
		shape.ObjectData[0] = shape.new ObjectDataBlock();

		shape.ObjectData[0].ObjectLocalID = localID;

		shape.ObjectData[0].PathCurve = prim.pathCurve.getValue();
		shape.ObjectData[0].PathBegin = Primitive.packBeginCut(prim.pathBegin);
		shape.ObjectData[0].PathEnd = Primitive.packEndCut(prim.pathEnd);
		shape.ObjectData[0].PathScaleX = Primitive.packPathScale(prim.pathScaleX);
		shape.ObjectData[0].PathScaleY = Primitive.packPathScale(prim.pathScaleY);
		shape.ObjectData[0].PathShearX = Primitive.packPathShear(prim.pathShearX);
		shape.ObjectData[0].PathShearY = Primitive.packPathShear(prim.pathShearY);
		shape.ObjectData[0].PathTwist = Primitive.packPathTwist(prim.pathTwist);
		shape.ObjectData[0].PathTwistBegin = Primitive.packPathTwist(prim.pathTwistBegin);
		shape.ObjectData[0].PathRadiusOffset = Primitive.packPathTwist(prim.pathRadiusOffset);
		shape.ObjectData[0].PathTaperX = Primitive.packPathTaper(prim.pathTaperX);
		shape.ObjectData[0].PathTaperY = Primitive.packPathTaper(prim.pathTaperY);
		shape.ObjectData[0].PathRevolutions = Primitive.packPathRevolutions(prim.pathRevolutions);
		shape.ObjectData[0].PathSkew = Primitive.packPathTwist(prim.pathSkew);

		shape.ObjectData[0].ProfileCurve = prim.getProfileValue();
		shape.ObjectData[0].ProfileBegin = Primitive.packBeginCut(prim.profileBegin);
		shape.ObjectData[0].ProfileEnd = Primitive.packEndCut(prim.profileEnd);
		shape.ObjectData[0].ProfileHollow = Primitive.packProfileHollow(prim.profileHollow);

		simulator.sendPacket(shape);
	}

	/**
	 * Set the Material data of an object
	 *
	 * @param simulator
	 *            A reference to the <seealso cref="OpenMetaverse.Simulator"/>
	 *            object where the object resides
	 * @param localID
	 *            The objects ID which is local to the simulator the object is in
	 * @param material
	 *            The new material of the object
	 * @throws Exception
	 */
	public void SetMaterial(Simulator simulator, int localID, Material material) throws Exception {
		ObjectMaterialPacket matPacket = new ObjectMaterialPacket();

		matPacket.AgentData.AgentID = _Client.Self.getAgentID();
		matPacket.AgentData.SessionID = _Client.Self.getSessionID();

		matPacket.ObjectData = new ObjectMaterialPacket.ObjectDataBlock[1];
		matPacket.ObjectData[0] = matPacket.new ObjectDataBlock();

		matPacket.ObjectData[0].ObjectLocalID = localID;
		matPacket.ObjectData[0].Material = material.getValue();

		simulator.sendPacket(matPacket);
	}
	// #endregion Utility Functions

	// #region Object Tracking Link

	/**
	 * Find the object with localID in the simulator and add it with fullID if it is
	 * not there
	 *
	 * @param simulator
	 *            The simulator in which the object is located
	 * @param localID
	 *            The simulator localID for this object
	 * @param fullID
	 *            The full object ID used to add a new object to the simulator list,
	 *            when the object could not be found.
	 * @return the object that corresponds to the localID
	 */
	protected final Primitive getPrimitive(SimulatorManager simulator, int localID, UUID fullID,
			RefObject<Boolean> created) {
		if (objectTracking) {
			synchronized (simulator.getObjectsPrimitives()) {
				Primitive prim = simulator.getObjectsPrimitives().get(localID);
				if (prim != null) {
					return prim;
				}

				prim = simulator.findPrimitive(fullID, true);
				if (prim == null) {
					prim = new Primitive();
					prim.id = fullID;
					prim.regionHandle = simulator.getHandle();
					if (created != null)
						created.argvalue = true;
				}
				prim.localID = localID;

				simulator.getObjectsPrimitives().put(localID, prim);

				return prim;
			}
		}
		return new Primitive();
	}

	/**
	 * Find the avatar with localID in the simulator and add it with fullID if it is
	 * not there
	 *
	 * @param simulator
	 *            The simulator in which the avatar is located
	 * @param localID
	 *            The simulator localID for this avatar
	 * @param fullID
	 *            The full avatar ID used to add a new avatar object to the
	 *            simulator list, when the avatar could not be found.
	 * @return the avatar object that corresponds to the localID
	 */
	protected final Avatar getAvatar(SimulatorManager simulator, int localID, UUID fullID, RefObject<Boolean> created) {
		if (_Client.Settings.getBool(LibSettings.AVATAR_TRACKING)) {
			HashMap<Integer, Avatar> avatars = simulator.getObjectsAvatars();
			synchronized (avatars) {
				Avatar avatar = avatars.get(localID);
				if (avatar != null) {
					return avatar;
				}

				avatar = simulator.findAvatar(fullID, true);
				if (avatar == null) {
					avatar = new Avatar();
					avatar.id = fullID;
					avatar.regionHandle = simulator.getHandle();
					if (created != null)
						created.argvalue = true;
				} else {
					if (avatar.localID == _Client.Self.getLocalID()) {
						_Client.Self.setLocalID(localID);
					}
				}
				avatar.localID = localID;

				avatars.put(localID, avatar);

				return avatar;
			}
		}
		return new Avatar();
	}
	// #endregion Object Tracking Link
}
