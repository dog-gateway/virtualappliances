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
package it.polito.elite.dog.drivers.appliances.base;

import it.polito.elite.dog.core.library.model.ControllableDevice;
import it.polito.elite.dog.core.library.model.DeviceCostants;
import it.polito.elite.dog.core.library.model.devicecategory.Controllable;
import it.polito.elite.dog.core.library.model.notification.Notification;
import it.polito.elite.dog.core.library.util.LogHelper;
import it.polito.elite.dog.drivers.appliances.base.interfaces.ApplianceStateMachineLocator;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.device.Device;
import org.osgi.service.device.Driver;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 * Abstract class to use as "template" for writing Appliance Device Drivers,
 * manages all common operations such as:
 * <ul>
 * <li>Driver registration</li>
 * <li>Device match and attach</li>
 * <li>StateMachineLocator binding</li>
 * <li>etc.</li>
 * </ul>
 * 
 * @author bonino
 * 
 */
public abstract class ApplianceDeviceDriver implements Driver, EventHandler
{
	// The OSGi framework context
	protected BundleContext context;

	// System logger
	protected LogHelper logger;

	// the list of instances controlled / spawned by this driver
	protected Hashtable<String, ApplianceDriverInstance> managedInstances;

	// the map of meter - device associations
	protected Hashtable<String, String> meterToDevice;

	// the reference to the state machine locator to be used by the driver
	private AtomicReference<ApplianceStateMachineLocator> stateMachineLocator;

	// the registration object needed to handle the life span of this bundle in
	// the OSGi framework (it is a ServiceRegistration object for use by the
	// bundle registering the service to update the service's properties or to
	// unregister the service).
	private ServiceRegistration<?> regDriver;

	// what are the device categories that can match with this driver?
	protected Set<String> deviceCategories;

	// the driver instance class from which extracting the supported device
	// categories
	protected Class<?> driverInstanceClass;

	/**
	 * Class constructor, initializes inner data structures.
	 */
	public ApplianceDeviceDriver()
	{
		// initialize the list of managed device instances (indexed by device
		// id)
		this.managedInstances = new Hashtable<>();

		// initialize the meter to device table
		this.meterToDevice = new Hashtable<>();

		// initialize the device categories matched by this driver
		this.deviceCategories = new HashSet<>();

		// initialize the atomic reference hosting the binding with the state
		// machine locator
		this.stateMachineLocator = new AtomicReference<>();
	}

	/**
	 * Handle the bundle activation, preparing the driver for the service
	 * registration that will actually occur when all dependencies will be
	 * satisfied.
	 */
	public void activate(BundleContext bundleContext)
	{
		// init the logger
		this.logger = new LogHelper(bundleContext);

		// store the context
		this.context = bundleContext;

		// fill the device categories
		this.properFillDeviceCategories(this.driverInstanceClass);

		// register driver
		registerDeviceDriver();

	}

	/**
	 * Handle the bundle de-activation by unregistering provided services
	 */
	public void deactivate()
	{
		// remove the service from the OSGi framework
		this.unRegisterDeviceDriver();
	}

	/**
	 * Binds an instance of {@link ApplianceStateMachineLocator}
	 * 
	 * @param locator
	 *            The instance to bind to.
	 */
	public void addedApplianceStateMachineLocator(
			ApplianceStateMachineLocator locator)
	{
		// store a reference to the state machine locator needed for handling
		// appliance state emulation / detection
		this.stateMachineLocator.set(locator);
	}

