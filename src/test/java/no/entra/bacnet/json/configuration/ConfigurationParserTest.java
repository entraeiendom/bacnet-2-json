package no.entra.bacnet.json.configuration;

import no.entra.bacnet.json.ConfigurationRequest;
import no.entra.bacnet.json.bvlc.BvlcParser;
import no.entra.bacnet.json.bvlc.BvlcResult;
import no.entra.bacnet.json.npdu.NpduParser;
import no.entra.bacnet.json.npdu.NpduResult;
import no.entra.bacnet.json.objects.ObjectType;
import no.entra.bacnet.json.objects.PduType;
import no.entra.bacnet.json.objects.Segmentation;
import no.entra.bacnet.json.services.ConfirmedServiceChoice;
import no.entra.bacnet.json.services.Service;
import no.entra.bacnet.json.services.ServiceParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationParserTest {

    @Test
    void buildWhoIsRequest() {
        String whoIsBody = "0a07ae1a07ae";
        ConfigurationRequest configuration = ConfigurationParser.parseWhoIsRequest(whoIsBody);
        assertNotNull(configuration);
        assertEquals("1966",configuration.getProperty("DeviceInstanceRangeLowLimit"));
        assertEquals("1966",configuration.getProperty("DeviceInstanceRangeHighLimit"));
        whoIsBody = "0b26259f1b26259f310036002d004e004100450032002f004600430042002e004c006f00630061006c0020004";
        configuration = ConfigurationParser.parseWhoIsRequest(whoIsBody);
        assertNotNull(configuration);
        assertEquals("2499999",configuration.getProperty("DeviceInstanceRangeLowLimit"));
        assertEquals("2499999",configuration.getProperty("DeviceInstanceRangeHighLimit"));
        whoIsBody = "09101910";
        configuration = ConfigurationParser.parseWhoIsRequest(whoIsBody);
        assertNotNull(configuration);
        assertEquals("16",configuration.getProperty("DeviceInstanceRangeLowLimit"));
        assertEquals("16",configuration.getProperty("DeviceInstanceRangeHighLimit"));

    }

    @Test
    void parseWhoIsRequest() {
        String hexString = "810b00160120ffff00ff10080c000000001c00000000";
        String apduHexString = findApduPart(hexString);
        assertEquals("10080c000000001c00000000", apduHexString);
        Service service = ServiceParser.fromApduHexString(apduHexString);
        String whoIsBody = service.getUnprocessedHexString();
        assertEquals("0c000000001c00000000", whoIsBody);
        ConfigurationRequest configuration = ConfigurationParser.parseWhoIsRequest(whoIsBody);
        assertNotNull(configuration);
        assertEquals("0",configuration.getProperty("DeviceInstanceRangeLowLimit"));
        assertEquals("0",configuration.getProperty("DeviceInstanceRangeHighLimit"));
    }

    @Test
    void mapToValueOctetLengthTest() {
        assertEquals(1, ConfigurationParser.mapWhoIsLength('9'));
        assertEquals(2, ConfigurationParser.mapWhoIsLength('a'));
        assertEquals(3, ConfigurationParser.mapWhoIsLength('b'));
        assertEquals(4, ConfigurationParser.mapWhoIsLength('c'));
    }

    @Test
    void buildWhoHasRequest() {
        String whoHasBody = "3d15040053004f004d00530032002d004e004100450058002f005300630068006500640075006c0065002e004b0061006c0065006e00640065007200200041004b0053004500200038002e00500072006f006700720061006d006d0065007200620061007200650020006d006f00640075006c00650072002e00440043004f002e00440043004f0035004600310031002e0044006100670042007600690031002d00310031002e004400610067004200760076";
        ConfigurationRequest configuration = ConfigurationParser.parseWhoHasRequest(whoHasBody);
        assertNotNull(configuration);
        assertEquals("SOMS2-NAEX", configuration.getProperty("ObjectName"));
    }
    @Test
    void buildAnotherWhoHasRequest() {
        String whoHasBody = "3b0061764400330045003000350035002d004e00430045003300300031002f00500072006f006700720061006d006d0069006e0067002e0045006e0065007200670069002e00420065007200650067006e0069006e006700650072002e0045006e006500720067006900420065007200650067006e0069006e006700650072003400330033003300300031002e0044006100670042007600690031002d00310031002e0044006100670042007600760000";
        ConfigurationRequest configuration = ConfigurationParser.parseWhoHasRequest(whoHasBody);
        assertNotNull(configuration);
        assertTrue( configuration.getProperty("ObjectName").contains("NCE301"));
    }

    @Test
    void buildTimeSynchronizationRequestTest() {
        String timeSyncBody = "a4770b1b03b40c0b3939";
        ConfigurationRequest configuration = ConfigurationParser.parseTimeSynchronizationRequest(timeSyncBody);
        assertNotNull(configuration);
        assertEquals("2019-11-27",configuration.getProperty("TimeSyncDate"));
        assertEquals("12:11:57",configuration.getProperty("TimeSyncTime"));
    }

    @Test
    void whatIsThis() {
        String unknown = "3b006176500072006f006700720061006d006d0069006e0067002e002b00300032003d00330032003000200030003500200053006e00f80073006d0065006c0074002e0053006e0065002e005200540035003000310061006d006d0065007200620061007200650020006d006f00640075006c00650072002e00440043004f002e00440043004f00350000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        //FIXME
    }

    @Test
    void iAmTest() {
        String iamApdu = "1000c40200020f22040091002105";
        String iamBody = "c40200020f22040091002105";
        String objectIdentifier = "c40200020f"; //c = BacnetObjectIdentifier, 4 = length, ObjectType device
        String maxADPULengthAccepted = "220400"; //2= unsigned integer, 2 = length 1024
        String segmentationSupported = "9100"; //9 Enumerated, 1 = length
        String vendorId = "2105"; //2 = unsigned integer, 1 = length, 5 = Johnson Controls

        ConfigurationRequest configuration = ConfigurationParser.parseIamRequest(iamBody);
        assertNotNull(configuration);
        assertEquals("Device", configuration.getProperty("ObjectType"));
        assertEquals("527", configuration.getProperty("InstanceNumber"));
        assertEquals("1024", configuration.getProperty("MaxADPULengthAccepted"));
        assertEquals(Segmentation.SegmentedBoth.name(), configuration.getProperty("SegmentationSupported"));
        assertEquals("Johnson Controls, Inc", configuration.getProperty("Vendor"));
        assertEquals("05", configuration.getProperty("VendorId"));
    }

    @Test
    void iHaveTest() {
        String iHaveApdu = "c40200000bc403c000007100";
        ConfigurationRequest configuration = ConfigurationParser.parseIHaveRequest(iHaveApdu);
        assertNotNull(configuration);
        String deviceId = configuration.getProperty(ObjectType.Device.name());
        assertEquals("11", deviceId);
        assertEquals("0", configuration.getProperty(ObjectType.NotificationClass.name()));
        assertEquals("", configuration.getProperty("ObjectName"));
        assertEquals("IHave", configuration.getProperty("Request"));
    }

    String findApduPart(String bacnetHexString) {
        BvlcResult bvlcResult = BvlcParser.parse(bacnetHexString);
        assertNotNull(bvlcResult);
        NpduResult npduResult = NpduParser.parse(bvlcResult.getUnprocessedHexString());
        assertNotNull(npduResult);
        String apduHexString = npduResult.getUnprocessedHexString();
        return apduHexString;
    }

    @Test
    void readPropertiesObjectNameProtocolVersionRevision() {
        String hexString = "810a0028010030010e0c020000081e294d4e75060046574643554f29624e21014f298b4e210e4f1f";
        BvlcResult bvlcResult = BvlcParser.parse(hexString);
        NpduResult npduResult = NpduParser.parse(bvlcResult.getUnprocessedHexString());
        String apduHexString = npduResult.getUnprocessedHexString();
        Service service = ServiceParser.fromApduHexString(apduHexString);
        assertNotNull(service);
        assertEquals(PduType.ComplexAck, service.getPduType());
        assertEquals(1, service.getInvokeId());
        assertEquals(ConfirmedServiceChoice.ReadPropertyMultiple, service.getServiceChoice());
        assertEquals("0c020000081e294d4e75060046574643554f29624e21014f298b4e210e4f1f",service.getUnprocessedHexString());

    }


}
