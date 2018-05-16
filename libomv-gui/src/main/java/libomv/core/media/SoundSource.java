/**
 * Copyright (c) 2011-2012, Frederick Martian
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
package libomv.core.media;

import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;

// A SoundSource represents the data (buffer or stream)
public class SoundSource {
	private AudioInputStream din = null;
	private float minDistance = 1.0f;
	private float maxDistance = 10000.0f;
	private int loopCount = -1;
	private int loopStart;
	private int loopEnd;
	private int bufOffset;
	private int bufLength;
	private byte[] data;

	public SoundSource(AudioInputStream din) {
		this.din = din;
		data = new byte[4096];
	}

	public void dispose() {
		if (din != null) {
			try {
				din.close();
			} catch (IOException ex) {
			}
			din = null;
		}
		data = null;
	}

	public AudioFormat getFormat() {
		return din.getFormat();
	}

	public int getFrameLength() {
		if (din != null)
			return (int) din.getFrameLength();
		return (bufLength % din.getFormat().getFrameSize());
	}

	public void setLoopPoints(int start, int end) {
		loopStart = start;
		loopEnd = end;
	}

	public void setLoopCount(int number) {
		loopCount = number;
	}

	public int getLoopCount() {
		return loopCount;
	}

	public void set3DMinMaxDistance(float minDistance, float maxDistance) {
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;
	}

	public float get3DMinDistance() {
		return this.minDistance;
	}

	public float get3DMaxDistance() {
		return this.maxDistance;
	}

	public int write(SourceDataLine line, int length) throws IOException {
		int bytesRead = 0, bytesWritten = 0, localLength = length;
		if (loopCount < 0 || loopCount > 1) {
			/* We are supposed to be looping */
			if (din != null) {
				/*
				 * First time executing, read the input stream and store it in our internal
				 * buffer
				 */
				int newcount = bufLength + localLength;
				if (newcount > data.length) {
					data = Arrays.copyOf(data, Math.max(data.length << 1, newcount));
				}
				bytesRead = din.read(data, bufLength, localLength);
				if (bytesRead >= 0) {
					bufLength += bytesRead;
				} else {
					din.close();
					din = null;
				}
			}
			if (bufOffset < bufLength) {
				localLength = Math.min(length, bufLength - bufOffset);
			} else {
				if (loopStart > 0)
					bufOffset = loopStart * din.getFormat().getFrameSize();
				else
					bufOffset = 0;

				if (loopEnd > 0)
					bufLength = loopEnd * din.getFormat().getFrameSize();

				if (loopCount > 0)
					loopCount--;
			}
			if (loopCount != 0) {
				bytesWritten = line.write(data, bufOffset, localLength);
				if (bytesWritten > 0) {
					bufOffset += bytesWritten;
				}
				return bytesWritten;
			}
			return 0;
		}

		{
			bytesRead = din.read(data, 0, Math.min(data.length, localLength));
			if (bytesRead > 0) {
				bytesRead = line.write(data, 0, bytesRead);
				if (bytesRead > 0) {
					bytesWritten += bytesRead;
				}
			}
		}
		while (bytesRead > 0 && bytesWritten < localLength)
			;
		return bytesWritten;
	}
}
