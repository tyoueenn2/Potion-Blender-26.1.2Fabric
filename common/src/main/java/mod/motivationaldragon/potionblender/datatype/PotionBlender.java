package mod.motivationaldragon.potionblender.datatype;

import com.mojang.serialization.Codec;
import mod.motivationaldragon.potionblender.Constants;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

import java.util.function.BiConsumer;

public class PotionBlender {
	public static DataComponentType<Integer> potionTypeData = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();

	public static void registerDataComponentType(BiConsumer<DataComponentType<?>, Identifier> r) {
	    r.accept(potionTypeData,  Identifier.fromNamespaceAndPath(Constants.MOD_ID, "potiontypedata"));
	}
}
