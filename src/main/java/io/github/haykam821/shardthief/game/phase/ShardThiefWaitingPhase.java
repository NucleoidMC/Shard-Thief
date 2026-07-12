package io.github.haykam821.shardthief.game.phase;

import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import io.github.haykam821.shardthief.game.ShardThiefConfig;
import io.github.haykam821.shardthief.game.map.ShardThiefGuideText;
import io.github.haykam821.shardthief.game.map.ShardThiefMap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class ShardThiefWaitingPhase {
	private final GameSpace gameSpace;
	private final ServerLevel level;
	private final ShardThiefMap map;
	private final ShardThiefConfig config;
	private HolderAttachment guideText;

	public ShardThiefWaitingPhase(GameSpace gameSpace, ServerLevel level, ShardThiefMap map, ShardThiefConfig config) {
		this.gameSpace = gameSpace;
		this.level = level;
		this.map = map;
		this.config = config;
	}

	public static GameOpenProcedure open(GameOpenContext<ShardThiefConfig> context) {
		ShardThiefConfig config = context.config();
		ShardThiefMap map = new ShardThiefMap(config.getMapConfig(), context.server());

		RuntimeLevelConfig levelConfig = new RuntimeLevelConfig()
			.setGenerator(map.createGenerator(context.server()));

		return context.openWithLevel(levelConfig, (activity, level) -> {
			ShardThiefWaitingPhase waiting = new ShardThiefWaitingPhase(activity.getGameSpace(), level, map, config);

			GameWaitingLobby.addTo(activity, config.getPlayerConfig());

			ShardThiefActivePhase.setRules(activity, false);

			// Listeners
			activity.listen(GameActivityEvents.ENABLE, waiting::enable);
			activity.listen(GameActivityEvents.TICK, waiting::tick);
			activity.listen(GamePlayerEvents.ACCEPT, waiting::onAcceptPlayers);
			activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
			activity.listen(PlayerDamageEvent.EVENT, waiting::onPlayerDamage);
			activity.listen(PlayerDeathEvent.EVENT, waiting::onPlayerDeath);
			activity.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
		});
	}

	private void enable() {
		// Spawn guide text
		Vec3 center = this.map.getCenterSpawnPos().add(0, 2, 0);
		this.guideText = ShardThiefGuideText.spawn(this.level, center);
	}

	private void tick() {
		for (ServerPlayer player : this.gameSpace.getPlayers()) {
			ShardThiefActivePhase.respawnIfOutOfBounds(player, this.map, this.level);
		}
	}

	private GameResult requestStart() {
		ShardThiefActivePhase.open(this.gameSpace, this.level, this.map, this.config, this.guideText);
		return GameResult.ok();
	}

	private JoinAcceptorResult onAcceptPlayers(JoinAcceptor acceptor) {
		return acceptor.teleport(this.level, Vec3.ZERO).thenRunForEach(player -> {
			player.setGameMode(GameType.ADVENTURE);
			ShardThiefActivePhase.spawn(this.level, this.map, player, this.gameSpace.getPlayers().size() - 1);
		});
	}

	private EventResult onPlayerDamage(ServerPlayer player, DamageSource source, float amount) {
		return EventResult.DENY;
	}

	private EventResult onPlayerDeath(ServerPlayer player, DamageSource source) {
		// Respawn player
		ShardThiefActivePhase.spawn(this.level, this.map, player, 0);
		return EventResult.DENY;
	}
}
