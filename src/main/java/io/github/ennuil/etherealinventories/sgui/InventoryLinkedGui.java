package io.github.ennuil.etherealinventories.sgui;

import java.util.OptionalInt;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public class InventoryLinkedGui extends SimpleGui {
    protected Inventory inventory;

    public InventoryLinkedGui(ScreenHandlerType<?> type, ServerPlayerEntity player, Inventory inventory, boolean includePlayerInventorySlots) {
        super(type, player, includePlayerInventorySlots);
        this.inventory = inventory;
    }
    
    @Override
    protected boolean sendGui() {
        this.reOpen = true;
        OptionalInt temp = this.player.openHandledScreen(new InventoryLinkedVirtualScreenHandlerFactory(this));
        this.reOpen = false;
        if (temp.isPresent()) {
            this.syncId = temp.getAsInt();
            if (this.player.currentScreenHandler instanceof InventoryLinkedVirtualScreenHandler) {
                this.screenHandler = (InventoryLinkedVirtualScreenHandler) this.player.currentScreenHandler;
                return true;
            }
        }
        return false;
    }
}
