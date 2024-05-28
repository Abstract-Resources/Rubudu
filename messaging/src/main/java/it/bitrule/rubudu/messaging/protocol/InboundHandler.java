package it.bitrule.rubudu.messaging.protocol;

public interface InboundHandler {

    /**
     * Called when a packet is received from the network.
     */
    void onInbound();
}