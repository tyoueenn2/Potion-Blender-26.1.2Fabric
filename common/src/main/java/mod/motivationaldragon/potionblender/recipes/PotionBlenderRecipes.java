package mod.motivationaldragon.potionblender.recipes;

import mod.motivationaldragon.potionblender.Constants;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.BiConsumer;

public class PotionBlenderRecipes {

    public static final RecipeSerializer<CombinedTippedArrowRecipe> COMBINED_TIPPED_ARROW_SERIALIZER = new RecipeSerializer<>(MapCodec.unit(CombinedTippedArrowRecipe::new), StreamCodec.unit(new CombinedTippedArrowRecipe()));


    public static void registerRecipeSerializer(BiConsumer<RecipeSerializer<?>, Identifier> r){
        r.accept(COMBINED_TIPPED_ARROW_SERIALIZER, Identifier.fromNamespaceAndPath(Constants.MOD_ID,"tipped_combined_arrow"));
        r.accept(BrewingCauldronRecipe.SERIALIZER,  Identifier.fromNamespaceAndPath(Constants.MOD_ID,"potion_blending"));
        Constants.LOG.debug("Loaded recipe");
    }

    public static void registerRecipeType(BiConsumer<RecipeType<?>, Identifier> r){
        r.accept(BrewingCauldronRecipe.Type.INSTANCE, Identifier.fromNamespaceAndPath(Constants.MOD_ID,BrewingCauldronRecipe.Type.ID));
    }
}
