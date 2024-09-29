package dji.simpleV5

import android.app.Application
import androidx.lifecycle.*
import dji.simpleV5.dji_sdk5_utils.BaseMainActivityVm
import dji.simpleV5.dji_sdk5_utils.MSDKInfoVm
import dji.simpleV5.dji_sdk5_utils.MSDKManagerVM
import dji.v5.common.utils.GeoidManager
import dji.v5.utils.common.LogUtils
import dji.v5.utils.common.StringUtils
import dji.v5.ux.core.communication.DefaultGlobalPreferences
import dji.v5.ux.core.communication.GlobalPreferencesManager
import dji.v5.ux.core.util.UxSharedPreferencesUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConnectionViewModel(application: Application) : AndroidViewModel(application) {

    private val tag: String = LogUtils.getTag(this)

    // LiveData properties to communicate with the Activity
    private val _msdkInfoText = MutableLiveData<String>()
    val msdkInfoText: LiveData<String> = _msdkInfoText

    private val _registrationStatusText = MutableLiveData<String>()
    val registrationStatusText: LiveData<String> = _registrationStatusText

    private val _showToastMessage = MutableLiveData<String>()
    val showToastMessage: LiveData<String> = _showToastMessage

    private val _isUxReady = MutableLiveData<Boolean>()
    val isUxReady: LiveData<Boolean> = _isUxReady

    private val baseMainActivityVm: BaseMainActivityVm = BaseMainActivityVm()
    private val msdkInfoVm: MSDKInfoVm = MSDKInfoVm()
    private val msdkManagerVM: MSDKManagerVM = MSDKManagerVM()

    init {
        initMSDKInfo()
        observeSDKManager()
    }

    private fun initMSDKInfo() {
        // Observe msdkInfoVm.msdkInfo
        msdkInfoVm.msdkInfo.observeForever { msdkInfo ->
            val summaryText = "SDK Version: ${msdkInfo.SDKVersion} ${msdkInfo.buildVer}" +
                    "\nProduct Name: ${msdkInfo.productType?.name}" +
                    "\nPackage Product Category: ${msdkInfo.packageProductCategory}" +
                    "\nIs SDK Debug: ${msdkInfo.isDebug}"
            _msdkInfoText.postValue(summaryText)
        }
    }

    private fun observeSDKManager() {
        msdkManagerVM.lvRegisterState.observeForever { resultPair ->
            if (resultPair.first) {
                // Register success
                _showToastMessage.postValue("Register Success")
                val statusText = StringUtils.getResStr(getApplication(), R.string.registered)
                _registrationStatusText.postValue(StringUtils.getResStr(R.string.registration_status, statusText))
                msdkInfoVm.initListener()
                // Prepare UX Activity after delay
                viewModelScope.launch {
                    delay(5000)
                    prepareUxActivity()
                }
            } else {
                // Register failure
                _showToastMessage.postValue("Register Failure: ${resultPair.second}")
                val statusText = StringUtils.getResStr(getApplication(), R.string.unregistered)
                _registrationStatusText.postValue(StringUtils.getResStr(R.string.registration_status, statusText))
            }
        }

        msdkManagerVM.lvProductConnectionState.observeForever {
            _showToastMessage.postValue("Product: ${it.second}, ConnectionState: ${it.first}")
        }

        msdkManagerVM.lvProductChanges.observeForever {
            _showToastMessage.postValue("Product: $it Changed")
        }

        msdkManagerVM.lvInitProcess.observeForever {
            _showToastMessage.postValue("Init Process event: ${it.first.name}")
        }

        msdkManagerVM.lvDBDownloadProgress.observeForever {
            _showToastMessage.postValue("Database Download Progress: ${it.first}/${it.second}")
        }
    }

    private fun prepareUxActivity() {
        UxSharedPreferencesUtil.initialize(getApplication())
        GlobalPreferencesManager.initialize(DefaultGlobalPreferences(getApplication()))
        GeoidManager.getInstance().init(getApplication())
        _isUxReady.postValue(true)
    }

    fun doPairing(onComplete: (String) -> Unit) {
        baseMainActivityVm.doPairing(onComplete)
    }
}
