package com.xamlo.core.engine.graphics.components.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ProgressBarTest {

    @Test
    public void fractionTracksValueWithinRange() {
        ProgressBar bar = new ProgressBar(); // по умолчанию 0..1
        assertEquals(0f, bar.getFillFraction(), 0.0001);

        bar.setValue(0.5f);
        assertEquals(0.5f, bar.getFillFraction(), 0.0001);

        bar.setValue(4f);
        assertEquals(1f, bar.getFillFraction(), "значение выше max обрезается до единицы");

        bar.setValue(-3f);
        assertEquals(0f, bar.getFillFraction(), "значение ниже min обрезается до нуля");
    }

    @Test
    public void percentageLabelFollowsTheClampedFraction() {
        ProgressBar bar = new ProgressBar();
        assertEquals("0%", bar.getText());

        bar.setValue(0.75f);
        assertEquals("75%", bar.getText());

        bar.setValue(9f);
        assertEquals("100%", bar.getText());
    }

    @Test
    public void boundariesAreIndependentAndValueStaysClamped() {
        ProgressBar bar = new ProgressBar(); // по умолчанию 0..1
        bar.setMinValue(10f);
        bar.setMaxValue(200f);
        bar.setValue(8f);
        assertEquals(10f, bar.getValue(), 0.0001, "значение поджимается к минимальной границе");

        // Границы меняются независимо друг от друга; инвертированный диапазон допустим,
        // но заполнение в нём равно нулю (защита в getFillFraction).
        bar.setMaxValue(5f);
        assertEquals(5f, bar.getMaxValue());
        assertEquals(0f, bar.getFillFraction(), "инвертированный диапазон не даёт заполнения");

        bar.setMinValue(300f);
        assertEquals(300f, bar.getMinValue());
        assertEquals(0f, bar.getFillFraction(), "минимум выше максимума — заполнение остаётся нулевым");
    }

}
