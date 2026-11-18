package mod.motivationaldragon.potionblender.mixins;

import mod.motivationaldragon.potionblender.utils.PotionType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static mod.motivationaldragon.potionblender.Constants.*;
import static mod.motivationaldragon.potionblender.datatype.PotionBlender.potionTypeData;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin {
    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void potionblender$getName(ItemStack stack, CallbackInfoReturnable<Component> cir) {
        PotionType type = PotionType.codeToPotionType.get(stack.get(potionTypeData));
        if (type == null) return;
        String key = switch (type) {
            case NORMAL -> COMBINED_POTION_NAME;
            case SPLASH -> COMBINED_SPLASH_POTION_NAME;
            case LINGERING -> COMBINED_LINGERING_POTION;
            case TIPPEDARROW -> "item.potionblender.combined_arrow";
        };
        cir.setReturnValue(Component.translatable(key));
    }
}
