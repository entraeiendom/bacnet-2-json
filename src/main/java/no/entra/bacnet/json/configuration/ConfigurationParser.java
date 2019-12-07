package no.entra.bacnet.json.configuration;

import no.entra.bacnet.Octet;
import no.entra.bacnet.json.ConfigurationRequest;
import no.entra.bacnet.json.objects.*;
import no.entra.bacnet.json.parser.ObjectIdParser;
import no.entra.bacnet.json.parser.ObjectIdParserResult;
import no.entra.bacnet.json.reader.OctetReader;
import no.entra.bacnet.json.utils.HexUtils;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.parseInt;
import static no.entra.bacnet.json.utils.HexUtils.toInt;
import static org.slf4j.LoggerFactory.getLogger;

public class ConfigurationParser {
    private static final Logger log = getLogger(ConfigurationParser.class);

    private static final char WHO_HAS_EXTENDED_VALUE = 'd';
    private static final char LOWER_LIMIT_KEY = '0';
    private static final char HIGH_LIMIT_KEY = '1';
    private static final char TIME_SYNC_DATE_KEY = 'a';
    private static final char TIME_SYNC_TIME_KEY = 'b';

    /*
    // Who Is
     */
    public static ConfigurationRequest buildWhoIsRequest(String whoIsBody) {
        ConfigurationRequest configuration = null;
        Integer rangeLowLimit = null;
        Integer rangeHighLimit = null;
        log.debug("whoisbody: {}", whoIsBody);
        OctetReader whoHasReader = new OctetReader(whoIsBody);
        Octet lowerLimitOctet = whoHasReader.next();
        if (lowerLimitOctet.getFirstNibble() == LOWER_LIMIT_KEY) {
            int numberOfOctets = mapWhoIsLength(lowerLimitOctet.getSecondNibble());
            String lowerLimitHex = whoHasReader.next(numberOfOctets);
            rangeLowLimit = toInt(lowerLimitHex);
        }
        Octet highLimitOctet = whoHasReader.next();
        if (highLimitOctet.getFirstNibble() == HIGH_LIMIT_KEY) {
            int numberOfOctets = mapWhoIsLength(highLimitOctet.getSecondNibble());
            String highLimitHex = whoHasReader.next(numberOfOctets);
            rangeHighLimit = toInt(highLimitHex);
        }

        if (rangeLowLimit != null || rangeHighLimit != null) {
            configuration = new ConfigurationRequest("TODO", null);
        }
        if (rangeLowLimit != null) {
            configuration.setProperty("DeviceInstanceRangeLowLimit", rangeLowLimit.toString());
        }
        if (rangeHighLimit != null) {
            configuration.setProperty("DeviceInstanceRangeHighLimit", rangeHighLimit.toString());
        }

        return configuration;
    }

    static int mapWhoIsLength(char lengthKey) {
        int length = 0;
        switch (lengthKey) {
            case '9':
                length = 1;
                break;
            case 'a':
                length = 2;
                break;
            case 'b':
                length = 3;
                break;
            default:
                log.debug("Unknown lenght of who is seccond nibble: {}", lengthKey);
                break;
        }
        return length;
    }

