package com.lzy.ninegrid.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.lzy.ninegrid.ImageInfo;
import com.lzy.ninegrid.NineGridView;
import com.lzy.ninegrid.R;

import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * ================================================
 * 作    者：廖子尧
 * 版    本：1.0
 * 创建日期：2016/3/21
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class ImagePreviewAdapter extends PagerAdapter implements PhotoViewAttacher.OnPhotoTapListener {

    private List<ImageInfo> imageInfo;
    private Context context;
    private View currentView;

    public ImagePreviewAdapter(Context context, @NonNull List<ImageInfo> imageInfo) {
        super();
        this.imageInfo = imageInfo;
        this.context = context;
    }

    @Override
    public int getCount() {
        return imageInfo.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        currentView = (View) object;
    }

    public View getPrimaryItem() {
        return currentView;
    }

    public ImageView getPrimaryImageView() {
        return (ImageView) currentView.findViewById(R.id.pv);
    }

    public interface OnImageLongClickListener{
        void onImageLongClick(Context context,String url);
    }

    OnImageLongClickListener onImageLongClickListener;

    public void setOnImageLongClickListener(OnImageLongClickListener onImageLongClickListener) {
        this.onImageLongClickListener = onImageLongClickListener;
    }
    
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photoview, container, false);
        final ProgressBar pb = (ProgressBar) view.findViewById(R.id.pb);
        final PhotoView imageView = (PhotoView) view.findViewById(R.id.pv);

        ImageInfo info = this.imageInfo.get(position);
        imageView.setOnPhotoTapListener(this);
        showExcessPic(info, imageView);

        //如果需要加载的loading,需要自己改写,不能使用这个方法
        NineGridView.getImageLoader().onDisplayImage(view.getContext(), imageView, info.bigImageUrl);

//        pb.setVisibility(View.VISIBLE);
//        Glide.with(context).load(info.bigImageUrl)//
//                .placeholder(R.drawable.ic_default_image)//
//                .error(R.drawable.ic_default_image)//
//                .diskCacheStrategy(DiskCacheStrategy.ALL)//
//                .listener(new RequestListener<String, GlideDrawable>() {
//                    @Override
//                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
//                        pb.setVisibility(View.GONE);
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                        pb.setVisibility(View.GONE);
//                        return false;
//                    }
//                }).into(imageView);
        holder.ivHead.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(onImageLongClickListener!=null){
                        onImageLongClickListener.onImageLongClick(context,info.bigImageUrl);
                    }else{
                        showSaveImgConfrim(context,info.bigImageUrl);
                    }
                    return false;
                }
         });
        container.addView(view);
        return view;
    }
    
    private void showSaveImgConfrim(Context context,String url){
        Dialog newDialog = new Dialog(context, R.style.myDialogTheme);
        View view = LayoutInflater.from(context).inflate(R.layout.confirm_dialog, null);
        newDialog.setContentView(view);
        newDialog.setCanceledOnTouchOutside(false);
        TextView tvTips = view.findViewById(R.id.tv_tips);
        tvTips.setText("是否保存图片到本地？");
        view.findViewById(R.id.tvConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownPicUtil.downPic(url, new DownPicUtil.DownFinishListener() {
                            @Override
                            public void getDownPath(String s) {
                               Toast.makeText(context,"图片已保存",Toast.LENGTH_SHORT).show();
                               sendBoardCast(new File(s));
                            }
                        })
                newDialog.dismiss();
            }
        });
        // 设置对话框的出现位置，借助于window对象
        Window win = newDialog.getWindow();
        win.setGravity(Gravity.CENTER);
        // 弹出对话框时，底部窗体，不变暗。
        WindowManager.LayoutParams lp = win.getAttributes();
        if (UiCommon.widthPixels == 320) {
            DisplayMetrics dm = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(dm);
            UiCommon.widthPixels = dm.widthPixels;
        }
        lp.width = (int) (UiCommon.widthPixels * 0.7);
        win.setAttributes(lp);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0新特性
            newDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }
        newDialog.show();
    }
    
    private void sendBoardCast(File file){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    /** 展示过度图片 */
    private void showExcessPic(ImageInfo imageInfo, PhotoView imageView) {
        //先获取大图的缓存图片
        Bitmap cacheImage = NineGridView.getImageLoader().getCacheImage(imageInfo.bigImageUrl);
        //如果大图的缓存不存在,在获取小图的缓存
        if (cacheImage == null) cacheImage = NineGridView.getImageLoader().getCacheImage(imageInfo.thumbnailUrl);
        //如果没有任何缓存,使用默认图片,否者使用缓存
        if (cacheImage == null) {
            imageView.setImageResource(R.drawable.ic_default_color);
        } else {
            imageView.setImageBitmap(cacheImage);
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    /** 单击屏幕关闭 */
    @Override
    public void onPhotoTap(View view, float x, float y) {
        ((ImagePreviewActivity) context).finishActivityAnim();
    }
}
