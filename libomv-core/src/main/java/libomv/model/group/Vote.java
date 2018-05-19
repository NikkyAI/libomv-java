package libomv.model.group;

import libomv.types.UUID;

// A group Vote
public final class Vote {
	// Key of Avatar who created Vote
	public UUID Candidate;
	// Text of the Vote proposal
	public String VoteString;
	// Total number of votes
	public int NumVotes;
}