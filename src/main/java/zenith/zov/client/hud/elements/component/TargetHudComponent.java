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

import static java.lang.Math.round;

public class TargetHudComponent extends DraggableHudElement {

    private final Animation healthAnimation = new Animation(200, Easing.LINEAR);
    private final Animation toggleAnimation = new Animation(200, Easing.QUAD_IN_OUT);

    private LivingEntity target;

    public TargetHudComponent(String name, float initialX, float initialY, float windowWidth, float windowHeight, float offsetX, float offsetY, Align align) {
        super(name,initialX, initialY,windowWidth,windowHeight,offsetX,offsetY,align);

    }

    @Override
    public void tick() {
        // Настройки анимации из модуля
        TargetHUD hud = TargetHUD.INSTANCE;
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
        TargetHUD hud = TargetHUD.INSTANCE;
        float opacity = hud.getOpacity();
        ColorRGBA bgColor = theme.getBackgroundColor().mulAlpha(opacity);
        ColorRGBA accentColor = theme.getColor().mulAlpha(opacity);
        ColorRGBA textColor = theme.getWhite();

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

            // Блюр и фон с тонкой рамкой
            DrawUtil.drawBlurHud(ctx.getMatrices(), posX, posY, width, height, 22, BorderRadius.all(6), ColorRGBA.WHITE);
            ctx.drawRoundedRect(posX, posY, width, height, BorderRadius.all(6), bgColor);
            ctx.drawRoundedBorder(posX, posY, width, height, 0.5f, BorderRadius.all(6), accentColor.mulAlpha(0.5f));

            float barFullWidth = width - padding * 2 - headSize - padding - (showHealthText ? hpTextWidth + padding : 0f);

            float animatedHealth = healthAnimation.update(barFullWidth * healthPercent);

            float headX = posX + padding;
            float headY = posY + (height - headSize) / 2f;

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

            ctx.drawText(nameFont, displayName, headX + headSize + padding, headY - 2f, textColor);

            // HP бар (под именем) с красивым дизайном - только если включен
            boolean showHealthBar = hud.isShowHealthBar();
            if (showHealthBar) {
                float barX = headX + headSize + padding;
                float barY = headY + 7f;
                float barHeight = 3f;
                ColorRGBA barBg = theme.getForegroundLight().mulAlpha(0.3f);

                ColorRGBA barColor;
                if (healthPercent > 0.6f) {
                    barColor = ColorRGBA.GREEN.mulAlpha(opacity);
                } else if (healthPercent > 0.2f) {
                    barColor = ColorRGBA.YELLOW.mulAlpha(opacity);
                } else {
                    barColor = ColorRGBA.RED.mulAlpha(opacity);
                }

                ctx.drawRoundedRect(barX, barY, barFullWidth, barHeight, BorderRadius.all(1f), barBg);

                if (animatedHealth > 0) {
                    ctx.drawRoundedRect(barX, barY, animatedHealth, barHeight, BorderRadius.all(1f), barColor);
                }

                // Текст здоровья справа от полосы
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
