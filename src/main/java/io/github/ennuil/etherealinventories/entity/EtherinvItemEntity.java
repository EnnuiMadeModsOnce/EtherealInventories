package io.github.ennuil.etherealinventories.entity;

import java.util.List;

import eu.pb4.polymer.api.entity.PolymerEntity;
import io.github.ennuil.etherealinventories.mixin.ItemEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.DataTracker.Entry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.NbtCompound;
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
    public void modifyTrackedData(List<Entry<?>> data) {
        ItemStack headStack = Items.PLAYER_HEAD.getDefaultStack();
        NbtCompound headStackData = new NbtCompound();
        NbtCompound headStackDataUuid = new NbtCompound();
        headStackDataUuid.putUuid("Id", this.getThrower());
        headStackData.put(SkullItem.SKULL_OWNER_KEY, headStackDataUuid);
        headStack.setNbt(headStackData);
        data.add(new DataTracker.Entry<>(ItemEntityAccessor.getStack(), headStack));
    }

    @Override
    public void setVelocity(Vec3d velocity) {}

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return this.isRemoved() || damageSource != DamageSource.OUT_OF_WORLD && !damageSource.isSourceCreativePlayer();
    }
}
