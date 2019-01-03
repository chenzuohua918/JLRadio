package com.semisky.jlradio.model;

public interface ISwitchFMAMCallback {
	void onSwitchFMAMPrepare(boolean resetFragment);

	void beginSwitchFMToFM();

	void beginSwitchFMToAM();

	void beginSwitchAMToFM();

	void beginSwitchAMToAM();

	void beginSwitchFMToFMWhenSearchNearStrongRadio();

	void beginSwitchFMToAMWhenSearchNearStrongRadio();

	void beginSwitchAMToFMWhenSearchNearStrongRadio();

	void beginSwitchAMToAMWhenSearchNearStrongRadio();
}
