package com.sekacorn.corn.parser;

import com.sekacorn.corn.ir.Picture;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PictureAnalyzerTest {

    @Test
    void numericPicture() {
        Picture pic = PictureAnalyzer.analyze("9(5)");
        assertEquals(Picture.PictureCategory.NUMERIC, pic.getCategory());
        assertEquals(5, pic.getLength());
        assertEquals(0, pic.getScale());
        assertFalse(pic.isSigned());
        assertFalse(pic.hasEditSymbols());
    }

    @Test
    void signedNumericWithScale() {
        Picture pic = PictureAnalyzer.analyze("S9(4)V99");
        assertEquals(Picture.PictureCategory.NUMERIC, pic.getCategory());
        assertEquals(6, pic.getLength()); // 4 + 2 digits, S and V not counted
        assertEquals(2, pic.getScale());
        assertTrue(pic.isSigned());
    }

    @Test
    void alphanumericPicture() {
        Picture pic = PictureAnalyzer.analyze("X(20)");
        assertEquals(Picture.PictureCategory.ALPHANUMERIC, pic.getCategory());
        assertEquals(20, pic.getLength());
        assertEquals(0, pic.getScale());
    }

    @Test
    void alphabeticPicture() {
        Picture pic = PictureAnalyzer.analyze("A(10)");
        assertEquals(Picture.PictureCategory.ALPHABETIC, pic.getCategory());
        assertEquals(10, pic.getLength());
    }

    @Test
    void numericEditedWithZ() {
        Picture pic = PictureAnalyzer.analyze("Z(4)9");
        assertEquals(Picture.PictureCategory.NUMERIC_EDITED, pic.getCategory());
        assertEquals(5, pic.getLength());
        assertTrue(pic.hasEditSymbols());
        assertNotNull(pic.getEditPattern());
    }

    @Test
    void numericEditedWithDecimalPoint() {
        Picture pic = PictureAnalyzer.analyze("9(3).99");
        assertEquals(Picture.PictureCategory.NUMERIC_EDITED, pic.getCategory());
        assertTrue(pic.hasEditSymbols());
    }

    @Test
    void expandParenthesizedRepetition() {
        assertEquals("99999", PictureAnalyzer.expand("9(5)"));
        assertEquals("XXXXXXXXXXXXXXXXXXXX", PictureAnalyzer.expand("X(20)"));
        assertEquals("S9999V99", PictureAnalyzer.expand("S9(4)V9(2)"));
    }

    @Test
    void expandWithNoParentheses() {
        assertEquals("999", PictureAnalyzer.expand("999"));
        assertEquals("XX", PictureAnalyzer.expand("XX"));
    }

    @Test
    void nullOrBlankThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> PictureAnalyzer.analyze(null));
        assertThrows(IllegalArgumentException.class, () -> PictureAnalyzer.analyze(""));
        assertThrows(IllegalArgumentException.class, () -> PictureAnalyzer.analyze("   "));
    }

    @Test
    void picXX() {
        // PIC XX = 2-char alphanumeric
        Picture pic = PictureAnalyzer.analyze("XX");
        assertEquals(Picture.PictureCategory.ALPHANUMERIC, pic.getCategory());
        assertEquals(2, pic.getLength());
    }

    @Test
    void picWithDollarSign() {
        Picture pic = PictureAnalyzer.analyze("$$$,$$9.99");
        assertEquals(Picture.PictureCategory.NUMERIC_EDITED, pic.getCategory());
        assertTrue(pic.hasEditSymbols());
    }
}
