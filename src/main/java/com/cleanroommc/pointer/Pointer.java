package com.cleanroommc.pointer;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = "pointer", name = "Pointer", version = "1.0")
public class Pointer {

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        }
    }

    @SubscribeEvent
    public void onItemRegister(RegistryEvent.Register<Item> event) {
        ItemPointer.INSTANCE = new ItemPointer();
        event.getRegistry().register(ItemPointer.INSTANCE);
    }

    public static class ClientEventHandler {

        @SubscribeEvent
        public void onModelRegistry(ModelRegistryEvent event) {
            ModelLoader.setCustomModelResourceLocation(ItemPointer.INSTANCE, 0, new ModelResourceLocation(ItemPointer.INSTANCE.getRegistryName(), "inventory"));
        }

    }

}
