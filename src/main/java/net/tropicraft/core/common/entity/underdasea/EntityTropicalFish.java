package net.tropicraft.core.common.entity.underdasea;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.tropicraft.core.common.entity.ai.EntityAIFindLeader;
import net.tropicraft.core.common.entity.ai.EntityAIFollowLeader;

public class EntityTropicalFish extends EntityTropicraftWaterMob {

	public boolean inSchool;
	public EntityTropicalFish leader; 
	public boolean targetHook;
	public Entity hook;
	public boolean hasBeenPlaced;
	public boolean isCatchable;
	public static final String[] names = {"Clownfish", "Queen Angelfish", "Yellow Tang", "Butterflyfish", "Geophagus Surinamensis", "Betta Fish"
		, "Regal Tang", "Royal Gamma"};

	private static final DataParameter<Integer> TEXTURE_COLOR = EntityDataManager.<Integer>createKey(EntityTropicalFish.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> SHOULD_SPAWN_SCHOOL = EntityDataManager.<Boolean>createKey(EntityTropicalFish.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> IS_LEADER = EntityDataManager.<Boolean>createKey(EntityTropicalFish.class, DataSerializers.BOOLEAN);

	public EntityTropicalFish(World world) {
		super(world);
		targetHook = false;
		inSchool = false;
		leader = null;      
		setSize(.3F, .4F);
		setColor(worldObj.rand.nextInt(names.length));
		setShouldSpawnSchool(true);
		setIsLeader(true);
		isCatchable = true;
		this.experienceValue = 3;
	}

	public EntityTropicalFish(World world, EntityLiving entityliving, int i) {
		super(world);
		setShouldSpawnSchool(false);
		targetHook = false;
		inSchool = false;
		leader = null;
		setColor(i);
		setSize(.3F, .4F);
		setIsLeader(true);
		setLocationAndAngles(entityliving.posX, entityliving.posY + (double)entityliving.getEyeHeight(), entityliving.posZ, entityliving.rotationYaw, entityliving.rotationPitch);
		posX -= MathHelper.cos((rotationYaw / 180F) * 3.141593F) * 0.16F;
		posY -= 0.10000000149011612D;
		posZ -= MathHelper.sin((rotationYaw / 180F) * 3.141593F) * 0.16F;
		setPosition(posX, posY, posZ);
		motionX = -MathHelper.sin((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F);
		motionZ = MathHelper.cos((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F);
		motionY = -MathHelper.sin((rotationPitch / 180F) * 3.141593F);
	}

	/**
	 * Will spawn a new fish of the same type that follows the original fish
	 */
	public EntityTropicalFish(EntityTropicalFish original) {
		this(original.worldObj);
		setShouldSpawnSchool(false);
		targetHook = false;
		inSchool = true;
		leader = original;
		setColor(original.getColor());
		setSize(.3F, .4F);
		setIsLeader(false);
		do {
			double offsetX = (new Random()).nextDouble() * 3 - 1.5D;
			double offsetY = (new Random()).nextDouble() * 2 + 1.0D;
			double offsetZ = (new Random()).nextDouble() * 3 - 1.5D;
			setLocationAndAngles(original.posX + offsetX, original.posY + offsetY, original.posZ + offsetZ, original.rotationYaw, original.rotationPitch);
		} while (!getCanSpawnHere());
		motionX = original.motionX;
		motionY = original.motionY;
		motionZ = original.motionZ;
	}
	
	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.tasks.addTask(1, new EntityAIFindLeader(this));
		this.tasks.addTask(2, new EntityAIFollowLeader(this, 1.25D));
	}

	@Override
	public void entityInit() {
		super.entityInit();

		int color = this.worldObj.rand.nextInt(names.length);
		this.getDataManager().register(TEXTURE_COLOR, Integer.valueOf(color));
		this.getDataManager().register(SHOULD_SPAWN_SCHOOL, Boolean.valueOf(false));
		this.getDataManager().register(IS_LEADER, Boolean.valueOf(false));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(5.0D);
	}
	
	public boolean getIsLeader() {
		return this.dataManager.get(IS_LEADER);
	}
	
	public void setIsLeader(boolean flag) {
		this.dataManager.set(IS_LEADER, Boolean.valueOf(flag));
	}

	public int getColor() {
		return this.dataManager.get(TEXTURE_COLOR);
	}

	public void setColor(int color) {
		this.dataManager.set(TEXTURE_COLOR, Integer.valueOf(color));
	}

	public void setShouldSpawnSchool(boolean spawnStatus) {
		this.dataManager.set(SHOULD_SPAWN_SCHOOL, Boolean.valueOf(spawnStatus));
	}

	public boolean getShouldSpawnSchool() {
		return this.dataManager.get(SHOULD_SPAWN_SCHOOL);
	}

//	public void checkForLeader(){
//		List<EntityTropicalFish> list = worldObj.getEntitiesWithinAABB(EntityTropicalFish.class, this.getEntityBoundingBox().expand(10D, 10D, 10D));
//		for (Object ent : list){
//			//System.out.println("Checking for leader");
//			if (((EntityTropicalFish)ent).getColor() == this.getColor()) {
//				if (getEntityId() > ((Entity)ent).getEntityId()) {
//					leader = (EntityTropicalFish)ent;
//					setIsLeader(false);
//				} else {
//					setIsLeader(true);
//
//				}
//			}
//		}
//	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setBoolean("Placed", hasBeenPlaced);
		nbttagcompound.setInteger("Color", Integer.valueOf(getColor()));
		nbttagcompound.setBoolean("isLeader", getIsLeader());
		super.writeEntityToNBT(nbttagcompound);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		//  color = nbttagcompound.getShort("Color");
		// Following conditional is to prevent fish despawning if the world
		// was created prior to introduction of the placed flag
		if (nbttagcompound.hasKey("Placed")) {
			hasBeenPlaced = nbttagcompound.getBoolean("Placed");
		} else {
			hasBeenPlaced = true;
		}

		setShouldSpawnSchool(false);
		setColor(Integer.valueOf(nbttagcompound.getInteger("Color")));
		setIsLeader(Boolean.valueOf(nbttagcompound.getBoolean("isLeader")));
		super.readEntityFromNBT(nbttagcompound);
	}

	@Override
	public EntityLivingBase getAttackTarget() {
		return null;
	}

	public void checkForHook(){
		List<EntityFishHook> list = worldObj.getEntitiesWithinAABB(EntityFishHook.class, this.getEntityBoundingBox().expand(10, 10, 10));
		if (list.isEmpty()){
			targetHook = false;
			hook = null;
			return;
		}
		hook = (EntityFishHook)(list.get(0));
		targetHook = true;
	}

	@Override
	public void applyEntityCollision(Entity entity) {        
		super.applyEntityCollision(entity);
		//		if (targetEntity != null && entity instanceof EntityTropicalFish) {
		//			targetEntity = null;
		//			inSchool = true;
		//		}       
	}

	@Override
	public double getYOffset() {
		return 0.0D;
	}
	//
	//	@Override
	//	public boolean interact(EntityPlayer entityplayer) {
	//		if (entityplayer.getCurrentEquippedItem() == null || entityplayer.getCurrentEquippedItem().getItem() != ItemRegistry.fishingNet) {
	//			return false;
	//		}
	//
	//		if (!entityplayer.inventory.hasItem(ItemRegistry.bucketTropicsWater)) {
	//			return false;
	//		} else {
	//			for (int i = 0 ; i < entityplayer.inventory.mainInventory.length; i ++ ) {
	//				if (entityplayer.inventory.getStackInSlot(i) != null) {
	//					if (entityplayer.inventory.getStackInSlot(i).getItem() == TCItemRegistry.bucketTropicsWater) {
	//						entityplayer.inventory.mainInventory[i] = new ItemStack(TCItemRegistry.fishBucket, 1, getColor());
	//						this.setDead();
	//						entityplayer.swingItem();
	//						return true;                        
	//					}
	//				}
	//			}
	//		}
	//
	//		return false;
	//	}

	//	@Override
	//	protected void updateEntityActionState()
	//	{
	//		if (getShouldSpawnSchool()) {
	//			// Note: min/max values include this fish
	//			int maxInSchool = 7;
	//			int minInSchool = 4;
	//			int numToSpawn = (new Random()).nextInt(1 + maxInSchool - minInSchool) + minInSchool - 1;
	//			for (int i = 0; i < numToSpawn; i++) {
	//				if (!worldObj.isRemote) {
	//					continue;
	//					//EntityTropicalFish fish = new EntityTropicalFish(this);
	//					//worldObj.spawnEntityInWorld(fish);
	//				}
	//			}
	//			setShouldSpawnSchool(false);
	//		}
	//
	//
	//		if (leader != null){         
	//			if(getDistanceToEntity(leader) < 1.5F){
	//				inSchool = true;                
	//			}           
	//		}
	//		if (leader != null && leader.isDead){
	//			leader = null;
	//		}
	//		if (leader == null || isLeader){
	//			checkForLeader();
	//		}
	//
	//
	//		if (!inSchool || isLeader){
	//			super.updateEntityActionState();
	//		} else if(inSchool && leader != null){
	//
	//			if (getDistanceToEntity(leader)>= 2.25F && ticksExisted % 40 == 0){
	//				inSchool = false;
	//			}
	//
	//			if (!leader.isLeader && leader.leader != null){
	//				leader = leader.leader;
	//			}
	//			randomMotionVecX = leader.randomMotionVecX;         
	//			randomMotionVecY = leader.randomMotionVecY;         
	//			randomMotionVecZ = leader.randomMotionVecZ;
	//		}
	//	}

	//	@Override
	//	protected int attackStrength() {
	//		return 0;
	//	}

	@Override
	public boolean canDespawn() {
		return !hasBeenPlaced;
	}

	public void disableDespawning() {
		hasBeenPlaced = true;
	}
}