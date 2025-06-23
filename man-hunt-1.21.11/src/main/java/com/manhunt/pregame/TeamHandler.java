package com.manhunt.pregame;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

import static com.manhunt.ManHunt.LOGGER;

public class TeamHandler {

    private static Scoreboard scoreboard;

    public static void init(Scoreboard scoreboard) {
        TeamHandler.scoreboard = scoreboard;
        initTeams();
    }

    private static void initTeams() {
        createTeam("runner", Text.literal("逃生者"), Formatting.GREEN);
        createTeam("hunter", Text.literal("追杀者"), Formatting.RED);
        createTeam("spectator", Text.literal("旁观者"), Formatting.GRAY);
        LOGGER.info("Registering teams");
    }

    private static void createTeam(String id, Text displayName, Formatting color) {
        // 如果队伍已存在则跳过
        if (scoreboard.getTeam(id) != null) return;

        Team team = scoreboard.addTeam(id);
        team.setDisplayName(displayName);
        team.setColor(color);
//        team.setFriendlyFireAllowed(false); // 禁止同队伤害
//        team.setShowFriendlyInvisibles(true); // 显示隐身队友
    }

    public static List<ServerPlayerEntity> getPlayersInTeam(MinecraftServer server, String teamName) {
        List<ServerPlayerEntity> players = new ArrayList<>();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            Team team = player.getScoreboardTeam();
            if (team != null && team.getName().equals(teamName)) {
                players.add(player);
            }
        }
        return players;
    }

    public static List<ServerPlayerEntity> getOpponents(MinecraftServer server, ServerPlayerEntity player) {
        Team team = player.getScoreboardTeam();
        if (team == null) return new ArrayList<>();

        String name = team.getName();
        if (name.equals("hunter")) {
            return getPlayersInTeam(server, "runner");
        } else if (name.equals("runner")) {
            return getPlayersInTeam(server, "hunter");
        } else {
            return new ArrayList<>();
        }
    }

    public static Team getTeam(String id) {
        return scoreboard.getTeam(id);
    }

    public static void addPlayerToTeam(ServerPlayerEntity player, String teamId) {
        if (scoreboard == null) {
            throw new IllegalStateException("TeamHandler 未初始化！请先调用 TeamHandler.init(scoreboard)");
        }

        Team team = scoreboard.getTeam(teamId);
        if (team == null) {
            player.sendMessage(Text.literal("队伍 " + teamId + " 不存在！").formatted(Formatting.RED));
            return;
        }

        String playerName = player.getName().getString();

        // 移除玩家原来的队伍
        for (Team otherTeam : scoreboard.getTeams()) {
            otherTeam.getPlayerList().remove(playerName);
        }

        // 添加到目标队伍
//        team.getPlayerList().add(playerName);
		player.getScoreboard().addScoreHolderToTeam(playerName, team);
//        player.sendMessage(Text.literal("你已加入队伍：" + team.getDisplayName().getString()).formatted(team.getColor()));
    }
}
