package com.memorizer.memorizer.models;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * Created by YS on 2017-01-10.
 */

public class LabelData implements Serializable {
    private String labelName;
    private int labelPosition;
    private Drawable labelDrawable;
    private boolean selected = false;

    public LabelData(String labelName, int labelPosition) {
        this.labelName = labelName;
        this.labelPosition = labelPosition;
    }

    public LabelData(String labelName, int labelPosition, Drawable labelDrawable) {
        this.labelName = labelName;
        this.labelPosition = labelPosition;
        this.labelDrawable = labelDrawable;
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public int getLabelPosition() {
        return labelPosition;
    }

    public void setLabelPosition(int labelPosition) {
        this.labelPosition = labelPosition;
    }

    public Drawable getLabelDrawable() {
        return labelDrawable;
    }

    public void setLabelDrawable(Drawable labelDrawable) {
        this.labelDrawable = labelDrawable;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
