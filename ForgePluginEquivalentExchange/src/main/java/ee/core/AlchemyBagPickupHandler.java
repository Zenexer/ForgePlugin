package ee.core;

import ee.EEItem;
import ee.ItemAlchemyBag;
import ee.item.ItemLootBall;
import forge.IPickupHandler;
import java.util.Collections;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityItem;
import net.minecraft.server.ItemStack;


public final class AlchemyBagPickupHandler implements IPickupHandler
{
	@Override
	public boolean onItemPickup(final EntityHuman player, final EntityItem item)
	{
		final ItemStack stack = item.itemStack;

		if (stack.getItem() instanceof ItemLootBall)
		{
			return true;
		}

		for (final ItemStack inventoryStack : player.inventory.getContents())
		{
			if (inventoryStack == null)
			{
				continue;
			}

			if (inventoryStack.id == EEItem.alchemyBag.id)
			{
				if (ItemAlchemyBag.getBagData(inventoryStack, player, player.world).onItemPickup(stack))
				{
					return false;
				}
			}
		}

		return true;
	}
}
