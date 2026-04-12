/*
 * CobolParser.g4 - ANTLR4 parser grammar for COBOL
 * Author: Sekacorn
 * Created from scratch; no third-party code copied.
 *
 * Covers ANSI-85 COBOL subset matching the Corn IR model.
 */
parser grammar CobolParser;

options { tokenVocab = CobolLexer; }

// ════════════════════════════════════════════════════════
// Top-level
// ════════════════════════════════════════════════════════

compilationUnit
    : program EOF
    ;

program
    : identificationDivision
      environmentDivision?
      dataDivision?
      procedureDivision?
    ;

// ════════════════════════════════════════════════════════
// IDENTIFICATION DIVISION
// ════════════════════════════════════════════════════════

identificationDivision
    : (IDENTIFICATION | ID) DIVISION DOT
      programIdParagraph
      identificationParagraph*
    ;

programIdParagraph
    : PROGRAM_ID DOT? programName DOT
    ;

programName
    : IDENTIFIER
    | STRINGLITERAL
    ;

identificationParagraph
    : AUTHOR DOT? freeText DOT                  # authorParagraph
    | DATE_WRITTEN DOT? freeText DOT             # dateWrittenParagraph
    | DATE_COMPILED DOT? freeText DOT            # dateCompiledParagraph
    | INSTALLATION DOT? freeText DOT             # installationParagraph
    | SECURITY DOT? freeText DOT                 # securityParagraph
    | REMARKS DOT? freeText DOT                  # remarksParagraph
    ;

freeText
    : (~DOT)*
    ;

// ════════════════════════════════════════════════════════
// ENVIRONMENT DIVISION
// ════════════════════════════════════════════════════════

environmentDivision
    : ENVIRONMENT DIVISION DOT
      configurationSection?
      inputOutputSection?
    ;

configurationSection
    : CONFIGURATION SECTION DOT
      configurationParagraph*
    ;

configurationParagraph
    : SOURCE_COMPUTER DOT? freeText DOT          # sourceComputerParagraph
    | OBJECT_COMPUTER DOT? freeText DOT          # objectComputerParagraph
    | SPECIAL_NAMES DOT? specialNameEntry* DOT    # specialNamesParagraph
    ;

specialNameEntry
    : IDENTIFIER IS? IDENTIFIER
    ;

inputOutputSection
    : INPUT_OUTPUT SECTION DOT
      fileControlParagraph?
    ;

fileControlParagraph
    : FILE_CONTROL DOT
      selectClause+
    ;

selectClause
    : SELECT OPTIONAL? fileName=IDENTIFIER
      ASSIGN TO? assignName
      fileControlOption*
      DOT
    ;

assignName
    : IDENTIFIER
    | STRINGLITERAL
    ;

fileControlOption
    : ORGANIZATION IS? fileOrganization               # organizationOption
    | ACCESS MODE? IS? accessMode                      # accessModeOption
    | RECORD KEY? IS? keyName=IDENTIFIER               # recordKeyOption
    | ALTERNATE RECORD? KEY? IS? altKey=IDENTIFIER
      (WITH? DUPLICATES)?                              # alternateKeyOption
    | RELATIVE KEY? IS? relKey=IDENTIFIER              # relativeKeyOption
    | FILE_STATUS IS? statusVar=IDENTIFIER             # fileStatusOption
    | STATUS IS? statusVar=IDENTIFIER                  # statusOption
    ;

fileOrganization
    : SEQUENTIAL
    | INDEXED
    | RELATIVE
    ;

accessMode
    : SEQUENTIAL
    | RANDOM
    | DYNAMIC
    ;

// ════════════════════════════════════════════════════════
// DATA DIVISION
// ════════════════════════════════════════════════════════

dataDivision
    : DATA DIVISION DOT
      fileSection?
      workingStorageSection?
      localStorageSection?
      linkageSection?
    ;

fileSection
    : FILE SECTION DOT
      fileDescriptionEntry*
    ;

fileDescriptionEntry
    : FD fileName=IDENTIFIER fdClause* DOT
      dataItemEntry+
    ;

