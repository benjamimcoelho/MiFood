package com.example.mobile_app_sensores

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_ementa.*
import java.lang.Exception

class EmentaActivity : AppCompatActivity() {

    var data = ""
    var pessoas_cantina = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        setContentView(R.layout.activity_ementa)

        data = intent.getStringExtra("data").toString()
        pessoas_cantina = intent.getStringExtra("pessoas_cantina").toString()

        //Obter valores enviados da main activity
        ac1.text = intent.getStringExtra("ac1").toString()
        ac2.text = intent.getStringExtra("ac2").toString()
        acucar.text = intent.getStringExtra("acucar").toString()
        energia.text = intent.getStringExtra("energia").toString()
        fibras.text = intent.getStringExtra("fibras").toString()
        hidratos.text = intent.getStringExtra("hidratos").toString()
        lipidos.text = intent.getStringExtra("lipidos").toString()
        saturados.text = intent.getStringExtra("saturados").toString()
        prato.text = intent.getStringExtra("prato").toString()
        proteina.text = intent.getStringExtra("proteina").toString()
        sal.text = intent.getStringExtra("sal").toString()
        sopa.text = intent.getStringExtra("sopa").toString()

        goBack()
    }

    private fun goBack(){
        ementa_back_btn.setOnClickListener {
            val atividade = Intent(this, MainActivity::class.java)
            if (data != "" && pessoas_cantina != "") {
                atividade.putExtra("data", data)
                atividade.putExtra("pessoas_cantina", pessoas_cantina)
            }
            startActivity(atividade)
        }
    }
}
