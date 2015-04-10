package vic.mod.integratedcircuits;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import org.apache.logging.log4j.Logger;

import vic.mod.integratedcircuits.compat.BPRedstoneProvider;
import vic.mod.integratedcircuits.compat.NEIAddon;
import vic.mod.integratedcircuits.gate.GateRegistry;
import vic.mod.integratedcircuits.gate.Part7Segment;
import vic.mod.integratedcircuits.gate.PartCircuit;
import vic.mod.integratedcircuits.gate.fmp.PartFactory;
import vic.mod.integratedcircuits.item.Item7Segment;
import vic.mod.integratedcircuits.item.ItemBase;
import vic.mod.integratedcircuits.item.ItemCircuit;
import vic.mod.integratedcircuits.item.ItemFloppyDisk;
import vic.mod.integratedcircuits.item.ItemPCB;
import vic.mod.integratedcircuits.item.ItemScrewdriver;
import vic.mod.integratedcircuits.misc.MiscUtils;
import vic.mod.integratedcircuits.proxy.CommonProxy;
import vic.mod.integratedcircuits.tile.BlockAssembler;
import vic.mod.integratedcircuits.tile.BlockGate;
import vic.mod.integratedcircuits.tile.BlockPCBLayout;
import vic.mod.integratedcircuits.tile.TileEntityAssembler;
import vic.mod.integratedcircuits.tile.TileEntityGate;
import vic.mod.integratedcircuits.tile.TileEntityPCBLayout;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import dan200.computercraft.api.ComputerCraftAPI;

@Mod(modid = "integratedcircuits", dependencies = "required-after:CodeChickenCore; after:ComputerCraft")
public class IntegratedCircuits
{
	public static boolean isPRLoaded = false;
	public static boolean isAWLoaded = false;
	public static boolean isBPLoaded = false;
	public static boolean isFMPLoaded = false;
	public static boolean isRLLoaded = false;
	public static boolean isMFRLoaded = false;
	
	public static Logger logger;
	
	public static GateRegistry.ItemGatePair itemCircuit;
	public static GateRegistry.ItemGatePair item7Segment;
	
	public static ItemFloppyDisk itemFloppyDisk;
	public static ItemPCB itemPCB;
	public static ItemBase itemLaser;
	
	public static ItemBase itemSilicon;
	public static ItemBase itemSiliconDrop;
	public static ItemBase itemCoalCompound;
	public static ItemBase itemPCBChip;
	public static ItemScrewdriver itemScrewdriver;
	
	public static BlockGate blockGate;
	public static BlockPCBLayout blockPCBLayout;
	public static BlockAssembler blockAssembler;
	public static CreativeTabs creativeTab;

	@Instance(Constants.MOD_ID)
	public static IntegratedCircuits instance;

	@SidedProxy(clientSide = "vic.mod.integratedcircuits.proxy.ClientProxy", serverSide = "vic.mod.integratedcircuits.proxy.CommonProxy")
	public static CommonProxy proxy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		logger = event.getModLog();
		logger.info("Loading Integrated Circutis " + Constants.MOD_VERSION);
		
		Config.initialize(event.getSuggestedConfigurationFile());
		
		//Compatibility
		logger.info("Searching for compatible mods");
		logger.info("ProjRed|Transmission: " + (isPRLoaded  = Loader.isModLoaded("ProjRed|Transmission")));
		logger.info("armourersWorkshop: "    + (isAWLoaded  = Loader.isModLoaded("armourersWorkshop")));
		logger.info("bluepower: "            + (isBPLoaded  = Loader.isModLoaded("bluepower")));
		logger.info("ForgeMultipart: "       + (isFMPLoaded = Loader.isModLoaded("ForgeMultipart")));
		logger.info("RedLogic: "             + (isRLLoaded  = Loader.isModLoaded("RedLogic")));
		logger.info("MineFactoryReloaded: "  + (isMFRLoaded = Loader.isModLoaded("MineFactoryReloaded")));
		
