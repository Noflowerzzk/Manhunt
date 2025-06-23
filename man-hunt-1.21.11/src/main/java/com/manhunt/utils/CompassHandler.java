package com.manhunt.utils;

import com.manhunt.pregame.TeamHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.util.*;

import static com.manhunt.ManHunt.LOGGER;

public class CompassHandler {
	private static final Map<UUID, Integer> compassTargetIndex = new HashMap<>();

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(CompassHandler::onServerTick);
		LOGGER.info("Compass Handler Registered!");
	}

	private static void onServerTick(MinecraftServer server) {
		if (server.getTicks() % 20 != 0) return;	// 每一秒更新一次

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (player.getScoreboardTeam() == null) continue;
			if (Objects.equals(player.getScoreboardTeam().getName(), "runner")) {
				updateCompass(player, TeamHandler.getPlayersInTeam(server, "hunter"));
			} else if (Objects.equals(player.getScoreboardTeam().getName(), "hunter")) {
				updateCompass(player, TeamHandler.getPlayersInTeam(server, "runner"));
			}
		}
	}

	public static void updateCompass(ServerPlayerEntity player, List<ServerPlayerEntity> targets) {
//		LOGGER.info("Compass Handler Updating Called!");
//		LOGGER.info(targets.toString());
		if (targets.isEmpty()) return;

		int index = compassTargetIndex.getOrDefault(player.getUuid(), 0) % targets.size();
		ServerPlayerEntity target = targets.get(index);



		// 显示action bar
		if (player.getMainHandStack().getItem() == Items.COMPASS) {
			String dimIdStr = target.getWorld().getRegistryKey().getValue().toString();
			String dimName;

			if (dimIdStr.equals("minecraft:overworld")) {
				dimName = "主世界";
			} else if (dimIdStr.equals("minecraft:the_nether")) {
				dimName = "下界";
			} else if (dimIdStr.equals("minecraft:the_end")) {
				dimName = "末地";
			} else {
				dimName = dimIdStr; // 其他维度保持原样
			}

			String message = "追踪玩家：" + target.getDisplayName().getString() +
					" | 所在维度：" + dimName;

			player.sendMessage(Text.of(message), true);
		}

		// 判断维度是否一致
		if (!target.getWorld().getRegistryKey().equals(player.getWorld().getRegistryKey())) {

			ItemStack compass = new ItemStack(Items.COMPASS);
			compass.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§7§l" +
					(Objects.equals(Objects.requireNonNull(player.getScoreboardTeam()).getName(), "runner") ? "逃生" : "追杀") + "指南针"));

			for (int i = 0; i < player.getInventory().size(); i++) {
				ItemStack stack = player.getInventory().getStack(i);
				if (stack.getItem() == Items.COMPASS) {
					player.getInventory().setStack(i, compass);
//					LOGGER.info("Compass updated!");
					return;
				}
			}

			player.getInventory().insertStack(compass);
			return;
		}

		BlockPos targetPos = target.getBlockPos();
		RegistryKey<World> targetDimension = target.getWorld().getRegistryKey();

		// 使用 GlobalPos 表示维度+位置
		GlobalPos globalPos = GlobalPos.create(targetDimension, targetPos);
		Optional<GlobalPos> optionalGlobalPos = Optional.of(globalPos);

		// 创建指南针并设置 LodestoneTrackerComponent（禁用追踪，使用静态位置）
		ItemStack compass = new ItemStack(Items.COMPASS);
		compass.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(optionalGlobalPos, false));
		compass.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§7§l" +
				(Objects.equals(Objects.requireNonNull(player.getScoreboardTeam()).getName(), "runner") ? "逃生" : "追杀") + "指南针"));

		// 替换已有指南针
		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack stack = player.getInventory().getStack(i);
			if (stack.getItem() == Items.COMPASS) {
				player.getInventory().setStack(i, compass);
//				LOGGER.info("Compass updated!");
				return;
			}
		}

		// 没找到就添加
		player.getInventory().insertStack(compass);
		LOGGER.info("Compass given!");
	}

	public static void rotateTarget(ServerPlayerEntity player) {
		List<ServerPlayerEntity> targets = TeamHandler.getOpponents(player.getServer(), player);
		if (!targets.isEmpty()) {
			if (!(targets.size() == 1)) {
				int current = compassTargetIndex.getOrDefault(player.getUuid(), 0);
				compassTargetIndex.put(player.getUuid(), (current + 1) % targets.size());
			}
		}
		LOGGER.info("Target switched!");
	}

	public static void reset(ServerPlayerEntity player) {
		compassTargetIndex.remove(player.getUuid());
	}
}
