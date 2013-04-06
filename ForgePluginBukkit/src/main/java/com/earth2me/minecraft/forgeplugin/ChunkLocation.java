package com.earth2me.minecraft.forgeplugin;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;


public final class ChunkLocation
{
	@Getter
	private final World world;
	@Getter
	private final int x;
	@Getter
	private final int z;
	
	public ChunkLocation(final Chunk chunk)
	{
		world = chunk.getWorld();
		x = chunk.getX();
		z = chunk.getZ();
	}
	
	public ChunkLocation(final World world, final int x, final int z)
	{
		this.world = world;
		this.x = x;
		this.z = z;
	}
	
	public static ChunkLocation parse(final List<?> list)
	{
		if (list == null || list.size() != 3)
		{
			return null;
		}
		
		Object[] elements = list.toArray();
		
		if (!(elements[0] instanceof String))
		{
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
			if (elements[i] == null || !(elements[i] instanceof Integer))
			{
				return null;
			}
		}
		
		return new ChunkLocation(world, (Integer)elements[0], (Integer)elements[1]);
	}
	
	public static ChunkLocation parse(final World world, final List<?> list)
	{
		if (list == null || list.size() != 2)
		{
			return null;
		}
		
		Object[] elements = list.toArray();
		
		for (int i = 0; i < elements.length; i++)
		{
			if (elements[i] == null || !(elements[i] instanceof Integer))
			{
				return null;
			}
		}
		
		return new ChunkLocation(world, (Integer)elements[0], (Integer)elements[1]);
	}
	
	public boolean isSame(final Chunk chunk)
	{
		if (chunk == null)
		{
			return world == null;
		}
		
		return Objects.equals(world, chunk.getWorld()) && x == chunk.getX() && z == chunk.getZ();
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj != null && obj instanceof ChunkLocation)
		{
			final ChunkLocation other = (ChunkLocation)obj;
			return Objects.equals(world, other.world) && x == other.x && z == other.z;
		}
		
		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		int hash = 3;
		hash = 89 * hash + Objects.hashCode(world);
		hash = 89 * hash + x;
		hash = 89 * hash + z;
		return hash;
	}

	@Override
	public String toString()
	{
		return String.format("{%s%d, %d}", world == null ? "" : world.getName() + ": ", x, z);
	}
}
