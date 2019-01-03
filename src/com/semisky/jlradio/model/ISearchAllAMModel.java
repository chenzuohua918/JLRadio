package com.semisky.jlradio.model;

public interface ISearchAllAMModel {
	void registerSearchAllAMListener(SearchAllAMListener listener);

	void unregisterSearchAllAMListener(SearchAllAMListener listener);

	void searchAllAM();

	void sendMsgToNotifyObserversSearchAllAMFinish();

	void sendMsgToNotifyObserversSearchAllAMUnFinish(int frequency);

	void sendMsgToNotifyObserversSearchAllAMInterrupt();

	void sendMsgToNotifyObserversSearchAllAMTimeOut();
}
