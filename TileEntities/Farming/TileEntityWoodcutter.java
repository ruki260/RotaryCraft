/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.TileEntities.Farming;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.item.EntityFallingSand;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import Reika.DragonAPI.Instantiable.TreeReader;
import Reika.DragonAPI.Libraries.ReikaEnchantmentHelper;
import Reika.DragonAPI.Libraries.ReikaInventoryHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaPlantHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaTreeHelper;
import Reika.DragonAPI.ModInteract.TwilightForestHandler;
import Reika.DragonAPI.ModRegistry.ModWoodList;
import Reika.RotaryCraft.RotaryCraft;
import Reika.RotaryCraft.Auxiliary.EnchantableMachine;
import Reika.RotaryCraft.Auxiliary.InertIInv;
import Reika.RotaryCraft.Auxiliary.ItemStacks;
import Reika.RotaryCraft.Base.InventoriedPowerReceiver;
import Reika.RotaryCraft.Registry.ConfigRegistry;
import Reika.RotaryCraft.Registry.MachineRegistry;

public class TileEntityWoodcutter extends InventoriedPowerReceiver implements EnchantableMachine, InertIInv {

	private HashMap<Enchantment,Integer> enchantments = new HashMap<Enchantment,Integer>();

	private ItemStack[] inv = new ItemStack[1];

	public int editx;
	public int edity;
	public int editz;
	public double dropx;
	public double dropz;

	/** For the 3x3 area of effect */
	public boolean varyx;
	public boolean varyz;
	public int stepx;
	public int stepz;

	private TreeReader tree = new TreeReader();

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		super.updateTileEntity();
		tickcount++;

		this.getIOSides(world, x, y, z, meta);
		this.getPower(false, false);

		if (power < MINPOWER || torque < MINTORQUE) {
			return;
		}

		if (world.isRemote)
			return;

		if (tree.isEmpty() && this.hasWood()) {
			tree.reset();
			ModWoodList wood = ModWoodList.getModWood(world.getBlockId(editx, edity, editz), world.getBlockMetadata(editx, edity, editz));
			ReikaTreeHelper vanilla = ReikaTreeHelper.getTree(world.getBlockId(editx, edity, editz), world.getBlockMetadata(editx, edity, editz));
			if (wood == ModWoodList.SEQUOIA) {
				for (int i = -32; i < 255; i += 16)
					tree.addSequoia(world, editx, edity+i, editz, RotaryCraft.logger.shouldDebug());
			}
			else if (wood == ModWoodList.DARKWOOD) {
				tree.addDarkForest(world, editx, edity, editz, editx-8, editx+8, editz-8, editz+8, RotaryCraft.logger.shouldDebug());
			}
			else if (wood != null) {
				for (int i = -1; i <= 1; i++) {
					for (int j = -1; j <= 1; j++) {
						//tree.addGenerousTree(world, editx+i, edity, editz+j, 16);
						tree.setModTree(wood);
						tree.addModTree(world, editx+i, edity, editz+j);
					}
				}
			}
			else if (vanilla != null) {
				for (int i = -1; i <= 1; i++) {
					for (int j = -1; j <= 1; j++) {
						//tree.addGenerousTree(world, editx+i, edity, editz+j, 16);
						tree.setTree(vanilla);
						tree.addTree(world, editx+i, edity, editz+j);
					}
				}
			}
			this.checkAndMatchInventory(wood, vanilla);
		}

		int id = world.getBlockId(x, y+1, z);
		if (id != 0) {
			Block b = Block.blocksList[id];
			ReikaItemHelper.dropItems(world, dropx, y-0.25, dropz, b.getBlockDropped(world, x, y+1, z, world.getBlockMetadata(x, y+1, z), this.getEnchantment(Enchantment.fortune)));
			world.setBlock(x, y+1, z, 0);
		}

		RotaryCraft.logger.debug(tree);

		if (tree.isEmpty())
			return;

		if (!this.operationComplete((int)(tickcount*ReikaEnchantmentHelper.getEfficiencyMultiplier(this.getEnchantment(Enchantment.efficiency))), 0) && ConfigRegistry.INSTACUT.getState())
			return;
		tickcount = 0;

		if (!tree.isValidTree())
			return;

		int[] xyz = tree.getNextAndMoveOn();
		int drop = world.getBlockId(xyz[0], xyz[1], xyz[2]);
		int dropmeta = world.getBlockMetadata(xyz[0], xyz[1], xyz[2]);

