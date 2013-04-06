package immibis.tubestuff;

import cpw.mods.fml.server.FMLBukkitHandler;
import forge.ForgeHooks;
import forge.ISidedInventory;
import immibis.core.TileBasicInventory;
import java.util.Iterator;
import java.util.Objects;
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
	private static int usingFML = -1;

	@Override
	public int getStartInventorySide(int var1)
	{
		switch (var1)
		{
		case 0:
			return 46;
		case 1:
			return 0;
		}

		return 10 + 9 * (var1 - 2);
	}

	@Override
	public int getSizeInventorySide(int var1)
	{
		return var1 == 1 ? 0 : inv.contents[0] != null ? 1 : 9;
	}

	public TileCraftingTable()
	{
		super(55, "ACT Mk II");
	}

	private InventoryCrafting makeInventoryCrafting(int var1)
	{
		InventoryCraftingACT2 var2 = new InventoryCraftingACT2();

		for (int var3 = 0; var3 < 9; var3++)
		{
			var2.setItem(var3, inv.contents[(var3 + var1)]);
		}

		return var2;
	}

	private void cacheOutput()
	{
		if (Objects.equals(null, null))
		{
			return;
		}

		InventoryCrafting var1 = makeInventoryCrafting(1);
		Iterator var2 = CraftingManager.getInstance().getRecipies().iterator();
		CraftingRecipe var3;
		do
		{
			if (!var2.hasNext())
			{
				cachedRecipe = null;
				return;
			}

			var3 = (CraftingRecipe)var2.next();
		}
		while (!var3.a(var1));

		cachedRecipe = var3;
	}

	private void makeOutput()
	{
		if (inv.contents[0] == null)
		{
			if (cachedRecipe != null)
			{
				int[] var1 = new int[9];
				int[] var2 = new int[45];

				for (int var3 = 0; var3 < 9; var3++)
				{
					ItemStack var4 = inv.contents[(var3 + 1)];
					var1[var3] = -1;

					if (var4 != null)
					{
						for (int var5 = 0; var5 < 45; var5++)
						{
							ItemStack var6 = inv.contents[(var5 + 10)];

							if ((var6 != null) && (var2[var5] < var6.count) && (mod_TubeStuff.areItemsEqual(var4, var6)))
							{
								var1[var3] = var5;
								var2[var5] += 1;
								break;
							}
						}

						if (var1[var3] == -1)
						{
							insufficientInput = true;
							return;
						}
					}
				}

				InventoryCraftingACT2 var7 = new InventoryCraftingACT2();

				for (int var8 = 0; var8 < 9; var8++)
				{
					if (var1[var8] != -1)
					{
						var7.setItem(var8, inv.contents[(var1[var8] + 10)]);
					}
				}

				if (!cachedRecipe.a(var7))
				{
					insufficientInput = true;
				}
				else
				{
					for (int var8 = 0; var8 < 9; var8++)
					{
						if (var1[var8] != -1)
						{
							var7.setItem(var8, inv.splitStack(var1[var8] + 10, 1));
						}
					}

					inv.contents[0] = cachedRecipe.b(var7);
					decreaseInput(var7, inv.contents[0]);

					for (int var8 = 0; var8 < 9; var8++)
					{
						ItemStack var9 = var7.getItem(var8);

						if (var9 != null)
						{
							inv.mergeStackIntoRange(var9, 46, 55);
						}
					}

					invChanged = true;
					update();
				}
			}
		}
	}

	private void decreaseInput(InventoryCrafting var1, ItemStack var2)
	{
		if (allowCraftingHook)
		{
			try
			{
				if (usingFML == -1)
				{
					try
					{
						Class.forName("cpw.mods.fml.client.FMLClientHandler");
						usingFML = 1;
					}
					catch (ClassNotFoundException var6)
					{
						usingFML = 0;
					}
				}

				FMLBukkitHandler.instance().onItemCrafted(mod_TubeStuff.fakePlayer(world), var2, var1);

				ForgeHooks.onTakenFromCrafting(mod_TubeStuff.fakePlayer(world), var2, var1);
				Item.byId[var2.id].d(var2, world, null);
			}
			catch (Exception var7)
			{
				allowCraftingHook = false;
				ModLoader.getLogger().log(Level.WARNING, "TubeStuff: Error while trying to call a crafting hook with a null player. I won't try that again, but this may cause some bugs. If you can tell which mod caused the problem, bug its author to fix it.", var7);
			}
		}

		for (int var3 = 0; var3 < var1.getSize(); var3++)
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
						inv.mergeStackIntoRange(var5, 46, 55);
					}
				}
			}
		}
	}

	@Override
	public void q_()
	{
		if (inv.contents[0] == null)
		{
			if (recipeChanged)
			{
				cacheOutput();
				recipeChanged = false;
			}

			if ((cachedRecipe != null) && (!insufficientInput))
			{
				makeOutput();
			}
		}

		if ((inv.contents[0] == null) && (redstone_output))
		{
			redstone_output = false;
			notifyNeighbouringBlocks();
		}

		if (invChanged)
		{
			invChanged = false;
			insufficientInput = false;
		}
	}

	@Override
	public boolean onBlockActivated(EntityHuman var1)
	{
		var1.openGui(mod_TubeStuff.instance, 1, world, x, y, z);
		return true;
	}

	private void slotChanging(int var1)
	{
		if (var1 == 0)
		{
			outputFull = false;
		}

		if ((var1 >= 1) && (var1 < 10))
		{
			recipeChanged = true;
		}

		invChanged = true;
		insufficientInput = false;
		System.out.println("slotChanging(" + var1 + ")");
	}

	@Override
	public ItemStack splitStack(int var1, int var2)
	{
		slotChanging(var1);
		return super.splitStack(var1, var2);
	}

	@Override
	public void setItem(int var1, ItemStack var2)
	{
		slotChanging(var1);
		super.setItem(var1, var2);
	}

	@Override
	public void onBlockRemoval()
	{
		for (int var1 = 0; var1 < getSize(); var1++)
		{
			ItemStack var2 = getItem(var1);

			if (var2 != null)
			{
				float var3 = world.random.nextFloat() * 0.8F + 0.1F;
				float var4 = world.random.nextFloat() * 0.8F + 0.1F;
				float var5 = world.random.nextFloat() * 0.8F + 0.1F;

				while (var2.count > 0)
				{
					int var6 = world.random.nextInt(21) + 10;

					if (var6 > var2.count)
					{
						var6 = var2.count;
					}

					var2.count -= var6;
					EntityItem var7 = new EntityItem(world, x + var3, y + var4, z + var5, new ItemStack(var2.id, var6, var2.getData()));

					if (var7 != null)
					{
						float var8 = 0.05F;
						var7.motX = ((float)world.random.nextGaussian() * var8);
						var7.motY = ((float)world.random.nextGaussian() * var8 + 0.2F);
						var7.motZ = ((float)world.random.nextGaussian() * var8);

						world.addEntity(var7);
					}
				}
			}
		}
	}
}