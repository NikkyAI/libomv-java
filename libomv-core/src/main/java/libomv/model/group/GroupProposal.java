package libomv.model.group;

// A group proposal
public final class GroupProposal {
	// The minimum number of members that must vote before proposal passes
	// or fails
	public int quorum;
	// The required ration of yes/no votes required for vote to pass
	// The three options are Simple Majority, 2/3 Majority, and Unanimous
	// TODO: this should be an enum
	public float majority;
	// The duration in days votes are accepted
	public int duration;
	// The Text of the proposal
	public String proposalText;
}