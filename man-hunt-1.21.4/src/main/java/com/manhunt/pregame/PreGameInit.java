package com.manhunt.pregame;

import com.manhunt.runtime.RunTimeControl;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public class PreGameInit {
	// 游戏初始化总调用
	public static void init(Scoreboard scoreboard) {
		TeamHandler.init(scoreboard);
	}

	// 玩家的初始化 玩家加入时调用
	public static void initPlayers(ServerPlayerEntity player) {
		player.setGlowing(true); // 发光
		player.changeGameMode(GameMode.ADVENTURE); // 冒险模式

		if (RunTimeControl.isRunning()) {
			player.sendMessage(Text.of("游戏已开始，使用 shift + f 选队后自动加入游戏！"));
		} else {
			player.sendMessage(Text.of("使用 shift + f 打开选队面板！"));
		}
	}
}
