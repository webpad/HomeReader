package org.geometerplus.android.fbreader.library;

import java.util.HashMap;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import ntx.reader3.R;

public class ReaderBookInformationDialogFragment extends Fragment {
    public final static String ARGUMENT_BOOK_INDEX = "arg_book_index";
    public final static String ARGUMENT_BOOK_TITLE = "arg_book_title";
    public final static String ARGUMENT_BOOK_LANGUAGE = "arg_book_language";
    public final static String ARGUMENT_BOOK_SIZE = "arg_book_size";
    public final static String ARGUMENT_BOOK_TYPE = "arg_book_type";
    public final static String ARGUMENT_BOOK_PATH = "arg_book_path";
    public final static String ARGUMENT_IS_IMAGE = "arg_is_image";
    private static final String STRING_KB = "KB";
    private static final String STRING_MB = "MB";

    private Activity mActivity;

    private int mBookIndex;
    private String mBookTitle;
    private String mBookLanguage;
    private long mBookSize;
    private String mBookType;
    private String mBookPath;
    private boolean isImage = false;

    public interface OnButtonClickListener {
        void onDeleteBtnClick(int bookIndex, boolean isImageData);

        void onOpenBtnClick(int bookIndex);
    }

    private OnButtonClickListener mCallback;

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.mCallback = listener;
    }

    public static ReaderBookInformationDialogFragment newInstance(RecentlyBookData bookData) {

        ReaderBookInformationDialogFragment frag = new ReaderBookInformationDialogFragment();
        Bundle args = new Bundle();

        args.putInt(ARGUMENT_BOOK_INDEX, bookData.getIndex());
        args.putString(ARGUMENT_BOOK_TITLE, bookData.getTitle());
        args.putString(ARGUMENT_BOOK_LANGUAGE, bookData.getLanguage());
        args.putLong(ARGUMENT_BOOK_SIZE, bookData.getSize());
        args.putString(ARGUMENT_BOOK_TYPE, bookData.getType());
        args.putString(ARGUMENT_BOOK_PATH, bookData.getPath());
        frag.setArguments(args);

        return frag;
    }
    
    public static ReaderBookInformationDialogFragment newInstance(HashMap bookData) {

        ReaderBookInformationDialogFragment frag = new ReaderBookInformationDialogFragment();
        Bundle args = new Bundle();

        args.putInt(ARGUMENT_BOOK_INDEX, Integer.parseInt(bookData.get(ARGUMENT_BOOK_INDEX).toString()));
        args.putString(ARGUMENT_BOOK_TITLE, bookData.get(ARGUMENT_BOOK_TITLE).toString());
		args.putString(ARGUMENT_BOOK_LANGUAGE,
				bookData.get(ARGUMENT_BOOK_LANGUAGE) == null ? "N/A" : bookData.get(ARGUMENT_BOOK_LANGUAGE).toString());
        args.putLong(ARGUMENT_BOOK_SIZE, Long.parseLong(bookData.get(ARGUMENT_BOOK_SIZE).toString()));
        args.putString(ARGUMENT_BOOK_TYPE, bookData.get(ARGUMENT_BOOK_TYPE).toString());
        args.putString(ARGUMENT_BOOK_PATH, bookData.get(ARGUMENT_BOOK_PATH).toString());
        frag.setArguments(args);

        return frag;
    }
    
    public static ReaderBookInformationDialogFragment newInstanceImage(HashMap ImageData) {

        ReaderBookInformationDialogFragment frag = new ReaderBookInformationDialogFragment();
        Bundle args = new Bundle();

        args.putInt(ARGUMENT_BOOK_INDEX, Integer.parseInt(ImageData.get(ARGUMENT_BOOK_INDEX).toString()));
        args.putString(ARGUMENT_BOOK_TITLE, ImageData.get(ARGUMENT_BOOK_TITLE).toString());
        args.putString(ARGUMENT_BOOK_LANGUAGE, ImageData.get(ARGUMENT_BOOK_LANGUAGE).toString());
        args.putLong(ARGUMENT_BOOK_SIZE, Long.parseLong(ImageData.get(ARGUMENT_BOOK_SIZE).toString()));
        args.putString(ARGUMENT_BOOK_TYPE, ImageData.get(ARGUMENT_BOOK_TYPE).toString());
        args.putString(ARGUMENT_BOOK_PATH, ImageData.get(ARGUMENT_BOOK_PATH).toString());
        args.putBoolean(ARGUMENT_IS_IMAGE, Boolean.parseBoolean(ImageData.get(ARGUMENT_IS_IMAGE).toString()));
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();

        mBookIndex = getArguments().getInt(ARGUMENT_BOOK_INDEX);
        mBookTitle = getArguments().getString(ARGUMENT_BOOK_TITLE, "");
        mBookLanguage = getArguments().getString(ARGUMENT_BOOK_LANGUAGE, "");
        mBookSize = getArguments().getLong(ARGUMENT_BOOK_SIZE, 0);
        mBookType = getArguments().getString(ARGUMENT_BOOK_TYPE, "");
        mBookPath = getArguments().getString(ARGUMENT_BOOK_PATH, "");
		isImage = getArguments().getBoolean(ARGUMENT_IS_IMAGE, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_home_book_item_info, container, false);
        initView(v);
        return v;
    }

    private void initView(View v) {
        EllipsizingTextView tvBookTitle = (EllipsizingTextView) v.findViewById(R.id.tv_book_title);
        tvBookTitle.setMaxLines(2);
        tvBookTitle.setText(mBookTitle);

        TextView tvBookLanguage = (TextView) v.findViewById(R.id.tv_book_language);
        tvBookLanguage.setText(mBookLanguage);

        TextView tvBookSize = (TextView) v.findViewById(R.id.tv_book_size);
        String sizeStr = "< 1 " + STRING_KB;
        if ((mBookSize / 1024f / 1024f) > 1) {
            sizeStr = " " + (int) (mBookSize / 1024f / 1024f) + " " + STRING_MB;
        } else if ((mBookSize / 1024f) > 1) {
            sizeStr = " " + (int) (mBookSize / 1024f) + " " + STRING_KB;
        }
        tvBookSize.setText(sizeStr);

        TextView tvBookFormat = (TextView) v.findViewById(R.id.tv_book_format);
        tvBookFormat.setText(mBookType);

        EllipsizingTextView tvBookPath = (EllipsizingTextView) v.findViewById(R.id.tv_book_path);
        tvBookPath.setMaxLines(2);
        tvBookPath.setText(mBookPath);

        Button btnClose = (Button) v.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        LinearLayout btnOpenBook = (LinearLayout) v.findViewById(R.id.btn_open_book);
        LinearLayout layout_language = (LinearLayout) v.findViewById(R.id.layout_language);
        
        if(isImage){
        	layout_language.setVisibility(View.GONE);
        	btnOpenBook.setVisibility(View.GONE);
        }
        
        btnOpenBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null)
                    mCallback.onOpenBtnClick(mBookIndex);
                dismiss();
            }
        });

        LinearLayout btnDeleteBook = (LinearLayout) v.findViewById(R.id.btn_delete_book);
        btnDeleteBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null)
                    mCallback.onDeleteBtnClick(mBookIndex, isImage);
                dismiss();
            }
        });
    }

    public void dismiss() {
        mActivity.getFragmentManager().beginTransaction().remove(this).commit();
    }
}
