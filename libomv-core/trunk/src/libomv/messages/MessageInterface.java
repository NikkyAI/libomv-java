package libomv.messages;

import java.util.Date;

import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;

public interface MessageInterface
{
	public int getNumberOfBlocks(String blockName);
	public char getMessageI8(String blockName, String fieldName, int blockNumber);
	public short getMessageI16(String blockName, String fieldName, int blockNumber);
	public int getMessageI32(String blockName, String fieldName, int blockNumber);
	public long getMessageI64(String blockName, String fieldName, int blockNumber);
	public float getMessageF32(String blockName, String fieldName, int blockNumber);
	public double getMessageF64(String blockName, String fieldName, int blockNumber);
	public String getMessageString(String blockName, String fieldName, int blockNumber);
	public UUID getMessageUUID(String blockName, String fieldName, int blockNumber);
	public Date getMessageDate(String blockName, String fieldName, int blockNumber);
	public Vector3 getMessageVector3(String blockName, String fieldName, int blockNumber);
	public Quaternion getMessageQuaternion(String blockName, String fieldName, int blockNumber);

	public int getNumberOfBlocks(int blockName);
	public char getMessageI8(int blockName, int fieldName, int blockNumber);
	public short getMessageI16(int blockName, int fieldName, int blockNumber);
	public int getMessageI32(int blockName, int fieldName, int blockNumber);
	public long getMessageI64(int blockName, int fieldName, int blockNumber);
	public float getMessageF32(int blockName, int fieldName, int blockNumber);
	public double getMessageF64(int blockName, int fieldName, int blockNumber);
	public String getMessageString(int blockName, int fieldName, int blockNumber);
	public UUID getMessageUUID(int blockName, int fieldName, int blockNumber);
	public Date getMessageDate(int blockName, int fieldName, int blockNumber);
	public Vector3 getMessageVector3(int blockName, int fieldName, int blockNumber);
	public Quaternion getMessageQuaternion(int blockName, int fieldName, int blockNumber);
}
