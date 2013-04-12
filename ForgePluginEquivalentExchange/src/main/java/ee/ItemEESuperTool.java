package ee;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.List;
import net.minecraft.server.*;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.BlockPlaceEvent;


public abstract class ItemEESuperTool extends ItemEECharged
{
	public ItemEESuperTool(final int idMinus256, final int maxCharge)
	{
		super(idMinus256, maxCharge);
	}
	
	protected boolean tryBuild(final World world, final EntityHuman human, final int x, final int y, final int z, final Block block)
	{
		return tryBuild(world, human, x, y, z, block, 0);
	}
	
	protected boolean tryBuild(final World world, final EntityHuman human, final int x, final int y, final int z, final Block block, final int data)
	{
		return tryBuild(world, human, x, y, z, block.id, data);
	}
	
	protected boolean tryBuild(final World world, final EntityHuman human, final int x, final int y, final int z, final int id)
	{
		return tryBuild(world, human, x, y, z, id, 0);
	}
	
	protected boolean tryBuild(final World world, final EntityHuman human, final int x, final int y, final int z, final int id, final int data)
	{
		final CraftBlockState blockState = CraftBlockState.getBlockState(world, x, y, z);
		final int origId = world.getTypeId(x, y, z);
		final int origData = world.getData(x, y, z);
		
		if (!world.setTypeIdAndData(x, y, z, id, data))
		{
			return false;
		}
		
		final BlockPlaceEvent event = CraftEventFactory.callBlockPlaceEvent(world, human, blockState, x, y, z);
		if (event.isCancelled() || !event.canBuild())
		{
			world.setRawTypeIdAndData(x, y, z, origId, origData);
			return false;
		}
		
		return true;
	}
	
	protected boolean tryBreak(final World world, final EntityHuman human, final int x, final int y, final int z)
	{
		if (!(human instanceof EntityPlayer))
		{
			return false;
		}
		
		final EntityPlayer player = (EntityPlayer)human;
		return player.itemInWorldManager.breakBlock(x, y, z);
	}

	protected boolean scanBlockAndBreak(final World world, final ItemStack stack, final EntityHuman human, final int x, final int y, final int z)
	{
		return scanBlockAndBreak(world, stack, human, x, y, z, (Iterator<ItemStack>)null);
	}

	protected boolean scanBlockAndBreak(final World world, final ItemStack stack, final EntityHuman human, final int x, final int y, final int z, final ItemStack... dropsOverride)
	{
		return scanBlockAndBreak(world, stack, human, x, y, z, Iterators.forArray(dropsOverride));
	}
	
	protected boolean scanBlockAndBreak(final World world, final ItemStack stack, final EntityHuman human, final int x, final int y, final int z, final Iterable<ItemStack> dropsOverride)
	{
		return scanBlockAndBreak(world, stack, human, x, y, z, dropsOverride.iterator());
	}
	
	protected boolean scanBlockAndBreak(final World world, final ItemStack stack, final EntityHuman human, final int x, final int y, final int z, final Iterator<ItemStack> dropsOverride)
	{
		int id = world.getTypeId(x, y, z);
		int data = world.getData(x, y, z);
		
		if (dropsOverride == null)
		{
			final List<ItemStack> drops = Block.byId[id].getBlockDropped(world, x, y, z, data, 0);

			if (!tryBreak(world, human, x, y, z))
			{
				return false;
			}

			for (final ItemStack drop : drops)
			{
				addToDroplist(stack, drop);
			}
		}
		else
		{
			if (!tryBreak(world, human, x, y, z))
			{
				return false;
			}
			
			while (dropsOverride.hasNext())
			{
				addToDroplist(stack, dropsOverride.next());
			}
		}
        
		setShort(stack, "fuelRemaining", getFuelRemaining(stack) - 1);

		if (world.random.nextInt(8) == 0)
		{
			world.a("largesmoke", x, y, z, 0.0D, 0.0D, 0.0D);
		}

		if (world.random.nextInt(8) == 0)
		{
			world.a("explode", x, y, z, 0.0D, 0.0D, 0.0D);
		}
		
		return true;
	}
}
