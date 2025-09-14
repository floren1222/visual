package zenith.zov.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.Vector2f;
import zenith.zov.Zenith;

import zenith.zov.base.events.impl.input.EventSetScreen;
import zenith.zov.base.events.impl.other.EventWindowResize;
import zenith.zov.base.events.impl.player.EventUpdate;
import zenith.zov.base.events.impl.render.EventHudRender;
import zenith.zov.base.events.impl.input.EventMouse;
import zenith.zov.client.hud.elements.component.*;
import zenith.zov.client.hud.elements.draggable.DraggableHudElement;
import zenith.zov.client.modules.api.Category;
import zenith.zov.client.modules.api.Module;
import zenith.zov.client.modules.api.ModuleAnnotation;
import zenith.zov.client.modules.api.setting.impl.BooleanSetting;
import zenith.zov.client.modules.api.setting.impl.MultiBooleanSetting;
import zenith.zov.client.modules.api.setting.impl.NumberSetting;
import zenith.zov.utility.game.other.TextUtil;
import zenith.zov.utility.math.MathUtil;
import zenith.zov.utility.render.display.base.CustomDrawContext;
import zenith.zov.utility.render.display.base.GuiUtil;

import java.util.*;

import static zenith.zov.utility.render.display.Render2DUtil.glowCache;

@ModuleAnnotation(name = "Interface", category = Category.RENDER, description = "Интерфейс Клиента")
public final class Interface extends Module {
    public static final Interface INSTANCE = new Interface();

    private final MultiBooleanSetting elementsSetting = MultiBooleanSetting.create("Элементы", List.of(
            "Ватермарка",      // 0
            "Эффекты",         // 1
            "Стафф",           // 2
            "Уведомления",     // 3
            "Инвентарь",       // 4
            "Кулдауны",        // 5
            "Информация",      // 6
            "Бинды",           // 7
            "Музыка",          // 8
            "Хотбар",          // 9
            "Скрореборд",      // 10
            "Таб"              // 11
            ));

    private final List<DraggableHudElement> elements = new ArrayList<>();
    private DraggableHudElement draggingElement = null;
    private float dragOffsetX, dragOffsetY;
    private final NumberSetting scale = new NumberSetting("Размер", 2, 1, 3, 0.1f, ((oldValue, newValue) -> {
        float width = mc.getWindow().getWidth() / newValue;
        float height = mc.getWindow().getHeight() / newValue;

        for (DraggableHudElement element : elements) {
            element.windowResized(width, height);
        }
    }
    ));
    private BooleanSetting corners = new BooleanSetting("Треугольнички", true);
    private BooleanSetting blur = new BooleanSetting("Блюр", false);
    private BooleanSetting glow = new BooleanSetting("Свечение", false);


    private Interface() {
        // Элементы (порядок соответствует MultiBooleanSetting)
        addElement(new WatermarkComponent("Watermark", 0.0f, 0.0f, 960.0f, 495.5f, 10.0f, 10.0f, DraggableHudElement.Align.TOP_LEFT));         // 0 - Ватермарка
        addElement(new PotionsComponent("Potions", 0.0f, 0.0f, 960.0f, 495.5f, 119.15234f, 73.0f, DraggableHudElement.Align.TOP_LEFT));       // 1 - Эффекты
        addElement(new StaffComponent("Staff", 0.0f, 0.0f, 960.0f, 495.5f, 10.0f, 73.0f, DraggableHudElement.Align.TOP_LEFT));                // 2 - Стафф

        NotifyComponent notifyComponent = new NotifyComponent("Notify", 181.80615f, 135.5f, 960.0f, 495.5f, 157.03516f, -72.5f, DraggableHudElement.Align.CENTER); // 3 - Уведомления
        addElement(notifyComponent);
        Zenith.getInstance().getNotifyManager().setNotifyComponent(notifyComponent);

        addElement(new InventoryComponent("Inventory", 269.0f, 229.0f, 960.0f, 495.5f, -11.5f, -74.0f, DraggableHudElement.Align.BOTTOM_RIGHT)); // 4 - Инвентарь
        addElement(new CooldownComponent("Cooldown", 349.0f, 0.0f, 960.0f, 495.5f, -11.5f, 73.0f, DraggableHudElement.Align.TOP_RIGHT));        // 5 - Кулдауны
        addElement(new InformationComponent("Information", 0.0f, 0.0f, 960.0f, 495.5f, 10.0f, 41.5f, DraggableHudElement.Align.TOP_LEFT));     // 6 - Информация
        addElement(new KeybindsComponent("Keybinds", 349.0f, 0.0f, 960.0f, 495.5f, -122.0f, 73.0f, DraggableHudElement.Align.TOP_RIGHT));      // 7 - Бинды
        // TargetHUD теперь управляется модулем TargetHUD
        addElement(new MusicInfoComponent("MusicInfo", 342.0f, 257.0f, 960.0f, 495.5f, -11.5f, -16.5f, DraggableHudElement.Align.BOTTOM_RIGHT)); // 9 - Музыка
        addElement(new HootBarComponent("Hotbar", 116.5f, 265.0f, 960.0f, 495.5f, 0.0f, -16.5f, DraggableHudElement.Align.BOTTOM_CENTER));      // 10 - Хотбар
        addElement(new ScoreBoardComponent("Скрореборд",0, 0.0f, 960.0f, 495.5f, -10, 10, DraggableHudElement.Align.CENTER_RIGHT));      // 11 - Скрореборд
        addElement(new PlayerListComponent("Таб"));      // 12 - Таб


    }

