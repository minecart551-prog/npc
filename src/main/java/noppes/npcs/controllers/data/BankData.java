package noppes.npcs.controllers.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.containers.ContainerNPCBankInterface;
import noppes.npcs.controllers.BankController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.PacketGuiData;
import noppes.npcs.util.CustomNPCsScheduler;

import java.util.HashMap;

public class BankData {
	public HashMap<Integer,NpcMiscInventory> itemSlots;  
	public HashMap<Integer,Boolean> upgradedSlots;  
	public int unlockedSlots = 0;
	public int bankId = -1;
	
	public BankData(){
		itemSlots = new HashMap<Integer,NpcMiscInventory>();
		upgradedSlots = new HashMap<Integer,Boolean>();
		
		for(int i = 0; i < 6; i++){
			itemSlots.put(i, new NpcMiscInventory(54));
			upgradedSlots.put(i, false);
		}
	}
	public void readNBT(CompoundTag nbttagcompound) {
		bankId = nbttagcompound.getInt("DataBankId");
        unlockedSlots = nbttagcompound.getInt("UnlockedSlots");
        itemSlots = getItemSlots(nbttagcompound.getList("BankInv", 10));
        upgradedSlots = NBTTags.getBooleanList(nbttagcompound.getList("UpdatedSlots", 10));
	}
	private HashMap<Integer, NpcMiscInventory> getItemSlots(ListTag tagList) {
		HashMap<Integer,NpcMiscInventory> list = new HashMap<Integer,NpcMiscInventory>();
        for(int i = 0; i < tagList.size(); i++)
        {
            CompoundTag nbttagcompound = tagList.getCompound(i);
            int slot = nbttagcompound.getInt("Slot");
            NpcMiscInventory inv = new NpcMiscInventory(54);
            inv.setFromNBT(nbttagcompound.getCompound("BankItems"));
            list.put(slot, inv);
        }
		return list;
	}
	public void writeNBT(CompoundTag nbttagcompound) {
		nbttagcompound.putInt("DataBankId", bankId);
		nbttagcompound.putInt("UnlockedSlots", unlockedSlots);
		nbttagcompound.put("UpdatedSlots", NBTTags.nbtBooleanList(upgradedSlots));
		nbttagcompound.put("BankInv", nbtItemSlots(itemSlots));
	}
	private ListTag nbtItemSlots(HashMap<Integer, NpcMiscInventory> items) {
		ListTag list = new ListTag();
		for(int slot: items.keySet()){
			CompoundTag nbttagcompound = new CompoundTag();
			nbttagcompound.putInt("Slot", slot);
			
			nbttagcompound.put("BankItems",items.get(slot).getToNBT());
			list.add(nbttagcompound);
		}
		return list;
	}
	public boolean isUpgraded(Bank bank, int slot) {
		if(bank.isUpgraded(slot))
			return true;
		return bank.canBeUpgraded(slot) && upgradedSlots.get(slot);
	}
	
	public void openBankGui(final ServerPlayer player, EntityNPCInterface npc, int bankId, int slot) {
		final Bank bank = BankController.getInstance().getBank(bankId);
		
		if(bank.getMaxSlots() <= slot)
			return;
		
    	if(bank.startSlots > unlockedSlots)
    		unlockedSlots = bank.startSlots;
    	
    	ItemStack currency = ItemStack.EMPTY;
		if(unlockedSlots <= slot){
			currency = bank.currencyInventory.getItem(slot);
			NoppesUtilServer.openContainerGui(player, EnumGuiType.PlayerBankUnlock, (buf) -> {
				buf.writeInt(slot);
				buf.writeInt(bank.id);
			});
		}
		else if(isUpgraded(bank,slot)){
			NoppesUtilServer.openContainerGui(player, EnumGuiType.PlayerBankLarge, (buf) -> {
				buf.writeInt(slot);
				buf.writeInt(bank.id);
			});
		}
		else if(bank.canBeUpgraded(slot)){
			currency = bank.upgradeInventory.getItem(slot);
			NoppesUtilServer.openContainerGui(player, EnumGuiType.PlayerBankUprade, (buf) -> {
				buf.writeInt(slot);
				buf.writeInt(bank.id);
			});
		}
		else{
			NoppesUtilServer.openContainerGui(player, EnumGuiType.PlayerBankSmall, (buf) -> {
				buf.writeInt(slot);
				buf.writeInt(bank.id);
			});
		}
    	final ItemStack item = currency;
        CustomNPCsScheduler.runTack(() -> {
            CompoundTag compound = new CompoundTag();
            compound.putInt("MaxSlots", bank.getMaxSlots());
            compound.putInt("UnlockedSlots", unlockedSlots);
            if (item != null && !item.isEmpty()) {
                compound.put("Currency", item.save(new CompoundTag()));
                ContainerNPCBankInterface container = getContainer(player);
                if (container != null)
                    container.setCurrency(item);
            }
            Packets.send(player, new PacketGuiData(compound));
        }, 300);
	}
	private ContainerNPCBankInterface getContainer(Player player){
		AbstractContainerMenu con = player.containerMenu;
		if(con == null || !(con instanceof ContainerNPCBankInterface))
			return null;
		
		return (ContainerNPCBankInterface) con;
	}
}
