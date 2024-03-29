package witixin.wanderingfighters;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import witixin.wanderingfighters.mixin.FireBlockInvoker;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;


@Mod(WanderingFighters.MODID)
public class WanderingFighters {

    public static final String MODID = "wanderingfighters";
    public static final String NBT_KEY = MODID + "_is_store_villager";

    public static final Supplier<AttributeModifier> DAMAGE_ATTRIBUTE_MODIFIER = () -> new AttributeModifier(UUID.fromString("96b43d56-abd8-4fbe-a48c-aa650b15793c"),
            "wandering_fighters_damage_boost", WanderingFightersConfig.TRADER_DAMAGE_ATTRIBUTE_ADDITION.get(), AttributeModifier.Operation.ADDITION);

    public static final UUID HEALTH_UUID = UUID.fromString("38862a24-5c80-43bd-8974-bb0b1ac1b34c");
    public static final Supplier<AttributeModifier> HEALTH_ATTRIBUTE_MODIFIER =
            () -> new AttributeModifier(HEALTH_UUID, "wandering_fighters_health_boost", WanderingFightersConfig.TRADER_HEALTH_ATTRIBUTE_MULTIPLICATION.get(), AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final UUID LLAMA_HEALTH_UUID = UUID.fromString("9e8e06a6-65aa-424d-8a4a-c407333af5bc");
    public static final Supplier<AttributeModifier> LLAMA_HEALTH_ATTRIBUTE_MODIFIER =
            () -> new AttributeModifier(LLAMA_HEALTH_UUID, "wandering_fighters_health_boost", WanderingFightersConfig.LLAMA_HEALTH_ATTRIBUTE_MULTIPLICATION.get(), AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final UUID WANDERING_TRADER_SPEED_BOOST = UUID.fromString("05c4e684-5b8f-4faf-9148-6845417176e3");

    public static final Supplier<AttributeModifier> TRADER_SPEED_BOOST_MODIFIER =
            () -> new AttributeModifier(WANDERING_TRADER_SPEED_BOOST, "wandering_fighters_speed_boost", WanderingFightersConfig.TRADER_SPEED_BOOST.get(), AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    public static final RegistryObject<CarpetBlock> CARPET_BLOCK = BLOCK_REGISTER.register("wander_mat", () -> new CarpetBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.1F).sound(SoundType.WOOL).ignitedByLava()));

    public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<BlockItem> CARPET_BLOCK_ITEM = ITEM_REGISTER.register("wander_mat", () -> new BlockItem(CARPET_BLOCK.get(), new Item.Properties()));

    public static final DeferredRegister<SoundEvent> SOUND_REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    public static final RegistryObject<SoundEvent> WANDERING_PEDDLER_SCREAM = SOUND_REGISTER.register("wandering_fighter_scream", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "wandering_fighter_scream")));

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_DEFERRED_REGISTER = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);

    public static final RegistryObject<Codec<WanderingFighterLootModifier>> GLM =
            LOOT_MODIFIER_DEFERRED_REGISTER.register("loot_modifier_serializer", () -> WanderingFighterLootModifier.CODEC);

    public static final DeferredRegister<StructureType<?>> DEFERRED_REGISTRY_STRUCTURE = DeferredRegister.create(Registries.STRUCTURE_TYPE, MODID);

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
        SOUND_REGISTER.register(modbus);
        LOOT_MODIFIER_DEFERRED_REGISTER.register(modbus);
        modbus.addListener(this::addMatToCreativeTab);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WanderingFightersConfig.GENERAL_SPEC, "wandering_fighters.toml");
        modbus.addListener(this::setupCommon);
    }

    public void setupCommon(final FMLCommonSetupEvent event) {
        event.enqueueWork( () -> ((FireBlockInvoker)(FireBlock) Blocks.FIRE).callSetFlammable(CARPET_BLOCK.get(), 60, 20));
    }

    public void addMatToCreativeTab(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) event.accept(CARPET_BLOCK_ITEM.get().getDefaultInstance());
    }

    public void entityAttributeModification(final EntityAttributeModificationEvent event) {
        event.add(EntityType.WANDERING_TRADER, Attributes.ATTACK_DAMAGE, 0.0);
    }


    @SubscribeEvent
    public void onEntitySpawn(final EntityJoinLevelEvent event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity() instanceof WanderingTrader trader) {

            List<Goal> goalListToRemove = trader.goalSelector.getAvailableGoals().stream().map(WrappedGoal::getGoal).filter(WanderingFighters::filterWandererGoals).toList();
            goalListToRemove.forEach(trader.goalSelector::removeGoal);
            trader.goalSelector.addGoal(2, new MoveTowardsTargetGoal(trader, 1.0, 32f));
            trader.goalSelector.addGoal(1, new MeleeAttackGoal(trader, 1.0, true){

                @Override
                protected void checkAndPerformAttack(LivingEntity p_25557_, double p_25558_) {
                    double d0 = this.getAttackReachSqr(p_25557_);
                    if (p_25558_ <= d0 && this.getTicksUntilNextAttack() <= 0) {
                        this.mob.playSound(WANDERING_PEDDLER_SCREAM.get(), 1.0f, 0.5f);
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
            llama.goalSelector.addGoal(3, new RangedAttackGoal(llama, 1.3d, WanderingFightersConfig.LLAMA_SPIT_INTERVAL.get(), 20.0f) {
                @Override
                public void start() {
                    super.start();
                    if (!llama.getAttributes().hasModifier(Attributes.MAX_HEALTH, LLAMA_HEALTH_UUID)) {
                        llama.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(LLAMA_HEALTH_ATTRIBUTE_MODIFIER.get());
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
        if (livingEntity.level().isClientSide) return;

        if (livingEntity instanceof WanderingTrader wanderingTrader) {
            wanderingTrader.setAggressive(true);
            if (event.getSource().getDirectEntity() instanceof LivingEntity attacker) {
                if (!(attacker instanceof Player player && player.isCreative())) {
                    wanderingTrader.setTarget(attacker);
                }
            }
            if (!(wanderingTrader.getAttributes().hasModifier(Attributes.MAX_HEALTH, HEALTH_UUID))) {

                wanderingTrader.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(HEALTH_ATTRIBUTE_MODIFIER.get());
                wanderingTrader.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(DAMAGE_ATTRIBUTE_MODIFIER.get());
                wanderingTrader.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(TRADER_SPEED_BOOST_MODIFIER.get());
                wanderingTrader.setHealth(wanderingTrader.getMaxHealth());
                wanderingTrader.setItemSlot(EquipmentSlot.MAINHAND, Items.STICK.getDefaultInstance());
            }
        }
        if (livingEntity instanceof TraderLlama llama) {
            llama.setAggressive(true);
            if (event.getSource().getDirectEntity() instanceof LivingEntity attacker) {
                if (!(attacker instanceof Player player && player.isCreative())) {
                    llama.setTarget(attacker);
                }
            }
            if (!llama.getAttributes().hasModifier(Attributes.MAX_HEALTH, LLAMA_HEALTH_UUID)) {
                llama.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(LLAMA_HEALTH_ATTRIBUTE_MODIFIER.get());
                llama.setHealth(llama.getMaxHealth());
            }
        }
    }

}
