package io.github.ennuil.etherealinventories.entity;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class EtherinvSlot extends Slot {
    public EtherinvSlot(Inventory inventory, int i, int j, int k) {
        super(inventory, i, j, k);
    }
    
    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }
}
