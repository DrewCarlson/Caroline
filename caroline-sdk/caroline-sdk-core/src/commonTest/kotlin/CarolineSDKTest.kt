package cloud.caroline

import cloud.caroline.core.CarolineSDK
import kotlin.test.Test

class CarolineSDKTest {

    @Test
    fun test() {
        CarolineSDK {
            serverUrl = "test"
            apiKey = ""
        }
    }
}