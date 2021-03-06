/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
// Date: 14/02/2013 10:18:33 PM
// Template version 1.1
// Java generated by Techne
// Keep in mind that you still need to fill in some blanks
// - ZeuX

package Reika.RotaryCraft.Models;

import java.util.List;

import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.common.ForgeDirection;
import Reika.RotaryCraft.Base.RotaryModelBase;

public class ModelReservoir extends RotaryModelBase
{
	//fields
	ModelRenderer mx;
	ModelRenderer mz;
	ModelRenderer pz;
	ModelRenderer px;
	ModelRenderer bottom;

	public ModelReservoir()
	{
		textureWidth = 128;
		textureHeight = 128;

		mx = new ModelRenderer(this, 56, 0);
		mx.addBox(0F, 0F, 0F, 1, 16, 16);
		mx.setRotationPoint(-8F, 8F, -8F);
		mx.setTextureSize(128, 128);
		mx.mirror = true;
		this.setRotation(mx, 0F, 0F, 0F);

		mz = new ModelRenderer(this, 56, 0);
		mz.addBox(0F, 0F, 0F, 1, 16, 16);
		mz.setRotationPoint(7F, 8F, -8F);
		mz.setTextureSize(128, 128);
		mz.mirror = true;
		this.setRotation(mz, 0F, 0F, 0F);

		pz = new ModelRenderer(this, 0, 0);
		pz.addBox(0F, 0F, 0F, 16, 16, 1);
		pz.setRotationPoint(-8F, 8F, 7F);
		pz.setTextureSize(128, 128);
		pz.mirror = true;
		this.setRotation(pz, 0F, 0F, 0F);

		px = new ModelRenderer(this, 0, 0);
		px.addBox(0F, 0F, 0F, 16, 16, 1);
		px.setRotationPoint(-8F, 8F, -8F);
		px.setTextureSize(128, 128);
		px.mirror = true;
		this.setRotation(px, 0F, 0F, 0F);

		bottom = new ModelRenderer(this, 0, 46);
		bottom.addBox(0F, 0F, 0F, 16, 1, 16);
		bottom.setRotationPoint(-8F, 23F, -8F);
		bottom.setTextureSize(128, 128);
		bottom.mirror = true;
		this.setRotation(bottom, 0F, 0F, 0F);
	}

	@Override
	public void renderAll(List li, float phi)
	{
		mx.render(f5);
		mz.render(f5);
		pz.render(f5);
		px.render(f5);
		bottom.render(f5);
	}

	public void renderSide(ForgeDirection dir) {
		switch(dir) {
		case DOWN:
			bottom.render(f5);
			break;
		case WEST:
			px.render(f5);
			break;
		case SOUTH:
			mz.render(f5);
			break;
		case EAST:
			pz.render(f5);
			break;
		case NORTH:
			mx.render(f5);
			break;
		default:
			break;
		}
	}


}
