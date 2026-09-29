package dev.orestegabo.sequo.data

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.data.*
import dev.orestegabo.sequo.domain.*
import dev.orestegabo.sequo.logic.*
import dev.orestegabo.sequo.model.*
import dev.orestegabo.sequo.theme.*
import dev.orestegabo.sequo.ui.account.*
import dev.orestegabo.sequo.ui.app.*
import dev.orestegabo.sequo.ui.basket.*
import dev.orestegabo.sequo.ui.catalog.*
import dev.orestegabo.sequo.ui.chrome.*
import dev.orestegabo.sequo.ui.components.*
import dev.orestegabo.sequo.ui.home.*
import dev.orestegabo.sequo.ui.markets.*
import dev.orestegabo.sequo.ui.orders.*
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.painterResource
import sequo.shared.generated.resources.*

internal val sequoShopTypes = listOf(
    SequoShopType("food", "Food", "Hot meals", Icons.Filled.LocalDining, SequoSecondary),
    SequoShopType("grocery", "Grocery", "Fresh & pantry", Icons.Filled.LocalGroceryStore, SequoPrimary),
    SequoShopType("fashion", "Fashion", "Clothes & shoes", Icons.Filled.Checkroom, Color(0xFF7B5EA7)),
    SequoShopType("beauty", "Beauty", "Perfume & makeup", Icons.Filled.Face, Color(0xFFA64F78)),
    SequoShopType("electronics", "Electronics", "Phones & tech", Icons.Filled.PhoneAndroid, Color(0xFF4D6F9E)),
    SequoShopType("tires", "Tires & auto", "Car care", Icons.Filled.Build, Color(0xFF805E48)),
    SequoShopType("pharmacy", "Pharmacy", "Care items", Icons.Filled.MedicalServices, Color(0xFF6C7E51)),
    SequoShopType("home", "Home & baby", "Daily basics", Icons.Filled.ShoppingBasket, Color(0xFFA77A41)),
)

