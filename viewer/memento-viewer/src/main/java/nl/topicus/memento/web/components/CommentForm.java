package nl.topicus.memento.web.components;

import java.util.Date;

import nl.topicus.memento.db.Tables;
import nl.topicus.memento.db.tables.records.CommentRecord;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.CompoundPropertyModel;
import org.jooq.DSLContext;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class CommentForm extends Form<CommentRecord>
{
	private static final long serialVersionUID = 1L;

	@Inject
	private Provider<DSLContext> contextProvider;

	DSLContext context;

	public CommentForm(final String id)
	{
		super(id, new CompoundPropertyModel<CommentRecord>(new CommentRecord()));

		context = contextProvider.get();

		final TextArea<String> text = new TextArea<String>("comment");
		text.setOutputMarkupId(true);
		add(text);
	}

	@Override
	public final void onSubmit()
	{
		// Construct a copy of the edited comment
		final CommentRecord comment = getModelObject();
		final CommentRecord newComment = new CommentRecord(null, 1, new Date().toString(), "1", getModelObject()
			.getComment());

		// Add the component we edited to the list of comments
		context.insertInto(Tables.COMMENT).set(newComment).execute();

		// Clear out the text component
		comment.setComment("");
	}
}
