package dev.vaultwatcher;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class VaultWatcherScreen extends Screen {

    private final Screen parent;
    private final VaultWatcherConfig cfg;
    private EditBox customItemBox;

    // куда будем класть кнопки-"чипы" кастомных предметов, чтобы их можно было
    // легко удалить и пересоздать при перестроении экрана
    private final List<Button> customChips = new ArrayList<>();

    public VaultWatcherScreen(Screen parent) {
        super(Component.literal("Vault Watcher"));
        this.parent = parent;
        this.cfg = VaultWatcherClient.CONFIG;
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        this.clearWidgets();
        customChips.clear();

        int centerX = this.width / 2;
        int y = 28;
        int rowHeight = 22;
        int fullWidth = 240;
        int halfWidth = 116;

        // --- главный переключатель ---
        addRenderableWidget(toggleButton(
                centerX - fullWidth / 2, y, fullWidth,
                "Мод", () -> cfg.enabled, v -> cfg.enabled = v
        ));
        y += rowHeight;

        addRenderableWidget(toggleButton(
                centerX - fullWidth / 2, y, fullWidth,
                "Авто-открытие сейфов", () -> cfg.openVaults, v -> cfg.openVaults = v
        ));
        y += rowHeight + 4;

        // --- встроенные предметы ---
        addRenderableWidget(toggleButton(
                centerX - fullWidth / 2, y, fullWidth,
                "Навершие булавы", () -> cfg.heavyCore, v -> cfg.heavyCore = v
        ));
        y += rowHeight;

        addRenderableWidget(toggleButton(
                centerX - fullWidth / 2, y, fullWidth,
                "Зловещая бутылочка", () -> cfg.ominousBottle, v -> cfg.ominousBottle = v
        ));
        y += rowHeight;

        addRenderableWidget(toggleButton(
                centerX - fullWidth / 2, y, fullWidth,
                "Заряд ветра", () -> cfg.windCharge, v -> cfg.windCharge = v
        ));
        y += rowHeight + 6;

        // --- книги с зачарованиями (по два переключателя в ряд) ---
        addRenderableWidget(toggleButton(
                centerX - fullWidth / 2, y, halfWidth,
                "Плотность IV", () -> cfg.density4, v -> cfg.density4 = v
        ));
        addRenderableWidget(toggleButton(
                centerX - fullWidth / 2 + halfWidth + 8, y, halfWidth,
                "Плотность V", () -> cfg.density5, v -> cfg.density5 = v
        ));
        y += rowHeight;

        addRenderableWidget(toggleButton(
                centerX - fullWidth / 2, y, halfWidth,
                "Порыв ветра II", () -> cfg.windBurst2, v -> cfg.windBurst2 = v
        ));
        addRenderableWidget(toggleButton(
                centerX - fullWidth / 2 + halfWidth + 8, y, halfWidth,
                "Порыв ветра III", () -> cfg.windBurst3, v -> cfg.windBurst3 = v
        ));
        y += rowHeight + 10;

        // --- поле для кастомных предметов ---
        customItemBox = new EditBox(this.font, centerX - fullWidth / 2, y, fullWidth - 70, 20,
                Component.literal("minecraft:название"));
        customItemBox.setMaxLength(128);
        customItemBox.setHint(Component.literal("minecraft:trident"));
        addRenderableWidget(customItemBox);

        addRenderableWidget(Button.builder(Component.literal("Добавить"), b -> addCustomItem())
                .bounds(centerX - fullWidth / 2 + fullWidth - 66, y, 66, 20)
                .build());
        y += rowHeight + 6;

        // --- список уже добавленных кастомных предметов (клик = удалить) ---
        int chipX = centerX - fullWidth / 2;
        int chipY = y;
        int chipWidth = 116;
        int chipGap = 8;
        int col = 0;

        for (String id : cfg.customItems) {
            int bx = chipX + col * (chipWidth + chipGap);
            Button chip = Button.builder(Component.literal(shorten(id) + "  ✕"), b -> {
                        cfg.removeCustomItem(id);
                        rebuild();
                    })
                    .bounds(bx, chipY, chipWidth, 18)
                    .build();
            addRenderableWidget(chip);
            customChips.add(chip);

            col++;
            if (col >= 2) {
                col = 0;
                chipY += 20;
            }
        }

        // --- кнопка закрытия ---
        addRenderableWidget(Button.builder(Component.literal("Готово"), b -> onClose())
                .bounds(centerX - 60, this.height - 28, 120, 20)
                .build());
    }

    private void addCustomItem() {
        if (customItemBox == null) return;
        String value = customItemBox.getValue();
        if (value != null && !value.isBlank()) {
            cfg.addCustomItem(value);
            customItemBox.setValue("");
            rebuild();
        }
    }

    private static String shorten(String id) {
        // убираем "minecraft:" для компактности, если это стандартный неймспейс
        return id.startsWith("minecraft:") ? id.substring("minecraft:".length()) : id;
    }

    private interface BoolGetter { boolean get(); }
    private interface BoolSetter { void set(boolean value); }

    private Button toggleButton(int x, int y, int width, String label, BoolGetter getter, BoolSetter setter) {
        return Button.builder(buttonText(label, getter.get()), b -> {
                    setter.set(!getter.get());
                    cfg.save();
                    b.setMessage(buttonText(label, getter.get()));
                })
                .bounds(x, y, width, 20)
                .build();
    }

    private static Component buttonText(String label, boolean value) {
        return Component.literal(label + ": " + (value ? "ВКЛ" : "ВЫКЛ"));
    }

    @Override
    public void onClose() {
        cfg.save();
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
