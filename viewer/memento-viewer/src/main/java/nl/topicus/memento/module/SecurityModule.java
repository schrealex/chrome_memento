package nl.topicus.memento.module;

import nl.topicus.memento.security.MementoRealm;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

public class SecurityModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(Realm.class).to(MementoRealm.class).in(Scopes.SINGLETON);
	}

	@Provides
	@Singleton
	private WebSecurityManager securityManager(Realm realm)
	{
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setRealm(realm);

		return securityManager;
	}

}
