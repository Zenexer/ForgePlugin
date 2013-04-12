package ee;

import java.util.List;
import net.minecraft.server.*;


public final class ContainerTransmutation extends Container
{
	private EntityHuman player;
	private TransTabletData transGrid;
	private int latentEnergy;
	private int currentEnergy;
	private int learned;
	private int lock;
	private boolean initialized;

	public ContainerTransmutation(PlayerInventory playerInventory, EntityHuman player, TransTabletData transGrid)
	{
		this.player = player;
		setPlayer(player);
		this.transGrid = transGrid;
		learned = transGrid.learned;
		lock = (transGrid.isMatterLocked() ? 1 : transGrid.isFuelLocked() ? 2 : 0);
		latentEnergy = transGrid.getLatentEnergy();
		currentEnergy = transGrid.getCurrentEnergy();
		a(new SlotTransmuteInput(transGrid, 0, 43, 29));
		a(new SlotTransmuteInput(transGrid, 1, 34, 47));
		a(new SlotTransmuteInput(transGrid, 2, 52, 47));
		a(new SlotTransmuteInput(transGrid, 3, 16, 56));
		a(new SlotTransmuteInput(transGrid, 4, 70, 56));
		a(new SlotTransmuteInput(transGrid, 5, 34, 65));
		a(new SlotTransmuteInput(transGrid, 6, 52, 65));
		a(new SlotTransmuteInput(transGrid, 7, 43, 83));
		a(new SlotTransmuteInput(transGrid, 8, 158, 56));
		a(new SlotConsume(transGrid, 9, 107, 103));
		a(new SlotTransmute(transGrid, 10, 158, 15));
		a(new SlotTransmute(transGrid, 11, 140, 19));
		a(new SlotTransmute(transGrid, 12, 176, 19));
		a(new SlotTransmute(transGrid, 13, 123, 36));
		a(new SlotTransmute(transGrid, 14, 158, 37));
		a(new SlotTransmute(transGrid, 15, 193, 36));
		a(new SlotTransmute(transGrid, 16, 116, 56));
		a(new SlotTransmute(transGrid, 17, 139, 56));
		a(new SlotTransmute(transGrid, 18, 177, 56));
		a(new SlotTransmute(transGrid, 19, 199, 56));
		a(new SlotTransmute(transGrid, 20, 123, 76));
		a(new SlotTransmute(transGrid, 21, 158, 75));
		a(new SlotTransmute(transGrid, 22, 193, 76));
		a(new SlotTransmute(transGrid, 23, 140, 93));
		a(new SlotTransmute(transGrid, 24, 176, 93));
		a(new SlotTransmute(transGrid, 25, 158, 97));

		for (int var4 = 0; var4 < 3; var4++)
		{
			for (int var5 = 0; var5 < 9; var5++)
			{
				a(new Slot(player.inventory, var5 + var4 * 9 + 9, 35 + var5 * 18, 123 + var4 * 18));
			}
		}

		for (int var4 = 0; var4 < 9; var4++)
		{
			a(new Slot(player.inventory, var4, 35 + var4 * 18, 181));
		}

		EEBase.watchTransGrid(player);

		update();
	}

	@Override
	public IInventory getInventory()
	{
		return transGrid;
	}

	@Override
	public void setItem(int var1, ItemStack var2)
	{
		super.setItem(var1, var2);

		if (var1 < 26)
		{
			if (var2 == null)
			{
				transGrid.items[var1] = null;
			}
			else
			{
				transGrid.items[var1] = var2.cloneItemStack();
			}
		}

		update();
	}

	private void update()
	{
		if (!EEProxy.isClient(EEProxy.theWorld))
		{
			transGrid.update();
		}

		onUpdate();
	}

	private void onUpdate()
	{
		int newLock = transGrid.isMatterLocked() ? 1 : transGrid.isFuelLocked() ? 2 : 0;
		boolean handled = false;

		@SuppressWarnings("unchecked")
		final List<ICrafting>listeners = (List<ICrafting>)this.listeners;
		for (final ICrafting listener : listeners)
		{
			if ((latentEnergy != transGrid.latentEnergy) || (!initialized))
			{
				listener.setContainerData(this, 0, transGrid.latentEnergy & 0xFFFF);
				listener.setContainerData(this, 1, transGrid.latentEnergy >>> 16);
			}

			if ((currentEnergy != transGrid.currentEnergy) || (!initialized))
			{
				listener.setContainerData(this, 2, transGrid.currentEnergy & 0xFFFF);
				listener.setContainerData(this, 3, transGrid.currentEnergy >>> 16);
			}

			if ((learned != 0) || (!initialized))
			{
				listener.setContainerData(this, 4, transGrid.learned);
				listener.setContainerData(this, 4, 0);
			}

			if (lock != newLock || !initialized)
			{
				listener.setContainerData(this, 5, newLock);
			}

			handled = true;
		}

		latentEnergy = transGrid.latentEnergy;
		currentEnergy = transGrid.currentEnergy;
		learned = 0;
		lock = transGrid.isMatterLocked() ? 1 : transGrid.isFuelLocked() ? 2 : 0;

		if (!initialized && handled)
		{
			initialized = true;
		}

		if (handled && player instanceof EntityPlayer)
		{
			final EntityPlayer player = (EntityPlayer)this.player;
			player.updateInventory(this);
		}

		if (initialized)
		{
			a();
		}
	}

