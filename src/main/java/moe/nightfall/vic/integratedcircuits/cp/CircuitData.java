package moe.nightfall.vic.integratedcircuits.cp;

import io.netty.buffer.ByteBuf;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.IntegratedCircuits;
import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import moe.nightfall.vic.integratedcircuits.cp.legacy.LegacyLoader;
import moe.nightfall.vic.integratedcircuits.cp.part.PartIOBit;
import moe.nightfall.vic.integratedcircuits.cp.part.PartNull;
import moe.nightfall.vic.integratedcircuits.misc.CraftingAmount;
import moe.nightfall.vic.integratedcircuits.misc.Vec2i;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants.NBT;

import com.google.common.primitives.Ints;

/**
 * Only {@link #updateMatrix()} is thread safe, so keep in mind to
 * {@code synchronize} any calls to the getters & setters that change the
 * internal arrays if you use them outside of the tick loop.
 */
public class CircuitData implements Cloneable {

	// cdata version
	public static final int version = 1;

	private int size;
	private int[][] meta;
	private int[][] id;

	private HashSet<Vec2i> tickSchedule = new LinkedHashSet<Vec2i>();
	private HashSet<Vec2i> updateQueue = new LinkedHashSet<Vec2i>();
	private HashSet<Vec2i> inputQueue = new LinkedHashSet<Vec2i>();

	private boolean hasChanged;

	private CraftingAmount cost;
	private int amount = -1;

	private ICircuit parent;
	private boolean queueEnabled = true;
	private CircuitProperties prop = new CircuitProperties();

	// private constructor for cloning
	private CircuitData() {
	};

	public CircuitData(int size, ICircuit parent) {
		this.parent = parent;
		this.size = size;
	}

	private CircuitData(int size, ICircuit parent, int[][] id, int[][] meta, LinkedHashSet<Vec2i> tickSchedule, CircuitProperties prop) {
		this(size, parent, id, meta, tickSchedule, new LinkedHashSet<Vec2i>(), prop);
	}

	private CircuitData(int size, ICircuit parent, int[][] id, int[][] meta, LinkedHashSet<Vec2i> tickSchedule, LinkedHashSet<Vec2i> inputQueue, CircuitProperties prop) {
		this.parent = parent;
		this.prop = prop;
		this.size = size;
		this.id = id;
		this.meta = meta;
		this.tickSchedule = tickSchedule;
		this.inputQueue = inputQueue;
		this.hasChanged = !isEmpty();
	}

