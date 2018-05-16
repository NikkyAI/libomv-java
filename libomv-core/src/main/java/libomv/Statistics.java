/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv;

import java.util.HashMap;

public class Statistics {
	public enum Type {
		Packet, Message
	}

	public class Stat {
		public Type Type;
		public long TxCount;
		public long RxCount;
		public long TxBytes;
		public long RxBytes;

		public Stat(Type type, long txCount, long rxCount, long txBytes, long rxBytes) {
			this.Type = type;
			this.TxCount = txCount;
			this.RxCount = rxCount;
			this.TxBytes = txBytes;
			this.RxBytes = rxBytes;
		}
	}

	private HashMap<String, Stat> m_StatsCollection;

	public Statistics() {
		m_StatsCollection = new HashMap<String, Stat>();
	}

	public void updateNetStats(String key, Type Type, long txBytes, long rxBytes) {
		synchronized (m_StatsCollection) {
			Stat stat = m_StatsCollection.get(key);
			if (stat != null) {
				if (rxBytes > 0) {
					stat.RxCount++;
					stat.RxBytes += rxBytes;
				}

				if (txBytes > 0) {
					stat.TxCount++;
					stat.TxBytes += txBytes;
				}

			} else {
				if (txBytes > 0)
					stat = new Stat(Type, 1, 0, txBytes, 0);
				else
					stat = new Stat(Type, 0, 1, 0, rxBytes);

				m_StatsCollection.put(key, stat);
			}
		}
	}

	public HashMap<String, Stat> getStatistics() {
		synchronized (m_StatsCollection) {
			return new HashMap<String, Stat>(m_StatsCollection);
		}
	}
}
