package libomv.imaging.j2k;

import java.io.IOException;
import java.io.OutputStream;

import jj2000.j2k.codestream.Markers;
import jj2000.j2k.codestream.writer.CodestreamWriter;
import jj2000.j2k.codestream.writer.HeaderEncoder;

class BufferedCodestreamWriter extends CodestreamWriter {

	private final static int SOP_MARKER_LIMIT = 65535;

	private OutputStream out;
	private byte sopMarker[];
	private byte ephMarker[];
	private int packetIdx = 0;
	private int offLastROIPkt = 0;
	private int lenLastNoROI = 0;

	public BufferedCodestreamWriter(OutputStream out, int mb) {
		super(mb);
		this.out = out;
		this.maxBytes = mb;
		initSOP_EPHArrays();
	}

	@Override
	public void close() throws IOException {
		if (getMaxAvailableBytes() > 2) {
			// Write the EOC marker.
			out.write(Markers.EOC >> 8);
			out.write(Markers.EOC);
		}
		ndata += 2; // Add two to length of codestream for EOC marker
	}

	@Override
	public void commitBitstreamHeader(HeaderEncoder he) throws IOException {
		// Actualize ndata
		ndata += he.getLength();
		he.writeTo(out); // Write the header
		// Reset packet index used for SOP markers
		packetIdx = 0;

		// Deal with ROI information
		lenLastNoROI += he.getLength();
	}

	@Override
	public int getLength() {
		if (getMaxAvailableBytes() >= 0) {
			return ndata;
		}
		return maxBytes;
	}

	@Override
	public int getMaxAvailableBytes() {
		return maxBytes - ndata;
	}

	@Override
	public int getOffLastROIPkt() {
		return offLastROIPkt;
	}

	@Override
	public int writePacketBody(byte[] body, int blen, boolean sim, boolean roiInPkt, int rlen) throws IOException {
		int len = blen;

		// If not in simulation mode write the data
		if (!sim) {
			// Write the body bytes
			len = blen;
			if (getMaxAvailableBytes() < len) {
				len = getMaxAvailableBytes();
			}
			if (blen > 0) {
				out.write(body, 0, len);
			}
			// Update data length
			ndata += len;

			// Deal with ROI information
			if (roiInPkt) {
				offLastROIPkt += lenLastNoROI + rlen;
				lenLastNoROI = len - rlen;
			} else {
				lenLastNoROI += len;
			}
		}
		return len;
	}

	@Override
	public int writePacketHead(byte[] head, int hlen, boolean sim, boolean sop, boolean eph) throws IOException {
		int len = hlen + (sop ? Markers.SOP_LENGTH : 0) + (eph ? Markers.EPH_LENGTH : 0);

		// If not in simulation mode write the data
		if (!sim) {
			// Write the head bytes
			if (getMaxAvailableBytes() < len) {
				len = getMaxAvailableBytes();
			}

			if (len > 0) {
				// Write Start Of Packet header markers if necessary
				if (sop) {
					// The first 4 bytes of the array have been filled in the
					// classe's constructor.
					sopMarker[4] = (byte) (packetIdx >> 8);
					sopMarker[5] = (byte) (packetIdx);
					out.write(sopMarker, 0, Markers.SOP_LENGTH);
					packetIdx++;
					if (packetIdx > SOP_MARKER_LIMIT) {
						// Reset SOP value as we have reached its upper limit
						packetIdx = 0;
					}
				}
				out.write(head, 0, hlen);
				// Update data length
				ndata += len;

				// Write End of Packet Header markers if necessary
				if (eph) {
					out.write(ephMarker, 0, Markers.EPH_LENGTH);
				}

				// Deal with ROI Information
				lenLastNoROI += len;
			}
		}
		return len;
	}

	/**
	 * Performs the initialisation of the arrays that are used to store the values
	 * used to write SOP and EPH markers
	 */
	private void initSOP_EPHArrays() {

		// Allocate and set first values of SOP marker as they will not be
		// modified
		sopMarker = new byte[Markers.SOP_LENGTH];
		sopMarker[0] = (byte) (Markers.SOP >> 8);
		sopMarker[1] = (byte) Markers.SOP;
		sopMarker[2] = (byte) 0x00;
		sopMarker[3] = (byte) 0x04;

		// Allocate and set values of EPH marker as they will not be
		// modified
		ephMarker = new byte[Markers.EPH_LENGTH];
		ephMarker[0] = (byte) (Markers.EPH >> 8);
		ephMarker[1] = (byte) Markers.EPH;
	}

}
