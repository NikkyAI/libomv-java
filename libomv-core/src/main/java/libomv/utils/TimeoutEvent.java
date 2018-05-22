/**
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
 * - Neither the name of the libomv-java project nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
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
package libomv.utils;

public class TimeoutEvent<T> {
	private boolean fired = false;
	private T object = null;

	/**
	 * Reset the state of the timeout event to untriggered and the object to
	 * undefined
	 */
	public synchronized void reset() {
		this.fired = false;
		this.object = null;
		this.notifyAll();
	}

	/**
	 * Reset the state of the timeout event to untriggered and the object to the
	 * object parameter
	 */
	public synchronized void reset(T object) {
		this.fired = false;
		this.object = object;
		this.notifyAll();
	}

	public synchronized void set(T object) {
		this.object = object;
		this.fired = object != null;
		this.notifyAll();
	}

	public synchronized T get() {
		return this.object;
	}

	public T waitOne(long timeout) throws InterruptedException {
		return waitOne(timeout, false);
	}

	/**
	 * Wait on the timeout to be triggered or until the timeout occurred
	 *
	 * @param timeout
	 *            The amount of milliseconds to wait. -1 will wait indefinitely
	 * @param reset
	 *            Reset the event just before returning
	 * @return
	 * @throws InterruptedException
	 */
	public synchronized T waitOne(long timeout, boolean reset) throws InterruptedException {
		if (!fired)
			if (timeout >= 0)
				wait(timeout);
			else
				wait();
		T obj = object;
		if (reset)
			reset();
		return obj;
	}
}
