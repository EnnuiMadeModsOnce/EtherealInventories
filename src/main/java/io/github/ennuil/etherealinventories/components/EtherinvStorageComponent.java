package io.github.ennuil.etherealinventories.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class EtherinvStorageComponent implements Component {
    private Map<UUID, EtherinvStorage> map = new HashMap<>();

    public void add(UUID uuid, EtherinvStorage etherinvStorage) {
        map.put(uuid, etherinvStorage);
    }

    public EtherinvStorage get(UUID uuid) {
        return map.get(uuid);
    }

    public void remove(UUID uuid) {
        map.remove(uuid);
    }

    public boolean hasUuid(UUID uuid) {
        return map.containsKey(uuid);
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        map.clear();
        if (nbt.contains("EtherinvStorage", NbtCompound.LIST_TYPE)) {
            NbtList nbtList = nbt.getList("Etherinvs", NbtCompound.COMPOUND_TYPE);
            for (int i = 0; i < nbtList.size(); i++) {
                NbtCompound compound = nbtList.getCompound(i);
                if (compound.containsUuid("UUID")
                && compound.contains("BlockPos", NbtCompound.LIST_TYPE)
                && compound.contains("Dimension", NbtCompound.STRING_TYPE)
                && compound.contains("IsLoaded", NbtCompound.BYTE_TYPE)) {
                    NbtList blockPosList = compound.getList("BlockPos", NbtCompound.INT_TYPE);
                    BlockPos blockPos = new BlockPos(blockPosList.getInt(0), MathHelper.clamp(blockPosList.getInt(1), -2.0E7, 2.0E7), blockPosList.getInt(2));

                    Optional<RegistryKey<World>> optionalDimension = World.CODEC.parse(NbtOps.INSTANCE, compound.get("LodestoneDimension")).result();
                    RegistryKey<World> dimension = null;
                    if (optionalDimension.isPresent()) {
                        dimension = optionalDimension.get();
                    }

                    Optional<UUID> owner = Optional.empty();
                    if (compound.containsUuid("Owner")) {
                        owner = Optional.of(compound.getUuid("Owner"));
                    }

                    boolean loaded = compound.getBoolean("IsLoaded");
                    
                    map.put(compound.getUuid("UUID"), new EtherinvStorage(blockPos, dimension, owner, loaded));
                }
            }
        }
        // World.CODEC.parse(NbtOps.INSTANCE, nbt.get("LodestoneDimension")).result();
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        NbtList nbtList = new NbtList();
        for (Entry<UUID, EtherinvStorage> entry : map.entrySet()) {
            NbtCompound compound = new NbtCompound();
            // UUID
            compound.putUuid("UUID", entry.getKey());
            
            EtherinvStorage etherinvStorage = entry.getValue();
            // BlockPos
            NbtList posList = new NbtList();
            BlockPos pos = etherinvStorage.getBlockPos();
            posList.add(NbtInt.of(pos.getX()));
            posList.add(NbtInt.of(pos.getY()));
            posList.add(NbtInt.of(pos.getZ()));
            compound.put("BlockPos", posList);
            
            // Dimension
            compound.putString("Dimension", etherinvStorage.getDimension().getValue().toString());

            // Owner
            if (etherinvStorage.getOwner().isPresent()) {
                compound.putUuid("Owner", etherinvStorage.getOwner().get());
            }

            // Loaded
            compound.putBoolean("IsLoaded", etherinvStorage.isLoaded());
            
            nbtList.add(compound);
        }
        nbt.put("EtherinvStorage", nbtList);
    }

    public static class EtherinvStorage {
        private BlockPos blockPos;
        private RegistryKey<World> dimension;
        private Optional<UUID> owner;
        private boolean loaded;

        public EtherinvStorage(BlockPos blockPos, RegistryKey<World> dimension, Optional<UUID> owner, boolean loaded) {
            this.blockPos = blockPos;
            this.dimension = dimension;
            this.owner = owner;
            this.loaded = loaded;
        }

        public BlockPos getBlockPos() {
            return blockPos;
        }

        public void setBlockPos(BlockPos blockPos) {
            this.blockPos = blockPos;
        }

        public RegistryKey<World> getDimension() {
            return dimension;
        }

        public void setDimension(RegistryKey<World> dimension) {
            this.dimension = dimension;
        }

        public Optional<UUID> getOwner() {
            return owner;
        }

        public void setOwner(Optional<UUID> owner) {
            this.owner = owner;
        }

        public boolean isLoaded() {
            return loaded;
        }

        public void setLoaded(boolean loaded) {
            this.loaded = loaded;
        }
    }
}
