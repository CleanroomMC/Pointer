package com.cleanroommc.pointer.block;

import com.cleanroommc.pointer.item.ItemPointer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import com.cleanroommc.pointer.block.tile.TilePointer;
import net.minecraftforge.common.property.Properties.PropertyAdapter;

import javax.annotation.Nullable;

public class BlockPointer extends Block implements ITileEntityProvider {

    public static final AxisAlignedBB SHAPE = Block.FULL_BLOCK_AABB.setMaxY(7 / 16D);
    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation("pointer", "render"), "normal");
    public static final IUnlistedProperty<EnumFacing> TOP_FACING = new PropertyAdapter<>(BlockDirectional.FACING);
    public static final IUnlistedProperty<EnumFacing> FRONT_FACING = new PropertyAdapter<>(BlockDirectional.FACING);

    public static BlockPointer INSTANCE;

    public BlockPointer(Material materialIn) {
        super(materialIn);
        setRegistryName("pointer", "remote_control_station");
        setTranslationKey("remote_control_station");
        setCreativeTab(CreativeTabs.TOOLS);
        setLightLevel(0.875F);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(TOP_FACING, FRONT_FACING).build();
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        IExtendedBlockState extState = (IExtendedBlockState) state;
        if (te instanceof TilePointer) {
            TilePointer pointer = (TilePointer) te;
            return extState.withProperty(TOP_FACING, pointer.getTopFacing()).withProperty(FRONT_FACING, pointer.getFrontFacing());
        }
        return super.getExtendedState(state, world, pos);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
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
            TilePointer tp = (TilePointer) te;

            ItemStack c = player.getHeldItemMainhand();
            if (c.isEmpty() && player.isSneaking()) {
                if (!world.isRemote) {
                    if (!tp.attemptReset()) {
                        player.sendStatusMessage(new TextComponentTranslation("pointer.message.reset", "x: " + pos.getX() + ", y: " + pos.getY() + ", z: " + pos.getZ()), true);
                    } else {
                        tp.clearPointer(tp.getTileData(), world, player);
                        player.sendStatusMessage(new TextComponentTranslation("pointer.message.removal"), true);
                    }
                }
                return true;
            }

            if (!c.isEmpty()) {
                if (c.getItem() instanceof ItemPointer) {
                    (tp).setFromPointer(c);
                    if (!world.isRemote) {
                        player.sendStatusMessage(new TextComponentTranslation("pointer.message.acquire", "x: " + pos.getX() + ", y: " + pos.getY() + ", z: " + pos.getZ()), true);
                        return true;
                    }
                }
            }

            if (!world.isRemote) {
                long current = System.currentTimeMillis();
                if ((current - tp.getLastUsed()) > 2000) {
                    player.sendStatusMessage(new TextComponentTranslation("pointer.message.cooldown"), false);
                } else {
                    tp.setLastUsed(current);
                }
                return true;
            }

            if (tp.hasTarget(tp.getTileData())) {
                if (tp.attemptRemoteUse(tp.getTileData().getCompoundTag("Pointer"), world, player, hand)) {
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        TileEntity te = world.getTileEntity(pos);
        if (te != null) {
            TilePointer tilePointer = (TilePointer) te;
            EnumFacing topFacing = tilePointer.getTopFacing();
            EnumFacing frontFacing = tilePointer.getFrontFacing();
            if (topFacing == axis) {
                tilePointer.setFrontFacing(frontFacing.rotateAround(topFacing.getAxis()));
            } else {
                tilePointer.setFacings(topFacing.rotateAround(axis.getAxis()), frontFacing.rotateAround(axis.getAxis()));
            }
            world.markBlockRangeForRenderUpdate(pos, pos);
            return true;
        }
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }

}
