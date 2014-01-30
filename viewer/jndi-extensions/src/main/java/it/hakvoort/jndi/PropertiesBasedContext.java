package it.hakvoort.jndi;


import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

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

public class PropertiesBasedContext implements Context
{

	protected final static NameParser PARSER = new PropertiesBasedParser();

	protected final Map<Object, Object> bindings = new ConcurrentHashMap<>();
	protected PropertiesBasedContext parent = null;
	protected String name = null;

	PropertiesBasedContext(Properties properties) throws NamingException
	{
		for (Map.Entry<Object, Object> entry : properties.entrySet())
		{
			PropertiesBasedContext context = this;

			Name name = PARSER.parse(String.valueOf(entry.getKey()));

			while (name.size() > 1)
			{
				Name head = name.getPrefix(1);

				if (!context.bindings.containsKey(head.get(0)))
				{
					context = context.createSubcontext(head);
				}
				else
				{
					context = (PropertiesBasedContext) context.lookup(head);
				}

				name = name.getSuffix(1);
			}

			context.bind(name, entry.getValue());
		}
	}

	/**
	 * Utility method for processing composite/compound name.
	 * @param name The non-null composite or compound name to process.
	 * @return The non-null string name in this namespace to be processed.
	 */
	protected Name getMyComponents(Name name) throws NamingException
	{
		if (name instanceof CompositeName)
		{
			if (name.size() > 1)
			{
				throw new InvalidNameException(name.toString() + " has more components than namespace can handle");
			}

			// Turn component that belongs to us into compound name
			return PARSER.parse(name.get(0));
		}
		else
		{
			// Already parsed
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
		Name nm = getMyComponents(name);
		String atom = nm.get(0);
		Object inter = bindings.get(atom);

		if (nm.size() == 1)
		{
			// Atomic name: Find object in internal data structure
			if (inter == null)
			{
				throw new NameNotFoundException(name + " not found");
			}

			// Call getObjectInstance for using any object factories
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
			// Intermediate name: Consume name in this context and continue
			if (!(inter instanceof Context))
			{
				throw new NotContextException(atom + " does not name a context");
			}

			return ((Context) inter).lookup(nm.getSuffix(1));
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
		Name nm = getMyComponents(name);
		String atom = nm.get(0);
		Object inter = bindings.get(atom);

		if (nm.size() == 1)
		{
			// Atomic name: Find object in internal data structure
			if (inter != null)
			{
				throw new NameAlreadyBoundException("Use rebind to override");
			}

			// Call getStateToBind for using any state factories
			obj = NamingManager.getStateToBind(obj, new CompositeName().add(atom), this, new Hashtable<>());

			// Add object to internal data structure
			bindings.put(atom, obj);
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
		if (name.isEmpty())
		{
			throw new InvalidNameException("Cannot bind empty name");
		}

		// Extract components that belong to this namespace
		Name nm = getMyComponents(name);
		String atom = nm.get(0);

		if (nm.size() == 1)
		{
			// Atomic name

			// Call getStateToBind for using any state factories
			obj = NamingManager.getStateToBind(obj, new CompositeName().add(atom), this, new Hashtable<>());

			// Add object to internal data structure
			bindings.put(atom, obj);
		}
		else
		{
			// Intermediate name: Consume name in this context and continue
			Object inter = bindings.get(atom);
			if (!(inter instanceof Context))
			{
				throw new NotContextException(atom + " does not name a context");
			}
			((Context) inter).rebind(nm.getSuffix(1), obj);
		}
	}

	@Override
	public void unbind(String name) throws NamingException
	{
		unbind(new CompositeName(name));
	}

	@Override
	public void unbind(Name name) throws NamingException
	{
		if (name.isEmpty())
		{
			throw new InvalidNameException("Cannot unbind empty name");
		}

		// Extract components that belong to this namespace
		Name nm = getMyComponents(name);
		String atom = nm.get(0);

		// Remove object from internal data structure
		if (nm.size() == 1)
		{
			// Atomic name: Find object in internal data structure
			bindings.remove(atom);
		}
		else
		{
			// Intermediate name: Consume name in this context and continue
			Object inter = bindings.get(atom);
			if (!(inter instanceof Context))
			{
				throw new NotContextException(atom + " does not name a context");
			}
			((Context) inter).unbind(nm.getSuffix(1));
		}
	}

	@Override
	public void rename(String oldname, String newname) throws NamingException
	{
		rename(new CompositeName(oldname), new CompositeName(newname));
	}

	@Override
	public void rename(Name oldname, Name newname) throws NamingException
	{
		if (oldname.isEmpty() || newname.isEmpty())
		{
			throw new InvalidNameException("Cannot rename empty name");
		}

		// Extract components that belong to this namespace
		Name oldnm = getMyComponents(oldname);
		Name newnm = getMyComponents(newname);

		// Simplistic implementation: support only rename within same context
		if (oldnm.size() != newnm.size())
		{
			throw new OperationNotSupportedException("Do not support rename across different contexts");
		}

		String oldatom = oldnm.get(0);
		String newatom = newnm.get(0);

		if (oldnm.size() == 1)
		{
			// Atomic name: Add object to internal data structure
			// Check if new name exists
			if (bindings.get(newatom) != null)
			{
				throw new NameAlreadyBoundException(newname.toString() + " is already bound");
			}

			// Check if old name is bound
			Object oldBinding = bindings.remove(oldatom);
			if (oldBinding == null)
			{
				throw new NameNotFoundException(oldname.toString() + " not bound");
			}

			bindings.put(newatom, oldBinding);
		}
		else
		{
			// Simplistic implementation: support only rename within same context
			if (!oldatom.equals(newatom))
			{
				throw new OperationNotSupportedException("Do not support rename across different contexts");
			}

			// Intermediate name: Consume name in this context and continue
			Object inter = bindings.get(oldatom);
			if (!(inter instanceof Context))
			{
				throw new NotContextException(oldatom + " does not name a context");
			}
			((Context) inter).rename(oldnm.getSuffix(1), newnm.getSuffix(1));
		}
	}

	@Override
	public NamingEnumeration list(String name) throws NamingException
	{
		return list(new CompositeName(name));
	}

	@Override
	public NamingEnumeration list(Name name) throws NamingException
	{
		if (name.isEmpty())
		{
			// listing this context
			return new ListOfNames(bindings.keySet().iterator());
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
	public NamingEnumeration listBindings(String name) throws NamingException
	{
		return listBindings(new CompositeName(name));
	}

	@Override
	public NamingEnumeration listBindings(Name name) throws NamingException
	{
		if (name.isEmpty())
		{
			// listing this context
			return new ListOfBindings(bindings.keySet().iterator());
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
	public PropertiesBasedContext createSubcontext(String name) throws NamingException
	{
		return createSubcontext(new CompositeName(name));
	}

	@Override
	public PropertiesBasedContext createSubcontext(Name name) throws NamingException
	{
		if (name.isEmpty())
		{
			throw new InvalidNameException("Cannot bind empty name");
		}

		// Extract components that belong to this namespace
		Name nm = getMyComponents(name);
		String atom = nm.get(0);
		Object inter = bindings.get(atom);

		if (nm.size() == 1)
		{
			// Atomic name: Find object in internal data structure
			if (inter != null)
			{
				throw new NameAlreadyBoundException("Use rebind to override");
			}

			// Create child
			PropertiesBasedContext child = new PropertiesBasedContext(new Properties());
			child.parent = this;
			child.name = atom;

			// Add child to internal data structure
			bindings.put(atom, child);

			return child;
		}
		else
		{
			// Intermediate name: Consume name in this context and continue
			if (!(inter instanceof Context))
			{
				throw new NotContextException(atom + " does not name a context");
			}
			return ((PropertiesBasedContext) inter).createSubcontext(nm.getSuffix(1));
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
	public Hashtable getEnvironment() throws NamingException
	{
		return new Hashtable<>();
	}

	@Override
	public String getNameInNamespace() throws NamingException
	{
		PropertiesBasedContext ancestor = parent;

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
			return "ROOT CONTEXT";
		}
	}

	@Override
	public void close() throws NamingException
	{
	}

	// Class for enumerating name/class pairs
	class ListOfNames implements NamingEnumeration<Object>
	{
		protected Iterator<Object> names;

		ListOfNames(Iterator<Object> names)
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
		public Object next() throws NamingException
		{
			String name = (String) names.next();
			String className = bindings.get(name).getClass().getName();
			return new NameClassPair(name, className);
		}

		@Override
		public Object nextElement()
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
	class ListOfBindings extends ListOfNames
	{

		ListOfBindings(Iterator<Object> names)
		{
			super(names);
		}

		@Override
		public Object next() throws NamingException
		{
			String name = (String) names.next();
			Object obj = bindings.get(name);

			try
			{
				obj = NamingManager.getObjectInstance(obj, new CompositeName().add(name), PropertiesBasedContext.this,
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
}