	@Override
	public void a()
	{
		if (!initialized)
		{
			update();
		}
		else
		{
			super.a();
		}
	}

	public void updateProgressBar(int var1, int var2)
	{
		if (var1 == 0)
		{
			transGrid.latentEnergy = (transGrid.latentEnergy & 0xFFFF_0000 | var2);
		}

		if (var1 == 1)
		{
			transGrid.latentEnergy = (transGrid.latentEnergy & 0xFFFF | var2 << 16);
		}

		if (var1 == 2)
		{
			transGrid.currentEnergy = (transGrid.currentEnergy & 0xFFFF_0000 | var2);
		}

		if (var1 == 3)
		{
			transGrid.currentEnergy = (transGrid.currentEnergy & 0xFFFF | var2 << 16);
		}

		if (var1 == 4)
		{
			transGrid.learned = var2;
		}

		if (var1 == 5)
		{
			if (var2 == 0)
			{
				transGrid.unlock();
			}

			if (var2 == 1)
			{
				transGrid.fuelUnlock();
				transGrid.matterLock();
			}

			if (var2 == 2)
			{
				transGrid.matterUnlock();
				transGrid.fuelLock();
			}
		}
	}

	@Override
	public boolean b(EntityHuman var1)
	{
		return true;
	}

	@Override
	public void a(EntityHuman var1)
	{
		super.a(var1);
		EEBase.closeTransGrid(player);

		if (!player.world.isStatic)
		{
			for (int var2 = 0; var2 < 25; var2++)
			{
				ItemStack var3 = transGrid.splitWithoutUpdate(var2);

				if (var3 != null)
				{
					player.drop(var3);
				}
			}
		}

		update();
	}

	@Override
	public ItemStack a(int var1)
	{
		try
		{
			ItemStack var2 = null;
			Slot var3 = (Slot)e.get(var1);
			ItemStack var4 = null;

			if ((var1 > 9) && (var1 < 26) && (var3 != null) && (var3.c()))
			{
				var4 = var3.getItem().cloneItemStack();
			}

			if ((var3 != null) && (var3.c()))
			{
				ItemStack var5 = var3.getItem();
				var2 = var5.cloneItemStack();

				if (var1 <= 8)
				{
					if (!a(var5, 26, 62, true))
					{
						var3.set(null);
					}
				}
				else if ((var1 > 9) && (var1 < 26))
				{
					if (!grabResult(var5, (Slot)e.get(var1), 26, 62, false))
					{
						var3.set(null);
					}
				}
				else if ((var1 >= 26) && (var1 < 62))
				{
					if (((EEMaps.getEMC(var5) > 0) || (EEBase.isKleinStar(var5.id))) && (!a(var5, 0, 8, false)))
					{
						if (var5.count == 0)
						{
							var3.set(null);
						}

						return null;
					}
				}
				else if (!a(var5, 26, 62, false))
				{
					if (var5.count == 0)
					{
						var3.set(null);
					}

					return null;
				}

				if (var5.count == 0)
				{
					if ((var1 > 9) && (var1 < 26))
					{
						var5.count = 1;
					}
					else
					{
						var3.set(null);
					}
				}
				else
				{
					var3.d();
				}

				if (var5.count == var2.count)
				{
					if ((var1 > 9) && (var1 < 26) && (var4 != null))
					{
						return var4;
					}

					return null;
				}

				if ((var1 > 9) && (var1 < 26) && (transGrid.latentEnergy + transGrid.currentEnergy < EEMaps.getEMC(var5)))
				{
					return null;
				}

				var3.c(var5);
			}

			if ((var4 != null) && (var1 > 9) && (var1 < 26))
			{
				var3.set(var4);
			}

			return var2;
		}
		finally
		{
			update();
		}
	}