internal val sequoShops = listOf(
    SequoShop(
        name = "Chez Ramatou Attieke",
        area = "Tokoin Gbadago",
        kind = "Food now",
        distanceKm = 1.8,
        eta = "22 min",
        photoStatus = "Hot meals",
        openStatus = "Open until 22:30",
        rating = "4.8",
        consolidation = "Packed warm",
        products = listOf(
            SequoProduct(
                name = "Attieke poisson braise",
                detail = "Choose fish size, piment, onion, alloco",
                priceCfa = 4200,
                label = "Food",
                optionHint = "Medium fish / piment doux / extra onion",
            ),
            SequoProduct(
                name = "Riz gras poulet",
                detail = "Sauce tomate, fried plantain, cold bissap",
                priceCfa = 3300,
                label = "Hot",
                optionHint = "No pepper / add bissap",
            ),
            SequoProduct(
                name = "Brochettes poulet",
                detail = "Grilled skewers, onion, piment",
                priceCfa = 2800,
                label = "Grill",
                optionHint = "4 pieces / spicy sauce",
            ),
            SequoProduct(
                name = "Yassa poulet",
                detail = "Onion sauce with couscous",
                priceCfa = 3800,
                label = "Food",
                optionHint = "Mild / extra onion",
            ),
            SequoProduct(
                name = "Pizza maison",
                detail = "Vegetable pizza, cold drink",
                priceCfa = 5200,
                label = "Food",
                optionHint = "Medium / sliced",
            ),
        ),
    ),
    SequoShop(
        name = "Grand Marche Assigame",
        area = "Assigame",
        kind = "Market cooperative",
        distanceKm = 3.1,
        eta = "45 min",
        photoStatus = "Fresh picks",
        openStatus = "Open now",
        rating = "4.6",
        consolidation = "Grouped order",
        products = listOf(
            SequoProduct(
                name = "Pagne wax 6 yards",
                detail = "Pattern preview, bargain available",
                priceCfa = 17500,
                label = "Bargain",
                optionHint = "Historical minimum: 15 000 CFA",
                bargainNote = "Try 15 500 CFA / 3 attempts",
            ),
            SequoProduct(
                name = "Tomato and onion basket",
                detail = "Fresh produce inspected at pickup",
                priceCfa = 5200,
                label = "Fresh",
                optionHint = "Family basket / today harvest",
            ),
            SequoProduct(
                name = "Banana bunch",
                detail = "Sweet ripe bananas",
                priceCfa = 1800,
                label = "Fresh",
                optionHint = "Small bunch / ready to eat",
            ),
            SequoProduct(
                name = "Fruit mix basket",
                detail = "Seasonal fruit selection",
                priceCfa = 4600,
                label = "Fresh",
                optionHint = "Banana / citrus / mango",
            ),
            SequoProduct(
                name = "Cherry snack pack",
                detail = "Small fruit pack",
                priceCfa = 2600,
                label = "Fresh",
                optionHint = "Washed / sealed cup",
            ),
        ),
    ),
    SequoShop(
        name = "Hedzranawoe Electronics",
        area = "Hedzranawoe",
        kind = "Electronics & phones",
        distanceKm = 5.6,
        eta = "Tomorrow",
        photoStatus = "Phones & laptops",
        openStatus = "Ships today",
        rating = "4.7",
        consolidation = "Sealed box",
        products = listOf(
            SequoProduct(
                name = "iPhone 15 rose",
                detail = "128 GB, clean finish",
                priceCfa = 420000,
                label = "Phone",
                optionHint = "Pink / sealed box",
            ),
            SequoProduct(
                name = "HP laptop 15",
                detail = "Everyday Windows laptop",
                priceCfa = 245000,
                label = "Bargain",
                optionHint = "8 GB RAM / 256 GB SSD",
                bargainNote = "Offer 230 000 CFA / 3 attempts",
            ),
            SequoProduct(
                name = "Dell Latitude",
                detail = "Work laptop, slim body",
                priceCfa = 310000,
                label = "Laptop",
                optionHint = "Core i5 / 512 GB SSD",
            ),
            SequoProduct(
                name = "iPhone 15 noir",
                detail = "128 GB, sealed box",
                priceCfa = 445000,
                label = "Phone",
                optionHint = "Black / 128 GB",
            ),
            SequoProduct(
                name = "Poco smartphone",
                detail = "Large screen Android phone",
                priceCfa = 155000,
                label = "Phone",
                optionHint = "8 GB RAM / 256 GB",
                bargainNote = "Offer 145 000 CFA / 3 attempts",
            ),
            SequoProduct(
                name = "Dell business laptop",
                detail = "Office laptop, metal body",
                priceCfa = 285000,
                label = "Laptop",
                optionHint = "Core i7 / 16 GB RAM",
            ),
        ),
    ),
    SequoShop(
        name = "Akodessewa Maison Service",
        area = "Akodessewa",
        kind = "Household essentials",
        distanceKm = 7.2,
        eta = "Today 18:10",
        photoStatus = "Daily basics",
        openStatus = "Open until 20:00",
        rating = "4.5",
        consolidation = "Route via Be-Kpota",
        products = listOf(
            SequoProduct(
                name = "Pack eau 1.5L x 6",
                detail = "Still water, family pack",
                priceCfa = 2400,
                label = "Water",
                optionHint = "Voltic / room temperature",
            ),
            SequoProduct(
                name = "Liquide vaisselle 1L",
                detail = "Lemon scent, sealed bottle",
                priceCfa = 1500,
                label = "Home",
                optionHint = "Lemon scent / sealed bottle",
            ),
            SequoProduct(
                name = "Savon lessive pack",
                detail = "Family laundry pack",
                priceCfa = 3200,
                label = "Home",
                optionHint = "6 bars / sealed pack",
            ),
            SequoProduct(
                name = "Papier cuisine",
                detail = "Two-roll kitchen paper",
                priceCfa = 2100,
                label = "Home",
                optionHint = "2 rolls / white",
            ),
        ),
    ),
    SequoShop(
        name = "Agbalepedo Pneus Express",
        area = "Agbalepedo",
        kind = "Tires & auto",
        distanceKm = 6.4,
        eta = "Today 17:40",
        photoStatus = "Auto parts",
        openStatus = "Open until 19:30",
        rating = "4.4",
        consolidation = "Garage pickup",
        products = listOf(
            SequoProduct(
                name = "Pneu neuf 185/65 R15",
                detail = "New tire, city driving",
                priceCfa = 38500,
                label = "Auto",
                optionHint = "185/65 R15 / road tire",
                bargainNote = "Historical minimum: 35 000 CFA",
            ),
            SequoProduct(
                name = "Roulement de roue",
                detail = "Front wheel bearing kit",
                priceCfa = 18500,
                label = "Auto",
                optionHint = "Front axle / standard kit",
            ),
            SequoProduct(
                name = "Demarreur voiture",
                detail = "Starter motor assembly",
                priceCfa = 62000,
                label = "Auto",
                optionHint = "12V / compact sedan",
            ),
            SequoProduct(
                name = "Kit carrosserie avant",
                detail = "Door, lamp, mirror set",
                priceCfa = 84000,
                label = "Auto",
                optionHint = "Front-left set",
            ),
            SequoProduct(
                name = "Pneus lot de 4",
                detail = "Four road tires",
                priceCfa = 148000,
                label = "Auto",
                optionHint = "Set of 4 / road tire",
                bargainNote = "Offer 138 000 CFA / 3 attempts",
            ),
        ),
    ),
    SequoShop(
        name = "Pharmacie du Golfe",
        area = "Be-Kpota",
        kind = "Pharmacy & care",
        distanceKm = 4.7,
        eta = "35 min",
        photoStatus = "Care items",
        openStatus = "Open until 23:00",
        rating = "4.7",
        consolidation = "Care items sealed",
        products = listOf(
            SequoProduct(
                name = "HydraFizz fraise",
                detail = "Effervescent hydration tube",
                priceCfa = 4800,
                label = "Care",
                optionHint = "Strawberry / 16 tablets",
            ),
            SequoProduct(
                name = "UPSA vitamine C",
                detail = "Orange sachets",
                priceCfa = 3500,
                label = "Care",
                optionHint = "Orange / 10 sachets",
            ),
            SequoProduct(
                name = "Paracetamol 500 mg",
                detail = "Pain and fever box",
                priceCfa = 1200,
                label = "Care",
                optionHint = "16 tablets / sealed box",
            ),
            SequoProduct(
                name = "UPSA booster mate",
                detail = "Energy tablets",
                priceCfa = 5200,
                label = "Care",
                optionHint = "20 tablets / lemon mate",
            ),
            SequoProduct(
                name = "Doliprane 1000 mg",
                detail = "Adult paracetamol box",
                priceCfa = 1800,
                label = "Care",
                optionHint = "8 tablets / sealed box",
            ),
        ),
    ),
    SequoShop(
        name = "Baguida Maison Express",
        area = "Baguida",
        kind = "Home goods",
        distanceKm = 9.1,
        eta = "Tomorrow",
        photoStatus = "Home basics",
        openStatus = "Ships today",
        rating = "4.3",
        consolidation = "Single package eligible",
        products = listOf(
            SequoProduct(
                name = "Ampoule LED 12W",
                detail = "White light bulb",
                priceCfa = 1800,
                label = "Home",
                optionHint = "White light / E27",
            ),
            SequoProduct(
                name = "Multiprise 4 ports",
                detail = "Four sockets with switch",
                priceCfa = 5500,
                label = "Home",
                optionHint = "Cable 1.5m / switch",
            ),
            SequoProduct(
                name = "Lampe rechargeable",
                detail = "Portable LED lamp",
                priceCfa = 8200,
                label = "Home",
                optionHint = "USB charge / white light",
            ),
            SequoProduct(
                name = "Boite rangement",
                detail = "Stackable home storage",
                priceCfa = 4200,
                label = "Home",
                optionHint = "Medium / clear lid",
            ),
        ),
    ),
    SequoShop(
        name = "Adidogome Sneaker House",
        area = "Adidogome",
        kind = "Fashion shoes",
        distanceKm = 6.8,
        eta = "Today 18:30",
        photoStatus = "Sneakers",
        openStatus = "Open until 21:00",
        rating = "4.6",
        consolidation = "Size check",
        products = listOf(
            SequoProduct("Adidas Campus green", "Suede low-top sneaker", 42000, "Shoes", "Size 42 / green"),
            SequoProduct("White running shoes", "Lightweight sport pair", 28500, "Shoes", "Size 41 / white"),
            SequoProduct("Monogram runner", "Black and white sneaker", 52000, "Shoes", "Size 43 / black"),
            SequoProduct("Canvas slip-on", "Soft casual shoe", 16500, "Shoes", "Size 42 / black"),
            SequoProduct("Tan low sneaker", "Leather-look court shoe", 36000, "Shoes", "Size 44 / tan", bargainNote = "Offer 32 000 CFA / 3 attempts"),
            SequoProduct("Brown comfort shoe", "Closed walking shoe", 24500, "Shoes", "Size 43 / brown"),
            SequoProduct("Black leather sneaker", "Clean low-top pair", 29500, "Shoes", "Size 42 / black"),
            SequoProduct("Gray casual sneaker", "Soft gray low-top", 26000, "Shoes", "Size 41 / gray"),
        ),
    ),
    SequoShop(
        name = "Tokoin Urban Wear",
        area = "Tokoin",
        kind = "Fashion clothes",
        distanceKm = 2.3,
        eta = "35 min",
        photoStatus = "Clothes",
        openStatus = "Open now",
        rating = "4.5",
        consolidation = "Folded pack",
        products = listOf(
            SequoProduct("Graphic black tee", "Printed cotton shirt", 8500, "Clothes", "Size M / black"),
            SequoProduct("White mountain tee", "Printed cotton shirt", 7500, "Clothes", "Size L / white"),
            SequoProduct("Plain black tee", "Everyday cotton shirt", 5500, "Clothes", "Size M / black"),
            SequoProduct("Plain white tee", "Clean cotton basic", 5500, "Clothes", "Size L / white"),
            SequoProduct("Green tee", "Soft cotton basic", 6500, "Clothes", "Size M / green"),
            SequoProduct("Blue office shirt", "Long-sleeve shirt", 14500, "Clothes", "Size L / blue"),
            SequoProduct("Striped shirt", "Long-sleeve striped shirt", 16000, "Clothes", "Size M / blue"),
        ),
    ),
    SequoShop(
        name = "Assigame Denim & Shorts",
        area = "Assigame",
        kind = "Fashion clothes",
        distanceKm = 3.4,
        eta = "45 min",
        photoStatus = "Denim",
        openStatus = "Open now",
        rating = "4.4",
        consolidation = "Folded pack",
        products = listOf(
            SequoProduct("Red chino shorts", "Knee-length shorts", 12500, "Clothes", "Size 32 / red"),
            SequoProduct("Rose chino shorts", "Casual shorts", 11500, "Clothes", "Size 34 / rose"),
            SequoProduct("Floral shorts", "Printed summer shorts", 13500, "Clothes", "Size 32 / blue"),
            SequoProduct("Denim shorts", "Light denim short", 13000, "Clothes", "Size 33 / blue"),
            SequoProduct("Blue straight jeans", "Classic denim jeans", 18500, "Clothes", "Size 32 / blue", bargainNote = "Offer 16 500 CFA / 3 attempts"),
            SequoProduct("Dark denim jeans", "Dark wash denim", 19500, "Clothes", "Size 34 / dark"),
            SequoProduct("Light relaxed jeans", "Light wash denim", 18000, "Clothes", "Size 32 / light blue"),
        ),
    ),
    SequoShop(
        name = "Nyekonakpoe Beauty Bar",
        area = "Nyekonakpoe",
        kind = "Beauty perfume",
        distanceKm = 4.2,
        eta = "Today 17:20",
        photoStatus = "Perfume",
        openStatus = "Open until 20:30",
        rating = "4.7",
        consolidation = "Fragile pack",
        products = listOf(
            SequoProduct("Saint Germain vanilla", "Warm vanilla perfume", 18500, "Perfume", "50ml / vanilla"),
            SequoProduct("Blue heel perfume", "Statement bottle", 22000, "Perfume", "80ml / floral"),
            SequoProduct("Dior Homme style", "Amber woody scent", 26000, "Perfume", "75ml / woody"),
            SequoProduct("Sauvage extrait", "Deep blue fragrance", 29500, "Perfume", "60ml / intense", bargainNote = "Offer 26 000 CFA / 3 attempts"),
            SequoProduct("L Interdit rouge", "Red floral scent", 24000, "Perfume", "50ml / floral"),
            SequoProduct("Nuxe Prodigieux", "Soft amber perfume", 16500, "Perfume", "50ml / amber"),
            SequoProduct("Scandal rouge", "Sweet evening scent", 23500, "Perfume", "50ml / sweet"),
        ),
    ),
    SequoShop(
        name = "Be Beaute Cosmetics",
        area = "Be",
        kind = "Beauty makeup",
        distanceKm = 3.8,
        eta = "40 min",
        photoStatus = "Makeup",
        openStatus = "Open now",
        rating = "4.6",
        consolidation = "Small pack",
        products = listOf(
            SequoProduct("Ace Beaute concealer", "Liquid concealer", 8500, "Makeup", "Shade medium"),
            SequoProduct("Lip palette set", "Lip balm and palette", 14500, "Makeup", "Pink set"),
            SequoProduct("Gold concealer stick", "Glow concealer", 9500, "Makeup", "Warm gold"),
            SequoProduct("NARS mini sticks", "Two-piece cheek set", 18500, "Makeup", "Blush duo"),
            SequoProduct("Lifter Glaze balm", "Soft pink lip balm", 7800, "Makeup", "Pink glaze"),
            SequoProduct("Liquid blush", "Soft rose blush", 9900, "Makeup", "Rose shade"),
        ),
    ),
    SequoShop(
        name = "Agoe Watches & Gifts",
        area = "Agoe",
        kind = "Fashion accessories",
        distanceKm = 8.4,
        eta = "Tomorrow",
        photoStatus = "Accessories",
        openStatus = "Ships today",
        rating = "4.4",
        consolidation = "Gift box",
        products = listOf(
            SequoProduct("Chronograph watch", "Leather strap watch", 38000, "Watch", "Black strap / silver case"),
            SequoProduct("Smart watch trio", "Rounded smart watch", 52000, "Watch", "Black / silver / cream"),
            SequoProduct("Black quartz watch", "Simple leather watch", 15500, "Watch", "Black strap"),
            SequoProduct("Casio blue dial", "Metal bracelet watch", 31500, "Watch", "Blue dial / steel"),
        ),
    ),
    SequoShop(
        name = "Baguida Socks & Basics",
        area = "Baguida",
        kind = "Fashion socks",
        distanceKm = 8.9,
        eta = "Tomorrow",
        photoStatus = "Socks",
        openStatus = "Ships today",
        rating = "4.3",
        consolidation = "Small pack",
        products = listOf(
            SequoProduct("Navy dress socks", "Classic dark socks", 2500, "Socks", "One pair / navy", subcategory = "Socks"),
            SequoProduct("Three-color socks", "Black, burgundy, beige", 5200, "Socks", "3 pairs / mixed", subcategory = "Socks"),
            SequoProduct("Office sock pack", "Blue and black socks", 6200, "Socks", "5 pairs / mixed", subcategory = "Socks"),
            SequoProduct("Blue argyle socks", "Pattern dress socks", 2800, "Socks", "One pair / blue", subcategory = "Socks"),
            SequoProduct("Black argyle socks", "Tall pattern socks", 3000, "Socks", "One pair / black", subcategory = "Socks"),
            SequoProduct("Wool knit socks", "Warm beige socks", 3500, "Socks", "One pair / wool", subcategory = "Socks"),
            SequoProduct("Sport sock pack", "Black gray white pack", 5800, "Socks", "6 pairs / sport", subcategory = "Socks"),
            SequoProduct("White crew socks", "Clean ribbed socks", 2200, "Socks", "One pair / white", subcategory = "Socks"),
            SequoProduct("Brown striped socks", "Warm striped socks", 3000, "Socks", "One pair / brown", subcategory = "Socks"),
            SequoProduct("Orange ribbed socks", "Bright cotton socks", 2600, "Socks", "One pair / orange", subcategory = "Socks"),
        ),
    ),
    SequoShop(
        name = "Lome Team Jerseys",
        area = "Tokoin",
        kind = "Fashion sportswear",
        distanceKm = 2.9,
        eta = "40 min",
        photoStatus = "Team kits",
        openStatus = "Open until 21:30",
        rating = "4.6",
        consolidation = "Folded pack",
        products = listOf(
            SequoProduct("Manchester blue jersey", "Short sleeve football shirt", 18500, "Jersey", "Size M / blue", subcategory = "Football jerseys"),
            SequoProduct("Manchester light jersey", "Light blue football shirt", 18000, "Jersey", "Size L / sky blue", subcategory = "Football jerseys"),
            SequoProduct("Manchester black jersey", "Black football shirt", 19000, "Jersey", "Size M / black", subcategory = "Football jerseys"),
            SequoProduct("Chelsea blue jersey", "Blue football shirt", 18500, "Jersey", "Size L / blue", subcategory = "Football jerseys"),
            SequoProduct("Arsenal red jersey", "Red football shirt", 18500, "Jersey", "Size M / red", subcategory = "Football jerseys"),
            SequoProduct("Arsenal yellow jersey", "Yellow football shirt", 17500, "Jersey", "Size M / yellow", subcategory = "Football jerseys"),
            SequoProduct("Togo yellow jersey", "National team shirt", 16000, "Jersey", "Size L / yellow", subcategory = "Football jerseys"),
            SequoProduct("Japan volleyball kit", "Top and shorts set", 21000, "Jersey", "Size M / red", subcategory = "Sportswear"),
        ),
    ),
    SequoShop(
        name = "Ablogame Court Gear",
        area = "Ablogame",
        kind = "Fashion sportswear",
        distanceKm = 5.7,
        eta = "Today 19:10",
        photoStatus = "Court gear",
        openStatus = "Open until 20:00",
        rating = "4.4",
        consolidation = "Sport pack",
        products = listOf(
            SequoProduct("BAL green jersey", "Sleeveless basketball jersey", 22000, "Jersey", "Size L / green", subcategory = "Basketball jerseys"),
            SequoProduct("BAL black jersey", "Sleeveless basketball jersey", 22000, "Jersey", "Size M / black", subcategory = "Basketball jerseys"),
            SequoProduct("Lakers yellow jersey", "Basketball jersey", 24000, "Jersey", "Size L / yellow", subcategory = "Basketball jerseys"),
            SequoProduct("Bulls red jersey", "Basketball jersey", 24000, "Jersey", "Size M / red", subcategory = "Basketball jerseys"),
            SequoProduct("Purple court shoe", "Basketball sneaker", 68000, "Shoes", "Size 42 / purple", subcategory = "Sports shoes"),
            SequoProduct("Pink court shoe", "Basketball sneaker", 64000, "Shoes", "Size 40 / pink", subcategory = "Sports shoes"),
            SequoProduct("Black court shoe", "Basketball sneaker", 61000, "Shoes", "Size 43 / black", subcategory = "Sports shoes"),
            SequoProduct("FIFA football", "Match-style football", 9500, "Sports", "Size 5 / white", subcategory = "Sporting goods"),
            SequoProduct("Volleyball", "Indoor/outdoor ball", 8500, "Sports", "Standard size", subcategory = "Sporting goods"),
            SequoProduct("American football", "Training ball", 10500, "Sports", "Brown / synthetic", subcategory = "Sporting goods"),
        ),
    ),
    SequoShop(
        name = "Ablogame Grocery",
        area = "Ablogame",
        kind = "Grocery pantry",
        distanceKm = 5.9,
        eta = "Today 19:00",
        photoStatus = "Pantry",
        openStatus = "Open until 21:00",
        rating = "4.6",
        consolidation = "Heavy bag",
        products = listOf(
            SequoProduct("Huile de colza 1L", "Cooking oil bottle", 3200, "Grocery", "1L / colza"),
            SequoProduct("Huile bio colza", "Organic cooking oil", 4800, "Grocery", "75cl / bio"),
            SequoProduct("Lait frais 1L", "Fresh milk bottle", 1900, "Grocery", "1L / chilled", originalPriceCfa = 2300, isCameraVerified = true, capturedAtLabel = "Taken today"),
            SequoProduct("Farine de ble 1kg", "Wheat flour bag", 1600, "Grocery", "1kg / T45"),
            SequoProduct("Farine artisanale", "Artisan flour bag", 2200, "Grocery", "1kg / bakery"),
            SequoProduct("Farine de manioc", "Cassava flour pouch", 1800, "Grocery", "250g / bio"),
            SequoProduct("Noix de cajou", "Cashew bowl", 3500, "Grocery", "250g / roasted"),
            SequoProduct("Noix de cajou vrac", "Bulk cashews", 4800, "Grocery", "500g / roasted", subcategory = "Pantry"),
            SequoProduct("Noix de coco", "Fresh coconut", 1200, "Grocery", "1 piece / fresh", subcategory = "Produce", isCameraVerified = true, capturedAtLabel = "Taken today"),
            SequoProduct("Haricots melanges", "Mixed dry beans", 2300, "Grocery", "1kg / dry", subcategory = "Pantry"),
            SequoProduct("Soja grains", "Soybean pack", 2100, "Grocery", "1kg / dry"),
        ),
    ),
    SequoShop(
        name = "Port Market Poissonnerie",
        area = "Kodjoviakope",
        kind = "Grocery seafood",
        distanceKm = 4.9,
        eta = "Today 16:50",
        photoStatus = "Seafood",
        openStatus = "Open now",
        rating = "4.5",
        consolidation = "Cold bag",
        products = listOf(
            SequoProduct("Saumon frais", "Fresh salmon tray", 16500, "Seafood", "500g / chilled"),
            SequoProduct("Paves de saumon", "Frozen salmon box", 14200, "Seafood", "500g / frozen"),
            SequoProduct("Tilapia frais", "Whole fresh tilapia", 3800, "Seafood", "1kg / cleaned", isCameraVerified = true, capturedAtLabel = "Taken 2h ago"),
            SequoProduct("Tilapia barquette", "Two fish pack", 4500, "Seafood", "2 pieces / chilled"),
            SequoProduct("Poisson blanc frais", "Fresh white fish", 5200, "Seafood", "1kg / cleaned"),
            SequoProduct("Steak de boeuf", "Fresh beef steak", 7200, "Meat", "500g / chilled", isCameraVerified = true, capturedAtLabel = "Taken 1h ago"),
            SequoProduct("Boeuf tranche", "Tender beef cuts", 8500, "Meat", "500g / premium"),
        ),
    ),
    SequoShop(
        name = "Agoe Fresh Garden",
        area = "Agoe",
        kind = "Grocery produce",
        distanceKm = 8.0,
        eta = "Tomorrow",
        photoStatus = "Produce",
        openStatus = "Ships today",
        rating = "4.3",
        consolidation = "Fresh crate",
        products = listOf(
            SequoProduct("Mais doux", "Fresh corn pair", 1500, "Produce", "2 pieces / fresh"),
            SequoProduct("Sorgho rouge", "Dry sorghum grain", 2400, "Produce", "1kg / dry"),
            SequoProduct("Petits pois", "Green peas", 2800, "Produce", "500g / fresh"),
            SequoProduct("Haricots verts", "Fresh green beans", 2200, "Produce", "500g / fresh"),
            SequoProduct("Taro frais", "Fresh taro roots", 2600, "Produce", "1kg / roots"),
            SequoProduct("Patate douce", "Sweet potatoes", 1900, "Produce", "1kg / orange"),
            SequoProduct("Pommes de terre", "Potato pack", 1800, "Produce", "1kg / yellow"),
            SequoProduct("Carottes", "Fresh carrots", 1500, "Produce", "1kg / orange"),
            SequoProduct("Poivron vert", "Green pepper", 1200, "Produce", "500g / green", isCameraVerified = true, capturedAtLabel = "Taken today"),
            SequoProduct("Aubergine", "Purple eggplant", 1300, "Produce", "500g / purple"),
            SequoProduct("Tomates grappe", "Vine tomatoes", 1700, "Produce", "1kg / red"),
            SequoProduct("Tomates rouges", "Tomato cluster", 1600, "Produce", "1kg / red"),
        ),
    ),
    SequoShop(
        name = "Be Family Essentials",
        area = "Be",
        kind = "Home baby essentials",
        distanceKm = 3.7,
        eta = "Today 18:45",
        photoStatus = "Family basics",
        openStatus = "Open until 20:00",
        rating = "4.5",
        consolidation = "Bulky pack",
        products = listOf(
            SequoProduct("Papier toilette classic", "Large toilet paper pack", 9500, "Home", "36 rolls / classic"),
            SequoProduct("Papier toilette soft", "Soft toilet paper pack", 8900, "Home", "24 rolls / soft"),
            SequoProduct("Papier bambou", "Bamboo toilet paper", 3200, "Home", "4 rolls / bamboo"),
            SequoProduct("Pampers new baby", "Newborn diapers", 7800, "Baby", "Size 2 / 31 pieces"),
            SequoProduct("Pampers active baby", "Active baby diapers", 13500, "Baby", "Size 6 / 56 pieces", originalPriceCfa = 15800, isCameraVerified = true, capturedAtLabel = "Taken today"),
            SequoProduct("Mir vaisselle pomme", "Dish soap bottle", 1800, "Soap", "675ml / apple", subcategory = "Soaps"),
            SequoProduct("OMO washing liquid", "Laundry liquid", 6200, "Soap", "3L / white laundry", subcategory = "Soaps"),
            SequoProduct("Ajax lavender floor", "Floor cleaner", 2600, "Soap", "1.25L / lavender", subcategory = "Cleaners", originalPriceCfa = 3100, isCameraVerified = true, capturedAtLabel = "Taken today"),
            SequoProduct("Sanytol disinfectant", "Multi-surface cleaner", 3200, "Soap", "1L / disinfectant", subcategory = "Cleaners"),
            SequoProduct("Ajax window cleaner", "Glass cleaner refill", 1900, "Soap", "750ml / blue", subcategory = "Cleaners"),
            SequoProduct("Sanytol bathroom spray", "Bathroom cleaner", 2800, "Soap", "500ml / spray", subcategory = "Cleaners"),
            SequoProduct("Harpic toilet cleaner", "Toilet cleaner", 2300, "Soap", "750ml / original", subcategory = "Cleaners"),
        ),
    ),
)

