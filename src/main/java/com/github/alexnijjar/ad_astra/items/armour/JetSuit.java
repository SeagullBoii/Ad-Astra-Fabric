package com.github.alexnijjar.ad_astra.items.armour;

import java.util.List;

import com.github.alexnijjar.ad_astra.AdAstra;
import com.github.alexnijjar.ad_astra.registry.ModItems;
import com.github.alexnijjar.ad_astra.util.ModKeyBindings;
import com.github.alexnijjar.ad_astra.util.ModUtils;

import net.fabricmc.fabric.api.entity.event.v1.FabricElytraItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import team.reborn.energy.api.base.SimpleBatteryItem;

public class JetSuit extends NetheriteSpaceSuit implements SimpleBatteryItem, FabricElytraItem {

    public static final long MAX_ENERGY = AdAstra.CONFIG.spaceSuit.jetSuitMaxEnergy;
    public static final long TANK_SIZE = AdAstra.CONFIG.spaceSuit.jetSuitTankSize;
    public static final double SPEED = AdAstra.CONFIG.spaceSuit.jetSuitSpeed;
    public static final double UPWARDS_SPEED = AdAstra.CONFIG.spaceSuit.jetSuitUpwardsSpeed;
    public static final long JET_SUIT_ENERGY_PER_TICK = AdAstra.CONFIG.spaceSuit.jetSuitEnergyPerTick;
    public boolean isFallFlying;

    public JetSuit(ArmorMaterial material, EquipmentSlot slot, Settings settings) {
        super(material, slot, settings);
    }

    @Override
    public long getTankSize() {
        return TANK_SIZE;
    }

    @Override
    public boolean useCustomElytra(LivingEntity entity, ItemStack chestStack, boolean tickElytra) {
        return this.isFallFlying;
    }

    // Display energy.
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (stack.isOf(ModItems.JET_SUIT)) {
            long energy = this.getStoredEnergy(stack);
            tooltip.add(Text.translatable("gauge_text.ad_astra.storage", energy, MAX_ENERGY).setStyle(Style.EMPTY.withColor(energy > 0 ? Formatting.GREEN : Formatting.RED)));
        }
    }

    public void fly(PlayerEntity player, ItemStack stack) {
        // Don't fly the Jet Suit in creative.
        if (player.getAbilities().flying) {
            return;
        }

        // Don't fly if the Jet Suit has no energy.
        if (this.getStoredEnergy(stack) <= 0) {
            return;
        }

        if (ModKeyBindings.sprintKeyDown(player)) {
            this.fallFly(player, stack);
        } else {
            this.hover(player, stack);
        }

        if (!player.world.isClient) {
            if (isFallFlying) {
                if (!player.isFallFlying()) {
                    player.startFallFlying();
                }
            } else {
                if (player.isFallFlying()) {
                    player.stopFallFlying();
                    if (!player.isCreative()) {
                    }
                }
            }

            this.spawnParticles(player, stack);
        }
    }

    public void hover(PlayerEntity player, ItemStack stack) {
        if (!this.tryUseEnergy(stack, JET_SUIT_ENERGY_PER_TICK)) {
            this.setStoredEnergy(stack, 0);
        }
        isFallFlying = false;

        double speed = UPWARDS_SPEED;
        player.setVelocity(player.getVelocity().add(0.0, speed, 0.0));
        if (player.getVelocity().getY() > speed) {
            player.setVelocity(player.getVelocity().getX(), speed, player.getVelocity().getZ());
        }
    }

    public void fallFly(PlayerEntity player, ItemStack stack) {
        if (!this.tryUseEnergy(stack, JET_SUIT_ENERGY_PER_TICK * 2)) {
            this.setStoredEnergy(stack, 0);
        }
        isFallFlying = true;

        double speed = SPEED - (ModUtils.getPlanetGravity(player.world) * 0.25);
        Vec3d rotationVector = player.getRotationVector().multiply(speed);
        Vec3d velocity = player.getVelocity();
        player.setVelocity(velocity.add(rotationVector.getX() * 0.1 + (rotationVector.getX() * 1.5 - velocity.getX()) * 0.5, rotationVector.getY() * 0.1 + (rotationVector.getY() * 1.5 - velocity.getY()) * 0.5,
                rotationVector.getZ() * 0.1 + (rotationVector.getZ() * 1.5 - velocity.getZ()) * 0.5));
    }

    public void spawnParticles(PlayerEntity player, ItemStack stack) {
        if (!AdAstra.CONFIG.spaceSuit.spawnJetSuitParticles) {
            return;
        }
        if (player.world instanceof ServerWorld serverWorld) {
            Vec3d pos = player.getPos();

            if (player.isFallFlying()) {
                ModUtils.spawnForcedParticles(serverWorld, ParticleTypes.FLAME, pos.getX(), pos.getY(), pos.getZ(), 1, 0.0, 0.0, 0.0, 0);
            } else {
                ModUtils.spawnForcedParticles(serverWorld, ParticleTypes.FLAME, pos.getX(), pos.getY(), pos.getZ(), 1, 0.0, 0.0, 0.0, 0);
            }
        }
    }

    @Override
    public long getEnergyCapacity() {
        return MAX_ENERGY;
    }

    @Override
    public long getEnergyMaxInput() {
        return 512;
    }

    @Override
    public long getEnergyMaxOutput() {
        return 256;
    }
}