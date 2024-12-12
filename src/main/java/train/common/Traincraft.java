package train.common;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.relauncher.Side;
import ebf.tim.entities.EntitySeat;
import ebf.tim.networking.PacketSeatUpdate;
import ebf.tim.utility.DebugUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import train.common.api.AbstractTrains;
import train.common.api.LiquidManager;
import train.common.blocks.TCBlocks;
import train.common.core.CommonProxy;
import train.common.core.CreativeTabTraincraft;
import train.common.core.TrainModCore;
import train.common.core.handlers.*;
import train.common.core.util.TraincraftUtil;
import train.common.entity.rollingStock.EntityPassengerPassengerCar1;
import train.common.generation.ComponentVillageTrainstation;
import train.common.generation.WorldGenWorld;
import train.common.items.TCItems;
import train.common.library.Info;
import train.common.library.ItemIDs;
import train.common.library.TraincraftRegistry;
import train.common.recipes.AssemblyTableRecipes;

import java.io.File;

@Mod(modid = Info.modID, name = Info.modName, version = Info.modVersion)
public class Traincraft {

    /* TrainCraft instance */
    @Instance(Info.modID)
    public static Traincraft instance;

    /* TrainCraft proxy files */
    @SidedProxy(clientSide = "train.client.core.ClientProxy", serverSide = "train.common.core.CommonProxy")
    public static CommonProxy proxy;

    /* TrainCraft Logger */
    public static Logger tcLog = LogManager.getLogger(Info.modName);

    /**
     * Network Channel to send packets on
     */
    public static SimpleNetworkWrapper modChannel;
    public static SimpleNetworkWrapper keyChannel;
    public static SimpleNetworkWrapper rotationChannel;


    public static SimpleNetworkWrapper slotschannel;
    public static SimpleNetworkWrapper ignitionChannel;
    public static SimpleNetworkWrapper brakeChannel;
    public static SimpleNetworkWrapper lockChannel;
    public static SimpleNetworkWrapper builderChannel;
    public static SimpleNetworkWrapper updateTrainIDChannel = NetworkRegistry.INSTANCE.newSimpleChannel("TrainIDChannel");
    public static SimpleNetworkWrapper updateDestinationChannel = NetworkRegistry.INSTANCE.newSimpleChannel("updateDestnChannel");
    public static SimpleNetworkWrapper updateChannel = NetworkRegistry.INSTANCE.newSimpleChannel("updateChannel");
    public static SimpleNetworkWrapper paintbrushColorChannel;
    public static SimpleNetworkWrapper overlayTextureChannel;
    public static SimpleNetworkWrapper rollingStockLightsChannel;
    public static SimpleNetworkWrapper rollingStockBeaconChannel;
    public static SimpleNetworkWrapper rollingStockDitchLightsChannel;

    public static final SimpleNetworkWrapper itaChannel = NetworkRegistry.INSTANCE.newSimpleChannel("TransmitterAspect");
    public static SimpleNetworkWrapper itsChannel = NetworkRegistry.INSTANCE.newSimpleChannel("TransmitterSpeed");
    //public static  SimpleNetworkWrapper mtcsChannel = NetworkRegistry.INSTANCE.newSimpleChannel("MTCSysSetSpeed");
    public static SimpleNetworkWrapper itnsChannel = NetworkRegistry.INSTANCE.newSimpleChannel("TransmitterNextSpeed");
    public static final SimpleNetworkWrapper mtlChannel = NetworkRegistry.INSTANCE.newSimpleChannel("MTCLevelUpdater");
    public static final SimpleNetworkWrapper msChannel = NetworkRegistry.INSTANCE.newSimpleChannel("MTCStatus");
    public static final SimpleNetworkWrapper mscChannel = NetworkRegistry.INSTANCE.newSimpleChannel("MTCStatusToClient");
    public static final SimpleNetworkWrapper atoChannel = NetworkRegistry.INSTANCE.newSimpleChannel("ATOPacket");
    public static final SimpleNetworkWrapper atoDoSlowDownChannel = NetworkRegistry.INSTANCE.newSimpleChannel("ATODoSlowDown");
    public static final SimpleNetworkWrapper atoDoAccelChannel = NetworkRegistry.INSTANCE.newSimpleChannel("ATODoAccel");
    public static final SimpleNetworkWrapper atoSetStopPoint = NetworkRegistry.INSTANCE.newSimpleChannel("ATOSetStopPoint");
    public static final SimpleNetworkWrapper NCSlowDownChannel = NetworkRegistry.INSTANCE.newSimpleChannel("NCDoSlowDown");
    //public static final SimpleNetworkWrapper ctChannel = NetworkRegistry.INSTANCE.newSimpleChannel("ctmChannel");
    public static final SimpleNetworkWrapper gsfsChannel = NetworkRegistry.INSTANCE.newSimpleChannel("gsfsChannel");
    public static final SimpleNetworkWrapper gsfsrChannel = NetworkRegistry.INSTANCE.newSimpleChannel("gsfsReturnChannel");

