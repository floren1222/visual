package zenith.zov.client.screens.menu.panels;

import by.saskkeee.user.UserInfo;
import zenith.zov.Zenith;
import zenith.zov.base.animations.base.Animation;
import zenith.zov.base.animations.base.Easing;
import zenith.zov.base.font.Font;
import zenith.zov.base.font.Fonts;
import zenith.zov.base.theme.Theme;
import zenith.zov.client.modules.api.Category;
import zenith.zov.client.modules.api.Module;
import zenith.zov.utility.render.display.TextBox;
import zenith.zov.utility.render.display.base.BorderRadius;
import zenith.zov.utility.render.display.base.Rect;
import zenith.zov.utility.render.display.base.UIContext;
import zenith.zov.utility.render.display.base.color.ColorRGBA;
import zenith.zov.utility.render.display.shader.DrawUtil;

import java.util.Locale;

public class HeaderPanel {

    public Rect themeButtonBounds;
    public Rect searchBarBounds;
    public Rect layoutToggleButtonBounds;

    private final TextBox searchField;
    private final Runnable onLayoutToggle;
    private final Runnable onThemeSwitch;
    private Category lastCategory = Category.COMBAT;

    private final Animation animation = new Animation(300, 1, Easing.QUAD_IN_OUT);

    public HeaderPanel(TextBox searchField, Runnable onLayoutToggle, Runnable onThemeSwitch) {
        this.searchField = searchField;
        this.onLayoutToggle = onLayoutToggle;
        this.onThemeSwitch = onThemeSwitch;
    }

    public void render(UIContext ctx,
                       float boxX,
                       float boxY,
                       float boxWidth,
                       float heroHeight,
                       float progress,
                       Theme theme,
                       Category selectedCategory,
                       int columns) {

        animation.update(1);

        ColorRGBA accent = theme.getColor().mulAlpha(progress);
        ColorRGBA textColor = theme.getWhite().mulAlpha(progress);
        ColorRGBA subtleText = textColor.mulAlpha(0.65f);

        float padding = 28f;
        float titleX = boxX + padding;
        float titleY = boxY + padding;

        Font iconFont = Fonts.ICONS.getFont(9);
        Font titleFont = Fonts.MEDIUM.getFont(10);
        float iconY = titleY + (titleFont.height() - iconFont.height()) / 2f;
        ctx.drawText(iconFont, selectedCategory.getIcon(), titleX, iconY, accent);

        float labelX = titleX + iconFont.width(selectedCategory.getIcon()) + 10f;
        ctx.enableScissor((int) labelX, (int) (titleY - 2), (int) (labelX + 240), (int) (titleY + titleFont.height() + 20));
        float blend = animation.getValue();
        ctx.drawText(titleFont, selectedCategory.getName(), labelX, titleY, textColor.mulAlpha(blend));
        ctx.drawText(titleFont, lastCategory.getName(), labelX, titleY, textColor.mulAlpha(1 - blend));
        ctx.disableScissor();

        Font subtitleFont = Fonts.MEDIUM.getFont(6.5f);
        String currentSubtitle = formatSubtitle(selectedCategory);
        String previousSubtitle = formatSubtitle(lastCategory);
        float subtitleY = titleY + titleFont.height() + 6f;

        ctx.enableScissor((int) labelX, (int) (subtitleY - 2), (int) (labelX + 260), (int) (subtitleY + subtitleFont.height() + 6));
        ctx.drawText(subtitleFont, currentSubtitle, labelX, subtitleY, subtleText.mulAlpha(blend));
        ctx.drawText(subtitleFont, previousSubtitle, labelX, subtitleY, subtleText.mulAlpha(1 - blend));
        ctx.disableScissor();

        float statsY = subtitleY + subtitleFont.height() + 12f;
        renderStats(ctx, titleX, statsY, progress, theme, selectedCategory, textColor);

        renderProfileCard(ctx, boxX, boxY, boxWidth, heroHeight, progress, theme, textColor, subtleText);

        float controlsPadding = 24f;
        float controlsY = boxY + controlsPadding;
        float buttonSize = 26f;
        float gap = 10f;

        float searchWidth = 172f;
        float searchHeight = 28f;
        float searchX = boxX + boxWidth - controlsPadding - searchWidth;
        float layoutX = searchX - gap - buttonSize;
        float themeX = layoutX - gap - buttonSize;

        renderThemeButton(ctx, themeX, controlsY, buttonSize, progress, theme);
        renderLayoutButton(ctx, layoutX, controlsY, buttonSize, progress, theme, columns);
        renderSearchBar(ctx, searchX, controlsY, searchWidth, searchHeight, progress, theme);
    }

