package huuthang.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageTest_24133057 {
    @Test
    void calculatesUserPaginationWithSixItemsPerPage() {
        Page_24133057<Integer> page = new Page_24133057<>(Arrays.asList(1, 2, 3, 4, 5, 6), 2, 6, 14);
        assertEquals(3, page.getTotalPages());
        assertTrue(page.isHasPrevious());
        assertTrue(page.isHasNext());
    }

    @Test
    void keepsOnePageWhenThereAreNoItems() {
        Page_24133057<Integer> page = new Page_24133057<>(null, 1, 3, 0);
        assertEquals(1, page.getTotalPages());
        assertTrue(page.getItems().isEmpty());
        assertFalse(page.isHasPrevious());
        assertFalse(page.isHasNext());
    }
}
