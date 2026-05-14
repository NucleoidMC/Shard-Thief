package io.github.haykam821.shardthief.game;

import io.github.haykam821.shardthief.game.phase.ShardThiefActivePhase;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class PlayerShardEntry implements Comparable<PlayerShardEntry> {
	private final ShardThiefActivePhase phase;
	private final ServerPlayer player;

	private int counts;
	private int invulnerability;

	public PlayerShardEntry(ShardThiefActivePhase phase, ServerPlayer player) {
		this.phase = phase;
		this.player = player;

		this.counts = phase.getConfig().getStartingCounts();
		this.invulnerability = phase.getConfig().getShardInvulnerability();
	}

	public ServerPlayer getPlayer() {
		return this.player;
	}

	public int getCounts() {
		return this.counts;
	}

	public void setCounts(int counts) {
		this.counts = counts;
	}

	public void decrementCounts() {
		this.counts -= 1;
	}

	public Component getWinMessage() {
		String key = this.phase.hasShardDropped() ? "text.shardthief.win" : "text.shardthief.win.perfect";
		return Component.translatable(key, this.getPlayer().getDisplayName()).withStyle(ChatFormatting.GOLD);
	}

	public Component getStealMessage() {
		MutableComponent playerName = this.getPlayer().getDisplayName().copy().withStyle(ChatFormatting.AQUA);
		return Component.translatable("text.shardthief.shard_stolen", playerName).withStyle(ChatFormatting.WHITE);
	}

	public void setInvulnerability(int invulnerability) {
		this.invulnerability = invulnerability;
	}

	public boolean canBeStolen() {
		return this.invulnerability <= 0;
	}

	public void tick() {
		if (this.invulnerability > 0) {
			this.invulnerability -= 1;
		}
	}

	@Override
	public int compareTo(PlayerShardEntry other) {
		return other.counts - this.counts;
	}

	@Override
	public String toString() {
		return "PlayerShardEntry{player=" + this.getPlayer() + ", counts=" + this.getCounts() + "}";
	}
}