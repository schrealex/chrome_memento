package it.hakvoort.jndi;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;
import javax.naming.spi.NamingManager;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptBasedContext implements Context
{

	private static final Logger logger = LoggerFactory.getLogger(ScriptBasedContext.class);

	protected final static NameParser PARSER = new PropertiesBasedParser();

	protected ScriptBasedContext parent = null;
	protected String name = null;

	protected ScriptEngine engine;
	protected Invocable invocable;
	protected Object root;

	ScriptBasedContext(ScriptEngine engine, Object root) throws NamingException
	{
		this.engine = engine;
		this.root = root;
		this.invocable = (Invocable) engine;
	}

	/**
	 * Utility method for processing composite/compound name.
	 * @param name The non-null composite or compound name to process.
	 * @return The non-null string name in this namespace to be processed.
	 */
	protected Name asCompoundName(Name name) throws NamingException
	{
		if (name instanceof CompositeName)
		{
			if (name.size() > 1)
			{
				throw new InvalidNameException(name.toString() + " has more components than namespace can handle");
			}

			return PARSER.parse(name.get(0));
		}
		else
		{
			return name;
		}
	}

	@Override
	public Object lookup(String name) throws NamingException
	{
		return lookup(new CompositeName(name));
	}

	@Override
	public Object lookup(Name name) throws NamingException
	{
		if (name.isEmpty())
		{
			return this;
		}

		// Extract components that belong to this namespace
		Name compoundName = asCompoundName(name);
		String atom = compoundName.get(0);

		Object inter = uncheckedGet(invocable, root, atom);

		if (compoundName.size() == 1)
		{
			if (inter == null)
			{
				throw new NameNotFoundException(name + " not found");
			}

			if (inter instanceof Map)
			{
				inter = new ScriptBasedContext(engine, inter);
				((ScriptBasedContext) inter).parent = this;
				((ScriptBasedContext) inter).name = atom;
			}

			/*
			 * Call getObjectInstance for using any object factories
			 */
			try
			{
				return NamingManager.getObjectInstance(inter, new CompositeName().add(atom), this, new Hashtable<>());
			}
			catch (Exception e)
			{
				NamingException ne = new NamingException("getObjectInstance failed");
				ne.setRootCause(e);

				throw ne;
			}
		}
		else
		{

			if (inter instanceof Map)
			{
				inter = new ScriptBasedContext(engine, inter);
				((ScriptBasedContext) inter).parent = this;
				((ScriptBasedContext) inter).name = atom;
			}

			// Intermediate name: Consume name in this context and continue
			if (!(inter instanceof Context))
			{
				throw new NotContextException(atom + " does not name a context");
			}

			return ((Context) inter).lookup(compoundName.getSuffix(1));
		}
	}

	@Override
	public void bind(String name, Object obj) throws NamingException
	{
		bind(new CompositeName(name), obj);
	}

	@Override
	public void bind(Name name, Object obj) throws NamingException
	{
		if (name.isEmpty())
		{
			throw new InvalidNameException("Cannot bind empty name");
		}

		// Extract components that belong to this namespace
		Name nm = asCompoundName(name);
		String atom = nm.get(0);
		Object inter = uncheckedGet(invocable, root, atom);

		if (nm.size() == 1)
		{
			// Atomic name: Find object in internal data structure
			if (inter != null)
			{
				throw new NameAlreadyBoundException("Use rebind to override");
			}

			// Call getStateToBind for using any state factories
			obj = NamingManager.getStateToBind(obj, new CompositeName().add(atom), this, new Hashtable<>());

			uncheckedSet(invocable, root, atom, obj);
		}
		else
		{
			// Intermediate name: Consume name in this context and continue
			if (!(inter instanceof Context))
			{
				throw new NotContextException(atom + " does not name a context");
			}
			((Context) inter).bind(nm.getSuffix(1), obj);
		}
	}

	@Override
	public void rebind(String name, Object obj) throws NamingException
	{
		rebind(new CompositeName(name), obj);
	}

	@Override
	public void rebind(Name name, Object obj) throws NamingException
	{
		throw new NamingException("Rebind not implemented");
	}

	@Override
	public void unbind(String name) throws NamingException
	{
		unbind(new CompositeName(name));
	}

	@Override
	public void unbind(Name name) throws NamingException
	{
		throw new NamingException("Unbind not implemented");
	}

	@Override
	public void rename(String oldname, String newname) throws NamingException
	{
		rename(new CompositeName(oldname), new CompositeName(newname));
	}

	@Override
	public void rename(Name oldname, Name newname) throws NamingException
	{
		throw new NamingException("Rename not implemented");
	}

	@Override
	public NamingEnumeration<NameClassPair> list(String name) throws NamingException
	{
		return list(new CompositeName(name));
	}

	@Override
	public NamingEnumeration<NameClassPair> list(Name name) throws NamingException
	{
		if (name.isEmpty())
		{
			// listing this context
			return new ListOfNames(Arrays.asList(uncheckedList(invocable, root)).iterator());
			// TODO : implement
			//			return new ListOfNames(bindings.keySet().iterator());
		}

		// Perhaps 'name' names a context
		Object target = lookup(name);
		if (target instanceof Context)
		{
			return ((Context) target).list("");
		}
		throw new NotContextException(name + " cannot be listed");
	}

	@Override
	public NamingEnumeration<Binding> listBindings(String name) throws NamingException
	{
		return listBindings(new CompositeName(name));
	}

	@Override
	public NamingEnumeration<Binding> listBindings(Name name) throws NamingException
	{
		if (name.isEmpty())
		{
			return new ListOfBindings(Arrays.asList(uncheckedList(invocable, root)).iterator());
		}

		// Perhaps 'name' names a context
		Object target = lookup(name);
		if (target instanceof Context)
		{
			return ((Context) target).listBindings("");
		}
		throw new NotContextException(name + " cannot be listed");
	}

	@Override
	public void destroySubcontext(String name) throws NamingException
	{
		destroySubcontext(new CompositeName(name));
	}

	@Override
	public void destroySubcontext(Name name) throws NamingException
	{
		if (name.isEmpty())
		{
			throw new InvalidNameException("Cannot destroy context using empty name");
		}

		// Simplistic implementation: not checking for nonempty context first
		// Use same implementation as unbind
		unbind(name);
	}

	@Override
	public ScriptBasedContext createSubcontext(String name) throws NamingException
	{
		return createSubcontext(new CompositeName(name));
	}

	@Override
	public ScriptBasedContext createSubcontext(Name name) throws NamingException
	{
		if (name.isEmpty())
		{
			throw new InvalidNameException("Cannot bind empty name");
		}

		// Extract components that belong to this namespace
		Name nm = asCompoundName(name);
		String atom = nm.get(0);

		Object inter = uncheckedGet(invocable, root, atom);

		if (nm.size() == 1)
		{
			// Create child
			ScriptBasedContext child = new ScriptBasedContext(engine, invocable);

			child.parent = this;
			child.name = atom;

			return child;
		}
		else
		{
			// Intermediate name: Consume name in this context and continue
			if (!(inter instanceof Context))
			{
				throw new NotContextException(atom + " does not name a context");
			}

			return ((ScriptBasedContext) inter).createSubcontext(nm.getSuffix(1));
		}
	}

	@Override
	public Object lookupLink(String name) throws NamingException
	{
		return lookupLink(new CompositeName(name));
	}

	@Override
	public Object lookupLink(Name name) throws NamingException
	{
		return lookup(name);
	}

	@Override
	public NameParser getNameParser(String name) throws NamingException
	{
		return getNameParser(new CompositeName(name));
	}

	@Override
	public NameParser getNameParser(Name name) throws NamingException
	{
		// Do lookup to verify name exists
		Object obj = lookup(name);

		if (obj instanceof Context)
		{
			((Context) obj).close();
		}

		return PARSER;
	}

	@Override
	public String composeName(String name, String prefix) throws NamingException
	{
		Name result = composeName(new CompositeName(name), new CompositeName(prefix));
		return result.toString();
	}

	@Override
	public Name composeName(Name name, Name prefix) throws NamingException
	{
		Name result;

		// Both are compound names, compose using compound name rules
		if (!(name instanceof CompositeName) && !(prefix instanceof CompositeName))
		{
			result = (Name) (prefix.clone());
			result.addAll(name);
			return new CompositeName().add(result.toString());
		}

		// Simplistic implementation: do not support federation
		throw new OperationNotSupportedException("Do not support composing composite names");
	}

	@Override
	public Object addToEnvironment(String propName, Object propVal) throws NamingException
	{
		return new Hashtable<>();
	}

	@Override
	public Object removeFromEnvironment(String propName) throws NamingException
	{
		return new Hashtable<>();
	}

	@Override
	public Hashtable<?, ?> getEnvironment() throws NamingException
	{
		return new Hashtable<>();
	}

	@Override
	public String getNameInNamespace() throws NamingException
	{
		ScriptBasedContext ancestor = parent;

		// No ancestor
		if (ancestor == null)
		{
			return "";
		}

		Name name = PARSER.parse("");
		name.add(name.get(0));

		// Get parent's names
		while (ancestor != null && ancestor.name != null)
		{
			name.add(0, ancestor.name);
			ancestor = ancestor.parent;
		}

		return name.toString();
	}

	@Override
	public String toString()
	{
		if (name != null)
		{
			return name;
		}
		else
		{
			return "ScriptBasedContext:root";
		}
	}

	@Override
	public void close() throws NamingException
	{
	}

	// Class for enumerating name/class pairs
	class ListOfNames implements NamingEnumeration<NameClassPair>
	{
		protected Iterator<String> names;

		ListOfNames(Iterator<String> names)
		{
			this.names = names;
		}

		@Override
		public boolean hasMoreElements()
		{
			try
			{
				return hasMore();
			}
			catch (NamingException e)
			{
				return false;
			}
		}

		@Override
		public boolean hasMore() throws NamingException
		{
			return names.hasNext();
		}

		@Override
		public NameClassPair next() throws NamingException
		{
			String name = names.next();
			String className = ScriptBasedContext.this.lookup(name).getClass().getName();

			return new NameClassPair(name, className);
		}

		@Override
		public NameClassPair nextElement()
		{
			try
			{
				return next();
			}
			catch (NamingException e)
			{
				throw new NoSuchElementException(e.toString());
			}
		}

		@Override
		public void close()
		{
		}
	}

	// Class for enumerating bindings
	class ListOfBindings implements NamingEnumeration<Binding>
	{

		Iterator<String> names;

		ListOfBindings(Iterator<String> names)
		{
			this.names = names;
		}

		@Override
		public boolean hasMoreElements()
		{
			try
			{
				return hasMore();
			}
			catch (NamingException e)
			{
				return false;
			}
		}

		@Override
		public boolean hasMore() throws NamingException
		{
			return names.hasNext();
		}

		@Override
		public Binding next() throws NamingException
		{
			String name = names.next();
			Object obj = ScriptBasedContext.this.lookup(name);

			try
			{
				obj = NamingManager.getObjectInstance(obj, new CompositeName().add(name), ScriptBasedContext.this,
						new Hashtable<>());
			}
			catch (Exception e)
			{
				NamingException ne = new NamingException("getObjectInstance failed");
				ne.setRootCause(e);
				throw ne;
			}

			return new Binding(name, obj);
		}

		@Override
		public Binding nextElement()
		{
			try
			{
				return next();
			}
			catch (NamingException e)
			{
				throw new NoSuchElementException(e.toString());
			}
		}

		@Override
		public void close()
		{
		}
	}

	private static final class PropertiesBasedParser implements NameParser
	{

		private static final Properties syntax = new Properties();

		static
		{
			syntax.put("jndi.syntax.direction", "left_to_right");
			syntax.put("jndi.syntax.separator", ".");
			syntax.put("jndi.syntax.ignorecase", "false");
			syntax.put("jndi.syntax.escape", "\\");
			syntax.put("jndi.syntax.beginquote", "'");
		}

		@Override
		public Name parse(String name) throws NamingException
		{
			return new CompoundName(name, syntax);
		}
	}

	private static Object uncheckedGet(Invocable invocable, Object root, String key)
	{
		try
		{
			return invocable.invokeFunction("get", root, key);
		}
		catch (NoSuchMethodException | ScriptException e)
		{
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	private static String[] uncheckedList(Invocable invocable, Object root)
	{
		try
		{
			return (String[]) invocable.invokeFunction("list", root);
		}
		catch (NoSuchMethodException | ScriptException e)
		{
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	private static String[] uncheckedSet(Invocable invocable, Object root, String key, Object value)
	{
		try
		{
			return (String[]) invocable.invokeFunction("set", root, key, value);
		}
		catch (NoSuchMethodException | ScriptException e)
		{
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	@SuppressWarnings("unused")
	private static Object uncheckedEval(ScriptEngine engine, String expression)
	{
		try
		{
			return engine.eval(expression);
		}
		catch (ScriptException e)
		{

			return null;
		}
	}
}
