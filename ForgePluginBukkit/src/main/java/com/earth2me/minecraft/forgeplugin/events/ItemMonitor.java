package com.earth2me.minecraft.forgeplugin.events;

import com.earth2me.minecraft.forgeplugin.ForgePlugin;
import com.earth2me.minecraft.forgeplugin.ItemData;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scheduler.BukkitScheduler;


public final class ItemMonitor implements IEventHandler
{
	private final static String[] lists = new String[]
	{
		"all",
		"use",
		"craft",
		"build",
	};
	
	private static final transient int DEFAULT_INVENTORY_SCAN_INTERVAL = 20;
	private final transient ForgePlugin plugin;
	private transient int inventoryScanInterval = DEFAULT_INVENTORY_SCAN_INTERVAL;
	private transient int inventoryScanTask = -1;
	private transient Set<ItemData> bannedAll = new HashSet<>();
	private transient Set<ItemData> bannedUse = new HashSet<>();
	private transient Set<ItemData> bannedCraft = new HashSet<>();
	private transient Set<ItemData> bannedBuild = new HashSet<>();

	public ItemMonitor(final ForgePlugin plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public void onEnabled()
	{
	}
	
	

	@Override
	public void onReload()
	{
		for (final String list : lists)
		{
			final String path = "banned-items/" + list;
			
			try
			{
				final Set<ItemData> itemSet = getItemSet(path);
				
				switch (list)
				{
				case "all":
					bannedAll = itemSet;
					break;
					
				case "use":
					bannedUse = itemSet;
					break;
					
				case "craft":
					bannedCraft = itemSet;
					break;
					
				case "build":
					bannedBuild = itemSet;
					break;
				}
			}
			catch (final Throwable ex)
			{
				plugin.getLogger().log(Level.SEVERE, String.format("Invalid %s list.  Check your YAML syntax.", path));
			}
		}
			
		final FileConfiguration config = plugin.getConfig();
		inventoryScanInterval = config.getInt("settings/inventory-scan-interval", DEFAULT_INVENTORY_SCAN_INTERVAL);

		final BukkitScheduler scheduler = plugin.getServer().getScheduler();
		if (inventoryScanTask >= 0 && scheduler.isCurrentlyRunning(inventoryScanTask))
		{
			scheduler.cancelTask(inventoryScanTask);
		}

		if (inventoryScanInterval > 0)
		{
			inventoryScanTask = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable()
			{
				@Override
				public void run()
				{
					for (final Player player : plugin.getServer().getOnlinePlayers())
					{
						if (isBannedAll(new ItemData(player.getItemInHand())))
						{
							player.setItemInHand(null);
						}
					}
				}
			}, inventoryScanInterval, inventoryScanInterval);

			if (inventoryScanTask >= 0)
			{
				plugin.getLogger().log(Level.INFO, String.format("Scanning held items every %d ticks.", inventoryScanInterval));
			}
		}
		else
		{
			plugin.getLogger().log(Level.INFO, "Not scanning held items at a set interval.");
		}
	}

	private Set<ItemData> getItemSet(final String path)
	{
		final List<?> itemList = plugin.getConfig().getList(path);
		final Set<ItemData> items = new HashSet<>();

		if (itemList != null && !itemList.isEmpty())
		{
			for (final Object i : itemList)
			{
				if (i == null)
				{
					continue;
				}

				final String[] tokens = i.toString().split(" ", 2);
				if (tokens.length < 1)
				{
					continue;
				}

				final ItemData item = ItemData.parse(tokens[0]);
				if (item != null)
				{
					items.add(item);
				}
			}
		}

		return items;
	}

	public boolean isBannedAll(final ItemData item)
	{
		if (item.getId() == 0)
		{
			return false;
		}

		return bannedAll.contains(item) || bannedAll.contains(item.getAnyData());
	}

	public boolean isBannedUse(final ItemData item)
	{
		if (item.getId() == 0)
		{
			return false;
		}

		return bannedUse.contains(item) || bannedUse.contains(item.getAnyData());
	}

	public boolean isBannedCraft(final ItemData item)
	{
		if (item.getId() == 0)
		{
			return false;
		}

		return bannedCraft.contains(item) || bannedCraft.contains(item.getAnyData());
	}

	private boolean isBannedBuild(final ItemData item)
	{
		if (item.getId() == 0)
		{
			return false;
		}

		return bannedBuild.contains(item) || bannedBuild.contains(item.getAnyData());
	}

