package com.indago.helpme.gui.dashboard;

import android.os.Bundle;

import com.android.helpme.demo.interfaces.DrawManagerInterface;
import com.android.helpme.demo.manager.MessageOrchestrator;
import com.android.helpme.demo.utils.Task;
import com.android.helpme.demo.utils.User;
import com.indago.helpme.R;
import com.indago.helpme.gui.ATemplateActivity;

public class HelpERCallDetailsActivity extends ATemplateActivity implements DrawManagerInterface {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help_er_call_details);

		MessageOrchestrator.getInstance().addDrawManager(DRAWMANAGER_TYPE.MAP, this);
	}

	@Override
	public void onBackPressed() {

//		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//
//		// set title
//		alertDialogBuilder.setTitle("Warning!\nCanceling Support Offer!");
//
//		// set dialog message
//		alertDialogBuilder.setMessage("Do you really want to cancel your current support offer?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int id) {
//				startActivity(new Intent(getApplicationContext(), com.indago.helpme.gui.dashboard.HelpERControlcenterActivity.class));
//				finish();
//			}
//		}).setNegativeButton("No", new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int id) {
//				// if this button is clicked, just close
//				// the dialog box and do nothing
//				dialog.cancel();
//			}
//		});
//
//		// create alert dialog
//		AlertDialog alertDialog = alertDialogBuilder.create();
//
//		// show it
//		alertDialog.show();
//		//startActivity(new Intent(getApplicationContext(), com.indago.helpme.gui.HelpERControlcenterActivity.class), null);
//
//		//super.onBackPressed();
//
//		//finish();
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

}
