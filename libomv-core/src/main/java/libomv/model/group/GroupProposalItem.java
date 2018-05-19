package libomv.model.group;

import java.util.Date;

import libomv.types.UUID;

// A group proposal
public final class GroupProposalItem {
	public UUID voteID;
	public UUID voteInitiator;
	public String terseDateID;
	public boolean alreadyVoted;
	public String voteCast;
	// The minimum number of members that must vote before proposal passes
	// or failes
	public int quorum;
	// The required ration of yes/no votes required for vote to pass
	// The three options are Simple Majority, 2/3 Majority, and Unanimous
	// TODO: this should be an enum
	public float majority;
	public Date startDateTime;
	public Date endDateTime;
	// The Text of the proposal
	public String proposalText;
}