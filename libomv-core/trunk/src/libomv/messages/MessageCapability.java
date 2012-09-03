package libomv.messages;

import java.util.Date;

import libomv.GridClient;
import libomv.types.Quaternion;
import libomv.types.UUID;
import libomv.types.Vector3;

public class MessageCapability implements MessageInterface
{
	private GridClient _Client;
	
	public MessageCapability(GridClient client)
	{
		_Client = client;
	}

	@Override
	public int getNumberOfBlocks(String blockName)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public char getMessageI8(String blockName, String fieldName, int blockNumber)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short getMessageI16(String blockName, String fieldName, int blockNumber)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMessageI32(String blockName, String fieldName, int blockNumber)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getMessageI64(String blockName, String fieldName, int blockNumber)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getMessageF32(String blockName, String fieldName, int blockNumber)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMessageF64(String blockName, String fieldName, int blockNumber)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMessageString(String blockName, String fieldName, int blockNumber)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getMessageUUID(String blockName, String fieldName, int blockNumber)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getMessageDate(String blockName, String fieldName, int blockNumber)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector3 getMessageVector3(String blockName, String fieldName, int blockNumber)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Quaternion getMessageQuaternion(String blockName, String fieldName, int blockNumber)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfBlocks(int blockName)
	{
		return getNumberOfBlocks(_Client.Protocol.keywordPosition(blockName));
	}

	@Override
	public char getMessageI8(int blockName, int fieldName, int blockNumber)
	{
		return getMessageI8(_Client.Protocol.keywordPosition(blockName), _Client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public short getMessageI16(int blockName, int fieldName, int blockNumber)
	{
		return getMessageI16(_Client.Protocol.keywordPosition(blockName), _Client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public int getMessageI32(int blockName, int fieldName, int blockNumber)
	{
		return getMessageI32(_Client.Protocol.keywordPosition(blockName), _Client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public long getMessageI64(int blockName, int fieldName, int blockNumber)
	{
		return getMessageI64(_Client.Protocol.keywordPosition(blockName), _Client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public float getMessageF32(int blockName, int fieldName, int blockNumber)
	{
		return getMessageF32(_Client.Protocol.keywordPosition(blockName), _Client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public double getMessageF64(int blockName, int fieldName, int blockNumber)
	{
		return getMessageF64(_Client.Protocol.keywordPosition(blockName), _Client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public String getMessageString(int blockName, int fieldName, int blockNumber)
	{
		return getMessageString(_Client.Protocol.keywordPosition(blockName), _Client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public UUID getMessageUUID(int blockName, int fieldName, int blockNumber)
	{
		return getMessageUUID(_Client.Protocol.keywordPosition(blockName), _Client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public Date getMessageDate(int blockName, int fieldName, int blockNumber)
	{
		return getMessageDate(_Client.Protocol.keywordPosition(blockName), _Client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public Vector3 getMessageVector3(int blockName, int fieldName, int blockNumber)
	{
		return getMessageVector3(_Client.Protocol.keywordPosition(blockName), _Client.Protocol.keywordPosition(fieldName), blockNumber);
	}

	@Override
	public Quaternion getMessageQuaternion(int blockName, int fieldName, int blockNumber)
	{
		return getMessageQuaternion(_Client.Protocol.keywordPosition(blockName), _Client.Protocol.keywordPosition(fieldName), blockNumber);
	}
}
