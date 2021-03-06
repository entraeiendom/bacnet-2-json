package no.entra.bacnet.json.values;

import no.entra.bacnet.json.objects.PropertyIdentifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static no.entra.bacnet.json.values.ValueParser.parseListOfValues;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ValueListParserTest {

    @Test
    void parseValuesTest() {
        String hexString = "09552e44428200002f096f2e8204002f";
        List<Value> values = parseListOfValues(hexString);
        assertNotNull(values);
        Value expectedValue = new Value(PropertyIdentifier.PresentValue, 65.0f);
        Value foundValue = values.get(0);
        assertEquals(expectedValue, foundValue);
    }

    @Test
    void parseArrayTest() {
        String hexStringArray = "4e095519012e4441a4cccd2f4f";
        List<Value> values = parseListOfValues(hexStringArray);
        assertNotNull(values);
        Value expectedValue = new Value(PropertyIdentifier.PresentValue, 20.6f);
        Value foundValue = values.get(0);
        assertEquals(expectedValue, foundValue);

    }
}