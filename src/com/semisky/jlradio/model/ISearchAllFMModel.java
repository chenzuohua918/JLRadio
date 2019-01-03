package com.semisky.jlradio.model;

public interface ISearchAllFMModel {
	void registerSearchAllFMListener(SearchAllFMListener listener);

	void unregisterSearchAllFMListener(SearchAllFMListener listener);

	void searchAllFM();

	void sendMsgToNotifyObserversSearchAllFMFinish();

	void sendMsgToNotifyObserversSearchAllFMUnFinish(int frequency);

	void sendMsgToNotifyObserversSearchAllFMInterrupt();
	
	void sendMsgToNotifyObserversSearchAllFMTimeOut();
}
