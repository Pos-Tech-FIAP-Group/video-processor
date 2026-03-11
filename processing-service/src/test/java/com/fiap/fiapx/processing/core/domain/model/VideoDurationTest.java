package com.fiap.fiapx.processing.core.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VideoDurationTest {

    @Test
    void shouldCreateValidDuration() {
        VideoDuration duration = new VideoDuration(120.0);
        assertTrue(duration.isValid());
        assertEquals(2.0, duration.toMinutes());
        assertEquals(120.0 / 3600.0, duration.toHours());
    }

    @Test
    void shouldBeInvalidWhenZero() {
        VideoDuration zero = new VideoDuration(0.0);
        assertFalse(zero.isValid());
    }

    @Test
    void shouldThrowWhenNegative() {
        assertThrows(IllegalArgumentException.class, () -> new VideoDuration(-10.0));
    }
}

