package witixin.wanderingfighters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.apache.logging.log4j.LogManager;

import java.util.Optional;

public class WanderingShopStructure extends Structure {

    public static final Codec<WanderingShopStructure> CODEC = RecordCodecBuilder.<WanderingShopStructure>mapCodec(instance ->
            instance.group(WanderingShopStructure.settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
                    Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter)
            ).apply(instance, WanderingShopStructure::new)).codec();


    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    public WanderingShopStructure(Structure.StructureSettings config,
                         Holder<StructureTemplatePool> startPool,
                         Optional<ResourceLocation> startJigsawName,
                         int size,
                         HeightProvider startHeight,
                         Optional<Heightmap.Types> projectStartToHeightmap,
                         int maxDistanceFromCenter)
    {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {

        int startY = this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));

        ChunkPos chunkPos = context.chunkPos();
        BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), startY, chunkPos.getMinBlockZ());
        Optional<GenerationStub> structurePiecesGenerator =
                JigsawPlacement.addPieces(context, this.startPool, this.startJigsawName, this.size, blockPos,
                        false, this.projectStartToHeightmap, this.maxDistanceFromCenter);

        return structurePiecesGenerator;
    }

    @Override
    public void afterPlace(WorldGenLevel worldGenLevel, StructureManager p_226561_, ChunkGenerator p_226562_, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, PiecesContainer piecesContainer) {
        PoolElementStructurePiece structurePiece = ((PoolElementStructurePiece)piecesContainer.pieces().get(0));
        BlockPos corePos = structurePiece.getPosition().offset(getVectorFromRotation(structurePiece.getRotation(), 8));
        if (boundingBox.isInside(corePos)) {
            for (int i = 0; i < 3; ++i) {
                if (randomSource.nextBoolean()) {
                    WanderingTrader trader = EntityType.WANDERING_TRADER.create(worldGenLevel.getLevel());
                    trader.setPersistenceRequired();
                    trader.moveTo(corePos.getX(), corePos.getY(), corePos.getZ());
                    trader.finalizeSpawn(worldGenLevel, worldGenLevel.getCurrentDifficultyAt(corePos), MobSpawnType.STRUCTURE, null, null);
                    trader.setWanderTarget(corePos);
                    trader.restrictTo(corePos, 8);
                    worldGenLevel.addFreshEntityWithPassengers(trader);
                    ((WanderingTraderInterface)(trader)).setChameleonVillager(true);
                    for (int j = 0; j < 2; ++j) {
                        if (randomSource.nextBoolean()) {
                            TraderLlama traderLlama = EntityType.TRADER_LLAMA.create(worldGenLevel.getLevel());
                            traderLlama.setPersistenceRequired();
                            traderLlama.moveTo(corePos.getX() - i, corePos.getY(), corePos.getZ() - j);
                            traderLlama.finalizeSpawn(worldGenLevel, worldGenLevel.getCurrentDifficultyAt(corePos), MobSpawnType.STRUCTURE, null, null);
                            worldGenLevel.addFreshEntityWithPassengers(traderLlama);
                            traderLlama.setLeashedTo(trader, true);
                        }
                    }
                }
            }
        }
    }


    private Vec3i getVectorFromRotation(Rotation rotation, int range) {
        switch (rotation) {
            case NONE -> new Vec3i(range, 0, 0);
            case CLOCKWISE_90 -> new Vec3i(-range, 0, range);
            case CLOCKWISE_180 -> new Vec3i(-range, 0, -range);
            case COUNTERCLOCKWISE_90 -> new Vec3i(range, 0, -range);
        }
        return Vec3i.ZERO;
    }


    @Override
    public StructureType<?> type() {
        return WanderingFighters.WANDERING_SHOP.get();
    }
}
