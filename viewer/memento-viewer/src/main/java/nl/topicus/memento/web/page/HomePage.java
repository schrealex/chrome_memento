package nl.topicus.memento.web.page;

import org.wicketstuff.shiro.ShiroConstraint;
import org.wicketstuff.shiro.annotation.ShiroSecurityConstraint;

@ShiroSecurityConstraint(constraint = ShiroConstraint.IsAuthenticated)
public class HomePage extends BasePage
{
	/** */
	private static final long serialVersionUID = 1L;

	public HomePage()
	{

	}

}
