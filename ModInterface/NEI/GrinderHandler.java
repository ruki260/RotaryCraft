/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.ModInterface.NEI;

import static codechicken.core.gui.GuiDraw.drawTexturedModalRect;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.RotaryCraft.RotaryCraft;
import Reika.RotaryCraft.Auxiliary.RecipesGrinder;
import Reika.RotaryCraft.GUIs.GuiGrinder;
import Reika.RotaryCraft.Registry.ItemRegistry;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class GrinderHandler extends TemplateRecipeHandler {

	public class GrinderRecipe extends CachedRecipe {

		private List<ItemStack> input;
		private ItemStack output;

		public GrinderRecipe(List<ItemStack> in) {
			input = in;
			output = RecipesGrinder.getRecipes().getSmeltingResult(in.get(0));
		}

		@Override
		public PositionedStack getResult() {
			return new PositionedStack(RecipesGrinder.getRecipes().getSmeltingResult(this.getEntry()), 131, 24);
		}

		@Override
		public PositionedStack getIngredient()
		{
			return new PositionedStack(this.getEntry(), 71, 24);
		}

		public ItemStack getEntry() {
			return input.get((int)(System.nanoTime()/1000000000)%input.size());
		}
	}

	public class CanolaRecipe extends CachedRecipe {

		@Override
		public PositionedStack getIngredient()
		{
			return new PositionedStack(ItemRegistry.CANOLA.getStackOf(), 71, 24);
		}

		@Override
		public PositionedStack getResult() {
			return null;
		}

	}

	@Override
	public String getRecipeName() {
		return "Grinder";
	}

	@Override
	public String getGuiTexture() {
		return "/Reika/RotaryCraft/Textures/GUI/grindergui.png";
	}

	@Override
	public void drawBackground(int recipe)
	{
		GL11.glColor4f(1, 1, 1, 1);
		ReikaTextureHelper.bindTexture(RotaryCraft.class, this.getGuiTexture());
		drawTexturedModalRect(0, 0, 5, 11, 166, 70);
	}

	@Override
	public void drawForeground(int recipe)
	{
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glDisable(GL11.GL_LIGHTING);
		ReikaTextureHelper.bindTexture(RotaryCraft.class, this.getGuiTexture());
		this.drawExtras(recipe);
		CachedRecipe c = arecipes.get(recipe);
		if (c.getIngredient() != null && c.getIngredient().item.itemID == ItemRegistry.CANOLA.getShiftedID()) {
			drawTexturedModalRect(19, 10, 176, 71, 8, 55);
		}
	}

	@Override
	public void loadCraftingRecipes(ItemStack result) {
		if (RecipesGrinder.getRecipes().isProduct(result)) {
			List<ItemStack> is = RecipesGrinder.getRecipes().getSources(result);
			if (is != null && !is.isEmpty())
				arecipes.add(new GrinderRecipe(is));
		}
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient) {
		if (ingredient.itemID == ItemRegistry.CANOLA.getShiftedID()) {
			arecipes.add(new CanolaRecipe());
		}
		if (RecipesGrinder.getRecipes().isGrindable(ingredient)) {
			arecipes.add(new GrinderRecipe(ReikaJavaLibrary.makeListFrom(ingredient)));
		}
	}

	@Override
	public Class<? extends GuiContainer> getGuiClass()
	{
		return GuiGrinder.class;
	}

}
