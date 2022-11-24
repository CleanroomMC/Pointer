package com.cleanroommc.pointer;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Triple;
import tile.TilePointer;

import javax.annotation.Nullable;

public class BlockPointer extends Block implements ITileEntityProvider {
    public static BlockPointer INSTANCE;

    public BlockPointer(Material materialIn) {
        super(materialIn);
        setRegistryName("pointer", "pointerBlock");
        setTranslationKey("pointerBlock");
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TilePointer();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TilePointer) {
            ItemStack c = player.getHeldItemMainhand();
            if (c.getItem() instanceof ItemPointer) {
                ((TilePointer) te).setPointer(c);
            } else {
                ItemStack stack = ((TilePointer) te).getPointer();
                if (!stack.isEmpty()) {
                    if (stack.getTagCompound().hasKey("Pointer", Constants.NBT.TAG_COMPOUND)) {
                        NBTTagCompound tag = stack.getTagCompound().getCompoundTag("Pointer");
                        Integer dimension = ItemPointer.INSTANCE.getPointerDimension(tag);
                        if (dimension == null || dimension != world.provider.getDimension()) {
                            return false;
                        }
                        BlockPos pointerPos = ItemPointer.INSTANCE.getPointerPos(tag);
                        if (pointerPos == null) {
                            return false;
                        }
                        IBlockState targetState = world.getBlockState(pointerPos);
                        if (!targetState.getBlock().isAir(targetState, world, pointerPos)) {
                            Triple<Float, Float, Float> hitPos = ItemPointer.INSTANCE.getHitPos(tag);
                            ((EntityPlayerExpansion) player).setUsingPointer();
                            player.swingArm(hand);
                            player.getCooldownTracker().setCooldown(ItemPointer.INSTANCE, 40);
                            if (!world.isRemote) {
                                ItemPointer.INSTANCE.runRemoteRightClickRoutine(player, world, hand, pointerPos, ItemPointer.INSTANCE.getPointerFacing(tag), hitPos.getLeft(), hitPos.getMiddle(), hitPos.getRight());
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return true;
    }
}
