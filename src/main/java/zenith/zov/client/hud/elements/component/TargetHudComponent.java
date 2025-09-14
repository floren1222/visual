package zenith.zov.client.hud.elements.component;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import zenith.zov.Zenith;
import zenith.zov.base.animations.base.Animation;
import zenith.zov.base.animations.base.Easing;
import zenith.zov.base.font.Font;
import zenith.zov.base.font.Fonts;
import zenith.zov.base.theme.Theme;
import zenith.zov.client.hud.elements.draggable.DraggableHudElement;
import zenith.zov.utility.game.player.PlayerIntersectionUtil;
import zenith.zov.utility.mixin.accessors.DrawContextAccessor;
import zenith.zov.utility.render.display.base.BorderRadius;
import zenith.zov.utility.render.display.base.CustomDrawContext;
import zenith.zov.utility.render.display.base.color.ColorRGBA;
import zenith.zov.utility.render.display.shader.DrawUtil;

import java.util.List;

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
        float opacity = 0.75f; // Будет получать из настроек модуля
        ColorRGBA bgColor = theme.getBackgroundColor().mulAlpha(opacity);
        ColorRGBA accentColor = theme.getColor().mulAlpha(opacity);
        ColorRGBA textColor = theme.getWhite();

        this.width = width;
        this.height = height;

        ctx.pushMatrix();
        {
            // Анимация появления
            ctx.getMatrices().translate(posX + width / 2f, posY + height / 2f, 0f);
            ctx.getMatrices().scale(animation, animation, 1f);
            ctx.getMatrices().translate(-(posX + width / 2f), -(posY + height / 2f), 0f);

            // Простой полупрозрачный фон
            ctx.drawRoundedRect(posX, posY, width, height, BorderRadius.all(6), bgColor);



            float hp = round(PlayerIntersectionUtil.getHealth(target));
            float maxHp = target.getMaxHealth();
            float healthPercent = hp / target.getMaxHealth();
            float barFullWidth = width - padding * 2 - headSize - padding * 2 - 25f; // -25f для HP текста

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

            // HP текст (справа) - только если включен
            boolean showHealthText = true; // Будет получать из настроек модуля
            if (showHealthText) {
                String hpText = (int) round(hp) + "";
                Font hpFont = Fonts.MEDIUM.getFont(fontSize);
                ctx.drawText(hpFont, hpText, posX + width - padding - hpFont.width(hpText), headY - 2f, accentColor);
            }

            // HP бар (под именем) с красивым дизайном - только если включен
            boolean showHealthBar = true; // Будет получать из настроек модуля
            if (showHealthBar) {
                float barX = headX + headSize + padding;
                float barY = headY + 7f;
                float barHeight = 2f;
                ColorRGBA barBg = theme.getForegroundLight().mulAlpha(0.3f);

                ctx.drawRoundedRect(barX, barY, barFullWidth, barHeight, BorderRadius.all(1f), barBg);

                if (animatedHealth > 0) {
                    ctx.drawRoundedRect(barX, barY, animatedHealth, barHeight, BorderRadius.all(1f), accentColor);
                }
            }
        }
        ctx.popMatrix();
    }

    private void drawArmor(CustomDrawContext ctx, PlayerEntity player, float posX, float posY, float headSize, float padding, float fontSize) {
        float boxSizeItem = 10;
        float paddingItem = 3;
        float iconX = posX;
        float iconY = posY + 1;

        Font xFont = Fonts.ICONS.getFont(5f);
        List<ItemStack> armor = player.getInventory().armor;
        ItemStack[] items = {
                player.getMainHandStack(),
                player.getOffHandStack(),
                armor.get(3), armor.get(2), armor.get(1), armor.get(0)
        };
        
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                ctx.getMatrices().push();
                ctx.getMatrices().translate(iconX + (boxSizeItem - 9.6) / 2, iconY + (boxSizeItem - 9.6) / 2, 0);
                ctx.getMatrices().scale(0.6f, 0.6f, 0.6f);
                ctx.drawItem(stack, 0, 0);
                ((DrawContextAccessor) ctx).callDrawItemBar(stack, 0, 0);
                ((DrawContextAccessor) ctx).callDrawCooldownProgress(stack, 0, 0);
                ctx.getMatrices().pop();
            } else {
                ColorRGBA emptyColor = Zenith.getInstance().getThemeManager().getCurrentTheme().getGrayLight();
                ctx.drawText(xFont, "M", iconX + (boxSizeItem - xFont.width("X")) / 2, 
                    iconY + (boxSizeItem - xFont.height()) / 2, emptyColor);
            }
            iconX += boxSizeItem + paddingItem;
        }
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
