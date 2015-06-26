package moe.nightfall.vic.integratedcircuits.net;

import java.io.IOException;

import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.misc.Vec2;
import moe.nightfall.vic.integratedcircuits.tile.TileEntityCAD;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import cpw.mods.fml.relauncher.Side;

public class PacketPCBChangePart extends PacketTileEntity<PacketPCBChangePart> {
	private int size;
	private int[] data;
	private int button = -1;
	private boolean flag;

	public PacketPCBChangePart() {
	}

	public PacketPCBChangePart(int x, int y, int button, boolean ctrl, int tx, int ty, int tz) {
		this(new int[] { x, y }, ctrl, tx, ty, tz);
		this.button = button;
	}

	/**
	 * The flag indicates weather a new snapshot should be taken before
	 * performing the action.
	 **/
	public PacketPCBChangePart(int data[], boolean flag, int tx, int ty, int tz) {
		super(tx, ty, tz);
		this.size = data.length;
		this.data = data;
		this.flag = flag;
	}

	@Override
	public void read(PacketBuffer buffer) throws IOException {
		super.read(buffer);
		button = buffer.readInt();
		flag = buffer.readBoolean();
		size = buffer.readInt();
		data = new int[size];
		for (int i = 0; i < size; i++)
			data[i] = buffer.readInt();
	}

	@Override
	public void write(PacketBuffer buffer) throws IOException {
		super.write(buffer);
		buffer.writeInt(button);
		buffer.writeBoolean(flag);
		buffer.writeInt(size);
		for (int i : data)
			buffer.writeInt(i);
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		TileEntityCAD te = (TileEntityCAD) player.worldObj.getTileEntity(xCoord, yCoord, zCoord);
		if (te != null) {
			CircuitData cdata = te.getCircuitData();

			if (button == -1 && flag)
				te.cache.create(player.getGameProfile().getId());

			for (int i = 0; i < size; i += 4) {
				Vec2 pos = new Vec2(data[i], data[i + 1]);
				if (button != -1)
					cdata.getPart(pos).onClick(pos, te, button, flag);
				else {
					int oid = cdata.getID(pos);
					int ometa = cdata.getMeta(pos);

					if (data[i + 2] != oid)
						cdata.getPart(pos).onRemoved(pos, te);

					cdata.setID(pos, data[i + 2]);
					cdata.setMeta(pos, data[i + 3]);

					if (data[i + 2] != oid)
						cdata.getPart(pos).onPlaced(pos, te);
					else if (data[i + 3] != ometa)
						cdata.getPart(pos).onChanged(pos, te, ometa);

					cdata.markForUpdate(pos);
				}
			}
			// Wires must update immediately, even if circuit is not ticked
			cdata.propagateSignals();

			if (button == -1 && flag)
				te.cache.capture(player.getGameProfile().getId());
		}
	}
}
