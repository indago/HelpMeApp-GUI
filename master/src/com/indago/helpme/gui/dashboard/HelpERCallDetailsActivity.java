package com.indago.helpme.gui.dashboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.helpme.demo.interfaces.DrawManagerInterface;
import com.android.helpme.demo.manager.MessageOrchestrator;
import com.android.helpme.demo.utils.Task;
import com.android.helpme.demo.utils.User;
import com.google.android.maps.MapActivity;
import com.indago.helpme.R;

public class HelpERCallDetailsActivity extends MapActivity implements DrawManagerInterface {
	private static final String LOGTAG = HelpERCallDetailsActivity.class.getSimpleName();
	protected static DisplayMetrics metrics = new DisplayMetrics();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
								WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help_er_call_details);

		MessageOrchestrator.getInstance().addDrawManager(DRAWMANAGER_TYPE.MAP, this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		setDefaulAppearance();
	}

	protected void setDefaulAppearance() {
		getWindow().getDecorView().getRootView()
				.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
	}

	@Override
	public void onBackPressed() {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle("Warning!\nCanceling Support Offer!");

		// set dialog message
		alertDialogBuilder.setMessage("Do you really want to cancel your current support offer?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				startActivity(new Intent(getApplicationContext(), com.indago.helpme.gui.dashboard.HelpERControlcenterActivity.class));

				finish();
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// if this button is clicked, just close
				// the dialog box and do nothing
				dialog.cancel();
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
		//startActivity(new Intent(getApplicationContext(), com.indago.helpme.gui.HelpERControlcenterActivity.class), null);

		//super.onBackPressed();

		//finish();
	}

	@Override
	public void drawThis(Object object) {
		if(object instanceof User) {
			User user = (User) object;
			//handler.post(addMarker(user));
		} else if(object instanceof Task) {
			Task task = (Task) object;
			//handler.post(showInRangeMessageBox(this));
		}

	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