    private void renderStats(UIContext ctx,
                              float x,
                              float y,
                              float progress,
                              Theme theme,
                              Category cat,
                              ColorRGBA textColor) {
        int enabled = 0;
        int total = 0;
        for (Module module : Zenith.getInstance().getModuleManager().getModules()) {
            if (module.getCategory() == cat) {
                total++;
                if (module.isEnabled()) {
                    enabled++;
                }
            }
        }

        float width = 220f;
        float height = 32f;
        float padding = 16f;
        float dividerX = x + width / 2f;

        ColorRGBA panel = theme.getForegroundColor().mulAlpha(progress * 0.6f);
        ctx.drawRoundedRect(x, y, width, height, BorderRadius.all(12f), panel);
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), x, y, width, height, -0.1f, BorderRadius.all(12f), theme.getForegroundStroke().mulAlpha(progress * 0.5f));

        Font valueFont = Fonts.MEDIUM.getFont(7.5f);
        Font labelFont = Fonts.MEDIUM.getFont(6);
        float columnY = y + (height - valueFont.height() - labelFont.height() - 4f) / 2f;

        ctx.drawText(valueFont, String.valueOf(enabled), x + padding, columnY, textColor);
        ctx.drawText(labelFont, "active", x + padding, columnY + valueFont.height() + 4f, textColor.mulAlpha(0.7f));

        ctx.drawRoundedRect(dividerX - 0.5f, y + 8f, 1f, height - 16f, BorderRadius.all(0.5f), theme.getForegroundStroke().mulAlpha(progress * 0.6f));

        float rightX = dividerX + padding / 2f;
        ctx.drawText(valueFont, String.valueOf(total), rightX, columnY, textColor);
        ctx.drawText(labelFont, "modules", rightX, columnY + valueFont.height() + 4f, textColor.mulAlpha(0.7f));
    }

    private void renderProfileCard(UIContext ctx,
                                   float boxX,
                                   float boxY,
                                   float boxWidth,
                                   float heroHeight,
                                   float progress,
                                   Theme theme,
                                   ColorRGBA textColor,
                                   ColorRGBA subtleText) {
        float padding = 24f;
        float width = 188f;
        float height = 56f;
        float cardX = boxX + boxWidth - padding - width;
        float cardY = boxY + heroHeight - height - 20f;

        ColorRGBA cardColor = theme.getForegroundColor().mulAlpha(progress * 0.6f);
        ctx.drawRoundedRect(cardX, cardY, width, height, BorderRadius.all(14f), cardColor);
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), cardX, cardY, width, height, -0.1f, BorderRadius.all(14f), theme.getForegroundStroke().mulAlpha(progress * 0.7f));

        float avatarSize = 32f;
        float avatarX = cardX + 12f;
        float avatarY = cardY + (height - avatarSize) / 2f;
        DrawUtil.drawRoundedTexture(ctx.getMatrices(), Zenith.id("icons/avatar.png"), avatarX, avatarY, avatarSize, avatarSize, BorderRadius.all(10f), ColorRGBA.WHITE.mulAlpha(progress));
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), avatarX, avatarY, avatarSize, avatarSize, -0.1f, BorderRadius.all(10f), theme.getForegroundStroke().mulAlpha(progress * 0.55f));

        Font nameFont = Fonts.MEDIUM.getFont(7);
        Font roleFont = Fonts.MEDIUM.getFont(6);
        float textX = avatarX + avatarSize + 12f;
        float nameY = cardY + 14f;

        ctx.drawText(nameFont, UserInfo.getUsername(), textX, nameY, textColor);
        ctx.drawText(roleFont, UserInfo.getRole(), textX, nameY + nameFont.height() + 4f, subtleText);

        float badgeWidth = 52f;
        float badgeHeight = 18f;
        float badgeX = cardX + width - badgeWidth - 12f;
        float badgeY = cardY + 12f;
        ctx.drawRoundedRect(badgeX, badgeY, badgeWidth, badgeHeight, BorderRadius.all(6f), theme.getColor().mulAlpha(progress * 0.4f));
        Font badgeFont = Fonts.MEDIUM.getFont(6);
        ctx.drawText(badgeFont, "ONLINE", badgeX + (badgeWidth - badgeFont.width("ONLINE")) / 2f, badgeY + (badgeHeight - badgeFont.height()) / 2f, theme.getWhite().mulAlpha(progress));
    }

    private void renderThemeButton(UIContext ctx, float x, float y, float size, float progress, Theme theme) {
        this.themeButtonBounds = new Rect(x, y, size, size);
        drawIconButton(ctx, x, y, size, theme.getIcon(), progress, theme);
    }

    private void renderLayoutButton(UIContext ctx, float x, float y, float size, float progress, Theme theme, int columns) {
        this.layoutToggleButtonBounds = new Rect(x, y, size, size);
        String icon = columns == 1 ? "9" : columns == 2 ? ":" : ";";
        drawIconButton(ctx, x, y, size, icon, progress, theme);
    }

    private void drawIconButton(UIContext ctx, float x, float y, float size, String icon, float progress, Theme theme) {
        ColorRGBA background = theme.getForegroundColor().mulAlpha(progress * 0.8f);
        ctx.drawRoundedRect(x, y, size, size, BorderRadius.all(9f), background);
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), x, y, size, size, -0.1f, BorderRadius.all(9f), theme.getForegroundStroke().mulAlpha(progress * 0.7f));
        Font iconFont = Fonts.ICONS.getFont(7);
        float iconX = x + (size - iconFont.width(icon)) / 2f;
        float iconY = y + (size - iconFont.height()) / 2f;
        ctx.drawText(iconFont, icon, iconX, iconY, theme.getColor().mulAlpha(progress));
    }

    private void renderSearchBar(UIContext ctx,
                                 float x,
                                 float y,
                                 float width,
                                 float height,
                                 float progress,
                                 Theme theme) {
        this.searchBarBounds = new Rect(x, y, width, height);
        ColorRGBA background = theme.getForegroundColor().mulAlpha(progress * 0.75f);
        ctx.drawRoundedRect(x, y, width, height, BorderRadius.all(9f), background);
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), x, y, width, height, -0.1f, BorderRadius.all(9f), theme.getForegroundStroke().mulAlpha(progress * 0.7f));

        Font iconFont = Fonts.ICONS.getFont(6);
        float iconX = x + 10f;
        float iconY = y + (height - iconFont.height()) / 2f;
        ctx.drawText(iconFont, "4", iconX, iconY, theme.getGrayLight().mulAlpha(progress));

        float textX = iconX + iconFont.width("4") + 8f;
        float textY = y + (height - searchField.getFont().height()) / 2f;
        ctx.enableScissor((int) textX, (int) y, (int) (x + width - 10f), (int) (y + height));
        searchField.render(ctx, textX, textY, theme.getWhite().mulAlpha(progress), theme.getWhite().mulAlpha(progress * 0.5f));
        ctx.disableScissor();
    }

    private String formatSubtitle(Category category) {
        return "Curated tools for " + category.getName().toLowerCase(Locale.ROOT) + " adventures";
    }

    public void resetAnim(Category last, Category next) {
        animation.reset(0);
        this.lastCategory = last;
    }

    public boolean handleMouseClicked(double mouseX, double mouseY) {
        if (layoutToggleButtonBounds.contains(mouseX, mouseY)) {
            onLayoutToggle.run();
            return true;
        }
        if (themeButtonBounds.contains(mouseX, mouseY)) {
            onThemeSwitch.run();
            return true;
        }
        return searchBarBounds.contains(mouseX, mouseY);
    }
}