    long init = 0;

    @Override
    public void onEnable() {
        System.out.println("Interface: Module enabled - registering with EventManager");
        System.out.println("Interface: Elements setting size: " + elementsSetting.getBooleanSettings().size());
        for (int i = 0; i < elementsSetting.getBooleanSettings().size(); i++) {
            System.out.println("Interface: Setting " + i + ": " + elementsSetting.getBooleanSettings().get(i).getName() + " = " + elementsSetting.getBooleanSettings().get(i).isEnabled());
        }
        System.out.println("Interface: Elements count: " + elements.size());
        init = System.currentTimeMillis();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        System.out.println("Interface: Module disabled - unregistering from EventManager");
        System.out.println("Interface: Stack trace for disable:");
        Thread.dumpStack();
        super.onDisable();
    }

    @Override
    public JsonObject save() {
        JsonObject object = super.save();
        JsonObject propertiesObject = new JsonObject();

        for (DraggableHudElement element : elements) {
            propertiesObject.add(element.getName(), element.save());
        }

        object.add("HudElements", propertiesObject);
        return object;
    }

    @Override
    public void load(JsonObject object) {
        System.out.println("Interface: Loading config - enabled before: " + this.isEnabled());
        super.load(object);
        System.out.println("Interface: Loading config - enabled after: " + this.isEnabled());

        if (object.has("HudElements") && object.get("HudElements").isJsonObject()) {
            JsonObject propertiesObject = object.getAsJsonObject("HudElements");

            for (DraggableHudElement element : elements) {
                String key = element.getName();
                if (propertiesObject.has(key) && propertiesObject.get(key).isJsonObject()) {
                    element.load(propertiesObject.getAsJsonObject(key));
                }
            }
        }
    }


    public void addElement(DraggableHudElement element) {
        elements.add(element);
    }

    public void removeElement(DraggableHudElement element) {
        elements.remove(element);
    }

    public List<DraggableHudElement> getElements() {
        return elements;
    }