internal val sequoBasket = listOf(
    BasketEntry(sequoShops[0], sequoShops[0].products[0], 1),
    BasketEntry(sequoShops[1], sequoShops[1].products[0], 1),
    BasketEntry(sequoShops[3], sequoShops[3].products[0], 2),
)

internal val recentOrders = listOf(
    SequoOrder(
        id = "SQ-2419",
        sellers = listOf("Chez Ramatou Attieke"),
        items = listOf(OrderItem("Attieke poisson braise", 1), OrderItem("Bissap frais", 2)),
        state = SequoOrderState.InDelivery,
        dateLine = "Arriving in 9 min",
        note = "On the way to Etoiles.",
        amountCfa = 5600,
        paymentMethod = "Yas Togo",
        pickupCode = pickupCodeFor("SQ-2419"),
    ),
    SequoOrder(
        id = "SQ-2418",
        sellers = listOf("Pharmacie du Golfe", "Akodessewa Maison Service"),
        items = listOf(OrderItem("Thermometre digital", 1), OrderItem("Eau minerale 1.5L", 2)),
        state = SequoOrderState.PickedUp,
        dateLine = "Picked up 12:44",
        note = "Grouped at Sequo.",
        amountCfa = 9100,
        paymentMethod = "Moov Africa",
        pickupCode = pickupCodeFor("SQ-2418"),
    ),
    SequoOrder(
        id = "SQ-2417",
        sellers = listOf("Grand Marche Assigame"),
        items = listOf(OrderItem("Tomato and onion basket", 1), OrderItem("Pagne wax 6 yards", 1)),
        state = SequoOrderState.ReadyForPickup,
        dateLine = "Ready since 12:31",
        note = "Rider assignment next.",
        amountCfa = 22700,
        paymentMethod = "Yas Togo",
        pickupCode = pickupCodeFor("SQ-2417"),
    ),
    SequoOrder(
        id = "SQ-2416",
        sellers = listOf("Hedzranawoe Electronics"),
        items = listOf(OrderItem("Tecno Spark 20 case", 1), OrderItem("Oraimo charger 20W", 1)),
        state = SequoOrderState.Preparing,
        dateLine = "Preparing now",
        note = "Seller is preparing.",
        amountCfa = 10900,
        paymentMethod = "Moov Africa",
        pickupCode = pickupCodeFor("SQ-2416"),
    ),
    SequoOrder(
        id = "SQ-2415",
        sellers = listOf("Be-Kpota Superette"),
        items = listOf(OrderItem("Pack eau minerale", 1)),
        state = SequoOrderState.MerchantAccepted,
        dateLine = "Accepted 12:09",
        note = "Seller accepted.",
        amountCfa = 3900,
        paymentMethod = "Yas Togo",
        pickupCode = pickupCodeFor("SQ-2415"),
    ),
    SequoOrder(
        id = "SQ-2414",
        sellers = listOf("Adidogome Bazar"),
        items = listOf(OrderItem("Lampe rechargeable", 1)),
        state = SequoOrderState.Paid,
        dateLine = "Paid 11:58",
        note = "Waiting on seller.",
        amountCfa = 8200,
        paymentMethod = "Moov Africa",
        pickupCode = pickupCodeFor("SQ-2414"),
    ),
    SequoOrder(
        id = "SQ-2408",
        sellers = listOf("Grand Marche Assigame", "Hedzranawoe Electronics"),
        items = listOf(OrderItem("Pagne wax 6 yards", 1), OrderItem("Oraimo charger 20W", 1)),
        state = SequoOrderState.Delivered,
        dateLine = "Delivered yesterday 18:40",
        note = "Return open until tomorrow.",
        amountCfa = 27000,
        paymentMethod = "Yas Togo",
        pickupCode = pickupCodeFor("SQ-2408"),
    ),
    SequoOrder(
        id = "SQ-2397",
        sellers = listOf("Chez Ramatou Attieke"),
        items = listOf(OrderItem("Attieke poisson braise", 1)),
        state = SequoOrderState.Delivered,
        dateLine = "Delivered Friday 13:05",
        note = "Food order closed after delivery confirmation.",
        amountCfa = 4600,
        paymentMethod = "Yas Togo",
        pickupCode = pickupCodeFor("SQ-2397"),
    ),
    SequoOrder(
        id = "SQ-2388",
        sellers = listOf("Pharmacie du Golfe"),
        items = listOf(OrderItem("Thermometre digital", 1)),
        state = SequoOrderState.ReturnInInspection,
        dateLine = "Dropped at Point de Relai today",
        note = "Sequo inspection pending before refund is released.",
        amountCfa = 7100,
        paymentMethod = "Moov Africa",
        pickupCode = pickupCodeFor("SQ-2388"),
    ),
    SequoOrder(
        id = "SQ-2374",
        sellers = listOf("Hedzranawoe Electronics"),
        items = listOf(OrderItem("Tecno Spark 20 case", 1)),
        state = SequoOrderState.RefundIssued,
        dateLine = "Return accepted Monday",
        note = "Refund sent back to Moov Africa after inspection.",
        amountCfa = 3900,
        paymentMethod = "Moov Africa",
        pickupCode = pickupCodeFor("SQ-2374"),
    ),
    SequoOrder(
        id = "SQ-2360",
        sellers = listOf("Grand Marche Assigame"),
        items = listOf(OrderItem("Tomato and onion basket", 1), OrderItem("Pagne wax 6 yards", 1)),
        state = SequoOrderState.CancelledBySequo,
        dateLine = "Cancelled before pickup",
        note = "Seller could not confirm availability.",
        amountCfa = 22700,
        paymentMethod = "Yas Togo",
        pickupCode = pickupCodeFor("SQ-2360"),
    ),
)

