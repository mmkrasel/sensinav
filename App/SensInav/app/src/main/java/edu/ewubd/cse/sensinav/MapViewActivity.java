package edu.ewubd.cse.sensinav;

import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import java.util.ArrayList;
import java.util.Collections;

import edu.ewubd.cse.sensinav.androidqa.wheel.ArrayWheelAdapter;
import edu.ewubd.cse.sensinav.androidqa.wheel.OnWheelChangedListener;
import edu.ewubd.cse.sensinav.androidqa.wheel.OnWheelScrollListener;
import edu.ewubd.cse.sensinav.androidqa.wheel.WheelView;
import edu.ewubd.cse.sensinav.mapview.Edge;
import edu.ewubd.cse.sensinav.mapview.MapInfo;
import edu.ewubd.cse.sensinav.mapview.MyCanvas;
import edu.ewubd.cse.sensinav.mapview.Node;
import edu.ewubd.cse.sensinav.util.Util;

public class MapViewActivity extends AppCompatActivity {
    private MyCanvas myCanvas;
    private WheelView wheel;
    private String[] whellItems;
    private boolean wheelScrolled = false;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    //private final HashMap<String,String> selectedNodesHashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

//        findViewById(R.id.QRCode).setOnClickListener(v -> {
//            if (ContextCompat.checkSelfPermission(MapNodeActivity.this, Manifest.permission.CAMERA)
//                    != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(MapNodeActivity.this, new String[]{Manifest.permission.CAMERA},
//                        CAMERA_PERMISSION_REQUEST_CODE);
//            } else {
//                ScanCode();
//            }
//        });


//        System.out.println(MapInfo.getInstance().floorsList);
//        int[] btnIds = {R.id.six, R.id.ground, R.id.five, R.id.two};
//        int i = 0;
//        for(int id: btnIds) {
//            findViewById(id).setVisibility(View.INVISIBLE);
//            if(i<MapInfo.getInstance().floorsList.size()){
//                findViewById(id).setVisibility(View.VISIBLE);
//                String label = Util.getInstance().zToFloorLabel(MapInfo.getInstance().floorsList.get(i));
//                Button btn = findViewById(id);
//                btn.setText(label);
//                btn.setOnClickListener(v -> {
//                    showMapForFloor(label);
//                });
//            }
//            i++;
//        }
        // Set initial background image for MyCanvas
        // Bitmap backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_image);
        // myCanvas.setBackgroundImage(backgroundBitmap);
        // Set click listeners for the zoom in and zoom out buttons