	@Override
	/** Deep copy **/
	protected CircuitData clone() {
		CircuitData clone = new CircuitData();

		clone.size = size;
		clone.id = new int[size][size];
		clone.meta = new int[size][size];
		clone.amount = amount;
		clone.queueEnabled = queueEnabled;
		clone.cost = cost;
		clone.parent = parent;
		clone.prop = prop;
		clone.hasChanged = hasChanged;

		for (int i = 0; i < size; i++)
			clone.id[i] = id[i].clone();
		for (int i = 0; i < size; i++)
			clone.meta[i] = meta[i].clone();

		for (Vec2i vec : tickSchedule)
			clone.tickSchedule.add(vec);
		for (Vec2i vec : updateQueue)
			clone.tickSchedule.add(vec);

		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof CircuitData))
			return false;
		CircuitData cdata = (CircuitData) obj;

		if (cdata.size != size)
			return false;
		if (cdata.parent != parent)
			return false;

		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				if (cdata.meta[x][y] != meta[x][y])
					return false;
				if (cdata.id[x][y] != id[x][y])
					return false;
			}
		}

		return true;
	}
	
	private EnumConnectionType[] getAndFixModePerSide() {
		// Stores the mode on each side
		EnumConnectionType[] modes = new EnumConnectionType[4];
		// What is the maximum supported size of IO?
		int maxIOSize = maximumIOSize();
		
		// On each side...
		for(int side = 0; side < 4; side++) {
			// Get the mode.
			EnumConnectionType mode = prop.getModeAtSide(EnumFacing.getHorizontal(side));
			// Work out if the mode will need to be changed for it to fit
			if (mode.size > maxIOSize) {
				// if so, set the mode to the first available input mode
				mode = EnumConnectionType.getSupportedList(maxIOSize).get(0);
			}
			// Set the mode
			prop.setCon(prop.setModeAtSide(EnumFacing.getHorizontal(side), mode));
			// Store the mode
			modes[side] = mode;
		}
		return modes;
	}
	
	/** Clears the circuit and sets it up **/
	public void clearAllAndSetup(int size) {
		clearAll(size);
		setupIO();
	}
	
	/** Clears the IOBits, and sets them up again. **/
	public void clearIOAndSetupIO() {
		clearIO();
		setupIO();
	}
	
	/** Sets up the IOBits for the circuit. **/
	public void setupIO() {
		getAndFixModePerSide();
		
		int o = (supportsBundled() ? size / 2 - 8 : 1);
		// Get the ID of the IOBit
		int cid = CircuitPart.getId(PartIOBit.class);
		
		for (int i = 0; i < (supportsBundled() ? 16 : 1); i++) {
			// Get the positions
			Vec2i pos1 = new Vec2i(size - 1 - (i + o), 0);
			Vec2i pos2 = new Vec2i(size - 1, size - 1 - (i + o));
			Vec2i pos3 = new Vec2i(i + o, size - 1);
			Vec2i pos4 = new Vec2i(0, i + o);
			
			if (prop.getModeAtSide(EnumFacing.SOUTH) != EnumConnectionType.NONE && !(prop.getModeAtSide(EnumFacing.SOUTH) == EnumConnectionType.SIMPLE && i >= 1)) {
				// Set the part at the position to be a IOBit
				setID(pos1, cid);
				// Get the IOBit at that position
				PartIOBit io1 = (PartIOBit) getPart(pos1);
				// Set the number of the IOBit (colour / redstone strength)
				io1.setFrequency(pos1, parent, i);
				// The rotation is what side the IOBit is on
				io1.setRotation(pos1, parent, EnumFacing.SOUTH);
			}
			
			if (prop.getModeAtSide(EnumFacing.WEST) != EnumConnectionType.NONE && !(prop.getModeAtSide(EnumFacing.WEST) == EnumConnectionType.SIMPLE && i >= 1)) {
				setID(pos2, cid);
				PartIOBit io2 = (PartIOBit) getPart(pos2);
				io2.setFrequency(pos2, parent, i);
				io2.setRotation(pos2, parent, EnumFacing.WEST);
			}
			
			if (prop.getModeAtSide(EnumFacing.NORTH) != EnumConnectionType.NONE && !(prop.getModeAtSide(EnumFacing.NORTH) == EnumConnectionType.SIMPLE && i >= 1)) {
				setID(pos3, cid);
				PartIOBit io3 = (PartIOBit) getPart(pos3);
				io3.setFrequency(pos3, parent, i);
				io3.setRotation(pos3, parent, EnumFacing.NORTH);
			}
			
			if (prop.getModeAtSide(EnumFacing.EAST) != EnumConnectionType.NONE && !(prop.getModeAtSide(EnumFacing.EAST) == EnumConnectionType.SIMPLE && i >= 1)) {
				setID(pos4, cid);
				PartIOBit io4 = (PartIOBit) getPart(pos4);
				io4.setFrequency(pos4, parent, i);
				io4.setRotation(pos4, parent, EnumFacing.EAST);
			}
		}
	}
	
	/** Clears everything with a new size, including comments and the actual circuit. Nothing, not even IOBits and comments, are left. **/
	public void clearAll(int size) {
		// Clear the actual circuit
		clearContents(size);
		// Clear out comments
		getProperties().clearComments();
	}
	
	public void clearCell(int x, int y) {
		this.id[x][y] = 0;
		this.meta[x][y] = 0;
	}
	
	public void clearRow(int y) {
		for (int x = 0; x < this.size; x++) {
			clearCell(x, y);
		}
	}
	
	public void clearColumn(int x) {
		for (int y = 0; y < this.size; y++) {
			clearCell(x, y);
		}
	}
	
	public void clearIO() {
		// Clear top
		clearRow(0);
		// Clear bottom
		clearRow(this.size - 1);
		// Clear left
		clearColumn(0);
		// Clear right
		clearColumn(this.size - 1);
	}
	
	/** Clears the contents of the circuit and gives it a new size. **/
	public void clearContents(int size) {
		this.id = new int[size][size];
		this.meta = new int[size][size];
		tickSchedule.clear();
		updateQueue.clear();
		this.size = size;
		
		// TODO: What's this for?
		this.setChanged(false);
	}

	/** Syncs the circuit's IO bits with the suspected input **/
	public void updateInput() {
		int o = supportsBundled() ? size / 2 - 8 : 1;
		
		for (int i = 0; i < (supportsBundled() ? 16 : 1); i++) {
			// Get the positions
			Vec2i pos1 = new Vec2i(size - 1 - (i + o), 0);
			Vec2i pos2 = new Vec2i(size - 1, size - 1 - (i + o));
			Vec2i pos3 = new Vec2i(i + o, size - 1);
			Vec2i pos4 = new Vec2i(0, i + o);
			
			// Get the parts
			CircuitPart part1 = getPart(pos1);
			CircuitPart part2 = getPart(pos2);
			CircuitPart part3 = getPart(pos3);
			CircuitPart part4 = getPart(pos4);
			
			// Only update PartIOBits
			if (part1 instanceof PartIOBit) part1.notifyNeighbours(pos1, parent);
			if (part2 instanceof PartIOBit) part2.notifyNeighbours(pos2, parent);
			if (part3 instanceof PartIOBit) part3.notifyNeighbours(pos3, parent);
			if (part4 instanceof PartIOBit) part4.notifyNeighbours(pos4, parent);
		}
		
		propagateSignals();
	}

	/** Syncs the circuit's IO bits with the suspected output **/
	public void updateOutput() {
		int o = supportsBundled() ? size / 2 - 8 : 1;

		for (int i = 0; i < (supportsBundled() ? 16 : 1); i++) {
			// Get the positions
			Vec2i pos1 = new Vec2i(size - 1 - (i + o), 0);
			Vec2i pos2 = new Vec2i(size - 1, size - 1 - (i + o));
			Vec2i pos3 = new Vec2i(i + o, size - 1);
			Vec2i pos4 = new Vec2i(0, i + o);
			
			// Get the parts
			CircuitPart part1 = getPart(pos1);
			CircuitPart part2 = getPart(pos2);
			CircuitPart part3 = getPart(pos3);
			CircuitPart part4 = getPart(pos4);
			
			// Only update PartIOBits
			if (part1 instanceof PartIOBit) ((PartIOBit) part1).updateExternalOutput(pos1, parent);
			if (part2 instanceof PartIOBit) ((PartIOBit) part2).updateExternalOutput(pos2, parent);
			if (part3 instanceof PartIOBit) ((PartIOBit) part3).updateExternalOutput(pos3, parent);
			if (part4 instanceof PartIOBit) ((PartIOBit) part4).updateExternalOutput(pos4, parent);
		}
	}

	public CircuitProperties getProperties() {
		return prop;
	}

	public boolean supportsBundled() {
		return maximumIOSize() == 16;
	}
	
	public int maximumIOSize() {
		if (size - 2 >= 16) return 16;
		else if (size - 2 >= 1) return 1;
		else return 0;
	}

	public int getMeta(Vec2i pos) {
		if (pos.x < 0 || pos.y < 0 || pos.x >= size || pos.y >= size)
			return 0;
		return meta[pos.x][pos.y];
	}

	public void setMeta(Vec2i pos, int m) {
		if (pos.x < 0 || pos.y < 0 || pos.x >= size || pos.y >= size)
			return;
		if (m != meta[pos.x][pos.y])
			setChanged(true);
		meta[pos.x][pos.y] = m;
	}

	public int getID(Vec2i pos) {
		if (pos.x < 0 || pos.y < 0 || pos.x >= size || pos.y >= size)
			return 0;
		return id[pos.x][pos.y];
	}

	public void setID(Vec2i pos, int id) {
		if (pos.x < 0 || pos.y < 0 || pos.x >= size || pos.y >= size)
			return;
		if (id != this.id[pos.x][pos.y])
			setChanged(true);
		this.id[pos.x][pos.y] = id;
	}

	public ICircuit getCircuit() {
		return parent;
	}

	public void setParent(ICircuit parent) {
		this.parent = parent;
	}

	public int getSize() {
		return size;
	}

	public CircuitPart getPart(Vec2i pos) {
		if (pos.x < 0 || pos.y < 0 || pos.x >= size || pos.y >= size)
			return CircuitPart.getPart(PartNull.class);
		CircuitPart part = CircuitPart.getPart(id[pos.x][pos.y]);
		if (part == null) {
			IntegratedCircuits.logger.warn("Removed circuit part! " + pos);
			setID(pos, 0);
			setMeta(pos, 0);
			part = getPart(pos);
		}
		return part;
	}

	public void scheduleTick(Vec2i pos) {
		tickSchedule.add(pos);
	}

	public void scheduleInputChange(Vec2i pos) {
		inputQueue.add(pos);
	}

	public void markForUpdate(Vec2i pos) {
		if (!queueEnabled)
			return;
		updateQueue.add(pos);
	}

	/** "Instantaneously" propagate signals through wires and e.g. null cell */
	public synchronized void propagateSignals() {
		while (inputQueue.size() > 0) {
			HashSet<Vec2i> tmp = (HashSet<Vec2i>) inputQueue.clone();
			inputQueue.clear();
			for (Vec2i pos : tmp) {
				CircuitPart part = getPart(pos);
				part.updateInput(pos, parent);
				part.onInputChange(pos, parent);
			}
		}
	}

	public synchronized void updateMatrix() {
		// InputQueue might not be empty between ticks in some rare cases.
		if (!inputQueue.isEmpty())
			propagateSignals();

		// Tick all circuit parts that need to be ticked
		HashSet<Vec2i> tmp = (HashSet<Vec2i>) tickSchedule.clone();
		tickSchedule.clear();
		for (Vec2i pos : tmp)
			getPart(pos).onScheduledTick(pos, parent);
		
		propagateSignals();
	}

	public static CircuitData readFromNBT(NBTTagCompound compound) {
		return readFromNBT(compound, null);
	}

	public static CircuitData readFromNBT(NBTTagCompound compound, ICircuit parent) {
		int version = compound.getInteger("version");

		List<LegacyLoader> legacyLoaders = null;
		boolean legacyLoad = version < CircuitData.version && Config.enableLegacyLoader;
		if (legacyLoad) {
			// TODO This can't work for multiple versions as those steps have to
			// be executed in sequence.
			legacyLoaders = LegacyLoader.getLegacyLoaders(version);
			for (LegacyLoader loader : legacyLoaders) {
				loader.transformNBT(compound);
			}
		}

		NBTTagList idlist = compound.getTagList("id", NBT.TAG_INT_ARRAY);
		int[][] id = new int[idlist.tagCount()][];
		for (int i = 0; i < idlist.tagCount(); i++) {
			id[i] = idlist.getIntArrayAt(i);
		}

		NBTTagList metalist = compound.getTagList("meta", NBT.TAG_INT_ARRAY);
		int[][] meta = new int[metalist.tagCount()][];
		for (int i = 0; i < metalist.tagCount(); i++) {
			meta[i] = metalist.getIntArrayAt(i);
		}

		CircuitProperties prop = CircuitProperties.readFromNBT(compound.getCompoundTag("properties"));
		int size = compound.getInteger("size");

		if (legacyLoad) {
			for (LegacyLoader loader : legacyLoaders) {
				loader.transform(size, id, meta);
			}
		}

		LinkedHashSet<Vec2i> scheduledTicks = new LinkedHashSet<Vec2i>();

		int[] scheduledList = compound.getIntArray("scheduled");
		for (int i = 0; i < scheduledList.length; i += 2) {
			scheduledTicks.add(new Vec2i(scheduledList[i], scheduledList[i + 1]));
		}

		LinkedHashSet<Vec2i> inputQueue = new LinkedHashSet<Vec2i>();
		if (compound.hasKey("inputQueue")) {
			// Input update queue should be empty between ticks,
			//  but it might not be in some rare cases.
			//  (Mostly when circuit was converted from previous version.)
			int[] inputList = compound.getIntArray("inputQueue");
			for (int i = 0; i < inputList.length; i += 2) {
				inputQueue.add(new Vec2i(inputList[i], inputList[i + 1]));
			}
		}

		CircuitData cdata = new CircuitData(size, parent, id, meta, scheduledTicks, inputQueue, prop);

		if (legacyLoad) {
			// TODO Not future-proof. Will break if e.g. inputQueue is removed.
			for (LegacyLoader loader : legacyLoaders) {
				loader.postTransform(cdata);
			}
		}

		return cdata;
	}

	/**
	 * Excludes additional data like comments that aren't needed for the final
	 * circuit
	 */
	public NBTTagCompound writeToNBTRaw(NBTTagCompound compound) {
		return writeToNBT(compound, true);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		return writeToNBT(compound, false);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound, boolean pcb) {
		NBTTagList idlist = new NBTTagList();
		for (int i = 0; i < size; i++) {
			int[] id = new int[size];
			for (int j = 0; j < size; j++)
				id[j] = this.id[i][j];
			idlist.appendTag(new NBTTagIntArray(id));
		}

		NBTTagList metalist = new NBTTagList();
		for (int i = 0; i < size; i++) {
			metalist.appendTag(new NBTTagIntArray(meta[i].clone()));
		}

		compound.setInteger("size", size);
		compound.setTag("id", idlist);
		compound.setTag("meta", metalist);
		compound.setTag("properties", prop.writeToNBT(new NBTTagCompound(), pcb));

		LinkedList<Integer> tmp = new LinkedList<Integer>();
		for (Vec2i v : tickSchedule) {
			tmp.add(v.x);
			tmp.add(v.y);
		}
		compound.setIntArray("scheduled", Ints.toArray(tmp));

		if (!inputQueue.isEmpty()) {
			// Input update queue should be empty between ticks,
			//  but it might not be in some rare cases.
			//  (Mostly when circuit was converted from previous version.)
			tmp = new LinkedList<Integer>();
			for (Vec2i v : inputQueue) {
				tmp.add(v.x);
				tmp.add(v.y);
			}
			compound.setIntArray("inputQueue", Ints.toArray(tmp));
		}

		compound.setInteger("version", version);

		return compound;
	}

	public void writeToStream(ByteBuf buf) {
		buf.writeInt(updateQueue.size());
		for (Vec2i v : updateQueue) {
			buf.writeByte(v.x);
			buf.writeByte(v.y);
			buf.writeByte(id[v.x][v.y]);
			buf.writeInt(meta[v.x][v.y]);
		}
		updateQueue.clear();
	}

	public void readFromStream(ByteBuf buf) {
		try {
			int length = buf.readInt();
			for (int i = 0; i < length; i++) {
				int x = buf.readByte();
				int y = buf.readByte();
				int cid = buf.readByte();
				int data = buf.readInt();
				setID(new Vec2i(x, y), cid);
				meta[x][y] = data;
			}
		} catch(Exception e) {
			// HOTFIXES! Create an issue for this ASAP.
		}
	}

	public boolean checkUpdate() {
		return updateQueue.size() > 0;
	}

	public void setQueueEnabled(boolean enabled) {
		queueEnabled = enabled;
	}

	public static CircuitData createShallowInstance(int state, ICircuit parent) {
		CircuitData data = new CircuitData();
		data.size = 3;
		data.id = new int[3][3];
		data.id[1][0] = 1;
		data.id[0][1] = 1;
		data.id[2][1] = 1;
		data.id[1][2] = 1;

		data.meta = new int[][] { new int[] { 0, 0, 0 }, new int[] { 0, state, 0 }, new int[] { 0, 0, 0 } };
		data.parent = parent;
		return data;
	}

	/** Cached, recalculate with {@link #calculateCost()} **/
	public CraftingAmount getCost() {
		if (cost == null)
			calculateCost();
		return cost;
	}

	/** Cached, recalculate with {@link #calculateCost()} **/
	public int getPartAmount() {
		if (amount == -1)
			calculateCost();
		return amount;
	}

	public void calculateCost() {
		cost = new CraftingAmount();
		amount = 0;
		for (int x = 1; x < size - 1; x++) {
			for (int y = 1; y < size - 1; y++) {
				Vec2i pos = new Vec2i(x, y);
				CircuitPart part = getPart(pos);
				if (part instanceof PartNull)
					continue;
				part.getCraftingCost(cost, this, pos);
				amount++;
			}
		}
	}

	public boolean hasChanged() {
		return hasChanged;
	}

	public void setChanged(boolean hasChanged) {
		this.hasChanged = hasChanged;
	}

	public boolean isEmpty() {
		for (int x = 1; x < size - 1; x++) {
			for (int y = 1; y < size - 1; y++) {
				if (id[x][y] != 0)
					return false;
			}
		}
		return true;
	}
}
