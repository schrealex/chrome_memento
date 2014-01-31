package nl.topicus.memento.web.page;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import nl.topicus.memento.web.components.FileTreeProvider;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.WindowsTheme;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.html5.media.MediaSource;
import org.wicketstuff.html5.media.video.Html5Video;
import org.wicketstuff.shiro.ShiroConstraint;
import org.wicketstuff.shiro.annotation.ShiroSecurityConstraint;

@ShiroSecurityConstraint(constraint = ShiroConstraint.IsAuthenticated)
public class HomePage extends BasePage
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(HomePage.class);

	public HomePage()
	{
		addFilesTree();
		addMetaData();
		add(new Html5Video("videoplayer", getMediaSourceList()));
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
						return Model.of(model.getObject().getName());
					}

					@Override
					protected boolean isClickable()
					{
						final File file = getModelObject();
						if (file.isDirectory())
						{
							return true;
						}

						if (file.getName().endsWith(".mp4"))
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

	public void addMetaData()
	{
		final StringBuilder metaBuilder = new StringBuilder();
		metaBuilder.append("MetaData");

		// final Label metaData = new Label("metaData", videofile.getMetaDataAsString());
		final Label metaData = new Label("metaData", metaBuilder.toString());
		add(metaData);
	}

	private ITreeProvider<File> createTreeProvider()
	{
		return new FileTreeProvider(new AbstractReadOnlyModel<List<File>>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public List<File> getObject()
			{
				return Collections.singletonList(new File("var"));
			}
		});
	}
}
