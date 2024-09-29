package dji.simpleV5

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


interface IMSDKManager{

    val registrationStatus: MutableLiveData<Pair<Boolean, String>>
    val productConnectionState: MutableLiveData<Pair<Boolean, String>>

}