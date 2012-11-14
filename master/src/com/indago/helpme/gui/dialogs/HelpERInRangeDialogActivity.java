package com.indago.helpme.gui.dialogs;

import android.app.Activity;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.helpme.demo.interfaces.UserInterface;
import com.android.helpme.demo.manager.UserManager;
import com.indago.helpme.R;
import com.indago.helpme.gui.util.ImageUtility;

public class HelpERInRangeDialogActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_help_er_in_range);

		Bundle extras = getIntent().getExtras();
		String taskString = extras.getString("TASK");

		UserInterface userInterface = UserManager.getInstance().getUserById(taskString);

		ImageView imageView;
		TextView text;
		String string;
		Button button;

		imageView = (ImageView) findViewById(R.id.dialog_helper_in_range_picture);
		imageView.setImageDrawable(new LayerDrawable(ImageUtility.retrieveDrawables(this, userInterface.getPicture())));

		text = (TextView) findViewById(R.id.dialog_helper_in_range_title);
		string = getResources().getString(R.string.helper_in_range_title);
		string = string.replace("[Name]", userInterface.getName());
		text.setText(Html.fromHtml(string));

		text = (TextView) findViewById(R.id.dialog_helper_in_range_text);
		string = getResources().getString(R.string.helper_in_range_text);

		if(userInterface.getGender().equalsIgnoreCase("female")) {
			string = string.replace("[gender]", "her");
		} else {
			string = string.replace("[gender]", "him");
		}
		text.setText(Html.fromHtml(string));

		button = (Button) findViewById(R.id.dialog_helper_in_range_button);
		button.setText("OK");
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});
	}

	@Override
	public void onBackPressed() {

	}

}
