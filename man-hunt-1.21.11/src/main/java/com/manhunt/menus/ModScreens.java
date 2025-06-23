package com.manhunt.menus;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreens {
//    public static final ScreenHandlerType<PreGameMenus> PRE_GAME_MENUS_SCREEN_HANDLER_TYPE =
//            Registry.register(
//                    Registries.SCREEN_HANDLER,
//                    Identifier.of("manhunt", "pregame_menus"),
//                    new ScreenHandlerType<>(PreGameMenus::new, FeatureFlags.VANILLA_FEATURES)
//            );

    public static void register() {
        // 这个方法会在 mod 初始化时调用，确保 ScreenHandler 被注册
    }
}
