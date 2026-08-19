package com.xamlo.core.engine.graphics.components.gui;

import com.xamlo.core.engine.graphics.api.gui.IDragListener;
import com.xamlo.core.engine.graphics.api.gui.IDraggable;
import com.xamlo.core.engine.graphics.api.gui.IDropTarget;
import com.xamlo.core.engine.graphics.api.gui.IPointerListener;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.api.gui.elements.IWidget;

/**
 * Универсальный скроллбар: трек (собственный фоновый цвет) и бегунок-полоска
 * ({@link NonInteractiveQuad} со своим цветом), ориентация HORIZONTAL/VERTICAL.
 * Значение value лежит в [0..maxValue], где maxValue — прокручиваемое расстояние
 * вдоль оси (пиксели содержимого за пределами окна). Размер бегунка отражает долю
 * видимого содержимого ({@link #setVisibleFraction}), минимум задаётся долей трека
 * и/или абсолютной длиной.
 *
 * Ввод без нового механизма событий: как у {@link Slider}, встроенный drag-слушатель
 * переводит абсолютную координату курсора в значение пропорционально длине трека
 * (клик по любой точке полосы = прыжок к этой позиции, drag продолжает то же маппинг);
 * stationary-клики доставляет тот же маппинг через pointer-слушатель. Точку hit-test'а
 * внутри полосы разрешает сама полоса (бегунок неучастен), поэтому жест начинается
 * из любого места трека. Полоса не является drop-target'ом.
 */
public class ScrollBar extends Widget implements IDraggable {

    /** Нейтральные цвета по умолчанию; потребитель переопределяет сеттерами. */
    public static final Color DEFAULT_TRACK_COLOR = new Color(38, 38, 46, 220);
    public static final Color DEFAULT_THUMB_COLOR = new Color(170, 170, 178, 255);

    protected EnumOrientation orientation;

    private float value;
    private float maxValue;
    private float visibleFraction = 1f;
    private float minThumbFraction;
    private float minThumbLengthPx;

    protected Color trackColor;
    protected Color thumbColor;
    private NonInteractiveQuad thumb;
    private OnScroll scrollListener;
    private IPointerListener activePointerListener;
    private IDragListener activeDragListener;

    /** Колбэк изменения значения setValue() (включая корректировки клампами). */
    @FunctionalInterface
    public interface OnScroll {
        void onChanged(ScrollBar bar, float newValue);
    }

    public ScrollBar() {
        this(null, EnumOrientation.VERTICAL);
    }

    public ScrollBar(EnumOrientation orientation) {
        this(null, orientation);
    }

    public ScrollBar(IWidget parent) {
        this(parent, EnumOrientation.VERTICAL);
    }

    public ScrollBar(IWidget parent, EnumOrientation orientation) {
        super();
        if (parent != null) {
            setParent(parent);
            parent.addChild(this);
        }
        this.orientation = orientation == null ? EnumOrientation.VERTICAL : orientation;
        this.trackColor = DEFAULT_TRACK_COLOR;
        this.thumbColor = DEFAULT_THUMB_COLOR;
        setBackgroundColor(this.trackColor);
        this.thumb = new NonInteractiveQuad();
        this.thumb.setBackgroundColor(this.thumbColor);
        addChild(this.thumb);
        this.activeDragListener = new DragHandler();
        this.activePointerListener = new PointerHandler();
        repositionThumb();
    }

    // --- значение и диапазон -------------------------------------------------

    public float getValue() {
        return value;
    }

    /** Устанавливает позицию вдоль оси (клампится в [0..maxValue]); без изменений — тихо. */
    public void setValue(float newValue) {
        float clamped = maxValue > 0f ? Math.max(0f, Math.min(maxValue, newValue)) : 0f;
        if (clamped == value) {
            return;
        }
        value = clamped;
        repositionThumb();
        if (scrollListener != null) {
            scrollListener.onChanged(this, value);
        }
    }

