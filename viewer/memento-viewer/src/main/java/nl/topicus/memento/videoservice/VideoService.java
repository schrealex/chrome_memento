package nl.topicus.memento.videoservice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.topicus.memento.db.Tables;

import org.jooq.DSLContext;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sun.jersey.core.header.FormDataContentDisposition;

@Singleton
public class VideoService
{
	@Inject
	private Provider<DSLContext> contextProvider;

	@Inject
	@Named("storage.folder")
	private File storageFolder;

	public File getStorageFolder()
	{
		return storageFolder;
	}

	public String getVideoName(final String UUID)
	{
		if (UUID.contains(".webM"))
		{
			return "video " + UUID;
		}
		else
		{
			return UUID;
		}

	}

	public void saveToDatabase(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String fileName)
	{
		DSLContext context = contextProvider.get();
		context.insertInto(Tables.VIDEO).set(Tables.VIDEO.FILENAME, fileName)
			.set(Tables.VIDEO.VIDEONAME, fileDetail.getName())
			.set(Tables.VIDEO.MAPLOCATION, getStorageFolder().getPath()).set(Tables.VIDEO.LENGTH, 1000)
			.set(Tables.VIDEO.BROWSERTYPE, "Chrome").execute();

	}

	public void saveToStorage(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String fileName)
	{
		writeToFile(uploadedInputStream, fileName);
	}

	// save uploaded file to new location
	private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation)
	{

		try
		{
			int read = 0;
			byte[] bytes = new byte[1024];

			OutputStream out = new FileOutputStream(new File(storageFolder.getPath() + "/" + uploadedFileLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1)
			{
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		}
		catch (IOException e)
		{

			e.printStackTrace();
		}

	}
}
