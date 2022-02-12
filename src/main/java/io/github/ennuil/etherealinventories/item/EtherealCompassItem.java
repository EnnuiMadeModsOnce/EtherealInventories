package io.github.ennuil.etherealinventories.item;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import eu.pb4.polymer.api.item.PolymerItem;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import io.github.ennuil.etherealinventories.components.EtherealInventoriesComponents;
import io.github.ennuil.etherealinventories.components.EtherinvStorageComponent.EtherinvStorage;
import io.github.ennuil.etherealinventories.entity.EtherinvEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EtherealCompassItem extends Item implements PolymerItem {
    private static final String ETHERINV_UUID_KEY = "EtherinvUUID";
    private static final String DEATH_NUMBER_KEY = "DeathNumber";
    private static final String UPDATE_KEY = "Update";
    private static final String IS_MAGNETIZED_KEY = "isMagnetized";
    private BlockPos pastPos;

    public EtherealCompassItem(Item.Settings settings) {
        super(settings);
        this.pastPos = null;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.COMPASS;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        ItemStack polymerStack = PolymerItemUtils.createItemStack(itemStack, player);
        NbtCompound nbt = itemStack.getNbt();
        NbtCompound polymerNbt = polymerStack.getNbt();
        if (nbt != null && nbt.containsUuid(ETHERINV_UUID_KEY)) {
            EtherinvStorage etherinvStorage = EtherealInventoriesComponents.ETHERINV_STORAGE.get(player.getWorld().getLevelProperties()).get(nbt.getUuid(ETHERINV_UUID_KEY));
            if (etherinvStorage != null) {
                polymerNbt.put("LodestonePos", NbtHelper.fromBlockPos(etherinvStorage.getBlockPos()));
                polymerNbt.putString("LodestoneDimension", etherinvStorage.getDimension().getValue().toString());
                polymerNbt.putBoolean("LodestoneTracked", true);
            }
        }
        return polymerStack;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    public static boolean hasEtherinv(ItemStack stack) {
        NbtCompound nbtCompound = stack.getNbt();
        return nbtCompound != null && (nbtCompound.containsUuid(ETHERINV_UUID_KEY));
    }

    public static UUID getEtherinv(ItemStack stack) {
        return stack.getNbt().getUuid(ETHERINV_UUID_KEY);
    }

    public static void setUpdate(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        boolean update = nbt.contains(UPDATE_KEY) && nbt.getBoolean(UPDATE_KEY);
        nbt.putBoolean(UPDATE_KEY, !update);
    }

    public static int getDeathNumber(ItemStack stack) {
        return stack.getNbt().getInt(DEATH_NUMBER_KEY);
    }

    public static boolean isMagnetized(ItemStack stack) {
        return stack.getNbt().getBoolean(IS_MAGNETIZED_KEY);
    }

    public static void setMagnetized(ItemStack stack, boolean newMagnetized) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putBoolean(IS_MAGNETIZED_KEY, newMagnetized);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            if (hasEtherinv(stack)) {
                if (!EtherealInventoriesComponents.ETHERINV_STORAGE.get(serverWorld.getLevelProperties()).hasUuid(getEtherinv(stack))) {
                    stack.setCount(0);
                } else {
                    EtherinvStorage etherinvStorage = EtherealInventoriesComponents.ETHERINV_STORAGE.get(serverWorld.getLevelProperties()).get(getEtherinv(stack));
                    if (etherinvStorage.getOwner().isPresent()
                    && serverWorld.getEntity(etherinvStorage.getOwner().get()) != null
                    && getDeathNumber(stack) != EtherealInventoriesComponents.ETHERINV.get(serverWorld.getEntity(etherinvStorage.getOwner().get())).getNumberOfDeaths()) {
                        stack.setCount(0);
                    }

                    if (entity.age % 10 == 0) {
                        BlockPos etherinvPos = EtherealInventoriesComponents.ETHERINV_STORAGE.get(serverWorld.getLevelProperties()).get(getEtherinv(stack)).getBlockPos();
                        if (this.pastPos == null) {
                            this.pastPos = etherinvPos;
                        }
    
                        if (!this.pastPos.equals(etherinvPos)) {
                            if (entity instanceof ServerPlayerEntity) {
                                setUpdate(stack);
                            }
                        }
    
                        this.pastPos = etherinvPos;
                    }
                }
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.pass(stack);
        }

        if (!world.isClient && hasEtherinv(stack) && world instanceof ServerWorld serverWorld) {
            if (!EtherealInventoriesComponents.ETHERINV_STORAGE.get(serverWorld.getLevelProperties()).hasUuid(getEtherinv(stack))) {
                stack.setCount(0);
                return TypedActionResult.fail(user.getStackInHand(hand));
            } else if (serverWorld.getEntity(getEtherinv(stack)) instanceof EtherinvEntity etherinv) {
                if (isMagnetized(stack)) {
                    this.pullEtherinv(stack, etherinv, user, hand);
                    return TypedActionResult.success(user.getStackInHand(hand));
                } else {
                    Hand oppositeHand = hand.equals(Hand.MAIN_HAND) ? Hand.OFF_HAND : Hand.MAIN_HAND;
                    if (etherinv.getOwner().isPresent() && user.getStackInHand(oppositeHand).getItem().equals(Items.IRON_INGOT)) {
                        Entity player = serverWorld.getEntity(etherinv.getOwner().get());
                        if (player != null && !EtherealInventoriesComponents.ETHERINV.get(player).isCompassMagnetized()) {
                            EtherealInventoriesComponents.ETHERINV.get(player).setMagnetizedCompass(true);
                            setMagnetized(stack, true);
                            user.getStackInHand(oppositeHand).decrement(1);
                            user.playSound(SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 1.0F, 1.0F);
                            return TypedActionResult.success(user.getStackInHand(hand));
                        }
                    }
                }
            } else {
                return TypedActionResult.pass(user.getStackInHand(hand));
            }
        }

        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    private void pullEtherinv(ItemStack stack, EtherinvEntity etherinv, PlayerEntity player, Hand hand) {
        // Pull the etherinv with code burglar'd from Crooked Crooks
        Vec3d pos = player.getEyePos().subtract(etherinv.getPos());
        pos = pos.subtract(player.getRotationVector());
        pos = pos.multiply(0.125);

        etherinv.setVelocity(pos);
        etherinv.velocityModified = true;
        etherinv.fallDistance = 0.0F;

        //etherinv.playSound(SoundEvents.BLOCK_CONDUIT_ATTACK_TARGET, 1.0F, 2.0F);
        if (stack.getDamage() + 1 != stack.getMaxDamage()) {
            System.out.println(String.format("%s %s", stack.getDamage(), stack.getMaxDamage()));
            float extraPitch = 1.0F - (((stack.getDamage()) / (stack.getMaxDamage() - 2.0F)));
            System.out.println(extraPitch);
            System.out.println(MathHelper.sin(extraPitch));
            player.playSound(SoundEvents.BLOCK_LODESTONE_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
            player.playSound(SoundEvents.ENTITY_ENDER_EYE_DEATH, SoundCategory.PLAYERS, 2.0F, 1.0F + extraPitch);
        }

        stack.damage(1, player, user -> user.sendToolBreakStatus(hand));
        player.getItemCooldownManager().set(this, 10);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        if (isMagnetized(stack)) {
            if (stack.getDamage() >= 4) {
                return "item.etherealinventories.shattering_magnetized_ethereal_compass";
            } else if (stack.getDamage() >= 2) {
                return "item.etherealinventories.cracked_magnetized_ethereal_compass";
            }

            return "item.etherealinventories.magnetized_ethereal_compass";
        }
        return super.getTranslationKey(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        if (!isMagnetized(stack)) {
            tooltip.add(new TranslatableText("item.etherealinventories.ethereal_compass.tooltip").formatted(Formatting.GRAY));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }
}
