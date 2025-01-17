package com.github.alexnijjar.ad_astra.client.renderer.block.globe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
public class GlobeItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

	private long prevWorldTime;
	@Override
	public void render(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

		GlobeModel model = GlobeModel.getModel();
		
		// Constant spin
		MinecraftClient client = MinecraftClient.getInstance();
		float tickDelta = client.getTickDelta();
		model.setYaw(MathHelper.lerp(tickDelta, prevWorldTime, client.world.getTime()) / -20.0f);
		prevWorldTime = client.world.getTime();

		GlobeRenderer.render(Registry.ITEM.getId(stack.getItem()), model, Direction.NORTH, matrices, vertexConsumers, light, overlay);
	}
}