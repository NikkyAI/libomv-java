/**
 * Copyright (c) 2009, openmetaverse.org
 * Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names
 *   of its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
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
package libomv.assets;

import java.util.ArrayList;
import java.util.Date;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.ObjectManager;
import libomv.primitives.ObjectProperties;
import libomv.primitives.ParticleSystem;
import libomv.primitives.ParticleSystem.SourcePattern;
import libomv.primitives.Primitive;
import libomv.primitives.Primitive.PathCurve;
import libomv.primitives.Primitive.PrimFlags;
import libomv.primitives.Primitive.ProfileCurve;
import libomv.primitives.TextureEntry;
import libomv.types.Color4;
import libomv.types.Permissions;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector2;
import libomv.types.Vector3;

// A linkset asset, containing a parent primitive and zero or more children
public class AssetPrim extends AssetItem
{
    // Only used internally for XML serialization/deserialization
    private enum ProfileShape
    {
        Circle,
        Square,
        IsometricTriangle,
        EquilateralTriangle,
        RightTriangle,
        HalfCircle;

        public static ProfileShape setValue(int value)
        {
        	return values()[value];
        }

        public static byte getValue(ProfileShape value)
        {
        	return (byte)value.ordinal();
        }

        public byte getValue()
        {
        	return (byte)ordinal();
        }
    }

    public PrimObject Parent;
    public ArrayList<PrimObject> Children;

    // Override the base classes AssetType
    @Override
    public AssetType getAssetType()
    {
        return AssetType.Object;
    }

    // Initializes a new instance of an AssetPrim object
    public AssetPrim()
    {
    }

/*
    public AssetPrim(String xmlData)
    {
        DecodeXml(xmlData);
    }
*/
    public AssetPrim(PrimObject parent, ArrayList<PrimObject> children)
    {
        Parent = parent;
        if (children != null)
            Children = children;
        else
            Children = new ArrayList<PrimObject>(0);
    }

    @Override
    public void Encode()
    {
        // FIXME:
    }

    @Override
    public boolean Decode()
    {
        // FIXME:
        return false;
    }

