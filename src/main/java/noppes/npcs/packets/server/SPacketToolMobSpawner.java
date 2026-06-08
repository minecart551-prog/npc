package noppes.npcs.packets.server;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NBTTags;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.mixin.BaseSpawnerMixin;
import noppes.npcs.packets.IPacketServer;
import noppes.npcs.packets.PacketServerBasic;

import java.util.Optional;

public class SPacketToolMobSpawner extends PacketServerBasic {
    private boolean createSpawner;
    private boolean server;
    private BlockPos pos;

    private String name = "";
    private int tab = -1;

    private CompoundTag clone = new CompoundTag();

    public SPacketToolMobSpawner(boolean createSpawner, BlockPos pos, String name, int tab) {
        this.server = true;
        this.createSpawner = createSpawner;
        this.pos = pos;
        this.name = name;
        this.tab = tab;
    }

    public SPacketToolMobSpawner(boolean createSpawner, BlockPos pos, CompoundTag clone) {
        this.server = false;
        this.createSpawner = createSpawner;
        this.pos = pos;
        this.clone = clone;
    }

    public SPacketToolMobSpawner(boolean createSpawner, boolean server, BlockPos pos, String name, int tab, CompoundTag clone) {
        this.createSpawner = createSpawner;
        this.server = server;
        this.pos = pos;
        this.name = name;
        this.tab = tab;
        this.clone = clone;
    }

    public SPacketToolMobSpawner(FriendlyByteBuf buf) {
        createSpawner = buf.readBoolean();
        server = buf.readBoolean();
        pos = buf.readBlockPos();
        name = buf.readUtf(32767);
        tab = buf.readInt();
        clone = buf.readNbt();
    }

    public static SPacketToolMobSpawner decode(FriendlyByteBuf buf) {
        return new SPacketToolMobSpawner(buf);
    }

    @Override
    public boolean toolAllowed(ItemStack item){
        return item.getItem() == CustomItems.cloner;
    }

    @Override
    public PermissionNode<Boolean> getPermission(){
        if(createSpawner){
            return CustomNpcsPermissions.SPAWNER_CREATE;
        }
        else{
            return CustomNpcsPermissions.SPAWNER_MOB;
        }
    }

    @Override
    protected void handle() {
        if(server)
            clone = ServerCloneController.Instance.getCloneData(player.createCommandSourceStack(), name, tab);
        if(clone == null || clone.isEmpty())
            return;
        if(createSpawner){
            createMobSpawner(pos, clone, player);
        }
        else{
            Entity entity = spawnClone(clone, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, player.level());
            if(entity == null){
                player.sendSystemMessage(Component.literal("Failed to create an entity out of your clone"));
            }
        }
    }

    public static Entity spawnClone(CompoundTag compound, double x, double y, double z, Level world) {
        ServerCloneController.Instance.cleanTags(compound);
        compound.put("Pos", NBTTags.nbtDoubleList(x, y, z));
        Entity entity = EntityType.create(compound, world).get();
        if(entity == null){
            return null;
        }
        if(entity instanceof EntityNPCInterface){
            EntityNPCInterface npc = (EntityNPCInterface) entity;
            npc.ais.setStartPos(npc.blockPosition());
        }
        world.addFreshEntity(entity);
        return entity;
    }

    public static void createMobSpawner(BlockPos pos, CompoundTag comp, Player player) {
        ServerCloneController.Instance.cleanTags(comp);

        if(comp.getString("id").equalsIgnoreCase("entityhorse")){
            player.sendSystemMessage(Component.literal("Currently you cant create horse spawner, its a minecraft bug"));
            return;
        }

        player.level().setBlockAndUpdate(pos, Blocks.SPAWNER.defaultBlockState()); //setBlock
        SpawnerBlockEntity tile = (SpawnerBlockEntity) player.level().getBlockEntity(pos);
        BaseSpawner logic = tile.getSpawner();

        if (!comp.contains("id", 8)){
            comp.putString("id", "Pig");
        }
        comp.putIntArray("StartPosNew", new int[]{pos.getX(), pos.getY(), pos.getZ()});
        ((BaseSpawnerMixin)logic).callSetNextSpawnData(player.level(), pos, new SpawnData(comp, Optional.empty()));
    }

    public static void encode(SPacketToolMobSpawner msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.createSpawner);
        buf.writeBoolean(msg.server);
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.name);
        buf.writeInt(msg.tab);
        buf.writeNbt(msg.clone);
    }
}