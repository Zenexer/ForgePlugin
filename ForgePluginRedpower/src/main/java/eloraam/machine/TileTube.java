// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   TileTube.java
package eloraam.machine;

import eloraam.core.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.server.*;

// Referenced classes of package eloraam.machine:
//            BlockMachine

public class TileTube extends TileCovered
	implements ITubeFlow, IPaintable
{
	private static final int MAX_ITEMS_PER_PACKET = 1;
	@SuppressWarnings("PackageVisibleField")
	TubeFlow flow;
	public byte lastDir;
	public byte paintColor;
	private boolean hasChanged;

	public TileTube()
	{
		flow = new TubeFlow()
		{
			@Override
			public TileEntity getParent()
			{
				return TileTube.this;
			}

			@Override
			public boolean schedule(TubeItem tubeitem, eloraam.core.TubeFlow.TubeScheduleContext tubeschedulecontext)
			{
				tubeitem.scheduled = true;
				tubeitem.progress = 0;

				int i = tubeschedulecontext.cons & ~(1 << tubeitem.side);

				if (i == 0)
				{
					return true;
				}

				if (Integer.bitCount(i) == 1)
				{
					tubeitem.side = (byte)Integer.numberOfTrailingZeros(i);
					return true;
				}

				if (CoreProxy.isClient(world))
				{
					return false;
				}

				if (tubeitem.mode != 3)
				{
					tubeitem.mode = 1;
				}

				tubeitem.side = (byte)TubeLib.findRoute(tubeschedulecontext.world, tubeschedulecontext.wc, tubeitem, i, tubeitem.mode, lastDir);
				if (tubeitem.side >= 0)
				{
					int j = i & ~((2 << lastDir) - 1);
					if (j == 0)
					{
						j = i;

						if (j == 0)
						{
							lastDir = 0;
						}
						else
						{
							lastDir = (byte)Integer.numberOfTrailingZeros(j);
						}
					}
				}
				else
				{
					tubeitem.side = (byte)TubeLib.findRoute(tubeschedulecontext.world, tubeschedulecontext.wc, tubeitem, tubeschedulecontext.cons, 2);
					if (tubeitem.side >= 0)
					{
						tubeitem.mode = 2;
						return true;
					}
					if (tubeitem.mode == 3)
					{
						tubeitem.side = (byte)TubeLib.findRoute(tubeschedulecontext.world, tubeschedulecontext.wc, tubeitem, tubeschedulecontext.cons, 1);
						tubeitem.mode = 1;
					}
					if (tubeitem.side < 0)
					{
						tubeitem.side = lastDir;
						int k = i & ~((2 << lastDir) - 1);
						if (k == 0)
						{
							k = i;
						}
						if (k == 0)
						{
							lastDir = 0;
						}
						else
						{
							lastDir = (byte)Integer.numberOfTrailingZeros(k);
						}
					}
				}
				return true;
			}

			@Override
			public boolean handleItem(TubeItem tubeitem, eloraam.core.TubeFlow.TubeScheduleContext tubeschedulecontext)
			{
				return MachineLib.addToInventory(world, tubeitem.item, tubeschedulecontext.dest, (tubeitem.side ^ 1) & 0x3f);
			}
		};

		lastDir = 0;
		paintColor = 0;
		hasChanged = false;
	}

	@Override
	public int getTubeConnectableSides()
	{
		int i = 63;
		for (int j = 0; j < 6; j++)
		{
			if ((CoverSides & 1 << j) > 0 && Covers[j] >> 8 < 3)
			{
				i &= ~(1 << j);
			}
		}

		return i;
	}

	@Override
	public int getTubeConClass()
	{
		return paintColor;
	}

	@Override
	public boolean canRouteItems()
	{
		return true;
	}

	@Override
	public boolean tubeItemEnter(int i, int j, TubeItem tubeitem)
	{
		if (j != 0)
		{
			return false;
		}
		if (tubeitem.color != 0 && paintColor != 0 && tubeitem.color != paintColor)
		{
			return false;
		}
		else
		{
			tubeitem.side = (byte)i;
			flow.add(tubeitem);
			hasChanged = true;
			dirtyBlock();
			return true;
		}
	}

	@Override
	public boolean tubeItemCanEnter(int i, int j, TubeItem tubeitem)
	{
		if (tubeitem.color != 0 && paintColor != 0 && tubeitem.color != paintColor)
		{
			return false;
		}
		else
		{
			return j == 0;
		}
	}

	@Override
	public int tubeWeight(int i, int j)
	{
		return 0;
	}

	@Override
	public void addTubeItem(TubeItem tubeitem)
	{
		tubeitem.side ^= 1;
		flow.add(tubeitem);
		hasChanged = true;
		dirtyBlock();
	}

	@Override
	public TubeFlow getTubeFlow()
	{
		return flow;
	}

	@Override
	public boolean tryPaint(int i, int j, int k)
	{
		if (i == 29)
		{
			if (paintColor == k)
			{
				return false;
			}
			else
			{
				paintColor = (byte)k;
				updateBlockChange();
				return true;
			}
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	@Override
	public void q_()
	{
		if (flow.update())
		{
			hasChanged = true;
		}

		if (hasChanged)
		{
			hasChanged = false;

			if (CoreProxy.isServer())
			{
				sendItemUpdate();
			}

			dirtyBlock();
		}
	}

	@Override
	public int getBlockID()
	{
		return RedPowerBase.blockMicro.id;
	}

	@Override
	public int getExtendedID()
	{
		return 8;
	}

	@Override
	public void onBlockNeighborChange(int i)
	{
	}

	@Override
	public int getPartsMask()
	{
		return CoverSides | 0x20000000;
	}

	@Override
	public int getSolidPartsMask()
	{
		return CoverSides | 0x20000000;
	}

	@Override
	public boolean blockEmpty()
	{
		return false;
	}

	@Override
	public void onHarvestPart(EntityHuman entityhuman, int i)
	{
		if (i == 29)
		{
			CoreLib.dropItem(world, x, y, z, new ItemStack(RedPowerBase.blockMicro.id, 1, getExtendedID() << 8));
			flow.onRemove();
			if (CoverSides > 0)
			{
				replaceWithCovers();
			}
			else
			{
				deleteBlock();
			}
		}
		else
		{
			super.onHarvestPart(entityhuman, i);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addHarvestContents(ArrayList arraylist)
	{
		super.addHarvestContents(arraylist);
		arraylist.add(new ItemStack(RedPowerBase.blockMicro.id, 1, getExtendedID() << 8));
	}

	@Override
	public float getPartStrength(EntityHuman entityhuman, int i)
	{
		BlockMachine blockmachine = RedPowerMachine.blockMachine;
		if (i == 29)
		{
			return entityhuman.getCurrentPlayerStrVsBlock(blockmachine, 0) / (blockmachine.m() * 30F);
		}
		else
		{
			return super.getPartStrength(entityhuman, i);
		}
	}

	@Override
	public void setPartBounds(BlockMultipart blockmultipart, int i)
	{
		if (i == 29)
		{
			blockmultipart.a(0.25F, 0.25F, 0.25F, 0.75F, 0.75F, 0.75F);
		}
		else
		{
			super.setPartBounds(blockmultipart, i);
		}
	}

	@Override
	public void a(NBTTagCompound nbttagcompound)
	{
		super.a(nbttagcompound);
		flow.readFromNBT(nbttagcompound);
		lastDir = nbttagcompound.getByte("lDir");
		paintColor = nbttagcompound.getByte("pCol");
	}

	@Override
	public void b(NBTTagCompound nbttagcompound)
	{
		super.b(nbttagcompound);
		flow.writeToNBT(nbttagcompound);
		nbttagcompound.setByte("lDir", lastDir);
		nbttagcompound.setByte("pCol", paintColor);
	}

	@Override
	protected void readFromPacket(Packet211TileDesc packet211tiledesc)
		throws IOException
	{
		if (packet211tiledesc.subId == 10)
		{
			flow.contents.clear();
			int i = (int)packet211tiledesc.getUVLC();
			for (int j = 0; j < i; j++)
			{
				flow.contents.add(TubeItem.newFromPacket(packet211tiledesc));
			}

		}
		else
		{
			super.readFromPacket(packet211tiledesc);
			paintColor = (byte)packet211tiledesc.getByte();
		}
	}

	@Override
	protected void writeToPacket(Packet211TileDesc packet211tiledesc)
	{
		super.writeToPacket(packet211tiledesc);
		packet211tiledesc.addByte(paintColor);
	}

	protected void sendItemUpdate()
	{
		TubeItem lowestItem = null;
		if (!flow.contents.isEmpty())
		{
			int lowestProgress = Integer.MAX_VALUE;
			for (TubeItem item : flow.contents)
			{
				if (item.isPacketSent())
				{
					return;
				}

				if (item.progress <= lowestProgress)
				{
					lowestItem = item;
					lowestProgress = item.progress;
				}
			}
		}

		final Packet211TileDesc packet211tiledesc = new Packet211TileDesc();
		packet211tiledesc.subId = 10;
		packet211tiledesc.xCoord = x;
		packet211tiledesc.yCoord = y;
		packet211tiledesc.zCoord = z;
		packet211tiledesc.addUVLC(1); // TubeItem count

		if (lowestItem != null)
		{
			lowestItem.writeToPacket(packet211tiledesc);
		}

		packet211tiledesc.encode();
		CoreProxy.sendPacketToPosition(packet211tiledesc, x, z);
	}

	@Override
	public void handlePacket(Packet211TileDesc packet211tiledesc)
	{
		try
		{
			readFromPacket(packet211tiledesc);
		}
		catch (IOException ex)
		{
		}
	}
}
