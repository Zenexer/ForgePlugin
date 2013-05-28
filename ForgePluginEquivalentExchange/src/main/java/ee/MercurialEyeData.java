package ee;

import java.util.Collections;
import java.util.List;
import net.minecraft.server.*;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;


public class MercurialEyeData extends WorldMapBase
	implements IInventory
{
	public boolean markForUpdate;
	public static final String prefix = "eye";
	public static final String prefix_ = "eye_";
	public ItemStack[] eyeContents = new ItemStack[2];
	public EntityHuman player;

	public MercurialEyeData(String var1)
	{
		super(var1);
	}

	public void onUpdate(World var1, EntityHuman var2)
	{
		player = var2;

		if (markForUpdate)
		{
			a();
		}
	}

	@Override
	public int getSize()
	{
		return 2;
	}

	@Override
	public ItemStack getItem(int var1)
	{
		return eyeContents[var1];
	}

	@Override
	public ItemStack splitStack(int var1, int var2)
	{
		if (eyeContents[var1] != null)
		{
			if (eyeContents[var1].count <= var2)
			{
				ItemStack var3 = eyeContents[var1];
				eyeContents[var1] = null;
				update();
				return var3;
			}

			ItemStack var3 = eyeContents[var1].a(var2);

			if (eyeContents[var1].count == 0)
			{
				eyeContents[var1] = null;
			}

			update();
			return var3;
		}

		return null;
	}

	@Override
	public void setItem(int var1, ItemStack var2)
	{
		if ((var2 != null) && (var2.id == EEItem.mercurialEye.id) && (player != null))
		{
			EntityItem var3 = new EntityItem(player.world, player.locX, player.locY, player.locZ, var2);
			player.world.addEntity(var3);
		}
		else
		{
			eyeContents[var1] = var2;

			if ((var2 != null) && (var2.count > getMaxStackSize()))
			{
				var2.count = getMaxStackSize();
			}

			update();
		}
	}

	@Override
	public String getName()
	{
		return "Mercurial Eye";
	}

	@Override
	public int getMaxStackSize()
	{
		return 64;
	}

	@Override
	public void update()
	{
		a();
	}

	@Override
	public boolean a(EntityHuman var1)
	{
		return true;
	}

	@Override
	public void f()
	{
	}

	@Override
	public void g()
	{
	}

	@Override
	public void a(NBTTagCompound var1)
	{
		NBTTagList var2 = var1.getList("Items");
		eyeContents = new ItemStack[2];

		for (int var3 = 0; var3 < var2.size(); var3++)
		{
			NBTTagCompound var4 = (NBTTagCompound)var2.get(var3);
			int var5 = var4.getByte("Slot") & 0xFF;

			if ((var5 >= 0) && (var5 < eyeContents.length))
			{
				eyeContents[var5] = ItemStack.a(var4);
			}
		}
	}

	@Override
	public void b(NBTTagCompound var1)
	{
		NBTTagList var2 = new NBTTagList();

		for (int var3 = 0; var3 < eyeContents.length; var3++)
		{
			if (eyeContents[var3] != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte)var3);
				eyeContents[var3].save(var4);
				var2.add(var4);
			}
		}

		var1.set("Items", var2);
	}

	@Override
	public ItemStack splitWithoutUpdate(int var1)
	{
		return null;
	}

	@Override
	public ItemStack[] getContents()
	{
		return eyeContents;
	}

	@Override
	public void onOpen(CraftHumanEntity who)
	{
	}

	@Override
	public void onClose(CraftHumanEntity who)
	{
	}

	@Override
	public List<HumanEntity> getViewers()
	{
		return Collections.emptyList();
	}

	@Override
	public InventoryHolder getOwner()
	{
		return null;
	}

	@Override
	public void setMaxStackSize(int size)
	{
	}
}