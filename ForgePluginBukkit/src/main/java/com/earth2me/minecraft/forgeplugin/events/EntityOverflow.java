package com.earth2me.minecraft.forgeplugin.events;

import com.earth2me.minecraft.forgeplugin.ChunkLocation;
import com.earth2me.minecraft.forgeplugin.ForgePlugin;
import eloraam.machine.EntityPlayerFake;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import net.minecraft.server.EntityPlayer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitScheduler;


public final class EntityOverflow implements IEventHandler, Runnable
{
	private static final transient int DEFAULT_SCAN_INTERVAL = 5;
	private static final transient int DEFAULT_MAX_ENTITIES = 150;
	private static final transient int DEFAULT_MIN_ITEM_INTERVAL = -1;
	private static final transient int DEFAULT_THRESHOLD_RESET_DELAY = 30;
	private static final transient int DEFAULT_MAX_PLAYERS_BEFORE_INCREASE = 50;
	private final transient ForgePlugin plugin;
	private transient int scanInterval = DEFAULT_SCAN_INTERVAL;
	private transient int configMaxEntities = DEFAULT_MAX_ENTITIES;
	private transient AtomicInteger maxEntities = new AtomicInteger(configMaxEntities);
	private transient int minItemInterval = DEFAULT_MIN_ITEM_INTERVAL;
	private transient int thresholdResetDelay = DEFAULT_THRESHOLD_RESET_DELAY;
	private transient int maxPlayersBeforeIncrease = DEFAULT_MAX_PLAYERS_BEFORE_INCREASE;
	private int taskId = -1;
	private final transient Map<ChunkLocation, Long> lastItems = new HashMap<>();
	private AtomicInteger resetThresholdTask = new AtomicInteger(-1);

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
		scanInterval = config.getInt("entity-overflow/scan-interval", DEFAULT_SCAN_INTERVAL) * 20;
		maxEntities.set(configMaxEntities = config.getInt("entity-overflow/max-entities", DEFAULT_MAX_ENTITIES));
		minItemInterval = config.getInt("entity-overflow/min-item-interval", DEFAULT_MIN_ITEM_INTERVAL);
		thresholdResetDelay = config.getInt("entity-overflow/threshold-reset-delay", DEFAULT_THRESHOLD_RESET_DELAY) * 20;
		maxPlayersBeforeIncrease = config.getInt("entity-overflow/max-players-before-increase", DEFAULT_MAX_PLAYERS_BEFORE_INCREASE);

		final BukkitScheduler scheduler = plugin.getServer().getScheduler();

		if (taskId >= 0)
		{
			scheduler.cancelTask(taskId);
		}

