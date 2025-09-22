package zenith.zov.client.screens.menu.elements.impl;

import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import zenith.zov.Zenith;
import zenith.zov.base.animations.base.Animation;
import zenith.zov.base.animations.base.Easing;
import zenith.zov.base.font.Font;
import zenith.zov.base.font.Fonts;
import zenith.zov.base.theme.Theme;
import zenith.zov.client.modules.api.Category;
import zenith.zov.client.modules.api.Module;
import zenith.zov.client.modules.api.setting.Setting;
import zenith.zov.client.modules.api.setting.impl.BooleanSetting;
import zenith.zov.client.modules.api.setting.impl.ButtonSetting;
import zenith.zov.client.modules.api.setting.impl.ColorSetting;
import zenith.zov.client.modules.api.setting.impl.ItemSelectSetting;
import zenith.zov.client.modules.api.setting.impl.KeySetting;
import zenith.zov.client.modules.api.setting.impl.ModeSetting;
import zenith.zov.client.modules.api.setting.impl.MultiBooleanSetting;
import zenith.zov.client.modules.api.setting.impl.NumberSetting;
import zenith.zov.client.screens.menu.elements.api.AbstractMenuElement;
import zenith.zov.client.screens.menu.settings.api.MenuSetting;
import zenith.zov.client.screens.menu.settings.impl.MenuBooleanSetting;
import zenith.zov.client.screens.menu.settings.impl.MenuButtonSetting;
import zenith.zov.client.screens.menu.settings.impl.MenuColorSetting;
import zenith.zov.client.screens.menu.settings.impl.MenuItemSetting;
import zenith.zov.client.screens.menu.settings.impl.MenuKeySetting;
import zenith.zov.client.screens.menu.settings.impl.MenuModeSetting;
import zenith.zov.client.screens.menu.settings.impl.MenuSelectSetting;
import zenith.zov.client.screens.menu.settings.impl.MenuSliderSetting;
import zenith.zov.utility.game.other.MouseButton;
import zenith.zov.utility.render.display.Keyboard;
import zenith.zov.utility.render.display.base.BorderRadius;
import zenith.zov.utility.render.display.base.Rect;
import zenith.zov.utility.render.display.base.UIContext;
import zenith.zov.utility.render.display.base.color.ColorRGBA;
import zenith.zov.utility.render.display.shader.DrawUtil;

import java.util.ArrayList;
import java.util.List;

public class MenuModuleElement extends AbstractMenuElement {

    private static final float HEADER_HEIGHT = 58f;
    private static final float CORNER_RADIUS = 16f;
    private static final float SETTINGS_PADDING = 18f;
    private static final float SETTINGS_SPACING = 12f;

    @Getter
    private final Module module;
    private final List<MenuSetting> settings = new ArrayList<>();
    private final Animation toggleAnimation;
    private final Animation animationPosition;
    private final Animation animationY;
    private Rect headerBounds;
    private Rect bindBounds;
    @Getter
    private boolean binding = false;
    private int lastColumn = -1;
    private boolean animated;

    public MenuModuleElement(Module module) {
        this.module = module;
        this.toggleAnimation = new Animation(200, module.isEnabled() ? 1f : 0f, Easing.QUAD_IN_OUT);
        this.animationPosition = new Animation(180, 1f, Easing.QUAD_IN_OUT);
        this.animationY = new Animation(180, 1f, Easing.QUAD_IN_OUT);

        for (Setting setting : module.getSettings()) {
            if (setting instanceof NumberSetting sliderSetting) {
                settings.add(new MenuSliderSetting(sliderSetting));
            } else if (setting instanceof ModeSetting modeSetting) {
                settings.add(new MenuModeSetting(modeSetting));
            } else if (setting instanceof MultiBooleanSetting selectSetting) {
                settings.add(new MenuSelectSetting(selectSetting));
            } else if (setting instanceof BooleanSetting booleanSetting) {
                settings.add(new MenuBooleanSetting(booleanSetting));
            } else if (setting instanceof ColorSetting colorSetting) {
                settings.add(new MenuColorSetting(colorSetting));
            } else if (setting instanceof ButtonSetting buttonSetting) {
                settings.add(new MenuButtonSetting(buttonSetting));
            } else if (setting instanceof ItemSelectSetting itemSelectSetting) {
                settings.add(new MenuItemSetting(itemSelectSetting));
            } else if (setting instanceof KeySetting keySetting) {
                settings.add(new MenuKeySetting(keySetting));
            }
        }
    }

