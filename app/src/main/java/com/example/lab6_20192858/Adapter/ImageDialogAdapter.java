package com.example.lab6_20192858.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

public class ImageDialogAdapter extends BaseAdapter {
    private Context context;
    private List<Bitmap> puzzlePieces; // Ahora manejamos Bitmaps

    public ImageDialogAdapter(Context c, List<Bitmap> puzzlePieces) {
        context = c;
        this.puzzlePieces = puzzlePieces;
    }

    public void updatePuzzlePieces(List<Bitmap> newPuzzlePieces) {
        puzzlePieces = newPuzzlePieces;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return puzzlePieces.size();
    }

    @Override
    public Object getItem(int position) {
        return puzzlePieces.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            imageView = (ImageView) convertView;
        }

        Bitmap item = puzzlePieces.get(position);
        if (item != null) {
            imageView.setImageBitmap(item);
        } else {
            imageView.setImageBitmap(null); // Puedes establecer un bitmap vacío o un color
        }

        return imageView;
    }
}
