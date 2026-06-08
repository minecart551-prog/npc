package noppes.npcs.api.wrapper;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.block.IBlockScriptedDoor;
import noppes.npcs.blocks.tiles.TileScriptedDoor;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.mixin.EntityIMixin;

public class BlockScriptedDoorWrapper extends BlockWrapper implements IBlockScriptedDoor{
	private TileScriptedDoor tile;

	public BlockScriptedDoorWrapper(Level level, Block block, BlockPos pos) {
		super(level, block, pos);
		tile = (TileScriptedDoor) super.tile;
	}

	@Override
	public boolean getOpen(){
		BlockState state = level.getMCLevel().getBlockState(pos);
		return state.getValue(DoorBlock.OPEN).equals(true);
	}

	@Override
	public void setOpen(boolean open){
		if(getOpen() == open || isRemoved())
			return;
		
		BlockState state = level.getMCLevel().getBlockState(pos);
		
		((DoorBlock)block).setOpen(null, level.getMCLevel(), state, pos, open);
	}
	
	@Override
	public void setBlockModel(String name){
		Block b = null;
		if(name != null){
			b = BuiltInRegistries.BLOCK.get(new ResourceLocation(name));
		}
		tile.setItemModel(b);
	}

	@Override
	public String getBlockModel(){
		return BuiltInRegistries.BLOCK.getKey(tile.blockModel) + "";
	}

	@Override
	public ITimers getTimers() {
		return tile.timers;
	}

	@Override
	public float getHardness() {
		return tile.blockHardness;
	}

	@Override
	public void setHardness(float hardness) {
		tile.blockHardness = hardness;
	}

	@Override
	public float getResistance() {
		return tile.blockResistance;
	}

	@Override
	public void setResistance(float resistance) {
		tile.blockResistance = resistance;		
	}

	@Override
	protected void setTile(BlockEntity tile){
		this.tile = (TileScriptedDoor) tile;
		super.setTile(tile);
	}

	@Override
	public String executeCommand(String command){
		if(!tile.getLevel().getServer().isCommandBlockEnabled())
			throw new CustomNPCsException("Command blocks need to be enabled to executeCommands");
		FakePlayer player = EntityNPCInterface.CommandPlayer;
		((EntityIMixin)player).setLevel((ServerLevel) tile.getLevel());
		player.setPos(getX(), getY(), getZ());
		return NoppesUtilServer.runCommand(tile.getLevel(), tile.getBlockPos(), "ScriptBlock: " + tile.getBlockPos(), command, null, player);
	}
}
