package dev.vaultwatcher;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Сейф сам закидывает игроку предмет в инвентарь (без окна), поэтому единственный
 * надёжный клиентский способ узнать "что выпало" — сравнивать инвентарь между тиками.
 * Предметы НЕ из списка вообще не трогаются и никак не отмечаются — как ты и просил.
 */
public final class InventoryWatcher {

    private InventoryWatcher() {}

    private static Map<String, Integer> lastCounts = new HashMap<>();
    private static boolean initialized = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(InventoryWatcher::tick);
    }

    private static void tick(Minecraft client) {
        VaultWatcherConfig cfg = VaultWatcherClient.CONFIG;
        Player player = client.player;
        if (cfg == null || !cfg.enabled || player == null || client.level == null) {
            return;
        }

        RegistryAccess registryAccess = client.level.registryAccess();
        Map<String, Integer> current = new HashMap<>();

        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            Optional<String> match = ItemScanner.match(stack, cfg, registryAccess);
            match.ifPresent(label -> current.merge(label, stack.getCount(), Integer::sum));
        }

        if (!initialized) {
            // при первом запуске просто запоминаем текущее состояние,
            // чтобы не сработать на предметы, которые уже лежали в инвентаре
            lastCounts = current;
            initialized = true;
            return;
        }

        for (Map.Entry<String, Integer> entry : current.entrySet()) {
            int before = lastCounts.getOrDefault(entry.getKey(), 0);
            if (entry.getValue() > before) {
                OverlayNotifier.notify("В хранилище: " + entry.getKey());
            }
        }

        lastCounts = current;
    }
}
