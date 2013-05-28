package ee;

import net.minecraft.server.*;


public abstract class ItemRedTool extends ItemEESuperTool
{
	private Block[] blocksEffectiveAgainst;
	private float efficiencyOnProperMaterial = 18.0F;
	private int damageVsEntity;

	protected ItemRedTool(int idMinus256, int maxCharge, int weaponDamage, Block[] blocksEffectiveAgainst)
	{
		super(idMinus256, maxCharge);
		this.blocksEffectiveAgainst = blocksEffectiveAgainst;
		this.weaponDamage = weaponDamage + 5;
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

		return 2.0F;
	}

	@Override
	public boolean a(ItemStack var1, int var2, int var3, int var4, int var5, EntityLiving var6)
	{
		return true;
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