    /*
    // I AM
     */
    public static ConfigurationRequest buildIamRequest(String iamBody) {
//        String iamBody = "c40200020f22040091002105";
//        String objectIdentifier = "c40200020f"; //c = BacnetObjectIdentifier, 4 = length, ObjectType device
//        String maxADPULengthAccepted = "220400"; //2= unsigned integer, 2 = length 1024
//        String segmentationSupported = "9100"; //9 Enumerated, 1 = length
//        String vendorId = "2105"; //2 = unsigned integer, 1 = length, 5 = Johnson Controls
        ConfigurationRequest configuration = new ConfigurationRequest("TODO", null);
        OctetReader iamReader = new OctetReader(iamBody);
        Octet objectIdOctet = iamReader.next();
        char idLengthChar = objectIdOctet.getSecondNibble();
        int idLength = toInt(idLengthChar);
        Octet objectType = iamReader.next();
        if (objectType.equals(Octet.fromHexString("02"))) {
            configuration.setProperty("ObjectType", ObjectType.Device.name());
        }
        String instanceNumberOctet = iamReader.next(idLength -1);
        Integer instanceNumber = toInt(instanceNumberOctet);
        if (instanceNumber != null) {
            configuration.setProperty("InstanceNumber", instanceNumber.toString());
        }

        //MaxADPULengthAccepted
        Octet adpuLengthOctet = iamReader.next();
        char maxPdpuLengthChar = adpuLengthOctet.getSecondNibble();
        int octetCount = toInt(maxPdpuLengthChar);
        String maxPduLengthString = iamReader.next(octetCount);
        Integer maxAdpu = toInt(maxPduLengthString);
        if (maxAdpu != null) {
            configuration.setProperty("MaxADPULengthAccepted",maxAdpu.toString());
        }

        //SegmentationSupported
        Octet acceptSegmentationOctet = iamReader.next();
        char segLengthChar = acceptSegmentationOctet.getSecondNibble();
        int segLength = toInt(segLengthChar);
        if (segLength == 1) {
            Octet segmentationOcet = iamReader.next();
            Segmentation segmentation = Segmentation.fromOctet(segmentationOcet);
            if (segmentation != null) {
                configuration.setProperty("SegmentationSupported", segmentation.name());
            }
        }

        //Vendor
        Octet vendorOctet = iamReader.next();
        char vendorLengthChar = vendorOctet.getSecondNibble();
        int vendorLength = toInt(vendorLengthChar);
        if (vendorLength == 1) {
            Octet vendorId = iamReader.next();
            configuration.setProperty("VendorId", vendorId.toString());
            if (vendorId.equals(Octet.fromHexString("05"))) {
                configuration.setProperty("Vendor", "Johnson Controls, Inc");
            }
        }

        return configuration;
    }

    public static ConfigurationRequest buildWhoHasRequest(String whoHasBody) {
        ConfigurationRequest configuration = null;
        String objectName = null;
        OctetReader whoHasReader = new OctetReader(whoHasBody);
        Octet contextTagOctet = whoHasReader.next();
        char contextTag = contextTagOctet.getFirstNibble();
        char namedTag = contextTagOctet.getSecondNibble();
        if (namedTag == WHO_HAS_EXTENDED_VALUE) {
            Octet valueLength = whoHasReader.next();
            int valueOctetLength = parseInt(String.valueOf(valueLength), 16);
            Octet encoding = whoHasReader.next();
            String objectNameHex = whoHasReader.next(valueOctetLength - 1);
            log.debug("WhoHas-ObjectNameHex: {}", objectNameHex);
            objectName = HexUtils.parseExtendedValue(encoding, objectNameHex);
        }
        if (objectName != null) {
            configuration = new ConfigurationRequest("TODO", null);
            configuration.setProperty("ObjectName", objectName);
        }

        return configuration;
    }

