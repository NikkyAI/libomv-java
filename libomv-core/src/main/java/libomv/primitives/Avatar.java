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
package libomv.primitives;

import java.util.ArrayList;
import java.util.List;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.model.agent.ControlFlags;
import libomv.model.appearance.AppearanceFlags;
import libomv.model.avatar.Animation;
import libomv.types.NameValue;
import libomv.types.UUID;
import libomv.utils.Helpers;

/* Basic class to hold other Avatar's data. */
public class Avatar extends Primitive {

	// Avatar profile flags
	// [Flags]
	public static class ProfileFlags {
		public static final byte None = 0;
		public static final byte AllowPublish = 1;
		public static final byte MaturePublish = 2;
		public static final byte Identified = 4;
		public static final byte Transacted = 8;
		public static final byte Online = 16;
		public static final byte AgeVerified = 32;

		private static final byte MASK = 0x3F;

		public static byte setValue(int value) {
			return (byte) (value & MASK);
		}

		public static byte getValue(byte value) {
			return (byte) (value & MASK);
		}

	}

	// Positive and negative ratings
	public final class Statistics {
		// Positive ratings for Behavior
		public int behaviorPositive;
		// Negative ratings for Behavior
		public int behaviorNegative;
		// Positive ratings for Appearance
		public int appearancePositive;
		// Negative ratings for Appearance
		public int appearanceNegative;
		// Positive ratings for Building
		public int buildingPositive;
		// Negative ratings for Building
		public int buildingNegative;
		// Positive ratings given by this avatar
		public int givenPositive;
		// Negative ratings given by this avatar
		public int givenNegative;

		public Statistics(OSD osd) {
			OSDMap tex = (OSDMap) osd;

			behaviorPositive = tex.get("behavior_positive").asInteger();
			buildingNegative = tex.get("behavior_negative").asInteger();
			appearancePositive = tex.get("appearance_positive").asInteger();
			appearanceNegative = tex.get("appearance_negative").asInteger();
			buildingPositive = tex.get("buildings_positive").asInteger();
			buildingNegative = tex.get("buildings_negative").asInteger();
			givenPositive = tex.get("given_positive").asInteger();
			givenNegative = tex.get("given_negative").asInteger();
		}

		public OSD serialize() {
			OSDMap tex = new OSDMap(8);
			tex.put("behavior_positive", OSD.fromInteger(behaviorPositive));
			tex.put("behavior_negative", OSD.fromInteger(behaviorNegative));
			tex.put("appearance_positive", OSD.fromInteger(appearancePositive));
			tex.put("appearance_negative", OSD.fromInteger(appearanceNegative));
			tex.put("buildings_positive", OSD.fromInteger(buildingPositive));
			tex.put("buildings_negative", OSD.fromInteger(buildingNegative));
			tex.put("given_positive", OSD.fromInteger(givenPositive));
			tex.put("given_negative", OSD.fromInteger(givenNegative));
			return tex;
		}

		public Statistics fromOSD(OSD osd) {
			return new Statistics(osd);
		}
	}

	// Avatar properties including about text, profile URL, image IDs and
	// publishing settings
	public final class AvatarProperties {
		// First Life about text
		public String firstLifeText;
		// First Life image ID
		public UUID firstLifeImage;
		//
		public UUID partner;
		//
		public String aboutText;
		//
		public String bornOn;
		//
		public String charterMember;
		// Profile image ID
		public UUID profileImage;
		// Flags of the profile
		public byte flags;
		// Web URL for this profile
		public String profileURL;

		public AvatarProperties() {
		}

		public AvatarProperties(OSD osd) {
			OSDMap tex = (OSDMap) osd;

			firstLifeText = tex.get("first_life_text").asString();
			firstLifeImage = tex.get("first_life_image").asUUID();
			partner = tex.get("partner").asUUID();
			aboutText = tex.get("about_text").asString();
			bornOn = tex.get("born_on").asString();
			charterMember = tex.get("chart_member").asString();
			profileImage = tex.get("profile_image").asUUID();
			flags = ProfileFlags.setValue(tex.get("flags").asInteger());
			profileURL = tex.get("profile_url").asString();
		}

		// Should this profile be published on the web
		public boolean getAllowPublish() {
			return (flags & ProfileFlags.AllowPublish) != 0;
		}

		public void setAllowPublish(boolean value) {
			if (value == true) {
				flags |= ProfileFlags.AllowPublish;
			} else {
				flags &= ~ProfileFlags.AllowPublish;
			}
		}

		// Avatar Online Status
		public boolean getOnline() {
			return (flags & ProfileFlags.Online) != 0;
		}

		public void setOnline(boolean value) {
			if (value == true) {
				flags |= ProfileFlags.Online;
			} else {
				flags &= ~ProfileFlags.Online;
			}
		}

