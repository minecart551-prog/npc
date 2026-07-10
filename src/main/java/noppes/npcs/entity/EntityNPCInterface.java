package noppes.npcs.entity;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import noppes.npcs.*;
import noppes.npcs.ai.*;
import noppes.npcs.ai.selector.NPCAttackSelector;
import noppes.npcs.ai.target.EntityAIClearTarget;
import noppes.npcs.ai.target.EntityAIOwnerHurtByTarget;
import noppes.npcs.ai.target.EntityAIOwnerHurtTarget;
import noppes.npcs.ai.target.NpcNearestAttackableTargetGoal;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.AnimationType;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.constants.PotionEffectType;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IProjectile;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.api.wrapper.NPCWrapper;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.client.ISynchedEntityData;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.LinkedNpcController;
import noppes.npcs.controllers.LinkedNpcController.LinkedData;
import noppes.npcs.controllers.VisibilityController;
import noppes.npcs.controllers.data.*;
import noppes.npcs.entity.data.*;
import noppes.npcs.items.ItemSoulstoneFilled;
import noppes.npcs.mixin.EntityIMixin;
import noppes.npcs.mixin.GoalSelectorMixin;
import noppes.npcs.packets.Packets;
import noppes.npcs.packets.client.*;
import noppes.npcs.roles.*;
import noppes.npcs.util.GameProfileAlt;
import java.util.*;

