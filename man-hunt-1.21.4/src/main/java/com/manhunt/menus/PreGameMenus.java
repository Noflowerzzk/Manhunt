package com.manhunt.menus;

import com.manhunt.pregame.TeamHandler;
import com.manhunt.runtime.RunTimeControl;
import com.manhunt.utils.LockedSlot;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class PreGameMenus extends GenericContainerScreenHandler implements NamedScreenHandlerFactory {
    private final Inventory inventory;

    public PreGameMenus(int syncId, PlayerInventory playerInventory) {
        super(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, new SimpleInventory(54), 6);
        inventory = this.getInventory();

        for (int i = 0; i < this.slots.size(); i++) {
            Slot oldSlot = this.slots.get(i);
            this.slots.set(i, new LockedSlot(oldSlot.inventory, oldSlot.getIndex(), oldSlot.x, oldSlot.y));
        }

//        inventory.setStack(0, new ItemStack(Items.DIAMOND).setCustomName(Text.of("选项A")));
//        ItemStack start_game_diamond = new ItemStack(Items.DIAMOND);
//        start_game_diamond.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§a准备"));
//        inventory.setStack(9, start_game_diamond);

        ItemStack get_ready_emerald = new ItemStack(Items.EMERALD);
        get_ready_emerald.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§a开始游戏"));
        inventory.setStack(10, get_ready_emerald);

        ItemStack chaser_wool = new ItemStack(Items.RED_WOOL);
        chaser_wool.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§c加入追杀者"));
        inventory.setStack(13, chaser_wool);

        ItemStack runner_wool = new ItemStack(Items.GREEN_WOOL);
        runner_wool.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§a加入逃生者"));
        inventory.setStack(14, runner_wool);

        ItemStack spectator_wool = new ItemStack(Items.GRAY_WOOL);
        spectator_wool.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§7加入旁观者"));
        inventory.setStack(15, spectator_wool);

    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public Text getDisplayName() {
        return Text.of("Pre Game Menu");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new PreGameMenus(syncId, player.getInventory());
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        return ItemStack.EMPTY; // 禁用 Shift+点击 快速移动
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex >= 0 && slotIndex < inventory.size()) {
            ItemStack clickedItem = inventory.getStack(slotIndex);
            if (!clickedItem.isEmpty()) {
                // **检测玩家点击了哪个槽位**
                if (clickedItem.getItem() == Items.EMERALD) {
                    player.getServer().getPlayerManager().broadcast(Text.of("游戏开始！").copy().formatted(Formatting.BOLD, Formatting.RED), false);
                    RunTimeControl.start((ServerPlayerEntity) player);
                } else if (clickedItem.getItem() == Items.GREEN_WOOL) {
                    TeamHandler.addPlayerToTeam((ServerPlayerEntity) player, "runner");
                    player.getServer().getPlayerManager().broadcast(player.getDisplayName().copy()
                            .append(Text.of(" 加入了逃生者！").copy().formatted(Formatting.BOLD, Formatting.GREEN)), false);
                } else if (clickedItem.getItem() == Items.RED_WOOL) {
                    TeamHandler.addPlayerToTeam((ServerPlayerEntity) player, "hunter");
                    player.getServer().getPlayerManager().broadcast(player.getDisplayName().copy()
                            .append(Text.of(" 加入了追杀者！").copy().formatted(Formatting.BOLD, Formatting.RED)), false);
                } else  if (clickedItem.getItem() == Items.GRAY_WOOL) {
                    TeamHandler.addPlayerToTeam((ServerPlayerEntity) player, "spectator");
                    player.getServer().getPlayerManager().broadcast(player.getDisplayName().copy()
                            .append(Text.of(" 加入了旁观者！").copy().formatted(Formatting.BOLD, Formatting.GRAY)), false);
                }
            }
        }

        super.onSlotClick(slotIndex, button, actionType, player);
    }
}