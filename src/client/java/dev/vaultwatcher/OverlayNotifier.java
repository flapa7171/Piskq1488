package dev.vaultwatcher;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Показывает надпись поверх экрана (похоже на /title, но чисто на клиенте,
 * без прав оператора и без отправки чего-либо на сервер).
 */
public final class OverlayNotifier {

    private OverlayNotifier() {}

    private static volatile String message = null;
    private static volatile long expireAtMillis = 0L;
    private static final long DISPLAY_MILLIS = 3000L;

    public static void notify(String text) {
        message = text;
        expireAtMillis = System.currentTimeMillis() + DISPLAY_MILLIS;
    }

    public static void register() {
        HudRenderCallback.EVENT.register(OverlayNotifier::render);
    }

    private static void render(GuiGraphics context, DeltaTracker tickCounter) {
        String currentMessage = message;
        if (currentMessage == null || System.currentTimeMillis() > expireAtMillis) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        int screenWidth = context.guiWidth();
        int y = 24; // немного отступив от верхнего края, "над инвентарём"

        Component text = Component.literal(currentMessage);
        int textWidth = client.font.width(text);
        int x = (screenWidth - textWidth) / 2;

        // полупрозрачная плашка под текстом для читаемости
        context.fill(x - 4, y - 3, x + textWidth + 4, y + 11, 0x90000000);
        context.drawString(client.font, text, x, y, 0xFFFFFF, true);
    }
}