public abstract class EntityNPCInterface extends PathfinderMob implements RangedAttackMob {
    public static final EntityDataAccessor<Boolean> Attacking = SynchedEntityData.defineId(EntityNPCInterface.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Integer> Animation = SynchedEntityData.defineId(EntityNPCInterface.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> RoleData = SynchedEntityData.defineId(EntityNPCInterface.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> JobData = SynchedEntityData.defineId(EntityNPCInterface.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> FactionData = SynchedEntityData.defineId(EntityNPCInterface.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> Walking = SynchedEntityData.defineId(EntityNPCInterface.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> Interacting = SynchedEntityData.defineId(EntityNPCInterface.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IsDead = SynchedEntityData.defineId(EntityNPCInterface.class, EntityDataSerializers.BOOLEAN);

    public static final GameProfileAlt CommandProfile = new GameProfileAlt();
    public static final GameProfileAlt ChatEventProfile = new GameProfileAlt();
    public static final GameProfileAlt GenericProfile = new GameProfileAlt();
	public static FakePlayer ChatEventPlayer;
	public static FakePlayer CommandPlayer;
	public static FakePlayer GenericPlayer;
	
	public ICustomNpc wrappedNPC;

	public final DataAbilities abilities = new DataAbilities(this);
	public DataDisplay display = new DataDisplay(this);
	public DataStats stats = new DataStats(this);
	public DataInventory inventory = new DataInventory(this);
	public final DataAI ais = new DataAI(this);
	public final DataAdvanced advanced = new DataAdvanced(this);
	public final DataScript script = new DataScript(this);
	public final DataTransform transform = new DataTransform(this);
	public final DataTimers timers = new DataTimers(this);
	
	public CombatHandler combatHandler = new CombatHandler(this);
	
	public String linkedName = "";
	public long linkedLast = 0;
	public LinkedData linkedData;

	public EntityDimensions baseSize = new EntityDimensions(0.6f, 1.8f, false);
	private static final EntityDimensions sizeSleep = new EntityDimensions(0.8f, 0.4f, false);


	public float scaleX, scaleY, scaleZ;
	private boolean wasKilled = false;
	
	public RoleInterface role = RoleInterface.NONE;
	public JobInterface job = JobInterface.NONE;
	public HashMap<Integer, DialogOption> dialogs;
	public boolean hasDied = false;
	public long killedtime = 0;
	public long totalTicksAlive = 0;
	private int taskCount = 1;
	public int lastInteract = 0;
	public Faction faction; //should only be used server side
	
	private EntityAIRangedAttack aiRange;
	private Goal aiAttackTarget;
	public EntityAILook lookAi;
	public EntityAIAnimation animateAi;
		
	public List<LivingEntity> interactingEntities = new ArrayList<LivingEntity>();

	public ResourceLocation textureLocation = null;
	public ResourceLocation textureGlowLocation = null;
	public ResourceLocation textureCloakLocation = null;
	
	public int currentAnimation = AnimationType.NONE;
	public int animationStart = 0;
	
	public int npcVersion = VersionCompatibility.ModRev;
	public IChatMessages messages;
	
	public boolean updateClient = false;
	public boolean updateAI = false;

	public final ServerBossEvent bossInfo;
	public final HashSet<Integer> tracking = new HashSet<Integer>();

	public EntityNPCInterface(EntityType<? extends PathfinderMob> type, Level world) {
		super(type, world);
		if(!isClientSide())
			wrappedNPC = new NPCWrapper(this);
		registerBaseAttributes();
		dialogs = new HashMap<Integer, DialogOption>();
		if(!CustomNpcs.DefaultInteractLine.isEmpty())
			advanced.interactLines.lines.put(0, new Line(CustomNpcs.DefaultInteractLine));

		xpReward = 0;
		scaleX = scaleY = scaleZ = 0.9375f;
        
		faction = getFaction();
		setFaction(faction.id);
		updateAI = true;
		bossInfo = new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
		bossInfo.setVisible(false);
	}

	@Override
    public boolean canBreatheUnderwater(){
        return ais.movementType == 2;
    }

	@Override
    public boolean isPushedByFluid(){
        return ais.movementType != 2;
    }

	public LivingEntity getControllingPassenger() {
		return (this.getPassengers().isEmpty() || !(this.getPassengers().get(0) instanceof LivingEntity) || !this.ais.mountControl) ? null : (LivingEntity) this.getPassengers().get(0);
	}

    private void registerBaseAttributes(){
		this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(stats.maxHealth);
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(CustomNpcs.NpcNavRange);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.getSpeed());
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(stats.melee.getStrength());
        this.getAttribute(Attributes.FLYING_SPEED).setBaseValue(this.getSpeed() * 2);
    }



	public static AttributeSupplier.Builder createMobAttributes() {
		return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_DAMAGE).add(Attributes.FLYING_SPEED).add(Attributes.FOLLOW_RANGE);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
        this.entityData.define(RoleData, String.valueOf(""));
        this.entityData.define(JobData, String.valueOf(""));
        this.entityData.define(FactionData, 0);
        this.entityData.define(Animation, Integer.valueOf(0));

        this.entityData.define(Walking, false);
        this.entityData.define(Interacting, false);
        this.entityData.define(IsDead, false);
        this.entityData.define(Attacking, false);
	}

    @Override
    public boolean isAlive(){
    	return super.isAlive() && !isKilled();
    }
    
	@Override
	public void tick(){
		super.tick();
		if(tickCount % 10 == 0){
			this.startYPos = calculateStartYPos(ais.startPos()) + 1;
			if(startYPos < level().getMinBuildHeight() && !isClientSide()) {
				discard();
			}
			EventHooks.onNPCTick(this);
		}
		timers.update();
		
		if(level().isClientSide) {
			if(wasKilled != isKilled() && wasKilled){
				this.deathTime = 0;
				refreshDimensions();
			}
		}
		wasKilled = isKilled();


        if(currentAnimation == AnimationType.DEATH){
    		deathTime = 19;
        }
	}

    @Override
    public boolean doHurtTarget(Entity par1Entity){
        //float f = (float)this.getAttribute(Attributes.attackDamage).getValue();
    	float f = stats.melee.getStrength();
    	if (stats.melee.getDelay() < 10){
        	par1Entity.invulnerableTime = 0;
        }
    	if(par1Entity instanceof LivingEntity){
    		NpcEvent.MeleeAttackEvent event = new NpcEvent.MeleeAttackEvent(wrappedNPC, (LivingEntity)par1Entity, f);
    		if(EventHooks.onNPCAttacksMelee(this, event))
    			return false;
			f = event.damage;
    	}
    	Holder<DamageType> damageTypeHolder = level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(NpcDamageSource.NPC);
        boolean var4 = par1Entity.hurt(new DamageSource(damageTypeHolder, this),f);

        if (var4){
        	if(getOwner() instanceof Player)
        		EntityUtil.setRecentlyHit((LivingEntity)par1Entity);
            if (stats.melee.getKnockback() > 0){
                par1Entity.push((-Mth.sin(this.getYRot() * (float)Math.PI / 180.0F) * stats.melee.getKnockback() * 0.5F), 0.1D, (Mth.cos(this.getYRot() * (float)Math.PI / 180.0F) * stats.melee.getKnockback() * 0.5F));
				
                setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1, 0.6));
            }
            if(role.getType() == RoleType.COMPANION){
            	((RoleCompanion)role).attackedEntity(par1Entity);
            }
        }

        if (stats.melee.getEffectType() != PotionEffectType.NONE){
        	if (stats.melee.getEffectType() != PotionEffectType.FIRE)
        		((LivingEntity)par1Entity).addEffect(new MobEffectInstance(PotionEffectType.getMCType(stats.melee.getEffectType()), stats.melee.getEffectTime() * 20, stats.melee.getEffectStrength()));
        	else
        		par1Entity.setRemainingFireTicks(stats.melee.getEffectTime() * 20);
        }
        return var4;
    }

    @Override
    public void aiStep(){
    	if(CustomNpcs.FreezeNPCs)
    		return;
    	if(this.isNoAi()) {
            super.aiStep();
            return;
    	}
    	totalTicksAlive++;
        this.updateSwingTime();
        if(this.tickCount % 20 == 0)
			faction = getFaction();
		if(!level().isClientSide){
	    	if(!isKilled() && this.tickCount % 20 == 0){
				advanced.scenes.update();
	    		if(this.getHealth() < this.getMaxHealth()){
	    			if(stats.healthRegen > 0 && !isAttacking())
	    				heal(stats.healthRegen);
	    			if(stats.combatRegen > 0 && isAttacking())
	    				heal(stats.combatRegen);
	    		}
	    		if(faction.getsAttacked && !isAttacking()){
	    			List<Monster> list = this.level().getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(16, 16, 16));
	    			for(Monster mob : list){
	    				if(mob.getTarget() == null && this.canNpcSee(mob)){
	    					mob.setTarget(this);
	    				}
	    			}
	    		}
	    		if(linkedData != null && linkedData.time > linkedLast){
	    			LinkedNpcController.Instance.loadNpcData(this);
	    		}
	    		if(updateClient){
	    			updateClient();
	    		}
	    		if(updateAI){
	    			updateTasks();
	    			updateAI = false;
	    		}
	    	}
			if(getHealth() <= 0 && !isKilled()){
				removeAllEffects();
				entityData.set(IsDead, true);
    			updateTasks();
    			refreshDimensions();
			}
			if(display.getBossbar() == 2)
				bossInfo.setVisible(this.getTarget() != null);
			entityData.set(Walking, !getNavigation().isDone());
			entityData.set(Interacting, isInteracting());
			
			combatHandler.update();
			onCollide();
		}
		
		if(wasKilled != isKilled() && wasKilled){
			reset();
		}
		
		if (this.level().isDay() && !this.level().isClientSide && this.stats.burnInSun){
            float f = this.getLightLevelDependentMagicValue();

            if (f > 0.5F && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.level().canSeeSky(this.blockPosition())){
                this.setRemainingFireTicks(8 * 20);
            }
        }
		
        super.aiStep();
        
        if (level().isClientSide){
			role.clientUpdate();
        	
        	if(textureCloakLocation != null)
        		cloakUpdate();
			if(currentAnimation != entityData.get(Animation)){
				currentAnimation = entityData.get(Animation);
				animationStart = this.tickCount;
				refreshDimensions();
			}
			if(job.getType() == JobType.BARD)
				((JobBard)job).aiStep();
			
        }
        
        if(display.getBossbar() > 0)
            this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
    }
    
    public void updateClient() {
		Packets.sendNearby(this, new PacketNpcUpdate(getId(), writeSpawnData()));
		updateClient = false;
    }

	@Override //processInteract
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if(level().isClientSide) {
			return isAttacking() ? InteractionResult.FAIL : InteractionResult.PASS;
		}

		if(hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}

		ItemStack stack = player.getItemInHand(hand);
		if (stack != null) {
			Item item = stack.getItem();
			if (item == CustomItems.cloner || item == CustomItems.wand || item == CustomItems.mount || item == CustomItems.scripter) {
				setTarget(null);
				setLastHurtByMob(null);
				return InteractionResult.SUCCESS;
			}
			if (item == CustomItems.moving) {
				setTarget(null);
				stack.addTagElement("NPCID", IntTag.valueOf(getId()));
				player.sendSystemMessage(Component.translatable("message.pather.register", this.getName()));
				return InteractionResult.SUCCESS;
			}
		}
		if(EventHooks.onNPCInteract(this, player))
			return InteractionResult.FAIL;

		if(getFaction().isAggressiveToPlayer(player) || isAttacking()) {
			return InteractionResult.FAIL;
		}

		addInteract(player);

		Dialog dialog = getDialog(player);
		QuestData data = PlayerData.get(player).questData.getQuestCompletion(player, this);
		if (data != null){
			Packets.send((ServerPlayer)player, new PacketQuestCompletion(data.quest.id));
		}
		else if (dialog != null){
			NoppesUtilServer.openDialog(player, this, dialog);
		}
		else if(role.getType() != RoleType.NONE)
			role.interact(player);
		else
			say(player, advanced.getInteractLine());

		return InteractionResult.PASS;
	}
	
	public void addInteract(LivingEntity entity){
		if( !ais.stopAndInteract || isAttacking() || !entity.isAlive() || this.isNoAi())
			return;
		if((tickCount - lastInteract)  < 180)
			interactingEntities.clear();
		getNavigation().stop();
		lastInteract = tickCount;
		if(!interactingEntities.contains(entity))
			interactingEntities.add(entity);
	}
	
	public boolean isInteracting(){
		if((tickCount - lastInteract) < 40 || isClientSide() && entityData.get(Interacting))
			return true;
		return ais.stopAndInteract && !interactingEntities.isEmpty() && (tickCount - lastInteract)  < 180;
	}

	private Dialog getDialog(Player player) {
		for (DialogOption option : dialogs.values()) {
			if (option == null)
				continue;
			if (!option.hasDialog())
				continue;
			Dialog dialog = option.getDialog();
			if (dialog.availability.isAvailable(player)){
				return dialog;
			}
		}
		return null;
	}

	@Override
	public boolean hurt(DamageSource damagesource, float i) {
        if (this.level().isClientSide || CustomNpcs.FreezeNPCs || damagesource.getMsgId().equals("inWall")){
            return false;
        }
        if(damagesource.getMsgId().equals("outOfLevel") && isKilled()){
        	reset();
        }
        i = stats.resistances.applyResistance(damagesource, i);
        
        if((float)this.invulnerableTime > (float)this.invulnerableDuration / 2.0F && i <= this.lastHurt)
        	return false;
        
		Entity entity = NoppesUtilServer.GetDamageSourcee(damagesource);
		LivingEntity attackingEntity = null;
		
		if (entity instanceof LivingEntity)
			attackingEntity = (LivingEntity) entity;
		
		if(attackingEntity != null && attackingEntity == getOwner())
			return false;
		else if (attackingEntity instanceof EntityNPCInterface){
			EntityNPCInterface npc = (EntityNPCInterface) attackingEntity;
			if(npc.faction.id == faction.id)
				return false;
			if(npc.getOwner() instanceof Player)
				this.hurtTime = 100;
		}
		else if (attackingEntity instanceof Player && faction.isFriendlyToPlayer((Player) attackingEntity)) {
			//net.minecraftforge.common.ForgeHooks.onLivingAttack(this, damagesource, i);
			return false;
		}
		
		NpcEvent.DamagedEvent event = new NpcEvent.DamagedEvent(wrappedNPC, entity, i, damagesource);
		if(EventHooks.onNPCDamaged(this, event)) {
			//net.minecraftforge.common.ForgeHooks.onLivingAttack(this, damagesource, i);
			return false;
		}
		i = event.damage;

		if(isKilled())
			return false;
		
		if(attackingEntity == null)
			return super.hurt(damagesource, i);
		
		try{
			if (isAttacking()){
				if(getTarget() != null && this.distanceToSqr(getTarget()) > this.distanceToSqr(attackingEntity)){
					setTarget(attackingEntity);
				}
				return super.hurt(damagesource, i);
			}
			
			if (i > 0) {
				List<EntityNPCInterface> inRange = level().getEntitiesOfClass(EntityNPCInterface.class, this.getBoundingBox().inflate(32D, 16D, 32D));
				for (EntityNPCInterface npc : inRange) {
					if (npc.isKilled() || !npc.advanced.defendFaction || npc.faction.id != faction.id)
						continue;
					
					if (npc.canNpcSee(this) || npc.ais.directLOS || npc.canNpcSee(attackingEntity))
						npc.onAttack(attackingEntity);
				}
				setTarget(attackingEntity);
			}
			return super.hurt(damagesource, i);
		}
		finally{
			if(event.clearTarget){
				setTarget(null);
				setLastHurtByMob(null);
			}
		}
	}

	@Override
	protected void actuallyHurt(DamageSource damageSrc, float damageAmount){
		super.actuallyHurt(damageSrc, damageAmount);
		combatHandler.damage(damageSrc, damageAmount);
	}

	public void onAttack(LivingEntity entity) {
		if (entity == null || entity == this || isAttacking() || ais.onAttack == 3 || entity == getOwner())
			return;
		super.setTarget(entity);
	}
	
	@Override
    public void setTarget(LivingEntity entity){
    	if(entity instanceof Player && ((Player)entity).getAbilities().invulnerable ||
    			entity != null && entity == getOwner() || getTarget() == entity)
    		return;
    	if(entity != null){
    		NpcEvent.TargetEvent event = new NpcEvent.TargetEvent(wrappedNPC, entity);
    		if(EventHooks.onNPCTarget(this, event))
    			return;
    		if(event.entity == null)
    			entity = null;
    		else
    			entity = event.entity.getMCEntity();
    	}
    	else{
    		for(WrappedGoal en : targetSelector.getAvailableGoals()){
    			en.stop();
    		}
    		if(EventHooks.onNPCTargetLost(this, getTarget()))
    			return;
    	}
    	
		if (entity != null && entity != this && ais.onAttack != 3 && !isAttacking() && !isClientSide()){
			Line line = advanced.getAttackLine();
			if(line != null)
				saySurrounding(Line.formatTarget(line, entity));
		}
		
		super.setTarget(entity);
    }

	@Override
	public void performRangedAttack(LivingEntity entity, float f) {
        final ItemStack proj = ItemStackWrapper.MCItem(inventory.getProjectile());
        if(proj == null){
    		updateAI = true;
        	return;
        }
        
        NpcEvent.RangedLaunchedEvent event = new NpcEvent.RangedLaunchedEvent(wrappedNPC, entity, stats.ranged.getStrength());        
		for(int i = 0; i < this.stats.ranged.getShotCount(); i++){
			EntityProjectile projectile = shoot(entity, stats.ranged.getAccuracy(), proj, f == 1);
			projectile.damage = event.damage;
            projectile.callback = (projectile1, pos, entity1) -> {
                if (proj.getItem() == CustomItems.soulstoneFull) {
                    Entity e = ItemSoulstoneFilled.Spawn(null, proj, EntityNPCInterface.this.level(), pos);
                    if (e instanceof LivingEntity && entity1 instanceof LivingEntity) {
                        if (e instanceof Mob)
                            ((Mob) e).setTarget((LivingEntity) entity1);
                        else
                            ((LivingEntity) e).setLastHurtByMob((LivingEntity) entity1);
                    }
                }
				SoundEvent sound = stats.ranged.getSoundEvent(entity1 != null ? 1 : 2);
				if(sound!=null) {
					projectile1.playSound(sound, 1.0F, 1.2F / (getRandom().nextFloat() * 0.2F + 0.9F));
				}
                return false;
            };
			SoundEvent sound = this.stats.ranged.getSoundEvent(0);
			if(sound!=null) {
				this.playSound(sound, 2.0F, 1.0f);
			}
			event.projectiles.add((IProjectile) NpcAPI.Instance().getIEntity(projectile));
		}
        EventHooks.onNPCRangedLaunched(this, event);
    }
	
	public EntityProjectile shoot(LivingEntity entity, int accuracy, ItemStack proj, boolean indirect){
		return shoot(entity.getX(), entity.getBoundingBox().minY + (double)(entity.getBbHeight() / 2.0F), entity.getZ(), accuracy, proj, indirect);
	}
	
	public EntityProjectile shoot(double x, double y, double z, int accuracy, ItemStack proj, boolean indirect){
        EntityProjectile projectile = new EntityProjectile(this.level(), this, proj.copy(), true);
        double varX = x - this.getX();
		double varY = y - (this.getY() + this.getEyeHeight());
		double varZ = z - this.getZ();
		float varF = projectile.hasGravity() ? (float)Math.sqrt(varX * varX + varZ * varZ) : 0.0F;
		float angle = projectile.getAngleForXYZ(varX, varY, varZ, varF, indirect);
		float acc = 20.0F - Mth.floor(accuracy / 5.0F);
        projectile.shoot(varX, varY, varZ, angle, acc);
        level().addFreshEntity(projectile);
        return projectile;
	}
	
	private void clearTasks(GoalSelector tasks){
        //Iterator iterator = tasks.availableGoals.iterator();
        List<WrappedGoal> list = new ArrayList(tasks.getAvailableGoals());
        for (WrappedGoal entityaitaskentry : list)
        {
            tasks.removeGoal(entityaitaskentry);
        }
		tasks.getAvailableGoals().clear();
		((GoalSelectorMixin)tasks).lockedFlags().clear();
		((GoalSelectorMixin)tasks).disabledFlags().clear();
	}
	
	private void updateTasks() {
		if (level() == null || level().isClientSide || !(level() instanceof ServerLevel))
			return;
		ServerLevel sLevel = (ServerLevel) level();
		clearTasks(this.goalSelector);
		clearTasks(this.targetSelector);
		if(isKilled())
			return;

		this.targetSelector.addGoal(0, new EntityAIClearTarget(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NpcNearestAttackableTargetGoal<>(this, LivingEntity.class, 4, this.ais.directLOS, false, new NPCAttackSelector(this)));
        this.targetSelector.addGoal(3, new EntityAIOwnerHurtByTarget(this));
        this.targetSelector.addGoal(4, new EntityAIOwnerHurtTarget(this));

		if(ais.movementType == 1){
	        this.moveControl = new FlyingMoveHelper(this);
			if(!(this.navigation instanceof FlyingPathNavigation)) {
				this.navigation = new FlyingPathNavigation(this, level()) {
					public boolean isStableDestination(BlockPos p_26439_) {
						return true;
					}
				};
			}
		}
		else if(ais.movementType == 2){
	        this.moveControl = new FlyingMoveHelper(this);
			if(!(this.navigation instanceof WaterBoundPathNavigation)) {
				this.navigation = new WaterBoundPathNavigation(this, level());
			}
		}
		else{
	        this.moveControl = new MoveControl(this);
			if(!(this.navigation instanceof GroundPathNavigation)) {
				this.navigation = new NpcGroundPathNavigator(this, level());
			}
			this.goalSelector.addGoal(0, new EntityAIWaterNav(this));
		}
		
		this.taskCount = 1;
		this.addRegularEntries();
		this.doorInteractType();
		this.seekShelter();
		this.setResponse();
		this.setMoveType();
	}

	@Override
	protected PathNavigation createNavigation(Level p_21480_) {
		return new NpcGroundPathNavigator(this, p_21480_);
	}

	private void setResponse(){
		aiAttackTarget = aiRange = null;
		if (this.ais.canSprint)
			this.goalSelector.addGoal(this.taskCount++, new EntityAISprintToTarget(this));
		
        if (this.ais.onAttack == 1){ 
        	this.goalSelector.addGoal(this.taskCount++, new EntityAIPanic(this, 1.2F));
        }
        else if (this.ais.onAttack == 2)  {
        	this.goalSelector.addGoal(this.taskCount++, new EntityAIAvoidTarget(this));
        }        
        else if (this.ais.onAttack == 0) {
    		if (this.ais.canLeap)
    			this.goalSelector.addGoal(this.taskCount++, new EntityAIPounceTarget(this));
    		

        	this.goalSelector.addGoal(this.taskCount, aiAttackTarget = new EntityAIAttackTarget(this));
        	
        	if(this.inventory.getProjectile() != null){
        		this.goalSelector.addGoal(this.taskCount++, aiRange = new EntityAIRangedAttack(this));
        	}
        }
        else if (this.ais.onAttack == 3) {
        	//do nothing
        }
    }

	public boolean canFly(){
		return this.navigation instanceof FlyingPathNavigation;
	}

	/*
	 * Branch task function for setting if an NPC wanders or not
	 */
	public void setMoveType(){	
		if (ais.getMovingType() == 1){
			this.goalSelector.addGoal(this.taskCount++, new EntityAIWander(this));
		}
		if (ais.getMovingType() == 2){
			this.goalSelector.addGoal(this.taskCount++, new EntityAIMovingPath(this));
		}
	}
	
	public void doorInteractType(){
		if(navigation instanceof GroundPathNavigation){//currently opening doors is only supported by ground navigation
			Goal aiDoor = null;
			if (this.ais.doorInteract == 1) {
				this.goalSelector.addGoal(this.taskCount++, aiDoor = new OpenDoorGoal(this, true));
			}
			else if (this.ais.doorInteract == 0)
			{
				this.goalSelector.addGoal(this.taskCount++, aiDoor = new EntityAIBustDoor(this));
			}
			((GroundPathNavigation)navigation).setCanOpenDoors(aiDoor != null);
		}
	}
	
	/*
	 * Branch task function for finding shelter under the appropriate conditions
	 */
	public void seekShelter() {
		if (this.ais.findShelter == 0)
		{
			this.goalSelector.addGoal(this.taskCount++, new EntityAIMoveIndoors(this));
		}
		else if (this.ais.findShelter == 1)
		{
			if(!canFly())//doesnt work when flying
				this.goalSelector.addGoal(this.taskCount++, new RestrictSunGoal(this));
			this.goalSelector.addGoal(this.taskCount++, new EntityAIFindShade(this));
		}
	}
			
	/*
	 * Add immutable task entries.
	 */
	public void addRegularEntries() {
		this.goalSelector.addGoal(this.taskCount++, new EntityAIReturn(this));
		this.goalSelector.addGoal(this.taskCount++, new EntityAIFollow(this));
		if (this.ais.getStandingType() != 1 && this.ais.getStandingType() != 3)
			this.goalSelector.addGoal(this.taskCount++, new EntityAIWatchClosest(this, LivingEntity.class, 5.0F));
		this.goalSelector.addGoal(this.taskCount++, lookAi = new EntityAILook(this));
		this.goalSelector.addGoal(this.taskCount++, new EntityAIWorldLines(this));
		this.goalSelector.addGoal(this.taskCount++, new EntityAIJob(this));
		this.goalSelector.addGoal(this.taskCount++, new EntityAIRole(this));
		this.goalSelector.addGoal(this.taskCount++, animateAi = new EntityAIAnimation(this));
		if(transform.isValid())
			this.goalSelector.addGoal(this.taskCount++, new EntityAITransform(this));
	}
	
	public float getSpeed() {
		return (float)ais.getWalkingSpeed() / 20.0F;
	}

	protected float getWaterSlowDown() {
		return ais.movementType == 2? 0.95F : 0.8F;
	}

    @Override
	public float getWalkTargetValue(BlockPos pos){
    	if(ais.movementType == 2) {
            return this.isInWater() ? 10.0F : 0;
    	}
		float weight = this.level().getLightEmission(pos) - 0.5F;
    	if(level().getBlockState(pos).isSolidRender(level(), pos))
    		weight += 10;
    	return weight;
    }

    @Override
	protected int decreaseAirSupply(int par1){
		if (!this.stats.canDrown)
			return par1;
        return super.decreaseAirSupply(par1);
    }

    @Override
	public MobType getMobType(){
        return this.stats == null ? null : this.stats.creatureType;
    }

	@Override
    public int getAmbientSoundInterval(){
        return 160;
    }

	@Override
    public void playAmbientSound(){
		if (!this.isAlive())
			return;
        this.advanced.playSound(this.getTarget() != null ? 1 : 0, getSoundVolume(), getVoicePitch());
    }
    
    @Override
    protected void playHurtSound(DamageSource source){
        this.advanced.playSound(2, getSoundVolume(), getVoicePitch());
    }

    @Override
    public SoundEvent getDeathSound(){
        return null;
    }
	
	@Override
	public float getVoicePitch(){
		if(this.advanced.disablePitch)
			return 1;
    	return super.getVoicePitch();
    }
    
    @Override
    protected void playStepSound(BlockPos pos, BlockState state){
    	if (this.advanced.getSound(4) != null) {
    		this.advanced.playSound(4, 0.15F, 1.0F);
    	}
    	else {
    		super.playStepSound(pos, state);
    	}
    }
    
    public ServerPlayer getFakeChatPlayer(){
    	if(level().isClientSide)
    		return null;
		EntityUtil.Copy(this, ChatEventPlayer);
		ChatEventProfile.npc = this;
		((EntityIMixin)ChatEventPlayer).setLevel((ServerLevel) level());
		ChatEventPlayer.setPos(getX(), getY(), getZ());
		return ChatEventPlayer;
    }

	public void saySurrounding(Line line) {
		if (line == null)
			return;
		if(line.getShowText() && !line.getText().isEmpty()){
//			ServerMessageEvents.AllowChatMessage
//			ServerChatEvent event = new ServerChatEvent(getFakeChatPlayer(), line.getText(), Component.translatable(line.getText().replace("%", "%%")));
//	        if (CustomNpcs.NpcSpeachTriggersChatEvent && (MinecraftForge.EVENT_BUS.post(event) || event.getMessage() == null)){
//	            return;
//	        }
//			line.setText(event.getMessage().getString().replace("%%", "%")); TODO ADD FABRIC EVENT
		}
		
		List<Player> inRange = level().getEntitiesOfClass(
				Player.class, this.getBoundingBox().inflate(20D, 20D, 20D));
		for (Player player : inRange)
			say(player, line);
	}

	public void say(Player player, Line line) {
		if (line == null || !this.canNpcSee(player))
			return;		
		
		if(!line.getSound().isEmpty()){
			BlockPos pos = this.blockPosition();
			Packets.send((ServerPlayer)player, new PacketPlaySound(line.getSound(), pos, this.getSoundVolume(), this.getVoicePitch()));
		}
		if(!line.getText().isEmpty()) {
			Packets.send((ServerPlayer)player, new PacketChatBubble(this.getId(), Component.translatable(line.getText()), line.getShowText()));
		}
	}
	
	@Override
    public boolean shouldShowName(){
    	return true;
    }

	@Override
	public void push(double d, double d1, double d2) {
		if (isWalking() && !isKilled())
			super.push(d, d1, d2);
	}


	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		npcVersion = compound.getInt("ModRev");
		VersionCompatibility.CheckNpcCompatibility(this, compound);
		
		display.readToNBT(compound);
		stats.readToNBT(compound);
		ais.readToNBT(compound);
		script.load(compound);
		timers.load(compound);
		
		advanced.readToNBT(compound);
		role.load(compound);
		job.load(compound);
        
		inventory.load(compound);
		transform.readToNBT(compound);
		
		killedtime = compound.getLong("KilledTime");	
		totalTicksAlive = compound.getLong("TotalTicksAlive");
		
		linkedName = compound.getString("LinkedNpcName");
		if(!isClientSide())
			LinkedNpcController.Instance.loadNpcData(this);
		
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(CustomNpcs.NpcNavRange);

		updateAI = true;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		display.save(compound);
		stats.save(compound);
		ais.save(compound);
		script.save(compound);
		timers.save(compound);
		
		advanced.save(compound);
		role.save(compound);
		job.save(compound);
        
		inventory.save(compound);
		transform.save(compound);

		compound.putLong("KilledTime", killedtime);
		compound.putLong("TotalTicksAlive", totalTicksAlive);
		compound.putInt("ModRev", npcVersion);
		compound.putString("LinkedNpcName", linkedName);
	}

	@Override
	public EntityDimensions getDimensions(Pose poseIn) {
		EntityDimensions size = baseSize;
		if(currentAnimation == AnimationType.SLEEP || currentAnimation == AnimationType.CRAWL || deathTime > 0){
			size = sizeSleep;
		}
		else if (isPassenger() || currentAnimation == AnimationType.SIT){
			size = baseSize.scale(1, 0.77f);
		}

		size = size.scale(display.getSize() * 0.2f);

		if(display.getHitboxState() == 1 || isKilled() && stats.hideKilledBody) {
			size = size.scalable(0.00001f, size.height);
		}
//		if(size.width / 2 > level().getMaxEntityRadius()) {
//			level().increaseMaxEntityRadius(size.width / 2);
//		}
		return size;
	}

	@Override
	public void tickDeath(){
		if(stats.spawnCycle == 3){
			super.tickDeath();
			return;
		}
		
		++this.deathTime;
		if(level().isClientSide)
			return;
		if(!hasDied){
			remove(RemovalReason.KILLED);
		}
		if (killedtime < System.currentTimeMillis()) {
			if (stats.spawnCycle == 0 || (this.level().isDay() && stats.spawnCycle == 1) || (!this.level().isDay() && stats.spawnCycle == 2) || stats.spawnCycle == 4) {
				reset();
			}
		}
	}
	
	public void reset() {
		boolean needsSync = hasDied;
		hasDied = false;
		unsetRemoved();
		dead = false;
		wasKilled = false;
		setSprinting(false);
		setHealth(getMaxHealth());
		entityData.set(Animation, 0);
		entityData.set(Walking, false);
		entityData.set(IsDead, false);
		entityData.set(Interacting, false);
		interactingEntities.clear();
		
		combatHandler.reset();
		this.setTarget(null);
		this.setLastHurtByMob(null);

		this.deathTime = 0;
		if(ais.returnToStart && !hasOwner() && !isClientSide() && !isPassenger()) {
			moveTo(getStartXPos(), getStartYPos(), getStartZPos(), getYRot(), getXRot());
		}
		killedtime = 0;
		clearFire();
		this.removeAllEffects();
		travel(Vec3.ZERO);
		this.walkDistO = this.walkDist = 0;
		getNavigation().stop();
		currentAnimation = AnimationType.NONE;
		refreshDimensions();
		updateAI = true;
		ais.movingPos = 0;
		if(getOwner() != null){
			getOwner().setLastHurtMob(null);
		}
		bossInfo.setVisible(display.getBossbar() == 1);

		job.reset();
		
		EventHooks.onNPCInit(this);
		if(needsSync){
			List<SynchedEntityData.DataValue<?>> data = this.getEntityData().getNonDefaultValues();
			for(ServerPlayer player : level().getServer().getPlayerList().getPlayers()){
				if(display.isVisibleTo(player) || player.isSpectator() || player.getMainHandItem().getItem() == CustomItems.wand){
					Packets.send(player, new PacketUpdatePhysics(this));
					if(data !=null) {
						player.connection.send(new ClientboundSetEntityDataPacket(this.getId(), data));
					}
					Packets.send(player, new PacketNpcUpdate(getId(), writeSpawnData()));
				}
			}
		}
	}

    public void onCollide() {	
    	if(!isAlive() || tickCount % 4 != 0 || level().isClientSide)
    		return;
    	
        AABB axisalignedbb = null;

        if (this.getVehicle() != null && this.getVehicle().isAlive()){
            axisalignedbb = this.getBoundingBox().minmax(this.getVehicle().getBoundingBox()).inflate(1.0D, 0.0D, 1.0D);
        }
        else{
            axisalignedbb = this.getBoundingBox().inflate(1.0D, 0.5D, 1.0D);
        }

        List list = this.level().getEntitiesOfClass(LivingEntity.class, axisalignedbb);
        if(list == null)
        	return;

        for (int i = 0; i < list.size(); ++i){
            Entity entity = (Entity)list.get(i);
            if (entity != this && entity.isAlive())
            	EventHooks.onNPCCollide(this, entity);
        }
        
    }
    
	@Override
    public void handleInsidePortal(BlockPos pos){
		//prevent npcs from walking into portals
	}

	public double prevChasingPosX;
	public double prevChasingPosY;
	public double prevChasingPosZ;
	public double chasingPosX;
	public double chasingPosY;
	public double chasingPosZ;

	public void cloakUpdate() {
		this.prevChasingPosX = this.chasingPosX;
		this.prevChasingPosY = this.chasingPosY;
		this.prevChasingPosZ = this.chasingPosZ;
		double d0 = this.getX() - this.chasingPosX;
		double d1 = this.getY() - this.chasingPosY;
		double d2 = this.getZ() - this.chasingPosZ;
		double d3 = 10.0D;
		if (d0 > 10.0D) {
			this.chasingPosX = this.getX();
			this.prevChasingPosX = this.chasingPosX;
		}

		if (d2 > 10.0D) {
			this.chasingPosZ = this.getZ();
			this.prevChasingPosZ = this.chasingPosZ;
		}

		if (d1 > 10.0D) {
			this.chasingPosY = this.getY();
			this.prevChasingPosY = this.chasingPosY;
		}

		if (d0 < -10.0D) {
			this.chasingPosX = this.getX();
			this.prevChasingPosX = this.chasingPosX;
		}

		if (d2 < -10.0D) {
			this.chasingPosZ = this.getZ();
			this.prevChasingPosZ = this.chasingPosZ;
		}

		if (d1 < -10.0D) {
			this.chasingPosY = this.getY();
			this.prevChasingPosY = this.chasingPosY;
		}

		this.chasingPosX += d0 * 0.25D;
		this.chasingPosZ += d2 * 0.25D;
		this.chasingPosY += d1 * 0.25D;
	}


	@Override
	public ItemStack getMainHandItem() {
		IItemStack item = null;
		if (isAttacking())
			item = inventory.getRightHand();
		else if(role.getType() == RoleType.COMPANION)
			item = ((RoleCompanion)role).getItemInHand();
		else if (job.overrideMainHand)
			item = job.getMainhand();
		else
			item = inventory.getRightHand();
		
		return ItemStackWrapper.MCItem(item);
	}

	@Override
	public ItemStack getOffhandItem() {
		IItemStack item = null;
		if (isAttacking())
			item = inventory.getLeftHand();
		else if (job.overrideOffHand)
			item = job.getOffhand();
		else
			item = inventory.getLeftHand();
		return ItemStackWrapper.MCItem(item);
	}

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot){
    	if(slot == EquipmentSlot.MAINHAND)
    		return getMainHandItem();
    	if(slot == EquipmentSlot.OFFHAND)
    		return getOffhandItem();
        return ItemStackWrapper.MCItem(inventory.getArmor(3 - slot.getIndex()));
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack item){
    	if(slot == EquipmentSlot.MAINHAND)
    		inventory.weapons.put(0, NpcAPI.Instance().getIItemStack(item));
    	else if(slot == EquipmentSlot.OFFHAND)
    		inventory.weapons.put(2, NpcAPI.Instance().getIItemStack(item));
    	else{
    		inventory.armor.put(3 - slot.getIndex(), NpcAPI.Instance().getIItemStack(item));
    	}
    }