    @Override
    public void render(UIContext ctx, float mouseX, float mouseY, Font font, float x, float y, float moduleWidth, float alpha, int column) {
        if (lastColumn == -1) {
            lastColumn = column;
        }

        if (lastColumn != column) {
            animated = true;
            animationPosition.animateTo(x);
            animationY.animateTo(y);
            lastColumn = column;
        }

        if (animated) {
            x = animationPosition.update(x);
            y = animationY.update(y);
            if (animationPosition.isDone() && animationY.isDone()) {
                animated = false;
            }
        } else {
            animationPosition.reset(x);
            animationY.reset(y);
        }

        toggleAnimation.animateTo(module.isEnabled() ? 1f : 0f);
        float toggleProgress = toggleAnimation.update();

        Theme theme = Zenith.getInstance().getThemeManager().getCurrentTheme();
        boolean hasSettings = hasSettings();

        float totalHeight = getHeight();
        float headerHeight = HEADER_HEIGHT;
        float bodyStart = y + headerHeight;

        ColorRGBA headerColor = theme.getForegroundColor().mulAlpha(alpha);
        ColorRGBA bodyColor = theme.getForegroundDark().mulAlpha(alpha * 0.85f);
        ColorRGBA borderColor = theme.getForegroundStroke().mulAlpha(alpha * 0.75f);
        ColorRGBA accentColor = theme.getColor().mulAlpha(alpha);
        ColorRGBA titleColor = theme.getWhite().mix(theme.getColor(), toggleProgress * 0.35f).mulAlpha(alpha);
        ColorRGBA mutedColor = theme.getGrayLight().mulAlpha(alpha * 0.85f);

        if (hasSettings) {
            ctx.drawRoundedRect(x, y, moduleWidth, totalHeight, BorderRadius.all(CORNER_RADIUS), bodyColor);
            ctx.drawRoundedRect(x, y, moduleWidth, headerHeight, BorderRadius.top(CORNER_RADIUS, CORNER_RADIUS), headerColor);
        } else {
            ctx.drawRoundedRect(x, y, moduleWidth, headerHeight, BorderRadius.all(CORNER_RADIUS), headerColor);
        }

        headerBounds = new Rect(x, y, moduleWidth, headerHeight);

        Font titleFont = Fonts.MEDIUM.getFont(7.5f);
        Font stateFont = Fonts.MEDIUM.getFont(6f);
        float textX = x + 20f;
        float textY = y + 16f;

        ctx.drawText(titleFont, module.getName(), textX, textY, titleColor);
        String state = module.isEnabled() ? "Enabled" : "Disabled";
        ctx.drawText(stateFont, state, textX, textY + titleFont.height() + 4f, mutedColor);

        Font keyFont = Fonts.MEDIUM.getFont(6.5f);
        String keyLabel = resolveKeyLabel();
        float keyWidth = Math.max(42f, keyFont.width(keyLabel) + 14f);
        float keyHeight = 18f;
        float keyX = textX;
        float keyY = y + headerHeight - keyHeight - 12f;

        ColorRGBA keyBg;
        if (binding) {
            keyBg = theme.getSecondColor().mulAlpha(alpha);
        } else if (module.getKeyCode() != -1) {
            keyBg = theme.getColor().mulAlpha(alpha * 0.6f);
        } else {
            keyBg = theme.getForegroundLight().mulAlpha(alpha * 0.8f);
        }

        bindBounds = new Rect(keyX, keyY, keyWidth, keyHeight);
        ctx.drawRoundedRect(keyX, keyY, keyWidth, keyHeight, BorderRadius.all(keyHeight / 2f), keyBg);
        ctx.drawText(keyFont, keyLabel,
                keyX + (keyWidth - keyFont.width(keyLabel)) / 2f,
                keyY + (keyHeight - keyFont.height()) / 2f,
                theme.getWhite().mulAlpha(alpha));

        float toggleWidth = 46f;
        float toggleHeight = 22f;
        float toggleX = x + moduleWidth - toggleWidth - 20f;
        float toggleY = y + (headerHeight - toggleHeight) / 2f;

        ColorRGBA toggleTrackOff = theme.getForegroundLight().mulAlpha(alpha * 0.7f);
        ColorRGBA toggleTrack = toggleTrackOff.mix(accentColor, toggleProgress);
        ColorRGBA knobColor = theme.getWhite().mulAlpha(alpha);

        ctx.drawRoundedRect(toggleX, toggleY, toggleWidth, toggleHeight, BorderRadius.all(toggleHeight / 2f), toggleTrack);

        float knobSize = toggleHeight - 6f;
        float knobX = toggleX + 3f + (toggleWidth - toggleHeight) * toggleProgress;
        float knobY = toggleY + 3f;
        ctx.drawRoundedRect(knobX, knobY, knobSize, knobSize, BorderRadius.all(knobSize / 2f), knobColor);

        float startY = bodyStart;
        if (hasSettings) {
            startY += SETTINGS_PADDING;
        }

        ColorRGBA enabledColor = theme.getGray().mix(theme.getColor(), toggleProgress).mulAlpha(alpha);
        ColorRGBA textColor = theme.getWhiteGray().mix(theme.getWhite(), toggleProgress).mulAlpha(alpha);
        ColorRGBA descriptionColor = theme.getGrayLight().mulAlpha(alpha * 0.9f);

        for (MenuSetting setting : settings) {
            if (!setting.isVisible()) {
                continue;
            }
            setting.render(ctx, mouseX, mouseY, x + 16f, startY, moduleWidth - 32f, alpha, toggleProgress,
                    enabledColor, textColor, descriptionColor, theme);
            startY += setting.getHeight() + SETTINGS_SPACING;
        }

        DrawUtil.drawRoundedBorder(ctx.getMatrices(), x, y, moduleWidth, totalHeight, -0.1f,
                BorderRadius.all(CORNER_RADIUS), borderColor);
    }

