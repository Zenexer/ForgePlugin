package ee;

import net.minecraft.server.*;


public class ItemDarkAxe extends ItemDarkTool
{
	public boolean itemCharging;
	private static Block[] blocksEffectiveAgainst =
	{
		Block.WOOD, Block.BOOKSHELF, Block.LOG, Block.CHEST
	};

	protected ItemDarkAxe(int var1)
	{
		super(var1, 2, 6, blocksEffectiveAgainst);
	}

	@Override
	public float getDestroySpeed(ItemStack var1, Block var2)
	{
		return var2.material == Material.WOOD ? 14.0F + chargeLevel(var1) * 2 : super.getDestroySpeed(var1, var2);
	}

	@Override
	public void doBreak(ItemStack stack, World world, EntityHuman human)
	{
		if (chargeLevel(stack) > 0)
		{
			double var4 = EEBase.playerX(human);
			double var6 = EEBase.playerY(human);
			double var8 = EEBase.playerZ(human);
			boolean var10 = false;
			cleanDroplist(stack);

			if (chargeLevel(stack) < 1)
			{
				return;
			}

			human.C_();
			world.makeSound(human, "flash", 0.8F, 1.5F);

			outer:
			for (int var11 = -(chargeLevel(stack) * 2) + 1; var11 <= chargeLevel(stack) * 2 - 1; var11++)
			{
				for (int var12 = chargeLevel(stack) * 2 + 1; var12 >= -2; var12--)
				{
					for (int var13 = -(chargeLevel(stack) * 2) + 1; var13 <= chargeLevel(stack) * 2 - 1; var13++)
					{
						int x = (int)(var4 + var11);
						int y = (int)(var6 + var12);
						int z = (int)(var8 + var13);
						int var17 = world.getTypeId(x, y, z);

						if ((EEMaps.isWood(var17)) || (EEMaps.isLeaf(var17)))
						{
							if (getFuelRemaining(stack) < 1)
							{
								if ((var11 == chargeLevel(stack)) && (var13 == chargeLevel(stack)))
								{
									ConsumeReagent(stack, human, var10);
									var10 = false;
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
			}

			ejectDropList(world, stack, var4, var6, var8);
		}
	}

	@Override
	public ItemStack a(ItemStack var1, World var2, EntityHuman var3)
	{
		if (EEProxy.isClient(var2))
		{
			return var1;
		}

		doBreak(var1, var2, var3);
		return var1;
	}

	@Override
	public boolean interactWith(ItemStack stack, EntityHuman human, World world, int startX, int startY, int startZ, int var7)
	{
		if (EEProxy.isClient(world))
		{
			return false;
		}

		if (chargeLevel(stack) > 0)
		{
			double var8 = startX;
			double var10 = startY;
			double var12 = startZ;
			boolean var14 = false;
			cleanDroplist(stack);

			if (chargeLevel(stack) < 1)
			{
				return false;
			}

			human.C_();
			world.makeSound(human, "flash", 0.8F, 1.5F);

			outer:
			for (int var15 = -(chargeLevel(stack) * 2) + 1; var15 <= chargeLevel(stack) * 2 - 1; var15++)
			{
				for (int var16 = chargeLevel(stack) * 2 + 1; var16 >= -2; var16--)
				{
					for (int var17 = -(chargeLevel(stack) * 2) + 1; var17 <= chargeLevel(stack) * 2 - 1; var17++)
					{
						int x = (int)(var8 + var15);
						int y = (int)(var10 + var16);
						int z = (int)(var12 + var17);
						int var21 = world.getTypeId(x, y, z);

						if ((EEMaps.isWood(var21)) || (EEMaps.isLeaf(var21)))
						{
							if (getFuelRemaining(stack) < 1)
							{
								if ((var15 == chargeLevel(stack)) && (var17 == chargeLevel(stack)))
								{
									ConsumeReagent(stack, human, var14);
									var14 = false;
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
			}

			ejectDropList(world, stack, var8, var10, var12);
		}

		return false;
	}

	@Override
	public void doPassive(ItemStack var1, World var2, EntityHuman var3)
	{
	}

	@Override
	public void doActive(ItemStack var1, World var2, EntityHuman var3)
	{
	}

	@Override
	public void doHeld(ItemStack var1, World var2, EntityHuman var3)
	{
	}

	@Override
	public void doRelease(ItemStack var1, World var2, EntityHuman var3)
	{
		doBreak(var1, var2, var3);
	}

	@Override
	public void doAlternate(ItemStack var1, World var2, EntityHuman var3)
	{
	}

	@Override
	public void doLeftClick(ItemStack var1, World var2, EntityHuman var3)
	{
	}

	@Override
	public boolean canActivate()
	{
		return false;
	}

	@Override
	public void doToggle(ItemStack var1, World var2, EntityHuman var3)
	{
	}
}