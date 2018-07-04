package com.airremote.android.editor;

import com.airremote.android.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class RemoteControllerEditor extends Activity
    implements View.OnLongClickListener, View.OnClickListener, View.OnTouchListener {

    // Object that sends out drag-drop events while a view is being moved.
    private DragController mDragController;
    // The ViewGroup that supports drag-drop.
    private DragLayer mDragLayer;
    // If true, it takes a long click to start the drag operation.
    // Otherwise, any touch event starts a drag.
    private boolean mLongClickStartsDrag = true;

    private boolean mBtnListShow = false;
    private ViewStub mBtnListViewStub;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDragController = new DragController(this);

        setContentView(R.layout.controller_editor);
        setupViews();
    }

    /**
     * This is the starting point for a drag operation if mLongClickStartsDrag is false.
     * It looks for the down event that gets generated when a user touches the screen.
     * Only that initiates the drag-drop sequence.
     *
     */
    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        boolean handleHere = false;
        final int action = ev.getAction();

        // In the situation where a long click is not needed to initiate a drag, simply start on the down event.
        if (action == MotionEvent.ACTION_DOWN) {
            handleHere = startDrag(v);
        }
        return handleHere;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onLongClick(View arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * Start dragging a view.
     *
     */
    public boolean startDrag(View v) {
        // Let the DragController initiate a drag-drop sequence.
        // I use the dragInfo to pass along the object being dragged.
        // I'm not sure how the Launcher designers do this.
        Object dragInfo = v;
        mDragController.startDrag(v, mDragLayer, dragInfo, DragController.DRAG_ACTION_MOVE);
        return true;
    }

    /**
     * Finds all the views we need and configure them to send click events to the activity.
     *
     */
    private void setupViews()
    {
        DragController dragController = mDragController;

        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mDragLayer.setDragController(dragController);
        dragController.addDropTarget (mDragLayer);

        ImageView i1 = (ImageView) findViewById (R.id.Image1);
        ImageView i2 = (ImageView) findViewById (R.id.Image2);

        i1.setOnClickListener(this);
        i1.setOnLongClickListener(this);
        i1.setOnTouchListener(this);

        i2.setOnClickListener(this);
        i2.setOnLongClickListener(this);
        i2.setOnTouchListener(this);

        TextView tv = (TextView) findViewById (R.id.Text1);
        tv.setOnLongClickListener(this);
        tv.setOnTouchListener(this);

        String message = mLongClickStartsDrag ? "Press and hold to start dragging."
                                              : "Touch a view to start dragging.";
        Toast.makeText (getApplicationContext(), message, Toast.LENGTH_LONG).show ();

        mBtnListViewStub = (ViewStub) findViewById(R.id.viewstub_btn_selection);
        if (mBtnListViewStub != null) {
            mBtnListViewStub.inflate();
        }
        Button btnListShow = (Button) findViewById(R.id.btn_list_show);
        btnListShow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mBtnListShow) {
                    mBtnListViewStub.setVisibility(View.VISIBLE);
                } else {
                    mBtnListViewStub.setVisibility(View.GONE);
                }
                mBtnListShow = !mBtnListShow;
            }
        });
    }

    /**
     * Show a string on the screen via Toast.
     *
     * @param msg String
     * @return void
     */

    public void toast (String msg)
    {
        Toast.makeText (getApplicationContext(), msg, Toast.LENGTH_SHORT).show ();
    } // end toast
}