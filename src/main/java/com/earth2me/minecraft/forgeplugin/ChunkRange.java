package com.earth2me.minecraft.forgeplugin;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;


public final class ChunkRange
{
	@Getter
	private final World world;
	@Getter
	private final ChunkLocation start;
	@Getter
	private final ChunkLocation end;
	
	public ChunkRange(final World world, final int x1, final int z1, final int x2, final int z2)
	{
		this.world = world;
		
		final int startX, endX, startZ, endZ;
		
		if (x1 > x2)
		{
			startX = x2;
			endX = x1;
		}
		else
		{
			startX = x1;
			endX = x2;
		}
		
		if (z1 > z2)
		{
			startZ = z2;
			endZ = z1;
		}
		else
		{
			startZ = z1;
			endZ = z2;
		}
		
		start = new ChunkLocation(world, startX, startZ);
		end = new ChunkLocation(world, endX, endZ);
	}
	
	public static ChunkRange parse(final List<?> list)
	{
		if (list == null || list.size() != 3)
		{
			Bukkit.getLogger().log(Level.WARNING, String.format("List is wrong size: %d", list == null ? -1 : list.size()));
			return null;
		}
		
		Object[] elements = list.toArray();
		
		if (elements[0] == null || !(elements[0] instanceof String))
		{
			Bukkit.getLogger().log(Level.WARNING, String.format("First element is not text; must be a world name."));
			return null;
		}
		
		final World world = Bukkit.getWorld((String)elements[0]);
		if (world == null)
		{
			Bukkit.getLogger().log(Level.WARNING, String.format("Invalid world ID: %s", elements[0]));
			return null;
		}
		
		for (int i = 1; i < elements.length; i++)
		{
			if (elements[i] == null || !(elements[i] instanceof List<?>))
			{
			Bukkit.getLogger().log(Level.WARNING, String.format("Element %d is not a coordinate list.", i));
				return null;
			}
		}
		
		final ChunkLocation[] points = new ChunkLocation[2];
		for (int i = 0; i < points.length; i++)
		{
			points[i] = ChunkLocation.parse(world, (List<?>)elements[i + 1]);
			
			if (points[i] == null)
			{
				Bukkit.getLogger().log(Level.WARNING, String.format("Element %d is not a valid coordinate pair.", i + 1));
				return null;
			}
		}
		
		return new ChunkRange(world, points[0].getX(), points[0].getZ(), points[1].getX(), points[1].getZ());
	}
	
	public boolean contains(final Location location)
	{
		return contains(location.getChunk());
	}
	
	public boolean contains(final Chunk chunk)
	{
		return contains(chunk.getWorld(), chunk.getX(), chunk.getZ());
	}
	
	public boolean contains(final World world, final int chunkX, final int chunkZ)
	{
		if (world != this.world)
		{
			return false;
		}
		
		return chunkX >= start.getX() && chunkX <= end.getX() && chunkZ >= start.getZ() && chunkZ <= end.getZ();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof ChunkRange)
		{
			final ChunkRange other = (ChunkRange)obj;
			return Objects.equals(world, other) && Objects.equals(start, other.start) && Objects.equals(end, other.end);
		}
		
		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 59 * hash + Objects.hashCode(world);
		hash = 59 * hash + Objects.hashCode(start);
		hash = 59 * hash + Objects.hashCode(end);
		return hash;
	}
	
	
}
