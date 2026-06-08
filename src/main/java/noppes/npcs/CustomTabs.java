package noppes.npcs;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CustomTabs {
    public static void registerCreativeTab() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, new ResourceLocation(CustomNpcs.MODID, "cnpcs"), FabricItemGroup.builder().title(Component.literal("cnpcs")).icon(() -> CustomItems.wand.getDefaultInstance()).displayItems((params, output) -> {
            output.accept(CustomItems.wand.getDefaultInstance());
            output.accept(CustomItems.cloner.getDefaultInstance());
            output.accept(CustomItems.scripter.getDefaultInstance());
            output.accept(CustomItems.moving.getDefaultInstance());
            output.accept(CustomItems.mount.getDefaultInstance());
            output.accept(CustomItems.teleporter.getDefaultInstance());
            output.accept(CustomItems.scripted_item.getDefaultInstance());
            output.accept(CustomItems.nbt_book.getDefaultInstance());
            output.accept(CustomItems.soulstoneEmpty.getDefaultInstance());

            output.accept(CustomBlocks.redstone_item.getDefaultInstance());
            output.accept(CustomBlocks.waypoint_item.getDefaultInstance());
            output.accept(CustomBlocks.border_item.getDefaultInstance());
            output.accept(CustomBlocks.scripted_item.getDefaultInstance());
            output.accept(CustomBlocks.scripted_door_item.getDefaultInstance());
            output.accept(CustomBlocks.builder_item.getDefaultInstance());
            output.accept(CustomBlocks.copy_item.getDefaultInstance());
            output.accept(CustomBlocks.carpentry_item.getDefaultInstance());
            output.accept(CustomBlocks.mailbox_item.getDefaultInstance());
            output.accept(CustomBlocks.mailbox2_item.getDefaultInstance());
            output.accept(CustomBlocks.mailbox3_item.getDefaultInstance());
        }).build());
    }
}
