package com.xamlo.core.engine.graphics.api.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Z-порядок (слои) UI-элементов. Эффективный индекс элемента — максимум его
 * собственного индекса и индексов всех предков: контейнер с высоким индексом
 * (например, открытый попап) поднимает вместе с собой всех потомков.
 */
public final class ZOrder {

    private ZOrder() {
    }

    public static int effectiveZIndex(IUIElement element) {
        int z = 0;
        IUIElement current = element;
        while (current != null) {
            z = Math.max(z, current.getZIndex());
            current = current.hasParent() ? current.getParent() : null;
        }
        return z;
    }

    /**
     * Стабильная копия списка, отсортированная по эффективному z-индексу.
     * Элементы с равным индексом сохраняют исходный (деревенный) порядок,
     * поэтому результат совпадает с порядком отрисовки.
     */
    public static List<IUIElement> sorted(List<? extends IUIElement> elements) {
        List<IUIElement> result = new ArrayList<>(elements);
        result.sort(Comparator.comparingInt(ZOrder::effectiveZIndex));
        return result;
    }

}