        // showMapForFloor(whellItems[wheel.getCurrentItem()]);
    }
    @Override
    public void onStart(){
        super.onStart();
        myCanvas = findViewById(R.id.myCanvas);
        myCanvas.invalidate();
        findViewById(R.id.btnZoomIn).setOnClickListener(v -> myCanvas.zoomIn());
        findViewById(R.id.btnZoomOut).setOnClickListener(v -> myCanvas.zoomOut());
        //        System.out.println(MapInfo.getInstance().floorsList);
        MapInfo.getInstance().floorsList.sort(Collections.reverseOrder().reversed());
        this.initWheel();
    }
    @Override
    public void onResume(){
        super.onResume();
        showMapForFloor("Ground Floor");
    }
    private void initWheel() {
        wheel = findViewById(R.id.floorWheel);
        OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
            @SuppressWarnings("unused")
            public void onScrollStarts(WheelView wheel) {
                wheelScrolled = true;
            }
            @SuppressWarnings("unused")
            public void onScrollEnds(WheelView wheel) {
                wheelScrolled = false;
                //showMapForFloor(whellItems[wheel.getCurrentItem()]);
            }
            @Override
            public void onScrollingStarted(WheelView wheel) { }
            @Override
            public void onScrollingFinished(WheelView wheel) {
                System.out.println(whellItems[wheel.getCurrentItem()]);
                if (!wheelScrolled) {
                    showMapForFloor(whellItems[wheel.getCurrentItem()]);
                }
            }
        };
        final OnWheelChangedListener changedListener = (wheel, oldValue, newValue) -> {
//            if (!wheelScrolled) {
//                showMapForFloor(whellItems[wheel.getCurrentItem()]);
//            }
        };
        MapInfo.getInstance().floorsList.sort(Collections.reverseOrder().reversed());
        whellItems = new String[MapInfo.getInstance().floorsList.size()];
        int i=0;
        int selectedItem = 0;
        int plus = 0;
        for(int f = 0; f < whellItems.length; f++){
            if(MapInfo.getInstance().floorsList.get(f)==0){
                selectedItem = i;
                plus = 1;
            }
            whellItems[i++] = Util.getInstance().zToFloorLabel(MapInfo.getInstance().floorsList.get(f)+plus);
        }
        ArrayWheelAdapter wa = new ArrayWheelAdapter(getApplicationContext(), whellItems);
        wa.setTextSize(24);
        wa.setTextColor(getResources().getColor(R.color.node_info_text_color));
        wheel.setViewAdapter(wa);
        wheel.setVisibleItems(5);
        wheel.setCurrentItem(selectedItem);
        wheel.setCyclic(true);
        wheel.addChangingListener(changedListener);
        wheel.addScrollingListener(scrolledListener);
    }
    private void ScanCode() {
        ScanOptions  options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        options.setCaptureActivity(CaptureActivity.class);
        barLauncher.launch(options);
    }
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(),result->{
        if(result.getContents() != null)
        {
            String extractedText = result.getContents();
            Toast.makeText(MapViewActivity.this, "Text: " + extractedText, Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(MapViewActivity.this, "Invalid QR", Toast.LENGTH_SHORT).show();
        }
    });
    private void showMapForFloor(String floorLabel) {
        float z = Util.getInstance().floorLabelToZ(floorLabel);
        System.out.println("******************************");
        System.out.println(z+"  "+floorLabel);
        System.out.println("******************************");
        ArrayList<Integer> floorNodeIds = new ArrayList<>();
        ArrayList<Node> floorNodes = new ArrayList<>();
        ArrayList<Edge> floorEdges = new ArrayList<>();
        double minX=99999999, maxX=-999999999, minY=999999999, maxY=-999999999;
        for(Edge e:MapInfo.getInstance().edgeList){
            Node n1 = MapInfo.getInstance().getNode(e.node1);
            Node n2 = MapInfo.getInstance().getNode(e.node2);
            //System.out.println(n.getZ());
            if(n1!=null && n2!=null && n1.getZ() == z && n2.getZ() == z){
                floorEdges.add(e);
                if(!floorNodeIds.contains(n1.getID())){
                    floorNodes.add(n1);
                    floorNodeIds.add(n1.getID());
                    if(minX > n1.getX()){
                        minX = n1.getX();
                    }
                    if(minY > n1.getY()){
                        minY = n1.getY();
                    }
                    if(maxX < n1.getX()){
                        maxX = n1.getX();
                    }
                    if(maxY < n1.getY()){
                        maxY = n1.getY();
                    }
                }
                if(!floorNodeIds.contains(n2.getID())){
                    floorNodes.add(n2);
                    floorNodeIds.add(n2.getID());
                    if(minX > n2.getX()){
                        minX = n2.getX();
                    }
                    if(minY > n2.getY()){
                        minY = n2.getY();
                    }
                    if(maxX < n2.getX()){
                        maxX = n2.getX();
                    }
                    if(maxY < n2.getY()){
                        maxY = n2.getY();
                    }
                }
            }
        }
        double mapWidth = maxX - minX;
        double mapHeight = maxY - minX;
        //System.out.println("@MapNodeActivity: "+floorNodes.size()+z+MapInfo.getInstance().nodeList);
        //System.out.println(floorEdges);
        myCanvas.clearCanvas();
        myCanvas.setNodeData(floorNodes, floorEdges, mapWidth, mapHeight);
    }
}

/**
 *
 *       (0,2)        (4,2)
 *         A ------2------ B
 *        / \              / \
 *   (0,1)3   4   (4,1)1   5(4,1)
 *      C ------6------ D ------ E
 *     / \              /         \
 * (1,0)8   2   (2,0)7             3
 *    \ /               \          /
 *     G ------5------ H ------2------ J   (2,0)
 *
 *
 */