	private boolean grabResult(ItemStack var1, Slot var2, int var3, int var4, boolean var5)
	{
		if (transGrid.latentEnergy + transGrid.currentEnergy < EEMaps.getEMC(var1))
		{
			return false;
		}

		var2.c(var1);
		boolean var6 = false;
		int var7 = var3;

		if (var5)
		{
			var7 = var4 - 1;
		}

		if (var1.isStackable())
		{
			while ((var1.count > 0) && (((!var5) && (var7 < var4)) || ((var5) && (var7 >= var3))))
			{
				Slot var8 = (Slot)e.get(var7);
				ItemStack var9 = var8.getItem();

				if ((var9 != null) && (var9.id == var1.id) && ((!var1.usesData()) || (var1.getData() == var9.getData())) && (ItemStack.equals(var1, var9)))
				{
					int var10 = var9.count + var1.count;

					if (var10 <= var1.getMaxStackSize())
					{
						var1.count = 0;
						var9.count = var10;
						var8.d();
						var6 = true;
					}
					else if (var9.count < var1.getMaxStackSize())
					{
						var1.count -= var1.getMaxStackSize() - var9.count;
						var9.count = var1.getMaxStackSize();
						var8.d();
						var6 = true;
					}
				}

				if (var5)
				{
					var7--;
				}
				else
				{
					var7++;
				}
			}
		}

		if (var1.count > 0)
		{
			int var11;
			if (var5)
			{
				var11 = var4 - 1;
			}
			else
			{
				var11 = var3;
			}

			while (((!var5) && (var11 < var4)) || ((var5) && (var11 >= var3)))
			{
				Slot var12 = (Slot)e.get(var11);
				ItemStack var13 = var12.getItem();

				if (var13 == null)
				{
					var12.set(var1.cloneItemStack());
					var12.d();
					var1.count = 0;
					var6 = true;
					break;
				}

				if (var5)
				{
					var11--;
				}
				else
				{
					var11++;
				}
			}
		}

		var1.count = 1;
		return var6;
	}

	@Override
	public ItemStack clickItem(int slotIndex, int var2, boolean var3, EntityHuman var4)
	{
		try
		{
			ItemStack var5 = null;

			if (var2 > 1)
			{
				return null;
			}

			if ((var2 == 0) || (var2 == 1))
			{
				PlayerInventory player = var4.inventory;

				if (slotIndex == -999)
				{
					if ((player.getCarried() != null) && (slotIndex == -999))
					{
						if (var2 == 0)
						{
							var4.drop(player.getCarried());
							player.setCarried(null);
						}

						if (var2 == 1)
						{
							var4.drop(player.getCarried().a(1));

							if (player.getCarried().count == 0)
							{
								player.setCarried(null);
							}
						}
					}
				}
				else if (var3)
				{
					ItemStack var7 = a(slotIndex);

					if (var7 != null)
					{
						int var8 = var7.id;
						var5 = var7.cloneItemStack();
						Slot var9 = (Slot)e.get(slotIndex);

						if ((var9 != null) && (var9.getItem() != null) && (var9.getItem().id == var8) && (var9.getItem().isStackable()))
						{
							retrySlotClick(slotIndex, var2, 1, var9.getItem().getMaxStackSize(), var3, var4);
						}
					}
				}
				else
				{
					if (slotIndex < 0)
					{
						return null;
					}

					Slot slot = (Slot)e.get(slotIndex);

					if (slot != null)
					{
						slot.d();
						ItemStack var13 = slot.getItem();
						ItemStack var14 = player.getCarried();

						if (var13 != null)
						{
							var5 = var13.cloneItemStack();
						}

						if (var13 == null)
						{
							if ((var14 != null) && (slot.isAllowed(var14)))
							{
								int var10 = var2 != 0 ? 1 : var14.count;

								if (var10 > slot.a())
								{
									var10 = slot.a();
								}

								slot.set(var14.a(var10));

								if (var14.count == 0)
								{
									player.setCarried(null);
								}
							}
						}
						else if (var14 == null)
						{
							int count = var2 != 0 ? (var13.count + 1) / 2 : var13.count;
							ItemStack slotStack = slot.a(count);
							player.setCarried(slotStack);

							if (slotIndex >= 10 && slotIndex <= 25)
							{
								slot.set(new ItemStack(slotStack.id, 1, slotStack.getData()));
							}
							else if (var13.count == 0)
							{
								slot.set(null);
							}

							slot.c(player.getCarried());
						}
						else if (slot.isAllowed(var14))
						{
							if ((var13.id == var14.id) && ((!var13.usesData()) || (var13.getData() == var14.getData())) && (ItemStack.equals(var13, var14)))
							{
								int var10 = var2 != 0 ? 1 : var14.count;

								if (var10 > slot.a() - var13.count)
								{
									var10 = slot.a() - var13.count;
								}

								if (var10 > var14.getMaxStackSize() - var13.count)
								{
									var10 = var14.getMaxStackSize() - var13.count;
								}

								var14.a(var10);

								if (var14.count == 0)
								{
									player.setCarried(null);
								}

								var13.count += var10;
							}
							else if (var14.count <= slot.a())
							{
								slot.set(var14);
								player.setCarried(var13);
							}
						}
						else if ((var13.id == var14.id) && (var14.getMaxStackSize() > 1) && ((!var13.usesData()) || (var13.getData() == var14.getData())) && (ItemStack.equals(var13, var14)))
						{
							int var10 = var13.count;

							if ((var10 > 0) && (var10 + var14.count <= var14.getMaxStackSize()))
							{
								var14.count += var10;

								if ((slotIndex < 10) || (slotIndex > 25))
								{
									var13.a(var10);

									if (var13.count == 0)
									{
										slot.set(null);
									}
								}

								slot.c(player.getCarried());
							}
						}
					}
				}
			}

			return var5;
		}
		finally
		{
			update();
		}
	}