    @EventTarget
    public void onRender(EventHudRender event) {
        System.out.println("Interface: onRender called - enabled: " + this.isEnabled() + ", HUD hidden: " + mc.options.hudHidden + ", elements count: " + elements.size());
        
        if (!(mc.currentScreen instanceof ChatScreen)) {
            if(draggingElement!=null){
                draggingElement.release();
                draggingElement = null;
            }
        }
        CustomDrawContext ctx = event.getContext();

        float width = mc.getWindow().getWidth() / getCustomScale();
        float height = mc.getWindow().getHeight() / getCustomScale();
        
        if (!mc.options.hudHidden) {
            for (int i = 0; i < elements.size(); i++) {
                DraggableHudElement element = elements.get(i);
                boolean shouldRender = shouldRender(element);
                System.out.println("Interface: Element " + i + " (" + element.getName() + ") should render: " + shouldRender);
                
                if (!shouldRender) continue;

                try {
                    System.out.println("Interface: Rendering element " + element.getName());
                    element.render(ctx);
                } catch (Exception e) {
                    System.out.println("Interface: Error rendering element " + element.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
                if (draggingElement != element) {
                    element.windowResized(width, height);
                }
            }
        } else {
            System.out.println("Interface: HUD is hidden by game options");
        }
        if ((mc.currentScreen instanceof ChatScreen)) {

            if (draggingElement != null) {
                Vector2f mousePos = GuiUtil.getMouse(getCustomScale());
                double mouseX = mousePos.getX();
                double mouseY = mousePos.getY();
                draggingElement.set(ctx, (float) mouseX - dragOffsetX, (float) mouseY - dragOffsetY, this,width,height);

            }
        }


    }


    private boolean shouldRender(DraggableHudElement element) {
        int index = elements.indexOf(element);
        if (index < 0 || index >= elementsSetting.getBooleanSettings().size()) {
            System.out.println("Interface: Element " + element.getName() + " has invalid index: " + index + " (max: " + (elementsSetting.getBooleanSettings().size() - 1) + ")");
            return false;
        }
        boolean enabled = elementsSetting.isEnable(index);
        System.out.println("Interface: Element " + element.getName() + " (index " + index + ") enabled: " + enabled);
        return enabled;
    }

    @EventTarget
    public void onMouse(EventMouse event) {
        if (!(mc.currentScreen instanceof ChatScreen)) {
            draggingElement.release();
            draggingElement = null;
            return;
        }
        Vector2f mousePos = GuiUtil.getMouse(getCustomScale());
        double mouseX = mousePos.getX();
        double mouseY = mousePos.getY();

        if (event.getAction() == 1 && event.getButton() == 0) {
            List<DraggableHudElement> reversedElements = new ArrayList<>(elements);
            Collections.reverse(reversedElements);

            for (DraggableHudElement element : reversedElements) {
                if (shouldRender(element) && element.isMouseOver(mouseX, mouseY)) {
                    draggingElement = element;
                    dragOffsetX = (float) mouseX - element.getX();
                    dragOffsetY = (float) mouseY - element.getY();

                    break;
                }
            }
        } else if (event.getAction() == 0) {
            draggingElement.release();
            draggingElement = null;
        }
    }

    public float getCustomScale() {
        return scale.getCurrent();
    }

    public org.joml.Vector2f getNearest(float x, float y) {

        float minDeltaX = Float.MAX_VALUE;
        float minDeltaY = Float.MAX_VALUE;
        float thoroughness = 2;
        org.joml.Vector2f nearest = new org.joml.Vector2f(-1, -1);
        for (DraggableHudElement s : elements) {
            if (s.equals(draggingElement)) continue;
            float tempXA = s.getX();
            float tempYA = s.getY();

            float tempXB = s.getX() + s.getWidth();
            float tempYB = s.getY() + s.getHeight();

            float tempXC = s.getX() + s.getWidth() / 2;
            float tempYC = s.getY() + s.getHeight() / 2;
            float minX = getNearest(tempXA, tempXB, tempXC, x);
            float minY = getNearest(tempYA, tempYB, tempYC, y);
            float deltaX = MathUtil.goodSubtract(minX, x);
            float deltaY = MathUtil.goodSubtract(minY, y);
            if (deltaX < minDeltaX) {
                minDeltaX = deltaX;
                if (minDeltaX < thoroughness) {
                    nearest.x = minX;

                }
            }
            ;
            if (deltaY < minDeltaY) {
                minDeltaY = deltaY;
                if (minDeltaY < thoroughness) {

                    nearest.y = minY;
                }
            }

        }
        if (nearest.x == -1 || nearest.y == -1) {
            float tempXA = mc.getWindow().getScaledWidth() / 2f;
            float tempYA = mc.getWindow().getScaledHeight() / 2f;


            float minX = getNearest(tempXA, tempXA, tempXA, x);
            float minY = getNearest(tempYA, tempYA, tempYA, y);
            float deltaX = MathUtil.goodSubtract(minX, x);
            float deltaY = MathUtil.goodSubtract(minY, y);

            if (deltaX < minDeltaX && deltaX < thoroughness) {
                nearest.x = minX;
            }
            if (deltaY < minDeltaY && deltaY < thoroughness) {
                nearest.y = minY;
            }
        }
        return nearest;
    }

    public float getNearest(float a, float b, float c, float target) {
        float nearest = a;
        if (MathUtil.goodSubtract(b, target) < MathUtil.goodSubtract(nearest, target)) {
            nearest = b;
        }
        if (MathUtil.goodSubtract(c, target) < MathUtil.goodSubtract(nearest, target)) {
            nearest = c;
        }
        return nearest;
    }
    public boolean isEnableScoreBar() {
        return elementsSetting.isEnable(10); //10 - Скрореборд
    }
    public boolean isEnableHotBar() {
        return elementsSetting.isEnable(9); //9 - Хотбар
    }
    public boolean isEnableTab() {
        return elementsSetting.isEnable(11); //11 - Таб
    }

    @EventTarget
    public void resize(EventWindowResize eventWindowResize) {
        float width = mc.getWindow().getWidth() / getCustomScale();
        float height = mc.getWindow().getHeight() / getCustomScale();

        for (DraggableHudElement element : elements) {

            element.windowResized(width, height);

        }
    }

    @EventTarget
    public void update(EventUpdate eventUpdate) {

        if(glowCache.size()>400){
            glowCache.values().removeIf(v -> {
                if (v.tick()) {
                    v.destroy();
                    return true;
                } else {
                    return false;
                }
            });
        }
        for (DraggableHudElement draggableHudElement : elements) {
            draggableHudElement.tick();
        }
        //draggableHudElement.tick();

        ;
    }

    public boolean isBlur() {
        return blur.isEnabled();
    }

    public boolean isGlow() {
        return glow.isEnabled();
    }

    public boolean isCorners() {
        return corners.isEnabled();
    }
    @EventTarget
    public void screenEvent(EventSetScreen event) {
        if(event.getScreen() instanceof ChatScreen){
            this.init = System.currentTimeMillis();
        }

    }
    public int getGlowRadius() {
        return (int) 10;
    }
}
