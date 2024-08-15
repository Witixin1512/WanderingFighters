package witixin.wanderingfighters.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import witixin.wanderingfighters.WanderingFighters;
import witixin.wanderingfighters.WanderingFightersConfig;
import witixin.wanderingfighters.WanderingTraderInterface;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin extends AbstractVillager implements WanderingTraderInterface {


    @Unique
    private final ServerBossEvent bossEvent = (ServerBossEvent)(new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.BLUE,
            BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(true);
    @Unique
    boolean isChameleonVillager = false;

    private WanderingTraderMixin(EntityType<? extends AbstractVillager> p_35267_, Level p_35268_) {
        super(p_35267_, p_35268_);
    }

    @Override
    public boolean isChameleonVillager() {
        return isChameleonVillager;
    }

    @Override
    public void setChameleonVillager(boolean value) {
        this.isChameleonVillager = value;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    public void wanderingfighters_addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo info) {
        compoundTag.putBoolean(WanderingFighters.NBT_KEY, isChameleonVillager);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    public void wanderingfighters_readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo callbackInfo) {
        isChameleonVillager = compoundTag.getBoolean(WanderingFighters.NBT_KEY);
    }

    @Inject(method = "maybeDespawn", at = @At("HEAD"))
    public void wanderingfighters_maybeDespawnInject(CallbackInfo callbackInfo){
        WanderingTrader internal = (WanderingTrader)(Object)this;
        if (isChameleonVillager && internal.tickCount % WanderingFightersConfig.RESTOCKING_TICK_TIME.get() == 0) {
            this.offers = new MerchantOffers();
            updateTrades();
        }
    }

    @Inject(method = "Lnet/minecraft/world/entity/npc/WanderingTrader;customServerAiStep()V", at = @At("TAIL"))
    private void wanderingfighters_customServerAiStep(CallbackInfo callbackInfo) {
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Inject(method = "Lnet/minecraft/entity/Entity;startSeenByPlayer(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("TAIL"))
    private void wanderingfighters_startSeenByPlayer(ServerPlayer player, CallbackInfo callbackInfo) {
        bossEvent.addPlayer(player);
    }

    @Inject(method = "Lnet/minecraft/entity/Entity;stopSeenByPlayer(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("TAIL"))
    private void wanderingfighters_stopSeenByPlayer(ServerPlayer player, CallbackInfo callbackInfo) {
        bossEvent.removePlayer(player);
    }

}
