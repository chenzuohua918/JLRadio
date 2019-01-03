package com.semisky.jlradio.model;

public interface SearchAllAMListener {
	void notifyObserversClearAMList();

	void notifyObserversSearchAllAMFinish();

	void notifyObserversSearchAllAMUnFinish(int frequency);

	void notifyObserversSearchAllAMInterrupt();

	void notifyObserversSearchAllAMTimeout();
}
