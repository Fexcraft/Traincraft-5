package train.common.api;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ebf.tim.api.SkinRegistry;
import ebf.tim.entities.EntitySeat;
import ebf.tim.utility.DebugUtil;
import fexcraft.tmt.slim.Vec3f;
import io.netty.buffer.ByteBuf;
import mods.railcraft.api.carts.CartTools;
import mods.railcraft.api.carts.ILinkableCart;
import mods.railcraft.api.tracks.RailTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockRailBase;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.TraincraftEntityHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.minecart.MinecartCollisionEvent;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.event.entity.minecart.MinecartUpdateEvent;
import train.client.core.handlers.SoundUpdaterRollingStock;
import train.common.Traincraft;
import train.common.adminbook.ServerLogger;
import train.common.blocks.BlockTCRail;
import train.common.blocks.BlockTCRailGag;
import train.common.core.HandleOverheating;
import train.common.core.handlers.*;
import train.common.core.network.PacketRollingStockRotation;
import train.common.core.util.DepreciatedUtil;
import train.common.core.util.TraincraftUtil;
import train.common.entity.rollingStock.EntityTracksBuilder;
import train.common.items.*;
import train.common.library.BlockIDs;
import train.common.library.GuiIDs;
import train.common.tile.TileTCRail;
import train.common.tile.TileTCRailGag;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static train.common.core.util.TraincraftUtil.degrees;
import static train.common.core.util.TraincraftUtil.isRailBlockAt;

public class EntityRollingStock extends AbstractTrains implements ILinkableCart {
    public int fuelTrain;
    protected static final int[][][] matrix = {
            {{0, 0, -1}, {0, 0, 1}},
            {{-1, 0, 0}, {1, 0, 0}},
            {{-1, -1, 0}, {1, 0, 0}},
            {{-1, 0, 0}, {1, -1, 0}},
            {{0, 0, -1}, {0, -1, 1}},
            {{0, -1, -1}, {0, 0, 1}},
            {{0, 0, 1}, {1, 0, 0}},
            {{0, 0, 1}, {-1, 0, 0}},
            {{0, 0, -1}, {-1, 0, 0}},
            {{0, 0, -1}, {1, 0, 0}}};

    protected EntityPlayer playerEntity;

    /**
     * Axis aligned bounding box. this needs to be it's own thing because collisions
     */
    private AxisAlignedBB boundingBoxSmall;

    public float maxSpeed;
    public float railMaxSpeed;
    public double speedLimiter = 1;
    public boolean speedWasSet = false;

    public ItemStack item;
    public float rotation;
    public List<EntitySeat> seats = new LinkedList<>();

    public int rail;
    public int meta;
    public double d6;
    public double d7;

    /**
     * appears to be the progress of the turn
     */
    private int rollingturnProgress;
    private double rollingX;
    private double rollingY;
    private double rollingZ;
    private float rollingServerPitch;
    public double rotationYawClient;
    public float rotationYawClientReal;
    public float anglePitchClient;//was a double
    private float previousServerRealRotation;
    public boolean isServerInReverse = false;
    public boolean isClientInReverse = false;
    public boolean serverInReverseSignPositive = false;
    public float serverRealPitch;
    private double rollingPitch;
    public float oldClientYaw = 0;//used in rendering class
    @SideOnly(Side.CLIENT)
    private double rollingVelocityX;
    @SideOnly(Side.CLIENT)
    private double rollingVelocityY;
    @SideOnly(Side.CLIENT)
    private double rollingVelocityZ;

    private CollisionHandler collisionhandler;
    private LinkHandler linkhandler;
    private TrainsOnClick trainsOnClick;
    public boolean isBraking;
    public boolean isClimbing;
    public int overheatLevel;
    public int linkageNumber;

    public Side side;
    @SideOnly(Side.CLIENT)
    private SoundHandler theSoundManager;
    @SideOnly(Side.CLIENT)
    private SoundUpdaterRollingStock sndUpdater;
    /**
     * Array containing @TrainHandler objects. In other words it contains all
     * the "trains" object the train object contains an array which contains all @RollingStocks
     * that are part of the train
     */
    public static ArrayList<TrainHandler> allTrains = new ArrayList<TrainHandler>();
    private HandleOverheating handleOverheating;
    /**
     * each ticks: numLaps++ used for fuel consumption rate
     */
    private int numLaps;

    private int ticksSinceHeld = 0;
    private boolean cartLocked = false;

    /**
     * New physics integration
     */
    private double bogieShift = 0;
    private boolean needsBogieUpdate;
    private boolean firstLoad = true;
    private boolean hasSpawnedBogie = false;
    private double mountedOffset = -0.5;
    public double posYFromServer;
    private double derailSpeed = 0.46;

    private int scrollPosition;
    public TileTCRail lastTrack=null;
    public Vec3f[] cachedVectors = new Vec3f[]{
            new Vec3f(0,0,0),new Vec3f(0,0,0),new Vec3f(0,0,0),new Vec3f(0,0,0)};

    public EntityRollingStock(World world) {
        super(world);
        initRollingStock(world);
    }

    @Override
    public GameProfile getOwner() {
        return CartTools.getCartOwner(this);
    }

    public EntityRollingStock(World world, double d, double d1, double d2) {
        super(world, d, d1, d2);
        if(world==null){return;}
        initRollingStock(world);
        setPosition(d, d1 + yOffset, d2);
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        prevPosX = d;
        prevPosY = d1;
        prevPosZ = d2;
    }

    public void initRollingStock(World world) {
        dataWatcher.addObject(20, 0);//heat
        dataWatcher.addObject(14, 0);
        dataWatcher.addObject(21, 0);

        preventEntitySpawning = true;
        isImmuneToFire = true;
        //field_70499_f = false;

        setSize(0.98F, 1.98F);
        //yOffset = 0;
        //ySize = 0.98F;
        yOffset = 0.65f;

        linkageNumber = 0;

        entityCollisionReduction = 0.8F;

        boundingBoxSmall = AxisAlignedBB.getBoundingBox(0.0D, 0.0D, 0.0D, 0.0D, 2.0D, 1.0D);
        //setBoundingBoxSmall(0.0D, 0.0D, 0.0D, 0.98F, 0.7F);
        setBoundingBoxSmall(0.0D, 0.0D, 0.0D, 2.0F, 1.5F);
        consist = new ArrayList<AbstractTrains>();
        handleOverheating = new HandleOverheating(this);

        collisionhandler = new CollisionHandler(world);
        linkhandler = new LinkHandler(world);
        trainsOnClick = new TrainsOnClick();

        /* Railcraft's stuff */
        //maxSpeed = defaultMaxSpeedRail;
        //maxSpeedGround = defaultMaxSpeedGround;
        maxSpeedAirLateral = defaultMaxSpeedAirLateral;
        maxSpeedAirVertical = defaultMaxSpeedAirVertical;
        dragAir = defaultDragAir;

        /**
         * Trains are always rendered even if out player's sight => no more
         * flickering/disappearing
         */
        if (ConfigHandler.FLICKERING) {
            this.ignoreFrustumCheck = true;
        }
        side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.CLIENT) {
            sndUpdater = new SoundUpdaterRollingStock();
        }

