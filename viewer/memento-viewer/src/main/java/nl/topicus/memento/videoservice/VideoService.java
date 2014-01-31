package nl.topicus.memento.videoservice;

import java.io.File;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class VideoService
{
	@Inject
	@Named("storage.folder")
	private File storageFolder;

	public File getStorageFolder()
	{
		return storageFolder;
	}

	public String getVideoName(final String UUID)
	{
		if (UUID.contains(".mp4"))
		{
			return "video " + UUID;
		}
		else
		{
			return UUID;
		}

	}
}
