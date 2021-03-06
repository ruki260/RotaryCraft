/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.TileEntities;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingSand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import Reika.DragonAPI.Libraries.ReikaSpawnerHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.RotaryCraft.RotaryCraft;
import Reika.RotaryCraft.Base.TileEntityPowerReceiver;
import Reika.RotaryCraft.Registry.MachineRegistry;
import Reika.RotaryCraft.Registry.SoundRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class TileEntityPileDriver extends TileEntityPowerReceiver {


	public int step = 0;
	private int step2 = 0;
	private boolean climbing  = false;
	private boolean active = false;
	private boolean smashed = false;

	public static final int BASEPOWER = 16384; //16 kW per meter to lift the hammer (P = mg x dh/dt)
	public static final int MINTIME = 1;
	public static final int BASESPEED = 300;

	public static int BITMETA = 4;

	public void addNausea(World world, int x, int y, int z) {
		AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x, y, z, x+1, y+1, z+1).expand(15, 15, 15); // 5m radius
		List sick = world.getEntitiesWithinAABB(EntityPlayer.class, box);
		for (int k = 0; k < sick.size(); k++) {
			EntityPlayer ep = (EntityPlayer)sick.get(k);
			if (ep != null)
				ep.addPotionEffect(new PotionEffect(Potion.confusion.id, 150, 10));
		}
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		super.updateTileEntity();
		this.getIOSides(world, x, y, z, meta);
		this.getPower(true, false);
		int speed = BASESPEED;
		int minpower = BASEPOWER*(step+1);
		//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d  %d  %d", readx, ready, readz));
		if (power < minpower || torque < MINTORQUE) {
			//this.climbing = true;
			return;
		}
		if (power > minpower)
			speed = ReikaMathLibrary.extrema(BASESPEED/((int)(power/minpower)), MINTIME, "max");
		//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d  %d", step, step2));
		tickcount++;

		if (!this.drawPile3(world, x, y, z, speed) && step != 0)
			return;
		climbing = true;
		tickcount = 0;
		if (this.smash(world, x, y-step-1, z))
			step += 1;
		//if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		this.bounce(world, x, y-step-1, z);
		this.dealDamage(world, x, y-step-1, z);
		this.addNausea(world, x, y-step-1, z);
		SoundRegistry.PILEDRIVER.playSoundAtBlock(world, x, y, z, 1, 1);
	}

	public void bounce(World world, int x, int y, int z) { //bounce entities
		AxisAlignedBB zone = AxisAlignedBB.getBoundingBox(x-2, y, z-2, x+3, y+1, z+3).expand(24, 24, 24);
		List inzone = world.getEntitiesWithinAABB(Entity.class, zone);
		//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d", inzone.size()));
		for (int i = 0; i < inzone.size(); i++) {
			Entity ent = (Entity)inzone.get(i);
			if (ent != null) {
				if (ent.onGround && !world.isRemote)
					ent.motionY += 0.5 / ReikaMathLibrary.doubpow(ReikaMathLibrary.py3d(ent.posX-x, ent.posY-y, ent.posZ-z), 0.5);
				//ent.motionX += (-0.05+par5Random.nextFloat()*0.1F);
				//ent.motionZ += (-0.05+par5Random.nextFloat()*0.1F);
				ent.velocityChanged = true;
			}
		}
	}

	public void getIOSides(World world, int x, int y, int z, int metadata) {
		switch(metadata) {
		case 1:
			readx = xCoord+1;
			readz = zCoord;
			readx2 = xCoord-1;
			readz2 = zCoord;
			break;
		case 0:
			readz = zCoord-1;
			readx = xCoord;
			readx2 = xCoord;
			readz2 = zCoord+1;
			break;
		}
		ready = yCoord;
		ready2 = yCoord;
	}

	public void dealDamage(World world, int x, int y, int z) {
		AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x, y, z, x+1, y+1, z+1).expand(0.5, 2, 0.5);
		List killed = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
		for (int i = 0; i < killed.size(); i++) {
			EntityLivingBase el = (EntityLivingBase)killed.get(i);
			if (el != null) {
				float dmg = el.getMaxHealth()*el.getTotalArmorValue();
				if (dmg <= 0)
					dmg = Float.MAX_VALUE;
				el.attackEntityFrom(DamageSource.inWall, dmg); //will kill anything
			}
		}
	}

	public void breakGlass(World world, int x, int y, int z) {
		//if (par5Random.nextInt(5) > 0)
		//return;
		int range = 5;
		for (int i = -range; i <= range; i++) {
			for (int j = -range; j <= range; j++) {
				for (int k = -range; k <= range; k++) {
					int id = world.getBlockId(x+i, y+j, z+k);
					this.breakGlass_do(world, x+i, y+j, z+k, id);
					//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d", id));
				}
			}
		}
		AxisAlignedBB nearby = AxisAlignedBB.getBoundingBox(x-range, y-range, z-range, x+1+range, y+1+range, z+1+range);
		List inzone = world.getEntitiesWithinAABB(EntityPainting.class, nearby);
		for (int p = 0; p < inzone.size(); p++) {
			EntityPainting ep = (EntityPainting)inzone.get(p);
			if (ep != null)
				ep.attackEntityFrom(DamageSource.outOfWorld, 100);
		}
		inzone = world.getEntitiesWithinAABB(EntityItemFrame.class, nearby);
		for (int p = 0; p < inzone.size(); p++) {
			EntityItemFrame eif = (EntityItemFrame)inzone.get(p);
			if (eif != null)
				eif.attackEntityFrom(DamageSource.outOfWorld, 100);
		}
	}

	public void breakGlass_do(World world, int x, int y, int z, int id) {
		int dropid = -1;
		int dropmeta = world.getBlockMetadata(x, y, z);
		if (id == Block.glass.blockID || id == Block.thinGlass.blockID || id == Block.glowStone.blockID) {
			Block.blocksList[id].dropBlockAsItem(world, x, y, z, dropmeta, 0);
			ReikaWorldHelper.legacySetBlockWithNotify(world, x, y, z, 0);
			world.playSoundEffect(x+0.5, y+0.5, z+0.5, "random.glass", 0.5F, 1F);
		}
		if (id == Block.cactus.blockID || id == Block.reed.blockID || id == Block.vine.blockID ||
				id == Block.waterlily.blockID || id == Block.tallGrass.blockID || id == Block.sapling.blockID ||
				id == Block.flowerPot.blockID || id == Block.skull.blockID) {
			Block.blocksList[id].dropBlockAsItem(world, x, y, z, dropmeta, 0);
			ReikaWorldHelper.legacySetBlockWithNotify(world, x, y, z, 0);
		}
		if (id == Block.ice.blockID) {
			ReikaWorldHelper.legacySetBlockWithNotify(world, x, y, z, Block.waterMoving.blockID);
			world.playSoundEffect(x+0.5, y+0.5, z+0.5, "random.glass", 0.5F, 1F);
			dropid = Block.ice.blockID;
		}
		if (id == Block.web.blockID) {
			ReikaWorldHelper.legacySetBlockWithNotify(world, x, y, z, 0);
			dropid = Item.silk.itemID;
		}/*
    	if (id == Block.tnt.blockID) {
    		ReikaWorldHelper.legacySetBlockWithNotify(world, x, y, z, 0);
            EntityTNTPrimed var6 = new EntityTNTPrimed(world, x+0.5D, y+0.5D, z+0.5D);
            world.spawnEntityInWorld(var6);
            world.playSoundAtEntity(var6, "random.fuse", 1.0F, 1.0F);
    	}*/
		if (id == Block.sand.blockID || id == Block.gravel.blockID)
			this.makeFall(world, x, y, z, id);
		/*if (id == RotaryCraft.miningpipe.blockID && dropmeta != 4)
			ReikaWorldHelper.legacySetBlockWithNotify(world, x, y, z, 0);*/
		if (dropid == -1)
			return;
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
			return;
		ItemStack is = new ItemStack(dropid, 1, 0);
		EntityItem ent = new EntityItem(world, x, y, z, is);
		world.spawnEntityInWorld(ent);
	}

	public void makeFall(World world, int x, int y, int z, int id) {
		BlockSand tofall = (BlockSand)Block.blocksList[id];
		if (tofall.canFallBelow(world, x, y-1, z)) {
			byte var8 = 32;
			if (!tofall.fallInstantly && world.checkChunksExist(x - var8, y - var8, z - var8, x + var8, y + var8, z + var8)) {
				if (!world.isRemote) {
					EntityFallingSand var9 = new EntityFallingSand(world, x + 0.5F, y + 0.5F, z + 0.5F, tofall.blockID, world.getBlockMetadata(x, y, z));
					//tofall.onStartFalling(var9);
					world.spawnEntityInWorld(var9);
				}
			}
			else {
				ReikaWorldHelper.legacySetBlockWithNotify(world, x, y, z, 0);
				while (tofall.canFallBelow(world, x, y-1, z) && y > 0)
					--y;
				if (y > 0)
					ReikaWorldHelper.legacySetBlockWithNotify(world, x, y, z, tofall.blockID);
			}
		}
	}

	public int[] getBlockProduct(int id, int meta) {
		int[] to = {0,0};
		if (id == Block.bedrock.blockID) //does not break bedrock
			to[0] = id;
		if (id == Block.stone.blockID)
			to[0] = Block.cobblestone.blockID;
		if (id == Block.stoneBrick.blockID && meta == 0) {
			to[0] = id;
			to[1] = 2;
		}
		if (id == Block.obsidian.blockID) {
			if (meta < 4) {
				to[0] = id;
				to[1] = meta+1;
			}
			else {

			}
		}
		if (id == Block.waterMoving.blockID || id == Block.waterStill.blockID ||
				id == Block.lavaMoving.blockID || id == Block.lavaStill.blockID) {
			to[0] = id;
			to[1] = meta;
		}
		if (id == RotaryCraft.miningpipe.blockID && meta == 3) {
			to[0] = id;
			to[1] = meta;
		}
		return to;
	}

	public boolean drawPile3(World world, int x, int y, int z, int speed) {
		if (climbing && tickcount > speed) {
			if (world.getBlockId(x, y-step2-2, z) == RotaryCraft.miningpipe.blockID)
				ReikaWorldHelper.legacySetBlockWithNotify(world, x, y-step2-2, z, 0);
			step2--;
			if (world.getBlockId(x, y-step2, z) == this.getBlockType().blockID) {
				climbing = false;
				//step2++;
				smashed = false;
			}
			else
				ReikaWorldHelper.legacySetBlockWithNotify(world, x, y-step2-1, z, 0);
			tickcount = 0;
		}
		if (climbing && tickcount <= speed) {
			//if (world.getBlockId(x, y-step2-2, z) == RotaryCraft.miningpipe.blockID)
			//ReikaWorldHelper.legacySetBlockWithNotify(world, x, y-step2-2, z, 0);
			if (step2 >= step)
				step2--;
			if (world.getBlockId(x, y-step2, z) == this.getBlockType().blockID) {
				climbing = false;
				//step2++;
				smashed = false;
			}
			else
				ReikaWorldHelper.legacySetBlockWithNotify(world, x, y-step2-1, z, 0);
			//this.tickcount = 0;
		}
		world.markBlockForUpdate(x, y-step2-1, z);
		if (!climbing){
			if (world.getBlockMaterial(x, y-step2-1, z) == Material.water) {
				world.spawnParticle("splash", x, y-step2+1, z, -0.2, 0.4, -0.2);
				world.spawnParticle("splash", x+0.5, y-step2+1, z, 0, 0.4, -0.2);
				world.spawnParticle("splash", x+1, y-step2+1, z, 0.2, 0.4, -0.2);
				world.spawnParticle("splash", x, y-step2+1, z+0.5, -0.2, 0.4, 0);
				world.spawnParticle("splash", x, y-step2+1, z+1, -0.2, 0.4, 0.2);
				world.spawnParticle("splash", x+0.5, y-step2+1, z+1, 0, 0.4, 0.2);
				world.spawnParticle("splash", x+1, y-step2+1, z+0.5, 0.2, 0.4, 0);
				world.spawnParticle("splash", x+1, y-step2+1, z+1, 0.2, 0.4, 0.2);

				world.spawnParticle("splash", x, y-step2+1, z, -0.2+0.4*rand.nextFloat(), 0.4, -0.2+0.4*rand.nextFloat());
				world.spawnParticle("splash", x+0.5, y-step2+1, z, 0, 0.4, -0.2+0.4*rand.nextFloat());
				world.spawnParticle("splash", x+1, y-step2+1, z, 0.2-0.4*rand.nextFloat(), 0.4, -0.2+0.4*rand.nextFloat());
				world.spawnParticle("splash", x, y-step2+1, z+0.5, -0.2+0.4*rand.nextFloat(), 0.4, 0);
				world.spawnParticle("splash", x, y-step2+1, z+1, -0.2+0.4*rand.nextFloat(), 0.4, 0.2-0.4*rand.nextFloat());
				world.spawnParticle("splash", x+0.5, y-step2+1, z+1, 0, 0.4, 0.2+0.4*rand.nextFloat());
				world.spawnParticle("splash", x+1, y-step2+1, z+0.5, 0.2-0.4*rand.nextFloat(), 0.4, 0);
				world.spawnParticle("splash", x+1, y-step2+1, z+1, 0.2-0.4*rand.nextFloat(), 0.4, 0.2-0.4*rand.nextFloat());

				world.spawnParticle("splash", x+rand.nextFloat(), y-step2+1, z+rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
				world.spawnParticle("splash", x+rand.nextFloat(), y-step2+1, z+rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
				world.spawnParticle("splash", x+rand.nextFloat(), y-step2+1, z+rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
				world.spawnParticle("splash", x+rand.nextFloat(), y-step2+1, z+rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
				world.spawnParticle("splash", x+rand.nextFloat(), y-step2+1, z+rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
				world.spawnParticle("splash", x+rand.nextFloat(), y-step2+1, z+rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
				world.spawnParticle("splash", x+rand.nextFloat(), y-step2+1, z+rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
				world.spawnParticle("splash", x+rand.nextFloat(), y-step2+1, z+rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());

				world.playSoundEffect(x+0.5, y-step2, z+0.5, "random.splash", 1F, 1F);
			}
			if (world.getBlockMaterial(x, y-step2-1, z) == Material.lava) {
				world.spawnParticle("lava", x, y-step2+1, z, -0.2, 0.4, -0.2);
				world.spawnParticle("lava", x+0.5, y-step2+1, z, 0, 0.4, -0.2);
				world.spawnParticle("lava", x+1, y-step2+1, z, 0.2, 0.4, -0.2);
				world.spawnParticle("lava", x, y-step2+1, z+0.5, -0.2, 0.4, 0);
				world.spawnParticle("lava", x, y-step2+1, z+1, -0.2, 0.4, 0.2);
				world.spawnParticle("lava", x+0.5, y-step2+1, z+1, 0, 0.4, 0.2);
				world.spawnParticle("lava", x+1, y-step2+1, z+0.5, 0.2, 0.4, 0);
				world.spawnParticle("lava", x+1, y-step2+1, z+1, 0.2, 0.4, 0.2);

				world.spawnParticle("lava", x, y-step2+1, z, -0.2+0.4*rand.nextFloat(), 0.4, -0.2+0.4*rand.nextFloat());
				world.spawnParticle("lava", x+0.5, y-step2+1, z, 0, 0.4, -0.2+0.4*rand.nextFloat());
				world.spawnParticle("lava", x+1, y-step2+1, z, 0.2-0.4*rand.nextFloat(), 0.4, -0.2+0.4*rand.nextFloat());
				world.spawnParticle("lava", x, y-step2+1, z+0.5, -0.2+0.4*rand.nextFloat(), 0.4, 0);
				world.spawnParticle("lava", x, y-step2+1, z+1, -0.2+0.4*rand.nextFloat(), 0.4, 0.2-0.4*rand.nextFloat());
				world.spawnParticle("lava", x+0.5, y-step2+1, z+1, 0, 0.4, 0.2+0.4*rand.nextFloat());
				world.spawnParticle("lava", x+1, y-step2+1, z+0.5, 0.2-0.4*rand.nextFloat(), 0.4, 0);
				world.spawnParticle("lava", x+1, y-step2+1, z+1, 0.2-0.4*rand.nextFloat(), 0.4, 0.2-0.4*rand.nextFloat());

				world.spawnParticle("lava", x+rand.nextFloat(), y-step2+1, z+rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
				world.spawnParticle("lava", x+rand.nextFloat(), y-step2+1, z+rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
				world.spawnParticle("lava", x+rand.nextFloat(), y-step2+1, z+rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
				world.spawnParticle("lava", x+rand.nextFloat(), y-step2+1, z+rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
				world.spawnParticle("lava", x+rand.nextFloat(), y-step2+1, z+rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
				world.spawnParticle("lava", x+rand.nextFloat(), y-step2+1, z+rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
				world.spawnParticle("lava", x+rand.nextFloat(), y-step2+1, z+rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
				world.spawnParticle("lava", x+rand.nextFloat(), y-step2+1, z+rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());

				world.playSoundEffect(x+0.5, y-step2, z+0.5, "random.fizz", 1F, 1F);
			}
			ReikaWorldHelper.legacySetBlockAndMetadataWithNotify(world, x, y-step2-1, z, RotaryCraft.miningpipe.blockID, BITMETA);
			step2++;
		}/*
		if (step2 == step) {
			if (world.getBlockId(x, y-step2-2, z) == 0)
				ReikaWorldHelper.legacySetBlockAndMetadataWithNotify(world, x, y-step2-2, z, RotaryCraft.miningpipe.blockID, BITMETA);
		}*/
		if (world.getBlockId(x, y-step2-1, z) == 0) {
			while(world.getBlockId(x, y-step2-2, z) == 0 && y-step2-2 > 0 && step == step2) {
				step++;
				step2 = step;
			}
		}
		//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d  %d    %d", step, step2, y-step2-2));
		return (step2 == step);
	}

	public boolean smash(World world, int x, int y, int z) {
		boolean cleared = true;
		smashed = true;
		for (int i = -2; i < 3; i++) {
			for (int j = -2; j < 3; j++) {
				int id = world.getBlockId(x+i, y, z+j);
				if (id != 0 && i*j != 4 && i*j != -4) {
					if (id == Block.mobSpawner.blockID) {
						TileEntityMobSpawner spw = (TileEntityMobSpawner)world.getBlockTileEntity(x+i, y, z+j);
						if (spw != null) {
							this.spawnSpawner(world, x+i, y, z+j, spw);
						}
					}
					if (world.getBlockId(x+i, y-1, z+j) == Block.netherrack.blockID) {
						Block.blocksList[Block.netherrack.blockID].dropBlockAsItem(world, x+i, y-1, z+j, 0, 0);
						ReikaWorldHelper.legacySetBlockWithNotify(world, x+i, y-1, z+j, 0);
						world.markBlockForUpdate(x+i, y-1, z+j);
						//this.step++;
					}
					if (world.getBlockId(x+i, y-2, z+j) == Block.netherrack.blockID) {
						Block.blocksList[Block.netherrack.blockID].dropBlockAsItem(world, x+i, y-2, z+j, 0, 0);
						ReikaWorldHelper.legacySetBlockWithNotify(world, x+i, y-2, z+j, 0);
						world.markBlockForUpdate(x+i, y-2, z+j);
						//this.step++;
					}
					int meta = world.getBlockMetadata(x+i, y, z+j);
					int[] blockTo = this.getBlockProduct(id, meta);
					ReikaWorldHelper.legacySetBlockAndMetadataWithNotify(world, x+i, y, z+j, blockTo[0], blockTo[1]);
					if (blockTo[0] == 0) {
						Block.blocksList[id].dropBlockAsItem(world, x+i, y, z+j, meta, 0);
					}
					world.markBlockForUpdate(x+i, y, z+j);
				}
			}
		}
		//ModLoader.getMinecraftInstance().thePlayer.addChatMessage("FDD");
		world.playSoundEffect(x+0.5D, y, z+0.5D, "Reika.RotaryCraft.piledriver", 1F, 1F);
		for (int i = -2; i < 3; i++) {
			for (int j = -2; j < 3; j++) {
				if (i*j != 4 && i*j != -4 && world.getBlockId(x+i, y, z+j) != 0 && world.getBlockMaterial(x+i, y, z+j) != Material.water && world.getBlockMaterial(x+i, y, z+j) != Material.lava) {
					cleared = false;
					//ReikaWorldHelper.legacySetBlockAndMetadataWithNotify(world, x, y, z, RotaryCraft.miningpipe.blockID, BITMETA);
				}
			}
		}
		this.breakGlass(world, x, y, z);
		return cleared;
	}

	public void spawnSpawner(World world, int x, int y, int z, TileEntityMobSpawner spw) {
		if (world.isRemote)
			return;
		ItemStack is = new ItemStack(RotaryCraft.spawner);
		ReikaSpawnerHelper.addMobNBTToItem(is, spw);
		EntityItem ent = new EntityItem(world, x, y, z, is);
		world.spawnEntityInWorld(ent);
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound NBT)
	{
		super.writeToNBT(NBT);
		NBT.setInteger("step", step);
		NBT.setInteger("step2", step2);
		NBT.setBoolean("active", active);
		NBT.setBoolean("climbing", climbing);
		NBT.setBoolean("smashed", smashed);
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound NBT)
	{
		super.readFromNBT(NBT);
		step = NBT.getInteger("step");
		step2 = NBT.getInteger("step2");
		climbing = NBT.getBoolean("climbing");
		active = NBT.getBoolean("active");
		smashed = NBT.getBoolean("smashed");
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
		if (power < BASEPOWER*(step+1) || torque < MINTORQUE)
			return;
		phi += ReikaMathLibrary.doubpow(ReikaMathLibrary.logbase(omega+1, 2), 1.05);
	}

	@Override
	public int getMachineIndex() {
		return MachineRegistry.PILEDRIVER.ordinal();
	}

	@Override
	public int getRedstoneOverride() {
		return 0;
	}

}