		// Is this a mature profile
		public boolean getMaturePublish() {
			return (flags & ProfileFlags.MaturePublish) != 0;
		}

		public void setMaturePublish(boolean value) {
			if (value == true) {
				flags |= ProfileFlags.MaturePublish;
			} else {
				flags &= ~ProfileFlags.MaturePublish;
			}
		}

		public boolean getIdentified() {
			return (flags & ProfileFlags.Identified) != 0;
		}

		public void setIdentified(boolean value) {
			if (value == true) {
				flags |= ProfileFlags.Identified;
			} else {
				flags &= ~ProfileFlags.Identified;
			}
		}

		public boolean getTransacted() {
			return (flags & ProfileFlags.Transacted) != 0;
		}

		public void setTransacted(boolean value) {
			if (value == true) {
				flags |= ProfileFlags.Transacted;
			} else {
				flags &= ~ProfileFlags.Transacted;
			}
		}

		public boolean getAgeVerified() {
			return (flags & ProfileFlags.AgeVerified) != 0;
		}

		public void setAgeVerified(boolean value) {
			if (value == true) {
				flags |= ProfileFlags.AgeVerified;
			} else {
				flags &= ~ProfileFlags.AgeVerified;
			}
		}

		public OSD serialize() {
			OSDMap tex = new OSDMap(9);
			tex.put("first_life_text", OSD.fromString(firstLifeText));
			tex.put("first_life_image", OSD.fromUUID(firstLifeImage));
			tex.put("partner", OSD.fromUUID(partner));
			tex.put("about_text", OSD.fromString(aboutText));
			tex.put("born_on", OSD.fromString(bornOn));
			tex.put("charter_member", OSD.fromString(charterMember));
			tex.put("profile_image", OSD.fromUUID(profileImage));
			tex.put("flags", OSD.fromInteger(ProfileFlags.getValue(flags)));
			tex.put("profile_url", OSD.fromString(profileURL));
			return tex;
		}

	}

	// Avatar interests including spoken languages, skills, and "want to"
	// choices
	public final class Interests {
		// Languages profile field
		public String languagesText;
		// FIXME: ORIGINAL LINE: public uint SkillsMask;
		public int skillsMask;
		//
		public String skillsText;
		// FIXME: ORIGINAL LINE: public uint WantToMask;
		public int wantToMask;
		//
		public String wantToText;

		public Interests() {
		}

		public Interests(OSD osd) {
			OSDMap tex = (OSDMap) osd;

			languagesText = tex.get("languages_text").asString();
			skillsMask = tex.get("skills_mask").asUInteger();
			skillsText = tex.get("skills_text").asString();
			wantToMask = tex.get("want_to_mask").asUInteger();
			wantToText = tex.get("want_to_text").asString();
		}

		public OSD serialize() {
			OSDMap InterestsOSD = new OSDMap(5);
			InterestsOSD.put("languages_text", OSD.fromString(languagesText));
			InterestsOSD.put("skills_mask", OSD.fromUInteger(skillsMask));
			InterestsOSD.put("skills_text", OSD.fromString(skillsText));
			InterestsOSD.put("want_to_mask", OSD.fromUInteger(wantToMask));
			InterestsOSD.put("want_to_text", OSD.fromString(wantToText));
			return InterestsOSD;
		}

	}

	// Groups that this avatar is a member of
	public List<UUID> groups = new ArrayList<>();
	// Positive and negative ratings
	public Statistics profileStatistics;
	// Avatar properties including about text, profile URL, image IDs and
	// publishing settings
	public AvatarProperties profileProperties;
	// Avatar interests including spoken languages, skills, and "want to"
	// choices
	public Interests profileInterests;
	// Movement control flags for avatars. Typically not set or used by clients.
	// To move your avatar, use Client.Self.Movement instead
	public int controlFlags;

	// Contains the visual parameters describing the deformation of the avatar
	public byte[] visualParameters = null;

	// Appearance version. Value greater than 0 indicates using server side baking
	public byte appearanceVersion = 0;

	// Version of the Current Outfit Folder that the appearance is based on
	public int cofVersion = 0;

	// Appearance flags. Introduced with server side baking, currently unused.
	public AppearanceFlags appearanceFlags = AppearanceFlags.None;

	// List of current avatar animations
	public List<Animation> animations;

	protected String name;
	protected String displayName;
	protected String groupName;

	// /#region Properties

	// Default constructor
	public Avatar() {
		super();
	}

	public Avatar(UUID id) {
		super();
		this.id = id;
	}

