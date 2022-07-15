package com.cleanroommc.pointer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPointer extends Item {

    public static ItemPointer INSTANCE;

    public ItemPointer() {
        setRegistryName("pointer", "pointer");
        setTranslationKey("pointer");
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getTagCompound() == null) {
            return ActionResult.newResult(EnumActionResult.PASS, stack);
        }
        if (player.isSneaking()) {
            stack.getTagCompound().removeTag("Pointer");
            if (!world.isRemote) {
                player.sendStatusMessage(new TextComponentTranslation("pointer.message.removal"), true);
            }
        } else {
            if (stack.getTagCompound().hasKey("Pointer", NBT.TAG_COMPOUND)) {
                NBTTagCompound tag = stack.getTagCompound().getCompoundTag("Pointer");
                Integer dimension = getPointerDimension(tag);
                if (dimension == null || dimension != world.provider.getDimension()) {
                    return ActionResult.newResult(EnumActionResult.PASS, stack);
                }
                BlockPos pos = getPointerPos(tag);
                if (pos == null) {
                    return ActionResult.newResult(EnumActionResult.PASS, stack);
                }
                IBlockState state = world.getBlockState(pos);
                if (!state.getBlock().isAir(state, world, pos)) {
                    Triple<Float, Float, Float> hitPos = getHitPos(tag);
                    ((EntityPlayerExpansion) player).setUsingPointer();
                    player.swingArm(hand);
                    player.getCooldownTracker().setCooldown(this, 40);
                    if (!world.isRemote) {
                        runRemoteRightClickRoutine(player, world, hand, pos, getPointerFacing(tag), hitPos.getLeft(), hitPos.getMiddle(), hitPos.getRight());
                    }
                    return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
                }
            }
        }
        return ActionResult.newResult(EnumActionResult.PASS, stack);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null) {
                tag = new NBTTagCompound();
                stack.setTagCompound(tag);
            }
            createOrAmendTag(tag, tag.getCompoundTag("Pointer"), world, pos, facing, hitX, hitY, hitZ, player.isSneaking());
            player.sendStatusMessage(new TextComponentTranslation("pointer.message.acquire", "x: " + pos.getX() + ", y: " + pos.getY() + ", z: " + pos.getZ()), true);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.getTagCompound() != null) {
            if (stack.getTagCompound().hasKey("Pointer", NBT.TAG_COMPOUND)) {
                NBTTagCompound tag = stack.getTagCompound().getCompoundTag("Pointer");
                Integer dimension = getPointerDimension(tag);
                if (dimension == null) {
                    return;
                }
                BlockPos pos = getPointerPos(tag);
                if (pos == null) {
                    return;
                }
                Triple<Float, Float, Float> hitPos = getHitPos(tag);
                tooltip.add(I18n.format("pointer.tooltip.dimension", dimension + " (" + getPointerDimensionName(tag) + ')'));
                tooltip.add(I18n.format("pointer.tooltip.position", ("x: " + pos.getX() + ", y: " + pos.getY() + ", z: " + pos.getZ())));
                tooltip.add(I18n.format("pointer.tooltip.facing", StringUtils.capitalize(getPointerFacing(tag).toString())));
                tooltip.add(I18n.format("pointer.tooltip.hit_vector", ("x: " + hitPos.getLeft() + ", y: " + hitPos.getMiddle() + ", z: " + hitPos.getRight())));
            }
        }
    }

    private NBTTagCompound createOrAmendTag(NBTTagCompound parent, NBTTagCompound tag, World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
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
    private BlockPos getPointerPos(NBTTagCompound tag) {
        return tag.hasKey("Position", NBT.TAG_LONG) ? BlockPos.fromLong(tag.getLong("Position")) : null;
    }

    @Nullable
    private Integer getPointerDimension(NBTTagCompound tag) {
        return tag.hasKey("Dimension", NBT.TAG_INT) ? tag.getInteger("Dimension") : null;
    }

    private String getPointerDimensionName(NBTTagCompound tag) {
        return tag.hasKey("DimensionName", NBT.TAG_STRING) ? tag.getString("DimensionName") : "N/A";
    }

    private EnumFacing getPointerFacing(NBTTagCompound tag) {
        return tag.hasKey("Facing", NBT.TAG_BYTE) ? EnumFacing.VALUES[tag.getByte("Facing")] : EnumFacing.NORTH;
    }

    private Triple<Float, Float, Float> getHitPos(NBTTagCompound tag) {
        MutableTriple<Float, Float, Float> hitPos = MutableTriple.of(0.5F, 0.5F, 0.5F);
        if (tag.hasKey("HitX", NBT.TAG_FLOAT)) {
            hitPos.left = tag.getFloat("HitX");
        }
        if (tag.hasKey("HitY", NBT.TAG_FLOAT)) {
            hitPos.middle = tag.getFloat("HitY");
        }
        if (tag.hasKey("HitZ", NBT.TAG_FLOAT)) {
            hitPos.right = tag.getFloat("HitZ");
        }
        return hitPos;
    }

    // Unused
    private boolean getPointerSneaking(NBTTagCompound tag) {
        return tag.hasKey("Sneaking") && tag.getBoolean("Sneaking");
    }

    // TODO: craft item with pointer for the pointer to mimic right-clicking with specific items
    private void runRemoteRightClickRoutine(EntityPlayer player, World world, EnumHand hand, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
        RightClickBlock event = ForgeHooks.onRightClickBlock(player, hand, pos, facing,
                ForgeHooks.rayTraceEyeHitVec(player, player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() + 1)); // Compatibility purposes
        if (event.isCanceled()) {
            return;
        }
        IBlockState state = world.getBlockState(pos);
        if (event.getUseBlock() != Result.DENY) {
            state.getBlock().onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
        }
    }

}
