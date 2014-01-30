package it.hakvoort.jndi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.InitialContextFactory;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class ScriptBasedInitialContextFactory implements InitialContextFactory
{

	private static final Logger logger = LoggerFactory.getLogger(ScriptBasedInitialContextFactory.class);

	@Override
	public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException
	{
		String providerUrl = String.valueOf(environment.get(Context.PROVIDER_URL));

		logger.debug("Loading initial context from {}", providerUrl);

		URL resource = null;
		try
		{
			resource = Resources.getResource(providerUrl);
		}
		catch (IllegalArgumentException iae)
		{
			try
			{
				resource = new URI(providerUrl).toURL();
			}
			catch (MalformedURLException | URISyntaxException e)
			{
				logger.error(e.getMessage(), e);
				throw new NoInitialContextException(e.getMessage());
			}
		}

		ScriptEngineManager mgr = new ScriptEngineManager();

		ScriptEngine engine = mgr.getEngineByName("javascript");

		Object root = null;

		try
		{
			StringBuilder script = new StringBuilder();

			script.append("for(var _ in (function() { return this; }).call(null)) {");
			script.append("    delete ((function() { return this; }).call(null))[_];");
			script.append("}");
			script.append("(function() {");
			script.append(Resources.toString(resource, Charsets.UTF_8));
			script.append("    for(var _ in (function() { return this; }).call(null)) {");
			script.append("        if(_ != '_') {");
			script.append("            this[_] = ((function() { return this; }).call(null))[_];");
			script.append("        }");
			script.append("    }");
			script.append("    return this;");
			script.append("}).apply({});");

			root = engine.eval(script.toString(), new SimpleScriptContext());

			script.setLength(0);

			script.append("function list(obj) {");
			script.append("    var arr = [];");
			script.append("    for(var name in obj) {");
			script.append("        arr.push(name);");
			script.append("    }");
			script.append("    var result = java.lang.reflect.Array.newInstance(java.lang.String, arr.length);");
			script.append("    for(var i = 0; i < arr.length; i++) {");
			script.append("        result[i] = arr[i];");
			script.append("    }");
			script.append("    return result;");
			script.append("}");

			script.append("function get(obj, attr) {");
			script.append("    return obj[attr];");
			script.append("}");

			script.append("function set(obj, attr, val) {");
			script.append("    obj[attr] = val;");
			script.append("}");

			engine.eval(script.toString());
		}
		catch (ScriptException | IOException e)
		{
			logger.error(e.getMessage(), e);
			throw new NoInitialContextException(
					"No initial context can be created due to a failure reading the context provider url (" + e.getMessage()
							+ ")");
		}

		return new ScriptBasedContext(engine, root);

	}
}
