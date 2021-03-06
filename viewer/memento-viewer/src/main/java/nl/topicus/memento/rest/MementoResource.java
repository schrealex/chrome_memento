package nl.topicus.memento.rest;

import java.io.InputStream;
import java.util.UUID;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import nl.topicus.memento.videoservice.VideoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
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

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String onPost(@FormDataParam("file") InputStream uploadedInputStream,
		@FormDataParam("file") FormDataContentDisposition fileDetail)

	{
		String fileName = UUID.randomUUID().toString() + fileDetail.getFileName();
		videoService.saveToDatabase(uploadedInputStream, fileDetail, fileName);
		videoService.saveToStorage(uploadedInputStream, fileDetail, fileName);

		return "OK";
	}

}
