package br.com.daniel.estudomqtt.mqtt.protocols

interface UIUpdater {
    fun resetUIWithConnetion(status: Boolean)
    fun updateStatusView(status: String)
    fun update(message: String)
}