fdClause
    : RECORD (CONTAINS? INTEGERLITERAL (TO INTEGERLITERAL)? CHARACTERS?)?
    | BLOCK (CONTAINS? INTEGERLITERAL (TO INTEGERLITERAL)? (RECORDS | CHARACTERS)?)?
    | LABEL (RECORD | RECORDS) (IS | ARE)? (STANDARD | OMITTED)?
    | VALUE OF? IDENTIFIER IS? (IDENTIFIER | literal)
    | DATA (RECORD | RECORDS) (IS | ARE)? IDENTIFIER+
    ;

workingStorageSection
    : WORKING_STORAGE SECTION DOT
      dataItemEntry*
    ;

localStorageSection
    : LOCAL_STORAGE SECTION DOT
      dataItemEntry*
    ;

linkageSection
    : LINKAGE SECTION DOT
      dataItemEntry*
    ;

dataItemEntry
    : INTEGERLITERAL dataName (VALUE | VALUES) (IS | ARE)?
      valueSpec (commaOrSpace valueSpec)* DOT      # conditionNameEntry
    | levelNumber dataName dataItemClause* DOT    # regularDataItem
    ;

levelNumber
    : INTEGERLITERAL
    ;

dataName
    : IDENTIFIER
    | FILLER
    ;

dataItemClause
    : picClause
    | valueClause
    | usageClause
    | occursClause
    | redefinesClause
    | signClause
    | justifiedClause
    | blankWhenZeroClause
    | synchronizedClause
    ;

picClause
    : (PIC | PICTURE) IS? PIC_STRING
    ;

valueClause
    : VALUE IS? (PLUS | MINUS)? literal
    ;

usageClause
    : USAGE IS? usageType
    | usageType                   // COBOL allows COMPUTATIONAL etc. without USAGE keyword
    ;

usageType
    : DISPLAY
    | COMP | COMPUTATIONAL
    | COMP_1 | COMPUTATIONAL_1
    | COMP_2 | COMPUTATIONAL_2
    | COMP_3 | COMPUTATIONAL_3
    | COMP_4 | COMPUTATIONAL_4
    | COMP_5 | COMPUTATIONAL_5
    | BINARY
    | PACKED_DECIMAL
    | INDEX
    | POINTER
    | FUNCTION_POINTER
    | PROCEDURE_POINTER
    ;

occursClause
    : OCCURS INTEGERLITERAL (TO INTEGERLITERAL)?
      TIMES?
      (DEPENDING ON? IDENTIFIER)?
      ((ASCENDING | DESCENDING) KEY? IS? IDENTIFIER+)?
      (INDEXED BY? IDENTIFIER (IDENTIFIER)*)?
    ;

redefinesClause
    : REDEFINES IDENTIFIER
    ;

signClause
    : SIGN IS? (LEADING | TRAILING) (SEPARATE CHARACTER?)?
    ;

justifiedClause
    : (JUSTIFIED | JUST) RIGHT?
    ;

blankWhenZeroClause
    : BLANK WHEN? (ZERO | ZEROS | ZEROES)
    ;

synchronizedClause
    : (SYNCHRONIZED_KW | SYNC) (LEFT | RIGHT)?
    ;

valueSpec
    : (PLUS | MINUS)? literal ((THRU | THROUGH) (PLUS | MINUS)? literal)?
    ;

commaOrSpace
    :   // commas are skipped by lexer; this rule is now a no-op separator
    ;

// ════════════════════════════════════════════════════════
// PROCEDURE DIVISION
// ════════════════════════════════════════════════════════

procedureDivision
    : PROCEDURE DIVISION
      procedureUsingClause?
      procedureReturningClause?
      DOT
      procedureBody
    ;

procedureUsingClause
    : USING procedureUsingParam+
    ;

procedureUsingParam
    : (BY? (REFERENCE | VALUE | CONTENT))? IDENTIFIER
    ;

procedureReturningClause
    : RETURNING IDENTIFIER
    ;

procedureBody
    : (section | paragraph | sentence)*
    ;

section
    : sectionName SECTION INTEGERLITERAL? DOT
      paragraph*
    ;

sectionName
    : IDENTIFIER
    ;

paragraph
    : paragraphName DOT
      sentence*
    ;

paragraphName
    : IDENTIFIER
    ;

sentence
    : statement+ DOT
    ;

