package com.semisky.jlradio.model;

public interface IRadioCollectCallback {
	void onRadioCollected(int radioType, int frequency);

	void onRadioUnCollected(int frequency);

	void onAllCollectRadioDelete();
}
