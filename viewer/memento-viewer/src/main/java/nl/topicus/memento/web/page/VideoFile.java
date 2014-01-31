package nl.topicus.memento.web.page;

public class VideoFile
{
	private String filename = "";

	private String filepath = "";

	public VideoFile()
	{
	}

	public VideoFile(final String filename, final String filepath)
	{
		super();
		this.filename = filename;
		this.filepath = filepath;
	}

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(final String filename)
	{
		this.filename = filename;
	}

	public String getFilepath()
	{
		return filepath;
	}

	public void setFilepath(final String filepath)
	{
		this.filepath = filepath;
	}
}
