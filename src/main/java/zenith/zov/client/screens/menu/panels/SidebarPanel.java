package zenith.zov.client.screens.menu.panels;

import lombok.Getter;
import net.minecraft.util.math.MathHelper;
import zenith.zov.base.animations.base.Animation;
import zenith.zov.base.animations.base.Easing;
import zenith.zov.base.font.Font;
import zenith.zov.base.font.Fonts;
import zenith.zov.base.theme.Theme;
import zenith.zov.client.modules.api.Category;
import zenith.zov.utility.math.MathUtil;
import zenith.zov.utility.render.display.base.BorderRadius;
import zenith.zov.utility.render.display.base.Rect;
import zenith.zov.utility.render.display.base.UIContext;
import zenith.zov.utility.render.display.base.color.ColorRGBA;
import zenith.zov.utility.render.display.shader.DrawUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SidebarPanel {

    @Getter
    private final Map<Category, Rect> categoryBounds = new HashMap<>();
    @Getter
    private Rect sidebarToggleButtonBounds;
    private Rect animRect = new Rect(0, 0, 0, 0);
    private final Animation animationChange = new Animation(200, 1, Easing.LINEAR);
    private final Animation sidebarAnimation;

    private final Consumer<Category> onCategorySelect;
    private final Runnable onSidebarToggle;
    private final List<SideBarCategory> categories = new ArrayList<>();

    public SidebarPanel(Animation sidebarAnimation,
                        boolean isSidebarExpanded,
                        Consumer<Category> onCategorySelect,
                        Runnable onSidebarToggle) {
        this.sidebarAnimation = sidebarAnimation;
        this.onCategorySelect = onCategorySelect;
        this.onSidebarToggle = onSidebarToggle;
        this.sidebarAnimation.setValue(isSidebarExpanded ? 1f : 0f);
        categories.addAll(Arrays.stream(Category.values()).map(SideBarCategory::new).toList());
    }

    public void render(UIContext ctx,
                       float startX,
                       float startY,
                       float width,
                       float height,
                       float progress,
                       Theme theme,
                       Category selectedCategory,
                       ColorRGBA accentColor,
                       ColorRGBA textColor,
                       ColorRGBA selectedTextColor) {

        categoryBounds.clear();

        float expandProgress = sidebarAnimation.update();
        float toggleSize = height;
        float toggleRadius = height / 2f;

        ColorRGBA toggleColor = theme.getForegroundColor().mulAlpha(progress * 0.75f);
        ctx.drawRoundedRect(startX, startY, toggleSize, height, BorderRadius.all(toggleRadius), toggleColor);
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), startX, startY, toggleSize, height, -0.1f, BorderRadius.all(toggleRadius), theme.getForegroundStroke().mulAlpha(progress * 0.6f));

        Font toggleFont = Fonts.MEDIUM.getFont(7);
        String toggleIcon = expandProgress > 0.5f ? "<" : ">";
        float toggleIconX = startX + (toggleSize - toggleFont.width(toggleIcon)) / 2f;
        float toggleIconY = startY + (height - toggleFont.height()) / 2f;
        ctx.drawText(toggleFont, toggleIcon, toggleIconX, toggleIconY, textColor.mulAlpha(progress));
        sidebarToggleButtonBounds = new Rect(startX, startY, toggleSize, height);

        float chipX = startX + toggleSize + 12f;
        float gap = 8f;

        Font labelFont = Fonts.MEDIUM.getFont(6.5f);

        animationChange.animateTo(1f);
        float animationValue = animationChange.update();

        for (SideBarCategory sideBarCategory : categories) {
            Category category = sideBarCategory.getCategory();
            float textWidth = labelFont.width(category.getName());
            float chipWidth = MathHelper.lerp(expandProgress, height, height + 18f + textWidth);
            float chipRadius = 12f;

            if (category == selectedCategory) {
                float targetX = chipX;
                float targetWidth = chipWidth;
                float targetHeight = height;
                float highlightX = MathUtil.interpolate(animRect.x(), targetX, animationValue);
                float highlightY = MathUtil.interpolate(animRect.y(), startY, animationValue);
                float highlightWidth = MathUtil.interpolate(animRect.width(), targetWidth, animationValue);
                float highlightHeight = MathUtil.interpolate(animRect.height(), targetHeight, animationValue);
                animRect = new Rect(highlightX, highlightY, highlightWidth, highlightHeight);

                ColorRGBA highlight = theme.getForegroundColor().mix(accentColor, 0.25f).mulAlpha(progress * 0.85f);
                ctx.drawRoundedRect(animRect.x(), animRect.y(), animRect.width(), animRect.height(), BorderRadius.all(chipRadius), highlight);
                DrawUtil.drawRoundedBorder(ctx.getMatrices(), animRect.x(), animRect.y(), animRect.width(), animRect.height(), -0.1f, BorderRadius.all(chipRadius), theme.getForegroundStroke().mulAlpha(progress * 0.6f));
            }

            sideBarCategory.render(ctx, chipX, startY, chipWidth, height, expandProgress, category == selectedCategory, selectedTextColor, textColor.mulAlpha(0.55f), theme.getGray().mulAlpha(progress * 0.6f), accentColor);
            categoryBounds.put(category, new Rect(chipX, startY, chipWidth, height));
            chipX += chipWidth + gap;
        }
    }

    public boolean handleMouseClicked(double mouseX, double mouseY) {
        if (sidebarToggleButtonBounds != null && sidebarToggleButtonBounds.contains(mouseX, mouseY)) {
            onSidebarToggle.run();
            return true;
        }

        for (Map.Entry<Category, Rect> entry : categoryBounds.entrySet()) {
            if (entry.getValue().contains(mouseX, mouseY)) {
                animationChange.animateTo(0);
                animationChange.setValue(0);
                onCategorySelect.accept(entry.getKey());
                return true;
            }
        }

        return false;
    }
}
