/*
 * Dog 3.1 - Dumb Appliance Driver
 * 
 * 
 * Copyright 2014 Dario Bonino 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package it.polito.elite.dog.drivers.appliances.base.service;

import it.polito.elite.dog.core.library.util.LogHelper;
import it.polito.elite.dog.drivers.appliances.base.interfaces.ApplianceStateMachineLocator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

/**
 * @author bonino
 * 
 */
public class ApplianceStateMachineLocatorImpl implements
		ApplianceStateMachineLocator, ManagedService
{
	public static final String STATE_MACHINE_REPO = "stateMachinesRepository";

	// the location of the state machine repository
	private String stateMachineRepositoryLocation;

	// The OSGi framework context
	protected BundleContext context;

	// the registration object needed to handle the life span of this bundle in
	// the OSGi framework (it is a ServiceRegistration object for use by the
	// bundle registering the service to update the service's properties or to
	// unregister the service).
	private ServiceRegistration<?> regService;

	// System logger
	LogHelper logger;
	
	

	/**
	 * 
	 */
	public ApplianceStateMachineLocatorImpl()
	{
		// intentionally left empty
	}

	/**
	 * Handle the bundle activation
	 */
	public void activate(BundleContext bundleContext)
	{
		// store the context
		context = bundleContext;

		// init the logger
		logger = new LogHelper(context);
	}

	public void deactivate()
	{
		// remove the service from the OSGi framework
		this.unRegisterService();
	}

	/**
	 * Handle the bundle de-activation
	 */
	private void unRegisterService()
	{
		// un-registers this driver
		if (regService != null)
		{
			regService.unregister();
			regService = null;
		}
	}

	/**
	 * Registers this driver in the OSGi framework, making its services
	 * available to all the other bundles living in the same or in connected
	 * frameworks.
	 */
	private void registerService()
	{
		if ((this.context != null) && (this.regService == null))
		{
			Hashtable<String, Object> propService = new Hashtable<String, Object>();

			regService = context.registerService(
					ApplianceStateMachineLocator.class.getName(), this,
					propService);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polito.elite.dog.drivers.appliances.base.interfaces.
	 * ApplianceStateMachineLocator#getStateMachine(java.lang.String)
	 */
	@Override
	public URL getStateMachine(String serialNumber)
	{
		// the URL of the scxml file corresponding to the given serial number
		URL scxmlURL = null;

		// check if a repository has been configured
		if (this.stateMachineRepositoryLocation != null)
		{

			// compose the file location
			String scxmlFileLocation = this.stateMachineRepositoryLocation
					+ "/" + serialNumber + ".scxml";

			// open a file on the location
			File scxmlFile = new File(scxmlFileLocation);

			// check if the file exists
			if (scxmlFile.exists())
			{
				try
				{
					// get the file url to return
					scxmlURL = scxmlFile.toURI().toURL();
				}
				catch (MalformedURLException e)
				{
					// do nothing, log the error if needed
				}
			}

		}

		// return the foun url, if any.
		return scxmlURL;
	}

	@Override
	public void updated(Dictionary<String, ?> config)
			throws ConfigurationException
	{
		// check if configuration is not null,
		// creation will be disabled
		if (config != null)
		{
			this.stateMachineRepositoryLocation = (String) config
					.get(ApplianceStateMachineLocatorImpl.STATE_MACHINE_REPO);

			// check not null
			if (this.stateMachineRepositoryLocation != null)
			{
				// trim the state machine repository location
				this.stateMachineRepositoryLocation = this.stateMachineRepositoryLocation
						.trim();

				// open a file on the location and store it.
				File repoFile = new File(this.stateMachineRepositoryLocation);

				if (!repoFile.isAbsolute())
					this.stateMachineRepositoryLocation = System
							.getProperty("configFolder")
							+ "/"
							+ this.stateMachineRepositoryLocation;
				
				//register the service
				this.registerService();
			}
		}

	}

}
