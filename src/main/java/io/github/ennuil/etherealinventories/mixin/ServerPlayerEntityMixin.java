package io.github.ennuil.etherealinventories.mixin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.ennuil.etherealinventories.EtherealInventoriesMod;
import io.github.ennuil.etherealinventories.components.EtherealInventoriesComponents;
import io.github.ennuil.etherealinventories.components.EtherinvComponent;
import io.github.ennuil.etherealinventories.entity.EtherinvEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(world, blockPos, f, gameProfile);
    }

    @Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;drop(Lnet/minecraft/entity/damage/DamageSource;)V"
        ),
        method = "onDeath"
    )
    protected void moveInventoryToEtherinv(ServerPlayerEntity player, DamageSource source) {
        boolean shouldKeepInventory = this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || player.isSpectator();
        if (shouldKeepInventory) {
            this.drop(source);
            return;
        }

        if (EtherealInventoriesComponents.SOULBOUND.get(this).isSoulbound()) {
            player.sendMessage(new TranslatableText("chat.etherinv.soulbound.respawn"), false);
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
            
            return;
        }

        this.drop(source);
    }

    @Inject(at = @At("TAIL"), method = "copyFrom")
    private void keepInventoryIfSoulbound(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if (!alive && !this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) && !oldPlayer.isSpectator()) {
            if (EtherealInventoriesComponents.SOULBOUND.get(oldPlayer).isSoulbound()) {
                this.getInventory().clone(oldPlayer.getInventory());
                this.experienceLevel = oldPlayer.experienceLevel;
                this.totalExperience = oldPlayer.totalExperience;
                this.experienceProgress = oldPlayer.experienceProgress;
                this.setScore(oldPlayer.getScore());
                    
                EtherealInventoriesComponents.SOULBOUND.get(oldPlayer).setSoulbound(false);
            }
            
            if (EtherealInventoriesComponents.ETHERINV.get(oldPlayer).getEtherinv().isPresent()) {
                if (!EtherealInventoriesComponents.ETHERINV.get(oldPlayer).isCompassMagnetized()) {
                    ItemStack stack = EtherealInventoriesMod.ETHERAL_COMPASS_ITEM.getDefaultStack();
                    UUID etherinvUuid = EtherealInventoriesComponents.ETHERINV.get(oldPlayer).getEtherinv().get();
                    stack.getOrCreateNbt().putUuid("EtherinvUUID", etherinvUuid);
                    stack.getNbt().putInt("DeathNumber", EtherealInventoriesComponents.ETHERINV.get(oldPlayer).getNumberOfDeaths());
                    this.giveItemStack(stack);
                }
            }
        }
    }
}
