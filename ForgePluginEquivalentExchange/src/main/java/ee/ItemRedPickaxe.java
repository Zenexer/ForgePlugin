package ee;

import static ee.EEMaps.isOreBlock;
import net.minecraft.server.*;


public class ItemRedPickaxe extends ItemRedTool
{
	private static Block[] blocksEffectiveAgainst =
	{
		Block.COBBLESTONE, Block.DOUBLE_STEP, Block.STEP, Block.STONE, Block.SANDSTONE, Block.MOSSY_COBBLESTONE, Block.IRON_ORE, Block.IRON_BLOCK, Block.COAL_ORE, Block.GOLD_BLOCK, Block.GOLD_ORE, Block.DIAMOND_ORE, Block.DIAMOND_BLOCK, Block.REDSTONE_ORE, Block.GLOWING_REDSTONE_ORE, Block.ICE, Block.NETHERRACK, Block.LAPIS_ORE, Block.LAPIS_BLOCK, Block.OBSIDIAN
	};

	protected ItemRedPickaxe(int idMinus256)
	{
		super(idMinus256, 3, 6, blocksEffectiveAgainst);
	}

	@Override
	public float getStrVsBlock(ItemStack toolStack, Block block, int data)
	{
		float denominator = 1.0F;

		if ((block.id == EEBlock.eePedestal.id && data == 0) || (block.id == EEBlock.eeStone.id && (data == 8 || data == 9)))
		{
			return 16.0F / denominator;
		}
		else if (block.material == Material.STONE || block.material == Material.ORE)
		{
			if (chargeLevel(toolStack) > 0)
			{
				return 16.0F + 16.0F * chargeLevel(toolStack) / denominator;
			}
			else
			{
				return 1200000.0F / denominator;
			}
		}

		return getDestroySpeed(toolStack, block) / denominator;
	}

	@Override
	public boolean a(ItemStack toolStack, int var2, int x, int y, int z, EntityLiving entity)
	{
		if (entity instanceof EntityHuman)
		{
			EntityHuman human = (EntityHuman)entity;

			if (EEBase.getToolMode(human) != 0)
			{
				if (EEBase.getToolMode(human) == 1)
				{
					doTallImpact(human.world, toolStack, human, x, y, z, EEBase.direction(human));
				}
				else if (EEBase.getToolMode(human) == 2)
				{
					doWideImpact(human.world, toolStack, human, x, y, z, EEBase.heading(human));
				}
				else if (EEBase.getToolMode(human) == 3)
				{
					doLongImpact(human.world, toolStack, human, x, y, z, EEBase.direction(human));
				}
			}
		}

		return true;
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
				var10 += var8;
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
				var11 -= var8;
			}

			if (var6 == 5.0D)
			{
				var9 += var8;
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

	public void doWideImpact(World world, ItemStack stack, EntityHuman human, int startX, int startY, int startZ, double var6)
	{
		cleanDroplist(stack);

		outer:
		for (int offset = -1; offset <= 1; offset++)
		{
			int x = startX;
			int z = startZ;

			if (offset != 0)
			{
				if ((var6 != 2.0D) && (var6 != 4.0D))
				{
					z = startZ + offset;
				}
				else
				{
					x = startX + offset;
				}

				int var12 = world.getTypeId(x, startY, z);
				int var13 = world.getData(x, startY, z);

				if (canBreak(var12, var13))
				{
					if (!scanBlockAndBreak(world, stack, human, x, startY, z))
					{
						break outer;
					}
				}
			}
		}

		ejectDropList(world, stack, startX, startY + 0.5D, startZ);
	}

	public void doTallImpact(World world, ItemStack stack, EntityHuman human, int startX, int startY, int startZ, double var7)
	{
		cleanDroplist(stack);

		outer:
		for (int offset = -1; offset <= 1; offset++)
		{
			int x = startX;
			int y = startY;
			int z = startZ;

			if (offset != 0)
			{
				if ((var7 != 0.0D) && (var7 != 1.0D))
				{
					y = startY + offset;
				}
				else if ((EEBase.heading(human) != 2.0D) && (EEBase.heading(human) != 4.0D))
				{
					x = startX + offset;
				}
				else
				{
					z = startZ + offset;
				}

				int id = world.getTypeId(x, y, z);
				int data = world.getData(x, y, z);

				if (canBreak(id, data))
				{
					if (!scanBlockAndBreak(world, stack, human, x, y, z))
					{
						break outer;
					}
				}
			}
		}

		ejectDropList(world, stack, startX, startY + 0.5D, startZ);
	}

	@Override
	public void doBreak(ItemStack stack, World world, EntityHuman human)
	{
		if (chargeLevel(stack) > 0)
		{
			int startX = (int)EEBase.playerX(human);
			int startY = (int)EEBase.playerY(human);
			int startZ = (int)EEBase.playerZ(human);

			for (int offsetX = -2; offsetX <= 2; offsetX++)
			{
				for (int offsetY = -2; offsetY <= 2; offsetY++)
				{
					for (int offsetZ = -2; offsetZ <= 2; offsetZ++)
					{
						int id = world.getTypeId(startX + offsetX, startY + offsetY, startZ + offsetZ);

						if (isOreBlock(id))
						{
							startSearch(world, stack, human, id, startX + offsetX, startY + offsetY, startZ + offsetZ, true);
						}
					}
				}
			}
		}
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

			for (int var3 = 0; var3 < blocksEffectiveAgainst.length; var3++)
			{
				if (var1 == blocksEffectiveAgainst[var3].id)
				{
					return true;
				}
			}

			if (canDestroySpecialBlock(Block.byId[var1]))
			{
				return true;
			}

			return false;
		}

		return false;
	}

