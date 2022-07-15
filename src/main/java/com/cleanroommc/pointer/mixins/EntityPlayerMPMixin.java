package com.cleanroommc.pointer.mixins;

import com.cleanroommc.pointer.EntityPlayerExpansion;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin implements EntityPlayerExpansion {

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;canInteractWith(Lnet/minecraft/entity/player/EntityPlayer;)Z"))
    private boolean canInteract(Container instance, EntityPlayer player) {
        return instance.canInteractWith(player) || canInteract();
    }

    @Inject(method = "closeScreen", at = @At("HEAD"))
    private void onCloseScreen(CallbackInfo ci) {
        onCloseScreen();
    }

}
