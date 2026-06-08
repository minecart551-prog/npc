package noppes.npcs.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomTabs;
import noppes.npcs.constants.EnumGuiType;

import java.util.List;


public class ItemTeleporter extends Item {
	
    public ItemTeleporter(){
        super(new Item.Properties().stacksTo(1));
    }

	@Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand){
    	ItemStack itemstack = player.getItemInHand(hand);
		if(!level.isClientSide)
	        return new InteractionResultHolder(InteractionResult.PASS, itemstack);
    	CustomNpcs.proxy.openGui(player, EnumGuiType.NpcDimensions);
        return new InteractionResultHolder(InteractionResult.PASS, itemstack);
    }

    public static boolean onEntitySwing(ItemStack stack, LivingEntity livingEntity){
    	if(livingEntity.level().isClientSide)
    		return true;
        float f = livingEntity.getXRot();
        float f1 = livingEntity.getYRot();
        Vec3 vector3d = livingEntity.getEyePosition(1.0F);
        float f2 = Mth.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f3 = Mth.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f4 = -Mth.cos(-f * ((float)Math.PI / 180F));
        float f5 = Mth.sin(-f * ((float)Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d0 = 80;
        Vec3 vector3d1 = vector3d.add((double)f6 * d0, (double)f5 * d0, (double)f7 * d0);

        HitResult movingobjectposition = livingEntity.level().clip(new ClipContext(vector3d, vector3d1, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, livingEntity));
        if (movingobjectposition == null)
            return true;
        
        Vec3 vec32 = livingEntity.getViewVector(f);
        boolean flag = false;
        float f9 = 1.0F;
        List list = livingEntity.level().getEntities(livingEntity, livingEntity.getBoundingBox().inflate(vec32.x * d0, vec32.y * d0, vec32.z * d0).inflate((double)f9, (double)f9, (double)f9));

        for (int i = 0; i < list.size(); ++i){
            Entity entity = (Entity)list.get(i);

            if (entity.canBeCollidedWith()){
                float f10 = entity.getPickRadius();
                AABB axisalignedbb = entity.getBoundingBox().inflate((double)f10, (double)f10, (double)f10);

                if (axisalignedbb.contains(vector3d)){
                    flag = true;
                }
            }
        }

        if (flag)
            return true;
        
        if (movingobjectposition.getType() == HitResult.Type.BLOCK){
        	BlockPos pos = ((BlockHitResult)movingobjectposition).getBlockPos();
            
            while(livingEntity.level().getBlockState(pos).getBlock() != Blocks.AIR){
            	pos = pos.above();
            }
            livingEntity.teleportTo(pos.getX() + 0.5F, pos.getY() + 1.0F, pos.getZ() + 0.5F);
        }
        
    	return true;
    }
}
