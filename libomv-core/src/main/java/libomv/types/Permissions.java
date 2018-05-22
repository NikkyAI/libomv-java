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
package libomv.types;

import java.io.Serializable;

import libomv.StructuredData.*;

public class Permissions implements Serializable {
	private static final long serialVersionUID = 1L;

	// [Flags]
	public static class PermissionMask {
		public static final int None = 0;
		public static final int Transfer = 1 << 13;
		public static final int Modify = 1 << 14;
		public static final int Copy = 1 << 15;
		public static final int Export = 1 << 16;
		public static final int Move = 1 << 19;
		public static final int Damage = 1 << 20;

		// All does not contain Export, which is special and must be explicitly given
		public static final int All = Transfer | Modify | Copy | Move | Damage;

		private static final int MASK = Transfer | Modify | Copy | Export | Move | Damage;

		public static int setValue(int value) {
			return value & MASK;
		}

		public static int getValue(int value) {
			return value;
		}

	}

	// [Flags]
	public static class PermissionWho {
		public static final byte Base = 0x01;
		public static final byte Owner = 0x02;
		public static final byte Group = 0x04;
		public static final byte Everyone = 0x08;
		public static final byte NextOwner = 0x10;
		public static final byte All = 0x1F;

		private static final byte MASK = All;

		public static byte setValue(int value) {
			return (byte) (value & MASK);
		}

		public static int getValue(int value) {
			return value;
		}

	}

	public static final Permissions NoPermissions = new Permissions();
	public static final Permissions FullPermissions = new Permissions(null, null, null, null, PermissionMask.All,
			PermissionMask.All, PermissionMask.All, PermissionMask.All, PermissionMask.All);

	public UUID creatorID;
	public UUID ownerID;
	public UUID lastOwnerID;
	public UUID groupID;

	public boolean isGroupOwned;
	public int baseMask;
	public int ownerMask;
	public int groupMask;
	public int everyoneMask;
	public int nextOwnerMask;

	public Permissions() {
		baseMask = 0;
		everyoneMask = 0;
		groupMask = 0;
		nextOwnerMask = 0;
		ownerMask = 0;
	}

	public Permissions(OSD osd) {
		fromOSD(osd);
	}

	public Permissions(UUID creator, UUID owner, UUID lastOwner, UUID group, int baseMask, int everyoneMask,
			int groupMask, int nextOwnerMask, int ownerMask) {
		this.creatorID = creator;
		this.ownerID = owner;
		this.lastOwnerID = lastOwner;
		this.groupID = group;

		this.isGroupOwned = ownerID == null && groupID != null;

		this.baseMask = baseMask;
		this.everyoneMask = everyoneMask;
		this.groupMask = groupMask;
		this.nextOwnerMask = nextOwnerMask;
		this.ownerMask = ownerMask;
	}

	public Permissions(UUID creator, UUID owner, UUID lastOwner, UUID group, boolean groupOwned, int baseMask,
			int everyoneMask, int groupMask, int nextOwnerMask, int ownerMask) {
		this.creatorID = creator;
		this.ownerID = owner;
		this.lastOwnerID = lastOwner;
		this.groupID = group;

		this.isGroupOwned = groupOwned;

		this.baseMask = baseMask;
		this.everyoneMask = everyoneMask;
		this.groupMask = groupMask;
		this.nextOwnerMask = nextOwnerMask;
		this.ownerMask = ownerMask;
	}

	public Permissions(Permissions perm) {
		creatorID = perm.creatorID;
		ownerID = perm.ownerID;
		lastOwnerID = perm.lastOwnerID;
		groupID = perm.groupID;

		isGroupOwned = perm.isGroupOwned;

		ownerMask = perm.ownerMask;
		groupMask = perm.groupMask;
		everyoneMask = perm.everyoneMask;
		nextOwnerMask = perm.nextOwnerMask;
	}

	public Permissions getNextPermissions(UUID newOwner, UUID group) {
		int nextMask = nextOwnerMask;

		return new Permissions(creatorID, newOwner, ownerID, group, baseMask & nextMask, everyoneMask & nextMask,
				groupMask & nextMask, nextOwnerMask, ownerMask & nextMask);
	}

	public OSD serialize() {
		OSDMap permissions = new OSDMap(5);
		permissions.put("creator_id", OSD.fromUUID(creatorID));
		permissions.put("owner_id", OSD.fromUUID(ownerID));
		permissions.put("last_owner_id", OSD.fromUUID(lastOwnerID));
		permissions.put("group_id", OSD.fromUUID(groupID));
		permissions.put("is_owner_group", OSD.fromBoolean(isGroupOwned));

		permissions.put("base_mask", OSD.fromInteger(baseMask));
		permissions.put("owner_mask", OSD.fromInteger(ownerMask));
		permissions.put("group_mask", OSD.fromInteger(groupMask));
		permissions.put("everyone_mask", OSD.fromInteger(everyoneMask));
		permissions.put("next_owner_mask", OSD.fromInteger(nextOwnerMask));
		return permissions;
	}

	public static Permissions fromOSD(OSD llsd) {
		Permissions permissions = new Permissions();
		OSDMap map = (OSDMap) ((llsd instanceof OSDMap) ? llsd : null);

		if (map != null) {
			permissions.creatorID = map.get("creator_id").asUUID();
			permissions.ownerID = map.get("owner_id").asUUID();
			permissions.lastOwnerID = map.get("last_owner_id").asUUID();
			permissions.groupID = map.get("group_id").asUUID();
			permissions.isGroupOwned = map.get("is_owner_group").asBoolean();

			permissions.baseMask = map.get("base_mask").asUInteger();
			permissions.everyoneMask = map.get("everyone_mask").asUInteger();
			permissions.groupMask = map.get("group_mask").asUInteger();
			permissions.nextOwnerMask = map.get("next_owner_mask").asUInteger();
			permissions.ownerMask = map.get("owner_mask").asUInteger();
		}

		return permissions;
	}

	@Override
	public String toString() {
		return String.format("Base: %s, Everyone: %s, Group: %s, NextOwner: %s, Owner: %s", baseMask, everyoneMask,
				groupMask, nextOwnerMask, ownerMask);
	}

	@Override
	public int hashCode() {
		return baseMask ^ everyoneMask ^ groupMask ^ nextOwnerMask ^ ownerMask;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null & (obj instanceof Permissions) && equals(this, (Permissions) obj);
	}

	public boolean equals(Permissions other) {
		return equals(this, other);
	}

	public static boolean equals(Permissions lhs, Permissions rhs) {
		return (lhs.baseMask == rhs.baseMask) && (lhs.everyoneMask == rhs.everyoneMask)
				&& (lhs.groupMask == rhs.groupMask) && (lhs.nextOwnerMask == rhs.nextOwnerMask)
				&& (lhs.ownerMask == rhs.ownerMask);
	}

	public static boolean hasPermissions(int perms, int checkPerms) {
		return (perms & checkPerms) == checkPerms;
	}

}
