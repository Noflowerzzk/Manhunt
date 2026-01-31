package com.manhunt.runtime;

import com.manhunt.pregame.TeamHandler;
import com.manhunt.utils.CompassHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
//import org.slf4j.LoggerFactory;

import java.util.*;

import static com.manhunt.ManHunt.LOGGER;

public class RunTimeControl {
	public static boolean running = false;

	private static Map<PlayerEntity, Boolean> isRunnerLiving = new HashMap<>();

	public static int prepareTime = 0;

	public static void init() {
		running = false;
	}

	public static boolean isRunning() { return running; }

	public static void runnerDeath(PlayerEntity player) {
		isRunnerLiving.remove(player);
	}

	public static boolean isAllRunnerDead() { return isRunnerLiving.isEmpty(); }

	public static void start(ServerPlayerEntity player) {
		if (running) {
			player.sendMessage(Text.literal("§c游戏正在进行中！"));
			return;
		}

		boolean exist_runner = false, exist_hunter = false;
		for (ServerWorld serverWorld : player.getServer().getWorlds()) {
			for (ServerPlayerEntity other : serverWorld.getPlayers()) {
				if (other.getScoreboardTeam() != null) {
					if (other.getScoreboardTeam().getName() == "runner") {
						exist_runner = true;
					}
					if (other.getScoreboardTeam().getName() == "hunter") {
						exist_hunter = true;
					}
				}
			}
		}

		if (!exist_runner) {
			player.sendMessage(Text.literal("§c缺少逃生者！"));
			return;
		}
		if (!exist_hunter) {
			player.sendMessage(Text.literal("§c缺少追杀者！"));
			return;
		}

		running = true;

		// 获取发起者的位置、方向、世界
		ServerWorld world = player.getServerWorld();
//		Vec3d pos = Vec3d.of(player.getBlockPos().add(0, 300, 0)); // 稍微居中

		int x = player.getBlockPos().getX();
		int y = 320;
		int z = player.getBlockPos().getZ();

		// 找到最高方块
		for (; y >= -63; -- y) {
			BlockPos blockpos = new BlockPos(x, y, z);
//			LOGGER.info(String.valueOf(world.getBlockState(blockpos).getBlock()));
//			LOGGER.info(String.valueOf(Blocks.AIR));
			if (!world.getBlockState(blockpos).getBlock().equals(Blocks.AIR) &&  !world.getBlockState(blockpos).getBlock().equals(Blocks.VOID_AIR)) {
				break;
			}
		}

		LOGGER.info(String.valueOf(y));
		Vec3d pos = new Vec3d(x + 0.5, y + 2, z + 0.5);
		float yaw = player.getYaw();
		float pitch = player.getPitch();

//		ServerCommandSource source = player.getServer().getCommandSource();
//
//		player.getServer().getCommandManager().executeWithPrefix(
//				source,
//				"tp @a " + x + " " + (y + 2) + " " + z
//		);

		// 构造目标传送数据
		TeleportTarget target = new TeleportTarget(
				world,
				pos,
				Vec3d.ZERO,
				yaw,
				pitch,
				EnumSet.noneOf(PositionFlag.class),
				TeleportTarget.NO_OP
		);

		// 传送所有玩家到发起者位置
		for (ServerWorld serverWorld : player.getServer().getWorlds()) {
			for (ServerPlayerEntity other : serverWorld.getPlayers()) {
				other.removeStatusEffect(StatusEffects.GLOWING);
				other.teleportTo(target);
				LOGGER.info("Teleporting " + player.getName() + " to " + target);
			}
		}

		// 修改玩家游戏模式
		for (ServerWorld serverWorld : player.getServer().getWorlds()) {
			for (ServerPlayerEntity targetPlayer : serverWorld.getPlayers()) {
				targetPlayer.setGlowing(false);
				if (targetPlayer.getScoreboardTeam() == null || Objects.equals(targetPlayer.getScoreboardTeam().getName(), "spectator")) {
					TeamHandler.addPlayerToTeam(targetPlayer, "spectator");
					targetPlayer.changeGameMode(GameMode.SPECTATOR);
				} else {
					targetPlayer.changeGameMode(GameMode.SURVIVAL);
				}
			}
		}

		for (ServerWorld serverWorld : player.getServer().getWorlds()) {
			for (ServerPlayerEntity targetPlayer : serverWorld.getPlayers()) {
//				LoggerFactory.getLogger("man-hunt").info(targetPlayer.getDisplayName().getString());
				if (Objects.requireNonNull(targetPlayer.getScoreboardTeam()).getName().equals("runner")) {
					isRunnerLiving.put(targetPlayer, true);
				}
			}
		}


		player.getServer().getCommandManager().executeWithPrefix(
				player.getCommandSource(),
				"title @a title {\"text\":\"游戏开始！\",\"color\":\"green\"}"
		);
		player.getServer().getCommandManager().executeWithPrefix(
				player.getCommandSource(),
				"title @a subtitle {\"text\":\"追杀者者将在 " + prepareTime + " 秒后开始追杀！\",\"color\":\"red\"}"
		);


//		give @a 指南针
		LOGGER.info("Start init campasses");
		CompassHandler.init();
		LOGGER.info("Inited campasses");

		if (prepareTime != 0) {
			for (ServerWorld serverWorld : player.getServer().getWorlds()) {
				for (ServerPlayerEntity targetPlayer : serverWorld.getPlayers()) {
					if (Objects.requireNonNull(targetPlayer.getScoreboardTeam()).getName().equals("hunter")) {
						player.addStatusEffect(new StatusEffectInstance(
								StatusEffects.BLINDNESS, prepareTime * 20, 0, false, false, false
						));
						player.addStatusEffect(new StatusEffectInstance(
								StatusEffects.SLOWNESS, prepareTime * 20, 255, false, false, false
						));
						player.addStatusEffect(new StatusEffectInstance(
								StatusEffects.WEAKNESS, prepareTime * 20, 15, false, false, false
						));
					}
				}
			}

		}
	}

	// 显示获胜信息
	public static void stop(boolean isRunnerWin, MinecraftServer server) {
		running = false;
		ServerCommandSource source = server.getCommandSource();
		if (isRunnerWin) {
			server.getCommandManager().executeWithPrefix(
					source,
					"title @a title {\"text\":\"逃生者胜利！\",\"color\":\"green\"}"
			);
		} else {
			server.getCommandManager().executeWithPrefix(
					source,
					"title @a title {\"text\":\"追杀者胜利！\",\"color\":\"red\"}"
			);
		}
	}
}
