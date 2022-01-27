package io.github.ennuil.etherealinventories.sgui;

import org.jetbrains.annotations.Nullable;

import eu.pb4.sgui.api.gui.SlotGuiInterface;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandlerType;

// canUse is set to true always??? I won't allow this
public class InventoryLinkedVirtualScreenHandler extends VirtualScreenHandler {
    private Inventory inventory;

    public InventoryLinkedVirtualScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId, SlotGuiInterface gui, PlayerEntity player, Inventory inventory) {
        super(type, syncId, gui, player);
        this.inventory = inventory;
    }
    
    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }
}
