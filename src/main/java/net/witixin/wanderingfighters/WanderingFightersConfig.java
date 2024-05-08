package net.witixin.wanderingfighters;


import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec;

public class WanderingFightersConfig {

    public static final IConfigSpec GENERAL_SPEC;
    public static ModConfigSpec.ConfigValue<Integer> RESTOCKING_TICK_TIME;
    public static ModConfigSpec.ConfigValue<Double> TRADER_DAMAGE_ATTRIBUTE_ADDITION;
    public static ModConfigSpec.ConfigValue<Double> TRADER_HEALTH_ATTRIBUTE_MULTIPLICATION;
    public static ModConfigSpec.ConfigValue<Double> LLAMA_HEALTH_ATTRIBUTE_MULTIPLICATION;

    public static ModConfigSpec.ConfigValue<Double> TRADER_SPEED_BOOST;
    public static ModConfigSpec.ConfigValue<Integer> LLAMA_SPIT_INTERVAL;
    public static ModConfigSpec.ConfigValue<Integer> TRADERS_TO_SPAWN;

    static {
        ModConfigSpec.Builder configBuilder = new ModConfigSpec.Builder();
        setupConfig(configBuilder);
        GENERAL_SPEC = configBuilder.build();
    }

    private static void setupConfig(ModConfigSpec.Builder builder) {
        builder.comment("Specify the time (in ticks) Wandering Traders in Wandering Shops wait for before restocking their inventories");
        RESTOCKING_TICK_TIME = builder.defineInRange("restocking_time", 24000, 0, Integer.MAX_VALUE);
        builder.comment("Specify the damage a Wandering Fighter inflicts on its target. The below number is a multiplier on their base stat.");
        TRADER_DAMAGE_ATTRIBUTE_ADDITION = builder.defineInRange("fighter_damage", 12.0, 0, Double.MAX_VALUE);
        builder.comment("Specify the health boost a Wandering Trader gets after being hurt");
        TRADER_HEALTH_ATTRIBUTE_MULTIPLICATION = builder.defineInRange("fighter_health", 12.5, 0, Double.MAX_VALUE);
        builder.comment("Specify the health boost a Trader Llama gets after being hurt or after it's Wandering Trader is hurt");
        builder.comment("The below number is a multiplier on their base stat.");
        LLAMA_HEALTH_ATTRIBUTE_MULTIPLICATION = builder.defineInRange("llama_health", 12.5, 0, Double.MAX_VALUE);
        builder.comment("Specify the amount of time that needs to pass between Llama Spit attacks. The time is in ticks");
        LLAMA_SPIT_INTERVAL = builder.defineInRange("llama_spit_interval", 10, 1, Integer.MAX_VALUE);
        builder.comment("Specify the speed boost that the wandering trader gets after being enraged. The number below is multiplied by their current stat.");
        TRADER_SPEED_BOOST = builder.defineInRange("trader_speed_boost", 0.5, 0, Double.MAX_VALUE);
        builder.comment("Specify the amount of traders that will spawn in a structure");
        TRADERS_TO_SPAWN = builder.defineInRange("trader_spawn_count",  3, 0, Integer.MAX_VALUE);
    }
}
