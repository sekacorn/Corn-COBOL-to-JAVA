/*
 * CobolLexer.g4 - ANTLR4 lexer grammar for COBOL
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 *
 * Expects preprocessed input (fixed-format columns stripped).
 * Case-insensitive to match COBOL conventions.
 */
lexer grammar CobolLexer;

options { caseInsensitive = true; }

// ─── Division / Section / Paragraph keywords ───

IDENTIFICATION  : 'IDENTIFICATION' ;
ID              : 'ID' ;
ENVIRONMENT     : 'ENVIRONMENT' ;
DATA            : 'DATA' ;
PROCEDURE       : 'PROCEDURE' ;
DIVISION        : 'DIVISION' ;
SECTION         : 'SECTION' ;

// ─── Identification Division paragraphs ───

PROGRAM_ID      : 'PROGRAM-ID' ;
AUTHOR          : 'AUTHOR' ;
DATE_WRITTEN    : 'DATE-WRITTEN' ;
DATE_COMPILED   : 'DATE-COMPILED' ;
INSTALLATION    : 'INSTALLATION' ;
SECURITY        : 'SECURITY' ;
REMARKS         : 'REMARKS' ;

// ─── Environment Division ───

CONFIGURATION   : 'CONFIGURATION' ;
SOURCE_COMPUTER : 'SOURCE-COMPUTER' ;
OBJECT_COMPUTER : 'OBJECT-COMPUTER' ;
SPECIAL_NAMES   : 'SPECIAL-NAMES' ;
INPUT_OUTPUT    : 'INPUT-OUTPUT' ;
FILE_CONTROL    : 'FILE-CONTROL' ;
I_O_CONTROL     : 'I-O-CONTROL' ;

SELECT          : 'SELECT' ;
ASSIGN          : 'ASSIGN' ;
ORGANIZATION    : 'ORGANIZATION' ;
ACCESS          : 'ACCESS' ;
MODE            : 'MODE' ;
SEQUENTIAL      : 'SEQUENTIAL' ;
INDEXED         : 'INDEXED' ;
RELATIVE        : 'RELATIVE' ;
RANDOM          : 'RANDOM' ;
DYNAMIC         : 'DYNAMIC' ;
RECORD          : 'RECORD' ;
ALTERNATE       : 'ALTERNATE' ;
KEY             : 'KEY' ;
DUPLICATES      : 'DUPLICATES' ;
FILE_STATUS     : 'FILE' WS+ 'STATUS' ;
STATUS          : 'STATUS' ;
OPTIONAL        : 'OPTIONAL' ;

// ─── Data Division ───

FD              : 'FD' ;
SD              : 'SD' ;
WORKING_STORAGE : 'WORKING-STORAGE' ;
LOCAL_STORAGE   : 'LOCAL-STORAGE' ;
LINKAGE         : 'LINKAGE' ;
FILE            : 'FILE' ;
COPY            : 'COPY' ;
REPLACING       : 'REPLACING' ;

PIC             : 'PIC' -> pushMode(PIC_MODE) ;
PICTURE         : 'PICTURE' -> pushMode(PIC_MODE) ;
VALUE           : 'VALUE' ;
VALUES          : 'VALUES' ;
USAGE           : 'USAGE' ;
// DISPLAY used for both USAGE DISPLAY and DISPLAY statement (context-sensitive)
COMP            : 'COMP' ;
COMP_1          : 'COMP-1' ;
COMP_2          : 'COMP-2' ;
COMP_3          : 'COMP-3' ;
COMP_4          : 'COMP-4' ;
COMP_5          : 'COMP-5' ;
COMPUTATIONAL   : 'COMPUTATIONAL' ;
COMPUTATIONAL_1 : 'COMPUTATIONAL-1' ;
COMPUTATIONAL_2 : 'COMPUTATIONAL-2' ;
COMPUTATIONAL_3 : 'COMPUTATIONAL-3' ;
COMPUTATIONAL_4 : 'COMPUTATIONAL-4' ;
COMPUTATIONAL_5 : 'COMPUTATIONAL-5' ;
BINARY          : 'BINARY' ;
PACKED_DECIMAL  : 'PACKED-DECIMAL' ;
INDEX           : 'INDEX' ;
POINTER         : 'POINTER' ;
FUNCTION_POINTER : 'FUNCTION-POINTER' ;
PROCEDURE_POINTER : 'PROCEDURE-POINTER' ;

OCCURS          : 'OCCURS' ;
TIMES           : 'TIMES' ;
DEPENDING       : 'DEPENDING' ;
ASCENDING       : 'ASCENDING' ;
DESCENDING      : 'DESCENDING' ;
INDEXED_BY      : 'INDEXED' WS+ 'BY' ;

