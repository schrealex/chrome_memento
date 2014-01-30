package nl.topicus.memento.web.page;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

import de.agilecoders.wicket.core.Bootstrap;

public class BasePage extends WebPage
{
	/** */
	private static final long serialVersionUID = 1L;

	private FeedbackPanel feedback;

	public BasePage()
	{
		feedback = new SimpleFeedbackPanel("feedback");

		feedback.setOutputMarkupPlaceholderTag(true);

		add(feedback);
	}

	protected FeedbackPanel getFeedbackPanel()
	{
		return feedback;
	}

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        Bootstrap.renderHead(response);
    }
}
