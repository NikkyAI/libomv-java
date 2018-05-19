package libomv.model.object;

import libomv.model.Simulator;
import libomv.primitives.Avatar;
import libomv.utils.CallbackArgs;

/**
 * Provides data for the <see cref="ObjectManager.OnAvatarUpdate"/> event
 * <p>
 * The <see cref="ObjectManager.OnAvatarUpdate"/> event occurs when the
 * simulator sends an <see cref="ObjectUpdatePacket"/> containing Avatar data
 * </p>
 * <p>
 * Note 1: The <see cref="ObjectManager.OnAvatarUpdate"/> event will not be
 * raised when the object is an Avatar
 * </p>
 * <p>
 * Note 2: It is possible for the <see cref="ObjectManager.OnAvatarUpdate"/> to
 * be raised twice for the same avatar if for example the avatar moved to a new
 * simulator, then returned to the current simulator
 * </p>
 *
 * <example> The following code example uses the
 * <see cref="AvatarUpdateCallbackArgs.Avatar"/> property to make a request for
 * the top picks using the <see cref="AvatarManager.RequestAvatarPicks"/> method
 * in the <see cref="AvatarManager"/> class to display the names of our own
 * agents picks listings on the <see cref="Console"/> window. <code>
 *     // subscribe to the OnAvatarUpdate event to get our information
 *
 *     CallbackHandler<AvatarUpdateCallbackArgs> cbu = new Objects_AvatarUpdate();
 *     CallbackHandler<AvatarPicksReplyCallbackArgs> cba = new Objects_AvatarPicksReply();
 *     _Client.Objects.OnAvatarUpdate.add(cbu, false);
 *     _Client.Avatars.OnAvatarPicksReply.add(cba, true);
 *
 *     private class Objects_AvatarUpdate implements CallbackHandler<AvatarUpdateCallbackArgs>
 *     {
 *     	   public void callback(AvatarUpdateCallbackArgs e)
 *         {
 *             // we only want our own data
 *             if (e.Avatar.LocalID == _Client.Self.LocalID)
 *             {
 *                 // Unsubscribe from the avatar update event to prevent a loop
 *                 // where we continually request the picks every time we get an update for ourselves
 *                 _Client.Objects.OnAvatarUpdate.remove(cbu);
 *                 // make the top picks request through AvatarManager
 *                 _Client.Avatars.RequestAvatarPicks(e.Avatar.ID);
 *             }
 *         }
 *     }
 *
 *     private class Avatars_AvatarPicksReply implements CallbackHandler<AvatarPicksReplyCallbackArgs>
 *     {
 *         public void callback(AvatarPicksReplyCallbackArgs e)
 *         {
 *             // we'll unsubscribe from the AvatarPicksReply event since we now have the data
 *             // we were looking for
 *             _Client.Avatars.AvatarPicksReply.remove(cba);
 *             // loop through the dictionary and extract the names of the top picks from our profile
 *             for (String pickName : e.Picks.Values)
 *             {
 *                 Console.WriteLine(pickName);
 *             }
 *         }
 *     }
 * </code> </example> {@link ObjectManager.OnObjectUpdate} {@link PrimEventArgs}
 */
public class AvatarUpdateCallbackArgs implements CallbackArgs {
	private final Simulator simulator;
	private final Avatar avatar;
	private final short timeDilation;
	private final boolean isNew;

	/**
	 * Construct a new instance of the AvatarUpdateEventArgs class
	 *
	 * @param simulator
	 *            The simulator the packet originated from
	 * @param avatar
	 *            The <see cref="Avatar"/> data
	 * @param timeDilation
	 *            The simulator time dilation
	 * @param isNew
	 *            The avatar was not in the dictionary before this update
	 */
	public AvatarUpdateCallbackArgs(Simulator simulator, Avatar avatar, short timeDilation, boolean isNew) {
		this.simulator = simulator;
		this.avatar = avatar;
		this.timeDilation = timeDilation;
		this.isNew = isNew;
	}

	// Get the simulator the object originated from
	public final Simulator getSimulator() {
		return simulator;
	}

	// Get the <see cref="Avatar"/> data
	public final Avatar getAvatar() {
		return avatar;
	}

	// Get the simulator time dilation
	public final short getTimeDilation() {
		return timeDilation;
	}

	// true if the <see cref="Avatar"/> did not exist in the dictionary
	// before this update (always true if avatar tracking has been disabled)
	public final boolean getIsNew() {
		return isNew;
	}

}