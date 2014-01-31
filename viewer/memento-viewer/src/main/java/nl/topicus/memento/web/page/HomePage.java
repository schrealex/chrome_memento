package nl.topicus.memento.web.page;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import nl.topicus.memento.db.tables.records.CommentRecord;
import nl.topicus.memento.videoservice.VideoService;
import nl.topicus.memento.web.components.CommentForm;
import nl.topicus.memento.web.components.FileTreeProvider;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.WindowsTheme;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.html5.media.MediaSource;
import org.wicketstuff.html5.media.video.Html5Video;
import org.wicketstuff.shiro.ShiroConstraint;
import org.wicketstuff.shiro.annotation.ShiroSecurityConstraint;

import com.google.inject.Inject;
import com.google.inject.Provider;

@ShiroSecurityConstraint(constraint = ShiroConstraint.IsAuthenticated)
public class HomePage extends BasePage
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(HomePage.class);

	@Inject
	private VideoService videoService;

	/** Container for the comments, used to update the listview. */
	private final WebMarkupContainer comments;

	/** A global list of all comments from all users across all sessions */
	public static final List<CommentRecord> commentList = new ArrayList<CommentRecord>();

	/** The list view that shows comments */
	private final ListView<CommentRecord> commentListView;

	@Inject
	private Provider<DSLContext> contextProvider;

	DSLContext context;

	public HomePage()
	{
		context = contextProvider.get();

		addMetaData();
		addVideo();
		addFilesTree();

		final CommentForm commentForm = new CommentForm("commentForm");
		add(commentForm);

		comments = new WebMarkupContainer("comments");
		add(comments.setOutputMarkupId(true));

		// commentList = context.select(Tables.COMMENT.FK_VIDEO_ID).where("1").execute();
		// commentList.add(new CommentRecord(1, 1, "31-01-2014", "admin", "Dit is een comment!"));

		// Add commentListView of existing comments
		comments.add(commentListView = new ListView<CommentRecord>("comments", new PropertyModel<List<CommentRecord>>(
			this, "commentList"))
		{
			@Override
			public void populateItem(final ListItem<CommentRecord> listItem)
			{
				final CommentRecord comment = listItem.getModelObject();
				listItem.add(new Label("date", comment.getDatetime()));
				listItem.add(new MultiLineLabel("text", comment.getComment()));
			}
		});
	}

	public void addMetaData()
	{
		final StringBuilder metaBuilder = new StringBuilder();
		metaBuilder.append("MetaData");

		// final Label metaData = new Label("metaData", videofile.getMetaDataAsString());
		final Label metaData = new Label("metaData", metaBuilder.toString());
		add(metaData);
	}

	public void addVideo()
	{
		add(new Html5Video("videoplayer", getMediaSourceList())
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected boolean isControls()
			{
				return true;
			}

		});
	}

	public void addFilesTree()
	{
		final ITreeProvider<File> treeProvider = createTreeProvider();
		final NestedTree<File> files = new NestedTree<File>("files", treeProvider)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Component newContentComponent(final String id, final IModel<File> model)
			{
				return new Folder<File>(id, this, model)
				{
					private static final long serialVersionUID = 1L;

					@Override
					protected IModel<?> newLabelModel(final IModel<File> model)
					{
						return Model.of(videoService.getVideoName(model.getObject().getName()));
					}

					@Override
					protected boolean isClickable()
					{
						final File file = getModelObject();
						if (file.isDirectory())
						{
							return true;
						}

						if (file.getName().endsWith(".webM"))
						{
							return true;
						}

						return false;
					}

					@Override
					protected void onClick(final AjaxRequestTarget target)
					{
						final File file = getModelObject();

						if (file.isDirectory())
						{
							super.onClick(target);
						}
						else
						{
							final VideoFile videoFile = new VideoFile();
							videoFile.setFilename(file.getAbsolutePath());
							LOG.info(videoFile.getFilename());
							SecurityUtils.getSubject().getSession().setAttribute("video", videoFile.getFilename());
						}
					}
				};
			}
		};
		files.setOutputMarkupId(true);
		files.add(new WindowsTheme());
		add(files);

		final Iterator<? extends File> rootIterator = treeProvider.getRoots();
		while (rootIterator.hasNext())
		{
			final File root = rootIterator.next();
			files.expand(root);
		}
	}

	public IModel<List<MediaSource>> getMediaSourceList()
	{
		final List<MediaSource> mediaSource = new ArrayList<MediaSource>();
		mediaSource.add(new MediaSource(StringEscapeUtils.escapeJavaScript("/var/sample_mpeg4.mp4"), "video/mp4"));

		final IModel<List<MediaSource>> mediaSourceList = new AbstractReadOnlyModel<List<MediaSource>>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public List<MediaSource> getObject()
			{
				return mediaSource;
			}
		};
		return mediaSourceList;
	}

	private ITreeProvider<File> createTreeProvider()
	{
		return new FileTreeProvider(new AbstractReadOnlyModel<List<File>>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public List<File> getObject()
			{
				return Collections.singletonList(videoService.getStorageFolder());
			}
		});
	}
}
