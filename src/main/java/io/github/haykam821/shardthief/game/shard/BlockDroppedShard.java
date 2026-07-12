package io.github.haykam821.shardthief.game.shard;

import io.github.haykam821.shardthief.game.phase.ShardThiefActivePhase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class BlockDroppedShard extends DroppedShard {
	private final BlockPos blockPos;
	private final BlockState oldState;

	public BlockDroppedShard(ShardThiefActivePhase phase, BlockPos pos, BlockState oldState, int invulnerability) {
		super(phase, Vec3.atCenterOf(pos), invulnerability);

		this.blockPos = pos;
		this.oldState = oldState;
	}

	private BlockState getBlockState() {
		Block block = this.oldState.getBlock();
		if (block instanceof SlabBlock) {
			return SLAB_DROP_STATE
				.setValue(BlockStateProperties.SLAB_TYPE, this.oldState.getValue(BlockStateProperties.SLAB_TYPE))
				.setValue(BlockStateProperties.WATERLOGGED, this.oldState.getValue(BlockStateProperties.WATERLOGGED));
		} else if (block instanceof StairBlock) {
			return STAIRS_DROP_STATE
				.setValue(HorizontalDirectionalBlock.FACING, this.oldState.getValue(HorizontalDirectionalBlock.FACING))
				.setValue(BlockStateProperties.HALF, this.oldState.getValue(BlockStateProperties.HALF))
				.setValue(BlockStateProperties.STAIRS_SHAPE, this.oldState.getValue(BlockStateProperties.STAIRS_SHAPE))
				.setValue(BlockStateProperties.WATERLOGGED, this.oldState.getValue(BlockStateProperties.WATERLOGGED));
		}

		return FULL_DROP_STATE;
	}

	@Override
	public void place(ServerLevel level) {
		level.setBlock(this.blockPos, this.getBlockState(), 3);
	}

	@Override
	public void reset(ServerLevel level) {
		level.setBlock(this.blockPos, this.oldState, 3);
	}
}
