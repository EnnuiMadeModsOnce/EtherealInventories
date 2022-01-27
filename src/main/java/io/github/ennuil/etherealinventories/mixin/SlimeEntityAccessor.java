package io.github.ennuil.etherealinventories.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.SlimeEntity;

@Mixin(SlimeEntity.class)
public interface SlimeEntityAccessor {
    @Accessor(value = "SLIME_SIZE")
    static TrackedData<Integer> getSlimeSize() {
        return null;
    }
}
