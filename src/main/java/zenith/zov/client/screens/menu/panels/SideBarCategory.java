package zenith.zov.client.screens.menu.panels;

import lombok.Getter;
import zenith.zov.base.animations.base.Animation;
import zenith.zov.base.animations.base.Easing;
import zenith.zov.base.font.Font;
import zenith.zov.base.font.Fonts;
import zenith.zov.client.modules.api.Category;
import zenith.zov.utility.render.display.base.Rect;
import zenith.zov.utility.render.display.base.UIContext;
import zenith.zov.utility.render.display.base.color.ColorRGBA;

public class SideBarCategory {

    @Getter
    private final Category category;
    private final Animation selectionAnimation;

    public SideBarCategory(Category category) {
        this.category = category;
        this.selectionAnimation = new Animation(180, category == Category.COMBAT ? 1f : 0f, Easing.QUAD_IN_OUT);
    }

    public void render(UIContext ctx,
                       Rect bounds,
                       boolean selected,
                       ColorRGBA activeColor,
                       ColorRGBA inactiveColor) {
        selectionAnimation.animateTo(selected ? 1f : 0f);
        float blend = selectionAnimation.update();

        Font font = Fonts.MEDIUM.getFont(6.5f);
        String label = category.getName();
        float textX = bounds.x() + (bounds.width() - font.width(label)) / 2f;
        float textY = bounds.y() + (bounds.height() - font.height()) / 2f;

        ColorRGBA color = inactiveColor.mix(activeColor, blend);
        ctx.drawText(font, label, textX, textY, color);
    }
}
