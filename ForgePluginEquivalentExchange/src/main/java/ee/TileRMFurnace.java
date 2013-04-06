package ee;

import buildcraft.api.ISpecialInventory;
import buildcraft.api.Orientations;
import ee.core.GuiIds;
import forge.ISidedInventory;
import java.util.Random;
import net.minecraft.server.*;


public class TileRMFurnace extends TileEE
	implements ISpecialInventory, ISidedInventory, IEEPowerNet
{
	private ItemStack[] items = new ItemStack[27];
	public int furnaceBurnTime = 0;
	public int currentItemBurnTime = 0;
	public int furnaceCookTime = 0;
	public int nextinstack;
	public int nextoutstack;
	private float woftFactor = 1.0F;

	private boolean isChest(TileEntity var1)
	{
		return ((var1 instanceof TileEntityChest)) || ((var1 instanceof TileAlchChest));
	}

	@Override
	public void onBlockRemoval()
	{
		for (int var1 = 0; var1 < getSize(); var1++)
		{
			ItemStack var2 = getItem(var1);

			if (var2 != null)
			{
				float var3 = this.world.random.nextFloat() * 0.8F + 0.1F;
				float var4 = this.world.random.nextFloat() * 0.8F + 0.1F;
				float var5 = this.world.random.nextFloat() * 0.8F + 0.1F;

				while (var2.count > 0)
				{
					int var6 = this.world.random.nextInt(21) + 10;

					if (var6 > var2.count)
					{
						var6 = var2.count;
					}

					var2.count -= var6;
					EntityItem var7 = new EntityItem(this.world, this.x + var3, this.y + var4, this.z + var5, new ItemStack(var2.id, var6, var2.getData()));

					if (var7 != null)
					{
						float var8 = 0.05F;
						var7.motX = ((float)this.world.random.nextGaussian() * var8);
						var7.motY = ((float)this.world.random.nextGaussian() * var8 + 0.2F);
						var7.motZ = ((float)this.world.random.nextGaussian() * var8);

						if ((var7.itemStack.getItem() instanceof ItemKleinStar))
						{
							((ItemKleinStar)var7.itemStack.getItem()).setKleinPoints(var7.itemStack, ((ItemKleinStar)var2.getItem()).getKleinPoints(var2));
						}

						this.world.addEntity(var7);
					}
				}
			}
		}
	}

	@Override
	public int getSize()
	{
		return this.items.length;
	}

	@Override
	public int getMaxStackSize()
	{
		return 64;
	}

	@Override
	public ItemStack getItem(int var1)
	{
		return this.items[var1];
	}

	@Override
	public ItemStack splitStack(int var1, int var2)
	{
		if (this.items[var1] != null)
		{
			if (this.items[var1].count <= var2)
			{
				ItemStack var3 = this.items[var1];
				this.items[var1] = null;
				return var3;
			}

			ItemStack var3 = this.items[var1].a(var2);

			if (this.items[var1].count == 0)
			{
				this.items[var1] = null;
			}

			return var3;
		}

		return null;
	}

	@Override
	public void setItem(int var1, ItemStack var2)
	{
		this.items[var1] = var2;

		if ((var2 != null) && (var2.count > getMaxStackSize()))
		{
			var2.count = getMaxStackSize();
		}
	}

	@Override
	public boolean addItem(ItemStack stack, boolean modify, Orientations side)
	{
		if (stack == null)
		{
			return false;
		}

		final int max = stack.getMaxStackSize();
		final boolean isFuel = getItemBurnTime(stack, true) > 0 && stack.id != Block.LOG.id;
		final boolean isSmeltable = FurnaceRecipes.getInstance().getSmeltingResult(stack) != null;
		
		for (int slot = isFuel ? 0 : 1; slot <= (isSmeltable ? 13 : 0); slot++)
		{
			if (this.items[slot] == null)
			{
				if (modify)
				{
					items[slot] = stack.cloneItemStack();
					stack.count = 0;
				}

				return true;
			}

			if (this.items[slot].doMaterialsMatch(stack) && this.items[slot].count < max)
			{
				if (modify)
				{
					final int free = max - items[slot].count;
					if (free >= stack.count)
					{
						stack.count = 0;
						items[slot].count = max;
						return true;
					}

					stack.count -= free;
					items[slot].count = max;
				}
				else
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public ItemStack extractItem(boolean var1, Orientations var2)
	{
		switch (var2)
		{
		case XNeg:
			if (this.items[0] == null)
			{
				return null;
			}
			if ((this.items[0].getItem() instanceof ItemKleinStar))
			{
				ItemStack var5 = this.items[0].cloneItemStack();

				if (var1)
				{
					this.items[0] = null;
				}

				return var5;
			}

		case Unknown:
		case XPos:
		case YNeg:
		case YPos:
		case ZNeg:
		case ZPos:
			for (int var3 = 10; var3 < this.items.length; var3++)
			{
				if (this.items[var3] != null)
				{
					ItemStack var4 = this.items[var3].cloneItemStack();
					var4.count = 1;

					if (var1)
					{
						this.items[var3].count -= 1;

						if (this.items[var3].count < 1)
						{
							this.items[var3] = null;
						}
					}

					return var4;
				}
			}
		}

		return null;
	}

	@Override
	public String getName()
	{
		return "RM Furnace";
	}

	@Override
	public void a(NBTTagCompound var1)
	{
		super.a(var1);
		NBTTagList var2 = var1.getList("Items");
		this.items = new ItemStack[getSize()];

		for (int var3 = 0; var3 < var2.size(); var3++)
		{
			NBTTagCompound var4 = (NBTTagCompound)var2.get(var3);
			byte var5 = var4.getByte("Slot");

			if ((var5 >= 0) && (var5 < this.items.length))
			{
				this.items[var5] = ItemStack.a(var4);
			}
		}

		this.woftFactor = var1.getFloat("TimeFactor");
		this.furnaceBurnTime = var1.getInt("BurnTime");
		this.furnaceCookTime = var1.getShort("CookTime");
		this.currentItemBurnTime = getItemBurnTime(this.items[1], false);
	}

	@Override
	public void b(NBTTagCompound var1)
	{
		super.b(var1);
		var1.setInt("BurnTime", this.furnaceBurnTime);
		var1.setShort("CookTime", (short)this.furnaceCookTime);
		var1.setFloat("TimeFactor", this.woftFactor);
		NBTTagList var2 = new NBTTagList();

		for (int var3 = 0; var3 < this.items.length; var3++)
		{
			if (this.items[var3] != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte)var3);
				this.items[var3].save(var4);
				var2.add(var4);
			}
		}

		var1.set("Items", var2);
	}

	public int getCookProgressScaled(int var1)
	{
		return (this.world != null) && (!EEProxy.isClient(this.world)) ? (this.furnaceCookTime + ((isBurning()) && (canSmelt()) ? 1 : 0)) * var1 / 3 : 0;
	}

	public int getBurnTimeRemainingScaled(int var1)
	{
		if (this.currentItemBurnTime == 0)
		{
			this.currentItemBurnTime = 10;
		}

		return this.furnaceBurnTime * var1 / this.currentItemBurnTime;
	}

	public boolean isBurning()
	{
		return this.furnaceBurnTime > 0;
	}

	@Override
	public void q_()
	{
		if (!clientFail())
		{
			this.woftFactor = (EEBase.getPedestalFactor(this.world) * EEBase.getPlayerWatchFactor());
			boolean var1 = this.furnaceBurnTime > 0;
			boolean var2 = false;
			boolean var3 = false;

			if (this.furnaceBurnTime > 0)
			{
				this.furnaceBurnTime = ((int)(this.furnaceBurnTime - (getWOFTReciprocal(this.woftFactor) >= 1.0F ? getWOFTReciprocal(this.woftFactor) : 1.0F)));

				if (this.furnaceBurnTime <= 0)
				{
					this.furnaceBurnTime = 0;
					var3 = true;
				}
			}

			if (!this.world.isStatic)
			{
				if ((this.furnaceBurnTime <= 0) && (canSmelt()))
				{
					this.currentItemBurnTime = (this.furnaceBurnTime = getItemBurnTime(this.items[0], false) / 48);

					if (this.furnaceBurnTime > 0)
					{
						var2 = true;
						var3 = true;

						if ((this.items[0] != null) && (!EEBase.isKleinStar(this.items[0].id)))
						{
							if (this.items[0].getItem().k())
							{
								this.items[0] = new ItemStack(this.items[0].getItem().j());
							}
							else
							{
								this.items[0].count -= 1;
							}

							if (this.items[0].count == 0)
							{
								this.items[0] = null;
							}
						}
					}
				}

				if ((isBurning()) && (canSmelt()))
				{
					this.furnaceCookTime = ((int)(this.furnaceCookTime + (getWOFTReciprocal(this.woftFactor) >= 1.0F ? getWOFTReciprocal(this.woftFactor) : 1.0F)));
					do
					{
						this.furnaceCookTime -= 3;
						smeltItem();
						var2 = true;

						var3 = true;
						if (this.furnaceCookTime < 3)
						{
							break;
						}
					}
					while (canSmelt());
				}
				else
				{
					for (int var4 = 15; var4 < 27; var4++)
					{
						if ((this.items[var4] != null) && (this.items[var4].count >= this.items[var4].getMaxStackSize()) && (tryDropInChest(new ItemStack(this.items[var4].getItem(), this.items[var4].count))))
						{
							this.items[var4] = null;
						}
					}

					this.furnaceCookTime = 0;
					this.furnaceBurnTime = 0;
				}
			}

			if (var2)
			{
				update();
			}

			if (var3)
			{
				this.world.notify(this.x, this.y, this.z);
			}
		}
	}

	private boolean canSmelt()
	{
		if (this.items[1] == null)
		{
			for (int var1 = 2; var1 <= 13; var1++)
			{
				if (this.items[var1] != null)
				{
					this.items[1] = this.items[var1].cloneItemStack();
					this.items[var1] = null;
					break;
				}
			}

			if (this.items[1] == null)
			{
				return false;
			}
		}

		ItemStack var3 = FurnaceRecipes.getInstance().getSmeltingResult(this.items[1]);

		if (var3 == null)
		{
			return false;
		}
		if (this.items[14] == null)
		{
			return true;
		}

		if (!this.items[14].doMaterialsMatch(var3))
		{
			if (tryDropInChest(this.items[14].cloneItemStack()))
			{
				this.items[14] = null;
				return true;
			}

			for (int var2 = 15; var2 <= 26; var2++)
			{
				if (this.items[var2] == null)
				{
					this.items[var2] = this.items[14].cloneItemStack();
					this.items[14] = null;
					return true;
				}

				if (this.items[var2].doMaterialsMatch(this.items[14]))
				{
					while ((this.items[14] != null) && (this.items[var2].count < 64))
					{
						this.items[14].count -= 1;
						this.items[var2].count += 1;

						if (this.items[14].count == 0)
						{
							this.items[14] = null;
							return true;
						}
					}
				}
			}
		}

		if ((this.items[14].count < getMaxStackSize()) && (this.items[14].count < this.items[14].getMaxStackSize()))
		{
			return true;
		}

		for (int var2 = 15; var2 < 27; var2++)
		{
			if ((this.items[var2] != null) && (this.items[var2].count >= this.items[var2].getMaxStackSize()) && (tryDropInChest(this.items[var2].cloneItemStack())))
			{
				this.items[var2] = null;
			}
		}

		if (this.items[14] == null)
		{
			return true;
		}

		for (int var2 = 15; var2 <= 26; var2++)
		{
			if (this.items[var2] == null)
			{
				this.items[var2] = this.items[14].cloneItemStack();
				this.items[14] = null;
				return true;
			}

			if (this.items[var2].doMaterialsMatch(this.items[14]))
			{
				while ((this.items[14] != null) && (this.items[var2].count < 64))
				{
					this.items[14].count -= 1;
					this.items[var2].count += 1;

					if (this.items[14].count == 0)
					{
						this.items[14] = null;
						return true;
					}
				}
			}
		}

		return this.items[14].count < var3.getMaxStackSize();
	}

	public void smeltItem()
	{
		if (canSmelt())
		{
			ItemStack var1 = FurnaceRecipes.getInstance().getSmeltingResult(this.items[1]);
			boolean var2 = false;

			if (this.items[14] == null)
			{
				this.items[14] = var1.cloneItemStack();

				if (EEMaps.isOreBlock(this.items[1].id))
				{
					this.items[14].count += 1;
				}
			}
			else if (this.items[14].id == var1.id)
			{
				this.items[14].count += var1.count;

				if (EEMaps.isOreBlock(this.items[1].id))
				{
					if (this.items[14].count < var1.getMaxStackSize())
					{
						this.items[14].count += 1;
					}
					else
					{
						var2 = true;
					}
				}
			}

			if (this.items[14].count == var1.getMaxStackSize())
			{
				if (tryDropInChest(this.items[14]))
				{
					this.items[14] = null;

					if (var2)
					{
						this.items[14] = var1.cloneItemStack();
					}
				}
				else
				{
					for (int var3 = 15; var3 <= 26; var3++)
					{
						if (this.items[var3] == null)
						{
							this.items[var3] = this.items[14].cloneItemStack();
							this.items[14] = null;

							if (!var2)
							{
								break;
							}
							this.items[14] = var1.cloneItemStack();

							break;
						}
					}
				}
			}

			if (this.items[1].getItem().k())
			{
				this.items[1] = new ItemStack(this.items[1].getItem().j());
			}
			else
			{
				this.items[1].count -= 1;
			}

			if (this.items[1].count < 1)
			{
				this.items[1] = null;
			}

			this.world.notify(this.x, this.y, this.z);
		}
	}

	public int getItemBurnTime(ItemStack stack, boolean preserve)
	{
		if (stack == null)
		{
			return 0;
		}

		int itemId = stack.getItem().id;

		if (EEBase.isKleinStar(itemId))
		{
			if (EEBase.takeKleinStarPoints(stack, preserve ? 0 : 32, this.world))
			{
				return 1600;
			}
		}


		int itemData = stack.getData();
		return itemId == EEItem.aeternalisFuel.id ? 409600 : itemId == EEItem.mobiusFuel.id ? 102400 : itemId == EEItem.alchemicalCoal.id ? 25600 : itemId == Block.SAPLING.id ? 100 : itemId == Item.LAVA_BUCKET.id ? 3200 : itemId == Item.COAL.id ? 6400 : itemId == Item.STICK.id ? 100 : (itemId < 256) && (Block.byId[itemId].material == Material.WOOD) ? 300 : 0; // TODO: Replace ModLoader.addAllFuel (currently 0)
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
	public boolean a(EntityHuman var1)
	{
		return this.world.getTileEntity(this.x, this.y, this.z) == this;
	}

	@Override
	public int getStartInventorySide(int side)
	{
		switch (side)
		{
		case 0:
			return 0;

		case 1:
			return 9;

		default:
			return 10;
		}
	}

	@Override
	public int getSizeInventorySide(int side)
	{
		switch (side)
		{
		case 1:
			return 1;

		default:
			return 13;
		}
	}

	@Override
	public boolean onBlockActivated(EntityHuman var1)
	{
		if (!this.world.isStatic)
		{
			var1.openGui(mod_EE.getInstance(), GuiIds.RM_FURNACE, this.world, this.x, this.y, this.z);
		}

		return true;
	}

	@Override
	public int getTextureForSide(int var1)
	{
		byte var2 = this.direction;
		return var1 == var2 ? EEBase.rmFurnaceFront : EEBase.rmBlockSide;
	}

	@Override
	public int getInventoryTexture(int var1)
	{
		return var1 == 3 ? EEBase.rmFurnaceFront : EEBase.rmBlockSide;
	}

	@Override
	public int getLightValue()
	{
		return isBurning() ? 15 : 0;
	}

	@Override
	public void randomDisplayTick(Random var1)
	{
		if (isBurning())
		{
			byte var2 = this.direction;
			float var3 = this.x + 0.5F;
			float var4 = this.y + 0.0F + var1.nextFloat() * 6.0F / 16.0F;
			float var5 = this.z + 0.5F;
			float var6 = 0.52F;
			float var7 = var1.nextFloat() * 0.6F - 0.3F;

			if (var2 == 4)
			{
				this.world.a("smoke", var3 - var6, var4, var5 + var7, 0.0D, 0.0D, 0.0D);
				this.world.a("flame", var3 - var6, var4, var5 + var7, 0.0D, 0.0D, 0.0D);
			}
			else if (var2 == 5)
			{
				this.world.a("smoke", var3 + var6, var4, var5 + var7, 0.0D, 0.0D, 0.0D);
				this.world.a("flame", var3 + var6, var4, var5 + var7, 0.0D, 0.0D, 0.0D);
			}
			else if (var2 == 2)
			{
				this.world.a("smoke", var3 + var7, var4, var5 - var6, 0.0D, 0.0D, 0.0D);
				this.world.a("flame", var3 + var7, var4, var5 - var6, 0.0D, 0.0D, 0.0D);
			}
			else if (var2 == 3)
			{
				this.world.a("smoke", var3 + var7, var4, var5 + var6, 0.0D, 0.0D, 0.0D);
				this.world.a("flame", var3 + var7, var4, var5 + var6, 0.0D, 0.0D, 0.0D);
			}

			for (int var8 = 0; var8 < 4; var8++)
			{
				double var9 = this.x + var1.nextFloat();
				double var11 = this.y + var1.nextFloat();
				double var13 = this.z + var1.nextFloat();
				int var21 = var1.nextInt(2) * 2 - 1;
				double var15 = (var1.nextFloat() - 0.5D) * 0.5D;
				double var17 = (var1.nextFloat() - 0.5D) * 0.5D;
				double var19 = (var1.nextFloat() - 0.5D) * 0.5D;

				if (((this.world.getTypeId(this.x - 1, this.y, this.z) != EEBlock.eeStone.id) || (this.world.getData(this.x - 1, this.y, this.z) != 3)) && ((this.world.getTypeId(this.x + 1, this.y, this.z) != EEBlock.eeStone.id) || (this.world.getData(this.x + 1, this.y, this.z) != 3)))
				{
					var9 = this.x + 0.5D + 0.25D * var21;
					var15 = var1.nextFloat() * 2.0F * var21;
				}
				else
				{
					var13 = this.z + 0.5D + 0.25D * var21;
					var19 = var1.nextFloat() * 2.0F * var21;
				}

				this.world.a("portal", var9, var11, var13, var15, var17, var19);
			}
		}
	}

	@Override
	public boolean receiveEnergy(int var1, byte var2, boolean var3)
	{
		if (canSmelt())
		{
			if (var3)
			{
				this.furnaceBurnTime += var1;
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean sendEnergy(int var1, byte var2, boolean var3)
	{
		return false;
	}

	@Override
	public boolean passEnergy(int var1, byte var2, boolean var3)
	{
		return false;
	}

	@Override
	public void sendAllPackets(int var1)
	{
	}

	@Override
	public int relayBonus()
	{
		return 0;
	}

	@Override
	public ItemStack splitWithoutUpdate(int var1)
	{
		return null;
	}

	@Override
	public ItemStack[] getContents()
	{
		return this.items;
	}

	@Override
	public void setMaxStackSize(int size)
	{
	}
}