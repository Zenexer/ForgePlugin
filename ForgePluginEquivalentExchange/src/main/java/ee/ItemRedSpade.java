package ee;

import net.minecraft.server.*;


public class ItemRedSpade extends ItemRedTool
{
	private static Block[] blocksEffectiveAgainst =
	{
		Block.GRASS, Block.DIRT, Block.SOUL_SAND, Block.SAND, Block.GRAVEL, Block.SNOW, Block.SNOW_BLOCK, Block.CLAY, Block.SOIL
	};

	public ItemRedSpade(int var1)
	{
		super(var1, 3, 5, blocksEffectiveAgainst);
	}

	@Override
	public boolean canDestroySpecialBlock(Block var1)
	{
        return var1 != Block.SNOW ? var1 == Block.SNOW_BLOCK : true;
	}

	@Override
	public float getDestroySpeed(ItemStack var1, Block var2)
	{
		float var3 = 1.0F;
		return super.getDestroySpeed(var1, var2) / var3;
	}

	@Override
	public boolean a(ItemStack var1, int var2, int var3, int var4, int var5, EntityLiving var6)
	{
		if ((var6 instanceof EntityHuman))
		{
			EntityHuman human = (EntityHuman)var6;

			if (EEBase.getToolMode(human) != 0)
			{
				if (EEBase.getToolMode(human) == 1)
				{
					doTallImpact(human.world, var1, human, var3, var4, var5, EEBase.direction(human));
				}
				else if (EEBase.getToolMode(human) == 2)
				{
					doWideImpact(human.world, var1, human, var3, var4, var5, EEBase.heading(human));
				}
				else if (EEBase.getToolMode(human) == 3)
				{
					doLongImpact(human.world, var1, human, var3, var4, var5, EEBase.direction(human));
				}
			}

			return true;
		}

		return true;
	}

	public boolean canBreak(int var1, int var2)
	{
		if (Block.byId[var1] == null)
		{
			return false;
		}
		if ((!Block.byId[var1].hasTileEntity(var2)) && (var1 != Block.BEDROCK.id))
		{
			if (Block.byId[var1].material == null)
			{
				return false;
			}

			for (int var3 = 0; var3 < blocksEffectiveAgainst.length; var3++)
			{
				if (var1 == blocksEffectiveAgainst[var3].id)
				{
					return true;
				}
			}

			if ((Block.byId[var1].material != Material.GRASS) && (Block.byId[var1].material != Material.EARTH) && (Block.byId[var1].material != Material.SAND) && (Block.byId[var1].material != Material.SNOW_LAYER) && (Block.byId[var1].material != Material.CLAY))
			{
				return false;
			}

			return true;
		}

		return false;
	}

	public void doLongImpact(World var1, ItemStack var2, EntityHuman human, int var3, int var4, int var5, double var6)
	{
		cleanDroplist(var2);

		outer:
		for (int var8 = 1; var8 <= 2; var8++)
		{
			int var9 = var3;
			int var10 = var4;
			int var11 = var5;

			if (var6 == 0.0D)
			{
				var10 = var4 - var8;
			}

			if (var6 == 1.0D)
			{
				var10 = var4 + var8;
			}

			if (var6 == 2.0D)
			{
				var11 = var5 + var8;
			}

			if (var6 == 3.0D)
			{
				var9 = var3 - var8;
			}

			if (var6 == 4.0D)
			{
				var11 = var5 - var8;
			}

			if (var6 == 5.0D)
			{
				var9 = var3 + var8;
			}

			int var12 = var1.getTypeId(var9, var10, var11);
			int var13 = var1.getData(var9, var10, var11);

			if (canBreak(var12, var13))
			{
				if (!scanBlockAndBreak(var1, var2, human, var9, var10, var11))
				{
					break outer;
				}
			}
		}

		ejectDropList(var1, var2, var3, var4 + 0.5D, var5);
	}

	public void doWideImpact(World var1, ItemStack var2, EntityHuman human, int var3, int var4, int var5, double var6)
	{
		cleanDroplist(var2);

		outer:
		for (int var8 = -1; var8 <= 1; var8++)
		{
			int var9 = var3;
			int var11 = var5;

			if (var8 != 0)
			{
				if ((var6 != 2.0D) && (var6 != 4.0D))
				{
					var11 = var5 + var8;
				}
				else
				{
					var9 = var3 + var8;
				}

				int var12 = var1.getTypeId(var9, var4, var11);
				int var13 = var1.getData(var9, var4, var11);

				if (canBreak(var12, var13))
				{
					if (!scanBlockAndBreak(var1, var2, human, var9, var4, var11))
					{
						break outer;
					}
				}
			}
		}

		ejectDropList(var1, var2, var3, var4 + 0.5D, var5);
	}

