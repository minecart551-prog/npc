package noppes.npcs;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import noppes.npcs.containers.*;

public class CustomContainer {
    public static MenuType<ContainerCarpentryBench> container_carpentrybench;
    public static MenuType<ContainerCustomGui> container_customgui;
    public static MenuType<ContainerMail> container_mail;
    public static MenuType<ContainerManageBanks> container_managebanks;
    public static MenuType<ContainerManageRecipes> container_managerecipes;
    public static MenuType<ContainerMerchantAdd> container_merchantadd;
    public static MenuType<ContainerNPCBankInterface> container_banklarge;
    public static MenuType<ContainerNPCBankInterface> container_banksmall;
    public static MenuType<ContainerNPCBankInterface> container_bankunlock;
    public static MenuType<ContainerNPCBankInterface> container_bankupgrade;
    public static MenuType<ContainerNPCCompanion> container_companion;
    public static MenuType<ContainerNPCFollower> container_follower;
    public static MenuType<ContainerNPCFollowerHire> container_followerhire;
    public static MenuType<ContainerNPCFollowerSetup> container_followersetup;
    public static MenuType<ContainerNPCInv> container_inv;
    public static MenuType<ContainerNpcItemGiver> container_itemgiver;
    public static MenuType<ContainerNpcQuestReward> container_questreward;
    public static MenuType<ContainerNpcQuestTypeItem> container_questtypeitem;
    public static MenuType<ContainerNPCTrader> container_trader;
    public static MenuType<ContainerNPCTraderSetup> container_tradersetup;

    public static void registerContainers() {
        container_carpentrybench = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_carpentrybench", createContainer((containerId, inv, data) -> new ContainerCarpentryBench(containerId, inv, data.readBlockPos())));
        container_customgui = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_customgui", createContainer((containerId, inv, data) -> new ContainerCustomGui(containerId, data.readNbt())));
        container_mail = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_mail", createContainer((containerId, inv, data) -> new ContainerMail(containerId, inv, data.readBoolean(), data.readBoolean())));
        container_managebanks = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_managebanks", createContainer((containerId, inv, data) -> new ContainerManageBanks(containerId, inv)));
        container_managerecipes = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_managerecipes", createContainer((containerId, inv, data) -> new ContainerManageRecipes(containerId, inv, data.readInt())));
        container_merchantadd = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_merchantadd", createContainer((containerId, inv, data) -> new ContainerMerchantAdd(containerId, inv)));
        container_banklarge = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_banklarge", createContainer((containerId, inv, data) -> new ContainerNPCBankLarge(containerId, inv, data.readInt(), data.readInt())));
        container_banksmall = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_banksmall", createContainer((containerId, inv, data) -> new ContainerNPCBankSmall(containerId, inv, data.readInt(), data.readInt())));
        container_bankunlock = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_bankunlock", createContainer((containerId, inv, data) -> new ContainerNPCBankUnlock(containerId, inv, data.readInt(), data.readInt())));
        container_bankupgrade = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_bankupgrade", createContainer((containerId, inv, data) -> new ContainerNPCBankUpgrade(containerId, inv, data.readInt(), data.readInt())));
        container_companion = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_companion", createContainer((containerId, inv, data) -> new ContainerNPCCompanion(containerId, inv, data.readInt())));
        container_follower = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_follower", createContainer((containerId, inv, data) -> new ContainerNPCFollower(containerId, inv, data.readInt())));
        container_followerhire = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_followerhire", createContainer((containerId, inv, data) -> new ContainerNPCFollowerHire(containerId, inv, data.readInt())));
        container_followersetup = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_followersetup", createContainer((containerId, inv, data) -> new ContainerNPCFollowerSetup(containerId, inv, data.readInt())));
        container_inv = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_inv", createContainer((containerId, inv, data) -> new ContainerNPCInv(containerId, inv, data.readInt())));
        container_itemgiver = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_itemgiver", createContainer((containerId, inv, data) -> new ContainerNpcItemGiver(containerId, inv, data.readInt())));
        container_questreward = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_questreward", createContainer((containerId, inv, data) -> new ContainerNpcQuestReward(containerId, inv)));
        container_questtypeitem = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_questtypeitem", createContainer((containerId, inv, data) -> new ContainerNpcQuestTypeItem(containerId, inv)));
        container_trader = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_trader", createContainer((containerId, inv, data) -> new ContainerNPCTrader(containerId, inv, data.readInt())));
        container_tradersetup = Registry.register(BuiltInRegistries.MENU, CustomNpcs.MODID + ":container_tradersetup", createContainer((containerId, inv, data) -> new ContainerNPCTraderSetup(containerId, inv, data.readInt())));
    }

    private static <T extends AbstractContainerMenu> MenuType<T> createContainer(ExtendedScreenHandlerType.ExtendedFactory<T> factoryIn){
        return new ExtendedScreenHandlerType<>(factoryIn);
    }
}
