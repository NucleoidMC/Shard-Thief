package io.github.haykam821.shardthief.game.map;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.StructureManager;
import xyz.nucleoid.plasmid.api.game.level.generator.GameChunkGenerator;

public final class ShardThiefChunkGenerator extends GameChunkGenerator {
	private final ShardThiefMapConfig mapConfig;
	private final StructureTemplate template;

	public ShardThiefChunkGenerator(ShardThiefMapConfig mapConfig, StructureTemplate template, MinecraftServer server) {
		super(new FixedBiomeSource(mapConfig.getBiome()));

		this.mapConfig = mapConfig;
		this.template = template;
	}

	private ProcessorRule getReplaceRule(RuleTest match, Block output) {
		return new ProcessorRule(
			match,
			AlwaysTrueTest.INSTANCE,
			output.defaultBlockState()
		);
	}

	private List<ProcessorRule> getRules(Block terracotta, Block concrete, Block stainedGlass, Block wool, Block carpet) {
		List<ProcessorRule> rules = new ArrayList<>();

		rules.add(this.getReplaceRule(this.mapConfig.getTerracottaRule(), terracotta));
		rules.add(this.getReplaceRule(this.mapConfig.getConcreteRule(), concrete));
		rules.add(this.getReplaceRule(this.mapConfig.getStainedGlassRule(), stainedGlass));
		rules.add(this.getReplaceRule(this.mapConfig.getWoolRule(), wool));
		rules.add(this.getReplaceRule(this.mapConfig.getCarpetRule(), carpet));
	
		return rules;
	}

	private void placeStructure(WorldGenLevel level, ChunkAccess chunk, BlockPos pos, Rotation rotation, Block terracotta, Block concrete, Block stainedGlass, Block wool, Block carpet) {
		StructurePlaceSettings placementData = new StructurePlaceSettings();

		placementData.setRotation(rotation);
		placementData.addProcessor(new RuleProcessor(this.getRules(terracotta, concrete, stainedGlass, wool, carpet)));


		ChunkPos chunkPos = chunk.getPos();
		BoundingBox chunkBox = new BoundingBox(chunkPos.getMinBlockX(), chunk.getMinY(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), chunk.getMaxY(), chunkPos.getMaxBlockZ());

		if (!chunkBox.intersects(this.template.getBoundingBox(placementData, pos))) return;
		placementData.setBoundingBox(chunkBox);

		this.template.placeInWorld(level, pos, pos, placementData, level.getRandom(), Block.UPDATE_INVISIBLE);
	}

	@Override
	public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structures) {
		Vec3i size = this.template.getSize();
		int x = size.getX() * 2 - 1;
		int z = size.getZ() * 2 - 1;

		this.placeStructure(level, chunk, ShardThiefMap.ORIGIN, Rotation.NONE, Blocks.DYED_TERRACOTTA.lime(), Blocks.DYED_TERRACOTTA.lime(), Blocks.STAINED_GLASS.lime(), Blocks.WOOL.lime(), Blocks.CARPET.lime());
		this.placeStructure(level, chunk, ShardThiefMap.ORIGIN.offset(x, 0, 0), Rotation.CLOCKWISE_90, Blocks.DYED_TERRACOTTA.lightBlue(), Blocks.CONCRETE.blue(), Blocks.STAINED_GLASS.blue(), Blocks.WOOL.blue(), Blocks.CARPET.blue());
		this.placeStructure(level, chunk, ShardThiefMap.ORIGIN.offset(x, 0, z), Rotation.CLOCKWISE_180, Blocks.DYED_TERRACOTTA.red(), Blocks.CONCRETE.red(), Blocks.STAINED_GLASS.red(), Blocks.WOOL.red(), Blocks.CARPET.red());
		this.placeStructure(level, chunk, ShardThiefMap.ORIGIN.offset(0, 0, z), Rotation.COUNTERCLOCKWISE_90, Blocks.DYED_TERRACOTTA.yellow(), Blocks.CONCRETE.yellow(), Blocks.STAINED_GLASS.yellow(), Blocks.WOOL.yellow(), Blocks.CARPET.yellow());
	}
}
