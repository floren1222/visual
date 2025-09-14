package zenith.zov.client.modules.impl.player;

import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import com.darkmagician6.eventapi.EventTarget;
import zenith.zov.base.events.impl.player.EventUpdate;
import zenith.zov.client.modules.api.Category;
import zenith.zov.client.modules.api.Module;
import zenith.zov.client.modules.api.ModuleAnnotation;
import zenith.zov.client.modules.api.setting.impl.BooleanSetting;
import zenith.zov.client.modules.api.setting.impl.NumberSetting;
import zenith.zov.utility.game.other.MessageUtil;
import zenith.zov.utility.interfaces.IMinecraft;

@ModuleAnnotation(name = "AutoArmor", category = Category.PLAYER, description = "Автоматически экипирует броню")
public final class AutoArmor extends Module implements IMinecraft {
    public static final AutoArmor INSTANCE = new AutoArmor();
    
    private AutoArmor() {
    }

    private final NumberSetting delay = new NumberSetting("Задержка", 25f, 1f, 1000f, 1f);
    private final BooleanSetting notifications = new BooleanSetting("Оповещения", true);
    private final BooleanSetting onlyInInventory = new BooleanSetting("Только в инвентаре", true);
    private final BooleanSetting onlyWhenStill = new BooleanSetting("Только когда стоит", true);

    private long lastEquipTime = 0;

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null) return;
        
        // Проверка условий работы модуля
        if (onlyInInventory.isEnabled() && mc.currentScreen == null) return;
        if (onlyWhenStill.isEnabled() && isMoving()) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastEquipTime < delay.getCurrent()) return;

        for (int i = 0; i < 4; ++i) {
            ItemStack currentArmor = mc.player.getInventory().getArmorStack(i);
            if (currentArmor.isEmpty()) {
                for (int j = 0; j < 36; ++j) {
                    ItemStack stack = mc.player.getInventory().getStack(j);
                    if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem) {
                        ArmorItem armorItem = (ArmorItem) stack.getItem();
                        if (getArmorSlotIndex(armorItem) == i) {
                            int slotToEquip = j;
                            if (slotToEquip < 9) slotToEquip += 36;
                            
                            mc.interactionManager.clickSlot(0, slotToEquip, 0, SlotActionType.QUICK_MOVE, mc.player);
                            lastEquipTime = currentTime;
                            
                            // Отправка уведомления
                            if (notifications.isEnabled()) {
                                sendArmorNotification(armorItem, i);
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean isMoving() {
        return mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
    }

    private int getArmorSlotIndex(ArmorItem armor) {
        String itemName = armor.toString().toLowerCase();
        if (itemName.contains("helmet") || itemName.contains("skull")) return 3;
        if (itemName.contains("chestplate") || itemName.contains("tunic")) return 2;
        if (itemName.contains("leggings") || itemName.contains("pants")) return 1;
        if (itemName.contains("boots") || itemName.contains("shoes")) return 0;
        return 0;
    }

    private void sendArmorNotification(ArmorItem armorItem, int slotIndex) {
        String armorType = getArmorTypeName(slotIndex);
        String materialName = getMaterialName(armorItem);
        String message = "Одет " + materialName + " " + armorType;
        
        MessageUtil.displayMessage(MessageUtil.LogLevel.INFO, message);
    }

    private String getArmorTypeName(int slotIndex) {
        switch (slotIndex) {
            case 3: return "шлем";
            case 2: return "нагрудник";
            case 1: return "поножи";
            case 0: return "ботинки";
            default: return "броня";
        }
    }

    private String getMaterialName(ArmorItem armorItem) {
        if (armorItem == Items.NETHERITE_HELMET || armorItem == Items.NETHERITE_CHESTPLATE || 
            armorItem == Items.NETHERITE_LEGGINGS || armorItem == Items.NETHERITE_BOOTS) {
            return "незеритовый";
        } else if (armorItem == Items.DIAMOND_HELMET || armorItem == Items.DIAMOND_CHESTPLATE || 
                   armorItem == Items.DIAMOND_LEGGINGS || armorItem == Items.DIAMOND_BOOTS) {
            return "алмазный";
        } else if (armorItem == Items.IRON_HELMET || armorItem == Items.IRON_CHESTPLATE || 
                   armorItem == Items.IRON_LEGGINGS || armorItem == Items.IRON_BOOTS) {
            return "железный";
        } else if (armorItem == Items.GOLDEN_HELMET || armorItem == Items.GOLDEN_CHESTPLATE || 
                   armorItem == Items.GOLDEN_LEGGINGS || armorItem == Items.GOLDEN_BOOTS) {
            return "золотой";
        } else if (armorItem == Items.LEATHER_HELMET || armorItem == Items.LEATHER_CHESTPLATE || 
                   armorItem == Items.LEATHER_LEGGINGS || armorItem == Items.LEATHER_BOOTS) {
            return "кожаный";
        } else if (armorItem == Items.CHAINMAIL_HELMET || armorItem == Items.CHAINMAIL_CHESTPLATE || 
                   armorItem == Items.CHAINMAIL_LEGGINGS || armorItem == Items.CHAINMAIL_BOOTS) {
            return "кольчужный";
        } else {
            return "неизвестный";
        }
    }
}
