package com.ahu.ahutong.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.ahu.ahutong.data.model.Banner

abstract class BannerAdapter constructor(banner: Array<Banner>): PagerAdapter() {
    private  var banner:Array<Banner> = banner

    override fun getCount(): Int {
      return  banner.size;
    }
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(container.context);
        imageView.scaleType = ImageView.ScaleType.CENTER;
        onLoadImg(imageView,banner[position].imgPath)
        imageView.setOnClickListener { imageView.context.startActivity(banner[position].clickIntent)  }
        container.addView(imageView);
        return imageView;
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }
    protected abstract fun onLoadImg(view:ImageView,imgPath:String);
}