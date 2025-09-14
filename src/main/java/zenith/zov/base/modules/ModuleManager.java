package zenith.zov.base.modules;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;


import zenith.zov.base.events.impl.input.EventKey;
import zenith.zov.client.modules.api.Module;
import zenith.zov.client.modules.impl.combat.*;
import zenith.zov.client.modules.impl.misc.*;
import zenith.zov.client.modules.impl.movement.*;
import zenith.zov.client.modules.impl.render.*;
import zenith.zov.utility.interfaces.IMinecraft;
import zenith.zov.client.modules.impl.player.AutoTool;
import zenith.zov.client.modules.impl.player.AutoArmor;


import java.util.*;

@Getter
public final class ModuleManager implements IMinecraft {

    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        init();
        EventManager.register(this);
    }

    private void init() {
        registerCombat();
        registerMovement();
        registerRender();
        registerPlayer();
        registerMisc();
    }

    private void registerCombat() {

        registerModule(AutoSwap.INSTANCE);

    }

    private void registerMovement() {

        registerModule(AutoSprint.INSTANCE);
        registerModule(GuiWalk.INSTANCE);
    }

    private void registerRender() {
        registerModule(Interface.INSTANCE);
        registerModule(Crosshair.INSTANCE);

        registerModule(Menu.INSTANCE);
        registerModule(NoRender.INSTANCE);
        registerModule(Predictions.INSTANCE);
        registerModule(BlockESP.INSTANCE);
        registerModule(SwingAnimation.INSTANCE);
        registerModule(ViewModel.INSTANCE);
        registerModule(WorldTweaks.INSTANCE);
        registerModule(EntityESP.INSTANCE);
        registerModule(TargetHUD.INSTANCE);
    }

    private void registerPlayer() {
        registerModule(AutoTool.INSTANCE);
        registerModule(AutoArmor.INSTANCE);
    }

    private void registerMisc() {
        registerModule(ElytraHelper.INSTANCE);
        registerModule(ItemScroller.INSTANCE);
        registerModule(ClickAction.INSTANCE);
        registerModule(CameraTweaks.INSTANCE);
        registerModule(AutoAuth.INSTANCE);
        registerModule(AutoDuels.INSTANCE);
        registerModule(AHHelper.INSTANCE);
        registerModule(AutoAccept.INSTANCE);
        registerModule(AutoRespawn.INSTANCE);
        registerModule(NameProtect.INSTANCE);
        registerModule(ClanUpgrade.INSTANCE);
    }

    private void registerModule(Module module) {
        modules.add(module);
    }


    public Module getModule(String name) {
        return modules.stream()
                .filter(module -> module.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public Set<Module> getActiveModules() {
        Set<Module> active = new HashSet<>();
        for (Module module : modules) {
            if (module.isEnabled()) active.add(module);
        }
        return active;
    }


    @EventTarget
    public void onKey(EventKey event) {

        if (mc.currentScreen != null || event.getAction() != GLFW.GLFW_PRESS) return;

        for (Module module : modules) {
            if (module.getKeyCode() == event.getKeyCode()
                    && module.getKeyCode() != GLFW.GLFW_KEY_UNKNOWN) {
                module.toggle();
            }
        }
    }
}
