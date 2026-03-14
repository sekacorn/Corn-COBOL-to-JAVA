/*
 * BANK-ACCOUNT - Translated from COBOL by Corn Compiler
 * Original Author: SEKACORN
 * Translation Date: 2025-01-10
 * Code Generation Level: 2 (Idiomatic Java)
 *
 * 🤖 Generated with Corn COBOL-to-Java Compiler
 * Source: BANK-ACCOUNT.cbl:1:1
 */
package com.generated.cobol;

import com.sekacorn.corn.runtime.CobolFile;
import com.sekacorn.corn.runtime.CobolMath;
import com.sekacorn.corn.runtime.Picture;
import com.sekacorn.corn.runtime.IndexedFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Bank Account Processor
 * <p>
 * Demonstrates typical financial COBOL patterns translated to idiomatic Java.
 * This is a Level 2 translation with readable structure and modern Java idioms.
 */
public class BankAccount {

    // File handles
    private IndexedFile<AccountRecord> accountFile;

    // Working storage
    private FileStatus fileStatus = FileStatus.INITIAL;
    private Transaction transaction = new Transaction();
    private Totals totals = new Totals();
    private String formattedBalance;

    // Constants
    private static final BigDecimal MIN_BALANCE = new BigDecimal("100.00");
    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("500.00");

    /**
     * Main entry point
     * Source: BANK-ACCOUNT.cbl:65:1
     */
    public static void main(String[] args) {
        BankAccount program = new BankAccount();
        program.run();
    }

    /**
     * Main processing logic
     * Source: BANK-ACCOUNT.cbl:66:8
     */
    public void run() {
        initializeProgram();

        while (!fileStatus.isEndOfFile()) {
            processAccounts();
        }

        displayTotals();
        cleanupProgram();
    }

    /**
     * Initialize program resources
     * Source: BANK-ACCOUNT.cbl:72:8
     */
    private void initializeProgram() {
        System.out.println("=== BANK ACCOUNT PROCESSOR ===");

        accountFile = new IndexedFileImpl<>("ACCOUNTS.DAT", AccountRecord.class);
        CobolFile.FileStatus status = accountFile.open(CobolFile.OpenMode.I_O);

        if (!status.isSuccess()) {
            System.err.println("ERROR OPENING ACCOUNT FILE: " + status.getCode());
            System.exit(1);
        }

        fileStatus = FileStatus.OK;
    }

    /**
     * Process next account from file
     * Source: BANK-ACCOUNT.cbl:80:8
     */
    private void processAccounts() {
        CobolFile.Result<AccountRecord> result = accountFile.read();

        if (result.isAtEnd()) {
            fileStatus = FileStatus.END_OF_FILE;
        } else if (result.isSuccess()) {
            result.getRecord().ifPresent(this::processSingleAccount);
        }
    }

    /**
     * Process a single account record
     * Source: BANK-ACCOUNT.cbl:87:8
     */
    private void processSingleAccount(AccountRecord account) {
        totals.incrementRecordCount();

        // EVALUATE TRUE -> Switch on account type
        // Source: BANK-ACCOUNT.cbl:90:12
        switch (account.getType()) {
            case CHECKING -> processCheckingAccount(account);
            case SAVINGS -> processSavingsAccount(account);
            case BUSINESS -> processBusinessAccount(account);
            default -> System.out.println(
                "WARNING: Unknown account type for " + account.getNumber()
            );
        }
    }

    /**
     * Process checking account
     * Source: BANK-ACCOUNT.cbl:101:8
     */
    private void processCheckingAccount(AccountRecord account) {
        // Low balance check
        if (account.getBalance().compareTo(MIN_BALANCE) < 0) {
            System.out.println(
                "LOW BALANCE ALERT: Account " + account.getNumber() +
                " Balance: " + account.getBalance()
            );
        }

        // Display active accounts
        if (account.getStatus() == AccountStatus.ACTIVE) {
            formattedBalance = Picture.formatNumeric(
                account.getBalance(),
                "$$$,$$$,$$9.99"
            );
            System.out.println(
                "Checking Account: " + account.getNumber() +
                " | Balance: " + formattedBalance
            );
        }
    }

    /**
     * Process savings account with interest
     * Source: BANK-ACCOUNT.cbl:113:8
     */
    private void processSavingsAccount(AccountRecord account) {
        // Apply 2% interest
        // COMPUTE ACCT-BALANCE = ACCT-BALANCE * 1.02
        BigDecimal newBalance = account.getBalance()
            .multiply(new BigDecimal("1.02"))
            .setScale(2, RoundingMode.HALF_UP);

        CobolMath.Result result = CobolMath.compute(newBalance, 2, 11);

        if (result.hasError()) {
            System.out.println("SIZE ERROR on interest calculation");
        } else {
            account.setBalance(result.getValue());

            formattedBalance = Picture.formatNumeric(
                account.getBalance(),
                "$$$,$$$,$$9.99"
            );
            System.out.println(
                "Savings Account: " + account.getNumber() +
                " | Balance (with interest): " + formattedBalance
            );

            // Update record in file
            CobolFile.FileStatus status = accountFile.rewrite(account);
            if (!status.isSuccess()) {
                System.out.println(
                    "ERROR: Cannot update account " + account.getNumber()
                );
            }
        }
    }

    /**
     * Process business account
     * Source: BANK-ACCOUNT.cbl:127:8
     */
    private void processBusinessAccount(AccountRecord account) {
        BigDecimal balance = account.getBalance();

        if (CobolMath.isNumeric(balance.toString()) &&
            CobolMath.isPositive(balance)) {
            totals.addDeposit(balance);
        } else {
            System.out.println(
                "WARNING: Invalid balance for " + account.getNumber()
            );
        }
    }