	public Avatar(OSD osd) {
		super(osd);

		OSDMap tex = (OSDMap) osd;

		groups = new ArrayList<>();

		for (OSD U : (OSDArray) tex.get("groups")) {
			groups.add(U.asUUID());
		}

		profileStatistics = new Statistics(tex.get("profile_statistics"));
		profileProperties = new AvatarProperties(tex.get("profile_properties"));
		profileInterests = new Interests(tex.get("profile_interest"));
		controlFlags = ControlFlags.setValue(tex.get("control_flags").asInteger());

		OSDArray vp = (OSDArray) tex.get("visual_parameters");
		visualParameters = new byte[vp.size()];

		for (int i = 0; i < vp.size(); i++) {
			visualParameters[i] = (byte) vp.get(i).asInteger();
		}

		nameValues = new NameValue[3];

		NameValue First = new NameValue("FirstName");
		First.type = NameValue.ValueType.String;
		First.valueObject = tex.get("first_name").asString();

		NameValue Last = new NameValue("LastName");
		Last.type = NameValue.ValueType.String;
		Last.valueObject = tex.get("last_name").asString();

		NameValue Group = new NameValue("Title");
		Group.type = NameValue.ValueType.String;
		Group.valueObject = tex.get("group_name").asString();

		nameValues[0] = First;
		nameValues[1] = Last;
		nameValues[2] = Group;
	}

	// First name
	public final String getFirstName() {
		for (int i = 0; i < nameValues.length; i++) {
			if (nameValues[i].name.equals("FirstName") && nameValues[i].type == NameValue.ValueType.String) {
				return (String) nameValues[i].valueObject;
			}
		}
		return Helpers.EmptyString;
	}

	// Last name
	public final String getLastName() {
		for (int i = 0; i < nameValues.length; i++) {
			if (nameValues[i].name.equals("LastName") && nameValues[i].type == NameValue.ValueType.String) {
				return (String) nameValues[i].valueObject;
			}
		}
		return Helpers.EmptyString;
	}

	// Full name
	public final String getName() {
		if (!Helpers.isEmpty(name)) {
			return name;
		} else if (nameValues != null && nameValues.length > 0) {
			synchronized (nameValues) {
				String firstName = Helpers.EmptyString;
				String lastName = Helpers.EmptyString;

				for (int i = 0; i < nameValues.length; i++) {
					if (nameValues[i].name.equals("FirstName") && nameValues[i].type == NameValue.ValueType.String) {
						firstName = (String) nameValues[i].valueObject;
					} else if (nameValues[i].name.equals("LastName")
							&& nameValues[i].type == NameValue.ValueType.String) {
						lastName = (String) nameValues[i].valueObject;
					}
				}

				if (!firstName.isEmpty() && !lastName.isEmpty()) {
					name = String.format("%s %s", firstName, lastName);
					return name;
				}
			}
		}
		return Helpers.EmptyString;
	}

	public void setNames(String firstName, String lastName) {
		if (!firstName.isEmpty() && !lastName.isEmpty()) {
			name = String.format("%s %s", firstName, lastName);
		} else {
			name = Helpers.EmptyString;
		}

		if (nameValues != null && nameValues.length > 0) {
			synchronized (nameValues) {
				for (int i = 0; i < nameValues.length; i++) {
					if (nameValues[i].name.equals("FirstName") && nameValues[i].type == NameValue.ValueType.String) {
						nameValues[i].valueObject = firstName;
					} else if (nameValues[i].name.equals("LastName")
							&& nameValues[i].type == NameValue.ValueType.String) {
						nameValues[i].valueObject = lastName;
					}
				}
			}
		}
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String name) {
		displayName = name;
	}

	// Active group
	public final String getGroupName() {
		if (!Helpers.isEmpty(groupName)) {
			return groupName;
		}

		if (nameValues != null || nameValues.length > 0) {
			synchronized (nameValues) {
				for (int i = 0; i < nameValues.length; i++) {
					if (nameValues[i].name.equals("Title") && nameValues[i].type == NameValue.ValueType.String) {
						groupName = (String) nameValues[i].valueObject;
						return groupName;
					}
				}
			}
		}
		return Helpers.EmptyString;
	}

	@Override
	public OSD serialize() {
		OSDMap Avi = (OSDMap) super.serialize();
		OSDArray grp = new OSDArray();

		for (int i = 0; i < groups.size(); i++) {
			grp.add(OSD.fromUUID(groups.get(i)));
		}

		OSDArray vp = new OSDArray();

		for (int i = 0; i < visualParameters.length; i++) {
			vp.add(OSD.fromInteger(visualParameters[i]));
		}

		Avi.put("groups", grp);
		Avi.put("profile_statistics", profileStatistics.serialize());
		Avi.put("profile_properties", profileProperties.serialize());
		Avi.put("profile_interest", profileInterests.serialize());
		Avi.put("control_flags", OSD.fromInteger(ControlFlags.getValue(controlFlags)));
		Avi.put("visual_parameters", vp);
		Avi.put("first_name", OSD.fromString(getFirstName()));
		Avi.put("last_name", OSD.fromString(getLastName()));
		Avi.put("group_name", OSD.fromString(getGroupName()));

		return Avi;

	}

}
