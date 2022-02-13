package io.github.ennuil.etherealinventories.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.ennuil.etherealinventories.components.EtherealInventoriesComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;sendEntityStatus(Lnet/minecraft/entity/Entity;B)V"
        ),
        method = "tryUseTotem"
    )
    private void soulbindPlayersInventory(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayerEntity player) {
            if (!EtherealInventoriesComponents.SOULBOUND.get(player).isSoulbound()) {
                EtherealInventoriesComponents.SOULBOUND.get(player).setSoulbound(true);
                player.sendMessage(new TranslatableText("chat.etherinv.soulbound.acquired"), true);
            }
        }
    }
}
