package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.SupplierProduct
import com.example.ui.OrderBookingViewModel

@Composable
fun SupplierPortalScreen(viewModel: OrderBookingViewModel, supplierId: String) {
    val products by viewModel.getProductsBySupplier(supplierId).collectAsState(initial = emptyList())
    var productName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Supplier Portal: Catalog", style = MaterialTheme.typography.headlineSmall)
        
        OutlinedTextField(value = productName, onValueChange = { productName = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Proposed Price") }, modifier = Modifier.fillMaxWidth())
        
        Button(onClick = { 
            viewModel.addSupplierProduct(supplierId, productName, price.toDoubleOrNull() ?: 0.0)
            productName = ""
            price = ""
        }) { Text("Upload Product") }

        Divider(modifier = Modifier.padding(vertical = 16.dp))
        
        Text("My Catalog", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(products) { product ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(product.productName)
                        Text("Price: ${product.price}")
                    }
                }
            }
        }
    }
}
