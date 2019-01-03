package com.semisky.jlradio.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.semisky.jlradio.R;

public class CollectToast extends ImageView implements AnimationListener {
	private Animation animation;

	public CollectToast(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setVisibility(GONE);
	}

	public CollectToast(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CollectToast(Context context) {
		this(context, null);
	}

	public void toast() {
		setVisibility(VISIBLE);
		if (animation == null) {
			animation = AnimationUtils.loadAnimation(getContext(),
					R.anim.collect_toast_anim);
			animation.setAnimationListener(this);
		}
		this.startAnimation(animation);
	}

	@Override
	public void onAnimationStart(Animation animation) {

	}

	@Override
	public void onAnimationEnd(Animation animation) {
		setVisibility(GONE);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}

}
