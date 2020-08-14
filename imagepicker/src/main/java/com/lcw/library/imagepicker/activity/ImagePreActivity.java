package com.lcw.library.imagepicker.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lcw.library.imagepicker.R;
import com.lcw.library.imagepicker.adapter.ImagePreViewAdapter;
import com.lcw.library.imagepicker.data.MediaFile;
import com.lcw.library.imagepicker.manager.ConfigManager;
import com.lcw.library.imagepicker.manager.SelectionManager;
import com.lcw.library.imagepicker.provider.ImagePickerProvider;
import com.lcw.library.imagepicker.utils.DataUtil;
import com.lcw.library.imagepicker.view.HackyViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 大图预览界面
 * Create by: chenWei.li
 * Date: 2018/10/3
 * Time: 下午11:32
 * Email: lichenwei.me@foxmail.com
 */
public class ImagePreActivity extends BaseActivity {

    public static final String IMAGE_POSITION = "imagePosition";
    private List<MediaFile> mMediaFileList;
    private int mPosition = 0;

    private ImageView mIvPlay;
    private HackyViewPager mViewPager;
    private LinearLayout mLlPreSelect;
    private ImagePreViewAdapter mImagePreViewAdapter;
    MenuItem menuItem;
    TextView mCount;


    @Override
    protected int bindLayout() {
        return R.layout.activity_pre_image;
    }

    @Override
    protected void initView() {

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mIvPlay = findViewById(R.id.iv_main_play);
        mViewPager = findViewById(R.id.vp_main_preImage);
        mLlPreSelect = findViewById(R.id.ll_pre_select);
        mCount = findViewById(R.id.count);

    }

    @Override
    protected void initListener() {

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                getSupportActionBar().setTitle(String.format("%d/%d", position + 1, mMediaFileList.size()));
                setIvPlayShow(mMediaFileList.get(position));
                updateSelectButton(mMediaFileList.get(position).getPath());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mLlPreSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //如果是单类型选取，判断添加类型是否满足（照片视频不能共存）
                if (ConfigManager.getInstance().isSingleType()) {
                    ArrayList<String> selectPathList = SelectionManager.getInstance().getSelectPaths();
                    if (!selectPathList.isEmpty()) {
                        //判断选中集合中第一项是否为视频
                        if (!SelectionManager.isCanAddSelectionPaths(mMediaFileList.get(mViewPager.getCurrentItem()).getPath(), selectPathList.get(0))) {
                            //类型不同
                            Toast.makeText(ImagePreActivity.this, getString(R.string.single_type_choose), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }

                boolean addSuccess = SelectionManager.getInstance().addImageToSelectList(mMediaFileList.get(mViewPager.getCurrentItem()).getPath());
                if (addSuccess) {
                    updateSelectButton(mMediaFileList.get(mViewPager.getCurrentItem()).getPath());
                    updateCommitButton();
                } else {
                    Toast.makeText(ImagePreActivity.this, String.format(getString(R.string.select_image_max), SelectionManager.getInstance().getMaxCount()), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mIvPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //实现播放视频的跳转逻辑(调用原生视频播放器)
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = FileProvider.getUriForFile(ImagePreActivity.this, ImagePickerProvider.getFileProviderName(ImagePreActivity.this), new File(mMediaFileList.get(mViewPager.getCurrentItem()).getPath()));
                intent.setDataAndType(uri, "video/*");
                //给所有符合跳转条件的应用授权
                List<ResolveInfo> resInfoList = getPackageManager()
                        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_image_picker_gallery, menu);

        menuItem = menu.findItem(R.id.apply);

        updateCommitButton();

        getSupportActionBar().setTitle(String.format("%d/%d", mPosition + 1, mMediaFileList.size()));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection


        if (item.getItemId() == R.id.apply) {

            setResult(RESULT_OK, new Intent());
            finish();

            return true;
        } else if (item.getItemId() == android.R.id.home) {

            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void getData() {
        mMediaFileList = DataUtil.getInstance().getMediaData();
        mPosition = getIntent().getIntExtra(IMAGE_POSITION, 0);

        mImagePreViewAdapter = new ImagePreViewAdapter(this, mMediaFileList);
        mViewPager.setAdapter(mImagePreViewAdapter);
        mViewPager.setCurrentItem(mPosition);
        //更新当前页面状态
        setIvPlayShow(mMediaFileList.get(mPosition));
        updateSelectButton(mMediaFileList.get(mPosition).getPath());
    }

    /**
     * 更新确认按钮状态
     */
    private void updateCommitButton() {

        int maxCount = SelectionManager.getInstance().getMaxCount();

        //改变确定按钮UI
        int selectCount = SelectionManager.getInstance().getSelectPaths().size();
        if (selectCount == 0) {
            menuItem.setTitle(getString(R.string.confirm));
            return;
        }
        if (selectCount < maxCount) {
            menuItem.setTitle(String.format(getString(R.string.confirm_msg), selectCount, maxCount));
            return;
        }
        if (selectCount == maxCount) {
            menuItem.setTitle(String.format(getString(R.string.confirm_msg), selectCount, maxCount));
            return;
        }
    }

    /**
     * 更新选择按钮状态
     */
    private void updateSelectButton(String imagePath) {
        boolean isSelect = SelectionManager.getInstance().isImageSelect(imagePath);

        mCount.setText(SelectionManager.getInstance().getSelectCountIndex(imagePath));
    }

    /**
     * 设置是否显示视频播放按钮
     *
     * @param mediaFile
     */
    private void setIvPlayShow(MediaFile mediaFile) {
        if (mediaFile.getDuration() > 0) {
            mIvPlay.setVisibility(View.VISIBLE);
        } else {
            mIvPlay.setVisibility(View.GONE);
        }
    }

}
