       IDENTIFICATION DIVISION.
       PROGRAM-ID. BANK-ACCOUNT.
       AUTHOR. SEKACORN.
       DATE-WRITTEN. 2025-01-10.
      *****************************************************************
      * SAMPLE BANKING PROGRAM - ACCOUNT PROCESSING
      * Demonstrates typical financial COBOL patterns
      *****************************************************************

       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT ACCOUNT-FILE
               ASSIGN TO "ACCOUNTS.DAT"
               ORGANIZATION IS INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS ACCT-NUMBER
               FILE STATUS IS WS-FILE-STATUS.

       DATA DIVISION.
       FILE SECTION.
       FD  ACCOUNT-FILE.
       01  ACCOUNT-RECORD.
           05  ACCT-NUMBER             PIC 9(10).
           05  ACCT-NAME               PIC X(30).
           05  ACCT-BALANCE            PIC 9(9)V99.
           05  ACCT-TYPE               PIC X.
               88  CHECKING            VALUE 'C'.
               88  SAVINGS             VALUE 'S'.
               88  BUSINESS            VALUE 'B'.
           05  ACCT-STATUS             PIC X.
               88  ACTIVE              VALUE 'A'.
               88  CLOSED              VALUE 'C'.
               88  FROZEN              VALUE 'F'.

       WORKING-STORAGE SECTION.
       01  WS-FILE-STATUS              PIC XX.
           88  FILE-OK                 VALUE '00'.
           88  END-OF-FILE             VALUE '10'.
           88  RECORD-NOT-FOUND        VALUE '23'.

       01  WS-TRANSACTION.
           05  WS-TRANS-TYPE           PIC X.
               88  DEPOSIT             VALUE 'D'.
               88  WITHDRAWAL          VALUE 'W'.
               88  TRANSFER            VALUE 'T'.
           05  WS-TRANS-AMOUNT         PIC 9(9)V99.

       01  WS-TOTALS.
           05  WS-TOTAL-DEPOSITS       PIC 9(11)V99 VALUE ZEROS.
           05  WS-TOTAL-WITHDRAWALS    PIC 9(11)V99 VALUE ZEROS.
           05  WS-RECORD-COUNT         PIC 9(5) VALUE ZEROS.

       01  WS-FORMATTED-BALANCE        PIC $$$,$$$,$$9.99.

       01  WS-CONSTANTS.
           05  WS-MIN-BALANCE          PIC 9(7)V99 VALUE 100.00.
           05  WS-OVERDRAFT-LIMIT      PIC 9(7)V99 VALUE 500.00.

       PROCEDURE DIVISION.
       MAIN-PROCESSING.
           PERFORM INITIALIZE-PROGRAM
           PERFORM PROCESS-ACCOUNTS UNTIL END-OF-FILE
           PERFORM DISPLAY-TOTALS
           PERFORM CLEANUP-PROGRAM
           STOP RUN.

       INITIALIZE-PROGRAM.
           DISPLAY "=== BANK ACCOUNT PROCESSOR ===".
           OPEN I-O ACCOUNT-FILE
           IF NOT FILE-OK
               DISPLAY "ERROR OPENING ACCOUNT FILE: " WS-FILE-STATUS
               STOP RUN
           END-IF.

       PROCESS-ACCOUNTS.
           READ ACCOUNT-FILE NEXT RECORD
               AT END
                   SET END-OF-FILE TO TRUE
               NOT AT END
                   PERFORM PROCESS-SINGLE-ACCOUNT
           END-READ.

       PROCESS-SINGLE-ACCOUNT.
           ADD 1 TO WS-RECORD-COUNT

           EVALUATE TRUE
               WHEN CHECKING
                   PERFORM PROCESS-CHECKING-ACCOUNT
               WHEN SAVINGS
                   PERFORM PROCESS-SAVINGS-ACCOUNT
               WHEN BUSINESS
                   PERFORM PROCESS-BUSINESS-ACCOUNT
               WHEN OTHER
                   DISPLAY "WARNING: Unknown account type for "
                           ACCT-NUMBER
           END-EVALUATE.

       PROCESS-CHECKING-ACCOUNT.
           IF ACCT-BALANCE < WS-MIN-BALANCE
               DISPLAY "LOW BALANCE ALERT: Account " ACCT-NUMBER
                       " Balance: " ACCT-BALANCE
           END-IF

           IF ACTIVE
               MOVE ACCT-BALANCE TO WS-FORMATTED-BALANCE
               DISPLAY "Checking Account: " ACCT-NUMBER
                       " | Balance: " WS-FORMATTED-BALANCE
           END-IF.

       PROCESS-SAVINGS-ACCOUNT.
           COMPUTE ACCT-BALANCE = ACCT-BALANCE * 1.02
               ON SIZE ERROR
                   DISPLAY "SIZE ERROR on interest calculation"
           END-COMPUTE

           MOVE ACCT-BALANCE TO WS-FORMATTED-BALANCE
           DISPLAY "Savings Account: " ACCT-NUMBER
                   " | Balance (with interest): " WS-FORMATTED-BALANCE

           REWRITE ACCOUNT-RECORD
               INVALID KEY
                   DISPLAY "ERROR: Cannot update account " ACCT-NUMBER
           END-REWRITE.

       PROCESS-BUSINESS-ACCOUNT.
           IF ACCT-BALANCE IS NUMERIC AND ACCT-BALANCE IS POSITIVE
               ADD ACCT-BALANCE TO WS-TOTAL-DEPOSITS
           ELSE
               DISPLAY "WARNING: Invalid balance for " ACCT-NUMBER
           END-IF.

       DISPLAY-TOTALS.
           DISPLAY " "
           DISPLAY "=== PROCESSING SUMMARY ==="
           DISPLAY "Total Records Processed: " WS-RECORD-COUNT
           MOVE WS-TOTAL-DEPOSITS TO WS-FORMATTED-BALANCE
           DISPLAY "Total Deposits: " WS-FORMATTED-BALANCE.

       CLEANUP-PROGRAM.
           CLOSE ACCOUNT-FILE
           DISPLAY "=== PROCESSING COMPLETE ===".
