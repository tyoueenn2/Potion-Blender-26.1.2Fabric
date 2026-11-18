package mod.motivationaldragon.potionblender.test;

import mod.motivationaldragon.potionblender.block.BrewingCauldron;
import mod.motivationaldragon.potionblender.block.PotionBlenderBlock;
import mod.motivationaldragon.potionblender.blockentities.BrewingCauldronBlockEntity;
import mod.motivationaldragon.potionblender.datatype.PotionBlender;
import mod.motivationaldragon.potionblender.recipes.*;
import mod.motivationaldragon.potionblender.utils.PotionType;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PotionBlenderGameTests {
    private static final BlockPos POS = new BlockPos(1, 1, 1);

    @GameTest
    public void normalPotion(GameTestHelper helper) { brew(helper, Items.NETHER_WART, Items.POTION, PotionType.NORMAL, 3600); }
    @GameTest
    public void splashPotion(GameTestHelper helper) { brew(helper, Items.GUNPOWDER, Items.SPLASH_POTION, PotionType.SPLASH, 3600); }
    @GameTest
    public void lingeringPotion(GameTestHelper helper) { brew(helper, Items.DRAGON_BREATH, Items.LINGERING_POTION, PotionType.LINGERING, 900); }

    private void brew(GameTestHelper helper, Item catalyst, Item output, PotionType type, int duration) {
        BrewingCauldronBlockEntity cauldron = filledCauldron(helper);
        add(helper, cauldron, new ItemStack(catalyst));
        tick(helper, cauldron, 141);
        ItemStack result = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(cauldron.getBlockPos()).inflate(1))
                .stream().map(ItemEntity::getItem).filter(stack -> stack.is(output)).findFirst().orElse(ItemStack.EMPTY);
        helper.assertFalse(result.isEmpty(), "Brewing must produce the selected potion type");
        helper.assertValueEqual(result.get(PotionBlender.potionTypeData), type.code, "Combined potion marker");
        var effects = result.get(DataComponents.POTION_CONTENTS).customEffects();
        helper.assertValueEqual(effects.size(), 2, "Both potion effects survive brewing");
        helper.assertValueEqual(effects.getFirst().getDuration(), duration, "Effect duration");
        helper.assertValueEqual(result.get(DataComponents.POTION_DURATION_SCALE), 1.0F, "Duration must not scale twice");
        helper.assertValueEqual(cauldron.getNumberOfItems(), 0, "Cauldron is emptied");
        helper.succeed();
    }

    @GameTest
    public void saveAndReloadInventory(GameTestHelper helper) {
        var cauldron = filledCauldron(helper);
        var registry = helper.getLevel().registryAccess();
        var tag = cauldron.saveWithFullMetadata(registry);
        var restored = (BrewingCauldronBlockEntity) BlockEntity.loadStatic(cauldron.getBlockPos(), cauldron.getBlockState(), tag, registry);
        helper.assertTrue(restored != null, "Saved cauldron must reload");
        helper.assertValueEqual(restored.getNumberOfItems(), 2, "Saved inventory count");
        helper.assertValueEqual(restored.getInventory().getFirst().get(DataComponents.POTION_CONTENTS),
                cauldron.getInventory().getFirst().get(DataComponents.POTION_CONTENTS), "Saved potion contents");
        helper.assertValueEqual(restored.getWaterColor(), cauldron.getWaterColor(), "Saved fluid color");
        helper.succeed();
    }

    @GameTest
    public void combinedArrowCrafting(GameTestHelper helper) {
        ItemStack potion = new ItemStack(Items.LINGERING_POTION);
        potion.set(PotionBlender.potionTypeData, PotionType.LINGERING.code);
        potion.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(),
                List.of(new MobEffectInstance(MobEffects.SPEED, 900)), Optional.empty()));
        List<ItemStack> grid = new ArrayList<>();
        for (int i = 0; i < 9; i++) grid.add(i == 4 ? potion : new ItemStack(Items.ARROW));
        var input = CraftingInput.of(3, 3, grid);
        var recipe = new CombinedTippedArrowRecipe();
        helper.assertTrue(recipe.matches(input, helper.getLevel()), "Combined lingering potion should craft arrows");
        var result = recipe.assemble(input);
        helper.assertTrue(result.is(Items.TIPPED_ARROW), "Crafted item is a tipped arrow");
        helper.assertValueEqual(result.getCount(), 8, "Eight arrows are produced");
        helper.assertValueEqual(result.get(DataComponents.POTION_CONTENTS).customEffects().getFirst().getDuration(), 450, "Arrow duration");
        helper.assertValueEqual(result.get(DataComponents.POTION_DURATION_SCALE), 1.0F, "Arrow duration scale");
        helper.succeed();
    }

    @GameTest
    public void unorderedIngredientsAreDistinct(GameTestHelper helper) {
        var recipe = new BrewingCauldronRecipe(20, false, 0, false, 2,
                List.of(Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.NETHER_WART)), new ItemStackTemplate(Items.DIAMOND));
        helper.assertFalse(recipe.matches(new MultipleInputRecipe(new SimpleContainer(new ItemStack(Items.NETHER_WART),
                new ItemStack(Items.GUNPOWDER))), helper.getLevel()), "One input cannot satisfy two ingredients");
        helper.assertTrue(recipe.matches(new MultipleInputRecipe(new SimpleContainer(new ItemStack(Items.NETHER_WART),
                new ItemStack(Items.NETHER_WART))), helper.getLevel()), "Two matching ingredients craft successfully");
        helper.succeed();
    }

    @GameTest
    public void campfireConversion(GameTestHelper helper) {
        var state = net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CampfireBlock.LIT, false);
        helper.setBlock(POS, state);
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var held = new ItemStack(Items.CAULDRON, 2);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, held);
        var absolute = helper.absolutePos(POS);
        state.useItemOn(held, helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(absolute),
                        net.minecraft.core.Direction.UP, absolute, false));
        helper.assertBlockPresent(PotionBlenderBlock.BREWING_CAULDRON_BLOCK, POS);
        helper.assertBlockProperty(POS, BrewingCauldron.IS_SOULFIRE, true);
        helper.assertBlockProperty(POS, BrewingCauldron.LIT, false);
        helper.assertValueEqual(held.getCount(), 1, "Conversion consumes one cauldron");
        helper.succeed();
    }

    @GameTest
    public void breakingCauldronDropsInventory(GameTestHelper helper) {
        var cauldron = filledCauldron(helper);
        helper.getLevel().destroyBlock(cauldron.getBlockPos(), true);
        helper.assertItemEntityCountIs(Items.POTION, POS, 2, 2);
        helper.assertItemEntityCountIs(PotionBlenderBlock.BREWING_CAULDRON_ITEM, POS, 2, 1);
        helper.succeed();
    }

    private BrewingCauldronBlockEntity filledCauldron(GameTestHelper helper) {
        helper.setBlock(POS, PotionBlenderBlock.BREWING_CAULDRON_BLOCK);
        var cauldron = helper.getBlockEntity(POS, BrewingCauldronBlockEntity.class);
        add(helper, cauldron, PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS));
        add(helper, cauldron, PotionContents.createItemStack(Items.POTION, Potions.STRENGTH));
        return cauldron;
    }

    private void add(GameTestHelper helper, BrewingCauldronBlockEntity cauldron, ItemStack stack) {
        var pos = cauldron.getBlockPos();
        cauldron.onEntityLandDelegate(new ItemEntity(helper.getLevel(), pos.getX(), pos.getY() + 1, pos.getZ(), stack));
    }

    private void tick(GameTestHelper helper, BrewingCauldronBlockEntity cauldron, int ticks) {
        for (int i = 0; i < ticks; i++) BrewingCauldronBlockEntity.tick(helper.getLevel(), cauldron.getBlockPos(), cauldron.getBlockState(), cauldron);
    }
}
