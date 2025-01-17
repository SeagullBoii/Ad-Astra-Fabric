package com.github.alexnijjar.ad_astra.util;

import com.github.alexnijjar.ad_astra.AdAstra;
import com.github.alexnijjar.ad_astra.client.registry.ClientModKeybindings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.UUID;

/**
 * Checks if a key is pressed, on both the client or server. On the server, it's saved for every player by mapping their UUID to
 * an instance of this class. Then, when a specific player presses a key, it's only pressed for that player's UUID.
 */
public class ModKeyBindings {
	public static final HashMap<UUID, ModKeyBindings> PLAYER_KEYS = new HashMap<>();

	private boolean clickingJump;
	private boolean clickingSprint;
	private boolean clickingForward;
	private boolean clickingBack;
	private boolean clickingLeft;
	private boolean clickingRight;

	public static boolean jumpKeyDown(PlayerEntity player) {
		return player.world.isClient ? getClientKeyPressed(player, "jump") : getServerKeyPressed(player, "jump");
	}

	public static boolean sprintKeyDown(PlayerEntity player) {
		return player.world.isClient ? getClientKeyPressed(player, "sprint") : getServerKeyPressed(player, "sprint");
	}

	public static boolean forwardKeyDown(PlayerEntity player) {
		return player.world.isClient ? getClientKeyPressed(player, "forward") : getServerKeyPressed(player, "forward");
	}

	public static boolean backKeyDown(PlayerEntity player) {
		return player.world.isClient ? getClientKeyPressed(player, "back") : getServerKeyPressed(player, "back");
	}

	public static boolean leftKeyDown(PlayerEntity player) {
		return player.world.isClient ? getClientKeyPressed(player, "left") : getServerKeyPressed(player, "left");
	}

	public static boolean rightKeyDown(PlayerEntity player) {
		return player.world.isClient ? getClientKeyPressed(player, "right") : getServerKeyPressed(player, "right");
	}

	private static boolean getServerKeyPressed(PlayerEntity player, String key) {
		PLAYER_KEYS.putIfAbsent(player.getUuid(), new ModKeyBindings());
		switch (key) {
			case "jump" -> {
				return PLAYER_KEYS.get(player.getUuid()).clickingJump;
			}
			case "sprint" -> {
				return PLAYER_KEYS.get(player.getUuid()).clickingSprint;
			}
			case "forward" -> {
				return PLAYER_KEYS.get(player.getUuid()).clickingForward;
			}
			case "back" -> {
				return PLAYER_KEYS.get(player.getUuid()).clickingBack;
			}
			case "left" -> {
				return PLAYER_KEYS.get(player.getUuid()).clickingLeft;
			}
			case "right" -> {
				return PLAYER_KEYS.get(player.getUuid()).clickingRight;
			}
		}
		AdAstra.LOGGER.warn("Invalid Keypress on server: " + key);
		return false;
	}

	@Environment(EnvType.CLIENT)
	private static boolean getClientKeyPressed(PlayerEntity player, String key) {

		MinecraftClient client = MinecraftClient.getInstance();
		if (!player.getUuid().equals(client.player.getUuid())) {
			return false;
		}

		switch (key) {
			case "jump" -> {
				return ClientModKeybindings.clickingJump;
			}
			case "sprint" -> {
				return ClientModKeybindings.clickingSprint;
			}
			case "forward" -> {
				return ClientModKeybindings.clickingForward;
			}
			case "back" -> {
				return ClientModKeybindings.clickingBack;
			}
			case "left" -> {
				return ClientModKeybindings.clickingLeft;
			}
			case "right" -> {
				return ClientModKeybindings.clickingRight;
			}
		}

		AdAstra.LOGGER.warn("Invalid Keypress on client: " + key);
		return false;
	}

	public static void pressedKeyOnServer(UUID uuid, String key, boolean keyDown) {
		PLAYER_KEYS.putIfAbsent(uuid, new ModKeyBindings());
		switch (key) {
			case "jump" -> PLAYER_KEYS.get(uuid).clickingJump = keyDown;

			case "sprint" -> PLAYER_KEYS.get(uuid).clickingSprint = keyDown;

			case "forward" -> PLAYER_KEYS.get(uuid).clickingForward = keyDown;

			case "back" -> PLAYER_KEYS.get(uuid).clickingBack = keyDown;

			case "left" -> PLAYER_KEYS.get(uuid).clickingLeft = keyDown;

			case "right" -> PLAYER_KEYS.get(uuid).clickingRight = keyDown;
			default -> AdAstra.LOGGER.warn("Invalid Keypress on server packet: " + key);
		}
	}

	static {
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			PLAYER_KEYS.remove(handler.getPlayer().getUuid());
		});
	}
}
