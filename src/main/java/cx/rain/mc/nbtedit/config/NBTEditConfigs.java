package cx.rain.mc.nbtedit.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class NBTEditConfigs {
    public static ForgeConfigSpec CONFIG;

    public static ForgeConfigSpec.BooleanValue OP_ONLY;
    public static ForgeConfigSpec.BooleanValue CAN_EDIT_OTHER_PLAYERS;
    public static ForgeConfigSpec.ConfigValue<String> LANGUAGE;

    static {
        var builder = new ForgeConfigSpec.Builder();
        builder.comment("General settings.");
        OP_ONLY = builder
                .comment("If true, only op can use nbtedit, otherwise any players in creative mode can use.")
                .define("op_only", true);
        builder.pop();

        CAN_EDIT_OTHER_PLAYERS = builder
                .comment("If true, allows you edit the nbt tags of other players. USE AT YOUR OWN RISK!")
                .define("can_edit_other_players", false);
        builder.pop();

        LANGUAGE = builder
                .comment("Language that NBTEdit use. ",
                        "Check https://github.com/MeowCraftMC/NBTEdit for avaliavle.",
                        "Welcome to help us translate.")
                .define("language", "en_us");
        builder.pop();

        CONFIG = builder.build();
    }
}
