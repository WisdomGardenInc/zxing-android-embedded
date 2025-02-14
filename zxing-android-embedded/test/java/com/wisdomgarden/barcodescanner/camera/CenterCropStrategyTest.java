package com.wisdomgarden.barcodescanner.camera;

import com.wisdomgarden.barcodescanner.Size;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class CenterCropStrategyTest {
    private final PreviewScalingStrategy strategy = new CenterCropStrategy();

    private Size s(int width, int height) {
        return new Size(width, height);
    }

    @Test
    public void testOrdering() {
        List<Size> sizes = Arrays.asList(s(30, 40), s(40, 30), s(1000, 1000), s(120, 80), s(120, 90), s(120, 100), s(110, 80), s(120, 20), s(0, 0));
        List<Size> ordered = strategy.getBestPreviewOrder(sizes, s(120, 90));
        List<Size> expected = Arrays.asList(s(120, 90), s(120, 100), s(110, 80), s(120, 80), s(40, 30), s(30, 40), s(1000, 1000), s(120, 20), s(0, 0));
        assertEquals(expected, ordered);
    }
}
