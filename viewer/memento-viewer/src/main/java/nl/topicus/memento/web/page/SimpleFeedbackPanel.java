package nl.topicus.memento.web.page;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

public class SimpleFeedbackPanel extends FeedbackPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SimpleFeedbackPanel(String id)
	{
		super(id);
	}

	@Override
	protected String getCSSClass(FeedbackMessage message)
	{
		String css;
		switch(message.getLevel())
		{
			case FeedbackMessage.SUCCESS:
				css = "alert-success";
				break;
			case FeedbackMessage.INFO:
				css = "alert-info";
				break;
			case FeedbackMessage.ERROR:
				css = "alert-warning";
				break;
			default:
				css = "";
		}

		return css;
	}

}