internal fun shopTypeFor(key: String): SequoShopType =
    sequoShopTypes.firstOrNull { it.key == key } ?: sequoShopTypes.first()

internal fun shopsForType(typeKey: String): List<SequoShop> =
    when (typeKey) {
        "food" -> sequoShops.filter { it.kind.contains("Food", ignoreCase = true) }
        "grocery" -> sequoShops.filter {
            it.kind.contains("Market", ignoreCase = true) ||
                it.kind.contains("Grocery", ignoreCase = true) ||
                it.kind.contains("produce", ignoreCase = true) ||
                it.kind.contains("pantry", ignoreCase = true) ||
                it.kind.contains("seafood", ignoreCase = true)
        }
        "fashion" -> sequoShops.filter { it.kind.contains("Fashion", ignoreCase = true) }
        "beauty" -> sequoShops.filter { it.kind.contains("Beauty", ignoreCase = true) }
        "electronics" -> sequoShops.filter { it.kind.contains("electronics", ignoreCase = true) || it.kind.contains("Phone", ignoreCase = true) }
        "tires" -> sequoShops.filter { it.kind.contains("Tires", ignoreCase = true) || it.kind.contains("auto", ignoreCase = true) }
        "pharmacy" -> sequoShops.filter { it.kind.contains("Pharmacy", ignoreCase = true) || it.kind.contains("care", ignoreCase = true) }
        "home" -> sequoShops.filter {
            it.kind.contains("Home", ignoreCase = true) ||
                it.kind.contains("household", ignoreCase = true) ||
                it.kind.contains("baby", ignoreCase = true) ||
                it.kind.contains("essentials", ignoreCase = true) ||
                it.kind.contains("cleaning", ignoreCase = true)
        }
        "bargains" -> sequoShops.filter { shop -> shop.products.any { it.isNegotiable } }
        else -> sequoShops
    }

