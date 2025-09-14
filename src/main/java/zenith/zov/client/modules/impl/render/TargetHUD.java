package zenith.zov.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import zenith.zov.Zenith;
import zenith.zov.base.events.impl.input.EventMouse;
import zenith.zov.base.events.impl.input.EventSetScreen;
import zenith.zov.base.events.impl.player.EventAttack;
import zenith.zov.base.events.impl.player.EventUpdate;
import zenith.zov.base.events.impl.render.EventHudRender;
import zenith.zov.client.hud.elements.component.TargetHudComponent;
import zenith.zov.client.hud.elements.draggable.DraggableHudElement;
import zenith.zov.client.modules.api.Category;
import zenith.zov.client.modules.api.Module;
import zenith.zov.client.modules.api.ModuleAnnotation;
import zenith.zov.client.modules.api.setting.impl.BooleanSetting;
import zenith.zov.client.modules.api.setting.impl.ModeSetting;
import zenith.zov.client.modules.api.setting.impl.NumberSetting;
import zenith.zov.utility.game.player.PlayerIntersectionUtil;
import zenith.zov.utility.render.display.base.CustomDrawContext;
import zenith.zov.utility.render.display.base.GuiUtil;

@ModuleAnnotation(name = "Target HUD", category = Category.RENDER, description = "Показывает информацию о цели при атаке или наведении")
public final class TargetHUD extends Module {

    public static final TargetHUD INSTANCE = new TargetHUD();

    private final ModeSetting mode = new ModeSetting("Режим");
    private final ModeSetting.Value attackMode = new ModeSetting.Value(mode, "Атака").select();
    private final ModeSetting.Value crosshairMode = new ModeSetting.Value(mode, "Наведение");
    private final ModeSetting.Value bothMode = new ModeSetting.Value(mode, "Оба");
    
    private final BooleanSetting players = new BooleanSetting("Игроки", "Показывать игроков", true);
    private final BooleanSetting mobs = new BooleanSetting("Мобы", "Показывать мобов", true);
    private final BooleanSetting animals = new BooleanSetting("Животные", "Показывать животных", false);
    private final NumberSetting maxDistance = new NumberSetting("Дистанция", 64.0f, 5.0f, 128.0f, 1.0f);
    private final NumberSetting showDuration = new NumberSetting("Время показа", 3.0f, 0.5f, 10.0f, 0.1f);
    
    // Новые настройки дизайна
    private final BooleanSetting showArmor = new BooleanSetting("Показывать броню", "Отображать экипировку цели", true);
    private final BooleanSetting showHealthBar = new BooleanSetting("Показывать HP бар", "Отображать полосу здоровья", true);
    private final BooleanSetting showHealthText = new BooleanSetting("Показывать HP текст", "Отображать числовое значение здоровья", true);
    private final NumberSetting animationSpeed = new NumberSetting("Скорость анимации", 200.0f, 50.0f, 500.0f, 10.0f);
    private final NumberSetting opacity = new NumberSetting("Прозрачность", 0.85f, 0.3f, 1.0f, 0.05f);

    private LivingEntity currentTarget = null;
    private long lastTargetTime = 0;
    private TargetHudComponent targetHudComponent;
    private boolean isDragging = false;
    private float dragOffsetX, dragOffsetY;
    private boolean demoMode = false;
    private LivingEntity demoTarget = null;

    private TargetHUD() {
        // Создаем собственный TargetHudComponent
        this.targetHudComponent = new TargetHudComponent(
            "TargetHUD_Module", 
            166.5f, 128.5f, 
            960.0f, 495.5f,
            0.0f, 31.75f, 
            DraggableHudElement.Align.CENTER
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        currentTarget = null;
    }

    @EventTarget
    public void onAttack(EventAttack event) {
        if (!attackMode.isSelected() && !bothMode.isSelected()) return;
        
        Entity target = event.getTarget();

        if (target instanceof LivingEntity livingEntity) {
            if (isValidTarget(livingEntity)) {
                setTarget(livingEntity);
            }
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        // Обновляем анимации TargetHudComponent
        if (targetHudComponent != null) {
            targetHudComponent.tick();
        }
        
        // Проверяем, не истекло ли время показа цели (только если не в демо режиме)
        if (!demoMode && currentTarget != null && System.currentTimeMillis() - lastTargetTime > showDuration.getCurrent() * 1000) {
            clearTarget();
            return;
        }
        
        // Проверяем, завершилась ли анимация исчезновения
        if (currentTarget == null && targetHudComponent != null) {
            // Если анимация исчезновения завершилась, полностью очищаем цель
            if (targetHudComponent.getTarget() == null) {
                // Анимация завершена, ничего не делаем
            }
        }
        
        if (!crosshairMode.isSelected() && !bothMode.isSelected()) return;
        
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) mc.crosshairTarget;
            Entity target = entityHit.getEntity();
            
            if (target instanceof LivingEntity livingEntity) {
                if (isValidTarget(livingEntity)) {
                    setTarget(livingEntity);
                }
            }
        } else {
            // Если не наведено на сущность, сбрасываем цель через некоторое время (только если не в демо режиме)
            if (!demoMode && currentTarget != null && System.currentTimeMillis() - lastTargetTime > 500) {
                clearTarget();
            }
        }
    }