// ════════════════════════════════════════════════════════
// STATEMENTS
// ════════════════════════════════════════════════════════

statement
    : moveStatement
    | addStatement
    | subtractStatement
    | multiplyStatement
    | divideStatement
    | computeStatement
    | ifStatement
    | evaluateStatement
    | performStatement
    | goToStatement
    | stopStatement
    | exitStatement
    | displayStatement
    | acceptStatement
    | openStatement
    | closeStatement
    | readStatement
    | writeStatement
    | rewriteStatement
    | deleteStatement
    | startStatement
    | callStatement
    | inspectStatement
    | stringStatement
    | unstringStatement
    | searchStatement
    | setStatement
    | initializeStatement
    | continueStatement
    | gobackStatement
    | alterStatement
    ;

// ─── MOVE ───

moveStatement
    : MOVE (CORRESPONDING | CORR)? expression TO identifier+
    ;

// ─── Arithmetic ───

addStatement
    : ADD expression+ TO identifier+
      givingClause? roundedClause? onSizeErrorClause? notSizeErrorClause? END_ADD?
    | ADD expression+ givingClause
      roundedClause? onSizeErrorClause? notSizeErrorClause? END_ADD?
    ;

subtractStatement
    : SUBTRACT expression+ FROM identifier+
      givingClause? roundedClause? onSizeErrorClause? notSizeErrorClause? END_SUBTRACT?
    | SUBTRACT expression+ FROM expression givingClause
      roundedClause? onSizeErrorClause? notSizeErrorClause? END_SUBTRACT?
    ;

multiplyStatement
    : MULTIPLY expression BY expression givingClause
      roundedClause? onSizeErrorClause? notSizeErrorClause? END_MULTIPLY?
    | MULTIPLY expression BY multiplyTarget+
      onSizeErrorClause? notSizeErrorClause? END_MULTIPLY?
    ;

multiplyTarget
    : identifier roundedClause?
    ;

divideStatement
    : DIVIDE expression (INTO | BY) expression givingClause remainderClause?
      roundedClause? onSizeErrorClause? notSizeErrorClause? END_DIVIDE?
    | DIVIDE expression INTO divideTarget+
      onSizeErrorClause? notSizeErrorClause? END_DIVIDE?
    ;

divideTarget
    : identifier roundedClause?
    ;

computeStatement
    : COMPUTE identifier+ EQUAL expression
      roundedClause? onSizeErrorClause? notSizeErrorClause? END_COMPUTE?
    ;

givingClause
    : GIVING givingTarget+
    ;

givingTarget
    : identifier roundedClause?
    ;

remainderClause
    : REMAINDER identifier
    ;

roundedClause
    : ROUNDED
    ;

onSizeErrorClause
    : ON? SIZE ERROR statement+
    ;

notSizeErrorClause
    : NOT ON? SIZE ERROR statement+
    ;

// ─── IF ───

ifStatement
    : IF condition THEN?
      statement+
      (ELSE statement+)?
      END_IF?
    ;

// ─── EVALUATE ───

evaluateStatement
    : EVALUATE evaluateSubject (ALSO evaluateSubject)*
      evaluateWhenClause+
      evaluateWhenOther?
      END_EVALUATE
    ;

evaluateSubject
    : TRUE_KW
    | FALSE_KW
    | expression
    ;

evaluateWhenClause
    : WHEN evaluateCondition (ALSO evaluateCondition)*
      statement+
    ;

evaluateCondition
    : ANY
    | TRUE_KW
    | FALSE_KW
    | NOT? expression (THRU expression)?
    ;

evaluateWhenOther
    : WHEN OTHER statement+
    ;

// ─── PERFORM ───

performStatement
    : PERFORM procedureRef performOption?              # performProcedure
    | PERFORM performOption? statement+ END_PERFORM    # performInline
    ;

procedureRef
    : IDENTIFIER ((THRU | THROUGH) IDENTIFIER)?
    ;

performOption
    : expression TIMES                                 # performTimes
    | (WITH? TEST (BEFORE | AFTER))? UNTIL condition   # performUntil
    | (WITH? TEST (BEFORE | AFTER))?
      VARYING IDENTIFIER FROM expression
      BY expression UNTIL condition
      afterVaryingClause*                              # performVarying
    ;

