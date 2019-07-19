package com.example.tk_employee.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.distancecalculator.GPSCallback;
import com.example.distancecalculator.GPSErrorCode;
import com.example.distancecalculator.GpsUtils;
import com.example.distancecalculator.RouteDO;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class RoutesActivity extends AppCompatActivity {

    private TextView tvStatus;
    private ListView lv;
    GpsUtils gpsUtils;
    private static String API_KEY = "AIzaSyAZnalksXcmp7aNCRrTtDxGRHaWvT5Ny3A";
    private HashMap<String, RouteDO> hmRoutes =new HashMap<String ,RouteDO> ();
    Vector<RouteDO> vec = new Vector<RouteDO>();
    RoutesAdapter adapter=null;
    ArrayAdapter<String> tempAdapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
        tvStatus  = (TextView) findViewById(R.id.tvStatus);
        lv = (ListView)findViewById(R.id.lv);
        gpsUtils = GpsUtils.getInstance(RoutesActivity.this,API_KEY);
        gpsUtils.setListner(new GPSCallback() {
            @Override
            public void gotGpsValidationResponse(Object response, GPSErrorCode code) {
                Log.d("sdf","XXXXXXx");
            }
        });
        loadData();
    }

    private void loadData()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                hmRoutes= gpsUtils.getAllRoutes();
                if(hmRoutes!=null && hmRoutes.keySet()!=null && hmRoutes.keySet().size()>0)
                {
                    Set set = hmRoutes.keySet() ;
                    Iterator iterator = set.iterator();
                    while(iterator.hasNext())
                    {
                        vec.add((RouteDO) hmRoutes.get( iterator.next()));
                    }

                }




                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(vec.size()>0) {
                            tvStatus.setVisibility(View.GONE);
//                            adapter = new RoutesAdapter(vec);
                            tempAdapter = new ArrayAdapter (RoutesActivity.this,R.layout.list_item_1,vec);
                            lv.setAdapter(tempAdapter);
                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent intent = new Intent(RoutesActivity.this,MapsActivity.class);
                                    intent.putExtra("TIME",vec.get(position).timeEllapse);
                                    intent.putExtra("CODE", vec.get(position).pathcode);
                                    intent.putExtra("DISTANCE", vec.get(position).travelledDistance);
                                    startActivity(intent);

                                }
                            });


                        }
                        else
                        {
                            tvStatus.setText("Routes are not found");
                        }
                    }
                });
            }
        }).start();
    }
    class RoutesAdapter extends BaseAdapter
    {
        Vector<RouteDO> vec = new Vector<RouteDO>();
        public RoutesAdapter(Vector<RouteDO> vec)
        {
            this.vec =vec;
        }

        @Override
        public int getCount() {
            return vec.size();
        }

        @Override
        public Object getItem(int position) {
            return vec.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            RouteDO routeDO = vec.get(position);
            ViewHolder holder = null;
            if(convertView==null)
            {
                convertView = (LinearLayout) LayoutInflater.from(RoutesActivity.this).inflate(R.layout.route_path_item,null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);

            }
            else
            {
                holder =(ViewHolder) convertView.getTag();
            }
            holder.tvDistance.setText(routeDO.travelledDistance+"");
            holder.tvPathCode.setText(routeDO.pathcode+"");
            holder.tvTime.setText(routeDO.timeEllapse+"");


            return convertView;
        }
        class ViewHolder
        {
            TextView tvPathCode,tvTime,tvDistance;
            public ViewHolder(View view)
            {
                tvDistance = view.findViewById(R.id.tvDistance);
                tvTime = view.findViewById(R.id.tvTime);
                tvPathCode = view.findViewById(R.id.tvPathCode);

            }
        }
    }
}
