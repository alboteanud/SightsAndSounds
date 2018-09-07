package com.craiovadata.sightsandsounds;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.craiovadata.sightsandsounds.model.Item;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog Fragment containing rating form.
 */
public class InfoDialogFragment extends DialogFragment {

    public static final String TAG = "InfoDialog";
    Item item;

    @BindView(R.id.iimage_info_text_view)
    TextView imageInfoTextView;

    @BindView(R.id.image_textView_title)
    TextView imgTitleTextView;

    @BindView(R.id.sound_info_text_view)
    TextView soundInfoTextView;

    @BindView(R.id.sound_textView_title)
    TextView soundTitleTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_info, container, false);
        ButterKnife.bind(this, v);

        imgTitleTextView.setText(item.getImg_title());
        imageInfoTextView.setText(item.getImg_description());
        soundTitleTextView.setText(item.getMusic_title());
        soundInfoTextView.setText(item.getMusic_description());

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    @OnClick(R.id.info_cancel)
    public void onOkClicked(View view) {
        dismiss();
    }


    public void setItem(Item item) {
        this.item = item;
    }
}
