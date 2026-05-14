package io.github.haykam821.shardthief.game;

import io.github.haykam821.shardthief.game.phase.ShardThiefActivePhase;
import net.minecraft.world.BossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.widget.BossBarWidget;

public class ShardThiefCountBar {
	private static final ChatFormatting TITLE_COLOR = ChatFormatting.AQUA;

	private final BossBarWidget bar;

	public ShardThiefCountBar(Component title, GlobalWidgets widgets) {
		this.bar = widgets.addBossBar(title.copy().withStyle(TITLE_COLOR), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
	}

	public void tick(ShardThiefActivePhase phase) {
		float percent = phase.getTimerBarPercent();
		this.bar.setProgress(percent);
	}
}
