package io.github.haykam821.shardthief.game;

import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import xyz.nucleoid.plasmid.api.util.ItemStackBuilder;

public class ShardInventoryManager {
	private static final int SHARD_HOLDER_ARMOR_COLOR = 0x649683;

	public static final ItemStack SHARD_HOLDER_SHARD = new ItemStack(Items.PRISMARINE_SHARD);
	public static final ItemStack NON_SHARD_HOLDER_ARROW = new ItemStack(Items.ARROW);

	public final ItemStack shardHolderHelmet;
	public final ItemStack shardHolderChestplate;
	public final ItemStack shardHolderLeggings;
	public final ItemStack shardHolderBoots;

	public final ItemStack nonShardHolderBow;

	public ShardInventoryManager(HolderLookup.Provider registries) {
		this.shardHolderHelmet = ShardInventoryManager.createArmorStack(registries, Items.LEATHER_HELMET);
		this.shardHolderChestplate = ShardInventoryManager.createArmorStack(registries, Items.LEATHER_CHESTPLATE);
		this.shardHolderLeggings = ShardInventoryManager.createArmorStack(registries, Items.LEATHER_LEGGINGS);
		this.shardHolderBoots = ShardInventoryManager.createArmorStack(registries, Items.LEATHER_BOOTS);

		this.nonShardHolderBow = ShardInventoryManager.createBowStack(registries, Items.BOW);
	}

	private static void updateInventory(ServerPlayer player) {
		player.containerMenu.broadcastChanges();
		player.inventoryMenu.slotsChanged(player.getInventory());
	}

	public void giveShardInventory(ServerPlayer player) {
		equipSilently(player, EquipmentSlot.HEAD, this.shardHolderHelmet.copy());
		equipSilently(player, EquipmentSlot.CHEST, this.shardHolderChestplate.copy());
		equipSilently(player, EquipmentSlot.LEGS, this.shardHolderLeggings.copy());
		equipSilently(player, EquipmentSlot.FEET, this.shardHolderBoots.copy());

		for (int slot = 0; slot < 9; slot++) {
			player.getInventory().setItem(slot, SHARD_HOLDER_SHARD.copy());
		}

		ShardInventoryManager.updateInventory(player);
	}

	public void giveNonShardInventory(ServerPlayer player) {
		player.getInventory().setItem(0, this.nonShardHolderBow.copy());
		ShardInventoryManager.updateInventory(player);
	}

	public void restockArrows(ServerPlayer player, int maxArrows) {
		int arrows = player.getInventory().countItem(NON_SHARD_HOLDER_ARROW.getItem());
		if (arrows <= maxArrows) {
			player.getInventory().clearContent();
			this.giveNonShardInventory(player);

			ItemStack arrowStack = NON_SHARD_HOLDER_ARROW.copyWithCount(arrows + 1);
			player.getInventory().setItem(1, arrowStack);
			player.connection.send(new ClientboundSoundEntityPacket(Holder.direct(SoundEvents.ITEM_PICKUP), SoundSource.PLAYERS, player, 1, 1, player.level().getRandom().nextLong()));
		}
	}

	private static void equipSilently(ServerPlayer player, EquipmentSlot slot, ItemStack stack) {
		int slotId = slot.getIndex(Inventory.INVENTORY_SIZE);
		player.getInventory().setItem(slotId, stack);
	}

	private static ItemStack createArmorStack(HolderLookup.Provider registries, ItemLike item) {
		return ItemStackBuilder.of(item)
			.addEnchantment(registries, Enchantments.BINDING_CURSE, 1)
			.setDyeColor(SHARD_HOLDER_ARMOR_COLOR)
			.setUnbreakable()
			.build();
	}

	private static ItemStack createBowStack(HolderLookup.Provider registries, ItemLike item) {
		return ItemStackBuilder.of(item)
			.addEnchantment(registries, Enchantments.MENDING, 1)
			.setUnbreakable()
			.build();
	}
}