package floris0106.elytratweaks.util;

import static net.minecraft.world.entity.LivingEntity.canGlideUsing;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;

import java.util.Comparator;
import java.util.Optional;

public class InventoryUtil
{
	public static boolean equipGlider(Player player)
	{
		Optional<Slot> gliderSlot = player.inventoryMenu.slots.stream()
			.filter(slot ->
			{
				ItemStack item = slot.getItem();
				if (!item.has(DataComponents.GLIDER) || item.nextDamageWillBreak())
					return false;
				Equippable equippable = item.get(DataComponents.EQUIPPABLE);
				return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
			})
			.max(Comparator.comparing((Slot slot) -> slot.getItem().isEnchanted()).thenComparing(slot -> slot.getItem().getDamageValue()));

		if (gliderSlot.isEmpty())
			return false;

		int slotIndex = 8 - gliderSlot.get().getItem().get(DataComponents.EQUIPPABLE).slot().getIndex();
		swapSlots(slotIndex, gliderSlot.get().index);

		return true;
	}

	public static void equipArmor(Player player)
	{
		for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES)
			if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && canGlideUsing(player.getItemBySlot(equipmentSlot), equipmentSlot))
			{
				Optional<Slot> armorSlot = player.inventoryMenu.slots.stream()
					.filter(slot ->
					{
						ItemStack item = slot.getItem();
						Equippable equippable = item.get(DataComponents.EQUIPPABLE);
						return equippable != null && equippable.slot() == equipmentSlot;
					})
					.max(Comparator.comparing((Slot slot) ->
					{
						ItemStack item = slot.getItem();
						ItemAttributeModifiers modifiers = item.get(DataComponents.ATTRIBUTE_MODIFIERS);
						return modifiers == null ? 0.0 : modifiers.modifiers().stream()
							.filter(entry -> entry.slot().test(equipmentSlot) && entry.attribute() == Attributes.ARMOR)
							.reduce(0.0, (value, entry) ->
							{
								double amount = entry.modifier().amount();
								return value + switch (entry.modifier().operation())
								{
									case ADD_VALUE -> amount;
									case ADD_MULTIPLIED_BASE -> 0.0;
									case ADD_MULTIPLIED_TOTAL -> amount * value;
								};
							}, Double::sum);
					}).thenComparing(slot -> slot.getItem().isEnchanted()));

				if (armorSlot.isEmpty())
					continue;

				int slotIndex = 8 - armorSlot.get().getItem().get(DataComponents.EQUIPPABLE).slot().getIndex();
				swapSlots(slotIndex, armorSlot.get().index);
			}
	}

	private static void swapSlots(int slot1, int slot2)
	{
		if (slot1 == slot2)
			return;

		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		InventoryMenu menu = player.inventoryMenu;
		MultiPlayerGameMode gameMode = minecraft.gameMode;

		gameMode.handleInventoryMouseClick(menu.containerId, slot1, 0, ClickType.PICKUP, player);
		gameMode.handleInventoryMouseClick(menu.containerId, slot2, 0, ClickType.PICKUP, player);
		gameMode.handleInventoryMouseClick(menu.containerId, slot1, 0, ClickType.PICKUP, player);
	}
}