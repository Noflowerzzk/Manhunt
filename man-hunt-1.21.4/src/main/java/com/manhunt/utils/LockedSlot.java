package com.manhunt.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class LockedSlot extends Slot {
    public LockedSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return false; // 禁止取出物品
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false; // 禁止放入物品
    }
}