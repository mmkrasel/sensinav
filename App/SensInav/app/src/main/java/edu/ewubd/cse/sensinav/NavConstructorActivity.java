package edu.ewubd.cse.sensinav;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.ewubd.cse.sensinav.androidqa.wheel.ArrayWheelAdapter;
import edu.ewubd.cse.sensinav.androidqa.wheel.OnWheelChangedListener;
import edu.ewubd.cse.sensinav.androidqa.wheel.OnWheelScrollListener;
import edu.ewubd.cse.sensinav.androidqa.wheel.WheelView;
import edu.ewubd.cse.sensinav.mapview.NodeListAdapter;
import edu.ewubd.cse.sensinav.sensors.Sensors;
import edu.ewubd.cse.sensinav.util.CallBackListener;
import edu.ewubd.cse.sensinav.util.Util;
import edu.ewubd.cse.sensinav.mapview.MapInfo;

// Need to import the data and Create object of that data.........
public class NavConstructorActivity extends AppCompatActivity implements CallBackListener {
    private Button btnStart;
    private String previous_node_name="",current_nodeName="";
    private float previous_node_x=0,previous_node_y = 0, previous_node_z=1;
    private double pre_X;
    private double pre_Y;
    private double pre_Z;
    private TextView tvSourceNodeName;
    private EditText etNewNodeName;
    private TextView previous_nodeTextview;
    private TextView tvLiveDistance;

