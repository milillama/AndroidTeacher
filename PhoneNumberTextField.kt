import android.content.Context
import android.telephony.PhoneNumberFormattingTextWatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.Locale

@Composable
fun PhoneNumberTextField(
    userSettings: UserSettings,
    phoneNumber: String,
    countryCode: String,
    onPhoneNumberChange: (String) -> Unit,
    onCountryCodeChange: (String) -> Unit
) {
    var showCountries by remember { mutableStateOf(false) }
    var countryFlag by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (countryCode.isEmpty()) "🇺🇸 +1" else "$countryFlag +$countryCode",
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.2f))
                    .padding(8.dp)
                    .clickable { showCountries = true }
                    .padding(horizontal = 8.dp, vertical = 16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            TextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                placeholder = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                singleLine = true
            )
        }

        if (showCountries) {
            CountryCodesDialog(
                onCountryCodeChange = {
                    onCountryCodeChange(it)
                    countryFlag = getFlagFromCountryCode(it)
                    showCountries = false
                },
                onDismiss = { showCountries = false }
            )
        }
    }
}

@Composable
fun CountryCodesDialog(
    onCountryCodeChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val countryDictionary = getCountryDictionary()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Your Country",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column {
                    countryDictionary.toList().sortedBy { it.second }.forEach { (countryCode, dialCode) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCountryCodeChange(dialCode) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${getFlagFromCountryCode(countryCode)} ${getCountryName(countryCode)}",
                                fontSize = 16.sp
                            )
                            Text(
                                text = "+$dialCode",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getCountryDictionary(): Map<String, String> {
    return mapOf(
        "AF" to "93", "AL" to "355", "DZ" to "213", "US" to "1",
        "AD" to "376", "AO" to "244", "AI" to "1", "AG" to "1", "AR" to "54",
        "AM" to "374", "AW" to "297", "AU" to "61", "AT" to "43", "AZ" to "994",
        "BS" to "1", "BH" to "973", "BD" to "880", "BB" to "1", "BY" to "375",
        "BE" to "32", "BZ" to "501", "BJ" to "229", "BM" to "1", "BT" to "975",
        "BA" to "387", "BW" to "267", "BR" to "55", "IO" to "246", "BG" to "359",
        "BF" to "226", "BI" to "257", "KH" to "855", "CM" to "237", "CA" to "1",
        "CV" to "238", "KY" to "345", "CF" to "236", "TD" to "235", "CL" to "56", "CN" to "86",
        "CX" to "61", "CO" to "57", "KM" to "269", "CG" to "242", "CK" to "682", "CR" to "506",
        "HR" to "385", "CU" to "53", "CY" to "537", "CZ" to "420", "DK" to "45", "DJ" to "253",
        "DM" to "1", "DO" to "1", "EC" to "593", "EG" to "20", "SV" to "503", "GQ" to "240",
        "ER" to "291", "EE" to "372", "ET" to "251", "FO" to "298", "FJ" to "679", "FI" to "358",
        "FR" to "33", "GF" to "594", "PF" to "689", "GA" to "241", "GM" to "220", "GE" to "995",
        "DE" to "49", "GH" to "233", "GI" to "350", "GR" to "30", "GL" to "299", "GD" to "1",
        "GP" to "590", "GU" to "1", "GT" to "502", "GN" to "224", "GW" to "245", "GY" to "595", "HT" to "509",
        "HN" to "504", "HU" to "36", "IS" to "354", "IN" to "91", "ID" to "62", "IQ" to "964",
        "IE" to "353", "IL" to "972", "IT" to "39", "JM" to "1", "JP" to "81", "JO" to "962",
        "KZ" to "77", "KE" to "254", "KI" to "686", "KW" to "965", "KG" to "996", "LV" to "371",
        "LB" to "961", "LS" to "266", "LR" to "231", "LI" to "423", "LT" to "370", "LU" to "352",
        "MG" to "261", "MW" to "265", "MY" to "60", "MV" to "960", "ML" to "223",
        "MT" to "356", "MH" to "692", "MQ" to "596", "MR" to "222", "MU" to "230", "YT" to "262",
        "MX" to "52", "MC" to "377", "MN" to "976", "ME" to "382", "MS" to "1", "MA" to "212",
        "MM" to "95", "NA" to "264", "NR" to "674", "NP" to "977", "NL" to "31", "NC" to "687",
        "NZ" to "64", "NI" to "505", "NE" to "227", "NG" to "234", "NU" to "683",
        "NF" to "672", "MP" to "1", "NO" to "47", "OM" to "968", "PK" to "92", "PW" to "680",
        "PA" to "507", "PG" to "675", "PY" to "595", "PE" to "51", "PH" to "63", "PL" to "48",
        "PT" to "351", "PR" to "1", "QA" to "974", "RO" to "40", "RW" to "250", "WS" to "685",
        "SM" to "378", "SA" to "966", "SN" to "221", "RS" to "381", "SC" to "248",
        "SL" to "232", "SG" to "65", "SK" to "421", "SI" to "386", "SB" to "677",
        "ZA" to "27", "GS" to "500", "ES" to "34", "LK" to "94", "SD" to "249", "SR" to "597",
        "SZ" to "268", "SE" to "46", "CH" to "41", "TJ" to "992", "TH" to "66", "TG" to "228",
        "TK" to "690", "TO" to "676", "TT" to "1", "TN" to "216", "TR" to "90",
        "TM" to "993", "TC" to "1", "TV" to "688", "UG" to "256", "UA" to "380",
        "AE" to "971", "GB" to "44", "AS" to "1", "UY" to "598", "UZ" to "998",
        "VU" to "678", "WF" to "681", "YE" to "967", "ZM" to "260",
        "ZW" to "263", "BO" to "591", "BN" to "673", "CC" to "61",
        "CD" to "243", "CI" to "225", "FK" to "500", "GG" to "44",
        "VA" to "379", "HK" to "852", "IR" to "98", "IM" to "44",
        "JE" to "44", "KP" to "850", "KR" to "82", "LA" to "856",
        "LY" to "218", "MO" to "853", "MK" to "389", "FM" to "691",
        "MD" to "373", "MZ" to "258", "PS" to "970", "PN" to "872",
        "RE" to "262", "RU" to "7", "BL" to "590", "SH" to "290", "KN" to "1",
        "LC" to "1", "MF" to "590", "PM" to "508", "VC" to "1", "ST" to "239",
        "SO" to "252", "SJ" to "47", "SY" to "963", "TW" to "886", "TZ" to "255",
        "TL" to "670", "VE" to "58", "VN" to "84", "VG" to "284", "VI" to "340"
    )
}

fun getCountryName(countryCode: String): String? {
    return Locale("", countryCode).displayCountry
}

fun getFlagFromCountryCode(countryCode: String): String {
    val base = 0x1F1E6 - 'A'.code
    return countryCode.uppercase(Locale.getDefault()).map { char ->
        (base + char.code).toChar()
    }.joinToString("") { it.toString() }
}