/*
    public String EncodeXml()
    {
        TextWriter textWriter = new StringWriter();
        using (XmlTextWriter xmlWriter = new XmlTextWriter(textWriter))
        {
            OarFile.SOGToXml2(xmlWriter, this);
            xmlWriter.Flush();
            return textWriter.ToString();
        }
    }

    public boolean DecodeXml(String xmlData)
    {
        using (XmlTextReader reader = new XmlTextReader(new StringReader(xmlData)))
        {
            reader.Read();
            reader.ReadStartElement("SceneObjectGroup");
            Parent = LoadPrim(reader);
            if (Parent != null)
            {
                ArrayList<PrimObject> children = new ArrayList<PrimObject>();

                reader.Read();

                while (!reader.EOF)
                {
                    switch (reader.NodeType)
                    {
                        case XmlNodeType.Element:
                            if (reader.Name == "SceneObjectPart")
                            {
                                PrimObject child = LoadPrim(reader);
                                if (child != null)
                                    children.Add(child);
                            }
                            else
                            {
                                //Logger.Log("Found unexpected prim XML element " + reader.Name, Helpers.LogLevel.Debug);
                                reader.Read();
                            }
                            break;
                        case XmlNodeType.EndElement:
                        default:
                            reader.Read();
                            break;
                    }
                }

                Children = children;
                return true;
            }
            else
            {
                Logger.Log("Failed to load root linkset prim", Helpers.LogLevel.Error);
                return false;
            }
        }
    }

    public static PrimObject LoadPrim(XmlTextReader reader)
    {
        PrimObject obj = new PrimObject();
        obj.Shape = new PrimObject.ShapeBlock();
        obj.Inventory = new PrimObject.InventoryBlock();

        reader.ReadStartElement("SceneObjectPart");

        if (reader.Name == "AllowedDrop")
            obj.AllowedDrop = reader.ReadElementContentAsBoolean("AllowedDrop", Helpers.EmptyString);
        else
            obj.AllowedDrop = true;

        obj.CreatorID = ReadUUID(reader, "CreatorID");
        obj.FolderID = ReadUUID(reader, "FolderID");
        obj.Inventory.Serial = reader.ReadElementContentAsInt("InventorySerial", Helpers.EmptyString);

        // FIXME: Parse TaskInventory
        obj.Inventory.Items = new PrimObject.InventoryBlock.ItemBlock[0];
        reader.ReadInnerXml();

        int flags = reader.ReadElementContentAsInt("ObjectFlags", Helpers.EmptyString);
        obj.UsePhysics = (flags & PrimFlags.Physics) != 0;
        obj.Phantom = (flags & PrimFlags.Phantom) != 0;
        obj.DieAtEdge = (flags & PrimFlags.DieAtEdge) != 0;
        obj.ReturnAtEdge = (flags & PrimFlags.ReturnAtEdge) != 0;
        obj.Temporary = (flags & PrimFlags.Temporary) != 0;
        obj.Sandbox = (flags & PrimFlags.Sandbox) != 0;

        obj.ID = ReadUUID(reader, "UUID");
        obj.LocalID = reader.ReadElementContentAsLong("LocalId", Helpers.EmptyString);
        obj.Name = reader.ReadElementString("Name");
        obj.Material = reader.ReadElementContentAsInt("Material", Helpers.EmptyString);

        reader.ReadInnerXml(); // RegionHandle

        obj.RemoteScriptAccessPIN = reader.ReadElementContentAsInt("ScriptAccessPin", Helpers.EmptyString);

        Vector3 groupPosition = ReadVector(reader, "GroupPosition");
        Vector3 offsetPosition = ReadVector(reader, "OffsetPosition");
        obj.Rotation = ReadQuaternion(reader, "RotationOffset");
        obj.Velocity = ReadVector(reader, "Velocity");
        Vector3 rotationalVelocity = ReadVector(reader, "RotationalVelocity");
        obj.AngularVelocity = ReadVector(reader, "AngularVelocity");
        obj.Acceleration = ReadVector(reader, "Acceleration");
        obj.Description = reader.ReadElementString("Description");
        reader.ReadStartElement("Color");
        if (reader.Name == "R")
        {
            obj.TextColor.R = reader.ReadElementContentAsFloat("R", Helpers.EmptyString);
            obj.TextColor.G = reader.ReadElementContentAsFloat("G", Helpers.EmptyString);
            obj.TextColor.B = reader.ReadElementContentAsFloat("B", Helpers.EmptyString);
            obj.TextColor.A = reader.ReadElementContentAsFloat("A", Helpers.EmptyString);
            reader.ReadEndElement();
        }
        obj.Text = reader.ReadElementString("Text", Helpers.EmptyString);
        obj.SitName = reader.ReadElementString("SitName", Helpers.EmptyString);
        obj.TouchName = reader.ReadElementString("TouchName", Helpers.EmptyString);

        obj.LinkNumber = reader.ReadElementContentAsInt("LinkNum", Helpers.EmptyString);
        obj.ClickAction = reader.ReadElementContentAsInt("ClickAction", Helpers.EmptyString);

        reader.ReadStartElement("Shape");
        obj.Shape.ProfileCurve = reader.ReadElementContentAsInt("ProfileCurve", Helpers.EmptyString);

        byte[] teData = Convert.FromBase64String(reader.ReadElementString("TextureEntry"));
        obj.Textures = new Primitive.TextureEntry(teData, 0, teData.Length);

        reader.ReadInnerXml(); // ExtraParams

        obj.Shape.PathBegin = Primitive.UnpackBeginCut((ushort)reader.ReadElementContentAsInt("PathBegin", Helpers.EmptyString));
        obj.Shape.PathCurve = reader.ReadElementContentAsInt("PathCurve", Helpers.EmptyString);
        obj.Shape.PathEnd = Primitive.UnpackEndCut((ushort)reader.ReadElementContentAsInt("PathEnd", Helpers.EmptyString));
        obj.Shape.PathRadiusOffset = Primitive.UnpackPathTwist((sbyte)reader.ReadElementContentAsInt("PathRadiusOffset", Helpers.EmptyString));
        obj.Shape.PathRevolutions = Primitive.UnpackPathRevolutions((byte)reader.ReadElementContentAsInt("PathRevolutions", Helpers.EmptyString));
        obj.Shape.PathScaleX = Primitive.UnpackPathScale((byte)reader.ReadElementContentAsInt("PathScaleX", Helpers.EmptyString));
        obj.Shape.PathScaleY = Primitive.UnpackPathScale((byte)reader.ReadElementContentAsInt("PathScaleY", Helpers.EmptyString));
        obj.Shape.PathShearX = Primitive.UnpackPathShear((sbyte)reader.ReadElementContentAsInt("PathShearX", Helpers.EmptyString));
        obj.Shape.PathShearY = Primitive.UnpackPathShear((sbyte)reader.ReadElementContentAsInt("PathShearY", Helpers.EmptyString));
        obj.Shape.PathSkew = Primitive.UnpackPathTwist((sbyte)reader.ReadElementContentAsInt("PathSkew", Helpers.EmptyString));
        obj.Shape.PathTaperX = Primitive.UnpackPathTaper((sbyte)reader.ReadElementContentAsInt("PathTaperX", Helpers.EmptyString));
        obj.Shape.PathTaperY = Primitive.UnpackPathShear((sbyte)reader.ReadElementContentAsInt("PathTaperY", Helpers.EmptyString));
        obj.Shape.PathTwist = Primitive.UnpackPathTwist((sbyte)reader.ReadElementContentAsInt("PathTwist", Helpers.EmptyString));
        obj.Shape.PathTwistBegin = Primitive.UnpackPathTwist((sbyte)reader.ReadElementContentAsInt("PathTwistBegin", Helpers.EmptyString));
        obj.PCode = reader.ReadElementContentAsInt("PCode", Helpers.EmptyString);
        obj.Shape.ProfileBegin = Primitive.UnpackBeginCut((ushort)reader.ReadElementContentAsInt("ProfileBegin", Helpers.EmptyString));
        obj.Shape.ProfileEnd = Primitive.UnpackEndCut((ushort)reader.ReadElementContentAsInt("ProfileEnd", Helpers.EmptyString));
        obj.Shape.ProfileHollow = Primitive.UnpackProfileHollow((ushort)reader.ReadElementContentAsInt("ProfileHollow", Helpers.EmptyString));
        obj.Scale = ReadVector(reader, "Scale");
        obj.State = (byte)reader.ReadElementContentAsInt("State", Helpers.EmptyString);

        ProfileShape profileShape = (ProfileShape)Enum.Parse(typeof(ProfileShape), reader.ReadElementString("ProfileShape"));
        HoleType holeType = (HoleType)Enum.Parse(typeof(HoleType), reader.ReadElementString("HollowShape"));
        obj.Shape.ProfileCurve = (int)profileShape | (int)holeType;

        UUID sculptTexture = ReadUUID(reader, "SculptTexture");
        SculptType sculptType = (SculptType)reader.ReadElementContentAsInt("SculptType", Helpers.EmptyString);
        if (!sculptTexture.equals(UUID.Zero))
        {
            obj.Sculpt = new PrimObject.SculptBlock();
            obj.Sculpt.Texture = sculptTexture;
            obj.Sculpt.Type = (int)sculptType;
        }

        PrimObject.FlexibleBlock flexible = new PrimObject.FlexibleBlock();
        PrimObject.LightBlock light = new PrimObject.LightBlock();

        reader.ReadInnerXml(); // SculptData

        flexible.Softness = reader.ReadElementContentAsInt("FlexiSoftness", Helpers.EmptyString);
        flexible.Tension = reader.ReadElementContentAsFloat("FlexiTension", Helpers.EmptyString);
        flexible.Drag = reader.ReadElementContentAsFloat("FlexiDrag", Helpers.EmptyString);
        flexible.Gravity = reader.ReadElementContentAsFloat("FlexiGravity", Helpers.EmptyString);
        flexible.Wind = reader.ReadElementContentAsFloat("FlexiWind", Helpers.EmptyString);
        flexible.Force.X = reader.ReadElementContentAsFloat("FlexiForceX", Helpers.EmptyString);
        flexible.Force.Y = reader.ReadElementContentAsFloat("FlexiForceY", Helpers.EmptyString);
        flexible.Force.Z = reader.ReadElementContentAsFloat("FlexiForceZ", Helpers.EmptyString);

        light.Color.R = reader.ReadElementContentAsFloat("LightColorR", Helpers.EmptyString);
        light.Color.G = reader.ReadElementContentAsFloat("LightColorG", Helpers.EmptyString);
        light.Color.B = reader.ReadElementContentAsFloat("LightColorB", Helpers.EmptyString);
        light.Color.A = reader.ReadElementContentAsFloat("LightColorA", Helpers.EmptyString);
        light.Radius = reader.ReadElementContentAsFloat("LightRadius", Helpers.EmptyString);
        light.Cutoff = reader.ReadElementContentAsFloat("LightCutoff", Helpers.EmptyString);
        light.Falloff = reader.ReadElementContentAsFloat("LightFalloff", Helpers.EmptyString);
        light.Intensity = reader.ReadElementContentAsFloat("LightIntensity", Helpers.EmptyString);

        boolean hasFlexi = reader.ReadElementContentAsBoolean("FlexiEntry", Helpers.EmptyString);
        boolean hasLight = reader.ReadElementContentAsBoolean("LightEntry", Helpers.EmptyString);
        reader.ReadInnerXml(); // SculptEntry

        if (hasFlexi)
            obj.Flexible = flexible;
        if (hasLight)
            obj.Light = light;

        reader.ReadEndElement();

        obj.Scale = ReadVector(reader, "Scale"); // Yes, again
        reader.ReadInnerXml(); // UpdateFlag

        reader.ReadInnerXml(); // SitTargetOrientation
        reader.ReadInnerXml(); // SitTargetPosition
        obj.SitOffset = ReadVector(reader, "SitTargetPositionLL");
        obj.SitRotation = ReadQuaternion(reader, "SitTargetOrientationLL");
        obj.ParentID = (uint)reader.ReadElementContentAsLong("ParentID", Helpers.EmptyString);
        obj.CreationDate = Utils.UnixTimeToDateTime(reader.ReadElementContentAsInt("CreationDate", Helpers.EmptyString));
        int category = reader.ReadElementContentAsInt("Category", Helpers.EmptyString);
        obj.SalePrice = reader.ReadElementContentAsInt("SalePrice", Helpers.EmptyString);
        obj.SaleType = reader.ReadElementContentAsInt("ObjectSaleType", Helpers.EmptyString);
        int ownershipCost = reader.ReadElementContentAsInt("OwnershipCost", Helpers.EmptyString);
        obj.GroupID = ReadUUID(reader, "GroupID");
        obj.OwnerID = ReadUUID(reader, "OwnerID");
        obj.LastOwnerID = ReadUUID(reader, "LastOwnerID");
        obj.PermsBase = (uint)reader.ReadElementContentAsInt("BaseMask", Helpers.EmptyString);
        obj.PermsOwner = (uint)reader.ReadElementContentAsInt("OwnerMask", Helpers.EmptyString);
        obj.PermsGroup = (uint)reader.ReadElementContentAsInt("GroupMask", Helpers.EmptyString);
        obj.PermsEveryone = (uint)reader.ReadElementContentAsInt("EveryoneMask", Helpers.EmptyString);
        obj.PermsNextOwner = (uint)reader.ReadElementContentAsInt("NextOwnerMask", Helpers.EmptyString);

        reader.ReadInnerXml(); // Flags

        obj.CollisionSound = ReadUUID(reader, "CollisionSound");
        obj.CollisionSoundVolume = reader.ReadElementContentAsFloat("CollisionSoundVolume", Helpers.EmptyString);

        reader.ReadEndElement();

        if (obj.ParentID == 0)
            obj.Position = groupPosition;
        else
            obj.Position = offsetPosition;

        return obj;
    }

    static UUID ReadUUID(XmlTextReader reader, String name)
    {
        UUID id;
        String idStr;

        reader.ReadStartElement(name);

        if (reader.Name == "Guid")
            idStr = reader.ReadElementString("Guid");
        else // UUID
            idStr = reader.ReadElementString("UUID");

        UUID.TryParse(idStr, out id);
        reader.ReadEndElement();

        return id;
    }

    static Vector3 ReadVector(XmlTextReader reader, String name)
    {
        Vector3 vec;

        reader.ReadStartElement(name);
        vec.X = reader.ReadElementContentAsFloat("X", Helpers.EmptyString);
        vec.Y = reader.ReadElementContentAsFloat("Y", Helpers.EmptyString);
        vec.Z = reader.ReadElementContentAsFloat("Z", Helpers.EmptyString);
        reader.ReadEndElement();

        return vec;
    }

    static Quaternion ReadQuaternion(XmlTextReader reader, String name)
    {
        Quaternion quat;

        reader.ReadStartElement(name);
        quat.X = reader.ReadElementContentAsFloat("X", Helpers.EmptyString);
        quat.Y = reader.ReadElementContentAsFloat("Y", Helpers.EmptyString);
        quat.Z = reader.ReadElementContentAsFloat("Z", Helpers.EmptyString);
        quat.W = reader.ReadElementContentAsFloat("W", Helpers.EmptyString);
        reader.ReadEndElement();

        return quat;
    }
*/
    /** The deserialized form of a single primitive in a linkset asset */
    public class PrimObject
    {
        public class FlexibleBlock
        {
            public int Softness;
            public float Gravity;
            public float Drag;
            public float Wind;
            public float Tension;
            public Vector3 Force;

            public OSDMap Serialize()
            {
                OSDMap map = new OSDMap();
                map.put("softness", OSD.FromInteger(Softness));
                map.put("gravity", OSD.FromReal(Gravity));
                map.put("drag", OSD.FromReal(Drag));
                map.put("wind", OSD.FromReal(Wind));
                map.put("tension", OSD.FromReal(Tension));
                map.put("force", OSD.FromVector3(Force));
                return map;
            }

            public void Deserialize(OSDMap map)
            {
                Softness = map.get("softness").AsInteger();
                Gravity = (float)map.get("gravity").AsReal();
                Drag = (float)map.get("drag").AsReal();
                Wind = (float)map.get("wind").AsReal();
                Tension = (float)map.get("tension").AsReal();
                Force = map.get("force").AsVector3();
            }
        }

        public class LightBlock
        {
            public Color4 Color;
            public float Intensity;
            public float Radius;
            public float Falloff;
            public float Cutoff;

            public OSDMap Serialize()
            {
                OSDMap map = new OSDMap();
                map.put("color", OSD.FromColor4(Color));
                map.put("intensity", OSD.FromReal(Intensity));
                map.put("radius", OSD.FromReal(Radius));
                map.put("falloff", OSD.FromReal(Falloff));
                map.put("cutoff", OSD.FromReal(Cutoff));
                return map;
            }

            public void Deserialize(OSDMap map)
            {
                Color = map.get("color").AsColor4();
                Intensity = (float)map.get("intensity").AsReal();
                Radius = (float)map.get("radius").AsReal();
                Falloff = (float)map.get("falloff").AsReal();
                Cutoff = (float)map.get("cutoff").AsReal();
            }
        }

        public class SculptBlock
        {
            public UUID Texture;
            public byte Type;

            public OSDMap Serialize()
            {
                OSDMap map = new OSDMap();
                map.put("texture", OSD.FromUUID(Texture));
                map.put("type", OSD.FromInteger(Type));
                return map;
            }

            public void Deserialize(OSDMap map)
            {
                Texture = map.get("texture").AsUUID();
                Type = (byte) map.get("type").AsInteger();
            }
        }

        public class ParticlesBlock
        {
            public int Flags;
            public int Pattern;
            public float MaxAge;
            public float StartAge;
            public float InnerAngle;
            public float OuterAngle;
            public float BurstRate;
            public float BurstRadius;
            public float BurstSpeedMin;
            public float BurstSpeedMax;
            public int BurstParticleCount;
            public Vector3 AngularVelocity;
            public Vector3 Acceleration;
            public UUID TextureID;
            public UUID TargetID;
            public int DataFlags;
            public float ParticleMaxAge;
            public Color4 ParticleStartColor;
            public Color4 ParticleEndColor;
            public Vector2 ParticleStartScale;
            public Vector2 ParticleEndScale;

            public OSDMap Serialize()
            {
                OSDMap map = new OSDMap();
                map.put("flags", OSD.FromInteger(Flags));
                map.put("pattern", OSD.FromInteger(Pattern));
                map.put("max_age", OSD.FromReal(MaxAge));
                map.put("start_age", OSD.FromReal(StartAge));
                map.put("inner_angle", OSD.FromReal(InnerAngle));
                map.put("outer_angle", OSD.FromReal(OuterAngle));
                map.put("burst_rate", OSD.FromReal(BurstRate));
                map.put("burst_radius", OSD.FromReal(BurstRadius));
                map.put("burst_speed_min", OSD.FromReal(BurstSpeedMin));
                map.put("burst_speed_max", OSD.FromReal(BurstSpeedMax));
                map.put("burst_particle_count", OSD.FromInteger(BurstParticleCount));
                map.put("angular_velocity", OSD.FromVector3(AngularVelocity));
                map.put("acceleration", OSD.FromVector3(Acceleration));
                map.put("texture_id", OSD.FromUUID(TextureID));
                map.put("target_id", OSD.FromUUID(TargetID));
                map.put("data_flags", OSD.FromInteger(DataFlags));
                map.put("particle_max_age", OSD.FromReal(ParticleMaxAge));
                map.put("particle_start_color", OSD.FromColor4(ParticleStartColor));
                map.put("particle_end_color", OSD.FromColor4(ParticleEndColor));
                map.put("particle_start_scale", OSD.FromVector2(ParticleStartScale));
                map.put("particle_end_scale", OSD.FromVector2(ParticleEndScale));
                return map;
            }

            public void Deserialize(OSDMap map)
            {
                Flags = map.get("flags").AsInteger();
                Pattern = map.get("pattern").AsInteger();
                MaxAge = (float)map.get("max_age").AsReal();
                StartAge = (float)map.get("start_age").AsReal();
                InnerAngle = (float)map.get("inner_angle").AsReal();
                OuterAngle = (float)map.get("outer_angle").AsReal();
                BurstRate = (float)map.get("burst_rate").AsReal();
                BurstRadius = (float)map.get("burst_radius").AsReal();
                BurstSpeedMin = (float)map.get("burst_speed_min").AsReal();
                BurstSpeedMax = (float)map.get("burst_speed_max").AsReal();
                BurstParticleCount = map.get("burst_particle_count").AsInteger();
                AngularVelocity = map.get("angular_velocity").AsVector3();
                Acceleration = map.get("acceleration").AsVector3();
                TextureID = map.get("texture_id").AsUUID();
                DataFlags = map.get("data_flags").AsInteger();
                ParticleMaxAge = (float)map.get("particle_max_age").AsReal();
                ParticleStartColor = map.get("particle_start_color").AsColor4();
                ParticleEndColor = map.get("particle_end_color").AsColor4();
                ParticleStartScale = map.get("particle_start_scale").AsVector2();
                ParticleEndScale = map.get("particle_end_scale").AsVector2();
            }
        }

        public class ShapeBlock
        {
            public int PathCurve;
            public float PathBegin;
            public float PathEnd;
            public float PathScaleX;
            public float PathScaleY;
            public float PathShearX;
            public float PathShearY;
            public float PathTwist;
            public float PathTwistBegin;
            public float PathRadiusOffset;
            public float PathTaperX;
            public float PathTaperY;
            public float PathRevolutions;
            public float PathSkew;
            public int ProfileCurve;
            public float ProfileBegin;
            public float ProfileEnd;
            public float ProfileHollow;

            public OSDMap Serialize()
            {
                OSDMap map = new OSDMap();
                map.put("path_curve", OSD.FromInteger(PathCurve));
                map.put("path_begin", OSD.FromReal(PathBegin));
                map.put("path_end", OSD.FromReal(PathEnd));
                map.put("path_scale_x", OSD.FromReal(PathScaleX));
                map.put("path_scale_y", OSD.FromReal(PathScaleY));
                map.put("path_shear_x", OSD.FromReal(PathShearX));
                map.put("path_shear_y", OSD.FromReal(PathShearY));
                map.put("path_twist", OSD.FromReal(PathTwist));
                map.put("path_twist_begin", OSD.FromReal(PathTwistBegin));
                map.put("path_radius_offset", OSD.FromReal(PathRadiusOffset));
                map.put("path_taper_x", OSD.FromReal(PathTaperX));
                map.put("path_taper_y", OSD.FromReal(PathTaperY));
                map.put("path_revolutions", OSD.FromReal(PathRevolutions));
                map.put("path_skew", OSD.FromReal(PathSkew));
                map.put("profile_curve", OSD.FromInteger(ProfileCurve));
                map.put("profile_begin", OSD.FromReal(ProfileBegin));
                map.put("profile_end", OSD.FromReal(ProfileEnd));
                map.put("profile_hollow", OSD.FromReal(ProfileHollow));
                return map;
            }

            public void Deserialize(OSDMap map)
            {
                PathCurve = map.get("path_curve").AsInteger();
                PathBegin = (float)map.get("path_begin").AsReal();
                PathEnd = (float)map.get("path_end").AsReal();
                PathScaleX = (float)map.get("path_scale_x").AsReal();
                PathScaleY = (float)map.get("path_scale_y").AsReal();
                PathShearX = (float)map.get("path_shear_x").AsReal();
                PathShearY = (float)map.get("path_shear_y").AsReal();
                PathTwist = (float)map.get("path_twist").AsReal();
                PathTwistBegin = (float)map.get("path_twist_begin").AsReal();
                PathRadiusOffset = (float)map.get("path_radius_offset").AsReal();
                PathTaperX = (float)map.get("path_taper_x").AsReal();
                PathTaperY = (float)map.get("path_taper_y").AsReal();
                PathRevolutions = (float)map.get("path_revolutions").AsReal();
                PathSkew = (float)map.get("path_skew").AsReal();
                ProfileCurve = map.get("profile_curve").AsInteger();
                ProfileBegin = (float)map.get("profile_begin").AsReal();
                ProfileEnd = (float)map.get("profile_end").AsReal();
                ProfileHollow = (float)map.get("profile_hollow").AsReal();
            }
        }

        public class InventoryBlock
        {
            public class ItemBlock
            {
                public UUID ID;
                public String Name;
                public String OwnerIdentity;
                public String CreatorIdentity;
                public String GroupIdentity;
                public UUID AssetID;
                public String ContentType;
                public String Description;
                public int PermsBase;
                public int PermsOwner;
                public int PermsGroup;
                public int PermsEveryone;
                public int PermsNextOwner;
                public int SalePrice;
                public int SaleType;
                public int Flags;
                public Date CreationDate;

                public OSDMap Serialize()
                {
                    OSDMap map = new OSDMap();
                    map.put("id", OSD.FromUUID(ID));
                    map.put("name", OSD.FromString(Name));
                    map.put("owner_identity", OSD.FromString(OwnerIdentity));
                    map.put("creator_identity", OSD.FromString(CreatorIdentity));
                    map.put("group_identity", OSD.FromString(GroupIdentity));
                    map.put("asset_id", OSD.FromUUID(AssetID));
                    map.put("content_type", OSD.FromString(ContentType));
                    map.put("description", OSD.FromString(Description));
                    map.put("perms_base", OSD.FromInteger(PermsBase));
                    map.put("perms_owner", OSD.FromInteger(PermsOwner));
                    map.put("perms_group", OSD.FromInteger(PermsGroup));
                    map.put("perms_everyone", OSD.FromInteger(PermsEveryone));
                    map.put("perms_next_owner", OSD.FromInteger(PermsNextOwner));
                    map.put("sale_price", OSD.FromInteger(SalePrice));
                    map.put("sale_type", OSD.FromInteger(SaleType));
                    map.put("flags", OSD.FromInteger(Flags));
                    map.put("creation_date", OSD.FromDate(CreationDate));
                    return map;
                }

                public void Deserialize(OSDMap map)
                {
                    ID = map.get("id").AsUUID();
                    Name = map.get("name").AsString();
                    OwnerIdentity = map.get("owner_identity").AsString();
                    CreatorIdentity = map.get("creator_identity").AsString();
                    GroupIdentity = map.get("group_identity").AsString();
                    AssetID = map.get("asset_id").AsUUID();
                    ContentType = map.get("content_type").AsString();
                    Description = map.get("description").AsString();
                    PermsBase = map.get("perms_base").AsInteger();
                    PermsOwner = map.get("perms_owner").AsInteger();
                    PermsGroup = map.get("perms_group").AsInteger();
                    PermsEveryone = map.get("perms_everyone").AsInteger();
                    PermsNextOwner = map.get("perms_next_owner").AsInteger();
                    SalePrice = map.get("sale_price").AsInteger();
                    SaleType = map.get("sale_type").AsInteger();
                    Flags = map.get("flags").AsInteger();
                    CreationDate = map.get("creation_date").AsDate();
                }
            }

            public int Serial;
            public ItemBlock[] Items;

            public OSDMap Serialize()
            {
                OSDMap map = new OSDMap();
                map.put("serial", OSD.FromInteger(Serial));

                if (Items != null)
                {
                    OSDArray array = new OSDArray(Items.length);
                    for (int i = 0; i < Items.length; i++)
                        array.add(Items[i].Serialize());
                    map.put("items", array);
                }

                return map;
            }

            public void Deserialize(OSDMap map)
            {
                Serial = map.get("serial").AsInteger();

                if (map.containsKey("items"))
                {
                    OSDArray array = (OSDArray)map.get("items");
                    Items = new ItemBlock[array.size()];

                    for (int i = 0; i < array.size(); i++)
                    {
                        ItemBlock item = new ItemBlock();
                        item.Deserialize((OSDMap)array.get(i));
                        Items[i] = item;
                    }
                }
                else
                {
                    Items = new ItemBlock[0];
                }
            }
        }

        public UUID ID;
        public boolean AllowedDrop;
        public Vector3 AttachmentPosition;
        public Quaternion AttachmentRotation;
        public Quaternion BeforeAttachmentRotation;
        public String Name;
        public String Description;
        public int PermsBase;
        public int PermsOwner;
        public int PermsGroup;
        public int PermsEveryone;
        public int PermsNextOwner;
        public UUID CreatorID;
        public UUID OwnerID;
        public UUID LastOwnerID;
        public UUID GroupID;
        public UUID FolderID;
        public long RegionHandle;
        public int ClickAction;
        public int LastAttachmentPoint;
        public int LinkNumber;
        public int LocalID;
        public int ParentID;
        public Vector3 Position;
        public Quaternion Rotation;
        public Vector3 Velocity;
        public Vector3 AngularVelocity;
        public Vector3 Acceleration;
        public Vector3 Scale;
        public Vector3 SitOffset;
        public Quaternion SitRotation;
        public Vector3 CameraEyeOffset;
        public Vector3 CameraAtOffset;
        public int State;
        public int PCode;
        public int Material;
        public UUID SoundID;
        public float SoundGain;
        public float SoundRadius;
        public byte SoundFlags;
        public Color4 TextColor;
        public String Text;
        public String SitName;
        public String TouchName;
        public boolean Selected;
        public UUID SelectorID;
        public boolean UsePhysics;
        public boolean Phantom;
        public int RemoteScriptAccessPIN;
        public boolean VolumeDetect;
        public boolean DieAtEdge;
        public boolean ReturnAtEdge;
        public boolean Temporary;
        public boolean Sandbox;
        public Date CreationDate;
        public Date RezDate;
        public int SalePrice;
        public int SaleType;
        public byte[] ScriptState;
        public UUID CollisionSound;
        public float CollisionSoundVolume;
        public FlexibleBlock Flexible;
        public LightBlock Light;
        public SculptBlock Sculpt;
        public ParticlesBlock Particles;
        public ShapeBlock Shape;
        public TextureEntry Textures;
        public InventoryBlock Inventory;

        public OSDMap Serialize()
        {
            OSDMap map = new OSDMap();
            map.put("id", OSD.FromUUID(ID));
            map.put("attachment_position", OSD.FromVector3(AttachmentPosition));
            map.put("attachment_rotation", OSD.FromQuaternion(AttachmentRotation));
            map.put("before_attachment_rotation", OSD.FromQuaternion(BeforeAttachmentRotation));
            map.put("name", OSD.FromString(Name));
            map.put("description", OSD.FromString(Description));
            map.put("perms_base", OSD.FromInteger(PermsBase));
            map.put("perms_owner", OSD.FromInteger(PermsOwner));
            map.put("perms_group", OSD.FromInteger(PermsGroup));
            map.put("perms_everyone", OSD.FromInteger(PermsEveryone));
            map.put("perms_next_owner", OSD.FromInteger(PermsNextOwner));
            map.put("creator_identity", OSD.FromUUID(CreatorID));
            map.put("owner_identity", OSD.FromUUID(OwnerID));
            map.put("last_owner_identity", OSD.FromUUID(LastOwnerID));
            map.put("group_identity", OSD.FromUUID(GroupID));
            map.put("folder_id", OSD.FromUUID(FolderID));
            map.put("region_handle", OSD.FromULong(RegionHandle));
            map.put("click_action", OSD.FromInteger(ClickAction));
            map.put("last_attachment_point", OSD.FromInteger(LastAttachmentPoint));
            map.put("link_number", OSD.FromInteger(LinkNumber));
            map.put("local_id", OSD.FromInteger(LocalID));
            map.put("parent_id", OSD.FromInteger(ParentID));
            map.put("position", OSD.FromVector3(Position));
            map.put("rotation", OSD.FromQuaternion(Rotation));
            map.put("velocity", OSD.FromVector3(Velocity));
            map.put("angular_velocity", OSD.FromVector3(AngularVelocity));
            map.put("acceleration", OSD.FromVector3(Acceleration));
            map.put("scale", OSD.FromVector3(Scale));
            map.put("sit_offset", OSD.FromVector3(SitOffset));
            map.put("sit_rotation", OSD.FromQuaternion(SitRotation));
            map.put("camera_eye_offset", OSD.FromVector3(CameraEyeOffset));
            map.put("camera_at_offset", OSD.FromVector3(CameraAtOffset));
            map.put("state", OSD.FromInteger(State));
            map.put("prim_code", OSD.FromInteger(PCode));
            map.put("material", OSD.FromInteger(Material));
            map.put("sound_id", OSD.FromUUID(SoundID));
            map.put("sound_gain", OSD.FromReal(SoundGain));
            map.put("sound_radius", OSD.FromReal(SoundRadius));
            map.put("sound_flags", OSD.FromInteger(SoundFlags));
            map.put("text_color", OSD.FromColor4(TextColor));
            map.put("text", OSD.FromString(Text));
            map.put("sit_name", OSD.FromString(SitName));
            map.put("touch_name", OSD.FromString(TouchName));
            map.put("selected", OSD.FromBoolean(Selected));
            map.put("selector_id", OSD.FromUUID(SelectorID));
            map.put("use_physics", OSD.FromBoolean(UsePhysics));
            map.put("phantom", OSD.FromBoolean(Phantom));
            map.put("remote_script_access_pin", OSD.FromInteger(RemoteScriptAccessPIN));
            map.put("volume_detect", OSD.FromBoolean(VolumeDetect));
            map.put("die_at_edge", OSD.FromBoolean(DieAtEdge));
            map.put("return_at_edge", OSD.FromBoolean(ReturnAtEdge));
            map.put("temporary", OSD.FromBoolean(Temporary));
            map.put("sandbox", OSD.FromBoolean(Sandbox));
            map.put("creation_date", OSD.FromDate(CreationDate));
            map.put("rez_date", OSD.FromDate(RezDate));
            map.put("sale_price", OSD.FromInteger(SalePrice));
            map.put("sale_type", OSD.FromInteger(SaleType));

            if (Flexible != null)
                map.put("flexible", Flexible.Serialize());
            if (Light != null)
                map.put("light", Light.Serialize());
            if (Sculpt != null)
                map.put("sculpt", Sculpt.Serialize());
            if (Particles != null)
                map.put("particles", Particles.Serialize());
            if (Shape != null)
                map.put("shape", Shape.Serialize());
            if (Textures != null)
                map.put("textures", Textures.Serialize());
            if (Inventory != null)
                map.put("inventory", Inventory.Serialize());

            return map;
        }

        public void Deserialize(OSDMap map)
        {
            ID = map.get("id").AsUUID();
            AttachmentPosition = map.get("attachment_position").AsVector3();
            AttachmentRotation = map.get("attachment_rotation").AsQuaternion();
            BeforeAttachmentRotation = map.get("before_attachment_rotation").AsQuaternion();
            Name = map.get("name").AsString();
            Description = map.get("description").AsString();
            PermsBase = map.get("perms_base").AsInteger();
            PermsOwner = map.get("perms_owner").AsInteger();
            PermsGroup = map.get("perms_group").AsInteger();
            PermsEveryone = map.get("perms_everyone").AsInteger();
            PermsNextOwner = map.get("perms_next_owner").AsInteger();
            CreatorID = map.get("creator_identity").AsUUID();
            OwnerID = map.get("owner_identity").AsUUID();
            LastOwnerID = map.get("last_owner_identity").AsUUID();
            GroupID = map.get("group_identity").AsUUID();
            FolderID = map.get("folder_id").AsUUID();
            RegionHandle = map.get("region_handle").AsULong();
            ClickAction = map.get("click_action").AsInteger();
            LastAttachmentPoint = map.get("last_attachment_point").AsInteger();
            LinkNumber = map.get("link_number").AsInteger();
            LocalID = map.get("local_id").AsInteger();
            ParentID = map.get("parent_id").AsInteger();
            Position = map.get("position").AsVector3();
            Rotation = map.get("rotation").AsQuaternion();
            Velocity = map.get("velocity").AsVector3();
            AngularVelocity = map.get("angular_velocity").AsVector3();
            Acceleration = map.get("acceleration").AsVector3();
            Scale = map.get("scale").AsVector3();
            SitOffset = map.get("sit_offset").AsVector3();
            SitRotation = map.get("sit_rotation").AsQuaternion();
            CameraEyeOffset = map.get("camera_eye_offset").AsVector3();
            CameraAtOffset = map.get("camera_at_offset").AsVector3();
            State = map.get("state").AsInteger();
            PCode = map.get("prim_code").AsInteger();
            Material = map.get("material").AsInteger();
            SoundID = map.get("sound_id").AsUUID();
            SoundGain = (float)map.get("sound_gain").AsReal();
            SoundRadius = (float)map.get("sound_radius").AsReal();
            SoundFlags = (byte) map.get("sound_flags").AsInteger();
            TextColor = map.get("text_color").AsColor4();
            Text = map.get("text").AsString();
            SitName = map.get("sit_name").AsString();
            TouchName = map.get("touch_name").AsString();
            Selected = map.get("selected").AsBoolean();
            SelectorID = map.get("selector_id").AsUUID();
            UsePhysics = map.get("use_physics").AsBoolean();
            Phantom = map.get("phantom").AsBoolean();
            RemoteScriptAccessPIN = map.get("remote_script_access_pin").AsInteger();
            VolumeDetect = map.get("volume_detect").AsBoolean();
            DieAtEdge = map.get("die_at_edge").AsBoolean();
            ReturnAtEdge = map.get("return_at_edge").AsBoolean();
            Temporary = map.get("temporary").AsBoolean();
            Sandbox = map.get("sandbox").AsBoolean();
            CreationDate = map.get("creation_date").AsDate();
            RezDate = map.get("rez_date").AsDate();
            SalePrice = map.get("sale_price").AsInteger();
            SaleType = map.get("sale_type").AsInteger();
        }

        public Primitive ToPrimitive()
        {
            Primitive prim = new Primitive();
            prim.Properties = new ObjectProperties();

            prim.Acceleration = this.Acceleration;
            prim.AngularVelocity = this.AngularVelocity;
            prim.clickAction = Primitive.ClickAction.setValue(this.ClickAction);
            prim.Properties.CreationDate = this.CreationDate;
            prim.Properties.CreatorID = this.CreatorID;
            prim.Properties.Description = this.Description;
            if (this.DieAtEdge)
                prim.Flags |= PrimFlags.DieAtEdge;
            prim.Properties.FolderID = this.FolderID;
            prim.Properties.GroupID = this.GroupID;
            prim.ID = this.ID;
            prim.Properties.LastOwnerID = this.LastOwnerID;
            prim.LocalID = this.LocalID;
            prim.PrimData.Material = Primitive.Material.setValue(this.Material);
            prim.Properties.Name = this.Name;
            prim.OwnerID = this.OwnerID;
            prim.ParentID = this.ParentID;
            prim.PrimData.PCode = Primitive.PCode.setValue(this.PCode);
            prim.Properties.Permissions = new Permissions(this.PermsBase, this.PermsEveryone, this.PermsGroup, this.PermsNextOwner, this.PermsOwner);
            if (this.Phantom)
                prim.Flags |= PrimFlags.Phantom;
            prim.Position = this.Position;
            if (this.ReturnAtEdge)
                prim.Flags |= PrimFlags.ReturnAtEdge;
            prim.Rotation = this.Rotation;
            prim.Properties.SalePrice = this.SalePrice;
            prim.Properties.SaleType = ObjectManager.SaleType.setValue(this.SaleType);
            if (this.Sandbox)
                prim.Flags |= PrimFlags.Sandbox;
            prim.Scale = this.Scale;
            prim.SoundFlags = this.SoundFlags;
            prim.SoundGain = this.SoundGain;
            prim.SoundID = this.SoundID;
            prim.SoundRadius = this.SoundRadius;
            prim.PrimData.State = (byte)this.State;
            if (this.Temporary)
                prim.Flags |= PrimFlags.Temporary;
            prim.Text = this.Text;
            prim.TextColor = this.TextColor;
            prim.Textures = this.Textures;
            if (this.UsePhysics)
                prim.Flags |= PrimFlags.Physics;
            prim.Velocity = this.Velocity;

            prim.PrimData.PathBegin = this.Shape.PathBegin;
            prim.PrimData.PathCurve = PathCurve.setValue(this.Shape.PathCurve);
            prim.PrimData.PathEnd = this.Shape.PathEnd;
            prim.PrimData.PathRadiusOffset = this.Shape.PathRadiusOffset;
            prim.PrimData.PathRevolutions = this.Shape.PathRevolutions;
            prim.PrimData.PathScaleX = this.Shape.PathScaleX;
            prim.PrimData.PathScaleY = this.Shape.PathScaleY;
            prim.PrimData.PathShearX = this.Shape.PathShearX;
            prim.PrimData.PathShearY = this.Shape.PathShearY;
            prim.PrimData.PathSkew = this.Shape.PathSkew;
            prim.PrimData.PathTaperX = this.Shape.PathTaperX;
            prim.PrimData.PathTaperY = this.Shape.PathTaperY;
            prim.PrimData.PathTwist = this.Shape.PathTwist;
            prim.PrimData.PathTwistBegin = this.Shape.PathTwistBegin;
            prim.PrimData.ProfileBegin = this.Shape.ProfileBegin;
            prim.PrimData.ProfileCurve = ProfileCurve.setValue(this.Shape.ProfileCurve);
            prim.PrimData.ProfileEnd = this.Shape.ProfileEnd;
            prim.PrimData.ProfileHollow = this.Shape.ProfileHollow;

            if (this.Flexible != null)
            {
                prim.Flexible = prim.new FlexibleData();
                prim.Flexible.Drag = this.Flexible.Drag;
                prim.Flexible.Force = this.Flexible.Force;
                prim.Flexible.Gravity = this.Flexible.Gravity;
                prim.Flexible.Softness = this.Flexible.Softness;
                prim.Flexible.Tension = this.Flexible.Tension;
                prim.Flexible.Wind = this.Flexible.Wind;
            }

            if (this.Light != null)
            {
                prim.Light = prim.new LightData();
                prim.Light.Color = this.Light.Color;
                prim.Light.Cutoff = this.Light.Cutoff;
                prim.Light.Falloff = this.Light.Falloff;
                prim.Light.Intensity = this.Light.Intensity;
                prim.Light.Radius = this.Light.Radius;
            }

            if (this.Particles != null)
            {
                prim.ParticleSys = new ParticleSystem();
                prim.ParticleSys.AngularVelocity = this.Particles.AngularVelocity;
                prim.ParticleSys.PartAcceleration = this.Particles.Acceleration;
                prim.ParticleSys.BurstPartCount = (byte)this.Particles.BurstParticleCount;
                prim.ParticleSys.BurstRate = this.Particles.BurstRadius;
                prim.ParticleSys.BurstRate = this.Particles.BurstRate;
                prim.ParticleSys.BurstSpeedMax = this.Particles.BurstSpeedMax;
                prim.ParticleSys.BurstSpeedMin = this.Particles.BurstSpeedMin;
                prim.ParticleSys.PartDataFlags = this.Particles.DataFlags;
                prim.ParticleSys.PartFlags = this.Particles.Flags;
                prim.ParticleSys.InnerAngle = this.Particles.InnerAngle;
                prim.ParticleSys.MaxAge = this.Particles.MaxAge;
                prim.ParticleSys.OuterAngle = this.Particles.OuterAngle;
                prim.ParticleSys.PartEndColor = this.Particles.ParticleEndColor;
                prim.ParticleSys.PartEndScaleX = this.Particles.ParticleEndScale.X;
                prim.ParticleSys.PartEndScaleY = this.Particles.ParticleEndScale.Y;
                prim.ParticleSys.MaxAge = this.Particles.ParticleMaxAge;
                prim.ParticleSys.PartStartColor = this.Particles.ParticleStartColor;
                prim.ParticleSys.PartStartScaleX = this.Particles.ParticleStartScale.X;
                prim.ParticleSys.PartStartScaleY = this.Particles.ParticleStartScale.Y;
                prim.ParticleSys.Pattern = SourcePattern.setValue(this.Particles.Pattern);
                prim.ParticleSys.StartAge = this.Particles.StartAge;
                prim.ParticleSys.Target = this.Particles.TargetID;
                prim.ParticleSys.Texture = this.Particles.TextureID;
            }

            if (this.Sculpt != null)
            {
                prim.Sculpt = prim.new SculptData();
                prim.Sculpt.SculptTexture = this.Sculpt.Texture;
                prim.Sculpt.setType(this.Sculpt.Type);
            }

            return prim;
        }
    }
}