package no.entra.bacnet.json.services;

import no.entra.bacnet.Octet;
import no.entra.bacnet.json.ConfigurationRequest;
import no.entra.bacnet.json.objects.PduType;
import org.slf4j.Logger;

import static no.entra.bacnet.json.configuration.ConfigurationParser.*;
import static org.slf4j.LoggerFactory.getLogger;

public class ConfirmedService extends Service {
    private static final Logger log = getLogger(ConfirmedService.class);
    public ConfirmedService(PduType pduType, Octet serviceChoice) {
        super(pduType, ConfirmedServiceChoice.fromOctet(serviceChoice));
    }

    public static ConfigurationRequest tryToUnderstandConfirmedRequest(Service service) {
        ConfigurationRequest configuration = null;
        if (service == null) {
            return null;
        }
        ServiceChoice serviceChoice = service.getServiceChoice();
        if (serviceChoice != null && serviceChoice instanceof UnconfirmedServiceChoice) {
            UnconfirmedServiceChoice unconfirmedServiceChoice = (UnconfirmedServiceChoice) serviceChoice;
            switch (unconfirmedServiceChoice) {
                case WhoIs:
                    log.trace("Is WhoIsMessage. hexString: {}", service.getUnprocessedHexString());
                    String whoIsBody = service.getUnprocessedHexString();
                    configuration = buildWhoIsRequest(whoIsBody);
                    break;
                case WhoHas:
                    log.trace("Is WhoHasMessage");
                    configuration = buildWhoHasRequest(service.getUnprocessedHexString());
                    break;
                case TimeSynchronization:
                    String timeSyncHexString = service.getUnprocessedHexString();
                    configuration = buildTimeSynchronizationRequest(timeSyncHexString);
                    break;
                case IAm:
                    String iamHexString = service.getUnprocessedHexString();
                    configuration = buildIamRequest(iamHexString);
                default:
                    log.trace("I do not know how to parse this service: {}", service);
            }
        }
        return configuration;
    }
}