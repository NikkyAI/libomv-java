package libomv.model.grid;

/* Type of grid item, such as telehub, event, populator location, etc. */
public enum GridItemType {
	Nothing, Telehub, PgEvent, MatureEvent, Popular, Unused1, AgentLocations, LandForSale, Classified, AdultEvent, AdultLandForSale;

	public static GridItemType convert(int value) {
		GridItemType values[] = GridItemType.values();

		for (int i = 0; i < values.length; i++)
			if (values[i].ordinal() == value)
				return values[i];
		return null;
	}
}