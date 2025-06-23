//package com.manhunt.mixin;
//
//import com.manhunt.menus.PreGameMenus;
//import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
//import net.minecraft.server.network.ServerPlayNetworkHandler;
//import net.minecraft.server.network.ServerPlayerEntity;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import static net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND;
//
//@Mixin(ServerPlayNetworkHandler.class)
//public class OpenMenuMixin {
//    @Inject(method = "onPlayerAction", at = @At("HEAD"), cancellable = true)
//    private void onSwapHandItems(PlayerActionC2SPacket packet, CallbackInfo ci) {
//        ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler) (Object) this;
//        ServerPlayerEntity player = handler.player;
//        PlayerActionC2SPacket.Action action = packet.getAction();
//
//        if (player.isSneaking() && action.equals(SWAP_ITEM_WITH_OFFHAND)) { // 检测玩家是否潜行
//            openCustomInventory(player);
//            ci.cancel(); // 取消原始换手动作，防止手部交换
//        }
//    }
//
//    @Unique
//    private void openCustomInventory(ServerPlayerEntity player) {
////        player.sendMessage(Text.of("打开自定义物品栏！"), false);
//        // 创建屏幕处理器
//        PreGameMenus screenHandler = new PreGameMenus(0, player.getInventory());
//        // 打开虚拟箱子
//        player.openHandledScreen(screenHandler);
//    }
//}