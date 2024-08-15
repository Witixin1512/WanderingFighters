package witixin.wanderingfighters.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import witixin.wanderingfighters.WanderingTraderInterface;

@Mixin(Entity.class)
public abstract class EntityIsWanderingTraderMixin {

    @Inject(method = "Lnet/minecraft/world/entity/Entity;startSeenByPlayer(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("TAIL"))
    private void wanderingfighters_startSeenByPlayer(ServerPlayer player, CallbackInfo callbackInfo) {
        if ((Object)this instanceof WanderingTraderInterface wanderingTrader) {
            if (((Mob)(Object)this).isAggressive()) wanderingTrader.getBossEvent().addPlayer(player);
        }
    }

    @Inject(method = "Lnet/minecraft/world/entity/Entity;stopSeenByPlayer(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At(
            "TAIL"))
    private void wanderingfighters_stopSeenByPlayer(ServerPlayer player, CallbackInfo callbackInfo) {
        if ((Object)this instanceof WanderingTraderInterface wanderingTrader) {
            wanderingTrader.getBossEvent().removePlayer(player);
        }
    }

}