    @Override
    public Iterable<ItemStack> getArmorSlots(){
    	ArrayList<ItemStack> list = new ArrayList<ItemStack>();
    	for(int i = 0; i < 4; i++){
    		list.add(ItemStackWrapper.MCItem(inventory.armor.get(3 - i)));
    	}
    	return list;
    }

    @Override
    public Iterable<ItemStack> getAllSlots(){
    	ArrayList list = new ArrayList();
    	list.add(ItemStackWrapper.MCItem(inventory.weapons.get(0)));
    	list.add(ItemStackWrapper.MCItem(inventory.weapons.get(2)));
        return list;
    }

	@Override
	protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {

	}

	@Override
	protected void dropFromLootTable(DamageSource damageSourceIn, boolean attackedRecently) {

	}

	@Override
	public void die(DamageSource damagesource){
		setSprinting(false);
		getNavigation().stop();
		clearFire();
		removeAllEffects();
		
		if(!isClientSide()){
			this.advanced.playSound(3, this.getSoundVolume(), this.getVoicePitch());
			Entity attackingEntity = NoppesUtilServer.GetDamageSourcee(damagesource);
			NpcEvent.DiedEvent event = new NpcEvent.DiedEvent(this.wrappedNPC, damagesource, attackingEntity);
			event.droppedItems = inventory.getItemsRNG();
			event.expDropped = inventory.getExpRNG();
			event.line = advanced.getKilledLine();
			EventHooks.onNPCDied(this, event);
			
			bossInfo.setVisible(false);
			inventory.dropStuff(event, attackingEntity, damagesource);
			if(event.line != null)
				saySurrounding(Line.formatTarget((Line)event.line, attackingEntity instanceof LivingEntity?(LivingEntity)attackingEntity:null));
		}
		super.die(damagesource);
	}

