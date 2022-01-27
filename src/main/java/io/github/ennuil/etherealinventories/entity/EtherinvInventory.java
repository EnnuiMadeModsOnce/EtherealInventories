package io.github.ennuil.etherealinventories.entity;

import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class EtherinvInventory implements Inventory {
    public DefaultedList<ItemStack> main = DefaultedList.ofSize(36, ItemStack.EMPTY);
    public DefaultedList<ItemStack> armor = DefaultedList.ofSize(4, ItemStack.EMPTY);
    public DefaultedList<ItemStack> offHand = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private final List<DefaultedList<ItemStack>> combinedInventory = List.of(this.main, this.armor, this.offHand);
    public final EtherinvEntity etherInv;

    public EtherinvInventory(EtherinvEntity etherInv) {
        this.etherInv = etherInv;
    }

    @Override
    public void clear() {
        for (DefaultedList<ItemStack> inventory : this.combinedInventory) {
            inventory.clear();
        }
    }

    @Override
    public int size() {
        return this.main.size() + this.armor.size() + this.offHand.size();
    }

    @Override
    public boolean isEmpty() {
        for (DefaultedList<ItemStack> inventory : this.combinedInventory) {
            for (ItemStack stack : inventory) {
                if (!stack.isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        DefaultedList<ItemStack> selectedInventory = null;

        for (DefaultedList<ItemStack> inventory : this.combinedInventory) {
            if (slot < inventory.size()) {
                selectedInventory = inventory;
                break;
            }

            slot -= inventory.size();
        }
        
        return selectedInventory != null ? selectedInventory.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        DefaultedList<ItemStack> selectedInventory = null;

        for (DefaultedList<ItemStack> inventory : this.combinedInventory) {
            if (slot < inventory.size()) {
                selectedInventory = inventory;
                break;
            }

            slot -= inventory.size();
        }
        
        return selectedInventory != null ? Inventories.splitStack(selectedInventory, slot, amount) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        DefaultedList<ItemStack> selectedInventory = null;

        for (DefaultedList<ItemStack> inventory : this.combinedInventory) {
            if (slot < inventory.size()) {
                selectedInventory = inventory;
                break;
            }

            slot -= inventory.size();
        }

        if (selectedInventory != null) {
            ItemStack removedStack = main.get(slot);
            if (!removedStack.isEmpty()) {
                main.remove(slot);
                return removedStack;
            }
        }
        
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        DefaultedList<ItemStack> selectedInventory = null;

        for (DefaultedList<ItemStack> inventory : this.combinedInventory) {
            if (slot < inventory.size()) {
                selectedInventory = inventory;
                break;
            }

            slot -= inventory.size();
        }

        if (selectedInventory != null) {
            selectedInventory.set(slot, stack);
        }
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public void markDirty() {
        if (this.isEmpty()) {
            this.etherInv.poof();
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (this.etherInv.isRemoved()) {
            return false;
        } else {
            return !(player.squaredDistanceTo(this.etherInv) > 64.0);
        }
    }
}