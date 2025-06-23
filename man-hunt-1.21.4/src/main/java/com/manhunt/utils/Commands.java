package com.manhunt.utils;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import com.manhunt.pregame.TeamHandler;
import com.manhunt.runtime.RunTimeControl;

public class Commands {
	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(
					LiteralArgumentBuilder.<ServerCommandSource>literal("manhunt")
							.then(CommandManager.literal("join")
									.then(CommandManager.literal("runner").executes(ctx -> {
										ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
										TeamHandler.addPlayerToTeam(player, "runner");
										player.getServer().getPlayerManager().broadcast(
												player.getDisplayName().copy()
														.append(Text.literal(" 加入了逃生者！").formatted(Formatting.BOLD, Formatting.GREEN)),
												false
										);
										return 1;
									}))
									.then(CommandManager.literal("hunter").executes(ctx -> {
										ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
										TeamHandler.addPlayerToTeam(player, "hunter");
										player.getServer().getPlayerManager().broadcast(
												player.getDisplayName().copy()
														.append(Text.literal(" 加入了追杀者！").formatted(Formatting.BOLD, Formatting.RED)),
												false
										);
										return 1;
									}))
									.then(CommandManager.literal("spectator").executes(ctx -> {
										ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
										TeamHandler.addPlayerToTeam(player, "spectator");
										player.getServer().getPlayerManager().broadcast(
												player.getDisplayName().copy()
														.append(Text.literal(" 加入了旁观者！").formatted(Formatting.BOLD, Formatting.GRAY)),
												false
										);
										return 1;
									}))
							)
							.then(CommandManager.literal("start").executes(ctx -> {
								ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
								player.getServer().getPlayerManager().broadcast(
										Text.literal("游戏开始！").formatted(Formatting.BOLD, Formatting.RED),
										false
								);
								RunTimeControl.start(player);
								return 1;
							}))
			);
		});
	}
}
