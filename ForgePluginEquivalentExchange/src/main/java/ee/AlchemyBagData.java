package ee;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.server.*;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;


public class AlchemyBagData extends WorldMapBase implements IInventory
{
	public boolean attractionOn;
	public boolean repairOn;
	public boolean markForUpdate;
	public boolean condenseOn;
	public int repairTimer = 0;
	public int condenseCheckTimer = 0;
	public static final String prefix = "bag";
	public static final String prefix_ = "bag_";
	public ItemStack[] items = new ItemStack[113];
	private int eternalDensity;
	private boolean initialized;
	public static List datas = new LinkedList();

	public AlchemyBagData(String var1)
	{
		super(var1);
	}

	public void onUpdate(World var1, EntityHuman var2)
	{
		if (!initialized)
		{
			initialized = true;
			update();
		}

		if (repairOn)
		{
			doRepair();
		}

		if (condenseOn)
		{
			doCondense(items[eternalDensity]);
		}

		if (attractionOn)
		{
			boolean var3 = false;

			for (int var4 = 0; var4 <= 15; var4++)
			{
				boolean var5 = true;
				ItemStack[] var6 = var2.inventory.items;
				int var7 = var6.length;

				for (int var8 = 0; var8 < var7; var8++)
				{
					ItemStack var9 = var6[var8];

					if ((var9 != null) && (var9.doMaterialsMatch(new ItemStack(EEItem.alchemyBag, 1, var4))))
					{
						var5 = false;
					}
				}

				if (!var5)
				{
					String var10 = "bag_" + var2.name + var4;
					AlchemyBagData var11 = (AlchemyBagData)var1.a(AlchemyBagData.class, var10);

					if (var11 != null)
					{
						if (var3)
						{
							break;
						}

						if (var11.attractionOn)
						{
							var3 = true;
						}

						if ((var11 == this) && (var3))
						{
							doAttraction(var2);
							break;
						}
					}
				}
			}
		}

		if (markForUpdate)
		{
			a();
		}
	}

	@Override
	public int getSize()
	{
		return 104;
	}

	@Override
	public ItemStack getItem(int var1)
	{
		return items[var1];
	}

	@Override
	public ItemStack splitStack(int var1, int var2)
	{
		if (items[var1] != null)
		{
			if (items[var1].count <= var2)
			{
				ItemStack var3 = items[var1];
				items[var1] = null;
				update();
				return var3;
			}

			ItemStack var3 = items[var1].a(var2);

			if (items[var1].count == 0)
			{
				items[var1] = null;
			}

			update();
			return var3;
		}

		return null;
	}

	@Override
	public void setItem(int var1, ItemStack var2)
	{
		items[var1] = var2;

		if ((var2 != null) && (var2.count > getMaxStackSize()))
		{
			var2.count = getMaxStackSize();
		}

		update();
	}

	@Override
	public String getName()
	{
		return "Bag";
	}

	@Override
	public int getMaxStackSize()
	{
		return 64;
	}

	@Override
	public void update()
	{
		markForUpdate = true;
		boolean var1 = false;
		boolean var2 = false;
		boolean var3 = false;

		for (int var4 = 0; var4 < items.length; var4++)
		{
			if (items[var4] != null)
			{
				if (items[var4].getItem() == EEItem.repairCharm)
				{
					var1 = true;
				}

				if (items[var4].getItem() == EEItem.voidRing)
				{
					eternalDensity = var4;

					if ((items[var4].getData() & 0x1) == 0)
					{
						items[var4].setData(items[var4].getData() + 1);
						((ItemEECharged)items[var4].getItem()).setBoolean(items[var4], "active", true);
					}

					var3 = true;
					var2 = true;
				}

				if (items[var4].getItem() == EEItem.eternalDensity)
				{
					eternalDensity = var4;

					if ((items[var4].getData() & 0x1) == 0)
					{
						items[var4].setData(items[var4].getData() + 1);
						((ItemEECharged)items[var4].getItem()).setBoolean(items[var4], "active", true);
					}

					var2 = true;
				}

				if (items[var4].getItem() == EEItem.attractionRing)
				{
					var3 = true;

					if ((items[var4].getData() & 0x1) == 0)
					{
						items[var4].setData(items[var4].getData() + 1);
						((ItemEECharged)items[var4].getItem()).setBoolean(items[var4], "active", true);
					}
				}
			}
		}

		if (var1 != repairOn)
		{
			repairOn = var1;
		}

		if (var2 != condenseOn)
		{
			condenseOn = var2;
		}

		if (var3 != attractionOn)
		{
			attractionOn = var3;
		}
	}

