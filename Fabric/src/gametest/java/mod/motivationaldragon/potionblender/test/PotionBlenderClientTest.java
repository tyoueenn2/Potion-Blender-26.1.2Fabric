package mod.motivationaldragon.potionblender.test;

import mod.motivationaldragon.potionblender.block.BrewingCauldron;
import mod.motivationaldragon.potionblender.block.PotionBlenderBlock;
import mod.motivationaldragon.potionblender.blockentities.BrewingCauldronBlockEntity;
import mod.motivationaldragon.potionblender.recipes.BrewingCauldronRecipe;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public class PotionBlenderClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            var pos = new BlockPos(0, 100, 0);
            world.getServer().runOnServer(server -> {
                var level = server.overworld();
                level.setBlockAndUpdate(pos, PotionBlenderBlock.BREWING_CAULDRON_BLOCK.defaultBlockState()
                        .setValue(BrewingCauldron.HAS_FLUID, true));
                var entity = (BrewingCauldronBlockEntity) level.getBlockEntity(pos);
                NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
                inventory.set(0, PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS));
                inventory.set(1, PotionContents.createItemStack(Items.POTION, Potions.STRENGTH));
                entity.setInventory(inventory);
                entity.update();
                server.getPlayerList().getPlayers().forEach(player -> {
                    player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
                    player.connection.teleport(0.5, 101, 3, 180, 30);
                });
            });
            world.getClientLevel().waitForChunksRender();
            context.runOnClient(client -> {
                client.player.teleportTo(0.5, 101, 3);
                client.player.setYRot(180);
                client.player.setXRot(30);
                var recipes = client.getConnection().recipes().getSynchronizedRecipes().getAllOfType(BrewingCauldronRecipe.Type.INSTANCE);
                if (recipes.size() != 6) throw new AssertionError("Expected six synchronized brewing recipes, got " + recipes.size());
                if (!(client.level.getBlockEntity(pos) instanceof BrewingCauldronBlockEntity)) {
                    throw new AssertionError("Cauldron block entity must synchronize to the client");
                }
            });
            context.getInput().lookAt(pos);
            context.waitTicks(20);
            context.runOnClient(client -> {
                System.out.println("CAULDRON CAMERA " + client.player.position() + " yaw=" + client.player.getYRot() + " pitch=" + client.player.getXRot());
                System.out.println("CAMERA ACTUAL " + client.gameRenderer.getMainCamera().position());
            });
            context.takeScreenshot("brewing-cauldron-26.1.2");
        }
    }
}
