package zenith.zov.client.hud.style;

import zenith.zov.base.theme.Theme;
import zenith.zov.utility.render.display.base.BorderRadius;
import zenith.zov.utility.render.display.base.CustomDrawContext;
import zenith.zov.utility.render.display.base.color.ColorRGBA;
import zenith.zov.utility.render.display.shader.DrawUtil;

/**
 * Shared helpers for rendering HUD panels with a unified minimalistic style.
 */
public final class HudStyle {
    private HudStyle() {}

    /**
     * Draws a rounded panel with a subtle border using the current theme colors.
     *
     * @param ctx      drawing context
     * @param theme    active theme
     * @param x        panel X
     * @param y        panel Y
     * @param width    panel width
     * @param height   panel height
     * @param radius   border radius
     * @param opacity  panel opacity (0..1)
     */
    public static void drawPanel(CustomDrawContext ctx, Theme theme,
                                 float x, float y, float width, float height,
                                 float radius, float opacity) {
        ColorRGBA bg = theme.getBackgroundColor().mulAlpha(opacity);
        ColorRGBA border = theme.getColor().mulAlpha(opacity);
        DrawUtil.drawBlurHud(ctx.getMatrices(), x, y, width, height, 10, BorderRadius.all(radius), ColorRGBA.WHITE);

        ctx.drawRoundedRect(x, y, width, height, BorderRadius.all(radius), bg);
        ctx.drawRoundedBorder(x, y, width, height, 0.5f, BorderRadius.all(radius), border.mulAlpha(0.5f));
    }
}
