package train.common.api;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ebf.XmlBuilder;
import ebf.tim.api.SkinRegistry;
import fexcraft.tmt.slim.ModelBase;
import io.netty.buffer.ByteBuf;
import mods.railcraft.api.carts.IMinecart;
import mods.railcraft.api.carts.IRoutableCart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import train.client.render.Bogie;
import train.client.render.TransportRenderCache;
import train.common.Traincraft;
import train.common.adminbook.ItemAdminBook;
import train.common.core.handlers.ConfigHandler;
import train.common.core.handlers.TrainHandler;
import train.common.core.util.DepreciatedUtil;
import train.common.items.ItemChunkLoaderActivator;
import train.common.items.ItemRollingStock;
import train.common.items.ItemWrench;
import train.common.library.Info;
import train.common.library.TraincraftRegistry;
import train.common.overlaytexture.OverlayTextureManager;

import java.util.*;

public abstract class AbstractTrains extends EntityMinecart implements IMinecart, IRoutableCart, IEntityAdditionalSpawnData {

    public boolean isAttached = false;
    public boolean isAttaching = false;
    public static int numberOfTrains;
    public EntityPlayer playerEntity;
    public double Link1;
    public double Link2;
    protected boolean linked = false;
    public AbstractTrains cartLinked1;
    public AbstractTrains cartLinked2;
    //private Set chunks;
    protected Ticket chunkTicket;
    public float renderYaw;
    protected float renderPitch;
    public float serverRealRotation;
    public TrainHandler train;
    public List<ChunkCoordIntPair> loadedChunks = new ArrayList<>();
    public boolean shouldChunkLoad = true;
    protected boolean itemdropped = false;

    public XmlBuilder entity_data = new XmlBuilder();
    public TransportRenderCache render_cache=new TransportRenderCache();

    public EntityBogie bogieFront=null;
    public EntityBogie bogieBack=null;

    public ArrayList<AbstractTrains> consist;
    /**
     * A reference to EnumTrains containing all spec for this specific train
     */
    private TrainRecord trainSpec = null;

    private TrainRenderRecord render = null;

    public TrainRecord getSpec() {
        if(trainSpec==null){
            trainSpec = Traincraft.instance.traincraftRegistry.getTrainRecord(this.getClass());
            //fallback if that failed
            if (trainSpec == null) {
                Traincraft.instance.traincraftRegistry.findTrainRecordByItem(getCartItem().getItem());
            }
        }
        return trainSpec;
    }

    @SideOnly(Side.CLIENT)
    public TrainRenderRecord getRender() {
        if (render == null) {
            render = Traincraft.instance.traincraftRegistry.getTrainRenderRecord(this.getClass());
        }
        return render;
    }

    //@Override
    // public boolean shouldRenderInPass(int pass){return pass==1;}
    /**
     * The name of the train based on the item name
     */
    public String trainName = "";
    public double accelerate = 0.7D;
    public double brake = 0.96D;
    /**
     * determines the mass of the carts from 0 to 10 it's then multiplied by 10
     * to pretend this is [tons]
     */
    public double mass = 1;
    /**
     * the default mass, not affected by weight of items/liquids
     */
    public double defaultMass = 1;
    /**
     * the power of locomotives, 0 for carts
     */
    public int power = 0;
    /**
     * Whether this train is locked and can only be used by the Owner
     */
    public boolean locked = false;
    /**
     * The owner of the train: The user who spawned it
     */
    public String trainOwner = "";

    public String getTrainOwner() {
        return trainOwner;
    }


    public void setTrainOwner(String trainOwner) {
        this.trainOwner = trainOwner;
    }

    /**
     * The creator of the train
     */
    public String trainCreator = "";

    /**
     * player who destroyed the train
     */
    protected String trainDestroyer = "";

    /**
     * unique ID for a train. ID is create when item is created. This allows to
     * track a train not only in his entity form
     */
    public int uniqueID = -1;
    /**
     * supposed to store the last ID given;
     */
    public static int uniqueIDs = 1;


