package nl.topicus.memento.web.components;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * @author verhage
 * @author Jan 31, 2012
 */
public class FileTreeProvider implements ITreeProvider<File>
{
	/** Default */
	private static final long serialVersionUID = 1L;

	private final IModel<List<File>> roots;

	public FileTreeProvider(IModel<List<File>> roots)
	{
		this.roots = roots;
	}

	/**
	 * @see wickettree.ITreeProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Iterator<? extends File> getChildren(File object)
	{
		File[] children = object.listFiles();

		if (children != null)
		{
			Arrays.sort(children);
			return Arrays.asList(children).iterator();
		}

		return null;
	}

	/**
	 * @see wickettree.ITreeProvider#getRoots()
	 */
	@Override
	public Iterator<? extends File> getRoots()
	{
		return roots.getObject().iterator();
	}

	/**
	 * @see wickettree.ITreeProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(File object)
	{
		return object.isDirectory() && (object.listFiles() != null || object.listFiles().length > 0);
	}

	/**
	 * @see wickettree.ITreeProvider#model(java.lang.Object)
	 */
	@Override
	public IModel<File> model(File object)
	{
		return new Model<File>(object);
	}

	/**
	 * @see org.apache.wicket.model.IDetachable#detach()
	 */
	@Override
	public void detach()
	{
		roots.detach();
	}
}
