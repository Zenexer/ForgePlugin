package com.earth2me.minecraft.forgeplugin;

import lombok.Data;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;


@Data
public class ItemData implements Cloneable
{
	private int id;
	private int data;
	
	public ItemData()
	{
	}
	
	public ItemData(final int id, final int data)
	{
		this.id = id;
		this.data = id == 0 ? 0 : data;
	}
	
	public ItemData(final ItemStack stack)
	{
		this(stack == null ? 0 : stack.getTypeId(), stack == null ? 0 : stack.getDurability());
	}
	
	public ItemData(final Item item)
	{
		this(item == null ? null : item.getItemStack());
	}
	
	public ItemData(final Block block)
	{
		this(block == null ? 0 : block.getTypeId(), block == null ? 0 : block.getData());
	}
	
	public static ItemData parse(final String text)
	{
		final String[] tokens = text.split("[^0-9*-]+", 2);
		
		try
		{
			final int id = Integer.parseInt(tokens[0]);
			
			final int data;
			if (tokens.length >= 2 && !tokens[1].isEmpty())
			{
				if ("*".equals(tokens[1]))
				{
					data = -1;
				}
				else
				{
					data = Integer.parseInt(tokens[1]);
				}
			}
			else
			{
				data = -1;
			}
			
			return new ItemData(id, data);
		}
		catch (NumberFormatException ex)
		{
			return null;
		}
	}
	
	@Override
	public boolean equals(Object object)
	{
		if (object != null && object instanceof ItemData)
		{
			final ItemData other = (ItemData)object;
			return other.id == id && other.data == data;
		}
		else
		{
			return super.equals(object);
		}
	}

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 17 * hash + this.id;
		hash = 17 * hash + this.data;
		return hash;
	}

	@Override
	public Object clone()
	{
		return new ItemData(id, data);
	}
	
	public ItemData getAnyData()
	{
		return new ItemData(id, -1);
	}
}
