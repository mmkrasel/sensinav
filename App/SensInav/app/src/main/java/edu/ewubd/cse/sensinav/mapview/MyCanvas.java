package edu.ewubd.cse.sensinav.mapview;

// campus main --> room 630
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import androidx.annotation.NonNull;
import java.util.ArrayList;

import edu.ewubd.cse.sensinav.R;

public class MyCanvas extends View {
    private Paint bgPaint, pathBorderPaint, circlePaint, currentCirclePaint, pathPaint, textPaint, shortestPathPaint;
    private float previousX, previousY, currentLocationX = -999999.0f, currentLocationY = -999999.0f;
    private float translateX=0, translateY=0;
    private float zoomFactor = 1.0f, scaleFactor = 5.0f;
    private final float maxScaleFactor = 50f;
    private ScaleGestureDetector mScaleGestureDetector;
    private final ArrayList<CircleCoordinates> circleCoordinatesList = new ArrayList<>();
    private final ArrayList<LineCoordinates> lineCoordinatesList = new ArrayList<>();
    private final ArrayList<LineCoordinates> shortestPathCoordinates = new ArrayList<>();
    private boolean isScaleSet = false, redrawCurrentLocation = false;
    private double mapWidth, mapHeight;
    private float direction;

    public MyCanvas(Context context) {
        super(context);
        init(context);
    }
    public MyCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public void clearCanvas() {
        circleCoordinatesList.clear();
        lineCoordinatesList.clear();
        invalidate();
    }
    private void init(Context c) {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inScaled = false;
//        options.inDensity = options.inTargetDensity ;
//        bmpDirection = BitmapFactory.decodeResource(getResources(), R.drawable.icon_navigation, options);
//        //bmpDirection = BitmapFactory.decodeResource(c.getResources(), R.drawable.icon_navigation);
//        int scaledWidth = 8;//(int) (width * 1);
//        int scaledHeight = 8;//(int) (height * 1);
//        //Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        bmpDirection = Bitmap.createScaledBitmap(bmpDirection, scaledWidth, scaledHeight, false);

        bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#D5F5E3"));
        bgPaint.setStyle(Paint.Style.FILL);

        pathPaint = new Paint();
        pathPaint.setColor(Color.parseColor("#FDEDEC"));
        pathPaint.setStyle(Paint.Style.STROKE);

        pathBorderPaint = new Paint();
        pathBorderPaint.setColor(Color.parseColor("#34495E")); // Set the border color
        pathBorderPaint.setStyle(Paint.Style.STROKE);
//        String pathColor = "";
//        if (pathLineCordinates.getStartZ() == 1) pathColor = "#54aeff";
//        else if (pathLineCordinates.getStartZ() == 2) pathColor = "#98e4ff";
//        else if (pathLineCordinates.getStartZ() == 3) pathColor = "#d0bfff";
//        else if (pathLineCordinates.getStartZ() == 4) pathColor = "#ffcf96";
//        else if (pathLineCordinates.getStartZ() == 5) pathColor = "#64ccc5";
//        else pathColor = "#8e8ffa";
        shortestPathPaint = new Paint();
        shortestPathPaint.setColor(Color.parseColor("#8e8ffa")); // Set color for the path
        shortestPathPaint.setStyle(Paint.Style.STROKE);

        circlePaint = new Paint();
        circlePaint.setColor(Color.parseColor("#922B21"));
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setStrokeWidth(2f);

        currentCirclePaint = new Paint();
        currentCirclePaint.setColor(Color.parseColor("#FF4400"));
        currentCirclePaint.setStyle(Paint.Style.FILL);
        //currentCirclePaint.setStrokeWidth(0.5f);


        textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);
        Typeface boldTypeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
        textPaint.setTypeface(boldTypeface);
        textPaint.setColor(Color.parseColor("#616A6B"));
        //private Path roadPath;
//        new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener() {
//            @Override
//            public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
//                translateX -= distanceX / zoomFactor;
//                translateY -= distanceY / zoomFactor;
//                invalidate();
//                return true;
//            }
//        });
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.1f, Math.min(scaleFactor, maxScaleFactor));
                invalidate();
                return true;
            }
        });
    }
    public void setNodeData(ArrayList<Node> nodes, ArrayList<Edge> edges, double mapWidth, double mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        for (Node n : nodes) {
            this.circleCoordinatesList.add(new CircleCoordinates((float) n.getX(), (float) n.getY(), (float) n.getZ(), n.getLabel()));
        }
        for(Edge e: edges) {
            if (MapInfo.getInstance().nodesIndices.containsKey(e.node1) && MapInfo.getInstance().nodesIndices.containsKey(e.node2)) {
                Integer n1id = MapInfo.getInstance().nodesIndices.get(e.node1);
                Integer n2id = MapInfo.getInstance().nodesIndices.get(e.node2);
                if (n1id != null && n2id != null) {
                    Node n1 = MapInfo.getInstance().nodeList.get(n1id);
                    Node n2 = MapInfo.getInstance().nodeList.get(n2id);
                    this.lineCoordinatesList.add(new LineCoordinates((float) n1.getX(), (float) n1.getY(), (float) n1.getZ(), (float) n2.getX(), (float) n2.getY(), (float) n2.getZ()));
                    //System.out.println(n1.getLabel() + " -> " + n2.getLabel() + (float) n1.getX() + ", " + (float) n1.getY() + ", " + (float) n1.getZ() + ", " + (float) n2.getX() + ", " + (float) n2.getY() + ", " + (float) n2.getZ());
                }
            }
        }
        redrawCurrentLocation = false;
        invalidate();
    }
    public void zoomIn() {
        zoomFactor += 0.1f; // Adjust the increment as per your preference
        invalidate();
    }
    public void zoomOut() {
        zoomFactor -= 0.1f; // Adjust the decrement as per your preference
        invalidate();
    }
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        if(!isScaleSet){
            if(width >0 && scaleFactor < width /mapWidth){
                scaleFactor = (float) (width /mapWidth);
            }
            if(height > 0 && scaleFactor > height /mapHeight){
                scaleFactor = (float) (height /mapHeight);
            }
            if(width >0 && height >0) {
                scaleFactor = scaleFactor * 0.85f;
            }
            //translateX = width/4;
            translateY = height /4;
            System.out.println("***************************************");
            System.out.println(width +" "+ height +" "+scaleFactor+" "+translateY+" "+mapWidth +" "+ mapHeight);
            System.out.println("***************************************");
            isScaleSet = true;
        }
        // Apply zoom and translation to the canvas
        canvas.scale(zoomFactor, zoomFactor);
        canvas.translate(translateX, translateY);
        canvas.scale(scaleFactor, scaleFactor, width /2, height /2);
        canvas.drawPaint(bgPaint);

        if (lineCoordinatesList != null) {
            for (LineCoordinates lineCoordinates : lineCoordinatesList) {
                float startX = width /2 - lineCoordinates.getStartX() * zoomFactor;
                float startY = height /2 - lineCoordinates.getStartY() * zoomFactor;
                float endX = width /2 - lineCoordinates.getEndX() * zoomFactor;
                float endY = height /2 - lineCoordinates.getEndY() * zoomFactor;
                pathBorderPaint.setStrokeWidth(5f * zoomFactor);
                pathPaint.setStrokeWidth(4f * zoomFactor);
                Path path = new Path();
                path.moveTo(startX, startY);
                path.lineTo(endX, endY);
                canvas.drawPath(path, pathBorderPaint);
                canvas.drawPath(path, pathPaint);
                // Reset the path effect to draw subsequent lines normally
                pathPaint.setPathEffect(null);
            }
        }
        // For path line
        if (shortestPathCoordinates != null) {
            for (LineCoordinates pathLineCordinates : shortestPathCoordinates) {
                float startX = width /2 - pathLineCordinates.getStartX() * zoomFactor;
                float startY = height /2 - pathLineCordinates.getStartY() * zoomFactor;
                float endX = width /2 - pathLineCordinates.getEndX() * zoomFactor;
                float endY = height /2 - pathLineCordinates.getEndY() * zoomFactor;
                Path path = new Path();
                path.moveTo(startX, startY);
                path.lineTo(endX, endY);
                shortestPathPaint.setStrokeWidth(zoomFactor);
                canvas.drawPath(path, shortestPathPaint);
                // Reset the path effect to draw subsequent lines normally
                shortestPathPaint.setPathEffect(null);
            }
        }
        // Draw decorative circles
        // Draw circles based on CircleCoordinates
        if (circleCoordinatesList != null) {
            for (CircleCoordinates circleCoordinates : circleCoordinatesList) {
                // Adjust the coordinates based on the current zoom factor
                float nodeX = circleCoordinates.getX() * zoomFactor;
                float nodeY = circleCoordinates.getY() * zoomFactor;
                String nodeName = circleCoordinates.getNodeName();
                canvas.drawCircle(width /2 - nodeX, height /2 - nodeY, 1.2f * zoomFactor, circlePaint);
                float textHeight = textPaint.descent() - textPaint.ascent();
                float textOffset = (textHeight / 2) - textPaint.descent();
                textPaint.setTextSize(2f * zoomFactor);
                canvas.drawText(nodeName, width /2 - nodeX, (height /2 - nodeY) - textOffset, textPaint);
            }
        }
        //if(redrawCurrentLocation){
        drawCurrentLocation(canvas, width, height, currentLocationX, currentLocationY);
        //return;
        //}
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        int action = event.getActionMasked();
        //    private float rotationDegrees = 0;
        float mScaleFactor = 1.0f;
        switch(action) {
            case MotionEvent.ACTION_DOWN:
                previousX = event.getX();
                previousY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - previousX;
                float dy = event.getY() - previousY;
                translateX += dx / mScaleFactor;
                translateY += dy / mScaleFactor;
                invalidate();
                previousX = event.getX();
                previousY = event.getY();
                break;
            case MotionEvent.ACTION_CANCEL:
                float dx2 = event.getX() - previousX;
                float dy2 = event.getY() - previousY;
                translateX=dx2 / mScaleFactor;
                translateY=dy2 / mScaleFactor;
                break;
        }
        return true;
    }
    private void drawCurrentLocation(Canvas canvas, float width, float height, float x, float y) {
        if(currentLocationX != -999999.0f && currentLocationY != -999999.0f) {
            float adjustedX =  width/2 - (x * zoomFactor);// - bmpDirection.getWidth()/2;
            float adjustedY = height/2 - (y * zoomFactor);// - bmpDirection.getHeight()/2;
            currentCirclePaint.setStyle(Paint.Style.STROKE);
            currentCirclePaint.setStrokeWidth(0.5f);
            currentCirclePaint.setColor(Color.RED);
            Path path = new Path();
            path.moveTo(0, -4);
            path.lineTo(2, 0);
            path.lineTo(0, -2);
            //path.lineTo(0.1f, 1);
            //path.lineTo(-0.1f, -2);
            path.lineTo(-2, 0);
            path.close();
            path.offset(adjustedX, adjustedY);

            Matrix mMatrix = new Matrix();
            RectF bounds = new RectF();
            path.computeBounds(bounds, true);
            mMatrix.postRotate(direction, bounds.centerX(), bounds.centerY());
            path.transform(mMatrix);
            canvas.drawPath(path, currentCirclePaint);
        }
        redrawCurrentLocation = false;
        //currentLocationX = -999999.0f;
        //currentLocationY = -999999.0f;
    }
//    public Canvas getCanvas() {
//        return canvas;
//    }
    public void setCoordinates(float x, float y, float direction) {
        this.currentLocationX = x;
        this.currentLocationY = y;
        this.direction = direction;
        redrawCurrentLocation = true;
        invalidate(); // Redraw the canvas with the new dot
    }
    public void setNodesAtShortestPath(ArrayList<Node> nodes) {
        //mapChange=true;
        boolean isFirstNode = true;
        Node s = null;
        for (Node n : nodes) {
            //this.circleCoordinatesList.add(new CircleCoordinates((float) n.getX(), (float) n.getY(), (float) n.getZ(), n.getLabel()));
            if(isFirstNode){
                isFirstNode = false;
                s = n;
                continue;
            }
            this.shortestPathCoordinates.add(new LineCoordinates((float) s.getX(), (float) s.getY(), (float) s.getZ(), (float) n.getX(), (float) n.getY(), (float) n.getZ()));
            //System.out.println("@MyCanvas: "+s.getLabel()+" -> "+n.getLabel()+": "+s.getJSONArray());
            s = n;
        }
        //invalidate();
    }
}