    public static ConfigurationRequest buildTimeSynchronizationRequest(String timeSyncHexString) {
        ConfigurationRequest configuration = null;
        LocalDate date = null;
        LocalTime time = null;
        log.debug("whoisbody: {}", timeSyncHexString);
        OctetReader timeSyncReader = new OctetReader(timeSyncHexString);
        Octet dateOctet = timeSyncReader.next();
        if (dateOctet.getFirstNibble() == TIME_SYNC_DATE_KEY) {
            int numberOfOctets = mapTimeSyncLength(dateOctet.getSecondNibble());
            if (numberOfOctets == 4) {
                int year = 1900 + toInt(timeSyncReader.next());
                int month = toInt(timeSyncReader.next());
                int day = toInt(timeSyncReader.next());
                date = LocalDate.of(year,month,day);
                //Get day of week even if we do not need that one
                timeSyncReader.next();
            }
        }
        Octet timeOctet = timeSyncReader.next();
        if (timeOctet.getFirstNibble() == TIME_SYNC_TIME_KEY) {
            int numberOfOctets = mapTimeSyncLength(timeOctet.getSecondNibble());
            if (numberOfOctets == 4) {
                int hour = toInt(timeSyncReader.next());
                int min = toInt(timeSyncReader.next());
                int sec = toInt(timeSyncReader.next());
                int hundredsOfSec = toInt(timeSyncReader.next());
                time = LocalTime.of(hour, min, sec, tenthToNano(hundredsOfSec));
            }
        }

        if (date != null || time != null) {
            configuration = new ConfigurationRequest("TODO", null);

            if (date != null) {
                configuration.setProperty("TimeSyncDate", date.toString());
            }
            if (time != null) {
                configuration.setProperty("TimeSyncTime", time.toString());
            }
            if (date != null && time != null) {
                LocalDateTime dateTime = LocalDateTime.of(date,time);
                configuration.setProperty("TimeSyncDateTime", dateTime.toString());
            }
        }

        return configuration;
    }

    static int tenthToNano(int hudredsOfSec) {
       long nanos =  TimeUnit.SECONDS.convert(hudredsOfSec / 100, TimeUnit.MILLISECONDS);
        int hundredsInNano = (int) nanos;
        return hundredsInNano;
    }

    static int mapTimeSyncLength(char lengthKey) {
        int length = 0;
        switch (lengthKey) {
            case '4':
                length = 4;
                break;
            default:
                log.debug("Unknown length of Time Sync nibble: {}", lengthKey);
                break;
        }
        return length;
    }

    public static ConfigurationRequest buildIHaveRequest(String iHaveHexString) {
        ConfigurationRequest configuration = null;
        //1. NotificationClass
        //2. ObjectIdentifier
        //3. ObjectName
        OctetReader iHaveReader = new OctetReader(iHaveHexString);
        Octet applicationOctet = iHaveReader.next();
        if (applicationOctet.getFirstNibble() == 'c') {

            int length = toInt(applicationOctet.getSecondNibble());
            Octet objectTypeOctet = iHaveReader.next();
            Octet[] instanceNumberOctets = iHaveReader.nextOctets(length -1);
            ObjectId objectId = new ObjectIdBuilder(objectTypeOctet).withInstanceNumberOctet(instanceNumberOctets).build();
            if (objectId != null) {
                configuration = new ConfigurationRequest("TODO", null);
                configuration.setProperty("Request", "IHave");
                configuration.setProperty(objectId.getObjectType().name(), objectId.getInstanceNumber());
            }
            applicationOctet = iHaveReader.next();
            length = toInt(applicationOctet.getSecondNibble());
            objectTypeOctet = iHaveReader.next();
            instanceNumberOctets = iHaveReader.nextOctets(length -1);
            if (objectId != null) {
                objectId = new ObjectIdBuilder(objectTypeOctet).withInstanceNumberOctet(instanceNumberOctets).build();
                configuration.setProperty(objectId.getObjectType().name(), objectId.getInstanceNumber());
            }
            //TODO the rest like ObjectName
        }
        return configuration;
    }

    public static ConfigurationRequest buildWritePropertyMultipleRequest(String hexString) {
        ConfigurationRequest configuration = null;
        //1. Object Identifier
        //2. List of bacnet property values
        ObjectIdParserResult<ObjectId> result = ObjectIdParser.parse(hexString);
        ObjectId objectId = result.getParsedObject();
        log.debug("objectId: {}", objectId);
        ReadAccessResult accessResult = ReadAccessResult.buildFromResultList(hexString);
        log.debug("WritePropertyMultiple-accessResult: {}", accessResult);
        return configuration;
    }

    public static ConfigurationRequest buildReadPropertyRequest(String hexString) {
        ConfigurationRequest configuration = new ConfigurationRequest();

        return configuration;
    }
}
