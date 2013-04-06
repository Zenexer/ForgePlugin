package ee;

import net.minecraft.server.*;


public class ContainerRMFurnace extends Container
{
	private TileRMFurnace furnace;
	private int cookTime = 0;
	private int burnTime = 0;
	private int itemBurnTime = 0;
	private boolean initialized;

	public ContainerRMFurnace(IInventory var1, TileRMFurnace var2)
	{
		this.furnace = var2;
		setPlayer(((PlayerInventory)var1).player);
		a(new Slot(var2, 0, 65, 53));
		a(new Slot(var2, 1, 65, 17));

		for (int var3 = 0; var3 <= 2; var3++)
		{
			for (int var4 = 0; var4 <= 3; var4++)
			{
				a(new Slot(var2, var3 * 4 + var4 + 2, 11 + var3 * 18, 8 + var4 * 18));
			}
		}

		a(new Slot(var2, 14, 125, 35));

		for (int var3 = 0; var3 <= 2; var3++)
		{
			for (int var4 = 0; var4 <= 3; var4++)
			{
				a(new Slot(var2, var3 * 4 + var4 + 15, 147 + var3 * 18, 8 + var4 * 18));
			}
		}

		for (int var3 = 0; var3 < 3; var3++)
		{
			for (int var4 = 0; var4 < 9; var4++)
			{
				a(new Slot(var1, var4 + var3 * 9 + 9, 24 + var4 * 18, 84 + var3 * 18));
			}
		}

		for (int var3 = 0; var3 < 9; var3++)
		{
			a(new Slot(var1, var3, 24 + var3 * 18, 142));
		}
	}

	@Override
	protected boolean a(ItemStack stack, int currentSlot, int endSlot, boolean reverseDirection)
	{
		if (stack == null)
		{
			return false;
		}

		final int max = stack.getMaxStackSize();
		final boolean isFuel = furnace.getItemBurnTime(stack, true) > 0 && stack.id != Block.LOG.id;
		final boolean isSmeltable = FurnaceRecipes.getInstance().getSmeltingResult(stack) != null;
		
		for (int slot = isFuel ? 0 : 1; slot <= (isSmeltable ? 13 : 0); slot++)
		{
			final ItemStack slotStack = furnace.getItem(slot);

			if (slotStack == null)
			{
				return true;
			}

			if (slotStack.doMaterialsMatch(stack) && slotStack.count < max)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public IInventory getInventory()
	{
		return this.furnace;
	}

	@Override
	public void a()
	{
		super.a();

		for (int var1 = 0; var1 < this.listeners.size(); var1++)
		{
			ICrafting var2 = (ICrafting)this.listeners.get(var1);

			if ((this.cookTime != this.furnace.furnaceCookTime) || (!this.initialized))
			{
				var2.setContainerData(this, 0, this.furnace.furnaceCookTime);
			}

			if ((this.burnTime != this.furnace.furnaceBurnTime) || (!this.initialized))
			{
				var2.setContainerData(this, 1, this.furnace.furnaceBurnTime);
			}

			if ((this.itemBurnTime != this.furnace.currentItemBurnTime) || (!this.initialized))
			{
				var2.setContainerData(this, 2, this.furnace.currentItemBurnTime);
			}
		}

		this.cookTime = this.furnace.furnaceCookTime;
		this.burnTime = this.furnace.furnaceBurnTime;
		this.itemBurnTime = this.furnace.currentItemBurnTime;
		this.initialized = true;
	}

	public void updateProgressBar(int var1, int var2)
	{
		if (var1 == 0)
		{
			this.furnace.furnaceCookTime = var2;
		}

		if (var1 == 1)
		{
			this.furnace.furnaceBurnTime = var2;
		}

		if (var1 == 2)
		{
			this.furnace.currentItemBurnTime = var2;
		}
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

			if ((var1 >= 14) && (var1 <= 26))
			{
				if (!a(var4, 27, 62, true))
				{
					if (var4.count == 0)
					{
						var3.set(null);
					}

					return null;
				}
			}
			else if ((var1 >= 27) && (var1 < 53))
			{
				if (this.furnace.getItemBurnTime(var4, true) > 0)
				{
					if (!a(var4, 0, 0, true))
					{
						if (var4.count == 0)
						{
							var3.set(null);
						}

						return null;
					}
				}
				else
				{
					if (!a(var4, 1, 13, false))
					{
						if (var4.count == 0)
						{
							var3.set(null);
						}

						return null;
					}

					if (!a(var4, 53, 62, false))
					{
						if (var4.count == 0)
						{
							var3.set(null);
						}

						return null;
					}
				}
			}
			else if ((var1 >= 54) && (var1 < 63))
			{
				if (this.furnace.getItemBurnTime(var4, true) > 0)
				{
					if (!a(var4, 0, 0, true))
					{
						if (var4.count == 0)
						{
							var3.set(null);
						}

						return null;
					}
				}
				else
				{
					if (!a(var4, 1, 13, false))
					{
						if (var4.count == 0)
						{
							var3.set(null);
						}

						return null;
					}

					if (!a(var4, 27, 53, false))
					{
						if (var4.count == 0)
						{
							var3.set(null);
						}

						return null;
					}
				}
			}
			else if (!a(var4, 27, 62, false))
			{
				if (var4.count == 0)
				{
					var3.set(null);
				}

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
	public void addSlotListener(ICrafting icrafting)
	{
		super.addSlotListener(icrafting);
		icrafting.setContainerData(this, 0, this.furnace.furnaceCookTime);
		icrafting.setContainerData(this, 1, this.furnace.furnaceBurnTime);
		icrafting.setContainerData(this, 2, this.furnace.currentItemBurnTime);
	}

	@Override
	public boolean b(EntityHuman var1)
	{
		return this.furnace.a(var1);
	}
}