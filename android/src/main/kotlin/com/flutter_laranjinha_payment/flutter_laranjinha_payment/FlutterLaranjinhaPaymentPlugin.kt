package com.flutter_laranjinha_payment.flutter_laranjinha_payment
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.flutter_laranjinha_payment.flutter_laranjinha_payment.deeplink.Deeplink
import com.flutter_laranjinha_payment.flutter_laranjinha_payment.deeplink.PaymentDeeplink
import com.flutter_laranjinha_payment.flutter_laranjinha_payment.deeplink.RefundDeeplink
import com.flutter_laranjinha_payment.flutter_laranjinha_payment.deeplink.ReprintDeeplink
import com.flutter_laranjinha_payment.flutter_laranjinha_payment.services.DeviceInfo
import com.flutter_laranjinha_payment.flutter_laranjinha_payment.services.PrintService
import rede.smartrede.sdk.api.IRedeSdk

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import rede.smartrede.commons.callback.IPrinterCallback

class FlutterLaranjinhaPaymentPlugin :
    FlutterPlugin,
    MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private var redeSdk: IRedeSdk? = null
    private var binding: ActivityPluginBinding? = null
    private var resultScope: Result? = null
    private val paymentDeeplink: PaymentDeeplink = PaymentDeeplink()
    private val refundDeeplink: RefundDeeplink = RefundDeeplink()
    private val reprintDeeplink: ReprintDeeplink = ReprintDeeplink()

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_laranjinha_payment")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        resultScope = result

        if ((binding?.activity is Activity).not()) {
            resultScope!!.error("UNAVAILABLE", "Activity is not available", null)
            return
        }

        when (call.method) {
            "pay" -> {
                val bundle = Bundle().apply {
                    putInt("amount", call.argument<Int>("amount") ?: 0)
                    putString("paymentType", call.argument<String>("paymentType"))
                    putInt("installments", call.argument<Int>("installments") ?: 0)
                }
                starDeeplink(paymentDeeplink, bundle)
            }
            "refund" -> {
                val bundle = Bundle().apply {
                    putString("nsu", call.argument<String>("nsu"))
                }
                starDeeplink(refundDeeplink, bundle)
            }
            "print" -> {
                val listPrintContent: List<HashMap<String, Any?>>? = call.argument<List<HashMap<String, Any?>>>("printable_content")

                val bundleResult = PrintService().start(
                    redeSdk!!,
                    PrinterCallback(
                        onSuccess = {
                            resultScope?.success(mapOf(
                                "code" to "SUCCESS",
                                "data" to true
                            ))
                            resultScope = null
                        },
                        onFailure = { message ->
                            resultScope?.error("ERROR", message, null)
                            resultScope = null
                        }
                    ),
                    listPrintContent?.toBundleList(),
                    binding!!
                )

                if (bundleResult.getString("code") == "ERROR") {
                    val message: String = (bundleResult.getString("message") ?: "result error").toString()
                    resultScope?.error((bundleResult.getString("code") ?: "ERROR").toString(), message, null)
                    resultScope = null
                }
            }
            "reprint" -> {
                starDeeplink(reprintDeeplink, Bundle())
            }
            "getSerialNumberAndDeviceModel" -> {
                val deviceInfo = DeviceInfo().getSerialNumberAndDeviceModel()
                resultScope?.success(mapOf(
                    "code" to "SUCCESS",
                    "data" to deviceInfo
                ))
            }
            else ->  {
                resultScope?.error("ERROR", "Value of ", null)
            }
        }
    }

    private fun List<Map<String, Any?>>.toBundleList(): ArrayList<Bundle> {
        val bundleList = ArrayList<Bundle>()
        for (map in this) {
            bundleList.add(map.toBundle())
        }
        return bundleList
    }

    private fun Map<String, Any?>.toBundle(): Bundle {
        val bundle = Bundle()
        for ((key, value) in this) {
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                is Double -> bundle.putDouble(key, value)
                is Float -> bundle.putFloat(key, value)
                is Long -> bundle.putLong(key, value)
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    bundle.putBundle(key, (value as? Map<String, Any?>)?.toBundle())
                }
            }
        }
        return bundle
    }

    private fun starDeeplink(deeplink: Deeplink, bundle: Bundle) {
        val bundleStartDeeplink: Bundle = deeplink.startDeeplink(redeSdk!!, binding!!, bundle)
        val code: String = bundleStartDeeplink.getString("code") ?: "ERROR"

        if (code == "ERROR") {
            val message: String = (bundleStartDeeplink.getString("message") ?: "start deeplink error").toString()
            resultScope?.error(code, message, null)
            resultScope = null
        }
    }

    private fun sendResultData(paymentData: Map<String, Any?>) {
        if (paymentData["code"] == "SUCCESS" && paymentData["data"] != null) {
            resultScope?.success(paymentData)
            resultScope = null
        } else  {
            val message: String = (paymentData["message"] ?: "result error").toString()
            resultScope?.error((paymentData["code"] ?: "ERROR").toString(), message, null)
            resultScope = null
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(newBinding: ActivityPluginBinding) {
        redeSdk = IRedeSdk.newInstance(newBinding.activity)
        binding = newBinding

        binding?.addActivityResultListener { requestCode: Int, resultCode: Int, intent: Intent? ->
            if (resultCode == Activity.RESULT_OK) {
                var responseMap: Map<String, Any?> = mapOf()
                when (requestCode) {
                    PaymentDeeplink.REQUEST_CODE -> {
                        responseMap = paymentDeeplink.validateIntent(intent)
                    }
                    RefundDeeplink.REQUEST_CODE -> {
                        responseMap = refundDeeplink.validateIntent(intent)
                    }
                    ReprintDeeplink.REQUEST_CODE -> {
                        responseMap = reprintDeeplink.validateIntent(intent)
                    }
                }
                sendResultData(responseMap)
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                resultScope?.error("CANCELLED", "Operation was cancelled by user", null)
                resultScope = null
            }
            true
        }
    }

    override fun onDetachedFromActivityForConfigChanges() {
        redeSdk = null;
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
        redeSdk = IRedeSdk.newInstance(binding.activity)
    }

    override fun onDetachedFromActivity() {
        redeSdk = null;
    }
}

class PrinterCallback(
    private val onSuccess: () -> Unit,
    private val onFailure: ( String) -> Unit
) : IPrinterCallback {

    override fun onError(errorMessage: String) {
        onFailure(errorMessage)
    }

    override fun onCompleted() {
        onSuccess()
    }
}

