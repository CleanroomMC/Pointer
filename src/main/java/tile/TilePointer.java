package tile;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.ItemStackHandler;

public class TilePointer extends TileEntity {
    private final ItemStackHandler handler;

    public TilePointer() {
        super();
        handler = new ItemStackHandler(1);
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
}
