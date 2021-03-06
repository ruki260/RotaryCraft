/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.Registry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import Reika.DragonAPI.Interfaces.IDRegistry;
import Reika.RotaryCraft.RotaryCraft;

/** NEVER ALTER THE ORDER OF THIS LIST!! */
public enum ExtraConfigIDs implements IDRegistry {

	MACHINEPLACER("ItemBlock IDs", "Machine Items", 30616, Item.class),
	MISCCRAFT("Crafting Item IDs", "Heat Ray Crafting Items", 30628, Item.class),
	ENGINECRAFT("Crafting Item IDs", "Engine Crafting Items", 30629, Item.class),
	BORECRAFT("Crafting Item IDs", "Borer Crafting Items", 30630, Item.class),
	SHAFTCRAFT("Crafting Item IDs", "Shaft Crafting Items", 30631, Item.class),
	EXTRACTS("Resource Item IDs", "Extractor Items", 30632, Item.class),
	COMPACTS("Resource Item IDs", "Compactor Items", 30633, Item.class),
	ENGINEITEMS("ItemBlock IDs", "Engine Items", 30634, Item.class),
	POWDERS("Resource Item IDs", "Powders", 30635, Item.class),
	SPAWNERS("ItemBlock IDs", "Spawner", 30636, Item.class),
	PIPEITEMS("ItemBlock IDs", "Pipe Items", 30637, Item.class),
	SHAFTITEMS("ItemBlock IDs", "Shaft Items", 30639, Item.class),
	GEARBOXITEMS("ItemBlock IDs", "Gearbox Items", 30640, Item.class),
	GEARUNITS("Crafting Item IDs", "Gear Units", 30641, Item.class),
	ADVGEARITEMS("ItemBlock IDs", "Advanced Gear Items", 30642, Item.class),
	FLYWHEELITEMS("ItemBlock IDs", "Flywheel Items", 30643, Item.class),
	MODEXTRACTS("Resource Item IDs", "Mod Ore Extractor Items", 30644, Item.class),
	MODINGOTS("Resource Item IDs", "Mod Ore Ingots", 30645, Item.class),

	//ACHIEVEMENT("Extra IDs", "Achievement Base ID", 24000, null),

	DECOBLOCKS("Extra Block IDs", "Deco Block", 450, Block.class),
	BEDROCKSLICE("Extra Block IDs", "Bedrock Slice", 451, Block.class),
	LIGHTBLOCK("Extra Block IDs", "LightBlock", 454, Block.class),
	CANOLA("Extra Block IDs", "Canola", 455, Block.class),
	MININGPIPE("Extra Block IDs", "Mining Pipe", 456, Block.class),
	BRIDGEBLOCK("Extra Block IDs", "Light Bridge", 457, Block.class),
	BLASTPANE("Extra Block IDs", "Blast Glass Pane", 458, Block.class),
	BLASTGLASS("Extra Block IDs", "Blast Glass", 459, Block.class),
	BEAMBLOCK("Extra Block IDs", "Beam Block", 460, Block.class),
	WATERBLOCK("Extra Block IDs", "Water Block", 452, Block.class);

	private String name;
	private String category;
	private int defaultID;
	private Class type;

	public static final ExtraConfigIDs[] idList = ExtraConfigIDs.values();

	private ExtraConfigIDs(String cat, String n, int d, Class c) {
		name = n;
		category = cat;
		defaultID = d;
		type = c;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public int getDefaultID() {
		return defaultID;
	}

	public boolean isBlock() {
		return type == Block.class;
	}

	public boolean isItem() {
		return type == Item.class;
	}

	public int getValue() {
		return RotaryCraft.config.getOtherID(this.ordinal());
	}

	@Override
	public String getConfigName() {
		return this.getName();
	}

	public boolean isDummiedOut() {
		return type == null;
	}
}
