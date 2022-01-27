package io.github.ennuil.etherealinventories.components;

import java.util.Optional;
import java.util.UUID;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class EtherinvComponent implements Component {
    private Optional<UUID> etherinv;
    private int numberOfDeaths;
    private boolean magnetizedCompass;

    public EtherinvComponent() {
        this.etherinv = Optional.empty();
        this.numberOfDeaths = 0;
        this.magnetizedCompass = false;
    }

    public Optional<UUID> getEtherinv() {
        return this.etherinv;
    }

    public void setEtherinv(Optional<UUID> etherinv) {
        this.etherinv = etherinv;
        this.numberOfDeaths = 0;
        this.magnetizedCompass = false;
    }

    public int getNumberOfDeaths() {
        return numberOfDeaths;
    }

    public void setNumberOfDeaths(int numberOfDeaths) {
        this.numberOfDeaths = numberOfDeaths;
    }

    public void incrementNumberOfDeaths() {
        this.numberOfDeaths++;
    }

    public boolean isCompassMagnetized() {
        return magnetizedCompass;
    }

    public void setMagnetizedCompass(boolean magnetizedCompass) {
        this.magnetizedCompass = magnetizedCompass;
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        if (nbt.containsUuid("Etherinv")) {
            this.etherinv = Optional.of(nbt.getUuid("Etherinv"));
            if (nbt.contains("NumberOfDeaths", NbtElement.INT_TYPE)) {
                this.numberOfDeaths = nbt.getInt("NumberOfDeaths");
            }
            if (nbt.contains("MagnetizedCompass", NbtElement.BYTE_TYPE)) {
                this.magnetizedCompass = nbt.getBoolean("MagnetizedCompass");
            }
        } else {
            this.etherinv = Optional.empty();
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        if (this.etherinv.isPresent()) {
            nbt.putUuid("Etherinv", this.etherinv.get());
            nbt.putInt("NumberOfDeaths", this.numberOfDeaths);
            nbt.putBoolean("MagnetizedCompass", this.magnetizedCompass);
        }
    }
}
