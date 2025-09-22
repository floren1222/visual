package zenith.zov.client.screens.menu;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import zenith.zov.Zenith;
import zenith.zov.base.animations.base.Animation;
import zenith.zov.base.animations.base.Easing;
import zenith.zov.base.font.Font;
import zenith.zov.base.font.Fonts;
import zenith.zov.base.theme.Theme;
import zenith.zov.client.modules.api.Category;
import zenith.zov.client.modules.impl.render.Interface;
import zenith.zov.client.screens.menu.elements.api.AbstractMenuElement;
import zenith.zov.client.screens.menu.elements.impl.MenuModuleElement;
import zenith.zov.client.screens.menu.elements.impl.MenuThemeElement;
import zenith.zov.client.screens.menu.panels.HeaderPanel;
import zenith.zov.client.screens.menu.panels.SidebarPanel;
import zenith.zov.client.screens.menu.settings.api.MenuPopupSetting;
import zenith.zov.utility.game.other.MouseButton;
import zenith.zov.utility.game.other.render.CustomScreen;
import zenith.zov.utility.math.MathUtil;
import zenith.zov.utility.render.display.ScrollHandler;
import zenith.zov.utility.render.display.base.BorderRadius;
import zenith.zov.utility.render.display.base.UIContext;
import zenith.zov.utility.render.display.base.color.ColorRGBA;
import zenith.zov.utility.render.display.shader.DrawUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MenuScreen extends CustomScreen {

    private Category selectedCategory = Category.COMBAT;

    private Category realSelectedCategory = Category.COMBAT;
    private float boxX, boxY;
    @Getter
    @Setter
    private int columns = 1;
    private float boxWidth = 522;
    private float boxHeight = 316;

    private final float containerPadding = 24f;
    private final float heroHeight = 128f;
    private final float navHeight = 34f;
    private final float navSpacing = 18f;
    private final float contentBottomPadding = 24f;

    private float contentStartX;
    private float contentY;
    private float contentWidth;
    private float visibleContentHeight;
    private float navX;
    private float navY;
    private float navWidth;
    private float scrollThumbHeight;
    private float scrollThumbY;

    private boolean dragging;
    private float dragOffsetX, dragOffsetY;

    private float boxX;
    private float boxY;
    private float boxWidth = 560f;
    private float boxHeight = 360f;

    private final float headerHeight = 74f;
    private final float navHeight = 34f;
    private final float navSpacing = 18f;
    private final float outerPadding = 28f;
    private final float innerPadding = 20f;
    private final float columnSpacing = 16f;

    private float contentStartX;
    private float contentY;
    private float contentWidth;
    private float visibleContentHeight;
    private float scrollThumbHeight;
    private float scrollThumbY;

    private boolean initialized;
    private boolean draggingWindow;
    private boolean draggingScrollbar;
    private float dragOffsetX;
    private float dragOffsetY;
    private float scrollClickOffset;

    private final Animation animationClose = new Animation(280, 0f, Easing.BAKEK_SIZE);
    private final Animation animationScrollHeight = new Animation(160, 1f, Easing.QUAD_IN_OUT);

    private final ScrollHandler scrollHandler = new ScrollHandler();

    private HeaderPanel headerPanel;
    private SidebarPanel sidebarPanel;

    private final Set<MenuPopupSetting> popupSettings = new HashSet<>();
    private final List<AbstractMenuElement> modules = new ArrayList<>();

    private int scaledScissorX;
    private int scaledScissorY;
    private int scaledScissorEndX;
    private int scaledScissorEndY;

    @Getter
    @Setter
    private boolean closing = false;

    public MenuScreen() {
        initialize();
    }

    public void initialize() {
        if (!modules.isEmpty()) {
            return;
        }

        Zenith.getInstance().getModuleManager().getModules()
                .stream()
                .map(MenuModuleElement::new)
                .forEach(modules::add);

        modules.add(new MenuThemeElement(Theme.DARK));
        modules.add(new MenuThemeElement(Theme.LIGHT));
        modules.add(new MenuThemeElement(Theme.CUSTOM_THEME));
    }

    @Override
    protected void init() {
        closing = false;

        animationColums.setValue(columns == 3 ? 1 : 0);

        boxWidth = MathHelper.lerp(animationColums.getValue(), 560, 640);

        boxHeight = MathHelper.lerp(animationColums.getValue(), 368, 420);

        boxX = (this.width - boxWidth) / 2f;
        boxY = (this.height - boxHeight) / 2f;

        animationClose.setValue(0f);
        animationClose.update(1f);

        if (!initialized) {
            sidebarPanel = new SidebarPanel(category -> {
                selectedCategory = category;
                scrollHandler.setTargetValue(0f);
            });

            this.searchField = new TextBox(new Vector2f(boxX + boxWidth - 160 - 24, boxY + 24), Fonts.MEDIUM.getFont(7), "Search", 156);
            this.sidebarPanel = new SidebarPanel(
                    this.sidebarAnimation,
                    this.isSidebarExpanded,
                    category -> {

                        this.headerPanel.resetAnim(realSelectedCategory, category);
                        realSelectedCategory = category;

                        this.scrollHandler.setTargetValue(0.0F);
                        this.searchField.setSelectAll(true);
                        this.searchField.setSelected(true);
                        this.searchField.keyPressed(GLFW.GLFW_KEY_BACKSPACE, 0, 0);
                        this.searchField.setSelected(false);
                    },
                    () -> {
                        this.isSidebarExpanded = !this.isSidebarExpanded;
                        this.sidebarAnimation.animateTo(this.isSidebarExpanded ? 1f : 0f);
                    }
            );

            this.headerPanel = new HeaderPanel(
                    this.searchField,
                    () -> this.columns = (this.columns % 3) + 1,
                    () -> Zenith.getInstance().getThemeManager().switchTheme()
            );
        }
        initialized = true;

            headerPanel = new HeaderPanel(() -> Zenith.getInstance().getThemeManager().switchTheme());
        }

        initialized = true;
    }

    @Override
    public void tick() {
        if (closing && animationClose.getValue() == 0f) {
            close();
        }

        super.tick();
    }

    @Override
    public void removed() {
        this.closing = true;
        super.removed();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // disable vanilla blur, handled manually
    }

    public boolean isFinish() {
        return animationClose.getValue() == 0.0F && closing;
    }

    public void renderTop(UIContext ctx, float mouseX, float mouseY) {
        if (!initialized) {
            return;
        }

        if (!initialized) return;

        animationColums.update(columns == 3 ? 1 : 0);

        boxWidth = MathHelper.lerp(animationColums.getValue(), 560, 640);

        boxHeight = MathHelper.lerp(animationColums.getValue(), 368, 420);
 

        float progress = MathHelper.clamp(animationClose.update(closing ? 0.0F : 1.0F), 0f, 1f);
        float scale = 0.9f + 0.1f * progress;


        progress = Math.min(Math.max(progress, 0), 1);
        float scale = 0.88f + 0.12f * progress;
        Theme theme = Zenith.getInstance().getThemeManager().getCurrentTheme();
        MatrixStack matrices = ctx.getMatrices();

        ctx.pushMatrix();

        float scaleCenterX = boxX + boxWidth / 2f;
        float scaleCenterY = boxY + boxHeight / 2f;

        matrices.translate(scaleCenterX, scaleCenterY, 0);
        matrices.scale(scale, scale, 1f);
        matrices.translate(-scaleCenterX, -scaleCenterY, 0);

        ColorRGBA surfaceColor = theme.getForegroundDark().mix(theme.getBackgroundColor(), 0.35f).mulAlpha(progress);
        ColorRGBA outlineColor = theme.getForegroundStroke().mulAlpha(progress * 0.6f);
        ColorRGBA accent = theme.getColor().mulAlpha(progress);
        ColorRGBA textPrimary = theme.getWhite().mulAlpha(progress);
        ColorRGBA textSecondary = theme.getGrayLight().mulAlpha(progress * 0.8f);

        if (Interface.INSTANCE.isBlur()) {
            DrawUtil.drawBlur(ctx.getMatrices(), boxX, boxY, boxWidth, boxHeight, 24f * progress * progress,
                    BorderRadius.all(20f), ColorRGBA.WHITE.mulAlpha(progress * 1.6f));
        }

        ctx.drawRoundedRect(boxX, boxY, boxWidth, boxHeight, BorderRadius.all(20f), surfaceColor);
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), boxX, boxY, boxWidth, boxHeight, -0.1f,
                BorderRadius.all(20f), outlineColor);

        float headerY = boxY;
        ctx.drawRoundedRect(boxX, headerY, boxWidth, headerHeight,
                BorderRadius.top(20f, 20f), theme.getForegroundColor().mulAlpha(progress * 0.8f));

        headerPanel.render(ctx, boxX, headerY, boxWidth, headerHeight, progress, theme, selectedCategory, textPrimary, textSecondary);

        float navX = boxX + outerPadding;
        float navWidth = boxWidth - outerPadding * 2f;
        float navY = headerY + headerHeight - 16f;

        float navAreaHeight = sidebarPanel.render(ctx, navX, navY, navWidth, navHeight, progress, theme, selectedCategory, accent,
                textPrimary, textSecondary.mulAlpha(0.7f));

        float containerX = navX;
        float containerY = navY + navAreaHeight + navSpacing;
        float containerWidth = navWidth;
        float containerHeight = boxHeight - (containerY - boxY) - outerPadding;

        ColorRGBA containerColor = theme.getForegroundColor().mulAlpha(progress * 0.45f);
        ctx.drawRoundedRect(containerX, containerY, containerWidth, containerHeight, BorderRadius.all(18f), containerColor);
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), containerX, containerY, containerWidth, containerHeight, -0.1f,
                BorderRadius.all(18f), outlineColor.mulAlpha(0.75f));

        contentStartX = containerX + innerPadding;
        contentWidth = containerWidth - innerPadding * 2f;
        contentY = containerY + innerPadding;
        visibleContentHeight = containerHeight - innerPadding * 2f;
        visibleContentHeight = Math.max(120f, visibleContentHeight);

        ColorRGBA primary = theme.getColor().mulAlpha(progress);
        ColorRGBA baseBg = theme.getBackgroundColor().mix(theme.getForegroundDark(), 0.35f).mulAlpha(progress);
        ColorRGBA selectedColor = theme.getWhite().mulAlpha(progress);
        ColorRGBA textColor = theme.getWhite().mulAlpha(progress);
        ColorRGBA outlineColor = theme.getForegroundStroke().mulAlpha(progress);

        if (Interface.INSTANCE.isBlur()) {
            DrawUtil.drawBlur(ctx.getMatrices(), boxX, boxY, boxWidth, boxHeight, 28 * progress * progress, BorderRadius.all(18f), ColorRGBA.WHITE.mulAlpha(progress * 1.8f));
        }

        ctx.drawRoundedRect(boxX, boxY, boxWidth, boxHeight, BorderRadius.all(18f), baseBg);
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), boxX, boxY, boxWidth, boxHeight, -0.1f, BorderRadius.all(18f), outlineColor);

        ColorRGBA heroColor = theme.getForegroundDark().mix(primary, 0.25f).mulAlpha(progress);
        ctx.drawRoundedRect(boxX, boxY, boxWidth, heroHeight, BorderRadius.top(18f, 18f), heroColor);
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), boxX, boxY, boxWidth, heroHeight, -0.1f, BorderRadius.top(18f, 18f), outlineColor.mulAlpha(0.35f));

        ctx.drawRoundedRect(boxX + containerPadding, boxY + 24f, 160f, 46f, BorderRadius.all(14f), primary.mulAlpha(0.18f));
        ctx.drawRoundedRect(boxX + boxWidth - containerPadding - 112f, boxY + heroHeight - 52f, 112f, 40f, BorderRadius.all(14f), theme.getForegroundColor().mulAlpha(progress * 0.35f));

        navX = boxX + containerPadding;
        navWidth = boxWidth - containerPadding * 2f;
        navY = boxY + heroHeight + navSpacing;

        float navBackgroundHeight = navHeight + 24f;
        ColorRGBA navBackground = theme.getForegroundColor().mulAlpha(progress * 0.32f);
        ctx.drawRoundedRect(navX, navY - 12f, navWidth, navBackgroundHeight, BorderRadius.all(12f), navBackground);
        DrawUtil.drawRoundedBorder(ctx.getMatrices(), navX, navY - 12f, navWidth, navBackgroundHeight, -0.1f, BorderRadius.all(12f), outlineColor.mulAlpha(0.4f));

        headerPanel.render(ctx, boxX, boxY, boxWidth, heroHeight, progress, theme, realSelectedCategory, columns);

        sidebarPanel.render(ctx, navX, navY, navWidth, navHeight, progress, theme, realSelectedCategory, primary, textColor, selectedColor);

        contentStartX = navX;
        contentWidth = navWidth;
        contentY = navY + navHeight + navSpacing;
        visibleContentHeight = boxHeight - (contentY - boxY) - contentBottomPadding;
        visibleContentHeight = Math.max(96f, visibleContentHeight);

        ctx.drawRoundedRect(contentStartX, contentY - 16f, contentWidth, visibleContentHeight + 32f, BorderRadius.all(16f), theme.getForegroundColor().mulAlpha(progress * 0.18f));

        this.scaledScissorX = (int) contentStartX;
        this.scaledScissorY = (int) contentY;
        this.scaledScissorEndX = (int) (contentStartX + contentWidth);
        this.scaledScissorEndY = (int) (contentY + visibleContentHeight);

        scaledScissorX = (int) contentStartX;
        scaledScissorY = (int) contentY;
        scaledScissorEndX = (int) (contentStartX + contentWidth);
        scaledScissorEndY = (int) (contentY + visibleContentHeight);

        ctx.enableScissor(scaledScissorX, scaledScissorY, scaledScissorEndX, scaledScissorEndY);
        renderModules(ctx, mouseX, mouseY, progress);
        ctx.disableScissor();

        float scrollbarWidth = 4f;
        float trackX = contentStartX + contentWidth - scrollbarWidth;
        float trackY = contentY;

        float visibleHeight = visibleContentHeight;
        float maxScroll = scrollHandler.getMax();
        float scrollProgress = maxScroll == 0 ? 0f : (float) (scrollHandler.getValue() / maxScroll);
        float scrollHeight = Math.max(visibleHeight * (visibleHeight / (float) (visibleHeight + maxScroll)), 28f);
        scrollHeight = Math.min(visibleHeight, animationScrollHeight.update(scrollHeight));

        float denom = Math.max(1f, visibleHeight - scrollHeight);
        float scrollY = trackY + denom * scrollProgress;
        scrollY = MathHelper.clamp(scrollY, trackY, trackY + visibleHeight - scrollHeight);

        ctx.drawRoundedRect(trackX, trackY, scrollbarWidth, visibleHeight,
                BorderRadius.all(scrollbarWidth / 2f), theme.getForegroundDark().mulAlpha(progress * 0.5f));
        ctx.drawRoundedRect(trackX, scrollY, scrollbarWidth, scrollHeight,
                BorderRadius.all(scrollbarWidth / 2f), accent);

        float scrollTrackWidth = 6f;
        float visibleHeight = visibleContentHeight;
        float scrollProgress = scrollHandler.getMax() == 0 ? 0f : (float) (scrollHandler.getValue() / scrollHandler.getMax());
        float scrollHeight = Math.max(visibleHeight * (visibleHeight / (float) (visibleHeight + scrollHandler.getMax())), 28);
        scrollHeight = Math.min(visibleHeight, animationScrollHeight.update(scrollHeight));
        float denom = Math.max(1f, (visibleHeight - scrollHeight));
        float scrollY = contentY + denom * scrollProgress;
        scrollY = MathHelper.clamp(scrollY, contentY, contentY + visibleHeight - scrollHeight);

        ctx.drawRoundedRect(contentStartX + contentWidth - scrollTrackWidth, contentY, scrollTrackWidth, visibleHeight, BorderRadius.all(2f), theme.getForegroundColor().mulAlpha(progress * 0.35f));
        ctx.drawRoundedRect(contentStartX + contentWidth - scrollTrackWidth, scrollY, scrollTrackWidth, scrollHeight, BorderRadius.all(2f), primary.mulAlpha(progress));

        scrollThumbHeight = scrollHeight;
        scrollThumbY = scrollY;

        List<MenuPopupSetting> remove = new ArrayList<>();

        List<MenuPopupSetting> removes = new ArrayList<>();
        for (MenuPopupSetting setting : popupSettings) {
            setting.render(ctx, mouseX, mouseY, progress, theme);
            if (setting.getAnimationScale().getValue() == 0f) {
                remove.add(setting);
            }
        }

        popupSettings.removeAll(remove);

        if (draggingScrollbar) {
            float newY = (float) mouseY - trackY - scrollClickOffset;
            float ratio = MathHelper.clamp(newY / denom, 0f, 1f);
            scrollHandler.setTargetValue(-(ratio * scrollHandler.getMax()));
        }

        ctx.popMatrix();
    }

    private void renderModules(UIContext ctx, float mouseX, float mouseY, float alpha) {
        List<AbstractMenuElement> visibleModules = modules.stream()
                .filter(module -> module.getCategory() == selectedCategory)
                .sorted(Comparator.comparing(AbstractMenuElement::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        int columns = 2;
        float scrollbarWidth = 6f;
        float padding = columnSpacing;
        float availableWidth = contentWidth - scrollbarWidth;
        float moduleWidth = (availableWidth - padding * (columns - 1)) / columns;

        double[] columnHeights = new double[columns];
        Font titleFont = Fonts.MEDIUM.getFont(7);

        for (AbstractMenuElement element : visibleModules) {
            int columnIndex = 0;
            for (int i = 1; i < columns; i++) {
                if (columnHeights[i] < columnHeights[columnIndex]) {
                    columnIndex = i;
                }
            }

            float x = contentStartX + columnIndex * (moduleWidth + padding);
            float y = (float) (contentY + columnHeights[columnIndex] - scrollHandler.getValue());

            element.render(ctx, mouseX, mouseY, titleFont, x, y, moduleWidth, alpha, columnIndex);
            columnHeights[columnIndex] += element.getHeight() + padding;

        if (animationChangeCategory.getValue() == 0) {
            selectedCategory = realSelectedCategory;
        }

        if (draggingScrollbar) {
            float newY = (float) mouseY - contentY - scrollClickOffset;
            float scrollRatio = MathHelper.clamp(newY / denom, 0f, 1f);
            scrollHandler.setTargetValue(-(scrollRatio * scrollHandler.getMax()));
        }

        scrollHandler.update();

        double maxHeight = 0;
        for (double columnHeight : columnHeights) {
            if (columnHeight > maxHeight) {
                maxHeight = columnHeight;
            }
        }

        float visibleHeight = visibleContentHeight;
        float overflow = (float) Math.max(0, maxHeight - visibleHeight);
        scrollHandler.setMax(overflow);
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (!popupSettings.isEmpty()) {
            for (MenuPopupSetting setting : popupSettings) {
                if (setting.getBounds().contains(mouseX, mouseY)) {
                    setting.onMouseClicked(mouseX, mouseY, button);
                    return;
                } else {
                    setting.getAnimationScale().update(0);
                }
            }
        }

        if (isClosing()) {
            return;
        }

        if (headerPanel.handleMouseClicked(mouseX, mouseY)) {
            return;
        }

        if (sidebarPanel.handleMouseClicked(mouseX, mouseY)) {
            return;
        }

        if (button.getButtonIndex() == GLFW.GLFW_MOUSE_BUTTON_LEFT &&
                MathUtil.isHovered(mouseX, mouseY, boxX, boxY, boxWidth, headerHeight)) {
            draggingWindow = true;
            dragOffsetX = (float) mouseX - boxX;
            dragOffsetY = (float) mouseY - boxY;
            return;
        }

        if (searchField.isSelected()) {
            searchField.setSelected(false);
        }
        if (button.getButtonIndex() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (MathUtil.isHovered(mouseX, mouseY, boxX, boxY, boxWidth, heroHeight * 0.65f)) {
                dragging = true;
                dragOffsetX = (float) mouseX - boxX;
                dragOffsetY = (float) mouseY - boxY;
                return;
            }
        }
        //чтобы все что обрезанно не нажималось
        if (!animationClose.isDone()) return;
        float scrollbarX = contentStartX + contentWidth - 6f;
        float scrollbarY = contentY;
        float visibleHeight = visibleContentHeight;
        if (button.getButtonIndex() == GLFW.GLFW_MOUSE_BUTTON_LEFT && visibleHeight > 0) {
            if (MathUtil.isHovered(mouseX, mouseY, scrollbarX, scrollbarY, 6, visibleHeight)) {
                draggingScrollbar = true;
                scrollClickOffset = MathHelper.clamp((float) mouseY - scrollThumbY, 0f, scrollThumbHeight);

        float scrollbarX = contentStartX + contentWidth - 4f;
        float scrollbarY = contentY;
        float visibleHeight = visibleContentHeight;

        if (button.getButtonIndex() == GLFW.GLFW_MOUSE_BUTTON_LEFT && visibleHeight > 0) {
            if (MathUtil.isHovered(mouseX, mouseY, scrollbarX, scrollbarY, 4f, visibleHeight)) {
                draggingScrollbar = true;
                scrollClickOffset = MathHelper.clamp((float) mouseY - scrollThumbY, 0f, scrollThumbHeight);
                return;
            }
        }

        if (!MathUtil.isHoveredByCords(mouseX, mouseY, scaledScissorX, scaledScissorY, scaledScissorEndX, scaledScissorEndY)) {
            return;
        }

        modules.stream()
                .filter(module -> module.getCategory() == selectedCategory)
                .forEach(module -> module.onMouseClicked(mouseX, mouseY, button));

        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        for (MenuPopupSetting setting : popupSettings) {
            setting.onMouseReleased(mouseX, mouseY, button);
        }

        if (button.getButtonIndex() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            draggingWindow = false;
            draggingScrollbar = false;
        }

        modules.forEach(module -> module.onMouseReleased(mouseX, mouseY, button));
        super.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onMouseDragged(double mouseX, double mouseY, MouseButton button, double deltaX, double deltaY) {
        if (button.getButtonIndex() == GLFW.GLFW_MOUSE_BUTTON_LEFT && draggingWindow) {
            boxX = (float) mouseX - dragOffsetX;
            boxY = (float) mouseY - dragOffsetY;
            return;
        }

        modules.forEach(module -> module.onMouseDragged(mouseX, mouseY, button, deltaX, deltaY));
        super.onMouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!popupSettings.isEmpty()) {
            for (MenuPopupSetting setting : popupSettings) {
                setting.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            }
            return true;
        }

        float visibleHeight = Math.max(visibleContentHeight, 1f);
        float step = Math.max(24f, Math.min(72f, (scrollHandler.getMax() / visibleHeight) * 12f));
        scrollHandler.scroll(verticalAmount * step / 8f);

        float visibleHeight = this.visibleContentHeight > 0 ? this.visibleContentHeight : boxHeight - heroHeight;
        float baseStep = (float) Math.max(24f, Math.min(72f, (scrollHandler.getMax() / Math.max(1f, visibleHeight)) * 12));

        scrollHandler.scroll(verticalAmount * baseStep / 8);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean handled = false;

        for (MenuPopupSetting setting : popupSettings) {
            if (setting.keyPressed(keyCode, scanCode, modifiers)) {
                handled = true;
            }
        }

        if (handled) {
            return true;
        }

        for (AbstractMenuElement module : modules) {
            if (module.keyPressed(keyCode, scanCode, modifiers)) {
                handled = true;
            }
        }

        if (handled) {
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !closing) {
            onMouseReleased(0, 0, MouseButton.LEFT);
            onMouseReleased(0, 0, MouseButton.RIGHT);
            onMouseReleased(0, 0, MouseButton.MIDDLE);
            for (MenuPopupSetting setting : popupSettings) {
                setting.getAnimationScale().setTargetValue(0);
            }
            closing = true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        super.close();
    }

    public void addPopupMenuSetting(MenuPopupSetting setting) {
        popupSettings.add(setting);
    }

    public void removePopupMenuSetting(MenuPopupSetting setting) {
        popupSettings.remove(setting);
    }

    @Override
    public void render(UIContext context, float mouseX, float mouseY) {
        // handled by parent layout
    }
}
