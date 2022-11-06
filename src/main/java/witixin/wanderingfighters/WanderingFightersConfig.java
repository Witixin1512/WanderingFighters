package witixin.wanderingfighters;

import net.minecraftforge.common.ForgeConfigSpec;

public class WanderingFightersConfig {

    public static final ForgeConfigSpec GENERAL_SPEC;
    public static ForgeConfigSpec.IntValue RESTOCKING_TICK_TIME;
    public static ForgeConfigSpec.DoubleValue TRADER_DAMAGE_ATTRIBUTE_ADDITION;
    public static ForgeConfigSpec.DoubleValue TRADER_HEALTH_ATTRIBUTE_MULTIPLICATION;
    public static ForgeConfigSpec.DoubleValue LLAMA_HEALTH_ATTRIBUTE_MULTIPLICATION;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupConfig(configBuilder);
        GENERAL_SPEC = configBuilder.build();
    }

    private static void setupConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Chooses the time (in ticks) Wandering Traders in Wandering Shops wait for before restocking their inventories");
        RESTOCKING_TICK_TIME = builder.defineInRange("restocking_time", 24000, 0, Integer.MAX_VALUE);
        builder.comment("Specify the damage a Wandering Fighter inflicts on its target");
        TRADER_DAMAGE_ATTRIBUTE_ADDITION = builder.defineInRange("fighter_damage", 12.0, 0, Double.MAX_VALUE);
        builder.comment("Specify the health boost a Wandering Trader gets after being hurt");
        TRADER_HEALTH_ATTRIBUTE_MULTIPLICATION = builder.defineInRange("fighter_health", 12.5, 0, Double.MAX_VALUE);
        builder.comment("Specify the health boost a Trader Llama gets after being hurt or after it's Wandering Trader is hurt");
        TRADER_HEALTH_ATTRIBUTE_MULTIPLICATION = builder.defineInRange("fighter_health", 12.5, 0, Double.MAX_VALUE);
    }
}
