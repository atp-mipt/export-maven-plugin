package org.atpfivt;

import org.junit.jupiter.api.Test;

import static org.atpfivt.StripMarkedSections.stripMarkedSections;
import static org.junit.jupiter.api.Assertions.*;

class StripMarkedSectionsTest {

    @Test
    void noMarkers() {
        String src = "public class Foo { }";
        assertEquals(src, stripMarkedSections(src));
    }

    @Test
    void oneMarker() {
        String src      = "before //[[hidden//]] after";
        String expected = "before  after";
        assertEquals(expected, stripMarkedSections(src));
    }

    @Test
    void multipleMarkers() {
        String src      = "a //[[x//]] b //[[y//]] c";
        String expected = "a  b  c";
        assertEquals(expected, stripMarkedSections(src));
    }

    @Test
    void unmatchedStart() {
        String src = "keep //[[oops";
        assertEquals(src, stripMarkedSections(src));
    }
}