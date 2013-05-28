package eloraam.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.TileEntity;
import net.minecraft.server.World;


public abstract class TubeFlow
{
	public LinkedList<TubeItem> contents;

	public TubeFlow()
	{
		contents = new LinkedList<>();
	}

	public abstract boolean schedule(TubeItem tubeitem, TubeScheduleContext tubeschedulecontext);

	public boolean handleItem(TubeItem tubeitem, TubeScheduleContext tubeschedulecontext)
	{
		return false;
	}

	public abstract TileEntity getParent();

	public boolean update()
	{
		boolean isUpdateRequired = false;
		if (contents.size() == 0)
		{
			return false;
		}

		TubeScheduleContext tubeschedulecontext = new TubeScheduleContext(getParent());
		tubeschedulecontext.tii = contents.iterator();
		while (tubeschedulecontext.tii.hasNext())
		{
			final TubeItem tubeitem = tubeschedulecontext.tii.next();

			if (!tubeitem.update())
			{
				continue;
			}

			tubeitem.progress += 0xff;

			//if (tubeitem.progress >= 0xff)
			//{
			isUpdateRequired = true;

			tubeitem.setPacketSent(false);

			if (tubeitem.power > 0)
			{
				tubeitem.power--;
			}

			if (!tubeitem.scheduled)
			{
				if (!schedule(tubeitem, tubeschedulecontext))
				{
					tubeschedulecontext.tii.remove();
				}
			}
			else
			{
				tubeschedulecontext.tii.remove();
				if (!CoreProxy.isClient(tubeschedulecontext.world))
				{
					tubeschedulecontext.tir.add(tubeitem);
				}
			}
			//}
		}

		if (CoreProxy.isClient(tubeschedulecontext.world))
		{
			return isUpdateRequired;
		}

		for (final TubeItem tubeitem1 : tubeschedulecontext.tir)
		{
			if (tubeitem1.side < 0 || (tubeschedulecontext.cons & 1 << tubeitem1.side) == 0)
			{
				if (tubeschedulecontext.cons == 0)
				{
					MachineLib.ejectItem(tubeschedulecontext.world, tubeschedulecontext.wc, tubeitem1.item, 1);
				}
				else
				{
					tubeitem1.side = (byte)Integer.numberOfTrailingZeros(tubeschedulecontext.cons);
					tubeitem1.progress = 128;
					tubeitem1.scheduled = false;
					contents.add(tubeitem1);
					isUpdateRequired = true;
				}
			}
			else
			{
				tubeschedulecontext.dest = tubeschedulecontext.wc.copy();
				tubeschedulecontext.dest.step(tubeitem1.side);
				ITubeConnectable itubeconnectable = (ITubeConnectable)CoreLib.getTileEntity(tubeschedulecontext.world, tubeschedulecontext.dest, ITubeConnectable.class);
				if (itubeconnectable instanceof ITubeFlow)
				{
					ITubeFlow itubeflow = (ITubeFlow)itubeconnectable;
					itubeflow.addTubeItem(tubeitem1);
				}
				else if ((itubeconnectable == null || !itubeconnectable.tubeItemEnter((tubeitem1.side ^ 1) & 0x3f, tubeitem1.mode, tubeitem1)) && !handleItem(tubeitem1, tubeschedulecontext))
				{
					tubeitem1.progress = 0;
					tubeitem1.scheduled = false;
					tubeitem1.mode = 2;
					contents.add(tubeitem1);
				}
			}
		}

		return isUpdateRequired;
	}

	public void add(TubeItem tubeitem)
	{
		tubeitem.progress = 0;
		tubeitem.scheduled = false;
		contents.add(tubeitem);
	}

	public void onRemove()
	{
		TileEntity tileentity = getParent();
		Iterator iterator = contents.iterator();
		do
		{
			if (!iterator.hasNext())
			{
				break;
			}
			TubeItem tubeitem = (TubeItem)iterator.next();
			if (tubeitem != null && tubeitem.item.count > 0)
			{
				CoreLib.dropItem(tileentity.world, tileentity.x, tileentity.y, tileentity.z, tubeitem.item);
			}
		}
		while (true);
	}

	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		NBTTagList nbttaglist = nbttagcompound.getList("Items");
		if (nbttaglist.size() > 0)
		{
			contents = new LinkedList<>();
			for (int i = 0; i < nbttaglist.size(); i++)
			{
				NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.get(i);
				contents.add(TubeItem.newFromNBT(nbttagcompound1));
			}

		}
	}

	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		NBTTagList nbttaglist = new NBTTagList();
		if (contents != null)
		{
			NBTTagCompound nbttagcompound1;
			for (Iterator iterator = contents.iterator(); iterator.hasNext(); nbttaglist.add(nbttagcompound1))
			{
				TubeItem tubeitem = (TubeItem)iterator.next();
				nbttagcompound1 = new NBTTagCompound();
				tubeitem.writeToNBT(nbttagcompound1);
			}

		}
		nbttagcompound.set("Items", nbttaglist);
	}


	public static class TubeScheduleContext
	{
		public World world;
		public WorldCoord wc;
		public int cons;
		public ArrayList<TubeItem> tir;
		public Iterator<TubeItem> tii;
		public WorldCoord dest;

		public TubeScheduleContext(TileEntity tileentity)
		{
			tir = new ArrayList<>();
			dest = null;
			world = tileentity.world;
			wc = new WorldCoord(tileentity);
			cons = TubeLib.getConnections(world, wc.x, wc.y, wc.z);
		}
	}
}
