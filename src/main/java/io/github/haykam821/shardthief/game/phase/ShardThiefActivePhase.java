package io.github.haykam821.shardthief.game.phase;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import io.github.haykam821.shardthief.game.PlayerShardEntry;
import io.github.haykam821.shardthief.game.ShardInventoryManager;
import io.github.haykam821.shardthief.game.ShardThiefConfig;
import io.github.haykam821.shardthief.game.ShardThiefCountBar;
import io.github.haykam821.shardthief.game.event.AllowProjectileHitEvent;
import io.github.haykam821.shardthief.game.map.ShardThiefMap;
import io.github.haykam821.shardthief.game.shard.BlockDroppedShard;
import io.github.haykam821.shardthief.game.shard.DisplayDroppedShard;
import io.github.haykam821.shardthief.game.shard.DroppedShard;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import net.minecraft.world.waypoints.Waypoint;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class ShardThiefActivePhase {
	private final ServerLevel level;
	private final GameSpace gameSpace;
	private final ShardThiefMap map;
	private final ShardThiefConfig config;
	private final Set<PlayerShardEntry> players;
	private final ShardThiefCountBar countBar;
	private final ShardInventoryManager shardInventoryManager;

	private final HolderAttachment guideText;
	private int guideTicks;

	private PlayerShardEntry shardHolder;
	private int ticksUntilClose = -1;
	private int ticksUntilCount;
	private int ticksUntilKitRestock;
	private DroppedShard droppedShard;
	private boolean shardDropped;
	private UUID shardUUID = UUID.randomUUID();

	public ShardThiefActivePhase(GameSpace gameSpace, ServerLevel level, ShardThiefMap map, ShardThiefConfig config, Set<ServerPlayer> players, GlobalWidgets widgets, HolderAttachment guideText) {
		this.level = level;
		this.gameSpace = gameSpace;
		this.map = map;
		this.config = config;

		this.players = players.stream().map(player -> {
			return new PlayerShardEntry(this, player);
		}).collect(Collectors.toSet());

		this.countBar = new ShardThiefCountBar(GameConfig.name(gameSpace.getMetadata().sourceConfig()), widgets);

		this.shardInventoryManager = new ShardInventoryManager(this.level.registryAccess());

		this.placeDisplayShard(this.map.getInitialShardPos());

		this.guideText = guideText;
		this.guideTicks = this.config.getGuideTicks();
	}

	public static void setRules(GameActivity activity, boolean pvp) {
		activity.deny(GameRuleType.CRAFTING);
		activity.deny(GameRuleType.FALL_DAMAGE);
		activity.deny(GameRuleType.HUNGER);
		activity.deny(GameRuleType.ICE_MELT);
		activity.deny(GameRuleType.INTERACTION);
		activity.deny(GameRuleType.PORTALS);
		activity.deny(GameRuleType.THROW_ITEMS);

		if (pvp) {
			activity.allow(GameRuleType.PVP);

			activity.allow(GameRuleType.INTERACTION);
			activity.deny(GameRuleType.USE_BLOCKS);
			activity.deny(GameRuleType.USE_ENTITIES);
		} else {
			activity.deny(GameRuleType.PVP);
		}
	}

	public static void open(GameSpace gameSpace, ServerLevel level, ShardThiefMap map, ShardThiefConfig config, HolderAttachment guideText) {
		gameSpace.setActivity(activity -> {
			GlobalWidgets widgets = GlobalWidgets.addTo(activity);

			ShardThiefActivePhase active = new ShardThiefActivePhase(gameSpace, level, map, config, Sets.newHashSet(gameSpace.getPlayers().participants()), widgets, guideText);
			ShardThiefActivePhase.setRules(activity, true);

			// Listeners
			activity.listen(GameActivityEvents.ENABLE, active::enable);
			activity.listen(GameActivityEvents.TICK, active::tick);
			activity.listen(GamePlayerEvents.ACCEPT, active::onAcceptPlayers);
			activity.listen(GamePlayerEvents.OFFER, JoinOffer::acceptSpectators);
			activity.listen(AllowProjectileHitEvent.EVENT, active::allowProjectileHit);
			activity.listen(PlayerDamageEvent.EVENT, active::onPlayerDamage);
			activity.listen(PlayerDeathEvent.EVENT, active::onPlayerDeath);
			activity.listen(GamePlayerEvents.REMOVE, active::removePlayer);
		});
	}

	private void enable() {
		int index = 0;
		for (PlayerShardEntry entry : this.players) {
			ServerPlayer player = entry.getPlayer();

			player.setGameMode(GameType.ADVENTURE);
			this.shardInventoryManager.giveNonShardInventory(player);

			ShardThiefActivePhase.spawn(this.level, this.map, player, index);
			index += 1;
		}

		for (ServerPlayer player : this.gameSpace.getPlayers().spectators()) {
			Vec3 pos = this.map.getCenterSpawnPos();
			player.teleportTo(level, pos.x(), pos.y(), pos.z(), Set.of(), 0, 0, true);

			this.setSpectator(player);
		}

		for (ServerPlayer player : this.gameSpace.getPlayers()) {
			player.connection.send(ClientboundTrackedWaypointPacket.addWaypointPosition(shardUUID, Waypoint.Icon.NULL, Vec3i.ZERO));
		}
	}

	public float getTimerBarPercent() {
		if (this.shardHolder == null) {
			return 1;
		}
		return this.shardHolder.getCounts() / (float) this.config.getStartingCounts();
	}

	private void resetTicksUntilCount() {
		this.ticksUntilCount = this.config.getCountDuration();
	}

	private void clearShard() {
		if (this.shardHolder == null) return;

		this.shardHolder.getPlayer().getInventory().clearContent();
		this.shardInventoryManager.giveNonShardInventory(this.shardHolder.getPlayer());

		if (this.shardHolder.getCounts() < this.config.getRestartCounts()) {
			this.shardHolder.setCounts(this.config.getRestartCounts());
		}
		this.shardHolder = null;

		this.resetTicksUntilCount();
	}

	private void setShardHolder(PlayerShardEntry entry) {
		this.clearShard();
		if (this.shardHolder != null && !this.shardHolder.equals(entry)) {
			this.shardHolder.getPlayer().connection.send(ClientboundTrackedWaypointPacket.addWaypointPosition(shardUUID, Waypoint.Icon.NULL, Vec3i.ZERO));
		}

		this.shardHolder = entry;
		entry.setInvulnerability(this.config.getShardInvulnerability());

		entry.getPlayer().getInventory().clearContent();
		this.shardInventoryManager.giveShardInventory(entry.getPlayer());
		entry.getPlayer().connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(shardUUID));
	}

	private void sendStealMessage() {
		Component stealText = this.shardHolder.getStealMessage();
		this.gameSpace.getPlayers().sendActionBar(stealText);
	}

	private void pickUpShard(PlayerShardEntry entry) {
		this.setShardHolder(entry);

		this.droppedShard.reset(this.level);
		this.droppedShard = null;
		this.applyStealSpeed(entry.getPlayer());
	
		this.level.playSound(null, entry.getPlayer().blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1, 1);
		this.sendStealMessage();
	}

	private BlockPos findDropPos(BlockPos initialPos) {
		BlockPos.MutableBlockPos pos = initialPos.mutable();
		while (true) {
			if (pos.getY() == 0) return pos;

			BlockState state = this.level.getBlockState(pos);
			if (DroppedShard.isDroppableOn(state, this.level, pos)) {
				return pos;
			}

			pos.move(Direction.DOWN);
		}
	}

	public void attemptResetShard(int ticks, Vec3 pos) {
		if (ticks < this.config.getDroppedShardResetTicks()) return;
		if (pos.equals(this.map.getInitialShardPos())) return;

		this.droppedShard.reset(this.level);

		this.placeDisplayShard(this.map.getInitialShardPos());
		this.playDropSound(pos);

		Component message = this.droppedShard.getResetMessage();
		this.gameSpace.getPlayers().sendMessage(message);
	}

	private void placeShard(BlockPos pos) {
		this.droppedShard = new BlockDroppedShard(this, pos, this.level.getBlockState(pos), this.config.getShardInvulnerability());
		this.droppedShard.place(this.level);
	}

	private void placeDisplayShard(Vec3 pos) {
		this.droppedShard = new DisplayDroppedShard(this, this.level, pos, this.config.getShardInvulnerability());
		this.droppedShard.place(this.level);
	}

	private void playDropSound(Vec3 pos) {
		level.playSound(null, pos.x(), pos.y(), pos.z(), SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 1, 1);
	}

	private void dropShard() {
		BlockPos pos = this.findDropPos(this.shardHolder.getPlayer().blockPosition());
		this.placeShard(pos);

		this.shardDropped = true;
		this.clearShard();

		this.playDropSound(Vec3.atCenterOf(pos));
	}

	private ChatFormatting getCountTitleColor() {
		int counts = this.shardHolder.getCounts();
		if (counts <= 1) {
			return ChatFormatting.RED;
		} else if (counts <= 3) {
			return ChatFormatting.GOLD;
		} else {
			return ChatFormatting.YELLOW;
		}
	}

	private void tickCounts() {
		this.shardHolder.decrementCounts();
		if (this.shardHolder.getCounts() <= 0) {
			Component message = this.shardHolder.getWinMessage();
			this.gameSpace.getPlayers().sendMessage(message);

			this.gameSpace.getPlayers().playSound(SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1, 1);

			this.ticksUntilClose = this.config.getTicksUntilClose().sample(this.level.getRandom());
			return;
		} else if (this.shardHolder.getCounts() <= 5) {
			String countString = Integer.toString(this.shardHolder.getCounts());
			Component countText = Component.literal(countString).withStyle(this.getCountTitleColor()).withStyle(ChatFormatting.BOLD);
			this.gameSpace.getPlayers().showTitle(countText, 70);

			this.gameSpace.getPlayers().playSound(SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.PLAYERS, 1, 1.5f);
		}

		this.resetTicksUntilCount();
	}

	private boolean canPlayerPickUpDroppedShard(Player player) {
		return this.droppedShard != null && this.droppedShard.canPlayerPickUp(player);
	}

	private void restockKits() {
		for (PlayerShardEntry entry : this.players) {
			if (!entry.equals(this.shardHolder)) {
				this.shardInventoryManager.restockArrows(entry.getPlayer(), this.config.getMaxArrows());
			}
		}
		this.ticksUntilKitRestock = this.config.getKitRestockInterval();
	}

	private void tick() {
		// Decrease ticks until game end to zero
		if (this.isGameEnding()) {
			if (this.ticksUntilClose == 0) {
				this.gameSpace.close(GameCloseReason.FINISHED);
			}

			this.ticksUntilClose -= 1;
		}

		this.countBar.tick(this);

		// Remove guide text after guide ticks reach zero
		if (this.guideTicks > 0) {
			this.guideTicks -= 1;
		} else if (this.guideText != null && this.guideTicks == 0) {
			this.guideText.destroy();
		}

		if (this.droppedShard != null) {
			this.droppedShard.tick(this.level);
		}

		if (!this.isGameEnding()) {
			if (this.ticksUntilKitRestock <= 0) {
				this.restockKits();
			}
			this.ticksUntilKitRestock -= 1;
		
			if (this.shardHolder != null) {
				if (this.ticksUntilCount <= 0) {
					this.tickCounts();
				}
				this.ticksUntilCount -= 1;
			}
		}

		Vec3i shardPos;
		if (this.droppedShard != null) {
			Vec3 shardRealPos = this.droppedShard.getPos();
            shardPos = new Vec3i((int) shardRealPos.x(), (int) shardRealPos.y(), (int) shardRealPos.z());
		} else {
			if (this.shardHolder != null) {
				shardPos = this.shardHolder.getPlayer().blockPosition();
			} else {
				shardPos = Vec3i.ZERO;
			}
		}

		for (PlayerShardEntry entry : this.players) {
			entry.tick();

			ServerPlayer player = entry.getPlayer();
			ShardThiefActivePhase.respawnIfOutOfBounds(player, this.map, this.level);

			if (!this.isGameEnding() && !entry.equals(this.shardHolder) && this.canPlayerPickUpDroppedShard(player)) {
				this.pickUpShard(entry);
			}
			if (!entry.equals(this.shardHolder)) {
				player.connection.send(ClientboundTrackedWaypointPacket.updateWaypointPosition(shardUUID, Waypoint.Icon.NULL, shardPos));
			}
		}
	}

	private boolean isGameEnding() {
		return this.ticksUntilClose >= 0;
	}

	private void setSpectator(ServerPlayer player) {
		player.setGameMode(GameType.SPECTATOR);
	}

	public ShardThiefConfig getConfig() {
		return this.config;
	}

	public boolean hasShardDropped() {
		return this.shardDropped;
	}

	private JoinAcceptorResult onAcceptPlayers(JoinAcceptor acceptor) {
		return acceptor.teleport(this.level, this.map.getCenterSpawnPos()).thenRunForEach(player -> {
			this.setSpectator(player);
			player.connection.send(ClientboundTrackedWaypointPacket.addWaypointPosition(shardUUID, Waypoint.Icon.NULL, Vec3i.ZERO));
		});
	}

	private void removePlayer(ServerPlayer player) {
		// Drop shard when player is removed
		if (!this.isGameEnding() && this.shardHolder != null && player.equals(this.shardHolder.getPlayer())) {
			this.dropShard();
		}
		player.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(shardUUID));
		this.players.removeIf(entry -> {
			return player.equals(entry.getPlayer());
		});
	}

	private void applyStealSpeed(ServerPlayer player) {
		if (this.config.getSpeedAmplifier() <= 0) return;
		player.addEffect(new MobEffectInstance(MobEffects.SPEED, this.config.getShardInvulnerability() * 2, this.config.getSpeedAmplifier(), true, false, true));
	}

	private void tryTransferShard(ServerPlayer damagedPlayer, DamageSource source) {
		if (this.isGameEnding()) return;

		if (!(source.getEntity() instanceof ServerPlayer)) return;
		ServerPlayer attacker = (ServerPlayer) source.getEntity();

		if (this.shardHolder == null) return;

		Player shardPlayer = this.shardHolder.getPlayer();
		if (!damagedPlayer.equals(shardPlayer)) return;
		if (attacker.equals(shardPlayer)) return;

		for (PlayerShardEntry entry : this.players) {
			if (attacker.equals(entry.getPlayer())) {
				if (source.is(DamageTypeTags.IS_PROJECTILE)) {
					this.dropShard();
					if (source.getDirectEntity() instanceof Projectile) {
						source.getDirectEntity().kill(this.level);
					}
 				} else if (this.shardHolder.canBeStolen()) {
					this.setShardHolder(entry);
					this.applyStealSpeed(entry.getPlayer());
					this.sendStealMessage();
				}
				return;
			}
		}
	}

	private EventResult allowProjectileHit(Entity entity, AbstractArrow projectile) {
		if (this.shardHolder != null) {
			if (entity == this.shardHolder.getPlayer()) {
				return EventResult.ALLOW;
			}
		}

		return EventResult.DENY;
	}

	private EventResult onPlayerDamage(ServerPlayer damagedPlayer, DamageSource source, float damage) {
		this.tryTransferShard(damagedPlayer, source);
		return EventResult.DENY;
	}

	private EventResult onPlayerDeath(ServerPlayer player, DamageSource source) {
		ShardThiefActivePhase.spawn(this.level, this.map, player, 0);
		return EventResult.ALLOW;
	}

	public static void spawn(ServerLevel world, ShardThiefMap map, ServerPlayer player, int index) {
		Direction direction = Direction.from2DDataValue(index);
		int distance = (int) Math.min(index / 4f + 4, 8);
		Vec3 pos = map.getCenterSpawnPos().relative(direction.getOpposite(), distance);

		player.teleportTo(world, pos.x(), pos.y(), pos.z(), Set.of(), direction.toYRot(), 0, true);
	}

	public static void respawnIfOutOfBounds(ServerPlayer player, ShardThiefMap map, ServerLevel world) {
		if (!map.getBox().isInside(player.blockPosition())) {
			ShardThiefActivePhase.spawn(world, map, player, 0);
		}
	}
}
