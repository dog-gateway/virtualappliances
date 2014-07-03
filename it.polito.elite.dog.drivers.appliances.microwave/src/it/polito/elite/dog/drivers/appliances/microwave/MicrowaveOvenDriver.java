/**
 * 
 */
package it.polito.elite.dog.drivers.appliances.microwave;

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
public class MicrowaveOvenDriver extends ApplianceDeviceDriver
{

	
	/**
	 * 
	 */
	public MicrowaveOvenDriver()
	{
		// fill supported device categories
		this.driverInstanceClass = MicrowaveOvenDriverInstance.class;
	}

	@Override
	public ApplianceDriverInstance createApplianceDriverInstance(
			ControllableDevice device,
			ApplianceStateMachineLocator stateMachineLocator, LogHelper logger,
			BundleContext context)
	{
		// TODO Auto-generated method stub
		return new MicrowaveOvenDriverInstance(device, stateMachineLocator, logger, context);
	}

}
