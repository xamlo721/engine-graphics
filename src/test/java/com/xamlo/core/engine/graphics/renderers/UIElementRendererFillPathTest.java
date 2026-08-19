package com.xamlo.core.engine.graphics.renderers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.xamlo.core.engine.graphics.api.gui.IStretchedBackground;
import com.xamlo.core.engine.graphics.api.gui.IUIElement;
import com.xamlo.core.engine.graphics.components.AbstractTexture;
import com.xamlo.core.engine.graphics.components.gui.EnumImageStretch;
import com.xamlo.core.engine.graphics.components.gui.ImageStretchLayout;
import com.xamlo.core.engine.graphics.components.gui.ImageStretchLayout.NinePatchInsets;
import com.xamlo.core.engine.graphics.components.gui.ImageStretchLayout.StretchPiece;
import com.xamlo.core.engine.graphics.components.gui.NonInteractiveQuad;
import com.xamlo.core.engine.graphics.components.gui.PushButton;
import com.xamlo.core.engine.graphics.components.gui.UIElementGeometry;
import com.xamlo.core.engine.graphics.components.gui.Widget;

/**
 * Интеграционный (без GL) тест принятия решений рендерером фона: сцена-фиксюра в духе
 * GUIMainMenu. По умолчанию все текстурированные элементы должны резолвиться в легаси-FILL —
 * тот же единственный unit-mesh квад, что до E3 (побайтово идентичный путь). Явное
 * переключение режима меняет решение на батч фрагментов с собственными UV.
 */
public class UIElementRendererFillPathTest {

    private final AbstractTexture menuBg = stubTexture(1024, 512);
    private final AbstractTexture buttonBg = stubTexture(64, 32);

    /** Точная копия расчёта из UIElementRenderer.draw для элемента со своим режимом/бордами. */
    private static List<StretchPiece> rendererPiecesFor(IUIElement element, int textureWidth, int textureHeight) {
        IStretchedBackground stretched = (element instanceof IStretchedBackground s) ? s : null;
        EnumImageStretch mode = (stretched != null) ? stretched.getBackgroundStretchMode() : EnumImageStretch.FILL;
        NinePatchInsets insets = (stretched != null) ? stretched.getNinePatchInsets() : NinePatchInsets.NONE;
        Widget widget = (Widget) element;
        return ImageStretchLayout.layout(mode, textureWidth, textureHeight,
                widget.getGeometry().getWidth(), widget.getGeometry().getHeight(), insets);
    }

    /** Фиксюра: корневой фон + кнопка внутри — как в главном меню игры. */
    private NonInteractiveQuad buildMainMenuFixture(PushButton button) {
        NonInteractiveQuad root = new NonInteractiveQuad();
        root.resize(new UIElementGeometry(0, 0, 1920, 1080));
        // FILL по умолчанию — ровно то, что делает GUIMainMenu сегодня.
        root.setBackgroundImage(menuBg);

        button.resize(new UIElementGeometry(760, 400, 400, 80));
        button.setBackgroundImage(buttonBg);
        root.addChild(button);
        return root;
    }

    @Test
    public void defaultConfiguredSceneStaysOnLegacyFillPathForEveryTexturedWidget() {
        PushButton singleplayer = new PushButton("Singleplayer");
        NonInteractiveQuad root = buildMainMenuFixture(singleplayer);

        for (Object[] entry : new Object[][]{{root, menuBg}, {singleplayer, buttonBg}}) {
            IUIElement element = (IUIElement) entry[0];
            AbstractTexture texture = (AbstractTexture) entry[1];
            Widget widget = (Widget) entry[0];

            List<StretchPiece> pieces = rendererPiecesFor(element, texture.getWidth(), texture.getHeight());
            boolean plainFill = ImageStretchLayout.isPlainFill(pieces, texture.getWidth(), texture.getHeight(),
                    widget.getGeometry().getWidth(), widget.getGeometry().getHeight());
            assertTrue(plainFill, "элемент без явного режима растяжения обязан остаться на легаси-FILL пути: " + element);
        }
    }

    @Test
    public void explicitModesSwitchTheSameWidgetsToMultiPieceBatchPath() {
        PushButton options = new PushButton("Options");
        NonInteractiveQuad root = buildMainMenuFixture(options);

        // FIT: один фрагмент, но уже не «всё изображение во всю область» — батч-путь.
        options.setBackgroundStretchMode(EnumImageStretch.FIT);
        assertFalse(ImageStretchLayout.isPlainFill(
                rendererPiecesFor(options, buttonBg.getWidth(), buttonBg.getHeight()),
                buttonBg.getWidth(), buttonBg.getHeight(), 400f, 80f));

        // NINEPATCH с бордами: девять фрагментов вместо одного квада.
        configureNinePatch(options);
        List<StretchPiece> pieces = rendererPiecesFor(options, buttonBg.getWidth(), buttonBg.getHeight());
        assertEquals(9, pieces.size());
        boolean plainFill = ImageStretchLayout.isPlainFill(pieces, buttonBg.getWidth(), buttonBg.getHeight(), 400f, 80f);
        assertFalse(plainFill);
    }

    private static void configureNinePatch(PushButton widget) {
        widget.setNinePatchInsets(2, 1, 3, 2);
        widget.setBackgroundStretchMode(EnumImageStretch.NINEPATCH);
    }

    @Test
    public void childAbsoluteCoordinatesFollowParentChainAsTheBatcherExpects() {
        PushButton exit = new PushButton("Exit");
        NonInteractiveQuad root = buildMainMenuFixture(exit);

        // Батч пишет абсолютные координаты окна — проверяем цепочку absX/absY фиксьюры.
        assertEquals(760f, exit.getAbsX(), 1e-5);
        assertEquals(400f, exit.getAbsY(), 1e-5);
        assertEquals(0f, root.getAbsX(), 1e-5);
        assertEquals(0f, root.getAbsY(), 1e-5);
    }

    private static AbstractTexture stubTexture(int width, int height) {
        return new AbstractTexture("stub-" + width + "x" + height, width, height) {
            @Override
            public void bind() {
            }

            @Override
            public void unbind() {
            }
        };
    }

}
