package com.earth2me.minecraft.forgeplugin.events;

import com.earth2me.minecraft.forgeplugin.ChunkLocation;
import com.earth2me.minecraft.forgeplugin.ChunkRange;
import com.earth2me.minecraft.forgeplugin.ForgePlugin;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
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

		recheckDelayTicks = config.getInt("settings/recheck-delay", 3) * 20 * 60; // Default: Three minutes
		entitiesCheckLimit = config.getInt("settings/entities-check-limit", -1);
		final int maxChunksPerRange = config.getInt("settings/max-chunks-per-range", 100);

		final Set<ChunkLocation> persistentLocations = new HashSet<>();
		final Set<ChunkRange> persistentRanges = new HashSet<>();

		final List<?> persistentChunks = config.getList("persistent-chunks");
		if (persistentChunks != null)
		{
			int itemId = 0;
			for (Object item : persistentChunks)
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

		this.persistentLocations = persistentLocations;
		this.persistentRanges = persistentRanges;
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
		checkSync(e.getChunk(), recheckDelayTicks);
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
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
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

	private void loadChunks(final Player player, Location location)
	{
		if (location == null && player == null)
		{
			return;
		}

		if (location == null)
		{
			location = player.getLocation();
		}

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

	public void checkSync(final Chunk chunk)
	{
		server.getScheduler().scheduleSyncDelayedTask(plugin, new Checker(chunk));
	}

	public void checkSync(final Chunk chunk, final long ticks)
	{
		server.getScheduler().scheduleSyncDelayedTask(plugin, new Checker(chunk), ticks);
	}

	public boolean isChunkNeeded(final Chunk chunk)
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

		for (Player player : world.getPlayers())
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
			if (!isChunkNeeded(chunk))
			{
				chunk.getWorld().unloadChunk(chunk);
			}
		}
	}
}
