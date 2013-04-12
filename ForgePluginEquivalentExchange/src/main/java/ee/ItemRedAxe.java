package ee;

import net.minecraft.server.*;


public class ItemRedAxe extends ItemRedTool
{
	public boolean itemCharging;
	private static Block[] blocksEffectiveAgainst =
	{
		Block.WOOD, Block.BOOKSHELF, Block.LOG, Block.CHEST
	};

	protected ItemRedAxe(int var1)
	{
		super(var1, 3, 12, blocksEffectiveAgainst);
	}

	@Override
	public float getDestroySpeed(ItemStack var1, Block var2)
	{
		System.out.println("getDestroySpeed");
		return var2.material == Material.WOOD ? 18.0F + chargeLevel(var1) * 2 : super.getDestroySpeed(var1, var2);
	}

	@Override
	public void doBreak(ItemStack stack, World world, EntityHuman human)
	{
		if (chargeLevel(stack) > 0)
		{
			final int startX = (int)EEBase.playerX(human);
			final int startY = (int)EEBase.playerY(human);
			final int startZ = (int)EEBase.playerZ(human);

			breakBlocks(stack, world, human, startX, startY, startZ);
		}
	}

	@Override
	public ItemStack a(ItemStack var1, World var2, EntityHuman var3)
	{
		if (!EEProxy.isClient(var2))
		{
			doBreak(var1, var2, var3);
		}

		return var1;
	}

	@Override
	public boolean interactWith(ItemStack stack, EntityHuman human, World world, int startX, int startY, int startZ, int unused)
	{
		if (EEProxy.isClient(world))
		{
			return false;
		}

		return breakBlocks(stack, world, human, startX, startY, startZ);
	}

	private boolean breakBlocks(final ItemStack stack, final World world, final EntityHuman human, final int startX, final int startY, final int startZ)
	{
		if (!(human instanceof EntityPlayer))
		{
			return false;
		}

		if (chargeLevel(stack) > 0)
		{
			boolean consumeReagentFlag = true;
			cleanDroplist(stack);

			if (chargeLevel(stack) < 1)
			{
				return false;
			}

			human.C_();
			world.makeSound(human, "flash", 0.8F, 1.5F);

			final int charge = chargeLevel(stack);
			final int offset = charge * 2 - 1;

			outer:
			for (int offsetX = -offset; offsetX <= offset; offsetX++)
			{
				for (int offsetY = offset + 2; offsetY >= -2; offsetY--)
				{
					for (int offsetZ = -offset; offsetZ <= offset; offsetZ++)
					{
						int x = startX + offsetX;
						int y = startY + offsetY;
						int z = startZ + offsetZ;
						int id = world.getTypeId(x, y, z);

						if ((EEMaps.isWood(id)) || (EEMaps.isLeaf(id)))
						{
							if (getFuelRemaining(stack) < 1)
							{
								if (offsetX == charge && offsetZ == charge)
								{
									ConsumeReagent(stack, human, consumeReagentFlag);
									consumeReagentFlag = false;
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

			ejectDropList(world, stack, startX, startY, startZ);
		}

		return false;
	}

	@Override
	public void doToggle(ItemStack var1, World var2, EntityHuman var3)
	{
	}
}