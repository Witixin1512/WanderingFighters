package witixin.wanderingfighters.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FireBlock.class)
public interface FireBlockInvoker {

    @Invoker
    void callSetFlammable(Block p_53445_, int p_53446_, int p_53447_);
}
