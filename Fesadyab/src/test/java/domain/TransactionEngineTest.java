package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class TransactionEngineTest {

    private TransactionEngine engine;


    private Transaction createTransaction(int transactionId, int accountId, int amount, boolean isDebit) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setDebit(isDebit);
        return transaction;
    }

    private Transaction createTransaction(int transactionId, int accountId, int amount) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setAccountId(accountId);
        transaction.setAmount(amount);
        return transaction;
    }

    @BeforeEach
    void setup() {
        engine = new TransactionEngine();
        engine = spy(engine);
    }

    @Test
    @DisplayName("Test get average transaction amount by account with no transactions")
    void testGetAverageTransactionAmountByAccountWithNoTransactions() {
        int result = engine.getAverageTransactionAmountByAccount(1);
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Test get average transaction amount by account with no matching transactions")
    void testGetAverageTransactionAmountByAccountWithNoMatchingTransactions() {
        engine.transactionHistory = new ArrayList<>(List.of(
                createTransaction(1, 2, 100),
                createTransaction(2, 2, 150)
        ));
        int result = engine.getAverageTransactionAmountByAccount(1);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Test get average transaction amount by account with single matching transaction")
    void testGetAverageTransactionAmountByAccountWithSingleMatchingTransaction() {
        engine.transactionHistory = new ArrayList<>(List.of(
                createTransaction(1, 1, 100)
        ));
        int result = engine.getAverageTransactionAmountByAccount(1);

        assertEquals(100, result);
    }

    @Test
    @DisplayName("Test get average transaction amount by account with multiple matching transactions")
    void testGetAverageTransactionAmountByAccountWithMultipleMatchingTransactions() {
        engine.transactionHistory = new ArrayList<>(List.of(
                createTransaction(1, 1, 150),
                createTransaction(2, 1, 250),
                createTransaction(3, 1, 500)
        ));
        int result = engine.getAverageTransactionAmountByAccount(1);

        assertEquals(300, result);
    }

    @Test
    @DisplayName("Test get average transaction amount by account with mixed account IDs")
    void testGetAverageTransactionAmountByAccountWithMixedAccountIds() {
        engine.transactionHistory = new ArrayList<>(List.of(
                createTransaction(1, 1, 150),
                createTransaction(2, 1, 250),
                createTransaction(3, 2, 500)
        ));
        int result = engine.getAverageTransactionAmountByAccount(1);

        assertEquals(200, result);
    }

    @Test
    @DisplayName("Test detect fraudulent transaction when debit exceeds twice the average")
    void testDetectFraudulentTransactionWithDebitExceedsTwiceAverage() {
        Transaction txn = createTransaction(1, 1, 150, true);

        when(engine.getAverageTransactionAmountByAccount(1)).thenReturn(50);
        int result = engine.detectFraudulentTransaction(txn);

        assertEquals(50, result);
    }

    @Test
    @DisplayName("Test detect fraudulent transaction with non-debit transaction")
    void testDetectFraudulentTransactionWithNonDebitTransaction() {
        Transaction txn = createTransaction(1, 1, 150, false);
        when(engine.getAverageTransactionAmountByAccount(1)).thenReturn(50);
        int result = engine.detectFraudulentTransaction(txn);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Test detect fraudulent transaction with debit transaction within limits")
    void testDetectFraudulentTransactionWithDebitTransactionWithinLimits() {
        Transaction txn = createTransaction(1, 1, 90, true);

        when(engine.getAverageTransactionAmountByAccount(1)).thenReturn(50);
        int result = engine.detectFraudulentTransaction(txn);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Test add transaction and detect fraud with duplicate transaction")
    void testAddTransactionAndDetectFraudWithDuplicateTransaction() {
        Transaction txn = createTransaction(1, 1, 100, true);
        engine.transactionHistory.add(txn);
        int fraudScore = engine.addTransactionAndDetectFraud(txn);

        assertEquals(0, fraudScore);
        assertEquals(1, engine.transactionHistory.size());
    }

    @Test
    @DisplayName("Test add transaction and detect fraud with fraudulent transaction")
    void testAddTransactionAndDetectFraudWithFraudulentTransaction() {
        engine.transactionHistory = new ArrayList<>(List.of(
                createTransaction(1, 1, 1000),
                createTransaction(2, 1, 1500)
        ));

        Transaction txn = createTransaction(3, 1, 2000, true);
        int fraudScore = engine.addTransactionAndDetectFraud(txn);

        assertEquals(500, fraudScore);
        assertEquals(3, engine.transactionHistory.size());
    }

    @Test
    @DisplayName("Test add transaction and detect fraud with not fraudulent transaction, returns pattern")
    void testAddTransactionAndDetectFraudWithNotFraudulentTransaction() {
        engine.transactionHistory = new ArrayList<>(List.of(
                createTransaction(1, 1, 150),
                createTransaction(2, 1, 250)
        ));

        Transaction txn = createTransaction(3, 1, 500, true);
        int fraudScore = engine.addTransactionAndDetectFraud(txn);

        assertEquals(100, fraudScore);
        assertEquals(3, engine.transactionHistory.size());
    }

    @Test
    @DisplayName("test transaction pattern, when the history is empty")
    public void testTransactionPatternWhenHistoryIsEmpty() {
        assertEquals(0, engine.getTransactionPatternAboveThreshold(100));
    }

    @Test
    @DisplayName("test transaction pattern with all transaction below the threshold")
    public void testTransactionPatternWithAllTransactionsBelowTheThreshold() {
        engine.transactionHistory = new ArrayList<>(List.of(
                createTransaction(1, 1, 100),
                createTransaction(2, 2, 200),
                createTransaction(3, 3, 300)
        ));

        assertEquals(0, engine.getTransactionPatternAboveThreshold(400));
    }

    @Test
    @DisplayName("test transaction pattern with all transaction below the threshold")
    public void testTransactionPatternWithTransactionsEqualToTheThreshold() {
        engine.transactionHistory = new ArrayList<>(List.of(
                createTransaction(1, 1, 300),
                createTransaction(2, 2, 300),
                createTransaction(3, 3, 300)
        ));
        assertEquals(0, engine.getTransactionPatternAboveThreshold(300));
    }

    @Test
    @DisplayName("test transaction pattern with no consistent pattern")
    public void testTransactionPatternWithNoPattern() {
        engine.transactionHistory = new ArrayList<>(List.of(
                createTransaction(1, 1, 10),
                createTransaction(2, 2, 20),
                createTransaction(2, 2, 30),
                createTransaction(3, 3, 50)
        ));
        assertEquals(0, engine.getTransactionPatternAboveThreshold(5));
    }

    @Test
    @DisplayName("Test transaction pattern with a consistent ascending pattern")
    public void testTransactionPatternWithAscendingPattern() {
        engine.transactionHistory = new ArrayList<>(List.of(
                createTransaction(1, 1, 10),
                createTransaction(2, 2, 20),
                createTransaction(3, 3, 30)
        ));
        assertEquals(10, engine.getTransactionPatternAboveThreshold(5));
    }

    @Test
    @DisplayName("Test transaction pattern with a consistent descending pattern")
    public void testTransactionPatternWithDescendingPattern() {
        engine.transactionHistory = new ArrayList<>(List.of(
                createTransaction(1, 1, 30),
                createTransaction(2, 2, 20),
                createTransaction(3, 3, 10)
        ));
        assertEquals(-10, engine.getTransactionPatternAboveThreshold(5));
    }

    @Test
    @DisplayName("Test transaction pattern with a consistent mixed Descending and Ascending pattern")
    public void testTransactionPatternWithMixedDescendingAndAscendingPattern() {
        engine.transactionHistory = new ArrayList<>(List.of(
                createTransaction(1, 1, 30),
                createTransaction(2, 2, 40),
                createTransaction(3, 3, 30)
        ));
        assertEquals(0, engine.getTransactionPatternAboveThreshold(5));
    }


}
