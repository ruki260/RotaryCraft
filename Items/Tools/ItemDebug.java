/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.Items.Tools;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.world.World;
import Reika.DragonAPI.Libraries.IO.ReikaChatHelper;
import Reika.RotaryCraft.Base.ItemRotaryTool;
import Reika.RotaryCraft.Base.RotaryCraftTileEntity;
import Reika.RotaryCraft.Registry.MachineRegistry;
import Reika.RotaryCraft.TileEntities.TileEntityBeltHub;
import Reika.RotaryCraft.TileEntities.TileEntityReservoir;
import Reika.RotaryCraft.TileEntities.Farming.TileEntityFan;
import Reika.RotaryCraft.TileEntities.Farming.TileEntitySprinkler;
import Reika.RotaryCraft.TileEntities.Piping.TileEntityHose;
import Reika.RotaryCraft.TileEntities.Piping.TileEntityPipe;
import Reika.RotaryCraft.TileEntities.Processing.TileEntityExtractor;
import Reika.RotaryCraft.TileEntities.Processing.TileEntityPulseFurnace;
import Reika.RotaryCraft.TileEntities.Production.TileEntityBlastFurnace;
import Reika.RotaryCraft.TileEntities.Production.TileEntityEngine;
import Reika.RotaryCraft.TileEntities.Production.TileEntityFractionator;
import Reika.RotaryCraft.TileEntities.Production.TileEntityObsidianMaker;
import Reika.RotaryCraft.TileEntities.Production.TileEntityPump;
import Reika.RotaryCraft.TileEntities.Transmission.TileEntityGearBevel;
import Reika.RotaryCraft.TileEntities.Transmission.TileEntityGearbox;
import Reika.RotaryCraft.TileEntities.Transmission.TileEntityShaft;
import Reika.RotaryCraft.TileEntities.Weaponry.TileEntityTNTCannon;

public class ItemDebug extends ItemRotaryTool {

