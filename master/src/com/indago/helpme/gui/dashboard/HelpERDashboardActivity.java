package com.indago.helpme.gui.dashboard;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.android.helpme.demo.interfaces.UserInterface;
import com.android.helpme.demo.manager.HistoryManager;
import com.android.helpme.demo.manager.UserManager;
import com.indago.helpme.R;
import com.indago.helpme.gui.ATemplateActivity;
import com.indago.helpme.gui.dashboard.statemachine.HelpERStateMachine;
import com.indago.helpme.gui.dashboard.statemachine.STATES;
import com.indago.helpme.gui.dashboard.views.HelpERButtonView;
import com.indago.helpme.gui.dashboard.views.HelpERHintView;
import com.indago.helpme.gui.dashboard.views.HelpERProgressView;
import com.indago.helpme.gui.util.ImageUtility;

public class HelpERDashboardActivity extends ATemplateActivity {
	private static final String LOGTAG = HelpERDashboardActivity.class.getSimpleName();

	private ImageView mTopCover;
	private Animator mFadeIn;
	private Animator mFadeOut;
	private HelpERProgressView mProgressBars;
	private HelpERButtonView mButton;
	private HelpERHintView mHintViewer;
	private Vibrator mVibrator;
	private HelpERStateMachine mStateMachine;
	private TextView mCounterText;
	private SlidingDrawer mSlidingDrawer;

	private CountdownTimer mCDT;

	private UserInterface mUser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(LOGTAG, "... logged in!");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help_er_dashboard);

		Bundle extras = getIntent().getExtras();
		String userID = extras.getString("USER_ID");
		mUser = UserManager.getInstance().getUserById(userID);

		if(mUser == null) {
			throw new NullPointerException(LOGTAG + ": User with ID " + userID + " could not be retrieved from Extras-Bundle at onCreate()");
		}

		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

		mTopCover = (ImageView) findViewById(R.id.iv_topcover);
		mFadeIn = (Animator) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.fade_in);
		mFadeOut = (Animator) AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.fade_out);

		mProgressBars = (HelpERProgressView) findViewById(R.id.iv_helpme_help_er_indicator);
		mHintViewer = (HelpERHintView) findViewById(R.id.tv_helpme_help_er_infoarea);
		mButton = (HelpERButtonView) findViewById(R.id.btn_helpme_help_er_button);
		mCounterText = (TextView) findViewById(R.id.tv_incomming_call_counter);

		/*
		 * SlidingDrawer Menu Setup
		 */
		mSlidingDrawer = (SlidingDrawer) findViewById(R.id.slidingdrawer);

		ViewGroup vg = (ViewGroup) mSlidingDrawer.getContent();
		vg.setRotation(180);
		vg.setBackgroundColor(getResources().getColor(R.color.helpme_grey_dark));
		TextView name = (TextView) vg.findViewById(R.id.tv_help_ee_name);
		name.setText(name.getText() + " " + mUser.getName());
		TextView age = (TextView) vg.findViewById(R.id.tv_help_ee_age);
		age.setText(age.getText() + " " + mUser.getAge());
		TextView gender = (TextView) vg.findViewById(R.id.tv_help_ee_gender);
		gender.setText(gender.getText() + " " + mUser.getGender());

		Drawable[] drawables = new Drawable[4];
		drawables[0] = getResources().getDrawable(R.drawable.user_picture_background);
		drawables[1] = ImageUtility.retrieveDrawable(this, mUser.getPicture());
		drawables[2] = getResources().getDrawable(R.drawable.user_picture_overlay);
		drawables[3] = getResources().getDrawable(R.drawable.user_picture_border);

		ImageView picture = (ImageView) vg.findViewById(R.id.iv_help_ee_picture);
		picture.setImageDrawable(new LayerDrawable(drawables));

		Button handle = (Button) ((LinearLayout) mSlidingDrawer.getHandle()).getChildAt(0);
		handle.setText("DRAG DOWN FOR MORE INFORMATION!");
		handle.setTextSize(14f);

		/*
		 * StateMachine Setup
		 */

		mStateMachine = HelpERStateMachine.getInstance();
		mStateMachine.setState(STATES.DEFAULT);
		mStateMachine.addOne(mButton);
		mStateMachine.addOne(mHintViewer);
		mStateMachine.addOne(mProgressBars);
		mStateMachine.updateAll();

		mCDT = new CountdownTimer();

		mButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(v instanceof HelpERButtonView) {

					switch(event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							if(mStateMachine.getState() == STATES.DEFAULT) {

								int y = (int) event.getY();

								if(y < (v.getMeasuredHeight() * 0.5)) {
									mStateMachine.setState(STATES.ACCEPTED);
								} else {
									mStateMachine.setState(STATES.DECLINED);
								}

								mVibrator.vibrate(10);

							}
							break;
						case MotionEvent.ACTION_UP:

							if(mStateMachine.getState() == STATES.ACCEPTED) {
								Toast.makeText(getApplicationContext(), "Thank you for providing help!", Toast.LENGTH_LONG).show();

								mCDT.dismiss();
								HistoryManager.getInstance().startNewTask(mUser);
								Intent intent = new Intent(getApplicationContext(), com.indago.helpme.gui.dashboard.HelpERCallDetailsActivity.class);
								intent.putExtra("USER_ID", mUser.getId());
								startActivity(intent);

								finish();
							} else if(mStateMachine.getState() == STATES.DECLINED) {
								Toast.makeText(getApplicationContext(), "Too bad! Maybe next time!", Toast.LENGTH_LONG).show();
								declineCall();
							} else {
								Toast.makeText(getApplicationContext(), "UNDEFINED!", Toast.LENGTH_LONG).show();
							}
							break;
					}
				}
				return false;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		mCDT.execute();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mCDT.dismiss();
		finish();
	}

	public void onBackPressed() {

//		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//
//		// set title
//		alertDialogBuilder.setTitle("Warning!\nDeclining Incomming Call!");
//
//		// set dialog message
//		alertDialogBuilder.setMessage("Do you really want to decline this call?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int id) {
//				declineCall();
//			}
//		}).setNegativeButton("No", new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int id) {
//				dialog.cancel();
//			}
//		});
//
//		// create alert dialog
//		AlertDialog alertDialog = alertDialogBuilder.create();
//
//		// show it
//		alertDialog.show();

		//super.onBackPressed();
	}

	private void declineCall() {
		mCDT.dismiss();

		setResult(RESULT_CANCELED);

		finish();
	}

	private class CountdownTimer extends AsyncTask<Void, Void, Void> {

		private volatile int seconds = 60;
		private volatile boolean dismissed = false;
		private volatile boolean stopThread = false;

		public void resetTime() {
			seconds = 60;
		}

		public void dismiss() {
			dismissed = true;
		}

		@Override
		synchronized protected Void doInBackground(Void... params) {
			try {

				while(!stopThread && !dismissed) {
					Thread.sleep(980);
					publishProgress();
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(!dismissed) {
				Toast.makeText(getApplicationContext(), "Time's up!", Toast.LENGTH_LONG).show();

				declineCall();
			}

			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Void... values) {

			if(seconds >= 0) {
				mCounterText.setText("" + seconds);
				if((seconds % 10 == 9 || seconds == 0) && seconds < 50) {
					mProgressBars.countdownStep(1);
				}

				seconds--;
			} else {
				stopThread = true;
			}

			super.onProgressUpdate(values);
		}

	}

}
