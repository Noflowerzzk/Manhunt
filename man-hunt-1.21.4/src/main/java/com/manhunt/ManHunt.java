package com.manhunt;

import com.manhunt.menus.ModScreens;
import com.manhunt.pregame.PreGameInit;
import com.manhunt.pregame.TeamHandler;
import com.manhunt.runtime.RunTimeControl;
import com.manhunt.utils.Commands;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.core.jmx.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static com.manhunt.runtime.RunTimeControl.running;

public class ManHunt implements ModInitializer {
	public static final String MOD_ID = "man-hunt";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

//		ModScreens.register();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			for (ServerWorld world : server.getWorlds()) {
				world.getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(false, server);
			}
		});

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			Scoreboard scoreboard = server.getScoreboard();
			// 移除所有玩家的队伍归属
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				for (Team team : scoreboard.getTeams()) {
					team.getPlayerList().remove(player.getName().getString());
				}
			}
			// 删除所有现有队伍
			for (Team team : List.copyOf(scoreboard.getTeams())) {
				scoreboard.removeTeam(team);
			}
			PreGameInit.init(scoreboard);
			RunTimeControl.init();
		});

//		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
//			ServerPlayerEntity player = handler.getPlayer();
//			PreGameInit.initPlayers(player);
//		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, livingEntity) -> {
			MinecraftServer server = entity.getServer();
			assert server != null;

			// 逃生者死完了
			if (entity instanceof ServerPlayerEntity player) {
				if (Objects.requireNonNull(player.getScoreboardTeam()).getName().equals("runner")) {
					RunTimeControl.runnerDeath(player);
					if (RunTimeControl.isAllRunnerDead()) {
						RunTimeControl.stop(false, server);
					}
				}
			}

			// 末影龙死了
			if (entity.getType() ==  EntityType.ENDER_DRAGON) {
				RunTimeControl.stop(true, server);
			}
		});

		// 注册指令
		Commands.register();
		LOGGER.info("Commands registered");

		ServerPlayConnectionEvents.JOIN.register((handler, sender, _server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			sendRoleSelectionButtons(player);
			LOGGER.info("Send messages to " + player.getName().getString());
		});
	}

	private void sendRoleSelectionButtons(ServerPlayerEntity player) {
		player.sendMessage(
				Text.literal("§6欢迎来到 ")
						.append(
								Text.literal("§bNoflowerzzk")
										.styled(style -> style
												.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Noflowerzzk"))
												.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("§7点击打开 GitHub 页面")))
										)
						)
						.append(Text.literal("§6 的高版本猎人游戏！"))
		);

		if (running) {
			player.sendMessage(Text.literal("§a游戏正在进行，选择角色后将自动加入！"));
		}

		player.sendMessage(Text.literal("§6扔出指南针以切换追踪目标"));
		// 加入逃生者
		player.sendMessage(Text.literal("§a[加入逃生者]").styled(style -> style
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/manhunt join runner"))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("点击加入逃生者")))
				.withBold(true)
		));

		// 加入追杀者
		player.sendMessage(Text.literal("§c[加入追杀者]").styled(style -> style
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/manhunt join hunter"))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("点击加入追杀者")))
				.withBold(true)
		));

		// 加入旁观者
		player.sendMessage(Text.literal("§7[加入旁观者]").styled(style -> style
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/manhunt join spectator"))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("点击加入旁观者")))
				.withBold(true)
		));

		// 开始游戏
		player.sendMessage(Text.literal("§e[开始游戏]").styled(style -> style
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/manhunt start"))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("点击开始游戏")))
				.withBold(true)
		));
	}
}