	@Override
	public boolean interactWith(ItemStack stack, EntityHuman human, World world, int var4, int var5, int var6, int var7)
	{
		if (EEProxy.isClient(world))
		{
			return false;
		}
		if (chargeLevel(stack) >= 1)
		{
			cleanDroplist(stack);
			int id = world.getTypeId(var4, var5, var6);

			if (isOreBlock(id))
			{
				startSearch(world, stack, human, id, var4, var5, var6, false);
			}

			return true;
		}

		return false;
	}

	@Override
	public ItemStack a(ItemStack stack, World world, EntityHuman human)
	{
		if (!EEProxy.isClient(world))
		{
			doBreak(stack, world, human);
		}

		return stack;
	}

	public void startSearch(World world, ItemStack stack, EntityHuman human, int id, int x, int y, int z, boolean flag)
	{
		if (id == Block.BEDROCK.id)
		{
			human.a("Nice try. You can't break bedrock.");
		}
		else
		{
			world.makeSound(human, "flash", 0.8F, 1.5F);

			if (flag)
			{
				human.C_();
			}

			doBreakS(world, stack, human, id, x, y, z);
		}
	}

	public void doBreakS(World world, ItemStack stack, EntityHuman human, int id, int x, int y, int z)
	{
		if (!scanBlockAndBreak(world, stack, human, x, y, z))
		{
			return;
		}

		for (int offsetX = -1; offsetX <= 1; offsetX++)
		{
			for (int offsetY = -1; offsetY <= 1; offsetY++)
			{
				for (int offsetZ = -1; offsetZ <= 1; offsetZ++)
				{
					if ((id != Block.REDSTONE_ORE.id) && (id != Block.GLOWING_REDSTONE_ORE.id))
					{
						if (world.getTypeId(x + offsetX, y + offsetY, z + offsetZ) == id)
						{
							doBreakS(world, stack, human, id, x + offsetX, y + offsetY, z + offsetZ);
						}
					}
					else if ((world.getTypeId(x + offsetX, y + offsetY, z + offsetZ) == Block.GLOWING_REDSTONE_ORE.id) || (world.getTypeId(x + offsetX, y + offsetY, z + offsetZ) == Block.REDSTONE_ORE.id))
					{
						doBreakS(world, stack, human, id, x + offsetX, y + offsetY, z + offsetZ);
					}
				}
			}
		}

		ejectDropList(world, stack, EEBase.playerX(human), EEBase.playerY(human), EEBase.playerZ(human));
	}

	@Override
	public boolean canDestroySpecialBlock(Block id)
	{
		return id != Block.OBSIDIAN ? id == Block.DIAMOND_BLOCK || id == Block.DIAMOND_ORE ? true : id == Block.GOLD_BLOCK || id == Block.GOLD_ORE ? true : id == Block.IRON_BLOCK || id == Block.IRON_ORE ? true : id == Block.LAPIS_BLOCK || id == Block.LAPIS_ORE ? true : id == Block.REDSTONE_ORE || id == Block.GLOWING_REDSTONE_ORE ? true : id.material != Material.STONE ? id.material == Material.ORE : true : true;
	}

	@Override
	public void doRelease(ItemStack stack, World world, EntityHuman human)
	{
		doBreak(stack, world, human);
	}

	@Override
	public void doAlternate(ItemStack stack, World world, EntityHuman human)
	{
		EEBase.updateToolMode(human);
	}

	@Override
	public void doToggle(ItemStack stack, World world, EntityHuman human)
	{
	}
}