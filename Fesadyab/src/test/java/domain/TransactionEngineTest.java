package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.Mockito.*;

class TransactionEngineTest {

    private TransactionEngine engine;

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
                new Transaction() {{
                    setTransactionId(1);
                    setAccountId(2);
                    setAmount(100);
                }},
                new Transaction() {{
                    setTransactionId(2);
                    setAccountId(2);
                    setAmount(150);
                }}
        ));
        int result = engine.getAverageTransactionAmountByAccount(1);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Test get average transaction amount by account with single matching transaction")
    void testGetAverageTransactionAmountByAccountWithSingleMatchingTransaction() {
        engine.transactionHistory = new ArrayList<>(List.of(
                new Transaction() {{
                    setTransactionId(1);
                    setAccountId(1);
                    setAmount(100);
                }}
        ));
        int result = engine.getAverageTransactionAmountByAccount(1);

        assertEquals(100, result);
    }

    @Test
    @DisplayName("Test get average transaction amount by account with multiple matching transactions")
    void testGetAverageTransactionAmountByAccountWithMultipleMatchingTransactions() {
        engine.transactionHistory = new ArrayList<>(List.of(
                new Transaction() {{
                    setTransactionId(1);
                    setAccountId(1);
                    setAmount(150);
                }},
                new Transaction() {{
                    setTransactionId(2);
                    setAccountId(1);
                    setAmount(250);
                }},
                new Transaction() {{
                    setTransactionId(3);
                    setAccountId(1);
                    setAmount(500);
                }}
        ));
        int result = engine.getAverageTransactionAmountByAccount(1);

        assertEquals(300, result);
    }

    @Test
    @DisplayName("Test get average transaction amount by account with mixed account IDs")
    void testGetAverageTransactionAmountByAccountWithMixedAccountIds() {
        engine.transactionHistory = new ArrayList<>(List.of(
                new Transaction() {{
                    setTransactionId(1);
                    setAccountId(1);
                    setAmount(150);
                }},
                new Transaction() {{
                    setTransactionId(2);
                    setAccountId(1);
                    setAmount(250);
                }},
                new Transaction() {{
                    setTransactionId(3);
                    setAccountId(2);
                    setAmount(500);
                }}
        ));
        int result = engine.getAverageTransactionAmountByAccount(1);

        assertEquals(200, result);
    }

    @Test
    @DisplayName("Test detect fraudulent transaction when debit exceeds twice the average")
    void testDetectFraudulentTransactionWithDebitExceedsTwiceAverage() {
        Transaction txn = new Transaction() {{
            setTransactionId(1);
            setAccountId(1);
            setAmount(150);
            setDebit(true);
        }};

        when(engine.getAverageTransactionAmountByAccount(1)).thenReturn(50);
        int result = engine.detectFraudulentTransaction(txn);

        assertEquals(50, result);
    }

    @Test
    @DisplayName("Test detect fraudulent transaction with non-debit transaction")
    void testDetectFraudulentTransactionWithNonDebitTransaction() {
        Transaction txn = new Transaction() {{
            setTransactionId(1);
            setAccountId(1);
            setAmount(150);
            setDebit(false);
        }};

        when(engine.getAverageTransactionAmountByAccount(1)).thenReturn(50);
        int result = engine.detectFraudulentTransaction(txn);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Test detect fraudulent transaction with debit transaction within limits")
    void testDetectFraudulentTransactionWithDebitTransactionWithinLimits() {
        Transaction txn = new Transaction() {{
            setTransactionId(1);
            setAccountId(1);
            setAmount(90);
            setDebit(true);
        }};

        when(engine.getAverageTransactionAmountByAccount(1)).thenReturn(50);
        int result = engine.detectFraudulentTransaction(txn);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Test add transaction and detect fraud with duplicate transaction")
    void testAddTransactionAndDetectFraudWithDuplicateTransaction() {
        Transaction txn = new Transaction() {{
            setTransactionId(1);
            setAccountId(1);
            setAmount(100);
            setDebit(true);
        }};
        engine.transactionHistory.add(txn);
        int fraudScore = engine.addTransactionAndDetectFraud(txn);

        assertEquals(0, fraudScore);
        assertEquals(1, engine.transactionHistory.size());
    }

    @Test
    @DisplayName("Test add transaction and detect fraud with fraudulent transaction")
    void testAddTransactionAndDetectFraudWithFraudulentTransaction() {
        engine.transactionHistory = new ArrayList<>(List.of(
                new Transaction() {{
                    setTransactionId(1);
                    setAccountId(1);
                    setAmount(150);
                }},
                new Transaction() {{
                    setTransactionId(2);
                    setAccountId(1);
                    setAmount(250);
                }}
        ));

        Transaction txn = new Transaction() {{
            setTransactionId(3);
            setAccountId(1);
            setAmount(500);
            setDebit(true);
        }};
        int fraudScore = engine.addTransactionAndDetectFraud(txn);

        assertEquals(100, fraudScore);
        assertEquals(3, engine.transactionHistory.size());
    }
}
