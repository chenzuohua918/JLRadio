package com.semisky.jlradio.model;

/**
 * 收音机开关Model
 * 
 * @author Anter
 * 
 */
public interface IRadioSwitchModel {
	void addIRadioSwitchCallback(IRadioSwitchCallback callback);

	void removeIRadioSwitchCallback(IRadioSwitchCallback callback);

	void switchOnOff(boolean on_off);
}
