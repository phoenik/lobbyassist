package com.lobbyassist.net.packet;

import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.UdpPacket;

import java.net.InetAddress;

/**
 * A helper class for StunPacket parsing from a pcap Packet.
 */
public class StunPacket {

    public enum BOUND {
        UNKNOWN,
        OUTBOUND,
        INBOUND;

        public static BOUND fromAddresses(int src, int dest, int orig) {
            if (src == orig) return OUTBOUND;
            if (dest == orig) return INBOUND;
            return UNKNOWN;
        }
    }

    public enum STATUS {
        UNKNOWN(-1),
        REQUEST(56),
        RESPONSE(68);

        private final int code;

        STATUS(int code) {
            this.code = code;
        }

        public static STATUS fromCode(int code){
            for(STATUS status : STATUS.values()) {
                if(status.code == code) {
                    return status;
                }
            }
            return UNKNOWN;
        }
    }

    private STATUS status;
    private BOUND bound;
    private int srcAddr;
    private int destAddr;

    public StunPacket(Packet packet, InetAddress source) {

        IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);

        if (ipV4Packet != null) {
            this.srcAddr = ipV4Packet.getHeader().getSrcAddr().hashCode();
            this.destAddr = ipV4Packet.getHeader().getDstAddr().hashCode();

            this.bound = BOUND.fromAddresses(this.srcAddr, this.destAddr, source.hashCode());

            UdpPacket udpPacket = ipV4Packet.get(UdpPacket.class);

            if (udpPacket != null && udpPacket.getPayload() != null) {
                this.status = STATUS.fromCode(udpPacket.getPayload().getRawData().length);
            }

        }

    }

    public int getSrcAddr() {
        return srcAddr;
    }

    public int getDestAddr() {
        return destAddr;
    }

    public STATUS getStatus() {
        return status;
    }

    public BOUND getBound() {
        return bound;
    }
}