    @EventTarget
    public void onRender(EventHudRender event) {
        if (!this.isEnabled() || mc.options.hudHidden) return;
        
        // Рендерим TargetHudComponent если есть активная цель или в демо режиме
        if ((currentTarget != null || demoMode) && targetHudComponent != null) {
            CustomDrawContext ctx = event.getContext();

            float width = mc.getWindow().getWidth() / Interface.INSTANCE.getCustomScale();
            float height = mc.getWindow().getHeight() / Interface.INSTANCE.getCustomScale();
            targetHudComponent.windowResized(width, height);

            targetHudComponent.render(ctx);

            if (mc.currentScreen instanceof ChatScreen && isDragging) {
                Vector2f mousePos = GuiUtil.getMouse(Interface.INSTANCE.getCustomScale());
                float newX = (float) mousePos.getX() - dragOffsetX;
                float newY = (float) mousePos.getY() - dragOffsetY;
                targetHudComponent.set(ctx, newX, newY, Interface.INSTANCE, width, height);
            }
        }
    }

    @EventTarget
    public void onScreenChange(EventSetScreen event) {
        if (event.getScreen() instanceof ChatScreen) {
            // Открыли чат - включаем демо режим
            demoMode = true;
            if (demoTarget == null) {
                // Создаем демо цель (игрок)
                demoTarget = mc.player;
                setTarget(demoTarget);
            }
        } else {
            // Закрыли чат - выключаем демо режим
            if (demoMode) {
                demoMode = false;
                clearTarget();
                demoTarget = null;
            }
        }
    }

    @EventTarget
    public void onMouse(EventMouse event) {
        if (!this.isEnabled() || targetHudComponent == null) return;
        
        // Перетаскивание работает только в чате (демо режим)
        if (!(mc.currentScreen instanceof ChatScreen)) {
            if (isDragging) {
                isDragging = false;
            }
            return;
        }
        
        Vector2f mousePos = GuiUtil.getMouse(Interface.INSTANCE.getCustomScale());
        double mouseX = mousePos.getX();
        double mouseY = mousePos.getY();
        
        if (event.getAction() == 1 && event.getButton() == 0) { // ЛКМ нажата
            if (targetHudComponent.isMouseOver(mouseX, mouseY)) {
                isDragging = true;
                dragOffsetX = (float) mouseX - targetHudComponent.getX();
                dragOffsetY = (float) mouseY - targetHudComponent.getY();
            }
        } else if (event.getAction() == 0) { // ЛКМ отпущена
            if (isDragging) {
                isDragging = false;
                targetHudComponent.release();
            }
        }
    }

    private void setTarget(LivingEntity target) {
        this.currentTarget = target;
        this.lastTargetTime = System.currentTimeMillis();

        // Обновляем цель в TargetHudComponent
        updateTargetHudComponent(target);
    }

    private void clearTarget() {
        // Очищаем цель в TargetHudComponent для анимации исчезновения
        updateTargetHudComponent(null);
        
        // Устанавливаем currentTarget в null только после завершения анимации
        // Это будет сделано в onUpdate когда анимация завершится
    }

    private void updateTargetHudComponent(LivingEntity target) {
        // Обновляем наш собственный TargetHudComponent
        if (targetHudComponent != null) {
            targetHudComponent.setTarget(target);
            if (target != null) {
                // Target set
            } else {
                // Target cleared
            }
        }
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity == null || entity == mc.player) return false;
        if (!entity.isAlive() || entity.getHealth() <= 0) return false;
        
        // Проверяем дистанцию
        if (mc.player.distanceTo(entity) > maxDistance.getCurrent()) {
            return false;
        }
        
        // Проверяем друзей
        if (entity instanceof PlayerEntity player) {
            if (Zenith.getInstance().getFriendManager().isFriend(player.getGameProfile().getName())) {
                return false;
            }
            return players.isEnabled();
        }
        
        // Проверяем мобов
        if (entity instanceof net.minecraft.entity.mob.MobEntity) {
            return mobs.isEnabled();
        }
        
        // Проверяем животных
        if (entity instanceof net.minecraft.entity.passive.AnimalEntity) {
            return animals.isEnabled();
        }
        
        return false;
    }

    public LivingEntity getCurrentTarget() {
        return currentTarget;
    }
}
