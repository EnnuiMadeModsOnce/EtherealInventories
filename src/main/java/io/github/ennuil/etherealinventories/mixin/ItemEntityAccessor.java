package io.github.ennuil.etherealinventories.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.item.ItemStack;

@Mixin(ItemEntity.class)
public abstract interface ItemEntityAccessor {
    @Accessor(value = "STACK")
    static TrackedData<ItemStack> getStack() {
        return null;
    }
}
