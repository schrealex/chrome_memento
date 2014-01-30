importPackage(java.lang);
importPackage(java.util);

memento = (function()
{
	var contextClassLoader = Thread.currentThread().getContextClassLoader();
	var stream = contextClassLoader
			.getResourceAsStream('application.properties');
	var properties = new Properties();
	properties.load(stream);

	stream.close();

	return {
		web : {
			root : properties.get('web.root')
		},

		http : {
			port : new Integer(9222)
		},

		storage : {
			root : properties.get('storage.root')
		},

		security : {
			username : 'admin',
			password : 'welcome'
		}
	}
})();