    /**
     * The distance this train has traveled
     */
    public double trainDistanceTraveled = 0;

    public String destination = "";
    public final Map<String, TextureDescription> textureDescriptionMap = new HashMap<>();
    private OverlayTextureManager overlayTextureContainer;
    private boolean acceptsOverlayTextures = false;


    public AbstractTrains(World world) {
        super(world);
        if(world==null){return;}
        renderDistanceWeight = 2.0D;
        entity_data.putString("color", SkinRegistry.get(this).size()>0 ? SkinRegistry.get(this).get(0) : "");
        dataWatcher.addObject(12, entity_data.toXMLString());
        dataWatcher.addObject(7, trainOwner);
        dataWatcher.addObject(8, trainDestroyer);
        dataWatcher.addObject(9, trainName);
        dataWatcher.addObject(10, numberOfTrains);
        dataWatcher.addObject(11, uniqueID);
        dataWatcher.addObject(13, trainCreator);
        shouldChunkLoad = ConfigHandler.CHUNK_LOADING;
        this.setFlag(7, shouldChunkLoad);


        if (getSpec() != null) {
            this.setDefaultMass(weightKg()*0.1);
            this.setSize(0.98f, 1.98f);
            this.setMinecartName(transportName());
        }
    }

    public AbstractTrains(World world, double x, double y, double z) {
        this(world);
        this.setPosition(x, y, z);
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity p_70114_1_) {
        if (riddenByEntity != p_70114_1_) {
            return super.getCollisionBox(p_70114_1_);
        } else {
            return null;
        }
    }

    public String getTrainType(){
        return TraincraftRegistry.findTrainType(this);
    }

    /**
     * this is basically NBT for entity spawn, to keep data between client and server in sync because some data is not automatically shared.
     */
    @Override
    public void readSpawnData(ByteBuf additionalData) {
        locked = additionalData.readBoolean();
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeBoolean(locked);
    }


    public abstract boolean isLocomotive();

    public abstract boolean isPassenger();

    public abstract boolean isFreightCart();

    public abstract boolean isFreightOrPassenger();

    public abstract boolean isBuilder();

    public abstract boolean isTender();

    public abstract boolean isWorkCart();

    public abstract boolean isElectricTrain();

    public abstract boolean isLinked();

    protected abstract boolean canOverheat();

    protected abstract int getOverheatTime();

    public float getLinkageDistanceFront(EntityMinecart cart){return 0.0f;}

    public float getLinkageDistanceBack(EntityMinecart cart){return 0.0f;}

    public abstract float getLinkageDistance(EntityMinecart cart);

    //public abstract int getID();

    public abstract boolean canBeAdjusted(EntityMinecart cart2);

    public abstract float getOptimalDistance(EntityMinecart cart2);

    public abstract List<ItemStack> getItemsDropped();

    public int getUniqueTrainID() {
        return uniqueID;
    }

    @Override
    public void setDead() {
        ForgeChunkManager.releaseTicket(chunkTicket);
    }

    public int setNewUniqueID(int numberOfTrains) {
        // System.out.println(numberOfTrains);
        if (numberOfTrains <= 0) {
            numberOfTrains = uniqueIDs++;
        } else {
            uniqueIDs = numberOfTrains++;
        }
        this.uniqueID = numberOfTrains;
        getEntityData().setInteger("uniqueID", numberOfTrains);
        // System.out.println("setting new ID "+uniqueID);
        return numberOfTrains;
    }

