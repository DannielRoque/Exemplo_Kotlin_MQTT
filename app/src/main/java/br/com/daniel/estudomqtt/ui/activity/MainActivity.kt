package br.com.daniel.estudomqtt.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import br.com.daniel.estudomqtt.R
import br.com.daniel.estudomqtt.mqtt.MQTTConnectionParams
import br.com.daniel.estudomqtt.mqtt.MQTTmanager
import br.com.daniel.estudomqtt.mqtt.protocols.UIUpdater
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), UIUpdater {

    var mqttManager : MQTTmanager? = null

    //metodos da interface

    override fun resetUIWithConnetion(status : Boolean){
        mqtt_address.isEnabled = !status
        mqtt_topic.isEnabled = !status
        mqtt_message.isEnabled = status
        mqtt_connect.isEnabled = !status
        mqtt_send.isEnabled = status

        //atualiza status textview
        if (status){
            updateStatusView("Conectado!")
        }else{
            updateStatusView("Desconectado")
        }
    }

    override fun updateStatusView(status: String) {
        mqtt_status.text = status
    }

    override fun update(message: String) {
        val text = mqtt_history.text.toString()
        var newText = " $text \n $message"

        mqtt_history.setText(newText)
        mqtt_history.setSelection(mqtt_history.text.length)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resetUIWithConnetion(false)
        buttons()
    }

    fun buttons(){
        mqtt_send.setOnClickListener { sendMessage() }
        mqtt_connect.setOnClickListener { connect() }
    }

    fun connect(){
        if (!(mqtt_address.text.isNullOrEmpty() && mqtt_topic.text.isNullOrEmpty())){
            var host = "tcp://${mqtt_address.text}:1883"
            var topic = mqtt_topic.text.toString()
            var connectParam = MQTTConnectionParams("estudomqtt", host, topic, "", "")
            mqttManager = MQTTmanager(connectParam, applicationContext, this)
            mqttManager?.connect()
        }else{
            updateStatusView("Por favor insira os dados corretamente")
        }
    }

    fun sendMessage(){
        mqttManager?.publish(mqtt_message.text.toString())
        mqtt_message.setText("")
    }
}