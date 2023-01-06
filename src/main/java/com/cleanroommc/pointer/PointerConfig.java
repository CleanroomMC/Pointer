package com.cleanroommc.pointer;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;

@Config(modid = "pointer")
public class PointerConfig {


    @Name("Pointer Item Max Durability")
    @LangKey("pointer.config.item_max_durability")
    @RangeInt(min = 0, max = Short.MAX_VALUE)
    public static int itemMaxDurability = 0;

    @Name("Pointer Item Durability Damage Mode")
    @LangKey("pointer.config.damage_mode")
    public static DamageMode durabilityMode = DamageMode.SQRT;

}
