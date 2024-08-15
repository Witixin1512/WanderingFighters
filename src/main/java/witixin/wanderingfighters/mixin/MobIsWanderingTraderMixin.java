package witixin.wanderingfighters.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import witixin.wanderingfighters.WanderingTraderInterface;

@Mixin(Mob.class)
public abstract class MobIsWanderingTraderMixin {
    @Inject(method = "Lnet/minecraft/world/entity/Mob;customServerAiStep()V", at = @At("TAIL"))
    private void wanderingfighters_customServerAiStep(CallbackInfo callbackInfo) {
        Mob self = ((Mob) (Object) this);
        if ((Object)this instanceof WanderingTraderInterface wanderingTrader) {
            wanderingTrader.getBossEvent().setProgress(self.getHealth() / self.getMaxHealth());
        }
    }
}