	public void doRepair()
	{
		if (repairTimer >= 20)
		{
			ItemStack var1;
			boolean var2;

			for (int var3 = 0; var3 < getSize(); var3++)
			{
				var2 = false;
				var1 = items[var3];

				if (var1 != null)
				{
					for (int var4 = 0; var4 < EEMaps.chargedItems.size(); var4++)
					{
						if (((Integer)EEMaps.chargedItems.get(Integer.valueOf(var4))).intValue() == var1.id)
						{
							var2 = true;
							break;
						}
					}

					if ((!var2) && (var1.getData() >= 1) && (var1.d()))
					{
						var1.setData(var1.getData() - 1);
					}
				}
			}

			repairTimer = 0;
		}

		repairTimer += 1;
		markForUpdate = true;
	}

	public void doCondense(ItemStack var1)
	{
		if (eternalDensity != -1)
		{
			int var2 = 0;

			for (int var3 = 0; var3 < items.length; var3++)
			{
				if ((items[var3] != null) && (isValidMaterial(items[var3])) && (EEMaps.getEMC(items[var3]) > var2))
				{
					var2 = EEMaps.getEMC(items[var3]);
				}
			}

			for (int var3 = 0; var3 < items.length; var3++)
			{
				if ((items[var3] != null) && (isValidMaterial(items[var3])) && (EEMaps.getEMC(items[var3]) < var2))
				{
					var2 = EEMaps.getEMC(items[var3]);
				}
			}

			if ((var2 >= EEMaps.getEMC(EEItem.redMatter.id)) || (AnalyzeTier(items[eternalDensity], EEMaps.getEMC(EEItem.redMatter.id))) || (var2 >= EEMaps.getEMC(EEItem.darkMatter.id)) || (AnalyzeTier(items[eternalDensity], EEMaps.getEMC(EEItem.darkMatter.id))) || (var2 >= EEMaps.getEMC(Item.DIAMOND.id)) || (AnalyzeTier(items[eternalDensity], EEMaps.getEMC(Item.DIAMOND.id))) || (var2 >= EEMaps.getEMC(Item.GOLD_INGOT.id)) || (AnalyzeTier(items[eternalDensity], EEMaps.getEMC(Item.GOLD_INGOT.id))) || (var2 >= EEMaps.getEMC(Item.IRON_INGOT.id)) || (!AnalyzeTier(items[eternalDensity], EEMaps.getEMC(Item.IRON_INGOT.id))));
		}
	}

	private boolean AnalyzeTier(ItemStack var1, int var2)
	{
		if (var1 == null)
		{
			return false;
		}

		int var3 = 0;

		for (int var4 = 0; var4 < items.length; var4++)
		{
			if ((items[var4] != null) && (isValidMaterial(items[var4])) && (EEMaps.getEMC(items[var4]) < var2))
			{
				var3 += EEMaps.getEMC(items[var4]) * items[var4].count;
			}
		}

		if (var3 + emc(var1) < var2)
		{
			return false;
		}

		for (int var4 = 1; var3 + emc(var1) >= var2 && var4 < 10; var4++)
		{
			ConsumeMaterialBelowTier(var1, var2);
		}

		if ((emc(var1) >= var2) && (roomFor(getProduct(var2))))
		{
			PushStack(getProduct(var2));
			takeEMC(var1, var2);
		}

		return true;
	}

	private boolean roomFor(ItemStack var1)
	{
		if (var1 == null)
		{
			return false;
		}

		for (int var2 = 0; var2 < items.length; var2++)
		{
			if (items[var2] == null)
			{
				return true;
			}

			if ((items[var2].doMaterialsMatch(var1)) && (items[var2].count <= var1.getMaxStackSize() - var1.count))
			{
				return true;
			}
		}

		return false;
	}

	private ItemStack getProduct(int var1)
	{
		return var1 == EEMaps.getEMC(EEItem.redMatter.id) ? new ItemStack(EEItem.redMatter, 1) : var1 == EEMaps.getEMC(EEItem.darkMatter.id) ? new ItemStack(EEItem.darkMatter, 1) : var1 == EEMaps.getEMC(Item.DIAMOND.id) ? new ItemStack(Item.DIAMOND, 1) : var1 == EEMaps.getEMC(Item.GOLD_INGOT.id) ? new ItemStack(Item.GOLD_INGOT, 1) : var1 == EEMaps.getEMC(Item.IRON_INGOT.id) ? new ItemStack(Item.IRON_INGOT, 1) : null;
	}

