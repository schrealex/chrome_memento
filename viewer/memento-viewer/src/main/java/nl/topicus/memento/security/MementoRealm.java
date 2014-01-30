package nl.topicus.memento.security;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class MementoRealm extends AuthorizingRealm
{

	@Inject
	@Named("security.username")
	private String username;

	@Inject
	@Named("security.password")
	private String password;

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException
	{
		if(token instanceof UsernamePasswordToken)
		{
			UsernamePasswordToken upToken = (UsernamePasswordToken) token;
			String username = Strings.nullToEmpty(upToken.getUsername());
			char[] password = upToken.getPassword();

			if(username.equals(this.username) && this.password.equals(new String(password)))
			{
				return new SimpleAuthenticationInfo(username, password, getName());
			}
		}

		return null;
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals)
	{
		return null;
	}
}
