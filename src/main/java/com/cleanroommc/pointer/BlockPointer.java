package com.cleanroommc.pointer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import tile.TilePointer;

import javax.annotation.Nullable;
import java.util.Map;

public class BlockPointer extends Block implements ITileEntityProvider {
    public static BlockPointer INSTANCE;
    public static Map<EntityPlayer, Long> lastUsed = new Object2ObjectOpenHashMap<>();
    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation("pointer", "render"), "normal");
    public static final IUnlistedProperty<EnumFacing[]> FACING = UnlistedDirection.INSTANCE;

    public BlockPointer(Material materialIn) {
        super(materialIn);
        setRegistryName("pointer", "remote_control_station");
        setTranslationKey("remote_control_station");
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{}, new IUnlistedProperty[]{FACING});
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        IExtendedBlockState extState = (IExtendedBlockState) state;
        if (te instanceof TilePointer) {
            return extState.withProperty(FACING, ((TilePointer) te).getFacings());
        }
        return super.getExtendedState(state, world, pos);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return super.getActualState(state, worldIn, pos);
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
                lastUsed.putIfAbsent(player, System.currentTimeMillis());
                if (System.currentTimeMillis() - lastUsed.get(player) > 2000) {
                    lastUsed.put(player, System.currentTimeMillis());
                } else {
                    return true;
                }
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

            EnumFacing topFacing = tilePointer.getFacings()[0];
            EnumFacing frontFacing = tilePointer.getFacings()[1];
            if (topFacing == axis) {
                tilePointer.setFront(frontFacing.rotateAround(topFacing.getAxis()));
            } else {
                tilePointer.setFront(frontFacing.rotateAround(axis.getAxis()));
                tilePointer.setTop(topFacing.rotateAround(axis.getAxis()));
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

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}
