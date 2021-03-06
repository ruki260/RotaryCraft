/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.Items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.RotaryCraft.RotaryCraft;
import Reika.RotaryCraft.API.TensionStorage;
import Reika.RotaryCraft.Base.ItemBasic;
import Reika.RotaryCraft.Registry.ItemRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemCoil extends ItemBasic implements TensionStorage
{
	public ItemCoil(int ID, int tex) {
		super(ID, tex);
		maxStackSize = 1;
		this.setCreativeTab(RotaryCraft.tabRotaryItems);
		hasSubtypes = true;
		this.setMaxDamage(0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List) //Adds the metadata blocks to the creative inventory
	{
		for (int i = 0; i < 30000; i++)
			if (ReikaMathLibrary.isPowerOf(i, 2) || i == 0)
				par3List.add(new ItemStack(par1, 1, i));
		par3List.add(new ItemStack(par1, 1, 24000));
		par3List.add(new ItemStack(par1, 1, 30000));
	}

	@Override
	public String getUnlocalizedName(ItemStack is)
	{
		int d = is.getItemDamage();
		return super.getUnlocalizedName() + "." + String.valueOf(d);
	}

	@Override
	public int getStiffness(ItemStack is) {
		ItemRegistry i = ItemRegistry.getEntry(is);
		switch(i) {
		case STRONGCOIL:
			return 16;
		default:
			return 1;
		}
	}

	@Override
	public int getPowerScale(ItemStack is) {
		ItemRegistry i = ItemRegistry.getEntry(is);
		switch(i) {
		case STRONGCOIL:
			return 4;
		default:
			return 1;
		}
	}
}
