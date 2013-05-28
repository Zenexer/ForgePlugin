package ee;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import java.util.*;
import net.minecraft.server.*;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;


public class TransTabletData extends WorldMapBase implements IInventory
{
	private final static ItemData[] sortedItems;
	public int latentEnergy = 0;
	public int currentEnergy = 0;
	public int learned = 0;
	public ItemStack[] items = new ItemStack[26];
	public boolean isMatterLocked;
	public boolean isFuelLocked;
	private boolean readTome;
	public EntityHuman player;
	private final Set<ItemData> knowledge = new HashSet<>();

	static
	{
		sortedItems = getSortedItems().toArray(new ItemData[0]);
	}

	public TransTabletData(String var1)
	{
		super(var1);
	}

	public TransTabletData(String var1, EntityHuman human)
	{
		super(var1);

		this.player = human;
	}

	private static List<ItemData> getSortedItems()
	{
		final List<ItemData> sortedItems = new ArrayList<>(Item.byId.length * 4);

		for (int itemId = 0, itemMaxData = EEMaps.getMeta(itemId); itemId < Item.byId.length; itemId++, itemMaxData = EEMaps.getMeta(itemId))
		{
			for (int itemData = 0; itemData <= itemMaxData; itemData++)
			{
				sortedItems.add(new ItemData(itemId, itemData));
			}
		}

		Collections.sort(sortedItems, new Comparator<ItemData>()
		{
			@Override
			public int compare(ItemData o1, ItemData o2)
			{
				return EEMaps.getEMC(o1.getTypeId(), o1.getData()) - EEMaps.getEMC(o2.getTypeId(), o2.getData());
			}
		});

		return unmodifiableList(sortedItems);
	}

	public void setPlayer(final EntityHuman player)
	{
		this.player = player;
	}

	public void onUpdate(World world, EntityHuman human)
	{
		if (EEProxy.isClient(world))
		{
			return;
		}

		calculateEMC();
		updateLock();
		displayResults(currentEnergy + latentEnergy);
	}

	private void updateLock()
	{
		if (latentEnergy <= 0)
		{
			unlock();
		}
	}

	public ItemStack target()
	{
		return items[8];
	}

	public boolean isOnGridBut(ItemStack var1, int var2)
	{
		for (int var3 = 10; var3 < items.length; var3++)
		{
			if ((var3 != var2) && (items[var3] != null) && (items[var3].doMaterialsMatch(var1)))
			{
				return true;
			}
		}

		return false;
	}

	public boolean isOnGrid(ItemStack var1)
	{
		for (int var2 = 10; var2 < items.length; var2++)
		{
			if ((items[var2] != null) && (items[var2].doMaterialsMatch(var1)))
			{
				return true;
			}
		}

		return false;
	}

	public int kleinEMCTotal()
	{
		int var1 = 0;

		for (int var2 = 0; var2 < 8; var2++)
		{
			if ((items[var2] != null) && ((items[var2].getItem() instanceof ItemKleinStar)))
			{
				var1 += ((ItemKleinStar)items[var2].getItem()).getKleinPoints(items[var2]);
			}
		}

		return var1;
	}

	public void displayResults(int totalEmc)
	{
		final ItemStack target = target();
		final int targetEmc = target == null ? 0 : EEMaps.getEMC(target);
		int slot = 10;
		ItemData isNot = null;

		for (int resultSlot = 10; resultSlot < items.length; resultSlot++)
		{
			if (resultSlot == 10 && target != null && targetEmc > 0 && totalEmc >= targetEmc && matchesLock(target))
			{
				int id = target.id;
				int data = target.d() ? 0 : target.getData();

				items[resultSlot] = new ItemStack(id, 1, data);

				isNot = new ItemData(id, data);
				slot = 11;

				continue;
			}

			items[resultSlot] = null;
		}

		for (int i = sortedItems.length - 1; i >= 0 && slot < items.length; i--)
		{
			final ItemData item = sortedItems[i];
			if (item == null)
			{
				continue;
			}

			final int itemEmc = EEMaps.getEMC(item.getTypeId(), item.getData());
			if (itemEmc > 0 && totalEmc >= itemEmc && !item.equals(isNot) && (target == null || itemEmc <= targetEmc) && playerKnows(item.getTypeId(), item.getData()))
			{
				final ItemStack itemStack = new ItemStack(item.getTypeId(), 1, item.getData());
				if (matchesLock(itemStack))
				{
					items[slot++] = new ItemStack(item.getTypeId(), 1, item.getData());
				}
			}
		}

		a();
	}

