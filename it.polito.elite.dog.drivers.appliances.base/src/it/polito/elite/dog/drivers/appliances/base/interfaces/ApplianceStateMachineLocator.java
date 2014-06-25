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
package it.polito.elite.dog.drivers.appliances.base.interfaces;

import java.net.URL;

/**
 * @author bonino
 * 
 */
public interface ApplianceStateMachineLocator
{
	/**
	 * The URL of the state machine document corresponding to the given serial number.
	 * @param serialNumber
	 * @return
	 */
	public URL getStateMachine(String serialNumber);
}