	@Override
    public void startSeenByPlayer(ServerPlayer player){
        super.startSeenByPlayer(player);
        this.bossInfo.addPlayer(player);
    }

	@Override
    public void stopSeenByPlayer(ServerPlayer player){
        super.stopSeenByPlayer(player);
        this.bossInfo.removePlayer(player);
    }
	
	@Override
	public boolean removeWhenFarAway(double distanceToPlayer) {
		return stats != null && (stats.spawnCycle == 3 || stats.spawnCycle == 4);
	}

	@Override
	public void remove(Entity.RemovalReason reason) {
		// Cache spawnCycle 3/4 NPC data before they're removed by Minecraft's despawn system
		if (reason != RemovalReason.KILLED && stats != null && (stats.spawnCycle == 3 || stats.spawnCycle == 4) && level() != null && !level().isClientSide) {
			noppes.npcs.controllers.NaturalSpawnCache.instance.cacheNpc(this);
		}
		if(reason != RemovalReason.KILLED){
			super.remove(reason);
			return;
		}
		hasDied = true;

		ejectPassengers();
		stopRiding();
		
		if(level().isClientSide || stats.spawnCycle == 3){
			//this.spawnExplosionParticle();
			delete();
		}
		else {			
			setHealth(-1);
			setSprinting(false);
			getNavigation().stop();

	    	setCurrentAnimation(AnimationType.SLEEP);
	    	refreshDimensions();
	    	
			if(killedtime <= 0)
				killedtime = stats.respawnTime * 1000 + System.currentTimeMillis();

			role.killed();
			job.killed();
		}
	}

