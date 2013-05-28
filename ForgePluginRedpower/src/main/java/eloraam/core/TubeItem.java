package eloraam.core;

import java.io.IOException;
import net.minecraft.server.Item;
import net.minecraft.server.ItemStack;
import net.minecraft.server.NBTTagCompound;


public class TubeItem
{
	public short progress;
	public byte mode;
	public byte side;
	public byte color;
	public short power;
	public boolean scheduled;
	public ItemStack item;
	private int ticks;
	public int nextUpdate = -1;
	private boolean packetSent;

	public TubeItem()
	{
		progress = 0;
		mode = 1;
		color = 0;
		power = 0;
		scheduled = false;
	}

	public TubeItem(int i, ItemStack itemstack)
	{
		progress = 0;
		mode = 1;
		color = 0;
		power = 0;
		scheduled = false;
		item = itemstack;
		side = (byte)i;
	}

	public boolean isPacketSent()
	{
		return packetSent;
	}

	public void setPacketSent(boolean packetSent)
	{
		this.packetSent = packetSent;
	}

	public boolean update()
	{
		ticks++;
		if (ticks < 0)
		{
			ticks -= Integer.MIN_VALUE;
		}

		final boolean update;
		if (nextUpdate < 0)
		{
			update = false;
		}
		else
		{
			update = ticks > nextUpdate;
		}

		if (update || nextUpdate < 0)
		{
			final short speed = getSpeed();
			nextUpdate = ticks + (speed == 1 ? 8 : Math.round(128.0f / speed));
		}

		if (nextUpdate < 0)
		{
			nextUpdate -= Integer.MIN_VALUE;
		}

		return update;
	}

	public int getTicks()
	{
		return ticks;
	}

	public boolean isInterval(final int interval)
	{
		return ticks % interval == 0;
	}

	public short getSpeed()
	{
		return (short)(power + 1);
	}

	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		item = ItemStack.a(nbttagcompound);
		side = nbttagcompound.getByte("side");
		progress = nbttagcompound.getShort("pos");
		mode = nbttagcompound.getByte("mode");
		color = nbttagcompound.getByte("col");
		if (progress < 0)
		{
			scheduled = true;
			progress = (short)(-progress - 1);
		}
		power = (short)(nbttagcompound.getByte("pow") & 0xff);
	}

	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		item.save(nbttagcompound);
		nbttagcompound.setByte("side", side);
		nbttagcompound.setShort("pos", (short)(scheduled ? -progress - 1 : progress));
		nbttagcompound.setByte("mode", mode);
		nbttagcompound.setByte("col", color);
		nbttagcompound.setByte("pow", (byte)power);
	}

	public static TubeItem newFromNBT(NBTTagCompound nbttagcompound)
	{
		TubeItem tubeitem = new TubeItem();
		tubeitem.readFromNBT(nbttagcompound);
		return tubeitem;
	}

	public void readFromPacket(Packet211TileDesc packet211tiledesc)
		throws IOException
	{
		side = (byte)packet211tiledesc.getByte();
		progress = (short)(int)packet211tiledesc.getVLC();
		if (progress < 0)
		{
			scheduled = true;
			progress = (short)(-progress - 1);
		}
		color = (byte)packet211tiledesc.getByte();
		power = (byte)packet211tiledesc.getByte();
		int i = packet211tiledesc.getByte();
		int j = (int)packet211tiledesc.getUVLC();
		int k = (int)packet211tiledesc.getUVLC();
		item = new ItemStack(Item.byId[k], i, j);
	}

	public void writeToPacket(Packet211TileDesc packet211tiledesc)
	{
		packet211tiledesc.addByte(side);
		int i = scheduled ? -progress - 1 : ((int)(progress));
		packet211tiledesc.addVLC(scheduled ? -progress - 1 : progress);
		packet211tiledesc.addByte(color);
		packet211tiledesc.addByte(power);
		packet211tiledesc.addByte(item.count);
		packet211tiledesc.addUVLC(item.getData());
		packet211tiledesc.addUVLC(item.id);

		setPacketSent(true);
	}

	public static TubeItem newFromPacket(Packet211TileDesc packet211tiledesc)
		throws IOException
	{
		TubeItem tubeitem = new TubeItem();
		tubeitem.readFromPacket(packet211tiledesc);
		return tubeitem;
	}
}