afterVaryingClause
    : AFTER IDENTIFIER FROM expression
      BY expression UNTIL condition
    ;

// ─── GO TO ───

goToStatement
    : GO TO? IDENTIFIER+ (DEPENDING ON? identifier)?
    ;

// ─── STOP / EXIT / CONTINUE / GOBACK ───

stopStatement
    : STOP RUN expression?
    ;

exitStatement
    : EXIT (PROGRAM | PARAGRAPH | SECTION | PERFORM)?
    ;

continueStatement
    : CONTINUE
    ;

gobackStatement
    : GOBACK
    ;

// ─── I/O ───

openStatement
    : OPEN openFileClause+
    ;

openFileClause
    : (INPUT | OUTPUT | I_O | EXTEND) IDENTIFIER+
    ;

closeStatement
    : CLOSE IDENTIFIER+
    ;

readStatement
    : READ IDENTIFIER NEXT? RECORD? (INTO identifier)?
      (KEY IS? identifier)?
      atEndClause?
      notAtEndClause?
      invalidKeyClause?
      notInvalidKeyClause?
      END_READ?
    ;

atEndClause
    : AT? END_KW statement+
    ;

notAtEndClause
    : NOT AT? END_KW statement+
    ;

invalidKeyClause
    : INVALID KEY? statement+
    ;

notInvalidKeyClause
    : NOT INVALID KEY? statement+
    ;

writeStatement
    : WRITE IDENTIFIER (FROM expression)?
      writeAdvancingClause?
      invalidKeyClause?
      END_WRITE?
    ;

writeAdvancingClause
    : (BEFORE | AFTER) ADVANCING? (expression (LINE | LINES)? | PAGE)
    ;

rewriteStatement
    : REWRITE IDENTIFIER (FROM expression)?
      invalidKeyClause?
      END_REWRITE?
    ;

deleteStatement
    : DELETE_KW IDENTIFIER RECORD?
      invalidKeyClause?
      END_DELETE?
    ;

startStatement
    : START_KW IDENTIFIER (KEY IS? (EQUAL | EQUAL_WORD | GREATER | GREATER_WORD | LESS | LESS_WORD | GREATER_EQUAL | LESS_EQUAL)? identifier)?
      invalidKeyClause?
      END_START?
    ;

// ─── DISPLAY / ACCEPT ───

displayStatement
    : DISPLAY expression+ (UPON IDENTIFIER)? (WITH? NO ADVANCING)?
    ;

acceptStatement
    : ACCEPT identifier (FROM (DATE | DAY | DAY_OF_WEEK | TIME | IDENTIFIER))?
    ;

// ─── CALL ───

callStatement
    : CALL expression
      (USING callUsingArg+)?
      (RETURNING identifier)?
      onExceptionClause?
      END_CALL?
    ;

callUsingArg
    : (BY? (REFERENCE | VALUE | CONTENT))? expression
    ;

onExceptionClause
    : ON? EXCEPTION statement+
      (NOT ON? EXCEPTION statement+)?
    ;

// ─── INSPECT ───

inspectStatement
    : INSPECT identifier inspectOp
    ;

inspectOp
    : TALLYING identifier FOR inspectTallyingClause+     # inspectTallying
    | REPLACING inspectReplacingClause+                   # inspectReplacing
    | CONVERTING expression TO expression
      (BEFORE INITIAL_KW? expression)?
      (AFTER INITIAL_KW? expression)?                     # inspectConverting
    ;

inspectTallyingClause
    : (ALL | LEADING) expression
      (BEFORE INITIAL_KW? expression)?
      (AFTER INITIAL_KW? expression)?
    | CHARACTERS
      (BEFORE INITIAL_KW? expression)?
      (AFTER INITIAL_KW? expression)?
    ;

inspectReplacingClause
    : (ALL | LEADING | FIRST) expression BY expression
      (BEFORE INITIAL_KW? expression)?
      (AFTER INITIAL_KW? expression)?
    | CHARACTERS BY expression
      (BEFORE INITIAL_KW? expression)?
      (AFTER INITIAL_KW? expression)?
    ;

// ─── STRING ───

