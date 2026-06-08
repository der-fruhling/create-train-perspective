package net.derfruhling.minecraft.create.trainperspective.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.simibubi.create.content.contraptions.actors.seat.ContraptionPlayerPassengerRotation;
import net.derfruhling.minecraft.create.trainperspective.ModConfig;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ContraptionPlayerPassengerRotation.class, remap = false)
public class ContraptionPlayerPassengerRotationMixin {
    @Contract(pure = true)
    @ModifyExpressionValue(method = "tick", require = 0, at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/config/ConfigBase$ConfigBool;get()Ljava/lang/Object;"))
    private static @NotNull Object modifyConfig(Object original) {
        return ((Boolean) original) && !ModConfig.INSTANCE.disableRotateWhenSeated;
    }

    @Contract(pure = true)
    @ModifyExpressionValue(method = "tick", require = 0, at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/config/ConfigBase$ConfigBool;get()Ljava/lang/Object;"))
    private static @NotNull Object modifyConfigForCreate5(Object original) {
        return ((Boolean) original) && !ModConfig.INSTANCE.disableRotateWhenSeated;
    }
}
