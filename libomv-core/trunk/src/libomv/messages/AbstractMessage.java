package libomv.messages;

import java.util.Date;

import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.types.Vector4;

public abstract class AbstractMessage
{
	public abstract int getNumberOfBlocks(String blockName);
	public abstract byte getMessageI8(String blockName, String fieldName, int blockNumber) throws Exception;
	public abstract short getMessageU8(String blockName, String fieldName, int blockNumber) throws Exception;
	public abstract short getMessageI16(String blockName, String fieldName, int blockNumber) throws Exception;
	public abstract int getMessageU16(String blockName, String fieldName, int blockNumber) throws Exception;
	public abstract int getMessageI32(String blockName, String fieldName, int blockNumber) throws Exception;
	public abstract long getMessageU32(String blockName, String fieldName, int blockNumber) throws Exception;
	public abstract long getMessageI64(String blockName, String fieldName, int blockNumber) throws Exception;
	public abstract long getMessageU64(String blockName, String fieldName, int blockNumber) throws Exception;
	public abstract float getMessageF32(String blockName, String fieldName, int blockNumber) throws Exception;
	public abstract double getMessageF64(String blockName, String fieldName, int blockNumber) throws Exception;
	public abstract String getMessageString(String blockName, String fieldName, int blockNumber) throws Exception;
	public abstract UUID getMessageUUID(String blockName, String fieldName, int blockNumber) throws Exception;
	public abstract Date getMessageDate(String blockName, String fieldName, int blockNumber) throws Exception;
	public abstract Vector3 getMessageVector3(String blockName, String fieldName, int blockNumber) throws Exception;
	public abstract Vector3d getMessageVector3d(String blockName, String fieldName, int blockNumber) throws Exception;
	public abstract Vector4 getMessageVector4(String blockName, String fieldName, int blockNumber) throws Exception;
	public abstract Quaternion getMessageQuaternion(String blockName, String fieldName, int blockNumber) throws Exception;

	public abstract int getNumberOfBlocks(int blockName);
	public abstract byte getMessageI8(int blockName, int fieldName, int blockNumber) throws Exception;
	public abstract short getMessageU8(int blockName, int fieldName, int blockNumber) throws Exception;
	public abstract short getMessageI16(int blockName, int fieldName, int blockNumber) throws Exception;
	public abstract int getMessageU16(int blockName, int fieldName, int blockNumber) throws Exception;
	public abstract int getMessageI32(int blockName, int fieldName, int blockNumber) throws Exception;
	public abstract long getMessageU32(int blockName, int fieldName, int blockNumber) throws Exception;
	public abstract long getMessageI64(int blockName, int fieldName, int blockNumber) throws Exception;
	public abstract long getMessageU64(int blockName, int fieldName, int blockNumber) throws Exception;
	public abstract float getMessageF32(int blockName, int fieldName, int blockNumber) throws Exception;
	public abstract double getMessageF64(int blockName, int fieldName, int blockNumber) throws Exception;
	public abstract String getMessageString(int blockName, int fieldName, int blockNumber) throws Exception;
	public abstract UUID getMessageUUID(int blockName, int fieldName, int blockNumber) throws Exception;
	public abstract Date getMessageDate(int blockName, int fieldName, int blockNumber) throws Exception;
	public abstract Vector3 getMessageVector3(int blockName, int fieldName, int blockNumber) throws Exception;
	public abstract Vector3d getMessageVector3d(int blockName, int fieldName, int blockNumber) throws Exception;
	public abstract Vector4 getMessageVector4(int blockName, int fieldName, int blockNumber) throws Exception;
	public abstract Quaternion getMessageQuaternion(int blockName, int fieldName, int blockNumber) throws Exception;
}