	private boolean checkBlock(final Block block)
	{
		final ItemData data = new ItemData(block);
		if (isBannedBuild(data) || isBannedAll(data))
		{
			block.setType(Material.AIR);
			return true;
		}

		return false;
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onEntityInteract(final EntityInteractEvent e)
	{
		final Block block = e.getBlock();
		
		if (checkBlock(block))
		{
			e.setCancelled(true);
		}
		
		final ItemData data = new ItemData(block);
		if (isBannedUse(data) || isBannedAll(data))
		{
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onEntityChangeBlock(final EntityChangeBlockEvent e)
	{
		checkBlock(e.getBlock());
		
		final ItemData data = new ItemData(e.getTo().getId(), -1);
		if (isBannedUse(data) || isBannedAll(data))
		{
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	public void onItemSpawn(final ItemSpawnEvent e)
	{
		final Item item = e.getEntity();
		final ItemData data = new ItemData(item);
		if (isBannedAll(data))
		{
			e.setCancelled(true);
			item.remove();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onBlockCanBuild(final BlockCanBuildEvent e)
	{
		// This is most likely the already existing block, so should usually be air.
		checkBlock(e.getBlock());

		final ItemData data = new ItemData(e.getMaterialId(), -1);
		if (isBannedBuild(data) || isBannedAll(data))
		{
			e.setBuildable(false);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onBlockPlace(final BlockPlaceEvent e)
	{
		checkBlock(e.getBlockAgainst());
		checkBlock(e.getBlock());

		if (checkBlock(e.getBlockPlaced()))
		{
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onCraftItem(final CraftItemEvent e)
	{
		final ItemData data = new ItemData(e.getCurrentItem());
		if (isBannedCraft(data) || isBannedAll(data))
		{
			e.setResult(Event.Result.DENY);
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onFurnaceSmelt(final FurnaceSmeltEvent e)
	{
		final ItemData data = new ItemData(e.getResult());
		if (isBannedCraft(data) || isBannedAll(data))
		{
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onInventoryClick(final InventoryClickEvent e)
	{
		if (isBannedAll(new ItemData(e.getCurrentItem())))
		{
			e.setResult(Event.Result.DENY);
			e.setCancelled(true);
		}

		final InventoryView inventory = e.getView();

		if (isBannedAll(new ItemData(e.getCursor())))
		{
			inventory.setCursor(null);
			e.setResult(Event.Result.DENY);
			e.setCancelled(true);
		}

		final int slot = e.getSlot();
		if (isBannedAll(new ItemData(inventory.getItem(slot))))
		{
			inventory.setItem(slot, null);
			e.setResult(Event.Result.DENY);
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onPlayerDropItem(final PlayerDropItemEvent e)
	{
		final Item item = e.getItemDrop();
		if (isBannedAll(new ItemData(item)))
		{
			item.remove();
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onPlayerBucketFill(final PlayerBucketFillEvent e)
	{
		final ItemData data = new ItemData(e.getItemStack());
		if (isBannedCraft(data) || isBannedAll(data))
		{
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent e)
	{
		checkBlock(e.getBlockClicked());

		final ItemData data = new ItemData(e.getItemStack());
		if (isBannedAll(data) || isBannedUse(data))
		{
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onPlayerPickupItem(final PlayerPickupItemEvent e)
	{
		final Item item = e.getItem();
		if (isBannedAll(new ItemData(item)))
		{
			item.remove();
			e.setCancelled(true);
		}
	}

	// For newer versions of Bukkit
	/*@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	 public void onPlayerItemConsume(final PlayerItemConsumeEvent e)
	 {
	 final ItemStack item = e.getItem();
	 final ItemData data = new ItemData(item);
	 if (isBannedAll(data))
	 {
	 item.setType(Material.AIR);
	 e.setCancelled(true);
	 }
	 else if (isBannedUse(data))
	 {
	 e.setCancelled(true);
	 }
	 }*/
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onPlayerItemHeld(final PlayerItemHeldEvent e)
	{
		final Inventory inventory = e.getPlayer().getInventory();
		final int[] slots = new int[]
		{
			e.getNewSlot(), e.getPreviousSlot()
		};

		for (int i = 0; i < slots.length; i++)
		{
			if (isBannedAll(new ItemData(inventory.getItem(slots[i]))))
			{
				inventory.setItem(slots[i], null);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onPlayerInteract(final PlayerInteractEvent e)
	{
		if (e.hasBlock() && checkBlock(e.getClickedBlock()))
		{
			e.setCancelled(true);
		}

		if (e.hasItem())
		{
			final ItemData data = new ItemData(e.getItem());
			if (isBannedAll(data))
			{
				e.setCancelled(true);
			}
			else if (isBannedUse(data))
			{
				e.setCancelled(true);
			}
		}
	}
}
