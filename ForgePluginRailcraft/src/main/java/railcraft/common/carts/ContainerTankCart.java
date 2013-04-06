package railcraft.common.carts;

import net.minecraft.server.*;
import railcraft.common.SlotBucket;
import railcraft.common.SlotBucketFilter;
import railcraft.common.SlotOutput;
import railcraft.common.api.EnumItemType;
import railcraft.common.api.InventoryTools;


public class ContainerTankCart extends Container
{
	private EntityCartTank tank;

	public ContainerTankCart(PlayerInventory var1, EntityCartTank var2)
	{
		setPlayer(var1.player);
		this.tank = var2;
		a(new SlotBucketFilter(var2, 0, 71, 39));
		a(new SlotBucket(var2, 1, 116, 21));
		a(new SlotOutput(var2, 2, 116, 56));

		for (int var3 = 0; var3 < 3; var3++)
		{
			for (int var4 = 0; var4 < 9; var4++)
			{
				a(new Slot(var1, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
			}
		}

		for (int var3 = 0; var3 < 9; var3++)
		{
			a(new Slot(var1, var3, 8 + var3 * 18, 142));
		}
	}

	@Override
	protected boolean a(ItemStack stack, int currentSlot, int endSlot, boolean reverseDirection)
	{
		return InventoryTools.isItemType(stack, EnumItemType.BUCKET) && super.a(stack, currentSlot, endSlot, reverseDirection);
	}

	@Override
	public boolean b(EntityHuman var1)
	{
		return this.tank.a(var1);
	}

	@Override
	public ItemStack a(int var1)
	{
		ItemStack var2 = null;
		Slot var3 = (Slot)this.e.get(var1);

		if ((var3 != null) && (var3.c()))
		{
			ItemStack var4 = var3.getItem();
			var2 = var4.cloneItemStack();

			if ((var1 >= 3) && (var1 < 39) && (SlotBucketFilter.canPlaceItem(var4)))
			{
				if (!a(var4, 1, 2, false))
				{
					return null;
				}
			}
			else if ((var1 >= 3) && (var1 < 30))
			{
				if (!a(var4, 29, 38, false))
				{
					return null;
				}
			}
			else if ((var1 >= 30) && (var1 < 39))
			{
				if (!a(var4, 2, 29, false))
				{
					return null;
				}
			}
			else if (!a(var4, 2, 38, false))
			{
				return null;
			}

			if (var4.count == 0)
			{
				var3.set(null);
			}
			else
			{
				var3.d();
			}

			if (var4.count == var2.count)
			{
				return null;
			}

			var3.c(var4);
		}

		return var2;
	}

	@Override
	public IInventory getInventory()
	{
		return this.tank;
	}
}