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

	public LoginPage()
	{
		Form<Void> form = new Form<>("form");

		final IModel<String> nameModel = new Model<>();
		final IModel<String> passwordModel = new Model<>();

		form.add(new TextField<>("name", nameModel).setRequired(true));
		form.add(new PasswordTextField("password", passwordModel).setRequired(true));
		form.add(new AjaxSubmitLink("submit")
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				AuthenticationToken loginToken = new UsernamePasswordToken(nameModel.getObject(), passwordModel
					.getObject());
				try
				{
					SecurityUtils.getSubject().login(loginToken);
					setResponsePage(HomePage.class);
				}
				catch(AuthenticationException ae)
				{
					error("Could not authenticate");
					target.add(getFeedbackPanel());
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
				target.add(getFeedbackPanel());
			}

		});

		add(form);
	}
}