	public void doTallImpact(World var1, ItemStack var2, EntityHuman human, int var4, int var5, int var6, double var7)
	{
		cleanDroplist(var2);

		outer:
		for (int var9 = -1; var9 <= 1; var9++)
		{
			int var10 = var4;
			int var11 = var5;
			int var12 = var6;

			if (var9 != 0)
			{
				if ((var7 != 0.0D) && (var7 != 1.0D))
				{
					var11 = var5 + var9;
				}
				else if ((EEBase.heading(human) != 2.0D) && (EEBase.heading(human) != 4.0D))
				{
					var10 = var4 + var9;
				}
				else
				{
					var12 = var6 + var9;
				}

				int var13 = var1.getTypeId(var10, var11, var12);
				int var14 = var1.getData(var10, var11, var12);

				if (canBreak(var13, var14))
				{
					if (!scanBlockAndBreak(var1, var2, human, var10, var11, var12))
					{
						break outer;
					}
				}
			}
		}

		ejectDropList(var1, var2, var4, var5 + 0.5D, var6);
	}

	@Override
	public boolean interactWith(ItemStack stack, EntityHuman human, World world, int var4, int y, int var6, int var7)
	{
		if (EEProxy.isClient(world))
		{
			return false;
		}

		if (chargeLevel(stack) >= 1)
		{
			cleanDroplist(stack);
			int var8 = world.getTypeId(var4, y, var6);

			if (var8 == Block.GRAVEL.id)
			{
				startSearch(world, human, stack, var8, var4, y, var6, false);
				return true;
			}
		}

		if (chargeLevel(stack) <= 0)
		{
			return false;
		}

		boolean var19 = true;
		cleanDroplist(stack);
		human.C_();
		world.makeSound(human, "flash", 0.8F, 1.5F);

		outer:
		for (int var9 = -chargeLevel(stack); var9 <= chargeLevel(stack); var9++)
		{
			for (int var10 = -chargeLevel(stack); var10 <= chargeLevel(stack); var10++)
			{
				int x = var4 + var9;
				int z = var6 + var10;

				if (var7 == 2)
				{
					z += chargeLevel(stack);
				}
				else if (var7 == 3)
				{
					z -= chargeLevel(stack);
				}
				else if (var7 == 4)
				{
					x += chargeLevel(stack);
				}
				else if (var7 == 5)
				{
					x -= chargeLevel(stack);
				}

				int var14 = world.getTypeId(x, y, z);
				int var15 = world.getData(x, y, z);

				if (canBreak(var14, var15))
				{
					if (getFuelRemaining(stack) < 1)
					{
						if ((var9 == chargeLevel(stack)) && (var10 == chargeLevel(stack)))
						{
							ConsumeReagent(stack, human, var19);
							var19 = false;
						}
						else
						{
							ConsumeReagent(stack, human, false);
						}
					}

					if (getFuelRemaining(stack) > 0)
					{
						if (!scanBlockAndBreak(world, stack, human, x, y, z))
						{
							break outer;
						}
					}
				}
			}
		}

		ejectDropList(world, stack, var4, y, var6);
		return true;
	}

	public void startSearch(World var1, EntityHuman var2, ItemStack var3, int var4, int var5, int var6, int var7, boolean var8)
	{
		var1.makeSound(var2, "flash", 0.8F, 1.5F);

		if (var8)
		{
			var2.C_();
		}

		doBreakShovel(var1, var2, var3, var4, var5, var6, var7);
	}

	public void doBreakShovel(World var1, EntityHuman var2, ItemStack var3, int var4, int var5, int var6, int var7)
	{
		if (getFuelRemaining(var3) < 1)
		{
			ConsumeReagent(var3, var2, false);
		}

		if (getFuelRemaining(var3) > 0 && scanBlockAndBreak(var1, var3, var2, var5, var6, var7))
		{
			for (int var14 = -1; var14 <= 1; var14++)
			{
				for (int var13 = -1; var13 <= 1; var13++)
				{
					for (int var12 = -1; var12 <= 1; var12++)
					{
						if (var1.getTypeId(var5 + var14, var6 + var13, var7 + var12) == var4)
						{
							doBreakShovelAdd(var1, var2, var3, var4, var5 + var14, var6 + var13, var7 + var12);
						}
					}
				}
			}

			ejectDropList(var1, var3, var5, var6, var7);
		}
	}

	public void doBreakShovelAdd(World var1, EntityHuman var2, ItemStack var3, int var4, int var5, int var6, int var7)
	{
		if (getFuelRemaining(var3) < 1)
		{
			ConsumeReagent(var3, var2, false);
		}

		if (getFuelRemaining(var3) > 0 && scanBlockAndBreak(var1, var3, var2, var5, var6, var7))
		{
			for (int var14 = -1; var14 <= 1; var14++)
			{
				for (int var13 = -1; var13 <= 1; var13++)
				{
					for (int var12 = -1; var12 <= 1; var12++)
					{
						if (var1.getTypeId(var5 + var14, var6 + var13, var7 + var12) == var4)
						{
							doBreakShovelAdd(var1, var2, var3, var4, var5 + var14, var6 + var13, var7 + var12);
						}
					}
				}
			}
		}
	}

	@Override
	public void doAlternate(ItemStack var1, World var2, EntityHuman var3)
	{
		EEBase.updateToolMode(var3);
	}

	@Override
	public void doToggle(ItemStack var1, World var2, EntityHuman var3)
	{
	}
}