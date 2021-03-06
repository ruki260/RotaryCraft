/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.GUIs;

import net.minecraft.entity.player.EntityPlayer;
import Reika.RotaryCraft.Base.GuiNonPoweredMachine;
import Reika.RotaryCraft.Containers.ContainerLandmine;
import Reika.RotaryCraft.TileEntities.Weaponry.TileEntityLandmine;

public class GuiLandmine extends GuiNonPoweredMachine {

	public GuiLandmine(EntityPlayer p5ep, TileEntityLandmine te) {
		super(new ContainerLandmine(p5ep, te), te);
		ep = p5ep;
	}

	@Override
	public String getGuiTexture() {
		return "landminegui";
	}
}
