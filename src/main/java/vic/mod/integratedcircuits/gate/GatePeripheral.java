package vic.mod.integratedcircuits.gate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import scala.actors.threadpool.Arrays;
import vic.mod.integratedcircuits.Constants;
import vic.mod.integratedcircuits.IntegratedCircuits;

import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;

import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

public abstract class GatePeripheral implements IPeripheral
{
	@Override
	public final String[] getMethodNames() 
	{
		MethodProvider provider = getMethodProvider();
		String[] ret = new String[provider.methods.size()];
		for(int i = 0; i < provider.methods.size(); i++)
			ret[i] = provider.methods.get(i).getName();
		return ret;
	}

	@Override
	public final Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException 
	{
		MethodProvider provider = getMethodProvider();
		Method m = provider.methods.get(method);
		m.match(arguments);
		return callMethod(m, computer, context, arguments);
	}
	
	public abstract Object[] callMethod(Method method, IComputerAccess computer, ILuaContext context, Object[] arguments) throws LuaException, InterruptedException;

	public abstract MethodProvider getMethodProvider();
	
	@Override
	public void detach(IComputerAccess computer) {}

	@Override
	public void attach(IComputerAccess computer) {}
	
	@Override
	public boolean equals(IPeripheral other) 
	{
		return other.getType().equals(getType());
	}
	
	public static class Method
	{
		private String name;
		private Class[] parameters;
		
		private Method(String name, Class[] parameters)
		{
			this.name = name;
			this.parameters = parameters;
		}
		
		public String getName()
		{
			return name;
		}
		
		public void match(Object[] args) throws LuaException
		{
			if(args.length - 1 != parameters.length) throw new LuaException("Illegal amount of parameters!");
			for(int i = 0; i < parameters.length; i++)
			{
				if(!parameters[i].isAssignableFrom(args[i + 1].getClass())) 
					throw new LuaException("Illegal parameter at index " + i + ". Expected '" + parameters[i] + "', got '" + args[i + 1].getClass() + "'.");
			}
		}
	}
	
	public static class MethodProvider
	{
		private ArrayList<Method> methods = Lists.newArrayList();
		
		public MethodProvider registerMethod(String name, Class... parameters)
		{
			methods.add(new Method(name, parameters));
			return this;
		}
	}
	
	public static class FileMount implements IMount
	{
		private File resourceFolder;
		private String path;
		
		public FileMount(String path)
		{
			this.path = "/assets/" + Constants.MOD_ID + "/" + path;
			try {
				Builder<String> builder = ImmutableSortedSet.naturalOrder();	
				resourceFolder = new File(IntegratedCircuits.class.getResource(this.path).toURI());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}	
		}
		
		@Override
		public boolean exists(String path) throws IOException 
		{
			return new File(resourceFolder + "/" + path).exists();
		}

		@Override
		public boolean isDirectory(String path) throws IOException 
		{
			return new File(resourceFolder + "/" + path).isDirectory();
		}

		@Override
		public void list(String path, List<String> contents) throws IOException 
		{
			contents.addAll(Arrays.asList(new File(resourceFolder + "/" + path).list()));
		}

		@Override
		public long getSize(String path) throws IOException
		{
			return new File(resourceFolder + "/" + path).length();
		}

		@Override
		public InputStream openForRead(String path) throws IOException 
		{
			if(!exists(path)) throw new IOException();
			return IntegratedCircuits.class.getResourceAsStream(this.path + "/" + path);
		}
	}
}
