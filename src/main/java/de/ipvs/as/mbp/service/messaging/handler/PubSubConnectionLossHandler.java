package de.ipvs.as.mbp.service.messaging.handler;

/**
 * Handler that is notified in case the connection to the external messaging broker is lost.
 */
public interface PubSubConnectionLossHandler {
    /**
     * Handles the loss of connection, possibly by inspecting the cause for the connection loss that is passed
     * as parameter.
     *
     * @param cause The cause for the connection loss
     */
    void handleConnectionLoss(Throwable cause);
}
