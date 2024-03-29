package com.cleanroommc.pointer.item;

import com.cleanroommc.pointer.EntityPlayerExpansion;
import com.cleanroommc.pointer.PointerConfig;
import com.cleanroommc.pointer.api.IPointingDevice;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemPointer extends Item implements IPointingDevice {

    public static ItemPointer INSTANCE;

    public ItemPointer() {
        setRegistryName("pointer", "pointer");
        setTranslationKey("pointer");
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return PointerConfig.itemMaxDurability;
    }

    @Override
    public @Nonnull ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getTagCompound() == null) {
            return ActionResult.newResult(EnumActionResult.PASS, stack);
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (player.isSneaking()) {
            clearPointer(tag, world, player);
        } else {
            if (hasTarget(tag)) {
                if (attemptRemoteUse(tag.getCompoundTag("Pointer"), world, player, hand)) {
                    return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
                }
            }
        }
        return ActionResult.newResult(EnumActionResult.PASS, stack);
    }

    @Override
    public void runUseLogic(NBTTagCompound tag, World world, EntityPlayer player, EnumHand hand, BlockPos pos) {
        Triple<Float, Float, Float> hitPos = getHitPos(tag);
        ((EntityPlayerExpansion) player).setUsingPointer();
        player.swingArm(hand);
        player.getCooldownTracker().setCooldown(this, 40);
        if (!world.isRemote) {
            if (PointerConfig.itemMaxDurability > 0) {
                int distance = (int) player.getDistance(pos.getX(), pos.getY(), pos.getZ());
                int damage;
                switch (PointerConfig.durabilityMode) {
                    case SQRT:
                        damage = (int) Math.sqrt(distance);
                        break;
                    case LOG_2:
                        damage = MathHelper.log2(distance);
                        break;
                    case LOG_10:
                        damage = (int) Math.log10(distance);
                        break;
                    default:
                        damage = distance;
                }
                player.getHeldItem(hand).damageItem(Math.max(1, damage), player);
            }
            runRemoteRightClickRoutine(player, world, hand, pos, getPointerFacing(tag), hitPos.getLeft(), hitPos.getMiddle(), hitPos.getRight());
        }
    }

    @Override
    public @Nonnull EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote && player.isSneaking()) {
            saveTargetToNBT(stack, world, player, pos, facing, hitX, hitY, hitZ);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.getTagCompound() != null) {
            NBTTagCompound pointerTag = stack.getTagCompound().getCompoundTag("Pointer");
            if (!pointerTag.isEmpty()) {
                Integer dimension = getPointerDimension(pointerTag);
                if (dimension == null) {
                    return;
                }
                BlockPos pos = getPointerPos(pointerTag);
                if (pos == null) {
                    return;
                }
                Triple<Float, Float, Float> hitPos = getHitPos(pointerTag);
                tooltip.add(I18n.format("pointer.tooltip.dimension", dimension + " (" + getPointerDimensionName(pointerTag) + ')'));
                tooltip.add(I18n.format("pointer.tooltip.position", ("x: " + pos.getX() + ", y: " + pos.getY() + ", z: " + pos.getZ())));
                tooltip.add(I18n.format("pointer.tooltip.facing", StringUtils.capitalize(getPointerFacing(pointerTag).toString())));
                tooltip.add(I18n.format("pointer.tooltip.hit_vector", ("x: " + hitPos.getLeft() + ", y: " + hitPos.getMiddle() + ", z: " + hitPos.getRight())));
            }
        }
    }

    // Unused
    private boolean getPointerSneaking(NBTTagCompound tag) {
        return tag.hasKey("Sneaking") && tag.getBoolean("Sneaking");
    }
    // TODO: craft item with pointer for the pointer to mimic right-clicking with specific items
}
