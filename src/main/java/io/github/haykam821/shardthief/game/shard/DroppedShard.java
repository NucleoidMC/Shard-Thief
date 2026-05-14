package io.github.haykam821.shardthief.game.shard;

import io.github.haykam821.shardthief.game.phase.ShardThiefActivePhase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

public abstract class DroppedShard {
	protected static final BlockState FULL_DROP_STATE = Blocks.PRISMARINE.defaultBlockState();
	protected static final BlockState SLAB_DROP_STATE = Blocks.PRISMARINE_SLAB.defaultBlockState();
	protected static final BlockState STAIRS_DROP_STATE = Blocks.PRISMARINE_STAIRS.defaultBlockState();

	private static final int TICKS_PER_PARTICLE = 2;
	private static final double PARTICLE_Y_OFFSET = 1.1;
	private static final double PARTICLE_MIN_DIAMETER = 0.2;
	private static final double PARTICLE_DIAMETER_VARIANCE = 0.05;
	private static final double PARTICLE_SPEED = 0.12;

	private final ShardThiefActivePhase phase;
	private final Vec3 pos;
	private final AABB pickUpBox;
	private final int invulnerability;

	private int ticks = 0;

	public DroppedShard(ShardThiefActivePhase phase, Vec3 pos, int invulnerability) {
		this.phase = phase;
		this.pos = pos;
		this.pickUpBox = new AABB(pos.subtract(0.5, 0, 0.5), pos.add(0.5, 3, 0.5));
		this.invulnerability = invulnerability;
	}

	public static boolean isDroppableOn(BlockState state, BlockGetter level, BlockPos pos) {
		Block block = state.getBlock();
		if (block instanceof SlabBlock) return true;
		if (block instanceof StairBlock) return true;

		VoxelShape collisionShape = state.getCollisionShape(level, pos);
		return Block.isFaceFull(collisionShape, Direction.UP);
	}

	public abstract void place(ServerLevel level);

	public abstract void reset(ServerLevel level);

	public Component getResetMessage() {
		return Component.translatable("text.shardthief.dropped_shard_reset").withStyle(ChatFormatting.RED);
	}

	public Vec3 getPos() {
		return this.pos;
	}

	private boolean hasInvulnerability() {
		return this.ticks <= this.invulnerability;
	}

	public boolean canPlayerPickUp(Player player) {
		return !this.hasInvulnerability() && this.pickUpBox.intersects(player.getBoundingBox());
	}

	private void spawnParticles(ServerLevel level) {
		RandomSource random = level.getRandom();
		double angle = random.nextDouble() * Math.PI * 2;
		double diameter = random.nextDouble() * PARTICLE_DIAMETER_VARIANCE + PARTICLE_MIN_DIAMETER;
		
		double x = this.pos.x() + (Math.sin(angle) * diameter);
		double y = this.pos.y() + PARTICLE_Y_OFFSET;
		double z = this.pos.z() + (Math.cos(angle) * diameter);

		level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0, 0, 0.8, 0, PARTICLE_SPEED);
	}

	public void tick(ServerLevel level) {
		this.ticks += 1;
		this.phase.attemptResetShard(this.ticks, this.pos);

		if (!this.hasInvulnerability() && this.ticks % TICKS_PER_PARTICLE == 0) {
			this.spawnParticles(level);
		}
	}
}