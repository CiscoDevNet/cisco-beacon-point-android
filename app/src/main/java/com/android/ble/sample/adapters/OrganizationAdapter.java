package com.android.ble.sample.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.ble.sample.R;
import com.android.ble.sample.model.PlaceNameModel;

import java.util.ArrayList;

/**
 * Created by Entappiainc on 30-08-2016.
 */
public class OrganizationAdapter extends BaseAdapter{
    Context mContext;
    LayoutInflater inflater = null;
    ArrayList<PlaceNameModel> placeNameModelArrayList;

    public OrganizationAdapter(Context mContext, ArrayList<PlaceNameModel> placeNameModelArrayList) {
        this.mContext = mContext;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.placeNameModelArrayList = placeNameModelArrayList;
    }

    @Override
    public int getCount() {
        return placeNameModelArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return placeNameModelArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        View view = convertView;
        if(view==null)
        {
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.places_list_item, null);
            viewHolder.venueNameTextView = (TextView) view.findViewById(R.id.venueNameTextView);
            view.setTag(viewHolder);
        }else
            viewHolder = (ViewHolder) view.getTag();

        PlaceNameModel placeNameModel = (PlaceNameModel) getItem(position);


        viewHolder.venueNameTextView.setText(placeNameModel.getsPlaceName());




        return view;
    }

    public class ViewHolder {
        TextView venueNameTextView;
    }
}
