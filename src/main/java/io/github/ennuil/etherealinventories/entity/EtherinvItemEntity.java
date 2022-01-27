package io.github.ennuil.etherealinventories.entity;

import eu.pb4.polymer.api.entity.PolymerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EtherinvItemEntity extends ItemEntity implements PolymerEntity {
    public EtherinvItemEntity(EntityType<? extends EtherinvItemEntity> entityType, World world) {
        super(entityType, world);
        // Yes, the server knows that setVelocity has been overriden, but the client doesn't know that
        this.setNoGravity(true);
        this.setPickupDelayInfinite();
        this.setNeverDespawn();
    }

    public EtherinvItemEntity(World world, double x, double y, double z, ItemStack itemStack) {
        super(world, x, y, z, itemStack, 0, 0, 0);
        this.setNoGravity(true);
        this.setPickupDelayInfinite();
        this.setNeverDespawn();
    }

    @Override
    public EntityType<?> getPolymerEntityType() {
        return EntityType.ITEM;
    }

    @Override
    public void setVelocity(Vec3d velocity) {}

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return this.isRemoved() || damageSource != DamageSource.OUT_OF_WORLD && !damageSource.isSourceCreativePlayer();
    }
}
