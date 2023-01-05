package com.cleanroommc.pointer.block.tile;

import com.cleanroommc.pointer.EntityPlayerExpansion;
import com.cleanroommc.pointer.api.IPointingDevice;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

public class TilePointer extends TileEntity implements IPointingDevice {

    private EnumFacing top, front;
    private boolean reset;

    public EnumFacing getTopFacing() {
        return top;
    }

    public EnumFacing getFrontFacing() {
        return front;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setByte("TopFacing", (byte) this.top.ordinal());
        compound.setByte("FrontFacing", (byte) this.front.ordinal());
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.top = EnumFacing.VALUES[(compound.getByte("TopFacing"))];
        this.front = EnumFacing.VALUES[(compound.getByte("FrontFacing"))];
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        this.readFromNBT(packet.getNbtCompound());
    }

    @Override
    public void runUseLogic(NBTTagCompound tag, World world, EntityPlayer player, EnumHand hand, BlockPos pos) {
        Triple<Float, Float, Float> hitPos = getHitPos(tag);
        if (!world.isRemote) {
            ((EntityPlayerExpansion) player).setUsingPointer();
            runRemoteRightClickRoutine(player, world, hand, pos, getPointerFacing(tag), hitPos.getLeft(), hitPos.getMiddle(), hitPos.getRight());
        }
    }

    public void setFromPointer(ItemStack pointerStack) {
        if (pointerStack.getTagCompound() != null) {
            if (pointerStack.getTagCompound().hasKey("Pointer")) {
                NBTTagCompound tileNBT = this.getTileData();
                tileNBT.setTag("Pointer", pointerStack.getTagCompound().getCompoundTag("Pointer").copy());
            }
        }
        markDirty();
    }

    public void setFacings(EnumFacing top, EnumFacing front) {
        this.top = top;
        this.front = front;
        markDirty();
    }

    public void setTopFacing(EnumFacing top) {
        this.top = top;
        markDirty();
    }

    public void setFrontFacing(EnumFacing front) {
        this.front = front;
        markDirty();
    }

    public boolean attemptReset() {
        if (this.reset) {
            this.getTileData().removeTag("Pointer");
            this.reset = false;
            return true;
        } else {
            this.reset = true;
            return false;
        }
    }

}
