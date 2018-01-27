package flow.hiray.com.ui.adapter;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import flow.hiray.com.R;

/**
 * Created by hiray on 2018/1/27.
 *
 * @author hiray
 */

public class FlowAdapter extends RecyclerView.Adapter<FlowAdapter.ViewHolder> {

    LayoutInflater layoutInflater;
    List<String> flowData;

    public FlowAdapter(List<String> flowData) {
        this.flowData = flowData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (layoutInflater == null)
            layoutInflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(layoutInflater.inflate(R.layout.item_flow, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.Do();
    }

    @Override
    public int getItemCount() {
        return flowData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView appCompatTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            appCompatTextView = (AppCompatTextView) itemView;
        }

        public void Do() {
            appCompatTextView.setText(flowData.get(getAdapterPosition()));
        }
    }
}
