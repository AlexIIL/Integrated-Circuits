package moe.nightfall.vic.integratedcircuits;

import moe.nightfall.vic.integratedcircuits.item.*;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import moe.nightfall.vic.integratedcircuits.tile.BlockAssembler;
import moe.nightfall.vic.integratedcircuits.tile.BlockCAD;
import moe.nightfall.vic.integratedcircuits.tile.BlockPrinter;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityAssembler;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityPrinter;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public final class Content {

	public static Item itemSocket;
	public static Item itemSocketFMP;
	public static Item itemCircuit;
	public static Item item7Segment;
	public static Item itemFloppyDisk;
	public static Item itemPCB;
	public static Item itemPCBPrint;

	public static Item itemLaser;
	public static Item itemSolderingIron;
	public static Item itemSilicon;
	public static Item itemSiliconDrop;
	public static Item itemCoalCompound;
	public static Item itemPCBChip;
	public static Item itemScrewdriver;

	public static Block blockSocket;
	public static Block blockPCBLayout;
	public static Block blockAssembler;
	public static Block blockPrinter;

	private Content() {
	};

	public static void init() {

		itemSocket = new ItemSocket();
		//if (IntegratedCircuits.isMCMPLoaded)
			//Content.itemSocketFMP = new ItemSocketMCMP();

		itemCircuit = new ItemCircuit();
		item7Segment = new Item7Segment();
		itemFloppyDisk = new ItemFloppyDisk();
		itemPCB = new ItemPCB();
		itemPCBChip = new ItemBase("pcb_chip");
		itemPCBPrint = new ItemPCBPrint();
		itemLaser = new ItemBase("laser").setHasIcon(false);

		itemSolderingIron = new ItemBase("soldering_iron").setMaxDamage(25).setMaxStackSize(1).setNoRepair();

		itemSiliconDrop = new ItemBase("silicon_drop");
		itemScrewdriver = new ItemScrewdriver();

		if (!(IntegratedCircuits.isBPLoaded || IntegratedCircuits.isPRLoaded)) {
			Content.itemSilicon = new ItemBase("silicon");
			Content.itemCoalCompound = new ItemBase("coalcompound");
		}

		blockPCBLayout = new BlockCAD();
		blockAssembler = new BlockAssembler();
		blockPrinter = new BlockPrinter();

		GameRegistry.register(Content.blockPCBLayout.setRegistryName("pcblayoutcad"));
		GameRegistry.register(Content.blockAssembler.setRegistryName("assembler"));
		GameRegistry.register(Content.blockPrinter.setRegistryName("pcbprinter"));

		GameRegistry.register(new ItemBlock(Content.blockPCBLayout).setRegistryName(Content.blockPCBLayout.getRegistryName()));
		GameRegistry.register(new ItemBlock(Content.blockAssembler), Content.blockAssembler.getRegistryName());
		GameRegistry.register(new ItemBlock(Content.blockPrinter), Content.blockPrinter.getRegistryName());

		GameRegistry.registerTileEntity(TileEntityCAD.class, Constants.MOD_ID + ".pcblayoutcad");
		GameRegistry.registerTileEntity(TileEntityAssembler.class, Constants.MOD_ID + ".assembler");
		GameRegistry.registerTileEntity(TileEntityPrinter.class, Constants.MOD_ID + ".pcbprinter");
	}

	public static void registerIcons() {
		((ItemBase)itemScrewdriver).registerIcons();
		((ItemBase)itemSilicon).registerIcons();
		((ItemBase)itemSiliconDrop).registerIcons();
		((ItemBase)itemPCB).registerIcons();
		((ItemBase)itemPCBChip).registerIcons();
		((ItemBase)itemCoalCompound).registerIcons();
		((ItemBase)itemSolderingIron).registerIcons();
		((ItemBase)itemFloppyDisk).registerIcons();

		Item itemBlockPCBLayout = Item.getItemFromBlock(Content.blockPCBLayout);
		ModelLoader.setCustomModelResourceLocation(itemBlockPCBLayout, 0, new ModelResourceLocation(Content.blockPCBLayout.getRegistryName(), "inventory"));
		Item itemBlockAssembler = Item.getItemFromBlock(Content.blockAssembler);
		ModelLoader.setCustomModelResourceLocation(itemBlockAssembler, 0, new ModelResourceLocation(Content.blockAssembler.getRegistryName(), "inventory"));
	}
}
