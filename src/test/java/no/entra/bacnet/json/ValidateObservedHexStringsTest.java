package no.entra.bacnet.json;

import no.entra.bacnet.ip.bvlc.Bvlc;
import no.entra.bacnet.ip.bvlc.BvlcParser;
import no.entra.bacnet.ip.npdu.Npdu;
import no.entra.bacnet.ip.npdu.NpduParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

public class ValidateObservedHexStringsTest {
    private static final Logger log = getLogger(ValidateObservedHexStringsTest.class);

    File file;
    BacNetParser parser;

    @BeforeEach
    public void setUp() throws Exception {
        String path = "src/test/resources/bacnet-hexstring";

        file = new File(path);
        String absolutePath = file.getAbsolutePath();
        assertTrue(absolutePath.endsWith(path));
        parser = new BacNetParser();
    }

    @Test
    public void validateHexStrings() throws IOException {
        List<String> allLines = Files.readAllLines(file.toPath());
        boolean success = true;
        for (String hexString : allLines) {
            try {
                Bvlc bvlc = BvlcParser.parseHex(hexString);
                assertNotNull(bvlc);
                Npdu npdu = NpduParser.parseNpduHex(hexString);
                assertNotNull(npdu);
                if (HexStringParser.hasApdu(hexString)) {
                    String apduHexString = HexStringParser.findApduHexString(hexString);
                    String json = parser.jasonFromApdu(apduHexString);
                    assertNotNull(json, "Failed to parse: " + apduHexString);
                }
            } catch (Exception e) {
                log.debug("Failed to parse {}. Reason: {}", hexString, e.getMessage());
                success = false;
            }
        }
        assertTrue(success, "Failed to parse. See log for info.");
    }
}
