package com.earth2me.minecraft.forgeplugin.events;

import com.earth2me.minecraft.forgeplugin.ItemData;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;


public final class ItemMonitor implements Listener
{
	@Getter
	@Setter
	private transient Set<ItemData> bannedAll = new HashSet<>();
	@Getter
	@Setter
	private transient Set<ItemData> bannedUse = new HashSet<>();
	@Getter
	@Setter
	private transient Set<ItemData> bannedCraft = new HashSet<>();
	@Getter
	@Setter
	private transient Set<ItemData> bannedBuild = new HashSet<>();

	private boolean isBannedAll(final ItemData item)
	{
		if (item.getId() == 0)
		{
			return false;
		}

		return bannedAll.contains(item) || bannedAll.contains(item.getAnyData());
	}

	private boolean isBannedUse(final ItemData item)
	{
		if (item.getId() == 0)
		{
			return false;
		}

		return bannedUse.contains(item) || bannedUse.contains(item.getAnyData());
	}

	private boolean isBannedCraft(final ItemData item)
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
