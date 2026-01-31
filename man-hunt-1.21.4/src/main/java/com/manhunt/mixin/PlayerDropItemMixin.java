package com.manhunt.mixin;

import com.manhunt.pregame.TeamHandler;
import com.manhunt.utils.CompassHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static com.manhunt.ManHunt.LOGGER;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerDropItemMixin {

	@Redirect(
			method = "dropSelectedItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/network/ServerPlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;"
			)
	)
	private ItemEntity onDropItem(ServerPlayerEntity player, ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
		// 判断是否是指南针，并禁止掉落
		Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
		if (stack.getItem() == Items.COMPASS && name != null) {
			String nameStr = name.getString();
			if (nameStr.equals("§7§l逃生指南针") || nameStr.equals("§7§l追杀指南针")) {
				CompassHandler.rotateTarget(player);

				List<ServerPlayerEntity> targets = new ArrayList<>();
				String team = player.getScoreboardTeam() != null ? player.getScoreboardTeam().getName() : "";
				if (Objects.equals(team, "runner")) {
					targets = TeamHandler.getPlayersInTeam(player.getServer(), "hunter");
				} else if (Objects.equals(team, "hunter")) {
					targets = TeamHandler.getPlayersInTeam(player.getServer(), "runner");
				}

				CompassHandler.updateCompass(player, targets);

				// 不掉落任何实体（返回 null 会让整个 dropSelectedItem 返回 false）
				return null;
			}
		}

		// 默认正常掉落
		return player.dropItem(stack, throwRandomly, retainOwnership);
	}
}