	public boolean PushStack(ItemStack stack)
	{
		if (stack == null)
		{
			return true;
		}
		
		final int max = stack.getMaxStackSize();

		for (int slot = 0; slot < items.length; slot++)
		{
			if (items[slot] == null)
			{
				items[slot] = stack.cloneItemStack();
				stack.count = 0;
				return true;
			}

			if ((items[slot].doMaterialsMatch(stack)) && (items[slot].count < stack.getMaxStackSize()))
			{
				final int available = max - items[slot].count;
				
				if (available > stack.count)
				{
					items[slot].count = max;
					stack.count -= available;
				}
				else
				{
					items[slot].count += available;
					stack.count = 0;
					return true;
				}
			}
		}

		return false;
	}

	private void ConsumeMaterialBelowTier(ItemStack var1, int var2)
	{
		for (int var3 = 0; var3 < items.length; var3++)
		{
			if ((items[var3] != null) && (isValidMaterial(items[var3])) && (EEMaps.getEMC(items[var3]) < var2))
			{
				addEMC(var1, EEMaps.getEMC(items[var3]));
				items[var3].count -= 1;

				if (items[var3].count == 0)
				{
					items[var3] = null;
				}

				return;
			}
		}
	}

	private boolean isValidMaterial(ItemStack var1)
	{
		if (var1 == null)
		{
			return false;
		}
		if (EEMaps.getEMC(var1) == 0)
		{
			return false;
		}
		if ((var1.getItem() instanceof ItemKleinStar))
		{
			return false;
		}

		int var2 = var1.id;
		return var2 != EEItem.redMatter.id;
	}

	private int emc(ItemStack var1)
	{
		return (var1.getItem() instanceof ItemEternalDensity) ? ((ItemEternalDensity)var1.getItem()).getInteger(var1, "emc") : (!(var1.getItem() instanceof ItemEternalDensity)) && (!(var1.getItem() instanceof ItemVoidRing)) ? 0 : ((ItemVoidRing)var1.getItem()).getInteger(var1, "emc");
	}

	private void takeEMC(ItemStack var1, int var2)
	{
		if (((var1.getItem() instanceof ItemEternalDensity)) || ((var1.getItem() instanceof ItemVoidRing)))
		{
			if ((var1.getItem() instanceof ItemEternalDensity))
			{
				((ItemEternalDensity)var1.getItem()).setInteger(var1, "emc", emc(var1) - var2);
			}
			else
			{
				((ItemVoidRing)var1.getItem()).setInteger(var1, "emc", emc(var1) - var2);
			}
		}
	}

	private void addEMC(ItemStack var1, int var2)
	{
		if (((var1.getItem() instanceof ItemEternalDensity)) || ((var1.getItem() instanceof ItemVoidRing)))
		{
			if ((var1.getItem() instanceof ItemEternalDensity))
			{
				((ItemEternalDensity)var1.getItem()).setInteger(var1, "emc", emc(var1) + var2);
			}
			else
			{
				((ItemVoidRing)var1.getItem()).setInteger(var1, "emc", emc(var1) + var2);
			}
		}
	}

	public void doAttraction(EntityHuman var1)
	{
	}

	private void PullItems(Entity var1, EntityHuman var2)
	{
	}

	private void GrabItems(Entity var1)
	{
	}

	private void PushDenseStacks(EntityLootBall var1)
	{
		for (int var2 = 0; var2 < var1.items.length; var2++)
		{
			if ((var1.items[var2] != null) && (PushStack(var1.items[var2])))
			{
				var1.items[var2] = null;
			}
		}
	}

