package nl.topicus.memento.http;

import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GuiceShiroFilter extends AbstractShiroFilter
{
	@Inject
	public GuiceShiroFilter(WebSecurityManager webSecurityManager)
	{
		this.setSecurityManager(webSecurityManager);
	}
}
