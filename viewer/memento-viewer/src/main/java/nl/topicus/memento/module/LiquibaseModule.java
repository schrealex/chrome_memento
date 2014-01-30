package nl.topicus.memento.module;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class LiquibaseModule extends AbstractModule
{

	@Override
	protected void configure()
	{

	}

	@Provides
	private Liquibase provideLiquibase(DataSource dataSource) throws SQLException, LiquibaseException
	{
		Connection connection = dataSource.getConnection();
		Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
			new JdbcConnection(connection));

		return new Liquibase("liquibase/memento-changelog-master.xml", new ClassLoaderResourceAccessor(), database);
	}
}
