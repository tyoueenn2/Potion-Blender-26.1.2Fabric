package mod.motivationaldragon.potionblender.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mod.motivationaldragon.potionblender.Constants;
import mod.motivationaldragon.potionblender.config.ConfigController;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public record BrewingCauldronRecipe(int brewingTime, boolean usePotionMergingRules, int color,
                                   boolean isOrdered, double decayRate, List<Ingredient> ingredients,
                                   ItemStackTemplate output) implements Recipe<MultipleInputRecipe> {
    public BrewingCauldronRecipe {
        if (usePotionMergingRules && !(output.item().value() instanceof PotionItem)) {
            throw new IllegalArgumentException("Potion merging recipes must produce a potion");
        }
        ingredients = List.copyOf(ingredients);
    }

    private static final Codec<List<Ingredient>> INGREDIENTS = Ingredient.CODEC.listOf().validate(list ->
            list.isEmpty() || list.size() > ConfigController.getConfig().getCauldronInventorySize()
                    ? DataResult.error(() -> "Invalid brewing cauldron ingredient count") : DataResult.success(list));
    public static final MapCodec<BrewingCauldronRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("brewingTime").forGetter(BrewingCauldronRecipe::brewingTime),
            Codec.BOOL.optionalFieldOf("usePotionMergingRules", false).forGetter(BrewingCauldronRecipe::usePotionMergingRules),
            Codec.INT.optionalFieldOf("color", Constants.WATER_TINT).forGetter(BrewingCauldronRecipe::color),
            Codec.BOOL.fieldOf("isOrdered").forGetter(BrewingCauldronRecipe::isOrdered),
            Codec.doubleRange(1.0, Double.MAX_VALUE).optionalFieldOf("decayRate", 2.0).forGetter(BrewingCauldronRecipe::decayRate),
            INGREDIENTS.fieldOf("ingredients").forGetter(BrewingCauldronRecipe::ingredients),
            ItemStackTemplate.CODEC.fieldOf("output").forGetter(BrewingCauldronRecipe::output)
    ).apply(instance, BrewingCauldronRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BrewingCauldronRecipe> STREAM_CODEC = StreamCodec.of(
            BrewingCauldronRecipe::toNetwork, BrewingCauldronRecipe::fromNetwork);
    public static final RecipeSerializer<BrewingCauldronRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    @Override
    public boolean matches(MultipleInputRecipe input, Level level) {
        if (input.size() != ingredients.size()) return false;
        if (isOrdered) {
            for (int i = 0; i < ingredients.size(); i++) if (!ingredients.get(i).test(input.getItem(i))) return false;
            return true;
        }
        // Backtracking assigns each input to a distinct ingredient, including overlapping tags.
        return matchesUnordered(input, 0, new boolean[ingredients.size()]);
    }

    private boolean matchesUnordered(MultipleInputRecipe input, int slot, boolean[] used) {
        if (slot == input.size()) return true;
        for (int i = 0; i < ingredients.size(); i++) {
            if (!used[i] && ingredients.get(i).test(input.getItem(slot))) {
                used[i] = true;
                if (matchesUnordered(input, slot + 1, used)) return true;
                used[i] = false;
            }
        }
        return false;
    }

    @Override public ItemStack assemble(MultipleInputRecipe input) { return output.create(); }
    @Override public RecipeSerializer<BrewingCauldronRecipe> getSerializer() { return SERIALIZER; }
    @Override public RecipeType<BrewingCauldronRecipe> getType() { return Type.INSTANCE; }
    @Override public boolean isSpecial() { return true; }
    @Override public boolean showNotification() { return false; }
    @Override public String group() { return ""; }
    @Override public PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }
    @Override public RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CAMPFIRE; }

    public ItemStack getResultItem(HolderLookup.Provider provider) { return output.create(); }
    public ItemStack getOutput() { return output.create(); }
    public List<Ingredient> getIngredients() { return ingredients; }
    public int getBrewingTime() { return brewingTime; }
    public int getColor() { return color; }
    public double getDecayRate() { return decayRate; }
    public boolean isOrdered() { return isOrdered; }
    public boolean usePotionMeringRules() { return usePotionMergingRules; }

    public static class Type implements RecipeType<BrewingCauldronRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "brewing_cauldron_recipe";
    }

    private static BrewingCauldronRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
        int time = buf.readVarInt();
        boolean merging = buf.readBoolean();
        int color = buf.readInt();
        boolean ordered = buf.readBoolean();
        double decay = buf.readDouble();
        int size = buf.readVarInt();
        if (size < 1 || size > 256) throw new IllegalArgumentException("Invalid ingredient count: " + size);
        List<Ingredient> ingredients = new ArrayList<>(size);
        for (int i = 0; i < size; i++) ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
        return new BrewingCauldronRecipe(time, merging, color, ordered, decay, ingredients, ItemStackTemplate.STREAM_CODEC.decode(buf));
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, BrewingCauldronRecipe recipe) {
        buf.writeVarInt(recipe.brewingTime);
        buf.writeBoolean(recipe.usePotionMergingRules);
        buf.writeInt(recipe.color);
        buf.writeBoolean(recipe.isOrdered);
        buf.writeDouble(recipe.decayRate);
        buf.writeVarInt(recipe.ingredients.size());
        for (Ingredient ingredient : recipe.ingredients) Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
        ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.output);
    }
}
