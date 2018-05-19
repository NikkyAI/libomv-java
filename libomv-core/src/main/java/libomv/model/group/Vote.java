package libomv.model.group;

import libomv.types.UUID;

// A group Vote
public final class Vote {
	// Key of Avatar who created Vote
	public UUID candidate;
	// Text of the Vote proposal
	public String voteString;
	// Total number of votes
	public int numVotes;
}