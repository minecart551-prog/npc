package noppes.npcs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import noppes.npcs.items.*;


public class CustomItems {
    public static Item wand = new ItemNpcWand();
    public static Item cloner = new ItemNpcCloner();
    public static Item scripter = new ItemNpcScripter();
    public static Item moving = new ItemNpcMovingPath();
    public static Item mount = new ItemMounter();
    public static Item teleporter = new ItemTeleporter();
    public static ItemScripted scripted_item = new ItemScripted(new Item.Properties().stacksTo(1));
    public static ItemNbtBook nbt_book = new ItemNbtBook();
    public static final Item soulstoneEmpty = new ItemSoulstoneEmpty();
    public static final Item soulstoneFull = new ItemSoulstoneFilled();

    public static void registerItems() {
        Registry.register(BuiltInRegistries.ITEM, CustomNpcs.MODID + ":npcwand", wand);
        Registry.register(BuiltInRegistries.ITEM, CustomNpcs.MODID + ":npcmobcloner", cloner);
        Registry.register(BuiltInRegistries.ITEM, CustomNpcs.MODID + ":npcscripter", scripter);
        Registry.register(BuiltInRegistries.ITEM, CustomNpcs.MODID + ":npcmovingpath", moving);
        Registry.register(BuiltInRegistries.ITEM, CustomNpcs.MODID + ":npcmounter", mount);
        Registry.register(BuiltInRegistries.ITEM, CustomNpcs.MODID + ":npcteleporter", teleporter);
        Registry.register(BuiltInRegistries.ITEM, CustomNpcs.MODID + ":npcsoulstoneempty", soulstoneEmpty);
        Registry.register(BuiltInRegistries.ITEM, CustomNpcs.MODID + ":npcsoulstonefilled", soulstoneFull);
        Registry.register(BuiltInRegistries.ITEM, CustomNpcs.MODID + ":scripted_item", scripted_item);
        Registry.register(BuiltInRegistries.ITEM, CustomNpcs.MODID + ":nbt_book", nbt_book);
    }

    public static void registerDispenser() {
        DispenserBlock.registerBehavior(soulstoneFull, new DefaultDispenseItemBehavior() {

            @Override
            public ItemStack execute(BlockSource source, ItemStack item) {
                Direction enumfacing = source.getBlockState().getValue(DispenserBlock.FACING);
                double x = source.x() + enumfacing.getStepX();
                double z = source.z() + enumfacing.getStepZ();
                ItemSoulstoneFilled.Spawn(null, item, source.getLevel(), new BlockPos((int) x, (int) source.y(), (int) z));
                item.split(1);
                return item;
            }
        });
    }

//	@OnlyIn(Dist.CLIENT)
//	@SubscribeEvent
//	public void registerModels(ModelRegistryEvent event) {
//        ModelLoader.setCustomStateMapper(mailbox, (new StateMap.Builder()).ignore(BlockMailbox.ROTATION, BlockMailbox.TYPE).build());
//        ModelLoader.setCustomStateMapper(scriptedDoor, (new StateMap.Builder()).ignore(DoorBlock.POWERED).build());
//        ModelLoader.setCustomStateMapper(builder, (new StateMap.Builder()).ignore(BlockBuilder.ROTATION).build());
//        ModelLoader.setCustomStateMapper(carpentyBench, (new StateMap.Builder()).ignore(BlockCarpentryBench.ROTATION).build());
//
//		ForgeHooksClient.registerTESRItemStack(Item.byBlock(carpentyBench), 0, TileBlockAnvil.class);
//		ForgeHooksClient.registerTESRItemStack(Item.byBlock(mailbox), 0, TileMailbox.class);
//		ForgeHooksClient.registerTESRItemStack(Item.byBlock(mailbox), 1, TileMailbox2.class);
//		ForgeHooksClient.registerTESRItemStack(Item.byBlock(mailbox), 2, TileMailbox3.class);
//
//	}
}
