package libomv.messages;

import java.util.Date;

import libomv.GridClient;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.types.Vector3d;
import libomv.types.Vector4;

public class MessageCapability extends AbstractMessage
{
	private GridClient client;
	
	public MessageCapability(GridClient client)
	{
		this.client = client;
	}

	@Override
	public int getNumberOfBlocks(String blockName)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte getMessageI8(String blockName, String fieldName, int blockNumber) throws Exception
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short getMessageU8(String blockName, String fieldName, int blockNumber) throws Exception
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short getMessageI16(String blockName, String fieldName, int blockNumber) throws Exception
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMessageU16(String blockName, String fieldName, int blockNumber) throws Exception
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMessageI32(String blockName, String fieldName, int blockNumber) throws Exception
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getMessageU32(String blockName, String fieldName, int blockNumber) throws Exception
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getMessageI64(String blockName, String fieldName, int blockNumber) throws Exception
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getMessageU64(String blockName, String fieldName, int blockNumber) throws Exception
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getMessageF32(String blockName, String fieldName, int blockNumber) throws Exception
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMessageF64(String blockName, String fieldName, int blockNumber) throws Exception
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMessageString(String blockName, String fieldName, int blockNumber) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getMessageUUID(String blockName, String fieldName, int blockNumber) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getMessageDate(String blockName, String fieldName, int blockNumber) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector3 getMessageVector3(String blockName, String fieldName, int blockNumber) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector3d getMessageVector3d(String blockName, String fieldName, int blockNumber) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector4 getMessageVector4(String blockName, String fieldName, int blockNumber) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Quaternion getMessageQuaternion(String blockName, String fieldName, int blockNumber) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfBlocks(int blockName)
	{
		return getNumberOfBlocks(client.Protocol.keywordPosition(blockName));
	}

	@Override
	public byte getMessageI8(int blockName, int fieldName, int blockNumber) throws Exception
	{
		return getMessageI8(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public short getMessageU8(int blockName, int fieldName, int blockNumber) throws Exception
	{
		return getMessageU8(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public short getMessageI16(int blockName, int fieldName, int blockNumber) throws Exception
	{
		return getMessageI16(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public int getMessageU16(int blockName, int fieldName, int blockNumber) throws Exception
	{
		return getMessageU16(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public int getMessageI32(int blockName, int fieldName, int blockNumber) throws Exception
	{
		return getMessageI32(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public long getMessageU32(int blockName, int fieldName, int blockNumber) throws Exception
	{
		return getMessageU32(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public long getMessageI64(int blockName, int fieldName, int blockNumber) throws Exception
	{
		return getMessageI64(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public long getMessageU64(int blockName, int fieldName, int blockNumber) throws Exception
	{
		return getMessageU64(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public float getMessageF32(int blockName, int fieldName, int blockNumber) throws Exception
	{
		return getMessageF32(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public double getMessageF64(int blockName, int fieldName, int blockNumber) throws Exception
	{
		return getMessageF64(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public String getMessageString(int blockName, int fieldName, int blockNumber) throws Exception
	{
		return getMessageString(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public UUID getMessageUUID(int blockName, int fieldName, int blockNumber) throws Exception
	{
		return getMessageUUID(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public Date getMessageDate(int blockName, int fieldName, int blockNumber) throws Exception
	{
		return getMessageDate(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public Vector3 getMessageVector3(int blockName, int fieldName, int blockNumber) throws Exception
	{
		return getMessageVector3(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(fieldName), blockNumber);
	}
	@Override
	public Vector3d getMessageVector3d(int blockName, int fieldName, int blockNumber) throws Exception
	{
		return getMessageVector3d(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public Vector4 getMessageVector4(int blockName, int fieldName, int blockNumber) throws Exception
	{
		return getMessageVector4(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public Quaternion getMessageQuaternion(int blockName, int fieldName, int blockNumber) throws Exception
	{
		return getMessageQuaternion(client.Protocol.keywordPosition(blockName), client.Protocol.keywordPosition(fieldName), blockNumber);
	}
}
