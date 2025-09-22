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

                       float x,
                       float y,
                       float width,
                       float height,
                       float sidebarProgress,
                       boolean selected,
                       ColorRGBA textColor,
                       ColorRGBA textColorDisable,
                       ColorRGBA iconColorDisable,
                       ColorRGBA primary) {
        animationSwitch.animateTo(selected ? 1 : 0);
        animationSwitch.update();

        Font iconFont = Fonts.ICONS.getFont(7);
        Font labelFont = Fonts.MEDIUM.getFont(6.5f);

        float iconX = x + 12f;
        float iconY = y + (height - iconFont.height()) / 2f;
        float iconScale = MathHelper.lerp(sidebarProgress, 0.85f, 1f);

        ColorRGBA iconColor = iconColorDisable.mix(primary, animationSwitch.getValue());

        ctx.pushMatrix();
        ctx.getMatrices().translate(iconX + iconFont.width(category.getIcon()) / 2f, iconY + iconFont.height() / 2f, 0);
        ctx.getMatrices().scale(iconScale, iconScale, 1);
        ctx.getMatrices().translate(-(iconX + iconFont.width(category.getIcon()) / 2f), -(iconY + iconFont.height() / 2f), 0);
        ctx.drawText(iconFont, category.getIcon(), iconX, iconY, iconColor.mulAlpha(MathHelper.clamp(sidebarProgress, 0.35f, 1f)));
        ctx.popMatrix();

        float textX = iconX + iconFont.width(category.getIcon()) * iconScale + 8f;
        float textY = y + (height - labelFont.height()) / 2f;
        ColorRGBA blended = textColorDisable.mix(textColor, animationSwitch.getValue());
        ctx.drawText(labelFont, category.getName(), textX, textY, blended.mulAlpha(MathHelper.clamp(sidebarProgress, 0f, 1f)));
    }

        ColorRGBA color = inactiveColor.mix(activeColor, blend);
        ctx.drawText(font, label, textX, textY, color);
    }
}