	/**
	 * Removes the binding to the given instance of
	 * {@link ApplianceStateMachineLocator}
	 * 
	 * @param locator
	 *            The instance to unbind from.
	 */
	public void removedApplianceStateMachineLocator(
			ApplianceStateMachineLocator locator)
	{
		// remove the reference to the given state machine locator
		if (this.stateMachineLocator.compareAndSet(locator, null))

			// unregister the driver
			this.unRegisterDeviceDriver();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public synchronized int match(ServiceReference reference) throws Exception
	{
		int matchValue = Device.MATCH_NONE;

		// get the given device category
		String deviceCategory = (String) reference
				.getProperty(DeviceCostants.DEVICE_CATEGORY);

		// get the given device manufacturer
		String manifacturer = (String) reference
				.getProperty(DeviceCostants.MANUFACTURER);

		// compute the matching score between the given device and
		// this driver
		if (deviceCategory != null)
		{
			if ((manifacturer != null)
					&& (manifacturer.equals(ApplianceInfo.MANUFACTURER))
					&& (this.deviceCategories.contains(deviceCategory)))
			{
				matchValue = Controllable.MATCH_MANUFACTURER
						+ Controllable.MATCH_TYPE;
			}

		}

		return matchValue;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public synchronized String attach(ServiceReference reference)
			throws Exception
	{
		// get the referenced device
		ControllableDevice device = ((ControllableDevice) context
				.getService(reference));

		// check if not already attached
		if (!this.managedInstances.containsKey(device.getDeviceId()))
		{

			// create a new driver instance
			ApplianceDriverInstance driverInstance = this
					.createApplianceDriverInstance(device,
							this.stateMachineLocator.get(), this.logger,
							this.context);

			// connect this driver instance with the device
			device.setDriver(driverInstance);

			// store a reference to the connected driver
			synchronized (this.managedInstances)
			{
				this.managedInstances.put(device.getDeviceId(), driverInstance);
			}

			// get the device meter, if any available
			String meter = device.getDeviceDescriptor().getHasMeter();

			// if a meter is associated to the device
			if ((meter != null) && (!meter.isEmpty()))
			{
				// fill the meter to device map
				synchronized (this.meterToDevice)
				{
					this.meterToDevice.put(meter, device.getDeviceId());
				}
			}
		}
		else
		{
			this.context.ungetService(reference);
		}

		return null;
	}

	/**
	 * Abstract method for building a device driver instance, must be
	 * implemented by all extending classes.
	 * 
	 * @param device
	 *            The {@link ControllableDevice} to which the driver instance
	 *            should attach.
	 * @param stateMachineLocator
	 *            The {@link ApplianceStateMachineLocator} to use for gathering
	 *            the right emulator machine.
	 * @param context
	 *            The {@link BundleContext} to use for logging and other
	 *            osgi-related tasks.
	 * @return
	 */
	public abstract ApplianceDriverInstance createApplianceDriverInstance(
			ControllableDevice device,
			ApplianceStateMachineLocator stateMachineLocator, LogHelper logger,
			BundleContext context);

	/**
	 * Registers this driver in the OSGi framework, making its services
	 * available to all the other bundles living in the same or in connected
	 * frameworks.
	 */
	private void registerDeviceDriver()
	{
		if ((this.context != null) && (this.regDriver == null))
		{
			// create a new property object describing this driver
			Hashtable<String, Object> propDriver = new Hashtable<String, Object>();
			// add the id of this driver to the properties
			propDriver.put(DeviceCostants.DRIVER_ID, this.getClass().getName());
			// register this driver in the OSGi framework
			regDriver = context.registerService(Driver.class.getName(), this,
					propDriver);
		}
	}

	/**
	 * Handle the bundle service un-registration
	 */
	protected void unRegisterDeviceDriver()
	{
		// TODO DETACH allocated Drivers
		if (regDriver != null)
		{
			regDriver.unregister();
			regDriver = null;
		}
	}

	/**
	 * Fill a set with all the device categories whose devices can match with
	 * this driver. Automatically retrieve the device categories list by reading
	 * the implemented interfaces of its DeviceDriverInstance class bundle.
	 */
	public void properFillDeviceCategories(Class<?> cls)
	{
		if (cls != null)
		{
			for (Class<?> devCat : cls.getInterfaces())
			{
				this.deviceCategories.add(devCat.getName());
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event
	 * .Event)
	 */
	@Override
	public void handleEvent(Event event)
	{
		// handles incoming events
		Object eventContent = event.getProperty(EventConstants.EVENT);

		// check if the incoming event is a notification
		if (eventContent instanceof Notification)
		{
			// get the notification object
			Notification currentNotification = (Notification) eventContent;

			// get the meter URI
			String meterURI = currentNotification.getDeviceUri();

			//check not null
			if (meterURI != null)
			{
				// get the associated device
				String deviceURI = this.meterToDevice.get(meterURI);

				//check not null
				if (deviceURI != null)
				{
					// get the associated device driver instance
					ApplianceDriverInstance instanceToNotify = this.managedInstances
							.get(deviceURI);

					//check not null
					if (instanceToNotify != null)
						// notify the right instance
						instanceToNotify
								.handleNotification(currentNotification);
				}
			}
		}

	}

}
