package noppes.npcs.blocks;

import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import noppes.npcs.CustomNpcs;

public abstract class BlockInterface extends BaseEntityBlock implements EntityBlock {

	protected BlockInterface(Block.Properties properties) {
		super(properties);
	}
}
