package io.github.haykam821.shardthief.game.shard;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import io.github.haykam821.shardthief.game.phase.ShardThiefActivePhase;
import net.minecraft.util.Brightness;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LightLayer;

public class DisplayDroppedShard extends DroppedShard {
	private final ElementHolder holder;
	private HolderAttachment attachment;

	public DisplayDroppedShard(ShardThiefActivePhase phase, ServerLevel level, Vec3 pos, int invulnerability) {
		super(phase, pos, invulnerability);

		this.holder = createHolder(level, pos);
	}

	@Override
	public void place(ServerLevel level) {
		this.attachment = ChunkAttachment.of(this.holder, level, this.getPos().add(-0.5, 0.0005, -0.5));
	}

	@Override
	public void reset(ServerLevel level) {
		this.attachment.destroy();
		this.attachment = null;
	}

	private static ElementHolder createHolder(ServerLevel level, Vec3 pos) {
		BlockPos lightPos = BlockPos.containing(pos.x(), pos.y() + 1, pos.z());

		int blockLight = level.getBrightness(LightLayer.BLOCK, lightPos);
		int skyLight = level.getBrightness(LightLayer.SKY, lightPos);

		BlockDisplayElement element = new BlockDisplayElement(FULL_DROP_STATE);
		element.setBrightness(new Brightness(blockLight, skyLight));

		ElementHolder holder = new ElementHolder();
		holder.addElement(element);

		return holder;
	}
}

