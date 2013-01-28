/**
 * Copyright (c) 2009, openmetaverse.org
 * Copyright (c) 2009-2012, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names
 *   of its contributors may be used to endorse or promote products derived from
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
package libomv.examples.TestClient.Commands.Stats;

import java.util.Map.Entry;

import libomv.LibSettings;
import libomv.Statistics;
import libomv.Statistics.Stat;
import libomv.examples.TestClient.Command;
import libomv.examples.TestClient.TestClient;
import libomv.types.UUID;

public class NetStatsCommand extends Command
{
    public NetStatsCommand(TestClient testClient)
    {
        Name = "netstats";
        Description = "Provide packet and capabilities utilization statistics";
        Category = CommandCategory.Simulator;
    }

	@Override
    public String execute(String[] args, UUID fromAgentID) throws Exception
    {
        if (!Client.Settings.getBool(LibSettings.TRACK_UTILIZATION))
        {
            return "TRACK_UTILIZATION is not enabled in Settings, statistics not available";
        }

        StringBuilder packetOutput = new StringBuilder();
        StringBuilder capsOutput = new StringBuilder();

        packetOutput.append(String.format("%-30s|%s|%s|%10s|%10s|\n", "Packet Name", "Sent", "Recv", " TX Bytes ", " RX Bytes "));

        capsOutput.append(String.format("%-30s|%s|%s|%10s|%10s|\n", "Packet Name", "Sent", "Recv", " TX Bytes ", " RX Bytes "));

        long packetsSentCount = 0;
        long packetsRecvCount = 0;
        long packetBytesSent = 0;
        long packetBytesRecv = 0;

        long capsSentCount = 0;
        long capsRecvCount = 0;
        long capsBytesSent = 0;
        long capsBytesRecv = 0;

        for (Entry<String, Statistics.Stat> kvp : Client.Stats.getStatistics().entrySet())
        {
        	Stat value = kvp.getValue();
            if (value.Type == Statistics.Type.Message)
            {                              
                capsOutput.append(String.format("%-30s|%s|%s|%10s|%10s|\n", kvp.getKey(), value.TxCount, value.RxCount,
                    formatBytes(value.TxBytes), formatBytes(value.RxBytes)));

                capsSentCount += value.TxCount;
                capsRecvCount += value.RxCount;
                capsBytesSent += value.TxBytes;
                capsBytesRecv += value.RxBytes;
            }
            else if (value.Type == Statistics.Type.Packet)
            {
                packetOutput.append(String.format("%-30s|%s|%s|%10s|%10s|\n", kvp.getKey(), value.TxCount, value.RxCount, 
                    formatBytes(value.TxBytes), formatBytes(value.RxBytes)));

                packetsSentCount += value.TxCount;
                packetsRecvCount += value.RxCount;
                packetBytesSent += value.TxBytes;
                packetBytesRecv += value.RxBytes;
            }
        }

        capsOutput.append(String.format("%-30s|%s|%s|%10s|%10s|\n", "Capabilities Totals", capsSentCount, capsRecvCount,
                    formatBytes(capsBytesSent), formatBytes(capsBytesRecv)));

        packetOutput.append(String.format("%-30s|%s|%s|%10s|%10s|\n", "Packet Totals", packetsSentCount, packetsRecvCount,
                    formatBytes(packetBytesSent), formatBytes(packetBytesRecv)));

        return capsOutput.toString() + "\n" + packetOutput.toString();
    }

    public String formatBytes(long bytes)
    {
        final int scale = 1024;
        String[] orders = new String[] {"Bytes", "KB", "MB"};
        for (String order : orders)
        {
            if (bytes < scale)
                return String.format("%d %s", bytes, order);

            bytes /= scale;
        }
        return String.format("%d GB", bytes);
    }
}