    private int fromFloor=-10, toFloor=100;
    private WheelView floorWheel;
    private String[] floorWheelItems;
    private boolean wheelScrolled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_constructor);

        this.settings();
        MapInfo.getInstance().init(this);
        MapInfo.getInstance().fetchMapInfo();

        btnStart = findViewById(R.id.startBtn);
        tvLiveDistance = findViewById(R.id.tvLiveDistance);
        this.initWheel();
        previous_nodeTextview  = findViewById(R.id.previous_nodeName);
        previous_nodeTextview.setText(previous_node_name);
        btnStart.setOnClickListener(v -> {
            // @ First need to check if Current Node is set or not.. If Current Node is set continue else Do nothing//
            // @ Get the source Node..
            // @ If (text==start) start the sensor otherwise proceed to save in the database and also toggle the text everytime it's pressed..
            String btnStartTxt = btnStart.getText().toString();
            if(previous_node_name.isEmpty()){
                Toast.makeText(NavConstructorActivity.this, "Need to set a Node first", Toast.LENGTH_SHORT).show();
            }
            else if(btnStartTxt.equalsIgnoreCase("Start Sensors")){
                // proceed to start sensor
                Sensors.getInstance().startSensors();
                //@ change button colour to red..
                btnStart.setText("Stop Sensors");
                btnStart.setTextColor(Color.RED);
            }
            else {
                //stopService;
                previous_node_z  = Util.getInstance().floorLabelToZ(floorWheelItems[floorWheel.getCurrentItem()]);
                Sensors.getInstance().stopSensors();
                float[] coordinate = Sensors.getInstance().getNewCoordinate(previous_node_x,previous_node_y,previous_node_z);
                //System.out.println(coordinate[0] + " "+ coordinate[1] + " " + coordinate[2]+": "+Sensors.getInstance().getDistance());
                AlertDialog.Builder alertDialog_nodeInfo = new AlertDialog.Builder(this);
                View view = getLayoutInflater().inflate(R.layout.dialog_new_node_info, null);
                tvSourceNodeName = view.findViewById(R.id.tvSourceNodeName);
                etNewNodeName = view.findViewById(R.id.etNewNodeName);
                String distance = ((int) (Sensors.getInstance().getDistance() * 10)) / 10.0 +"m";
                ((TextView)view.findViewById(R.id.tvDistance)).setText(distance);
                alertDialog_nodeInfo.setView(view);
                AlertDialog dialog_nodeInfo = alertDialog_nodeInfo.create();
                dialog_nodeInfo.show();
                tvSourceNodeName.setText(previous_node_name);
                view.findViewById(R.id.btnCreatePath).setOnClickListener(view1 -> {
                    current_nodeName =  etNewNodeName.getText().toString();
                    MapInfo.getInstance().createNewPath(coordinate, current_nodeName, previous_node_name);
                    // updating the previous Node value..
                    previous_node_x = coordinate[0];
                    previous_node_y = coordinate[1];
                    previous_node_z = coordinate[2];
                    previous_node_name = current_nodeName;
                    previous_nodeTextview.setText(previous_node_name);
                    //floorTextview.setText(Float.toString(previous_node_z));
                    //System.out.println(nodeInfo);
                    tvLiveDistance.setText("0m");
                    dialog_nodeInfo.dismiss();
                });
                btnStart.setText("Start Sensors");
                btnStart.setTextColor(Color.GREEN);
                //@ proceed to save in the database...
            }
        });
        findViewById(R.id.btnShowNodeList).setOnClickListener(v -> {
            // @ Set the start from button can choose from the already saved node ..
            // @ if there is no saved node create a node first..
            if(MapInfo.getInstance().nodeList.size() > 0){
                showNodesList();
            } else {
                showDialogToCreateFirstNode();
            }
        });
        findViewById(R.id.btnShowMap).setOnClickListener(v -> {
            Intent I = new Intent(NavConstructorActivity.this, MapViewActivity.class);
            startActivity(I);
        });
        findViewById(R.id.btnSyncMap).setOnClickListener(v -> {
            findViewById(R.id.progressCircle).setVisibility(View.VISIBLE);
            MapInfo.getInstance().syncData(this);
        });
        findViewById(R.id.btnSwitch).setOnClickListener(v-> {
            startActivity(new Intent(this, SearchLocationActivity.class));
        });
    }
    private void settings(){
        Intent i = this.getIntent();
        if(i.hasExtra("URL")){
            String url = i.getStringExtra("URL");
            Util.getInstance().setUrl(url);
        } else {
            Toast.makeText(this, "URL was not set", Toast.LENGTH_LONG).show();
        }

        if(i.hasExtra("BUILDING_NAME")){
            MapInfo.getInstance().buildingName = i.getStringExtra("BUILDING_NAME");
            ((TextView)findViewById(R.id.buildingName)).setText(MapInfo.getInstance().buildingName);
        }
        if(i.hasExtra("LOWEST_FLOOR")){
            fromFloor = i.getIntExtra("LOWEST_FLOOR", 0);
        }
        if(i.hasExtra("HIGHEST_FLOOR")){
            toFloor = i.getIntExtra("HIGHEST_FLOOR", 30);
        }
    }
    private void showDialogToCreateFirstNode(){
        AlertDialog.Builder alertDialog_nodeInfo = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_entrance_node_info, null);
        alertDialog_nodeInfo.setView(view);
        AlertDialog dialog_nodeInfo = alertDialog_nodeInfo.create();
        dialog_nodeInfo.show();
        view.findViewById(R.id.btnSaveEntranceNode).setOnClickListener(v1 -> {
            previous_node_name = ((EditText)view.findViewById(R.id.etFirstNodeName)).getText().toString();
            previous_node_x = 0;
            previous_node_y = 0;
            previous_node_z = 1;
            previous_nodeTextview.setText(previous_node_name);
            this.setFloorLabel(previous_node_z);
            dialog_nodeInfo.dismiss();
            MapInfo.getInstance().createNode(previous_node_name, previous_node_x, previous_node_y, previous_node_z);
        });
    }
    private void showNodesList(){
        if(MapInfo.getInstance().isLoading()){
            return;
        }
//        String floorLabel = whellItems[wheel.getCurrentItem()];
//        ArrayList<Node> nodes = MapInfo.getInstance().getNodes(floorLabel);
//        if(nodes.size()==0){
//            Toast.makeText(this, "No rooms were inserted for "+floorLabel, Toast.LENGTH_LONG).show();
//            return;
//        }
        // First show all existing nodes
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        View rowList = getLayoutInflater().inflate(R.layout.dialog_nodes_list, null);
        ListView mListView = rowList.findViewById(R.id.listViewNodes);
        NodeListAdapter adapter = new NodeListAdapter(this, R.layout.row_node, MapInfo.getInstance().nodeList);
        mListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        alertDialog.setView(rowList);
        AlertDialog dialog = alertDialog.create();
        dialog.show();
        // then select from the existing nodes
        mListView.setOnItemClickListener((adapterView, view, i1, l) -> {
            // preNodeID = MapInfo.getInstance().nodeList.get(i1).getID();
            pre_X=MapInfo.getInstance().nodeList.get(i1).getX();
            pre_Y=MapInfo.getInstance().nodeList.get(i1).getY();
            pre_Z=MapInfo.getInstance().nodeList.get(i1).getZ();
            previous_node_x = (float)pre_X;
            previous_node_y = (float)pre_Y;
            previous_node_z = (float)pre_Z;
            this.setFloorLabel(previous_node_z);
            previous_node_name = MapInfo.getInstance().nodeList.get(i1).getLabel();
            previous_nodeTextview.setText(previous_node_name);
            Toast.makeText(NavConstructorActivity.this, "X: " + previous_node_x+" Z: "+previous_node_z, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void initWheel() {
        floorWheel = findViewById(R.id.floorWheel);
        OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
            @SuppressWarnings("unused")
            public void onScrollStarts(WheelView wheel) {
                wheelScrolled = true;
            }
            @SuppressWarnings("unused")
            public void onScrollEnds(WheelView wheel) {
                wheelScrolled = false;
                //updateStatus();
            }
            @Override
            public void onScrollingStarted(WheelView wheel) { }
            @Override
            public void onScrollingFinished(WheelView wheel) { }
        };
        final OnWheelChangedListener changedListener = (wheel, oldValue, newValue) -> {
            if (!wheelScrolled) {
                //updateStatus();
                //wheel.getCurrentItem();
            }
        };
        floorWheelItems = new String[toFloor-fromFloor+1];
        int i=0;
        int selectedItem = 0;
        int plus = 0;
        for(int f = fromFloor; f <= toFloor; f++){
            if(f==0){
                selectedItem = i;
                plus = 1;
            }
            floorWheelItems[i++] = Util.getInstance().zToFloorLabel(f+plus);
        }
        ArrayWheelAdapter wa = new ArrayWheelAdapter(getApplicationContext(), floorWheelItems);
        wa.setTextSize(24);
        wa.setTextColor(getResources().getColor(R.color.node_info_text_color));
        floorWheel.setViewAdapter(wa);
        floorWheel.setVisibleItems(5);
        floorWheel.setCurrentItem(selectedItem);
        floorWheel.setCyclic(true);
        floorWheel.addChangingListener(changedListener);
        floorWheel.addScrollingListener(scrolledListener);
    }
    private void setFloorLabel(float z){
        for(int i = 0; i< floorWheelItems.length; i++){
            if(floorWheelItems[i].equals("Ground Floor") && z == 0){
                floorWheel.setCurrentItem(i);
                break;
            }
            if(floorWheelItems[i].startsWith("Basement") && z < 0){
                String selectedFloor = floorWheelItems[i].split(" ")[1];
                if(z == -Float.parseFloat(selectedFloor)){
                    floorWheel.setCurrentItem(i);
                    break;
                }
            }
            if(floorWheelItems[i].startsWith("Floor") && z > 0){
                String selectedFloor = floorWheelItems[i].split(" ")[1];
                if(z == Float.parseFloat(selectedFloor)){
                    floorWheel.setCurrentItem(i);
                    break;
                }
            }
        }
    }
    @Override
    public void requestCallBack(Object obj) {
        runOnUiThread(() -> {
            findViewById(R.id.progressCircle).setVisibility(View.GONE);
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        // Start the SensorService
        Sensors.getInstance().register(NavConstructorActivity.this, tvLiveDistance);
    }
    @Override
    protected void onPause() {
        super.onPause();
        Sensors.getInstance().unregister();
    }

}
//    INSERT INTO `inav_nodeinfo` SELECT * FROM inav_nodeinfo_backup;
//        INSERT INTO `inav_edgeinfo` SELECT * FROM inav_edgeinfo_backup;