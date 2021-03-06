/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.Auxiliary;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import Reika.DragonAPI.ModRegistry.ModOreList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TabModOre extends CreativeTabs {

	public TabModOre(int position, String tabID) {
		super(position, tabID); //The constructor for your tab
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getIconItemStack() //The item it displays for your tab
	{
		return ExtractorModOres.getDustProduct(ModOreList.COPPER);
	}

	@Override
	public String getTranslatedTabLabel()
	{
		return "Mod Ores"; //The name of the tab ingame
	}
}
