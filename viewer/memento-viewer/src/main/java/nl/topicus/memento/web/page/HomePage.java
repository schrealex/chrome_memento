package nl.topicus.memento.web.page;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import nl.topicus.memento.videoservice.VideoService;
import nl.topicus.memento.web.components.CommentForm;
import nl.topicus.memento.web.components.FileTreeProvider;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.WindowsTheme;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.html5.media.MediaSource;
import org.wicketstuff.html5.media.video.Html5Video;
import org.wicketstuff.shiro.ShiroConstraint;
import org.wicketstuff.shiro.annotation.ShiroSecurityConstraint;

import com.google.inject.Inject;

@ShiroSecurityConstraint(constraint = ShiroConstraint.IsAuthenticated)
public class HomePage extends BasePage
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(HomePage.class);

	@Inject
	private VideoService videoService;

	private Html5Video video;

	private IModel<File> videoFileModel = new Model<>();

	private WebMarkupContainer videoContainer;

	public HomePage()
	{

	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		addMetaData();
		addVideo();
		addFilesTree();

		final CommentForm commentForm = new CommentForm("commentForm");
		add(commentForm);
	}

	public void addMetaData()
	{
		final StringBuilder metaBuilder = new StringBuilder();
		metaBuilder.append("Publish date: ");

		Date today = Calendar.getInstance().getTime();
		metaBuilder.append(today.toString());

		metaBuilder.append("\n");
		metaBuilder.append("URL: ");
		metaBuilder.append("http://www.topicuszorg.nl");

		// final Label metaData = new Label("metaData", videofile.getMetaDataAsString());
		final MultiLineLabel metaData = new MultiLineLabel("metaData", metaBuilder.toString());
		add(metaData);
	}

	public void addVideo()
	{
		video = new Html5Video("videoplayer", getMediaSourceList())
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected boolean isControls()
			{
				return true;
			}

		};

		videoContainer = new WebMarkupContainer("container");
		videoContainer.setOutputMarkupPlaceholderTag(true);

		videoContainer.add(video);

		add(videoContainer);
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

						if (file.getName().endsWith(".webM") || file.getName().endsWith(".mp4"))
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
							videoFileModel.setObject(file);
							target.add(videoContainer);
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
		return new AbstractReadOnlyModel<List<MediaSource>>()
		{
			@Override
			public List<MediaSource> getObject()
			{
				if (videoFileModel.getObject() != null)
				{
					MediaSource source = new MediaSource("video/" + videoFileModel.getObject().getName(), "video/webp");
					return Arrays.asList(source);
				}
				return Collections.emptyList();
			}
		};
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
