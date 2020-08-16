package io.github.haykam821.shardthief.game;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

public class ShardInventoryManager {
	private static final int SHARD_HOLDER_ARMOR_COLOR = 0x649683;
	public static final ItemStack SHARD_HOLDER_HELMET = ShardInventoryManager.createArmorStack(Items.LEATHER_HELMET);
	public static final ItemStack SHARD_HOLDER_CHESTPLATE = ShardInventoryManager.createArmorStack(Items.LEATHER_CHESTPLATE);
	public static final ItemStack SHARD_HOLDER_LEGGINGS = ShardInventoryManager.createArmorStack(Items.LEATHER_LEGGINGS);
	public static final ItemStack SHARD_HOLDER_BOOTS = ShardInventoryManager.createArmorStack(Items.LEATHER_BOOTS);
	public static final ItemStack SHARD_HOLDER_SHARD = new ItemStack(Items.PRISMARINE_SHARD);

	public static final ItemStack NON_SHARD_HOLDER_BOW = ShardInventoryManager.createBowStack(Items.BOW);
	public static final ItemStack NON_SHARD_HOLDER_ARROW = new ItemStack(Items.ARROW);

	private static void updateInventory(ServerPlayerEntity player) {
		player.currentScreenHandler.sendContentUpdates();
		player.playerScreenHandler.onContentChanged(player.inventory);
		player.updateCursorStack();
	}

	public static void giveShardInventory(ServerPlayerEntity player) {
		player.inventory.armor.set(3, SHARD_HOLDER_HELMET.copy());
		player.inventory.armor.set(2, SHARD_HOLDER_CHESTPLATE.copy());
		player.inventory.armor.set(1, SHARD_HOLDER_LEGGINGS.copy());
		player.inventory.armor.set(0, SHARD_HOLDER_BOOTS.copy());
	
		for (int slot = 0; slot < 9; slot++) {
			player.inventory.setStack(slot, SHARD_HOLDER_SHARD.copy());
		}

		ShardInventoryManager.updateInventory(player);
	}

	public static void giveNonShardInventory(ServerPlayerEntity player) {
		player.inventory.setStack(0, NON_SHARD_HOLDER_BOW.copy());
		player.inventory.setStack(27, NON_SHARD_HOLDER_ARROW.copy());

		ShardInventoryManager.updateInventory(player);
	}

	private static ItemStack createArmorStack(ItemConvertible item) {
		return ItemStackBuilder.of(item)
			.addEnchantment(Enchantments.BINDING_CURSE, 1)
			.setColor(SHARD_HOLDER_ARMOR_COLOR)
			.setUnbreakable()
			.build();
	}

	private static ItemStack createBowStack(ItemConvertible item) {
		return ItemStackBuilder.of(item)
			.addEnchantment(Enchantments.INFINITY, 1)
			.setUnbreakable()
			.build();
	}
}