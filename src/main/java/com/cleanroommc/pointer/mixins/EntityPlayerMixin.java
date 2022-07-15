package com.cleanroommc.pointer.mixins;

import com.cleanroommc.pointer.EntityPlayerExpansion;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayer.class)
public class EntityPlayerMixin implements EntityPlayerExpansion {

    @Unique private boolean usingPointer = false;
    @Unique private boolean checkPointerUsage = false;

    @Override
    public void setUsingPointer() {
        this.usingPointer = true;
        this.checkPointerUsage = true;
    }

    @Override
    public void onCloseScreen() {
        if (this.usingPointer) {
            this.usingPointer = false;
            this.checkPointerUsage = false;
        }
    }

    @Override
    public boolean canInteract() {
        if (this.usingPointer) {
            this.checkPointerUsage = false;
            return true;
        }
        if (this.checkPointerUsage) {
            this.checkPointerUsage = false;
        }
        return false;
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;canInteractWith(Lnet/minecraft/entity/player/EntityPlayer;)Z"))
    private boolean canInteract(Container instance, EntityPlayer player) {
        return instance.canInteractWith(player) || canInteract();
    }

    @Inject(method = "closeScreen", at = @At("HEAD"))
    private void onCloseScreen(CallbackInfo ci) {
        onCloseScreen();
    }

}
