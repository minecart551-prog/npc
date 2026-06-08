package noppes.npcs;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.entity.player.Inventory;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.item.IItemScripted;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.client.*;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.controllers.PresetController;
import noppes.npcs.client.gui.GuiMerchantAdd;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.global.GuiNPCManageBanks;
import noppes.npcs.client.gui.global.GuiNpcManageRecipes;
import noppes.npcs.client.gui.global.GuiNpcQuestReward;
import noppes.npcs.client.gui.mainmenu.GuiNPCInv;
import noppes.npcs.client.gui.player.*;
import noppes.npcs.client.gui.player.companion.GuiNpcCompanionInv;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeItem;
import noppes.npcs.client.gui.roles.GuiNpcFollowerSetup;
import noppes.npcs.client.gui.roles.GuiNpcItemGiver;
import noppes.npcs.client.gui.roles.GuiNpcTraderSetup;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.mixin.MinecraftAccessor;
import noppes.npcs.packets.Packets;

public class CustomNpcsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        ClientProxy.createFolders();
        CustomKeybinds.registerKeys();

        MenuScreens.register(CustomContainer.container_carpentrybench, GuiNpcCarpentryBench::new);
        MenuScreens.register(CustomContainer.container_customgui, (ContainerCustomGui container, Inventory inv, Component comp) -> {
            GuiCustom gui = new GuiCustom(container, inv, comp);
            gui.setGuiData(container.data);
            return gui;
        });
        MenuScreens.register(CustomContainer.container_mail, GuiMailmanWrite::new);
        MenuScreens.register(CustomContainer.container_managebanks, GuiNPCManageBanks::new);
        MenuScreens.register(CustomContainer.container_managerecipes, GuiNpcManageRecipes::new);
        MenuScreens.register(CustomContainer.container_merchantadd, GuiMerchantAdd::new);
        MenuScreens.register(CustomContainer.container_banklarge, GuiNPCBankChest::new);
        MenuScreens.register(CustomContainer.container_banksmall, GuiNPCBankChest::new);
        MenuScreens.register(CustomContainer.container_bankunlock, GuiNPCBankChest::new);
        MenuScreens.register(CustomContainer.container_bankupgrade, GuiNPCBankChest::new);
        MenuScreens.register(CustomContainer.container_companion, GuiNpcCompanionInv::new);
        MenuScreens.register(CustomContainer.container_follower, GuiNpcFollower::new);
        MenuScreens.register(CustomContainer.container_followerhire, GuiNpcFollowerHire::new);
        MenuScreens.register(CustomContainer.container_followersetup, GuiNpcFollowerSetup::new);
        MenuScreens.register(CustomContainer.container_inv, GuiNPCInv::new);
        MenuScreens.register(CustomContainer.container_itemgiver, GuiNpcItemGiver::new);
        MenuScreens.register(CustomContainer.container_questreward, GuiNpcQuestReward::new);
        MenuScreens.register(CustomContainer.container_questtypeitem, GuiNpcQuestTypeItem::new);
        MenuScreens.register(CustomContainer.container_trader, GuiNPCTrader::new);
        MenuScreens.register(CustomContainer.container_tradersetup, GuiNpcTraderSetup::new);

        new MusicController();
        HudRenderCallback.EVENT.register(new OverlayEventHandler());
        ClientTickEvents.START_CLIENT_TICK.register(new ClientTickHandler());
        ScreenEvents.AFTER_INIT.register(new ClientEventHandler());

        Minecraft mc = Minecraft.getInstance();


        //mc.options.loadOptions();

        new PresetController(CustomNpcs.Dir);

        if(CustomNpcs.EnableUpdateChecker){
            VersionChecker checker = new VersionChecker();
            checker.start();
        }
        PixelmonHelper.loadClient();



    }
}
