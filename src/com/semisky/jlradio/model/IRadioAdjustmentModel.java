package com.semisky.jlradio.model;

public interface IRadioAdjustmentModel {
	void addIRadioAdjustmentCallback(IRadioAdjustmentCallback callback);

	void removeIRadioAdjustmentCallback(IRadioAdjustmentCallback callback);

	void adjustmentStepPrevious();

	void adjustmentStepNext();
}
