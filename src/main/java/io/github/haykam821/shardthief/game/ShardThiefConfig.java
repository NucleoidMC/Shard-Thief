package io.github.haykam821.shardthief.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.shardthief.game.map.ShardThiefMapConfig;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public class ShardThiefConfig {
	public static final Codec<ShardThiefConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			ShardThiefMapConfig.CODEC.fieldOf("map").forGetter(ShardThiefConfig::getMapConfig),
			PlayerConfig.CODEC.fieldOf("players").forGetter(ShardThiefConfig::getPlayerConfig),
			Codec.INT.optionalFieldOf("starting_counts", 20).forGetter(ShardThiefConfig::getStartingCounts),
			Codec.INT.optionalFieldOf("restart_counts", 5).forGetter(ShardThiefConfig::getRestartCounts),
			Codec.INT.optionalFieldOf("shard_invulnerability", 10).forGetter(ShardThiefConfig::getShardInvulnerability),
			Codec.INT.optionalFieldOf("kit_restock_interval", 20 * 5).forGetter(ShardThiefConfig::getKitRestockInterval),
			Codec.INT.optionalFieldOf("max_arrows", 3).forGetter(ShardThiefConfig::getMaxArrows),
			Codec.INT.optionalFieldOf("speed_amplifier", 2).forGetter(ShardThiefConfig::getSpeedAmplifier)
		).apply(instance, ShardThiefConfig::new);
	});

	private final ShardThiefMapConfig mapConfig;
	private final PlayerConfig playerConfig;
	private final int startingCounts;
	private final int restartCounts;
	private final int shardInvulnerability;
	private final int kitRestockInterval;
	private final int maxArrows;
	private final int speedAmplifier;

	public ShardThiefConfig(ShardThiefMapConfig mapConfig, PlayerConfig playerConfig, int startingCounts, int restartCounts, int shardInvulnerability, int kitRestockInterval, int maxArrows, int speedAmplifier) {
		this.mapConfig = mapConfig;
		this.playerConfig = playerConfig;
		this.startingCounts = startingCounts;
		this.restartCounts = restartCounts;
		this.shardInvulnerability = shardInvulnerability;
		this.kitRestockInterval = kitRestockInterval;
		this.maxArrows = maxArrows;
		this.speedAmplifier = speedAmplifier;
	}

	public ShardThiefMapConfig getMapConfig() {
		return this.mapConfig;
	}

	public PlayerConfig getPlayerConfig() {
		return this.playerConfig;
	}

	public int getStartingCounts() {
		return this.startingCounts;
	}

	public int getRestartCounts() {
		return this.restartCounts;
	}

	public int getShardInvulnerability() {
		return this.shardInvulnerability;
	}

	public int getKitRestockInterval() {
		return this.kitRestockInterval;
	}

	public int getMaxArrows() {
		return this.maxArrows;
	}

	public int getSpeedAmplifier() {
		return this.speedAmplifier;
	}
}
