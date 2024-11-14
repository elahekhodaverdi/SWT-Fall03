package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionTest {
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = new Transaction() {{
            setTransactionId(0);
        }};
    }

    @Test
    public void testEqualsForDifferentTypeOfObject() {
        Object wrongObject = new Object();
        assertFalse(transaction.equals(wrongObject));
    }

    @Test
    public void testEqualsComparesIds() {
        Transaction newTransaction = new Transaction() {{
            setTransactionId(0);
        }};
        assertTrue(transaction.equals(newTransaction));
    }

    @Test
    public void testEqualsForDifferentIds() {
        Transaction newTransaction = new Transaction() {{
            setTransactionId(6);
        }};
        assertFalse(transaction.equals(newTransaction));
    }

}
