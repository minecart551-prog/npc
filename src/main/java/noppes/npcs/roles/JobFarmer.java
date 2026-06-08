package noppes.npcs.roles;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.data.role.IJobFarmer;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.MassBlockController;
import noppes.npcs.controllers.MassBlockController.IMassBlock;
import noppes.npcs.controllers.data.BlockData;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class JobFarmer extends JobInterface implements IMassBlock, IJobFarmer{
	
	public int chestMode = 1; //0:nothing, 1:bring to chest, 2:drop
	
	private List<BlockPos> trackedBlocks = new ArrayList<BlockPos>();
	
	private int ticks = 0;
	private int walkTicks = 0;
	private int blockTicks = 800;
	private boolean waitingForBlocks = false;
	
	private BlockPos ripe = null;
	private BlockPos chest = null;
	
	private ItemStack holding = ItemStack.EMPTY;

	public JobFarmer(EntityNPCInterface npc) {
		super(npc);
		overrideMainHand = true;
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
	public CompoundTag save(CompoundTag compound) {
		compound.putInt("JobChestMode", chestMode);
		if(!holding.isEmpty()){
			compound.put("JobHolding", holding.save(new CompoundTag()));
		}
		return compound;
	}

	@Override
	public void load(CompoundTag compound) {
		chestMode = compound.getInt("JobChestMode");
		
		holding = ItemStack.of(compound.getCompound("JobHolding"));
		
		blockTicks = 1100;
	}
	
	public void setHolding(ItemStack item){
		holding = item;
		npc.setJobData(itemToString(holding));	
			
	}

	@Override
	public boolean aiShouldExecute() {
		if(!holding.isEmpty()){
			if(chestMode == 0)
				setHolding(ItemStack.EMPTY);
			else if(chestMode == 1){
				if(chest == null){
					dropItem(holding);
					setHolding(ItemStack.EMPTY);
				}
				else
					chest();
			}
			else if(chestMode == 2){
				dropItem(holding);
				setHolding(ItemStack.EMPTY);
			}
			return false;
		}
		if(ripe != null){
			pluck();
			return false;
		}
		if(!waitingForBlocks && blockTicks++ > 1200){
			blockTicks = 0;
			waitingForBlocks = true;
			MassBlockController.Queue(this);
		}
		if(ticks++ < 100)
			return false;
		ticks = 0;
		return true;
	}
	
	private void dropItem(ItemStack item){
        ItemEntity entityitem = new ItemEntity(npc.level(), npc.getX(), npc.getY(), npc.getZ(), item);
        entityitem.setDefaultPickUpDelay();
        npc.level().addFreshEntity(entityitem);
	}

	private void chest() {
		BlockPos pos = chest;
		npc.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1);
		npc.getLookControl().setLookAt(pos.getX(), pos.getY(), pos.getZ(), 10, npc.getMaxHeadXRot());
		if(npc.nearPosition(pos) || walkTicks++ > 400){
			if(walkTicks < 400){
				npc.swing(InteractionHand.MAIN_HAND);
			}
			npc.getNavigation().stop();
			ticks = 100;
			walkTicks = 0;
			BlockState state = npc.level().getBlockState(pos);
			BlockEntity  tile = npc.level().getBlockEntity(pos);
			Container inventory = tile instanceof Container? (Container) tile : null;
			if(state.getBlock() instanceof ChestBlock){
				inventory = ChestBlock.getContainer((ChestBlock)state.getBlock(), state, npc.level(), pos, true);
			}
			if(inventory != null){
				for(int i = 0; !holding.isEmpty() && i < inventory.getContainerSize(); i++){
					holding = mergeStack(inventory, i, holding);
				}
				for(int i = 0; !holding.isEmpty() && i < inventory.getContainerSize(); i++){
					ItemStack item = inventory.getItem(i);
					if(item.isEmpty()){
						inventory.setItem(i, holding);
						holding = ItemStack.EMPTY;
					}
				}
				if(!holding.isEmpty()){//chest is full so drop the item
					dropItem(holding);
					holding = ItemStack.EMPTY;
				}
			}
			else{
				chest = null;
			}
			setHolding(holding);
		}
	}
	
	private ItemStack mergeStack(Container inventory, int slot, ItemStack item){
		ItemStack item2 = inventory.getItem(slot);
		if(!NoppesUtilPlayer.compareItems(item, item2, false, false))
			return item;
		int size = item2.getMaxStackSize() - item2.getCount();
		if(size >= item.getCount()){
			item2.setCount(item2.getCount() + item.getCount());
			return ItemStack.EMPTY;
		}
		item2.setCount(item2.getMaxStackSize());
		item.setCount(item.getCount() - size);
		if(item.isEmpty())
			return ItemStack.EMPTY;
		return item;
	}

	private void pluck() {
		BlockPos pos = ripe;
		npc.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1);
		npc.getLookControl().setLookAt(pos.getX(), pos.getY(), pos.getZ(), 10, npc.getMaxHeadXRot());
		if(npc.nearPosition(pos) || walkTicks++ > 400){

			if(walkTicks > 400){
				pos = NoppesUtilServer.GetClosePos(pos, npc.level());
				npc.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			}
			ripe = null;
			npc.getNavigation().stop();
			ticks = 90;
			walkTicks = 0;
			npc.swing(InteractionHand.MAIN_HAND);
			BlockState state = npc.level().getBlockState(pos);
			Block b = state.getBlock();
			if(b instanceof CropBlock && ((CropBlock)b).isMaxAge(state)){
				CropBlock crop = (CropBlock) b;
				Item item = crop.getCloneItemStack(npc.level(), pos, state).getItem();

				LootParams.Builder builder = (new LootParams.Builder((ServerLevel) npc.level())).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).withParameter(LootContextParams.TOOL, npc.getMainHandItem()).withParameter(LootContextParams.BLOCK_STATE, state).withOptionalParameter(LootContextParams.BLOCK_ENTITY, npc.level().getBlockEntity(pos));
				LootTable loottable = npc.getServer().getLootData().getLootTable(b.getLootTable());
				List<ItemStack> l = loottable.getRandomItems(builder.create(LootContextParamSets.BLOCK));

				npc.level().setBlock(pos, crop.getStateForAge(0), 2);

				if(l.isEmpty()){
					holding = ItemStack.EMPTY;
				}
				else if(l.size() == 1){
					holding = l.get(0);
				}
				else{
					List<ItemStack> fl = l.stream().filter(t -> t.getItem() != item).collect(Collectors.toList());
					if(fl.isEmpty()){
						fl = l;
					}
					holding = fl.get(npc.getRandom().nextInt(fl.size()));
				}

				holding.setCount(1);
			}
			if(b instanceof StemGrownBlock){
				b = npc.level().getBlockState(pos).getBlock();
				npc.level().removeBlock(pos, false);
				holding = new ItemStack(b);
			}
			setHolding(holding);
		}
	}

	@Override
	public boolean aiContinueExecute() {
		return false;
	}

	@Override
	public void aiUpdateTask() {
		Iterator<BlockPos> ite = trackedBlocks.iterator();
		while(ite.hasNext() && ripe == null){
			BlockPos pos = ite.next();
			BlockState state = npc.level().getBlockState(pos);
			Block b = state.getBlock();
			if((b instanceof CropBlock && ((CropBlock)b).isMaxAge(state) || b instanceof StemGrownBlock) && b.getLootTable() != BuiltInLootTables.EMPTY){
				ripe = pos;
			}
			else{
				ite.remove();
			}
		}
		npc.ais.returnToStart = ripe == null;
		if(ripe != null){
			npc.getNavigation().stop();
			npc.getLookControl().setLookAt(ripe.getX(), ripe.getY(), ripe.getZ(), 10, npc.getMaxHeadXRot());
		}
	}

	@Override
	public boolean isPlucking(){
		return ripe != null || !holding.isEmpty();
	}

	@Override
	public EntityNPCInterface getNpc() {
		return npc;
	}

	@Override
	public int getRange() {
		return 16;
	}

	@Override
	public void processed(List<BlockData> list) {
		List<BlockPos> trackedBlocks = new ArrayList<BlockPos>();
		BlockPos chest = null;
		for(BlockData data : list){
			BlockEntity  tile = npc.level().getBlockEntity(data.pos);
			Block b = data.state.getBlock();
			if(tile instanceof RandomizableContainerBlockEntity){
				if(chest == null || npc.distanceToSqr(chest.getX(), chest.getY(), chest.getZ()) > npc.distanceToSqr(data.pos.getX(), data.pos.getY(), data.pos.getZ()))
					chest = data.pos;
				continue;
			}
			if(!(b instanceof CropBlock) && !(b instanceof StemBlock))
				continue;
			if(!trackedBlocks.contains(data.pos))
				trackedBlocks.add(data.pos);
		}
		this.chest = chest;
		this.trackedBlocks = trackedBlocks;
		waitingForBlocks = false;
	}

	@Override
	public EnumSet<Goal.Flag> getFlags() {
		return EnumSet.of(Goal.Flag.MOVE);
	}

	@Override
	public int getType() {
		return JobType.FARMER;
	}
}
