package it.polito.elite.dog.drivers.appliances.microwave;

import it.polito.elite.dog.core.library.model.ControllableDevice;
import it.polito.elite.dog.core.library.model.DeviceStatus;
import it.polito.elite.dog.core.library.model.devicecategory.MicrowaveOven;
import it.polito.elite.dog.core.library.model.state.ConnectionState;
import it.polito.elite.dog.core.library.model.state.FaultState;
import it.polito.elite.dog.core.library.model.state.MicrowaveEmissionState;
import it.polito.elite.dog.core.library.model.state.ProgramState;
import it.polito.elite.dog.core.library.model.state.RunState;
import it.polito.elite.dog.core.library.model.state.StandByOnOffState;
import it.polito.elite.dog.core.library.model.statevalue.DisconnectedStateValue;
import it.polito.elite.dog.core.library.model.statevalue.EmittingMicrowavesStateValue;
import it.polito.elite.dog.core.library.model.statevalue.IdleStateValue;
import it.polito.elite.dog.core.library.model.statevalue.NoFailureStateValue;
import it.polito.elite.dog.core.library.model.statevalue.NotEmittingMicrowavesStateValue;
import it.polito.elite.dog.core.library.model.statevalue.NotProgrammedStateValue;
import it.polito.elite.dog.core.library.model.statevalue.OffStateValue;
import it.polito.elite.dog.core.library.model.statevalue.OnStateValue;
import it.polito.elite.dog.core.library.model.statevalue.RunningStateValue;
import it.polito.elite.dog.core.library.model.statevalue.StandByStateValue;
import it.polito.elite.dog.core.library.util.LogHelper;
import it.polito.elite.dog.drivers.appliances.base.ApplianceDriverInstance;
import it.polito.elite.dog.drivers.appliances.base.interfaces.ApplianceStateMachineLocator;

import javax.measure.Measure;

import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

