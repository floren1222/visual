package zenith.zov.client.screens.menu.panels;

import lombok.Getter;
import net.minecraft.util.math.MathHelper;
import zenith.zov.base.animations.base.Animation;
import zenith.zov.base.animations.base.Easing;
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
import java.util.LinkedHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SidebarPanel {

    private final List<SideBarCategory> categories = new ArrayList<>();
    private final Consumer<Category> onCategorySelect;

    @Getter
    private final Map<Category, Rect> categoryBounds = new LinkedHashMap<>();

    private final Map<Category, Rect> categoryBounds = new HashMap<>();
    @Getter
    private Rect sidebarToggleButtonBounds;
    private Rect animRect = new Rect(0, 0, 0, 0);
    private final Animation animationChange = new Animation(200, 1, Easing.LINEAR);
    private final Animation sidebarAnimation;

    private final Animation highlightAnimation = new Animation(220, 1f, Easing.QUAD_IN_OUT);
    private Rect highlightFrom = new Rect(0, 0, 0, 0);
    private Rect highlightTo = new Rect(0, 0, 0, 0);
    private Rect highlightCurrent = new Rect(0, 0, 0, 0);
    private boolean initialized;

    public SidebarPanel(Consumer<Category> onCategorySelect) {
        this.onCategorySelect = onCategorySelect;
        Arrays.stream(Category.values()).map(SideBarCategory::new).forEach(categories::add);
    }

    public float render(UIContext ctx,
                         float startX,
                         float startY,
                         float width,
                         float chipHeight,
                         float progress,
                         Theme theme,
                         Category selectedCategory,
                         ColorRGBA accent,
                         ColorRGBA activeText,
                         ColorRGBA inactiveText) {

        categoryBounds.clear();

        int total = categories.size();
        if (total == 0) {
            return 0f;
        }

        float gap = 8f;
        int perRow = Math.max(1, Math.min(total, MathHelper.clamp((int) Math.floor(width / 160f), 1, total)));
        int rows = Math.max(1, (int) Math.ceil(total / (float) perRow));
        float trackPadding = 10f;
        float chipWidth = (width - trackPadding * 2f - gap * (perRow - 1)) / perRow;
        float containerHeight = rows * chipHeight + (rows - 1) * gap + trackPadding * 2f;

        ColorRGBA trackColor = theme.getForegroundLight().mulAlpha(progress * 0.5f);
        Rect container = new Rect(startX, startY, width, containerHeight);

        ctx.drawRoundedRect(container.x(), container.y(), container.width(), container.height(), BorderRadius.all(18f), trackColor);
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), container.x(), container.y(), container.width(), container.height(), -0.1f,
                BorderRadius.all(18f), theme.getForegroundStroke().mulAlpha(progress * 0.4f));

        Rect selectedRect = null;

        for (int index = 0; index < total; index++) {
            SideBarCategory category = categories.get(index);
            int row = index / perRow;
            int column = index % perRow;

            float x = startX + trackPadding + column * (chipWidth + gap);
            float y = startY + trackPadding + row * (chipHeight + gap);

            Rect rect = new Rect(x, y, chipWidth, chipHeight);
            categoryBounds.put(category.getCategory(), rect);

            if (category.getCategory() == selectedCategory) {
                selectedRect = rect;
            }
        }

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

        if (selectedRect != null) {
            if (!initialized || highlightCurrent.width() == 0) {
                highlightFrom = selectedRect;
                highlightCurrent = selectedRect;
                initialized = true;
            }

            highlightTo = selectedRect;
            float t = highlightAnimation.update();
            float newX = MathUtil.interpolate(highlightFrom.x(), highlightTo.x(), t);
            float newY = MathUtil.interpolate(highlightFrom.y(), highlightTo.y(), t);
            float newW = MathUtil.interpolate(highlightFrom.width(), highlightTo.width(), t);
            float newH = MathUtil.interpolate(highlightFrom.height(), highlightTo.height(), t);
            highlightCurrent = new Rect(newX, newY, newW, newH);

            ColorRGBA highlight = theme.getForegroundColor().mix(accent, 0.35f).mulAlpha(progress);
            ctx.drawRoundedRect(highlightCurrent.x(), highlightCurrent.y(), highlightCurrent.width(), highlightCurrent.height(),
                    BorderRadius.all(chipHeight / 2f), highlight);
        }

        for (SideBarCategory category : categories) {
            Rect bounds = categoryBounds.get(category.getCategory());
            if (bounds == null) {
                continue;
            }
            boolean selected = category.getCategory() == selectedCategory;
            category.render(ctx, bounds, selected, activeText, inactiveText);
        }

        return containerHeight;
    }

    public boolean handleMouseClicked(double mouseX, double mouseY) {
        for (Map.Entry<Category, Rect> entry : categoryBounds.entrySet()) {
            if (entry.getValue().contains(mouseX, mouseY)) {
                highlightFrom = highlightCurrent;
                highlightAnimation.setValue(0f);
                highlightAnimation.animateTo(1f);

                animationChange.animateTo(0);
                animationChange.setValue(0);
                onCategorySelect.accept(entry.getKey());
                return true;
            }
        }
        return false;
    }
}
