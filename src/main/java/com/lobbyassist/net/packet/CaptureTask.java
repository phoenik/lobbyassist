package com.lobbyassist.net.packet;

import com.lobbyassist.model.StunAck;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class CaptureTask<Void> extends Task{

    private class PingUpdate implements Runnable {
        private final Map<Integer, Long> pings;
        private final Integer key;
        private final Long value;

        public PingUpdate (final Map<Integer, Long> pings,
                          final Integer key, final Long value) {
            this.pings = pings;
            this.key = key;
            this.value = value;
        }

        @Override
        public void run () {
            if( value == null ) {
                this.pings.remove(key);
            } else {
                this.pings.put(key, value);
            }
        }
    }

    private final Map<Integer, Long> pings;
    private final Map<Integer, StunAck> acks = new HashMap<>();
    private final InetAddress addr;
    private PcapHandle ph;

    public CaptureTask (final InetAddress addr, final Map<Integer, Long> pings) {
        this.pings = pings;
        this.addr = addr;
    }

    private void processPacket(Packet packet) {
        StunPacket stunPacket = new StunPacket(packet, this.addr);

        if(StunPacket.STATUS.REQUEST == stunPacket.getStatus()
                && StunPacket.BOUND.OUTBOUND == stunPacket.getBound()) {
            processAckRequest(stunPacket);
        }else if(StunPacket.STATUS.RESPONSE == stunPacket.getStatus()
                && StunPacket.BOUND.INBOUND == stunPacket.getBound()){
            processAckResponse(stunPacket);
        }
    }

    private void processAckRequest (StunPacket stunPacket) {
        StunAck ack = acks.get(stunPacket.getDestAddr());
        if (ack != null) {
            ack.request(ph.getTimestamp().getTime());
        } else {
            acks.put(stunPacket.getDestAddr(), new StunAck(ph.getTimestamp().getTime()));
        }
    }

    private void processAckResponse (StunPacket stunPacket) {
        StunAck ack = acks.get(stunPacket.getSrcAddr());

        if (ack != null && ack.isWaiting()) {
            Platform.runLater(new PingUpdate(pings,
                    stunPacket.getSrcAddr(), ack.ack(ph.getTimestamp().getTime())));
        }

    }

    @Override
    protected Void call() throws Exception {
        try {
            ph = Pcaps.getDevByAddress(addr).openLive(65536,
                    PcapNetworkInterface.PromiscuousMode.NONPROMISCUOUS, 0);
            ph.setFilter("udp && less 150", BpfProgram.BpfCompileMode.OPTIMIZE);
        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }

        while (!isCancelled() && ph != null && ph.isOpen()) {
            try {
                Packet packet = ph.getNextPacket();
                if( packet != null ) processPacket(packet);
            } catch (NotOpenException e) {
                e.printStackTrace();
            }

            acks.forEach((k,v) -> {
                if (v.isExpired()) {
                    Platform.runLater(new PingUpdate(pings, k, null));
                }
            });
        }

        if (ph != null && ph.isOpen()) ph.close();

        return null;
    }
}