    @Override
    public boolean interactFirst(EntityPlayer entityplayer) {
        ItemStack itemstack = entityplayer.inventory.getCurrentItem();
        if (!worldObj.isRemote && ConfigHandler.CHUNK_LOADING && (this instanceof Locomotive)) {
            if (itemstack != null && itemstack.getItem() instanceof ItemChunkLoaderActivator) {
                this.playerEntity = entityplayer;
                if (getFlag(7)) {
                    this.setFlag(7, false);
                    entityplayer.addChatMessage(new ChatComponentText("Stop loading chunks"));
                    ForgeChunkManager.releaseTicket(chunkTicket);
                    chunkTicket = null;
                } else if (!getFlag(7)) {
                    this.setFlag(7, true);
                    entityplayer.addChatMessage(new ChatComponentText("Start loading chunks"));
                }
                itemstack.damageItem(1, entityplayer);
                return true;
            } else if (lockThisCart(itemstack, entityplayer)) {
                return true;
            }
        }
        return false;
    }

    /**
     * set the color of the rollingstock
     *
     * @param color
     * @see ItemRollingStock
     */
	/*public void setColor(int color) {
		if (color==-1 && EnumTrains.getCurrentTrain(getCartItem().getItem()).getColors()!=null){
			color = getColorFromString(EnumTrains.getCurrentTrain(getCartItem().getItem()).getColors()[0]);
		}
		dataWatcher.updateObject(12, color);
	}*/
	/*public void setColor(int color) {
		if (EnumTrains.getCurrentTrain(getCartItem().getItem()).getColors()!=null){
			if (color==-1 || !ArrayUtils.contains(EnumTrains.getCurrentTrain(getCartItem().getItem()).getColors(),(byte)color)) {
				color = (EnumTrains.getCurrentTrain(getCartItem().getItem()).getColors()[0]);
			}
		}
		dataWatcher.updateObject(12, color);
		this.getEntityData().setInteger("color", color);
	}*/
    public void setColor(int color) {
        TrainRecord trainRecord = Traincraft.instance.traincraftRegistry.findTrainRecordByItem(getCartItem().getItem());
        if (trainRecord != null && !trainRecord.getLiveries().isEmpty()) {
            if (color == -1 || !trainRecord.getLiveries().contains(DepreciatedUtil.getColorAsString(color))) {
                color = color+1>trainRecord.getLiveries().size()-1?0:color+1;
            }
            entity_data.putString("color", trainRecord.getLiveries().get(color));
        }
        dataWatcher.updateObject(12, entity_data.toXMLString());
        this.getEntityData().setString("xml", entity_data.toXMLString());
    }

    public void setColor(String color) {
        if (SkinRegistry.get(this) != null && SkinRegistry.get(this).size()>0) {
            if (color.equals("-1") || !SkinRegistry.get(this).contains(color)) {
                color = (SkinRegistry.get(this).get(SkinRegistry.get(this).indexOf(color)+1>SkinRegistry.get(this).size()-1?0:SkinRegistry.get(this).indexOf(color)+1));
            }
        }

        entity_data.putString("color", color);
        dataWatcher.updateObject(12, entity_data.toXMLString());
        this.getEntityData().setString("xml", entity_data.toXMLString());
    }

    public void setRenderYaw(float yaw) {
        this.renderYaw = yaw;
    }

    public void setRenderPitch(float pitch) {
        this.renderPitch = pitch;
    }