	private void retrySlotClick(int var1, int var2, int var3, int var4, boolean var5, EntityHuman var6)
	{
		if (var3 < var4)
		{
			slotClick(var1, var2, var3 + 1, var4, var5, var6);
		}
	}

	public ItemStack slotClick(int var1, int var2, int var3, int var4, boolean var5, EntityHuman var6)
	{
		try
		{
			ItemStack var7 = null;

			if (var2 > 1)
			{
				return null;
			}

			if ((var2 == 0) || (var2 == 1))
			{
				PlayerInventory var8 = var6.inventory;

				if (var1 == -999)
				{
					if ((var8.getCarried() != null) && (var1 == -999))
					{
						if (var2 == 0)
						{
							var6.drop(var8.getCarried());
							var8.setCarried(null);
						}

						if (var2 == 1)
						{
							var6.drop(var8.getCarried().a(1));

							if (var8.getCarried().count == 0)
							{
								var8.setCarried(null);
							}
						}
					}
				}
				else if (var5)
				{
					ItemStack var9 = a(var1);

					if (var9 != null)
					{
						int var10 = var9.id;
						var7 = var9.cloneItemStack();
						Slot var11 = (Slot)e.get(var1);

						if ((var11 != null) && (var11.getItem() != null) && (var11.getItem().id == var10))
						{
							retrySlotClick(var1, var2, var3, var4, var5, var6);
						}
					}
				}
				else
				{
					if (var1 < 0)
					{
						return null;
					}

					Slot var14 = (Slot)e.get(var1);

					if (var14 != null)
					{
						var14.d();
						ItemStack var16 = var14.getItem();
						ItemStack var15 = var8.getCarried();

						if (var16 != null)
						{
							var7 = var16.cloneItemStack();
						}

						if (var16 == null)
						{
							if ((var15 != null) && (var14.isAllowed(var15)))
							{
								int var12 = var2 != 0 ? 1 : var15.count;

								if (var12 > var14.a())
								{
									var12 = var14.a();
								}

								var14.set(var15.a(var12));

								if (var15.count == 0)
								{
									var8.setCarried(null);
								}
							}
						}
						else if (var15 == null)
						{
							int var12 = var2 != 0 ? (var16.count + 1) / 2 : var16.count;
							ItemStack var13 = var14.a(var12);
							var8.setCarried(var13);

							if ((var1 >= 10) && (var1 <= 25))
							{
								var14.set(new ItemStack(var13.id, 1, var13.getData()));
							}
							else if (var16.count == 0)
							{
								var14.set(null);
							}

							var14.c(var8.getCarried());
						}
						else if (var14.isAllowed(var15))
						{
							if ((var16.id == var15.id) && ((!var16.usesData()) || (var16.getData() == var15.getData())) && (ItemStack.equals(var16, var15)))
							{
								int var12 = var2 != 0 ? 1 : var15.count;

								if (var12 > var14.a() - var16.count)
								{
									var12 = var14.a() - var16.count;
								}

								if (var12 > var15.getMaxStackSize() - var16.count)
								{
									var12 = var15.getMaxStackSize() - var16.count;
								}

								var15.a(var12);

								if (var15.count == 0)
								{
									var8.setCarried(null);
								}

								var16.count += var12;
							}
							else if (var15.count <= var14.a())
							{
								var14.set(var15);
								var8.setCarried(var16);
							}
						}
						else if ((var16.id == var15.id) && (var15.getMaxStackSize() > 1) && ((!var16.usesData()) || (var16.getData() == var15.getData())) && (ItemStack.equals(var16, var15)))
						{
							int var12 = var16.count;

							if ((var12 > 0) && (var12 + var15.count <= var15.getMaxStackSize()))
							{
								var15.count += var12;

								if ((var1 < 10) || (var1 > 25))
								{
									var16.a(var12);

									if (var16.count == 0)
									{
										var14.set(null);
									}
								}

								var14.c(var8.getCarried());
							}
						}
					}
				}
			}

			return var7;
		}
		finally
		{
			update();
		}
	}
}