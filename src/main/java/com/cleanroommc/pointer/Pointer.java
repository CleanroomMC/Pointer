package com.cleanroommc.pointer;

import com.cleanroommc.pointer.block.BlockPointer;
import com.cleanroommc.pointer.client.model.BlockPointerBakedModel;
import com.cleanroommc.pointer.item.ItemBlockPointer;
import com.cleanroommc.pointer.item.ItemPointer;
import com.cleanroommc.pointer.client.model.SimpleStateMapper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import com.cleanroommc.pointer.block.tile.TilePointer;

@Mod(modid = "pointer", name = "Pointer", version = "2.1", dependencies = "required:mixinbooter")
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
        ItemBlockPointer.INSTANCE = new ItemBlockPointer(BlockPointer.INSTANCE);
        event.getRegistry().registerAll(ItemBlockPointer.INSTANCE);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        BlockPointer.INSTANCE = new BlockPointer(Material.IRON);
        event.getRegistry().register(BlockPointer.INSTANCE);
    }

    @SubscribeEvent
    public void onRegisterEntities(RegistryEvent.Register<EntityEntry> event) {
        GameRegistry.registerTileEntity(TilePointer.class, new ResourceLocation("pointer", "tilepointer"));
    }

    @SubscribeEvent
    public void onConfigChangedListener(ConfigChangedEvent.OnConfigChangedEvent event) {
        if ("pointer".equals(event.getModID())) {
            ConfigManager.sync("pointer", Type.INSTANCE);
        }
    }

    public static class ClientEventHandler {

        @SubscribeEvent
        public void onModelRegistry(ModelRegistryEvent event) {
            ModelLoader.setCustomModelResourceLocation(ItemPointer.INSTANCE, 0, new ModelResourceLocation(ItemPointer.INSTANCE.getRegistryName(), "inventory"));
            ModelLoader.setCustomModelResourceLocation(ItemBlockPointer.INSTANCE, 0, new ModelResourceLocation(ItemBlockPointer.INSTANCE.getRegistryName(), "inventory"));
            ModelLoader.setCustomStateMapper(BlockPointer.INSTANCE, new SimpleStateMapper(BlockPointer.MODEL_LOCATION));
            ModelLoader.setCustomModelResourceLocation(ItemBlockPointer.INSTANCE, 0, new ModelResourceLocation(ItemBlockPointer.INSTANCE.getRegistryName(), "normal"));
        }

        @SubscribeEvent
        public void onModelsBake(ModelBakeEvent event) {
            event.getModelRegistry().putObject(BlockPointer.MODEL_LOCATION, new BlockPointerBakedModel());
        }

    }

}
