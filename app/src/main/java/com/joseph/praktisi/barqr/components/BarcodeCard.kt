package com.joseph.praktisi.barqr.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.common.Barcode

@Composable
fun BarcodeCard(barcode: Barcode) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Barcode Type: ${getBarcodeType(barcode)}")

            when (barcode.valueType) {
                Barcode.TYPE_WIFI -> {
                    barcode.wifi?.let { wifi ->
                        Text(text = "SSID: ${wifi.ssid}")
                        Text(text = "Password: ${wifi.password}")
                        Text(text = "Encryption Type: ${wifi.encryptionType}")
                    }
                }
                Barcode.TYPE_URL -> {
                    barcode.url?.let { url ->
                        Text(text = "Title: ${url.title}")
                        Text(text = "URL: ${url.url}")
                    }
                }
                Barcode.TYPE_TEXT -> {
                    Text(text = "Text: ${barcode.displayValue}")
                }
                Barcode.TYPE_CONTACT_INFO -> {
                    barcode.contactInfo?.let { contact ->
                        Text(text = "Name: ${contact.name?.formattedName}")
                        Text(text = "Organization: ${contact.organization}")
                        contact.phones.forEach { phone ->
                            Text(text = "Phone: ${phone.number}")
                        }
                        contact.emails.forEach { email ->
                            Text(text = "Email: ${email.address}")
                        }
                    }
                }
                Barcode.TYPE_EMAIL -> {
                    barcode.email?.let { email ->
                        Text(text = "Address: ${email.address}")
                        Text(text = "Subject: ${email.subject}")
                        Text(text = "Body: ${email.body}")
                    }
                }
                Barcode.TYPE_PHONE -> {
                    barcode.phone?.let { phone ->
                        Text(text = "Phone Number: ${phone.number}")
                        Text(text = "Type: ${getPhoneType(phone.type)}")
                    }
                }
                Barcode.TYPE_SMS -> {
                    barcode.sms?.let { sms ->
                        Text(text = "Phone Number: ${sms.phoneNumber}")
                        Text(text = "Message: ${sms.message}")
                    }
                }
                Barcode.TYPE_GEO -> {
                    barcode.geoPoint?.let { geo ->
                        Text(text = "Latitude: ${geo.lat}")
                        Text(text = "Longitude: ${geo.lng}")
                    }
                }
                Barcode.TYPE_CALENDAR_EVENT -> {
                    barcode.calendarEvent?.let { event ->
                        Text(text = "Summary: ${event.summary}")
                        Text(text = "Location: ${event.location}")
                        Text(text = "Start: ${event.start}")
                        Text(text = "End: ${event.end}")
                    }
                }
                Barcode.TYPE_DRIVER_LICENSE -> {
                    barcode.driverLicense?.let { license ->
                        Text(text = "License Number: ${license.licenseNumber}")
                        Text(text = "First Name: ${license.firstName}")
                        Text(text = "Last Name: ${license.lastName}")
                        Text(text = "Gender: ${license.gender}")
                        Text(text = "Birth Date: ${license.birthDate}")
                        Text(text = "Issue Date: ${license.issueDate}")
                        Text(text = "Expiry Date: ${license.expiryDate}")
                    }
                }
                else -> {
                    // Show the raw display value for unknown barcode types
                    Text(text = "Unknown Barcode Type")
                    barcode.displayValue?.let { value ->
                        Text(text = "Read Value: $value")
                    }
                }
            }
        }
    }
}

// Helper function to determine the barcode type
fun getBarcodeType(barcode: Barcode): String {
    return when (barcode.valueType) {
        Barcode.TYPE_WIFI -> "Wi-Fi"
        Barcode.TYPE_URL -> "URL"
        Barcode.TYPE_TEXT -> "Text"
        Barcode.TYPE_CONTACT_INFO -> "Contact Info"
        Barcode.TYPE_EMAIL -> "Email"
        Barcode.TYPE_PHONE -> "Phone"
        Barcode.TYPE_SMS -> "SMS"
        Barcode.TYPE_GEO -> "Geo Location"
        Barcode.TYPE_CALENDAR_EVENT -> "Calendar Event"
        Barcode.TYPE_DRIVER_LICENSE -> "Driver License"
        else -> "Unknown"
    }
}

// Helper function to get the phone type as a string
fun getPhoneType(type: Int): String {
    return when (type) {
        Barcode.Phone.TYPE_HOME -> "Home"
        Barcode.Phone.TYPE_WORK -> "Work"
        Barcode.Phone.TYPE_MOBILE -> "Mobile"
        else -> "Unknown"
    }
}
