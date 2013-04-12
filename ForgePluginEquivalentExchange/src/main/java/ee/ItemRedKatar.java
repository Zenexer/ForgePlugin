package ee;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.server.*;


public class ItemRedKatar extends ItemRedTool
{
	public boolean itemCharging;
	private static Block[] blocksEffectiveAgainst =
	{
		Block.WOOD, Block.BOOKSHELF, Block.LOG, Block.CHEST, Block.DIRT, Block.GRASS, Block.LEAVES, Block.WEB, Block.WOOL
	};

	protected ItemRedKatar(int var1)
	{
		super(var1, 4, 18, blocksEffectiveAgainst);
	}

	@Override
	public boolean a(ItemStack var1, int var2, int var3, int var4, int var5, EntityLiving var6)
	{
		boolean var7 = false;

		if ((!EEMaps.isLeaf(var2)) && (var2 != Block.WEB.id) && (var2 != Block.VINE.id) && (var2 != BlockFlower.LONG_GRASS.id) && (var2 != BlockFlower.DEAD_BUSH.id))
		{
			var7 = true;
		}

		if (!var7)
		{
			EEProxy.dropBlockAsItemStack(Block.byId[var2], var6, var3, var4, var5, new ItemStack(var2, 1, var2 == Block.LEAVES.id ? ((ItemEECharged)var1.getItem()).getShort(var1, "lastMeta") & 0x3 : ((ItemEECharged)var1.getItem()).getShort(var1, "lastMeta")));
		}

		return super.a(var1, var2, var3, var4, var5, var6);
	}

	@Override
	public boolean canDestroySpecialBlock(Block var1)
	{
		return var1.id == Block.WEB.id;
	}

	public boolean ConsumeReagent(int var1, ItemStack var2, EntityHuman var3, boolean var4)
	{
		if (getFuelRemaining(var2) >= 16)
		{
			setFuelRemaining(var2, getFuelRemaining(var2) - 16);
			return true;
		}

		int var5 = getFuelRemaining(var2);

		while (getFuelRemaining(var2) < 16)
		{
			ConsumeReagent(var2, var3, var4);

			if (var5 == getFuelRemaining(var2))
			{
				break;
			}

			var5 = getFuelRemaining(var2);

			if (getFuelRemaining(var2) >= 16)
			{
				setFuelRemaining(var2, getFuelRemaining(var2) - 16);
				return true;
			}
		}

		return false;
	}

	@Override
	public float getStrVsBlock(ItemStack var1, Block var2, int var3)
	{
		if (getShort(var1, "lastMeta") != var3)
		{
			setShort(var1, "lastMeta", var3);
		}
		if (var2.id != Block.VINE.id && var2.id != Block.LEAVES.id && var2.id != Block.WEB.id)
		{
			if (var2.id == Block.WOOL.id)
			{
				return 5F;
			}

			if (var2.material != Material.EARTH && var2.material != Material.GRASS)
			{
				return var2.material != Material.WOOD ? super.getDestroySpeed(var1, var2) : 18F + chargeLevel(var1) * 2;
			}

			return 18F + chargeLevel(var1) * 4;
		}

		return 15.0f;
	}

	public void doSwordBreak(ItemStack var1, World var2, EntityHuman var3)
	{
		if (chargeLevel(var1) > 0)
		{
			boolean var4 = false;

			int var5;
			for (var5 = 1; var5 <= chargeLevel(var1); var5++)
			{
				if (var5 == chargeLevel(var1))
				{
					var4 = true;
				}

				if (!ConsumeReagent(1, var1, var3, var4))
				{
					var5--;
					break;
				}
			}

			if (var5 < 1)
			{
				return;
			}

			var3.C_();
			var2.makeSound(var3, "flash", 0.8F, 1.5F);
			List var6 = var2.getEntities(var3, AxisAlignedBB.b((float)var3.locX - (var5 / 1.5D + 2.0D), var3.locY - (var5 / 1.5D + 2.0D), (float)var3.locZ - (var5 / 1.5D + 2.0D), (float)var3.locX + var5 / 1.5D + 2.0D, var3.locY + var5 / 1.5D + 2.0D, (float)var3.locZ + var5 / 1.5D + 2.0D));

			for (int var7 = 0; var7 < var6.size(); var7++)
			{
				if (((var6.get(var7) instanceof EntityLiving)) && ((EEBase.getSwordMode(var3)) || ((var6.get(var7) instanceof EntityMonster))))
				{
					Entity var8 = (Entity)var6.get(var7);
					var8.damageEntity(DamageSource.playerAttack(var3), weaponDamage + chargeLevel(var1) * 2);
				}
			}
		}
	}

