package zenith.zov.client.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import zenith.zov.base.events.impl.player.EventUpdate;
import zenith.zov.client.modules.api.Category;
import zenith.zov.client.modules.api.Module;
import zenith.zov.client.modules.api.ModuleAnnotation;
import zenith.zov.client.modules.api.setting.impl.BooleanSetting;
import zenith.zov.client.modules.api.setting.impl.NumberSetting;
import zenith.zov.utility.game.other.MessageUtil;
import zenith.zov.utility.interfaces.IMinecraft;

@ModuleAnnotation(name = "AutoTool", category = Category.PLAYER, description = "Выбирает лучший инструмент для добычи блоков")
public final class AutoTool extends Module implements IMinecraft {

    public static final AutoTool INSTANCE = new AutoTool();
    
    private final NumberSetting delay = new NumberSetting("Задержка", 50f, 10f, 1000f, 10f);
    private final BooleanSetting returnPrevious = new BooleanSetting("Возвращать предмет", "Возвращает предыдущий предмет после добычи", true);
    private final BooleanSetting showMessages = new BooleanSetting("Показывать сообщения", "Показывает сообщения о поиске инструментов", true);
    
    private int previousSlot = -1;
    private long lastSwapTime = 0;
    private boolean isSwapped = false;

    private AutoTool() {
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null || mc.player.isCreative()) {
            resetState();
            return;
        }

        long currentTime = System.currentTimeMillis();
        
        if (mc.interactionManager.isBreakingBlock()) {
            // Начало добычи блока
            if (previousSlot == -1) {
                previousSlot = mc.player.getInventory().selectedSlot;
                lastSwapTime = currentTime;
            }
            
            // Свап на лучший инструмент с задержкой
            if (currentTime - lastSwapTime >= delay.getCurrent() && !isSwapped) {
                int toolSlot = findOptimalTool();
                if (toolSlot != -1) {
                    mc.player.getInventory().selectedSlot = toolSlot;
                    isSwapped = true;
                    lastSwapTime = currentTime;
                } else {
                    if (showMessages.isEnabled()) {
                        MessageUtil.displayMessage(MessageUtil.LogLevel.WARN, "Не найден подходящий инструмент в хотбаре");
                    }
                }
            }
        } else {
            // Конец добычи блока - возвращаем предыдущий предмет
            if (previousSlot != -1 && returnPrevious.isEnabled()) {
                if (currentTime - lastSwapTime >= delay.getCurrent()) {
                    mc.player.getInventory().selectedSlot = previousSlot;
                    resetState();
                }
            } else if (previousSlot != -1) {
                resetState();
            }
        }
    }

    private int findOptimalTool() {
        if (mc.player == null || mc.world == null) return -1;

        if (mc.crosshairTarget instanceof BlockHitResult blockHitResult) {
            Block block = mc.world.getBlockState(blockHitResult.getBlockPos()).getBlock();
            return findTool(block);
        }
        return -1;
    }

    private int findTool(Block block) {
        int bestSlot = -1;
        float bestSpeed = 1.0f;

        // Ищем только в хотбаре (слоты 0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            
            float speed = getMiningSpeed(i, block);

            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        
        return bestSlot;
    }


    private float getMiningSpeed(int slot, Block block) {
        if (mc.player == null) return 0.0f;
        return mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(block.getDefaultState());
    }

    private void resetState() {
        previousSlot = -1;
        isSwapped = false;
        lastSwapTime = 0;
    }

    @Override
    public void onDisable() {
        resetState();
        super.onDisable();
    }
}