    /** Прокручиваемое расстояние вдоль оси (px). Текущее значение удерживается внутри. */
    public void setMaxValue(float maxValue) {
        float range = Math.max(0f, maxValue);
        if (range == this.maxValue && clampIntoRange(value) == value) {
            return;
        }
        this.maxValue = range;
        boolean adjusted = false;
        float v = clampIntoRange(value);
        if (v != value) {
            value = v;
            adjusted = true;
        }
        repositionThumb();
        if (adjusted && scrollListener != null) {
            scrollListener.onChanged(this, value);
        }
    }

    public float getMaxValue() {
        return maxValue;
    }

    private float clampIntoRange(float v) {
        return maxValue > 0f ? Math.max(0f, Math.min(maxValue, v)) : 0f;
    }

    // --- размер бегунка ------------------------------------------------------

    /** Доля содержимого в окне (viewport/content ≤ 1): определяет длину бегунка. */
    public void setVisibleFraction(float fraction) {
        if (fraction <= 0f) {
            return;
        }
        visibleFraction = Math.min(1f, fraction);
        repositionThumb();
    }

    public float getVisibleFraction() {
        return visibleFraction;
    }

    /** Минимальная длина бегунка как доля трека (например 0.2 — не меньше пятой части). */
    public void setMinThumbFraction(float fraction) {
        minThumbFraction = Math.max(0f, Math.min(1f, fraction));
        repositionThumb();
    }

    public float getMinThumbFraction() {
        return minThumbFraction;
    }

    /** Минимальная абсолютная длина бегунка (px), поверх долевой. */
    public void setMinThumbLengthPx(float pixels) {
        minThumbLengthPx = Math.max(0f, pixels);
        repositionThumb();
    }

    public float getMinThumbLengthPx() {
        return minThumbLengthPx;
    }

    // --- ориентация и цвета --------------------------------------------------

    public EnumOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(EnumOrientation orientation) {
        this.orientation = orientation == null ? EnumOrientation.VERTICAL : orientation;
        repositionThumb();
    }

    private boolean isVertical() {
        return orientation != EnumOrientation.HORIZONTAL;
    }

    public NonInteractiveQuad getThumb() {
        return thumb;
    }

    /** Цвет трека (null — нейтральный по умолчанию). */
    public void setTrackColor(Color color) {
        this.trackColor = color == null ? DEFAULT_TRACK_COLOR : color;
        setBackgroundColor(this.trackColor);
    }

    public Color getTrackColor() {
        return trackColor;
    }

    /** Цвет бегунка (null — нейтральный по умолчанию). */
    public void setThumbColor(Color color) {
        this.thumbColor = color == null ? DEFAULT_THUMB_COLOR : color;
        thumb.setBackgroundColor(this.thumbColor);
    }

    public Color getThumbColor() {
        return thumbColor;
    }

    /** Слушатель изменений значения (null — отключить). */
    public void setScrollListener(OnScroll listener) {
        this.scrollListener = listener;
    }

    // --- геометрия бегунка ---------------------------------------------------

    /** Пересчитывает позицию/размер бегунка при текущем значении и геометрии. */
    protected void repositionThumb() {
        UIElementGeometry g = getGeometry();
        int len = isVertical() ? g.getHeight() : g.getWidth();
        if (len <= 0) {
            return;
        }
        int thumbLen = computeThumbLength(len, visibleFraction, minThumbFraction, minThumbLengthPx);
        int pos = computeThumbPosition(value, maxValue, len, thumbLen);
        if (isVertical()) {
            thumb.resize(new UIElementGeometry(0, pos, g.getWidth(), thumbLen));
        } else {
            thumb.resize(new UIElementGeometry(pos, 0, thumbLen, g.getHeight()));
        }
    }

