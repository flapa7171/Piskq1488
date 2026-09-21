package dev.vaultwatcher;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VaultWatcherClient implements ClientModInitializer {

    public static final String MOD_ID = "vaultwatcher";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static VaultWatcherConfig CONFIG;

    @Override
    public void onInitializeClient() {
        VaultWatcherConfig.init(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID));
        CONFIG = VaultWatcherConfig.load();

        // команда /autovault — открывает меню настроек
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("autovault")
                        .executes(context -> {
                            net.minecraft.client.Minecraft.getInstance()
                                    .setScreen(new VaultWatcherScreen(null));
                            return 1;
                        }))
        );

        OverlayNotifier.register();
        VaultAutoOpener.register();
        InventoryWatcher.register();

        LOGGER.info("[VaultWatcher] инициализирован. Команда: /autovault");
    }
}
