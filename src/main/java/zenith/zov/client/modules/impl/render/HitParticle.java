package zenith.zov.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.entity.LivingEntity;
import zenith.zov.client.modules.api.Category;
import zenith.zov.client.modules.api.Module;
import zenith.zov.client.modules.api.ModuleAnnotation;
import zenith.zov.base.events.impl.player.EventAttack;
import zenith.zov.client.particles.ModParticles;

@ModuleAnnotation(name = "Hit Particle", category = Category.RENDER, description = "Красивые частицы при ударе")
public final class HitParticle extends Module {
    public static final HitParticle INSTANCE = new HitParticle();

    private HitParticle() {}

    @EventTarget
    public void onAttack(EventAttack event) {
        if (mc.world == null) return;
        if (!(event.getTarget() instanceof LivingEntity entity)) return;

        double x = entity.getX();
        double y = entity.getBodyY(0.5);
        double z = entity.getZ();

        for (int i = 0; i < 8; i++) {
            double vx = (mc.world.random.nextDouble() - 0.5) * 0.1;
            double vy = mc.world.random.nextDouble() * 0.1;
            double vz = (mc.world.random.nextDouble() - 0.5) * 0.1;
            mc.world.addParticle(ModParticles.HIT, x, y, z, vx, vy, vz);
        }
    }
}
