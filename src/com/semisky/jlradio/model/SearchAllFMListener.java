package com.semisky.jlradio.model;

public interface SearchAllFMListener {
	void notifyObserversClearFMList();
	
	void notifyObserversSearchAllFMFinish();

	void notifyObserversSearchAllFMUnFinish(int frequency);

	void notifyObserversSearchAllFMInterrupt();

	void notifyObserversSearchAllFMTimeout();
}
