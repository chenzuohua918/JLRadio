package com.semisky.jlradio.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;

public class CollectStar extends ImageView {
	private ObjectAnimator animatorIn, animatorOut;
	private long duration = 500;

	public CollectStar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		PropertyValuesHolder pvhXIn = PropertyValuesHolder.ofFloat("scaleX",
				0.0f, 1.0f, 1.0f);
		PropertyValuesHolder pvhYIn = PropertyValuesHolder.ofFloat("scaleY",
				0.0f, 1.0f, 1.0f);
		animatorIn = ObjectAnimator
				.ofPropertyValuesHolder(this, pvhXIn, pvhYIn);
		animatorIn.setDuration(duration);
		animatorIn.setInterpolator(new BounceInterpolator());
		animatorIn.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				show();
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}
		});

		PropertyValuesHolder pvhXOut = PropertyValuesHolder.ofFloat("scaleX",
				1.0f, 0.0f, 0.0f);
		PropertyValuesHolder pvhYOut = PropertyValuesHolder.ofFloat("scaleY",
				1.0f, 0.0f, 0.0f);
		animatorOut = ObjectAnimator.ofPropertyValuesHolder(this, pvhXOut,
				pvhYOut);
		animatorOut.setDuration(duration);
		animatorOut.setInterpolator(new BounceInterpolator());
		animatorOut.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				dismiss();
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}
		});
	}

	public CollectStar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CollectStar(Context context) {
		this(context, null);
	}

	/**
	 * 显示
	 */
	public void show() {
		if (getVisibility() != VISIBLE) {
			setVisibility(VISIBLE);
		}
	}

	/**
	 * 隐藏
	 */
	public void dismiss() {
		if (getVisibility() == VISIBLE) {
			setVisibility(GONE);
		}
	}

	/**
	 * 动画展示
	 */
	public void animateShow() {
		animatorIn.start();
	}

	/**
	 * 动画消失
	 */
	public void animateDismiss() {
		animatorOut.start();
	}

}
