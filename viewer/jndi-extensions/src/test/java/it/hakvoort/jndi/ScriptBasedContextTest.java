package it.hakvoort.jndi;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.junit.Assert;
import org.junit.Test;

public class ScriptBasedContextTest
{

	@Test
	public void testContext() throws NamingException
	{
		Context c = new InitialContext();
		NamingEnumeration<Binding> bindings = c.listBindings("");

		Binding binding = bindings.next();

		Assert.assertEquals(ScriptBasedContext.class.getName(), binding.getClassName());
		Assert.assertEquals("root", binding.getName());

		Assert.assertEquals("hello", c.lookup("root.branch1"));
		Assert.assertEquals("world", c.lookup("root.branch2"));
	}
}
