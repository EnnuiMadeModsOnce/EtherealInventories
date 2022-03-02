package io.github.ennuil.etherealinventories.mixin;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.ennuil.etherealinventories.EtherealInventoriesMod;
import io.github.ennuil.etherealinventories.components.EtherealInventoriesComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(world, blockPos, f, gameProfile);
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
                    //this.giveItemStack(stack);
                    this.getInventory().insertStack(PlayerInventory.OFF_HAND_SLOT, stack);
                }
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void tickIfSoulbound(CallbackInfo ci) {
        if (this.age % 5 == 0) {
            if (EtherealInventoriesComponents.SOULBOUND.get(this).isSoulbound()) {
                ((ServerWorld)this.world).spawnParticles((ServerPlayerEntity)(Object)this, ParticleTypes.WITCH, true, this.getX(), this.getY(), this.getZ(), 2, 0.0, 0.0, 0.0, 0.25);
            }
            if (EtherealInventoriesComponents.ETHERINV.get(this).getEtherinv().isPresent()) {
                ((ServerWorld)this.world).spawnParticles((ServerPlayerEntity)(Object)this, ParticleTypes.END_ROD, true, this.getX(), this.getY() + 0.125, this.getZ(), 1, 0.25, 0.125, 0.25, 0.125);
            }
        }
    }
}
