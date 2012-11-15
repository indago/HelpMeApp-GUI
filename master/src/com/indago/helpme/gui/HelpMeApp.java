package com.indago.helpme.gui;

import java.io.File;
import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
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

import com.android.helpme.demo.interfaces.DrawManagerInterface;
import com.android.helpme.demo.interfaces.UserInterface;
import com.android.helpme.demo.manager.HistoryManager;
import com.android.helpme.demo.manager.MessageOrchestrator;
import com.android.helpme.demo.manager.PositionManager;
import com.android.helpme.demo.manager.RabbitMQManager;
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
public class HelpMeApp extends ATemplateActivity implements OnItemClickListener, DrawManagerInterface {
	private static final String LOGTAG = HelpMeApp.class.getSimpleName();

	private TabHost mTabHost;
	private Handler mHandler;
	private MessageOrchestrator mOrchestrator;

	private ListView lvHelpER;
	private ListView lvHelpEE;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_helpmeapp);

		mHandler = new Handler();

		Intent intent = new Intent(this, RabbitMQService.class);
		startService(intent);
		mHandler.post((RabbitMQManager.getInstance().bindToService(this)));
		showDialog();

	}

	private void initBackend() {
		ThreadPool.getThreadPool(10);

		mOrchestrator = MessageOrchestrator.getInstance();
		mOrchestrator.addDrawManager(DRAWMANAGER_TYPE.SWITCHER, this);
		mOrchestrator.addDrawManager(DRAWMANAGER_TYPE.LOGIN, this);

		mOrchestrator.listenToMessageSystem(RabbitMQManager.getInstance());
		mOrchestrator.listenToMessageSystem(PositionManager.getInstance(this));
		mOrchestrator.listenToMessageSystem(UserManager.getInstance());
		mOrchestrator.listenToMessageSystem(HistoryManager.getInstance());

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
			ThreadPool.runTask(UserManager.getInstance().setThisUser(user, getApplicationContext()));

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

		initBackend();

		ThreadPool.runTask(UserManager.getInstance().deleteUserChoice(getApplicationContext()));

		ThreadPool.runTask(UserManager.getInstance().readUserChoice(getApplicationContext()));
	}

	@Override
	protected void onPause() {
		MessageOrchestrator.getInstance().removeDrawManager(DRAWMANAGER_TYPE.SWITCHER);
		MessageOrchestrator.getInstance().removeDrawManager(DRAWMANAGER_TYPE.LOGIN);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mHandler.post((RabbitMQManager.getInstance().unbindFromService(getApplicationContext())));
		android.os.Process.killProcess(android.os.Process.myPid());
		Editor editor = getSharedPreferences("clear_cache", Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		trimCache(this);
		super.onDestroy();
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

	@Override
	public void drawThis(Object object) {
		if(object instanceof User) {
			User user = (User) object;
			if(user.isHelper()) {
				mHandler.post(startHelpERActivity());
			} else {
				mHandler.post(startHelpEEActivity());
			}
		} else if(object instanceof ArrayList<?>) {
			ArrayList<User> tmpList = (ArrayList<User>) object;
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
		} else if(object == null) {
			ThreadPool.runTask(UserManager.getInstance().readUserFromProperty(getApplicationContext()));
		}

	}

	private void cleanUp() {
		MessageOrchestrator.getInstance().removeDrawManager(DRAWMANAGER_TYPE.SWITCHER);
		MessageOrchestrator.getInstance().removeDrawManager(DRAWMANAGER_TYPE.LOGIN);
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

		final Dialog dialog = new Dialog(this);
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
		button.setText("OK");
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}
}
