package com.semisky.jlradio.model;

public interface ISwitchFMAMModel {
	void addISwitchFMAMCallback(ISwitchFMAMCallback callback);

	void removeISwitchFMAMCallback(ISwitchFMAMCallback callback);

	void switchRadioType(int radioType, boolean resetFragment);
}
