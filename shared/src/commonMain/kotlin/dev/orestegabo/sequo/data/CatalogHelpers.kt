package dev.orestegabo.sequo.data

import dev.orestegabo.sequo.model.SequoProduct
import dev.orestegabo.sequo.model.SequoShop

internal fun productSubcategory(product: SequoProduct): String {
    if (product.subcategory.isNotBlank()) return product.subcategory

    val text = "${product.name} ${product.label} ${product.detail}".lowercase()
    return when {
        "pampers" in text || "diaper" in text -> "Baby care"
        "soap" in text || "vaisselle" in text || "washing" in text || "cleaner" in text || "ajax" in text || "sanytol" in text || "harpic" in text -> "Soaps & cleaners"
        "toilet paper" in text || "papier toilette" in text || "papier bambou" in text -> "Paper goods"
        "socks" in text -> "Socks"
        "jersey" in text || "football shirt" in text -> "Football jerseys"
        "basketball" in text -> "Basketball jerseys"
        "shoe" in text || "sneaker" in text -> "Shoes"
        "tee" in text || "t-shirt" in text || ("shirt" in text && "office" !in text && "striped" !in text) -> "T-shirts"
        "office shirt" in text || "striped shirt" in text -> "Shirts"
        "shorts" in text -> "Shorts"
        "jeans" in text || "denim" in text -> "Denim"
        "watch" in text -> "Watches"
        "perfume" in text || "scent" in text -> "Perfume"
        "concealer" in text || "blush" in text || "lip" in text || "makeup" in text -> "Makeup"
        "phone" in text || "iphone" in text -> "Phones"
        "laptop" in text -> "Laptops"
        "pneu" in text || "tire" in text || "roulement" in text || "demarreur" in text || "carrosserie" in text -> "Auto parts"
        "saumon" in text || "tilapia" in text || "poisson" in text -> "Seafood"
        "boeuf" in text || "beef" in text || "steak" in text -> "Meat"
        "haricots melanges" in text || "mixed dry beans" in text || "beans mix" in text -> "Pantry"
        "coco" in text || "coconut" in text -> "Produce"
        "tomate" in text || "carotte" in text || "poivron" in text || "aubergine" in text || "taro" in text || "patate" in text || "mais" in text || "haricots" in text || "peas" in text -> "Produce"
        "farine" in text || "huile" in text || "lait" in text || "cajou" in text || "soja" in text -> "Pantry"
        else -> product.label
    }
}

internal fun productSubcategoriesFor(shops: List<SequoShop>): List<String> =
    shops.flatMap { shop -> shop.products.map(::productSubcategory) }
        .distinct()
        .sorted()
