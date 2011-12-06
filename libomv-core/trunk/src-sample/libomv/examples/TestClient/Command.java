package libomv.examples.TestClient;

import libomv.types.UUID;

public abstract class Command implements Comparable<Object>
{
    public enum CommandCategory
    {
        Parcel,
        Appearance,
        Movement,
        Simulator,
        Communication,
        Inventory,
        Objects,
        Voice,
        TestClient,
        Friends,
        Groups,
        Other,
        Unknown,
        Search
    }

	public String Name;
	public String Description;
    public CommandCategory Category;

	protected TestClient Client;

	public abstract String Execute(String[] args, UUID fromAgentID) throws Exception;

	/// <summary>
	/// When set to true, think will be called.
	/// </summary>
	public boolean Active;

	/// <summary>
	/// Called twice per second, when Command.Active is set to true.
	/// </summary>
	public void Think()
	{
		
	}

    @Override
	public int compareTo(Object obj)
    {
        if (obj instanceof Command)
        {
            Command c2 = (Command)obj;
            return Category.compareTo(c2.Category);
        }
		throw new IllegalArgumentException("Object is not of type Command.");
    }
}