REDEFINES       : 'REDEFINES' ;
RENAMES         : 'RENAMES' ;
FILLER          : 'FILLER' ;
JUSTIFIED       : 'JUSTIFIED' ;
JUST            : 'JUST' ;
RIGHT           : 'RIGHT' ;
BLANK           : 'BLANK' ;
WHEN            : 'WHEN' ;
ZERO            : 'ZERO' ;
ZEROS           : 'ZEROS' ;
ZEROES          : 'ZEROES' ;
SYNCHRONIZED_KW : 'SYNCHRONIZED' ;
SYNC            : 'SYNC' ;
LEFT            : 'LEFT' ;

SIGN            : 'SIGN' ;
LEADING         : 'LEADING' ;
TRAILING        : 'TRAILING' ;
SEPARATE        : 'SEPARATE' ;
CHARACTER       : 'CHARACTER' ;

THRU            : 'THRU' ;
THROUGH         : 'THROUGH' ;

// ─── Procedure Division keywords ───

USING           : 'USING' ;
RETURNING       : 'RETURNING' ;
BY              : 'BY' ;
REFERENCE       : 'REFERENCE' ;
CONTENT         : 'CONTENT' ;

// ─── Statement keywords ───

MOVE            : 'MOVE' ;
CORRESPONDING   : 'CORRESPONDING' ;
CORR            : 'CORR' ;
TO              : 'TO' ;

ADD             : 'ADD' ;
SUBTRACT        : 'SUBTRACT' ;
MULTIPLY        : 'MULTIPLY' ;
DIVIDE          : 'DIVIDE' ;
INTO            : 'INTO' ;
GIVING          : 'GIVING' ;
REMAINDER       : 'REMAINDER' ;
COMPUTE         : 'COMPUTE' ;
ROUNDED         : 'ROUNDED' ;
SIZE            : 'SIZE' ;
ERROR           : 'ERROR' ;
NOT             : 'NOT' ;
ON              : 'ON' ;
END_ADD         : 'END-ADD' ;
END_SUBTRACT    : 'END-SUBTRACT' ;
END_MULTIPLY    : 'END-MULTIPLY' ;
END_DIVIDE      : 'END-DIVIDE' ;
END_COMPUTE     : 'END-COMPUTE' ;

IF              : 'IF' ;
THEN            : 'THEN' ;
ELSE            : 'ELSE' ;
END_IF          : 'END-IF' ;

EVALUATE        : 'EVALUATE' ;
ALSO            : 'ALSO' ;
OTHER           : 'OTHER' ;
TRUE_KW         : 'TRUE' ;
FALSE_KW        : 'FALSE' ;
ANY             : 'ANY' ;
END_EVALUATE    : 'END-EVALUATE' ;

PERFORM         : 'PERFORM' ;
UNTIL           : 'UNTIL' ;
VARYING         : 'VARYING' ;
AFTER           : 'AFTER' ;
BEFORE          : 'BEFORE' ;
TEST            : 'TEST' ;
WITH            : 'WITH' ;
FROM            : 'FROM' ;
END_PERFORM     : 'END-PERFORM' ;

GO              : 'GO' ;
CONTINUE        : 'CONTINUE' ;
STOP            : 'STOP' ;
RUN             : 'RUN' ;
EXIT            : 'EXIT' ;
PROGRAM         : 'PROGRAM' ;
PARAGRAPH       : 'PARAGRAPH' ;
// Note: SECTION already defined above

OPEN            : 'OPEN' ;
INPUT           : 'INPUT' ;
OUTPUT          : 'OUTPUT' ;
I_O             : 'I-O' ;
EXTEND          : 'EXTEND' ;
CLOSE           : 'CLOSE' ;
READ            : 'READ' ;
WRITE           : 'WRITE' ;
REWRITE         : 'REWRITE' ;
DELETE_KW       : 'DELETE' ;
START_KW        : 'START' ;
AT              : 'AT' ;
END_KW          : 'END' ;
INVALID         : 'INVALID' ;
END_READ        : 'END-READ' ;
END_WRITE       : 'END-WRITE' ;
END_REWRITE     : 'END-REWRITE' ;
END_DELETE      : 'END-DELETE' ;
END_START       : 'END-START' ;

DISPLAY         : 'DISPLAY' ;
UPON            : 'UPON' ;
ACCEPT          : 'ACCEPT' ;
DATE            : 'DATE' ;
DAY             : 'DAY' ;
DAY_OF_WEEK     : 'DAY-OF-WEEK' ;
TIME            : 'TIME' ;

CALL            : 'CALL' ;
EXCEPTION       : 'EXCEPTION' ;
OVERFLOW        : 'OVERFLOW' ;
END_CALL        : 'END-CALL' ;