		if (drop != 0) {
			Material mat = world.getBlockMaterial(xyz[0], xyz[1], xyz[2]);
			if (ConfigRegistry.INSTACUT.getState()) {

				//ReikaItemHelper.dropItems(world, dropx, y-0.25, dropz, dropBlock.getBlockDropped(world, xyz[0], xyz[1], xyz[2], dropmeta, 0));
				this.dropBlocks(world, xyz[0], xyz[1], xyz[2]);
				world.setBlock(xyz[0], xyz[1], xyz[2], 0);

				if (xyz[1] == edity) {
					int idbelow = world.getBlockId(xyz[0], xyz[1]-1, xyz[2]);
					int root = TwilightForestHandler.getInstance().rootID;
					if (ReikaPlantHelper.SAPLING.canPlantAt(world, xyz[0], xyz[1], xyz[2])) {
						ItemStack plant = this.getPlantedSapling();
						if (plant != null) {
							if (inv[0] != null)
								ReikaInventoryHelper.decrStack(0, inv);
							world.setBlock(xyz[0], xyz[1], xyz[2], plant.itemID, plant.getItemDamage(), 3);
						}
					}
					else if (tree.getModTree() == ModWoodList.TIMEWOOD && (idbelow == root || idbelow == 0)) {
						ItemStack plant = this.getPlantedSapling();
						if (plant != null) {
							if (inv[0] != null)
								ReikaInventoryHelper.decrStack(0, inv);
							world.setBlock(xyz[0], xyz[1]-1, xyz[2], Block.dirt.blockID);
							world.setBlock(xyz[0], xyz[1], xyz[2], plant.itemID, plant.getItemDamage(), 3);
						}
					}
				}
			}
			else {
				boolean fall = BlockSand.canFallBelow(world, xyz[0], xyz[1]-1, xyz[2]);
				if (fall) {
					EntityFallingSand e = new EntityFallingSand(world, xyz[0]+0.5, xyz[1]+0.65, xyz[2]+0.5, drop, dropmeta);
					e.fallTime = -2000;
					e.shouldDropItem = false;
					if (!world.isRemote) {
						world.spawnEntityInWorld(e);
					}
					world.setBlock(xyz[0], xyz[1], xyz[2], 0);
				}
				else {

					//ReikaItemHelper.dropItems(world, dropx, y-0.25, dropz, dropBlock.getBlockDropped(world, xyz[0], xyz[1], xyz[2], dropmeta, 0));
					this.dropBlocks(world, xyz[0], xyz[1], xyz[2]);
					world.setBlock(xyz[0], xyz[1], xyz[2], 0);

					if (mat == Material.leaves)
						world.playSoundEffect(x+0.5, y+0.5, z+0.5, "dig.grass", 0.5F+rand.nextFloat(), 1F);
					else
						world.playSoundEffect(x+0.5, y+0.5, z+0.5, "dig.wood", 0.5F+rand.nextFloat(), 1F);

					if (xyz[1] == edity) {
						int idbelow = world.getBlockId(xyz[0], xyz[1]-1, xyz[2]);
						int root = TwilightForestHandler.getInstance().rootID;
						if (ReikaPlantHelper.SAPLING.canPlantAt(world, xyz[0], xyz[1], xyz[2])) {
							ItemStack plant = this.getPlantedSapling();
							if (plant != null) {
								if (inv[0] != null)
									ReikaInventoryHelper.decrStack(0, inv);
								world.setBlock(xyz[0], xyz[1], xyz[2], plant.itemID, plant.getItemDamage(), 3);
							}
						}
						else if (tree.getModTree() == ModWoodList.TIMEWOOD && (idbelow == root || idbelow == 0)) {
							ItemStack plant = this.getPlantedSapling();
							if (plant != null) {
								if (inv[0] != null)
									ReikaInventoryHelper.decrStack(0, inv);
								world.setBlock(xyz[0], xyz[1]-1, xyz[2], Block.dirt.blockID);
								world.setBlock(xyz[0], xyz[1], xyz[2], plant.itemID, plant.getItemDamage(), 3);
							}
						}
					}
				}
			}
		}
	}

	private void checkAndMatchInventory(ModWoodList wood, ReikaTreeHelper vanilla) {
		ItemStack sapling;
		if (wood != null) {
			sapling = wood.getCorrespondingSapling();
			if (!ReikaItemHelper.matchStacks(inv[0], sapling)) {
				this.dumpInventory();
			}
		}
		else if (vanilla != null) {
			sapling = vanilla.getSapling();
			if (!ReikaItemHelper.matchStacks(inv[0], sapling)) {
				this.dumpInventory();
			}
		}
	}

	private void dropBlocks(World world, int x, int y, int z) {
		int drop = world.getBlockId(x, y, z);
		if (drop == 0)
			return;
		int dropmeta = world.getBlockMetadata(x, y, z);
		Block dropBlock = Block.blocksList[drop];
		ItemStack sapling = null;
		int logID = -1;
		if (tree.isVanillaTree()) {
			sapling = tree.getVanillaTree().getSapling();
			logID = tree.getVanillaTree().getLog().itemID;
		}
		else if (tree.isModTree()) {
			sapling = tree.getModTree().getCorrespondingSapling();
			logID = tree.getModTree().getItem().itemID;
		}

		List<ItemStack> drops = dropBlock.getBlockDropped(world, x, y, z, dropmeta, this.getEnchantment(Enchantment.fortune));
		if (drop == logID) {
			if (rand.nextInt(3) == 0) {
				drops.add(ReikaItemHelper.getSizedItemStack(ItemStacks.sawdust.copy(), 1+rand.nextInt(4)));
			}
		}

		for (int i = 0; i < drops.size(); i++) {
			ItemStack todrop = drops.get(i);

			if (ReikaItemHelper.matchStacks(todrop, sapling)) {
				ReikaInventoryHelper.addOrSetStack(todrop, inv, 0);
			}
			else {
				if (!this.chestCheck(todrop))
					ReikaItemHelper.dropItem(world, dropx, yCoord-0.25, dropz, todrop);
			}
		}
	}

	private boolean chestCheck(ItemStack is) {
		TileEntity te = worldObj.getBlockTileEntity(xCoord, yCoord-1, zCoord);
		if (te instanceof IInventory) {
			IInventory ii = (IInventory)te;
			if (ReikaInventoryHelper.addToIInv(is, ii))
				return true;
		}
		return false;
	}

	private void dumpInventory() {
		if (inv[0] == null)
			return;
		ItemStack is = inv[0].copy();
		inv[0] = null;
		this.chestCheck(is);
	}

	public ItemStack getPlantedSapling() {
		if (!this.shouldPlantSapling())
			return null;
		if (tree.isVanillaTree())
			return tree.getVanillaTree().getSapling();
		else if (tree.isModTree())
			return tree.getModTree().getCorrespondingSapling();
		else
			return null;
	}

	private boolean shouldPlantSapling() {
		if (this.hasEnchantment(Enchantment.infinity))
			return true;
		if (tree.isVanillaTree()) {
			return inv[0] != null && inv[0].stackSize > 0 && ReikaItemHelper.matchStacks(inv[0], tree.getVanillaTree().getSapling());
		}
		return inv[0] != null && inv[0].stackSize > 0 && ReikaItemHelper.matchStacks(inv[0], tree.getModTree().getCorrespondingSapling());
	}

	public void getIOSides(World world, int x, int y, int z, int metadata) {
		switch(metadata) {
		case 0:
			readx = x+1;
			readz = z;
			ready = y;
			editx = x-1;
			edity = y;
			editz = z;
			dropx = x+1+0.125;
			dropz = z+0.5;
			stepx = 1;
			stepz = 0;
			varyx = false;
			varyz = true;
			break;
		case 1:
			readx = x-1;
			readz = z;
			ready = y;
			editx = x+1;
			edity = y;
			editz = z;
			dropx = x-0.125;
			dropz = z+0.5;
			stepx = -1;
			stepz = 0;
			varyx = false;
			varyz = true;
			break;
		case 2:
			readz = z+1;
			readx = x;
			ready = y;
			editx = x;
			edity = y;
			editz = z-1;
			dropx = x+0.5;
			dropz = z+1+0.125;
			stepx = 0;
			stepz = 1;
			varyx = true;
			varyz = false;
			break;
		case 3:
			readz = z-1;
			readx = x;
			ready = y;
			editx = x;
			edity = y;
			editz = z+1;
			dropx = x+0.5;
			dropz = z-0.125;
			stepx = 0;
			stepz = -1;
			varyx = true;
			varyz = false;
			break;
		}
		dropx = x+0.5; dropz = z+0.5;
	}

	@Override
	public boolean hasModelTransparency() {
		return false;
	}

	@Override
	public void animateWithTick(World world, int x, int y, int z) {
		if (!this.isInWorld()) {
			phi = 0;
			return;
		}
		if (power < MINPOWER || torque < MINTORQUE)
			return;
		phi += ReikaMathLibrary.doubpow(ReikaMathLibrary.logbase(omega+1, 2), 1.05);
	}

	@Override
	public int getMachineIndex() {
		return MachineRegistry.WOODCUTTER.ordinal();
	}

	@Override
	public int getRedstoneOverride() {
		if (!this.hasWood())
			return 15;
		return 0;
	}

	private boolean hasWood() {
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				int id = worldObj.getBlockId(editx+i, edity, editz+j);
				int meta = worldObj.getBlockMetadata(editx+i, edity, editz+j);
				if (id == Block.wood.blockID)
					return true;
				ModWoodList wood = ModWoodList.getModWood(id, meta);
				RotaryCraft.logger.debug("Retrieved wood "+wood+" from "+id+":"+meta);
				if (wood != null)
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean applyEnchants(ItemStack is) {
		boolean accepted = false;
		if (ReikaEnchantmentHelper.hasEnchantment(Enchantment.fortune, is)) {
			enchantments.put(Enchantment.fortune, ReikaEnchantmentHelper.getEnchantmentLevel(Enchantment.fortune, is));
			accepted = true;
		}
		if (ReikaEnchantmentHelper.hasEnchantment(Enchantment.infinity, is)) {
			enchantments.put(Enchantment.infinity, ReikaEnchantmentHelper.getEnchantmentLevel(Enchantment.infinity, is));
			accepted = true;
		}
		if (ReikaEnchantmentHelper.hasEnchantment(Enchantment.efficiency, is))	 {
			enchantments.put(Enchantment.efficiency, ReikaEnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency, is));
			accepted = true;
		}
		return accepted;
	}

	public ArrayList<Enchantment> getValidEnchantments() {
		ArrayList<Enchantment> li = new ArrayList<Enchantment>();
		li.add(Enchantment.fortune);
		li.add(Enchantment.infinity);
		li.add(Enchantment.efficiency);
		return li;
	}

	@Override
	public HashMap<Enchantment,Integer> getEnchantments() {
		return enchantments;
	}

	@Override
	public boolean hasEnchantment(Enchantment e) {
		return this.getEnchantments().containsKey(e);
	}

	@Override
	public boolean hasEnchantments() {
		for (int i = 0; i < Enchantment.enchantmentsList.length; i++) {
			if (Enchantment.enchantmentsList[i] != null) {
				if (this.getEnchantment(Enchantment.enchantmentsList[i]) > 0)
					return true;
			}
		}
		return false;
	}

	@Override
	public int getEnchantment(Enchantment e) {
		if (!this.hasEnchantment(e))
			return 0;
		else
			return this.getEnchantments().get(e);
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return inv[i];
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		inv[i] = itemstack;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack is) {
		return false;
	}

	@Override
	public void writeToNBT(NBTTagCompound NBT) {
		super.writeToNBT(NBT);

		for (int i = 0; i < Enchantment.enchantmentsList.length; i++) {
			if (Enchantment.enchantmentsList[i] != null) {
				int lvl = this.getEnchantment(Enchantment.enchantmentsList[i]);
				NBT.setInteger(Enchantment.enchantmentsList[i].getName(), lvl);
			}
		}

		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < inv.length; i++)
		{
			if (inv[i] != null)
			{
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte)i);
				inv[i].writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}
		}

		NBT.setTag("Items", nbttaglist);
	}

	@Override
	public void readFromNBT(NBTTagCompound NBT) {
		super.readFromNBT(NBT);

		enchantments = new HashMap<Enchantment,Integer>();
		for (int i = 0; i < Enchantment.enchantmentsList.length; i++) {
			if (Enchantment.enchantmentsList[i] != null) {
				int lvl = NBT.getInteger(Enchantment.enchantmentsList[i].getName());
				enchantments.put(Enchantment.enchantmentsList[i], lvl);
			}
		}

		NBTTagList nbttaglist = NBT.getTagList("Items");
		inv = new ItemStack[this.getSizeInventory()];

		for (int i = 0; i < nbttaglist.tagCount(); i++)
		{
			NBTTagCompound nbttagcompound = (NBTTagCompound)nbttaglist.tagAt(i);
			byte byte0 = nbttagcompound.getByte("Slot");

			if (byte0 >= 0 && byte0 < inv.length)
			{
				inv[byte0] = ItemStack.loadItemStackFromNBT(nbttagcompound);
			}
		}
	}

	@Override
	public void onEMP() {}

	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		return false;
	}
}
