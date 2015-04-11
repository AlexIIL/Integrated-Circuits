package moe.nightfall.vic.integratedcircuits.gate;

import java.util.HashMap;

import moe.nightfall.vic.integratedcircuits.api.IGate;
import moe.nightfall.vic.integratedcircuits.api.IGateRegistry;
import moe.nightfall.vic.integratedcircuits.api.IPartRenderer;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GateRegistry implements IGateRegistry
{
	private HashBiMap<String, Class<? extends IGate>> registry = HashBiMap.create();
	private HashMap<Class<?>, IPartRenderer<?>> rendererRegistry = Maps.newHashMap();
	
	@Override
	public void registerGate(String name, Class<? extends IGate> clazz)
	{
		registry.put(name, clazz);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public <T extends IGate> void registerGateRenderer(Class<T> clazz, IPartRenderer<T> renderer)
	{
		rendererRegistry.put(clazz, renderer);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IPartRenderer<IGate> getRenderer(Class<? extends IGate> clazz) 
	{
		return (IPartRenderer<IGate>) rendererRegistry.get(clazz);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IPartRenderer<IGate> getRenderer(String gateID) 
	{
		return getRenderer(registry.get(gateID));
	}
	
	@Override
	public String getName(Class<? extends IGate> gate)
	{
		return registry.inverse().get(gate);
	}
	
	@Override
	public IGate createGateInstace(String name)
	{
		try {
			return registry.get(name).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Coundn't instance gate \"" + name + "\", need an empty constructor!");
		}
	}
}