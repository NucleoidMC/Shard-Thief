package io.github.haykam821.shardthief.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

public class ShardThiefMapConfig {
	public static final Codec<ShardThiefMapConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Identifier.CODEC.fieldOf("structure_id").forGetter(ShardThiefMapConfig::getStructureId),
			Biome.CODEC.fieldOf("biome").forGetter(ShardThiefMapConfig::getBiome),
			RuleTest.CODEC.optionalFieldOf("terracotta_rule", new BlockMatchTest(Blocks.DYED_TERRACOTTA.white())).forGetter(ShardThiefMapConfig::getTerracottaRule),
			RuleTest.CODEC.optionalFieldOf("concrete_rule", new BlockMatchTest(Blocks.CONCRETE.white())).forGetter(ShardThiefMapConfig::getConcreteRule),
			RuleTest.CODEC.optionalFieldOf("stained_glass_rule", new BlockMatchTest(Blocks.STAINED_GLASS.white())).forGetter(ShardThiefMapConfig::getStainedGlassRule),
			RuleTest.CODEC.optionalFieldOf("wool_rule", new BlockMatchTest(Blocks.WOOL.white())).forGetter(ShardThiefMapConfig::getWoolRule),
			RuleTest.CODEC.optionalFieldOf("carpet_rule", new BlockMatchTest(Blocks.CARPET.white())).forGetter(ShardThiefMapConfig::getCarpetRule),
			Codec.INT.optionalFieldOf("spawn_y_offset", 1).forGetter(ShardThiefMapConfig::getSpawnYOffset)
		).apply(instance, ShardThiefMapConfig::new);
	});

	private final Identifier structureId;
	private final Holder<Biome> biome;
	private final RuleTest terracottaRule;
	private final RuleTest concreteRule;
	private final RuleTest stainedGlassRule;
	private final RuleTest woolRule;
	private final RuleTest carpetRule;
	private final int spawnYOffset;

	public ShardThiefMapConfig(Identifier structureId, Holder<Biome> biome, RuleTest terracottaRule, RuleTest concreteRule, RuleTest stainedGlassRule, RuleTest woolRule, RuleTest carpetRule, int spawnYOffset) {
		this.structureId = structureId;
		this.biome = biome;
		this.terracottaRule = terracottaRule;
		this.concreteRule = concreteRule;
		this.stainedGlassRule = stainedGlassRule;
		this.woolRule = woolRule;
		this.carpetRule = carpetRule;
		this.spawnYOffset = spawnYOffset;
	}

	public Identifier getStructureId() {
		return this.structureId;
	}

	public Holder<Biome> getBiome() {
		return this.biome;
	}

	public RuleTest getTerracottaRule() {
		return this.terracottaRule;
	}

	public RuleTest getConcreteRule() {
		return this.concreteRule;
	}

	public RuleTest getStainedGlassRule() {
		return this.stainedGlassRule;
	}

	public RuleTest getWoolRule() {
		return this.woolRule;
	}

	public RuleTest getCarpetRule() {
		return this.carpetRule;
	}

	public int getSpawnYOffset() {
		return this.spawnYOffset;
	}
}