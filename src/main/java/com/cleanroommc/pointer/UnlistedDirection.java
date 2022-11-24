package com.cleanroommc.pointer;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.Arrays;

public enum UnlistedDirection implements IUnlistedProperty<EnumFacing[]> {

    INSTANCE;

    @Override
    public String getName() {
        return "facingArray";
    }

    @Override
    public boolean isValid(EnumFacing[] value) {
        return true;
    }

    @Override
    public Class<EnumFacing[]> getType() {
        return EnumFacing[].class;
    }

    @Override
    public String valueToString(EnumFacing[] value) {
        if (value == null)
            return "";
        return Arrays.toString(value);
    }
}
