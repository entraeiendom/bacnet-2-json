package no.entra.bacnet.json.services;

import no.entra.bacnet.Octet;
import no.entra.bacnet.json.objects.PduType;
import no.entra.bacnet.json.reader.OctetReader;
import no.entra.bacnet.json.utils.HexUtils;
import org.slf4j.Logger;

import static no.entra.bacnet.json.apdu.PduFlags.*;
import static no.entra.bacnet.json.utils.HexUtils.toInt;
import static org.slf4j.LoggerFactory.getLogger;

public class ServiceParser {
    private static final Logger log = getLogger(ServiceParser.class);

    public static Service fromApduHexString(String apduHexString) {
        Service service = null;
        Octet serviceChoiceOctet = null;
        OctetReader serviceReader = new OctetReader(apduHexString);
        Octet pduTypeOctet = serviceReader.next();
        log.debug("PDU Type {} in bits: {}{}", pduTypeOctet,HexUtils.toBitString(pduTypeOctet.getFirstNibble()),HexUtils.toBitString(pduTypeOctet.getSecondNibble()));
        PduType pduType = PduType.fromOctet(pduTypeOctet);
        if (pduType != null) {
            char pduFlags = pduTypeOctet.getSecondNibble();

            ServiceBuilder serviceBuilder = new ServiceBuilder(pduType);
            if (isSegmented(pduFlags)) {
                serviceBuilder = serviceBuilder.withIsSegmented(true);
            }
            if (hasMoreSegments(pduFlags)) {
                serviceBuilder = serviceBuilder.withHasMoreSegments(true);
            }
            if (willAcceptSegmentedResponse(pduFlags) ) {
//                serviceReader.next();
                Octet maxApduOctetsAccepted = serviceReader.next();
                int numberOfOctets = 9999;

                Octet invokeIdOctet = serviceReader.next();
                int invokeId = toInt(invokeIdOctet);
                serviceBuilder = serviceBuilder.withWillAcceptSegmentedResponse(true)
                        .withMaxAPDUSize(numberOfOctets)
                        .withInvokId(invokeId);
                if (hasServiceChoice(pduType)) {
                    serviceChoiceOctet = serviceReader.next();
                    serviceBuilder.withServiceChoice(serviceChoiceOctet);
                }
            } else if (isSegmented(pduFlags) || pduType == PduType.SegmentACK) {
                Octet invokeIdOctet = serviceReader.next();
                int invokeId = toInt(invokeIdOctet);
                serviceBuilder = serviceBuilder.withInvokId(invokeId);
                Octet sequenceNumberOctet = serviceReader.next();
                int sequenceNumber = toInt(sequenceNumberOctet);
                serviceBuilder = serviceBuilder.withSequenceNumber(sequenceNumber);
                Octet proposedWindowSizeOctet = serviceReader.next();
                int proposedWindowSize = toInt(proposedWindowSizeOctet);
                serviceBuilder = serviceBuilder.withProposedWindowSize(proposedWindowSize);
                if (hasServiceChoice(pduType)) {
                    serviceChoiceOctet = serviceReader.next();
                    serviceBuilder.withServiceChoice(serviceChoiceOctet);
                }
            } else if (hasServiceChoice(pduType)) {
                if (pduType.equals(PduType.ComplexAck)) {
                    Octet invokeIdOctet = serviceReader.next();
                    int invokeId = toInt(invokeIdOctet);
                    serviceBuilder.withInvokId(invokeId);
                }
                serviceChoiceOctet = serviceReader.next();
                serviceBuilder.withServiceChoice(serviceChoiceOctet);
            }
            service = serviceBuilder.build();
            service.setUnprocessedHexString(serviceReader.unprocessedHexString());
        } else {
            log.debug("Could not find PduType. pduTypeOctet: {}, from apduHexString: {}", pduTypeOctet, apduHexString);
        }
        return service;
    }

    private static boolean hasServiceChoice(PduType pduType) {
        boolean hasServiceChoice = true;
        switch (pduType) {
            case SegmentACK:
                hasServiceChoice = false;
                break;
            default:
                hasServiceChoice = true;

        }
        return hasServiceChoice;
    }
}