	public void calculateEMC()
	{
		int var1 = 0;
		boolean var2 = false;

		for (int var3 = 0; var3 < 8; var3++)
		{
			if (items[var3] != null)
			{
				if ((EEMaps.getEMC(items[var3]) == 0) && (!EEBase.isKleinStar(items[var3].id)))
				{
					rejectItem(var3, player);
				}
				else if (EEBase.isKleinStar(items[var3].id))
				{
					if ((!playerKnows(items[var3].id, items[var3].getData())) && (EEMaps.getEMC(items[var3]) > 0))
					{
						if (items[var3].id == EEItem.alchemyTome.id)
						{
							pushTome();
						}

						pushKnowledge(items[var3].id, items[var3].getData(), false);
						learned = 60;
					}

					if (latentEnergy > 0)
					{
						int var4 = ((ItemKleinStar)items[var3].getItem()).getMaxPoints(items[var3]) - ((ItemKleinStar)items[var3].getItem()).getKleinPoints(items[var3]);

						if (var4 > 0)
						{
							if (var4 > latentEnergy)
							{
								var4 = latentEnergy;
							}

							latentEnergy -= var4;
							EEBase.addKleinStarPoints(items[var3], var4);
						}
					}

					var1 += ((ItemKleinStar)items[var3].getItem()).getKleinPoints(items[var3]);
				}
				else
				{
					if ((!playerKnows(items[var3].id, items[var3].getData())) && (EEMaps.getEMC(items[var3]) > 0))
					{
						if (items[var3].id == EEItem.alchemyTome.id)
						{
							pushTome();
						}

						pushKnowledge(items[var3].id, items[var3].getData(), false);
						learned = 60;
					}

					if ((!var2) && (!isFuelLocked()) && (!isMatterLocked()))
					{
						if (EEMaps.isFuel(items[var3]))
						{
							fuelLock();
						}
						else
						{
							matterLock();
						}
					}

					if (!matchesLock(items[var3]))
					{
						rejectItem(var3, player);
					}
					else
					{
						var1 += EEMaps.getEMC(items[var3]);
					}
				}
			}
		}

		currentEnergy = var1;
	}

	public boolean matchesLock(ItemStack var1)
	{
		if (isFuelLocked())
		{
			if (EEMaps.isFuel(var1))
			{
				return true;
			}
		}
		else
		{
			if (!isMatterLocked())
			{
				return true;
			}

			if (!EEMaps.isFuel(var1))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public int getSize()
	{
		return items.length;
	}

	@Override
	public ItemStack getItem(int var1)
	{
		return items[var1];
	}

	@Override
	public ItemStack splitStack(int var1, int var2)
	{
		if (items[var1] != null)
		{
			if (items[var1].count <= var2)
			{
				ItemStack var3 = items[var1];
				items[var1] = null;
				return var3;
			}

			ItemStack var3 = items[var1].a(var2);

			if (items[var1].count == 0)
			{
				items[var1] = null;
			}

			return var3;
		}

		return null;
	}

	@Override
	public void setItem(int var1, ItemStack var2)
	{
		items[var1] = var2;

		if ((var2 != null) && (var2.count > getMaxStackSize()))
		{
			var2.count = getMaxStackSize();
		}
	}

	@Override
	public String getName()
	{
		return "Trans Tablet";
	}

	@Override
	public int getMaxStackSize()
	{
		return 64;
	}

	public void update(World world, EntityHuman human)
	{
		update();
	}

	@Override
	public void update()
	{
		onUpdate(player.world, player);
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
		isMatterLocked = var1.getBoolean("matterLock");
		isFuelLocked = var1.getBoolean("fuelLock");
		currentEnergy = var1.getInt("currentEnergy");
		latentEnergy = var1.getInt("latentEnergy");
		NBTTagList var2 = var1.getList("Items");
		items = new ItemStack[getSize()];

		for (int var3 = 0; var3 < var2.size(); var3++)
		{
			NBTTagCompound var4 = (NBTTagCompound)var2.get(var3);
			int var5 = var4.getByte("Slot") & 0xFF;

			if ((var5 >= 0) && (var5 < items.length))
			{
				items[var5] = ItemStack.a(var4);
			}
		}

		NBTTagList var8 = var1.getList("knowledge");
		knowledge.clear();

		for (int var9 = 0; var9 < var8.size(); var9++)
		{
			NBTTagCompound var10 = (NBTTagCompound)var8.get(var9);
			int var6 = var10.getInt("item");
			int var7 = var10.getInt("meta");
			pushKnowledge(var6, var7, false);
		}

		readTome = var1.getBoolean("readTome");
	}

	@Override
	public void b(NBTTagCompound var1)
	{
		var1.setBoolean("matterLock", isMatterLocked);
		var1.setBoolean("fuelLock", isFuelLocked);
		var1.setInt("currentEnergy", currentEnergy);
		var1.setInt("latentEnergy", latentEnergy);
		NBTTagList var2 = new NBTTagList();

		for (int var3 = 0; var3 < items.length; var3++)
		{
			if (items[var3] != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte)var3);
				items[var3].save(var4);
				var2.add(var4);
			}
		}

		var1.set("Items", var2);
		var1.setBoolean("readTome", readTome);

		for (final ItemData item : knowledge)
		{
			if (item != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setInt("item", item.getTypeId());
				var4.setInt("meta", item.getData());
				var2.add(var4);
			}
		}

		var1.set("knowledge", var2);
	}

