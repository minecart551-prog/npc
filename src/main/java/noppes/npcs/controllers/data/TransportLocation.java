package noppes.npcs.controllers.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import noppes.npcs.api.entity.data.role.IRoleTransporter.ITransportLocation;


public class TransportLocation implements ITransportLocation{
	public int id = -1;
	public String name = "default name";
	public BlockPos pos;
	
	public int type = 0;
	public ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, Level.OVERWORLD.location());
	
	public TransportCategory category;
	
	public void readNBT(CompoundTag compound) {
		if(compound == null)
			return;
		id = compound.getInt("Id");
		pos = new BlockPos((int) compound.getDouble("PosX"), (int) compound.getDouble("PosY"), (int) compound.getDouble("PosZ"));
		type = compound.getInt("Type");
		dimension = ResourceKey.create(Registries.DIMENSION, (new ResourceLocation(compound.getString("DimensionType"))));
		name = compound.getString("Name");
	}

	public CompoundTag writeNBT() {
		CompoundTag compound = new CompoundTag();
		compound.putInt("Id", id);
		compound.putDouble("PosX", pos.getX());
		compound.putDouble("PosY", pos.getY());
		compound.putDouble("PosZ", pos.getZ());
		compound.putInt("Type", type);
		compound.putString("DimensionType", dimension.location().toString());
		compound.putString("Name", name);
		return compound;
	}
	
	@Override
	public int getId(){
		return id;
	}

	@Override
	public String getDimension(){
		return dimension.location().toString();
	}

	@Override
	public int getX(){
		return pos.getX();
	}

	@Override
	public int getY(){
		return pos.getY();
	}

	@Override
	public int getZ(){
		return pos.getZ();
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public int getType(){
		return type;
	}

	public boolean isDefault() {
		return type == 1;
	}
}
