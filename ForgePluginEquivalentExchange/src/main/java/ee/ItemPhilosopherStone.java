package ee;

import ee.core.GuiIds;
import net.minecraft.server.*;


public class ItemPhilosopherStone extends ItemEESuperTool
{
	public ItemPhilosopherStone(int var1)
	{
		super(var1, 4);
	}

	public void doExtra(World var1, ItemStack var2, EntityHuman var3)
	{
		var3.openGui(mod_EE.getInstance(), GuiIds.PORT_CRAFTING, var1, (int)var3.locX, (int)var3.locY, (int)var3.locZ);
	}

	@Override
	public void a(ItemStack var1, World var2, Entity var3, int var4, boolean var5)
	{
		if (cooldown(var1) > 0)
		{
			setCooldown(var1, cooldown(var1) - 1);
		}

		super.a(var1, var2, var3, var4, var5);
	}

	private void setCooldown(ItemStack var1, int var2)
	{
		setShort(var1, "cooldown", var2);
	}

	private int cooldown(ItemStack var1)
	{
		return getShort(var1, "cooldown");
	}

	@Override
	public void doRelease(ItemStack var1, World var2, EntityHuman var3)
	{
		var3.C_();
		var2.makeSound(var3, "transmute", 0.6F, 1.0F);
		var2.addEntity(new EntityPhilosopherEssence(var2, var3, chargeLevel(var1)));
	}

	@Override
	public void doAlternate(ItemStack var1, World var2, EntityHuman var3)
	{
		doExtra(var2, var1, var3);
	}

	@Override
	public void doLeftClick(ItemStack var1, World var2, EntityHuman var3)
	{
	}

