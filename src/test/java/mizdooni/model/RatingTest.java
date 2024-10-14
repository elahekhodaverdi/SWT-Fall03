package mizdooni.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RatingTest {

    private Rating rating;

    @BeforeEach
    void setup() {
        rating = new Rating();
    }

    @Test
    void testGetStarCount() {
        rating.food = 3;
        rating.service = 3.5;
        rating.ambiance = 3.75;
        rating.overall = 3.5;
        assertEquals(4, rating.getStarCount());
    }
}
