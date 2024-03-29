package io.github.haykam821.shardthief.game;

import io.github.haykam821.shardthief.game.phase.ShardThiefActivePhase;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PlayerShardEntry implements Comparable<PlayerShardEntry> {
	private final ShardThiefActivePhase phase;
	private final ServerPlayerEntity player;

	private int counts;
	private int invulnerability;

	public PlayerShardEntry(ShardThiefActivePhase phase, ServerPlayerEntity player) {
		this.phase = phase;
		this.player = player;

		this.counts = phase.getConfig().getStartingCounts();
		this.invulnerability = phase.getConfig().getShardInvulnerability();
	}

	public ServerPlayerEntity getPlayer() {
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

	public Text getWinMessage() {
		String key = this.phase.hasShardDropped() ? "text.shardthief.win" : "text.shardthief.win.perfect";
		return Text.translatable(key, this.getPlayer().getDisplayName()).formatted(Formatting.GOLD);
	}

	public Text getStealMessage() {
		MutableText playerName = this.getPlayer().getDisplayName().copy().formatted(Formatting.AQUA);
		return Text.translatable("text.shardthief.shard_stolen", playerName).formatted(Formatting.WHITE);
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