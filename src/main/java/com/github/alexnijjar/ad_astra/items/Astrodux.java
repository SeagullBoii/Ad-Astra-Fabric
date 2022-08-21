package com.github.alexnijjar.ad_astra.items;

import com.github.alexnijjar.ad_astra.util.ModIdentifier;
import com.github.alexnijjar.ad_astra.util.ModUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import vazkii.patchouli.api.PatchouliAPI;

public class Astrodux extends Item {

    public Astrodux(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player) {
            if (ModUtils.modLoaded("patchouli")) {
                PatchouliAPI.get().openBookGUI(player, new ModIdentifier("astrodux"));
            } else {
                user.sendMessage(Text.translatable("info.ad_astra.install_patchouli"), true);
            }
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }
}