    public final TraincraftRegistry traincraftRegistry = new TraincraftRegistry();


    public static File configDirectory;

    /* Creative tab for Traincraft */
    public static CreativeTabTraincraft tcTab, tcTrainTab, tcCommunityTab ;

    public ArmorMaterial armor = EnumHelper.addArmorMaterial("Armor", 5, new int[]{1, 2, 2, 1}, 25);
    public ArmorMaterial armorCloth = EnumHelper.addArmorMaterial("TCcloth", 5, new int[]{1, 2, 2, 1}, 25);
    public ArmorMaterial armorCompositeSuit = EnumHelper.addArmorMaterial("TCsuit", 70, new int[]{2, 6, 5, 2}, 50);
    public static int trainArmor;
    public static int trainCloth;
    public static int trainCompositeSuit;


    public static WorldGenWorld worldGen;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        DebugUtil.dev = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
        tcLog.info("Starting Traincraft " + Info.modVersion + "!");
        /* Config handler */
        configDirectory = event.getModConfigurationDirectory();
        ConfigHandler.init(new File(event.getModConfigurationDirectory(), Info.modName + ".cfg"));

        proxy.configDirectory = event.getModConfigurationDirectory().getAbsolutePath();
        /* Register the KeyBinding Handler */
        proxy.registerKeyBindingHandler();

        /* Register Items, Blocks, ... */
        tcLog.info("Initialize Blocks, Items, ...");
        tcTab = new CreativeTabTraincraft("Traincraft", Info.modID, "trains/train_br80");
        if (ConfigHandler.SPLIT_CREATIVE) {
            tcTrainTab = new CreativeTabTraincraft("Traincraft Trains",  Info.modID,"trains/train_br01");
        }
        trainArmor = proxy.addArmor("armor");
        trainCloth = proxy.addArmor("Paintable");
        trainCompositeSuit = proxy.addArmor("CompositeSuit");

