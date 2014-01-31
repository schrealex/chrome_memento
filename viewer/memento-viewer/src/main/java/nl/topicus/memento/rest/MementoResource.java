package nl.topicus.memento.rest;

import java.io.*;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import nl.topicus.memento.videoservice.VideoService;

import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Singleton
@Path("/game")
@Produces(MediaType.TEXT_PLAIN)
public class MementoResource
{
	private static final Logger logger = LoggerFactory.getLogger(MementoResource.class);

	@Inject
	private VideoService videoService;

	@Inject
	private Provider<DSLContext> contextProvider;

	// @POST
	// @Consumes(MediaType.MULTIPART_FORM_DATA)
	// public String onPost(@QueryParam("id") String gameId, @QueryParam("callbackPoint") String video)
	// {
	// File gameRoot = new File(storageFolder, gameId);
	//
	// DSLContext context = contextProvider.get();
	//
	// context.insertInto(Tables.VIDEO).set(Tables.VIDEO.ID, "...").execute();
	//
	// return "ok";
	// }

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String onPost(@FormDataParam("file") InputStream uploadedInputStream,
		@FormDataParam("file") FormDataContentDisposition fileDetail)

	{
		String fileLocation = "Users/ilsantijhuis/workspaceDJDD/output/" + fileDetail.getFileName();

		DSLContext context = contextProvider.get();

		// context.insertInto(Tables.VIDEO).set(Tables.VIDEO.ID, "videovalueid").execute();

		return "ok";
	}

	// save uploaded file to new location
	private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation)
	{

		try
		{
			int read = 0;
			byte[] bytes = new byte[1024];

			OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
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
