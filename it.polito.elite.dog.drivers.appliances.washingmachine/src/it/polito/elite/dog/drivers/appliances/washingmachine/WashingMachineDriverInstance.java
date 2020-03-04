/**
 * 
 */
package it.polito.elite.dog.drivers.appliances.washingmachine;

import it.polito.elite.dog.core.library.model.ControllableDevice;
import it.polito.elite.dog.core.library.model.DeviceStatus;
import it.polito.elite.dog.core.library.model.devicecategory.WashingMachine;
import it.polito.elite.dog.core.library.model.state.ConnectionState;
import it.polito.elite.dog.core.library.model.state.FaultState;
import it.polito.elite.dog.core.library.model.state.ProgramState;
import it.polito.elite.dog.core.library.model.state.RunState;
import it.polito.elite.dog.core.library.model.state.StandByOnOffState;
import it.polito.elite.dog.core.library.model.state.WashingApplianceState;
import it.polito.elite.dog.core.library.model.statevalue.ConnectedStateValue;
import it.polito.elite.dog.core.library.model.statevalue.DisconnectedStateValue;
import it.polito.elite.dog.core.library.model.statevalue.HeatingStateValue;
import it.polito.elite.dog.core.library.model.statevalue.IdleStateValue;
import it.polito.elite.dog.core.library.model.statevalue.NoFailureStateValue;
import it.polito.elite.dog.core.library.model.statevalue.NotProgrammedStateValue;
import it.polito.elite.dog.core.library.model.statevalue.OffStateValue;
import it.polito.elite.dog.core.library.model.statevalue.OnStateValue;
import it.polito.elite.dog.core.library.model.statevalue.RinseHoldStateValue;
import it.polito.elite.dog.core.library.model.statevalue.RunningStateValue;
import it.polito.elite.dog.core.library.model.statevalue.StandByStateValue;
import it.polito.elite.dog.core.library.model.statevalue.WashRinseStateValue;
import it.polito.elite.dog.core.library.util.LogHelper;
import it.polito.elite.dog.drivers.appliances.base.ApplianceDriverInstance;
import it.polito.elite.dog.drivers.appliances.base.interfaces.ApplianceStateMachineLocator;

import javax.measure.Measure;

import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

/**
 * @author bonino
 * 
 */
