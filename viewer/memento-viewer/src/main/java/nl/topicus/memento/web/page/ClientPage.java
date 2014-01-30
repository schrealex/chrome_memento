package nl.topicus.memento.web.page;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.wicketstuff.shiro.ShiroConstraint;
import org.wicketstuff.shiro.annotation.ShiroSecurityConstraint;

@ShiroSecurityConstraint(constraint = ShiroConstraint.IsAuthenticated)
public class ClientPage extends WebPage
{
	/** */
	private static final long serialVersionUID = 1L;

	public ClientPage()
	{
		System.out.println(SecurityUtils.getSubject());
		// setStatelessHint(true);
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{

	}
}