	public void delete() {
		VisibilityController.instance.remove(this);
		role.delete();
		job.delete();
		super.remove(RemovalReason.DISCARDED);
	}
	
	public float getStartXPos(){
		return ais.startPos().getX() + ais.bodyOffsetX / 10;
	}
	
	public float getStartZPos(){
		return ais.startPos().getZ() + ais.bodyOffsetZ / 10;
	}

	public boolean isVeryNearAssignedPlace() {
		double xx = getX() - getStartXPos();
		double zz = getZ() - getStartZPos();
		if (xx < -0.2 || xx > 0.2)
			return false;
        return !(zz < -0.2) && !(zz > 0.2);
    }

//	@Override
//	public IIcon getItemIcon(ItemStack par1ItemStack, int limbSwingAmount){
//        if (par1ItemStack.getItem() == Items.bow){
//            return Items.bow.getIcon(par1ItemStack, limbSwingAmount);
//        }
//		Player player = CustomNpcs.proxy.getPlayer();
//		if(player == null)
//			return super.getItemIcon(par1ItemStack, limbSwingAmount);
//		return player.getItemIcon(par1ItemStack, limbSwingAmount);
//    }
	private double startYPos = -6666;
	public double getStartYPos() {
		if(startYPos < level().getMinBuildHeight())
			return calculateStartYPos(ais.startPos());
		return startYPos;
	}
	private double calculateStartYPos(BlockPos pos) {
		BlockPos startPos = ais.startPos();
		while(pos.getY() > level().getMinBuildHeight()){
			BlockState state = level().getBlockState(pos);
			VoxelShape shape = state.getShape(level(), pos);
			if(shape.isEmpty()){
				pos = pos.below();
				continue;
			}
			AABB bb = shape.bounds().move(pos);
			if(ais.movementType == 2 && startPos.getY() <= pos.getY() && state.is(Blocks.WATER)) {
				pos = pos.below();
				continue;
			}
			return bb.maxY;
		}
		return level().getMinBuildHeight();
	}
	
