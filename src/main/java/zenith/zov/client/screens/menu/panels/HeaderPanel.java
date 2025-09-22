package zenith.zov.client.screens.menu.panels;

import zenith.zov.base.font.Font;
import zenith.zov.base.font.Fonts;
import zenith.zov.base.theme.Theme;
import zenith.zov.client.modules.api.Category;
import zenith.zov.utility.render.display.base.BorderRadius;
import zenith.zov.utility.render.display.base.Rect;
import zenith.zov.utility.render.display.base.UIContext;
import zenith.zov.utility.render.display.base.color.ColorRGBA;
import zenith.zov.utility.render.display.shader.DrawUtil;

public class HeaderPanel {

    private final Runnable onThemeSwitch;
    private Rect themeButtonBounds = new Rect(0, 0, 0, 0);

    public HeaderPanel(Runnable onThemeSwitch) {
        this.onThemeSwitch = onThemeSwitch;
    }

    public void render(UIContext ctx,
                       float boxX,
                       float boxY,
                       float boxWidth,
                       float headerHeight,
                       float progress,
                       Theme theme,
                       Category category,
                       ColorRGBA titleColor,
                       ColorRGBA subtleColor) {

        float padding = 20f;
        float dotsY = boxY + padding;
        float dotsX = boxX + padding;
        float dotSpacing = 12f;
        float dotSize = 8f;

        ColorRGBA red = new ColorRGBA(255, 95, 86).mulAlpha(progress);
        ColorRGBA yellow = new ColorRGBA(255, 189, 46).mulAlpha(progress);
        ColorRGBA green = new ColorRGBA(39, 201, 63).mulAlpha(progress);

        ctx.drawRoundedRect(dotsX, dotsY, dotSize, dotSize, BorderRadius.all(dotSize / 2f), red);
        ctx.drawRoundedRect(dotsX + dotSpacing, dotsY, dotSize, dotSize, BorderRadius.all(dotSize / 2f), yellow);
        ctx.drawRoundedRect(dotsX + dotSpacing * 2f, dotsY, dotSize, dotSize, BorderRadius.all(dotSize / 2f), green);

        Font titleFont = Fonts.MEDIUM.getFont(9);
        String title = "Control Center";
        float titleWidth = titleFont.width(title);
        float titleX = boxX + (boxWidth - titleWidth) / 2f;
        float titleY = boxY + padding - 2f;
        ctx.drawText(titleFont, title, titleX, titleY, titleColor);

        Font subtitleFont = Fonts.MEDIUM.getFont(6.5f);
        String subtitle = category.getName() + " toolkit";
        float subtitleWidth = subtitleFont.width(subtitle);
        float subtitleX = boxX + (boxWidth - subtitleWidth) / 2f;
        float subtitleY = titleY + titleFont.height() + 4f;
        ctx.drawText(subtitleFont, subtitle, subtitleX, subtitleY, subtleColor);

        float themeSize = 30f;
        float themeX = boxX + boxWidth - padding - themeSize;
        float themeY = boxY + padding - 6f;

        themeButtonBounds = new Rect(themeX, themeY, themeSize, themeSize);

        ColorRGBA buttonBg = theme.getForegroundLight().mulAlpha(progress * 0.6f);
        ctx.drawRoundedRect(themeX, themeY, themeSize, themeSize, BorderRadius.all(12f), buttonBg);
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), themeX, themeY, themeSize, themeSize, -0.1f,
                BorderRadius.all(12f), theme.getForegroundStroke().mulAlpha(progress * 0.6f));

        Font iconFont = Fonts.ICONS.getFont(8);
        float iconX = themeX + (themeSize - iconFont.width(theme.getIcon())) / 2f;
        float iconY = themeY + (themeSize - iconFont.height()) / 2f;
        ctx.drawText(iconFont, theme.getIcon(), iconX, iconY, theme.getColor().mulAlpha(progress));
    }

    public boolean handleMouseClicked(double mouseX, double mouseY) {
        if (themeButtonBounds.contains(mouseX, mouseY)) {
            onThemeSwitch.run();
            return true;
        }
        return false;
    }
}
