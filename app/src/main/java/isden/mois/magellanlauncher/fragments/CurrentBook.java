package isden.mois.magellanlauncher.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import isden.mois.magellanlauncher.Metadata;
import isden.mois.magellanlauncher.Onyx;
import isden.mois.magellanlauncher.R;

/**
 * Created by ray on 03.08.2014.
 */
public class CurrentBook extends Fragment {

    private static String TAG = "BookFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_current_book, container, false);

        LinearLayout l = (LinearLayout) v.findViewById(R.id.nowRead);
        l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(getActivity().getPackageManager().getLaunchIntentForPackage("com.neverland.alreader"));
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.app_not_started, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return v;
    }

    /**
     * Set current book data.
     */
    private void setLastReading() {
        Metadata metadata = Onyx.getCurrentBook(getActivity());
        if (metadata == null) {
            return;
        }

        TextView twTitle = (TextView) getActivity().findViewById(R.id.txtTitle);
        TextView twAuthor = (TextView) getActivity().findViewById(R.id.txtAuthor);

        if (metadata.getTitle() != null) {
            twTitle.setText(metadata.getTitle());
            twAuthor.setText(metadata.getAuthor());
        } else {
            twTitle.setText(metadata.getName());
            twAuthor.setHeight(0);
        }

        ImageView imgBook = (ImageView) getActivity().findViewById(R.id.imgBook);
        Bitmap image = metadata.getThumbnail();
        if (image == null) {
            imgBook.setImageResource(R.drawable.book_img);
        }
        else {
            imgBook.setImageBitmap(image);
        }

        ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.pbProgress);
        progressBar.setProgress((int) Math.round(metadata.getPercent()));

        TextView progressText = (TextView) getActivity().findViewById(R.id.twProgress);
        progressText.setText(metadata.getProgress());

        TextView TimeReadText = (TextView) getActivity().findViewById(R.id.twReadTime);
        TimeReadText.setText(metadata.formatTimeProgress());
    }

    @Override
    public void onResume() {
        super.onResume();
        setLastReading(); // Считываем последнюю считанную книгу
    }
}
