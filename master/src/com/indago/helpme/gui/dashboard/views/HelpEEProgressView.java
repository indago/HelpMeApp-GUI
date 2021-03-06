package com.indago.helpme.gui.dashboard.views;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.indago.helpme.R;
import com.indago.helpme.gui.dashboard.statemachine.HelpEEStateMachine;
import com.indago.helpme.gui.dashboard.statemachine.IStateAwareView;
import com.indago.helpme.gui.dashboard.statemachine.IStateMachine;
import com.indago.helpme.gui.dashboard.statemachine.STATES;

/**
 * 
 * @author martinmajewski
 * 
 */
public class HelpEEProgressView extends ImageView implements IStateAwareView {
	private static final String LOGTAG = HelpEEProgressView.class.getSimpleName();

	public static enum ANIMATION {
		FLASH, FLASH_GREEN, FLASH_YELLOW, FLASH_RED, FLASH_BLUE,

		PROGRESS, PROGRESS_GREEN, PROGRESS_YELLOW, PROGRESS_RED, PROGRESS_BLUE,

		STANDBY, STANDBY_GREEN, STANDBY_YELLOW;
	}

	public static enum GRADIENT_COLORS {
		GREEN, YELLOW, RED, BLUE, WHITE;
	}

	private STATES mState;
	private GradientDrawable[] drawable;
	private ObjectAnimator pulse;
	private AnimatorSet flash;
	private AnimatorSet progress;
	private volatile Animator activeAnimation = null;

	public HelpEEProgressView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public HelpEEProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public HelpEEProgressView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		setPadding(0, 0, 0, 0);
		setAlpha(0.0f);
		setScaleY(1.0f);
		//		setPivotY(256);
		drawable = new GradientDrawable[GRADIENT_COLORS.values().length];

		drawable[GRADIENT_COLORS.BLUE.ordinal()] = (GradientDrawable) context.getResources().getDrawable(R.drawable.gradient_indicator_blue);
		drawable[GRADIENT_COLORS.GREEN.ordinal()] = (GradientDrawable) context.getResources().getDrawable(R.drawable.gradient_indicator_green);
		drawable[GRADIENT_COLORS.YELLOW.ordinal()] = (GradientDrawable) context.getResources().getDrawable(R.drawable.gradient_indicator_yellow);
		drawable[GRADIENT_COLORS.RED.ordinal()] = (GradientDrawable) context.getResources().getDrawable(R.drawable.gradient_indicator_red);
		drawable[GRADIENT_COLORS.WHITE.ordinal()] = (GradientDrawable) context.getResources().getDrawable(R.drawable.gradient_indicator_white);
		setImageDrawable(drawable[GRADIENT_COLORS.BLUE.ordinal()]);

