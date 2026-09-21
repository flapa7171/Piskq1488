package dev.vaultwatcher;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class VaultAutoOpener {

    private VaultAutoOpener() {}

    private static final ResourceLocation VAULT_ID = ResourceLocation.withDefaultNamespace("vault");
    private static final ResourceLocation OMINOUS_VAULT_ID = ResourceLocation.withDefaultNamespace("ominous_vault");

    private static final ResourceLocation TRIAL_KEY = ResourceLocation.withDefaultNamespace("trial_key");
    private static final ResourceLocation OMINOUS_TRIAL_KEY = ResourceLocation.withDefaultNamespace("ominous_trial_key");

    private static final int RETRY_TICKS = 4;
    private static BlockPos lastPos = null;
    private static int cooldown = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(VaultAutoOpener::tick);
    }

    private static void tick(Minecraft client) {
        VaultWatcherConfig cfg = VaultWatcherClient.CONFIG;
        if (cfg == null || !cfg.enabled || !cfg.openVaults) {
            return;
        }

        LocalPlayer player = client.player;
        if (player == null || client.level == null || client.gameMode == null) {
            return;
        }

        HitResult hit = client.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = client.level.getBlockState(pos);
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        boolean isVault = VAULT_ID.equals(blockId);
        boolean isOminousVault = OMINOUS_VAULT_ID.equals(blockId);
        if (!isVault && !isOminousVault) {
            lastPos = null;
            return;
        }

        ItemStack held = player.getMainHandItem();
        ResourceLocation heldId = BuiltInRegistries.ITEM.getKey(held.getItem());
        boolean hasCorrectKey = (isVault && TRIAL_KEY.equals(heldId))
                || (isOminousVault && OMINOUS_TRIAL_KEY.equals(heldId));
        if (!hasCorrectKey) {
            return;
        }

        if (pos.equals(lastPos)) {
            if (cooldown > 0) {
                cooldown--;
                return;
            }
        } else {
            lastPos = pos;
            cooldown = 0;
        }

        InteractionResult result = client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, blockHit);
        if (result.consumesAction()) {
            player.swing(InteractionHand.MAIN_HAND);
        }
        cooldown = RETRY_TICKS;
    }
}
