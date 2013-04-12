package ee;

import net.minecraft.server.*;


public class ItemRedHoe extends ItemRedTool
{
	private static Block[] blocksEffectiveAgainst =
	{
		Block.DIRT, Block.GRASS
	};
	private static boolean breakMode;

	public ItemRedHoe(int var1)
	{
		super(var1, 3, 8, blocksEffectiveAgainst);
	}

	@Override
	public float getDestroySpeed(ItemStack var1, Block var2)
	{
		if ((var2.material != Material.EARTH) && (var2.material != Material.GRASS))
		{
			return super.getDestroySpeed(var1, var2);
		}

		float var3 = 18.0F + chargeLevel(var1) * 4;

		if (breakMode)
		{
			var3 /= 10.0F;
		}

		return var3;
	}

	@Override
	public void doBreak(ItemStack stack, World world, EntityHuman human)
	{
		if (chargeLevel(stack) > 0)
		{
			int startX = (int)EEBase.playerX(human);
			int startY = (int)(EEBase.playerY(human) - 1.0D);
			int startZ = (int)EEBase.playerZ(human);

			if (chargeLevel(stack) < 1)
			{
				return;
			}

			human.C_();
			world.makeSound(human, "flash", 0.8F, 1.5F);

			outer:
			for (int offsetX = -(chargeLevel(stack) * chargeLevel(stack)) - 1; offsetX <= chargeLevel(stack) * chargeLevel(stack) + 1; offsetX++)
			{
				for (int offsetZ = -(chargeLevel(stack) * chargeLevel(stack)) - 1; offsetZ <= chargeLevel(stack) * chargeLevel(stack) + 1; offsetZ++)
				{
					int x = startX + offsetX;
					int y = startY;
					int z = startZ + offsetZ;
					int id = world.getTypeId(x, startY, z);
					int aboveId = world.getTypeId(x, startY + 1, z);

					if ((aboveId == BlockFlower.YELLOW_FLOWER.id) || (aboveId == BlockFlower.RED_ROSE.id) || (aboveId == BlockFlower.BROWN_MUSHROOM.id) || (aboveId == BlockFlower.RED_MUSHROOM.id) || (aboveId == BlockLongGrass.LONG_GRASS.id) || (aboveId == BlockDeadBush.DEAD_BUSH.id))
					{
						if (tryBreak(world, human, x, y + 1, z))
						{
							Block.byId[aboveId].dropNaturally(world, x, y + 1, z, world.getData(x, startY + 1, z), 1.0F, 1);
							aboveId = 0;
						}
						else
						{
							break outer;
						}
					}

					if ((aboveId == 0) && ((id == Block.DIRT.id) || (id == Block.GRASS.id)))
					{
						if (getFuelRemaining(stack) < 1)
						{
							ConsumeReagent(stack, human, false);
						}

						if (getFuelRemaining(stack) > 0 && tryBuild(world, human, x, y, z, Block.SOIL.id, 0))
						{
							world.setTypeId(x, startY, z, 60);
							setShort(stack, "fuelRemaining", getFuelRemaining(stack) - 1);

							if (world.random.nextInt(8) == 0)
							{
								world.a("largesmoke", x, startY, z, 0.0D, 0.0D, 0.0D);
							}

							if (world.random.nextInt(8) == 0)
							{
								world.a("explode", x, startY, z, 0.0D, 0.0D, 0.0D);
							}
						}
					}
				}
			}
		}
	}

	@Override
	@SuppressWarnings("AssignmentToMethodParameter")
	public boolean interactWith(ItemStack stack, EntityHuman human, World world, int startX, int startY, int startZ, int var7)
	{
		if (EEProxy.isClient(world))
		{
			return false;
		}

		if (chargeLevel(stack) > 0)
		{
			human.C_();
			world.makeSound(human, "flash", 0.8F, 1.5F);

			if ((world.getTypeId(startX, startY, startZ) == BlockFlower.YELLOW_FLOWER.id) || (world.getTypeId(startX, startY, startZ) == BlockFlower.RED_ROSE.id) || (world.getTypeId(startX, startY, startZ) == BlockFlower.BROWN_MUSHROOM.id) || (world.getTypeId(startX, startY, startZ) == BlockFlower.RED_MUSHROOM.id) || (world.getTypeId(startX, startY, startZ) == BlockLongGrass.LONG_GRASS.id) || (world.getTypeId(startX, startY, startZ) == BlockDeadBush.DEAD_BUSH.id))
			{
				startY--;
			}

			outer:
			for (int offsetX = -(chargeLevel(stack) * chargeLevel(stack)) - 1; offsetX <= chargeLevel(stack) * chargeLevel(stack) + 1; offsetX++)
			{
				for (int offsetZ = -(chargeLevel(stack) * chargeLevel(stack)) - 1; offsetZ <= chargeLevel(stack) * chargeLevel(stack) + 1; offsetZ++)
				{
					int x = startX + offsetX;
					int y = startY;
					int z = startZ + offsetZ;
					int id = world.getTypeId(x, startY, z);
					int aboveId = world.getTypeId(x, startY + 1, z);

					if ((aboveId == BlockFlower.YELLOW_FLOWER.id) || (aboveId == BlockFlower.RED_ROSE.id) || (aboveId == BlockFlower.BROWN_MUSHROOM.id) || (aboveId == BlockFlower.RED_MUSHROOM.id) || (aboveId == BlockLongGrass.LONG_GRASS.id) || (aboveId == BlockDeadBush.DEAD_BUSH.id))
					{
						if (tryBreak(world, human, x, y + 1, z))
						{
							Block.byId[aboveId].dropNaturally(world, x, y + 1, z, world.getData(x, startY + 1, z), 1.0F, 1);
							aboveId = 0;
						}
						else
						{
							break outer;
						}
					}

					if ((aboveId == 0) && ((id == Block.DIRT.id) || (id == Block.GRASS.id)))
					{
						if (getFuelRemaining(stack) < 1)
						{
							ConsumeReagent(stack, human, false);
						}

						if (getFuelRemaining(stack) > 0 && tryBuild(world, human, x, y, z, Block.SOIL.id, 0))
						{
							world.setTypeId(x, startY, z, 60);
							setShort(stack, "fuelRemaining", getFuelRemaining(stack) - 1);

							if (world.random.nextInt(8) == 0)
							{
								world.a("largesmoke", x, startY, z, 0.0D, 0.0D, 0.0D);
							}

							if (world.random.nextInt(8) == 0)
							{
								world.a("explode", x, startY, z, 0.0D, 0.0D, 0.0D);
							}
						}
					}
				}
			}

			return false;
		}
		if ((human != null) && (!human.d(startX, startY, startZ)))
		{
			return false;
		}

		int var8 = world.getTypeId(startX, startY, startZ);
		int var9 = world.getTypeId(startX, startY + 1, startZ);

		if (((var7 == 0) || (var9 != 0) || (var8 != Block.GRASS.id)) && (var8 != Block.DIRT.id))
		{
			return false;
		}

		Block soil = Block.SOIL;
		world.makeSound(startX + 0.5F, startY + 0.5F, startZ + 0.5F, soil.stepSound.getName(), (soil.stepSound.getVolume1() + 1.0F) / 2.0F, soil.stepSound.getVolume2() * 0.8F);

		if (world.isStatic)
		{
			return true;
		}

		tryBuild(world, human, startX, startY, startZ, Block.SOIL.id, 0);

		return false;
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
	public boolean isFull3D()
	{
		return true;
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
	public void doToggle(ItemStack var1, World var2, EntityHuman var3)
	{
	}
}