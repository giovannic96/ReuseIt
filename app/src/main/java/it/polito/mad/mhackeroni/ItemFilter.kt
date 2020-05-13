package it.polito.mad.mhackeroni

import android.util.Log

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
        return !name.isNullOrEmpty() && !item.name.isNullOrEmpty() && item.name.contains(name!!, true)
    }

    fun priceMatch(item : Item) : Boolean{
        return item.price <= price_max && item.price >= price_min
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
        if(!user.isNullOrEmpty() && !matchUser(item)) {
            return false
        }

        // Check name constraint
        if(!name.isNullOrEmpty() && !nameMatch(item)) {
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
        if(!location.isNullOrEmpty() && !matchLocation(item)){
            return false
        }


        // Check condition
        if(condition.isNotEmpty() && !matchCondition(item)){
            return false
        }

        return true
    }
}