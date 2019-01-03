package com.semisky.jlradio.model;

public interface IRadioCollectModel {
	void addIRadioCollectCallback(IRadioCollectCallback callback);

	void removeIRadioCollectCallback(IRadioCollectCallback callback);

	void collectRadioOrNot(int radioType, int frequency);

	void collectRadio(int radioType, int frequency);

	void disCollectRadio(int frequency);

	void deleteAllCollectRadio();
}