		if(isFMPLoaded) logger.info("Forge Multi Part installation found! FMP Compatible gates will be added.");
		
		proxy.preInitialize();
		
		creativeTab = new CreativeTabs(Constants.MOD_ID + ".ctab") 
		{
			@Override
			public Item getTabIconItem() 
			{
				return itemCircuit.getItem();
			}
		};
		
		itemCircuit = GateRegistry.registerGate(new PartCircuit(), ItemCircuit.class);
		item7Segment = GateRegistry.registerGate(new Part7Segment(), Item7Segment.class);
		
		itemFloppyDisk = new ItemFloppyDisk();
		itemPCB = new ItemPCB();
		itemPCBChip = new ItemBase("pcb_chip");
		itemLaser = new ItemBase("laser").setHasIcon(false);
		
		itemSiliconDrop = new ItemBase("silicon_drop");
		itemScrewdriver = new ItemScrewdriver();
		
		if(!(isBPLoaded || isPRLoaded))
		{
			itemSilicon = new ItemBase("silicon");
			itemCoalCompound = new ItemBase("coalcompound");
		}

		blockGate = new BlockGate();
		blockPCBLayout = new BlockPCBLayout();
		blockAssembler = new BlockAssembler();
		
		GameRegistry.registerBlock(blockGate, Constants.MOD_ID + ".gate");
		GameRegistry.registerBlock(blockPCBLayout, Constants.MOD_ID + ".pcblayout");
		GameRegistry.registerBlock(blockAssembler, Constants.MOD_ID + ".assembler");
		
		GameRegistry.registerTileEntity(TileEntityPCBLayout.class, Constants.MOD_ID + ".pcblayoutcad");
		GameRegistry.registerTileEntity(TileEntityAssembler.class, Constants.MOD_ID + ".assembler");
		GameRegistry.registerTileEntity(TileEntityGate.class, Constants.MOD_ID + ".gate");
		
		//Computercraft
		ComputerCraftAPI.registerBundledRedstoneProvider(blockGate);
		ComputerCraftAPI.registerPeripheralProvider(blockGate);
		
		if(Loader.isModLoaded("NotEnoughItems") && !MiscUtils.isServer()) 
			new NEIAddon().initialize();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		if(isFMPLoaded) PartFactory.initialize();
		proxy.initialize();
		
		FMLInterModComms.sendMessage("Waila", "register", "vic.mod.integratedcircuits.compat.WailaAddon.registerAddon");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		IntegratedCircuitsRecipes.loadRecipes();
		
		//Register provider for bluepower
		if(isBPLoaded) new BPRedstoneProvider();
		
		//Tracker
		if(Config.enableTracker) 
		{
			new Thread() {
				@Override
				public void run()
				{
					//I would have done it with commons, but it doesn't let me. So this is pretty much copied from AW
					//https://github.com/RiskyKen/Armourers-Workshop
					try {
						String location = "http://bit.ly/1GIaUA6";
						HttpURLConnection conn = null;
						while(location != null && !location.isEmpty()) {
							URL url = new URL(location);
							if(conn != null) conn.disconnect();

							conn = (HttpURLConnection) url.openConnection();
							conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0");
							conn.setRequestProperty("Referer", "http://" + Constants.MOD_VERSION);
							conn.connect();
							location = conn.getHeaderField("Location");
						}

						if(conn == null) throw new NullPointerException();
    					String newestVersion = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8"))).readLine();
    					// TODO version checker? I don't really like them but we have the information now...
    					logger.info("Your version: {}, Newest version: {}", Constants.MOD_VERSION, newestVersion);
    					conn.disconnect();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.run();
		}
		logger.info("Done! This is an extremely early alpha version so please report any bugs occurring to https://github.com/Victorious3/Integrated-Circuits");
	}
}
