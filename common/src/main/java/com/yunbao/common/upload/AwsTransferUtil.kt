package com.yunbao.common.upload

import android.content.Context
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client

/**
 * Created by http://www.yunbaokj.com on 2024/2/29.
 */
class AwsTransferUtil(
    context: Context,
    cognitoPoolId: String,
    regionStr: String,
) {
    private val networkLossHandler:TransferNetworkLossHandler
    val mTransferUtility: TransferUtility

    init {
        networkLossHandler = TransferNetworkLossHandler.getInstance(context)
        val regions = Regions.fromName(regionStr)
        val cognitoCredProvider = CognitoCachingCredentialsProvider(
            context,
            cognitoPoolId,
            regions
        )
        val s3Client = AmazonS3Client(cognitoCredProvider, Region.getRegion(regions))
        mTransferUtility = TransferUtility.builder().s3Client(s3Client).context(context).build()
    }

}