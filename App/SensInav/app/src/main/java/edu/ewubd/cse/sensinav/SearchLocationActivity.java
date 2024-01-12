package edu.ewubd.cse.sensinav;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import java.util.ArrayList;
import edu.ewubd.cse.sensinav.mapview.MapInfo;
import edu.ewubd.cse.sensinav.mapview.Node;
import edu.ewubd.cse.sensinav.util.Util;

public class SearchLocationActivity extends AppCompatActivity {
    private AutoCompleteTextView fromNodes,toNodes;
    private double selectedFloor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);
        fromNodes = findViewById(R.id.tvCurrentRoom);
        toNodes = findViewById(R.id.tvDestinationRoom);
        this.setNodesList();
        findViewById(R.id.btnShortestPath).setOnClickListener(v -> {
            int sourceID = this.getNodeID(fromNodes.getText().toString());
            int  destinationID = this.getNodeID(toNodes.getText().toString());
            if(sourceID>0 && destinationID>0) {
                Intent intent = new Intent(this, NavigateShortestPathActivity.class);
                intent.putExtra("_FROM_DOOR_", sourceID);
                intent.putExtra("_TO_DOOR_", destinationID);
                intent.putExtra("_FLOOR_", selectedFloor);
                startActivity(intent);
            } else{
                Toast.makeText(SearchLocationActivity.this, "Unknown door", Toast.LENGTH_LONG).show();
            }
        });
        findViewById(R.id.btnSwitch).setOnClickListener(v-> finish());
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setNodesList() {
        ArrayList<String> list = new ArrayList<>();
        for (Node node : MapInfo.getInstance().nodeList) {
            //list.add(node.getBuilding()+">"+ Util.getInstance().zToFloorLabel(node.getZ())+">"+node.getLabel());
            list.add(node.getLabel()+" @"+Util.getInstance().zToFloorLabel(node.getZ()));
        }
        //list.sort(Comparator.naturalOrder());
        String[] data = new String[list.size()];
        data = list.toArray(data);
        System.out.println("@SearchLocationActivity*********************");
        System.out.println(list);
        System.out.println(data.length);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(SearchLocationActivity.this, android.R.layout.simple_list_item_1, data);
        fromNodes.setAdapter(arrayAdapter);
        toNodes.setAdapter(arrayAdapter);
        fromNodes.invalidate();
        toNodes.invalidate();
        fromNodes.setOnTouchListener((v, event) -> {
            if(!fromNodes.isPopupShowing()){
                fromNodes.showDropDown();
            }
            return false;
        });
        toNodes.setOnTouchListener((v, event) -> {
            if(!toNodes.isPopupShowing()){
                toNodes.showDropDown();
            }
            return false;
        });
        arrayAdapter.notifyDataSetChanged();
    }
    private int getNodeID(String doorInfo){
        String[] info = doorInfo.split(" @");
//        float z = Util.getInstance().floorLabelToZ(info[1]);
//        for (Node node : MapInfo.getInstance().nodeList) {
//            if(node.getBuilding().equals(info[0]) && z==node.getZ() && node.getLabel().equals(info[2])){
//                selectedFloor = node.getZ();
//                return node.getID();
//            }
//        }
        float z = Util.getInstance().floorLabelToZ(info[1]);
        for (Node node : MapInfo.getInstance().nodeList) {
            if(z==node.getZ() && node.getLabel().equals(info[0])){
                selectedFloor = node.getZ();
                return node.getID();
            }
        }
        return -1;
    }
}