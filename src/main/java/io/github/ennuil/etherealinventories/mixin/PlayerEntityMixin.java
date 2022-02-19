package io.github.ennuil.etherealinventories.mixin;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.ennuil.etherealinventories.EtherealInventoriesMod;
import io.github.ennuil.etherealinventories.components.EtherealInventoriesComponents;
import io.github.ennuil.etherealinventories.components.EtherinvComponent;
import io.github.ennuil.etherealinventories.entity.EtherinvEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract void sendMessage(Text message, boolean actionBar);

    @Shadow
    public abstract PlayerInventory getInventory();

    @Shadow
    protected void vanishCursedItems() {}

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;vanishCursedItems()V"
        ),
        method = "dropInventory",
        cancellable = true
    )
    private void moveInventoryToEtherinv(CallbackInfo ci) {
        if (this.world.isClient) return;

        if (EtherealInventoriesComponents.SOULBOUND.get(this).isSoulbound()) {
            this.sendMessage(new TranslatableText("chat.etherinv.soulbound.respawn"), false);
            ci.cancel();
            return;
        }

        ServerWorld serverWorld = (ServerWorld)this.getWorld();
        EtherinvComponent etherinv = EtherealInventoriesComponents.ETHERINV.get(this);
        boolean hasEtherinv = etherinv.getEtherinv().isPresent();
        if (hasEtherinv && !EtherealInventoriesComponents.ETHERINV_STORAGE.get(serverWorld.getLevelProperties()).hasUuid(etherinv.getEtherinv().get())) {
            etherinv.setEtherinv(Optional.empty());
            hasEtherinv = false;
        }

        if (!etherinv.isCompassMagnetized()) {
            // This vanishes ethereal compasses
            etherinv.incrementNumberOfDeaths();
            this.getInventory().updateItems();
        }

        if (!hasEtherinv && !this.getInventory().isEmpty()) {
            this.vanishCursedItems();

            BlockPos etherinvPos = new BlockPos(this.getEyePos().getX(), MathHelper.clamp(this.getEyePos().getY(), this.world.getBottomY(), this.world.getTopY()), this.getEyePos().getZ());
    
            EtherinvEntity entity = EtherealInventoriesMod.ETHERINV_ENTITY_TYPE.create(
                serverWorld,
                null,
                null,
                null,
                new BlockPos(etherinvPos),
                SpawnReason.CONVERSION,
                true,
                false
            );
            entity.setOwner(Optional.of(this.getUuid()));
            entity.setInventory(List.of(this.getInventory().main, this.getInventory().armor, this.getInventory().offHand));
            serverWorld.spawnEntity(entity);
            this.getInventory().clear();
            etherinv.setEtherinv(Optional.of(entity.getUuid()));

            ci.cancel();
            return;
        }
    }
}
