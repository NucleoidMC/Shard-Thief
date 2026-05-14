package io.github.haykam821.shardthief.game.map;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.world.entity.Display.BillboardConstraints;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.Vec3;

public final class ShardThiefGuideText {
	private static final ChatFormatting GUIDE_FORMATTING = ChatFormatting.GOLD;
	private static final Component GUIDE_TEXT = Component.empty()
		.append(Component.translatable("gameType.shardthief.shard_thief").withStyle(ChatFormatting.BOLD))
		.append(CommonComponents.NEW_LINE)
		.append(Component.translatable("text.shardthief.guide"))
		.withStyle(GUIDE_FORMATTING);

	public static HolderAttachment spawn(ServerLevel world, Vec3 pos) {
		TextDisplayElement element = new TextDisplayElement(GUIDE_TEXT);

		element.setBillboardMode(BillboardConstraints.CENTER);
		element.setLineWidth(350);

		ElementHolder holder = new ElementHolder();
		holder.addElement(element);

		return ChunkAttachment.of(holder, world, pos);
	}
}
