package dev.orestegabo.sequo.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.catalog.DemoJourneyState
import dev.orestegabo.sequo.core.network.SequoApiException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun DemoOrderControls(checkoutId:String, refresh:Int, load:suspend(String)->DemoJourneyState,
    advance:suspend(String,String,String?)->DemoJourneyState,onChanged:()->Unit) {
    var state by remember(checkoutId) { mutableStateOf<DemoJourneyState?>(null) }
    var error by remember(checkoutId) { mutableStateOf<String?>(null) }
    var rider by remember(checkoutId) { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var retry by remember { mutableStateOf(0) }
    val scope=rememberCoroutineScope()
    LaunchedEffect(checkoutId, refresh, retry) {
        error=null
        try { state=load(checkoutId); if(rider==null) rider=state?.riders?.firstOrNull()?.id }
        catch(e:CancellationException){throw e}
        catch(_:Exception){error="Could not load demo controls. Retry."}
    }
    Surface(color=MaterialTheme.colorScheme.tertiaryContainer.copy(alpha=.5f),shape=RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("DEMO · no real payment or dispatch",style=MaterialTheme.typography.labelMedium,fontWeight=FontWeight.Bold)
            state?.let { current ->
                Text(current.title,style=MaterialTheme.typography.titleMedium)
                current.riderName?.let { Text(it,style=MaterialTheme.typography.bodySmall) }
                if(current.stage=="ASSIGN") {
                    Text("Choose a test rider",style=MaterialTheme.typography.bodySmall)
                    current.riders.chunked(2).forEach { pair -> Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        pair.forEach { option -> FilterChip(selected=rider==option.id,onClick={rider=option.id},label={Text(option.name)},enabled=!busy) }
                    } }
                }
                current.nextAction?.let { action ->
                    Button(onClick={
                        busy=true;error=null
                        scope.launch {
                            try { state=advance(checkoutId,action,rider);onChanged() }
                            catch(e:CancellationException){throw e}
                            catch(e:SequoApiException){error=if(e.statusCode==409) "This step changed. Refresh and try again." else "This step could not finish. Retry or choose another available rider."}
                            catch(_:Exception){error="This step could not finish. Retry."}
                            finally{busy=false}
                        }
                    },enabled=!busy,modifier=Modifier.fillMaxWidth()) { Text(if(busy) "Updating…" else current.nextLabel ?: action) }
                }
                if(current.stage=="DONE") Text("Journey complete. You can place another demo order.",style=MaterialTheme.typography.bodySmall)
            }
            error?.let { Text(it,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall);TextButton(onClick={retry++},enabled=!busy){Text("Refresh demo step")} }
        }
    }
}