	private BlockPos calculateTopPos(BlockPos pos) {
		BlockPos check = pos;
		while(check.getY() > level().getMinBuildHeight()){
			BlockState state = level().getBlockState(pos);
			VoxelShape shape = state.getShape(level(), pos);
			if(!shape.isEmpty()){
				AABB bb = shape.bounds().move(pos);
				if (bb != null){
					return check;
				}
			}
			check = check.below();
		}
		return pos;
	}
	
	public boolean isInRange(Entity entity, double range){
		return this.isInRange(entity.getX(), entity.getY(), entity.getZ(), range);
	}
	
	public boolean isInRange(double posX, double posY, double posZ, double range){
		double y = Math.abs(this.getY() - posY);
		if(posY >= level().getMinBuildHeight() && y > range)
			return false;
		
		double x = Math.abs(this.getX() - posX);
		double z = Math.abs(this.getZ() - posZ);
		
		return x <= range && z <= range;
	}

	public void givePlayerItem(Player player, ItemStack item) {
		if (level().isClientSide) {
			return;
		}
		item = item.copy();
		float f = 0.7F;
		double d = (double) (level().random.nextFloat() * f)
				+ (double) (1.0F - f);
		double d1 = (double) (level().random.nextFloat() * f)
				+ (double) (1.0F - f);
		double d2 = (double) (level().random.nextFloat() * f)
				+ (double) (1.0F - f);
		ItemEntity entityitem = new ItemEntity(level(), getX() + d, getY() + d1,
				getZ() + d2, item);
		entityitem.setPickUpDelay(2);
		level().addFreshEntity(entityitem);

		int i = item.getCount();

		if (player.getInventory().add(item)) {
            this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			player.take(entityitem, i);

			if (item.getCount() <= 0) {
				entityitem.discard();
			}
		}
	}
	