	@Override
	public boolean interactWith(ItemStack var1, EntityHuman var2, World var3, int var4, int var5, int var6, int var7)
	{
		if (EEProxy.isClient(var3))
		{
			return false;
		}

		int var8 = var3.getTypeId(var4, var5, var6);
		chargeLevel(var1);

		if ((EEMaps.isLeaf(var8)) || (var8 == Block.WEB.id) || (var8 == Block.VINE.id) || (var8 == BlockFlower.LONG_GRASS.id) || (var8 == BlockFlower.DEAD_BUSH.id))
		{
			onItemUseShears(var1, var2, var3, var4, var5, var6, var7);
		}

		if (((var8 == Block.DIRT.id) || (var8 == Block.GRASS.id)) && (var3.getTypeId(var4, var5 + 1, var6) == 0))
		{
			onItemUseHoe(var1, var2, var3, var4, var5, var6, var7);
		}

		if (EEMaps.isWood(var8))
		{
			onItemUseAxe(var1, var2, var3, var4, var5, var6, var7);
		}

		return false;
	}

	public boolean onItemUseShears(ItemStack stack, EntityHuman human, World world, int startX, int startY, int startZ, int var7)
	{
		if (chargeLevel(stack) > 0)
		{
			boolean var8 = false;
			cleanDroplist(stack);
			human.C_();
			world.makeSound(human, "flash", 0.8F, 1.5F);

			outer:
			for (int offsetX = -(chargeLevel(stack) + 2); offsetX <= chargeLevel(stack) + 2; offsetX++)
			{
				for (int offsetY = -(chargeLevel(stack) + 2); offsetY <= chargeLevel(stack) + 2; offsetY++)
				{
					for (int offsetZ = -(chargeLevel(stack) + 2); offsetZ <= chargeLevel(stack) + 2; offsetZ++)
					{
						int id = world.getTypeId(startX + offsetX, startY + offsetY, startZ + offsetZ);

						if ((EEMaps.isLeaf(id)) || (id == Block.VINE.id) || (id == Block.WEB.id) || (id == BlockFlower.LONG_GRASS.id) || (id == BlockFlower.DEAD_BUSH.id))
						{
							if (getFuelRemaining(stack) < 1)
							{
								ConsumeReagent(stack, human, false);
							}

							if (getFuelRemaining(stack) > 0)
							{
								int data = world.getData(startX + offsetX, startY + offsetY, startZ + offsetZ);

								if ((!EEMaps.isLeaf(id)) && (id != Block.VINE.id) && (id != Block.WEB.id) && (id != Block.LONG_GRASS.id) && (id != Block.DEAD_BUSH.id))
								{
									if (!scanBlockAndBreak(world, stack, human, startX + offsetX, startY + offsetY, startZ + offsetZ))
									{
										break outer;
									}
								}
								else if (id == Block.LEAVES.id)
								{
									if (!scanBlockAndBreak(world, stack, human, startX + offsetX, startY + offsetY, startZ + offsetZ, new ItemStack(Block.LEAVES.id, 1, data & 0x3)))
									{
										break outer;
									}
								}
								else
								{
									if (!scanBlockAndBreak(world, stack, human, startX + offsetX, startY + offsetY, startZ + offsetZ, new ItemStack(Block.byId[id], 1, data)))
									{
										break outer;
									}
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

	public boolean onItemUseHoe(ItemStack stack, EntityHuman human, World world, int startX, int startY, int startZ, int var7)
	{
		if (chargeLevel(stack) > 0)
		{
			human.C_();
			world.makeSound(human, "flash", 0.8F, 1.5F);

			if ((world.getTypeId(startX, startY, startZ) == BlockFlower.YELLOW_FLOWER.id) || (world.getTypeId(startX, startY, startZ) == BlockFlower.RED_ROSE.id) || (world.getTypeId(startX, startY, startZ) == BlockFlower.BROWN_MUSHROOM.id) || (world.getTypeId(startX, startY, startZ) == BlockFlower.RED_MUSHROOM.id) || (world.getTypeId(startX, startY, startZ) == BlockLongGrass.LONG_GRASS.id) || (world.getTypeId(startX, startY, startZ) == BlockDeadBush.DEAD_BUSH.id))
			{
				startY--;
			}

			for (int var8 = -(chargeLevel(stack) * chargeLevel(stack)) - 1; var8 <= chargeLevel(stack) * chargeLevel(stack) + 1; var8++)
			{
				for (int var9 = -(chargeLevel(stack) * chargeLevel(stack)) - 1; var9 <= chargeLevel(stack) * chargeLevel(stack) + 1; var9++)
				{
					int x = startX + var8;
					int y = startY;
					int z = startZ + var9;
					int id = world.getTypeId(x, startY, z);
					int aboveId = world.getTypeId(x, startY + 1, z);

					if ((aboveId == BlockFlower.YELLOW_FLOWER.id) || (aboveId == BlockFlower.RED_ROSE.id) || (aboveId == BlockFlower.BROWN_MUSHROOM.id) || (aboveId == BlockFlower.RED_MUSHROOM.id) || (aboveId == BlockLongGrass.LONG_GRASS.id) || (aboveId == BlockDeadBush.DEAD_BUSH.id))
					{
						if (tryBreak(world, human, x, y + 1, z))
						{
							Block.byId[aboveId].dropNaturally(world, x, y + 1, z, world.getData(x, startY + 1, z), 1.0F, 1);
							aboveId = 0;
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

		Block var10 = Block.SOIL;
		world.makeSound(startX + 0.5F, startY + 0.5F, startZ + 0.5F, var10.stepSound.getName(), (var10.stepSound.getVolume1() + 1.0F) / 2.0F, var10.stepSound.getVolume2() * 0.8F);

		if (world.isStatic)
		{
			return true;
		}

		tryBuild(world, human, startX, startY, startZ, Block.SOIL.id, 0);

		return true;
	}

	public boolean onItemUseAxe(ItemStack var1, EntityHuman var2, World var3, int var4, int var5, int var6, int var7)
	{
		if (chargeLevel(var1) > 0)
		{
			double var8 = var4;
			double var10 = var5;
			double var12 = var6;
			boolean var14 = false;
			cleanDroplist(var1);

			if (chargeLevel(var1) < 1)
			{
				return false;
			}

			var2.C_();
			var3.makeSound(var2, "flash", 0.8F, 1.5F);

			outer:
			for (int var15 = -(chargeLevel(var1) * 2) + 1; var15 <= chargeLevel(var1) * 2 - 1; var15++)
			{
				for (int var16 = chargeLevel(var1) * 2 + 1; var16 >= -2; var16--)
				{
					for (int var17 = -(chargeLevel(var1) * 2) + 1; var17 <= chargeLevel(var1) * 2 - 1; var17++)
					{
						int x = (int)(var8 + var15);
						int y = (int)(var10 + var16);
						int z = (int)(var12 + var17);
						int var21 = var3.getTypeId(x, y, z);

						if ((EEMaps.isWood(var21)) || (EEMaps.isLeaf(var21)))
						{
							if (getFuelRemaining(var1) < 1)
							{
								if ((var15 == chargeLevel(var1)) && (var17 == chargeLevel(var1)))
								{
									ConsumeReagent(var1, var2, var14);
									var14 = false;
								}
								else
								{
									ConsumeReagent(var1, var2, false);
								}
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

			ejectDropList(var3, var1, var8, var10, var12);
		}

		return false;
	}

	@Override
	public boolean isFull3D()
	{
		return true;
	}

	@Override
	public EnumAnimation d(ItemStack var1)
	{
		return EnumAnimation.d;
	}

	@Override
	public int c(ItemStack var1)
	{
		return 72000;
	}

	@Override
	public ItemStack a(ItemStack var1, World var2, EntityHuman var3)
	{
		if (EEProxy.isClient(var2))
		{
			return var1;
		}

		var3.a(var1, c(var1));
		return var1;
	}

	public void doShear(ItemStack var1, World world, EntityHuman var3, Entity target)
	{
		if (chargeLevel(var1) > 0)
		{
			boolean var5 = false;
			int power = 0;

			if (getFuelRemaining(var1) < 10)
			{
				ConsumeReagent(var1, var3, false);
			}

			if (getFuelRemaining(var1) < 10)
			{
				ConsumeReagent(var1, var3, false);
			}

			if (getFuelRemaining(var1) < 10)
			{
				ConsumeReagent(var1, var3, false);
			}

			while ((getFuelRemaining(var1) >= 10) && (power < chargeLevel(var1)))
			{
				setShort(var1, "fuelRemaining", getFuelRemaining(var1) - 10);
				power++;

				if (getFuelRemaining(var1) < 10)
				{
					ConsumeReagent(var1, var3, false);
				}

				if (getFuelRemaining(var1) < 10)
				{
					ConsumeReagent(var1, var3, false);
				}

				if (getFuelRemaining(var1) < 10)
				{
					ConsumeReagent(var1, var3, false);
				}
			}

			if (power > 0)
			{
				var3.C_();
				world.makeSound(var3, "flash", 0.8F, 1.5F);
				int var7 = 3 * power;

				if ((target instanceof EntitySheep))
				{
					if (world.random.nextInt(100) < var7)
					{
						EntitySheep var8 = new EntitySheep(world);
						double var9 = target.locX - var3.locX;
						double var11 = target.locZ - var3.locZ;

						if (var9 < 0.0D)
						{
							var9 *= -1.0D;
						}

						if (var11 < 0.0D)
						{
							var11 *= -1.0D;
						}

						var9 += target.locX;
						var11 += target.locZ;
						double var13 = target.locY;

						for (int var15 = -5; var15 <= 5; var15++)
						{
							if ((world.getTypeId((int)var9, (int)var13 + var15, (int)var11) != 0) && (world.getTypeId((int)var9, (int)var13 + var15 + 1, (int)var11) == 0))
							{
								var8.setPosition(var9, var13 + var15 + 1.0D, var11);
								var8.setColor(((EntitySheep)target).getColor());
								world.addEntity(var8);
								break;
							}
						}
					}

					((EntitySheep)target).setSheared(true);
					int var19 = 3 + world.random.nextInt(2) + chargeLevel(var1) / 2;

					for (int var10 = 0; var10 < var19; var10++)
					{
						world.addEntity(new EntityItem(world, var3.locX, var3.locY, var3.locZ, new ItemStack(Block.WOOL.id, var19, ((EntitySheep)target).getColor())));
					}
				}
				else if ((target instanceof EntityMushroomCow))
				{
					if (world.random.nextInt(100) < var7)
					{
						EntityMushroomCow var18 = new EntityMushroomCow(world);
						double var9 = target.locX - var3.locX;
						double var11 = target.locZ - var3.locZ;

						if (var9 < 0.0D)
						{
							var9 *= -1.0D;
						}

						if (var11 < 0.0D)
						{
							var11 *= -1.0D;
						}

						var9 += target.locX;
						var11 += target.locZ;
						double var13 = target.locY;

						for (int var15 = -5; var15 <= 5; var15++)
						{
							if ((world.getTypeId((int)var9, (int)var13 + var15, (int)var11) != 0) && (world.getTypeId((int)var9, (int)var13 + var15 + 1, (int)var11) == 0))
							{
								var18.setPosition(var9, var13 + var15 + 1.0D, var11);
								world.addEntity(var18);
								break;
							}
						}
					}

					((EntityMushroomCow)target).die();
					EntityCow var20 = new EntityCow(world);
					var20.setPositionRotation(target.locX, target.locY, target.locZ, target.yaw, target.pitch);
					var20.setHealth(((EntityMushroomCow)target).getHealth());
					var20.V = ((EntityMushroomCow)target).V;
					world.addEntity(var20);
					world.a("largeexplode", target.locX, target.locY + target.length / 2.0F, target.locZ, 0.0D, 0.0D, 0.0D);
					int var23 = 5 + world.random.nextInt(2) + chargeLevel(var1) / 2;
					Object var22 = null;

					for (int var24 = 0; var24 < var23; var24++)
					{
						world.addEntity(new EntityItem(world, var3.locX, var3.locY, var3.locZ, new ItemStack(Block.RED_MUSHROOM, var23)));
					}
				}
			}
		}
		else if ((target instanceof EntitySheep))
		{
			((EntitySheep)target).setSheared(true);
			int var6 = 3 + world.random.nextInt(2);

			for (int var19 = 0; var19 < var6; var19++)
			{
				world.addEntity(new EntityItem(world, var3.locX, var3.locY, var3.locZ, new ItemStack(Block.WOOL.id, var6, ((EntitySheep)target).getColor())));
			}
		}
		else if ((target instanceof EntityMushroomCow))
		{
			((EntityMushroomCow)target).die();
			EntityCow var16 = new EntityCow(((EntityMushroomCow)target).world);
			var16.setPositionRotation(((EntityMushroomCow)target).locX, ((EntityMushroomCow)target).locY, ((EntityMushroomCow)target).locZ, ((EntityMushroomCow)target).yaw, ((EntityMushroomCow)target).pitch);
			var16.setHealth(((EntityMushroomCow)target).getHealth());
			var16.V = ((EntityMushroomCow)target).V;
			((EntityMushroomCow)target).world.addEntity(var16);
			((EntityMushroomCow)target).world.a("largeexplode", ((EntityMushroomCow)target).locX, ((EntityMushroomCow)target).locY + ((EntityMushroomCow)target).length / 2.0F, ((EntityMushroomCow)target).locZ, 0.0D, 0.0D, 0.0D);

			for (int var6 = 0; var6 < 5; var6++)
			{
				((EntityMushroomCow)target).world.addEntity(new EntityItem(((EntityMushroomCow)target).world, ((EntityMushroomCow)target).locX, ((EntityMushroomCow)target).locY + ((EntityMushroomCow)target).length, ((EntityMushroomCow)target).locZ, new ItemStack(Block.RED_MUSHROOM)));
			}
		}
	}

	@Override
	public int a(Entity var1)
	{
		return (!(var1 instanceof EntitySheep)) && (!(var1 instanceof EntityMushroomCow)) ? weaponDamage : 1;
	}

	@Override
	public boolean a(ItemStack var1, EntityLiving var2, EntityLiving var3)
	{
		if ((var3 instanceof EntityHuman))
		{
			EntityHuman var4 = (EntityHuman)var3;

			if ((var2 instanceof EntitySheep))
			{
				if (!((EntitySheep)var2).isSheared())
				{
					doShear(var1, var4.world, var4, var2);
				}

				var2.heal(1);
			}
			else if ((var2 instanceof EntityMushroomCow))
			{
				doShear(var1, var4.world, var4, var2);
				var2.heal(1);
			}
		}

		return true;
	}

	@Override
	public void doAlternate(ItemStack var1, World var2, EntityHuman var3)
	{
		EEBase.updateSwordMode(var3);
	}

	@Override
	public void doRelease(ItemStack var1, World var2, EntityHuman var3)
	{
		doSwordBreak(var1, var2, var3);
	}

	@Override
	public void doToggle(ItemStack var1, World var2, EntityHuman var3)
	{
	}
}