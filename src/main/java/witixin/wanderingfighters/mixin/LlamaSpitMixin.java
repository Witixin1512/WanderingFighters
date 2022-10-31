package witixin.wanderingfighters.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LlamaSpit.class)
public abstract class LlamaSpitMixin extends Projectile {

    private LlamaSpitMixin(EntityType<? extends Projectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
    }

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    public void wanderingfighters_onHitEntity(EntityHitResult result, CallbackInfo callbackInfo) {
        if (this.getOwner() instanceof TraderLlama owner) {
            if (result.getEntity() instanceof TraderLlama || result.getEntity() instanceof WanderingTrader) {
                owner.setTarget(null);
                return;
            }
            if (result.getEntity() instanceof LivingEntity target) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, false));
            }
        }

    }
}