    public String getColor() {
        if (worldObj != null) {
            entity_data.updateData(dataWatcher.getWatchableObjectString(12));
            if (entity_data.hasString("color")) {
                return entity_data.getString("color");
            }
        }
        return SkinRegistry.get(this).get(0);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        //super.writeEntityToNBT(nbttagcompound);
        if (!getColor().isEmpty()) {
            nbttagcompound.setString("colorstr", getColor());
        }
        nbttagcompound.setBoolean("chunkLoadingState", getFlag(7));
        nbttagcompound.setDouble("trainDistanceTraveled", trainDistanceTraveled);
        nbttagcompound.setString("theOwner", trainOwner);
        nbttagcompound.setBoolean("locked", locked);
        nbttagcompound.setString("theCreator", trainCreator);
        nbttagcompound.setString("theName", trainName);
        nbttagcompound.setInteger("uniqueID", uniqueID);
        //nbttagcompound.setInteger("uniqueIDs",uniqueIDs);

        nbttagcompound.setInteger("numberOfTrains", AbstractTrains.numberOfTrains);
        nbttagcompound.setBoolean("isAttached", this.isAttached);
        nbttagcompound.setBoolean("linked", this.linked);
        //nbttagcompound.setDouble("motionX", motionX);
        //nbttagcompound.setDouble("motionZ", motionZ);
        nbttagcompound.setTag("Motion", this.newDoubleNBTList(this.motionX, this.motionY, this.motionZ));
        nbttagcompound.setDouble("Link1", Link1);
        nbttagcompound.setDouble("Link2", Link2);

        nbttagcompound.setInteger("Dim", this.dimension);

        nbttagcompound.setLong("UUIDM", this.getUniqueID().getMostSignificantBits());
        nbttagcompound.setLong("UUIDL", this.getUniqueID().getLeastSignificantBits());
        nbttagcompound.setBoolean("acceptsOverlayTextures", acceptsOverlayTextures);
        if (acceptsOverlayTextures) {
            nbttagcompound.setTag("overlayTextureConfigTag", overlayTextureContainer.getOverlayConfigTag());
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        //super.readEntityFromNBT(nbttagcompound);
        if (nbttagcompound.hasKey("color")) {
            setColor(nbttagcompound.getInteger("color"));
        } else if (nbttagcompound.hasKey("colorstr")) {
            setColor(nbttagcompound.getString("colorstr"));
        }
        setFlag(7, nbttagcompound.getBoolean("chunkLoadingState"));
        trainDistanceTraveled = nbttagcompound.getDouble("trainDistanceTraveled");
        trainOwner = nbttagcompound.getString("theOwner");
        this.locked = nbttagcompound.getBoolean("locked");
        setFlag(8, locked);
        trainCreator = nbttagcompound.getString("theCreator");
        trainName = nbttagcompound.getString("theName");
        uniqueID = nbttagcompound.getInteger("uniqueID");
        //uniqueIDs = nbttagcompound.getInteger("uniqueIDs");
        setInformation(trainOwner, trainCreator, trainName, uniqueID);

        numberOfTrains = nbttagcompound.getInteger("numberOfTrains");
        isAttached = nbttagcompound.getBoolean("isAttached");
        linked = nbttagcompound.getBoolean("linked");
        //motionX = nbttagcompound.getDouble("motionX");
        //motionZ = nbttagcompound.getDouble("motionZ");
        NBTTagList nbttaglist1 = nbttagcompound.getTagList("Motion", 6);            this.motionX = nbttaglist1.func_150309_d(0);
        this.motionX = nbttaglist1.func_150309_d(0);
        this.motionZ = nbttaglist1.func_150309_d(2);
        Link1 = nbttagcompound.getDouble("Link1");
        Link2 = nbttagcompound.getDouble("Link2");
        if(nbttagcompound.hasKey("Dim")){
            this.dimension=nbttagcompound.getInteger("Dim");
        }
        if(nbttagcompound.hasKey("UUIDM")){
            this.entityUniqueID = new UUID(nbttagcompound.getLong("UUIDM"), nbttagcompound.getLong("UUIDL"));
        }
        if (nbttagcompound.getBoolean("acceptsOverlayTextures")) {
            acceptsOverlayTextures = true;
            overlayTextureContainer.importFromConfigTag(nbttagcompound.getCompoundTag("overlayTextureConfigTag"));
        }
    }

    @Override
    public boolean writeMountToNBT(NBTTagCompound tag) {
        return false;
    }

    @Override
    public boolean writeToNBTOptional(NBTTagCompound p_70039_1_) {
        if (!this.isDead && this.getEntityString() != null) {
            p_70039_1_.setString("id", this.getEntityString());
            this.writeToNBT(p_70039_1_);
            return true;
        }
        return false;
    }

    public void setInformation(String trainOwner, String trainCreator, String trainName, int uniqueID) {
        if (!worldObj.isRemote) {
            dataWatcher.updateObject(7, trainOwner);
            dataWatcher.updateObject(9, trainName);
            dataWatcher.updateObject(11, uniqueID);
            if (trainCreator != null && !trainCreator.isEmpty()) {
                dataWatcher.updateObject(13, trainCreator);
            }
        }
    }

    public void dropCartAsItem(boolean isCreative) {
        if (!isCreative && !itemdropped) {
            itemdropped = true;
            for (ItemStack item : getItemsDropped()) {
                if (item.getItem() instanceof ItemRollingStock) {
                    ItemStack stack = ItemRollingStock.setPersistentData(item, this, this.getUniqueTrainID(), trainCreator, trainOwner, getColor());
                    entityDropItem(stack != null ? stack : item, 0);
                } else {
                    setUniqueIDToItem(item);
                    entityDropItem(item, 0);
                }
            }
        }
    }

    protected void setUniqueIDToItem(ItemStack stack) {
        NBTTagCompound var3 = stack.getTagCompound();
        if (var3 == null) {
            var3 = new NBTTagCompound();
            stack.setTagCompound(var3);
        }
        if (this.uniqueID != -1) stack.getTagCompound().setInteger("uniqueID", this.uniqueID);
        if (this.trainCreator != null && !this.trainCreator.isEmpty()) stack.getTagCompound().setString("trainCreator", this.trainCreator);
        stack.getTagCompound().setString("trainColor", this.getColor());
        // Only save the overlay configuration to NBT if it exists. No need to store an empty configuration in NBT as it will be initialized as the default when the entity spawns in.
        if (this.acceptsOverlayTextures && this.getOverlayTextureContainer().getType() != OverlayTextureManager.Type.NONE) {
            stack.getTagCompound().setTag("overlayTextureConfigTag", getOverlayTextureContainer().getOverlayConfigTag());
        }
    }

    protected void setDefaultMass(double def) {
        this.mass = def;
        this.defaultMass = def;
    }

    protected double getDefaultMass() {
        return defaultMass;
    }

    //this is only for locomotive GUI stuff
    public double getSpeed() {
        return 0;
    }
    /**
     * Lock packet
     */
    public boolean getTrainLockedFromPacket() {
        return locked;
    }

    /**
     * Lock packet
     */
    public void setTrainLockedFromPacket(boolean set) {
        // System.out.println(worldObj.isRemote + " " + set);
        locked = set;
    }


    /**
     * Locking for passengers, flat, caboose, jukebox,workcart
     */
    protected boolean lockThisCart(ItemStack itemstack, EntityPlayer entityplayer) {
        if (itemstack != null && (itemstack.getItem() instanceof ItemWrench || itemstack.getItem() instanceof ItemAdminBook)) {
            if (entityplayer.getDisplayName().equals(this.trainOwner) || entityplayer.getGameProfile().getName().equals(this.trainOwner)
                    || this.trainOwner.isEmpty() || entityplayer.canCommandSenderUseCommand(2, "")) {
                if (locked) {
                    locked = false;
                    if (worldObj.isRemote) {
                        entityplayer.addChatMessage(new ChatComponentText("Unlocked."));
                    }
                } else {
                    locked = true;
                    if (worldObj.isRemote) {
                        entityplayer.addChatMessage(new ChatComponentText("Locked."));
                    }
                }
            } else if (worldObj.isRemote) {
                entityplayer.addChatMessage(new ChatComponentText("You are not the owner!"));
            }
            return true;
        }
        return false;
    }

    protected boolean canBeRiddenWhileLocked(AbstractTrains train) {
        return (train instanceof Locomotive) || (train instanceof IPassenger) || (train instanceof AbstractWorkCart);
    }

    protected boolean canBeDestroyedByPlayer(DamageSource damagesource) {
        if (this.getTrainLockedFromPacket()) {
            if (damagesource.getEntity() instanceof EntityPlayer) {
                if ((damagesource.getEntity() instanceof EntityPlayerMP) &&
                        ((EntityPlayerMP) damagesource.getEntity()).canCommandSenderUseCommand(2, "") &&
                        ((EntityPlayer) damagesource.getEntity()).inventory.getCurrentItem() != null &&
                        ((EntityPlayer) damagesource.getEntity()).inventory.getCurrentItem().getItem() instanceof ItemWrench) {

                    ((EntityPlayer) damagesource.getEntity()).addChatMessage(new ChatComponentText("Removing the train using OP permission"));
                    return false;
                } else if (!((EntityPlayer) damagesource.getEntity()).getDisplayName().equalsIgnoreCase(this.trainOwner)) {
                    ((EntityPlayer) damagesource.getEntity()).addChatMessage(new ChatComponentText("You are not the owner!"));
                    return true;
                }
            } else return !damagesource.isProjectile();
        }
        return false;
    }

    /**
     * Railcraft routing integration
     */
    @Override
    public boolean doesCartMatchFilter(ItemStack stack, EntityMinecart cart) {
        if (stack == null || cart == null) {
            return false;
        }
        ItemStack cartItem = cart.getCartItem();
        return cartItem.getItem() == stack.getItem();
    }

    @Override
    public String getDestination() {
        if (destination == null) return "";
        return destination;
    }

    /**
     * Only locomotives can receive a destination from a track. It is then
     * transmitted to attached carts
     */
    @Override
    public boolean setDestination(ItemStack ticket) {
        return false;
    }

    public static String getTicketDestination(ItemStack ticket) {
        if ((ticket == null)) {
            return "";
        }
        NBTTagCompound nbt = ticket.getTagCompound();
        if (nbt == null) {
            return "";
        }
        return nbt.getString("dest");
    }


    @Override
    public String getCommandSenderName() {
        String s = EntityList.getEntityString(this);
        if (s == null) {
            s = "generic";
        }

        return StatCollector.translateToLocal("entity." + s + ".name");
    }


    public void setTicket(ForgeChunkManager.Ticket ticket) {
        this.chunkTicket = ticket;
    }

    public ForgeChunkManager.Ticket getTicket() {
        return this.chunkTicket;
    }

    public void requestTicket() {
        ForgeChunkManager.Ticket chunkTicket = ForgeChunkManager.requestTicket(Traincraft.instance, worldObj, ForgeChunkManager.Type.ENTITY);
        if (chunkTicket != null) {
            chunkTicket.setChunkListDepth(25);
            chunkTicket.bindEntity(this);
            this.setTicket(chunkTicket);
        }
    }

    public String getPersistentUUID() {
        if (getEntityData().hasKey("puuid")) {
            return getEntityData().getString("puuid");
        } else {
            getEntityData().setString("puuid", getUniqueID().toString());
            return this.getUniqueID().toString();
        }
    }

    /**
     * @author 02skaplan
     * <p>Called to setup the overlay texture manager for the given AbstractTrain. It is recommended
     * to call this from the constructor of the AbstractTrain-derived entity class.</p>
     * <p>After calling, it is recommended to use getOverlayTextureContainer to initialze the fixed, dynamic, or both
     * fixed and dynamic overlays with their respective settings.</p>
     * @param acceptedType Whether the overlay manager will allow fixed, dynamic, or both fixed and dynamic overlays.
     */
    public void initOverlayTextures(OverlayTextureManager.Type acceptedType) {
        overlayTextureContainer = new OverlayTextureManager(acceptedType, this);
        acceptsOverlayTextures = true;
    }

    public OverlayTextureManager getOverlayTextureContainer() {
        return overlayTextureContainer;
    }

    public boolean acceptsOverlayTextures() {
        return acceptsOverlayTextures;
    }


    public Item getItem(){return getSpec().getItem();}


    /**
     * This function returns an ItemStack that represents this cart. This should
     * be an ItemStack that can be used by the player to place the cart. This is
     * the item that was registered with the cart via the registerMinecart
     * function, but is not necessary the item the cart drops when destroyed.
     *
     * @return An ItemStack that can be used to place the cart.
     */
    @Override
    public ItemStack getCartItem() {
        return new ItemStack(getItem());
    }
    /**
     * Functionality imported from TC5
     */

    public String transportName(){return getSpec().getName();}

    public String transportcountry(){return "";}

    public String transportYear(){return "";}

    public String transportFuelType(){
        if(this instanceof SteamTrain) {
            return "Steam";
        } else if(this instanceof DieselTrain) {
            return "Diesel";
        } else if(this instanceof ElectricTrain) {
            return "Electric";
        }

        return "";
    }

    public String transportFreightType(){return "";}

    public boolean isFictional(){return false;}

    public String[] additionalItemText(){return getSpec()==null?null:getSpec().getAdditionnalTooltip().split("\n");}
    /**the top speed in km/h for the transport.
     * not used tor rollingstock.*/
    public float transportTopSpeed(){return getSpec().getMaxSpeed();}
    /**the top speed in km/h for the transportwhen moving in reverse, default is half for diesel and 75% for others.
     * not used tor rollingstock.*/
    public float transportTopSpeedReverse(){return this instanceof DieselTrain?transportTopSpeed()*0.5f:transportTopSpeed();}
    /**this is the default value to define the acceleration speed and pulling power of a transport.*/
    public float transportMetricHorsePower(){return getSpec().getMHP();}
    /**the tractive effort for the transport, this is a fallback if metric horsepower (mhp) is not available*/
    public float transportTractiveEffort(){return 0;}

    /**defines the size of the inventory row by row, not counting any special slots like for fuel.
     * end result number of slots is this times 9. plus any crafting/fuel slots
     * may not return null*/
    public int getInventoryRows(){return 0;}

    /**defines the capacity of the fluidTank tank.
     * each value defibes another tank.
     * Usually value is 1,000 *the cubic meter capacity, so 242 gallons, is 0.9161 cubic meters, which is 916.1 tank capacity
     * mind you one water bucket is values at 1000, a full cubic meter of water.
     *example:
     * return new int[]{11000, 1000};
     * may return null*/
    public int[] getTankCapacity(){return getSpec()==null?new int[]{0}:new int[]{getSpec().getTankCapacity()};}

    /**defines the rider position offsets, with 0 being the center of the entity.
     * Each set of coords represents a new rider seat, with the first one being the "driver"
     * example:
     * return new float[][]{{x1,y1,z1},{x2,y2,z2}, etc...};
     * may return null*/
    public float[][] getRiderOffsets(){return null;}


    /**returns the size of the hitbox in blocks.
     * example:
     * return new float[]{x,y,z};
     * may not return null*/
    public float[] getHitboxSize(){return new float[]{getOptimalDistance(null)-((float)rotationPoints()[0]*0.5f),1.5f,0.21f};}

    /**defines if the transport is immune to explosions*/
    public boolean isReinforced(){return false;}


    /**defines the weight of the transport.*/
    public float weightKg(){return (float)getSpec().getMass()*10f;}

        /*
    <h1>Bogies and models</h1>
    */

    /**returns a list of models to be used for the bogies
     * example:
     * return new Bogie[]{new Bogie(new MyModel1(), offset), new Bogie(new MyModel2(), offset2), etc...};
     * may return null. */
    public Bogie[] bogies(){
        return new Bogie[]{null};
    }

    /**defines the points that the entity uses for path-finding and rotation, with 0 being the entity center.
     * Usually the point where the front and back bogies would connect to the transport.
     * Or the center of the frontmost and backmost wheel if there are no bogies.
     * The first value is the back point, the second is the front point
     * example:
     * return new float{2f, -1f};
     * may not return null*/
    public float[] rotationPoints(){
        if(getSpec()==null || getSpec().getBogieLocoPosition()==0){
            return new float[]{0.5f,-0.5f};
        }
        return new float[]{(float)getSpec().getBogieLocoPosition(),0};}

    /**defines the scale to render the model at. Default is 0.0625*/
    public float[][] getRenderScale(){return new float[][]{getRender().getScale()};}

    /**defines the scale to render the model at. Default is 0.65*/
    public float getPlayerScale(){return 0.65f;}

    /**returns the x/y/z offset each model should render at, with 0 being the entity center, in order with getModels
     * example:
     * return new float[][]{{x1,y1,z1},{x2,y2,z2}, etc...};
     * may return null.*/
    @SideOnly(Side.CLIENT)
    public float[][] modelOffsets(){return new float[][]{getRender().getTrans()};}


    /**returns the x/y/z rotation each model should render at in degrees, in order with getModels
     * example:
     * return new float[][]{{x1,y1,z1},{x2,y2,z2}, etc...};
     * may return null.*/
    @SideOnly(Side.CLIENT)
    public float[][] modelRotations(){return new float[][]{getRender().getRotate()};}

    /**event is to add skins for the model to the skins registry on mod initialization.
     * this function can be used to register multiple skins, one after another.
     * example:
     * SkinRegistry.addSkin(this.class, MODID, "folder/mySkin.png", new int[][]{{oldHex, newHex},{oldHex, newHex}, etc... }, displayName, displayDescription);
     * the int[][] for hex recolors may be null.
     * hex values use "0x" in place of "#"
     * "0xff00aa" as an example.
     * the first TransportSkin added to the registry for a transport class will be the default
     * additionally the addSkin function may be called from any other class at any time.
     * the registerSkins method is only for organization and convenience.*/
    public void registerSkins(){}

    /**
     * return the name for the default TransportSkin of the transport.
     */
    public String getDefaultSkin(){
        return SkinRegistry.get(this).get(0);
    }

    /**returns a list of models to be used for the transport
     * example:
     * return new MyModel();
     * may return null. */
    @SideOnly(Side.CLIENT)
    public ModelBase[] getModel(){return new ModelBase[]{getRender().getModel()};}


    public ArrayList<double[]> getSmokePosition() {return null;}

    public int[] getParticleData(int id) {
        switch (id){
            case 1: {return new int[]{1,200,0xFF0000};}
            default: {return new int[]{1,10,0xCCCC11};}
        }
    }

    public ItemStack[] getRecipe(){return null;}
    public int getTier(){return 1;}

    public TrainSound getHorn(){
        TrainSoundRecord sound = Traincraft.instance.traincraftRegistry.getTrainSoundRecord(this.getClass());
        if(sound != null && !sound.getHornString().isEmpty()){
            return new TrainSound(sound.getHornString(),sound.getHornVolume(),1f, 0);
        }
        return null;
    }

    public TrainSound getBell(){
        return new TrainSound(Info.resourceLocation + ":bell",0.5f,1f, 0);
    }

    public TrainSound getRunningSound(){
        TrainSoundRecord sound = Traincraft.instance.traincraftRegistry.getTrainSoundRecord(this.getClass());
        if(sound != null && !sound.getRunString().isEmpty()){
            if(sound.getSoundChangeWithSpeed()){
                return new TrainSound(sound.getRunString(), sound.getRunVolume(), 0.4f, sound.getRunSoundLength()).enableRunningPitch();
            } else {
                return new TrainSound(sound.getRunString(), sound.getRunVolume(), 0.4f, sound.getRunSoundLength());
            }
        }
        return null;
    }


    public TrainSound getIdleSound(){
        TrainSoundRecord sound = Traincraft.instance.traincraftRegistry.getTrainSoundRecord(this.getClass());
        if(sound != null && !sound.getIdleString().isEmpty()){
            return new TrainSound(sound.getIdleString(),sound.getIdleVolume(),0.001F, sound.getIdleSoundLength());
        }
        return null;
    }
}