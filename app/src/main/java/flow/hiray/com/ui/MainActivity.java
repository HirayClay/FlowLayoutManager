package flow.hiray.com.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.lm.FlowLayoutManager;

import java.util.Arrays;
import java.util.List;

import flow.hiray.com.R;
import flow.hiray.com.ui.adapter.FlowAdapter;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(new FlowAdapter(fetch()));
        recyclerView.setLayoutManager(new FlowLayoutManager());
    }

    private List<String> fetch() {
        return Arrays.asList("Eroe",
                "Cherry","Bee","Tank","Crappy Monster",
                "SakuraCherry Moi",
                "Bob Dylan","RxUnfur","Tinker","solidity",
                "Etherium","Bitcoin","EOS","otcbtc.com","Reksai","MingMing",
                "Blander","FireMan","Freaky","psycho","Wolf","Vampire");
    }
}