        this.needsBogieUpdate = false;
        setCollisionHandler(null);
        //this.boundingBox.offset(0, 0.5, 0);
    }


    /**
     * this is basically NBT for entity spawn, to keep data between client and server in sync because some data is not automatically shared.
     */
    @Override
    public void readSpawnData(ByteBuf additionalData) {
        isBraking = additionalData.readBoolean();
        setTrainLockedFromPacket(additionalData.readBoolean());
        if (additionalData.readBoolean()) { // If accepts overlay textures...
            getOverlayTextureContainer().importFromConfigTag(ByteBufUtils.readTag(additionalData));
        }
    }
    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeBoolean(isBraking);
        buffer.writeBoolean(getTrainLockedFromPacket());
        buffer.writeBoolean(acceptsOverlayTextures());
        if (acceptsOverlayTextures()) {
            if (acceptsOverlayTextures()) {
                ByteBufUtils.writeTag(buffer, getOverlayTextureContainer().getOverlayConfigTag());
            }
        }
    }

    public String getTrainName() {
        return dataWatcher.getWatchableObjectString(9);
    }

    public String getTrainType() {
        return dataWatcher.getWatchableObjectString(6);
    }

    @Override
    public String getTrainOwner() {
        return dataWatcher.getWatchableObjectString(7);
    }

    public String getTrainCreator() {
        return dataWatcher.getWatchableObjectString(13);
    }

    public int getIDForServer() {
        return dataWatcher.getWatchableObjectInt(5);
    }

    public int getNumberOfTrainsForServer() {
        return dataWatcher.getWatchableObjectInt(10);
    }

    public int getUniqueTrainIDClient() {
        return dataWatcher.getWatchableObjectInt(11);
    }

    /*
     * @Override public int getID() { return ID; }
     */

    @Override
    public double getMountedYOffset() {
        return mountedOffset;
    }

    public void setMountedYOffset(double offset) {
        mountedOffset = offset;
    }

    public void setYFromServer(double posYServer) {
        this.posYFromServer = posYServer;
    }

    @Override
    protected void entityInit() {
        dataWatcher.addObject(16, (byte) 0);
        dataWatcher.addObject(17, 0);
        dataWatcher.addObject(18, 1);
        dataWatcher.addObject(19, 0.0F);
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity entity) {
        return null;
    }

    @Override
    public boolean canBePushed() {
        return true;
    }

    @Override
    public boolean isLocomotive() {
        return (this instanceof Locomotive);
    }

    @Override
    public boolean isPassenger() {
        return (this instanceof IPassenger);
    }

    @Override
    public boolean isFreightCart() {
        return (this instanceof Freight || this instanceof LiquidTank);
    }

    @Override
    public boolean isFreightOrPassenger() {
        return (this instanceof Freight || this instanceof IPassenger || this instanceof LiquidTank);
    }

    @Override
    public boolean isBuilder() {
        return (this instanceof EntityTracksBuilder);
    }

    @Override
    public boolean isTender() {
        return (this instanceof Tender);
    }

    @Override
    public boolean isWorkCart() {
        return (this instanceof AbstractWorkCart);
    }

    @Override
    public boolean isElectricTrain() {
        return (this instanceof ElectricTrain);
    }

    protected int steamFuelLast(ItemStack it) {
        return FuelHandler.steamFuelLast(it);
    }

    @Override
    public boolean attackEntityFrom(DamageSource damagesource, float i) {
        if (worldObj.isRemote || isDead) {
            return true;
        }
        if (damagesource.getEntity() instanceof EntityPlayer && !damagesource.isProjectile()) {
            if (this instanceof IPassenger) {
                if (canBeDestroyedByPlayer(damagesource)) return false;
            }
            setRollingDirection(-getRollingDirection());
            setRollingAmplitude(10);
            setBeenAttacked();
            if (((EntityPlayer) damagesource.getEntity()).capabilities.isCreativeMode) {
                this.setDamage(1000);
                if (ConfigHandler.ENABLE_WAGON_REMOVAL_NOTICES && ((EntityPlayer) damagesource.getEntity()).canCommandSenderUseCommand(2, "")) {
                    ((EntityPlayer) damagesource.getEntity()).addChatComponentMessage(new ChatComponentText("Operator removed train owned by " + getTrainOwner()));
                }
            }
            setDamage(getDamage() + i * 10);
            if (getDamage() > 40) {
/*                if (riddenByEntity != null) {
                    riddenByEntity.mountEntity(this);
                }*/ //#!#
                ServerLogger.deleteWagon(this);
                /**
                 * Destroy IPassenger since they don't extend Freight or
                 * Locomotive and don't have a proper attackEntityFrom() method
                 */
                if (this instanceof IPassenger) {
                    this.setDead();
                    dropCartAsItem(((EntityPlayer) damagesource.getEntity()).capabilities.isCreativeMode);
                }
            }
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void performHurtAnimation() {
        setRollingDirection(-getRollingDirection());
        setRollingAmplitude(10);
        setDamage(getDamage() + getDamage() * 10);
    }

    public void unLink() {
        if (this.isAttached) {
            if (this.cartLinked1 != null) {
                if (cartLinked1.Link1 == this.uniqueID) {
                    cartLinked1.Link1 = 0;
                    cartLinked1.cartLinked1 = null;
                    if (cartLinked1.consist != null) cartLinked1.consist.clear();

                } else if (cartLinked1.Link2 == this.uniqueID) {
                    cartLinked1.Link2 = 0;
                    cartLinked1.cartLinked2 = null;
                    if (cartLinked1.consist != null) cartLinked1.consist.clear();

                }
            }
            if (this.cartLinked2 != null) {
                if (cartLinked2.Link1 == this.uniqueID) {
                    cartLinked2.Link1 = 0;
                    cartLinked2.cartLinked1 = null;
                    if (cartLinked2.consist != null) cartLinked2.consist.clear();

                } else if (cartLinked2.Link2 == this.uniqueID) {
                    cartLinked2.Link2 = 0;
                    cartLinked2.cartLinked2 = null;
                    if (cartLinked2.consist != null) cartLinked2.consist.clear();

                }
            }
            this.cartLinked1 = null;
            this.cartLinked2 = null;
            this.isAttached = false;
        }
    }

    @Override
    public void setDead() {
        super.setDead();
        this.unLink();
        if (train != null) {
            if (train.getTrains() != null) {
                for (int i2 = 0; i2 < train.getTrains().size(); i2++) {
                    if ((train.getTrains().get(i2)) instanceof Locomotive) {
                        train.getTrains().get(i2).cartLinked1 = null;
                        train.getTrains().get(i2).Link1 = 0;
                        train.getTrains().get(i2).cartLinked2 = null;
                        train.getTrains().get(i2).Link2 = 0;
                    }
                    if ((train.getTrains().get(i2)) != this) {
                        if (train != null && train.getTrains() != null && train.getTrains().get(i2) != null && train.getTrains().get(i2).train != null && train.getTrains().get(i2).train.getTrains() != null) train.getTrains().get(i2).train.getTrains().clear();
                    }
                }
            }
        }
        if (train != null && train.getTrains().size() <= 1) {
            train.getTrains().clear();
            allTrains.remove(train);
        }
        if (this.bogieFront != null) {
            bogieFront.setDead();
            bogieFront.isDead = true;
        }
        if (this.bogieBack != null) {
            bogieBack.setDead();
            bogieBack.isDead = true;
        }
        isDead = true;
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.CLIENT) {
            soundUpdater();
        }
        //remove seats
        for (EntitySeat seat : seats) {
            seat.setDead();
            seat.getWorld().removeEntity(seat);
        }
    }


    @Override
    public boolean canBeCollidedWith() {
        return !isDead;
    }

    public void pressKey(int i) {
    }


    public float getPlayerScale() {
        return 1f;
    }

    /**
     * gets packet from server and distribute for GUI handles motion
     *
     * @param
     */
    public boolean isLockedAndNotOwner() {
        if (this.getTrainLockedFromPacket()) {
            if (this.riddenByEntity instanceof EntityPlayer && !((EntityPlayer) this.riddenByEntity).getDisplayName().equalsIgnoreCase(this.getTrainOwner())) {
                return true;
            }
            if (this.seats.size() > 0 && this.seats.get(0).getPassenger() instanceof EntityPlayer && !((EntityPlayer) this.seats.get(0).getPassenger()).getDisplayName().equalsIgnoreCase(this.getTrainOwner())) {
                return true;
            }
        }
        return false;
    }
    public void keyHandlerFromPacket(int i) {
        if (this.getTrainLockedFromPacket()) {
            if (isLockedAndNotOwner()) {
                return;
            }
        }
        this.pressKey(i);
        if (i == 7) {
            if (this instanceof AbstractWorkCart) {
                if (this.seats != null && this.seats.size() != 0 && this.seats.get(0).getPassenger() != null) {
                    ((EntityPlayer) this.seats.get(0).getPassenger()).openGui(Traincraft.instance, GuiIDs.CRAFTING_CART, worldObj, (int) this.posX, (int) this.posY, (int) this.posZ);
                } else {
                    playerEntity.openGui(Traincraft.instance,GuiIDs.CRAFTING_CART, worldObj, (int) this.posX, (int) this.posY, (int) this.posZ);
                }
            } else if (this.seats != null && this.seats.size() > 1 && this.getInventoryRows() == 0 && riddenByEntity != null && riddenByEntity instanceof EntityPlayer) {
                ((EntityPlayer) riddenByEntity).openGui(Traincraft.instance, GuiIDs.SEAT_GUI, worldObj, (int) this.posX, (int) this.posY, (int) this.posZ);
            }
        }
        if (i == 9) {
            if (this instanceof AbstractWorkCart) {
                if (this.seats != null && this.seats.size() != 0 && this.seats.get(0).getPassenger() != null) {
                    ((EntityPlayer) this.seats.get(0).getPassenger()).openGui(Traincraft.instance, GuiIDs.FURNACE_CART, worldObj, (int) this.posX, (int) this.posY, (int) this.posZ);
                } else {
                    playerEntity.openGui(Traincraft.instance,GuiIDs.FURNACE_CART, worldObj, (int) this.posX, (int) this.posY, (int) this.posZ);
                }
            }
        }


    }

    private void handleTrain() {
        if (this instanceof Locomotive && train != null) {
            for (int i2 = 0; i2 < train.getTrains().size(); i2++) {
                if (RailTools.isCartLockedDown(train.getTrains().get(i2))) {
                    cartLocked = true;
                    /** If something in the train is locked down */
                    ticksSinceHeld = 40;
                    if (!((Locomotive) this).canBeAdjusted) {
                        ((Locomotive) this).setCanBeAdjusted(true);

                    }
                }
                cartLocked = false;
            }
            if (ticksSinceHeld > 0 && !cartLocked) {
                ticksSinceHeld--;
            }
            if (ticksSinceHeld <= 0 && !cartLocked) {
                if (((Locomotive) this).canBeAdjusted && !((Locomotive) this).canBePulled) {
                    ((Locomotive) this).setCanBeAdjusted(false);

                }
            }
        }

        /*
         * if(train!=null && RailTools.isCartLockedDown((EntityMinecart) this)){
         * train.setTicksSinceHeld(100); train.setCartLocked(true); for(int
         * i2=0;i2<train.getTrains().size();i2++){ if(train.getTrains().get(i2)
         * instanceof Locomotive &&
         * !((Locomotive)train.getTrains().get(i2)).canBeAdjusted){
         * ((Locomotive)train.getTrains().get(i2)).setCanBeAdjusted(true);
         * System
         * .out.println(((Locomotive)train.getTrains().get(i2))+"canBeAdjusted=true"
         * ); } } }
         */

        /**
         * if the global train list is empty this is only used when the first @EntityRollingStock
         * is put down or when the world reloads
         */
        if (ticksExisted % 40 != 0) return;
        if (allTrains.isEmpty()) {
            if ((this.cartLinked1 != null || this.cartLinked2 != null)) {
                train = new TrainHandler(this);
            }
            /**
             * This is used when global train list isn't empty but this @EntityRollingStock
             * isn't part of a train yet
             */
        } else if (train == null || train.getTrains().isEmpty()) {
            if ((this.cartLinked1 != null || this.cartLinked2 != null)) {
                if (this.cartLinked1 != null && cartLinked1.train != null && cartLinked1.train.getTrains() != null && !cartLinked1.train.getTrains().isEmpty()) {
                    train = cartLinked1.train;
                    return;
                }
                if (this.cartLinked2 != null && cartLinked2.train != null && cartLinked2.train.getTrains() != null && !cartLinked2.train.getTrains().isEmpty()) {
                    train = cartLinked2.train;
                    return;
                }

                train = new TrainHandler(this);
            }
        }
        /**
         * getting main locomotive of the train and copying its destination to
         * all attached carts
         */
        if (train != null && train.getTrains().size() > 1) {
            if (this instanceof Locomotive && !((Locomotive) this).canBeAdjusted && !this.getDestination().isEmpty()) {
                for (int i = 0; i < train.getTrains().size(); i++) {
                    if (train.getTrains().get(i) != null && !train.getTrains().get(i).equals(this))
                        train.getTrains().get(i).destination = this.getDestination();
                    CartTools.setCartOwner(train.getTrains().get(i), CartTools.getCartOwner(this));
                }
            }
        }
        /**
         * Resets destination
         */
        else if (!(this instanceof Locomotive)) {
            destination = "";
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    /**
     * Sets the position and rotation. Only difference from the other one is no bounding on the rotation. Args: posX,
     * posY, posZ, yaw, pitch
     */
    public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9) {
        this.rollingX = par1;
        this.rollingY = par3;
        this.rollingZ = par5;
        this.rollingPitch = par8;
        this.rollingturnProgress = par9 + 2;
        this.motionX = this.rollingVelocityX;
        this.motionY = this.rollingVelocityY;
        this.motionZ = this.rollingVelocityZ;
    }

    List list = null;
    Block l;


    @Override
    public void onUpdate() {
        if (addedToChunk && !this.hasSpawnedBogie) {

            if (bogieFront == null) {
                this.bogieShift = this.rotationPoints()[0];
                this.bogieFront = new EntityBogie(worldObj,
                        (posX - Math.cos(this.serverRealRotation * TraincraftUtil.radian) * this.bogieShift),
                        posY + ((Math.tan(this.renderPitch * TraincraftUtil.radian) * -this.bogieShift) + getMountedYOffset() - 0.1d),
                        (posZ - Math.sin(this.serverRealRotation * TraincraftUtil.radian) * this.bogieShift), this, this.uniqueID, this.bogieShift);


                if (!worldObj.isRemote) worldObj.spawnEntityInWorld(bogieFront);

                this.bogieBack = new EntityBogie(worldObj,
                        (posX - Math.cos(this.serverRealRotation * TraincraftUtil.radian) * this.rotationPoints()[1]),
                        posY + ((Math.tan(this.renderPitch * TraincraftUtil.radian) * -this.rotationPoints()[1]) + getMountedYOffset() - 0.1d),
                        (posZ - Math.sin(this.serverRealRotation * TraincraftUtil.radian) * this.rotationPoints()[1]), this, this.uniqueID, this.rotationPoints()[1]);


                if (!worldObj.isRemote) worldObj.spawnEntityInWorld(bogieBack);
                this.needsBogieUpdate = true;
            }
            this.hasSpawnedBogie = true;
        }

        /**
         * manage chunkloading
         */
        if (!worldObj.isRemote && this.uniqueID == -1) {
            if (FMLCommonHandler.instance().getMinecraftServerInstance() != null) {

                setNewUniqueID(this.getEntityId());
            }
        }
        shouldChunkLoad = getFlag(7);
        if (shouldChunkLoad) {
            if (this.chunkTicket == null) {
                this.requestTicket();
            }
        }

        /**
         * Set the uniqueID if the entity doesn't have one.
         */
        if (!worldObj.isRemote && this.uniqueID == -1) {
            if (FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
                setNewUniqueID(this.getEntityId());
            }
        }
        if(ticksExisted % 18 == 0) { //just so we aren't doing it *every* tick, but still frequent enough to not let the player actually take damage
            if (seats.size() != 0) {
                for (EntitySeat seat : seats) {
                    if (seat.getPassenger() != null) {
                        seat.getPassenger().addPotionEffect(new PotionEffect(Potion.resistance.id, 20, 5, true));
                    }
                }
            } else {
                if (riddenByEntity instanceof EntityPlayer) {
                    ((EntityPlayer) riddenByEntity).addPotionEffect(new PotionEffect(Potion.resistance.id,20,5,true));
                }
            }
        }
        if (getRollingAmplitude() > 0) {
            setRollingAmplitude(getRollingAmplitude() - 1);
        }
        if (getDamage() > 0) {
            setDamage(getDamage() - 1);
        }

        isBraking = false;
        if (this.ticksExisted > 60) { //add a delay to spawn the seats so you don't bug out; has an issue where when the seats haven't spawned you can still get in the main entity
            if (getRiderOffsets() != null && getRiderOffsets().length > 0 && seats.size() < getRiderOffsets().length) {
                for (int i = 0; i < getRiderOffsets().length; i++) {
                    EntitySeat seat = new EntitySeat(getWorld(), posX, posY, posZ, getRiderOffsets()[i][0], getRiderOffsets()[i][1] + 1, getRiderOffsets()[i][2], this, i);
                    seats.add(seat);
                    if (i == 0) {
                        seats.get(i).setControlSeat();
                    }
                    getWorld().spawnEntityInWorld(seats.get(i));
                }
            }
        }
        if (seats.size() != 0 && worldObj.isRemote && Traincraft.proxy.getCurrentScreen() == null && seats.get(0).getPassenger() != null) {
            EntityLivingBase entity = seats.get(0).getPassenger();
            if (TraincraftEntityHelper.getIsJumping(entity)) isBraking = true;
        }

        int var2;
        if (!this.worldObj.isRemote && this.worldObj instanceof WorldServer) {
            this.worldObj.theProfiler.startSection("portal");
            MinecraftServer var1 = MinecraftServer.getServer();
            var2 = this.getMaxInPortalTime();

            if (this.inPortal) {
                if (var1.getAllowNether()) {
                    if (this.ridingEntity == null && this.portalCounter++ >= var2) {
                        this.portalCounter = var2;
                        this.timeUntilPortal = this.getPortalCooldown();
                        byte var3;

                        if (this.worldObj.provider.dimensionId == -1) {
                            var3 = 0;
                        } else {
                            var3 = -1;
                        }

                        this.travelToDimension(var3);
                    }

                    this.inPortal = false;
                }
            } else {
                if (this.portalCounter > 0) {
                    this.portalCounter -= 4;
                }

                if (this.portalCounter < 0) {
                    this.portalCounter = 0;
                }
            }

            if (this.timeUntilPortal > 0) {
                --this.timeUntilPortal;
            }

            this.worldObj.theProfiler.endSection();
        }

        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.CLIENT) {
            soundUpdater();
        }

        if (worldObj.isRemote) {
            //rotationYaw = (float) rotationYawClient;
            if (rollingturnProgress > 0) {
                rotationYaw = (float) rotationYawClient;
                this.rotationPitch = (float) (this.rotationPitch + (this.rollingPitch - this.rotationPitch) / this.rollingturnProgress);

                this.setPosition(this.posX + (this.rollingX - this.posX) / this.rollingturnProgress,
                        this.posY + (this.rollingY - this.posY) / this.rollingturnProgress,
                        this.posZ + (this.rollingZ - this.posZ) / this.rollingturnProgress);
                --this.rollingturnProgress;
                this.setRotation(this.rotationYaw, this.rotationPitch);

            } else {
                setPosition(posX, posY, posZ);
                setRotation(rotationYaw, rotationPitch);

            }
            if (seats.size() != 0) {
                for (int i = 0; i < seats.size(); i++) {
                    if (seats.get(i) != null) {
                        TraincraftUtil.updateRider(this, getRiderOffsets()[i][0], getRiderOffsets()[i][1] + 1, getRiderOffsets()[i][2], seats.get(i));
                    }
                }
            }
            return;
        }
        /**
         * As entities can't be registered in nbttagcompound I had to setup this
         * system... When world loads, only the (double) Link1 and Link2 are
         * known. This method search for the entity with the ID corresponding to
         * Link1 or Link2 When it finds it, (EntityRollingStock)cartLinked1 and
         * cartLinked2 will be updated accordingly
         */
        if (addedToChunk && ((this.cartLinked1 == null && this.Link1 != 0) || (this.cartLinked2 == null && this.Link2 != 0))) {
            list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(15, 15, 15));

            if (list != null && list.size() > 0) {
                for (Object entity : list) {
                    if (entity instanceof EntityRollingStock) {
                        if (((EntityRollingStock) entity).uniqueID == this.Link1) {
                            this.cartLinked1 = (EntityRollingStock) entity;
                        } else if (((EntityRollingStock) entity).uniqueID == this.Link2) {
                            this.cartLinked2 = (EntityRollingStock) entity;
                        }
                    }
                }
            }
        }

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        int floor_posX = MathHelper.floor_double(posX);
        int floor_posY = MathHelper.floor_double(posY);
        int floor_posZ = MathHelper.floor_double(posZ);

        if (needsBogieUpdate) {
            if (bogieFront != null) {
                float rotationCos1 = (float) Math.cos(Math.toRadians(serverRealRotation));
                float rotationSin1 = (float) Math.sin(Math.toRadians((serverRealRotation)));
                if (!firstLoad) {
                    rotationCos1 = (float) Math.cos(Math.toRadians(serverRealRotation + 90));
                    rotationSin1 = (float) Math.sin(Math.toRadians((serverRealRotation + 90)));
                }
                double bogieX1 = (this.posX + (rotationCos1 * Math.abs(bogieShift)));
                double bogieZ1 = (this.posZ + (rotationSin1 * Math.abs(bogieShift)));
                this.bogieFront.setPosition(bogieX1, bogieFront.posY, bogieZ1);

            }
            if (bogieBack != null) {
                float rotationCos1 = (float) Math.cos(Math.toRadians(serverRealRotation));
                float rotationSin1 = (float) Math.sin(Math.toRadians((serverRealRotation)));
                if (!firstLoad) {
                    rotationCos1 = (float) Math.cos(Math.toRadians(serverRealRotation + 90));
                    rotationSin1 = (float) Math.sin(Math.toRadians((serverRealRotation + 90)));
                }
                double bogieX1 = (this.posX + (rotationCos1 * Math.abs(rotationPoints()[1])));
                double bogieZ1 = (this.posZ + (rotationSin1 * Math.abs(rotationPoints()[1])));
                this.bogieBack.setPosition(bogieX1, bogieBack.posY, bogieZ1);

            }
            firstLoad = false;

            needsBogieUpdate = false;
        }
        if (bogieFront != null) {
            bogieFront.updateDistance();
        }
        if (bogieBack != null) {
            bogieBack.updateDistance();
        }

        if (worldObj.isAirBlock(floor_posX, floor_posY, floor_posZ)) {
            floor_posY--;
        } else if (isRailBlockAt(worldObj, floor_posX, floor_posY + 1, floor_posZ) || worldObj.getBlock(floor_posX, floor_posY + 1, floor_posZ) == BlockIDs.tcRail.block || worldObj.getBlock(floor_posX, floor_posY + 1, floor_posZ) == BlockIDs.tcRailGag.block) {
            floor_posY++;
        }

        l = worldObj.getBlock(floor_posX, floor_posY, floor_posZ);

        updateOnTrack(floor_posX, floor_posY, floor_posZ, l);

        d6 = prevPosX - posX;
        d7 = prevPosZ - posZ;
        prevRotationYaw = rotationYaw;

        //this.rotationPitch = 0.0F;

        if (d6 * d6 + d7 * d7 > 0.0001D) {
            this.rotationYaw = (float) (Math.atan2(d7, d6) * 180.0D / Math.PI);
            if (this.isClientInReverse) {
                this.rotationYaw += 180.0F;
            }
        }

        //double var49 = MathHelper.wrapAngleTo180_float(this.rotationYaw - this.prevRotationYaw);

        float anglePitch = 0;
        if (bogieFront != null) {

            serverRealRotation = MathHelper.wrapAngleTo180_float((float) Math.toDegrees(Math.atan2((float) (bogieFront.posZ - this.posZ), (float) (bogieFront.posX - this.posX))) - 90F);

            anglePitch = (float) Math.atan(((bogieFront.posY - posY)) /
                    MathHelper.sqrt_double(((bogieFront.posX - posX) * (bogieFront.posX - posX)) +
                            ((bogieFront.posZ - posZ) * (bogieFront.posZ - posZ))));//1.043749988079071
            serverRealPitch = anglePitch + (float)
                    ((bogieFront.posZ - posZ) * (bogieFront.posZ - posZ));//1.043749988079071
        } else {
            DebugUtil.println("obsolete me");
            float rotation = rotationYaw;

            float delta = MathHelper.wrapAngleTo180_float(this.rotationYaw - this.previousServerRealRotation);

            if (delta < -179.0F || delta > 179.0F) { // if (delta > 170.0F || delta < 190.0F) {

                this.rotationYaw += 180.0F;
                this.isServerInReverse = !this.isServerInReverse;
            }
            previousServerRealRotation = rotation;
            if (this.isServerInReverse) {
                if (serverInReverseSignPositive) {
                    rotation += 180.0f;
                } else {
                    rotation -= 180.0f;
                }
            }

            serverRealRotation = rotation;

            double zDist = posZ - prevPosZ;
            double xDist = posX - prevPosX;
            float tempPitch = rollingServerPitch;
            float tempPitch2 = tempPitch;
            if (Math.abs(zDist) > 0.02) {
                tempPitch = (float) ((Math.atan((posY - prevPosY) / zDist)) * degrees);
            } else if (Math.abs(xDist) > 0.02) {
                tempPitch = (float) ((Math.atan((posY - prevPosY) / xDist)) * degrees);
            }

            if (tempPitch2 < tempPitch && Math.abs(tempPitch2 - tempPitch) > 3) {
                tempPitch2 += 3;
            } else if (tempPitch2 > tempPitch && Math.abs(tempPitch2 - tempPitch) > 3) {
                tempPitch2 -= 3;
            } else if (tempPitch2 < tempPitch && Math.abs(tempPitch2 - tempPitch) > 0.5) {
                tempPitch2 += 0.5;
            } else if (tempPitch2 > tempPitch && Math.abs(tempPitch2 - tempPitch) > 0.5) {
                tempPitch2 -= 0.5;
            }
            anglePitch = -tempPitch2;
            rollingServerPitch = 0;
        }


        if (ticksExisted % 2 == 0) {
            Traincraft.rotationChannel.sendToAllAround(new PacketRollingStockRotation(this, (int) (anglePitch * 60)), new TargetPoint(worldObj.provider.dimensionId, posX, posY, posZ, 300.0D));
        }
        if (!worldObj.isRemote) {
            anglePitchClient = (anglePitch * 60);
        }

        //this.setRotation(this.rotationYaw, this.rotationPitch);

        list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, getCollisionHandler() != null ?
                getCollisionHandler().getMinecartCollisionBox(this) :
                boundingBox.expand(0.2D, 0.0D, 0.2D));

        if (list != null && !list.isEmpty()) {
            Entity entity;
            for (Object obj : list) {
                if (obj == this.riddenByEntity) {
                    continue;
                }
                entity = (Entity) obj;

                if (entity.canBePushed() && entity instanceof EntityMinecart) {
                    entity.applyEntityCollision(this);
                } else if (entity.canBePushed() && !(entity instanceof EntityMinecart)) {
                    this.applyEntityCollision(entity);
                }
            }
        }

        handleTrain();
        handleOverheating.HandleHeatLevel(this);
        linkhandler.handleStake(this, boundingBox);
        collisionhandler.handleCollisions(this, boundingBox);
        this.func_145775_I();
        MinecraftForge.EVENT_BUS.post(new MinecartUpdateEvent(this, floor_posX, floor_posY, floor_posZ));
        //setBoundingBoxSmall(posX, posY, posZ, 0.98F, 0.7F);
        numLaps++;
        if ((this instanceof Locomotive) && (this.Link1 == 0) && (this.Link2 == 0) && numLaps > 700) {
            this.consist.clear();
        }


        for (EntitySeat seat: seats) { //handle died in train
            if (seat.getPassenger() != null && (seat.getPassenger().isDead || seat != seat.getPassenger().ridingEntity)) {
                this.seats.get(0).getPassenger().ridingEntity = null;
                this.seats.get(0).removePassenger(this.seats.get(0).getPassenger());
            }
        }
        this.dataWatcher.updateObject(14, (int) (motionX * 100));
        this.dataWatcher.updateObject(21, (int) (motionZ * 100));
        //rider updating isn't called if there's no driver/conductor, so just in case of that, we reposition the seats here too.
        if (getRiderOffsets() != null) {
            for (int i1 = 0; i1 < seats.size(); i1++) {
                //sometimes seats die when players log out. make new ones.
                if(seats.get(i1) ==null){
                    seats.set(i1, new EntitySeat(getWorld(), posX, posY,posZ,0,0,0, this,i1));
                    if(i1==0){
                        seats.get(i1).setControlSeat();
                    }
                    getWorld().spawnEntityInWorld(seats.get(i1));
                }
                cachedVectors[0] = new Vec3f(getRiderOffsets()[i1][0], getRiderOffsets()[i1][1], getRiderOffsets()[i1][2])
                        .rotatePoint(rotationPitch, rotationYaw, 0f);
                cachedVectors[0].addVector(posX,posY,posZ);
                seats.get(i1).setPosition(cachedVectors[0].xCoord, cachedVectors[0].yCoord, cachedVectors[0].zCoord);
            }
        }
        if (ConfigHandler.ENABLE_LOGGING && !worldObj.isRemote && ticksExisted % 120 == 0) {
            ServerLogger.writeWagonToFolder(this);
        }
    }

    boolean flag, flag1;

    private void updateOnTrack(int floor_posX, int floor_posY, int floor_posZ, Block block) { //vanilla rails
        if (canUseRail() && BlockRailBase.func_150051_a(block)) {

            Vec3 vec3d = TraincraftUtil.func_514_g(posX, posY, posZ);
            int blockMeta = ((BlockRailBase) block).getBasicRailMetadata(worldObj, this, floor_posX, floor_posY, floor_posZ);
            meta = blockMeta;
            posY = floor_posY;
            flag = false;
            flag1 = block == Blocks.golden_rail;
            if (flag1) {
                flag = worldObj.getBlockMetadata(floor_posX, floor_posY, floor_posZ) > 2;
                if (blockMeta == 8) {
                    blockMeta = 0;
                } else if (blockMeta == 9) {
                    blockMeta = 1;
                }
            }

            if (block == Blocks.detector_rail) {
                worldObj.setBlockMetadataWithNotify(floor_posX, floor_posY, floor_posZ, meta | 8, 3);
                worldObj.notifyBlocksOfNeighborChange(floor_posX, floor_posY, floor_posZ, block);
                worldObj.notifyBlocksOfNeighborChange(floor_posX, floor_posY - 1, floor_posZ, block);
                worldObj.markBlockRangeForRenderUpdate(floor_posX, floor_posY, floor_posZ, floor_posX, floor_posY, floor_posZ);
                worldObj.scheduleBlockUpdate(floor_posX, floor_posY, floor_posZ, block, block.tickRate(worldObj));
            }

            if (blockMeta >= 2 && blockMeta <= 5) {
                posY = (floor_posY + 1);
            }

            adjustSlopeVelocities(blockMeta);


            int[][] metaMatrix = matrix[blockMeta];
            double d9 = metaMatrix[1][0] - metaMatrix[0][0];
            double d10 = metaMatrix[1][2] - metaMatrix[0][2];
            double d11 = Math.sqrt(d9 * d9 + d10 * d10); //something normalized
            if (motionX * d9 + motionZ * d10 < 0.0D) {
                d9 = -d9;
                d10 = -d10;
            }
            double motionNormalized = Math.sqrt(motionX * motionX + motionZ * motionZ);
            motionX = (motionNormalized * d9) / d11;
            motionZ = (motionNormalized * d10) / d11;
            if (flag1 && !flag && shouldDoRailFunctions()) {
                if (Math.sqrt(motionX * motionX + motionZ * motionZ) < 0.029999999999999999D) {
                    motionX = 0.0D;
                    motionY = 0.0D;
                    motionZ = 0.0D;
                } else {
                    motionX *= 0.5D;
                    motionY *= 0.0D;
                    motionZ *= 0.5D;
                }
            }
            double d17;
            double d18 = floor_posX + 0.5D + metaMatrix[0][0] * 0.5D;
            double d19 = floor_posZ + 0.5D + metaMatrix[0][2] * 0.5D;
            double d20 = floor_posX + 0.5D + metaMatrix[1][0] * 0.5D;
            double d21 = floor_posZ + 0.5D + metaMatrix[1][2] * 0.5D;
            d9 = d20 - d18;
            d10 = d21 - d19;
            if (d9 == 0.0D) {
                posX = floor_posX + 0.5D;
                d17 = posZ - floor_posZ;
            } else if (d10 == 0.0D) {
                posZ = floor_posZ + 0.5D;
                d17 = posX - floor_posX;
            } else {
                double d22 = posX - d18;
                double d24 = posZ - d19;
                d17 = (d22 * d9 + d24 * d10) * 2D;
                //double derailSpeed = 0;//0.46;
            }
            if (bogieFront != null) {
                if (!bogieFront.isOnRail()) {
                    derailSpeed = 0;
                    this.unLink();
                }
            }
            /**
             * Handles derail
             */
            if ((this instanceof Locomotive || this instanceof ISecondBogie) && motionNormalized > derailSpeed && blockMeta >= 6) {
                if (d9 > 0 && d10 < 0) {
                    d10 = 0;
                    d9 += 2;
                } else if (d9 < 0 && d10 > 0) {
                    d9 = 0;
                    d10 += 2;
                } else if (d10 < 0 && d9 < 0) {
                    d10 -= 2;
                    d9 = 0;
                } else if (d9 > 0 && d10 > 0) {
                    d10 += 2;
                    d9 = 0;
                }
                if (FMLCommonHandler.instance().getMinecraftServerInstance() != null &&
                        this.seats != null && this.seats.size()>0 && this.seats.get(0).getPassenger() != null && this.seats.get(0).getPassenger() instanceof EntityPlayer) {
                    FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(new
                            ChatComponentText(((EntityPlayer) this.seats.get(0).getPassenger()).getDisplayName() + "derailed"
                            + this.trainOwner + "'s locomotive"));
                }
            }


            posX = d18 + d9 * d17;
            posZ = d19 + d10 * d17;
            setPosition(posX, posY + yOffset + 0.35, posZ);

            moveMinecartOnRail(floor_posX, floor_posY, floor_posZ, 0.0D);

            if (metaMatrix[0][1] != 0 && MathHelper.floor_double(posX) - floor_posX == metaMatrix[0][0] &&
                    MathHelper.floor_double(posZ) - floor_posZ == metaMatrix[0][2]) {
                setPosition(posX, posY + metaMatrix[0][1], posZ);
            } else if (metaMatrix[1][1] != 0 && MathHelper.floor_double(posX) - floor_posX == metaMatrix[1][0] &&
                    MathHelper.floor_double(posZ) - floor_posZ == metaMatrix[1][2]) {
                setPosition(posX, posY + metaMatrix[1][1], posZ);
            }

            applyDragAndPushForces();

            Vec3 vec3d1 = TraincraftUtil.func_514_g(posX, posY, posZ);
            double d28 = (vec3d.yCoord - vec3d1.yCoord) * 0.050000000000000003D;
            if (this instanceof Locomotive) d28 = 0;
            double d14 = Math.sqrt(motionX * motionX + motionZ * motionZ);
            if (d14 > 0.0D) {
                motionX = (motionX / d14) * (d14 + d28);
                motionZ = (motionZ / d14) * (d14 + d28);
            }
            setPosition(posX, posY + yOffset - 0.8d, posZ);
            int entity_floor_posX = MathHelper.floor_double(posX);
            int entity_floor_posZ = MathHelper.floor_double(posZ);
            if (entity_floor_posX != floor_posX || entity_floor_posZ != floor_posZ) {
                double d15 = Math.sqrt(motionX * motionX + motionZ * motionZ);
                motionX = d15 * (entity_floor_posX - floor_posX);
                motionZ = d15 * (entity_floor_posZ - floor_posZ);
            }

            if (shouldDoRailFunctions()) {
                ((BlockRailBase) block).onMinecartPass(worldObj, this, floor_posX, floor_posY, floor_posZ);
            }

            if (flag && shouldDoRailFunctions()) {
                double d31 = Math.sqrt(motionX * motionX + motionZ * motionZ);
                if (d31 > 0.01D) {
                    motionX += (motionX / d31) * 0.059999999999999998D;
                    motionZ += (motionZ / d31) * 0.059999999999999998D;
                } else if (blockMeta == 1) {
                    if (worldObj.isBlockNormalCubeDefault(floor_posX - 1, floor_posY, floor_posZ, false)) {
                        motionX = 0.02D;
                    } else if (worldObj.isBlockNormalCubeDefault(floor_posX + 1, floor_posY, floor_posZ, false)) {
                        motionX = -0.02D;
                    }
                } else if (blockMeta == 0) {
                    if (worldObj.isBlockNormalCubeDefault(floor_posX, floor_posY, floor_posZ - 1, false)) {
                        motionZ = 0.02D;
                    } else if (worldObj.isBlockNormalCubeDefault(floor_posX, floor_posY, floor_posZ + 1, false)) {
                        motionZ = -0.02D;
                    }
                }
            }
        } else if (block == BlockIDs.tcRail.block) {
            limitSpeedOnTCRail();
            if(lastTrack==null || lastTrack.xCoord!=floor_posX ||lastTrack.zCoord!=floor_posZ){
                if( worldObj.getTileEntity(floor_posX, floor_posY, floor_posZ) instanceof TileTCRail) {
                    lastTrack = (TileTCRail) worldObj.getTileEntity(floor_posX, floor_posY, floor_posZ);
                } else {
                    return;
                }
            }
            int meta = lastTrack.getBlockMetadata();

            if (TCRailTypes.isStraightTrack(lastTrack) || (TCRailTypes.isSwitchTrack(lastTrack) && !lastTrack.getSwitchState())) {
                moveOnTCStraight(floor_posX, floor_posY, floor_posZ, lastTrack.xCoord, lastTrack.zCoord, meta);
            } else if (TCRailTypes.isTurnTrack(lastTrack) || (TCRailTypes.isSwitchTrack(lastTrack) && lastTrack.getSwitchState())) {
                if (bogieFront != null) {
                    if (!bogieFront.isOnRail()) {
                        derailSpeed = 0;
                    }
                }
                if (bogieBack != null) {
                    if (!bogieBack.isOnRail()) {
                        derailSpeed = 0;
                    }
                }
                if (derailSpeed == 0) {
                    this.unLink();
                    moveOnTCStraight(floor_posX, floor_posY, floor_posZ, lastTrack.xCoord, lastTrack.zCoord, meta);
                } else {

                    if (shouldIgnoreSwitch(lastTrack, floor_posX, floor_posY, floor_posZ, meta)) {

                        moveOnTCStraight(floor_posX, floor_posY, floor_posZ, lastTrack.xCoord, lastTrack.zCoord, meta);
                    } else {
                        if (TCRailTypes.isTurnTrack(lastTrack) || (TCRailTypes.isSwitchTrack(lastTrack) && lastTrack.getSwitchState()))
                            moveOnTC90TurnRail(floor_posX, floor_posY, floor_posZ, lastTrack.r, lastTrack.cx, lastTrack.cz);
                    }
                    // shouldIgnoreSwitch(tile, i, j, k, meta);
                    // if (ItemTCRail.isTCTurnTrack(tile)) moveOnTC90TurnRail(i, j, k, r, cx, cy,
                    // cz, tile.getType(), meta);
                }
            } else if (TCRailTypes.isSlopeTrack(lastTrack)) {
                moveOnTCSlope(floor_posY, lastTrack.xCoord, lastTrack.zCoord, lastTrack.slopeAngle, lastTrack.slopeHeight, meta);
            } else if (TCRailTypes.isCrossingTrack(lastTrack)) {
                moveOnTCTwoWaysCrossing(floor_posX, floor_posY, floor_posZ, lastTrack.xCoord, lastTrack.yCoord, lastTrack.zCoord, meta);
            } else if (TCRailTypes.isDiagonalCrossingTrack(lastTrack)) {
                moveOnTCDiamondCrossing(floor_posX, floor_posY, floor_posZ, lastTrack.xCoord, lastTrack.yCoord, lastTrack.zCoord, meta);
            } else if (TCRailTypes.isDiagonalTrack(lastTrack)) {
                moveOnTCDiagonal(floor_posX, floor_posY, floor_posZ, lastTrack.xCoord, lastTrack.zCoord, lastTrack.getBlockMetadata(), lastTrack.getRailLength());
            } else if (TCRailTypes.isCurvedSlopeTrack(lastTrack)) {
                moveOnTCCurvedSlope(floor_posX, floor_posY, floor_posZ, lastTrack.r, lastTrack.cx, lastTrack.cz, lastTrack.xCoord, lastTrack.zCoord, meta, 1, lastTrack.slopeAngle);
            }

        } else if (block == BlockIDs.tcRailGag.block) {
            limitSpeedOnTCRail();
            TileTCRailGag tileGag = (TileTCRailGag) worldObj.getTileEntity(floor_posX, floor_posY, floor_posZ);

            if(lastTrack==null || lastTrack.xCoord!=tileGag.originX ||lastTrack.zCoord!=tileGag.originZ){
                if(worldObj.getTileEntity(tileGag.originX, tileGag.originY, tileGag.originZ) instanceof TileTCRail) {
                    lastTrack = (TileTCRail) worldObj.getTileEntity(tileGag.originX, tileGag.originY, tileGag.originZ);
                } else {
                    return;
                }
            }
            if (TCRailTypes.isTurnTrack(lastTrack)) {
                moveOnTC90TurnRail(floor_posX, floor_posY, floor_posZ, lastTrack.r, lastTrack.cx, lastTrack.cz);
            }
            if (TCRailTypes.isStraightTrack(lastTrack)) {
                moveOnTCStraight(floor_posX, floor_posY, floor_posZ, lastTrack.xCoord, lastTrack.zCoord, lastTrack.getBlockMetadata());
            }
            if (TCRailTypes.isSlopeTrack(lastTrack)) {
                moveOnTCSlope(floor_posY, lastTrack.xCoord, lastTrack.zCoord, lastTrack.slopeAngle, lastTrack.slopeHeight, lastTrack.getBlockMetadata());
            }
            if (TCRailTypes.isDiagonalTrack(lastTrack)) {
                moveOnTCDiagonal(floor_posX, floor_posY, floor_posZ, lastTrack.xCoord, lastTrack.zCoord, lastTrack.getBlockMetadata(), lastTrack.getRailLength());
            } else if (TCRailTypes.isDiagonalCrossingTrack(lastTrack)) {
                moveOnTCDiamondCrossing(floor_posX, floor_posY, floor_posZ, lastTrack.xCoord, lastTrack.yCoord, lastTrack.zCoord, meta);
            }
            if (TCRailTypes.isCurvedSlopeTrack(lastTrack)) {
                moveOnTCCurvedSlope(floor_posX, floor_posY, floor_posZ, lastTrack.r, lastTrack.cx, lastTrack.cz, lastTrack.xCoord, lastTrack.zCoord, lastTrack.getBlockMetadata(), lastTrack.slopeHeight, lastTrack.slopeAngle);
            }
        } else {
/*            Vec3f closest = null;  this is test/in-dev anti-derailment code.
            double dist = Double.MAX_VALUE;
            for(int a = -1; a<2;a++) {
                for(int c = -1;c<2;c++) {
                    if (isRailBlockAt(worldObj, floor_posX+a, floor_posY + 1, floor_posZ+c) || worldObj.getBlock(floor_posX+a, floor_posY + 1, floor_posZ+c) == BlockIDs.tcRail.block || worldObj.getBlock(floor_posX+a, floor_posY + 1, floor_posZ+c) == BlockIDs.tcRailGag.block) {
                        if (closest == null) {
                            closest = new Vec3f(floor_posX+a,floor_posY+1,floor_posZ+c);
                            dist = Math.sqrt(Math.pow(closest.xCoord-posX,2)+Math.pow(closest.zCoord-posZ,2));
                        } else {
                            double tdist = Math.sqrt(Math.pow((floor_posX+a)-posX,2)+Math.pow((floor_posZ+c)-posZ,2));
                            if (tdist < dist) {
                                dist = tdist;
                            }
                        }
                    }
                }
            }
            if (closest != null) {
                this.setPosition( closest.xCoord, closest.yCoord, closest.zCoord);
            } else {*/
            moveMinecartOffRail(floor_posX, floor_posY, floor_posZ);
            //}
            super.onUpdate();
        }

    }

    private boolean shouldIgnoreSwitch(TileTCRail tile, int i, int j, int k, int meta) {
        if (tile != null && TCRailTypes.isTurnTrack(tile) && tile.canTypeBeModifiedBySwitch) {

            /* Handles reverse straight movement of a cart on a switch that happened to be turned on*/
            if (meta == 2) {
                if (motionZ > 0 && Math.abs(motionX) < 0.01) {
                    TileEntity tile2 = worldObj.getTileEntity(i, j, k + 1);
                    if (tile2 instanceof TileTCRail) {
                        ((TileTCRail) tile2).setSwitchState(false, true);}
                    return true;
                }
            } else if (meta == 0) {
                if (motionZ < 0 && Math.abs(motionX) < 0.01) {
                    TileEntity tile2 = worldObj.getTileEntity(i, j, k - 1);
                    if (tile2 instanceof TileTCRail) {
                        ((TileTCRail) tile2).setSwitchState(false, true);
                    }
                    return true;
                }
            } else if (meta == 1) {
                if (Math.abs(motionZ) < 0.01 && motionX > 0) {
                    TileEntity tile2 = worldObj.getTileEntity(i + 1, j, k);
                    if (tile2 instanceof TileTCRail) {
                        ((TileTCRail) tile2).setSwitchState(false, true);
                    }
                    return true;
                }
            } else if (meta == 3) {
                if (Math.abs(motionZ) < 0.01 && motionX < 0) {
                    TileEntity tile2 = worldObj.getTileEntity(i - 1, j, k);
                    if (tile2 instanceof TileTCRail) {
                        ((TileTCRail) tile2).setSwitchState(false, true);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void moveOnTCDiagonal(int i, int j, int k, double cx, double cz, int meta, double length) {

        double Y_OFFSET = 0.2;
        double X_OFFSET = 0.5;
        double Z_OFFSET = 1.5;
        posY = j + Y_OFFSET;
        if (length == 0) {
            length = 1;
        }
        double exitX = 0;
        double exitZ = 0;
        double directionX;
        double directionZ;
        double norm = Math.sqrt(motionX * motionX + motionZ * motionZ);
        double distanceNorm;

        if (meta == 6) {
            exitX = (motionX > 0) ? cx + length + X_OFFSET : cx - X_OFFSET;
            exitZ = (motionX > 0) ? cz - length + X_OFFSET : cz + Z_OFFSET;
        } else if (meta == 4) {
            exitX = (motionX > 0) ? cx + Z_OFFSET : cx - (length - X_OFFSET);
            exitZ = (motionX > 0) ? cz - X_OFFSET : cz + (length + X_OFFSET);
        } else if (meta == 5) {
            exitX = (motionX > 0) ? cx + Z_OFFSET : cx - (length + X_OFFSET);
            exitZ = (motionX > 0) ? cz + Z_OFFSET : cz - (length + X_OFFSET);
        } else if (meta == 7) {
            exitX = (motionX > 0) ? cx + (length + X_OFFSET) : cx - X_OFFSET;
            exitZ = (motionX > 0) ? cz + (length + X_OFFSET) : cz - X_OFFSET;
        }

        directionX = exitX - posX;
        directionZ = exitZ - posZ;
        distanceNorm = Math.sqrt(directionX * directionX + directionZ * directionZ);
        motionX = (directionX / distanceNorm) * norm;
        motionZ = (directionZ / distanceNorm) * norm;
        this.boundingBox.offset(Math.copySign(motionX, this.motionX), 0, Math.copySign(motionZ, this.motionZ));

        List boxes = worldObj.getCollidingBoundingBoxes(this, boundingBox);
        for (Object b : boxes) {
            if (!(b instanceof BlockRailBase) && !(b instanceof BlockTCRail) && !(b instanceof BlockTCRailGag) && !(b instanceof BlockAir)) {
                return;
            }
        }
        this.posX = (this.boundingBox.minX + this.boundingBox.maxX) / 2.0D;
        this.posY = this.boundingBox.minY + (double) this.yOffset - (double) this.ySize;
        this.posZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0D;

    }


    private void moveOnTCCurvedSlope(int floor_X, int floor_Y, int floor_Z, double tileRadius, double circleX, double circleZ, int tileX, int tileZ, int meta, double slopeHeight, double slopeAngle) {
        double newTileX = tileX;
        double newTileZ = tileZ;
        if (meta == 2) {
            newTileZ += 1;
            newTileX += 0.5;
        }
        if (meta == 0) {
            newTileX += 0.5;
        }
        if (meta == 1) {
            newTileX += 1;
            newTileZ += 0.5;
        }
        if (meta == 3) {
            newTileZ += 0.5;
        }
        double circlePosX = posX - circleX;
        double circlePosZ = posZ - circleZ;

        double tilePositionNormalized = Math.sqrt(Math.pow((newTileX - posX),2) + Math.pow((newTileZ - posZ),2));

        double circlePositionNormalized = Math.sqrt(circlePosX * circlePosX + circlePosZ * circlePosZ);
        double velocityNormalized = Math.sqrt(motionX * motionX + motionZ * motionZ);

        double normCirclePosX = circlePosX / circlePositionNormalized; //u
        double normCirclePosZ = circlePosZ / circlePositionNormalized; //v

        double negVelNormX = -normCirclePosZ * velocityNormalized;//-v
        double velNormZ = normCirclePosX * velocityNormalized;//u

        double positionX = posX + motionX;
        double positionZ = posZ + motionZ;

        double positionXOffsetByCircle = positionX - circleX;
        double positionZOffsetByCircle = positionZ - circleZ;

        double offsetPositionNormalized = Math.sqrt((positionXOffsetByCircle * positionXOffsetByCircle) + (positionZOffsetByCircle * positionZOffsetByCircle));

        double offsetPositionNormX = positionXOffsetByCircle / offsetPositionNormalized;
        double offsetPositionNormZ = positionZOffsetByCircle / offsetPositionNormalized;

        double posX3 = circleX + (offsetPositionNormX * tileRadius);
        double posZ3 = circleZ + (offsetPositionNormZ * tileRadius);

        double signX = posX3 - posX;
        double signZ = posZ3 - posZ;

        negVelNormX = Math.copySign(negVelNormX, signX);
        velNormZ = Math.copySign(velNormZ, signZ);

        double correctedPosX = circleX + ((circlePosX / circlePositionNormalized) * tileRadius);
        double correctedPosZ = circleZ + ((circlePosZ / circlePositionNormalized) * tileRadius);
        double newYPos = Math.abs(floor_Y + Math.min(1, (slopeAngle * Math.abs(tilePositionNormalized))) + yOffset + 0.34f);
        setPosition(correctedPosX, newYPos, correctedPosZ);

        /* slope speed-up. it works* but not in a desired fashion. will come back to it.
        double normalizedSlopeVelocity = Math.sqrt((negVelNormX*negVelNormX)+(velNormZ*velNormZ));
        normalizedSlopeVelocity = getSlopeAdjustedSpeed(normalizedSlopeVelocity, slopeAngle);
        double slopeVelopcityX = negVelNormX / normalizedSlopeVelocity;
        double slopeVelopcityZ = velNormZ / normalizedSlopeVelocity;
        motionX = Math.copySign(slopeVelopcityX, motionX);
        motionZ = Math.copySign(slopeVelopcityZ, motionZ);
        System.out.println(slopeAngle);
        */

        moveEntity(negVelNormX, 0, velNormZ);
    }

    private void moveOnTCStraight(int i, int j, int k, double cx, double cz, int meta) {
        posY = j + 0.2;
        if (meta == 2 || meta == 0) {
            double norm = Math.sqrt(motionX * motionX + motionZ * motionZ);

            setPosition(cx + 0.5, posY + yOffset, posZ);

            motionX = 0;
            motionZ = Math.copySign(norm, motionZ);
            this.boundingBox.offset(0, 0, Math.copySign(norm, this.motionZ));

            List boxes = worldObj.getCollidingBoundingBoxes(this, boundingBox);
            for (Object b : boxes) {
                if (!(b instanceof BlockRailBase) && !(b instanceof BlockTCRail) && !(b instanceof BlockTCRailGag) && !(b instanceof BlockAir)) {
                    return;
                }
            }
            this.posX = (this.boundingBox.minX + this.boundingBox.maxX) / 2.0D;
            this.posY = this.boundingBox.minY + (double) this.yOffset - (double) this.ySize;
            this.posZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0D;

        } else if (meta == 1 || meta == 3) {

            setPosition(posX, posY + yOffset, cz + 0.5);
            //setPosition(posX, posY + yOffset, posZ);

            motionX = Math.copySign(Math.sqrt(motionX * motionX + motionZ * motionZ), motionX);
            motionZ = 0;
            this.boundingBox.offset(motionX, 0, 0);

            List boxes = worldObj.getCollidingBoundingBoxes(this, boundingBox);
            for (Object b : boxes) {
                if (!(b instanceof BlockRailBase) && !(b instanceof BlockTCRail) && !(b instanceof BlockTCRailGag) && !(b instanceof BlockAir)) {
                    return;
                }
            }
            this.posX = (this.boundingBox.minX + this.boundingBox.maxX) / 2.0D;
            this.posY = this.boundingBox.minY + (double) this.yOffset - (double) this.ySize;
            this.posZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0D;


        }
    }

    private int numCarsOnSlope() {
        int count = 0;
        if (train == null) {
            return 1;
        }
        for(AbstractTrains entity: train.getTrains()) {
            int floorX = (int) Math.floor(entity.posX);
            int floorY = (int) Math.floor(entity.posY);
            int floorZ = (int) Math.floor(entity.posZ);
            Block block = worldObj.getBlock(floorX, floorY, floorZ);
            TileTCRail tile = null;
            if (block instanceof BlockAir) {
                floorY--;
                block = worldObj.getBlock(floorX, floorY, floorZ);
            }
            if (block instanceof BlockTCRail) {
                tile = (TileTCRail) worldObj.getTileEntity(floorX, floorY, floorZ);
            } else if (block instanceof BlockTCRailGag) {
                TileTCRailGag tileGag = (TileTCRailGag) worldObj.getTileEntity(floorX, floorY, floorZ);
                if (worldObj.getTileEntity(tileGag.originX, tileGag.originY, tileGag.originZ) instanceof TileTCRail) {
                    tile = (TileTCRail) worldObj.getTileEntity(tileGag.originX, tileGag.originY, tileGag.originZ);
                }
            }
            if (tile != null && tile.slopeAngle != 0) {
                count++;
            }
        }
        return count;
    }

    private int numCarsTotal() {
        if (train == null) { //train is null when there is nothing coupled to the stock
            return 2;
        }
        return train.getTrains().size();
    }

    private boolean hasLocomotive() {
        if (train == null) { //train is null when there is nothing coupled to the stock
            return false;
        }
        return (train.getTrainPower() != 0);
    }

    private void moveOnTCSlope(int posY, double posX, double posZ, double slopeAngle, double slopeHeight, int meta) {
        //posY = j + 2.5;

        if (meta == 2) {
            posZ ++;
        }
        if (meta == 1) {
            posX ++;
        }
        double normalizedSpeed = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

        if (meta == 2 || meta == 0) {
            this.setPosition(posX + 0.5D, Math.abs(posY + (Math.tan(slopeAngle * Math.abs(posZ - this.posZ))) + this.yOffset + 0.3), this.posZ);
            this.boundingBox.offset(0, 0, Math.copySign(normalizedSpeed, this.motionZ));
        }
        else if (meta == 1 || meta == 3) {
            this.setPosition(this.posX, (posY + (Math.tan(slopeAngle * Math.abs(posX - this.posX))) + this.yOffset + 0.3), posZ + 0.5D);
            this.boundingBox.offset(Math.copySign(normalizedSpeed, this.motionX), 0, 0);
        } else {
            return;
        }
        this.posX = (this.boundingBox.minX + this.boundingBox.maxX) / 2.0D;
        this.posY = this.boundingBox.minY + (double) this.yOffset - (double) this.ySize;
        this.posZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0D;
        normalizedSpeed = getSlopeAdjustedSpeed(normalizedSpeed, slopeAngle);
        if (meta == 2 || meta == 0) {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = Math.copySign(normalizedSpeed, this.motionZ);
        } else {
            this.motionX = Math.copySign(normalizedSpeed, this.motionX);
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
        }
    }

    public double getSlopeAdjustedSpeed(double normalizedSpeed, double slopeAngle) {
        if (ConfigHandler.ENABLE_SLOPE_ACCELERATION) {
            if (this instanceof Locomotive && !((Locomotive) this).canBePulled) { //make this speedup only happen twice a second
                if (this.ticksExisted % 10 == 0) {
                    int carsPulled = numCarsTotal();
                    carsPulled--; //locomotive counting as two entities?
                    int carsOnSlope = numCarsOnSlope();
                    if ((this.posY - this.prevPosY < 0)) {
                        normalizedSpeed *= (((double) carsOnSlope / carsPulled) * (slopeAngle)) + getDragAir();
                    } else if ((this.posY - this.prevPosY) > 0.013) {//0.013 to account for the jank that happens when over slopes back to back.
                        normalizedSpeed *= 1 - (((double) carsOnSlope / carsPulled) * slopeAngle);
                        if (normalizedSpeed - 0.001 <= 0) {
                            normalizedSpeed = -0.001;
                        }
                    }
                }
            } else if (!hasLocomotive()) { //traincars. is a bit jumpy but doesn't seem to derail
                if ((this.posY - this.prevPosY) < 0) {
                    if (slopeAngle < 0.05) {
                        normalizedSpeed *= getDragAir() + (slopeAngle * 2.7);

                    } else {
                        normalizedSpeed *= getDragAir() + (slopeAngle * 2);
                    }
                } else if ((this.posY - this.prevPosY) > 0.013) {
                    normalizedSpeed *= (0.98 - (slopeAngle));
                }
            }
        }
        return normalizedSpeed;
    }

    protected void moveOnTC90TurnRail(int i, int j, int k, double r, double cx, double cz) {
        posY = j + 0.2;
        double cpx = posX - cx;
        double cpz = posZ - cz;

        double cp_norm = Math.sqrt(cpx * cpx + cpz * cpz);
        double vnorm = Math.sqrt(motionX * motionX + motionZ * motionZ);

        double norm_cpx = cpx / cp_norm; //u
        double norm_cpz = cpz / cp_norm; //v

        double vx2 = -norm_cpz * vnorm;//-v
        double vz2 = norm_cpx * vnorm;//u

        double px2 = posX + motionX;
        double pz2 = posZ + motionZ;

        double px2_cx = px2 - cx;
        double pz2_cz = pz2 - cz;

        double p2_c_norm = Math.sqrt((px2_cx * px2_cx) + (pz2_cz * pz2_cz));

        double px2_cx_norm = px2_cx / p2_c_norm;
        double pz2_cz_norm = pz2_cz / p2_c_norm;

        double px3 = cx + (px2_cx_norm * r);
        double pz3 = cz + (pz2_cz_norm * r);

        double signX = px3 - posX;
        double signZ = pz3 - posZ;

        vx2 = Math.copySign(vx2, signX);
        vz2 = Math.copySign(vz2, signZ);

        double p_corr_x = cx + ((cpx / cp_norm) * r);
        double p_corr_z = cz + ((cpz / cp_norm) * r);

        setPosition(p_corr_x, posY + yOffset, p_corr_z);
        moveEntity(vx2, 0.0D, vz2);

        motionX = vx2;
        motionZ = vz2;

    }

    protected void moveOnTCTwoWaysCrossing(int i, int j, int k, double cx, double cy, double cz, int meta) {
        posY = j + 0.2;
        if (!(this instanceof Locomotive)) {
            int l = MathHelper.floor_double(serverRealRotation * 4.0F / 360.0F + 0.5D) & 3;
            if (l == 2 || l == 0) {
                moveEntity(motionX, 0.0D, 0.0D);
            } else if (l == 1 || l == 3) {
                moveEntity(0.0D, 0.0D, motionZ);
            }
        } else {
            int l = MathHelper.floor_double(rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
            if (l == 2 || l == 0) {
                moveEntity(motionX, 0.0D, 0.0D);
            } else if (l == 1 || l == 3) {
                moveEntity(0.0D, 0.0D, motionZ);
            }
            //moveEntity(motionX, 0.0D, motionZ);
        }
    }

    protected void moveOnTCDiamondCrossing(int i, int j, int k, double cx, double cy, double cz, int meta) {

        int l;
        if ((this.bogieFront == null)) {
            l = MathHelper.floor_double(serverRealRotation * 8.0F / 360.0F + 0.5) & 7;
        } else {
            l = MathHelper.floor_double(rotationYaw * 8.0F / 360.0F + 0.5) & 7;

        }
        if (l == 0 || l == 4) {
            moveEntity(motionX, 0.0D, 0.0D);
        } else if (l == 2 || l == 6) {
            moveEntity(0.0D, 0.0D, motionZ);
        } else if (l == 1) {
            moveOnTCDiagonal(i, j, k, cx, cz, 5, 1);
        } else if (l == 3) {
            moveOnTCDiagonal(i, j, k, cx, cz, 6, 1);
        } else if (l == 5) {
            moveOnTCDiagonal(i, j, k, cx, cz, 7, 1);
        } else if (l == 7) {
            moveOnTCDiagonal(i, j, k, cx, cz, 4, 1);
        }
    }
    public void limitSpeedOnTCRail() {
        railMaxSpeed = 3;
        maxSpeed = Math.min(railMaxSpeed, getMaxCartSpeedOnRail());
        maxSpeed = SpeedHandler.handleSpeed(railMaxSpeed, maxSpeed, this);

        if (this.speedLimiter != 0 && speedWasSet) {
            //maxSpeed *= this.speedLimiter;
            adjustSpeed(maxSpeed, speedLimiter);
        }
        motionX *= 0.9D;
        motionZ *= 0.9D;

        if (motionX < -maxSpeed) {
            motionX = -maxSpeed;
        }
        if (motionX > maxSpeed) {
            motionX = maxSpeed;
        }
        if (motionZ < -maxSpeed) {
            motionZ = -maxSpeed;
        }
        if (motionZ > maxSpeed) {
            motionZ = maxSpeed;
        }
    }

    protected void moveMinecartOffRail(int i, int j, int k) {
        motionY -= 0.039999999105930328D;
        double d2 = getMaxCartSpeedOnRail();
        if (!onGround) {
            d2 = getMaxSpeedAirLateral();
        }
        if (motionX < -d2) motionX = -d2;
        if (motionX > d2) motionX = d2;
        if (motionZ < -d2) motionZ = -d2;
        if (motionZ > d2) motionZ = d2;
        double moveY = motionY;
        if (getMaxSpeedAirVertical() > 0 && motionY > getMaxSpeedAirVertical()) {
            moveY = getMaxSpeedAirVertical();
            if (Math.abs(motionX) < 0.3f && Math.abs(motionZ) < 0.3f) {
                moveY = 0.15f;
                motionY = moveY;
            }
        }
        if (onGround) {
            motionX *= 0.5D;
            motionY *= 0.5D;
            motionZ *= 0.5D;
        }
        moveEntity(motionX, moveY, motionZ);
        if (!onGround) {
            motionX *= getDragAir();
            motionY *= getDragAir();
            motionZ *= getDragAir();
        }
    }


    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setDouble("speedLimiter", this.speedLimiter);
        nbttagcompound.setFloat("serverRealRotation", this.serverRealRotation);
        nbttagcompound.setFloat("yawRotation", this.rotationYaw);
        //nbttagcompound.setBoolean("hasSpawnedBogie", this.hasSpawnedBogie);
        //nbttagcompound.setBoolean("needsBogieUpdate", this.needsBogieUpdate);
        nbttagcompound.setBoolean("firstLoad", this.firstLoad);
        nbttagcompound.setFloat("rotation", this.rotation);
        nbttagcompound.setBoolean("brake", isBraking);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        super.readEntityFromNBT(nbttagcompound);
        this.speedLimiter = nbttagcompound.getDouble("speedLimiter");
        this.serverRealRotation = nbttagcompound.getFloat("serverRealRotation");

        if (nbttagcompound.hasKey("yawRotation")) {
            rotationYaw = nbttagcompound.getFloat("yawRotation");
        }
        //if (Math.abs(this.serverRealRotation) > 178.5f) this.serverRealRotation = Math.copySign(178.5f, this.serverRealRotation);
        //this.hasSpawnedBogie = nbttagcompound.getBoolean("hasSpawnedBogie");
        //this.needsBogieUpdate = nbttagcompound.getBoolean("needsBogieUpdate");
        this.firstLoad = nbttagcompound.getBoolean("firstLoad");
        this.rotation = nbttagcompound.getFloat("rotation");
        this.isBraking = nbttagcompound.getBoolean("brake");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getShadowSize() {
        return 0.0F;
    }

    @Override
    public boolean interactFirst(EntityPlayer entityplayer) {
        if (super.interactFirst(entityplayer)) {
            return true;
        }
        if (entityplayer.ridingEntity == this) {
            return false;
        }

        if (lockThisCart(entityplayer.inventory.getCurrentItem(), entityplayer)) {
            return true;
        }

        playerEntity = entityplayer;
        ItemStack itemstack = entityplayer.inventory.getCurrentItem();

        if (this.getTrainLockedFromPacket()) {
            if (!playerEntity.getDisplayName().toLowerCase().equals(this.trainOwner.toLowerCase()) && !canBeRiddenWhileLocked(this)) {
                if (!worldObj.isRemote) entityplayer.addChatMessage(new ChatComponentText("Train is locked"));
                return true;
            } else if (!playerEntity.getDisplayName().toLowerCase().equals(this.trainOwner.toLowerCase()) && entityplayer.inventory.getCurrentItem() != null && entityplayer.inventory.getCurrentItem().getItem() instanceof ItemDye && (this instanceof Locomotive)) {
                if (!worldObj.isRemote) entityplayer.addChatMessage(new ChatComponentText("Train is locked"));
                return true;
            }

        }

        if (itemstack != null && itemstack.getItem() instanceof ItemWrench && this instanceof Locomotive && entityplayer.isSneaking() && !worldObj.isRemote) {
            destination = "";
            entityplayer.addChatMessage(new ChatComponentText("Destination reset"));
            return true;
        }
        if(itemstack != null) {
            ItemStack crowbar = GameRegistry.findItemStack("railcraft","tool.crowbar",1);
            ItemStack crowbar1 = GameRegistry.findItemStack("railcraft","tool.crowbar.reinforced",1);
            if (itemstack == crowbar || itemstack == crowbar1) {
                return false;
            }
        }
        if (itemstack != null && itemstack.hasTagCompound() && getTicketDestination(itemstack) != null && getTicketDestination(itemstack).length() > 0) {
            this.setDestination(itemstack);
            /**
             * ticket are single use but golden ones are multiple uses
             */
            ItemStack ticket = GameRegistry.findItemStack("Railcraft", "railcraft.routing.ticket", 1);
            if (ticket != null && ticket.getItem() != null && itemstack.getItem() == ticket.getItem()) {
                if (--itemstack.stackSize == 0) {
                    entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, null);
                }
            }
            return true;
        }
        /**
         * If the color is valid for the cart, then change it and reduce
         * itemstack size
         */
        if (itemstack != null && itemstack.getItem() instanceof ItemDye) {
            if (SkinRegistry.get(this).size() > 0) {
                for (int i = 0; i < SkinRegistry.get(this).size(); i++) {
                    if (itemstack.getItemDamage() == DepreciatedUtil.getColorFromString(SkinRegistry.get(this).get(i))) {
                        this.setColor(SkinRegistry.get(this).get(i));
                        itemstack.stackSize--;

                        //if (!worldObj.isRemote)PacketHandler.sendPacketToClients(PacketHandler.sendStatsToServer(10,this.uniqueID,trainName ,trainType, this.trainOwner, this.getColorAsString(itemstack.getItemDamage()), (int)posX, (int)posY, (int)posZ),this.worldObj, (int)posX,(int)posY,(int)posZ, 12.0D);

                        return true;
                    }
                }
                if (worldObj.isRemote && ConfigHandler.SHOW_POSSIBLE_COLORS) {
                    String concatColors = ": ";
                    for (int t = 0; t < SkinRegistry.get(this).size(); t++) {
                        concatColors = concatColors.concat(SkinRegistry.get(this).get(t) + ", ");
                    }
                    entityplayer.addChatMessage(new ChatComponentText("Possible colors" + concatColors));
                    entityplayer.addChatMessage(new ChatComponentText("To paint, click me with the right dye"));
                    return true;
                }
            } else if (SkinRegistry.get(this) != null || SkinRegistry.get(this).size() == 0) {
                entityplayer.addChatMessage(new ChatComponentText("No other colors available"));
            }
        }
        if ((trainsOnClick.onClickWithStake(this, itemstack, playerEntity, worldObj))) {
            return true;
        }

        if (itemstack != null && itemstack.getItem() instanceof ItemPaintbrushThing && !entityplayer.isSneaking()) {
            if (SkinRegistry.get(this).size() > 0) {
                entityplayer.openGui(Traincraft.instance, GuiIDs.PAINTBRUSH, entityplayer.getEntityWorld(), this.getEntityId(), -1, (int) this.posZ);
            }

            if (SkinRegistry.get(this).size() == 0) {
                entityplayer.addChatMessage(new ChatComponentText("There are no other colors available."));
            }
            return true;
        }
        else if (itemstack != null && itemstack.getItem() instanceof ItemPaintbrushThing && entityplayer.isSneaking()){
            for (int i = 0; i < SkinRegistry.get(this).size(); i++) {
                if (this.getColor().equals(SkinRegistry.get(this).get(i))) {
                    if(SkinRegistry.get(this).size()>i+1){
                        setColor(i+1);
                    } else {
                        setColor(0);
                    }
                    return true;
                }
            }
        }


        //be sure the player has permission to enter the transport, and that the transport has the main seat open.
        if (getRiderOffsets() != null && getPermissions(playerEntity, false, true) && !entityplayer.isSneaking()) {
            for (EntitySeat seat : seats) {
                //1.12 is stupid, sometimes when the passenger is null, it returns the player
                if (!getWorld().isRemote && (seat.getPassenger() == null
                        || seat.getPassenger().getEntityId()==playerEntity.getEntityId())) {
                    seat.addPassenger(playerEntity);
                    entityplayer.mountEntity(seat);
                    return true;
                }
            }
        }
        if (MinecraftForge.EVENT_BUS.post(new MinecartInteractEvent(this, entityplayer))) {
            return true;
        }
        return worldObj.isRemote;
    }

    @SideOnly(Side.CLIENT)
    private void soundUpdater() {
        if (FMLClientHandler.instance().getClient() != null) {
            this.theSoundManager = FMLClientHandler.instance().getClient().getSoundHandler();
        }
        if (FMLClientHandler.instance().getClient() != null && this.theSoundManager != null && FMLClientHandler.instance().getClient().thePlayer != null) {
            if (sndUpdater != null) {
                sndUpdater.update(FMLClientHandler.instance().getClient().getSoundHandler(), this, FMLClientHandler.instance().getClient().thePlayer);
            }
        }
    }

    /**
     * Applies a velocity to each of the entities pushing them away from each
     * other. Args: entity
     */
    @Override
    public void applyEntityCollision(Entity par1Entity) {
        //if(par1Entity instanceof EntityPlayer)return;
        if (this.bogieFront == null) return;

        if (par1Entity == this) {
            return;
        }
        if (par1Entity instanceof EntityBogie) {
            if (((EntityBogie) par1Entity).entityMainTrainID == this.uniqueID) return;
            if(cartLinked1!=null) {
                if (((EntityBogie) par1Entity).entityMainTrainID == cartLinked1.uniqueID) return;
            }
            if(cartLinked2!=null) {
                if (((EntityBogie) par1Entity).entityMainTrainID == cartLinked2.uniqueID) return;
            }
        }
        if (par1Entity == bogieFront || par1Entity == bogieBack) {
            return;
        }
        if (par1Entity instanceof EntityRollingStock) {
            if (par1Entity == cartLinked1 || par1Entity == cartLinked2) return;
        }

        MinecraftForge.EVENT_BUS.post(new MinecartCollisionEvent(this, par1Entity));
        if (getCollisionHandler() != null) {
            getCollisionHandler().onEntityCollision(this, par1Entity);
            return;
        }
        if (!this.worldObj.isRemote) {
            if (par1Entity != this.riddenByEntity) { //so we don't collide with current entity TODO: convert for seats or remove?
                double d0 = par1Entity.posX - this.posX;
                double d1 = par1Entity.posZ - this.posZ;
                double distancesX[] = new double[4];
                double distancesZ[] = new double[4];
                double euclidian[] = new double[4];
                if (par1Entity instanceof EntityRollingStock) {
                    EntityRollingStock entity = (EntityRollingStock) par1Entity;
                    if (((EntityRollingStock) par1Entity).bogieFront != null && this.bogieFront != null) {


                        distancesX[0] = entity.posX - this.posX;
                        distancesZ[0] = entity.posZ - this.posZ;
                        euclidian[0] = MathHelper.sqrt_double((distancesX[0] * distancesX[0]) + (distancesZ[0] * distancesZ[0]));
                        distancesX[1] = entity.bogieFront.posX - this.posX;
                        distancesZ[1] = entity.bogieFront.posZ - this.posZ;
                        euclidian[1] = MathHelper.sqrt_double((distancesX[1] * distancesX[1]) + (distancesZ[1] * distancesZ[1]));
                        distancesX[2] = entity.posX - this.bogieFront.posX;
                        distancesZ[2] = entity.posZ - this.bogieFront.posZ;
                        euclidian[2] = MathHelper.sqrt_double((distancesX[2] * distancesX[2]) + (distancesZ[2] * distancesZ[2]));
                        distancesX[3] = entity.bogieFront.posX - this.bogieFront.posX;
                        distancesZ[3] = entity.bogieFront.posZ - this.bogieFront.posZ;
                        euclidian[3] = MathHelper.sqrt_double((distancesX[3] * distancesX[3]) + (distancesZ[3] * distancesZ[3]));

                        double min = euclidian[0];
                        int minIndex = 0;
                        for (int k = 0; k < euclidian.length; k++) {
                            if (Math.abs(euclidian[k]) < Math.abs(min)) {
                                min = euclidian[k];
                                minIndex = k;
                            }
                        }
                        d0 = distancesX[minIndex];
                        d1 = distancesZ[minIndex];
                    }
                }
                double d2 = d0 * d0 + d1 * d1;

                if ((par1Entity instanceof AbstractTrains && d2 <= ((AbstractTrains) par1Entity).getLinkageDistance((EntityMinecart) par1Entity) * 0.7 && d2 >= 9.999999747378752E-5D) || (par1Entity instanceof EntityBogie && ((EntityBogie) par1Entity).entityMainTrain != null && d2 <= ((EntityBogie) par1Entity).entityMainTrain.getLinkageDistance((EntityMinecart) par1Entity) * 0.7 && d2 >= 9.999999747378752E-5D) || (!(par1Entity instanceof AbstractTrains) && d2 >= 9.999999747378752E-5D))// >= 9.999999747378752E-5D)
                {
                    d2 = MathHelper.sqrt_double(d2);
                    double d2Clone = d2;
                    if (d0 != 0) {
                        d0 /= d2;
                    } else {
                        d2=0;
                    }
                    if (d1 != 0) {
                        d1 /= d2Clone;
                    } else {
                        d2Clone = 0;
                    }
                    if (d2 != d2Clone && d2 != 0) {
                        d2 = d2Clone;
                    }
                    if (d2 > 1.0D) {
                        d2 = 1.0D;
                    }

                    d0 *= d2;
                    d1 *= d2;
                    d0 *= 0.10000000149011612D;
                    d1 *= 0.10000000149011612D;
                    d0 *= 1.0F - this.entityCollisionReduction;
                    d1 *= 1.0F - this.entityCollisionReduction;
                    d0 *= 0.5D;
                    d1 *= 0.5D;

                    if ((par1Entity instanceof EntityMinecart) && !this.isAttached) {

                        Vec3 vec31 = Vec3.createVectorHelper(MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F), 0.0D, MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F)).normalize();

                        if (Math.abs(Vec3.createVectorHelper(par1Entity.posX - this.posX, 0.0D, par1Entity.posZ - this.posZ).normalize().dotProduct(vec31)) < 0.800000011920929D) {
                            return;
                        }

                        double d9 = par1Entity.motionX + this.motionX;
                        double d8 = par1Entity.motionZ + this.motionZ;

                        if ((par1Entity instanceof Locomotive && !isPoweredCart()) || (((EntityMinecart) par1Entity).isPoweredCart()) && !isPoweredCart()) {

                            this.motionX *= 0.20000000298023224D;
                            this.motionZ *= 0.20000000298023224D;
                            this.addVelocity(par1Entity.motionX - d0, 0.0D, par1Entity.motionZ - d1);
                            if (!(par1Entity instanceof Locomotive)) {
                                par1Entity.motionX *= 0.949999988079071D;
                                par1Entity.motionZ *= 0.949999988079071D;
                            }
                        } else if ((!(par1Entity instanceof Locomotive) && isPoweredCart()) || (!((EntityMinecart) par1Entity).isPoweredCart() && isPoweredCart())) {
                            if (par1Entity instanceof EntityBogie && ((EntityBogie) par1Entity).entityMainTrain != null) {
                                this.motionX *= 0.2;
                                this.motionZ *= 0.2;
                                this.addVelocity(this.motionX + d0 * 3, 0.0D, this.motionZ + d1 * 3);
                                if (this instanceof Locomotive && ((EntityBogie) par1Entity).entityMainTrain instanceof Locomotive) {
                                    this.motionX *= 0;
                                    this.motionZ *= 0;
                                    ((EntityBogie) par1Entity).entityMainTrain.motionX *= 0;
                                    ((EntityBogie) par1Entity).entityMainTrain.motionZ *= 0;
                                }
                            } else {
                                par1Entity.motionX *= 0.20000000298023224D;
                                par1Entity.motionZ *= 0.20000000298023224D;
                                par1Entity.addVelocity(this.motionX + d0, 0.0D, this.motionZ + d1);
                            }
                            if (!(this instanceof Locomotive)) {
                                this.motionX *= 0.949999988079071D;
                                this.motionZ *= 0.949999988079071D;
                            }

                        } else {
                            d9 *= 0.4D;
                            d8 *= 0.4D;

                            if (par1Entity instanceof EntityBogie || par1Entity instanceof Locomotive) {
                                d9 *= -1;//-3
                                d8 *= -1;//-3
                            }

                            this.motionX *= 0.20000000298023224D;
                            this.motionZ *= 0.20000000298023224D;
                            this.addVelocity(d9 - d0, 0.0D, d8 - d1);
                            if (par1Entity instanceof EntityBogie) {
                                //d7/=3;
                                //d8/=3;
                                d9 *= 0.333333333333;
                                d8 *= 0.333333333333;
                            }

                            par1Entity.motionX *= 0.20000000298023224D;
                            par1Entity.motionZ *= 0.20000000298023224D;
                            par1Entity.addVelocity(d9 + d0, 0.0D, d8 + d1);

                        }
                    } else {

                        if (!(par1Entity instanceof EntityItem) && !(par1Entity instanceof EntityPlayer && this instanceof Locomotive) && !(par1Entity instanceof EntityLiving) && !(par1Entity instanceof EntityBogie)) {
                            this.addVelocity(-d0 * 2, 0.0D, -d1 * 2);
                        } else if ((par1Entity instanceof EntityBogie)) {
                            this.addVelocity(-d0, 0.0D, -d1);
                        }/*
                         * else if(par1Entity instanceof EntityBogie){
                         * par1Entity.addVelocity(-d0, 0.0D, -d1);
                         *
                         * }
                         */
                        //if(!(par1Entity instanceof EntityPlayer))par1Entity.addVelocity(d0 / 4.0D, 0.0D, d1 / 4.0D);
                        //par1Entity.setVelocity(0, 0.0D, 0);
                        par1Entity.addVelocity(d0 * 2, 0.0D, d1 * 2);
                        /*
                         * if(this.bogieUtility[0]!=null &&
                         * this.bogieUtility[1]!=null){
                         * this.bogieUtility[0].addVelocity(-d0*2, 0.0D, -d1*2);
                         * this.bogieUtility[1].addVelocity(-d0*2, 0.0D, -d1*2);
                         * }
                         */

                        if (par1Entity instanceof EntityPlayer) {

                            MovingObjectPosition movingobjectposition = new MovingObjectPosition(par1Entity);
                            if (movingobjectposition.entityHit != null) {
                                // float f1 = MathHelper.sqrt_double(this.motionX * this.motionX +
                                // this.motionY * this.motionY + this.motionZ * this.motionZ);
                                // float f7 = MathHelper.sqrt_double(this.motionX * this.motionX +
                                // this.motionZ * this.motionZ);
                                //movingobjectposition.entityHit.setVelocity(-par1Entity.motionX, 0, -par1Entity.motionZ);
                                //movingobjectposition.entityHit.addVelocity(-((par1Entity.motionX * (double) (Math.abs(this.motionX+0.01)) * 2.60000002384185791D)) / (double) f7, 0.00000000000000001D, -(((par1Entity.motionZ * (double) (Math.abs(this.motionZ+0.01)) * 2.60000002384185791D)) / (double) f7));
                                //movingobjectposition.entityHit.addVelocity(-((Math.abs(this.motionX) * (double) 1 * 0.0260000002384185791D)) / (double) f7, 0.00000000000000001D, -(((Math.abs(this.motionZ) * (double) 1 * 0.0260000002384185791D)) / (double) f7));
                                par1Entity.velocityChanged = true;
                            }
                        }

                        if (par1Entity instanceof EntityLiving) {
                            float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ) * 60;
                            //f1 *= 6;//ratio
                            //f1 *= 10;//to get speed in "pseudo m/s"
                            if ((f1 * 3.6) < 35) {//if speed is smaller than 35km/h then don't do any damage but push entities
                                return;
                            }
                            int j1 = (int) Math.ceil((f1) * ((par1Entity instanceof EntityCreeper) ? 100 : 1));
                            par1Entity.attackEntityFrom(TrainsDamageSource.ranOver, j1);
                        }
                    }
                }
            }
        }
    }

    /**
     * To disable linking altogether, return false here.
     *
     * @return True if this cart is linkable.
     */
    @Override
    public boolean isLinkable() {
        return true;
    }

    /**
     * Check called when attempting to link carts.
     *
     * @param cart The cart that we are attempting to link with.
     * @return True if we can link with this cart.
     */
    @Override
    public boolean canLinkWithCart(EntityMinecart cart) {
        return true;
    }

    /**
     * Returns true if this cart has two links or false if it can only link with
     * one cart.
     *
     * @return True if two links
     */
    @Override
    public boolean hasTwoLinks() {
        return true;
    }

    /**
     * Gets the distance at which this cart can be linked. This is called on
     * both carts and added together to determine how close two carts need to be
     * for a successful link. Default = LinkageManager.LINKAGE_DISTANCE
     *
     * @param cart The cart that you are attempting to link with.
     * @return The linkage distance
     */
    @Override
    public float getLinkageDistance(EntityMinecart cart) {
        return this.getOptimalDistance(cart) + 2.4F;
    }

    /**
     * Gets the optimal distance between linked carts. This is called on both
     * carts and added together to determine the optimal rest distance between
     * linked carts. The LinkageManager will attempt to maintain this distance
     * between linked carts at all times. Default =
     * LinkageManager.OPTIMAL_DISTANCE
     * ETERNAL's NOTE: because this is forcing the value of EntityMinecart, it's actually a call to the super but using this instance. Not actually an infinate look like compiler thinks.
     *
     * @param cart The cart that you are linked with.
     * @return The optimal rest distance
     */
    @Override
    public float getOptimalDistance(EntityMinecart cart) {
        return getHitboxSize()[0];
    }

    /**
     * Return false if linked carts have no effect on the velocity of this cart.
     * Use carefully, if you link two carts that can't be adjusted, it will
     * behave as if they are not linked.
     *
     * @param cart The cart doing the adjusting.
     * @return Whether the cart can have its velocity adjusted.
     */
    @Override
    public boolean canBeAdjusted(EntityMinecart cart) {
        return true;
    }

    @Override
    public void onLinkCreated(EntityMinecart cart) {
        linked = true;
    }

    /**
     * Called when a link is broken (usually).
     *
     * @param cart The cart we were linked with.
     */
    @Override
    public void onLinkBroken(EntityMinecart cart) {
        linked = false;
    }

    @Override
    public boolean isLinked() {
        return linked;
    }

    /**
     * Returns true if this cart is self propelled.
     *
     * @return True if powered.
     */
    @Override
    public boolean isPoweredCart() {
        return (isLocomotive());
    }

    /**
     * Returns true if this cart is a storage cart Some carts may have
     * inventories but not be storage carts and some carts without inventories
     * may be storage carts.
     *
     * @return True if this cart should be classified as a storage cart.
     */
    public boolean isStorageCart() {
        return (isFreightCart());
    }

    /**
     * Returns true if this cart can be ridden by an Entity.
     *
     * @return True if this cart can be ridden.
     */
    @Override
    public boolean canBeRidden() {
        return ((isLocomotive() || isPassenger() || isWorkCart()));
    }

    /**
     * Returns true if this cart can currently use rails. This function is
     * mainly used to gracefully detach a minecart from a rail.
     *
     * @return True if the minecart can use rails.
     */
    @Override
    public boolean canUseRail() {
        return canUseRail;
    }

    /**
     * Set whether the minecart can use rails. This function is mainly used to
     * gracefully detach a minecart from a rail.
     *
     * @param use Whether the minecart can currently use rails.
     */
    @Override
    public void setCanUseRail(boolean use) {
        canUseRail = use;
    }

    /**
     * Return false if this cart should not call IRail.onMinecartPass() and
     * should ignore Powered Rails.
     *
     * @return True if this cart should call IRail.onMinecartPass().
     */
    @Override
    public boolean shouldDoRailFunctions() {
        return true;
    }

    protected void applyDragAndPushForces() {
        motionX *= getDragAir();
        motionY *= 0.0D;
        motionZ *= getDragAir();
    }

    /**
     * Carts should return their drag factor here
     *
     * @return The drag rate.
     */
    @Override
    public double getDragAir() {
        return 0.9998D;
    }

    @Override
    public void moveMinecartOnRail(int i, int j, int k, double d) {
        Block id = worldObj.getBlock(i, j, k);
        if (!BlockRailBase.func_150051_a(id)) {
            return;
        }
        railMaxSpeed = ((BlockRailBase) id).getRailMaxSpeed(worldObj, this, i, j, k);
        maxSpeed = Math.max(railMaxSpeed, getMaxCartSpeedOnRail());
        maxSpeed = SpeedHandler.handleSpeed(railMaxSpeed, maxSpeed, this);
        if (this.speedLimiter != 0 && speedWasSet) {
            //maxSpeed *= this.speedLimiter;
            adjustSpeed(maxSpeed, speedLimiter);
        }
        if ((!isLocomotive())) {
            motionX *= 0.99D;
            motionZ *= 0.99D;
        } else {
            motionX *= 1D;
            motionZ *= 1D;
        }
        if (motionX < -maxSpeed) {
            motionX = -maxSpeed;
        }
        if (motionX > maxSpeed) {
            motionX = maxSpeed;
        }
        if (motionZ < -maxSpeed) {
            motionZ = -maxSpeed;
        }
        if (motionZ > maxSpeed) {
            motionZ = maxSpeed;
        }
        moveEntity(motionX, 0.0D, motionZ);
    }

    public void adjustSpeed(float maxSpeed, double limiter) {
        float targetSpeed = (float) (maxSpeed * limiter);
        float targetSpeedX = (float) Math.copySign(targetSpeed, motionX);
        float targetSpeedZ = (float) Math.copySign(targetSpeed, motionZ);
        if (motionX > targetSpeedX && motionX != 0) motionX -= 0.01;
        if (motionZ > targetSpeedZ && motionZ != 0) motionZ -= 0.01;
        if (motionX < targetSpeedX && motionX != 0) motionX += 0.01;
        if (motionZ < targetSpeedZ && motionZ != 0) motionZ += 0.01;
        if ((Math.abs(motionX) < Math.abs(targetSpeedX) + 0.01) && (Math.abs(motionX) > Math.abs(targetSpeedX) - 0.01)) {
            speedWasSet = false;
        }
        if ((Math.abs(motionZ) < Math.abs(targetSpeedZ) + 0.01) && (Math.abs(motionZ) > Math.abs(targetSpeedZ) - 0.01)) {
            speedWasSet = false;
        }
    }


    protected void adjustSlopeVelocities(int i1) {
        if (this instanceof Locomotive) {
            return;
        }
        double d4 = -0.002D;//0.0078125D
        if (i1 == 2) {
            motionX -= d4;
        } else if (i1 == 3) {
            motionX += d4;
        } else if (i1 == 4) {
            motionZ += d4;
        } else if (i1 == 5) {
            motionZ -= d4;
        }
    }

    public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        return (!isDead && entityplayer.getDistanceSqToEntity(this) <= 300D);
    }

    /**
     * Returns the carts max speed. Carts going faster than 1.1 cause issues
     * with chunk loading. This value is compared with the rails max speed to determine
     * the carts current max speed. A normal rails max speed is 0.4.
     *
     * @return Carts max speed.
     */
    @Override
    public float getMaxCartSpeedOnRail() {
        return maxSpeed;
    }

    @Override
    public float getMaxSpeedAirLateral() {
        return maxSpeedAirLateral;
    }

    @Override
    public void setMaxSpeedAirLateral(float value) {
        maxSpeedAirLateral = value;
    }

    @Override
    public float getMaxSpeedAirVertical() {
        return maxSpeedAirVertical;
    }

    @Override
    public void setMaxSpeedAirVertical(float value) {
        maxSpeedAirVertical = value;
    }

    @Override
    public void setDragAir(double value) {
        dragAir = value;
    }

    @Override
    public boolean canOverheat() {
        return false;
    }

    @Override
    public int getOverheatTime() {
        return 0;
    }

    /**
     * returns the middle of the overheat bar in the HUD
     */
    public int getAverageOverheat() {
        return (this.getOverheatTime() + 30) / 2;
    }

    /**
     * client-server communication
     */
    public void setOverheatLevel(int overheatLevel) {
        this.overheatLevel = overheatLevel;
        this.dataWatcher.updateObject(20, overheatLevel);
    }

    /**
     * client-server communication
     */
    public int getOverheatLevel() {
        return (this.dataWatcher.getWatchableObjectInt(20));
    }

    /**
     * @see SpeedHandler description in SpeedHandler
     */
    public double convertSpeed(Locomotive entity) {
        double speed = entity.getCustomSpeed();// speed in m/s
        if (ConfigHandler.REAL_TRAIN_SPEED) {
            speed /= 2;// applying ratio
        } else {
            speed /= 6;
        }
        speed /= 10;
        return speed;
    }

    /**
     * Used in SoundUpdaterRollingStock
     */
    public int getMotionXClient() {
        return (this.dataWatcher.getWatchableObjectInt(14));
    }

    /**
     * Used in SoundUpdaterRollingStock
     */
    public int getMotionZClient() {
        return (this.dataWatcher.getWatchableObjectInt(21));
    }

    @Override
    protected void func_145775_I() {
        int var1 = MathHelper.floor_double(this.boundingBoxSmall.minX + 0.001D);
        int var2 = MathHelper.floor_double(this.boundingBoxSmall.minY + 0.001D);
        int var3 = MathHelper.floor_double(this.boundingBoxSmall.minZ + 0.001D);
        int var4 = MathHelper.floor_double(this.boundingBoxSmall.maxX - 0.001D);
        int var5 = MathHelper.floor_double(this.boundingBoxSmall.maxY - 0.001D);
        int var6 = MathHelper.floor_double(this.boundingBoxSmall.maxZ - 0.001D);

        if (this.worldObj.checkChunksExist(var1, var2, var3, var4, var5, var6)) {
            for (int var7 = var1; var7 <= var4; ++var7) {
                for (int var8 = var2; var8 <= var5; ++var8) {
                    for (int var9 = var3; var9 <= var6; ++var9) {
                        Block var10 = this.worldObj.getBlock(var7, var8, var9);

                        if (var10 != null) {
                            var10.onEntityCollidedWithBlock(this.worldObj, var7, var8, var9, this);
                        }
                    }
                }
            }
        }
    }

    private void setBoundingBoxSmall(double par1, double par3, double par5, float width, float height) {
        float var7 = width * 0.5F;
        this.boundingBoxSmall.setBounds(par1 - var7, par3 - this.yOffset + this.ySize, par5 - var7, par1 + var7, par3 - this.yOffset + this.ySize + height, par5 + var7);
    }

    public float getYaw() {
        return this.rotationYaw;
    }

    public float getPitch() {
        return this.rotationPitch;
    }

    @Override
    public int getMinecartType() {
        return 0;
    }

    @Override
    public List<ItemStack> getItemsDropped() {
        List<ItemStack> items = new ArrayList<ItemStack>();
        TrainRecord train = Traincraft.instance.traincraftRegistry.getTrainRecord(this.getClass());
        if (train != null) {
            items.add(ItemRollingStock.setPersistentData(new ItemStack(getItem()), this, this.getUniqueTrainID(), trainCreator, trainOwner, getColor()));
            return items;
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public Vec3 renderY(double par1, double par3, double par5, double par7) {
        int i = MathHelper.floor_double(par1);
        int j = MathHelper.floor_double(par3);
        int k = MathHelper.floor_double(par5);

        if (worldObj.getBlock(i, j - 1, k) == BlockIDs.tcRail.block || worldObj.getBlock(i, j - 1, k) == BlockIDs.tcRailGag.block) {
            --j;
        } else if (worldObj.getBlock(i, j + 1, k) == BlockIDs.tcRail.block || worldObj.getBlock(i, j + 1, k) == BlockIDs.tcRailGag.block) {
            j++;
        }

        Block l = this.worldObj.getBlock(i, j, k);
        int i1;
        if (l == BlockIDs.tcRail.block || l == BlockIDs.tcRailGag.block) {
            i1 = worldObj.getBlockMetadata(i, j, k);
            if (i1 == 2) {
                i1 = 0;
            } else if (i1 == 3) {
                i1 = 1;
            }
        } else {
            return null;
        }
        if (l != BlockIDs.tcRail.block && l != BlockIDs.tcRailGag.block) {
            par3 = j;

            if (i1 >= 2 && i1 <= 5) {
                par3 = j + 1;
            }
        } else if (l == BlockIDs.tcRail.block || l == BlockIDs.tcRailGag.block) {
            TileEntity tile = worldObj.getTileEntity(i, j, k);
            if (tile != null && tile instanceof TileTCRail) {
                if (((TileTCRail) tile).getType() != null && !TCRailTypes.isSlopeTrack((TileTCRail) tile)) {
                    par3 = j;
                }
            } else if (tile != null && tile instanceof TileTCRailGag) {
                int xOrigin = ((TileTCRailGag) tile).originX;
                int yOrigin = ((TileTCRailGag) tile).originY;
                int zOrigin = ((TileTCRailGag) tile).originZ;
                TileEntity tileOrigin = worldObj.getTileEntity(xOrigin, yOrigin, zOrigin);
                if (tileOrigin != null && (tileOrigin instanceof TileTCRail) && ((TileTCRail) tileOrigin).getType() != null && !TCRailTypes.isSlopeTrack((TileTCRail) tileOrigin)) {
                    par3 = j;
                }
            }
        }
        int[][] aint = matrix[i1];
        double d4 = aint[1][0] - aint[0][0];
        double d5 = aint[1][2] - aint[0][2];
        double d6 = Math.sqrt(d4 * d4 + d5 * d5);
        d4 /= d6;
        d5 /= d6;
        par1 += d4 * par7;
        par5 += d5 * par7;

        if (l != BlockIDs.tcRail.block && l != BlockIDs.tcRailGag.block) {
            if (aint[0][1] != 0 && MathHelper.floor_double(par1) - i == aint[0][0] && MathHelper.floor_double(par5) - k == aint[0][2]) {
                par3 += aint[0][1];
            } else if (aint[1][1] != 0 && MathHelper.floor_double(par1) - i == aint[1][0] && MathHelper.floor_double(par5) - k == aint[1][2]) {
                par3 += aint[1][1];
            }
        }
        return this.func_70489_a(par1, par3, par5);
    }

    //this does weird math for putch on stuff without bogies, that shouldn't even be needed. but kinda is.
    // replace this with a proper atan2 over time later
    @Deprecated
    public Vec3 yVector(double par1, double par3, double par5) {
        if(rotationPoints()[0]!=0){
            return null;
        }
        int i = MathHelper.floor_double(par1);
        int j = MathHelper.floor_double(par3);
        int k = MathHelper.floor_double(par5);
        if (worldObj.getBlock(i, j - 1, k) == BlockIDs.tcRail.block || worldObj.getBlock(i, j - 1, k) == BlockIDs.tcRailGag.block) {
            --j;
        } else if (worldObj.getBlock(i, j + 1, k) == BlockIDs.tcRail.block || worldObj.getBlock(i, j + 1, k) == BlockIDs.tcRailGag.block) {
            j++;
        }

        Block l = this.worldObj.getBlock(i, j, k);
        int i1 = 0;

        /*
         * boolean shouldIgnoreYCoord = false; TileEntity tile =
         * worldObj.getBlockTileEntity(i, j, k); if(tile!=null && tile
         * instanceof TileTCRail){ if(((TileTCRail)tile).getType()!=null &&
         * ((TileTCRail
         * )tile).getType().equals(TrackTypes.MEDIUM_SLOPE.getLabel())){
         * shouldIgnoreYCoord = true; } } if(tile!=null && tile instanceof
         * TileTCRailGag){ int xOrigin = ((TileTCRailGag)tile).originX; int
         * yOrigin = ((TileTCRailGag)tile).originY; int zOrigin =
         * ((TileTCRailGag)tile).originZ; TileEntity tileOrigin =
         * worldObj.getBlockTileEntity(xOrigin, yOrigin, zOrigin);
         * if(tileOrigin!=null && (tileOrigin instanceof TileTCRail) &&
         * ((TileTCRail)tileOrigin).getType()!=null &&
         * ((TileTCRail)tileOrigin).getType
         * ().equals(TrackTypes.MEDIUM_SLOPE.getLabel())){ shouldIgnoreYCoord =
         * true; } }
         */
        if (l == BlockIDs.tcRail.block || l == BlockIDs.tcRailGag.block) {
            //par3 = (double) j;
            double d3 = 0.0D;
            double d4 = i + 0.5D + matrix[i1][0][0] * 0.5D;
            double d6 = k + 0.5D + matrix[i1][0][2] * 0.5D;
            double d10 = (i + 0.5D + matrix[i1][1][0] * 0.5D) - d4;
            double d12 = (k + 0.5D + matrix[i1][1][2] * 0.5D) - d6;

            if (d10 == 0.0D) {
                d3 = par5 - k;
            } else if (d12 == 0.0D) {
                d3 = par1 - i;
            } else {
                double d13 = par1 - d4;
                double d14 = par5 - d6;
                d3 = (d13 * d10 + d14 * d12) * 2.0D;
            }
            return Vec3.createVectorHelper(d4 + d10 * d3, par3, d6 + d12 * d3);
        } else {
            return null;
        }
    }

    public ItemStack[] getInventory() {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void setSeats(EntitySeat seat, int seatNumber){
        if (seats.size() <= seatNumber) {
            seats.add(seat);
        } else {
            seats.set(seatNumber, seat);
        }
    }

    public boolean shouldRiderSit(int seat){
        return this.shouldRiderSit();
    }
    @Override
    public boolean shouldRiderSit(){
        return true;
    }

    /**
     * <h2>Permissions handler</h2>
     * Used to check if the player has permission to do whatever it is the player is trying to do. Yes I could be more vague with that.
     *
     * @param player the player attenpting to interact.
     * @param driverOnly can this action only be done by the driver/conductor?
     * @return if the player has permission to continue
     */
    public boolean getPermissions(EntityPlayer player, boolean driverOnly, boolean decreaseTicketStack) {
        //make sure the player is not null, and be sure that driver only rules are applied.
        if (player ==null){
            return false;
        } else if (driverOnly && (!(player.ridingEntity instanceof EntitySeat) || ! ((EntitySeat) player.ridingEntity).isControlSeat())){
            return false;
        }

        //be sure operators and owners can do whatever
        if ((player.capabilities.isCreativeMode && player.canCommandSenderUseCommand(2, ""))
                || (this.getOwner()!=null && this.getOwner() == player.getGameProfile())) {
            return true;
        }

        /*//if a ticket is needed, like for passenger cars
        if(getBoolean(boolValues.LOCKED) && getRiderOffsets().length>1){
            for(ItemStack stack : player.inventory.mainInventory){
                if(stack.getItem() instanceof ItemKey){
                    for(UUID id : ItemKey.getHostList(stack)){
                        if (id == this.entityUniqueID){
                            if(stack.getItem() instanceof ItemTicket &&decreaseTicketStack) {
                                stack.stackSize--;
                                if (stack.stackSize<=0){
                                    stack=null;
                                }
                            }
                            return true;
                        }
                    }
                }
            }
            return false;
        }*/

        //all else fails, just return if this is locked.
        //return !getBoolean(boolValues.LOCKED);
        return !this.getTrainLockedFromPacket();
    }
    public World getWorld(){ return worldObj;}
}