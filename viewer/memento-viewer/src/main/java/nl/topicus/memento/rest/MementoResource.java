package nl.topicus.memento.rest;

import java.io.File;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

@Singleton
@Path("/game")
@Produces(MediaType.TEXT_PLAIN)
public class MementoResource
{
	private static final Logger logger = LoggerFactory.getLogger(MementoResource.class);

	@Inject
	@Named("storage.folder")
	private File storageFolder;

	@Inject
	private Provider<DSLContext> contextProvider;

	@GET
	public String onGet(@QueryParam("id") String gameId, @QueryParam("callbackPoint") String video)
	{
		File gameRoot = new File(storageFolder, gameId);

		DSLContext context = contextProvider.get();


		return "ok";
	}
}
