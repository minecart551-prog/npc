package noppes.npcs.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ai.selector.NPCInteractSelector;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class EntityAIWander extends Goal
{
    private EntityNPCInterface entity;
    public final NPCInteractSelector selector;
    private double x;
    private double y;
    private double zPosition;
    private EntityNPCInterface nearbyNPC;

    public EntityAIWander(EntityNPCInterface npc){
        this.entity = npc;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        selector = new NPCInteractSelector(npc);
    }

    @Override
    public boolean canUse(){
        if (this.entity.getNoActionTime() >= 100 || !entity.getNavigation().isDone() || entity.isInteracting() || entity.isPassenger() ||
        		this.entity.ais.movingPause && this.entity.getRandom().nextInt(80) != 0){
            return false;
        }
        if(entity.ais.npcInteracting && this.entity.getRandom().nextInt(this.entity.ais.movingPause?6:16) == 1)
        	nearbyNPC = getNearbyNPC();
        
        if(nearbyNPC != null){
            this.x = Mth.floor(nearbyNPC.getX());
            this.y = Mth.floor(nearbyNPC.getY());
            this.zPosition = Mth.floor(nearbyNPC.getZ());
    		nearbyNPC.addInteract(entity);
        }
        else{
        	Vec3 vec = getVec();
            if (vec == null){
                return false;
            }
            else{
                this.x = vec.x;
                this.y = vec.y;
                if(entity.ais.movementType == 1)
                    this.y = entity.getStartYPos() + entity.getRandom().nextFloat() * 0.75 * entity.ais.walkingRange;
                this.zPosition = vec.z;
            }
        }
        return true;
    }

    @Override
    public void tick() {
    	if(nearbyNPC != null){
    		nearbyNPC.getNavigation().stop();
    	}
    }
    private EntityNPCInterface getNearbyNPC() {
		List<Entity> list = entity.level().getEntities(entity, entity.getBoundingBox().inflate(entity.ais.walkingRange, entity.ais.walkingRange > 7?7:entity.ais.walkingRange, entity.ais.walkingRange), selector);
		Iterator<Entity> ita = list.iterator();
		while(ita.hasNext()){
			EntityNPCInterface npc = (EntityNPCInterface) ita.next();
			if(!npc.ais.stopAndInteract || npc.isAttacking() || !npc.isAlive() || entity.faction.isAggressiveToNpc(npc))
				ita.remove();
		}
		
		if(list.isEmpty())
			return null;
		
		return (EntityNPCInterface) list.get(entity.getRandom().nextInt(list.size()));
	}

	private Vec3 getVec(){
    	if(entity.ais.walkingRange > 0){
    		BlockPos start = new BlockPos((int) this.entity.getStartXPos(), (int) this.entity.getStartYPos(), (int) this.entity.getStartZPos());
            int distance = (int)Math.sqrt(this.entity.blockPosition().distSqr(start));
            int range = Math.min(this.entity.ais.walkingRange, CustomNpcs.NpcNavRange);
            if(range - distance < 4){
                Vec3 pos2 = new Vec3((entity.getX() + start.getX()) / 2, (entity.getY() + start.getY()) / 2, (entity.getZ() + start.getZ()) / 2);
                return DefaultRandomPos.getPosTowards(entity, range / 2, Math.min(range / 2, 7), pos2, Math.PI / 2F);
            }
            else{
                return DefaultRandomPos.getPos(this.entity, range / 2, Math.min(range / 2, 7));
            }
    	}
    	return DefaultRandomPos.getPos(this.entity, CustomNpcs.NpcNavRange, 7);
    }

    @Override
    public boolean canContinueToUse(){
    	if(nearbyNPC != null && (!selector.apply(nearbyNPC) || entity.isInRange(nearbyNPC, entity.getBbWidth())))
    		return false;
        return !this.entity.getNavigation().isDone() && this.entity.isAlive() && !entity.isInteracting();
    }

    @Override
    public void start(){
        this.entity.getNavigation().moveTo(this.entity.getNavigation().createPath(this.x, this.y, this.zPosition, 0), 1);
    }

    @Override
    public void stop() {
    	if(nearbyNPC != null && entity.isInRange(nearbyNPC, 3.5)){
    		EntityNPCInterface talk = entity;
    		if(entity.getRandom().nextBoolean())
    			talk = nearbyNPC;
    		Line line = talk.advanced.getNPCInteractLine();
    		if(line == null)
    			line = new Line(".........");
    		line.setShowText(false);
			talk.saySurrounding(line);
    		
    		entity.addInteract(nearbyNPC);
    		nearbyNPC.addInteract(entity);
    	}
    	nearbyNPC = null;
    }
}