public class MicrowaveOvenDriverInstance extends ApplianceDriverInstance
		implements MicrowaveOven
{

	public MicrowaveOvenDriverInstance(ControllableDevice device,
			ApplianceStateMachineLocator stateMachineLocator, LogHelper logger,
			BundleContext context)
	{
		super(device, stateMachineLocator, logger);

		// initialize states
		this.initializeStates();
	}

	@Override
	protected void newMessageFromHouse(String newStateName)
	{
		this.logger.log(LogService.LOG_INFO, "Entered:" + newStateName);
		// handle the new state
		switch (newStateName)
		{
			case "on":
			{
				// change the current state
				this.currentState.setState(
						StandByOnOffState.class.getSimpleName(),
						new StandByOnOffState(new OnStateValue()));

				// notify the state change
				this.notifyOn();
				break;
			}
			case "off":
			{
				// change the current state
				this.currentState.setState(
						StandByOnOffState.class.getSimpleName(),
						new StandByOnOffState(new OffStateValue()));

				// notify the state change
				this.notifyOff();
				break;
			}
			case "running":
			{
				// change the current state
				this.currentState.setState(RunState.class.getSimpleName(),
						new RunState(new RunningStateValue()));

				// notify the state change
				// TODO: check if the dogont model is correct or should be
				// changed..
				this.notifyStart();
				break;
			}

			case "wave_on":
			{
				// change the current state
				this.currentState.setState(MicrowaveEmissionState.class
						.getSimpleName(), new MicrowaveEmissionState(
						new EmittingMicrowavesStateValue()));
				// notify the state change
				this.notifyEmittingMicrowaves();
				break;
			}

			case "wave_off":
			{
				// change the current state
				this.currentState.setState(MicrowaveEmissionState.class
						.getSimpleName(), new MicrowaveEmissionState(
						new NotEmittingMicrowavesStateValue()));
				// notify the state change
				this.notifyNotEmittingMicrowaves();
				break;
			}

			case "door_open":
			{
				// change the current state
				this.currentState.setState(RunState.class.getSimpleName(),
						new RunState(new IdleStateValue()));

				// notify
				this.notifyStop();
				break;
			}

			case "standby":
			{
				// change the current state
				this.currentState.setState(RunState.class.getSimpleName(),
						new RunState(new IdleStateValue()));

				// change the current state
				this.currentState.setState(
						StandByOnOffState.class.getSimpleName(),
						new StandByOnOffState(new StandByStateValue()));

				// notify the state change
				this.notifyStop();
				this.notifyStandby();
				break;
			}
		}
		
		this.updateStatus();

	}

	@Override
	protected void specificConfiguration()
	{
		// intentionally left empty

	}

	@Override
	public void startSuperHeating()
	{
		// do nothing....
	}

	@Override
	public Measure<?, ?> getRemainingTime()
	{
		// do nothing....
		return null;
	}

	@Override
	public void stop()
	{
		// do nothing....
	}

	@Override
	public DeviceStatus getState()
	{
		// return the current device state
		return this.currentState;
	}

	@Override
	public Measure<?, ?> getEndTime()
	{
		// do nothing....
		return null;
	}

	@Override
	public void on()
	{
		// do nothing....
	}

	@Override
	public void stopSuperHeating()
	{
		// do nothing....
	}

	@Override
	public void start()
	{
		// do nothing....
	}

	@Override
	public void pause()
	{
		// do nothing....
	}

	@Override
	public Object[] getEventsAndAlerts()
	{
		// do nothing....
		return null;
	}

	@Override
	public void setStartTime(Measure<?, ?> endTime,
			Measure<?, ?> remainingTime, Measure<?, ?> startTime)
	{
		// do nothing....
	}

	@Override
	public void off()
	{
		// do nothing....
	}

	@Override
	public Measure<?, ?> getStartTime()
	{
		// do nothing....
		return null;
	}

	@Override
	public void standBy()
	{
		// do nothing....
	}

	@Override
	public void notifyStandby()
	{
		((MicrowaveOven) this.device).notifyStandby();
	}

	@Override
	public void notifyStart()
	{
		((MicrowaveOven) this.device).notifyStart();
	}

	@Override
	public void notifyNewEventSet(Object[] events)
	{
		((MicrowaveOven) this.device).notifyNewEventSet(events);
	}

	@Override
	public void notifyNewAlertSet(Object[] alerts)
	{
		((MicrowaveOven) this.device).notifyNewAlertSet(alerts);
	}

	@Override
	public void notifyNewEvent(Object event)
	{
		((MicrowaveOven) this.device).notifyNewEvent(event);
	}

	@Override
	public void notifyNewAlert(Object alert)
	{
		((MicrowaveOven) this.device).notifyNewAlert(alert);
	}

	@Override
	public void notifyChangedEndTime(Measure<?, ?> endTime)
	{
		((MicrowaveOven) this.device).notifyChangedEndTime(endTime);
	}

	@Override
	public void notifyChangedRemainingTime(Measure<?, ?> remainingTime)
	{
		((MicrowaveOven) this.device).notifyChangedRemainingTime(remainingTime);
	}

	@Override
	public void notifyStop()
	{
		((MicrowaveOven) this.device).notifyStop();
	}

	@Override
	public void notifyOff()
	{
		((MicrowaveOven) this.device).notifyOff();
	}

	@Override
	public void notifyStoppedSuperHeating()
	{
		((MicrowaveOven) this.device).notifyStoppedSuperHeating();
	}

	@Override
	public void notifyNotEmittingMicrowaves()
	{
		((MicrowaveOven) this.device).notifyNotEmittingMicrowaves();
	}

	@Override
	public void notifyEmittingMicrowaves()
	{
		((MicrowaveOven) this.device).notifyEmittingMicrowaves();
	}

	@Override
	public void notifyChangedStartTime(Measure<?, ?> startTime)
	{
		((MicrowaveOven) this.device).notifyChangedStartTime(startTime);
	}

	@Override
	public void notifyOn()
	{
		((MicrowaveOven) this.device).notifyOn();
	}

	@Override
	public void notifyStartedSuperHeating()
	{
		((MicrowaveOven) this.device).notifyStartedSuperHeating();
	}

	@Override
	public void notifyPause()
	{
		((MicrowaveOven) this.device).notifyPause();
	}

	@Override
	public void updateStatus()
	{
		((MicrowaveOven) this.device).updateStatus();
	}

	/**
	 * Initializes the state of this pellet stove instance, initially set to
	 * off.
	 */
	private void initializeStates()
	{
		// initialize the state at off
		this.currentState.setState(StandByOnOffState.class.getSimpleName(),
				new StandByOnOffState(new OffStateValue()));

		this.currentState.setState(
				MicrowaveEmissionState.class.getSimpleName(),
				new MicrowaveEmissionState(
						new NotEmittingMicrowavesStateValue()));
		
		this.currentState.setState(RunState.class.getSimpleName(),
				new RunState(new IdleStateValue()));
		
		this.currentState.setState(FaultState.class.getSimpleName(),
				new FaultState(new NoFailureStateValue()));
		
		this.currentState.setState(ProgramState.class.getSimpleName(),
				new ProgramState(new NotProgrammedStateValue()));
		
		this.currentState.setState(ConnectionState.class.getSimpleName(),
				new ConnectionState(new DisconnectedStateValue()));

	}

}
