package edu.ewubd.cse.sensinav.mapview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

import edu.ewubd.cse.sensinav.R;
import edu.ewubd.cse.sensinav.mapview.Node;
import edu.ewubd.cse.sensinav.util.Util;

public class NodeListAdapter extends ArrayAdapter<Node> {

    private static final String TAG = "NodeListAdapter";

    private Context mContext;
    private int mResource;
    //private int lastPosition = -1;
    private static class ViewHolder {
        TextView labelNode;
        TextView nodeID;
        TextView X;
        TextView Y;
        TextView Z;
    }

    public NodeListAdapter(Context context, int resource, ArrayList<Node> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //get the Node information
        Node node = getItem(position);
        //create the view result for showing the animation
        final View result;
        //ViewHolder object
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.labelNode = convertView.findViewById(R.id.labelNode);
            holder.nodeID = convertView.findViewById(R.id.nodeID);
            holder.X = convertView.findViewById(R.id.X);
            holder.Y = convertView.findViewById(R.id.Y);
            holder.Z = convertView.findViewById(R.id.Z);
            result = convertView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }
        holder.labelNode.setText(node.getLabel());
        holder.nodeID.setText(String.valueOf(node.getID()));
        holder.X.setText(String.valueOf(node.getX()));
        holder.Y.setText(String.valueOf(node.getY()));
        holder.Z.setText(Util.getInstance().zToFloorLabel(node.getZ()));
        return convertView;
    }
}