public class WashingMachineDriverInstance extends ApplianceDriverInstance
        implements WashingMachine
{

    /**
     * @param device
     * @param stateMachineLocator
     * @param logger
     * @param context
     */
    public WashingMachineDriverInstance(ControllableDevice device,
            ApplianceStateMachineLocator stateMachineLocator, LogHelper logger,
            BundleContext context)
    {
        super(device, stateMachineLocator, logger);

        // initialize states
        this.initializeStates();
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.polito.elite.dog.drivers.appliances.base.ApplianceDriverInstance#
     * newMessageFromHouse(java.lang.String)
     */
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

                // change the current state
                this.currentState.setState(RunState.class.getSimpleName(),
                        new RunState(new RunningStateValue()));

                // notify the state change
                this.notifyOn();
                this.notifyStart();
                this.updateStatus();
                break;
            }
            case "off":
            {
                // change the current state
                this.currentState.setState(
                        StandByOnOffState.class.getSimpleName(),
                        new StandByOnOffState(new OffStateValue()));

                this.currentState.setState(RunState.class.getSimpleName(),
                        new RunState(new IdleStateValue()));

                this.currentState.setState(
                        WashingApplianceState.class.getSimpleName(),
                        new WashingApplianceState(new RinseHoldStateValue()));

                this.currentState.setState(
                        ConnectionState.class.getSimpleName(),
                        new ConnectionState(new DisconnectedStateValue()));

                // notify the state change
                this.notifyOff();
                this.updateStatus();
                break;
            }

            case "standby":
            {
                // change the current state
                this.currentState.setState(RunState.class.getSimpleName(),
                        new RunState(new IdleStateValue()));

                this.currentState.setState(
                        ConnectionState.class.getSimpleName(),
                        new ConnectionState(new ConnectedStateValue()));

                this.currentState.setState(
                        StandByOnOffState.class.getSimpleName(),
                        new StandByOnOffState(new StandByStateValue()));

                this.currentState.setState(
                        WashingApplianceState.class.getSimpleName(),
                        new WashingApplianceState(new RinseHoldStateValue()));

                // notify the state change
                this.notifyStop();
                this.notifyStandby();
                this.updateStatus();
                break;
            }

            case "washing":
            {
                // check the current state and if needed send the stopped
                // heating notification
                if (this.currentState
                        .getState(WashingApplianceState.class.getSimpleName())
                        .getCurrentStateValue()[0] instanceof HeatingStateValue)
                    // send the notification
                    this.notifyStoppedHeatingCycle();

                // change the current state
                this.currentState.setState(
                        WashingApplianceState.class.getSimpleName(),
                        new WashingApplianceState(new WashRinseStateValue()));

                // notify the state change
                this.notifyStartedWashOrRinseCycle();
                this.updateStatus();
                break;
            }

            case "heating":
            {
                // check the current state and if needed send the stopped
                // heating notification
                if (this.currentState
                        .getState(WashingApplianceState.class.getSimpleName())
                        .getCurrentStateValue()[0] instanceof WashRinseStateValue)
                    // send the notification
                    this.notifyStoppedWashOrRinseCycle();

                // change the current state
                this.currentState.setState(
                        WashingApplianceState.class.getSimpleName(),
                        new WashingApplianceState(new HeatingStateValue()));

                // notify the state change
                this.notifyStartedHeatingCycle();
                this.updateStatus();
                break;
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see it.polito.elite.dog.drivers.appliances.base.ApplianceDriverInstance#
     * specificConfiguration()
     */
    @Override
    protected void specificConfiguration()
    {
        // intentionally left empty

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * getRemainingTime()
     */
    @Override
    public Measure<?, ?> getRemainingTime()
    {
        // intentionally left empty
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * stop()
     */
    @Override
    public void stop()
    {
        // intentionally left empty
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * getState()
     */
    @Override
    public DeviceStatus getState()
    {
        // return the device state
        return this.currentState;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * getEndTime()
     */
    @Override
    public Measure<?, ?> getEndTime()
    {
        // intentionally left empty
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#on()
     */
    @Override
    public void on()
    {
        // intentionally left empty
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * start()
     */
    @Override
    public void start()
    {
        // intentionally left empty
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * pause()
     */
    @Override
    public void pause()
    {
        // intentionally left empty
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * getEventsAndAlerts()
     */
    @Override
    public Object[] getEventsAndAlerts()
    {
        // intentionally left empty
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * setStartTime(javax.measure.Measure, javax.measure.Measure,
     * javax.measure.Measure)
     */
    @Override
    public void setStartTime(Measure<?, ?> endTime, Measure<?, ?> remainingTime,
            Measure<?, ?> startTime)
    {
        // intentionally left empty
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * standBy()
     */
    @Override
    public void standBy()
    {
        // intentionally left empty
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * off()
     */
    @Override
    public void off()
    {
        // intentionally left empty
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * getStartTime()
     */
    @Override
    public Measure<?, ?> getStartTime()
    {
        // intentionally left empty
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * notifyStart()
     */
    @Override
    public void notifyStart()
    {
        ((WashingMachine) this.device).notifyStart();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * notifyNewEvent(java.lang.Object)
     */
    @Override
    public void notifyNewEvent(Object event)
    {
        ((WashingMachine) this.device).notifyNewEvent(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * notifyNewAlert(java.lang.Object)
     */
    @Override
    public void notifyNewAlert(Object alert)
    {
        ((WashingMachine) this.device).notifyNewAlert(alert);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * notifyChangedStartTime(javax.measure.Measure)
     */
    @Override
    public void notifyChangedStartTime(Measure<?, ?> startTime)
    {
        ((WashingMachine) this.device).notifyChangedStartTime(startTime);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * notifyOn()
     */
    @Override
    public void notifyOn()
    {
        ((WashingMachine) this.device).notifyOn();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * notifyChangedEndTime(javax.measure.Measure)
     */
    @Override
    public void notifyChangedEndTime(Measure<?, ?> endTime)
    {
        ((WashingMachine) this.device).notifyChangedEndTime(endTime);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * notifyChangedRemainingTime(javax.measure.Measure)
     */
    @Override
    public void notifyChangedRemainingTime(Measure<?, ?> remainingTime)
    {
        ((WashingMachine) this.device)
                .notifyChangedRemainingTime(remainingTime);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * notifyStandby()
     */
    @Override
    public void notifyStandby()
    {
        ((WashingMachine) this.device).notifyStandby();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * notifyOff()
     */
    @Override
    public void notifyOff()
    {
        ((WashingMachine) this.device).notifyOff();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * notifyStop()
     */
    @Override
    public void notifyStop()
    {
        ((WashingMachine) this.device).notifyStop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * notifyPause()
     */
    @Override
    public void notifyPause()
    {
        ((WashingMachine) this.device).notifyPause();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.polito.elite.dog.core.library.model.devicecategory.WashingMachine#
     * updateStatus()
     */
    @Override
    public void updateStatus()
    {
        ((WashingMachine) this.device).updateStatus();
    }

    @Override
    public void notifyStartedHeatingCycle()
    {
        ((WashingMachine) this.device).notifyStartedHeatingCycle();
    }

    @Override
    public void notifyStoppedHeatingCycle()
    {
        ((WashingMachine) this.device).notifyStoppedHeatingCycle();
    }

    @Override
    public void notifyStartedWashOrRinseCycle()
    {
        ((WashingMachine) this.device).notifyStartedWashOrRinseCycle();
    }

    @Override
    public void notifyStoppedWashOrRinseCycle()
    {
        ((WashingMachine) this.device).notifyStoppedWashOrRinseCycle();
    }

    @Override
    public void notifyNewEvent(Object event, Object sensorId)
    {
        ((WashingMachine) this.device).notifyNewEvent(event, sensorId);
    }

    @Override
    public void notifyNewAlert(Object alertId, Object alert)
    {
        ((WashingMachine) this.device).notifyNewAlert(alertId, alert);
    }

    private void initializeStates()
    {
        // initialize the state at off
        this.currentState.setState(StandByOnOffState.class.getSimpleName(),
                new StandByOnOffState(new OffStateValue()));

        this.currentState.setState(WashingApplianceState.class.getSimpleName(),
                new WashingApplianceState(new RinseHoldStateValue()));

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
