package noppes.npcs.api.gui;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.wrapper.gui.CustomGuiButtonWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketGuiParts;

public abstract class MainMenuGui {

    protected CustomGuiWrapper gui;

    public MainMenuGui(int active, EntityCustomNpc npc, IPlayer player){
        this(active, npc, player, true);
    }

    public MainMenuGui(int active, EntityCustomNpc npc, IPlayer player, boolean renderHeader){
        gui = new CustomGuiWrapper(player);
        gui.setBackgroundTexture("customnpcs:textures/gui/components.png");
        gui.setSize(420, 220);
        gui.getBackgroundRect().setPos(0, 20);
        gui.getBackgroundRect().setSize(420, 200);
        gui.getBackgroundRect().setTextureOffset(0, 0);
        gui.getBackgroundRect().setRepeatingTexture(64, 64, 4);
        gui.npc = npc;

        if(renderHeader) {
            int buttonId = 0;
            IButton button = new CustomGuiButtonWrapper().setSize(22, 20);
            ITexturedRect rect = button.getTextureRect().setTexture("customnpcs:textures/gui/components.png").setRepeatingTexture(64, 20, 3).setTextureOffset(0, 64);
            button.setTextureHoverOffset(22).setPos(buttonId * 22 + 4, 0).setID(buttonId++);
            button.setDisplayItem(NpcAPI.Instance().getIItemStack(new ItemStack(Items.ENDER_EYE)));
            button.setHoverText("menu.display");
            button.setOnPress((gui, b) -> player.showCustomGui(new DisplayMenu(npc, player).gui));
            gui.addComponent(button);

            button = new CustomGuiButtonWrapper().setSize(22, 20);
            button.setTextureRect(rect);
            button.setTextureHoverOffset(22).setPos(buttonId * 22 + 4, 0).setID(buttonId++);
            button.setDisplayItem(NpcAPI.Instance().getIItemStack(new ItemStack(Items.PLAYER_HEAD)));
            button.setHoverText("menu.model");
            button.setOnPress((gui, b) -> {
                CustomGuiWrapper wrapper = new ModelMenu(npc, player).gui;
                ((ContainerCustomGui) player.getMCEntity().containerMenu).setGui(wrapper, player.getMCEntity());
                Packets.send(player.getMCEntity(), new PacketGuiParts(npc.getId(), wrapper.toNBT()));
            });
            gui.addComponent(button);

            button = new CustomGuiButtonWrapper().setSize(22, 20);
            button.setTextureRect(rect);
            button.setTextureHoverOffset(22).setPos(buttonId * 22 + 4, 0).setID(buttonId++);
            button.setDisplayItem(NpcAPI.Instance().getIItemStack(new ItemStack(Items.CHEST)));
            button.setHoverText("menu.inventory");
            button.setOnPress((gui, b) -> player.showCustomGui(new InventoryMenu(npc, player).gui));
            gui.addComponent(button);

            button = new CustomGuiButtonWrapper().setSize(22, 20);
            button.setTextureRect(rect);
            button.setTextureHoverOffset(22).setPos(buttonId * 22 + 4, 0).setID(buttonId++);
            button.setDisplayItem(NpcAPI.Instance().getIItemStack(new ItemStack(Items.REDSTONE)));
            button.setHoverText("menu.logic");
            button.setOnPress((gui, b) -> player.showCustomGui(new LogicMenu(npc, player).gui));
            gui.addComponent(button);

            button = new CustomGuiButtonWrapper().setSize(22, 20);
            button.setTextureRect(rect);
            button.setTextureHoverOffset(22).setPos(buttonId * 22 + 4, 0).setID(buttonId++);
            button.setDisplayItem(NpcAPI.Instance().getIItemStack(new ItemStack(Items.DIAMOND_CHESTPLATE)));
            button.setHoverText("menu.health");
            button.setOnPress((gui, b) -> player.showCustomGui(new HealthMenu(npc, player).gui));
            gui.addComponent(button);

            button = new CustomGuiButtonWrapper().setSize(22, 20);
            button.setTextureRect(rect);
            button.setTextureHoverOffset(22).setPos(buttonId * 22 + 4, 0).setID(buttonId++);
            button.setDisplayItem(NpcAPI.Instance().getIItemStack(new ItemStack(Items.TOTEM_OF_UNDYING)));
            button.setHoverText("menu.death");
            button.setOnPress((gui, b) -> player.showCustomGui(new DeathMenu(npc, player).gui));
            gui.addComponent(button);

            button = new CustomGuiButtonWrapper().setSize(22, 20);
            button.setTextureRect(rect);
            button.setTextureHoverOffset(22).setPos(buttonId * 22 + 4, 0).setID(buttonId++);
            button.setDisplayItem(NpcAPI.Instance().getIItemStack(new ItemStack(Items.IRON_BOOTS)));
            button.setHoverText("menu.movement");
            button.setOnPress((gui, b) -> player.showCustomGui(new MovementMenu(npc, player).gui));
            gui.addComponent(button);

            button = new CustomGuiButtonWrapper().setSize(22, 20);
            button.setTextureRect(rect);
            button.setTextureHoverOffset(22).setPos(buttonId * 22 + 4, 0).setID(buttonId++);
            button.setDisplayItem(NpcAPI.Instance().getIItemStack(new ItemStack(Items.DIAMOND_SWORD)));
            button.setHoverText("stats.meleeproperties");
            button.setOnPress((gui, b) -> player.showCustomGui(new MeleeMenu(npc, player).gui));
            gui.addComponent(button);

            button = new CustomGuiButtonWrapper().setSize(22, 20);
            button.setTextureRect(rect);
            button.setTextureHoverOffset(22).setPos(buttonId * 22 + 4, 0).setID(buttonId++);
            button.setDisplayItem(NpcAPI.Instance().getIItemStack(new ItemStack(Items.ARROW)));
            button.setOnPress((gui, b) -> player.showCustomGui(new DisplayMenu(npc, player).gui));
            gui.addComponent(button);

            button = new CustomGuiButtonWrapper().setSize(22, 20);
            button.setTextureRect(rect);
            button.setTextureHoverOffset(22).setPos(buttonId * 22 + 4, 0).setID(buttonId++);
            button.setDisplayItem(NpcAPI.Instance().getIItemStack(new ItemStack(Items.REDSTONE)));
            button.setOnPress((gui, b) -> player.showCustomGui(new DisplayMenu(npc, player).gui));
            gui.addComponent(button);

            IButton b = (IButton) gui.getComponent(active);
            b.setEnabled(false);
            b.setPos(b.getPosX(), 3);
        }
    }

    public static void open(Player player, EntityCustomNpc npc){
        IPlayer p = (IPlayer)NpcAPI.Instance().getIEntity(player);
        DisplayMenu menu = new DisplayMenu(npc, p);
        p.showCustomGui(menu.gui);
    }
}
