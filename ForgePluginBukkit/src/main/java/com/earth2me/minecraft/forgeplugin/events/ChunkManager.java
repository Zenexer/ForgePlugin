package com.earth2me.minecraft.forgeplugin.events;

import com.earth2me.minecraft.forgeplugin.ChunkLocation;
import com.earth2me.minecraft.forgeplugin.ChunkRange;
import com.earth2me.minecraft.forgeplugin.ForgePlugin;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import lombok.Getter;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitScheduler;


public final class ChunkManager implements IEventHandler
{
	private final transient ForgePlugin plugin;
	private transient Server server;
	private transient int viewDistance;
	private transient Set<ChunkLocation> persistentLocations;
	private transient Set<ChunkRange> persistentRanges;
	private transient int recheckDelayTicks;
	private transient int entitiesCheckLimit;
	private transient int chunkCleanerTask = -1;
	private transient Cleaner cleaner;
	private transient int chunkCleanTicks;

	public ChunkManager(final ForgePlugin plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public void onReload()
	{
		server = plugin.getServer();
		viewDistance = server.getViewDistance();

		final FileConfiguration config = plugin.getConfig();
		recheckDelayTicks = (int)(config.getDouble("settings/recheck-delay", 1) * 20 * 60);
		entitiesCheckLimit = config.getInt("settings/entities-check-limit", -1);
		final int maxChunksPerRange = config.getInt("settings/max-chunks-per-range", 100);
		chunkCleanTicks = (int)(config.getDouble("settings/chunk-clean-interval", 5) * 20 * 60);

		final Set<ChunkLocation> persistentLocations = new HashSet<>();
		final Set<ChunkRange> persistentRanges = new HashSet<>();

		final FileConfiguration persistentConfig = plugin.loadConfig("persistence.yml");
		if (persistentConfig == null)
		{
			plugin.getLogger().log(Level.WARNING, "Persistence configuration (persistence.yml) could not be loaded.");
		}
		else
		{
			final List<?> persistentRegions = persistentConfig.getList("persistent-regions", null);
			if (persistentRegions != null)
			{
				int itemId = 0;
				for (Object item : persistentRegions)
				{
					itemId++;

					if (item == null || !(item instanceof List<?>))
					{
						plugin.getLogger().log(Level.WARNING, String.format("Configuration: persistent-chunks contains a null or non-list item.", itemId));
						continue;
					}

					final List<?> list = (List<?>)item;
					if (list.size() != 3)
					{
						plugin.getLogger().log(Level.WARNING, String.format("Configuration: persistent-chunks item %d has the wrong number of elements in its array.", itemId));
						continue;
					}

					final Object[] elements = list.toArray();
					for (int i = 0; i < elements.length; i++)
					{
						if (elements[i] == null)
						{
							plugin.getLogger().log(Level.WARNING, String.format("Configuration: persistent-chunks item %d has a null element.", itemId));
							continue;
						}
					}

					if (!(elements[0] instanceof String))
					{
						plugin.getLogger().log(Level.WARNING, String.format("Configuration: persistent-chunks item %d has a world name that is not a String.", itemId));
						continue;
					}

					if (elements[1] instanceof Integer && elements[2] instanceof Integer)
					{
						final ChunkLocation location = ChunkLocation.parse(list);
						if (location == null)
						{
							plugin.getLogger().log(Level.WARNING, String.format("Configuration: persistent-chunks item %d uses an invalid chunk location syntax.", itemId));
							continue;
						}

						persistentLocations.add(location);
					}
					else if (elements[1] instanceof List<?> && elements[2] instanceof List<?>)
					{
						final ChunkRange range = ChunkRange.parse(list);
						if (range == null)
						{
							plugin.getLogger().log(Level.WARNING, String.format("Configuration: persistent-chunks item %d uses an invalid chunk range syntax.", itemId));
							continue;
						}

						final ChunkLocation start = range.getStart();
						final ChunkLocation end = range.getEnd();
						if ((end.getX() - start.getX()) * (end.getZ() - start.getZ()) > maxChunksPerRange)
						{
							plugin.getLogger().log(Level.WARNING, String.format("Configuration: persistent-chunks item %d contains too many chunks.  (Max: %d)", itemId, maxChunksPerRange));
							continue;
						}

						persistentRanges.add(range);
					}
					else
					{
						plugin.getLogger().log(Level.WARNING, String.format("Configuration: persistent-chunks item %d contains elements of invalid types.", itemId));
					}
				}
			}
		}

		this.persistentLocations = persistentLocations;
		this.persistentRanges = persistentRanges;

		final BukkitScheduler scheduler = server.getScheduler();

		if (chunkCleanerTask >= 0 && (scheduler.isQueued(chunkCleanerTask) || scheduler.isCurrentlyRunning(chunkCleanerTask)))
		{
			scheduler.cancelTask(chunkCleanerTask);
		}

		if (chunkCleanTicks > 0)
		{
			cleaner = createCleaner();
			cleaner.scheduleRepeating();
		}
	}

	@Override
	public void onEnabled()
	{
		final BukkitScheduler scheduler = plugin.getServer().getScheduler();

		if (persistentLocations != null)
		{
			for (final ChunkLocation location : persistentLocations)
			{
				scheduler.scheduleSyncDelayedTask(plugin, new Runnable()
				{
					@Override
					public void run()
					{
						location.getWorld().loadChunk(location.getX(), location.getZ());
					}
				});
			}
		}

		if (persistentRanges != null)
		{
			for (final ChunkRange range : persistentRanges)
			{
				final World world = range.getWorld();
				final ChunkLocation start = range.getStart();
				final ChunkLocation end = range.getEnd();
				final int startX = start.getX();
				final int startZ = start.getZ();
				final int endX = end.getX();
				final int endZ = end.getZ();

				for (int x = startX; x <= endX; x++)
				{
					for (int z = startZ; z <= endZ; z++)
					{
						final int fx = x;
						final int fz = z;
						scheduler.scheduleSyncDelayedTask(plugin, new Runnable()
						{
							@Override
							public void run()
							{
								world.loadChunk(fx, fz);
							}
						});
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkLoad(final ChunkLoadEvent e)
	{
		checkChunk(e.getChunk(), recheckDelayTicks);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChunkUnload(final ChunkUnloadEvent e)
	{
		final Chunk chunk = e.getChunk();
		if (isPersistent(chunk) || hasPlayer(chunk))
		{
			e.setCancelled(true);

			// Incoming manual override!  This is just because we are so cool and hackish and clearly nobody else
			// has any valid reason to ever contradict us.  Damn, boy, that is some bad ethics!
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{
				@Override
				public void run()
				{
					chunk.load();
				}
			}, 1);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(final PlayerMoveEvent e)
	{
		onGenericMove(e);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(final PlayerTeleportEvent e)
	{
		onGenericMove(e);
	}

	private void onGenericMove(final PlayerMoveEvent e)
	{
		final Location from = e.getFrom();
		final Location to = e.getTo();

		if (!isDifferentChunk(from, to))
		{
			return;
		}

		loadChunks(e.getPlayer(), to);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerLogin(final PlayerLoginEvent e)
	{
		loadChunks(e.getPlayer(), null);
	}

	private boolean isDifferentChunk(final Location from, final Location to)
	{
		return from.getChunk().equals(to.getChunk());
	}

	public boolean isPersistent(final Chunk chunk)
	{
		if (chunk == null)
		{
			return false;
		}

		if (persistentLocations != null)
		{
			for (ChunkLocation location : persistentLocations)
			{
				if (location != null && location.isSame(chunk))
				{
					return true;
				}
			}
		}

		if (persistentRanges != null)
		{
			for (ChunkRange range : persistentRanges)
			{
				if (range != null && range.contains(chunk))
				{
					return true;
				}
			}
		}

		return false;
	}

	@SuppressWarnings("AssignmentToMethodParameter")
	private void loadChunks(final Player player, Location loc)
	{
		if (loc == null && player == null)
		{
			return;
		}

		final Location location = loc == null ? player.getLocation() : loc;

		server.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				final World world = location.getWorld();
				final int originX = location.getBlockX() >> 4;
				final int originZ = location.getBlockZ() >> 4;

				for (int x = originX - viewDistance; x <= originX + viewDistance; x++)
				{
					for (int z = originZ - viewDistance; z <= originZ + viewDistance; z++)
					{
						final int fx = x;
						final int fz = z;
						server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
						{
							@Override
							public void run()
							{
								world.loadChunk(fx, fz, true);
							}
						});
					}
				}
			}
		});
	}

	public void checkChunk(final Chunk chunk)
	{
		server.getScheduler().scheduleAsyncDelayedTask(plugin, new Checker(chunk));
	}

	public void checkChunk(final Chunk chunk, final long ticks)
	{
		server.getScheduler().scheduleAsyncDelayedTask(plugin, new Checker(chunk), ticks);
	}

	public boolean isChunkNeeded(final Chunk chunk, Player[] players)
	{
		if (chunk == null)
		{
			return false;
		}

		if (isPersistent(chunk))
		{
			return true;
		}

		final World world = chunk.getWorld();
		final int chunkX = chunk.getX();
		final int chunkZ = chunk.getZ();

		for (Player player : players)
		{
			final Chunk chunkLocation = player.getLocation().getChunk();
			final int dx = Math.abs(chunkX - chunkLocation.getX());
			final int dz = Math.abs(chunkZ - chunkLocation.getZ());

			if (dx <= viewDistance && dz <= viewDistance)
			{
				return true;
			}
		}

		return false;
	}

	public boolean hasPlayer(final Chunk chunk)
	{
		if (chunk == null)
		{
			return false;
		}

		final Entity[] entities = chunk.getEntities();
		if (entities.length > entitiesCheckLimit)
		{
			return false;
		}

		for (Entity entity : entities)
		{
			if (entity != null && entity instanceof Player)
			{
				return true;
			}
		}

		return false;
	}

	public Cleaner createCleaner()
	{
		return new Cleaner();
	}


	public final class Cleaner implements Runnable
	{
		public void scheduleRepeating()
		{
			chunkCleanerTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, chunkCleanTicks, chunkCleanTicks);
		}

		@Override
		public void run()
		{
			final Server server = plugin.getServer();
			final BukkitScheduler scheduler = server.getScheduler();
			final World[] worlds = server.getWorlds().toArray(new World[0]);

			scheduler.scheduleAsyncDelayedTask(plugin, new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						int failCounter = 0;
						int chunkCounter = 0;

						for (World world : worlds)
						{
							final Chunk[] chunks = world.getLoadedChunks();
							failCounter += scheduleCleanup(chunks);
							chunkCounter += chunks.length;
						}

						final int failCount = failCounter;
						final int chunkCount = chunkCounter;

						scheduler.scheduleSyncDelayedTask(plugin, new Runnable()
						{
							@Override
							public void run()
							{
								final String format;
								if (failCount <= 0)
								{
									format = "%1$sScanning %3$d chunks passively.  TPS may drop temporarily.";
								}
								else if (chunkCount > 0)
								{
									format = "%1$sScanning %3$d chunks passively.  %2$sFailed to schedule %4$d chunks for scanning.";
								}
								else
								{
									format = "%2$sFailed to schedule %4$d chunks for scanning.";
								}

								server.broadcastMessage(String.format(format, ForgePlugin.NORMAL_COLOR, ForgePlugin.FAIL_COLOR, chunkCount, failCount));
							}
						});
					}
					catch (Throwable ex)
					{
						plugin.getLogger().log(Level.SEVERE, "Unable to perform chunk cleanup.", ex);

						scheduler.scheduleSyncDelayedTask(plugin, new Runnable()
						{
							@Override
							public void run()
							{
								server.broadcastMessage(ForgePlugin.FAIL_COLOR + "Chunk cleanup failed.");
							}
						});
					}
				}
			});
		}

		private int scheduleCleanup(final Chunk[] chunks)
		{
			int failed = 0;
			for (final Chunk chunk : chunks)
			{
				int taskID = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
				{
					@Override
					public void run()
					{
						checkChunk(chunk);
					}
				});

				if (taskID < 0)
				{
					failed++;
				}
			}

			return failed;
		}
	}


	private final class Checker implements Runnable
	{
		private final Chunk chunk;

		public Checker(final Chunk chunk)
		{
			this.chunk = chunk;
		}

		@Override
		public void run()
		{
			final BukkitScheduler scheduler = plugin.getServer().getScheduler();

			scheduler.scheduleSyncDelayedTask(plugin, new Runnable()
			{
				@Override
				public void run()
				{
					final Player[] players = chunk.getWorld().getPlayers().toArray(new Player[0]);

					scheduler.scheduleAsyncDelayedTask(plugin, new Runnable()
					{
						@Override
						public void run()
						{
							if (!isChunkNeeded(chunk, players))
							{
								plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
								{
									@Override
									public void run()
									{
										chunk.getWorld().unloadChunk(chunk);
									}
								});
							}
						}
					});
				}
			});
		}
	}
}