	public ItemDebug(int ID, int tex) {
		super(ID, tex);
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int s, float par8, float par9, float par10) {
		ReikaChatHelper.clearChat();
		if (!player.isSneaking()) {
			ReikaChatHelper.writeBlockAtCoords(world, x, y, z);
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if (te instanceof RotaryCraftTileEntity)
				ReikaChatHelper.write("Tile Entity Direction Data: "+(((RotaryCraftTileEntity)te).getBlockMetadata()+1)+" of "+((RotaryCraftTileEntity)te).getMachine().getNumberDirections());
			ReikaChatHelper.write("Additional Data (Meaning differs per machine):");
		}
		MachineRegistry m = MachineRegistry.getMachine(world, x, y, z);
		if (m == MachineRegistry.BEVELGEARS) {
			TileEntityGearBevel tile = (TileEntityGearBevel)world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				ReikaChatHelper.write(String.format("%d", tile.direction));
			}
		}
		if (m == MachineRegistry.BLASTFURNACE) {
			TileEntityBlastFurnace tile = (TileEntityBlastFurnace)world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				ReikaChatHelper.write(String.format("Temperature: %dC", tile.getTemperature()));
				if (player.isSneaking()) {
					tile.addTemperature(tile.MAXTEMP-tile.getTemperature());
				}
			}
		}
		if (m == MachineRegistry.BELT) {
			TileEntityBeltHub tile = (TileEntityBeltHub)world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				ReikaChatHelper.write(tile.getDistanceToTarget()+" @ "+tile.getBeltDirection());
			}
		}
		if (m == MachineRegistry.HOSE) {
			TileEntityHose tile = (TileEntityHose)world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				ReikaChatHelper.write(String.format("%d", tile.getLiquidLevel()));
			}
		}
		if (world.getBlockId(x, y, z) == Block.mobSpawner.blockID) {
			TileEntityMobSpawner tile = (TileEntityMobSpawner)world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				MobSpawnerBaseLogic lgc = tile.getSpawnerLogic();
				lgc.spawnDelay = 0;
			}
		}
		if (m == MachineRegistry.PIPE) {
			TileEntityPipe tile = (TileEntityPipe)world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				ReikaChatHelper.write(String.format("%d  %d  %d", tile.getLiquidType(), tile.getLiquidLevel(), tile.getPressure()));
			}
		}
		if (m == MachineRegistry.PUMP) {
			TileEntityPump tile = (TileEntityPump)world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				ReikaChatHelper.write(String.format("%d  %d", tile.getLevel() <= 0 ? 0 : tile.getLiquid().getID(), tile.getLevel()));
			}
		}
		if (m == MachineRegistry.RESERVOIR) {
			TileEntityReservoir tile = (TileEntityReservoir)world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				ReikaChatHelper.write(String.format("%s  %d", tile.getFluid().getLocalizedName(), tile.getLevel()));
			}
		}
		if (m == MachineRegistry.EXTRACTOR) {
			TileEntityExtractor tile = (TileEntityExtractor)world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				ReikaChatHelper.write(String.format("%d", tile.getLevel()));
			}
		}
		if (m == MachineRegistry.SPRINKLER) {
			TileEntitySprinkler tile = (TileEntitySprinkler)world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				ReikaChatHelper.write(String.format("%d  %d", tile.getWater(), tile.getPressure()));
			}
		}
		if (m == MachineRegistry.OBSIDIAN) {
			TileEntityObsidianMaker tile = (TileEntityObsidianMaker)world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				ReikaChatHelper.write(String.format("%d  %d  %d", tile.getWater(), tile.getLava(), tile.temperature));
			}
			if (player.isSneaking()) {
				tile.setLava(tile.CAPACITY);
				tile.setWater(tile.CAPACITY);
				ReikaChatHelper.write("Filled to capacity.");
			}
		}
		if (m == MachineRegistry.TNTCANNON) {
			TileEntityTNTCannon tile = (TileEntityTNTCannon)world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				if (player.isSneaking()) {
					if (tile.isCreative) {
						tile.isCreative = false;
						ReikaChatHelper.write("Set to default mode.");
					}
					else {
						tile.isCreative = true;
						ReikaChatHelper.write("Set to infinite-TNT mode.");
					}
				}
			}
		}
		if (m == MachineRegistry.PULSEJET) {
			TileEntityPulseFurnace tile = (TileEntityPulseFurnace)world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				ReikaChatHelper.write(String.format("%d  %d  %d", tile.getWater(), tile.temperature, tile.getFuel()));
				if (player.isSneaking()) {
					tile.setFuel(tile.MAXFUEL);
					tile.setWater(tile.CAPACITY);
					ReikaChatHelper.write("Filled to capacity.");
				}
			}
		}
		if (m == MachineRegistry.FRACTIONATOR) {
			TileEntityFractionator tile = (TileEntityFractionator)world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				ReikaChatHelper.write(String.format("%d", tile.getFuelLevel()));
			}
		}
		if (m == MachineRegistry.FAN) {
			TileEntityFan tile = (TileEntityFan)world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				ReikaChatHelper.write(String.format("%d %d %d", tile.xstep, tile.ystep, tile.zstep));
			}
		}
		if (m == MachineRegistry.ENGINE) {
			TileEntityEngine tile = (TileEntityEngine)world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				ReikaChatHelper.write(String.format("%d  %d", tile.getWater(), tile.temperature));
			}
			if (player.isSneaking()) {
				tile.addFuel(tile.FUELCAP);
				tile.additives = tile.FUELCAP/1000;
				tile.addWater(tile.CAPACITY);
				ReikaChatHelper.write("Filled to capacity.");
				tile.omega = tile.type.getSpeed();
			}
		}
		if (m == MachineRegistry.SHAFT) {
			TileEntityShaft tile = (TileEntityShaft)world.getBlockTileEntity(x, y, z);
			if (tile != null) {
				ReikaChatHelper.write(String.format("%d %d %d %d", tile.readomega[0], tile.readomega[1], tile.readtorque[0], tile.readtorque[1]));
			}
		}
		if (m == MachineRegistry.GEARBOX) {
			TileEntityGearbox tile = (TileEntityGearbox)world.getBlockTileEntity(x, y, z);
			if (player.isSneaking()) {
				tile.damage = 0;
				tile.setLubricant(tile.MAXLUBE);
				ReikaChatHelper.write("Filled to capacity.");
			}
		}

		return true;
	}
}
