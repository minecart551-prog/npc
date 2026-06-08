package noppes.npcs.api.wrapper;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlockScripted;
import noppes.npcs.api.block.ITextPlane;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.mixin.EntityIMixin;

public class BlockScriptedWrapper extends BlockWrapper implements IBlockScripted{
	private TileScripted tile;

	public BlockScriptedWrapper(Level level, Block block, BlockPos pos) {
		super(level, block, pos);
		tile = (TileScripted) super.tile;
	}
	
	@Override
	public void setModel(IItemStack item){
		if(item == null)
			tile.setItemModel(null, null);
		else {
			Item itemmc = item.getMCItemStack().getItem();
			tile.setItemModel(item.getMCItemStack(), itemmc instanceof BlockItem ? ((BlockItem) itemmc).getBlock() : Blocks.AIR);
		}
	}
	
	@Override
	public void setModel(String name){
		if(name == null)
			tile.setItemModel(null, null);
		else{
			ResourceLocation loc = new ResourceLocation(name);
			Block block = BuiltInRegistries.BLOCK.get(loc);
			tile.setItemModel(new ItemStack(BuiltInRegistries.ITEM.get(loc)), block);
		}
	}

	@Override
	public IItemStack getModel(){
		return NpcAPI.Instance().getIItemStack(tile.itemModel);
	}
	
	@Override
	public void setRedstonePower(int strength){
		tile.setRedstonePower(strength);
	}
	
	@Override
	public int getRedstonePower(){
		return tile.powering;
	}
	
	@Override
	public void setIsLadder(boolean bo){
		tile.isLadder = bo;
		tile.needsClientUpdate = true;
	}
	
	@Override
	public boolean getIsLadder(){
		return tile.isLadder;
	}
	
	@Override
	public void setIsPassible(boolean bo){
		tile.isPassible = bo;
		tile.needsClientUpdate = true;
	}
	
	@Override
	public boolean getIsPassible(){
		return tile.isPassible;
	}
	
	@Override
	public void setLight(int value){
		tile.setLightValue(value);
	}
	
	@Override
	public int getLight(){
		return tile.lightValue;
	}
	
	@Override
	public void setScale(float x, float y, float z){
		tile.setScale(x, y, z);
	}
	
	@Override
	public float getScaleX(){
		return tile.scaleX;
	}
	
	@Override
	public float getScaleY(){
		return tile.scaleY;
	}
	
	@Override
	public float getScaleZ(){
		return tile.scaleZ;
	}
	
	@Override
	public void setRotation(int x, int y, int z){
		tile.setRotation(x % 360, y  % 360, z  % 360);
	}
	
	@Override
	public int getRotationX(){
		return tile.rotationX;
	}
	
	@Override
	public int getRotationY(){
		return tile.rotationY;
	}
	
	@Override
	public int getRotationZ(){
		return tile.rotationZ;
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
	public String executeCommand(String command){
		if(!tile.getLevel().getServer().isCommandBlockEnabled())
			throw new CustomNPCsException("Command blocks need to be enabled to executeCommands");
		FakePlayer player = EntityNPCInterface.CommandPlayer;
		((EntityIMixin)player).setLevel((ServerLevel) tile.getLevel());
		player.setPos(getX(), getY(), getZ());
		return NoppesUtilServer.runCommand(tile.getLevel(), tile.getBlockPos(), "ScriptBlock: " + tile.getBlockPos(), command, null, player);
	}

	@Override
	public ITextPlane getTextPlane() {
		return tile.text1;
	}

	@Override
	public ITextPlane getTextPlane2() {
		return tile.text2;
	}

	@Override
	public ITextPlane getTextPlane3() {
		return tile.text3;
	}

	@Override
	public ITextPlane getTextPlane4() {
		return tile.text4;
	}

	@Override
	public ITextPlane getTextPlane5() {
		return tile.text5;
	}

	@Override
	public ITextPlane getTextPlane6() {
		return tile.text6;
	}

	@Override
	public ITimers getTimers() {
		return tile.timers;
	}

	@Override
	protected void setTile(BlockEntity tile){
		this.tile = (TileScripted) tile;
		super.setTile(tile);
	}

	@Override
	public void trigger(int id, Object... arguments) {
		EventHooks.onScriptTriggerEvent(tile, id, level, getPos(), null, arguments);
	}
}
