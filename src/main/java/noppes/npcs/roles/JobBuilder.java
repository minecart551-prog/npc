package noppes.npcs.roles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.data.role.IJobBuilder;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.blocks.tiles.TileBuilder;
import noppes.npcs.controllers.data.BlockData;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.Stack;




public class JobBuilder extends JobInterface implements IJobBuilder{
	public TileBuilder build = null;
	private BlockPos possibleBuildPos = null;
	private Stack<BlockData> placingList = null;
	private BlockData placing = null;

	private int tryTicks = 0;
	private int ticks = 0;
	
	public JobBuilder(EntityNPCInterface npc) {
		super(npc);
		overrideMainHand = true;
	}

	@Override
	public CompoundTag save(CompoundTag compound) {
		if(build != null){
			compound.putInt("BuildX", build.getBlockPos().getX());
			compound.putInt("BuildY", build.getBlockPos().getY());
			compound.putInt("BuildZ", build.getBlockPos().getZ());
			if(placingList != null && !placingList.isEmpty()){
				ListTag list = new ListTag();
				for(BlockData data : placingList){
					list.add(data.getNBT());
				}
				if(placing != null)
					list.add(placing.getNBT());
				compound.put("Placing", list);
			}
		}
		return compound;
	}

	@Override
	public void load(CompoundTag compound) {
		if(compound.contains("BuildX")){
			possibleBuildPos = new BlockPos(compound.getInt("BuildX"), compound.getInt("BuildY"), compound.getInt("BuildZ"));
		}
		if(possibleBuildPos != null && compound.contains("Placing")){
			Stack<BlockData> placing = new Stack<BlockData>();
			ListTag list = compound.getList("Placing", 10);
			for(int i = 0; i < list.size(); i++){
				BlockData data = BlockData.getData(list.getCompound(i));
				if(data != null)
					placing.add(data);
			}
			this.placingList = placing;
		}
		npc.ais.doorInteract = 1;
	}

	@Override
	public IItemStack getMainhand(){
		String name = npc.getJobData();
		ItemStack item = stringToItem(name);
		if(item.isEmpty())
			return npc.inventory.weapons.get(0);
		return NpcAPI.Instance().getIItemStack(item);
	}

	@Override
	public boolean aiShouldExecute() {
		if(possibleBuildPos != null){
			BlockEntity  tile = npc.level().getBlockEntity(possibleBuildPos);
			if(tile instanceof TileBuilder){
				build = (TileBuilder) tile;
			}
			else
				placingList.clear();
			possibleBuildPos = null;
		}
		return build != null;
	}

	@Override
	public void aiUpdateTask() {
		if(build.finished && placingList == null || !build.enabled || build.isRemoved()){
			build = null;
			npc.getNavigation().moveTo(npc.getStartXPos(), npc.getStartYPos(), npc.getStartZPos(), 1);
			return;
		}
		if(ticks++ < 10)
			return;
		ticks = 0;
		if((placingList == null || placingList.isEmpty()) && placing == null){
			placingList = build.getBlock();
            npc.setJobData("");
            return;
		}
		if(placing == null){
			placing = placingList.pop();
			if(placing.state.getBlock() == Blocks.STRUCTURE_VOID){
				placing = null;
				return;
			}
			tryTicks = 0;
			npc.setJobData(blockToString(placing));
			
		}
		npc.getNavigation().moveTo(placing.pos.getX(), placing.pos.getY() + 1, placing.pos.getZ(), 1);
		if(tryTicks++ > 40 || npc.nearPosition(placing.pos)){
			BlockPos blockPos = placing.pos;
			placeBlock();
			if(tryTicks > 40){
				blockPos = NoppesUtilServer.GetClosePos(blockPos, npc.level());
				npc.teleportTo(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
			}
		}
	}
	
	private String blockToString(BlockData data){
		if(data.state.getBlock() == Blocks.AIR)
            return BuiltInRegistries.ITEM.getKey(Items.IRON_PICKAXE).toString();
		return itemToString(data.getStack());
	}

	@Override
	public void stop() {
		reset();
	}

	@Override
	public void reset(){
		build = null;
		npc.setJobData("");
	}
	
	public void placeBlock(){
		if(placing == null)
			return;
		npc.getNavigation().stop();
		npc.swing(InteractionHand.MAIN_HAND);
		npc.level().setBlock(placing.pos, placing.state, 2);
    	if(placing.state.getBlock() instanceof EntityBlock && placing.tile != null){
    		BlockEntity  tile = npc.level().getBlockEntity(placing.pos);
    		if(tile != null){
    			try{
    				tile.load(placing.tile);
    			}
    			catch(Exception e){
    				
    			}
    		}
    	}
    	placing = null;
	}

	@Override
	public boolean isBuilding() {
		return build != null && build.enabled && !build.finished && build.started;
	}

	@Override
	public int getType() {
		return JobType.BUILDER;
	}
}
