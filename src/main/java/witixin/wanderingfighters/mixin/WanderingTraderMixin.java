package witixin.wanderingfighters.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import witixin.wanderingfighters.WanderingFighters;
import witixin.wanderingfighters.WanderingTraderInterface;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin extends AbstractVillager implements WanderingTraderInterface {

    boolean isStoreVillager;

    private WanderingTraderMixin(EntityType<? extends AbstractVillager> p_35267_, Level p_35268_) {
        super(p_35267_, p_35268_);
    }

    @Override
    public boolean isStoreVillager() {
        return isStoreVillager;
    }

    @Override
    public void setChameleonVillager(boolean value) {
        this.isStoreVillager = value;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void wanderingfighters_addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo info) {
        compoundTag.putBoolean(WanderingFighters.NBT_KEY, isStoreVillager);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void wanderingfighters_readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo callbackInfo) {
        isStoreVillager = compoundTag.getBoolean(WanderingFighters.NBT_KEY);
    }

    @Inject(method = "maybeDespawn", at = @At("TAIL"))
    public void wanderingfighters_maybeDespawnInject(CallbackInfo callbackInfo){
        if (this.isStoreVillager() && this.tickCount % 100 == 0) {
            this.updateTrades();
        }
    }
}
