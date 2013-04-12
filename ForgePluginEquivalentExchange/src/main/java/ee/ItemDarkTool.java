package ee;

import net.minecraft.server.*;


public abstract class ItemDarkTool extends ItemEESuperTool
{
	private Block[] blocksEffectiveAgainst;
	private float efficiencyOnProperMaterial = 14.0F;

	protected ItemDarkTool(int var1, int var2, int var3, Block[] var4)
	{
		super(var1, var2);
		blocksEffectiveAgainst = var4;
		weaponDamage = (var3 + 3);
	}

	@Override
	public float getDestroySpeed(ItemStack var1, Block var2)
	{
		for (int var3 = 0; var3 < blocksEffectiveAgainst.length; var3++)
		{
			if (blocksEffectiveAgainst[var3] == var2)
			{
				return efficiencyOnProperMaterial;
			}
		}

		return 1.0F;
	}

	@Override
	public int a(Entity var1)
	{
		return weaponDamage;
	}

	@Override
	public boolean a(ItemStack var1, EntityLiving var2, EntityLiving var3)
	{
		return true;
	}

	@Override
	public boolean a(ItemStack var1, int var2, int var3, int var4, int var5, EntityLiving var6)
	{
		return true;
	}

	public boolean isFull3D()
	{
		return true;
	}

	public void doBreak(ItemStack var1, World var2, EntityHuman var3)
	{
	}

	@Override
	public void doPassive(ItemStack var1, World var2, EntityHuman var3)
	{
	}

	@Override
	public void doActive(ItemStack var1, World var2, EntityHuman var3)
	{
	}
}