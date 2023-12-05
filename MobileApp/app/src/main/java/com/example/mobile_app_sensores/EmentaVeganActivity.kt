package com.example.mobile_app_sensores

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_ementa_vegan.*

class EmentaVeganActivity : AppCompatActivity() {

    var data = ""
    var pessoas_cantina = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        setContentView(R.layout.activity_ementa_vegan)

        data = intent.getStringExtra("data").toString()
        pessoas_cantina = intent.getStringExtra("pessoas_cantina").toString()

        ac1_vegan.text = intent.getStringExtra("ac1_vegan").toString()
        ac2_vegan.text = intent.getStringExtra("ac2_vegan").toString()
        acucar_vegan.text = intent.getStringExtra("acucar_vegan").toString()
        energia_vegan.text = intent.getStringExtra("energia_vegan").toString()
        fibras_vegan.text = intent.getStringExtra("fibras_vegan").toString()
        hidratos_vegan.text = intent.getStringExtra("hidratos_vegan").toString()
        lipidos_vegan.text = intent.getStringExtra("lipidos_vegan").toString()
        saturados_vegan.text = intent.getStringExtra("saturados_vegan").toString()
        prato_vegan.text = intent.getStringExtra("prato_vegan").toString()
        proteina_vegan.text = intent.getStringExtra("proteina_vegan").toString()
        sal_vegan.text = intent.getStringExtra("sal_vegan").toString()
        sopa_vegan.text = intent.getStringExtra("sopa_vegan").toString()

        goBack()
    }

    private fun goBack(){
        ementa_vegan_back_btn.setOnClickListener {
            val atividade = Intent(this, MainActivity::class.java)
            atividade.putExtra("data", data)
            atividade.putExtra("pessoas_cantina", pessoas_cantina)
            startActivity(atividade)
        }
    }
}
