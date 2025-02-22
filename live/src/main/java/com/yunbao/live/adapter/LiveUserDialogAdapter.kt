package com.yunbao.live.adapter

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.yunbao.common.CommonAppConfig
import com.yunbao.common.Constants
import com.yunbao.common.adapter.RefreshAdapter
import com.yunbao.common.glide.ImgLoader
import com.yunbao.common.utils.StringUtil
import com.yunbao.live.R
import com.yunbao.live.bean.LiveUserGiftBean

/**
 * Created by http://www.yunbaokj.com on 2023/6/29.
 */
class LiveUserDialogAdapter(
    context: Context
) : RefreshAdapter<LiveUserGiftBean>(context) {

    private val mOnItemClick = View.OnClickListener {
        (it.tag as? LiveUserGiftBean)?.let {
            mOnItemClickListener?.onItemClick(it, 0)
        }
    }
    private val mColor0 = Color.parseColor("#FF4C4C")
    private val mColor1 = Color.parseColor("#FFAD4C")
    private val mColor2 = Color.parseColor("#FFEC4C")
    private val mColor3 = ContextCompat.getColor(context, R.color.textColor)
    private val mVotesName = CommonAppConfig.getInstance().votesName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return Vh(mInflater.inflate(R.layout.item_live_user_dialog, parent, false))
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        (vh as Vh).setData(mList[position], position)
    }

    private inner class Vh(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val mNum: TextView
        val mAvatar: ImageView
        val mName: TextView
        val mSexGroup: View
        val mSex: ImageView
        val mAge: TextView
        val mCity: TextView
        val mConsume: TextView


        init {
            itemView.setOnClickListener(mOnItemClick)
            mNum = itemView.findViewById(R.id.num)
            mAvatar = itemView.findViewById(R.id.avatar)
            mName = itemView.findViewById(R.id.name)
            mSexGroup = itemView.findViewById(R.id.group_sex)
            mSex = itemView.findViewById(R.id.img_sex)
            mAge = itemView.findViewById(R.id.age)
            mCity = itemView.findViewById(R.id.city)
            mConsume = itemView.findViewById(R.id.consume)
        }

        fun setData(bean: LiveUserGiftBean, position: Int) {
            itemView.setTag(bean)
            mNum.text = (position + 1).toString()
            if (position == 0) {
                mNum.setTextColor(mColor0)
            } else if (position == 1) {
                mNum.setTextColor(mColor1)
            } else if (position == 2) {
                mNum.setTextColor(mColor2)
            } else {
                mNum.setTextColor(mColor3)
            }
            ImgLoader.displayAvatar(mContext, bean.avatar, mAvatar)
            mName.text = bean.userNiceName
            if (bean.sex == Constants.SEX_MALE) {
                mSexGroup.background =
                    ContextCompat.getDrawable(mContext, R.drawable.bg_live_user_sex_1)
                mSex.setImageResource(R.mipmap.icon_live_user_sex_1)
            } else {
                mSexGroup.background =
                    ContextCompat.getDrawable(mContext, R.drawable.bg_live_user_sex_2)
                mSex.setImageResource(R.mipmap.icon_live_user_sex_2)
            }
            mAge.text = bean.age.toString()
            mCity.text = bean.city
            mConsume.text = StringUtil.contact(bean.contribution, mVotesName)
        }
    }
}