internal fun featuredProductsFor(typeKey: String): List<Pair<SequoShop, SequoProduct>> {
    val pairs = if (typeKey == "bargains") {
        sequoShops.flatMap { shop ->
            shop.products.filter { it.isNegotiable }.map { product -> shop to product }
        }
    } else {
        shopsForType(typeKey).flatMap { shop ->
            shop.products.map { product -> shop to product }
        }
    }
    return pairs.take(4)
}

internal val sequoPromos: List<SequoPromo> = listOfNotNull(
    promoFor(
        productName = "Attieke poisson braise",
        headline = "Lunch hour",
        title = "Hot food near you",
        subtitle = "Meal-time picks can lead the carousel before any category is selected.",
    ),
    promoFor(
        productName = "Pampers active baby",
        headline = "Family basics",
        title = "Baby essentials",
        subtitle = "Home campaigns can promote Pampers, paper goods, and cleaning bundles.",
    ),
    promoFor(
        productName = "Mir vaisselle pomme",
        headline = "Weekend reset",
        title = "Cleaning deals",
        subtitle = "Push household products when it is time for restock or chores.",
    ),
    promoFor(
        productName = "Noix de coco",
        headline = "Fresh today",
        title = "Market picks",
        subtitle = "Seasonal grocery promos can run without changing the selected shelf.",
    ),
    promoFor(
        productName = "Sauvage extrait",
        headline = "Christmas picks",
        title = "Gift-ready finds",
        subtitle = "Holiday campaigns can promote perfumes, fashion, tech, and bundles.",
    ),
)

private fun promoFor(
    productName: String,
    headline: String,
    title: String,
    subtitle: String,
): SequoPromo? {
    val match = sequoShops.firstNotNullOfOrNull { shop ->
        shop.products.firstOrNull { it.name == productName }?.let { product -> shop to product }
    } ?: return null
    return SequoPromo(
        headline = headline,
        title = title,
        subtitle = subtitle,
        shop = match.first,
        product = match.second,
    )
}

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
        "tee" in text || "t-shirt" in text || "shirt" in text && "office" !in text && "striped" !in text -> "T-shirts"
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
