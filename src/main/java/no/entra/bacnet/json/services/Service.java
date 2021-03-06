package no.entra.bacnet.json.services;

import no.entra.bacnet.json.objects.PduType;

public class Service {
    private final PduType pduType;
    private final ServiceChoice serviceChoice;
    private String unprocessedHexString;
    private boolean isSegmented = false;
    private boolean hasMoreSegments = false;
    private boolean willAcceptSegmentedResponse = false;
    private Integer maxAcceptedPduSize;
    private boolean replyExpected;
    private Integer sequenceNumber;
    private Integer proposedWindowSize;

    public void setInvokeId(Integer invokeId) {
        this.invokeId = invokeId;
    }

    private Integer invokeId = null;

    public Service(PduType pduType, ServiceChoice serviceChoice) {
        this.pduType = pduType;
        this.serviceChoice = serviceChoice;
    }

    public PduType getPduType() {
        return pduType;
    }

    public ServiceChoice getServiceChoice() {
        return serviceChoice;
    }

    public void setUnprocessedHexString(String unprocessedHexString) {
        this.unprocessedHexString = unprocessedHexString;
    }

    public String getUnprocessedHexString() {
        return unprocessedHexString;
    }

    public void isSegmented(boolean isSegmented) {
        this.isSegmented = isSegmented;
    }

    public void hasMoreSegments(boolean hasMoreSegments) {
        this.hasMoreSegments = hasMoreSegments;
    }

    public void willAcceptSegmentedResponse(boolean willAcceptSegmentedResponse) {
        this.willAcceptSegmentedResponse = willAcceptSegmentedResponse;
    }

    public boolean isSegmented() {
        return isSegmented;
    }

    public boolean isHasMoreSegments() {
        return hasMoreSegments;
    }

    public boolean isWillAcceptSegmentedResponse() {
        return willAcceptSegmentedResponse;
    }

    public Integer getInvokeId() {
        return invokeId;
    }

    public void setMaxAcceptedPduSize(Integer maxAcceptedPduSize) {
        this.maxAcceptedPduSize = maxAcceptedPduSize;
    }

    public Integer getMaxAcceptedPduSize() {
        return maxAcceptedPduSize;
    }

    public boolean isReplyExpected() {
        return replyExpected;
    }

    public void setReplyExpected(boolean replyExpected) {
        this.replyExpected = replyExpected;
    }

    @Override
    public String toString() {
        return "Service{" +
                "pduType=" + pduType +
                ", serviceChoice=" + serviceChoice +
                ", unprocessedHexString='" + unprocessedHexString + '\'' +
                ", isSegmented=" + isSegmented +
                ", hasMoreSegments=" + hasMoreSegments +
                ", willAcceptSegmentedResponse=" + willAcceptSegmentedResponse +
                ", maxAcceptedPduSize=" + maxAcceptedPduSize +
                ", replyExpected=" + replyExpected +
                ", invokeId=" + invokeId +
                '}';
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Integer getProposedWindowSize() {
        return proposedWindowSize;
    }

    public void setProposedWindowSize(Integer proposedWindowSize) {
        this.proposedWindowSize = proposedWindowSize;
    }
}
