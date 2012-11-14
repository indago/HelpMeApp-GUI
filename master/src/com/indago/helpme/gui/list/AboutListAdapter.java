package com.indago.helpme.gui.list;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.indago.helpme.R;

/**
 * 
 * @author martinmajewski
 * 
 */
public class AboutListAdapter extends ArrayAdapter<Drawable> {

	private Context mContext;

	public AboutListAdapter(Context context, int resourceId, List<Drawable> objects) {
		super(context, resourceId, objects);
		mContext = context;
	}

	private class ViewHolder {
		ImageView picture;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		Drawable item = getItem(position);

		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		if(convertView == null) {
			convertView = inflater.inflate(R.layout.list_item_picture, null);
			holder = new ViewHolder();
			holder.picture = (ImageView) convertView.findViewById(R.id.iv_list_picture);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.picture.setImageDrawable(item);

		return convertView;
	}
}
