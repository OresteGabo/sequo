package dev.orestegabo.sequo.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.catalog.*
import dev.orestegabo.sequo.feature.settings.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun TestOrderControls(id:String,confirmed:Boolean,status:String,refresh:Int,confirm:suspend(String)->CheckoutSummary,
    loadDispatch:suspend(String)->TestDispatchState,assign:suspend(String,String)->TestDispatchState,onChanged:()->Unit) {
    val french=LocalAppLanguage.current==AppLanguage.French
    fun t(en:String,fr:String)=if(french)fr else en
    var state by remember(id){mutableStateOf<TestDispatchState?>(null)}
    var rider by remember(id){mutableStateOf("demo-v1-rider-1")}
    var busy by remember {mutableStateOf(false)}
    var error by remember(id){mutableStateOf(false)}
    var retry by remember {mutableStateOf(0)}
    val scope=rememberCoroutineScope()
    LaunchedEffect(id,confirmed,refresh,retry){if(confirmed)try{state=loadDispatch(id);error=false}catch(e:CancellationException){throw e}catch(_:Exception){error=true}}
    Surface(shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.tertiaryContainer.copy(alpha=.5f)) {
        Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text(t("TEST ORDER · no money moved","COMMANDE DE TEST · aucun paiement"),style=MaterialTheme.typography.labelMedium,fontWeight=FontWeight.Bold)
            if(!confirmed && status=="AWAITING_WEBHOOK") {
                Text(t("Confirm this test payment to send the order to the test vendor. No wallet will be charged.","Confirmez ce paiement de test pour envoyer la commande au vendeur de test. Aucun portefeuille ne sera debite."),style=MaterialTheme.typography.bodySmall)
                Button(enabled=!busy,onClick={busy=true;error=false;scope.launch{try{confirm(id);onChanged()}catch(e:CancellationException){throw e}catch(_:Exception){error=true}finally{busy=false}}}) {
                    Text(t("Confirm test payment","Confirmer le paiement de test"))
                }
            }
            if(confirmed) state?.let { s ->
                Text(s.message,style=MaterialTheme.typography.bodySmall)
                if(s.ready && !s.assigned) {
                    s.riders.chunked(2).forEach { pair -> Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        pair.forEach {r->FilterChip(selected=rider==r.id,onClick={rider=r.id},enabled=!busy,label={Text(r.name)})}
                    } }
                    Button(enabled=!busy,onClick={busy=true;error=false;scope.launch{try{state=assign(id,rider);onChanged()}catch(e:CancellationException){throw e}catch(_:Exception){error=true}finally{busy=false}}}) {
                        Text(t("Assign test rider","Affecter le livreur de test"))
                    }
                }
                TextButton(enabled=!busy,onClick={retry++}){Text(t("Refresh test dispatch","Actualiser l'affectation"))}
            }
            if(error){Text(t("Could not finish. Refresh or choose an available test rider.","Impossible de terminer. Actualisez ou choisissez un livreur de test disponible."),color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)
                TextButton(onClick={retry++},enabled=!busy){Text(t("Retry","Reessayer"))}}
        }
    }
}