		flash = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.indicator_flash);
		flash.setTarget(this);

		pulse = new ObjectAnimator();
		pulse.setProperty(ALPHA);
		pulse.setDuration(1500);
		pulse.setFloatValues(0.0f, 1.0f);
		pulse.setInterpolator(new AccelerateDecelerateInterpolator());
		pulse.setRepeatCount(ObjectAnimator.INFINITE);
		pulse.setRepeatMode(ObjectAnimator.REVERSE);
		pulse.setTarget(this);

		progress = HelpEEProgressView.createProgressAnimatorSet();
		progress.setTarget(this);
	}

	public void startAnimation(ANIMATION animation) {
		stopAnimAndReset();

		switch(animation) {
			case FLASH:
				activeAnimation = flash;
				activeAnimation.start();
				break;
			case FLASH_GREEN:
				activeAnimation = flash;
				setImageDrawable(drawable[GRADIENT_COLORS.GREEN.ordinal()]);
				activeAnimation.start();
				break;
			case FLASH_YELLOW:
				activeAnimation = flash;
				setImageDrawable(drawable[GRADIENT_COLORS.YELLOW.ordinal()]);
				activeAnimation.start();
				break;
			case FLASH_RED:
				activeAnimation = flash;
				setImageDrawable(drawable[GRADIENT_COLORS.RED.ordinal()]);
				activeAnimation.start();
				break;
			case FLASH_BLUE:
				activeAnimation = flash;
				setImageDrawable(drawable[GRADIENT_COLORS.BLUE.ordinal()]);
				activeAnimation.start();
				break;
			case PROGRESS:
			case PROGRESS_BLUE:
				activeAnimation = progress;
				setPivotY(this.getMeasuredHeight());
				setImageDrawable(drawable[GRADIENT_COLORS.BLUE.ordinal()]);
				activeAnimation.start();
				break;
			case PROGRESS_YELLOW:
				activeAnimation = progress;
				setPivotY(this.getMeasuredHeight());
				setImageDrawable(drawable[GRADIENT_COLORS.YELLOW.ordinal()]);
				activeAnimation.start();
				break;
			case PROGRESS_GREEN:
				activeAnimation = progress;
				setPivotY(this.getMeasuredHeight());
				setImageDrawable(drawable[GRADIENT_COLORS.GREEN.ordinal()]);
				activeAnimation.start();
				break;
			case STANDBY:
				activeAnimation = pulse;
				setImageDrawable(drawable[GRADIENT_COLORS.BLUE.ordinal()]);
				activeAnimation.start();
				break;
			case STANDBY_GREEN:
				activeAnimation = pulse;
				setImageDrawable(drawable[GRADIENT_COLORS.GREEN.ordinal()]);
				activeAnimation.start();
				break;
			case STANDBY_YELLOW:
				activeAnimation = pulse;
				setImageDrawable(drawable[GRADIENT_COLORS.YELLOW.ordinal()]);
				activeAnimation.start();
				break;
			default:
				break;
		}
	}

	public void stopAnimAndReset() {
		if(activeAnimation != null) {
			activeAnimation.end();
			activeAnimation = null;
			this.setAlpha(0.0f);
			this.setScaleY(1.0f);
		}
	}

	@Override
	public void setState(IStateMachine sm) {
		if(sm == null || !(sm instanceof HelpEEStateMachine)) {
			return;
		}

		switch((STATES) sm.getState()) {
			case SHIELDED:
				mState = STATES.SHIELDED;
				stopAnimAndReset();
				//startAnimation(ANIMATION.FLASH_GREEN);
				break;
			case PART_SHIELDED:
				mState = STATES.PART_SHIELDED;
				startAnimation(ANIMATION.FLASH_YELLOW);
				break;
			case UNSHIELDED:
				mState = STATES.UNSHIELDED;
				startAnimation(ANIMATION.FLASH_RED);
				break;
			case PRESSED:
				mState = STATES.PRESSED;
				startAnimation(ANIMATION.FLASH_RED);
				break;
			case LOCKED:
				mState = STATES.LOCKED;
				startAnimation(ANIMATION.PROGRESS_BLUE);
				break;
			case CALLCENTER:
				mState = STATES.CALLCENTER;
				startAnimation(ANIMATION.FLASH_YELLOW);
				break;
			case CALLCENTER_PRESSED:
				mState = STATES.CALLCENTER_PRESSED;
				startAnimation(ANIMATION.STANDBY_YELLOW);
				break;
			case HELP_INCOMMING:
				mState = STATES.HELP_INCOMMING;
				startAnimation(ANIMATION.PROGRESS_GREEN);
				break;
			case HELP_ARRIVED:
				mState = STATES.HELP_ARRIVED;
				startAnimation(ANIMATION.STANDBY_GREEN);
				break;
			case FINISHED:
				mState = STATES.FINISHED;
				break;
			default:
				break;
		}
	}

	@Override
	public STATES getState() {
		return mState;
	}

	private static AnimatorSet createProgressAnimatorSet() {

		ObjectAnimator fadeInFlash = new ObjectAnimator();
		fadeInFlash.setProperty(ALPHA);
		fadeInFlash.setDuration(100);
		fadeInFlash.setFloatValues(0.0f, 1.0f);
		fadeInFlash.setInterpolator(new AccelerateInterpolator());

		ObjectAnimator fadeOutFlash = new ObjectAnimator();
		fadeOutFlash.setProperty(ALPHA);
		fadeOutFlash.setDuration(1000);
		fadeOutFlash.setFloatValues(1.0f, 0.0f);
		fadeOutFlash.setInterpolator(new DecelerateInterpolator());

		ObjectAnimator scaleToZeroAnimator = new ObjectAnimator();
		scaleToZeroAnimator.setProperty(SCALE_Y);
		scaleToZeroAnimator.setFloatValues(0.0f);
		scaleToZeroAnimator.setInterpolator(new LinearInterpolator());
		scaleToZeroAnimator.setDuration(10);

		ObjectAnimator alphaToOneAnimator = new ObjectAnimator();
		alphaToOneAnimator.setProperty(ALPHA);
		alphaToOneAnimator.setFloatValues(1.0f);
		alphaToOneAnimator.setInterpolator(new LinearInterpolator());
		alphaToOneAnimator.setDuration(10);

		Keyframe kf00 = Keyframe.ofFloat(0.0000f, 0.0f);
		Keyframe kf01 = Keyframe.ofFloat(0.1399f, 0.0f);
		Keyframe kf02 = Keyframe.ofFloat(0.1400f, 0.1666f);
		Keyframe kf03 = Keyframe.ofFloat(0.2856f, 0.1666f);
		Keyframe kf04 = Keyframe.ofFloat(0.2857f, 0.3333f);
		Keyframe kf05 = Keyframe.ofFloat(0.4285f, 0.3333f);
		Keyframe kf06 = Keyframe.ofFloat(0.4286f, 0.5000f);
		Keyframe kf07 = Keyframe.ofFloat(0.5713f, 0.5000f);
		Keyframe kf08 = Keyframe.ofFloat(0.5714f, 0.6666f);
		Keyframe kf09 = Keyframe.ofFloat(0.7142f, 0.6666f);
		Keyframe kf10 = Keyframe.ofFloat(0.7143f, 0.8333f);
		Keyframe kf11 = Keyframe.ofFloat(0.8570f, 0.8333f);
		Keyframe kf12 = Keyframe.ofFloat(0.8571f, 1.0f);
		Keyframe kf13 = Keyframe.ofFloat(1.0000f, 1.0f);
		PropertyValuesHolder pvhProgress = PropertyValuesHolder.ofKeyframe(SCALE_Y, kf00, kf01, kf02, kf03, kf04, kf05, kf06, kf07, kf08, kf09, kf10, kf11, kf12, kf13);

		ObjectAnimator progressBarsAnimator = new ObjectAnimator();
		progressBarsAnimator.setValues(pvhProgress);
		progressBarsAnimator.setInterpolator(new LinearInterpolator());
		progressBarsAnimator.setDuration(4200);
		progressBarsAnimator.setRepeatMode(ObjectAnimator.RESTART);
		progressBarsAnimator.setRepeatCount(ObjectAnimator.INFINITE);

		AnimatorSet as = new AnimatorSet();
		as.playSequentially(fadeInFlash, fadeOutFlash, scaleToZeroAnimator, alphaToOneAnimator, progressBarsAnimator);

		return as;
	}

}
