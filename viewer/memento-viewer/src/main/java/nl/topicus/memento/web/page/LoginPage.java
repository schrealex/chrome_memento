package nl.topicus.memento.web.page;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class LoginPage extends BasePage
{

	private static final long serialVersionUID = 1L;

	public LoginPage()
	{
		final Form<Void> form = new Form<>("form");

		final IModel<String> nameModel = new Model<>();
		final IModel<String> passwordModel = new Model<>();

		form.add(new TextField<>("name", nameModel).setRequired(true));
		form.add(new PasswordTextField("password", passwordModel).setRequired(true));
		form.add(new AjaxSubmitLink("submit")
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(final AjaxRequestTarget target, final Form<?> form)
			{
				final AuthenticationToken loginToken = new UsernamePasswordToken(nameModel.getObject(), passwordModel
					.getObject());
				try
				{
					SecurityUtils.getSubject().login(loginToken);
					SecurityUtils.getSubject().getSession().setAttribute("username", nameModel.getObject());
					setResponsePage(HomePage.class);
				}
				catch (final AuthenticationException ae)
				{
					error("Could not authenticate");
					target.add(getFeedbackPanel());
				}
			}

			@Override
			protected void onError(final AjaxRequestTarget target, final Form<?> form)
			{
				target.add(getFeedbackPanel());
			}

		});

		add(form);
	}
}
