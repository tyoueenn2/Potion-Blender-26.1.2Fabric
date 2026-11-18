package mod.motivationaldragon.potionblender.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.motivationaldragon.potionblender.block.BrewingCauldron;
import mod.motivationaldragon.potionblender.blockentities.BrewingCauldronBlockEntity;
import mod.motivationaldragon.potionblender.utils.ModUtils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class BrewingCauldronRenderer implements BlockEntityRenderer<BrewingCauldronBlockEntity, BrewingCauldronRenderer.State> {
    private static final SpriteId WATER = new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.withDefaultNamespace("block/water_still"));
    private final ItemModelResolver items;
    private final SpriteGetter sprites;

    public BrewingCauldronRenderer(BlockEntityRendererProvider.Context context) {
        items = context.itemModelResolver();
        sprites = context.sprites();
    }

    public static class State extends BlockEntityRenderState {
        final List<ItemStackRenderState> items = new ArrayList<>();
        boolean hasFluid;
        int waterColor;
        float fullness;
        TextureAtlasSprite water;
    }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(BrewingCauldronBlockEntity entity, State state, float partialTick,
                                   Vec3 camera, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, camera, overlay);
        state.items.clear();
        for (var stack : entity.getInventory()) {
            ItemStackRenderState item = new ItemStackRenderState();
            items.updateForTopItem(item, stack, ItemDisplayContext.FIXED, entity.getLevel(), null, 0);
            state.items.add(item);
        }
        state.hasFluid = entity.getBlockState().getValue(BrewingCauldron.HAS_FLUID);
        state.waterColor = entity.getWaterColor();
        state.fullness = (float) entity.getNumberOfItems() / entity.getInventory().size();
        state.water = sprites.get(WATER);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        for (int i = 0; i < state.items.size(); i++) {
            if (state.items.get(i).isEmpty()) continue;
            pose.pushPose();
            pose.translate(0.5, ModUtils.lerp(0.5F, 0.8F, (float) i / state.items.size()), 0.5);
            pose.scale(0.5F, 0.5F, 0.5F);
            pose.mulPose(Axis.XP.rotationDegrees(90));
            pose.mulPose(Axis.ZP.rotationDegrees(i * 27));
            state.items.get(i).submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
        if (!state.hasFluid) return;
        int color = 0xBE000000 | (state.waterColor & 0xFFFFFF);
        float height = 0.5F + state.fullness * 0.4F;
        var water = state.water;
        collector.submitCustomGeometry(pose, RenderTypes.translucentMovingBlock(), (transform, consumer) -> {
            consumer.addVertex(transform, 0.875F, height, 0.125F).setColor(color).setUv(water.getU0(), water.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(state.lightCoords).setNormal(transform, 0, 1, 0);
            consumer.addVertex(transform, 0.125F, height, 0.125F).setColor(color).setUv(water.getU1(), water.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(state.lightCoords).setNormal(transform, 0, 1, 0);
            consumer.addVertex(transform, 0.125F, height, 0.875F).setColor(color).setUv(water.getU1(), water.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(state.lightCoords).setNormal(transform, 0, 1, 0);
            consumer.addVertex(transform, 0.875F, height, 0.875F).setColor(color).setUv(water.getU0(), water.getV0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(state.lightCoords).setNormal(transform, 0, 1, 0);
        });
    }
}
