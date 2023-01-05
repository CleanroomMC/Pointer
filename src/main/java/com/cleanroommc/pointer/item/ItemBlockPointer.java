package com.cleanroommc.pointer.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.cleanroommc.pointer.block.tile.TilePointer;

public class ItemBlockPointer extends ItemBlock {

    public static ItemBlockPointer INSTANCE;

    public ItemBlockPointer(Block block) {
        super(block);
        setRegistryName("pointer", "remote_control_station");
        setTranslationKey("remote_control_station");
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        if (!world.setBlockState(pos, newState, 11)) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == this.block) {
            setTileEntityNBT(world, player, pos, stack);
            this.block.onBlockPlacedBy(world, pos, state, player, stack);
            TilePointer tp = (TilePointer) world.getTileEntity(pos);
            if (tp != null) {
                EnumFacing front = side.getAxis() == Axis.Y ? player.getHorizontalFacing().getOpposite() : (player.rotationPitch < 0) ? EnumFacing.DOWN : EnumFacing.UP;
                tp.setFacings(side, front);
            }
            if (player instanceof EntityPlayerMP) {
                CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, stack);
            }
        }
        return true;
    }
}
