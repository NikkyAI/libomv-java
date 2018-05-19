package libomv.model.simulator;

import java.util.HashSet;

public final class IncomingPacketIDCollection {
	private final int[] items;
	private HashSet<Integer> hashSet;
	private int first = 0;
	private int next = 0;
	private int capacity;

	public IncomingPacketIDCollection(int capacity) {
		this.capacity = capacity;
		items = new int[capacity];
		hashSet = new HashSet<Integer>();
	}

	public boolean tryEnqueue(int ack) {
		synchronized (hashSet) {
			if (hashSet.add(ack)) {
				items[next] = ack;
				next = (next + 1) % capacity;
				if (next == first) {
					hashSet.remove(items[first]);
					first = (first + 1) % capacity;
				}
				return true;
			}
		}
		return false;
	}
}