	@Override
	public boolean isSleeping() {
		return currentAnimation == AnimationType.SLEEP && !isAttacking();
	}
//
//	@Override
//	public boolean isPassenger() {
//		return currentAnimation == AnimationType.SITTING && !isAttacking() || getVehicle() != null;
//	}

	public boolean isWalking() {
		return ais.getMovingType() != 0 || isAttacking() || isFollower() || entityData.get(Walking);
	}

	@Override
	public boolean isCrouching() {
		return currentAnimation == AnimationType.CROUCH;
	}

	@Override
    public void knockback(double strength, double ratioX, double ratioZ){
		super.knockback(strength * (2 - stats.resistances.knockback), ratioX, ratioZ);
    }
    
	public Faction getFaction() {
		Faction fac = FactionController.instance.getFaction(entityData.get(FactionData));
		if (fac == null) {
			return FactionController.instance.getFaction(FactionController.instance.getFirstFactionId());
		}
		return fac;
	}
	
	public boolean isClientSide(){
		return level() == null || level().isClientSide;
	}
	
	public void setFaction(int id) {
		if(id < 0|| isClientSide())
			return;
		entityData.set(FactionData, id);
	}
	
	@Override
	public boolean canBeAffected(MobEffectInstance effect){
		if(stats.potionImmune)
			return false;
		if(getMobType() == MobType.ARTHROPOD && effect.getEffect() == MobEffects.POISON)
			return false;
        return super.canBeAffected(effect);
    }

	public boolean isAttacking() {
		return entityData.get(Attacking);
	}

	public boolean isKilled() {
		return this.isRemoved() || entityData.get(IsDead);
	}

	public void writeSpawnData(FriendlyByteBuf buffer) {
		buffer.writeNbt(writeSpawnData());
	}

	public CompoundTag writeSpawnData() {
		CompoundTag compound = new CompoundTag();
		display.save(compound);
		compound.putInt("MaxHealth", stats.maxHealth);
		compound.put("Armor", NBTTags.nbtIItemStackMap(inventory.armor));
		compound.put("Weapons", NBTTags.nbtIItemStackMap(inventory.weapons));
		compound.putInt("Speed", ais.getWalkingSpeed());
		compound.putBoolean("MountControl", ais.mountControl);
		compound.putBoolean("DeadBody", stats.hideKilledBody);
		compound.putInt("StandingState", ais.getStandingType());
		compound.putInt("MovingState", ais.getMovingType());
		compound.putInt("Orientation", ais.orientation);
		compound.putFloat("PositionXOffset", ais.bodyOffsetX);
		compound.putFloat("PositionYOffset", ais.bodyOffsetY);
		compound.putFloat("PositionZOffset", ais.bodyOffsetZ);
		compound.putInt("Role", role.getType());
		compound.putInt("Job", job.getType());
		if(job.getType() == JobType.BARD){
			CompoundTag bard = new CompoundTag();
			job.save(bard);
			compound.put("Bard", bard);
		}
		if(job.getType() == JobType.PUPPET){
			CompoundTag bard = new CompoundTag();
			job.save(bard);
			compound.put("Puppet", bard);
		}
		if(role.getType() == RoleType.COMPANION){
			CompoundTag bard = new CompoundTag();
			role.save(bard);
			compound.put("Companion", bard);
		}
		
		if(this instanceof EntityCustomNpc){
			compound.put("ModelData", ((EntityCustomNpc)this).modelData.save());
		}
		return compound;
	}

	public void readSpawnData(FriendlyByteBuf buf) {
		readSpawnData(buf.readNbt());
	}

	public void readSpawnData(CompoundTag compound) {
		stats.setMaxHealth(compound.getInt("MaxHealth"));
		ais.setWalkingSpeed(compound.getInt("Speed"));
		stats.hideKilledBody = compound.getBoolean("DeadBody");
		ais.setStandingType(compound.getInt("StandingState"));
		ais.mountControl = compound.getBoolean("MountControl");
		ais.setMovingType(compound.getInt("MovingState"));
		ais.orientation = compound.getInt("Orientation");
		ais.bodyOffsetX = compound.getFloat("PositionXOffset");
		ais.bodyOffsetY = compound.getFloat("PositionYOffset");
		ais.bodyOffsetZ = compound.getFloat("PositionZOffset");
		
		inventory.armor = NBTTags.getIItemStackMap(compound.getList("Armor", 10));
		inventory.weapons = NBTTags.getIItemStackMap(compound.getList("Weapons", 10));
		advanced.setRole(compound.getInt("Role"));
		advanced.setJob(compound.getInt("Job"));
		if(job.getType() == JobType.BARD){
			CompoundTag bard = compound.getCompound("Bard");
			job.load(bard);
		}		
		if(job.getType() == JobType.PUPPET){
			CompoundTag puppet = compound.getCompound("Puppet");
			job.load(puppet);
		}	
		if(role.getType() == RoleType.COMPANION){
			CompoundTag puppet = compound.getCompound("Companion");
			role.load(puppet);
		}		
		if(this instanceof EntityCustomNpc){
			((EntityCustomNpc)this).modelData.load(compound.getCompound("ModelData"));
		}
		display.readToNBT(compound);
		refreshDimensions();
	}

	@Override
    public CommandSourceStack createCommandSourceStack(){
    	if(level().isClientSide)
    		return super.createCommandSourceStack();
		EntityUtil.Copy(this, CommandPlayer);
		((EntityIMixin)CommandPlayer).setLevel((ServerLevel)level());
		CommandPlayer.setPos(getX(), getY(), getZ());
		return new CommandSourceStack(this, this.position(), this.getRotationVector(), this.level() instanceof ServerLevel ? (ServerLevel)this.level() : null, this.getPermissionLevel(), this.getName().getString(), this.getDisplayName(), this.level().getServer(), this);
    }
	
	@Override
	public Component getName() {
		return Component.translatable(display.getName());
	}

//	@Override
//	public boolean canAttack(EntityType type){
//        return EntityType.BAT != type;
//    }

	public void setImmuneToFire(boolean immuneToFire) {
		stats.immuneToFire = immuneToFire;
	}

	@Override
	public boolean fireImmune() {
		return stats.immuneToFire;
	}
	
	@Override
	public boolean causeFallDamage(float distance, float modifier, DamageSource source) {
		if (!this.stats.noFallDamage) {
			return super.causeFallDamage(distance, modifier, source);
		}
		return false;
	}
	
	@Override
	public void makeStuckInBlock(BlockState state, Vec3 motionMultiplierIn) {
		if (state != null && !state.is(Blocks.COBWEB) || !stats.ignoreCobweb) {
			super.makeStuckInBlock(state, motionMultiplierIn);
		}
    }
	
	@Override
	public boolean canBeCollidedWith(){
		return !isKilled() && display.getHitboxState() == 2;
	}

	@Override
	protected void pushEntities() {
		if(display.getHitboxState() != 0)
			return;
		super.pushEntities();
	}

	@Override
    public boolean isPushable(){
		return isWalking() && !isKilled();
    }

	@Override
    public PushReaction getPistonPushReaction(){
        return display.getHitboxState() == 0 ? super.getPistonPushReaction() : PushReaction.IGNORE;
    }
	
	public EntityAIRangedAttack getRangedTask(){
		return this.aiRange;
	}

	public String getRoleData(){
		return entityData.get(RoleData);
	}
	
	public void setRoleData(String s){
		entityData.set(RoleData, s);
	}

	public String getJobData(){
		return entityData.get(RoleData);
	}
	
	public void setJobData(String s){
		entityData.set(RoleData, s);
	}
	
	@Override
	public Level getCommandSenderWorld() {
		return level();
	}

	@Override
	public boolean isInvisibleTo(Player player){
		return display.getVisible() == 1 && player.getMainHandItem().getItem() != CustomItems.wand
				&& !display.availability.hasOptions();
	}

