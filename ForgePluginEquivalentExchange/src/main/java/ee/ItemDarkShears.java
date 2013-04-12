package ee;

import net.minecraft.server.*;


public class ItemDarkShears extends ItemDarkTool
{
	private static Block[] blocksEffectiveAgainst =
	{
		Block.LEAVES, Block.WEB, Block.WOOL
	};
	private static boolean leafHit;
	private static boolean vineHit;

	public ItemDarkShears(int var1)
	{
		super(var1, 2, 9, blocksEffectiveAgainst);
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

	@Override
	public float getDestroySpeed(ItemStack var1, Block var2)
	{
		return var2.id == Block.VINE.id || var2.id == Block.LEAVES.id || var2.id == Block.WEB.id ? 15F : var2.id != Block.WOOL.id ? super.getDestroySpeed(var1, var2) : 5F;
	}

	@Override
	public void doBreak(ItemStack var1, World var2, EntityHuman var3)
	{
		if (chargeLevel(var1) > 0)
		{
			int startX = (int)EEBase.playerX(var3);
			int startY = (int)EEBase.playerY(var3);
			int startZ = (int)EEBase.playerZ(var3);
			cleanDroplist(var1);

			if (chargeLevel(var1) < 1)
			{
				return;
			}

			var3.C_();
			var2.makeSound(var3, "flash", 0.8F, 1.5F);

			for (int offsetX = -(chargeLevel(var1) + 2); offsetX <= chargeLevel(var1) + 2; offsetX++)
			{
				for (int offsetY = -(chargeLevel(var1) + 2); offsetY <= chargeLevel(var1) + 2; offsetY++)
				{
					for (int offsetZ = -(chargeLevel(var1) + 2); offsetZ <= chargeLevel(var1) + 2; offsetZ++)
					{
						int var10 = var2.getTypeId(startX + offsetX, startY + offsetY, startZ + offsetZ);

						if ((EEMaps.isLeaf(var10)) || (var10 == Block.VINE.id) || (var10 == Block.WEB.id) || (var10 == Block.LONG_GRASS.id) || (var10 == Block.DEAD_BUSH.id))
						{
							if (getFuelRemaining(var1) < 1)
							{
								ConsumeReagent(var1, var3, false);
							}

							if (getFuelRemaining(var1) > 0)
							{
								int var11 = var2.getData(startX + offsetX, startY + offsetY, startZ + offsetZ);
								ItemStack var12 = new ItemStack(var10, 1, var11 & (var10 == Block.LEAVES.id ? 3 : 15));
								scanBlockAndBreak(var2, var1, var3, startX + offsetX, startY + offsetY, startZ + offsetZ, var12);
							}
						}
					}
				}
			}

			ejectDropList(var2, var1, startX, startY, startZ);
		}
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
			boolean var8 = false;
			cleanDroplist(stack);

			if (chargeLevel(stack) < 1)
			{
				return false;
			}

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

						if (((EEMaps.isLeaf(id)) || (id == Block.VINE.id) || (id == Block.WEB.id) || (id == Block.LONG_GRASS.id) || (id == Block.DEAD_BUSH.id)) && (getFuelRemaining(stack) < 1))
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

			ejectDropList(world, stack, startX, startY, startZ);
		}

		return false;
	}

