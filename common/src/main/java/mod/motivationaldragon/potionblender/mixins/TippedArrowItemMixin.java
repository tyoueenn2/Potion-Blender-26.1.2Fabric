package mod.motivationaldragon.potionblender.mixins;

import mod.motivationaldragon.potionblender.utils.PotionType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TippedArrowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static mod.motivationaldragon.potionblender.datatype.PotionBlender.potionTypeData;

@Mixin(TippedArrowItem.class)
public class TippedArrowItemMixin {
    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void potionblender$getName(ItemStack stack, CallbackInfoReturnable<Component> cir) {
        if (PotionType.codeToPotionType.get(stack.get(potionTypeData)) == PotionType.TIPPEDARROW) {
            cir.setReturnValue(Component.translatable("item.potionblender.combined_arrow"));
        }
    }
}
