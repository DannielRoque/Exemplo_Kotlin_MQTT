package br.com.daniel.estudomqtt.mqtt

import android.content.Context
import android.util.Log
import br.com.daniel.estudomqtt.mqtt.protocols.UIUpdater
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.*

class MQTTmanager(
    val connection: MQTTConnectionParams,
    val context: Context,
    val uiUpdate: UIUpdater
) {

    private var client = MqttAndroidClient(
        context, connection.host, connection.clientId + id(context)
    )
    private var uniqueID: String? = null
    private var prefe_unique_id = "PREF_UNIQUE_ID"

    init {
        client.setCallback(object : MqttCallbackExtended {

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                uiUpdate?.resetUIWithConnetion(true)
            }

            override fun connectionLost(cause: Throwable?) {
                uiUpdate?.resetUIWithConnetion(false)
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                uiUpdate?.update(message.toString())
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {}

        })
    }

    fun connect() {
        val mqttConnectionOptions = MqttConnectOptions()
        mqttConnectionOptions.isAutomaticReconnect = true
        mqttConnectionOptions.isCleanSession = false

        try {
            var params = this.connection
            client.connect(mqttConnectionOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 100
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    client.setBufferOpts(disconnectedBufferOptions)
                    subscribe(params.topic)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(
                        "MQTTmanager",
                        "Falha para se conectar ${params.host + exception.toString()}"
                    )
                }

            })
        } catch (e: MqttException) {
            Log.e("MQTTmanager", e.stackTraceToString())
        }
    }

    fun disconect() {

        try {

            client.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    uiUpdate?.resetUIWithConnetion(false)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    uiUpdate?.resetUIWithConnetion(false)
                }

            })
        } catch (e: MqttException) {
            Log.e("MQTTmanager", "Exception disconnect  ${e.printStackTrace()}")
        }
    }

    // increver no topico
    private fun subscribe(topic: String) {
        try {
            client.subscribe(topic, 0, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.e("MQTTmanager", "incricao")
                    uiUpdate?.updateStatusView("Subscribe to topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    uiUpdate?.updateStatusView("Subscribe Fail to topic")
                }

            })
        } catch (e: MqttException) {
            Log.e("MQTTmanager", "error subscribe ${e.printStackTrace()}")
        }
    }

    private fun unSubscribe(topic: String) {
        try {
            client.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    uiUpdate?.updateStatusView("UnSubscribe to topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    uiUpdate?.updateStatusView("UnSubscribe to topic Fail! ")
                }

            })
        } catch (e: MqttException) {
            Log.e("MQTTmanager", "error subscribe ${e.printStackTrace()}")
        }
    }

    fun publish(message: String) {
        try {

            var msg = "Android says: $message"
            client.publish(
                this.connection.topic,
                msg.toByteArray(),
                0,
                false,
                null,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.e("MQTTmanager", "publish sucess")
                        uiUpdate?.update("Publish to topic")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.e("MQTTmanager", "publish failed")
                        uiUpdate?.update("Publish to topic Failed ")
                    }

                })
        } catch (e: MqttException) {
            Log.e("MQTTmanager", "publish exception ${e.printStackTrace()}")
        }
    }

    fun id(context: Context): String {
        if (uniqueID == null) {
            val sharedPrefs = context.getSharedPreferences(prefe_unique_id, Context.MODE_PRIVATE)
            uniqueID = sharedPrefs.getString(prefe_unique_id, null)
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString()
                val editor = sharedPrefs.edit()
                editor.putString(prefe_unique_id, uniqueID)
                editor.commit()
            }
        }
        return uniqueID!!
    }
}

data class MQTTConnectionParams(
    val clientId: String, val host: String, val topic: String,
    val userName: String, val password: String
)