        if (Loader.isModLoaded("ComputerCraft")) {
            try {
                proxy.registerComputerCraftPeripherals();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (hasTCCEAddon()){
            tcCommunityTab = new CreativeTabTraincraft("Traincraft: Community Edition", Info.modID, "trains/train_mogul");
        }

        /* Other Proxy init */
        tcLog.info("Initialize Renderer and Events");
        proxy.registerEvents(event);


        tcLog.info("Finished PreInitialization");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        tcLog.info("Start Initialization");
        TCBlocks.init();
        TCItems.init();
        if (Traincraft.hasTCCEAddon()) {
            TCItems.registerTCCERollingStock();
            Traincraft.tcLog.info("Enabled Traincraft: Community Edition rollingstock");
        }


        TraincraftRegistry.registerTransports("", listSteamTrains());
        TraincraftRegistry.registerTransports("", listFreight());
        TraincraftRegistry.registerTransports("", listPassenger());
        TraincraftRegistry.registerTransports("", listTanker());
        TraincraftRegistry.registerTransports("", listElectricTrains());
        TraincraftRegistry.registerTransports("", listDieselTrains());
        TraincraftRegistry.registerTransports("", listTender());


        proxy.registerTileEntities();

        tcLog.info("Initialize Fluids");
        LiquidManager.getInstance().registerLiquids();

        proxy.registerSounds();
        proxy.setHook(); // Moved file needed to run JLayer, we need to set a hook in order to retrieve it

        GameRegistry.registerFuelHandler(new FuelHandler());
        AchievementHandler.load();
        AchievementPage.registerAchievementPage(AchievementHandler.tmPage);
        GameRegistry.registerWorldGenerator(worldGen = new WorldGenWorld(), 5);

        //Retrogen Handling
        RetrogenHandler retroGen = new RetrogenHandler();
        MinecraftForge.EVENT_BUS.register(retroGen);
        FMLCommonHandler.instance().bus().register(retroGen);

        MapGenStructureIO.func_143031_a(ComponentVillageTrainstation.class, "Trainstation");

        //proxy.getCape();

        /* GUI handler initiation */
        tcLog.info("Initialize Gui");
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
        FMLCommonHandler.instance().bus().register(new CraftingHandler());

        /* Ore dictionary */
        OreHandler.registerOres();

        /* Recipes */
        tcLog.info("Initialize Recipes");
        RecipeHandler.initBlockRecipes();
        RecipeHandler.initItemRecipes();
        RecipeHandler.initSmeltingRecipes();
        AssemblyTableRecipes.recipes();

        /* Register the liquids */
        EntityHandler.init();



        /* Liquid FX */
        proxy.registerTextureFX();

        /*Trainman Villager*/
        tcLog.info("Initialize Station Chief Villager");
        VillagerRegistry.instance().registerVillagerId(ConfigHandler.TRAINCRAFT_VILLAGER_ID);
        VillagerTraincraftHandler villageHandler = new VillagerTraincraftHandler();
        VillagerRegistry.instance().registerVillageCreationHandler(villageHandler);
        proxy.registerVillagerSkin(ConfigHandler.TRAINCRAFT_VILLAGER_ID, "station_chief.png");
        VillagerRegistry.instance().registerVillageTradeHandler(ConfigHandler.TRAINCRAFT_VILLAGER_ID, villageHandler);
        Traincraft.updateChannel.registerMessage(PacketSeatUpdate.Handler.class, PacketSeatUpdate.class, 8, Side.CLIENT);
        Traincraft.updateChannel.registerMessage(PacketSeatUpdate.Handler.class, PacketSeatUpdate.class, 9, Side.SERVER);


        proxy.registerBookHandler();
        proxy.registerPlayerScaler();

        /* Networking and Packet initialisation, apparently this needs to be in init to prevent conflicts */
        PacketHandler.init();
        proxy.registerRenderInformation();



        traincraftRegistry.init();

        tcLog.info("Finished Initialization");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        tcLog.info("Start to PostInitialize");
        TraincraftRegistry.endRegistration();

        tcLog.info("Activation Mod Compatibility");
        TrainModCore.ModsLoaded();

        trainConverter.write();

        tcLog.info("Finished PostInitialization");
    }

    @EventHandler
    public void serverStop(FMLServerStoppedEvent event) {
        CommonProxy.killAllStreams();
    }

    public static boolean hasComputerCraft() {
        return Loader.isModLoaded("ComputerCraft");
    }

    public static boolean hasNotEnoughItems() {
        return Loader.isModLoaded("NotEnoughItems");
    }

    public static boolean hasRailcraft() {
        return Loader.isModLoaded("Railcraft");
    }

    public static boolean hasTCCEAddon() {return Loader.isModLoaded("tcce");}




    public static AbstractTrains[] listElectricTrains() {
        return new AbstractTrains[]{};
    }
    public static AbstractTrains[] listDieselTrains() {
        return new AbstractTrains[]{};
    }
    public static AbstractTrains[] listSteamTrains() {
        return new AbstractTrains[]{};
    }
    public static AbstractTrains[] listPassenger() {
        return new AbstractTrains[]{new EntityPassengerPassengerCar1(null)};
    }
    public static AbstractTrains[] listFreight() {
        return new AbstractTrains[]{};
    }
    public static AbstractTrains[] listTanker() {
        return new AbstractTrains[]{};
    }
    public static AbstractTrains[] listTender() {
        return new AbstractTrains[]{};
    }


}
