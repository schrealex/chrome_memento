package nl.topicus.memento.runtime;

import org.apache.commons.daemon.support.DaemonLoader.Context;

import com.synsere.service.SynsereService;

public final class Launcher
{
	private Launcher()
	{

	}

	public static void main(String[] args) throws Exception
	{
		Class.forName("org.sqlite.JDBC");
		SynsereService service = new SynsereService();
		Context context = new Context();
		context.setArguments(new String[] { "-service_class=nl.topicus.memento.runtime.SiteApplication" });
		service.init(context);
		service.start();

		synchronized ("forever")
		{
			"forever".wait();
		}
	}
}
