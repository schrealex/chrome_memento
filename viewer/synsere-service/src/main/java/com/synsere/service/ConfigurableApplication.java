package com.synsere.service;

import com.synsere.module.ConfigurationModule;
import com.twitter.common.application.Application;

public interface ConfigurableApplication extends Application
{
	ConfigurationModule getConfigurationModule();
}
