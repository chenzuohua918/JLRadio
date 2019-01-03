package com.semisky.jlradio.model;

public interface IRadioPlayModel {
	void addIRadioPlayCallback(IRadioPlayCallback callback);

	void removeIRadioPlayCallback(IRadioPlayCallback callback);

	void playRadio(int targetType, int frequency);
}
