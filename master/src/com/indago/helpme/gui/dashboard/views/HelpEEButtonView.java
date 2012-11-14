package com.indago.helpme.gui.dashboard.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageButton;

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
public class HelpEEButtonView extends ImageButton implements IStateAwareView {
	private static final String LOGTAG = HelpEEButtonView.class.getSimpleName();

	private static final int STATESS_MAX = 8;
	private STATES mState;
	private Drawable[] drawables = new Drawable[STATESS_MAX];

	public HelpEEButtonView(Context context) {
		super(context);
		init(context);
	}

	public HelpEEButtonView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public HelpEEButtonView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		drawables[0] = context.getResources().getDrawable(R.drawable.btn_sos_shielded);
		drawables[1] = context.getResources().getDrawable(R.drawable.btn_sos_part_shielded);
		drawables[2] = context.getResources().getDrawable(R.drawable.btn_sos_unshielded);
		drawables[3] = context.getResources().getDrawable(R.drawable.btn_sos_pressed);
		drawables[4] = context.getResources().getDrawable(R.drawable.btn_sos_wait);
		drawables[5] = context.getResources().getDrawable(R.drawable.btn_call_default);
		drawables[6] = context.getResources().getDrawable(R.drawable.btn_call_pressed);
		drawables[7] = context.getResources().getDrawable(R.drawable.btn_help_incomming);

		this.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.btn_sos_shielded));

	}

	public int getStateAsInteger() {
		return mState.ordinal();
	}

	@Override
	public void setState(IStateMachine sm) {
		if(sm == null || !(sm instanceof HelpEEStateMachine)) {
			return;
		}

		switch((STATES) sm.getState()) {
			case SHIELDED:
				mState = STATES.SHIELDED;
				this.setBackgroundDrawable(drawables[0]);
				break;
			case PART_SHIELDED:
				mState = STATES.PART_SHIELDED;
				this.setBackgroundDrawable(drawables[1]);
				break;
			case UNSHIELDED:
				mState = STATES.UNSHIELDED;
				this.setBackgroundDrawable(drawables[2]);
				break;
			case PRESSED:
				mState = STATES.PRESSED;
				this.setBackgroundDrawable(drawables[3]);
				break;
			case LOCKED:
				mState = STATES.LOCKED;
				this.setBackgroundDrawable(drawables[4]);
				break;
			case CALLCENTER:
				mState = STATES.CALLCENTER;
				this.setBackgroundDrawable(drawables[5]);
				break;
			case CALLCENTER_PRESSED:
				mState = STATES.CALLCENTER_PRESSED;
				this.setBackgroundDrawable(drawables[6]);
				break;
			case HELP_INCOMMING:
				mState = STATES.HELP_INCOMMING;
				this.setBackgroundDrawable(drawables[7]);
				break;
			case HELP_ARRIVED:
				mState = STATES.HELP_ARRIVED;
				break;
			case FINISHED:
				mState = STATES.FINISHED;
				break;
			default:
				break;
		}
	}

	@Override
	public Enum<?> getState() {
		return mState;
	}

}
