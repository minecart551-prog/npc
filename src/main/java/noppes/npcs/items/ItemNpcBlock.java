package noppes.npcs.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class ItemNpcBlock extends BlockItem {

	public final Block block;
	
	public ItemNpcBlock(Block block, Properties builder) {
		super(block, builder);
		this.block = block;
	}
}
