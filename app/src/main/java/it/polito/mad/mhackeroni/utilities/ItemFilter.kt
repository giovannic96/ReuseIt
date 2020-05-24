package it.polito.mad.mhackeroni.utilities

import it.polito.mad.mhackeroni.model.Item
import java.io.Serializable

class ItemFilter(){
    var name :String = ""
    var price_min:Double = 0.0
    var price_max : Double = 9999.0
    var category:MutableList<String> = mutableListOf()
    var subcategory: MutableList<String> = mutableListOf()
    var location:String = ""
    var condition: MutableList<String> = mutableListOf()
    var user : String = ""

    fun nameMatch(item : Item) : Boolean{
        return !name.isEmpty() && !item.name.isEmpty() && item.name.contains(name, true)
    }

    fun priceMatch(item : Item) : Boolean{
        return item.price in price_min..price_max
    }

     fun matchCategory(item : Item) : Boolean {
        return category.contains(item.category)
    }

    fun matchSubcategory(item : Item) : Boolean {
        return  subcategory.contains(item.subcategory)
    }

    fun matchLocation(item : Item) : Boolean {
        return item.location.contains(location, true)
    }

    fun matchCondition(item : Item) : Boolean {
        return condition.contains(item.condition)
    }

    fun matchUser(item: Item) : Boolean {
        return item.user == user
    }

    fun match(item: Item) : Boolean{

        // Check user
        if(!user.isEmpty() && !matchUser(item)) {
            return false
        }

        // Check name constraint
        if(!name.isEmpty() && !nameMatch(item)) {
            return false
        }

        // Check price constraint
        if(!priceMatch(item)){
            return false
        }

        // Check category and subcategory constraint
        if(category.isNotEmpty() && !matchCategory(item)){
            return false
        }


        if(subcategory.isNotEmpty() && !matchSubcategory(item)){
            return false
        }


        // Check location
        if(!location.isEmpty() && !matchLocation(item)){
            return false
        }


        // Check condition
        if(condition.isNotEmpty() && !matchCondition(item)){
            return false
        }

        return true
    }
}