		if (scanInterval > 0 && configMaxEntities > 0 && thresholdResetDelay >= 0)
		{
			taskId = scheduler.scheduleAsyncRepeatingTask(plugin, this, scanInterval, scanInterval);
			plugin.getLogger().log(Level.INFO, String.format("Scanning for entity overflows every %d ticks.", scanInterval));
		}
		else
		{
			plugin.getLogger().log(Level.INFO, "Entity overflow scanning disabled.");
		}
	}

	private boolean isExceedingThreshold(final Entity[] entities)
	{
		int localMaxEntities = maxEntities.get();

		if (entities.length < localMaxEntities)
		{
			return false;
		}

		assert thresholdResetDelay >= 0;
		assert localMaxEntities > 0;

		int playerCount = 0;
		for (final Entity entity : entities)
		{
			if (entity instanceof EntityPlayer && !(entity instanceof EntityPlayerFake))
			{
				playerCount++;
			}
		}

		if (playerCount <= maxPlayersBeforeIncrease)
		{
			assert entities.length > localMaxEntities;
			return true;
		}

		maxEntities.set(localMaxEntities = playerCount + configMaxEntities);

		final BukkitScheduler scheduler = plugin.getServer().getScheduler();

		int task = resetThresholdTask.get();
		if (task >= 0)
		{
			scheduler.cancelTask(task);
		}

		if (thresholdResetDelay == 0)
		{
			task = -1;
		}
		else
		{
			task = scheduler.scheduleAsyncDelayedTask(plugin, new Runnable()
			{
				@Override
				public void run()
				{
					maxEntities.set(configMaxEntities);
				}
			}, thresholdResetDelay);
		}

		resetThresholdTask.set(task);

		return entities.length > localMaxEntities;
	}

	@Override
	public void run()
	{
		for (final World world : plugin.getServer().getWorlds())
		{
			for (final Chunk chunk : world.getLoadedChunks())
			{
				final Entity[] entities = chunk.getEntities();

				if (!isExceedingThreshold(entities))
				{
					continue;
				}

				final int localMaxEntities = maxEntities.get();
				final Set<Entity> toRemove = new HashSet<>();
				int entityCount = entities.length;

				int level = 1;
				do
				{
					for (Entity entity : entities)
					{
						if (entity == null)
						{
							entityCount--;
							continue;
						}

						switch (level)
						{
						case 1:
							if (!(entity instanceof Item || entity instanceof ExperienceOrb))
							{
								continue;
							}
							break;

						case 2:
							if (!(entity instanceof LivingEntity && !(entity instanceof HumanEntity || entity instanceof NPC)))
							{
								continue;
							}
							break;

						case 3:
							if (!(entity instanceof Vehicle))
							{
								continue;
							}
							break;

						case 4:
							if (!(entity instanceof Projectile
								|| entity instanceof TNTPrimed
								|| entity instanceof FallingSand
								|| entity instanceof LightningStrike))
							{
								continue;
							}
							break;

						case 5:
							if (entity instanceof HumanEntity || entity instanceof NPC)
							{
								continue;
							}
							break;

						default:
							if (entity instanceof HumanEntity)
							{
								continue;
							}
							break;
						}

						if (toRemove.add(entity))
						{
							entityCount--;
						}
					}
				}
				while (++level <= 6 && entityCount > localMaxEntities);
				level--;

				removeEntities(toRemove);

				plugin.getLogger().log(Level.WARNING, String.format("Removing %d out of %d entities at level %d in chunk %s.", toRemove.size(), entities.length, level, chunk));
			}
		}
	}

	private void removeEntities(final Iterable<Entity> entities)
	{
		final BukkitScheduler scheduler = plugin.getServer().getScheduler();

		for (final Entity entity : entities)
		{
			if (entity != null)
			{
				scheduler.scheduleSyncDelayedTask(plugin, new Runnable()
				{
					@Override
					public void run()
					{
						entity.remove();
					}
				});
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkUnload(final ChunkUnloadEvent e)
	{
		lastItems.remove(new ChunkLocation(e.getChunk()));
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onItemSpawn(final ItemSpawnEvent e)
	{
		final int localMaxEntities = maxEntities.get();
		final Item item = e.getEntity();
		final Location location = e.getLocation();
		final Chunk chunk = location.getChunk();

		if (localMaxEntities > 0 && chunk.getEntities().length >= localMaxEntities)
		{
			e.setCancelled(true);
			item.remove();
			return;
		}

		if (minItemInterval > 0)
		{
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

	public void onCreatureSpawn(final CreatureSpawnEvent e)
	{
		final int localMaxEntities = maxEntities.get();

		if (localMaxEntities > 0 && e.getLocation().getChunk().getEntities().length >= localMaxEntities)
		{
			e.setCancelled(true);
			e.getEntity().remove();
		}
	}

	public void onSlimeSplit(final SlimeSplitEvent e)
	{
		final int localMaxEntities = maxEntities.get();
		final Slime entity = e.getEntity();

		if (localMaxEntities > 0 && entity.getLocation().getChunk().getEntities().length + e.getCount() - 1 >= localMaxEntities)
		{
			e.setCancelled(true);
			entity.remove();
		}
	}
}
