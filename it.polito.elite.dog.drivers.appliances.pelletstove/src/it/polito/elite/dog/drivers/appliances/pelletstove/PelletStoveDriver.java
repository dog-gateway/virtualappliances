/*
 * Dog 3.1 - Dumb Pellet Stove Driver
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
package it.polito.elite.dog.drivers.appliances.pelletstove;

import it.polito.elite.dog.core.library.model.ControllableDevice;
import it.polito.elite.dog.core.library.model.devicecategory.PelletHeater;
import it.polito.elite.dog.core.library.util.LogHelper;
import it.polito.elite.dog.drivers.appliances.base.ApplianceDeviceDriver;
import it.polito.elite.dog.drivers.appliances.base.ApplianceDriverInstance;
import it.polito.elite.dog.drivers.appliances.base.interfaces.ApplianceStateMachineLocator;

import org.osgi.framework.BundleContext;

/**
 * @author bonino
 * 
 */
public class PelletStoveDriver extends ApplianceDeviceDriver
{

	/**
	 * Class constructor, initializes inner data structures
	 */
	public PelletStoveDriver()
	{
		// fill supported device categories
		this.deviceCategories.add(PelletHeater.class.getName());
		this.driverInstanceClass = PelletStoveDriverInstance.class;
	}

	@Override
	public ApplianceDriverInstance createApplianceDriverInstance(
			ControllableDevice device,
			ApplianceStateMachineLocator stateMachineLocator, LogHelper logger,
			BundleContext context)
	{
		return new PelletStoveDriverInstance(device, stateMachineLocator, logger, context);
	}



}
