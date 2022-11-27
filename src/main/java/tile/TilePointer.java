package tile;

import api.IPointingDevice;
import com.cleanroommc.pointer.EntityPlayerExpansion;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
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
    private EnumFacing[] facings;
    private boolean reset;

    public TilePointer() {
        super();
        facings = new EnumFacing[2];
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("topFacing", facings[0].ordinal());
        compound.setInteger("frontFacing", facings[1].ordinal());
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        facings[0] = EnumFacing.byIndex(compound.getInteger("topFacing"));
        facings[1] = EnumFacing.byIndex(compound.getInteger("frontFacing"));
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        // getUpdateTag() is called whenever the chunkdata is sent to the
        // client. In contrast getUpdatePacket() is called when the tile entity
        // itself wants to sync to the client. In many cases you want to send
        // over the same information in getUpdateTag() as in getUpdatePacket().
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        // Prepare a packet for syncing our TE to the client. Since we only have to sync the stack
        // and that's all we have we just write our entire NBT here. If you have a complex
        // tile entity that doesn't need to have all information on the client you can write
        // a more optimal NBT here.
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        // Here we get the packet from the server and read it into our client side tile entity
        this.readFromNBT(packet.getNbtCompound());
    }

    @Override
    public void runUseLogic(NBTTagCompound tag, World world, EntityPlayer player, EnumHand hand, BlockPos pos) {
        Triple<Float, Float, Float> hitPos = getHitPos(tag);
        ((EntityPlayerExpansion) player).setUsingPointer();
        if (!world.isRemote) {
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

    public void setFacings(EnumFacing[] facings) {
        this.facings = new EnumFacing[facings.length];
        System.arraycopy(facings, 0, this.facings, 0, facings.length);
        markDirty();
    }

    public EnumFacing[] getFacings() {
        return facings;
    }

    public boolean attemptReset() {
        if (reset) {
            this.getTileData().removeTag("Pointer");
            reset = false;
            return true;
        } else {
            this.reset = true;
            return false;
        }
    }
}
