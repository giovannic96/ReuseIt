package it.polito.mad.mhackeroni

class ItemFilter(){
    var name :String = ""
    var price_min:Double = 0.0
    var price_max : Double = 9999.0
    var category:List<String> = listOf()
    var subcategory: List<String> = listOf()
    var location:String = ""
    var condition: List<String> = listOf()
    var user : String = ""

    public fun nameMatch(item : Item) : Boolean{
        return !name.isNullOrEmpty() && !item.name.isNullOrEmpty() && item.name.contains(name!!, true)
    }

    public fun priceMatch(item : Item) : Boolean{
        return item.price <= price_max && item.price >= price_min
    }

    public fun matchCategory(item : Item) : Boolean {
        return category!!.contains(item.category)
    }

    public fun matchSubcategory(item : Item) : Boolean {
        return  subcategory!!.contains(item.subcategory)
    }

    public fun matchLocation(item : Item) : Boolean {
        return item.location.contains(location!!, true)
    }

    public fun matchCondition(item : Item) : Boolean {
        return condition!!.contains(item.condition)
    }

    public fun matchUser(item: Item) : Boolean {
        return item.user == user
    }

    public fun match(item: Item) : Boolean{
        var retval = true


        // Check user
        if(!user.isNullOrEmpty() && !matchUser(item))
            return false

        // Check name constraint
        if(!name.isNullOrEmpty() && !nameMatch(item))
            return false

        // Check price constraint
        if(!priceMatch(item))
            return false

        // Check category and subcategory constraint
        if(category.isNotEmpty() && !matchCategory(item))
            return false

        if(subcategory.isNotEmpty() && !matchSubcategory(item))
            return false

        // Check location
        if(!location.isNullOrEmpty() && !matchLocation(item))
            return false

        // Check condition
        if(condition.isNotEmpty() && !matchCondition(item))
            return false

        return true
    }
}