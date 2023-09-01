package com.signage.yomie

import android.app.admin.DevicePolicyManager
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.UserManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import coil.dispose
import com.bugsnag.android.Bugsnag
import com.novoda.merlin.Merlin
import com.signage.yomie.CMSPlayer.CMSPlayerVMFactory
import com.signage.yomie.CMSPlayer.network.ApiInterfaceCMSPlayer
import com.signage.yomie.CMSPlayer.network.RequestParameter
import com.signage.yomie.CMSPlayer.network.addplayer.AddCMSPlayerResponse
import com.signage.yomie.CMSPlayer.network.checkdevice.CheckDeviceResponse
import com.signage.yomie.CMSPlayer.network.timezone.ApiInterfaceTimeZone
import com.signage.yomie.CMSPlayer.network.timezone.Timezone
import com.signage.yomie.CMSPlayer.network.translation.En
import com.signage.yomie.CMSPlayer.network.translation.Fr
import com.signage.yomie.CMSPlayer.network.translation.Nl
import com.signage.yomie.CMSPlayer.network.translation.TranslationResponse
import com.signage.yomie.commons.AppConstants
import com.signage.yomie.commons.AppPreferences
import com.signage.yomie.database.YomieViewModel
import com.signage.yomie.databinding.ActivityRegistrationBinding
import com.signage.yomie.utils.AppUtils
import com.signage.yomie.utils.ErrorLog.ApiInterfaceErrorLog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RegistrationActivity : BaseActivity() {
    private var context: Context? = null
    private var deviceId: String? = null
    private var installationId: String? = null
    private var playerId: String? = null
    private var claimId: String? = null
    private lateinit var playerIdTv: TextView
    private lateinit var claimIdTv: TextView
    private lateinit var playerIdCopyBtn: ImageView
    private lateinit var claimIdCopyBtn: ImageView
    private lateinit var regMsgLL: LinearLayout
    private lateinit var regMsg: TextView
    private var binding: ActivityRegistrationBinding? = null
    private var en: En? = null
    private var fr: Fr? = null
    private var nl: Nl? = null
    var viewModel: YomieViewModel? = null
    var vmFactory: CMSPlayerVMFactory? = null
    var deviceManger: DevicePolicyManager? = null
    var compName: ComponentName? = null
    lateinit var merlin: Merlin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (preferences.getString(AppPreferences.KEY_PLAYER_ORIENTATION)) {
            AppConstants.ORIENTATION_LANDSCAPE -> requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            AppConstants.ORIENTATION_PORTRAIT -> requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            AppConstants.ORIENTATION_R_PORTRAIT -> requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        }

        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        val view = binding?.root
        setContentView(view)
        context = this@RegistrationActivity

        /*compName = ComponentName(applicationContext, AdminManager::class.java)
        deviceManger = getSystemService(
            DEVICE_POLICY_SERVICE
        ) as DevicePolicyManager

        if (deviceManger!!.isDeviceOwnerApp(packageName)) {
            setDefaultCosuPolicies(true)
            Log.e("ADMIN", "This application whitelisted")
        } else {
            Log.e("ADMIN", "This application not whitelisted")
        }*/

        initViews()
        initVariables()
        setUpClicks()
    }


    private fun getTranslation() {
        try {
            if (AppUtils.isOnline(this@RegistrationActivity)) {
                val apiTranslation = ApiInterfaceCMSPlayer.create().getTranslations()
                apiTranslation.enqueue(object : Callback<TranslationResponse> {
                    override fun onResponse(
                        call: Call<TranslationResponse>, response: Response<TranslationResponse>
                    ) {
                        if (response.body()?.ApiStatus == true) {
                            en = response.body()!!.Translations.en
                            fr = response.body()!!.Translations.fr
                            nl = response.body()!!.Translations.nl
                            viewModel?.setTranslation(en!!)
                            viewModel?.setTranslation(fr!!)
                            viewModel?.setTranslation(nl!!)
                            checkDevice()
                        }
                    }

                    override fun onFailure(call: Call<TranslationResponse>, t: Throwable) {
                        AppUtils.logError(
                            "${this::class.simpleName}\n${t.message}",
                            ApiInterfaceErrorLog.TYPE_ERROR
                        )
                    }

                })
            } else {
                openMediaActivity()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            AppUtils.logError(
                "${this::class.simpleName}\n${AppUtils.appendExp(ex)}",
                ApiInterfaceErrorLog.TYPE_ERROR
            )
        }
    }

    private fun checkDevice() {
        try {
            val apiInterface = ApiInterfaceCMSPlayer.create()
                .checkDevice(RequestParameter.CheckDevice(deviceId!!, installationId!!))
            apiInterface.enqueue(object : Callback<CheckDeviceResponse> {
                override fun onResponse(
                    call: Call<CheckDeviceResponse>, response: Response<CheckDeviceResponse>
                ) {
                    if (response.body()?.Status.equals("SUCCESS")) {
                        preferences.setInt(
                            AppPreferences.KEY_PLAYER_PKID, response.body()!!.Data.PlayerPKID
                        )
                        preferences.setString(
                            AppPreferences.KEY_USER_LANG, response.body()!!.Data.Language
                        )
                        openMediaActivity()
                    } else {
                        registerDevice()
                    }
                }

                override fun onFailure(call: Call<CheckDeviceResponse>, t: Throwable) {
                    AppUtils.logError(
                        "${this::class.simpleName}\n${t.message}", ApiInterfaceErrorLog.TYPE_ERROR
                    )
                }
            })
        } catch (ex: Exception) {
            ex.printStackTrace()
            AppUtils.logError(
                "${this::class.simpleName}\n${AppUtils.appendExp(ex)}",
                ApiInterfaceErrorLog.TYPE_ERROR
            )
        }
    }

    private fun registerDevice() {
        regMsgLL.visibility = View.VISIBLE
        when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
            AppConstants.English_table_name -> regMsg.text = en!!.registering_device
            AppConstants.French_table_name -> regMsg.text = fr!!.registering_device
            AppConstants.Dutch_table_name -> regMsg.text = nl!!.registering_device
            else -> regMsg.text = "Registering Device"
        }
        val apiInterface = ApiInterfaceCMSPlayer.create().addCMSPlayer(
            RequestParameter.AddDevice(
                deviceId!!, installationId!!, playerId!!, claimId!!
            )
        )
        apiInterface.enqueue(object : Callback<AddCMSPlayerResponse> {
            override fun onResponse(
                call: Call<AddCMSPlayerResponse>, response: Response<AddCMSPlayerResponse>
            ) {
                if (response.body()?.ApiStatus == true) {
                    preferences.setBoolean(AppPreferences.KEY_IS_REG, true)
                    preferences.setString(
                        AppPreferences.KEY_USER_LANG, response.body()!!.Data.Language
                    )
                    openMediaActivity()
                } else {
                    Toast.makeText(context, "Failed to registered", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AddCMSPlayerResponse>, t: Throwable) {
                AppUtils.logError(
                    "${this::class.simpleName}\n${t.message}", ApiInterfaceErrorLog.TYPE_ERROR
                )

            }
        })

    }

    override fun initViews() {
        playerIdTv = binding!!.registerPlayerId
        claimIdTv = binding!!.registerClaimId
        playerIdCopyBtn = binding!!.registerPlayerIdCopyBtn
        claimIdCopyBtn = binding!!.registerClaimIdCopyBtn
        regMsgLL = binding!!.registerMsgLL
        regMsg = binding!!.registerMsg
    }

    override fun initVariables() {
        regMsgLL.visibility = View.GONE
        vmFactory = CMSPlayerVMFactory(application)
        viewModel = ViewModelProvider(this, vmFactory!!)[YomieViewModel::class.java]
        val enT = viewModel!!.getEnTranslation()
        val frT = viewModel!!.getFrTranslation()
        val nlT = viewModel!!.getNlTranslation()
        if (enT.isNotEmpty()) {
            en = enT[0]
        }
        if (frT.isNotEmpty()) {
            fr = frT[0]
        }
        if (nlT.isNotEmpty()) {
            nl = nlT[0]
        }
        getOrGenerateId()
        merlin =
            Merlin.Builder().withConnectableCallbacks().withDisconnectableCallbacks().build(this)
        merlin.registerConnectable {
            checkCurrentDate()
        }
        checkCurrentDate()
        deleteCache(YomieApp.getContext())
    }

    override fun setUpClicks() {
        playerIdCopyBtn.setOnClickListener(onPlayerIdCopyClick())
        claimIdCopyBtn.setOnClickListener(onClaimIdCopyClick())
    }

    private fun getOrGenerateId() {
        if (!preferences.getString(AppPreferences.KEY_DEVICE_ID).isNullOrEmpty()) {
            deviceId = preferences.getString(AppPreferences.KEY_DEVICE_ID).toString()
            installationId = preferences.getString(AppPreferences.KEY_INSTALLATION_ID).toString()
            playerId = preferences.getString(AppPreferences.KEY_PLAYER_ID).toString()
            claimId = preferences.getString(AppPreferences.KEY_CLAIM_ID).toString()
            playerIdTv.text = playerId
            claimIdTv.text = claimId
            Bugsnag.setUser(playerId, "", "")
            Log.i("CMSVALUES", "DB\nD $deviceId\nI $installationId\nP $playerId\nC $claimId")
        } else {
            deviceId = AppUtils.getDeviceId(context)
            installationId = AppUtils.generateIds(4)
            playerId = AppUtils.generateIds(5)
            claimId = AppUtils.generateIds(4)
            preferences.storeIds(deviceId!!, installationId!!, playerId!!, claimId!!)
            playerIdTv.text = playerId
            claimIdTv.text = claimId
            Bugsnag.setUser(playerId, "", "")
            Log.i("CMSVALUES", "local\nD $deviceId\nI $installationId\nP $playerId\nC $claimId")
        }


    }


    fun openMediaActivity() {
        startActivity(Intent(context, MediaActivity::class.java))
        finish()
    }

    private fun onPlayerIdCopyClick(): View.OnClickListener {
        return View.OnClickListener {
            AppUtils.copyToClipboard(context, playerId!!)
        }
    }

    private fun onClaimIdCopyClick(): View.OnClickListener {
        return View.OnClickListener {
            AppUtils.copyToClipboard(context, claimId!!)
        }
    }

    private fun deleteCache(context: Context) {
        try {
            context.cacheDir.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setDefaultCosuPolicies(active: Boolean) {
        // Set user restrictions
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active)
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active)
        setUserRestriction(UserManager.DISALLOW_ADD_USER, active)
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, false)
        setUserRestriction(UserManager.DISALLOW_INSTALL_APPS, active)

        // Disable keyguard and status bar
        deviceManger!!.setKeyguardDisabled(compName!!, active)
        deviceManger!!.setStatusBarDisabled(compName!!, active)
        // Set system update policy

        if (active) {
            deviceManger!!.setSystemUpdatePolicy(
                compName!!, SystemUpdatePolicy.createWindowedInstallPolicy(60, 120)
            )
        } else {
            deviceManger!!.setSystemUpdatePolicy(compName!!, null); }
        // set this Activity as a lock task package
        deviceManger!!.setLockTaskPackages(
            compName!!, if (active) arrayOf(packageName) else arrayOf()
        )

        val intentFilter = IntentFilter(Intent.ACTION_MAIN)
        intentFilter.addCategory(Intent.CATEGORY_HOME)
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)

        if (active) {
            // set Cosu activity as home TrackerIntent receiver so that it is started        // on reboot
            deviceManger!!.addPersistentPreferredActivity(
                compName!!, intentFilter, ComponentName(packageName, AdminManager::class.java.name)
            )
        } else {
            deviceManger!!.clearPackagePersistentPreferredActivities(
                compName!!, packageName
            )
        }
    }

    private fun setUserRestriction(restriction: String, disallow: Boolean) {
        if (disallow) {
            deviceManger!!.addUserRestriction(compName!!, restriction)
        } else {
            deviceManger!!.clearUserRestriction(compName!!, restriction)
        }
    }

    private fun checkCurrentDate() {
        if (AppUtils.isOnline(this@RegistrationActivity)) {
            val apiTimeZone = ApiInterfaceTimeZone.create().getTimeDate()
            apiTimeZone.enqueue(object : Callback<Timezone> {
                override fun onResponse(
                    call: Call<Timezone>, response: Response<Timezone>
                ) {
                    if (response.body()?.status.equals("SUCCESS")) {
                        val d: List<String> = response.body()!!.data!!.dateTime!!.split(" ")
                        AppConstants.tempDate = d[0]
                        AppConstants.tempTime = d[1]
                        AppConstants.tempDateTime = response.body()!!.data!!.dateTime!!
                        if (preferences.getBoolean(AppPreferences.KEY_IS_REG)) {
                            openMediaActivity()
                        } else {
                            getTranslation()
                        }
                        AppUtils.logError("Box started", ApiInterfaceErrorLog.TYPE_INFO)
                    } else {
                        preferences.setBoolean(AppPreferences.KEY_IS_CACHE_TIME, true)
                        checkCurrentDate()
                    }
                }

                override fun onFailure(call: Call<Timezone>, t: Throwable) {
                    preferences.setBoolean(AppPreferences.KEY_IS_CACHE_TIME, true)
                    checkCurrentDate()
                }
            })
        } else {
            preferences.setBoolean(AppPreferences.KEY_IS_CACHE_TIME, true)
            openMediaActivity()
            regMsgLL.visibility = View.VISIBLE
            binding?.registerPb?.visibility = View.GONE
            when (preferences.getString(AppPreferences.KEY_USER_LANG)) {
                AppConstants.English_table_name -> regMsg.text = en!!.no_internet
                AppConstants.French_table_name -> regMsg.text = fr!!.no_internet
                AppConstants.Dutch_table_name -> regMsg.text = nl!!.no_internet
                else -> regMsg.text = resources.getString(R.string.no_internet)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::merlin.isInitialized)
            merlin.bind()
    }

    override fun onDestroy() {
        if (::merlin.isInitialized)
            merlin.unbind()
        deviceId = null
        installationId = null
        playerId = null
        claimId = null
        binding = null
        viewModel = null
        vmFactory = null
        playerIdCopyBtn.dispose()
        claimIdCopyBtn.dispose()
        super.onDestroy()
    }
}


