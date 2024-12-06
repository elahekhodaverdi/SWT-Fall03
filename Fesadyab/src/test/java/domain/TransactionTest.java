package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest {
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
        transaction.setTransactionId(0);
    }

    @Test
    @DisplayName("Test equals with null object")
    public void testEqualsWithNull() {
        assertFalse(transaction.equals(null));
    }

    @Test
    @DisplayName("Test equals with different type of object")
    public void testEqualsForDifferentTypeOfObject() {
        Object wrongObject = new Object();
        assertFalse(transaction.equals(wrongObject));
    }

    @Test
    @DisplayName("Test equals returns true for same IDs, regardless of other fields")
    public void testEqualsOnlyComparesIds() {
        Transaction newTransaction = new Transaction() {{
            setTransactionId(0);
        }};
        assertTrue(transaction.equals(newTransaction));
    }

    @Test
    @DisplayName("Test equals returns false for different IDs")
    public void testEqualsForDifferentIds() {
        Transaction newTransaction = new Transaction() {{
            setTransactionId(6);
        }};
        assertFalse(transaction.equals(newTransaction));
    }

    @Test
    @DisplayName("Test getters and setters")
    public void testGettersAndSetter() {
        transaction.setTransactionId(1);
        transaction.setAccountId(2);
        transaction.setAmount(3);
        transaction.setDebit(false);
        assertEquals(1, transaction.getTransactionId());
        assertEquals(2, transaction.getAccountId());
        assertEquals(3, transaction.getAmount());
        assertEquals(false, transaction.isDebit());
    }

}