	@Override
	public boolean isInvisible(){
		return display.getVisible() != 0 && !display.availability.hasOptions();
	}


	public void setInvisible(ServerPlayer playerMP){
		if(tracking.contains(playerMP.getId())) {
			tracking.remove(playerMP.getId());
			Packets.send(playerMP, new PacketNpcVisibleFalse(this.getId()));
		}
	}

	public void setVisible(ServerPlayer playerMP){
		if(!tracking.contains(playerMP.getId())) {
			tracking.add(playerMP.getId());
			Packets.send(playerMP, new PacketNpcVisibleTrue(this));
			List<SynchedEntityData.DataValue<?>> data = this.getEntityData().getNonDefaultValues();
			if(data !=null) {
				playerMP.connection.send(new ClientboundSetEntityDataPacket(this.getId(), data));
			}
		}
		//fix for data not syncing properly in 1.16
		Packets.send(playerMP, new PacketNpcUpdate(getId(), writeSpawnData()));
	}

    public void setCurrentAnimation(int animation) {
    	currentAnimation = animation;
    	entityData.set(Animation, animation);
	}

	public boolean canNpcSee(Entity entity){
		return this.getSensing().hasLineOfSight(entity);
	}
	
	public boolean isFollower() {
		if(advanced.scenes.getOwner() != null)
			return true;
		return role.isFollowing() || job.isFollowing();
	}
		
	public LivingEntity getOwner(){
		if(advanced.scenes.getOwner() != null)
			return advanced.scenes.getOwner();
		if(role.getType() == RoleType.FOLLOWER && role instanceof RoleFollower)
			return ((RoleFollower)role).owner;
		
		if(role.getType() == RoleType.COMPANION && role instanceof RoleCompanion)
			return ((RoleCompanion)role).owner;

		if(job.getType() == JobType.FOLLOWER && job instanceof JobFollower)
			return ((JobFollower)job).following;
		
		return null;
	}
	
	public boolean hasOwner(){
		if(advanced.scenes.getOwner() != null)
			return true;
		return role.getType() == RoleType.FOLLOWER && ((RoleFollower) role).hasOwner() || 
				role.getType() == RoleType.COMPANION && ((RoleCompanion) role).hasOwner() || 
				job.getType() == JobType.FOLLOWER && ((JobFollower)job).hasOwner();
	}
	
	public int followRange() {
		if(advanced.scenes.getOwner() != null)
			return 4;
        if (role.getType() == RoleType.FOLLOWER && role.isFollowing())
			return 6;
        if (role.getType() == RoleType.COMPANION && role.isFollowing())
			return 4;
        if (job.getType() == JobType.FOLLOWER && job.isFollowing())
			return 4;
		
		return 15;
	}

	@Override
    protected float getDamageAfterArmorAbsorb(DamageSource source, float damage){
		if(role.getType() == RoleType.COMPANION)
			damage = ((RoleCompanion)role).getDamageAfterArmorAbsorb(source, damage);
    	return damage;
    }

	@Override
    public boolean isAlliedTo(Entity entity){
		if(!isClientSide()){
			if(entity instanceof Player && getFaction().isFriendlyToPlayer((Player)entity))
				return true;
			if(entity == getOwner())
				return true;
			if(entity instanceof EntityNPCInterface && ((EntityNPCInterface)entity).faction.id == faction.id)
				return true;
		}
        return super.isAlliedTo(entity);
    }
	
	public void setDataWatcher(SynchedEntityData entityData) {
		List<SynchedEntityData.DataValue<?>> list = new ArrayList<>();
		for(SynchedEntityData.DataItem<?> entry : ((ISynchedEntityData)entityData).getAll()){
			if(entry.getValue() instanceof SynchedEntityData.DataValue){
				list.add(((SynchedEntityData.DataValue)entry.getValue()));
			}
		}
		this.entityData.assignValues(list);
	}

	@Override
    public void travel(Vec3 travelVector){
        BlockPos pos = blockPosition();
		if (this.isAlive() && this.isVehicle() && this.ais.mountControl && this.getControllingPassenger() != null) {
			LivingEntity livingentity = this.getControllingPassenger();
			this.setYRot(livingentity.getYRot());
			this.yRotO = this.getYRot();
			this.setXRot(livingentity.getXRot() * 0.5F);
			this.setRot(this.getYRot(), this.getXRot());
			this.yBodyRot = this.getYRot();
			this.yHeadRot = this.yBodyRot;
			float f = livingentity.xxa * 0.5F;
			float f1 = livingentity.zza;
			if (f1 <= 0.0F) {
				f1 *= 0.25F;
			}
			this.setMaxUpStep(1.1F);
			super.travel(new Vec3(f, travelVector.y, f1));
		} else {
			this.setMaxUpStep(0.5F);
			super.travel(travelVector);
		}
    	if(role.getType() == RoleType.COMPANION && !isClientSide()) {
			BlockPos delta = blockPosition().subtract(pos);
			((RoleCompanion) role).addMovementStat(delta.getX(), delta.getY(), delta.getZ());
		}
    }
	
	@Override
	public boolean canBeLeashed(Player player){
    	return false;
    }
    
    @Override
    public boolean isLeashed(){
        return false;
    }
	
	public boolean nearPosition(BlockPos pos) {
		BlockPos npcpos = blockPosition();
		float x = npcpos.getX() - pos.getX();
		float z = npcpos.getZ() - pos.getZ();
		float y = npcpos.getY() - pos.getY();
		float height = Mth.ceil(this.getBbHeight() + 1) * Mth.ceil(this.getBbHeight() + 1);
		return x * x + z * z < 2.5 && y * y < height + 2.5;
	}
	
	public void tpTo(LivingEntity owner) {
		if(owner == null)
			return;
		Direction facing = owner.getDirection().getOpposite();
		BlockPos pos = new BlockPos((int) owner.getX(), (int) owner.getBoundingBox().minY, (int) owner.getZ());
		pos = pos.offset(facing.getStepX(), 0, facing.getStepZ());
		pos = calculateTopPos(pos);
		
		for(int i = -1; i < 2; i++){
			for(int j = 0; j < 3; j++){
				BlockPos check;
				if(facing.getStepX() == 0){
					check = pos.offset(i, 0, j * facing.getStepZ());
				}
				else{
					check = pos.offset(j * facing.getStepX(), 0, i);
				}
				check = calculateTopPos(check);
				if(!level().getBlockState(check).isSolidRender(level(), check) && !level().getBlockState(check.above()).isSolidRender(level(), check.above())){
			        moveTo(check.getX() + 0.5F, check.getY(), check.getZ() + 0.5F, getYRot(), getXRot());
			        this.getNavigation().stop();
					break;
				}
			}
		}
	}

//	@Override
//	public boolean checkSpawnRules(IWorld level, MobSpawnType spawnReasonIn) {
//        return this.getWalkTargetValue(new BlockPos(this.getX(), this.getBoundingBox().minY, this.getZ())) >= 0.0F;
//    }

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> para) {
		super.onSyncedDataUpdated(para);
		if (Animation.equals(para)) {
			this.refreshDimensions();
		}
	}

	protected void updateControlFlags() {
		boolean flag1 = !(this.getVehicle() instanceof Boat);
		this.goalSelector.setControlFlag(Goal.Flag.MOVE, true);
		this.goalSelector.setControlFlag(Goal.Flag.JUMP, flag1);
		this.goalSelector.setControlFlag(Goal.Flag.LOOK, true);
	}

	@Override
	public void checkDespawn() {
		super.checkDespawn();
		if(getNoActionTime()!=0 && level()!=null){
			Entity entity = this.level().getNearestPlayer(this, -1.0D);
			if (entity != null) {
				double d0 = entity.distanceToSqr(this);
				double range = (double) ais.activeRange * (double) ais.activeRange;
				if (d0 < range) {
					this.noActionTime = 0;
				}
			}
		}
	}
}