    /** Длина бегунка: доля видимого содержимого с минимумом по доле трека и px. */
    static int computeThumbLength(int trackLen, float visibleFraction, float minFraction, float minPx) {
        if (trackLen <= 0) {
            return 0;
        }
        float frac = clampUnit(visibleFraction > 0f ? Math.min(1f, visibleFraction) : 1f);
        int base = Math.round(trackLen * frac);
        int minimum = Math.max(Math.round(trackLen * clampUnit(minFraction)), (int) Math.round(Math.max(0f, minPx)));
        return Math.min(Math.max(base, minimum), trackLen);
    }

    /** Позиция начала бегунка вдоль оси для значения value в диапазоне [0..maxValue]. */
    static int computeThumbPosition(float value, float maxValue, int trackLen, int thumbLen) {
        if (maxValue <= 0f || trackLen <= 0) {
            return 0;
        }
        int usable = Math.max(0, trackLen - thumbLen);
        if (usable <= 0) {
            // Бегунок на всю полосу — зафиксирован у начала.
            return 0;
        }
        float v = Math.max(0f, Math.min(maxValue, value));
        return (int) Math.round(v / maxValue * usable);
    }

    /** Пропорциональный маппинг координаты курсора (локально вдоль оси) в значение. */
    static float cursorToValue(float localCoordAlongAxis, int trackLen, float maxValue) {
        if (trackLen <= 0 || maxValue <= 0f) {
            return 0f;
        }
        return maxValue * clampUnit(localCoordAlongAxis / trackLen);
    }

    private static float clampUnit(float f) {
        return Math.max(0f, Math.min(1f, f));
    }

    // --- ввод: drag-жест и клики ---------------------------------------------

    /** По умолчанию слушатель не заменяется: null возвращает встроенную логику полосы. */
    @Override
    public void setDragListener(IDragListener listener) {
        this.activeDragListener = (listener == null) ? new DragHandler() : listener;
    }

    /** Возвращает активный drag-слушатель: по умолчанию встроенная логика полосы. */
    @Override
    public IDragListener getDragListener() {
        return activeDragListener;
    }

    /** Pointer-обработчик; null возвращает встроенный (клик по треку = прыжок). */
    @Override
    public void setPointerListener(IPointerListener listener) {
        this.activePointerListener = (listener == null) ? new PointerHandler() : listener;
    }

    /** Возвращает активный pointer-слушатель: по умолчанию встроенный. */
    @Override
    public IPointerListener getPointerListener() {
        return activePointerListener;
    }

    /** Полоса не принимает перетаскиваемые элементы — подсветка таргета отключена. */
    @Override
    public void setDropActive(boolean active) {
        // no-op: ScrollBar не drop-target
    }

    @Override
    public boolean isDropActive() {
        return false;
    }

    private void commitCursor(float xCoord, float yCoord) {
        if (isVertical()) {
            setValue(cursorToValue(yCoord - getAbsY(), getGeometry().getHeight(), maxValue));
        } else {
            setValue(cursorToValue(xCoord - getAbsX(), getGeometry().getWidth(), maxValue));
        }
    }

    /** Встроенное поведение: позиция курсора вдоль оси пропорционально задаёт значение. */
    protected class DragHandler implements IDragListener {

        @Override
        public void onDragStart(IDraggable element, float xCoord, float yCoord) {
            // Без состояния: каждый DRAG независимо переводится в значение.
        }

        @Override
        public void onDrag(IDraggable element, float xCoord, float yCoord, float deltaX, float deltaY) {
            commitCursor(xCoord, yCoord);
        }

        @Override
        public void onDrop(IDraggable element, IDropTarget target, float xCoord, float yCoord) {
        }

        @Override
        public void onDragEnd(IDraggable element, float xCoord, float yCoord) {
        }

        @Override
        public void onDragEnterTarget(IDraggable element, IDropTarget target) {
        }

        @Override
        public void onDragLeaveTarget(IDraggable element, IDropTarget target) {
        }

    }

    /** Встроенный клик по любой точке полосы — то же пропорциональное маппинг-значение. */
    protected class PointerHandler implements IPointerListener {

        @Override
        public void onPointer(IUIElement element, float xCoord, float yCoord) {
            commitCursor(xCoord, yCoord);
        }

    }

}
