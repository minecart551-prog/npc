package noppes.npcs.packets.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import noppes.npcs.containers.ContainerManageRecipes;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.RecipeCarpentry;
import noppes.npcs.packets.PacketServerBasic;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketGuiData;

public class SPacketRecipeGet extends PacketServerBasic {
    private int recipe;

    public SPacketRecipeGet(int recipe) {
        this.recipe = recipe;
    }

    public static void encode(SPacketRecipeGet msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.recipe);
    }

    public static SPacketRecipeGet decode(FriendlyByteBuf buf) {
        return new SPacketRecipeGet(buf.readInt());
    }

    @Override
    protected void handle() {
        RecipeCarpentry r = RecipeController.instance.getRecipe(recipe);
        setRecipeGui(player,r);
    }

    public static void setRecipeGui(ServerPlayer player, RecipeCarpentry recipe){
        if(recipe == null)
            return;
        if(!(player.containerMenu instanceof ContainerManageRecipes))
            return;

        ContainerManageRecipes container = (ContainerManageRecipes) player.containerMenu;
        container.setRecipe(recipe,player.level().registryAccess());

        Packets.send(player, new PacketGuiData(recipe.writeNBT()));
    }
}