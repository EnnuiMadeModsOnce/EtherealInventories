package io.github.ennuil.etherealinventories.components;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class SoulboundComponent implements Component {
    private boolean soulbound;

    public SoulboundComponent() {
        this.soulbound = false;
    }

    public boolean isSoulbound() {
        return this.soulbound;
    }

    public void setSoulbound(boolean soulbound) {
        this.soulbound = soulbound;
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        if (nbt.contains("Soulbound", NbtElement.BYTE_TYPE)) {
            this.soulbound = nbt.getBoolean("Soulbound");
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        nbt.putBoolean("Soulbound", this.soulbound);
    }
}