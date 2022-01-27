package io.github.ennuil.etherealinventories.sgui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public record InventoryLinkedVirtualScreenHandlerFactory(InventoryLinkedGui gui) implements NamedScreenHandlerFactory {
    @Override
    public Text getDisplayName() {
        return this.gui.getTitle() != null ? this.gui.getTitle() : new LiteralText("");
    }
    
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new InventoryLinkedVirtualScreenHandler(this.gui.getType(), syncId, this.gui, playerEntity, this.gui.inventory);
    }
}
