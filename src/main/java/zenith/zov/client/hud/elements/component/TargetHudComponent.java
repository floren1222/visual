package zenith.zov.client.hud.elements.component;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import zenith.zov.Zenith;
import zenith.zov.base.animations.base.Animation;
import zenith.zov.base.animations.base.Easing;
import zenith.zov.base.font.Font;
import zenith.zov.base.font.Fonts;
import zenith.zov.base.theme.Theme;
import zenith.zov.client.hud.elements.draggable.DraggableHudElement;
import zenith.zov.utility.game.player.PlayerIntersectionUtil;
import zenith.zov.client.modules.impl.render.TargetHUD;
import zenith.zov.utility.render.display.base.BorderRadius;
import zenith.zov.utility.render.display.base.CustomDrawContext;
import zenith.zov.utility.render.display.base.color.ColorRGBA;
import zenith.zov.utility.render.display.shader.DrawUtil;
import zenith.zov.client.hud.style.HudStyle;

import static java.lang.Math.round;

public class TargetHudComponent extends DraggableHudElement {

    private final Animation healthAnimation = new Animation(200, Easing.QUAD_OUT);
    private final Animation toggleAnimation = new Animation(200, Easing.QUAD_IN_OUT);
    private final TargetHUD hud = TargetHUD.INSTANCE;

    private LivingEntity target;

    public TargetHudComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, Align align) {
        super(name,initialX, initialY,windowWidth,windowHeight,offsetX,offsetY,align);

    }

    @Override
    public void tick() {
        // Настройки анимации из модуля
        long speed = (long) hud.getAnimationSpeed();
        healthAnimation.setDuration(speed);
        toggleAnimation.setDuration(speed);

        // Обновляем анимации
        healthAnimation.update();
        toggleAnimation.update();
    }

    @Override
    public void render(CustomDrawContext ctx) {
        if (this.target == null) return;
        
        renderTargetHud(ctx, this.target, toggleAnimation.getValue());
    }

    private void renderTargetHud(CustomDrawContext ctx, LivingEntity target, float animation) {
        float posX = x, posY = y;
        float width = 120f, height = 32f;
        float headSize = 20f, padding = 6f;
        float fontSize = 7f;

        Theme theme = Zenith.getInstance().getThemeManager().getCurrentTheme();
        float fade = animation;
        float baseOpacity = hud.getOpacity();
        ColorRGBA bgColor = theme.getBackgroundColor().mulAlpha(baseOpacity * fade);
        ColorRGBA accentColor = theme.getColor().mulAlpha(baseOpacity * fade);
        ColorRGBA textColor = theme.getWhite().mulAlpha(fade);

        // Параметры здоровья
        float hp = round(PlayerIntersectionUtil.getHealth(target));
        float maxHp = target.getMaxHealth();
        float healthPercent = hp / maxHp;
        boolean showHealthText = hud.isShowHealthText();
        Font hpFont = Fonts.MEDIUM.getFont(fontSize);
        String hpText = (int) hp + "";
        float hpTextWidth = showHealthText ? hpFont.width(hpText) : 0f;

        this.width = width;
        this.height = height;

        ctx.pushMatrix();
        {
            // Анимация появления
            ctx.getMatrices().translate(posX + width / 2f, posY + height / 2f, 0f);
            ctx.getMatrices().scale(animation, animation, 1f);
            ctx.getMatrices().translate(-(posX + width / 2f), -(posY + height / 2f), 0f);

            // Минималистичный фон с лёгкой прозрачностью
            HudStyle.drawPanel(ctx, theme, posX, posY, width, height, 6f, baseOpacity * fade);

            float headX = posX + padding;
            float headY = posY + (height - headSize) / 2f;

            float contentX = headX + headSize + padding;
            float contentRight = posX + width - padding - (showHealthText ? hpTextWidth + padding : 0f);
            float barFullWidth = contentRight - contentX;

            float animatedHealth = healthAnimation.update(barFullWidth * healthPercent);

            // Аватар игрока (компактный)
            if (target instanceof PlayerEntity player) {
                DrawUtil.drawPlayerHeadWithRoundedShader(
                        ctx.getMatrices(),
                        ((AbstractClientPlayerEntity) player).getSkinTextures().texture(),
                        headX, headY, headSize,
                        BorderRadius.all(4f), ColorRGBA.WHITE
                );
            } else {
                Font qFont = Fonts.MEDIUM.getFont(10);
                ctx.drawText(qFont, "?", headX + (headSize - qFont.width("?")) / 2f,
                        headY + headSize / 2f - qFont.height() / 2f, textColor);
            }

            // Имя игрока (одна строка)
            Font nameFont = Fonts.MEDIUM.getFont(fontSize);
            String name = target.getName().getString();

            float maxNameWidth = barFullWidth - 10f;
            String displayName = name;
            if (nameFont.width(name) > maxNameWidth) {
                while (nameFont.width(displayName + "...") > maxNameWidth && displayName.length() > 0) {
                    displayName = displayName.substring(0, displayName.length() - 1);
                }
                displayName += "...";
            }

            ctx.drawText(nameFont, displayName, contentX, posY + padding, textColor);

            // HP бар под именем
            if (hud.isShowHealthBar()) {
                float barX = contentX;
                float barHeight = 3f;
                float barY = posY + height - padding - barHeight;
                ColorRGBA barBg = theme.getForegroundLight().mulAlpha(0.25f * fade);

                ColorRGBA barColor;
                if (healthPercent > 0.5f) {
                    barColor = ColorRGBA.lerp(ColorRGBA.YELLOW, ColorRGBA.GREEN, (healthPercent - 0.5f) / 0.5f);
                } else {
                    barColor = ColorRGBA.lerp(ColorRGBA.RED, ColorRGBA.YELLOW, healthPercent / 0.5f);
                }
                barColor = barColor.mulAlpha(baseOpacity * fade);

                ctx.drawRoundedRect(barX, barY, barFullWidth, barHeight, BorderRadius.all(1.5f), barBg);

                if (animatedHealth > 0) {
                    ctx.drawRoundedRect(barX, barY, animatedHealth, barHeight, BorderRadius.all(1.5f), barColor);
                }

                if (showHealthText) {
                    float textX = barX + barFullWidth + padding / 2f;
                    float textY = barY + barHeight / 2f - hpFont.height() / 2f;
                    ctx.drawText(hpFont, hpText, textX, textY, accentColor);
                }
            }
        }
        ctx.popMatrix();
    }

    public void setTarget(LivingEntity target) {
        if (target == null) {
            // Начинаем анимацию исчезновения
            if (this.target != null) {
                toggleAnimation.update(0);

                // Устанавливаем target в null только когда анимация завершится
                if (toggleAnimation.getValue() == 0) {
                    this.target = null;
                }
            }
        } else {
            if (target != this.target) {
                // Новая цель - сбрасываем анимации и начинаем новые
                this.target = target;

                toggleAnimation.reset();

                // Начинаем анимацию появления
                toggleAnimation.update(1);
            } else {
                // Та же цель - продолжаем анимацию появления
                toggleAnimation.update(1);
            }
        }
    }
    
    public LivingEntity getTarget() {
        return this.target;
    }
}
