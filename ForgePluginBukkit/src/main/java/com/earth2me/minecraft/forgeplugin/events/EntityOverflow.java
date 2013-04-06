package com.earth2me.minecraft.forgeplugin.events;

import com.earth2me.minecraft.forgeplugin.ChunkLocation;
import com.earth2me.minecraft.forgeplugin.ForgePlugin;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitScheduler;


public final class EntityOverflow implements IEventHandler, Runnable
{
	private static final transient int DEFAULT_SCAN_INTERVAL = 60;
	private static final transient int DEFAULT_MAX_ENTITIES = 300;
	private static final transient int DEFAULT_MIN_ITEM_INTERVAL = -1;
	private final transient ForgePlugin plugin;
	private transient int scanInterval = DEFAULT_SCAN_INTERVAL;
	private transient int maxEntities = DEFAULT_MAX_ENTITIES;
	private transient int minItemInterval = DEFAULT_MIN_ITEM_INTERVAL;
	private int taskId = -1;
	private final transient Map<ChunkLocation, Long> lastItems = new HashMap<>();

	public EntityOverflow(final ForgePlugin plugin)
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
		lastItems.clear();

		final FileConfiguration config = plugin.getConfig();
		scanInterval = config.getInt("entity-overflow/scan-interval", DEFAULT_SCAN_INTERVAL);
		maxEntities = config.getInt("entity-overflow/max-entities", DEFAULT_MAX_ENTITIES);
		minItemInterval = config.getInt("entity-overflow/min-item-interval", DEFAULT_MIN_ITEM_INTERVAL);

		final BukkitScheduler scheduler = plugin.getServer().getScheduler();

		if (taskId >= 0 && scheduler.isCurrentlyRunning(taskId))
		{
			scheduler.cancelTask(taskId);
		}

		scheduler.scheduleSyncRepeatingTask(plugin, this, scanInterval, scanInterval);
	}

	@Override
	public void run()
	{
		for (final World world : plugin.getServer().getWorlds())
		{
			for (final Chunk chunk : world.getLoadedChunks())
			{
				final Entity[] entities = chunk.getEntities();
				if (entities.length > maxEntities)
				{
					removeEntities(entities);
				}
			}
		}
	}

	private void removeEntities(final Entity[] entities)
	{
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				for (final Entity entity : entities)
				{
					if (entity != null && entity instanceof HumanEntity)
					{
						continue;
					}

					entity.remove();
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkUnload(final ChunkUnloadEvent e)
	{
		lastItems.remove(new ChunkLocation(e.getChunk()));
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onItemSpawn(final ItemSpawnEvent e)
	{
		if (minItemInterval <= 0)
		{
			return;
		}
		
		final Item item = e.getEntity();
		final Location location = e.getLocation();
		final Chunk chunk = location.getChunk();
		final ChunkLocation chunkLocation = new ChunkLocation(chunk);
		final long time = chunk.getWorld().getTime();
		final Long previousTime = lastItems.put(chunkLocation, time);

		if (previousTime != null)
		{
			if (time - previousTime < minItemInterval)
			{
				e.setCancelled(true);
				item.remove();
			}
		}
	}
}
