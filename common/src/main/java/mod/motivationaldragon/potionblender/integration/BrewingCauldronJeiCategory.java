package mod.motivationaldragon.potionblender.integration;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mod.motivationaldragon.potionblender.Constants;
import mod.motivationaldragon.potionblender.block.PotionBlenderBlock;
import mod.motivationaldragon.potionblender.recipes.BrewingCauldronRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public class BrewingCauldronJeiCategory implements IRecipeCategory<BrewingCauldronRecipe> {

	//TODO make potion display properly and not as uncraftable potion


	public static final Identifier UID =  Identifier.fromNamespaceAndPath(Constants.MOD_ID, "brewing_cauldron");
	public static final Identifier BACKGROUND_TEXTURE =  Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/jei/brewing_cauldron_gui.png");

	public static final IRecipeType<BrewingCauldronRecipe> brewing_cauldron_recipe_type = IRecipeType.create(UID, BrewingCauldronRecipe.class);

	private final IDrawable background;
	private final IDrawable icon;

	public BrewingCauldronJeiCategory(IGuiHelper guiHelper) {
		this.background = guiHelper.createDrawable(BACKGROUND_TEXTURE, 0, 0, 166, 74);
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(PotionBlenderBlock.BREWING_CAULDRON_ITEM));
	}

	/**
	 * @return the type of recipe that this category handles.
	 * @since 9.5.0
	 */
	@Override
	public @NotNull IRecipeType<BrewingCauldronRecipe> getRecipeType() {
		return brewing_cauldron_recipe_type;
	}

	/**
	 * Returns a text component representing the name of this recipe type.
	 * Drawn at the top of the recipe GUI pages for this category.
	 *
	 * @since 7.6.4
	 */
	@Override
	public @NotNull Component getTitle() {
		return Component.translatable("block.potionblender.brewing_cauldron");
	}

	/**
	 * Returns the drawable background for a single recipe in this category.
	 */
	@Override
	public int getWidth() { return 166; }
    @Override public int getHeight() { return 74; }
    @Override public void createRecipeExtras(mezz.jei.api.gui.widgets.IRecipeExtrasBuilder builder,
                                              BrewingCauldronRecipe recipe, IFocusGroup focuses) {
        builder.addDrawable(background, 0, 0);
    }

	/**
	 * Icon for the category tab.
	 * You can use {@link IGuiHelper#createDrawableIngredient(IIngredientType, Object)}
	 * to create a drawable from an ingredient.
	 *
	 * @return icon to draw on the category tab, max size is 16x16 pixels.
	 */
	@Override
	public @NotNull IDrawable getIcon() {
		return this.icon;
	}

	/**
	 * Sets all the recipe's ingredients by filling out an instance of {@link IRecipeLayoutBuilder}.
	 * This is used by JEI for lookups, to figure out what ingredients are inputs and outputs for a recipe.
	 *
	 * @param builder
	 * @param recipe
	 * @param focuses
	 * @since 9.4.0
	 */
	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, BrewingCauldronRecipe recipe, @NotNull IFocusGroup focuses) {

		if (!recipe.isOrdered()) {
			builder.setShapeless();
		}

		for (int i = 0; i < recipe.getIngredients().size(); i++) {
			Ingredient ingredient = recipe.getIngredients().get(i);
			builder.addSlot(RecipeIngredientRole.INPUT, 38 + i * 18, 4)
					.add(ingredient);
		}
		builder.addSlot(RecipeIngredientRole.OUTPUT, 74, 52).add(recipe.getOutput());
	}
}