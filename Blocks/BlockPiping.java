/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.Blocks;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import Reika.RotaryCraft.RotaryCraft;
import Reika.RotaryCraft.Base.BlockBasicMultiTE;
import Reika.RotaryCraft.Base.TileEntityPiping;
import Reika.RotaryCraft.Registry.MachineRegistry;

public class BlockPiping extends BlockBasicMultiTE {

	public BlockPiping(int par1, Material par3Material) {
		super(par1, par3Material);
		this.setHardness(0F);
		this.setResistance(1F);
		this.setLightValue(0F);
	}

	@Override
	public final boolean isOpaqueCube() {
		return false;
	}

	@Override
	public final int getRenderType() {
		return RotaryCraft.proxy.pipeRender;
	}

	@Override
	public int idDropped(int id, Random r, int fortune) {
		return 0*RotaryCraft.pipeplacer.itemID;
	}

	@Override
	public int damageDropped(int par1)
	{
		return par1;
	}

	@Override
	public int quantityDropped(Random par1Random)
	{
		return 0;
	}

	@Override
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune)
	{
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		ret.add(new ItemStack(RotaryCraft.pipeplacer.itemID, 1, metadata));
		return ret;
	}
	/*
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess iba, int x, int y, int z) {
		this.setBlockBounds(0.33F, 0.33F, 0.33F, 0.67F, 0.67F, 0.67F);
		float minx = (float)minX;
		float maxx = (float)maxX;
		float miny = (float)minY;
		float maxy = (float)maxY;
		float minz = (float)minZ;
		float maxz = (float)maxZ;
		TileEntityPiping te = (TileEntityPiping)iba.getBlockTileEntity(x, y, z);
		if (te.isConnectionValidForIDAndSide(EnumLook.MINX))
			minx = 0;
		if (te.isConnectionValidForIDAndSide(EnumLook.PLUSX))
			maxx = 1;
		if (te.isConnectionValidForIDAndSide(EnumLook.DOWN))
			miny = 0;
		if (te.isConnectionValidForIDAndSide(EnumLook.UP))
			maxy = 1;
		if (te.isConnectionValidForIDAndSide(EnumLook.MINZ))
			minz = 0;
		if (te.isConnectionValidForIDAndSide(EnumLook.PLUSZ))
			maxz = 1;

		this.setBlockBounds(minx, miny, minz, maxx, maxy, maxz);
	}
	 */
	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta)
	{
		return true;
	}

	@Override
	public void registerIcons(IconRegister ico) {
		for (int i = 0; i < 6; i++) {
			icons[MachineRegistry.HOSE.getMachineMetadata()][0][i][0] = ico.registerIcon("RotaryCraft:hose");
			icons[MachineRegistry.PIPE.getMachineMetadata()][0][i][0] = ico.registerIcon("RotaryCraft:pipe");
			icons[MachineRegistry.FUELLINE.getMachineMetadata()][0][i][0] = ico.registerIcon("RotaryCraft:fuelline");
			icons[MachineRegistry.SPILLER.getMachineMetadata()][0][i][0] = ico.registerIcon("RotaryCraft:spiller");
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id) {
		TileEntityPiping te = (TileEntityPiping)world.getBlockTileEntity(x, y, z);
		te.recomputeConnections(world, x, y, z);
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		TileEntityPiping te = (TileEntityPiping)world.getBlockTileEntity(x, y, z);
		te.addToAdjacentConnections(world, x, y, z);
		te.recomputeConnections(world, x, y, z);
	}
}