	public boolean doTransmute(World world, int x, int y, int z, int targetId, EntityHuman human)
	{
		int id = world.getTypeId(x, y, z);
		if (id != targetId)
		{
			if (((targetId == Block.DIRT.id) && (id != Block.DIRT.id) && (id != Block.GRASS.id)) || ((targetId == Block.GRASS.id) && (id != Block.DIRT.id) && (id != Block.GRASS.id)))
			{
				return true;
			}

			if ((targetId != Block.DIRT.id) && (targetId != Block.GRASS.id))
			{
				return true;
			}
		}

		int data = world.getData(x, y, z);
		int aboveId = world.getTypeId(x, y + 1, z);
		int aboveData = world.getData(x, y + 1, z);

		if ((id != Block.DIRT.id) && (id != Block.GRASS.id))
		{
			if (id == Block.NETHERRACK.id)
			{
				if (tryBuild(world, human, x, y, z, Block.COBBLESTONE))
				{
					if (world.random.nextInt(8) == 0)
					{
						world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
						return true;
					}
				}
			}
			else if (id == Block.GLASS.id)
			{
				if (human.isSneaking() && tryBuild(world, human, x, y, z, Block.SAND))
				{
					if (world.random.nextInt(8) == 0)
					{
						world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
					}
				}
			}
			else if (id == Block.COBBLESTONE.id)
			{
				if (world.worldProvider.d)
				{
					if (!tryBuild(world, human, x, y, z, Block.NETHERRACK))
					{
						return false;
					}

					if (world.random.nextInt(8) == 0)
					{
						world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
					}
				}
				else if (human.isSneaking())
				{
					if (aboveId == 0)
					{
						if (!tryBuild(world, human, x, y, z, Block.GRASS))
						{
							return false;
						}

						if (world.random.nextInt(8) == 0)
						{
							world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
						}
					}
					else if (tryBuild(world, human, x, y, z, Block.DIRT))
					{
						if (world.random.nextInt(8) == 0)
						{
						world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
						}
					}
					else
					{
						return false;
					}
				}
				else if (tryBuild(world, human, x, y, z, Block.STONE))
				{
					if (world.random.nextInt(8) == 0)
					{
					world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
					}
				}
				else
				{
					return false;
				}
			}
			else if (id == Block.SAND.id)
			{
				if (human.isSneaking())
				{
						if (!tryBuild(world, human, x, y, z, Block.COBBLESTONE))
						{
							return false;
						}
						
					if (world.random.nextInt(8) == 0)
					{
						world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
					}
				}
				else if ((aboveId == Block.DEAD_BUSH.id) && (aboveData == 0))
				{
					if (!tryBuild(world, human, x, y, z, Block.GRASS) || !tryBuild(world, human, x, y + 1, z, Block.LONG_GRASS, 1))
					{
						return false;
					}

					if (world.random.nextInt(8) == 0)
					{
						world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
					}
				}
				else if (aboveId == 0)
				{
					if (!tryBuild(world, human, x, y, z, Block.GRASS))
					{
						return false;
					}

					if (world.random.nextInt(8) == 0)
					{
						world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
					}
				}
				else
				{
					if (!tryBuild(world, human, x, y, z, Block.DIRT))
					{
						return false;
					}

					if (world.random.nextInt(8) == 0)
					{
						world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
					}
				}
			}
			else if (id == Block.SANDSTONE.id)
			{
				if (!tryBuild(world, human, x, y, z, Block.GRAVEL))
				{
					return false;
				}

				if (world.random.nextInt(8) == 0)
				{
					world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
				}
			}
			else if ((id == Block.DEAD_BUSH.id) && (data == 0))
			{
				if (!tryBuild(world, human, x, y - 1, z, Block.GRASS) || !tryBuild(world, human, x, y, z, Block.LONG_GRASS, 1))
				{
					return false;
				}

				if (world.random.nextInt(8) == 0)
				{
					world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
				}
			}
			else if ((id == Block.LONG_GRASS.id) && (data == 1))
			{
				if (!tryBuild(world, human, x, y - 1, z, Block.SAND) || !tryBuild(world, human, x, y, z, Block.DEAD_BUSH))
				{
					return false;
				}
				
				if (world.random.nextInt(8) == 0)
				{
					world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
				}
			}
			else if (id == Block.GRAVEL.id)
			{
				if (!tryBuild(world, human, x, y, z, Block.SANDSTONE))
				{
					return false;
				}
				
				if (world.random.nextInt(8) == 0)
				{
					world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
				}
			}
			else if (id == Block.SAPLING.id)
			{
				int saplingData = 0;

				if ((data & 0x3) != 2)
				{
					saplingData++;
				}
				
				if (!tryBuild(world, human, x, y, z, Block.SAPLING, saplingData))
				{
					return false;
				}

				if (world.random.nextInt(8) == 0)
				{
					world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
				}
			}
			else if (id == Block.STONE.id)
			{
				if (human.isSneaking())
				{
					if (aboveId == 0)
					{
						if (!tryBuild(world, human, x, y, z, Block.GRASS))
						{
							return false;
						}

						if (world.random.nextInt(8) == 0)
						{
							world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
						}
					}
					else
					{
						if (!tryBuild(world, human, x, y, z, Block.DIRT))
						{
							return false;
						}

						if (world.random.nextInt(8) == 0)
						{
							world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
						}
					}
				}
				else
				{
					if (!tryBuild(world, human, x, y, z, Block.COBBLESTONE))
					{
						return false;
					}

					if (world.random.nextInt(8) == 0)
					{
						world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
					}
				}
			}
			else if (id == Block.PUMPKIN.id)
			{
				if (!tryBuild(world, human, x, y, z, Block.MELON))
				{
					return false;
				}

				if (world.random.nextInt(8) == 0)
				{
					world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
				}
			}
			else if (id == Block.MELON.id)
			{
				if (!tryBuild(world, human, x, y, z, Block.PUMPKIN))
				{
					return false;
				}

				if (world.random.nextInt(8) == 0)
				{
					world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
				}
			}
			else if (id == BlockFlower.RED_ROSE.id)
			{
				if (!tryBuild(world, human, x, y, z, Block.YELLOW_FLOWER))
				{
					return false;
				}

				if (world.random.nextInt(8) == 0)
				{
					world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
				}
			}
			else if (id == BlockFlower.YELLOW_FLOWER.id)
			{
				if (!tryBuild(world, human, x, y, z, Block.RED_ROSE))
				{
					return false;
				}

				if (world.random.nextInt(8) == 0)
				{
					world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
				}
			}
			else if (id == BlockFlower.RED_MUSHROOM.id)
			{
				if (!tryBuild(world, human, x, y, z, Block.BROWN_MUSHROOM))
				{
					return false;
				}

				if (world.random.nextInt(8) == 0)
				{
					world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
				}
			}
			else if (id == BlockFlower.BROWN_MUSHROOM.id)
			{
				if (!tryBuild(world, human, x, y, z, Block.RED_MUSHROOM))
				{
					return false;
				}

				if (world.random.nextInt(8) == 0)
				{
					world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
				}
			}
		}
		else
		{
			if (human.isSneaking())
			{
				if (!tryBuild(world, human, x, y, z, Block.COBBLESTONE))
				{
					return false;
				}

				if (world.random.nextInt(8) == 0)
				{
					world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
				}
			}
			else
			{
				if (!tryBuild(world, human, x, y, z, Block.SAND))
				{
					return false;
				}
				
				if (aboveId == Block.LONG_GRASS.id && aboveData == 1)
				{
					if (!tryBuild(world, human, x, y + 1, z, Block.DEAD_BUSH))
					{
						return false;
					}
				}

				if (world.random.nextInt(8) == 0)
				{
					world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
				}
			}
		}
		
		return true;
	}

