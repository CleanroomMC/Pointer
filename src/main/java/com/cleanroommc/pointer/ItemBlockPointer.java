package com.cleanroommc.pointer;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tile.TilePointer;

import javax.annotation.Nullable;

public class ItemBlockPointer extends ItemBlock implements ITileEntityProvider {

    public static ItemBlockPointer INSTANCE;

    public ItemBlockPointer(Block block) {
        super(block);
        setRegistryName("pointer", "remote_control_station");
        setTranslationKey("remote_control_station");
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TilePointer();
    }
}
