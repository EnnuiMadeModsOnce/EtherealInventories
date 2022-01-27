package io.github.ennuil.etherealinventories.entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.ennuil.etherealinventories.components.EtherealInventoriesComponents;
import io.github.ennuil.etherealinventories.components.EtherinvComponent;
import io.github.ennuil.etherealinventories.components.EtherinvStorageComponent.EtherinvStorage;
import io.github.ennuil.etherealinventories.mixin.SlimeEntityAccessor;
import io.github.ennuil.etherealinventories.sgui.InventoryLinkedGui;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker.Entry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class EtherinvEntity extends Entity implements PolymerEntity {
    private static final TrackedData<Optional<UUID>> ETHERINV_OWNER = DataTracker.registerData(EtherinvEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private static final TrackedData<Optional<UUID>> OWNER_HEAD_UUID = DataTracker.registerData(EtherinvEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private static final TrackedData<Integer> STRIKES = DataTracker.registerData(EtherinvEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private EtherinvInventory inventory;
    
    public EtherinvEntity(EntityType<? extends EtherinvEntity> entityType, World world) {
        super(entityType, world);
        this.inventory = new EtherinvInventory(this);
    }

    @Override
    public void onBeforeSpawnPacket(Consumer<Packet<?>> packetConsumer) {
        if (!this.world.isClient) {
            if (!EtherealInventoriesComponents.ETHERINV_STORAGE.get(this.world.getLevelProperties()).hasUuid(this.uuid)) {
                System.out.println(String.format("Adding %s", this.uuid));
                EtherealInventoriesComponents.ETHERINV_STORAGE.get(this.world.getLevelProperties()).add(this.uuid, new EtherinvStorage(
                    this.getBlockPos(),
                    this.getWorld().getRegistryKey(),
                    this.getOwner(),
                    this.isRemoved()));
            } else {
                EtherealInventoriesComponents.ETHERINV_STORAGE.get(this.world.getLevelProperties()).get(this.uuid).setLoaded(true);
            }
        }
    }

    @Override
    public void tick() {
        this.noClip = true;
        if (this.world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 0.5, this.getZ(), 3, 0.0, 0.0, 0.0, 0.125);
            switch (this.getStrikes()) {
                case 2 -> {
                    serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 0.5, this.getZ(), 3, 0.0, 0.0, 0.0, 0.25);
                    serverWorld.spawnParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 0.5, this.getZ(), 1, 0.0, 0.0, 0.0, 0.125);
                }
                case 1 -> {
                    if (this.age % 10 == 0) {
                        serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.5, this.getZ(), 2, 0.0, 0.0, 0.0, 0.25);
                    }
                    serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 0.5, this.getZ(), 3, 0.0, 0.0, 0.0, 0.5);
                    serverWorld.spawnParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 0.5, this.getZ(), 2, 0.0, 0.0, 0.0, 0.25);
                }
            }
            
            if (this.getStrikes() <= 0) { 
                this.explode();
            }
            
            if (this.getOwner().isPresent()) {
                if (this.getOwnerHeadUuid().isEmpty()) {
                    ItemStack headStack = Items.PLAYER_HEAD.getDefaultStack();
                    NbtCompound headStackData = new NbtCompound();
                    NbtCompound headStackDataUuid = new NbtCompound();
                    headStackDataUuid.putUuid("Id", this.getOwner().get());
                    headStackData.put("SkullOwner", headStackDataUuid);
                    headStack.setNbt(headStackData);
                    
                    ItemEntity headEntity = new EtherinvItemEntity(serverWorld, this.getX(), this.getY() + 0.25, this.getZ(), headStack);
                    serverWorld.spawnEntity(headEntity);
                    this.setOwnerHeadUuid(Optional.of(headEntity.getUuid()));
                }
            }
        }

        if (!this.getVelocity().equals(Vec3d.ZERO)) {
            this.move(MovementType.SELF, this.getVelocity());
        }
        this.setVelocity(this.getVelocity().multiply(0.5D));

        super.tick();
        this.noClip = false;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);
        if (this.world instanceof ServerWorld serverWorld) {
            if (this.getOwnerHeadUuid().isPresent()) {
                Entity entity = serverWorld.getEntity(this.getOwnerHeadUuid().get());
                if (entity != null) {
                    entity.setPosition(x, y + 0.25, z);
                }
            }

            EtherinvStorage etherinvStorage = EtherealInventoriesComponents.ETHERINV_STORAGE.get(serverWorld.getLevelProperties()).get(this.uuid);
            if (etherinvStorage != null) {
                etherinvStorage.setBlockPos(this.getBlockPos());
            }
        }
    }

    @Override
    public EntityType<?> getPolymerEntityType() {
        return EntityType.SLIME;
    }

    @Override
    protected void initDataTracker() {
        this.setGlowing(true);
        this.setInvisible(true);
        this.dataTracker.startTracking(ETHERINV_OWNER, Optional.empty());
        this.dataTracker.startTracking(OWNER_HEAD_UUID, Optional.empty());
        this.dataTracker.startTracking(STRIKES, 3);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.containsUuid("Owner")) {
            this.setOwner(Optional.of(nbt.getUuid("Owner")));
        }

        if (nbt.containsUuid("OwnerHeadUUID")) {
            this.setOwnerHeadUuid(Optional.of(nbt.getUuid("OwnerHeadUUID")));
        }

        if (nbt.contains("Strikes", NbtElement.INT_TYPE)) {
            this.setStrikes(nbt.getInt("Strikes"));
        }
        
        this.inventory.clear();

        NbtList list = nbt.getList("Inventory", NbtType.COMPOUND);
        for(int i = 0; i < list.size(); ++i) {
            NbtCompound nbtCompound = list.getCompound(i);
            int j = nbtCompound.getByte("Slot");
            if (j >= 0 && j < this.inventory.size()) {
                this.inventory.setStack(j, ItemStack.fromNbt(nbtCompound));
            }
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (this.getOwner().isPresent()) {
            nbt.putUuid("Owner", this.getOwner().get());
        }

        if (this.getOwnerHeadUuid().isPresent()) {
            nbt.putUuid("OwnerHeadUUID", this.getOwnerHeadUuid().get());
        }

        nbt.putInt("Strikes", this.getStrikes());

        // TODO - Move this to the class
        NbtList list = new NbtList();
        for (int i = 0; i < this.inventory.size(); ++i) {
            if (!this.inventory.getStack(i).isEmpty()) {
                NbtCompound compound = new NbtCompound();
                compound.putByte("Slot", (byte)i);
                this.inventory.getStack(i).writeNbt(compound);
                list.add(compound);
            }
        }
        nbt.put("Inventory", list);
    }

    @Override
    public Packet<?> createSpawnPacket() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(this.getId());
        buf.writeUuid(this.getUuid());
        buf.writeVarInt(Registry.ENTITY_TYPE.getRawId(this.getPolymerEntityType()));
        Vec3d clientPos = this.getClientSidePosition(this.getPos());
        buf.writeDouble(clientPos.x);
        buf.writeDouble(clientPos.y);
        buf.writeDouble(clientPos.z);
        buf.writeByte((byte)((int)(this.getYaw() * 256.0F / 360.0F)));
        buf.writeByte((byte)((int)(this.getPitch() * 256.0F / 360.0F)));
        buf.writeByte((byte)((int)(this.getYaw() * 256.0F / 360.0F)));
        double d = 3.9;
        Vec3d vec3d = this.getVelocity();
        buf.writeInt((int)(MathHelper.clamp(vec3d.x, -d, d) * 8000));
        buf.writeInt((int)(MathHelper.clamp(vec3d.y, -d, d) * 8000));
        buf.writeInt((int)(MathHelper.clamp(vec3d.z, -d, d) * 8000));
        return new MobSpawnS2CPacket(buf);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof PlayerEntity player && amount >= 1.0F) {
            if (this.getOwner().isEmpty() || ((this.getOwner().isPresent() && this.getOwner().get().equals(player.getUuid())))) {
                this.setStrikes(this.getStrikes() - 1);
            }
        }
        return super.damage(source, amount);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!this.world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            if (this.getOwner().isEmpty() || ((this.getOwner().isPresent() && this.getOwner().get().equals(player.getUuid())))) {
                SimpleGui gui = new InventoryLinkedGui(ScreenHandlerType.GENERIC_9X5, serverPlayer, this.inventory, false);
                gui.setTitle(new TranslatableText("container.etherinv"));
                for (int i = 9; i < this.inventory.main.size(); i++) {
                    gui.addSlotRedirect(new EtherinvSlot(this.inventory, i, 0, 0));
                }
                for (int i = 0; i < 9; i++) {
                    gui.addSlotRedirect(new EtherinvSlot(this.inventory, i, 0, 0));
                }
                gui.setSlotRedirect(38, new EtherinvSlot(this.inventory, 39, 0, 0));
                gui.setSlotRedirect(39, new EtherinvSlot(this.inventory, 38, 0, 0));
                gui.setSlotRedirect(40, new EtherinvSlot(this.inventory, 37, 0, 0));
                gui.setSlotRedirect(41, new EtherinvSlot(this.inventory, 36, 0, 0));
                gui.setSlotRedirect(42, new EtherinvSlot(this.inventory, 40, 0, 0));
                
                gui.open();
                return ActionResult.SUCCESS;
            } else {
                serverPlayer.sendMessage(new TranslatableText("container.etherinv.not_owner"), true);
                return ActionResult.FAIL;
            }
        }

        return super.interact(player, hand);
    }
    
    public void setOwner(Optional<UUID> owner) {
        this.dataTracker.set(ETHERINV_OWNER, owner);
    }

    public Optional<UUID> getOwner() {
        return this.dataTracker.get(ETHERINV_OWNER);
    }

    public void setOwnerHeadUuid(Optional<UUID> owner) {
        this.dataTracker.set(OWNER_HEAD_UUID, owner);
    }

    public Optional<UUID> getOwnerHeadUuid() {
        return this.dataTracker.get(OWNER_HEAD_UUID);
    }

    public void setStrikes(int strikes) {
        this.dataTracker.set(STRIKES, strikes);
    }

    public int getStrikes() {
        return this.dataTracker.get(STRIKES);
    }

    public void setInventory(List<DefaultedList<ItemStack>> inventory) {
        this.inventory.clear();
        int slot = 0;
        for (DefaultedList<ItemStack> subInventory : inventory) {
            for (ItemStack stack : subInventory) {
                this.inventory.setStack(slot, stack);
                slot++;
            }
        }
    }

    @Override
    public void modifyTrackedData(List<Entry<?>> data) {
        data.add(new DataTracker.Entry<>(SlimeEntityAccessor.getSlimeSize(), 2));
    }

    @Override
    public float getClientSideYaw(float yaw) {
        return 0.0F;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.PLAYERS;
    }

    public void explode() {
        if (!this.world.isClient) {
            ItemScatterer.spawn(this.world, this.getBlockPos(), inventory);
            this.playSound(SoundEvents.ITEM_TOTEM_USE, 0.8F, 1.0F);
            if (this.world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 0.5, this.getZ(), 100, 0.0, 0.0, 0.0, 2.0);
                serverWorld.spawnParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.5, this.getZ(), 150, 0.0, 0.0, 0.0, 0.5);
                serverWorld.spawnParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 0.5, this.getZ(), 50, 0.0, 0.0, 0.0, 1.0);
            }
        }
        this.discard();
    }
 
    public void poof() {
        if (!this.world.isClient) {
            this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HURT, 0.8F, 1.0F);
            if (this.world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.PORTAL, this.getX(), this.getBlockY() + 0.5, this.getZ(), 50, 0.0, 0.0, 0.0, 2.0);
                serverWorld.spawnParticles(ParticleTypes.WITCH, this.getX(), this.getBlockY() + 0.5, this.getZ(), 25, 0.0, 0.0, 0.0, 1.0);
            }
        }
        this.discard();
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.world instanceof ServerWorld serverWorld) {
            if (reason.shouldDestroy()) {
                if (this.getOwnerHeadUuid().isPresent()) {
                    Entity entity = serverWorld.getEntity(this.getOwnerHeadUuid().get());
                    if (entity != null) {
                        entity.remove(reason);
                    }
                }
    
                if (this.getOwner().isPresent()) {
                    Entity player = serverWorld.getEntity(this.getOwner().get());
                    if (player != null) {
                        EtherinvComponent etherinv = EtherealInventoriesComponents.ETHERINV.get(player);
                        if (etherinv.getEtherinv().isPresent()) {
                            if (etherinv.getEtherinv().get().equals(this.uuid)) {
                                etherinv.setEtherinv(Optional.empty());
                            }
                        }
                    }
                }
    
                System.out.println(String.format("Removing %s...", this.uuid));
                EtherealInventoriesComponents.ETHERINV_STORAGE.get(serverWorld.getLevelProperties()).remove(this.uuid);
            } else {
                EtherealInventoriesComponents.ETHERINV_STORAGE.get(serverWorld.getLevelProperties()).get(this.uuid).setLoaded(false);
            }
        }

        super.remove(reason);
    }

    @Override
    public void refreshPositionAndAngles(double x, double y, double z, float yaw, float pitch) {
        super.refreshPositionAndAngles(x, y, z, 0.0F, pitch);
    }

    @Override
    public boolean collides() {
        return !this.isRemoved();
    }
}
