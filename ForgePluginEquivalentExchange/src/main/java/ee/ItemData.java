package ee;

import lombok.Getter;
import net.minecraft.server.ItemStack;


public final class ItemData
{
	@Getter
	private final int typeId;
	@Getter
	private final int data;
	
	public ItemData(final int typeId)
	{
		this(typeId, 0);
	}
	
	public ItemData(final ItemStack stack)
	{
		this(stack.id, stack.d() ? 0 : stack.getData());
	}
	
	public ItemData(final int typeId, final int data)
	{
		this.typeId = typeId;
		this.data = data;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj != null && obj instanceof ItemData)
		{
			final ItemData other = (ItemData)obj;
			return typeId == other.typeId && data == other.data;
		}
		
		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		int hash = 3;
		hash = 71 * hash + typeId;
		hash = 71 * hash + data;
		return hash;
	}
}
