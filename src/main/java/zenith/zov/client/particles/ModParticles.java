package zenith.zov.client.particles;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import zenith.zov.Zenith;

public final class ModParticles {
    private ModParticles() {}

    public static final SimpleParticleType HIT = FabricParticleTypes.simple();

    public static void register() {
        Registry.register(Registries.PARTICLE_TYPE, Zenith.id("hit_particle"), HIT);
        ParticleFactoryRegistry.getInstance().register(HIT, HitParticleEffect.Factory::new);
    }
}
