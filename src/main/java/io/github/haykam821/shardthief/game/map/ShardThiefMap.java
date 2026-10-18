package io.github.haykam821.shardthief.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.chunk.ChunkGenerator;

public final class ShardThiefMap {
	public static final BlockPos ORIGIN = new BlockPos(0, 64, 0);

	private final ShardThiefMapConfig mapConfig;
	private final StructureTemplate template;
	private final Vec3 centerSpawnPos;
	private final Vec3 initialShardPos;
	private final BoundingBox box;

	public ShardThiefMap(ShardThiefMapConfig mapConfig, StructureTemplate template) {
		this.mapConfig = mapConfig;
		this.template = template;

		Vec3i size = this.template.getSize();
		this.centerSpawnPos = new Vec3(size.getX(), 64 + this.mapConfig.getSpawnYOffset(), size.getZ());
		this.initialShardPos = this.centerSpawnPos.subtract(0, 1, 0);

		this.box = new BoundingBox(ORIGIN.getX() + 1, ORIGIN.getY(), ORIGIN.getZ() + 1, ORIGIN.getX() + size.getX() * 2, ORIGIN.getY() + size.getY(), ORIGIN.getZ() + size.getZ() * 2);
	}

	public ShardThiefMap(ShardThiefMapConfig mapConfig, MinecraftServer server) {
		this(mapConfig, server.getStructureTemplateManager().getOrCreate(mapConfig.getStructureId()));
	}

	public ShardThiefMapConfig getMapConfig() {
		return this.mapConfig;
	}
	
	public StructureTemplate getTemplate() {
		return this.template;
	}

	public Vec3 getCenterSpawnPos() {
		return this.centerSpawnPos;
	}

	public Vec3 getInitialShardPos() {
		return this.initialShardPos;
	}

	public BoundingBox getBox() {
		return this.box;
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new ShardThiefChunkGenerator(this.mapConfig, this.template, server);
	}
}
