/**
 Copyright (c) 2006, Lateral Arts Limited
 All rights reserved.


 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.

 $Id: IntArray.java,v 1.1 2007/01/28 14:08:52 nuage Exp $
 */

package libomv.utils;

public class IntArray {

	public int size() {
		return m_SP;
	}

	public int at(int i) {
		return m_Array[i];
	}

	public void add(int i) {
		if (m_SP >= m_Array.length) {
			int tmpArray[] = new int[m_Array.length + m_GrowthSize];
			System.arraycopy(m_Array, 0, tmpArray, 0, m_Array.length);
			m_Array = tmpArray;
		}
		m_Array[m_SP] = i;
		m_SP++;
	}

	public int[] toArray() {
		int trimmedArray[] = new int[m_SP];
		System.arraycopy(m_Array, 0, trimmedArray, 0, trimmedArray.length);
		return trimmedArray;
	}

	public void clear() {
		m_SP = 0;
	}

	public IntArray() {
		init(256);
	}

	public IntArray(int[] arr) {
		if (arr == null || arr.length == 0) {
			init(256);
		} else {
			m_GrowthSize = arr.length;
			m_SP = arr.length;
			m_Array = arr;
		}
	}

	public IntArray(int initialSize) {
		init(initialSize);
	}

	public IntArray(int initialSize, int growthSize) {
		init(initialSize, growthSize);
	}

	protected void init(int initialSize) {
		init(initialSize, initialSize / 4);
	}

	protected void init(int initialSize, int growthSize) {
		m_SP = 0;
		m_GrowthSize = growthSize > 0 ? growthSize : 2;
		m_Array = new int[initialSize];
	}

	int m_SP;

	private int m_Array[];

	private int m_GrowthSize;
}
