package isden.mois.magellanlauncher;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.*;

/**
 * Created by fray on 15.05.14.
 */
// TODO: Переименовать, почистить код, переместить код из адаптера.
public class ApplicationsActivity extends Activity implements View.OnClickListener {
    public static final String ActionLibrary = "fraylauncher.lib";
    public static final String ActionFM = "fraylauncher.fm";
    public static final String ActionApps = "fraylauncher.apps";

    private static GridView g;
    private static TextView path;
    private static TextView lib_progress;
    private BaseAdapter ad;

    public static void setPageNum(int progress)
    {
        int numElems = g.getCount();
        int size = g.getLastVisiblePosition() - g.getFirstVisiblePosition() + 1;
        if (size <= 0)
            size = 16;


        if (lib_progress != null)
            if (numElems != 0)
                lib_progress.setText(
                        "[" +
                        (progress/size + 1) + "/" + (numElems/size + (numElems % size == 0 ? 0 : 1))
                        +"]"
                );
            else
                lib_progress.setText("[1/1]");

    }

    public static void changeDir(String title)
    {
        if (path != null)
        {
            path.setText(title);
        }
        setPageNum(0);
    }

    @Override
    public void onCreate(Bundle sl)
    {
        super.onCreate(sl);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_applications);

        path = (TextView)findViewById(R.id.lib_path);
        lib_progress = (TextView)findViewById(R.id.lib_progress);


        g = (GridView)findViewById(R.id.app_grid);
        String action = getIntent().getAction();


        ad = new com.fray.launcher.adapters.AppAdapter(this);
        g.setAdapter(ad);
        g.setOnItemClickListener((com.fray.launcher.adapters.AppAdapter)ad);

        ImageButton butt = (ImageButton)findViewById(R.id.lib_prev);
        butt.setOnClickListener(this);

        butt = (ImageButton)findViewById(R.id.lib_next);
        butt.setOnClickListener(this);

//        RadioButton rb = (RadioButton)findViewById(R.id.rbList);
//        rb.setOnClickListener(this);
//        if (ViewSwitcher.ViewFactory.type == ViewType.LIST) {
//            rb.setChecked(true);
//            g.setVerticalSpacing(10);
//            g.setNumColumns(2);
//        }
//
//        rb = (RadioButton)findViewById(R.id.rbGrid);
//        rb.setOnClickListener(this);

    }

    @Override
    public void onClick(View v)
    {
        int first = g.getFirstVisiblePosition();
        int last = g.getLastVisiblePosition();

        final int target;

        switch (v.getId()) {
            case R.id.lib_prev:
                first -= last - first + 1;
                if (first < 0)
                    first = 0;
                target = first;
                break;
            case R.id.lib_next:
                int total = g.getCount();

                if (total == last + 1)
                    return;
                last++;
                if (last > (total - 1))
                    last = total - 1;
                target = last;
                break;
            case R.id.rbGrid:
                target = 0;
                ViewFactory.type = ViewType.GRID;
                g.setNumColumns(4);
                g.setVerticalSpacing(0);
                g.setAdapter(ad);
                break;
            case R.id.rbList:
                target = 0;
                ViewFactory.type = ViewType.LIST;
                g.setNumColumns(2);
                g.setVerticalSpacing(10);
                g.setAdapter(ad);
                break;

            default:
                target = 0;
        }


        g.clearFocus();
        g.post(new Runnable() {

            public void run() {
                g.setSelection(target);
            }
        });

        setPageNum(target);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        View v;
        switch (keyCode) {
            case 92:
                v = findViewById(R.id.lib_prev);
                onClick(v);
                break;
            case 93:
                v = findViewById(R.id.lib_next);
                onClick(v);
                break;
            default:
                return super.onKeyDown(keyCode,event);
        }
        //Log.i("KEY_DOWNED","ID: " + keyCode);
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        setPageNum(0);
    }

}