stringStatement
    : STRING_KW stringSendingClause+
      INTO identifier
      (WITH? POINTER identifier)?
      (ON? OVERFLOW statement+)?
      (NOT ON? OVERFLOW statement+)?
      END_STRING?
    ;

stringSendingClause
    : expression DELIMITED BY? (SIZE | expression)
    ;

// ─── UNSTRING ───

unstringStatement
    : UNSTRING identifier
      (DELIMITED BY? ALL? expression (OR ALL? expression)*)?
      INTO unstringIntoClause+
      (WITH? POINTER identifier)?
      (TALLYING IN? identifier)?
      (ON? OVERFLOW statement+)?
      (NOT ON? OVERFLOW statement+)?
      END_UNSTRING?
    ;

unstringIntoClause
    : identifier (DELIMITER IN? identifier)? (COUNT IN? identifier)?
    ;

// ─── SEARCH ───

searchStatement
    : SEARCH ALL? IDENTIFIER (VARYING identifier)?
      (AT? END_KW statement+)?
      searchWhenClause+
      END_SEARCH?
    ;

searchWhenClause
    : WHEN condition statement+
    ;

// ─── SET ───

setStatement
    : SET identifier+ TO (TRUE_KW | FALSE_KW)                # setToTrueStatement
    | SET identifier+ TO expression                            # setToValueStatement
    | SET identifier+ (UP | DOWN) BY expression                # setUpDownStatement
    ;

// ─── INITIALIZE ───

initializeStatement
    : INITIALIZE identifier+
    ;

// ─── ALTER ───

alterStatement
    : ALTER (IDENTIFIER TO (PROCEED TO)? IDENTIFIER)+
    ;

// ════════════════════════════════════════════════════════
// EXPRESSIONS & CONDITIONS
// ════════════════════════════════════════════════════════

condition
    : combinableCondition ((AND | OR) combinableCondition)*
    ;

combinableCondition
    : NOT? simpleCondition
    ;

simpleCondition
    : classCondition
    | relationCondition
    | LPAREN condition RPAREN
    | conditionNameCondition
    ;

conditionNameCondition
    : identifier
    ;

classCondition
    : identifier IS NOT?
      (NUMERIC | ALPHABETIC | ALPHABETIC_LOWER | ALPHABETIC_UPPER
      | POSITIVE | NEGATIVE | ZERO | ZEROS | ZEROES)
    ;

relationCondition
    : expression relationalOperator expression
    ;

relationalOperator
    : (IS | ARE)? NOT? ( GREATER_WORD THAN? | LESS_WORD THAN?
      | EQUAL_WORD TO? | EQUAL TO?
      | GREATER_EQUAL | LESS_EQUAL | GREATER | LESS | EQUAL)
    ;

expression
    : arithmeticExpression
    ;

arithmeticExpression
    : term ((PLUS | MINUS) term)*
    ;

term
    : power ((STAR | SLASH) power)*
    ;

power
    : unaryExpression (DOUBLESTAR unaryExpression)?
    ;

unaryExpression
    : (PLUS | MINUS)? primaryExpression
    ;

primaryExpression
    : literal                                    # literalExpr
    | functionCall                               # functionExpr
    | identifier                                 # identifierExpr
    | LPAREN expression RPAREN                   # parenExpr
    ;

// ─── Identifiers (qualified, subscripted, reference-modified) ───

identifier
    : IDENTIFIER qualifiedTail* subscriptPart? referenceModification?
    ;

qualifiedTail
    : (OF | IN) IDENTIFIER
    ;

subscriptPart
    : LPAREN expression (expression)* RPAREN
    ;

referenceModification
    : LPAREN expression COLON expression? RPAREN
    ;

// ─── Literals ───

literal
    : INTEGERLITERAL
    | DECIMALLITERAL
    | STRINGLITERAL
    | figurativeConstant
    ;

figurativeConstant
    : ZERO | ZEROS | ZEROES
    | SPACE | SPACES
    | HIGH_VALUE | HIGH_VALUES
    | LOW_VALUE | LOW_VALUES
    | QUOTE | QUOTES
    | NULL_KW | NULLS
    | ALL literal
    ;

// ─── Function calls ───

functionCall
    : FUNCTION IDENTIFIER LPAREN expression (expression)* RPAREN
    ;

