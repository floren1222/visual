package zenith.zov.utility.game.other.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import zenith.zov.utility.game.other.MouseButton;
import zenith.zov.utility.interfaces.IMinecraft;
import zenith.zov.utility.render.display.base.UIContext;

public abstract class CustomScreen extends Screen implements IMinecraft {

    protected CustomScreen() {
        super(Text.empty());
    }

    public abstract void render(UIContext context,float mouseX, float mouseY);

    @Override
    public final void render(DrawContext context, int mouseX, int mouseY, float delta) {

       // super.render(context, mouseX, mouseY, delta);

        UIContext uiContext = UIContext.of(context, mouseX, mouseY, delta);

        this.render(uiContext,mouseX,mouseY);
        super.render(context, mouseX, mouseY, delta);

    }

    @Override
    public final boolean mouseClicked(double mouseX, double mouseY, int button) {
        MouseButton mouseButton = MouseButton.fromButtonIndex(button);
        this.onMouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void tick() {}

    @Override
    public final boolean mouseReleased(double mouseX, double mouseY, int button) {
        MouseButton mouseButton = MouseButton.fromButtonIndex(button);

        this.onMouseReleased(mouseX, mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public final boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        MouseButton mouseButton = MouseButton.fromButtonIndex(button);

        this.onMouseDragged(mouseX, mouseY, mouseButton, deltaX, deltaY);

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {}

    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {}

    public void onMouseDragged(double mouseX, double mouseY, MouseButton button, double deltaX, double deltaY) {}
}
