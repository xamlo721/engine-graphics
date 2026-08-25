package com.xamlo.core.engine.graphics.components.gui;

import com.xamlo.core.engine.graphics.api.gui.elements.IImageView;
import com.xamlo.core.engine.graphics.api.gui.ITextureHandle;

/**
 * Отображение текстуры в пределах геометрии элемента. Все режимы растяжения из
 * {@link EnumImageStretch} учитываются при рендеринге (FILL — исторический путь
 * unit-mesh'а, остальные — CPU-side набор квадов с собственными UV); nine-patch
 * настраивается бордами через setNinePatchInsets. Подрегион исходной текстуры задаётся
 * через setRegion: кроп применяется первым (до маппинга режима и борд), а FILL с неполным
 * регионом рисуется тем же CPU-side путём, что и прочие режимы. Состояние режима
 * синхронизировано со storage уровня виджета ({@code setBackgroundStretchMode}),
 * который читает UIElementRenderer для любого текстурированного фона.
 */
public class ImageView extends Widget implements IImageView {

    protected ITextureHandle image;
    protected EnumImageStretch stretchMode;

    public ImageView() {
        super();
        this.image = null;
        this.stretchMode = EnumImageStretch.FILL;
        this.backgroundColor = new Color(0, 0, 0, 96);
    }

    @Override
    public void setImage(ITextureHandle image) {
        this.image = image;
        setBackgroundImage(image);
    }

    @Override
    public ITextureHandle getImage() {
        return this.image != null ? this.image : getBackgroundImage();
    }

    /** Режим растяжения изображения и состояния фона виджета (общий источник истины). */
    @Override
    public void setStretchMode(EnumImageStretch mode) {
        this.stretchMode = mode == null ? EnumImageStretch.FILL : mode;
        super.setBackgroundStretchMode(this.stretchMode);
    }

    @Override
    public EnumImageStretch getStretchMode() {
        return this.stretchMode;
    }

    /** Виджетный уровень задаёт режим напрямую — локальное зеркало остаётся в синхроне. */
    @Override
    public void setBackgroundStretchMode(EnumImageStretch mode) {
        super.setBackgroundStretchMode(mode);
        this.stretchMode = (mode == null) ? EnumImageStretch.FILL : mode;
    }

    @Override
    public EnumImageStretch getBackgroundStretchMode() {
        return getStretchMode();
    }

    /** Борды nine-patch для режима NINEPATCH (см. {@link ImageStretchLayout}). */
    @Override
    public void setNinePatchInsets(int left, int top, int right, int bottom) {
        super.setNinePatchInsets(left, top, right, bottom);
    }

    /** Подрегион исходной текстуры в пикселях от её левого верхнего угла; хранение — на уровне виджета. */
    @Override
    public void setRegion(int uPx, int vPx, int wPx, int hPx) {
        super.setRegion(uPx, vPx, wPx, hPx);
    }

    @Override
    public ImageStretchLayout.TextureRegion getRegion() {
        return super.getRegion();
    }

    /** Якорь обрезания/выравнивания изображения; хранение — на уровне виджета фона. */
    @Override
    public void setCropAnchor(float x, float y) {
        super.setCropAnchor(x, y);
    }

    @Override
    public ImageStretchLayout.StretchAnchor getCropAnchor() {
        return super.getCropAnchor();
    }

}
