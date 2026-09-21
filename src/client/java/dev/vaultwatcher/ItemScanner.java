package dev.vaultwatcher;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Optional;

/**
 * Определяет, "интересен" ли предмет в открытом контейнере, и как его подписать.
 *
 * ВНИМАНИЕ: код написан по маппингам Mojang (официальные названия классов,
 * которые Fabric использует по умолчанию для 1.21.8). Если у тебя используется
 * Yarn — часть имён классов/методов будет отличаться (см. README).
 */
public final class ItemScanner {

    private ItemScanner() {}

    private static final ResourceLocation HEAVY_CORE = ResourceLocation.withDefaultNamespace("heavy_core");
    private static final ResourceLocation OMINOUS_BOTTLE = ResourceLocation.withDefaultNamespace("ominous_bottle");
    private static final ResourceLocation WIND_CHARGE = ResourceLocation.withDefaultNamespace("wind_charge");

    private static final ResourceKey<Enchantment> DENSITY =
            ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.withDefaultNamespace("density"));
    private static final ResourceKey<Enchantment> WIND_BURST =
            ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.withDefaultNamespace("wind_burst"));

    /**
     * @return человекочитаемое название найденного "интересного" предмета, если он совпал
     *         с включёнными настройками, иначе Optional.empty().
     */
    public static Optional<String> match(ItemStack stack, VaultWatcherConfig cfg, RegistryAccess registryAccess) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemIdStr = itemId.toString();

        // --- встроенные предметы ---
        if (cfg.heavyCore && HEAVY_CORE.equals(itemId)) {
            return Optional.of("Навершие булавы");
        }
        if (cfg.ominousBottle && OMINOUS_BOTTLE.equals(itemId)) {
            return Optional.of("Зловещая бутылочка");
        }
        if (cfg.windCharge && WIND_CHARGE.equals(itemId)) {
            return Optional.of("Заряд ветра");
        }

        // --- зачарованная книга с нужными чарами ---
        if (stack.is(Items.ENCHANTED_BOOK) && registryAccess != null) {
            String enchantMatch = matchEnchantedBook(stack, cfg, registryAccess);
            if (enchantMatch != null) {
                return Optional.of(enchantMatch);
            }
        }

        // --- кастомные предметы по id ---
        for (String customId : cfg.customItems) {
            if (customId.equalsIgnoreCase(itemIdStr)) {
                return Optional.of(stack.getHoverName().getString());
            }
        }

        return Optional.empty();
    }

    private static String matchEnchantedBook(ItemStack stack, VaultWatcherConfig cfg, RegistryAccess registryAccess) {
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) {
            return null;
        }

        Registry<Enchantment> enchantmentRegistry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);

        if (cfg.density4 || cfg.density5) {
            Optional<Holder.Reference<Enchantment>> density = enchantmentRegistry.getHolder(DENSITY);
            if (density.isPresent()) {
                int lvl = enchantments.getLevel(density.get());
                if (lvl == 4 && cfg.density4) return "Книга: Плотность IV";
                if (lvl == 5 && cfg.density5) return "Книга: Плотность V";
            }
        }

        if (cfg.windBurst2 || cfg.windBurst3) {
            Optional<Holder.Reference<Enchantment>> windBurst = enchantmentRegistry.getHolder(WIND_BURST);
            if (windBurst.isPresent()) {
                int lvl = enchantments.getLevel(windBurst.get());
                if (lvl == 2 && cfg.windBurst2) return "Книга: Порыв ветра II";
                if (lvl == 3 && cfg.windBurst3) return "Книга: Порыв ветра III";
            }
        }

        return null;
    }
}
