package immibis.tubestuff;

import cpw.mods.fml.server.FMLBukkitHandler;
import forge.ForgeHooks;
import forge.ISidedInventory;
import immibis.core.TileBasicInventory;
import java.util.Iterator;
import java.util.logging.Level;
import net.minecraft.server.*;


public class TileCraftingTable extends TileBasicInventory implements ISidedInventory
{
	static final int SLOT_OUTPUT = 0;
	static final int START_RECIPE = 1;
	static final int SIZE_RECIPE = 9;
	static final int START_INPUT = 10;
	static final int SIZE_INPUT = 45;
	static final int START_OVERFLOW = 46;
	static final int SIZE_OVERFLOW = 9;
	static final int INVSIZE = 55;
	private CraftingRecipe cachedRecipe;
	private boolean invChanged = true;
	private boolean recipeChanged = true;
	private boolean outputFull = false;
	private boolean insufficientInput = false;
	private int pulse_ticks = 0;
	private static boolean allowCraftingHook = true;

	@Override
	public int getStartInventorySide(int var1)
	{
		switch (var1)
		{
		case 0:
			return 46;

		case 1:
			return 0;

		default:
			return 10 + 9 * (var1 - 2);
		}
	}

	@Override
	public int getSizeInventorySide(int var1)
	{
		return var1 == 1 ? (this.inv.contents[0] != null ? 1 : 0) : 9;
	}

	public TileCraftingTable()
	{
		super(55, "ACT Mk II");
	}

	private InventoryCrafting makeInventoryCrafting(int var1)
	{
		InventoryCraftingACT2 var2 = new InventoryCraftingACT2();

		for (int var3 = 0; var3 < 9; ++var3)
		{
			var2.setItem(var3, this.inv.contents[var3 + var1]);
		}

		return var2;
	}

	private void cacheOutput()
	{
		InventoryCrafting var1 = this.makeInventoryCrafting(1);
		Iterator var2 = CraftingManager.getInstance().getRecipies().iterator();
		CraftingRecipe var3;

		do
		{
			if (!var2.hasNext())
			{
				this.cachedRecipe = null;
				return;
			}

			var3 = (CraftingRecipe)var2.next();
		}
		while (!var3.a(var1));

		this.cachedRecipe = var3;
	}

	private void makeOutput()
	{
		if (this.inv.contents[0] != null && inv.contents[0].id != 0 && inv.contents[0].count != 0)
		{
			return;
		}

		if (this.cachedRecipe != null)
		{
			int[] craftCache = new int[9];
			int[] supplyCache = new int[45];

			for (int craftIndex = 0; craftIndex < craftCache.length; ++craftIndex)
			{
				ItemStack craftStack = this.inv.contents[craftIndex + 1];
				craftCache[craftIndex] = -1;

				if (craftStack != null)
				{
					for (int supplyIndex = 0; supplyIndex < 45; ++supplyIndex)
					{
						ItemStack supplyStack = this.inv.contents[supplyIndex + 10];

						if (supplyStack != null && supplyCache[supplyIndex] < supplyStack.count && mod_TubeStuff.areItemsEqual(craftStack, supplyStack))
						{
							craftCache[craftIndex] = supplyIndex;
							++supplyCache[supplyIndex];
							break;
						}
					}

					if (craftCache[craftIndex] == -1)
					{
						this.insufficientInput = true;
						return;
					}
				}
			}

			InventoryCraftingACT2 inventory = new InventoryCraftingACT2();

			for (int var8 = 0; var8 < 9; ++var8)
			{
				if (craftCache[var8] != -1)
				{
					inventory.setItem(var8, this.inv.contents[craftCache[var8] + 10]);
				}
			}

			if (!this.cachedRecipe.a(inventory))
			{
				this.insufficientInput = true;
			}
			else
			{
				for (int craftIndex = 0; craftIndex < craftCache.length; ++craftIndex)
				{
					if (craftCache[craftIndex] >= 0)
					{
						inventory.setItem(craftIndex, this.inv.splitStack(craftCache[craftIndex] + 10, 1));
					}
				}

				this.inv.contents[0] = this.cachedRecipe.b(inventory);
				this.decreaseInput(inventory, this.inv.contents[0]);

				for (int inventorySlot = 0; inventorySlot < 9; ++inventorySlot)
				{
					ItemStack inventoryStack = inventory.getItem(inventorySlot);

					if (inventoryStack != null)
					{
						this.inv.mergeStackIntoRange(inventoryStack, 46, 55);
					}
				}

				this.invChanged = true;
				this.update();
			}
		}
	}

	private void decreaseInput(InventoryCrafting var1, ItemStack var2)
	{
		if (allowCraftingHook)
		{
			try
			{
				FMLBukkitHandler.instance().onItemCrafted(mod_TubeStuff.fakePlayer(this.world), var2, var1);
				ForgeHooks.onTakenFromCrafting(mod_TubeStuff.fakePlayer(this.world), var2, var1);
				Item.byId[var2.id].d(var2, this.world, (EntityHuman)null);
			}
			catch (Exception var7)
			{
				allowCraftingHook = false;
				ModLoader.getLogger().log(Level.WARNING, "TubeStuff: This happened when trying to call a crafting hook with a null player. I won\'t try that again, but this may cause some bugs. If you can tell which mod caused the problem, bug its author to fix it.");
			}
		}

		for (int var3 = 0; var3 < var1.getSize(); ++var3)
		{
			ItemStack var4 = var1.getItem(var3);

			if (var4 != null)
			{
				var1.splitStack(var3, 1);

				if (var4.getItem().k())
				{
					ItemStack var5 = new ItemStack(var4.getItem().j());

					if (var1.getItem(var3) == null)
					{
						var1.setItem(var3, var5);
					}
					else
					{
						this.inv.mergeStackIntoRange(var5, 46, 55);
					}
				}
			}
		}
	}

	/**
	 * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner uses this to count
	 * ticks and creates a new spawn inside its implementation.
	 */
	@Override
	public void q_()
	{
		if (this.inv.contents[0] == null)
		{
			if (this.recipeChanged)
			{
				this.cacheOutput();
				this.recipeChanged = false;
			}

			if (this.cachedRecipe != null && !this.insufficientInput)
			{
				this.makeOutput();
			}
		}

		if (this.inv.contents[0] == null && this.redstone_output)
		{
			this.redstone_output = false;
			this.notifyNeighbouringBlocks();
		}

		if (this.invChanged)
		{
			this.invChanged = false;
			this.insufficientInput = false;
		}
	}

	@Override
	public boolean onBlockActivated(EntityHuman var1)
	{
		var1.openGui(mod_TubeStuff.instance, 1, this.world, this.x, this.y, this.z);
		return true;
	}

	private void slotChanging(int var1)
	{
		if (var1 == 0)
		{
			this.outputFull = false;
		}

		if (var1 >= 1 && var1 < 10)
		{
			this.recipeChanged = true;
		}

		this.invChanged = true;
		this.insufficientInput = false;
	}

	/**
	 * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
	 * stack.
	 *
	 * @param var1
	 * @param var2
	 * @return
	 */
	@Override
	public ItemStack splitStack(int var1, int var2)
	{
		this.slotChanging(var1);
		return super.splitStack(var1, var2);
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
	 *
	 * @param var1
	 * @param var2
	 */
	@Override
	public void setItem(int var1, ItemStack var2)
	{
		this.slotChanging(var1);
		super.setItem(var1, var2);
	}
}