    /**
     * Display processing summary
     * Source: BANK-ACCOUNT.cbl:134:8
     */
    private void displayTotals() {
        System.out.println();
        System.out.println("=== PROCESSING SUMMARY ===");
        System.out.println("Total Records Processed: " + totals.getRecordCount());

        formattedBalance = Picture.formatNumeric(
            totals.getTotalDeposits(),
            "$$$,$$$,$$9.99"
        );
        System.out.println("Total Deposits: " + formattedBalance);
    }

    /**
     * Cleanup and close resources
     * Source: BANK-ACCOUNT.cbl:141:8
     */
    private void cleanupProgram() {
        accountFile.close();
        System.out.println("=== PROCESSING COMPLETE ===");
    }

    // ===== NESTED CLASSES (Level 2: Modern Java Records) =====

    /**
     * Account record structure
     * Source: BANK-ACCOUNT.cbl:20:8
     */
    public static class AccountRecord {
        private String number;           // PIC 9(10)
        private String name;             // PIC X(30)
        private BigDecimal balance;      // PIC 9(9)V99
        private AccountType type;        // PIC X
        private AccountStatus status;    // PIC X

        // Getters and setters
        public String getNumber() { return number; }
        public void setNumber(String number) { this.number = number; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }

        public AccountType getType() { return type; }
        public void setType(AccountType type) { this.type = type; }

        public AccountStatus getStatus() { return status; }
        public void setStatus(AccountStatus status) { this.status = status; }
    }

    /**
     * Account type enumeration (from 88-levels)
     * Source: BANK-ACCOUNT.cbl:25:16
     */
    public enum AccountType {
        CHECKING('C'),
        SAVINGS('S'),
        BUSINESS('B');

        private final char code;

        AccountType(char code) {
            this.code = code;
        }

        public char getCode() {
            return code;
        }

        public static Optional<AccountType> fromCode(char code) {
            for (AccountType type : values()) {
                if (type.code == code) {
                    return Optional.of(type);
                }
            }
            return Optional.empty();
        }
    }

    /**
     * Account status enumeration (from 88-levels)
     * Source: BANK-ACCOUNT.cbl:30:16
     */
    public enum AccountStatus {
        ACTIVE('A'),
        CLOSED('C'),
        FROZEN('F');

        private final char code;

        AccountStatus(char code) {
            this.code = code;
        }

        public char getCode() {
            return code;
        }
    }

    /**
     * File status tracking
     * Source: BANK-ACCOUNT.cbl:36:8
     */
    private enum FileStatus {
        INITIAL,
        OK,
        END_OF_FILE,
        RECORD_NOT_FOUND;

        public boolean isEndOfFile() {
            return this == END_OF_FILE;
        }
    }

    /**
     * Transaction information
     * Source: BANK-ACCOUNT.cbl:41:8
     */
    private static class Transaction {
        private TransactionType type;
        private BigDecimal amount = BigDecimal.ZERO;

        public enum TransactionType {
            DEPOSIT, WITHDRAWAL, TRANSFER
        }
    }

    /**
     * Processing totals
     * Source: BANK-ACCOUNT.cbl:48:8
     */
    private static class Totals {
        private BigDecimal totalDeposits = BigDecimal.ZERO;      // PIC 9(11)V99
        private BigDecimal totalWithdrawals = BigDecimal.ZERO;   // PIC 9(11)V99
        private int recordCount = 0;                              // PIC 9(5)

        public void addDeposit(BigDecimal amount) {
            totalDeposits = totalDeposits.add(amount);
        }

        public void addWithdrawal(BigDecimal amount) {
            totalWithdrawals = totalWithdrawals.add(amount);
        }

        public void incrementRecordCount() {
            recordCount++;
        }

        public BigDecimal getTotalDeposits() { return totalDeposits; }
        public BigDecimal getTotalWithdrawals() { return totalWithdrawals; }
        public int getRecordCount() { return recordCount; }
    }

    /**
     * Simple implementation of IndexedFile for demonstration
     */
    private static class IndexedFileImpl<T> implements IndexedFile<T> {
        private final String fileName;
        private final Class<T> recordClass;
        private boolean isOpen = false;

        public IndexedFileImpl(String fileName, Class<T> recordClass) {
            this.fileName = fileName;
            this.recordClass = recordClass;
        }

        @Override
        public CobolFile.FileStatus open(CobolFile.OpenMode mode) {
            isOpen = true;
            return CobolFile.FileStatus.SUCCESS;
        }

        @Override
        public void close() {
            isOpen = false;
        }

        @Override
        public CobolFile.Result<T> read() {
            // Stub implementation
            return new CobolFile.Result<>(null, CobolFile.FileStatus.END_OF_FILE);
        }

        @Override
        public CobolFile.Result<T> read(Object key) {
            // Stub implementation
            return new CobolFile.Result<>(null, CobolFile.FileStatus.RECORD_NOT_FOUND);
        }

        @Override
        public CobolFile.FileStatus write(T record) {
            return CobolFile.FileStatus.SUCCESS;
        }

        @Override
        public CobolFile.FileStatus rewrite(T record) {
            return CobolFile.FileStatus.SUCCESS;
        }

        @Override
        public CobolFile.FileStatus delete() {
            return CobolFile.FileStatus.SUCCESS;
        }

        @Override
        public CobolFile.FileStatus start(Object key, KeyComparison comparison) {
            return CobolFile.FileStatus.SUCCESS;
        }

        @Override
        public CobolFile.FileStatus getStatus() {
            return isOpen ? CobolFile.FileStatus.SUCCESS : CobolFile.FileStatus.FILE_NOT_FOUND;
        }
    }
}
