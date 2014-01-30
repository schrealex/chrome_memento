package nl.topicus.memento.module;

import java.io.File;
import java.sql.SQLException;

import javax.inject.Singleton;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.jolbox.bonecp.BoneCPDataSource;

public class DatabaseModule extends AbstractModule
{

	@Override
	protected void configure()
	{

	}

	@Provides
	@Singleton
	protected DataSource provideDatasource(@Named("storage.folder") File storagefolder) throws NamingException
	{
		BoneCPDataSource dataSource = new BoneCPDataSource();
		dataSource.setDefaultAutoCommit(true);
		dataSource.setJdbcUrl("jdbc:sqlite:" + storagefolder.getAbsolutePath() + "/memento.db");
		dataSource.setUsername("sa");

		return dataSource;
	}

	@Provides
	protected DSLContext provideDSLContext(DataSource dataSource) throws SQLException
	{
		return DSL.using(dataSource, SQLDialect.SQLITE);
	}
}
