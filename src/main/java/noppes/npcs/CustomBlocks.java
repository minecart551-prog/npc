package noppes.npcs;

import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import noppes.npcs.blocks.*;
import noppes.npcs.blocks.tiles.*;
import noppes.npcs.items.ItemNpcBlock;
import noppes.npcs.items.ItemScriptedDoor;

public class CustomBlocks {
    public static Block redstone = new BlockNpcRedstone();
    public static Item redstone_item = createItem(redstone);
    public static Block mailbox = new BlockMailbox(0);
    public static Item mailbox_item = createItem(mailbox);
    public static Block mailbox2 = new BlockMailbox(1);
    public static Item mailbox2_item = createItem(mailbox2);
    public static Block mailbox3 = new BlockMailbox(2);
    public static Item mailbox3_item = createItem(mailbox3);
    public static Block waypoint = new BlockWaypoint();
    public static Item waypoint_item = createItem(waypoint);
    public static Block border = new BlockBorder();
    public static Item border_item = createItem(border);
    public static Block scripted = new BlockScripted();
    public static Item scripted_item = createItem(scripted);
    public static Block scripted_door = new BlockScriptedDoor();
    public static Item scripted_door_item = new ItemScriptedDoor(scripted_door);
    public static Block builder = new BlockBuilder();
    public static Item builder_item = createItem(builder);
    public static Block copy = new BlockCopy();
    public static Item copy_item = createItem(copy);
    public static Block carpenty = new BlockCarpentryBench();
    public static Item carpentry_item = createItem(carpenty);
    public static BlockEntityType<TileBlockAnvil> tile_anvil = createTile("tileblockanvil", TileBlockAnvil::new, CustomBlocks.carpenty);
    public static BlockEntityType<TileBorder> tile_border = createTile("tilenpcborder", TileBorder::new, CustomBlocks.border);
    public static BlockEntityType<TileBuilder> tile_builder = createTile("tilenpcbuilder", TileBuilder::new, CustomBlocks.builder);
    public static BlockEntityType<TileCopy> tile_copy = createTile("tilenpccopy", TileCopy::new, CustomBlocks.copy);
    public static BlockEntityType<TileMailbox> tile_mailbox = createTile("tilemailbox", TileMailbox::new, CustomBlocks.mailbox, CustomBlocks.mailbox2, CustomBlocks.mailbox3);
    public static BlockEntityType<TileRedstoneBlock> tile_redstoneblock = createTile("tileredstoneblock", TileRedstoneBlock::new, CustomBlocks.redstone);
    public static BlockEntityType<TileScripted> tile_scripted = createTile("tilenpcscripted", TileScripted::new, CustomBlocks.scripted);
    public static BlockEntityType<TileScriptedDoor> tile_scripteddoor = createTile("tilenpcscripteddoor", TileScriptedDoor::new, CustomBlocks.scripted_door);
    public static BlockEntityType<TileWaypoint> tile_waypoint = createTile("tilewaypoint", TileWaypoint::new, CustomBlocks.waypoint);

    public static void registerBlocks() {
            Registry.register(BuiltInRegistries.BLOCK,CustomNpcs.MODID+ ":npcredstoneblock", redstone);
            Registry.register(BuiltInRegistries.BLOCK,CustomNpcs.MODID+ ":npcmailbox",mailbox);
            Registry.register(BuiltInRegistries.BLOCK,CustomNpcs.MODID+ ":npcmailbox2",mailbox2);
            Registry.register(BuiltInRegistries.BLOCK,CustomNpcs.MODID+ ":npcmailbox3",mailbox3);
            Registry.register(BuiltInRegistries.BLOCK,CustomNpcs.MODID+ ":npcwaypoint",waypoint);
            Registry.register(BuiltInRegistries.BLOCK,CustomNpcs.MODID+ ":npcborder",border);
            Registry.register(BuiltInRegistries.BLOCK,CustomNpcs.MODID+ ":npcscripted",scripted);
            Registry.register(BuiltInRegistries.BLOCK,CustomNpcs.MODID+ ":npcscripteddoor",scripted_door);
            Registry.register(BuiltInRegistries.BLOCK,CustomNpcs.MODID+ ":npcbuilderblock",builder);
            Registry.register(BuiltInRegistries.BLOCK,CustomNpcs.MODID+ ":npccopyblock",copy);
            Registry.register(BuiltInRegistries.BLOCK,CustomNpcs.MODID+ ":npccarpentybench",carpenty);

            Registry.register(BuiltInRegistries.ITEM,CustomNpcs.MODID+ ":npcredstoneblock", redstone_item);
            Registry.register(BuiltInRegistries.ITEM,CustomNpcs.MODID+ ":npcmailbox",mailbox_item);
            Registry.register(BuiltInRegistries.ITEM,CustomNpcs.MODID+ ":npcmailbox2",mailbox2_item);
            Registry.register(BuiltInRegistries.ITEM,CustomNpcs.MODID+ ":npcmailbox3",mailbox3_item);
            Registry.register(BuiltInRegistries.ITEM,CustomNpcs.MODID+ ":npcwaypoint",waypoint_item);
            Registry.register(BuiltInRegistries.ITEM,CustomNpcs.MODID+ ":npcborder",border_item);
            Registry.register(BuiltInRegistries.ITEM,CustomNpcs.MODID+ ":npcscripted",scripted_item);
            Registry.register(BuiltInRegistries.ITEM,CustomNpcs.MODID+ ":npcscripteddoortool",scripted_door_item);
            Registry.register(BuiltInRegistries.ITEM,CustomNpcs.MODID+ ":npcbuilderblock", builder_item);
            Registry.register(BuiltInRegistries.ITEM,CustomNpcs.MODID+ ":npccopyblock",copy_item);
            Registry.register(BuiltInRegistries.ITEM,CustomNpcs.MODID+ ":npccarpentybench",carpentry_item);

            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,CustomNpcs.MODID + ":tileblockanvil", tile_anvil);
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,CustomNpcs.MODID + ":tilenpcborder", tile_border);
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,CustomNpcs.MODID + ":tilenpcbuilder", tile_builder);
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,CustomNpcs.MODID + ":tilenpccopy", tile_copy);
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,CustomNpcs.MODID + ":tilemailbox", tile_mailbox);
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,CustomNpcs.MODID + ":tileredstoneblock", tile_redstoneblock);
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,CustomNpcs.MODID + ":tilenpcscripted", tile_scripted);
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,CustomNpcs.MODID + ":tilenpcscripteddoor", tile_scripteddoor);
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,CustomNpcs.MODID + ":tilewaypoint", tile_waypoint);
    }
    private static <T extends BlockEntity> BlockEntityType<T> createTile(String key, BlockEntityType.BlockEntitySupplier<T> factoryIn, Block... blocks){
        BlockEntityType.Builder<T> builder = BlockEntityType.Builder.of(factoryIn, blocks);
        return builder.build(Util.fetchChoiceType(References.BLOCK_ENTITY, key));
    }

    public static Item createItem(Block block){
        Item item = new ItemNpcBlock(block, new Item.Properties());
        return item;
    }
}