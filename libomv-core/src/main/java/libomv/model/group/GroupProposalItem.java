package libomv.model.group;

import java.util.Date;

import libomv.types.UUID;

// A group proposal
public final class GroupProposalItem {
	public UUID VoteID;
	public UUID VoteInitiator;
	public String TerseDateID;
	public boolean AlreadyVoted;
	public String VoteCast;
	// The minimum number of members that must vote before proposal passes
	// or failes
	public int Quorum;
	// The required ration of yes/no votes required for vote to pass
	// The three options are Simple Majority, 2/3 Majority, and Unanimous
	// TODO: this should be an enum
	public float Majority;
	public Date StartDateTime;
	public Date EndDateTime;
	// The Text of the proposal
	public String ProposalText;
}