	@Override
	public boolean interactWith(ItemStack stack, EntityHuman human, World world, int x, int y, int z, int var7)
	{
		if (EEProxy.isClient(world))
		{
			return false;
		}
		if (cooldown(stack) > 0)
		{
			return false;
		}

		setCooldown(stack, 10);
		human.C_();
		world.makeSound(human, "transmute", 0.6F, 1.0F);

		if ((world.getTypeId(x, y, z) == Block.SNOW.id) && (var7 == 1))
		{
			y--;
		}

		int var8 = world.getTypeId(x, y, z);

		if ((var8 != Block.LOG.id) && (var8 != Block.LEAVES.id))
		{
			int var9 = chargeLevel(stack);

			if (getMode(stack) == 0)
			{
				outer:
				for (int var10 = -var9 * (var7 == 4 ? 0 : var7 == 5 ? 2 : 1); var10 <= var9 * (var7 == 5 ? 0 : var7 == 4 ? 2 : 1); var10++)
				{
					for (int var11 = -var9 * (var7 == 0 ? 0 : var7 == 1 ? 2 : 1); var11 <= var9 * (var7 == 1 ? 0 : var7 == 0 ? 2 : 1); var11++)
					{
						for (int var12 = -var9 * (var7 == 2 ? 0 : var7 == 3 ? 2 : 1); var12 <= var9 * (var7 == 3 ? 0 : var7 == 2 ? 2 : 1); var12++)
						{
							int var13 = x + var10;
							int var14 = y + var11;
							int var15 = z + var12;
							
							if (!doTransmute(world, var13, var14, var15, var8, human))
							{
								break outer;
							}
						}
					}
				}
			}
			else if (getMode(stack) == 1)
			{
				outer:
				for (int var10 = -1 * (var7 == 4 ? 0 : var7 == 5 ? var9 * var9 : 1); var10 <= 1 * (var7 == 5 ? 0 : var7 == 4 ? var9 * var9 : 1); var10++)
				{
					for (int var11 = -1 * (var7 == 0 ? 0 : var7 == 1 ? var9 * var9 : 1); var11 <= 1 * (var7 == 1 ? 0 : var7 == 0 ? var9 * var9 : 1); var11++)
					{
						for (int var12 = -1 * (var7 == 2 ? 0 : var7 == 3 ? var9 * var9 : 1); var12 <= 1 * (var7 == 3 ? 0 : var7 == 2 ? var9 * var9 : 1); var12++)
						{
							int var13 = x + var10;
							int var14 = y + var11;
							int var15 = z + var12;
							
							if (!doTransmute(world, var13, var14, var15, var8, human))
							{
								break outer;
							}
						}
					}
				}
			}
			else if (getMode(stack) == 2)
			{
				outer:
				for (int var10 = -1 * ((var7 != 4) && (var7 != 5) ? var9 : 0); var10 <= 1 * ((var7 != 4) && (var7 != 5) ? var9 : 0); var10++)
				{
					for (int var11 = -1 * ((var7 != 0) && (var7 != 1) ? var9 : 0); var11 <= 1 * ((var7 != 0) && (var7 != 1) ? var9 : 0); var11++)
					{
						for (int var12 = -1 * ((var7 != 2) && (var7 != 3) ? var9 : 0); var12 <= 1 * ((var7 != 2) && (var7 != 3) ? var9 : 0); var12++)
						{
							int var13 = x + var10;
							int var14 = y + var11;
							int var15 = z + var12;
							
							if (!doTransmute(world, var13, var14, var15, var8, human))
							{
								break outer;
							}
						}
					}
				}
			}
		}
		else
		{
			doTreeTransmute(stack, human, world, x, y, z);
		}

		return false;
	}

	private void doTreeTransmute(ItemStack stack, EntityHuman human, World world, int var4, int var5, int var6)
	{
		int targetData = world.getData(var4, var5, var6) & 0x3;
		byte newData = 0;

		if (targetData == 0)
		{
			newData = 1;
		}
		else if (targetData == 1)
		{
			newData = 2;
		}
		else if (targetData == 2)
		{
			newData = 0;
		}

		outer:
		for (int var9 = -1; var9 <= 1; var9++)
		{
			for (int var10 = -1; var10 <= 1; var10++)
			{
				for (int var11 = -1; var11 <= 1; var11++)
				{
					if ((var9 == 0) && (var10 == 0) && (var11 == 0))
					{
						int x = var4 + var9;
						int y = var5 + var10;
						int z = var6 + var11;
						int id = world.getTypeId(x, y, z);
						int data = world.getData(x, y, z) & 0x3;

						if ((data == targetData) && ((id == Block.LOG.id) || (id == Block.LEAVES.id)))
						{
							if (!tryBuild(world, human, x, y, z, id, newData))
							{
								break outer;
							}
							
							doTreeSearch(world, human, x, y, z, targetData, newData);
						}
					}
				}
			}
		}
	}

