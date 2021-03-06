package com.indago.helpme.gui;

import java.io.File;
import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.android.helpme.demo.interfaces.UserInterface;
import com.android.helpme.demo.manager.NetworkManager;
import com.android.helpme.demo.manager.PositionManager;
import com.android.helpme.demo.manager.TaskManager;
import com.android.helpme.demo.manager.UserManager;
import com.android.helpme.demo.rabbitMQ.RabbitMQService;
import com.android.helpme.demo.utils.ThreadPool;
import com.android.helpme.demo.utils.User;
import com.indago.helpme.R;
import com.indago.helpme.gui.list.LogInListAdapter;

/**
 * 
 * @author martinmajewski
 * @author andreaswieland
 * 
 */
public class HelpMeApp extends ATemplateActivity implements OnItemClickListener {
	private static final String LOGTAG = HelpMeApp.class.getSimpleName();

	private TabHost mTabHost;
	private Handler mHandler;

	private ListView lvHelpER;
	private ListView lvHelpEE;
	private Dialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_helpmeapp);

		mHandler = new Handler();
		dialog = new Dialog(this);
		showDialog();

	}

	private void initBackend() {
//		ThreadPool.getThreadPool(10);

		if (NetworkManager.getInstance().init(this)) {
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					Button button = (Button) dialog.findViewById(R.id.dialog_button_ok);
					button.setText("Ok");
					button.setEnabled(true);
				}
			});
			
		}
		PositionManager.getInstance(getApplicationContext(),mHandler);
		UserManager.getInstance().init();
		TaskManager.getInstance().init();

		
		getUsers();
	}
	
	private void getUsers(){
		ArrayList<User> tmpList = (ArrayList<User>) UserManager.getInstance().readUsersFromProperty(getApplicationContext());
		if (tmpList.size() == 0) {
			return;
		}
		final ArrayList<UserInterface> helpERList = new ArrayList<UserInterface>();
		final ArrayList<UserInterface> helpEEList = new ArrayList<UserInterface>();
		for(UserInterface user : tmpList) {
			if(user.isHelper()) {
				helpERList.add(user);
			} else {
				helpEEList.add(user);
			}
		}

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				initTabs(helpERList, helpEEList);
			}
		});
	}

	private void initTabs(ArrayList<UserInterface> helpERList, ArrayList<UserInterface> helpEEList) {
		if(mTabHost == null) {
			mTabHost = (TabHost) findViewById(R.id.tabhost);
			mTabHost.setup();

			TabSpec specHelpER = mTabHost.newTabSpec((String) getResources().getText(R.string.tab_helper));
			specHelpER.setContent(R.id.ll_tab1);
			specHelpER.setIndicator((String) getResources().getText(R.string.tab_helper));

			TabSpec specHelpEE = mTabHost.newTabSpec((String) getResources().getText(R.string.tab_helpee));
			specHelpEE.setContent(R.id.ll_tab2);
			specHelpEE.setIndicator((String) getResources().getText(R.string.tab_helpee));

			mTabHost.addTab(specHelpER);
			mTabHost.addTab(specHelpEE);
		}

		LogInListAdapter adapterHelpER = new LogInListAdapter(getApplicationContext(), R.layout.list_item_picture_text, helpERList);
		LogInListAdapter adapterHelpEE = new LogInListAdapter(getApplicationContext(), R.layout.list_item_picture_text, helpEEList);

		lvHelpER = (ListView) findViewById(R.id.lv_tab1);
		lvHelpEE = (ListView) findViewById(R.id.lv_tab2);

		lvHelpER.setAdapter(adapterHelpER);
		lvHelpEE.setAdapter(adapterHelpEE);

		lvHelpER.setOnItemClickListener(this);
		lvHelpEE.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		UserInterface user = (UserInterface) parent.getItemAtPosition(position);

		if(user != null) {
			UserManager.getInstance().setThisUser(user, getApplicationContext());

			if(user.isHelper()) {
				mHandler.post(startHelpERActivity());
			} else {
				mHandler.post(startHelpEEActivity());
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		new Thread(new Runnable() {
			
			@Override
			public void run() {
				initBackend();
			}
		}).start();
		

		UserManager.getInstance().deleteUserChoice(getApplicationContext());

		UserManager.getInstance().readUserChoice(getApplicationContext());
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		NetworkManager.getInstance().unbindFromService(getApplicationContext());
		
		android.os.Process.killProcess(android.os.Process.myPid());
		Editor editor = getSharedPreferences("clear_cache", Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		trimCache(this);
		clearApplicationData();
		super.onDestroy();
	}

	public void clearApplicationData() {
		File cache = getCacheDir();
		File appDir = new File(cache.getParent());
		if(appDir.exists()) {
			String[] children = appDir.list();
			for(String s : children) {
				if(!s.equals("lib")) {
					deleteDir(new File(appDir, s));
					Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
				}
			}
		}
	}

	public static void trimCache(Context context) {
		try {
			File dir = context.getCacheDir();
			if(dir != null && dir.isDirectory()) {
				deleteDir(dir);

			}
		} catch(Exception e) {
			// TODO: handle exception
		}
	}

	public static boolean deleteDir(File dir) {
		if(dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for(int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if(!success) {
					return false;
				}
			}
		}

		// <uses-permission
		// android:name="android.permission.CLEAR_APP_CACHE"></uses-permission>
		// The directory is now empty so delete it

		return dir.delete();
	}

//	@Override
//	public void drawThis(Object object) {
//		if(object instanceof User) {
//			User user = (User) object;
//			if(user.isHelper()) {
//				mHandler.post(startHelpERActivity());
//			} else {
//				mHandler.post(startHelpEEActivity());
//			}
//		} else if(object instanceof ArrayList<?>) {
//			ArrayList<User> tmpList = (ArrayList<User>) object;
//			final ArrayList<UserInterface> helpERList = new ArrayList<UserInterface>();
//			final ArrayList<UserInterface> helpEEList = new ArrayList<UserInterface>();
//			for(UserInterface user : tmpList) {
//				if(user.isHelper()) {
//					helpERList.add(user);
//				} else {
//					helpEEList.add(user);
//				}
//			}
//
//			mHandler.post(new Runnable() {
//				@Override
//				public void run() {
//					initTabs(helpERList, helpEEList);
//				}
//			});
//		} else if(object == null) {
//			UserManager.getInstance().readUsersFromProperty(getApplicationContext());
//		}
//
//	}

	private void cleanUp() {
	}

	private Runnable startHelpERActivity() {
		return new Runnable() {

			@Override
			public void run() {
				startActivity(new Intent(getBaseContext(), com.indago.helpme.gui.dashboard.HelpERControlcenterActivity.class));
//				startActivity(new Intent(getBaseContext(), com.indago.helpme.gui2.HistoryActivity.class));
			}
		};

	}

	private Runnable startHelpEEActivity() {
		return new Runnable() {

			@Override
			public void run() {
				startActivity(new Intent(getBaseContext(), com.indago.helpme.gui.dashboard.HelpEEDashboardActivity.class));
			}
		};

	}

	private void showDialog() {

		
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_select_your_role);
		dialog.setCancelable(false);

		TextView text;
		String string;
		Button button;

		text = (TextView) dialog.findViewById(R.id.dialog_title);
		string = getResources().getString(R.string.dialog_select_your_role_title);
		text.setText(Html.fromHtml(string));

		text = (TextView) dialog.findViewById(R.id.dialog_text);
		string = getResources().getString(R.string.dialog_select_your_role_text);
		text.setText(Html.fromHtml(string));

		button = (Button) dialog.findViewById(R.id.dialog_button_ok);
		button.setText("Not Connected");
		button.setEnabled(false);
		button.setOnClickListener(new OnClickListener() {
			

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}
}
