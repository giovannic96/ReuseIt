package it.polito.mad.mhackeroni.model

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import org.intellij.lang.annotations.Language
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import java.lang.Exception
import java.util.*

class Item(var id: String, var name:String, var price:Double, var desc:String, var category:String, var subcategory: String, var expiryDate:String, var location:String, var condition: String, var image: String?, var buyer : String?, var hasFeedback: Boolean, var rating: Int, var user : String = "null" , var state : ItemState = ItemState.AVAILABLE, var lat: Double? = null, var lng: Double? = null) : Serializable {
    constructor() : this("", "", 0.0, "", "", "", "", "", "", "", "",  false, 0, "")

    companion object Factory {

        fun fromStringJSON(jsonString: String): Item? {
            var jsonObject: JSONObject? = null
            try {
                jsonObject = JSONObject(jsonString)
            } catch (e: JSONException) {
                e.printStackTrace()
            } finally {
                return if (jsonObject != null)
                    fromJSON(
                        jsonObject
                    )
                else
                    null
            }
        }

        private fun fromJSON(jsonObject: JSONObject): Item {
            val item =  Item(
                jsonObject.getString("id"),
                jsonObject.getString("name"),
                jsonObject.getDouble("price"),
                jsonObject.getString("desc"),
                jsonObject.getString("category"),
                jsonObject.getString("subcategory"),
                jsonObject.getString("expiryDate"),
                jsonObject.getString("location"),
                jsonObject.getString("condition"),
                jsonObject.getString("image"),
                jsonObject.getString("buyer"),
                jsonObject.getBoolean("hasFeedback"),
                jsonObject.getInt("rating")
            )

            try{
                val lat = jsonObject.getDouble("lat")
                val lng = jsonObject.getDouble("lng")
                item.lat = lat
                item.lng = lng
            } catch (e: Exception){
                item.lat = null
                item.lng = null
            }

            return item
        }

        fun toJSON(item: Item): JSONObject {
            val obj: JSONObject = JSONObject()
            try {
                obj.put("id", item.id)
                obj.put("name", item.name)
                obj.put("price", item.price)
                obj.put("desc", item.desc)
                obj.put("category", item.category)
                obj.put("subcategory", item.subcategory)
                obj.put("expiryDate", item.expiryDate)
                obj.put("location", item.location)
                obj.put("condition", item.condition)
                obj.put("buyer", item.buyer)
                obj.put("hasFeedback", item.hasFeedback)
                obj.put("rating", item.rating)
                obj.put("lat", item.lat ?: "")
                obj.put("lng", item.lng ?: "")


                if(item.image.isNullOrEmpty())
                    obj.put("image", "")
                else
                    obj.put("image", item.image)

            } catch (e: JSONException) {
                e.printStackTrace()
            } finally {
                return obj
            }
        }

        fun localize(item : Item, toIt : Boolean = false) : Item{
            val localizedItem : Item = item
            val lang = Locale.getDefault().displayLanguage

            val catMap = mapOf(
                "Arti & Mestieri" to "Arts & Crafts",
                "Sport & attività all'aperto" to "Sports and Outdoors",
                "Bambini" to "Baby",
                "Donna" to "Women's fashion",
                "Uomo" to "Men's fashion",
                "Elettronica" to "Electronics",
                "Giochi & Videogiochi" to "Games & Videogames",
                "Automobilistica" to "Automotive")

            val subCatMap = mapOf(
                "Pittura, Disegno & Accessori" to "Painting, Drawing & Art Supplies",
                "Cucire" to "Sewing",
                "Foto & Stampa" to "Scrapbooking & Stamping",
                "Decorazioni & Accessori" to "Party Decorations & Supplies",
                "Attività all'aperto" to "Outdoor Recreation",
                "Sport & Fitness" to "Sports & Fitness",
                "Accessori per animali" to "Pet Supplies",
                "Abbigliamento & Accessori" to "Apparel & Accessories",
                "Giochi Bimbo & Neonato" to "Baby & Toddler Toys",
                "Sedili auto & Accessori" to "Car Seats & Accessories",
                "Gravidanza & Maternità" to "Pregnancy & Maternity",
                "Passeggini & Accessori" to "Strollers & Accessories",
                "Abbigliamento" to "Clothing",
                "Scarpe" to "Shoes",
                "Orologi" to "Watches",
                "Borse" to "Handbags",
                "Accessori" to "Accessories",
                "Computer" to "Computers",
                "Monitor" to "Monitors",
                "Stampanti & Scanner" to "Printers & Scanners",
                "Fotocamere & Foto" to "Camera & Photo",
                "Smartphone & Tablet" to "Smartphone & Tablet",
                "Audio" to "Audio",
                "Televisioni & Video" to "Television & Video",
                "Console & Videogiochi" to "Video Game Consoles",
                "Tecnologia Indossabile" to "Wearable Technology",
                "Accessori & Forniture" to "Accessories & Supplies",
                "Ferri da stiro & Micronde" to "Irons & Steamers",
                "Aspiratori & Accessori Casa" to "Vacuums & Floor Care",
                "Action Figures & Statue" to "Action Figures & Statues",
                "Arti & Mestieri" to "Arts & Crafts",
                "Giochi da Costruzione" to "Building Toys",
                "Bambole & Accessori" to "Dolls & Accessories",
                "Elettronica per Bambini" to "Kid's Electronics",
                "Giochi Educativi" to "Learning & Education",
                "Tricicli, Scooter & Treni" to "Tricycles, Scooters & Wagons",
                "Videogiochi" to "Videogames",
                "Elettronica & Accessories" to "Car Electronics & Accessories",
                "Accessori" to "Accessories",
                "Moto & Quad" to "Motorcycle & Powersports",
                "Pezzi di Ricambio" to "Replacement Parts",
                "Camper Accessories" to "RV Parts & Accessories",
                "Strumenti & Equipaggiamenti" to "Tools & Equipment"
            )

            val condMap = mapOf(
                "Nuovo" to "New",
                "Come nuovo" to "As new",
                "Buono" to "Good",
                "Ottimo" to "Optimal",
                "Accettabile" to "Acceptable"
            )

            val catMapIt = catMap.entries.associate{(k, v) -> v to k}
            val subCatMapIt = subCatMap.entries.associate { (k, v) -> v to k }
            val condMapIt = condMap.entries.associate{(k, v) -> v to k}


            if(!toIt) {
                if (lang != "italiano") {
                    localizedItem.subcategory = subCatMap.get(item.subcategory) ?: item.subcategory
                    localizedItem.category = catMap.get(item.category) ?: item.category
                    localizedItem.condition = condMap.get(item.condition) ?: item.condition

                    return localizedItem
                } else {

                    localizedItem.subcategory = subCatMapIt.get(item.subcategory) ?: item.subcategory
                    localizedItem.category = catMapIt.get(item.category) ?: item.category
                    localizedItem.condition = condMapIt.get(item.condition) ?: item.condition

                    return localizedItem
                }
            } else { // Force to Italian

                if(lang == "italiano"){
                    return localizedItem
                } else {
                    localizedItem.subcategory = subCatMapIt.get(item.subcategory) ?: item.subcategory
                    localizedItem.category = catMapIt.get(item.category) ?: item.category
                    localizedItem.condition = condMapIt.get(item.condition) ?: item.condition

                    return localizedItem
                }
            }
        }
    }

    enum class ItemState{
        AVAILABLE,
        BLOCKED,
        SOLD
    }
}