	public Set<ItemData> getKnowledge()
	{
		return unmodifiableSet(knowledge);
	}

	public void pushKnowledge(int id, int rawData)
	{
		pushKnowledge(id, rawData, true);
	}

	public void pushKnowledge(int id, int rawData, boolean update)
	{
		if (Item.byId[id] != null)
		{
			final int data;
			if (Item.byId[id].g())
			{
				data = 0;
			}
			else
			{
				data = rawData;
			}

			if (!playerKnows(id, data))
			{
				knowledge.add(new ItemData(id, data));

				if (update)
				{
					a();
				}
			}
		}
	}

	public boolean playerKnows(int id, int data)
	{
		if (readTome)
		{
			return true;
		}

		return knowledge.contains(new ItemData(id, Item.byId[id].g() ? 0 : data));
	}

	public void pushTome()
	{
		readTome = true;
		a();
	}

	public long getDisplayEnergy()
	{
		return latentEnergy + currentEnergy;
	}

	public int getLatentEnergy()
	{
		return latentEnergy;
	}

	public void setLatentEnergy(int var1)
	{
		latentEnergy = var1;
		a();
	}

	public int getCurrentEnergy()
	{
		return currentEnergy;
	}

	public void setCurrentEnergy(int var1)
	{
		currentEnergy = var1;
		a();
	}

	public boolean isFuelLocked()
	{
		return isFuelLocked;
	}

	public void fuelUnlock()
	{
		isFuelLocked = false;
		a();
	}

	public void fuelLock()
	{
		isFuelLocked = true;
		a();
	}

	public boolean isMatterLocked()
	{
		return isMatterLocked;
	}

	public void matterUnlock()
	{
		isMatterLocked = false;
		a();
	}

	public void matterLock()
	{
		isMatterLocked = true;
		a();
	}

	public void unlock()
	{
		fuelUnlock();
		matterUnlock();
	}

	public void rejectItem(int var1, EntityHuman player)
	{
		if (player != null)
		{
			if (player.world != null)
			{
				if (!EEProxy.isClient(player.world))
				{
					if (getItem(var1) != null)
					{
						EntityItem var2 = new EntityItem(player.world, player.locX, player.locY - 0.5D, player.locZ, getItem(var1));
						nullStack(var1);
						var2.pickupDelay = 1;
						player.world.addEntity(var2);
					}
				}
			}
		}
	}

	private void nullStack(int var1)
	{
		items[var1] = null;
		a();
	}

	@Override
	public ItemStack splitWithoutUpdate(int var1)
	{
		if (var1 <= 8)
		{
			if (items[var1] != null)
			{
				ItemStack var2 = items[var1];
				items[var1] = null;
				return var2;
			}

			return null;
		}

		return null;
	}

	@Override
	public ItemStack[] getContents()
	{
		return items;
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