    private String resolveKeyLabel() {
        int keyCode = module.getKeyCode();
        if (binding) {
            return "...";
        }
        if (keyCode == -1 || keyCode == 0) {
            return "Unbound";
        }
        try {
            String name = Keyboard.getKeyName(keyCode);
            if (name != null && !name.isBlank()) {
                return name.toUpperCase();
            }
        } catch (Exception ignored) {
        }
        return "Unknown";
    }

    @Override
    public float getHeight() {
        float total = HEADER_HEIGHT;
        float settingsHeight = 0f;
        for (MenuSetting setting : settings) {
            if (!setting.isVisible()) {
                continue;
            }
            settingsHeight += setting.getHeight();
            settingsHeight += SETTINGS_SPACING;
        }
        if (settingsHeight > 0f) {
            settingsHeight -= SETTINGS_SPACING;
            total += SETTINGS_PADDING + settingsHeight;
        }
        return total;
    }

    public boolean hasSettings() {
        return settings.stream().anyMatch(MenuSetting::isVisible);
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (headerBounds != null && headerBounds.contains(mouseX, mouseY)) {
            if (button.getButtonIndex() > 2 && binding) {
                binding = false;
                this.module.setKeyCode(button.getButtonIndex());
            }

            if (button == MouseButton.LEFT) {
                if (bindBounds != null && bindBounds.contains(mouseX, mouseY)) {
                    binding = !binding;
                } else {
                    module.toggle();
                }
            } else if (button == MouseButton.MIDDLE) {
                binding = !binding;
            }
        }

        for (MenuSetting setting : settings) {
            setting.onMouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                module.setKeyCode(-1);
            } else {
                module.setKeyCode(keyCode);
            }
            binding = false;
            return true;
        }

        boolean handled = false;
        for (MenuSetting setting : settings) {
            if (setting.keyPressed(keyCode, scanCode, modifiers)) {
                handled = true;
            }
        }
        return handled;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return true;
    }

    @Override
    public Category getCategory() {
        return module.getCategory();
    }

    @Override
    public String getName() {
        return module.getName();
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        for (MenuSetting setting : settings) {
            setting.onMouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public void onMouseDragged(double mouseX, double mouseY, MouseButton button, double deltaX, double deltaY) {
    }
}
