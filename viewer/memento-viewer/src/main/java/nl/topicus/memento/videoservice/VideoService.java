package nl.topicus.memento.videoservice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.io.File;

@Singleton
public class VideoService
{
	@Inject
	@Named("storage.folder")
	private File storageFolder;
}