INSPECT         : 'INSPECT' ;
TALLYING        : 'TALLYING' ;
CONVERTING      : 'CONVERTING' ;
ALL             : 'ALL' ;
FIRST           : 'FIRST' ;
INITIAL_KW      : 'INITIAL' ;
FOR             : 'FOR' ;
CHARACTERS      : 'CHARACTERS' ;

STRING_KW       : 'STRING' ;
DELIMITED       : 'DELIMITED' ;
END_STRING      : 'END-STRING' ;

UNSTRING        : 'UNSTRING' ;
DELIMITER       : 'DELIMITER' ;
COUNT           : 'COUNT' ;
END_UNSTRING    : 'END-UNSTRING' ;

SEARCH          : 'SEARCH' ;
END_SEARCH      : 'END-SEARCH' ;

SET             : 'SET' ;
UP              : 'UP' ;
DOWN            : 'DOWN' ;

INITIALIZE      : 'INITIALIZE' ;

GOBACK          : 'GOBACK' ;

// ─── FD clause keywords ───

STANDARD        : 'STANDARD' ;
OMITTED         : 'OMITTED' ;
LABEL           : 'LABEL' ;
RECORDS         : 'RECORDS' ;
BLOCK           : 'BLOCK' ;
CONTAINS        : 'CONTAINS' ;
ADVANCING       : 'ADVANCING' ;
NO              : 'NO' ;

// ─── Noise words ───

IS              : 'IS' ;
ARE             : 'ARE' ;
OF              : 'OF' ;
IN              : 'IN' ;
THAN            : 'THAN' ;
THE             : 'THE' ;
A_KW            : 'A' ;

// ─── Condition testing ───

NUMERIC         : 'NUMERIC' ;
ALPHABETIC      : 'ALPHABETIC' ;
ALPHABETIC_LOWER : 'ALPHABETIC-LOWER' ;
ALPHABETIC_UPPER : 'ALPHABETIC-UPPER' ;
POSITIVE        : 'POSITIVE' ;
NEGATIVE        : 'NEGATIVE' ;

// ─── Logical operators ───

AND             : 'AND' ;
OR              : 'OR' ;

// ─── Figurative constants ───

SPACE           : 'SPACE' ;
SPACES          : 'SPACES' ;
HIGH_VALUE      : 'HIGH-VALUE' ;
HIGH_VALUES     : 'HIGH-VALUES' ;
LOW_VALUE       : 'LOW-VALUE' ;
LOW_VALUES      : 'LOW-VALUES' ;
QUOTE           : 'QUOTE' ;
QUOTES          : 'QUOTES' ;
NULL_KW         : 'NULL' ;
NULLS           : 'NULLS' ;

// ─── Intrinsic functions ───

FUNCTION        : 'FUNCTION' ;

// ─── Symbols ───

DOT             : '.' ;
COMMA           : ',' ;
SEMICOLON       : ';' ;
COLON           : ':' ;
LPAREN          : '(' ;
RPAREN          : ')' ;
PLUS            : '+' ;
MINUS           : '-' ;
STAR            : '*' ;
SLASH           : '/' ;
DOUBLESTAR      : '**' ;
EQUAL           : '=' ;
GREATER         : '>' ;
LESS            : '<' ;
GREATER_EQUAL   : '>=' ;
LESS_EQUAL      : '<=' ;

// ─── Literals ───

INTEGERLITERAL  : [0-9]+ ;
DECIMALLITERAL  : [0-9]+ '.' [0-9]+ ;
STRINGLITERAL   : '"' (~["\r\n] | '""')* '"'
                | '\'' (~['\r\n] | '\'\'')* '\''
                ;

// ─── Identifier (COBOL words: letters, digits, hyphens) ───

IDENTIFIER      : [A-Z] [A-Z0-9-]* ;

// ─── Whitespace and comments ───

WS              : [ \t\r\n]+ -> skip ;
INLINE_COMMENT  : '*>' ~[\r\n]* -> skip ;

// ════════════════════════════════════════════════════════
// PIC_MODE: activated after PIC / PICTURE keyword
// Captures the entire PICTURE string as a single token
// ════════════════════════════════════════════════════════

mode PIC_MODE;

PIC_IS          : [ \t]+ 'IS'? [ \t]* -> skip ;
// Match PIC string characters; a dot is only valid when followed by a digit
// (decimal point like 9(3).99), not as a trailing statement-ending period.
PIC_STRING      : PIC_CHAR+ -> popMode ;
fragment PIC_CHAR
    : [A-Z0-9()+\-*/$,]
    | '.' [0-9A-Z()+\-*/$]    // dot only if followed by another PIC char (not space)
    ;
PIC_WS          : [ \t]+ -> skip ;
