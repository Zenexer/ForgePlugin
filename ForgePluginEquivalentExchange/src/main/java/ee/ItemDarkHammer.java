package ee;

import net.minecraft.server.*;


public class ItemDarkHammer extends ItemDarkTool
{
	public static boolean breakMode;
	private boolean haltImpact;
	private static Block[] blocksEffectiveAgainst =
	{
		Block.COBBLESTONE, Block.STONE, Block.SANDSTONE, Block.MOSSY_COBBLESTONE, Block.IRON_ORE, Block.IRON_BLOCK, Block.COAL_ORE, Block.GOLD_BLOCK, Block.GOLD_ORE, Block.DIAMOND_ORE, Block.DIAMOND_BLOCK, Block.REDSTONE_ORE, Block.GLOWING_REDSTONE_ORE, Block.ICE, Block.NETHERRACK, Block.LAPIS_ORE, Block.LAPIS_BLOCK, Block.OBSIDIAN
	};

	protected ItemDarkHammer(int var1)
	{
		super(var1, 2, 12, blocksEffectiveAgainst);
	}

	@Override
	public float getStrVsBlock(ItemStack var1, Block var2, int var3)
	{
		float var4 = 1.0F;
        return var2.material != Material.STONE || chargeLevel(var1) <= 0 ? var2.material != Material.STONE ? super.getDestroySpeed(var1, var2) / var4 : 14F / var4 : 30F / var4;
	}

	@Override
	public boolean a(ItemStack var1, EntityLiving var2, EntityLiving var3)
	{
		return true;
	}

	@Override
	public boolean a(ItemStack var1, int var2, int var3, int var4, int var5, EntityLiving var6)
	{
		if ((var6 instanceof EntityHuman))
		{
			EntityHuman var7 = (EntityHuman)var6;

			if (EEBase.getHammerMode(var7))
			{
				doMegaImpact(var7.world, var1, var7, var3, var4, var5, EEBase.direction(var7));
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
		if (!Block.byId[var1].b())
		{
			return false;
		}
		if ((!Block.byId[var1].hasTileEntity(var2)) && (var1 != Block.BEDROCK.id))
		{
			if (Block.byId[var1].material == null)
			{
				return false;
			}
			if (Block.byId[var1].material == Material.STONE)
			{
				return true;
			}

			for (int var3 = 0; var3 < blocksEffectiveAgainst.length; var3++)
			{
				if (var1 == blocksEffectiveAgainst[var3].id)
				{
					return true;
				}
			}

			return false;
		}

		return false;
	}

	public void doMegaImpact(World var1, ItemStack var2, EntityHuman human, int var3, int var4, int var5, double var6)
	{
		cleanDroplist(var2);

		outer:
		for (int var8 = -1; var8 <= 1; var8++)
		{
			for (int var9 = -1; var9 <= 1; var9++)
			{
				int var10 = var3;
				int var11 = var4;
				int var12 = var5;

				if ((var8 != 0) || (var9 != 0))
				{
					if ((var6 != 0.0D) && (var6 != 1.0D))
					{
						if ((var6 != 2.0D) && (var6 != 4.0D))
						{
							if ((var6 == 3.0D) || (var6 == 5.0D))
							{
								var11 = var4 + var8;
								var12 = var5 + var9;
							}
						}
						else
						{
							var10 = var3 + var8;
							var11 = var4 + var9;
						}
					}
					else
					{
						var10 = var3 + var8;
						var12 = var5 + var9;
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
		}

		ejectDropList(var1, var2, var3, var4 + 0.5D, var5);
	}

	@Override
	public boolean interactWith(ItemStack var1, EntityHuman var2, World var3, int var4, int var5, int var6, int var7)
	{
		if (EEProxy.isClient(var3))
		{
			return false;
		}

		boolean var8 = true;

		if (chargeLevel(var1) > 0)
		{
			cleanDroplist(var1);
			var2.C_();
			var3.makeSound(var2, "flash", 0.8F, 1.5F);

			outer:
			for (int var9 = -(chargeLevel(var1) * (var7 == 4 ? 0 : var7 == 5 ? 2 : 1)); var9 <= chargeLevel(var1) * (var7 == 4 ? 2 : var7 == 5 ? 0 : 1); var9++)
			{
				for (int var10 = -(chargeLevel(var1) * (var7 == 0 ? 0 : var7 == 1 ? 2 : 1)); var10 <= chargeLevel(var1) * (var7 == 0 ? 2 : var7 == 1 ? 0 : 1); var10++)
				{
					for (int var11 = -(chargeLevel(var1) * (var7 == 2 ? 0 : var7 == 3 ? 2 : 1)); var11 <= chargeLevel(var1) * (var7 == 2 ? 2 : var7 == 3 ? 0 : 1); var11++)
					{
						int x = var4 + var9;
						int y = var5 + var10;
						int z = var6 + var11;
						int var15 = var3.getTypeId(x, y, z);
						int var16 = var3.getData(x, y, z);

						if (canBreak(var15, var16))
						{
							if (getFuelRemaining(var1) < 1)
							{
								ConsumeReagent(var1, var2, var8);
								var8 = false;
							}

							if (getFuelRemaining(var1) > 0)
							{
								if (!scanBlockAndBreak(var3, var1, var2, x, y, z))
								{
									break outer;
								}
							}
						}
					}
				}
			}

			ejectDropList(var3, var1, var4, var5, var6);
		}

		return false;
	}

	@Override
	public boolean canDestroySpecialBlock(Block var1)
	{
        return var1 != Block.OBSIDIAN ? var1 == Block.DIAMOND_BLOCK || var1 == Block.DIAMOND_ORE ? true : var1 == Block.GOLD_BLOCK || var1 == Block.GOLD_ORE ? true : var1 == Block.IRON_BLOCK || var1 == Block.IRON_ORE ? true : var1 == Block.LAPIS_BLOCK || var1 == Block.LAPIS_ORE ? true : var1 == Block.REDSTONE_ORE || var1 == Block.GLOWING_REDSTONE_ORE ? true : var1.material != Material.STONE ? var1.material == Material.ORE : true : true;
	}

	@Override
	public void doToggle(ItemStack var1, World var2, EntityHuman var3)
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
		EEBase.updateHammerMode(var3, true);
	}
}