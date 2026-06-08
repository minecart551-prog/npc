package noppes.npcs.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketGuiData;
import noppes.npcs.packets.client.PacketGuiOpen;
import org.jetbrains.annotations.Nullable;

public class ItemNbtBook extends Item{
	
	public ItemNbtBook() {
		super(new Item.Properties().stacksTo(1));
	}

	public void blockEvent(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		Packets.send((ServerPlayer) player, new PacketGuiOpen(EnumGuiType.NbtBook, hitResult.getBlockPos()));
		
		BlockState state = world.getBlockState(hitResult.getBlockPos());
		CompoundTag data = new CompoundTag();
		BlockEntity  tile = world.getBlockEntity(hitResult.getBlockPos());
		if(tile != null) {
			tile.saveWithFullMetadata();
		}
		
		CompoundTag compound = new CompoundTag();
		compound.put("Data", data);
		Packets.send((ServerPlayer)player, new PacketGuiData(compound));
	}

	public void entityEvent(Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		Packets.send((ServerPlayer)player, new PacketGuiOpen(EnumGuiType.NbtBook, BlockPos.ZERO));
		
		CompoundTag data = new CompoundTag();
		entity.saveAsPassenger(data);
		CompoundTag compound = new CompoundTag();
		compound.putInt("EntityId", entity.getId());
		compound.put("Data", data);
		Packets.send((ServerPlayer)player, new PacketGuiData(compound));
	}
}