	public boolean PushStack(EntityItem var1)
	{
		if (var1 == null)
		{
			return false;
		}
		if (var1.itemStack == null)
		{
			var1.die();
			return false;
		}
		if (var1.itemStack.count < 1)
		{
			var1.die();
			return false;
		}

		for (int var2 = 0; var2 < items.length; var2++)
		{
			if (items[var2] == null)
			{
				items[var2] = var1.itemStack.cloneItemStack();

				for (items[var2].count = 0; (var1.itemStack.count > 0) && (items[var2].count < items[var2].getMaxStackSize()); var1.itemStack.count -= 1)
				{
					items[var2].count += 1;
				}

				var1.die();
				return true;
			}

			if ((items[var2].doMaterialsMatch(var1.itemStack)) && (items[var2].count <= var1.itemStack.getMaxStackSize() - var1.itemStack.count))
			{
				while ((var1.itemStack.count > 0) && (items[var2].count < items[var2].getMaxStackSize()))
				{
					items[var2].count += 1;
					var1.itemStack.count -= 1;
				}

				var1.die();
				return true;
			}
		}

		return false;
	}

	private void PushDenseStacks(EntityLootBall var1, EntityHuman var2)
	{
		for (int var3 = 0; var3 < var1.items.length; var3++)
		{
			if (var1.items[var3] != null)
			{
				PushStack(var1.items[var3], var2);
				var1.items[var3] = null;
			}
		}
	}

	public void PushStack(ItemStack var1, EntityHuman var2)
	{
		for (int var3 = 0; var3 < getSize(); var3++)
		{
			if (var1 != null)
			{
				if (items[var3] == null)
				{
					items[var3] = var1.cloneItemStack();
					markForUpdate = true;
					return;
				}

				if (items[var3].doMaterialsMatch(var1))
				{
					do
					{
						items[var3].count += 1;
						var1.count -= 1;

						if (var1.count == 0)
						{
							markForUpdate = true;
							return;
						}
						if (items[var3].count >= items[var3].getMaxStackSize())
						{
							break;
						}
					}
					while (var1 != null);
				}
				else if (var3 == items.length - 1)
				{
					EntityItem var4 = new EntityItem(var2.world, EEBase.playerX(var2), EEBase.playerY(var2), EEBase.playerZ(var2), var1);
					var4.pickupDelay = 1;
					var2.world.addEntity(var4);
					markForUpdate = true;
					return;
				}
			}
		}

		if (var1 != null)
		{
			for (int var3 = 0; var3 < items.length; var3++)
			{
				if (items[var3] == null)
				{
					items[var3] = var1.cloneItemStack();
					markForUpdate = true;
					return;
				}
			}
		}
	}

	public boolean a(EntityHuman var1)
	{
		return true;
	}

	@Override
	public void f()
	{
	}

	@Override
	public void g()
	{
	}

	@Override
	public void a(NBTTagCompound var1)
	{
		attractionOn = var1.getBoolean("voidOn");
		repairOn = var1.getBoolean("repairOn");
		condenseOn = var1.getBoolean("condenseOn");
		eternalDensity = var1.getShort("eternalDensity");
		NBTTagList var2 = var1.getList("Items");
		items = new ItemStack[113];

		for (int var3 = 0; var3 < var2.size(); var3++)
		{
			NBTTagCompound var4 = (NBTTagCompound)var2.get(var3);
			int var5 = var4.getByte("Slot") & 0xFF;

			if ((var5 >= 0) && (var5 < items.length))
			{
				items[var5] = ItemStack.a(var4);
			}
		}
	}

	@Override
	public void b(NBTTagCompound var1)
	{
		var1.setBoolean("voidOn", attractionOn);
		var1.setBoolean("repairOn", repairOn);
		var1.setBoolean("condenseOn", condenseOn);
		var1.setShort("eternalDensity", (short)eternalDensity);
		NBTTagList var2 = new NBTTagList();

		for (int var3 = 0; var3 < items.length; var3++)
		{
			if (items[var3] != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte)var3);
				items[var3].save(var4);
				var2.add(var4);
			}
		}

		var1.set("Items", var2);
	}

	@Override
	public ItemStack splitWithoutUpdate(int var1)
	{
		return null;
	}

	@Override
	public ItemStack[] getContents()
	{
		return items;
	}

	@Override
	public void onOpen(CraftHumanEntity who)
	{
	}

	@Override
	public void onClose(CraftHumanEntity who)
	{
	}

	@Override
	public List<HumanEntity> getViewers()
	{
		return new ArrayList<>();
	}

	@Override
	public InventoryHolder getOwner()
	{
		return null;
	}

	@Override
	public void setMaxStackSize(int size)
	{
	}

	public boolean onItemPickup(final ItemStack stack)
	{
		return attractionOn && PushStack(stack);
	}
}