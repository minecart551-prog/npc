package noppes.npcs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import noppes.npcs.CustomBlocks;
import noppes.npcs.EventHooks;
import noppes.npcs.blocks.tiles.TileScripted;

import java.util.Collections;
import java.util.List;

/**
 * Survival friendly variant of the scripted block. Players can place and break
 * it like any normal block, but the scripting menu is only opened for players
 * with permission level 4 (the same bar used by the script save packets), so
 * regular players can never view or change the script. The script lives in the
 * block entity and travels with the item when the block is broken, so the
 * configuration survives being mined and placed again.
 */
public class BlockScriptedSurvival extends BlockScripted {

	public BlockScriptedSurvival() {
		// Very fast to break by hand and drops without requiring a pickaxe
		super(Block.Properties.of().mapColor(MapColor.STONE).strength(0.05F, 0.1F).sound(SoundType.STONE));
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray) {
		// Only admins get the scripting menu, everyone else just triggers the interact event
		if (player.hasPermissions(4))
			return super.use(state, level, pos, player, hand, ray);

		if (level.isClientSide)
			return InteractionResult.SUCCESS;
		Vec3 vec = ray.getLocation();
		float x = (float) (vec.x - (double) pos.getX());
		float y = (float) (vec.y - (double) pos.getY());
		float z = (float) (vec.z - (double) pos.getZ());
		TileScripted tile = (TileScripted) level.getBlockEntity(pos);
		return EventHooks.onScriptBlockInteract(tile, player, ray.getDirection().get3DDataValue(), x, y, z) ? InteractionResult.FAIL : InteractionResult.SUCCESS;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack item) {
		// Never open the scripting menu on placement, not even for admins
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
		ItemStack stack = new ItemStack(CustomBlocks.scripted_survival_item);
		BlockEntity tile = level.getBlockEntity(pos);
		if (tile instanceof TileScripted) {
			CompoundTag tag = ((TileScripted) tile).saveWithoutMetadata();
			if (!tag.isEmpty())
				stack.addTagElement("BlockEntityTag", tag);
		}
		return stack;
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
		ItemStack stack = new ItemStack(CustomBlocks.scripted_survival_item);
		// Always drop the item; carry the script along if the block entity is available
		BlockEntity tile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (tile instanceof TileScripted) {
			CompoundTag tag = ((TileScripted) tile).saveWithoutMetadata();
			if (!tag.isEmpty())
				stack.addTagElement("BlockEntityTag", tag);
		}
		return Collections.singletonList(stack);
	}
}
