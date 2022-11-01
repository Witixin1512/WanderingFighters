package witixin.wanderingfighters;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.UUID;


@Mod(WanderingFighters.MODID)
public class WanderingFighters {

    public static final String MODID = "wanderingfighters";
    public static final String NBT_KEY = MODID + "_is_store_villager";

    public static final AttributeModifier DAMAGE_ATTRIBUTE_MODIFIER = new AttributeModifier(UUID.fromString("96b43d56-abd8-4fbe-a48c-aa650b15793c"),
            "wandering_fighters_damage_boost", 12.0, AttributeModifier.Operation.ADDITION);

    //TODO do MAT and WT loot tables

    public static final UUID HEALTH_UUID = UUID.fromString("38862a24-5c80-43bd-8974-bb0b1ac1b34c");
    public static final AttributeModifier HEALTH_ATTRIBUTE_MODIFIER = new AttributeModifier(HEALTH_UUID, "wandering_fighters_health_boost", 12.5, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    public static final RegistryObject<CarpetBlock> CARPET_BLOCK = BLOCK_REGISTER.register("wander_mat", () -> new CarpetBlock(BlockBehaviour.Properties.of(Material.CLOTH_DECORATION, MaterialColor.SNOW).strength(0.1F).sound(SoundType.WOOL)));

    public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<BlockItem> CARPET_BLOCK_ITEM = ITEM_REGISTER.register("wander_mat", () -> new BlockItem(CARPET_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));

    /**
     * We are using the Deferred Registry system to register our structure as this is the preferred way on Forge.
     * This will handle registering the base structure for us at the correct time so we don't have to handle it ourselves.
     */
    public static final DeferredRegister<StructureType<?>> DEFERRED_REGISTRY_STRUCTURE = DeferredRegister.create(Registry.STRUCTURE_TYPE_REGISTRY, MODID);

    /**
     * Registers the base structure itself and sets what its path is. In this case,
     * this base structure will have the resourcelocation of structure_tutorial:sky_structures.
     */
    public static final RegistryObject<StructureType<WanderingShopStructure>> WANDERING_SHOP = DEFERRED_REGISTRY_STRUCTURE.register("wandering_shop", () -> explicitStructureTypeTyping(WanderingShopStructure.CODEC));

    private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(Codec<T> structureCodec) {
        return () -> structureCodec;
    }

    public WanderingFighters() {
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        modbus.addListener(this::entityAttributeModification);
        BLOCK_REGISTER.register(modbus);
        ITEM_REGISTER.register(modbus);
        DEFERRED_REGISTRY_STRUCTURE.register(modbus);
    }


    public void entityAttributeModification(final EntityAttributeModificationEvent event) {
        event.add(EntityType.WANDERING_TRADER, Attributes.ATTACK_DAMAGE, 0.0);
    }

    public static final SoundEvent WANDERING_PEDDLER_SCREAM = new SoundEvent(new ResourceLocation(MODID, "wandering_fighter_scream"));

    @SubscribeEvent
    public void onEntitySpawn(final EntityJoinLevelEvent event) {
        if (event.getEntity().level.isClientSide) return;

        if (event.getEntity() instanceof WanderingTrader trader) {

            List<Goal> goalListToRemove = trader.goalSelector.getAvailableGoals().stream().map(WrappedGoal::getGoal).filter(WanderingFighters::filterWandererGoals).toList();
            goalListToRemove.forEach(trader.goalSelector::removeGoal);
            trader.goalSelector.addGoal(2, new MoveTowardsTargetGoal(trader, 0.6, 32f));
            trader.goalSelector.addGoal(1, new MeleeAttackGoal(trader, 0.8, true){

                @Override
                protected void checkAndPerformAttack(LivingEntity p_25557_, double p_25558_) {
                    double d0 = this.getAttackReachSqr(p_25557_);
                    if (p_25558_ <= d0 && this.getTicksUntilNextAttack() <= 0) {
                        this.mob.playSound(WANDERING_PEDDLER_SCREAM, 1.0f, 0.5f);
                        this.resetAttackCooldown();
                        this.mob.swing(InteractionHand.MAIN_HAND);
                        this.mob.doHurtTarget(p_25557_);
                        if (p_25557_.isDeadOrDying() || p_25557_ instanceof TraderLlama || p_25557_ instanceof WanderingTrader) {
                            mob.setTarget(null);
                        }
                    }
                }
            });
        }
        if (event.getEntity() instanceof TraderLlama llama) {
            List<Goal> goalListToRemove = llama.goalSelector.getAvailableGoals().stream().map(WrappedGoal::getGoal).filter(WanderingFighters::filterLlamaGoals).toList();
            goalListToRemove.forEach(llama.goalSelector::removeGoal);
            llama.goalSelector.addGoal(3, new RangedAttackGoal(llama, 1.3d, 10, 20.0f) {
                @Override
                public void start() {
                    super.start();
                    if (!llama.getAttributes().hasModifier(Attributes.MAX_HEALTH, HEALTH_UUID)) {
                        llama.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(HEALTH_ATTRIBUTE_MODIFIER);
                        llama.setHealth(llama.getMaxHealth());
                    }

                }
            });
        }
    }

    private static boolean filterWandererGoals(Goal goal) {
        return goal instanceof PanicGoal || goal instanceof UseItemGoal<?> || goal instanceof AvoidEntityGoal<?>;
    }

    private static boolean filterLlamaGoals(Goal goal) {
        return goal instanceof PanicGoal || goal instanceof RunAroundLikeCrazyGoal || goal instanceof RangedAttackGoal;
    }

    @SubscribeEvent
    public void onDamageEvent(final LivingDamageEvent event) {
        final LivingEntity livingEntity = event.getEntity();
        if (livingEntity.level.isClientSide) return;

        if (livingEntity instanceof WanderingTrader wanderingTrader) {
            wanderingTrader.setAggressive(true);
            if (event.getSource().getDirectEntity() instanceof LivingEntity attacker) {
                wanderingTrader.setTarget(attacker);
            }
            if (!(wanderingTrader.getAttributes().hasModifier(Attributes.MAX_HEALTH, HEALTH_UUID))) {

                wanderingTrader.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(HEALTH_ATTRIBUTE_MODIFIER);
                wanderingTrader.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(DAMAGE_ATTRIBUTE_MODIFIER);
                wanderingTrader.setHealth(wanderingTrader.getMaxHealth());
                wanderingTrader.setItemSlot(EquipmentSlot.MAINHAND, Items.STICK.getDefaultInstance());
            }
        }
        if (livingEntity instanceof TraderLlama llama) {
            llama.setAggressive(true);
            if (event.getSource().getDirectEntity() instanceof LivingEntity attacker) {
                llama.setTarget(attacker);
            }
            if (!llama.getAttributes().hasModifier(Attributes.MAX_HEALTH, HEALTH_UUID)) {
                llama.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(HEALTH_ATTRIBUTE_MODIFIER);
                llama.setHealth(llama.getMaxHealth());
            }
        }
    }

}
