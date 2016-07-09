package moe.nightfall.vic.integratedcircuits.gate.peripheral;

import java.util.Map;

import moe.nightfall.vic.integratedcircuits.Config;
import moe.nightfall.vic.integratedcircuits.Constants;
import moe.nightfall.vic.integratedcircuits.cp.CircuitData;
import moe.nightfall.vic.integratedcircuits.cp.CircuitPart;
import moe.nightfall.vic.integratedcircuits.gate.GateCircuit;
import moe.nightfall.vic.integratedcircuits.misc.PropertyStitcher.IProperty;
import moe.nightfall.vic.integratedcircuits.misc.Vec2i;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraftforge.fml.common.Optional;
import dan200.computercraft.api.peripheral.IComputerAccess;

// TODO Test me! -- If it didn't work, it would probably be reported by now
public class CircuitPeripheral extends GatePeripheral {

	private GateCircuit circuit;

	public CircuitPeripheral(GateCircuit circuit) {
		this.circuit = circuit;
	}

	@Override
	public String getType() {
		return "ic_circuit";
	}

	@LuaMethod
	public Object[] getGateAt(double x, double y) {
		CircuitData cdata = circuit.getCircuitData();
		Vec2i pos = getPos(x, y);
		CircuitPart cp = cdata.getPart(pos);
		String name = cp.getName(pos, circuit);
		int id = CircuitPart.getId(cp);
		int meta = cp.getState(pos, circuit);
		return new Object[] { name, id, meta };
	}

	@LuaMethod
	public Object[] getPowerTo(double x, double y) {
		CircuitData cdata = circuit.getCircuitData();
		Vec2i pos = getPos(x, y);
		CircuitPart cp = cdata.getPart(pos);

		boolean b1 = cp.getInputFromSide(pos, circuit, ForgeDirection.NORTH);
		boolean b2 = cp.getInputFromSide(pos, circuit, ForgeDirection.EAST);
		boolean b3 = cp.getInputFromSide(pos, circuit, ForgeDirection.SOUTH);
		boolean b4 = cp.getInputFromSide(pos, circuit, ForgeDirection.WEST);

		return new Boolean[] { b1, b2, b3, b4 };
	}

	@LuaMethod
	public Object[] getGateProperties(double x, double y) {
		CircuitData cdata = circuit.getCircuitData();
		CircuitPart cp = cdata.getPart(getPos(x, y));

		Map<String, IProperty> properties = cp.stitcher.getProperties();
		return properties.keySet().toArray();
	}

	@LuaMethod
	public Object[] getGateProperty(double x, double y, String name) throws LuaException {
		CircuitData cdata = circuit.getCircuitData();
		Vec2i pos = getPos(x, y);
		CircuitPart cp = cdata.getPart(pos);

		int state = cp.getState(pos, circuit);
		IProperty property = getProperty(cp, pos, name);

		return new Object[] { property.get(state), property.getClass().getSimpleName() };
	}

	@LuaMethod
	public void setGateProperty(double x, double y, String name, Object obj) throws LuaException {
		CircuitData cdata = circuit.getCircuitData();
		Vec2i pos = getPos(x, y);
		CircuitPart cp = cdata.getPart(pos);

		int state = cp.getState(pos, circuit);
		IProperty property = getProperty(cp, pos, name);

		if (!Config.enablePropertyEdit)
			throw new LuaException("Property editing is disabled from the config file.");
		if (obj instanceof Double)
			obj = ((Double) obj).intValue();
		cp.setProperty(pos, circuit, property, (Comparable) obj);
		cp.notifyNeighbours(pos, circuit);
	}

	@LuaMethod
	public Object[] getOutputToSide(double side) throws LuaException {
		if (side < 0 || side > 3)
			throw new LuaException(String.format("Illegal side provided. (%s) [0->3]", side));
		return ArrayUtils.toObject(circuit.getProvider().getOutput()[(int) side]);
	}

	@LuaMethod
	public Object[] getInputFromSide(double side) throws LuaException {
		if (side < 0 || side > 3)
			throw new LuaException(String.format("Illegal side provided. (%s) [0->3]", side));
		return ArrayUtils.toObject(circuit.getProvider().getInput()[(int) side]);
	}

	@LuaMethod
	public double getSize() {
		return circuit.getCircuitData().getSize();
	}

	@LuaMethod
	public String getName() {
		return circuit.getCircuitData().getProperties().getName();
	}

	@LuaMethod
	public String getAuthor() {
		return circuit.getCircuitData().getProperties().getAuthor();
	}

	@LuaMethod
	public String getGateName(double id) {
		CircuitPart cp = CircuitPart.getPart((int) id);
		return cp.toString();
	}

	private Vec2i getPos(double x, double y) {
		return new Vec2i((int) x, (int) y);
	}

	private IProperty getProperty(CircuitPart part, Vec2i pos, String name) throws LuaException {
		IProperty property = part.stitcher.getPropertyByName(name);
		if (property == null)
			throw new LuaException(String.format("No property by the name of '&s' found for gate %s", name, part.getName(pos, circuit)));
		return property;
	}

	@Override
	@Optional.Method(modid = "ComputerCraft")
	public void attach(IComputerAccess computer) {
		computer.mount("rom/programs/" + Constants.MOD_ID, new FileMount("lua"));
	}
}
