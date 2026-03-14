/*
 * CobolStringTest - Unit tests for COBOL string operations
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.runtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CobolString - COBOL string operation semantics")
class CobolStringTest {

    @Nested
    @DisplayName("MOVE operations")
    class MoveTests {
        @Test
        @DisplayName("move to exact length field")
        void moveExactLength() {
            String result = CobolString.move("HELLO", 5, CobolString.Justification.LEFT);
            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("move to longer field - left justified pads with spaces")
        void movePadLeft() {
            String result = CobolString.move("HI", 5, CobolString.Justification.LEFT);
            assertThat(result).isEqualTo("HI   ");
            assertThat(result.length()).isEqualTo(5);
        }

        @Test
        @DisplayName("move to longer field - right justified pads with spaces")
        void movePadRight() {
            String result = CobolString.move("HI", 5, CobolString.Justification.RIGHT);
            assertThat(result).isEqualTo("   HI");
        }

        @Test
        @DisplayName("move to shorter field - left justified truncates right")
        void moveTruncateLeft() {
            String result = CobolString.move("HELLO WORLD", 5, CobolString.Justification.LEFT);
            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("move to shorter field - right justified truncates left")
        void moveTruncateRight() {
            String result = CobolString.move("HELLO WORLD", 5, CobolString.Justification.RIGHT);
            assertThat(result).isEqualTo("WORLD");
        }

        @Test
        @DisplayName("move null source treated as empty")
        void moveNull() {
            String result = CobolString.move(null, 5, CobolString.Justification.LEFT);
            assertThat(result).isEqualTo("     ");
        }

        @Test
        @DisplayName("move empty source pads entirely")
        void moveEmpty() {
            String result = CobolString.move("", 3, CobolString.Justification.LEFT);
            assertThat(result).isEqualTo("   ");
        }
    }

    @Nested
    @DisplayName("INSPECT TALLYING")
    class InspectTallyingTests {
        @Test
        @DisplayName("count single character occurrences")
        void countSingleChar() {
            assertThat(CobolString.inspectTallying("ABCABC", "A")).isEqualTo(2);
        }

        @Test
        @DisplayName("count substring occurrences")
        void countSubstring() {
            assertThat(CobolString.inspectTallying("ABABAB", "AB")).isEqualTo(3);
        }

        @Test
        @DisplayName("count with no matches")
        void countNoMatch() {
            assertThat(CobolString.inspectTallying("HELLO", "X")).isEqualTo(0);
        }

        @Test
        @DisplayName("null inputs return 0")
        void nullInputs() {
            assertThat(CobolString.inspectTallying(null, "A")).isEqualTo(0);
            assertThat(CobolString.inspectTallying("HELLO", null)).isEqualTo(0);
            assertThat(CobolString.inspectTallying("HELLO", "")).isEqualTo(0);
        }

        @Test
        @DisplayName("non-overlapping counting")
        void nonOverlapping() {
            // "AAA" searching for "AA" should find 1 (non-overlapping)
            assertThat(CobolString.inspectTallying("AAA", "AA")).isEqualTo(1);
        }

        @Test
        @DisplayName("count leading occurrences within bounded range")
        void countLeadingWithBoundaries() {
            assertThat(CobolString.inspectTallying("AAABBAA", "A", true, false, "BB", null))
                    .isEqualTo(3);
        }

        @Test
        @DisplayName("count characters after delimiter")
        void countCharactersAfterDelimiter() {
            assertThat(CobolString.inspectTallying("ABCD", "", false, true, null, "A"))
                    .isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("INSPECT REPLACING")
    class InspectReplacingTests {
        @Test
        @DisplayName("replace all occurrences")
        void replaceAll() {
            String result = CobolString.inspectReplacing("ABCABC", "A", "X");
            assertThat(result).isEqualTo("XBCXBC");
        }

        @Test
        @DisplayName("replace with longer string")
        void replaceLonger() {
            String result = CobolString.inspectReplacing("ABC", "B", "XX");
            assertThat(result).isEqualTo("AXXC");
        }

        @Test
        @DisplayName("null subject returns empty")
        void nullSubject() {
            assertThat(CobolString.inspectReplacing(null, "A", "B")).isEqualTo("");
        }

        @Test
        @DisplayName("null target returns subject unchanged")
        void nullTarget() {
            assertThat(CobolString.inspectReplacing("HELLO", null, "X")).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("null replacement removes target")
        void nullReplacement() {
            assertThat(CobolString.inspectReplacing("HELLO", "L", null)).isEqualTo("HEO");
        }

        @Test
        @DisplayName("replace first occurrence only within boundary")
        void replaceFirstWithinBoundary() {
            assertThat(CobolString.inspectReplacing("ABABA", "A", "Z", false, true, false, "BA", null))
                    .isEqualTo("ZBABA");
        }

        @Test
        @DisplayName("replace characters mode across bounded range")
        void replaceCharactersMode() {
            assertThat(CobolString.inspectReplacing("ABCDE", "", "*", false, false, true, "DE", "A"))
                    .isEqualTo("A**DE");
        }
    }

    @Nested
    @DisplayName("INSPECT CONVERTING")
    class InspectConvertingTests {
        @Test
        @DisplayName("convert lowercase to uppercase")
        void convertCase() {
            String result = CobolString.inspectConverting("hello", "helo", "HELO");
            assertThat(result).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("convert specific characters")
        void convertChars() {
            String result = CobolString.inspectConverting("123-456", "-", "/");
            assertThat(result).isEqualTo("123/456");
        }

        @Test
        @DisplayName("character not in fromChars stays unchanged")
        void noMatchUnchanged() {
            String result = CobolString.inspectConverting("ABCXYZ", "ABC", "123");
            assertThat(result).isEqualTo("123XYZ");
        }

        @Test
        @DisplayName("null subject returns empty string")
        void nullSubject() {
            assertThat(CobolString.inspectConverting(null, "A", "B")).isEqualTo("");
        }

        @Test
        @DisplayName("null fromChars returns subject unchanged")
        void nullFromChars() {
            assertThat(CobolString.inspectConverting("HELLO", null, "B")).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("convert only within bounded range")
        void convertWithinBoundaries() {
            assertThat(CobolString.inspectConverting("ABCDAB", "AB", "XY", "DA", null))
                    .isEqualTo("XYCDAB");
        }
    }

    @Nested
    @DisplayName("STRING statement")
    class StringIntoTests {
        @Test
        @DisplayName("concatenate into target buffer")
        void basicString() {
            char[] target = new char[10];
            java.util.Arrays.fill(target, ' ');
            int newPointer = CobolString.stringInto(target, 1, "HELLO", " ", "WLD");
            assertThat(new String(target)).isEqualTo("HELLO WLD ");
            assertThat(newPointer).isEqualTo(10); // 1-based position after last write
        }

        @Test
        @DisplayName("overflow returns -1")
        void overflow() {
            char[] target = new char[3];
            int result = CobolString.stringInto(target, 1, "TOOLONG");
            assertThat(result).isEqualTo(-1);
        }

        @Test
        @DisplayName("pointer starts at correct position")
        void pointerOffset() {
            char[] target = "XXXXXXXXXX".toCharArray();
            int newPointer = CobolString.stringInto(target, 4, "HI");
            assertThat(new String(target)).isEqualTo("XXXHIXXXXX");
            assertThat(newPointer).isEqualTo(6); // 1-based
        }

        @Test
        @DisplayName("null target returns -1")
        void nullTarget() {
            assertThat(CobolString.stringInto(null, 1, "A")).isEqualTo(-1);
        }

        @Test
        @DisplayName("invalid pointer returns -1")
        void invalidPointer() {
            char[] target = new char[5];
            assertThat(CobolString.stringInto(target, 0, "A")).isEqualTo(-1);
            assertThat(CobolString.stringInto(target, -1, "A")).isEqualTo(-1);
        }

        @Test
        @DisplayName("null sources are skipped")
        void nullSources() {
            char[] target = new char[5];
            java.util.Arrays.fill(target, ' ');
            int result = CobolString.stringInto(target, 1, "A", null, "B");
            assertThat(new String(target)).isEqualTo("AB   ");
            assertThat(result).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("UNSTRING statement")
    class UnstringTests {
        @Test
        @DisplayName("split by delimiter")
        void basicUnstring() {
            String[] parts = CobolString.unstringBy("ONE,TWO,THREE", ",", 3);
            assertThat(parts).containsExactly("ONE", "TWO", "THREE");
        }

        @Test
        @DisplayName("fewer fields than maxFields pads with empty strings")
        void fewerFields() {
            String[] parts = CobolString.unstringBy("ONE,TWO", ",", 4);
            assertThat(parts).containsExactly("ONE", "TWO", "", "");
        }

        @Test
        @DisplayName("more fields than maxFields keeps remainder in last")
        void moreFields() {
            String[] parts = CobolString.unstringBy("A,B,C,D,E", ",", 3);
            assertThat(parts).containsExactly("A", "B", "C,D,E");
        }

        @Test
        @DisplayName("null source returns empty array")
        void nullSource() {
            assertThat(CobolString.unstringBy(null, ",", 3)).isEmpty();
        }

        @Test
        @DisplayName("empty source returns empty array")
        void emptySource() {
            assertThat(CobolString.unstringBy("", ",", 3)).isEmpty();
        }

        @Test
        @DisplayName("null delimiter returns entire string")
        void nullDelimiter() {
            String[] parts = CobolString.unstringBy("HELLO", null, 3);
            assertThat(parts).containsExactly("HELLO");
        }

        @Test
        @DisplayName("multi-character delimiter")
        void multiCharDelimiter() {
            String[] parts = CobolString.unstringBy("ONE::TWO::THREE", "::", 3);
            assertThat(parts).containsExactly("ONE", "TWO", "THREE");
        }
    }

    @Nested
    @DisplayName("Reference modification")
    class ReferenceModTests {
        @Test
        @DisplayName("basic substring extraction (1-based)")
        void basicRefMod() {
            // COBOL: WS-NAME(1:5)
            assertThat(CobolString.referenceModification("HELLO WORLD", 1, 5))
                    .isEqualTo("HELLO");
        }

        @Test
        @DisplayName("extract from middle")
        void middleExtract() {
            // COBOL: WS-NAME(7:5)
            assertThat(CobolString.referenceModification("HELLO WORLD", 7, 5))
                    .isEqualTo("WORLD");
        }

        @Test
        @DisplayName("extract rest of string (length = -1)")
        void extractRest() {
            assertThat(CobolString.referenceModification("HELLO WORLD", 7, -1))
                    .isEqualTo("WORLD");
        }

        @Test
        @DisplayName("start position beyond string returns empty")
        void beyondEnd() {
            assertThat(CobolString.referenceModification("HI", 10, 3)).isEqualTo("");
        }

        @Test
        @DisplayName("length extends beyond string clips to end")
        void clipToEnd() {
            assertThat(CobolString.referenceModification("HELLO", 4, 10))
                    .isEqualTo("LO");
        }

        @Test
        @DisplayName("null source returns empty")
        void nullSource() {
            assertThat(CobolString.referenceModification(null, 1, 5)).isEqualTo("");
        }

        @Test
        @DisplayName("invalid position returns empty")
        void invalidPos() {
            assertThat(CobolString.referenceModification("HELLO", 0, 3)).isEqualTo("");
            assertThat(CobolString.referenceModification("HELLO", -1, 3)).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("Figurative constants")
    class FigurativeConstantsTests {
        @Test
        @DisplayName("SPACE and SPACES are single space")
        void spaces() {
            assertThat(CobolString.FigurativeConstants.SPACE).isEqualTo(" ");
            assertThat(CobolString.FigurativeConstants.SPACES).isEqualTo(" ");
        }

        @Test
        @DisplayName("ZERO/ZEROS/ZEROES are '0'")
        void zeros() {
            assertThat(CobolString.FigurativeConstants.ZERO).isEqualTo("0");
            assertThat(CobolString.FigurativeConstants.ZEROS).isEqualTo("0");
            assertThat(CobolString.FigurativeConstants.ZEROES).isEqualTo("0");
        }

        @Test
        @DisplayName("HIGH-VALUE is 0xFF")
        void highValue() {
            assertThat(CobolString.FigurativeConstants.HIGH_VALUE).isEqualTo("\u00FF");
            assertThat(CobolString.FigurativeConstants.HIGH_VALUES).isEqualTo("\u00FF");
        }

        @Test
        @DisplayName("LOW-VALUE is 0x00")
        void lowValue() {
            assertThat(CobolString.FigurativeConstants.LOW_VALUE).isEqualTo("\u0000");
            assertThat(CobolString.FigurativeConstants.LOW_VALUES).isEqualTo("\u0000");
        }

        @Test
        @DisplayName("ALL generates repeated characters")
        void allRepeated() {
            assertThat(CobolString.FigurativeConstants.all('*', 5)).isEqualTo("*****");
            assertThat(CobolString.FigurativeConstants.all('0', 3)).isEqualTo("000");
        }
    }

    @Nested
    @DisplayName("Context-aware MOVE")
    class ContextMoveTests {
        @Test
        @DisplayName("move with MoveContext - alphanumeric left justified")
        void moveAlphanumericLeft() {
            var ctx = MoveContext.alphanumeric(10, false);
            String result = CobolString.move("HELLO", ctx);
            assertThat(result).isEqualTo("HELLO     ");
        }

        @Test
        @DisplayName("move with MoveContext - JUSTIFIED RIGHT")
        void moveJustifiedRight() {
            var ctx = MoveContext.alphanumeric(10, true);
            String result = CobolString.move("HELLO", ctx);
            assertThat(result).isEqualTo("     HELLO");
        }

        @Test
        @DisplayName("move with BLANK WHEN ZERO - all zeros")
        void moveBlankWhenZeroAllZeros() {
            var ctx = MoveContext.numeric(5, true);
            String result = CobolString.move("00.00", ctx);
            assertThat(result).isEqualTo("     ");
        }

        @Test
        @DisplayName("move with BLANK WHEN ZERO - non-zero value")
        void moveBlankWhenZeroNonZero() {
            var ctx = MoveContext.numeric(5, true);
            String result = CobolString.move("12345", ctx);
            assertThat(result).isEqualTo("12345");
        }

        @Test
        @DisplayName("move with sign positioning - LEADING")
        void moveSignLeading() {
            var ctx = MoveContext.numeric(7, false)
                    .withSign(MoveContext.SignPosition.LEADING);
            String result = CobolString.move("-123", ctx);
            assertThat(result).startsWith("-");
            assertThat(result).contains("123");
        }

        @Test
        @DisplayName("move with sign positioning - TRAILING")
        void moveSignTrailing() {
            var ctx = MoveContext.numeric(7, false)
                    .withSign(MoveContext.SignPosition.TRAILING);
            String result = CobolString.move("-123", ctx);
            assertThat(result).endsWith("-");
            assertThat(result).contains("123");
        }

        @Test
        @DisplayName("move null source with context")
        void moveNullSource() {
            var ctx = MoveContext.alphanumeric(5, false);
            String result = CobolString.move(null, ctx);
            assertThat(result).isEqualTo("     ");
        }
    }

    @Test
    @DisplayName("cannot instantiate utility class")
    void cannotInstantiate() {
        assertThatThrownBy(() -> {
            var ctor = CobolString.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ctor.newInstance();
        }).hasCauseInstanceOf(AssertionError.class);
    }
}