	public void doShear(ItemStack var1, World var2, EntityHuman var3, Entity var4)
	{
		if (chargeLevel(var1) > 0)
		{
			boolean var5 = false;
			int var6 = 0;

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

			while ((getFuelRemaining(var1) >= 10) && (var6 < chargeLevel(var1)))
			{
				setShort(var1, "fuelRemaining", getFuelRemaining(var1) - 10);
				var6++;

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

			if (var6 > 0)
			{
				var3.C_();
				var2.makeSound(var3, "flash", 0.8F, 1.5F);
				int var7 = 1 * var6;

				if ((var4 instanceof EntitySheep))
				{
					if (var2.random.nextInt(100) < var7)
					{
						EntitySheep var8 = new EntitySheep(var2);
						double var9 = var4.locX - var3.locX;
						double var11 = var4.locZ - var3.locZ;

						if (var9 < 0.0D)
						{
							var9 *= -1.0D;
						}

						if (var11 < 0.0D)
						{
							var11 *= -1.0D;
						}

						var9 += var4.locX;
						var11 += var4.locZ;
						double var13 = var4.locY;

						for (int var15 = -5; var15 <= 5; var15++)
						{
							if ((var2.getTypeId((int)var9, (int)var13 + var15, (int)var11) != 0) && (var2.getTypeId((int)var9, (int)var13 + var15 + 1, (int)var11) == 0))
							{
								var8.setPosition(var9, var13 + var15 + 1.0D, var11);
								var8.setColor(((EntitySheep)var4).getColor());
								var2.addEntity(var8);
								break;
							}
						}
					}

					((EntitySheep)var4).setSheared(true);
					int var19 = 3 + var2.random.nextInt(2) + chargeLevel(var1) / 8;

					for (int var10 = 0; var10 < var19; var10++)
					{
						var2.addEntity(new EntityItem(var2, var3.locX, var3.locY, var3.locZ, new ItemStack(Block.WOOL.id, var19, ((EntitySheep)var4).getColor())));
					}
				}
				else if ((var4 instanceof EntityMushroomCow))
				{
					if (var2.random.nextInt(100) < var7)
					{
						EntityMushroomCow var18 = new EntityMushroomCow(var2);
						double var9 = var4.locX - var3.locX;
						double var11 = var4.locZ - var3.locZ;

						if (var9 < 0.0D)
						{
							var9 *= -1.0D;
						}

						if (var11 < 0.0D)
						{
							var11 *= -1.0D;
						}

						var9 += var4.locX;
						var11 += var4.locZ;
						double var13 = var4.locY;

						for (int var15 = -5; var15 <= 5; var15++)
						{
							if ((var2.getTypeId((int)var9, (int)var13 + var15, (int)var11) != 0) && (var2.getTypeId((int)var9, (int)var13 + var15 + 1, (int)var11) == 0))
							{
								var18.setPosition(var9, var13 + var15 + 1.0D, var11);
								var2.addEntity(var18);
								break;
							}
						}
					}

					((EntityMushroomCow)var4).die();
					EntityCow var20 = new EntityCow(var2);
					var20.setPositionRotation(var4.locX, var4.locY, var4.locZ, var4.yaw, var4.pitch);
					var20.setHealth(((EntityMushroomCow)var4).getHealth());
					var20.V = ((EntityMushroomCow)var4).V;
					var2.addEntity(var20);
					var2.a("largeexplode", var4.locX, var4.locY + var4.length / 2.0F, var4.locZ, 0.0D, 0.0D, 0.0D);
					int var23 = 5 + var2.random.nextInt(2) + chargeLevel(var1) / 8;
					Object var22 = null;

					for (int var24 = 0; var24 < var23; var24++)
					{
						var2.addEntity(new EntityItem(var2, var3.locX, var3.locY, var3.locZ, new ItemStack(Block.RED_MUSHROOM, var23)));
					}
				}
			}
		}
		else if ((var4 instanceof EntitySheep))
		{
			((EntitySheep)var4).setSheared(true);
			int var6 = 3 + var2.random.nextInt(2);
			EntityItem var17 = null;

			for (int var19 = 0; var19 < var6; var19++)
			{
				var2.addEntity(new EntityItem(var2, var3.locX, var3.locY, var3.locZ, new ItemStack(Block.WOOL.id, var6, ((EntitySheep)var4).getColor())));
			}
		}
		else if ((var4 instanceof EntityMushroomCow))
		{
			((EntityMushroomCow)var4).die();
			EntityCow var16 = new EntityCow(((EntityMushroomCow)var4).world);
			var16.setPositionRotation(((EntityMushroomCow)var4).locX, ((EntityMushroomCow)var4).locY, ((EntityMushroomCow)var4).locZ, ((EntityMushroomCow)var4).yaw, ((EntityMushroomCow)var4).pitch);
			var16.setHealth(((EntityMushroomCow)var4).getHealth());
			var16.V = ((EntityMushroomCow)var4).V;
			((EntityMushroomCow)var4).world.addEntity(var16);
			((EntityMushroomCow)var4).world.a("largeexplode", ((EntityMushroomCow)var4).locX, ((EntityMushroomCow)var4).locY + ((EntityMushroomCow)var4).length / 2.0F, ((EntityMushroomCow)var4).locZ, 0.0D, 0.0D, 0.0D);

			for (int var6 = 0; var6 < 5; var6++)
			{
				((EntityMushroomCow)var4).world.addEntity(new EntityItem(((EntityMushroomCow)var4).world, ((EntityMushroomCow)var4).locX, ((EntityMushroomCow)var4).locY + ((EntityMushroomCow)var4).length, ((EntityMushroomCow)var4).locZ, new ItemStack(Block.RED_MUSHROOM)));
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