package com.android.ble.sample.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.ble.sample.R;
import com.android.ble.sample.helper.Utility;
import com.android.ble.sample.listeners.AdapterListener;
import com.android.ble.sample.model.NotificationModel;
import com.mist.android.MSTPoint;
import com.mist.android.MSTVirtualBeacon;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Entappia on 07-04-2017.
 */

public class NotifiListAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater inflater = null;
    ArrayList<NotificationModel> notificationModelArrayList;
    AdapterListener adapterListener;
    int filterColor;
    private MSTPoint mstPoint;
    public NotifiListAdapter(Context mContext, ArrayList<NotificationModel> notificationModelArrayList, MSTPoint mstPoint) {
        this.mContext = mContext;
        this.notificationModelArrayList = notificationModelArrayList;
        adapterListener = (AdapterListener) mContext;
        this.mstPoint = mstPoint;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        filterColor = (mContext.getResources().getColor(R.color.colorBreadCrumb));
    }

    @Override
    public int getCount() {
        return  notificationModelArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return  notificationModelArrayList != null && notificationModelArrayList.size()>position ? notificationModelArrayList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        NotifiViewHolder notifiViewHolder;
        if (convertView == null) {
            view = inflater.inflate(R.layout.notifi_list_item, null);
            notifiViewHolder = new NotifiViewHolder();
            notifiViewHolder.notificationLinearLayout = (LinearLayout) view.findViewById(R.id.notificationLinearLayout);
            notifiViewHolder.textViewMessage = (TextView) view.findViewById(R.id.textViewMessage);
            notifiViewHolder.textViewDistance = (TextView) view.findViewById(R.id.textViewDistance);
            notifiViewHolder.imageViewInfo = (ImageView) view.findViewById(R.id.imageViewInfo);

            view.setTag(notifiViewHolder);
        } else
            notifiViewHolder = (NotifiViewHolder) view.getTag();

        notifiViewHolder.imageViewInfo.setColorFilter(filterColor, PorterDuff.Mode.SRC_ATOP);

        final NotificationModel notificationModel = (NotificationModel) getItem(position);
        if(notificationModel!=null) {
            notifiViewHolder.textViewMessage.setText(notificationModel.getBodyMessage());
            notifiViewHolder.textViewDistance.setText(getDistanceMstPointToVirtualBeacon(notificationModel.getMstVirtualBeacon()) + "m");
            notifiViewHolder.notificationLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!Utility.isEmptyString(notificationModel.getForwardUrl())) {

                        Uri intentUri = Uri.parse(notificationModel.getForwardUrl());
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(intentUri);
                        mContext.startActivity(intent);
                        // adapterListener.performAdapterAction("notification_event", notificationModel.getForwardUrl());
                       // adapterListener.performAdapterAction("showBrowserAlertDialog", notificationModel.getForwardUrl());
                    } else
                        adapterListener.performAdapterAction("showBrowserErrorDialog", true);

                }
            });
        }

        return view;
    }


    private String getDistanceMstPointToVirtualBeacon(MSTVirtualBeacon virtualBeacon) {

        if(virtualBeacon== null || mstPoint== null)
            return "0";

        int xValue =  virtualBeacon.getX() ;
        int yValue =  virtualBeacon.getY() ;

        double distance = distanceBetweenTwoPoints(xValue, yValue,
                mstPoint.getX(),  mstPoint.getY());

        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(distance);
    }

    /**
     * Get distance between two points.
     */
    public  double distanceBetweenTwoPoints(double x1, double y1, double x2, double y2 )
    {
        return  Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }

    public static class NotifiViewHolder {
        LinearLayout notificationLinearLayout;
        TextView textViewMessage;
        TextView textViewDistance;
        ImageView imageViewInfo;
    }
}
