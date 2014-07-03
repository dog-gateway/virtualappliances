/**
 * 
 */
package it.polito.elite.dog.drivers.appliances.washingmachine;

import org.osgi.framework.BundleContext;

import it.polito.elite.dog.core.library.model.ControllableDevice;
import it.polito.elite.dog.core.library.util.LogHelper;
import it.polito.elite.dog.drivers.appliances.base.ApplianceDeviceDriver;
import it.polito.elite.dog.drivers.appliances.base.ApplianceDriverInstance;
import it.polito.elite.dog.drivers.appliances.base.interfaces.ApplianceStateMachineLocator;

/**
 * @author bonino
 * 
 */
public class WashingMachineDriver extends ApplianceDeviceDriver
{

	/**
	 * 
	 */
	public WashingMachineDriver()
	{
		// fill the device categories
		this.driverInstanceClass = WashingMachineDriverInstance.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polito.elite.dog.drivers.appliances.base.ApplianceDeviceDriver#
	 * createApplianceDriverInstance
	 * (it.polito.elite.dog.core.library.model.ControllableDevice,
	 * it.polito.elite
	 * .dog.drivers.appliances.base.interfaces.ApplianceStateMachineLocator,
	 * it.polito.elite.dog.core.library.util.LogHelper,
	 * org.osgi.framework.BundleContext)
	 */
	@Override
	public ApplianceDriverInstance createApplianceDriverInstance(
			ControllableDevice device,
			ApplianceStateMachineLocator stateMachineLocator, LogHelper logger,
			BundleContext context)
	{
		return new WashingMachineDriverInstance(device, stateMachineLocator, logger, context);
	}

}
