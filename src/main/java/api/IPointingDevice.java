package api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;

public interface IPointingDevice {

    default void saveTargetToNBT(ItemStack stack, World world, EntityPlayer player, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null) {
                tag = new NBTTagCompound();
                stack.setTagCompound(tag);
            }
            createOrAmendTag(tag, tag.getCompoundTag("Pointer"), world, pos, facing, hitX, hitY, hitZ, true);
            player.sendStatusMessage(new TextComponentTranslation("pointer.message.acquire", "x: " + pos.getX() + ", y: " + pos.getY() + ", z: " + pos.getZ()), true);
        }
    }

    default void clearPointer(NBTTagCompound tagCompound, World world, EntityPlayer player) {
        tagCompound.removeTag("Pointer");
        if (!world.isRemote) {
            player.sendStatusMessage(new TextComponentTranslation("pointer.message.removal"), true);
        }
    }

    default boolean attemptRemoteUse(NBTTagCompound tag, World world, EntityPlayer player, EnumHand hand) {
        Integer dimension = getPointerDimension(tag);
        if (dimension == null || dimension != world.provider.getDimension()) {
            return false;
        }
        BlockPos pos = getPointerPos(tag);
        if (pos == null) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        if (!state.getBlock().isAir(state, world, pos)) {
            runUseLogic(tag, world, player, hand, pos);
            return true;
        }
        return false;
    }

    void runUseLogic(NBTTagCompound tag, World world, EntityPlayer player, EnumHand hand, BlockPos pos);

    default boolean hasTarget(NBTTagCompound nbtTagCompound) {
        return nbtTagCompound.hasKey("Pointer", Constants.NBT.TAG_COMPOUND);
    }


    default NBTTagCompound createOrAmendTag(NBTTagCompound parent, NBTTagCompound tag, World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                            boolean isSneaking) {
        parent.setTag("Pointer", tag);
        tag.setLong("Position", pos.toLong());
        tag.setByte("Facing", (byte) facing.ordinal());
        tag.setInteger("Dimension", world.provider.getDimension());
        tag.setString("DimensionName", WordUtils.capitalizeFully(world.provider.getDimensionType().name(), '_').replaceAll("_", " "));
        tag.setFloat("HitX", hitX);
        tag.setFloat("HitY", hitY);
        tag.setFloat("HitZ", hitZ);
        tag.setBoolean("Sneaking", isSneaking);
        return tag;
    }

    @Nullable
    default BlockPos getPointerPos(NBTTagCompound tag) {
        return tag.hasKey("Position", Constants.NBT.TAG_LONG) ? BlockPos.fromLong(tag.getLong("Position")) : null;
    }

    @Nullable
    default Integer getPointerDimension(NBTTagCompound tag) {
        return tag.hasKey("Dimension", Constants.NBT.TAG_INT) ? tag.getInteger("Dimension") : null;
    }

    default String getPointerDimensionName(NBTTagCompound tag) {
        return tag.hasKey("DimensionName", Constants.NBT.TAG_STRING) ? tag.getString("DimensionName") : "N/A";
    }

    default EnumFacing getPointerFacing(NBTTagCompound tag) {
        return tag.hasKey("Facing", Constants.NBT.TAG_BYTE) ? EnumFacing.VALUES[tag.getByte("Facing")] : EnumFacing.NORTH;
    }

    default Triple<Float, Float, Float> getHitPos(NBTTagCompound tag) {
        MutableTriple<Float, Float, Float> hitPos = MutableTriple.of(0.5F, 0.5F, 0.5F);
        if (tag.hasKey("HitX", Constants.NBT.TAG_FLOAT)) {
            hitPos.left = tag.getFloat("HitX");
        }
        if (tag.hasKey("HitY", Constants.NBT.TAG_FLOAT)) {
            hitPos.middle = tag.getFloat("HitY");
        }
        if (tag.hasKey("HitZ", Constants.NBT.TAG_FLOAT)) {
            hitPos.right = tag.getFloat("HitZ");
        }
        return hitPos;
    }

    default void runRemoteRightClickRoutine(EntityPlayer player, World world, EnumHand hand, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
        PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(player, hand, pos, facing,
                ForgeHooks.rayTraceEyeHitVec(player, player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() + 1)); // Compatibility purposes
        if (event.isCanceled()) {
            return;
        }
        IBlockState state = world.getBlockState(pos);
        if (event.getUseBlock() != Event.Result.DENY) {
            state.getBlock().onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
        }
    }
}