	private boolean doTreeSearch(World world, EntityHuman human, int var2, int var3, int var4, int targetData, int newData)
	{
		for (int var7 = -1; var7 <= 1; var7++)
		{
			for (int var8 = -1; var8 <= 1; var8++)
			{
				for (int var9 = -1; var9 <= 1; var9++)
				{
					if ((var7 == 0) && (var8 == 0) && (var9 == 0))
					{
						int x = var2 + var7;
						int y = var3 + var8;
						int z = var4 + var9;
						int id = world.getTypeId(x, y, z);
						int data = world.getData(x, y, z) & 0x3;

						if ((data == targetData) && ((id == Block.LOG.id) || (id == Block.LEAVES.id)))
						{
							if (!tryBuild(world, human, x, y, z, id, newData))
							{
								return false;
							}
							
							doTreeSearch(world, human, x, y, z, targetData, newData);
						}
					}
				}
			}
		}
		
		return true;
	}

	@Override
	public ItemStack a(ItemStack stack, World world, EntityHuman human)
	{
		if (EEProxy.isClient(world))
		{
			return stack;
		}
		if (cooldown(stack) > 0)
		{
			return stack;
		}

		setCooldown(stack, 10);
		float var4 = 1.0F;
		float var5 = human.lastPitch + (human.pitch - human.lastPitch) * var4;
		float var6 = human.lastYaw + (human.yaw - human.lastYaw) * var4;
		double var7 = human.lastX + (human.locX - human.lastX) * var4;
		double var9 = human.lastY + (human.locY - human.lastY) * var4 + 1.62D - human.height;
		double var11 = human.lastZ + (human.locZ - human.lastZ) * var4;
		Vec3D var13 = Vec3D.create(var7, var9, var11);
		float var14 = MathHelper.cos(-var6 * 0.01745329F - 3.141593F);
		float var15 = MathHelper.sin(-var6 * 0.01745329F - 3.141593F);
		float var16 = -MathHelper.cos(-var5 * 0.01745329F);
		float var17 = MathHelper.sin(-var5 * 0.01745329F);
		float var18 = var15 * var16;
		float var20 = var14 * var16;
		double var21 = 5.0D;
		Vec3D var23 = var13.add(var18 * var21, var17 * var21, var20 * var21);
		MovingObjectPosition var24 = world.rayTrace(var13, var23, true);

		if (var24 == null)
		{
			return stack;
		}

		if (var24.type == EnumMovingObjectType.TILE)
		{
			int var25 = var24.b;
			int var26 = var24.c;
			int var27 = var24.d;
			Material var28 = world.getMaterial(var25, var26, var27);

			if ((var28 != Material.LAVA) && (var28 != Material.WATER))
			{
				return stack;
			}

			for (int var29 = -1 * chargeLevel(stack); var29 <= chargeLevel(stack); var29++)
			{
				for (int var30 = -1 * chargeLevel(stack); var30 <= chargeLevel(stack); var30++)
				{
					for (int var31 = -1 * chargeLevel(stack); var31 <= chargeLevel(stack); var31++)
					{
						int x = var25 + var29;
						int y = var26 + var30;
						int z = var27 + var31;
						Material var35 = world.getMaterial(x, y, z);

						if (var35 == var28)
						{
							int var36 = world.getData(x, y, z);

							if (var35 == Material.WATER)
							{
								if (world.getTypeId(x, y + 1, z) == 0)
								{
									if (tryBuild(world, human, x, y, z, Block.ICE) && world.random.nextInt(8) == 0)
									{
										world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
									}
								}
							}
							else if ((var35 == Material.LAVA) && (var36 == 0) && (world.getTypeId(x, y + 1, z) == 0))
							{
								if (tryBuild(world, human, x, y, z, Block.OBSIDIAN) && world.random.nextInt(8) == 0)
								{
									world.a("largesmoke", x, y + 1, z, 0.0D, 0.0D, 0.0D);
								}
							}
						}
					}
				}
			}
		}

		return stack;
	}

	@Override
	public void doToggle(ItemStack var1, World var2, EntityHuman var3)
	{
		changeMode(var1, var3);
	}

	public int getMode(ItemStack var1)
	{
		return getInteger(var1, "transmode");
	}

	public void setMode(ItemStack var1, int var2)
	{
		setInteger(var1, "transmode", var2);
	}

	public void changeMode(ItemStack var1, EntityHuman var2)
	{
		if (getMode(var1) == 2)
		{
			setMode(var1, 0);
		}
		else
		{
			setMode(var1, getMode(var1) + 1);
		}

		var2.a("Philosopher Stone transmuting " + (getMode(var1) == 0 ? "in a cube" : getMode(var1) == 1 ? "in a line" : "in a panel") + ".");
	}

	@Override
	public boolean e(ItemStack var1)
	{
		return false;
	}
}