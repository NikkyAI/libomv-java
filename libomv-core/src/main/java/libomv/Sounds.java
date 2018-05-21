package libomv;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import libomv.types.UUID;

// pre-defined built in sounds
// From http://wiki.secondlife.com/wiki/Client_sounds
public class Sounds {
	public final static UUID BELL_TING = new UUID("ed124764-705d-d497-167a-182cd9fa2e6c");

	public final static UUID CLICK = new UUID("4c8c3c77-de8d-bde2-b9b8-32635e0fd4a6");

	public final static UUID HEALTH_REDUCTION_FEMALE = new UUID("219c5d93-6c09-31c5-fb3f-c5fe7495c115");

	public final static UUID HEALTH_REDUCTION_MALE = new UUID("e057c244-5768-1056-c37e-1537454eeb62");

	public final static UUID IM_START = new UUID("c825dfbc-9827-7e02-6507-3713d18916c1");

	// 2 bells
	public final static UUID INSTANT_MESSAGE_NOTIFICATION = new UUID("67cc2844-00f3-2b3c-b991-6418d01e1bb7");

	public final static UUID INVALID_OPERATION = new UUID("4174f859-0d3d-c517-c424-72923dc21f65");

	public final static UUID KEYBOARD_LOOP = new UUID("5e191c7b-8996-9ced-a177-b2ac32bfea06");

	// coins
	public final static UUID MONEY_REDUCTION_COINS = new UUID("77a018af-098e-c037-51a6-178f05877c6f");

	// cash register bell
	public final static UUID MONEY_INCREASE_CASH_REGISTER_BELL = new UUID("104974e3-dfda-428b-99ee-b0d4e748d3a3");

	public final static UUID NULL_KEYSTROKE = new UUID("2ca849ba-2885-4bc3-90ef-d4987a5b983a");

	public final static UUID OBJECT_COLLISION = new UUID("be582e5d-b123-41a2-a150-454c39e961c8");

	// rubber
	public final static UUID OBJECT_COLLISION_RUBBER = new UUID("212b6d1e-8d9c-4986-b3aa-f3c6df8d987d");

	// plastic
	public final static UUID OBJECT_COLLISION_PLASTIC = new UUID("d55c7f3c-e1c3-4ddc-9eff-9ef805d9190e");

	// flesh
	public final static UUID OBJECT_COLLISION_FLESH = new UUID("2d8c6f51-149e-4e23-8413-93a379b42b67");

	// wood splintering?
	public final static UUID OBJECT_COLLISION_WOOD_SPLINTERING = new UUID("6f00669f-15e0-4793-a63e-c03f62fee43a");

	// glass break
	public final static UUID OBJECT_COLLISION_GLASS_BREAK = new UUID("85cda060-b393-48e6-81c8-2cfdfb275351");

	// metal clunk
	public final static UUID OBJECT_COLLISION_METAL_CLUNK = new UUID("d1375446-1c4d-470b-9135-30132433b678");

	// whoosh
	public final static UUID OBJECT_CREATE_WHOOSH = new UUID("3c8fc726-1fd6-862d-fa01-16c5b2568db6");

	// shake
	public final static UUID OBJECT_DELETE_SHAKE = new UUID("0cb7b00a-4c10-6948-84de-a93c09af2ba9");

	public final static UUID OBJECT_REZ = new UUID("f4a0660f-5446-dea2-80b7-6482a082803c");

	// ding
	public final static UUID PIE_MENU_APPEAR_DING = new UUID("8eaed61f-92ff-6485-de83-4dcc938a478e");

	public final static UUID PIE_MENU_SLICE_HIGHLIGHT = new UUID("d9f73cf8-17b4-6f7a-1565-7951226c305d");

	public final static UUID PIE_MENU_SLICE_HIGHLIGHT1 = new UUID("f6ba9816-dcaf-f755-7b67-51b31b6233e5");

	public final static UUID PIE_MENU_SLICE_HIGHLIGHT2 = new UUID("7aff2265-d05b-8b72-63c7-dbf96dc2f21f");

	public final static UUID PIE_MENU_SLICE_HIGHLIGHT3 = new UUID("09b2184e-8601-44e2-afbb-ce37434b8ba1");

	public final static UUID PIE_MENU_SLICE_HIGHLIGHT4 = new UUID("bbe4c7fc-7044-b05e-7b89-36924a67593c");

	public final static UUID PIE_MENU_SLICE_HIGHLIGHT5 = new UUID("d166039b-b4f5-c2ec-4911-c85c727b016c");

	public final static UUID PIE_MENU_SLICE_HIGHLIGHT6 = new UUID("242af82b-43c2-9a3b-e108-3b0c7e384981");

	public final static UUID PIE_MENU_SLICE_HIGHLIGHT7 = new UUID("c1f334fb-a5be-8fe7-22b3-29631c21cf0b");

	public final static UUID SNAPSHOT = new UUID("3d09f582-3851-c0e0-f5ba-277ac5c73fb4");

	public final static UUID TELEPORT_TEXTURE_APPLY = new UUID("d7a9a565-a013-2a69-797d-5332baa1a947");

	public final static UUID THUNDER = new UUID("e95c96a5-293c-bb7a-57ad-ce2e785ad85f");

	public final static UUID WINDOW_CLOSE = new UUID("2c346eda-b60c-ab33-1119-b8941916a499");

	public final static UUID WINDOW_OPEN = new UUID("c80260ba-41fd-8a46-768a-6bf236360e3a");

	public final static UUID ZIPPER = new UUID("6cf2be26-90cb-2669-a599-f5ab7698225f");

	private Sounds() {
	}

	/**
	 * A dictionary containing all pre-defined sounds
	 *
	 * @return A dictionary containing the pre-defined sounds, where the key is the
	 *         sounds ID, and the value is a string containing a name to identify
	 *         the purpose of the sound
	 */
	public static Map<UUID, String> toDictionary() {
		Map<UUID, String> dict = new HashMap<>();
		for (Field field : Sounds.class.getDeclaredFields()) {
			if ((field.getModifiers() & (Modifier.STATIC | Modifier.PUBLIC)) == (Modifier.STATIC | Modifier.PUBLIC)) {
				try {
					dict.put((UUID) field.get(null), field.getName());
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
			}
		}
		return dict;
	}

}