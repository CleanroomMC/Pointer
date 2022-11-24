package tile;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Arrays;

public class TilePointer extends TileEntity {
    private final ItemStackHandler handler;
    private EnumFacing[] facings;

    public TilePointer() {
        super();
        handler = new ItemStackHandler(1);
        facings = new EnumFacing[1];
    }

    public ItemStack getPointer() {
        return handler.getStackInSlot(0);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("pointer", handler.serializeNBT());
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        handler.deserializeNBT(compound.getCompoundTag("pointer"));
        super.readFromNBT(compound);
    }

    public void setPointer(ItemStack c) {
        handler.setStackInSlot(0, c.copy());
        markDirty();
    }

    public void setFacings(EnumFacing[] facings) {
        this.facings = new EnumFacing[facings.length];
        System.arraycopy(facings, 0, this.facings, 0, facings.length);
        markDirty();
    }

    public EnumFacing[] getFacings() {
        return facings;
    }
}
