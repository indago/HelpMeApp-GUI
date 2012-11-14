package com.indago.helpme.gui.dashboard.views;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.indago.helpme.R;
import com.indago.helpme.gui.dashboard.statemachine.HelpERStateMachine;
import com.indago.helpme.gui.dashboard.statemachine.IStateAwareView;
import com.indago.helpme.gui.dashboard.statemachine.IStateMachine;
import com.indago.helpme.gui.dashboard.statemachine.STATES;

/**
 * 
 * @author martinmajewski
 * 
 */
public class HelpERProgressView extends ImageView implements IStateAwareView {
	private static final String LOGTAG = HelpERProgressView.class.getSimpleName();

	public static enum ANIMATION {
		FLASH, FLASH_GREEN, FLASH_YELLOW, FLASH_RED, FLASH_BLUE,

		PROGRESS, PROGRESS_GREEN, PROGRESS_YELLOW, PROGRESS_RED, PROGRESS_BLUE,

		COUNTDOWN, COUNTDOWN_GREEN, COUNTDOWN_YELLOW, COUNTDOWN_RED, COUNTDOWN_BLUE;
	}

	public static enum GRADIENT_COLORS {
		GREEN, YELLOW, RED, BLUE, WHITE,
	}

	public static enum GRADIENT_COUNTDOWN_BLUE {
		B00, B01, B02, B03, B04, B05;
	}

	private STATES mState;
	private GradientDrawable[] drawable;
	private GradientDrawable[] drawable_countdown;
	private int countdownIdx;
	private AnimatorSet flash;
	private AnimatorSet asProgressAnim;
	private volatile AnimatorSet activeAnimation = null;

	public HelpERProgressView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public HelpERProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public HelpERProgressView(Context context) {
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

		drawable_countdown = new GradientDrawable[GRADIENT_COUNTDOWN_BLUE.values().length];
		drawable_countdown[GRADIENT_COUNTDOWN_BLUE.B00.ordinal()] = (GradientDrawable) context.getResources().getDrawable(R.drawable.gradient_indicator_countdown_blue01);
		drawable_countdown[GRADIENT_COUNTDOWN_BLUE.B01.ordinal()] = (GradientDrawable) context.getResources().getDrawable(R.drawable.gradient_indicator_countdown_blue02);
		drawable_countdown[GRADIENT_COUNTDOWN_BLUE.B02.ordinal()] = (GradientDrawable) context.getResources().getDrawable(R.drawable.gradient_indicator_countdown_blue03);
		drawable_countdown[GRADIENT_COUNTDOWN_BLUE.B03.ordinal()] = (GradientDrawable) context.getResources().getDrawable(R.drawable.gradient_indicator_countdown_blue04);
		drawable_countdown[GRADIENT_COUNTDOWN_BLUE.B04.ordinal()] = (GradientDrawable) context.getResources().getDrawable(R.drawable.gradient_indicator_countdown_blue05);
		drawable_countdown[GRADIENT_COUNTDOWN_BLUE.B05.ordinal()] = (GradientDrawable) context.getResources().getDrawable(R.drawable.gradient_indicator_countdown_blue06);

		flash = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.indicator_flash);
		flash.setTarget(this);

		asProgressAnim = HelpERProgressView.createProgressAnimatorSet();
		asProgressAnim.setTarget(this);

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
				activeAnimation = asProgressAnim;
				setPivotY(this.getMeasuredHeight());
				setImageDrawable(drawable[GRADIENT_COLORS.BLUE.ordinal()]);
				activeAnimation.start();
				break;
			case PROGRESS_YELLOW:
				activeAnimation = asProgressAnim;
				setPivotY(this.getMeasuredHeight());
				setImageDrawable(drawable[GRADIENT_COLORS.YELLOW.ordinal()]);
				activeAnimation.start();
				break;
			case COUNTDOWN:
				break;
			case COUNTDOWN_BLUE:
				setImageDrawable(drawable_countdown[5]);
				//drawable.setColors(GRADIENT_COLORS.BLUE.getColors());
				break;
			default:
				break;
		}
	}

	public void stopAnimAndReset() {
		if(activeAnimation != null) {
			activeAnimation.end();
			activeAnimation = null;
		}

		this.setAlpha(0.0f);
		this.setScaleY(1.0f);
	}

	@Override
	public void setState(IStateMachine sm) {
		if(sm == null || !(sm instanceof HelpERStateMachine)) {
			return;
		}

		switch((STATES) sm.getState()) {
			case DEFAULT:
				mState = STATES.DEFAULT;
				prepareCountdown();
				break;
			case COUNTDOWN:
				mState = STATES.COUNTDOWN;
				//startAnimation(ANIMATION.COUNTDOWN);
				break;
			default:
				break;
		}
	}

	@Override
	public STATES getState() {
		return mState;
	}

	public float countdownStep(int size) {
		if(size > 0) {
			float scaleY = this.getScaleY();

			setPivotY(this.getMeasuredHeight());

			if(scaleY > 0.0f) {
				scaleY -= 0.16667f * size;
				this.setScaleY(scaleY);
				countdownIdx -= size;
				if(countdownIdx >= 0) {
					setImageDrawable(drawable_countdown[countdownIdx]);
				}
			}
		}

		return this.getScaleY();
	}

	private void prepareCountdown() {
		activeAnimation = null;
		countdownIdx = drawable_countdown.length - 1;
		setImageDrawable(drawable_countdown[countdownIdx]);
		this.setAlpha(1.0f);
		this.setScaleY(1.0f);
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
