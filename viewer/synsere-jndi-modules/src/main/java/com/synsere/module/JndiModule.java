package com.synsere.module;

import javax.naming.Context;
import javax.naming.InitialContext;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class JndiModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(Context.class).to(InitialContext.class).in(Scopes.SINGLETON);
	}

}
