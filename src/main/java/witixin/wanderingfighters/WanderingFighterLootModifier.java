package witixin.wanderingfighters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class WanderingFighterLootModifier extends LootModifier {

    public static final Codec<WanderingFighterLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance)
                    .and(instance.group(
                            Codec.INT.fieldOf("emeralds").forGetter(thing -> thing.emeralds),
                            Codec.INT.fieldOf("tradesToDrop").forGetter(thing -> thing.tradesToDrop),
                            ItemStack.CODEC.listOf().optionalFieldOf("additionalContents", List.of()).forGetter(thing -> thing.additionalContents),
                            Codec.BOOL.optionalFieldOf("applyToAllWanderingTraders", false).forGetter(thing -> thing.applyToAllTraders)
                    )).apply(instance, WanderingFighterLootModifier::new));

    private final int emeralds;
    private final int tradesToDrop;
    private final List<ItemStack> additionalContents;

    private final boolean applyToAllTraders;

    public WanderingFighterLootModifier(LootItemCondition[] conditionsIn, int emeralds, int tradesToDrop, List<ItemStack> toDrop, boolean applyToAllTraders) {
        super(conditionsIn);
        this.emeralds = emeralds;
        this.tradesToDrop = tradesToDrop;
        this.additionalContents = toDrop;
        this.applyToAllTraders = applyToAllTraders;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {

        if (context.hasParam(LootContextParams.THIS_ENTITY) && context.getParam(LootContextParams.THIS_ENTITY) instanceof WanderingTrader trader && (applyToAllTraders || ((WanderingTraderInterface) trader).isStoreVillager())) {
            if (emeralds > 0) {
                generatedLoot.add(new ItemStack(Items.EMERALD, emeralds));
            }

            final List<MerchantOffer> offersToDrop = new ArrayList<>();

            final MerchantOffers offers = trader.getOffers();

            while (offersToDrop.size() <= tradesToDrop - 1) {
                final int toGet = context.getRandom().nextInt(offers.size());
                final MerchantOffer offer = offers.get(toGet);
                if (!offersToDrop.contains(offer) && context.getRandom().nextBoolean()) {
                    offersToDrop.add(offer);
                }
            }

            for (MerchantOffer merchantOffer : offersToDrop) {
                generatedLoot.add(merchantOffer.assemble());
                generatedLoot.add(merchantOffer.getBaseCostA().copy());
                generatedLoot.add(merchantOffer.getCostB().copy());
            }

            if (!additionalContents.isEmpty()) {
                additionalContents.forEach(itemStack -> {
                    generatedLoot.add(itemStack.copy());
                });
            }
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
